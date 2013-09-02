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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class NewDatabasePage {
    private static final Log log = LogFactory.getLog(ResourceOverviewPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public NewDatabasePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("newdatabase.jag"))) {
            throw new IllegalStateException("This is not the New Database page");
        }
    }
    /**
     * this method is used to create a database.
     *
     * @param databaseName Database name
     * @param passWord     password
     * @return DatabaseConfigurationPage
     * @throws IOException          for input output exception
     * @throws InterruptedException for thread sleeps
     */
    public DatabaseConfigurationPage createDatabaseDefault(String databaseName, String passWord) throws IOException, InterruptedException {
        log.info("loading the Create Database Page");
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.name.id"))).sendKeys(databaseName);
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.password.id"))).sendKeys(passWord);
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.confirm.password.id"))).sendKeys(passWord);
        driver.findElement(By.cssSelector(uiElementMapper.getElement
                ("app.factory.database.submit.button.css.value"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText
                (uiElementMapper.getElement("app.overview.button.link.text"))));
        return new DatabaseConfigurationPage(driver);
    }

    /**
     * this method is used to Create a customized database
     *
     * @param databaseName  database name
     * @param passWord      password
     * @param dbEnvironment database environment
     * @param user          database user
     * @param template      database template
     * @return DatabaseConfigurationPage
     * @throws IOException          for input output exception.
     * @throws InterruptedException for thread sleeps.
     */
    public DatabaseConfigurationPage createDatabaseCustomised(String databaseName, String passWord,
                                                              String dbEnvironment, String user, String template) throws IOException, InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.name.id")))
                .sendKeys(databaseName);
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.password.id")))
                .sendKeys(passWord);
        driver.findElement(By.cssSelector(uiElementMapper.getElement
                ("app.factory.database.advance.Checkbox.css.value")))
                .click();
        //going for the advanced option of creating the database
        //This thread will ease the driver to do the selection
        Thread.sleep(5000);
        //selecting the database environment
        new Select(driver.findElement(By.id(uiElementMapper.getElement("app.database.db.environment.id")))).
                selectByVisibleText(dbEnvironment);
        //selecting the user
        //thread waits for the selection
        Thread.sleep(1000);
        new Select(driver.findElement(By.id(uiElementMapper.getElement("app.database.db.environment.user.id")))).
                selectByVisibleText(user);
        //selecting the template
        //Thread waits for the selection
        Thread.sleep(1000);
        new Select(driver.findElement(By.id(uiElementMapper.getElement("app.database.db.environment.template.id")))).
                selectByVisibleText(template);
        driver.findElement(By.cssSelector(uiElementMapper.getElement("app.factory.database.submit.button.css.value"))).click();
        return new DatabaseConfigurationPage(driver);
    }
}