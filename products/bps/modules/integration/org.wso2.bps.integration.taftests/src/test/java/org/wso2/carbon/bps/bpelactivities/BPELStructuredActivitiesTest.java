/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.bps.bpelactivities;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bps.BPSMasterTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BPELStructuredActivitiesTest extends BPSMasterTest{
    private static final Log log = LogFactory.getLog(BPELStructuredActivitiesTest.class);

    BpelPackageManagementClient bpelManager;
    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        init();
        bpelManager = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws Exception {
        uploadBpelForTest("TestForEach");
        uploadBpelForTest("TestPickOneWay");
        uploadBpelForTest("TestFlowLinks");
        requestSender.waitForProcessDeployment(serviceUrl + File.separator + "FlowLinkTest");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "for each in structured activities",
            dependsOnMethods = "flowLinks")
    private void forEach() throws Exception {
        String payload = "<input xmlns=\"http://www.example.org/jms\">in</input>";
        String operation = "start";
        String serviceName = "ForEachService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("123");

        log.info("Service: " + serviceUrl + serviceName);
        requestSender.sendRequest(serviceUrl + File.separator + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "flow links in structured activities")
    private void flowLinks() throws Exception {
        String payload = "<ns1:ExecuteWorkflow xmlns:ns1=\"workflowns\"><value>foo</value>" +
                "</ns1:ExecuteWorkflow>";
        String operation = "ExecuteWorkflow";
        String serviceName = "FlowLinkTest";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("foo");

        log.info("Service: " + serviceUrl + serviceName);
        requestSender.sendRequest(serviceUrl + File.separator + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "pick one way in structured activities",
            dependsOnMethods = "forEach")
    private void pickOneWay() throws Exception {
        dealDeck();
        pickDiamond();
    }

    private void dealDeck() throws Exception {
        String payload = "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\"><pic:Deck>one</pic:Deck></pic:dealDeck>";
        String operation = "dealDeck";
        String serviceName = "PickService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">one<");

        log.info("Service: " + serviceUrl + serviceName);
        requestSender.sendRequest(serviceUrl + File.separator + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    private void pickDiamond() throws Exception {
        String payload = "<pic:pickDiamond xmlns:pic=\"http://www.stark.com/PickService\"><pic:Deck>one</pic:Deck></pic:pickDiamond>";
        String operation = "pickDiamond";
        String serviceName = "PickService";

        log.info("Service: " + serviceUrl + serviceName);
        requestSender.sendRequest(serviceUrl + File.separator + serviceName, operation, payload,
                1, new ArrayList<String>(), false);
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("TestFlowLinks");
        bpelManager.undeployBPEL("TestForEach");
        bpelManager.undeployBPEL("TestPickOneWay");
        adminServiceAuthentication.logOut();
    }
}
