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

public class NewDataSourcePage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public NewDataSourcePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("dbadministration.jag"))) {
            throw new IllegalStateException("This is not the Data Source page");
        }
    }

    /**
     * this method is used to create new data source
     *
     * @param dataSourceName data source name
     * @param description    description of the data source
     * @param passWord       password
     * @return DataSourcePage
     * @throws IOException          for input output exceptions    app.factory.new.data.source.page.button.xpath
     */
    public DataSourcePage createNewDataSource(String dataSourceName, String description,
                                              String passWord) throws IOException {
        driver.findElement(By.id(uiElementMapper.getElement("app.data.source.name.id")))
                .sendKeys(dataSourceName);
        driver.findElement(By.id(uiElementMapper.getElement("app.data.source.description.id")))
                .sendKeys(description);
        driver.findElement(By.id(uiElementMapper.getElement("app.data.source.password.id")))
                .sendKeys(passWord);
        driver.findElement(By.name(uiElementMapper.getElement("app.data.source.add.button.name"))).click();
        //this thread waits until data source creation
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                (uiElementMapper.getElement("app.factory.new.data.source.page.button.xpath"))));
        return new DataSourcePage(driver);
    }
}