/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.output.transport.adaptor.manager.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.CarbonOutputTransportAdaptorManagerService;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.ds.OutputTransportAdaptorManagerValueHolder;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.util.OutputTransportAdaptorManagerConstants;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.util.helper.OutputTransportAdaptorConfigurationHelper;

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
import java.util.Iterator;
import java.util.List;

/**
 * Deploy transport adaptors as axis2 service
 */
@SuppressWarnings("unused")
public class OutputTransportAdaptorDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(OutputTransportAdaptorDeployer.class);
    private ConfigurationContext configurationContext;
    private List<String> deployedTransportAdaptorFilePaths = new ArrayList<String>();
    private List<String> unDeployedTransportAdaptorFilePaths = new ArrayList<String>();


    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;

    }

    /**
     * Process the transport adaptor file, create it and deploy it
     *
     * @param deploymentFileData information about the transport adaptor
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();

        if (!deployedTransportAdaptorFilePaths.contains(path)) {
            try {
                processDeploy(deploymentFileData, path);
            } catch (OutputTransportAdaptorManagerConfigurationException e) {
                throw new DeploymentException("Transport Adaptor file " + path.substring(path.lastIndexOf('/') + 1, path.length()) + " is not deployed ", e);
            }
        } else {
            deployedTransportAdaptorFilePaths.remove(path);
        }
    }

    public OMElement getTransportAdaptorOMElement(String filePath,
                                                  File transportAdaptorFile)
            throws DeploymentException {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
        OMElement transportAdaptorElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(transportAdaptorFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            transportAdaptorElement = builder.getDocumentElement();
            transportAdaptorElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " .xml file cannot be found in the path : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + transportAdaptorFile.getName() + " located in the path : " + fileName;
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
        return transportAdaptorElement;
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

        if (!unDeployedTransportAdaptorFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        } else {
            unDeployedTransportAdaptorFilePaths.remove(filePath);
        }

    }

    public void processDeploy(DeploymentFileData deploymentFileData, String path)
            throws DeploymentException, OutputTransportAdaptorManagerConfigurationException {

        File transportAdaptorFile = new File(path);
        CarbonOutputTransportAdaptorManagerService carbonTransportAdaptorManagerService = OutputTransportAdaptorManagerValueHolder.getCarbonTransportAdaptorManagerService();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();

        String transportAdaptorName = "";
        try {
            OMElement transportAdaptorOMElement = getTransportAdaptorOMElement(path, transportAdaptorFile);
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration = OutputTransportAdaptorConfigurationHelper.fromOM(transportAdaptorOMElement);

            if (!(transportAdaptorOMElement.getQName().getLocalPart().toString()).equals(OutputTransportAdaptorManagerConstants.TM_ELE_ROOT_ELEMENT)) {
                throw new DeploymentException("Invalid root element " + transportAdaptorOMElement.getQName().getLocalPart() + " in " + transportAdaptorFile.getName());
            }

            if (transportAdaptorConfiguration.getName() == null || transportAdaptorConfiguration.getType() == null) {
                throw new DeploymentException(transportAdaptorFile + " is not a valid xml configuration file");
            }

            transportAdaptorName = transportAdaptorOMElement.getAttributeValue(new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_NAME));

            if (OutputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(OutputTransportAdaptorConfigurationHelper.fromOM(transportAdaptorOMElement))) {
                if (carbonTransportAdaptorManagerService.checkAdaptorValidity(tenantId, transportAdaptorName)) {
                    carbonTransportAdaptorManagerService.addTransportAdaptorConfigurationForTenant(tenantId, transportAdaptorConfiguration);
                    carbonTransportAdaptorManagerService.addFileConfiguration(tenantId, transportAdaptorName, path, OutputTransportAdaptorFile.Status.DEPLOYED, null, null, null);

                    log.info("Transport adaptor " + transportAdaptorName + " successfully deployed");
                    if (carbonTransportAdaptorManagerService.outputTransportAdaptorNotificationListener != null) {
                        Iterator<OutputTransportAdaptorNotificationListener> deploymentListenerIterator = carbonTransportAdaptorManagerService.outputTransportAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            OutputTransportAdaptorNotificationListener outputTransportAdaptorNotificationListener = deploymentListenerIterator.next();
                            outputTransportAdaptorNotificationListener.configurationAdded(tenantId, transportAdaptorName);
                        }
                    }
                } else {
                    throw new OutputTransportAdaptorManagerConfigurationException(transportAdaptorName + " is already registered for this tenant");
                }
            } else {
                carbonTransportAdaptorManagerService.addFileConfiguration(tenantId, transportAdaptorName, path, OutputTransportAdaptorFile.Status.WAITING_FOR_DEPENDENCY, configurationContext.getAxisConfiguration(), "Transport Adaptor type is not found", transportAdaptorConfiguration.getType());
                log.info("Transport Adaptor configuration " + transportAdaptorName + " added for later deployment because of " + transportAdaptorConfiguration.getType() + " dependency");
            }
        } catch (OutputTransportAdaptorManagerConfigurationException ex) {
            carbonTransportAdaptorManagerService.addFileConfiguration(tenantId, transportAdaptorName, path, OutputTransportAdaptorFile.Status.ERROR, null, null, null);
            log.error("The deployment of " + transportAdaptorFile.getName() + " is not valid.", ex);
            throw new OutputTransportAdaptorManagerConfigurationException(ex);
        } catch (DeploymentException e) {
            carbonTransportAdaptorManagerService.addFileConfiguration(tenantId, transportAdaptorName, path, OutputTransportAdaptorFile.Status.ERROR, configurationContext.getAxisConfiguration(), "Deployment exception occurred", null);
            log.error("The deployment of " + transportAdaptorFile.getName() + " is not valid.", e);
            throw new DeploymentException(e);
        }

    }

    public void processUndeploy(String filePath) {

        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
        log.info("Transport Adaptor file " + fileName + " is undeployed");
        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        CarbonOutputTransportAdaptorManagerService carbonTransportAdaptorManagerService = OutputTransportAdaptorManagerValueHolder.getCarbonTransportAdaptorManagerService();
        carbonTransportAdaptorManagerService.removeTransportAdaptorConfigurationFromMap(filePath, tenantID);
    }

    public void setDirectory(String directory) {

    }

    public void addToDeployedEventFormatterFiles(String filePath){
        deployedTransportAdaptorFilePaths.add(filePath);
    }

    public void removeFromDeployedEventFormatterFiles(String filePath){
        unDeployedTransportAdaptorFilePaths.add(filePath);
    }
}


