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
package org.wso2.carbon.transport.adaptor.manager.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TMConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.CarbonTransportManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.internal.config.TransportConfigurationHelper;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TMConstants;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportManagerValueHolder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Deploy cep buckets as axis2 service
 */
@SuppressWarnings("unused")
public class TransportAdaptorDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(TransportAdaptorDeployer.class);
    private ConfigurationContext configurationContext;

    public List<String> deployedTransportAdaptorFilePaths = new ArrayList<String>();
    public List<String> unDeployedTransportAdaptorFilePaths = new ArrayList<String>();


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
               processDeploy(deploymentFileData, path);
        }

        else{
            deployedTransportAdaptorFilePaths.remove(path);
        }


    }

    public OMElement getTransportAdaptorOMElement(String path,
                                                  File transportAdaptorFile) throws DeploymentException {
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
            String errorMessage = " .xml file cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + transportAdaptorFile.getName() + " located in the path : " + path;
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

        if (! unDeployedTransportAdaptorFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        }

        else {
            unDeployedTransportAdaptorFilePaths.remove(filePath);
        }

    }

    public void processDeploy(DeploymentFileData deploymentFileData , String path) throws DeploymentException {

        log.info("Deploying a transport-adaptor file started");
        File transportAdaptorFile = new File(path);
        CarbonTransportManagerService carbonTransportManagerService = TransportManagerValueHolder.getCarbonTransportManagerService();
        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        String transportAdaptorName = "";


        try {

            OMElement transportAdaptorOMElement = getTransportAdaptorOMElement(path, transportAdaptorFile);

            TransportAdaptorConfiguration transportAdaptorConfiguration = TransportConfigurationHelper.fromOM(transportAdaptorOMElement);
            transportAdaptorName = transportAdaptorOMElement.getAttributeValue(new QName(TMConstants.TM_ATTR_NAME));

            if (carbonTransportManagerService.checkAdaptorValidity(tenantID, transportAdaptorName)) {

                carbonTransportManagerService.removeFromCancelDeployMap(tenantID, transportAdaptorName);
                carbonTransportManagerService.addTransportConfigurationForTenant(tenantID, transportAdaptorConfiguration);
                carbonTransportManagerService.addFileConfiguration(tenantID, transportAdaptorName, path, true);
            } else {
                throw new TMConfigurationException(transportAdaptorName + " is already registered for this tenant");
            }

        } catch (TMConfigurationException e) {
            carbonTransportManagerService.addFileConfiguration(tenantID, transportAdaptorName, path, false);
            log.error("The deployment of " + transportAdaptorFile.getName() + " is not valid.", e);
            throw new DeploymentException(e);
        }

    }

    private void processUndeploy(String filePath) {
        log.info("Transport Adaptor file is undeployed");
        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();

        CarbonTransportManagerService carbonTransportManagerService = TransportManagerValueHolder.getCarbonTransportManagerService();
        carbonTransportManagerService.removeFromCancelUnDeployMap(tenantID, filePath);
        carbonTransportManagerService.removeTransportConfigurationFromMap(filePath, tenantID);
    }

    public void setDirectory(String directory) {

    }

    public void manualDeploy(DeploymentFileData deploymentFileData, String filePath) throws DeploymentException {
        deployedTransportAdaptorFilePaths.add(filePath );
        processDeploy(deploymentFileData, filePath);
    }

    public void manualUnDeploy(String filePath){
        unDeployedTransportAdaptorFilePaths.add(filePath);
        processUndeploy(filePath);
    }





}


