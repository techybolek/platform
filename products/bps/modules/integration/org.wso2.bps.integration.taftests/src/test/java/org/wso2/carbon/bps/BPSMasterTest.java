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
package org.wso2.carbon.bps;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelUploaderClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;

import java.io.File;
import java.rmi.RemoteException;

public class BPSMasterTest {
    protected EnvironmentVariables environment;
    protected String sessionCookie = null;
    protected String backEndUrl = null;
    protected String serviceUrl = null;
    protected AuthenticatorClient adminServiceAuthentication;
    protected BpelUploaderClient bpelUploader;

    protected void init() throws RemoteException, LoginAuthenticationExceptionException {
        EnvironmentBuilder builder = new EnvironmentBuilder().bps(3);
        environment = builder.build().getBps();
        backEndUrl = environment.getBackEndUrl();
        serviceUrl = environment.getServiceUrl();
        sessionCookie = environment.getSessionCookie();
        adminServiceAuthentication = environment.getAdminServiceAuthentication();
        bpelUploader = new BpelUploaderClient(backEndUrl, sessionCookie);
    }

    protected void uploadBpelForTest(String bpelName) throws RemoteException, InterruptedException, PackageManagementException {
        bpelUploader.deployBPEL(bpelName, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +"artifacts"
                + File.separator + "bpel");
    }

    protected void uploadBpelVersionedSample(String bpelName) throws InterruptedException, RemoteException, PackageManagementException {
        bpelUploader.deployBPEL(bpelName, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +"artifacts"
                + File.separator + "bpel"+File.separator+"VersioningSamples");
    }

}
