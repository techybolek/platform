package org.wso2.carbon.automation.core.globalcontext.frameworkcontext;

import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.*;

public interface FrameworkPropertyContext {
    public DataSource getDataSource();

    public EnvironmentSettings getEnvironmentSettings();

    public EnvironmentVariables getEnvironmentVariables();

    public Selenium getSelenium();

    public FrameworkContext getFrameworkProperties();
}
