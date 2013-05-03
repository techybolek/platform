package org.wso2.carbon.automation.core.globalcontext.frameworkcontext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.*;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;

import java.io.*;
import java.util.*;

public class FrameworkContextProvider implements FrameworkPropertyContext {
    private static final Log log = LogFactory.getLog(ProductUrlGeneratorUtil.class);

    private Properties prop;
    private DataSource dataSource = new DataSource();
    private DashboardVariables dashboardVariables = new DashboardVariables();
    private EnvironmentSettings environmentSettings = new EnvironmentSettings();
    private EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private CoverageSettings coverageSettings = new CoverageSettings();

    Selenium selenium = new Selenium();
    FrameworkContext frameworkProperties = new FrameworkContext();

    public FrameworkContextProvider() {
        String clarityPropertiesFile = null;
        ProductConstant.init();
        prop = new Properties();
        try {
            clarityPropertiesFile = ProductConstant.SYSTEM_TEST_SETTINGS_LOCATION + ProductConstant.PROPERTY_FILE_NAME;
            File clarityPropertyFile = new File(clarityPropertiesFile);
            InputStream inputStream = null;
            if (clarityPropertyFile.exists()) {
                inputStream = new FileInputStream(clarityPropertyFile);
            }
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
            }

        } catch (IOException ignored) {
            log.info("clarity.properties file not found");
        }
    }

    public DataSource getDataSource() {
        String driverName = (prop.getProperty("database.driver.name", "com.mysql.jdbc.Driver"));
        String jdbcUrl = (prop.getProperty("jdbc.url", "jdbc:mysql://localhost:3306"));
        String user = (prop.getProperty("db.user", "root"));
        String passwd = (prop.getProperty("db.password", "root123"));
        String dbName = (prop.getProperty("db.name", "testAutomation"));
        String rssDbUser = (prop.getProperty("rss.database.user", "tstusr"));
        String rssDbPassword = (prop.getProperty("rss.database.password", "test1234"));
        dataSource.setDatasource(driverName, jdbcUrl, user, passwd, dbName, rssDbUser, rssDbPassword);
        return dataSource;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        boolean enebleStratosPort = false;
        boolean runOnStratos = false;
        boolean enableSelenium = Boolean.parseBoolean(prop.getProperty("remote.selenium.web.driver.start", "false"));
        if (System.getProperty("integration.stratos.cycle") == null) {
            runOnStratos = Boolean.parseBoolean(prop.getProperty("stratos.test", "false"));
            enebleStratosPort = Boolean.parseBoolean(prop.getProperty("port.enable"));
        } else {
            runOnStratos = Boolean.valueOf(System.getProperty("integration.stratos.cycle"));
            enebleStratosPort = Boolean.valueOf(System.getProperty("integration.stratos.cycle"));
        }
        boolean enableWebContextRoot = Boolean.parseBoolean(prop.getProperty("carbon.web.context.enable", "false"));
        boolean enableCluster = Boolean.parseBoolean(prop.getProperty("cluster.enable", "false"));
        boolean enableBuilder = Boolean.parseBoolean(prop.getProperty("builder.enable", "false"));
        String executionEnvironment = (prop.getProperty("execution.environment", "integration"));
        String executionMode = (prop.getProperty("execution.mode", "user"));
        environmentSettings.setEnvironmentSettings
                (executionEnvironment, executionMode, runOnStratos, enableSelenium,
                        enebleStratosPort, enableWebContextRoot, enableCluster, enableBuilder);
        return environmentSettings;
    }

    public EnvironmentVariables getEnvironmentVariables() {
        String keystorePath;
        String keyStrorePassword;
        String deploymentFrameworkPath = (prop.getProperty("deployment.framework.home", "/"));
        List<String> productList = Arrays.asList((System.getProperty("server.list").split(",")));
        int deploymentDelay = Integer.parseInt(prop.getProperty("service.deployment.delay", "1000"));
        String ldapUserName = (prop.getProperty("ldap.username", "admin"));
        String ldapPasswd = (prop.getProperty("ldap.password", "admin"));
        if (Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            keystorePath = ProductConstant.SYSTEM_TEST_SETTINGS_LOCATION +
                    "keystores" + File.separator + "stratos" + File.separator +
                    (prop.getProperty("truststore.name", "wso2carbon.jks"));

            keyStrorePassword = (prop.getProperty("truststore.password", "wso2carbon"));
        } else {
            keystorePath = ProductConstant.SYSTEM_TEST_SETTINGS_LOCATION +
                    "keystores" + File.separator + "products" + File.separator +
                    (prop.getProperty("truststore.name", "wso2carbon.jks"));

            keyStrorePassword = (prop.getProperty("truststore.password", "wso2carbon"));
        }
        environmentVariables.setEnvironmentVariables(deploymentFrameworkPath, productList,
                deploymentDelay, ldapUserName, ldapPasswd,
                keystorePath, keyStrorePassword);
        return environmentVariables;
    }


    public Selenium getSelenium() {
        String browserName = prop.getProperty("browser.name", "firefox");
        String chromrDriverPath = prop.getProperty("path.to.chrome.driver", "/path/to/chrome/driver");
        boolean remoteWebDriver = Boolean.parseBoolean(prop.getProperty("remote.selenium.web.driver.start", "false"));
        String remoteWebDriverUrl = prop.getProperty("remote.webdirver.url", "http://10.100.3.95:3002/wd");
        selenium.setSelenium(browserName, chromrDriverPath, remoteWebDriver, remoteWebDriverUrl);
        return selenium;
    }

    public FrameworkContext getFrameworkProperties() {
        frameworkProperties.setDataSource(getDataSource());
        frameworkProperties.setEnvironmentSettings(getEnvironmentSettings());
        frameworkProperties.setEnvironmentVariables(getEnvironmentVariables());
        frameworkProperties.setSelenium(getSelenium());
        frameworkProperties.setDashboardVariables(getDashboardVariables());
        frameworkProperties.setCoverageSettings(getCoverageSettings());
        return frameworkProperties;
    }


    public DashboardVariables getDashboardVariables() {
        String driverName = (prop.getProperty("dashboard.database.driver.name", "com.mysql.jdbc.Driver"));
        String jdbcUrl = (prop.getProperty("dashboard.jdbc.url", "jdbc:mysql://localhost:3306"));
        String user = (prop.getProperty("dashboard.db.user", "root"));
        String passwd = (prop.getProperty("dashboard.db.password", "root"));
        String dbName = (prop.getProperty("dashboard.db.name", "FRAMEWORK_DB1"));
        String isEnableDashboard = (prop.getProperty("dashboard.enable", "false"));
        dashboardVariables.setDashboardVariables(driverName, jdbcUrl, user, passwd, dbName, isEnableDashboard);
        return dashboardVariables;
    }

    public CoverageSettings getCoverageSettings() {
        boolean coverageEnable = Boolean.parseBoolean(prop.getProperty("coverage.enable", "false"));
        coverageSettings.setCoverage(coverageEnable, null);
        return coverageSettings;
    }


    private void printInputStream(InputStream in) {
        BufferedReader ina = new BufferedReader(new InputStreamReader(in));
        String inputLine;
        try {
            while ((inputLine = ina.readLine()) != null) {
                System.out.println(inputLine);
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
