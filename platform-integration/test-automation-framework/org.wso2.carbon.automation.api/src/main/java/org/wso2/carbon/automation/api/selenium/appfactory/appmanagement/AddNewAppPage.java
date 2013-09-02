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

package org.wso2.carbon.automation.api.selenium.appfactory.appmanagement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppHomePage;
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppLogin;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

/**
 * This class adds a new application fo the AppFactory
 */
public class AddNewAppPage {
    private static final Log log = LogFactory.getLog(AppLogin.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public AddNewAppPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 45000);
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("createapplication.jag"))) {
            throw new IllegalStateException("This is not the Create Application page");
        }
    }
    /**
     * this method is used to create a new application for the AppFactory
     *
     * @param appName        application name .
     * @param appKey         application Key.
     * @param iconPath       icon path of the application .
     * @param description    description of the application .
     * @param appType        type of the application .
     * @param repositoryType repository type of the application .
     * @return AppHOmePage.
     * @throws IOException          on input error
     **/
    public AppHomePage createAnApplication(String appName, String appKey, String iconPath
            , String description, String appType, String repositoryType)
            throws IOException {
        try {
            driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.name.id"))).sendKeys(appName);
            driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.key.id"))).clear();
            driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.key.id"))).sendKeys(appKey);
            driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.icon.id"))).sendKeys(iconPath);
            driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.Description.id"))).sendKeys(description);
            new Select(driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.type")))).
                    selectByVisibleText(appType);
            new Select(driver.findElement(By.id(uiElementMapper.getElement("app.new.add.repository.type")))).
                    selectByVisibleText(repositoryType);
            //this  thread sleep is to wait till add button appears
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(uiElementMapper
                    .getElement("app.AddNew.App.link.text"))));
            log.info("Application Creation is successful");
            return new AppHomePage(driver);
        } catch (Exception ex) {
            throw new IllegalStateException("Create Application Process is unsuccessful");
        }
    }
}







