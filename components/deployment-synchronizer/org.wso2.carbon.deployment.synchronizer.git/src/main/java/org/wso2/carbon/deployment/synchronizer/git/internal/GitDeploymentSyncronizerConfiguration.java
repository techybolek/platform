package org.wso2.carbon.deployment.synchronizer.git.internal;

/**
 * Configuration class
 */
public class GitDeploymentSyncronizerConfiguration {

    private boolean isStandardDeployment;

    public GitDeploymentSyncronizerConfiguration() {
        isStandardDeployment = true;
    }

    public boolean isStandardDeployment() {
        return isStandardDeployment;
    }

    public void setStandardDeployment (boolean isStandardDeployment) {
        this.isStandardDeployment = isStandardDeployment;
    }

}
