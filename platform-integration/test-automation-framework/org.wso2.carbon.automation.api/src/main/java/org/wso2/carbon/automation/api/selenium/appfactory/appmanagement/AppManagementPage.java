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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.appfactory.resources.ResourceOverviewPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

public class AppManagementPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriverWait wait;

    public AppManagementPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("application.jag"))) {
            throw new IllegalStateException("this is not the Application Management Page");
        }
    }
    /**
     * this method is used to check the application details accuracy in the application Overview page.
     *
     * @param repositoryType  repository details of the application.
     * @param appOwner        appOwner of the Application .
     * @param Description     Description of the application .
     * @param applicationType Type of the Application.
     * @param applicationKey  Key of the Application.
     * @return boolean value of the application state.
     * @throws Exception if application details are not matching.
     */
    public boolean isAppDetailsAvailable(String repositoryType, String appOwner, String Description,
                                         String applicationType, String applicationKey) throws Exception {
        String repositoryTypeName = driver.findElement(By.id(uiElementMapper.getElement
                ("app.overview.page.repository.type.id"))).getText();

        String appOwnerName = driver.findElement(By.id(uiElementMapper.getElement
                ("app.overview.page.app.owner.id"))).getText().toUpperCase();
        String DescriptionOfApp = driver.findElement(By.id(uiElementMapper.getElement
                ("app.overview.page.app.description.id"))).getText();
        String applicationTypeOfApp = driver.findElement(By.id(uiElementMapper.getElement
                ("app.overview.page.app.type.id"))).getText();
        String applicationKeyOfApp = driver.findElement(By.xpath(uiElementMapper.getElement
                ("app.overview.page.app.key.xpath"))).getText();


        if (repositoryType.equals(repositoryTypeName)) {
            log.info("repository type is equal");
        } else {
            throw new Exception("repository types are not equal");
        }

        if (appOwner.equals(appOwnerName)) {
            log.info("app owner name is equal");
        } else {
            throw new Exception("app owner names are not equal");
        }

        if (Description.equals(DescriptionOfApp)) {
            log.info("Description of the app is equal");
        } else {
            throw new Exception("Description of app is not equal");
        }

        if (applicationType.equals(applicationTypeOfApp)) {
            log.info("application type is equal");
        } else {
            throw new Exception("application types are not equal");
        }

        if (applicationKey.equals(applicationKeyOfApp)) {
            log.info("application key is equal");
        } else {
            throw new Exception("application keys are not equal");
        }
        return true;
    }


    /**
     * this method use to edit the description of the application
     *
     * @param newDescription new description to be enter.
     * @throws InterruptedException for thread sleeps.
     */
    public void editApplicationDescription(String newDescription) throws InterruptedException {
        //only description could be edited by now later this method will give the functionality to
        //edit the rest of the values
        driver.findElement(By.id(uiElementMapper.getElement("app.overview.page.app.description.id"))).click();
        //this wait until overview page loads
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id
                (uiElementMapper.getElement("app.new.add.app.edit.Description.id"))));
        driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.edit.Description.id"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("app.new.add.app.edit.Description.id")))
                .sendKeys(newDescription);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("updateDescription()");
        log.info("Description is updated");
    }


    /**
     * this method use to check the Description editing of the Application.
     *
     * @param expectedNewDescription edited description of the app
     * @return the description state of the application.
     */
    public boolean isApplicationDescriptionEdited(String expectedNewDescription)  {
        log.info("checking the edited text of the description text area");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id
                (uiElementMapper.getElement("app.overview.page.app.description.id"))));
        String newDescription = driver.findElement(By.id(uiElementMapper.getElement
                ("app.overview.page.app.description.id"))).getText();
        log.info("-------------------------------------");
        log.info(expectedNewDescription);
        log.info(newDescription);

        if (expectedNewDescription.equals(newDescription)) {
            log.info("Application Description edit is successful");
            return true;
        }

        log.info("Application Description edit is unsuccessful");
        return false;
    }
    /**
     * this method is used to LifeCycleManagementPage
     *
     * @return AppLifeCycleManagementPage
     * @throws IOException on input error
     */
    public AppLifeCycleManagementPage gotoLifeCycleManagePage() throws IOException {
        driver.findElement(By.id(uiElementMapper.getElement("app.navigate.Governance.page.id"))).click();
        return new AppLifeCycleManagementPage(driver);
    }

    /**
     * this method is used to gotoIssue page
     *
     * @return gotoIssuePage
     * @throws IOException on input error
     */
    public IssuePage gotoIssuePage() throws IOException {
        driver.findElement(By.id(uiElementMapper.getElement("app.navigate.issue.page.link.id"))).click();
        return new IssuePage(driver);
    }

    /**
     * this method is used to go to team page
     *
     * @return gotoTeamPage
     * @throws IOException on input error
     */
    public TeamPage gotoTeamPage() throws IOException {
        driver.findElement(By.id(uiElementMapper.getElement("app.team.page.id"))).click();
        return new TeamPage(driver);
    }

    /**
     * this method is used to navigate to ResourceOverviewPage
     *
     * @return ResourceOverviewPage
     * @throws IOException on input error
     */
    public ResourceOverviewPage gotoResourceOverviewPage() throws IOException {
        driver.findElement(By.id(uiElementMapper.getElement("app.factory.db.admin.id"))).click();
        return new ResourceOverviewPage(driver);
    }


    /**
     * this method is used to navigate to RepositoryAndBuildPage
     *
     * @return RepositoryAndBuildPage
     * @throws IOException on input error
     */
    public RepositoryAndBuildPage gotoRepositoryAndBuildPage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.navigate.link.text"))).click();
        return new RepositoryAndBuildPage(driver);
    }
    /**
     * This method is used to check the Team Details.
     *
     * @param teamMember checking member of the team
     * @return team details status
     */
    public boolean isTeamDetailsAvailable(String teamMember) {
        String memberDetails = driver.findElement(By.id(uiElementMapper.getElement
                ("app.overview.page.team.details.id"))).getText();
        if (memberDetails.contains(teamMember)) {
            log.info(teamMember + "Team Details Are Available");
            return true;
        }
        return false;
    }

    /**
     * this method is used to sign out from the AppManagement Page.
     */
    public void signOut() {
        log.info("Ready to sign out from the system");
        driver.findElement(By.cssSelector(uiElementMapper.getElement
                ("app.factory.sign.out.email.css.value"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.factory.sing.out.link.text"))).click();

        log.info("log out from the app factory");
    }


    /**
     * this method is used to check the Build details of the application
     *
     * @param buildVersion checking version.
     * @return build status
     * @throws InterruptedException used in thread sleep
     */
    public String isBuildDetailsAccurate(String buildVersion) throws InterruptedException {
        String buildStatus = "";
        //this thread waits until deployment details loads to the overview Page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id
                (uiElementMapper.getElement("app.trunk.overview.xpath"))));
        log.info("Verifying the Build Details of the application");
        String version = driver.findElement(By.xpath(uiElementMapper.getElement("app.trunk.overview.xpath")))
                .getText();
        if (buildVersion.equals(version))

        {
            log.info("Trunk of the Application");

            buildStatus = driver.findElement(By.xpath(uiElementMapper.getElement
                    ("app.trunk.build.status.xpath"))).getText();
            log.info(buildStatus);
            return buildStatus;
        } else {

            //constructing the Xpath in order to traverse the Table
            String XpathConstructorFistPart = "/html/body/div/div/article/section[3]/div/ul[";
            String XpathConstructorSecondPart = "]/li/p/strong";

            for (int XpathTableValue = 2; XpathTableValue < 10; XpathTableValue++) {
                String versionXpath = XpathConstructorFistPart + XpathTableValue
                        + XpathConstructorSecondPart;
                String versionName = driver.findElement(By.xpath(versionXpath)).getText();
                log.info("val on app Factory Page -------> " + versionName);
                log.info("Value Passed in test    -------> " + buildVersion);

                try {

                    if (buildVersion.equals(versionName)) {

                        String buildStatusXpath = "/html/body/div/div[2]/article/section[3]/div/ul[";
                        String buildStatusXpath2 = "]/li[4]/p/span/strong";
                        String xpathConstructForBuild = buildStatusXpath + XpathTableValue + buildStatusXpath2;
                        buildStatus = driver.findElement(By.xpath(xpathConstructForBuild)).getText();
                        return buildStatus;
                    }
                } catch (NoSuchElementException ex) {
                    log.error("Cannot Find the Uploaded Profile", ex);
                    throw ex;
                }
            }
        }

        return buildStatus;
    }
}
