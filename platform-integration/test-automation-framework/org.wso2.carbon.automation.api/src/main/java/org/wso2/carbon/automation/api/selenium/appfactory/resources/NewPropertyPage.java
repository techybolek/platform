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

public class NewPropertyPage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public NewPropertyPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("resources-add.jag"))) {
            throw new IllegalStateException("This is not the Add new PropertyPage page");
        }
    }

    /**
     * this method is used to create a new property
     *
     * @param propertyName  property name
     * @param propertyType  property type
     * @param description   Description
     * @param propertyValue property value
     * @return EndPointAndRegistryPage
     * @throws IOException          for input output exceptions
     * @throws InterruptedException for thread sleeps.
     */
    public EndPointAndRegistryPage createNewProperty(String propertyName, String propertyType,
                                                     String description, String propertyValue)
            throws IOException, InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("app.property.name.id")))
                .sendKeys(propertyName);
        new Select(driver.findElement(By.id(uiElementMapper.getElement("app.property.type.id")))).
                selectByVisibleText(propertyType);
        driver.findElement(By.id(uiElementMapper.getElement("app.property.description.id")))
                .sendKeys(description);
        driver.findElement(By.id(uiElementMapper.getElement("app.property.value.id")))
                .sendKeys(propertyValue);
        driver.findElement(By.id(uiElementMapper.getElement("app.data.source.add.button.name"))).click();
        //this thread waits until data source creation
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                (uiElementMapper.getElement("app.factory.new.data.source.page.button.xpath"))));
        return new EndPointAndRegistryPage(driver);
    }

    /**
     * this method use to delete a property
     *
     * @return EndPointAndRegistryPage
     * @throws IOException          for input output exception
     * @throws InterruptedException for thread sleeps.
     */
    public EndPointAndRegistryPage deleteProperty() throws IOException, InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("app.property.value.delete.id"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText
                (uiElementMapper.getElement("app.property.ok.button.link.text"))));
        driver.findElement(By.linkText(uiElementMapper.getElement("app.property.ok.button.link.text"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                (uiElementMapper.getElement("app.data.add.property"))));
        return new EndPointAndRegistryPage(driver);
    }
}
