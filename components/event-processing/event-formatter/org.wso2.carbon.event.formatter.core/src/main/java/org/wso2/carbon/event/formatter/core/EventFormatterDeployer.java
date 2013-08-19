/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.formatter.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.internal.util.FormatterConfigurationBuilder;
import org.wso2.carbon.event.formatter.core.internal.util.helper.EventFormatterConfigurationHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deploy event formatter as axis2 service
 */
public class EventFormatterDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(EventFormatterDeployer.class);
    private ConfigurationContext configurationContext;

    private List<String> deployedEventFormatterFilePaths = new ArrayList<String>();
    private List<String> undeployedEventFormatterFilePaths = new ArrayList<String>();

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the event formatter file, create it and deploy it
     *
     * @param deploymentFileData information about the event formatter
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        if (!deployedEventFormatterFilePaths.contains(path)) {
            try {
                processDeploy(path);
            } catch (EventFormatterConfigurationException e) {
                throw new DeploymentException("Event formatter file " + path.substring(path.lastIndexOf('/') + 1, path.length()) + " is not deployed ", e);
            }
        } else {
            deployedEventFormatterFilePaths.remove(path);
        }
    }

    public OMElement getEventFormatterOMElement(String filePath,
                                                File eventFormatterFile)
            throws DeploymentException {
        String fileName = new File(filePath).getName();
        OMElement eventFormatterElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(eventFormatterFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            eventFormatterElement = builder.getDocumentElement();
            eventFormatterElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " .xml file cannot be found in the path : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + eventFormatterFile.getName() + " located in the path : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (OMException e) {
            String errorMessage = "XML tags are not properly closed " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
        return eventFormatterElement;
    }

    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed bucket
     *
     * @param filePath the path to the bucket to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String filePath) throws DeploymentException {


        if (!undeployedEventFormatterFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        } else {
            undeployedEventFormatterFilePaths.remove(filePath);
        }

    }

    public void processDeploy(String path)
            throws DeploymentException, EventFormatterConfigurationException {

        File eventFormatterFile = new File(path);
        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        String eventFormatterName = "";
        try {
            OMElement eventFormatterOMElement = getEventFormatterOMElement(path, eventFormatterFile);

            if (!(eventFormatterOMElement.getQName().getLocalPart()).equals(EventFormatterConstants.EF_ELE_ROOT_ELEMENT)) {
                throw new DeploymentException("Invalid root element " + eventFormatterOMElement.getQName() + " in " + eventFormatterFile.getName());
            }

            EventFormatterConfigurationHelper.validateEventFormatterConfiguration(eventFormatterOMElement);
            String mappingType = EventFormatterConfigurationHelper.getOutputMappingType(eventFormatterOMElement);
            if (mappingType != null) {
                mappingType = mappingType.toLowerCase();
                EventFormatterConfiguration eventFormatterConfiguration = FormatterConfigurationBuilder.getEventFormatterConfiguration(eventFormatterOMElement, tenantId, mappingType);
                eventFormatterName = eventFormatterConfiguration.getEventFormatterName();
                if (carbonEventFormatterService.checkEventFormatterValidity(tenantId, eventFormatterName)) {
                    carbonEventFormatterService.addEventFormatterConfiguration(tenantId, eventFormatterConfiguration);
                    carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId, createEventFormatterConfigurationFile(eventFormatterName, path, EventFormatterConfigurationFile.Status.DEPLOYED, configurationContext.getAxisConfiguration(), null, null));
                    log.info("Event Formatter configuration successfully deployed and in active state : " + eventFormatterName);
                } else {
                    throw new EventFormatterConfigurationException("Event Formatter not deployed and in inactive state, since there is a event formatter registered with the same name in this tenant :" + eventFormatterFile.getName());
                }
            } else {
                throw new EventFormatterConfigurationException("Event Formatter not deployed and in inactive state, since not contain a proper mapping type : "+ eventFormatterFile.getName());
            }
        } catch (EventFormatterConfigurationException ex) {
            log.error("Event Formatter not deployed and in inactive state, "+ex.getMessage(),ex);
            carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId, createEventFormatterConfigurationFile(eventFormatterName, path, EventFormatterConfigurationFile.Status.ERROR, null, null, null));
            throw new EventFormatterConfigurationException(ex.getMessage(), ex);
        } catch (EventFormatterValidationException ex) {
            carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId, createEventFormatterConfigurationFile(eventFormatterName, path, EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY, configurationContext.getAxisConfiguration(), "Dependency not loaded", ex.getDependency()));
            log.info("Event Formatter deployment held back and in inactive state : " + eventFormatterFile.getName()+ ", waiting for dependency : "+ex.getDependency());
        } catch (DeploymentException e) {
            log.error("Event Formatter not deployed and in inactive state : "+eventFormatterFile.getName() +" , " +e.getMessage(),e);
            carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId, createEventFormatterConfigurationFile(eventFormatterName, path, EventFormatterConfigurationFile.Status.ERROR, null, "Deployment exception occurred", null));
            throw new EventFormatterConfigurationException(e.getMessage(), e);
        }
    }

    public void processUndeploy(String filePath) {

        String fileName = new File(filePath).getName();
        log.info("Event Formatter undeployed successfully : "+ fileName );
        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();
        carbonEventFormatterService.removeEventFormatterConfigurationFromMap(filePath, tenantID);
    }

    public void setDirectory(String directory) {

    }

    public void addToDeployedEventFormatterFiles(String filePath) {
        deployedEventFormatterFilePaths.add(filePath);
    }

    public void removeFromDeployedEventFormatterFiles(String filePath) {
        undeployedEventFormatterFilePaths.add(filePath);
    }

    private EventFormatterConfigurationFile createEventFormatterConfigurationFile(
            String eventFormatterName,
            String filePath,
            EventFormatterConfigurationFile.Status status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage,
            String dependency) {
        EventFormatterConfigurationFile eventFormatterConfigurationFile = new EventFormatterConfigurationFile();
        eventFormatterConfigurationFile.setFilePath(filePath);
        eventFormatterConfigurationFile.setEventFormatterName(eventFormatterName);
        eventFormatterConfigurationFile.setStatus(status);
        eventFormatterConfigurationFile.setDependency(dependency);
        eventFormatterConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventFormatterConfigurationFile.setAxisConfiguration(axisConfiguration);

        return eventFormatterConfigurationFile;
    }
}


