
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
package org.wso2.carbon.bps.bpelactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelInstanceManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.carbon.bps.BPSMasterTest;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BpelActIgnoreMissingFromDataTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelActIgnoreMissingFromDataTest.class);

    LimitedInstanceInfoType instanceInfo = null;
    BpelPackageManagementClient bpelManager;
    BpelProcessManagementClient bpelProcrss;
    BpelInstanceManagementClient bpelInstance;

    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        init();
        bpelManager = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        bpelProcrss = new BpelProcessManagementClient(backEndUrl, sessionCookie);
        bpelInstance = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void deployArtifact()
            throws InterruptedException, RemoteException, MalformedURLException,
                   PackageManagementException {
        uploadBpelForTest("TestCombineUrl");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Invike combine URL Bpel")
    public void testCombineUrl() throws Exception, RemoteException {
        int instanceCount = 0;

        String processID = bpelProcrss.getProcessId("TestCombineUrl");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstance.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.forEachRequest();
                Thread.sleep(5000);
                if (instanceCount >= bpelInstance.filterPageInstances(processID).getInstance().length) {
                    Assert.fail("Instance is not created for the request");
                }
            } catch (InterruptedException e) {
                log.error("Process management failed" + e);
                Assert.fail("Process management failed" + e);
            }
            bpelInstance.clearInstancesOfProcess(processID);
        }
    }

    @AfterClass(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("TestCombineUrl");
        adminServiceAuthentication.logOut();
    }

    public void forEachRequest() throws Exception {
        String payload = "      <p:combineUrl xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "     <base>http://www.google.lk/</base>\n" +
                "     <!--Exactly 1 occurrence-->\n" +
                "      <relative>search</relative>\n" +
                "   </p:combineUrl>";
        String operation = "combineUrl";
        String serviceName = "TestCombineUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("http://www.google.lk/search");
        requestSender.sendRequest(serviceUrl+"/" + serviceName, operation, payload,
                1, expectedOutput, true);
    }
}

