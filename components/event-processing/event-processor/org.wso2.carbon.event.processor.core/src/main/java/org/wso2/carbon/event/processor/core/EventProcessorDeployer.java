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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorConfigurationHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Deploy query plans as axis2 service
 */
@SuppressWarnings("unused")
public class EventProcessorDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(org.wso2.carbon.event.processor.core.EventProcessorDeployer.class);
    private ConfigurationContext configurationContext;
    private List<String> deployedExecutionPlanFilePaths = new CopyOnWriteArrayList<String>();
    private List<String> unDeployedExecutionPlanFilePaths = new CopyOnWriteArrayList<String>();


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
        try {
            String path = deploymentFileData.getAbsolutePath();

            if (!deployedExecutionPlanFilePaths.contains(path)) {
                try {
                    processDeploy(path);
                } catch (ExecutionPlanConfigurationException e) {
                    throw new DeploymentException("Query plan is not deployed properly.", e);
                }
            } else {
                log.debug("Execution plan file is already deployed :" + path);
                deployedExecutionPlanFilePaths.remove(path);
            }
        } catch (Throwable t) {
            log.error("Can't deploy the execution plan: " + deploymentFileData.getName(), t);
            throw new DeploymentException("Can't deploy the execution plan: " + deploymentFileData.getName(), t);
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
        try {
            if (!unDeployedExecutionPlanFilePaths.contains(filePath)) {
                processUndeploy(filePath);
            } else {
                log.debug("Execution plan file is already undeployed :" + filePath);
                unDeployedExecutionPlanFilePaths.remove(filePath);
            }
        } catch (Throwable t) {
            log.error("Can't undeploy the execution plan: " + filePath, t);
            throw new DeploymentException("Can't undeploy the execution plan: " + filePath, t);
        }

    }

    public synchronized void processDeploy(String path)
            throws DeploymentException, ExecutionPlanConfigurationException {
        // can't be null at this point
        CarbonEventProcessorService carbonEventProcessorService = EventProcessorValueHolder.getEventProcessorService();

        File executionPlanFile = new File(path);
        int tenantId = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        OMElement executionPlanOMElement = getExecutionPlanOMElement(path, executionPlanFile);
        try {
            ExecutionPlanConfiguration executionPlanConfiguration = EventProcessorConfigurationHelper.fromOM(executionPlanOMElement);
            ExecutionPlanConfigurationFile executionPlanConfigurationFile = new ExecutionPlanConfigurationFile();

            if (EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(executionPlanOMElement)) {
                try {
                    carbonEventProcessorService.addExecutionPlanConfiguration(executionPlanConfiguration, PrivilegedCarbonContext.getCurrentContext(configurationContext.getAxisConfiguration()).getTenantId());
                    executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.DEPLOYED);
                    log.info("Execution plan is deployed successfully and in active state  : " + executionPlanConfiguration.getName());

                } catch (ExecutionPlanDependencyValidationException ex) {
                    executionPlanConfigurationFile.setDependency(ex.getDependency());
                    executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY);
                    log.info("Execution plan deployment held back and in inactive state : " + executionPlanConfiguration.getName() +" ,waiting for dependency : "+ex.getDependency());

                } catch (ExecutionPlanConfigurationException ex) {
                    executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.ERROR);
                    log.info("Execution plan is not deployed and in inactive state : " + executionPlanConfiguration.getName());
                }
                executionPlanConfigurationFile.setExecutionPlanName(executionPlanConfiguration.getName());
                executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
                executionPlanConfigurationFile.setFilePath(path);
                carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getCurrentContext(configurationContext.getAxisConfiguration()).getTenantId());

            } else {
                executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.ERROR);
                executionPlanConfigurationFile.setExecutionPlanName(executionPlanConfiguration.getName());
                executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
                executionPlanConfigurationFile.setFilePath(path);
                carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getCurrentContext(configurationContext.getAxisConfiguration()).getTenantId());
                throw new ExecutionPlanConfigurationException("Invalid query plan configuration :" + executionPlanConfiguration.getName());
            }
        } catch (Exception ex) {
            log.error("Unable to deploy the configuration : " + ex.getMessage(), ex);
        }
    }

    public synchronized void processUndeploy(String filePath) {

        log.info("Execution plan was undeployed successfully : " + new File(filePath).getName());
        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        CarbonEventProcessorService carbonEventProcessorService = EventProcessorValueHolder.getEventProcessorService();
        ExecutionPlanConfigurationFile executionPlanConfigurationFile = carbonEventProcessorService.getExecutionPlanConfigurationFile(filePath, configurationContext.getAxisConfiguration());
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        carbonEventProcessorService.removeExecutionPlanConfiguration(executionPlanConfigurationFile.getExecutionPlanName(), tenantID);
        carbonEventProcessorService.removeExecutionPlanConfigurationFile(filePath, tenantID);
    }

    public void setDirectory(String directory) {

    }

    public void addToDeployedExecutionPlanFiles(String filePath) {
        deployedExecutionPlanFilePaths.add(filePath);
    }

    public void removeFromDeployedExecutionPlanFiles(String filePath) {
        unDeployedExecutionPlanFilePaths.add(filePath);
    }


    private OMElement getExecutionPlanOMElement(String filePath, File executionPlanFile)
            throws DeploymentException {
        OMElement executionPlanElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(executionPlanFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            executionPlanElement = builder.getDocumentElement();
            executionPlanElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " .xml file cannot be found in the path : " + filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + executionPlanFile.getName() + " located in the path : " + filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
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
        return executionPlanElement;
    }

}


