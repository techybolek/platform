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

    public NewDatabasePage gotoNewDatabasePage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.factory.new.database.link"))).click();
        return new NewDatabasePage(driver);
    }

    public NewDatabaseUserPage gotoNewDatabaseUserPage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.factory.db.dbUser.link"))).click();
        return new NewDatabaseUserPage(driver);
    }
    public NewDatabaseTemplatePage gotoNewDatabaseTemplatePage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.factory.db.template.link"))).click();
        return new NewDatabaseTemplatePage(driver);
    }

    public boolean isDatabaseDetailsAvailable(String database, String user, String template) {
        String databaseName = driver.findElement(By.partialLinkText(uiElementMapper.getElement
                ("app.db.link"))).getText().toUpperCase();
        String databaseUser = driver.findElement(By.partialLinkText(uiElementMapper.getElement
                ("app.db.user"))).getText().toUpperCase();
        String databaseTemplate = driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.db.template"))).getText().toUpperCase();
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

    public DeleteDBPage gotoDeleteDbPage(String resource) throws IOException {
        log.info(resource);
        driver.findElement(By.partialLinkText((resource))).click();
        return new DeleteDBPage(driver);
    }

    public DeleteDbUserPage gotoDeleteDbUserPage(String resource) throws IOException {
        log.info(resource);
        driver.findElement(By.partialLinkText((resource))).click();
        return new DeleteDbUserPage(driver);
    }

    public DeleteTemplatePage gotoDeleteDbTemplatePage(String resource) throws IOException {
        log.info(resource);
        driver.findElement(By.partialLinkText((resource))).click();
        return new DeleteTemplatePage(driver);
    }
}
