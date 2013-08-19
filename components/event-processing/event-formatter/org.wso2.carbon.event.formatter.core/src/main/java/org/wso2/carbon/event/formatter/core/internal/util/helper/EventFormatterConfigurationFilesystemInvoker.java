/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.formatter.core.internal.util.helper;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.formatter.core.EventFormatterDeployer;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class used to do the file system related tasks
 */
public class EventFormatterConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(EventFormatterConfigurationFilesystemInvoker.class);

    public static void save(OMElement eventFormatterOMElement,
                            String pathInFileSystem,
                            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        EventFormatterConfigurationFilesystemInvoker.save(eventFormatterOMElement.toString(), pathInFileSystem, axisConfiguration);
    }

    public static void save(String eventFormatter,
                            String filePath, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            out.write(new XmlFormatter().format(eventFormatter));
            out.close();
            log.info("Event Formatter configuration saved in the filesystem : "+ new File(filePath).getName());

            EventFormatterDeployer eventFormatterDeployer = (EventFormatterDeployer) getDeployer(axisConfiguration, EventFormatterConstants.TM_ELE_DIRECTORY);
            eventFormatterDeployer.addToDeployedEventFormatterFiles(filePath);
            eventFormatterDeployer.processDeploy(filePath);

        } catch (IOException e) {
            log.error("Could not save Event Formatter configuration : " + new File(filePath).getName(), e);
            throw new EventFormatterConfigurationException("Error while saving ", e);
        }
    }

    public static void delete(String filePath,
                              AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        try {
            String fileName = new File(filePath).getName();
            File file = new File(filePath);
            if (file.exists()) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete Event Formatter configuration : " + fileName);
                } else {
                    log.info("Event Formatter configuration deleted from the file system : "+fileName);
                    EventFormatterDeployer deployer = (EventFormatterDeployer) getDeployer(axisConfiguration, EventFormatterConstants.TM_ELE_DIRECTORY);
                    deployer.removeFromDeployedEventFormatterFiles(filePath);
                    deployer.processUndeploy(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventFormatterConfigurationException("Error while deleting the Event Formatter : "+e.getMessage(), e);
        }
    }

    public static void reload(String filePath, AxisConfiguration axisConfiguration) {
        EventFormatterDeployer deployer = (EventFormatterDeployer) getDeployer(axisConfiguration, EventFormatterConstants.TM_ELE_DIRECTORY);
        try {
            deployer.processUndeploy(filePath);
            deployer.processDeploy(filePath);
        } catch (DeploymentException e) {
            throw new EventFormatterConfigurationException(e);
        }

    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

}
