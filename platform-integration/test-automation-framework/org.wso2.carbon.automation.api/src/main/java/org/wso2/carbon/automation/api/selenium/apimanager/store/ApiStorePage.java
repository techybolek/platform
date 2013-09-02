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

package org.wso2.carbon.automation.api.selenium.apimanager.store;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.apimanager.subscription.SubscriptionPage;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class ApiStorePage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public ApiStorePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.
        if (!driver.findElement(By.className(uiElementMapper.getElement("app.api.manager.class.name.text"))).
                getText().contains("APIs")) {
            throw new IllegalStateException("This is not the api home Page");
        }
    }

    /**
     * this method uses to subscribe to Api Manager
     *
     * @param appName name of the application
     * @return SubscriptionPage
     * @throws IOException input output exception
     */
    public SubscriptionPage subscribeToApiManager(String appName)
            throws IOException {
        driver.findElement(By.cssSelector(uiElementMapper.getElement
                ("app.factory.subscribe.api.css.Value"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id
                (uiElementMapper.getElement("app.api.select.app.name"))));
        new Select(driver.findElement(By.id(uiElementMapper.getElement("app.api.select.app.name")))).
                selectByVisibleText(appName);
        driver.findElement(By.id(uiElementMapper.getElement
                ("app.api.subscribe.button.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.go.to.subscriptions.link.text"))).click();

        return new SubscriptionPage(driver);
    }
    /**
     * navigates to Subscribe API Page
     *
     * @return SubscriptionPage
     * @throws IOException          input output exceptions
     * @throws InterruptedException for thread sleeps
     */
    public SubscriptionPage gotoSubscribeAPiPage() throws IOException, InterruptedException {

        driver.findElement(By.linkText(uiElementMapper.getElement("app.factory.subscription.page.link.text")))
                .click();
        //This Thread waits until Subscription Page gets loaded.
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id
                (uiElementMapper.getElement("api.subscription.header.css.value"))));
        return new SubscriptionPage(driver);
    }
}
