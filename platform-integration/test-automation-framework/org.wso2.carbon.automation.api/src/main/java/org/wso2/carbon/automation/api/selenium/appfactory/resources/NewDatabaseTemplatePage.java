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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class NewDatabaseTemplatePage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public NewDatabaseTemplatePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("createdbtemplate.jag"))) {
            throw new IllegalStateException("This is not the New Database Template page");
        }
    }
    /**
     * This method is used to create database template
     * @param templateName   template name
     * @return  DatabaseConfigurationPage
     * @throws IOException for input output exception
     * @throws InterruptedException for thread sleeps
     */
    public DatabaseConfigurationPage createDatabaseTemplate(String templateName) throws IOException, InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.template.name.id")))
                .sendKeys(templateName);
        driver.findElement(By.name(uiElementMapper.getElement("app.factory.database.template.submit.name")))
                .click();
        //this thread sleep waits until database template creates
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText
                (uiElementMapper.getElement("app.overview.button.link.text"))));
        return new DatabaseConfigurationPage(driver);
    }
}