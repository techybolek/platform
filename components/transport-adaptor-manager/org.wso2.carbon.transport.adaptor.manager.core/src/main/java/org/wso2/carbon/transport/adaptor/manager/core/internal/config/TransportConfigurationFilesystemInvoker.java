package org.wso2.carbon.transport.adaptor.manager.core.internal.config;/*
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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorDeployer;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TMConfigurationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TransportConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(TransportConfigurationFilesystemInvoker.class);

    public static void saveConfigurationToFileSystem(OMElement transportAdaptorElement, String transportName, String pathInFileSystem, AxisConfiguration axisConfiguration) throws TMConfigurationException {

        TransportConfigurationFilesystemInvoker.save(transportAdaptorElement.toString(), transportName, pathInFileSystem, axisConfiguration);

    }

    public static void save(String transportAdaptor, String transportName,
                            String transportPath, AxisConfiguration axisConfiguration) {
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(transportPath));
            out.write(new XmlFormatter().format(transportAdaptor));
            out.close();


            log.info("Transport Adaptor configuration for " + transportName + " saved in the filesystem");

            TransportAdaptorDeployer deployer = (TransportAdaptorDeployer)getDeployer(axisConfiguration,"transportadaptors");
            DeploymentFileData deploymentFileData = new DeploymentFileData(new File(transportPath));
            deployer.manualDeploy(deploymentFileData,transportPath);

        } catch (IOException e) {
            log.error("Error while saving " + transportName, e);
        }

    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();

        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

    public static void executeUnDeploy(String transportPath , AxisConfiguration axisConfiguration){
        TransportAdaptorDeployer deployer = (TransportAdaptorDeployer) getDeployer(axisConfiguration,"transportadaptors");
        //DeploymentFileData deploymentFileData = new DeploymentFileData(new File(transportPath));

            deployer.manualUnDeploy(transportPath);

    }



}
