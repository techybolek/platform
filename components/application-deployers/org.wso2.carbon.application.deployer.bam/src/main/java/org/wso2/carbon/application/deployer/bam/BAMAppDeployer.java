package org.wso2.carbon.application.deployer.bam;


import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.bam.internal.BAMAppDeployerDSComponent;
import org.wso2.carbon.application.deployer.bam.internal.ServiceHolder;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BAMAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(BAMAppDeployer.class);
    public static final String BAM_TYPE = "bam/toolbox";
    public static final String BAM_DIR = "bam-toolbox";
    private Map<String, Boolean> acceptanceList = null;

    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        // loop through all artifacts
        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Registry registry = null;
            Resource resource;
            InputStream scriptStream;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            if (BAM_TYPE.equals(artifact.getType())) {
                deployer =  AppDeployerUtils.getArtifactDeployer(axisConfig, BAM_DIR, "tbox");
            } else {
                continue;
            }

            try {
                registry = ServiceHolder.getRegistry(CarbonContext.getCurrentContext().getTenantId());
            } catch (RegistryException e) {
                e.printStackTrace();
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("there must be a single file to " +
                        "be deployed. But " + files.size() + " files found.");
                continue;
            }

            String scriptPath = "repository/component/org.wso2.carbon.application.deployer.bam/tools";
            try {
                if(!registry.resourceExists(scriptPath)) {
                    resource = registry.newResource();
                    String toolBoxes = artifact.getFiles().get(0).getName();
                    resource.setContent(toolBoxes);
                    registry.put( scriptPath, resource );
                }else {
                    resource = registry.get(scriptPath);
                    scriptStream = resource.getContentStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(scriptStream));

                    String line;
                    String toolBoxes = "";

                    while ((line = br.readLine()) != null) {
                        if( !artifact.getFiles().get(0).getName().equals( line ) ) {
                            toolBoxes += line + '\n';
                        }
                    }
                    toolBoxes += artifact.getFiles().get(0).getName();
                    resource.setContent( toolBoxes );
                    registry.put( scriptPath, resource );
                }
            } catch (RegistryException e) {
                e.printStackTrace();
            } catch (IOException e) {
                log.error("Error while retrieving the deployed toolboxes' names from the registry", e);
                //throw new HiveScriptStoreException("Error while retrieving the script - " + scriptName + " from registry", e);
            }

            if (deployer != null) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    deployer.deploy(new DeploymentFileData(new File(artifactPath), deployer));
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                } catch (DeploymentException e) {
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                    throw e;
                }
            }
        }
    }

    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {
        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Deployer deployer;
            Registry registry = null;
            Resource resource;
            InputStream scriptStream;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            if (BAM_TYPE.equals(artifact.getType())) {
                deployer = AppDeployerUtils.getArtifactDeployer(axisConfig, BAM_DIR, "tbox");
            } else {
                continue;
            }

            try {
                registry = ServiceHolder.getRegistry(CarbonContext.getCurrentContext().getTenantId());
            } catch (RegistryException e) {
                e.printStackTrace();
            }

            // loop through all dependencies
            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Toolbox must have a single file. But " +
                        files.size() + " files found.");
                continue;
            }

            String scriptPath = "repository/component/org.wso2.carbon.application.deployer.bam/tools";
            try {
                if(registry.resourceExists(scriptPath)) {
                    resource = registry.get(scriptPath);
                    scriptStream = resource.getContentStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(scriptStream));

                    String line;
                    String toolBoxes = "";

                    while ((line = br.readLine()) != null) {
                        if( !artifact.getFiles().get(0).getName().equals( line ) ) {
                            toolBoxes = line + '\n';
                        }
                    }
                    resource.setContent( toolBoxes );
                    registry.put( scriptPath, resource );
                }
            } catch (RegistryException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (deployer != null && AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.
                    equals(artifact.getDeploymentStatus())) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    deployer.undeploy(artifactPath);
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                    File artifactFile = new File(artifactPath);
                    if (artifactFile.exists() && !artifactFile.delete()) {
                        log.warn("Couldn't delete App artifact file : " + artifactPath);
                    }
                } catch (DeploymentException e) {
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                    log.error("Error occured while trying to un deploy : "+artifact.getName());
                }
            }
        }
    }
}
