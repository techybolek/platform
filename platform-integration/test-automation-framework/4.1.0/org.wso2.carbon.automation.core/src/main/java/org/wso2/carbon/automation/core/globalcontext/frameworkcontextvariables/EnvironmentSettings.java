package org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables;

public class EnvironmentSettings {

    private boolean runningOnStratos;
    private boolean enableSelenium;
    private boolean enebleStratosPort;
    private boolean enableWebContextRoot;
    private boolean enablecluster;
    private boolean enableBuilder;
    private String environment;
    private String mode;

    public boolean isRunningOnStratos() {
        return runningOnStratos;
    }

    public String executionEnvironment() {
        return environment;
    }

    public String executionMode() {
        return mode;
    }

    public boolean isBuilderEnabled() {
        return enableBuilder;
    }

    public boolean isEnableSelenium() {
        return enableSelenium;
    }

    public boolean isEnablePort() {
        return enebleStratosPort;
    }

    public boolean isEnableCarbonWebContext() {
        return enableWebContextRoot;
    }

    public boolean isClusterEnable() {
        return enablecluster;
    }

    public void setEnvironmentSettings(String executionEnvironment,
                                       String executionMode, boolean runningOnStratos,
                                       boolean enebleSelenium, boolean enableStratosPort, boolean enableWebContextRoot,
                                       boolean enableCluster, boolean enableBuilder) {

        this.runningOnStratos = runningOnStratos;
        this.enableSelenium = enebleSelenium;
        this.enebleStratosPort = enableStratosPort;
        this.enableWebContextRoot = enableWebContextRoot;
        this.enablecluster = enableCluster;
        this.enableBuilder = enableBuilder;
        this.environment = executionEnvironment;
        this.mode = executionMode;
    }

}
