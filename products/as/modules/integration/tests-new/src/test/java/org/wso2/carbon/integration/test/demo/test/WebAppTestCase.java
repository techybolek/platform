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

package org.wso2.carbon.integration.test.demo.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.webapp.mgt.WebAppAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.HttpResponse;
import org.wso2.carbon.automation.utils.as.WebApplicationDeploymentUtil;
import org.wso2.carbon.automation.utils.httpclient.HttpURLConnectionClient;
import org.wso2.carbon.integration.test.ASIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class WebAppTestCase extends ASIntegrationTest {
    String webAppName = "appServer-valid-deploymant-1.0.0";
    String webAppFileName = "appServer-valid-deploymant-1.0.0.war";
    private WebAppAdminClient webAppAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(2);
        webAppAdminClient = new WebAppAdminClient(asServer.getBackEndUrl(), asServer.getSessionCookie());

    }

    @Test(groups = "wso2.as", description = "xxx")
    public void testUploadWebApp() throws Exception {
        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "AS" + File.separator + "war" + webAppFileName;
        webAppAdminClient.warFileUplaoder(filePath);

        assertTrue(WebApplicationDeploymentUtil.isWebApplicationDeployed(asServer.getBackEndUrl(),
                                                                         asServer.getSessionCookie(), webAppName));
    }

    @Test(groups = "wso2.as", dependsOnMethods = "testUploadWebApp")
    public void testInvokeWebApp() throws IOException {
        String webappURL = asServer.getWebAppURL() + "/" + webAppName;
        HttpResponse response = HttpURLConnectionClient.sendGetRequest(webappURL, null);
        assertTrue(response.getData().contains("success"));
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {
        webAppAdminClient.deleteWebAppFile(webAppName);
        assertTrue(WebApplicationDeploymentUtil.isWebApplicationUnDeployed(asServer.getBackEndUrl(),
                                                                           asServer.getSessionCookie(), webAppName));
        super.cleanup();

    }

}
