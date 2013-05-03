package org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables;


public class FrameworkContext {

    private DataSource dataSource;
    private EnvironmentSettings environmentSettings;
    private EnvironmentVariables environmentVariables;
    private Selenium selenium;
    private DashboardVariables dashboardVariables;
    private CoverageSettings coverageSettings;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return environmentSettings;
    }

    public void setEnvironmentSettings(EnvironmentSettings environmentSettings) {
        this.environmentSettings = environmentSettings;
    }

    public EnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }


    public Selenium getSelenium() {
        return selenium;
    }

    public void setSelenium(Selenium selenium) {
        this.selenium = selenium;
    }

    public DashboardVariables setDashboardVariables(DashboardVariables dashboardVariable) {
        return dashboardVariable;
    }

    public CoverageSettings getCoverageSettings() {
        return coverageSettings;
    }

    public void setCoverageSettings(CoverageSettings coverage) {
        this.coverageSettings = coverage;
    }

}
