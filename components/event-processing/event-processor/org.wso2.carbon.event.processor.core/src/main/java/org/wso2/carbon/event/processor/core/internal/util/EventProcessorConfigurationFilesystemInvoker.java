package org.wso2.carbon.event.processor.core.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.processor.core.EventProcessorDeployer;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EventProcessorConfigurationFilesystemInvoker {
    private static final Log log = LogFactory.getLog(EventProcessorConfigurationFilesystemInvoker.class);


    public static void save(OMElement executionPlanOM,
                            String executionPlanName,
                            String pathInFileSystem,
                            AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        EventProcessorConfigurationFilesystemInvoker.save(executionPlanOM.toString(), executionPlanName, pathInFileSystem, axisConfiguration);
    }

    public static void save(String executionPlan, String executionPlanName,
                            String filePath, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            out.write(EventProcessorUtil.formatXml(executionPlan));
            out.close();
            log.info("Execution plan configuration for " + executionPlanName + " saved in the filesystem");

            EventProcessorDeployer eventProcessorDeployer = (EventProcessorDeployer) getDeployer(axisConfiguration, EventProcessorConstants.EP_ELE_DIRECTORY);
            eventProcessorDeployer.addToDeployedExecutionPlanFiles(filePath);
            eventProcessorDeployer.processDeploy(filePath);
        } catch (IOException e) {
            log.error("Error while saving " + executionPlanName, e);
            throw new ExecutionPlanConfigurationException("Error while saving ", e);
        }
    }

    public static void delete(String filePath,
                              AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        try {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
            File file = new File(filePath);
            if (file.exists()) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + fileName);
                } else {
                    log.info(fileName + " is deleted from the file system");
                    EventProcessorDeployer deployer = (EventProcessorDeployer) getDeployer(axisConfiguration, EventProcessorConstants.EP_ELE_DIRECTORY);
                    deployer.removeFromDeployedExecutionPlanFiles(filePath);
                    deployer.processUndeploy(filePath);
                }
            }
        } catch (Exception e) {
            throw new ExecutionPlanConfigurationException("Error while deleting the execution plan file ", e);
        }
    }

    public static void reload(String filePath, AxisConfiguration axisConfiguration) throws ExecutionPlanConfigurationException {
        EventProcessorDeployer eventProcessorDeployer = (EventProcessorDeployer) getDeployer(axisConfiguration, EventProcessorConstants.EP_ELE_DIRECTORY);
        try {
            eventProcessorDeployer.processUndeploy(filePath);
            eventProcessorDeployer.processDeploy(filePath);
        } catch (DeploymentException e) {
            throw new EventFormatterConfigurationException(e);
        }

    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }


}
