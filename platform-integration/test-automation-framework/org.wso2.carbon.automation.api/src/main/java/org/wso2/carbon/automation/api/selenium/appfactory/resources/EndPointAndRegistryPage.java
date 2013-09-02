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
import org.openqa.selenium.WebElement;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class EndPointAndRegistryPage {
    private static final Log log = LogFactory.getLog(ResourceOverviewPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public EndPointAndRegistryPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("resources.jag"))) {
            throw new IllegalStateException("This is not the End Point And Registry page");
        }
    }
    /**
     * navigate to New Property Page
     *
     * @return NewPropertyPage
     * @throws IOException for input output exceptions.
     */
    public NewPropertyPage gotoNewProperty() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.data.add.property"))).click();
        return new NewPropertyPage(driver);
    }
    /**
     * this method is used to verify the created property exists.
     *
     * @param propertyName property name
     * @return property availability status
     */
    public boolean verifyCreateProperty(String propertyName) {
        WebElement propertyValue = driver.findElement(By.linkText((propertyName)));
        String property = propertyValue.getText();
        if (property.equals(propertyName)) {
            log.info("Added Property Is Available");
            return true;
        }

        return false;
    }

    /**
     * verify the deleted property
     *
     * @return deleted property status.
     */
    public boolean verifyDeleteProperty() {
        WebElement deletedProperty = driver.findElement(By.id(uiElementMapper.getElement
                ("property.value.id")));
        String propertyValue = deletedProperty.getText();
        if (propertyValue.equals("Registries and Endpoints have not been created yet.")) {
            log.info("Property is deleted");
            return true;
        }

        return false;
    }

    /**
     * navigate to Delete New Property Page.
     *
     * @param propertyName property name
     * @return NewPropertyPage
     * @throws IOException for input output exceptions.
     */
    public NewPropertyPage gotoDeleteNewPropertyPage(String propertyName) throws IOException {
        driver.findElement(By.linkText((propertyName))).click();
        return new NewPropertyPage(driver);
    }
}
