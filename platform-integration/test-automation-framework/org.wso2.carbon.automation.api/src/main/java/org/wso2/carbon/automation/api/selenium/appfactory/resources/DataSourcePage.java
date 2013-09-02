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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class DataSourcePage {
    private static final Log log = LogFactory.getLog(ResourceOverviewPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public DataSourcePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("listDatasources.jag"))) {
            throw new IllegalStateException("This is not the Resources Overview page");
        }
    }

    public NewDataSourcePage gotoNewDataSourcePage() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement
                ("app.factory.new.data.source.page.button.xpath"))).click();
        return new NewDataSourcePage(driver);
    }
    /**
     * navigate to Resource Overview Page
     *
     * @return ResourceOverviewPage
     * @throws IOException          input output exception
     * @throws InterruptedException for thread sleeps
     */
    public ResourceOverviewPage gotoResourceOverviewPage() throws IOException, InterruptedException {
        driver.findElement(By.xpath(uiElementMapper.getElement("app.overview.link.css.value"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText
                (uiElementMapper.getElement("app.overview.button.link.text"))));
        log.info("loading the resource overview page");
        return new ResourceOverviewPage(driver);
    }
}