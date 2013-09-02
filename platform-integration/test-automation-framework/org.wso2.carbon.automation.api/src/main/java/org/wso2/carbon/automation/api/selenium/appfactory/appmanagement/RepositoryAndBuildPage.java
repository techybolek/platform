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
import org.wso2.carbon.automation.api.selenium.appfactory.home.AppLogin;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class RepositoryAndBuildPage {
    private static final Log log = LogFactory.getLog(AppLogin.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public RepositoryAndBuildPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("reposBuilds.jag"))) {
            throw new IllegalStateException("This is not the Repository and Build page");
        }
    }


    /**
     * This method is used to create a branch from the trunk of the Application
     *
     * @param branchVersion branch version of the application
     * @throws InterruptedException for Thread sleeps.
     */
    public void createBranchFromTrunk(String branchVersion) throws InterruptedException {
        log.info("Creating a Branch from the Trunk");
        driver.findElement(By.linkText(uiElementMapper.getElement("app.add.branch.link.text"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("app.add.branch.version.id"))).sendKeys(branchVersion);
        driver.findElement(By.xpath(uiElementMapper.getElement("app.add.branch.button.xpath"))).click();
        //waits until Branch build and deploy
        Thread.sleep(15000);
        driver.navigate().refresh();
        log.info("Branch is created from the trunk");
    }

    /**
     * This method is used to check the created branch of the application.
     *
     * @param branchVersionChecking version of the branch.
     * @return branch creation status
     * @throws Exception on exception
     */
    public boolean checkBranchCreated(String branchVersionChecking) throws Exception {
        String branchVersion = driver.findElement(By.xpath(uiElementMapper.getElement
                ("app.add.branch.button.xpath"))).getText();
        if (branchVersionChecking.equals(branchVersion)) {
            log.info("version is matching");
        } else {
            throw new Exception("Branch is not Build Accurately" + branchVersionChecking);
        }
        return true;
    }

    //this method is used to create a branch from a branch version
    /**
     * This method is used to create a branch from a version.
     *
     * @param branchID      branch ID of the application.
     * @param branchVersion branch version of the application.
     * @throws InterruptedException for thread sleeps
     */
    public void createBranchFromVersion(String branchID, String branchVersion)
            throws InterruptedException {
        log.info("Creating a Branch from the 1st branch");
        String tmp = branchID.replace('.', '_');
        String branch = "create_branch" + tmp;
        log.info("BranchID= " + branch);
        driver.findElement(By.xpath(uiElementMapper.getElement("app.add.second.branch.xpath"))).click();
        driver.findElement(By.id((branch))).sendKeys(branchVersion);
        driver.findElement(By.xpath(uiElementMapper.getElement("app.add.branch.two.button.xpath"))).click();
        //wait until branch build and deploy
        Thread.sleep(15000);
        driver.navigate().refresh();
        log.info("Branch is created from the 1st branch");
    }
    /**
     * this method is used to navigate to Life Cycle Management Page.
     */
    public void gotoLifeCycleManagementPage() {
        driver.findElement(By.id(uiElementMapper.getElement("app.life.cycle.management.page.main.id")))
                .click();
    }
    /**
     * this method is used to sing out from the page.
     */
    public void signOut() {
        log.info("Ready to sign out from the system");
        driver.findElement(By.cssSelector(uiElementMapper.getElement
                ("app.factory.sign.out.email.css.value"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.factory.sing.out.link.text"))).click();

        log.info("log out from the app factory");
    }
}
