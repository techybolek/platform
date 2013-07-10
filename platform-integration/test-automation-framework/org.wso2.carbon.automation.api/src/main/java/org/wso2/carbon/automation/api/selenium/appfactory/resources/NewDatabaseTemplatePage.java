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
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class NewDatabaseTemplatePage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public NewDatabaseTemplatePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("createdbtemplate.jag"))) {
            throw new IllegalStateException("This is not the Resources Overview page");
        }
    }

    public DatabaseConfigurationPage createDatabaseTemplate(String templateName) throws IOException, InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.database.template.name")))
                .sendKeys(templateName);
        driver.findElement(By.name(uiElementMapper.getElement("app.factory.database.template.submit.name")))
                .click();
        //this thread sleep waits until database template creates
        Thread.sleep(15000);
        return new DatabaseConfigurationPage(driver);
    }
}
