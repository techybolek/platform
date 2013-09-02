/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.appfactory.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.appfactory.appmanagement.AppManagementPage;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppHomePage;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppLogin;
import org.wso2.carbon.automation.api.selenium.appfactory.resources.*;
import org.wso2.carbon.automation.core.BrowserManager;

import static org.testng.Assert.assertTrue;

public class DatabaseResourceCreationTestCase extends AppFactoryIntegrationBase {
    private WebDriver driver;
    public String databaseName ;


    public DatabaseResourceCreationTestCase(){
        super.setDatabaseName();
        databaseName=super.getDatabaseName();

    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }


    @Test(groups = "wso2.af", description = "create Database")
    public void testDatabaseResourceCreation() throws Exception {
        AppLogin appLogin = new AppLogin(driver);
        AppHomePage appHomePage = appLogin.loginAs(getUserInfo().getUserName(), getUserInfo().getPassword());
        String appName = AppCredentialsGenerator.getAppName();
        appHomePage.gotoApplicationManagementPage(appName);
        AppManagementPage appManagementPage = new AppManagementPage(driver);
        appManagementPage.gotoResourceOverviewPage();
        ResourceOverviewPage resourceOverviewPage = new ResourceOverviewPage(driver);
        resourceOverviewPage.gotoDataBaseConfigPage();
        DatabaseConfigurationPage databaseConfigurationPage = new DatabaseConfigurationPage(driver);
        databaseConfigurationPage.gotoNewDatabasePage();
        NewDatabasePage newDatabasePage = new NewDatabasePage(driver);
        AppCredentialsGenerator.setDbName(databaseName);
        String database = AppCredentialsGenerator.getDbName();
        newDatabasePage.createDatabaseDefault(database, "DbUser123");
        //DatabaseConfigurationPage databaseConfigurationPage = new DatabaseConfigurationPage(driver);
        databaseConfigurationPage.gotoNewDatabaseUserPage();
        NewDatabaseUserPage newDatabaseUserPage = new NewDatabaseUserPage(driver);
        newDatabaseUserPage.createNewDatabaseUser("wso2usr", "wso2DbUser123", "Testing");
        // DatabaseConfigurationPage databaseConfigurationPage = new DatabaseConfigurationPage(driver);
        databaseConfigurationPage.gotoNewDatabaseTemplatePage();
        NewDatabaseTemplatePage newDatabaseTemplatePage = new NewDatabaseTemplatePage(driver);
        newDatabaseTemplatePage.createDatabaseTemplate("wso2Temp");
        //databaseConfigurationPage.signOut();
        String databaseCheckName = AppCredentialsGenerator.getDbName();
        assertTrue(databaseConfigurationPage.isDatabaseDetailsAvailable(databaseCheckName, "wso2usr_",
                "wso2Temp@Development")
                , "Database Details Are Not Available in Database Configuration Page");
        databaseConfigurationPage.signOut();
    }

    //starting the data source creation and deletion tests

    @Test(dependsOnMethods = "testDatabaseResourceCreation", groups = "wso2.af",
            description = "Creating a data Source")
    public void createNewDataSourceTestCase() throws Exception {
        AppLogin appLogin = new AppLogin(driver);
        AppHomePage appHomePage = appLogin.loginAs(getUserInfo().getUserName(), getUserInfo().getPassword());
        String appName = AppCredentialsGenerator.getAppName();
        //assign the application name
        appHomePage.gotoApplicationManagementPage(appName);
        AppManagementPage appManagementPage = new AppManagementPage(driver);
        appManagementPage.gotoResourceOverviewPage();
        ResourceOverviewPage resourceOverviewPage = new ResourceOverviewPage(driver);
        resourceOverviewPage.gotoDataSourcePage();
        DataSourcePage dataSourcePage = new DataSourcePage(driver);
        dataSourcePage.gotoNewDataSourcePage();
        NewDataSourcePage newDataSourcePage = new NewDataSourcePage(driver);
        newDataSourcePage.createNewDataSource("Wso2DS", "Test Description", "test123");
        //DataSourcePage dataSourcePage = new DataSourcePage(driver);
        dataSourcePage.gotoResourceOverviewPage();
        //  ResourceOverviewPage resourceOverviewPage = new ResourceOverviewPage(driver);
        assertTrue(resourceOverviewPage.isDataSourceAvailable("Wso2DS")
                , "Data Source details are not available");
        resourceOverviewPage.signOut();
    }


    @Test(dependsOnMethods = "createNewDataSourceTestCase", groups = "wso2.af",
            description = "Delete database Resource")
    public void testDeleteDatabaseResource() throws Exception {
        AppLogin appLogin = new AppLogin(driver);
        AppHomePage appHomePage = appLogin.loginAs(getUserInfo().getUserName(), getUserInfo().getPassword());
        String appName = AppCredentialsGenerator.getAppName();
        appHomePage.gotoApplicationManagementPage(appName);
        AppManagementPage appManagementPage = new AppManagementPage(driver);
        appManagementPage.gotoResourceOverviewPage();
        ResourceOverviewPage resourceOverviewPage = new ResourceOverviewPage(driver);
        resourceOverviewPage.gotoDataBaseConfigPage();
        //deleting the template
        DatabaseConfigurationPage databaseConfigurationPage = new DatabaseConfigurationPage(driver);
        databaseConfigurationPage.gotoDeleteDbTemplatePage("wso2Temp@Development");
        DeleteTemplatePage deleteTemplatePage = new DeleteTemplatePage(driver);
        deleteTemplatePage.deleteTemplate();
        //deleting the db user
        databaseConfigurationPage.gotoDeleteDbUserPage("wso2usr_");
        DeleteDbUserPage deleteDbUserPage = new DeleteDbUserPage(driver);
        deleteDbUserPage.deleteDbUser();
        //deleting the auto user
        String dbUser = AppCredentialsGenerator.getDbName();
        databaseConfigurationPage.gotoDeleteDbUserPage(dbUser + "_");
        deleteDbUserPage.deleteDbUser();
        //deleting the auto template
        databaseConfigurationPage.gotoDeleteDbTemplatePage(dbUser + "@");
        deleteTemplatePage.deleteTemplate();
        //deleting the database
        String dbName = AppCredentialsGenerator.getDbName();
        databaseConfigurationPage.gotoDeleteDbPage(dbName);
        DeleteDBPage deleteDBPage = new DeleteDBPage(driver);
        deleteDBPage.deleteDatabase();
        resourceOverviewPage.gotoDataBaseConfigPage();
        assertTrue(databaseConfigurationPage.isDatabaseDetailsDeleted()
                , "Database Deletion has a  error");
    }


    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}