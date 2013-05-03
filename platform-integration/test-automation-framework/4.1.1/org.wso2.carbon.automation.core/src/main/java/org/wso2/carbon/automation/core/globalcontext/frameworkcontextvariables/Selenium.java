package org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables;

public class Selenium {

    private String browserName;
    private String chromrDriverPath;
    private boolean remoteWebDriver;
    private String remoteWebDriverUrl;

    public String getBrowserName() {
        return browserName;
    }

    public String getChromrDriverPath() {
        return chromrDriverPath;
    }

    public boolean getRemoteWebDriver() {
        return remoteWebDriver;
    }

    public String getRemoteWebDriverUrl() {
        return remoteWebDriverUrl;
    }

    public void setSelenium(String browserName, String chromrDriverPath, boolean remoteWebDriver,
                            String remoteWebDriverUrl) {
        browserName = browserName;
        chromrDriverPath = chromrDriverPath;
        remoteWebDriver = remoteWebDriver;
        remoteWebDriverUrl = remoteWebDriverUrl;
    }
}
