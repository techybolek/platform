package org.wso2.carbon.automation.core.globalcontext.frameworkcontext;

import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.*;

/**
 * Object for Framework settings object to keep all settings data
 * **/
public class FrameworkSettings {


    private DataSource dataSource;
    private EnvironmentSettings environmentSettings;
    private EnvironmentVariables environmentVariables;
    private Selenium selenium;
    private DashboardVariables dashboardVariables;
    private CoverageSettings coverageSettings;

    public DataSource getDataSource() {
        return dataSource;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return environmentSettings;
    }

    public EnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    public Selenium getSelenium() {
        return selenium;
    }

    public DashboardVariables getDashboardVariables() {
        return dashboardVariables;
    }

    public CoverageSettings getCoverageSettings() {
        return coverageSettings;
    }

    public void setFrameworkSettings(DataSource dataSource, EnvironmentSettings environmentSettings,
                                     EnvironmentVariables environmentVariables, Selenium selenium,
                                     DashboardVariables dshVariables,
                                     CoverageSettings coverage) {
        this.dataSource = dataSource;
        this.environmentSettings = environmentSettings;
        this.environmentVariables = environmentVariables;
        this.selenium = selenium;
        this.dashboardVariables = dshVariables;
        this.coverageSettings = coverage;
    }
}
