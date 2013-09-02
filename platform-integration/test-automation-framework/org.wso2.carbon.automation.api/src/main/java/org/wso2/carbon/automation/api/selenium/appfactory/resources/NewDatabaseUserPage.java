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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class NewDatabaseUserPage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public NewDatabaseUserPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("createdbuser.jag"))) {
            throw new IllegalStateException("This is not the new database user page");
        }
    }
    /**
     * this method is used to create a new Database User
     *
     * @param username            user name
     * @param password            password
     * @param databaseEnvironment database environment
     * @return DatabaseConfigurationPage
     * @throws IOException          for input output exceptions.
     * @throws InterruptedException for thread sleeps.
     */
    public DatabaseConfigurationPage createNewDatabaseUser(String username, String password,
                                                           String databaseEnvironment) throws IOException, InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.user.CheckBox.id")))
                .sendKeys(username);
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.user.password")))
                .sendKeys(password);
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.user.Repeat.password.id")))
                .sendKeys(password);
        new Select(driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.environment.id")))).
                selectByVisibleText(databaseEnvironment);
        driver.findElement(By.name(uiElementMapper.getElement("app.factory.database.user.submit.name")))
                .click();
        //this thread waits until database user creation process is successful
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText
                (uiElementMapper.getElement("app.overview.button.link.text"))));
        return new DatabaseConfigurationPage(driver);
    }
}
