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

package org.wso2.carbon.registry.tck.cmis;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * A test class to run the CMIS TCK as a TestNG module
 */

public class CMISTck {
   
    public static final String CMIS_PATH = "cmis.jar.path";
    public static final String LOG_PATH = "logging.jar.path";

    public RemoteRegistry registry;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;
    private UserInfo userInfo;

    @BeforeClass(groups = {"wso2.greg"})
    public void init()
            throws MalformedURLException, RegistryException, LoginAuthenticationExceptionException,
                   RemoteException {
        int userId = 1;
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        userInfo = UserListCsvReader.getUserInfo(userId);
        builder = new EnvironmentBuilder().greg(1);
        environment = builder.build();

        registry = new RegistryProviderUtil().getRemoteRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    @Test(groups = {"wso2.greg"})
    public void runTckTest() throws RegistryException, InterruptedException, IOException {
	
	PrintStream ps = new PrintStream(new FileOutputStream("system.properties"));
	ps.println(CMIS_PATH + "=" + System.getProperty(CMIS_PATH));
	ps.println(LOG_PATH + "=" + System.getProperty(LOG_PATH));

	Process p = Runtime.getRuntime().exec("ant cmis");
	int stat = p.waitFor();
	
	if (stat == 0) {
	   System.out.println("CMIS TCK ran successfully");	
	} else {
	   System.out.println("Failed to run CMIS TCK");	

	}
    }
}
