package org.wso2.carbon.event.processor.admin;

public class ExecutionPlanConfigurationFileDto {

    private String path;
    private String name;
    private String status;
    private String deploymentStatusMessage;

    public String getDeploymentStatusMessage() {
        return deploymentStatusMessage;
    }

    public void setDeploymentStatusMessage(String deploymentStatusMessage) {
        this.deploymentStatusMessage = deploymentStatusMessage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
