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
package org.wso2.carbon.bps.managescenarios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelInstanceManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bps.BPSMasterTest;


import java.io.File;
import java.rmi.RemoteException;

public class BpelProcessManagementTest extends BPSMasterTest {
    private static final Log log = LogFactory.getLog(BpelInstanceManagementTest.class);

    LimitedInstanceInfoType instanceInfo = null;
    BpelPackageManagementClient bpelManager;
    org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient bpelProcrss;
    BpelInstanceManagementClient bpelInstance;

    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        init();
        bpelManager = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        bpelProcrss = new org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient(backEndUrl, sessionCookie);
        bpelInstance = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact() throws InterruptedException, RemoteException, PackageManagementException {
        uploadBpelForTest("LoanService");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Set setvice to Retire State", priority=1)
    public void testServiceRetire() throws ProcessManagementException, RemoteException {
        try {
            String processID = bpelProcrss.getProcessId("XKLoanService");
            bpelProcrss.setStatus(processID, "RETIRED");
            Assert.assertTrue(bpelProcrss.getStatus(processID).equals("RETIRED"), "PPEL process is not set as RETIRED");
            Assert.assertFalse(requestSender.isServiceAvailable(serviceUrl + "/XKLoanService"), "Service is still available");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Process management failed " + e);
            Assert.fail("Process management failed " + e);
        }
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Set setvice to Active State",priority=2)
    public void testServiceActive() throws ProcessManagementException, RemoteException {
        try {
            String processID = bpelProcrss.getProcessId("XKLoanService");
            bpelProcrss.setStatus(processID, "ACTIVE");
            Thread.sleep(5000);
            Assert.assertTrue(bpelProcrss.getStatus(processID).equals("ACTIVE"), "PPEL process is not set as ACTIVE");
            Assert.assertTrue(requestSender.isServiceAvailable(serviceUrl + "/XKLoanService"), "Service is not available");
        } catch (InterruptedException e) {
            log.error("Process management failed" + e);
            Assert.fail("Process management failed" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts() throws Exception {
       bpelManager.undeployBPEL("LoanService");
       adminServiceAuthentication.logOut();
    }
}
