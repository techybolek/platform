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

package org.wso2.carbon.event.builder.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.builder.core.internal.config.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.ConfigurationValidator;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import javax.xml.namespace.QName;
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
 * Deploy event builders as axis2 service
 */
public class EventBuilderDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(EventBuilderDeployer.class);
    private ConfigurationContext configurationContext;
    private List<String> deployedEventBuilderFilePaths = new ArrayList<String>();
    private List<String> undeployedEventBuilderFilePaths = new ArrayList<String>();

    @Override
    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the event builder configuration file, create it and deploy it
     *
     * @param deploymentFileData information about the event builder
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        if (!deployedEventBuilderFilePaths.contains(path)) {
            try {
                processDeployment(deploymentFileData);
            } catch (Throwable e) {
                String errorMsg = "Event builder not deployed and in inactive state :'" + deploymentFileData.getFile().getName();
                log.error(errorMsg, e);
                throw new DeploymentException(errorMsg, e);
            }
        } else {
            deployedEventBuilderFilePaths.remove(path);
        }
    }

    private OMElement getEbConfigOMElement(File ebConfigFile) throws DeploymentException {
        OMElement ebConfigElement = null;
        BufferedInputStream inputStream = null;
        String path = ebConfigFile.getPath();
        try {
            inputStream = new BufferedInputStream(new FileInputStream(ebConfigFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            ebConfigElement = builder.getDocumentElement();
            ebConfigElement.build();
        } catch (FileNotFoundException e) {
            String errorMessage = ebConfigFile.getName() + " cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + ebConfigFile.getName() + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Error parsing configuration syntax : " + ebConfigFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Cannot close the input stream";
                log.error(errorMessage, e);
            }
        }

        return ebConfigElement;
    }

    @Override
    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed event builder configuration file
     *
     * @param filePath the filePath to the EventBuilderConfiguration file to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    @Override
    public void undeploy(String filePath) throws DeploymentException {
        if (!undeployedEventBuilderFilePaths.contains(filePath)) {
            try {
                processUndeployment(filePath);
            } catch (Throwable e) {
                String errorMsg = "Event builder file is not deployed : " + new File(filePath).getName();
                log.error(errorMsg + ":" + e.getMessage(), e);
                throw new DeploymentException(errorMsg, e);
            }
        } else {
            undeployedEventBuilderFilePaths.remove(filePath);
        }
    }

    public void setDirectory(String directory) {

    }

    public void processDeployment(DeploymentFileData deploymentFileData)
            throws DeploymentException, EventBuilderConfigurationException {

        File ebConfigXmlFile = deploymentFileData.getFile();
        String path = deploymentFileData.getAbsolutePath();
        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        String eventBuilderName = EventBuilderUtil.deriveEventBuilderNameFrom(ebConfigXmlFile.getName());
        String streamNameWithVersion = null;
        AxisConfiguration currentAxisConfiguration = configurationContext.getAxisConfiguration();
        OMElement ebConfigOMElement = null;
        try {
            ebConfigOMElement = getEbConfigOMElement(ebConfigXmlFile);
            eventBuilderName = ebConfigOMElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
            String inputMappingType = EventBuilderConfigHelper.getInputMappingType(ebConfigOMElement);
            EventBuilderConfigBuilder eventBuilderConfigBuilder = EventBuilderConfigHelper.getEventBuilderConfigBuilder(inputMappingType);
            EventBuilderConfiguration eventBuilderConfiguration = eventBuilderConfigBuilder.getEventBuilderConfiguration(ebConfigOMElement, tenantId, inputMappingType);

            if (eventBuilderConfiguration != null) {
                streamNameWithVersion = eventBuilderConfiguration.getToStreamName() + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + eventBuilderConfiguration.getToStreamVersion();
                if (ConfigurationValidator.isValidEventBuilderConfiguration(eventBuilderConfiguration, tenantId)) {
                    carbonEventBuilderService.addEventBuilder(eventBuilderConfiguration, configurationContext.getAxisConfiguration());
                    carbonEventBuilderService.addEventBuilderConfigurationFile(eventBuilderName, path, DeploymentStatus.DEPLOYED, eventBuilderName + " successfully deployed.", null, streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
                    List<EventBuilderNotificationListener> eventBuilderNotificationListeners = carbonEventBuilderService.getEventBuilderNotificationListeners();
                    if (eventBuilderNotificationListeners != null) {
                        for (EventBuilderNotificationListener eventBuilderNotificationListener : eventBuilderNotificationListeners) {
                            eventBuilderNotificationListener.configurationAdded(tenantId, streamNameWithVersion);
                        }
                    }
                    log.info("Event Builder deployed successfully and in active state : " + eventBuilderName);
                } else {
                    carbonEventBuilderService.addEventBuilderConfigurationFile(eventBuilderName, path, DeploymentStatus.WAITING_FOR_DEPENDENCY,
                                                                               "Transport Adaptor Configuration is not found", eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName(), streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
                }
            }
        } catch (EventBuilderValidationException e) {
            carbonEventBuilderService.addEventBuilderConfigurationFile(eventBuilderName, path, DeploymentStatus.WAITING_FOR_DEPENDENCY,
                                                                       "Dependency not loaded", e.getDependency(), streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
            log.info("Event builder deployment held back and in inactive state :" + eventBuilderName + ", Waiting for Input Transport Adaptor dependency :" + e.getDependency());
        } catch (EventBuilderConfigurationException e) {
            carbonEventBuilderService.addEventBuilderConfigurationFile(eventBuilderName, path, DeploymentStatus.ERROR,
                                                                       "Exception when deploying event builder configuration file:\n" + e.getMessage(), null, streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
            throw e;
        } catch (Throwable e) {
            log.error("Invalid configuration in event builder configuration file :" + ebConfigXmlFile.getName(), e);
            carbonEventBuilderService.addEventBuilderConfigurationFile(eventBuilderName, path, DeploymentStatus.ERROR,
                                                                       "Deployment exception: " + e.getMessage(), null, streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
            throw new DeploymentException(e);
        }
    }

    public void processUndeployment(String filePath) {
        File file = new File(filePath);
        int tenantId = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        if (carbonEventBuilderService.isEventBuilderConfigurationFileRegistered(filePath, tenantId)) {
            carbonEventBuilderService.removeEventBuilderConfigurationFile(filePath, tenantId);
            log.info("Event builder undeployed successfully :" + file.getName());
        }
    }

    public void executeManualDeployment(DeploymentFileData deploymentFileData)
            throws EventBuilderConfigurationException {
        try {
            deployedEventBuilderFilePaths.add(deploymentFileData.getAbsolutePath());
            processDeployment(deploymentFileData);
        } catch (DeploymentException e) {
            throw new EventBuilderConfigurationException("Error while attempting manual deployment :" + e.getMessage(), e);
        }
    }

    public void executeManualUndeployment(String filePath) {
        undeployedEventBuilderFilePaths.add(filePath);
        processUndeployment(filePath);
    }

}


