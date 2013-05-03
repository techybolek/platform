package org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables;

public class DataSource {
    private String dbDriverName;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbName;

    private String rssDbUser;
    private String rssDbPassword;

    public String getDbDriverName() {
        return dbDriverName;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public String getRssDbUser() {
        return rssDbUser;
    }

    public String getRssDbPassword() {
        return rssDbPassword;
    }

    public void setDatasource(String dbDriverName, String dbUrl, String dbUser, String dbPassword,
                              String dbName, String rssDbUser, String rssDbPassword) {
        dbDriverName = dbDriverName;
        dbUrl = dbUrl;
        dbUser = dbUser;
        dbPassword = dbPassword;
        dbName = dbName;
        rssDbUser = rssDbUser;
        rssDbPassword = rssDbPassword;
    }
}
