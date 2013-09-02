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

package org.wso2.carbon.automation.api.selenium.appfactory.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class DatabaseConfigurationPage {


    private static final Log log = LogFactory.getLog(ResourceOverviewPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public DatabaseConfigurationPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("listdatabases.jag"))) {
            throw new IllegalStateException("This is not the database Configuration page");
        }
    }

    //checking database configuration panes to check whether resources has deleted successfully
    /**
     * this method is checking database configuration panes to check whether resources has deleted
     * successfully
     *
     * @return database status deletion status
     */
    public boolean isDatabaseDetailsDeleted() {
        String databaseName = driver.findElement(By.id(uiElementMapper.getElement
                ("app.db.details.id"))).getText().toUpperCase();
        String dataBaseUser = driver.findElement(By.id(uiElementMapper.getElement
                ("app.db.user.id"))).getText().toUpperCase();
        String dataBaseTemplate = driver.findElement(By.id(uiElementMapper.getElement
                ("app.db.template.id"))).getText().toUpperCase();

        log.info("------------------------------------------");
        log.info(databaseName);
        log.info(dataBaseUser);
        log.info(dataBaseTemplate);
        log.info("------------------------------------------");
        if (!databaseName.equals("Databases have not been created yet.".toUpperCase()) && dataBaseUser.
                equals("DB users have not been created yet.".toUpperCase()) && dataBaseTemplate.
                equals("DB Templates have not been created yet.".toUpperCase())) {
            log.info("Database resources deleted Successfully");
            return true;
        }
        return false;
    }

    /**
     * this method is used to check the availability of Database Details.
     *
     * @param database Database name
     * @param user     Database user
     * @param template Database Template
     * @return database details availability status.
     */
    public boolean isDatabaseDetailsAvailable(String database, String user, String template) {
        String databaseName = driver.findElement(By.partialLinkText(uiElementMapper.getElement
                ("app.db.link.text"))).getText().toUpperCase();
        String databaseUser = driver.findElement(By.partialLinkText(uiElementMapper.getElement
                ("app.db.user.link.text"))).getText().toUpperCase();
        String databaseTemplate = driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.db.template.link.text"))).getText().toUpperCase();
        log.info(databaseName);
        log.info(databaseUser);
        log.info(databaseTemplate);

        if (databaseName.contains(database.toUpperCase()) && databaseUser.contains(user.toUpperCase())
                && databaseTemplate.contains(template.toUpperCase())) {
            log.info("Database details Available");
            return true;
        }
        return false;
    }
    /**
     * navigate to New Database Page
     *
     * @return NewDatabasePage
     * @throws IOException for input output exceptions.
     */
    public NewDatabasePage gotoNewDatabasePage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.factory.new.database.link.text"))).click();
        return new NewDatabasePage(driver);
    }
    /**
     * navigate to New Database user page
     *
     * @return NewDatabaseUserPage
     * @throws IOException for input output exceptions
     */
    public NewDatabaseUserPage gotoNewDatabaseUserPage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.factory.db.dbUser.link.text"))).click();
        return new NewDatabaseUserPage(driver);
    }
    /**
     * navigate to New Database Template Page.
     *
     * @return NewDatabaseTemplatePage
     * @throws IOException for input output exceptions
     */
    public NewDatabaseTemplatePage gotoNewDatabaseTemplatePage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.factory.db.template.link.text"))).click();
        return new NewDatabaseTemplatePage(driver);
    }

    /**
     * navigate to Delete Database Page
     *
     * @param resource database resource
     * @return DeleteDBPage
     * @throws IOException for input output exceptions
     */
    public DeleteDBPage gotoDeleteDbPage(String resource) throws IOException {
        log.info(resource);
        driver.findElement(By.partialLinkText((resource))).click();
        return new DeleteDBPage(driver);
    }
    /**
     * navigate to Delete Database User page
     *
     * @param resource Database Resource
     * @return DeleteDbUserPage
     * @throws IOException for input output exceptions.
     */
    public DeleteDbUserPage gotoDeleteDbUserPage(String resource) throws IOException {
        log.info(resource);
        driver.findElement(By.partialLinkText((resource))).click();
        return new DeleteDbUserPage(driver);
    }
    /**
     * navigate to Delete Database Template Page
     *
     * @param resource database resource name
     * @return DeleteTemplatePage
     * @throws IOException for input output exceptions.
     */
    public DeleteTemplatePage gotoDeleteDbTemplatePage(String resource) throws IOException {
        log.info(resource);
        driver.findElement(By.partialLinkText((resource))).click();
        return new DeleteTemplatePage(driver);
    }

    /**
     * this method is used to sign out from the system.
     */
    public void signOut() {
        log.info("Ready to sign out from the system");
        driver.findElement(By.cssSelector(uiElementMapper.getElement
                ("app.factory.sign.out.email.css.value"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.factory.sing.out.link.text"))).click();

        log.info("log out from the app factory");
    }
}
