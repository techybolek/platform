/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.git;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.git.util.Util;
import org.wso2.carbon.appfactory.repository.mgt.service.RepositoryAuthenticationServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Service client for repository authentication service
 */
public class AppFactoryRepositoryAuthorizationClient {
    private static final Logger log = LoggerFactory.getLogger(ApplicationManagementServiceClient.class);
    private RepositoryAuthenticationServiceStub serviceStub;
    private GitBlitConfiguration gitBlitConfiguration;
    private ContextHolder holder;
    /**
     * Constructor taking Gitblit configuration
     *
     * @param configuration
     */
    public AppFactoryRepositoryAuthorizationClient(GitBlitConfiguration configuration) {
        gitBlitConfiguration=configuration;
        holder=ContextHolder.getHolder(gitBlitConfiguration);
       String username=configuration.getProperty(GitBlitConstants
                                                                    .APPFACTORY_ADMIN_USERNAME,
                                                            "admin@admin.com");
       String password =configuration.getProperty(GitBlitConstants
                                                                    .APPFACTORY_ADMIN_PASSWORD,
                                                            "admin");

        try {
            ConfigurationContext context= holder.getConfigurationContext();
            serviceStub = new RepositoryAuthenticationServiceStub(context,configuration.getProperty
                    (GitBlitConstants
                             .APPFACTORY_URL, "https://localhost:9443") + "/services/RepositoryAuthenticationService");
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, serviceStub._getServiceClient());
            Util.setMaxTotalConnection(serviceStub._getServiceClient());
        } catch (AxisFault fault) {
            log.error("Error occurred while initializing client ", fault);
        } catch (RemoteException e) {
            log.error("Error occurred in remote end while initializing client ", e);
        }
    }

    /**
     * @param userName
     * @param repositoryName
     * @return
     */
    public boolean authorize(String userName, String repositoryName) {

        boolean isAuth = false;
        try {
            if (serviceStub.hasAccess(userName, repositoryName)) {
                isAuth = true;
                System.out.println("calling RepositoryAuthenticationServiceStub.hasAccess");
            }
        } catch (AxisFault e) {
            log.error("Error while calling ApplicationManagementService:Error is " + e.getLocalizedMessage(), e);
        } catch (RemoteException e) {
            log.error("Error while calling ApplicationManagementService:Error is " + e.getLocalizedMessage(), e);
        } catch (IOException e) {
            log.error("Error while calling ApplicationManagementService:Error is " + e.getLocalizedMessage(), e);
        }finally {
            try {
                serviceStub._getServiceClient().cleanupTransport();
                serviceStub._getServiceClient().cleanup();
                serviceStub.cleanup();
            } catch (AxisFault fault) {
                //ignore
            }
        }
        return isAuth;
    }
}
