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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;
public class AppLifeCycleManagementPage {
    private static final Log log = LogFactory.getLog(AppLifeCycleManagementPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public AppLifeCycleManagementPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.

        if (!(driver.getCurrentUrl().contains("governance.jag"))) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            throw new IllegalStateException("This is not the Create Application page");
        }
    }

    /**
     * this method is used to sign out from the Application Life Cycle Management Page
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
     * this method is used to navigate to App management page
     */
    public void goToAppManagementPage() {
        driver.findElement(By.linkText(uiElementMapper.getElement
                ("app.factory.main.overview.page.link.text"))).click();
    }

    /**
     * this method is used to promote the life cycle
     *
     * @param version        Branch of the application
     * @param lifeCycleStage lifecycle stage of the application
     * @throws InterruptedException on for thread sleeps.
     */
    public void promoteVersion(String version, String lifeCycleStage)
            throws InterruptedException {
        log.info("waiting to load list of versions");
        //wait until list of versions get load
        Thread.sleep(10000);
        if (lifeCycleStage.equals("Development")) {
            /*Variable XpathValueOfFistSection &XpathValueOfSecondSection is used to construct and
            Traverse through the LC Table of the Governance Page*/
            String XpathValueOfFistSection = "/html/body/div/div[3]/article/section/div[2]/ul/li[";
            String XpathValueOfSecondSection = "]/ul/li/div/strong";
            for (int xpathValue = 2; xpathValue < 10; xpathValue++) {
                String versionXpath = XpathValueOfFistSection + xpathValue + XpathValueOfSecondSection;
                String versionName = driver.findElement(By.xpath(versionXpath)).getText();
                log.info("value on governance page -------> " + versionName);
                log.info("the correct value is     -------> " + version);

                try {
                    if (version.equals(versionName)) {
                        /*adding the date on promoting is removed due to its not related to the
                        function */
                        //Generating the promote button Xpath
                        String promoteButtonXpathFirstSection = "//div[@id='whereItAllGoes']/ul/li[";
                        String promoteButtonXpathSecondSection = "]/ul/li[4]/div/ul/li/button";
                        String promoteButtonXpath = promoteButtonXpathFirstSection + xpathValue +
                                                    promoteButtonXpathSecondSection;
                        driver.findElement(By.xpath((promoteButtonXpath))).click();
                        //this thread sleep is to until loading the code status
                        Thread.sleep(5000);
                        //checking the code status
                        if (!driver.findElement(By.xpath(uiElementMapper.getElement
                                ("app.code.completed.status.xpath"))).isSelected()) {
                            driver.findElement(By.xpath(uiElementMapper.getElement
                                    ("app.code.completed.status.xpath"))).click();
                        }
                        if (!driver.findElement(By.xpath(uiElementMapper.getElement
                                ("app.code.review.status.xpath"))).isSelected()) {
                            driver.findElement(By.xpath(uiElementMapper.getElement
                                    ("app.code.review.status.xpath"))).click();
                        }

                        if (!driver.findElement(By.xpath(uiElementMapper.getElement
                                ("app.design.review.status.xpath"))).isSelected()) {
                            driver.findElement(By.xpath(uiElementMapper.getElement
                                    ("app.design.review.status.xpath"))).click();
                        }
                        //promoting the code after creation of the promote button Xpath.
                        String promotingXpathFirstSection = "//button[@onclick=\"doGovernanceAction(this, " +
                                                            "'Promote','Development', '";
                        String promotingXpathSecondSection = version + "')\"]";
                        String promoteXpath = promotingXpathFirstSection + promotingXpathSecondSection;
                        driver.findElement(By.xpath((promoteXpath))).click();
                        //this thread is to wait until application's promotion.
                        Thread.sleep(45000);
                        break;
                    }
                } catch (NoSuchElementException ex) {

                    log.error("Cannot find the Build version", ex);
                    throw ex;
                }
            }
        } else if (lifeCycleStage.equals("testing")) {
            //checking the first element of the testing stage
            String versionName = driver.findElement(By.xpath
                    ("/html/body/div/div[3]/article/section/div[2]/ul[2]/li/ul/li/div/strong")).getText();
            if (version.equals(versionName)) {
                //removed the date adding part it is not related to the governance logic
                driver.findElement(By.xpath(("//div[@id='whereItAllGoes']/ul[2]/li/ul/li[4]/div/ul/" +
                                             "li/button"))).click();
                Thread.sleep(5000);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                        (uiElementMapper.getElement("app.code.completed.status.xpath"))));

                if (!driver.findElement(By.xpath(uiElementMapper.getElement("app.code.completed.status.xpath")))
                        .isSelected()) {
                    driver.findElement(By.xpath(uiElementMapper.getElement("app.code.completed.status.xpath")))
                            .click();
                }
                if (!driver.findElement(By.xpath(uiElementMapper.getElement("app.code.review.status.xpath")))
                        .isSelected()) {
                    driver.findElement(By.xpath(uiElementMapper.getElement("app.code.review.status.xpath")))
                            .click();
                }
                //promoting the code after generating the Xpath of the promote Button
                String promoteButtonXpathFirstSection = "//button[@onclick=\"doGovernanceAction(this," +
                                                        " 'Promote','Testing', '";
                String promoteButtonXpathSecondSection = version + "')\"]";
                String promoteButtonXpath = promoteButtonXpathFirstSection + promoteButtonXpathSecondSection;
                driver.findElement(By.xpath((promoteButtonXpath))).click();
                //this thread is to wait until application's promotion.
                Thread.sleep(30000);
            } else {

                String XpathValueOfFistSection = "/html/body/div/div[3]/article/section/div[2]/ul[2]/li[";
                String XpathValueOfSecondSection = "]/ul/li/div/strong";
                for (int xpathValue = 2; xpathValue < 10; xpathValue++) {
                    String testingStageVersionXpath = XpathValueOfFistSection + xpathValue +
                                                      XpathValueOfSecondSection;
                    String versionNameInPage = driver.findElement(By.xpath(testingStageVersionXpath)).getText();
                    log.info("val on app is -------> " + versionNameInPage);
                    log.info("Correct is    -------> " + version);

                    try {

                        if (version.equals(versionNameInPage)) {
                            //generating the Promote Main Button Xpath.
                            String promoteButtonXpathFirstSection = "//div[@id='whereItAllGoes']/ul/li[";
                            String promoteButtonXpathSecondSection = "]/ul/li[4]/div/ul/li/button";
                            String promoteButtonXpath = promoteButtonXpathFirstSection + xpathValue +
                                                        promoteButtonXpathSecondSection;
                            driver.findElement(By.xpath((promoteButtonXpath))).click();
                            //this thread sleep is to until loading the code status
                            Thread.sleep(5000);
                            //checking the code status
                            if (!driver.findElement(By.xpath(uiElementMapper.getElement
                                    ("app.code.completed.status.xpath"))).isSelected()) {
                                driver.findElement(By.xpath(uiElementMapper.getElement
                                        ("app.code.completed.status.xpath"))).click();
                            }
                            if (!driver.findElement(By.xpath(uiElementMapper.getElement
                                    ("app.code.review.status.xpath"))).isSelected()) {
                                driver.findElement(By.xpath(uiElementMapper.getElement
                                        ("app.code.review.status.xpath"))).click();
                            }
                            //promoting the code after creating the Xpath of the promote Button
                            String promotingXpathFirstSection = "//button[@onclick=\"doGovernanceAction" +
                                                                "(this, 'Promote','Testing', '";
                            String promotingXpathSecondSection = version + "')\"]";
                            String promoteXpath = promotingXpathFirstSection + promotingXpathSecondSection;
                            driver.findElement(By.xpath((promoteXpath))).click();
                            //this thread is to wait until application's promotion.
                            Thread.sleep(30000);
                            break;
                        }
                    } catch (NoSuchElementException ex) {
                        log.error("Cannot find the Build version", ex);
                        throw ex;
                    }
                }
            }
        }
    }
}
