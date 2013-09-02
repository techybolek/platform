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

package org.wso2.carbon.automation.api.selenium.appfactory.home;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.appfactory.appmanagement.AddNewAppPage;
import org.wso2.carbon.automation.api.selenium.appfactory.appmanagement.AppManagementPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class AppHomePage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public AppHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("index.jag"))) {
            throw new IllegalStateException("This is not the home page");
        }
    }
    /**
     * this method used for logging out/
     *
     * @return LoginPage
     * @throws IOException for input out put exception
     */
    public LoginPage logout() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("home.sign.out.link.text"))).click();
        return new LoginPage(driver);
    }

    //this method will navigate to addNewApplication Page
    /**
     * this method is used to navigate to AddNewApplication page
     *
     * @return AddNewAppPage
     * @throws IOException          for input output exception.
     * @throws InterruptedException for thread sleeps.
     */
    public AddNewAppPage gotoAddNewAppPage() throws IOException, InterruptedException {
        log.info("loading the Home Page");
        //this pause is set until created applications loaded to the home page
        Thread.sleep(15000);
        driver.findElement(By.linkText(uiElementMapper.getElement("app.AddNew.App.link.text"))).click();
        return new AddNewAppPage(driver);
    }

    //this method is used to check the availability of an added application in the home page
    /**
     * this method is used to check whether an added application is available or not
     *
     * @param applicationName name of the application
     * @return application status
     * @throws Exception for exceptions.
     */
    public boolean isApplicationAvailable(String applicationName) throws Exception {
        //this pause is set until created applications loaded to the home page
        Thread.sleep(15000);
        driver.navigate().refresh();
        //this pause is set until created applications Deployment
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector
                (uiElementMapper.getElement("app.factory.list.view"))));
        driver.findElement(By.cssSelector(uiElementMapper
                .getElement("app.factory.list.view"))).click();
        log.info("application is processing");
        driver.findElement(By.id(uiElementMapper.getElement("app.homepage.search.textBox")))
                .sendKeys(applicationName);
        String applicationNameInAppFactory = driver.findElement(By.xpath(uiElementMapper
                .getElement("app.first.element.of.home.page.xpath"))).getText();
        if (applicationName.equals(applicationNameInAppFactory)) {
            log.info("Added Application is Available");
            return true;
        } else {
            throw new Exception("Application is not available");
        }
    }

    //This method will navigate to desired application's overview Page
    /**
     * this method is used to navigate to Application Management Page
     *
     * @param applicationName name of the application
     * @return AppManagementPage
     * @throws IOException          for input output  exceptions
     * @throws InterruptedException for thread sleeps
     */
    public AppManagementPage gotoApplicationManagementPage(String applicationName)
            throws IOException, InterruptedException {
        //this pause is set until created applications loaded to the home page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector
                (uiElementMapper.getElement("app.factory.list.view.css.value"))));
        driver.findElement(By.cssSelector(uiElementMapper
                .getElement("app.factory.list.view.css.value"))).getText();
        Thread.sleep(15000);
        driver.navigate().refresh();
        driver.findElement(By.cssSelector(uiElementMapper
                .getElement("app.factory.list.view.css.value"))).click();
        //this pause is set until created applications Deployment
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText
                (applicationName)));
        driver.findElement(By.linkText((applicationName))).click();
        return new AppManagementPage(driver);
    }
}
