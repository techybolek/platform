/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.ss.rss.test;

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminStub;
import org.wso2.carbon.ss.RSSTestUtil;

import java.io.File;
import java.rmi.RemoteException;

public abstract class RSSBaseTestCase {

    private EnvironmentVariables environment;

    private RSSAdminStub rssAdminStub;

    public void initEnvironment() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder();
        try {
            this.environment = builder.ss(0).build().getSs();
        } catch (LoginAuthenticationExceptionException e) {
            throw new Exception("Error occurred while building SS server environment", e);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while building SS server environment", e);
        }

        String srcConfigPath = RSSTestUtil.getRSSSrcConfigPath();
        String destConfigPath = RSSTestUtil.getRSSDestConfigPath();

        FileUtils.copyFile(new File(srcConfigPath), new File(destConfigPath));

        String backendURL = this.getEnvironment().getBackEndUrl();
        ServerConfigurationManager serverConfigurationManager =
                new ServerConfigurationManager(backendURL);
        serverConfigurationManager.restartGracefully();

        String sessionCookie = this.getEnvironment().getSessionCookie();
        String endpoint = backendURL + "RSSAdmin";
        this.rssAdminStub = new RSSAdminStub(endpoint);
        AuthenticateStub.authenticateStub(sessionCookie, rssAdminStub);
    }

    public EnvironmentVariables getEnvironment() {
        return environment;
    }

}
