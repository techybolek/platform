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
package org.wso2.carbon.event.processor.core;

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
import org.wso2.carbon.event.processor.core.exception.EventProcessorConfigurationException;
import org.wso2.carbon.event.processor.core.internal.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.config.EventProcessorConfigurationHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Deploy query plans as axis2 service
 */
@SuppressWarnings("unused")
public class EventProcessorDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(org.wso2.carbon.event.processor.core.EventProcessorDeployer.class);
    private ConfigurationContext configurationContext;
    private List<String> deployedQueryPlanFilePaths = new ArrayList<String>();
    private List<String> unDeployedQueryPlanFilePaths = new ArrayList<String>();


    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Reads the query-plan.xml and deploys it.
     *
     * @param deploymentFileData information about query plan
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String path = deploymentFileData.getAbsolutePath();

        if (!deployedQueryPlanFilePaths.contains(path)) {
            try {
                processDeploy(deploymentFileData, path);
            } catch (EventProcessorConfigurationException e) {
                throw new DeploymentException("Query plan is not deployed properly.", e);
            }
        } else {
            deployedQueryPlanFilePaths.remove(path);
        }
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

        if (!unDeployedQueryPlanFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        } else {
            unDeployedQueryPlanFilePaths.remove(filePath);
        }

    }

    public void processDeploy(DeploymentFileData deploymentFileData, String path)
            throws DeploymentException, EventProcessorConfigurationException {
        File queryPlanFile = new File(path);
        int tenantId = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        OMElement transportAdaptorOMElement = getQueryPlanOMElement(path, queryPlanFile);
        try {
            QueryPlanConfiguration queryPlanConfiguration = EventProcessorConfigurationHelper.fromOM(transportAdaptorOMElement);
            if (EventProcessorConfigurationHelper.validateQueryPlanConfiguration(queryPlanConfiguration)) {
                EventProcessorValueHolder.getEventProcessorService().addQueryPlanConfiguration(queryPlanConfiguration, configurationContext.getAxisConfiguration());
            }   else {
                throw new EventProcessorConfigurationException("Invalid query plan configuration :" + queryPlanConfiguration.getName());
            }
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    public OMElement getQueryPlanOMElement(String filePath,
                                           File transportAdaptorFile)
            throws DeploymentException {
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
            String errorMessage = " .xml file cannot be found in the path : " + filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + transportAdaptorFile.getName() + " located in the path : " + filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (OMException e) {
            String errorMessage = "XML tags are not properly closed " + filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
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

    private void processUndeploy(String filePath) {
//        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
//        CarbonTransportAdaptorManagerService carbonTransportAdaptorManagerService = TransportAdaptorManagerValueHolder.getCarbonTransportAdaptorManagerService();
//        carbonTransportAdaptorManagerService.removeTransportAdaptorConfigurationFromMap(filePath, tenantID);


    }

    public void setDirectory(String directory) {

    }
}


