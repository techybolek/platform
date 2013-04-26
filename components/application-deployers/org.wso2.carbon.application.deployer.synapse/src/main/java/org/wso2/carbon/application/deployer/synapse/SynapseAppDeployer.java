/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.application.deployer.synapse;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.deployers.AbstractSynapseArtifactDeployer;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.synapse.internal.DataHolder;
import org.wso2.carbon.application.deployer.synapse.internal.SynapseAppDeployerDSComponent;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SynapseAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(SynapseAppDeployer.class);

    private Map<String, Boolean> acceptanceList = null;

    /**
     * Deploy the artifacts which can be deployed through this deployer (endpoints, sequences,
     * proxy service etc.).
     *
     * @param carbonApp  - CarbonApplication instance to check for artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException{
        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (!isAccepted(artifact.getType())) {
                log.warn("Can't deploy artifact : " + artifact.getName() + " of type : " +
                        artifact.getType() + ". Required features are not installed in the system");
                continue;
            }

            if (SynapseAppDeployerConstants.SEQUENCE_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.SEQUENCES_FOLDER);
            } else if (SynapseAppDeployerConstants.ENDPOINT_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.ENDPOINTS_FOLDER);
            } else if (SynapseAppDeployerConstants.PROXY_SERVICE_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.PROXY_SERVICES_FOLDER);
            } else if (SynapseAppDeployerConstants.LOCAL_ENTRY_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.LOCAL_ENTRIES_FOLDER);
            } else if (SynapseAppDeployerConstants.EVENT_SOURCE_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.EVENTS_FOLDER);
            } else if (SynapseAppDeployerConstants.TASK_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.TASKS_FOLDER);
            } else if (SynapseAppDeployerConstants.MESSAGE_STORE_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.MESSAGE_STORE_FOLDER);
            } else if (SynapseAppDeployerConstants.MESSAGE_PROCESSOR_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.MESSAGE_PROCESSOR_FOLDER);
            } else if (SynapseAppDeployerConstants.API_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.APIS_FOLDER);
            } else if (SynapseAppDeployerConstants.TEMPLATE_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.TEMPLATES_FOLDER);
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Synapse artifact types must have a single file to " +
                        "be deployed. But " + files.size() + " files found.");
                continue;
            }
            if (deployer != null) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                deployer.deploy(new DeploymentFileData(new File(artifactPath), deployer));
            }
        }
    }

    /**
     * Undeploys Synapse artifacts found in this application. Just delete the files from the
     * hot folders. Synapse hot deployer will do the rest..
     *
     * @param carbonApplication - CarbonApplication instance
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfig)
            throws DeploymentException{

        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig()
                .getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (SynapseAppDeployerConstants.SEQUENCE_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.SEQUENCES_FOLDER);
            } else if (SynapseAppDeployerConstants.ENDPOINT_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.ENDPOINTS_FOLDER);
            } else if (SynapseAppDeployerConstants.PROXY_SERVICE_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.PROXY_SERVICES_FOLDER);
            } else if (SynapseAppDeployerConstants.LOCAL_ENTRY_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.LOCAL_ENTRIES_FOLDER);
            } else if (SynapseAppDeployerConstants.EVENT_SOURCE_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.EVENTS_FOLDER);
            } else if (SynapseAppDeployerConstants.TASK_TYPE.equals(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.TASKS_FOLDER);
            } else if (SynapseAppDeployerConstants.MESSAGE_STORE_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.MESSAGE_STORE_FOLDER);
            } else if (SynapseAppDeployerConstants.MESSAGE_PROCESSOR_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.MESSAGE_PROCESSOR_FOLDER);
            } else if (SynapseAppDeployerConstants.API_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.APIS_FOLDER);
            } else if (SynapseAppDeployerConstants.TEMPLATE_TYPE.endsWith(artifact.getType())) {
                deployer = getDeployer(axisConfig, SynapseAppDeployerConstants.TEMPLATES_FOLDER);
            } else {
                continue;
            }
            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Synapse artifact types must have a single file. But " +
                        files.size() + " files found.");
                continue;
            }
            if (deployer != null) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactName = artifact.getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    AbstractSynapseArtifactDeployer synapseArtifactDeployer =
                            (AbstractSynapseArtifactDeployer) deployer;
                    synapseArtifactDeployer.undeploySynapseArtifact(artifactName);
                    File artifactFile = new File(artifactPath);
                    if (artifactFile.exists() && !artifactFile.delete()) {
                        log.warn("Couldn't delete App artifact file : " + artifactPath);
                    }
                } catch (Exception e) {
                    log.error("Error occured while trying to un deploy : "+artifactName);
                }
            }
        }
    }

    /**
     * Check whether a particular artifact type can be accepted for deployment. If the type doesn't
     * exist in the acceptance list, we assume that it doesn't require any special features to be
     * installed in the system. Therefore, that type is accepted.
     * If the type exists in the acceptance list, the acceptance value is returned.
     *
     * @param serviceType - service type to be checked
     * @return true if all features are there or entry is null. else false
     */
    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils.buildAcceptanceList(SynapseAppDeployerDSComponent
                    .getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }


    /**
     * Finds the correct deployer for the given artifact type
     *
     * @param axisConfig - AxisConfiguration instance
     * @return Deployer instance
     */
    private Deployer getDeployer(AxisConfiguration axisConfig, String directory) {
        Deployer deployer = null;
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        String tenantId = AppDeployerUtils.getTenantIdString(axisConfig);
        SynapseEnvironmentService environmentService = DataHolder.getInstance().
                getSynapseEnvironmentService(Integer.parseInt(tenantId));
        if (environmentService != null) {
            String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                    environmentService.getSynapseEnvironment().getServerContextInformation());
            String endpointDirPath = synapseConfigPath
                                     + File.separator + directory;
            deployer = deploymentEngine.getDeployer(endpointDirPath,
                                                    ServiceBusConstants.ARTIFACT_EXTENSION);
        }
        return deployer;
    }

}


