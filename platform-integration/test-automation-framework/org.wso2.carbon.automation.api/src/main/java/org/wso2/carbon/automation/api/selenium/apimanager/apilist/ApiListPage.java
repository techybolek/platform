package org.wso2.carbon.automation.api.selenium.apimanager.apilist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

public class ApiListPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebDriverWait wait;

    public ApiListPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        wait = new WebDriverWait(this.driver, 30000);
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("carbon.Main.tab"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("api.list.link"))).click();

        log.info("API List Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("api.dashboard.middle.text"))).
                getText().contains("API List")) {

            throw new IllegalStateException("This is not the API  Add Page");
        }
    }
    /**
     * this method method uses to check the uploaded API exists or not
     *
     * @param apiName name of the api
     * @return availability status of the API
     * @throws InterruptedException for thread sleeps.
     */
    public boolean checkOnUploadApi(String apiName) throws InterruptedException {
        log.info(apiName);
        Thread.sleep(5000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("firstElementXpath")));
        String firstElementXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/" +
                "form[4]/table/tbody/tr/td/a";
        String apiNameOnServer = driver.findElement(By.xpath(firstElementXpath)).getText();
        log.info(apiNameOnServer);
        if (apiName.equals(apiNameOnServer)) {
            log.info("Uploaded Api exists");
            return true;
        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/" +
                    "form[4]/table/tbody/tr[";
            String resourceXpath2 = "]/td/a";
            for (int i = 2; i < 10; i++) {
                String apiNameOnAppServer = resourceXpath + i + resourceXpath2;

                String actualApiName = driver.findElement(By.xpath(apiNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualApiName);
                log.info("Correct is    -------> " + apiName);

                try {
                    if (apiName.contains(actualApiName)) {
                        log.info("Uploaded API    exists");
                        return true;
                    } else {
                        return false;
                    }
                } catch (NoSuchElementException ex) {
                    log.error("cannot Find the API", ex);
                    throw ex;
                }
            }
        }
        return false;
    }
    /**
     * this method uses to promote the lifeCycle
     *
     * @param lifeCycleName life cycle
     */
    public void lifeCyclePromotion(String lifeCycleName)  {
        driver.findElement(By.id(uiElementMapper.getElement("life.cycle.expand.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("life.cycle.add.link.text"))).click();
        new Select(driver.findElement(By.id("aspect"))).selectByVisibleText(lifeCycleName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addAspect()");

        //checking the checkList
        String lifeCycleStage = driver.findElement(By.xpath(uiElementMapper.getElement("life.cycle.stage"))).getText();

        if (lifeCycleStage.contains("Development")) {
            log.info("lifecycle is at the Testing stage");

            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option.id"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiElementMapper
                    .getElement("life.cycle.add.option.id"))));
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option1.id"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiElementMapper
                    .getElement("life.cycle.add.option2.id"))));
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option2.id"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiElementMapper
                    .getElement("life.cycle.promote.id"))));
            //promoting the lifecycle
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.promote.id"))).click();
            driver.findElement(By.cssSelector(uiElementMapper.getElement
                    ("life.cycle.promote.ok.button.css.value"))).click();
            String nextLifeCycleStage = driver.findElement(By.xpath(uiElementMapper.getElement
                    ("life.cycle.stage.xpath"))).getText();

            if (nextLifeCycleStage.contains("Testing")) {
                log.info("lifecycle is at the Testing stage");
            } else {
                log.info("lifecycle is not  at the Testing stage");
                throw new NoSuchElementException();
            }
        } else {
            log.info("lifecycle is not  at the Development stage");
            throw new NoSuchElementException();
        }


        String lifeCycleStage2 = driver.findElement(By.xpath(uiElementMapper.getElement
                ("life.cycle.stage.xpath"))).getText();


        if (lifeCycleStage2.contains("Testing")) {
            log.info("lifecycle is promoting from  Testing stage");

            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option.id"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiElementMapper
                    .getElement("life.cycle.add.option1.id"))));

            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option1.id"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiElementMapper
                    .getElement("life.cycle.add.option2.id"))));
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option2.id"))).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiElementMapper
                    .getElement("life.cycle.promote.id"))));

            //promoting the lifecycle
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.promote.id"))).click();
            driver.findElement(By.cssSelector(uiElementMapper.getElement
                    ("life.cycle.promote.ok.button.css.value"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(uiElementMapper
                    .getElement("life.cycle.stage.xpath"))));

            String FinalLifeCycleStage = driver.findElement(By.xpath(uiElementMapper.getElement
                    ("life.cycle.stage.xpath"))).getText();

            if (FinalLifeCycleStage.contains("production")) {
                log.info("lifecycle is at the production stage");

                driver.findElement(By.id(uiElementMapper.getElement("life.cycle.publish.id"))).click();
                driver.findElement(By.cssSelector(uiElementMapper.getElement
                        ("life.cycle.promote.ok.button.css.value"))).click();
            } else {
                log.info("lifecycle is not at the production stage");
                throw new NoSuchElementException();
            }
        } else {
            log.info("cannot promote the lifecycle its not at the Testing stage");
            throw new NoSuchElementException();
        }
    }

    /**
     * this method promotes the API Life Cycle Stage
     *
     * @param apiName       name of the API
     * @param lifeCycleName Life Cycle Name
     * @return API life Cycle Promotion status
     * @throws InterruptedException for thread sleeps
     */
    public boolean promoteApiLifecycle(String apiName, String lifeCycleName) throws InterruptedException {
        log.info(apiName);
        Thread.sleep(5000);
        String firstElementXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/" +
                "form[4]/table/tbody/tr/td/a";
        String apiNameOnServer = driver.findElement(By.xpath(firstElementXpath)).getText();
        log.info(apiNameOnServer);
        if (apiName.equals(apiNameOnServer)) {
            log.info("Uploaded Api exists");
            driver.findElement(By.xpath(firstElementXpath)).click();
            lifeCyclePromotion(lifeCycleName);
            return true;
        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/" +
                    "form[4]/table/tbody/tr[";
            String resourceXpath2 = "]/td/a";
            for (int i = 2; i < 10; i++) {
                String apiNameOnAppServer = resourceXpath + i + resourceXpath2;

                String actualApiName = driver.findElement(By.xpath(apiNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualApiName);
                log.info("Correct is    -------> " + apiName);

                try {
                    if (apiName.contains(actualApiName)) {
                        log.info("Uploaded API    exists");
                        driver.findElement(By.xpath(apiNameOnAppServer)).click();
                        lifeCyclePromotion(lifeCycleName);
                        return true;
                    } else {
                        return false;
                    }
                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the Uploaded API");
                }
            }
        }
        return false;
    }

    /**
     * this method uses to log out from the page
     *
     * @return LoginPage
     * @throws IOException for input output exceptions.
     */
    public LoginPage logout() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("home.sign.out.link.text"))).click();
        return new LoginPage(driver);
    }
}
