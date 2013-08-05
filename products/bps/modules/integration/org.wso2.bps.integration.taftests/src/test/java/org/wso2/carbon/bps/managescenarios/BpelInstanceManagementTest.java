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

import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelInstanceManagementClient;

import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.carbon.bps.BPSMasterTest;


import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class BpelInstanceManagementTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelInstanceManagementTest.class);

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

    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws InterruptedException, RemoteException, MalformedURLException, PackageManagementException {
        uploadBpelForTest("TestPickOneWay");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Set setvice to Active State", priority = 1)
    public void testCreateInstance()
            throws InterruptedException, XMLStreamException, RemoteException,
            ProcessManagementException, InstanceManagementException {
        EndpointReference epr = new EndpointReference(backEndUrl + "PickService" + "/" + "dealDeck");
        requestSender.sendRequest("<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                "   <pic:Deck>testPick</pic:Deck>" +
                "</pic:dealDeck>", epr);
        Thread.sleep(5000);
        PaginatedInstanceList instanceList = bpelInstance.filterPageInstances(bpelProcrss.getProcessId("PickProcess"));
        instanceInfo = instanceList.getInstance()[0];
        if (instanceList.getInstance().length == 0) {
            Assert.fail("Instance failed to create");
        }
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 2)
    public void testSuspendInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.performAction(instanceInfo.getIid(), BpelInstanceManagementClient.InstanceOperation.SUSPEND);
        Thread.sleep(5000);
        Assert.assertTrue(bpelInstance.getInstanceInfo(instanceInfo.getIid()).getStatus().getValue().equals("SUSPENDED"), "The Service Is not Suspended");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 3)
    public void testResumeInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.performAction(instanceInfo.getIid(), BpelInstanceManagementClient.InstanceOperation.RESUME);
        Thread.sleep(5000);
        Assert.assertTrue(bpelInstance.getInstanceInfo(instanceInfo.getIid()).getStatus().getValue().equals("ACTIVE"), "The Service Is not Suspended");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 4)
    public void testTerminateInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.performAction(instanceInfo.getIid(), BpelInstanceManagementClient.InstanceOperation.TERMINATE);
        Thread.sleep(5000);
        Assert.assertTrue(bpelInstance.getInstanceInfo(instanceInfo.getIid()).getStatus().getValue().equals("TERMINATED"), "The Service Is not Terminated");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 5)
    public void testDeleteInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.deleteInstance(instanceInfo.getIid());
        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("TestPickOneWay");
        adminServiceAuthentication.logOut();
    }
}
