package org.wso2.carbon.deployment.synchronizer.git.internal;

/**
 * Configuration class
 */
public class GitDeploymentSyncronizerConfiguration {

    private boolean isStandardDeployment;
    private String gitServer;

    public GitDeploymentSyncronizerConfiguration() {
        isStandardDeployment = true;
    }

    public boolean isStandardDeployment() {
        return isStandardDeployment;
    }

    public void setStandardDeployment (boolean isStandardDeployment) {
        this.isStandardDeployment = isStandardDeployment;
    }

    public String getGitServer() {
        return gitServer;
    }

    public void setGitServer(String gitServer) {
        this.gitServer = gitServer;
    }
}
