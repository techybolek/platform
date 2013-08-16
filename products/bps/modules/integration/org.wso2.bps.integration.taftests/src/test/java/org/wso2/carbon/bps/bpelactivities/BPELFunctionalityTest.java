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
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BPELFunctionalityTest extends BPSMasterTest{
    private static final Log log = LogFactory.getLog(BPELFunctionalityTest.class);

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
        uploadBpelForTest("TestAlarm");
        uploadBpelForTest("DynPartner");
        uploadBpelForTest("TestForEach");
        uploadBpelForTest("TestPickOneWay");
        requestSender.waitForProcessDeployment(serviceUrl + File.separator + "PickService");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "onAlarm BPEL functionality test case")
    public void processProperty() throws AxisFault, XMLStreamException {
        String payload = "<exam:start xmlns:exam=\"http://ode.apache.org/example\">4</exam:start>";
        String operation = "receive";
        String serviceName = "/CanonicServiceForClient";
        String expectedOutput = "start";

        log.info("Service: " + serviceUrl + serviceName);
        requestSender.assertRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Dynamic Partner Links and Dynamic Addressing in BPEL")
    public void dynamicPartner() throws AxisFault, XMLStreamException {
        String payload = "<ns2:dummy xmlns:ns2=\"http://ode/bpel/responder.wsdl\">fire!</ns2:dummy>";
        String operation = "execute";
        String serviceName = File.separator + "DynMainService";
        String expectedOutput = "OK";

        requestSender.assertRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "forEach BPEL functionality test case")
    public void forEach() throws AxisFault, XMLStreamException {
        String payload = "<jms:input xmlns:jms=\"http://www.example.org/jms\">testIf</jms:input>";
        String operation = "start";
        String serviceName = File.separator + "ForEachService";
        String expectedOutput = "testIf123";

        requestSender.assertRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Pick BPEL functionality test case")
    public void pick() throws AxisFault, XMLStreamException {
        //create new instance
        String payload = "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                "         <pic:Deck>testPick</pic:Deck>" +
                "      </pic:dealDeck>";
        String operation = "dealDeck";
        String serviceName = File.separator + "PickService";
        String expectedOutput = "testPick";

        requestSender.assertRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);

        //try the pick service
        payload = "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                "         <pic:Deck>testPick</pic:Deck>" +
                "      </pic:pickClub>";
        operation = "pickClub";
        serviceName = File.separator + "PickService";

        requestSender.assertRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("TestAlarm");
        bpelManager.undeployBPEL("DynPartner");
        bpelManager.undeployBPEL("TestForEach");
        bpelManager.undeployBPEL("TestPickOneWay");
        adminServiceAuthentication.logOut();
    }
}
