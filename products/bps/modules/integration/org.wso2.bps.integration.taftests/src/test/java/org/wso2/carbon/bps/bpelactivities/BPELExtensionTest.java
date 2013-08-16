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
import org.testng.annotations.*;
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


public class BPELExtensionTest extends BPSMasterTest{

    private static final Log log = LogFactory.getLog(BPELExtensionTest.class);

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
        uploadBpelForTest("TestProcessProperties");
        requestSender.waitForProcessDeployment(serviceUrl + File.separator + "TestProcessPropertiesService");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Process Property in BPEL extension test")
    public void processProperty() throws AxisFault, XMLStreamException {
        String payload = "<p:hello xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <TestPart>Hello</TestPart>\n" +
                "   </p:hello>";
        String operation = "hello";
        String serviceName = File.separator + "TestProcessPropertiesService";
        String expectedOutput = "Hello From BPS.";

        log.info("Service: " + serviceUrl + serviceName);
        requestSender.assertRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("TestProcessProperties");
        adminServiceAuthentication.logOut();
    }
}
