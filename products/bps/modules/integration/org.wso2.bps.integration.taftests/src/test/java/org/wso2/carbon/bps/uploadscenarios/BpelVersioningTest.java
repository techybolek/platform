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
package org.wso2.carbon.bps.uploadscenarios;

import org.apache.axis2.AxisFault;
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
import org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus;
import org.wso2.carbon.bps.BPSMasterTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

public class BpelVersioningTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelVersioningTest.class);

    LimitedInstanceInfoType instanceInfo = null;
    BpelPackageManagementClient bpelManager;
    BpelProcessManagementClient bpelProcrss;
    BpelInstanceManagementClient bpelInstance;

    RequestSender requestSender;
    private LinkedList<String> activeStatus;
    private boolean activeProcessFound;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        init();
        bpelManager = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        bpelProcrss = new BpelProcessManagementClient(backEndUrl, sessionCookie);
        bpelInstance = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws InterruptedException, RemoteException, MalformedURLException,
            PackageManagementException {
        uploadBpelForTest("HelloWorld2");

    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel Service with In memory", priority = 0)
    public void getVersion() throws RemoteException, XMLStreamException, InterruptedException,
            ProcessManagementException {

        Thread.sleep(5000);
        LinkedList<String> processBefore = bpelProcrss.getProcessInfoList("HelloWorld2");
        activeStatus = new LinkedList<String>();
        activeProcessFound = false;
        for (String processid : processBefore) {
            if (bpelProcrss.getStatus(processid).equals("ACTIVE")) {
                activeStatus.add(processid);
            }
        }
        sendRequest();
    }

    private void sendRequest() throws XMLStreamException, AxisFault {
        String payLoad = " <p:hello xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                "      <!--Exactly 1 occurrence--><TestPart>test</TestPart>\n" +
                "   </p:hello>";

        String operation = "hello";
        String serviceName = "/HelloService";
        String expectedBefore = "World";
        String expectedAfter = "World-Version";
        requestSender.assertRequest(serviceUrl + serviceName, operation, payLoad,
                1, expectedBefore, true);
    }


    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel Service with In memory", priority = 2)
    public void checkVersion() throws InterruptedException, XMLStreamException, RemoteException,
            ProcessManagementException, PackageManagementException {
        uploadBpelVersionedSample("HelloWorld2");
        LinkedList<String> processAfter = null;
        for (int a = 0; a <= 10; a++) {
            Thread.sleep(5000);
            processAfter = bpelProcrss.getProcessInfoList("HelloWorld2");
            if (bpelProcrss.getStatus(activeStatus.get(0)).equals(ProcessStatus.RETIRED.toString()))
                break;
        }

        for (String process : activeStatus)

        {
            Assert.assertTrue(bpelProcrss.getStatus(process).equals(ProcessStatus.RETIRED.toString()), "Versioning failed : Previous Version " + process + "is still active");
        }

        for (String processInfo : processAfter) {
            if (bpelProcrss.getStatus(processInfo).equals("ACTIVE")) {
                activeProcessFound = true;
                for (String process : activeStatus) {
                    Assert.assertFalse(process.equals(processInfo), "Versioning failed : Previous Version " + processInfo + "is still active");
                }
            }
        }

        sendRequest();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("HelloWorld2");
        adminServiceAuthentication.logOut();
    }


}
