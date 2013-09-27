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
package org.wso2.carbon.esb.car.deployment.test;

import org.apache.commons.lang.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.application.mgt.ApplicationAdminClient;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
import org.wso2.carbon.esb.ESBIntegrationTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class RuleMediatorCappTestCase extends ESBIntegrationTest {

    private CarbonAppUploaderClient carbonAppUploaderClient;
    private ApplicationAdminClient applicationAdminClient;
    private final String carFileName = "esb-artifacts-rule-mediator-car";
    private final int MAX_TIME = 120000;

    @BeforeTest(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        carbonAppUploaderClient = new CarbonAppUploaderClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        carbonAppUploaderClient.uploadCarbonAppArtifact("esb-artifacts-rule-mediator-car_1.0.0.car"
                , new DataHandler(new URL("file:" + File.separator + File.separator + getESBResourceLocation()
                + File.separator + "car" + File.separator + "esb-artifacts-rule-mediator-car_1.0.0.car")));
        applicationAdminClient = new ApplicationAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        Assert.assertTrue(isCarFileDeployed(carFileName));
        TimeUnit.SECONDS.sleep(5);
    }

    @Test(groups = {"wso2.esb"}, description = "test proxy service deployment from car file")
    public void proxyServiceDeploymentTest() throws Exception {
        Assert.assertTrue(esbUtils.isProxyDeployed(esbServer.getBackEndUrl(), esbServer.getSessionCookie(), "proxyService2")
                , "Pass Through Proxy service deployment failed");
    }

    private boolean isCarFileDeployed(String carFileName) throws Exception {

        log.info("waiting " + "180000" + " millis for car deployment " + carFileName);
        boolean isCarFileDeployed = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < MAX_TIME) {
            String[] applicationList = applicationAdminClient.listAllApplications();
            if (applicationList != null) {
                if (ArrayUtils.contains(applicationList, carFileName)) {
                    isCarFileDeployed = true;
                    log.info("car file deployed in " + time + " mills");
                    return isCarFileDeployed;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //ignore
            }

        }
        return isCarFileDeployed;
    }
}
