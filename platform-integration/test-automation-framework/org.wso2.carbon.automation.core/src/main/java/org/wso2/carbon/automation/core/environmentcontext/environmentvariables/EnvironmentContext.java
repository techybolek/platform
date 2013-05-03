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

package org.wso2.carbon.automation.core.environmentcontext.environmentvariables;

import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.FrameworkContext;
/**
 * Object for individual Environment
 * **/
public class EnvironmentContext {
    private String sessionCookie;
    private String backEndUrl;
    private String serviceUrl;
    private String secureServiceUrl;
    private AuthenticatorClient adminServiceAuthentication;
    private InstanceVariables managerVariables;
    private InstanceVariables workerVariables;
    private String webAppURL;
    private FrameworkContext glbFrameworkContext;

    public String getSessionCookie() {
        return sessionCookie;
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public String getBackEndUrl() {
        return backEndUrl;
    }

    public void setBackEndUrl(String backendUrl) {
        this.backEndUrl = backendUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getSecureServiceUrl() {
        return secureServiceUrl;
    }

    public void setSecureServiceUrl(String secureServiceUrl) {
        this.secureServiceUrl = secureServiceUrl;
    }

    public String getWebAppURL() {
        return webAppURL;
    }

    public void setWebAppURL(String webAppURL) {
        this.webAppURL = webAppURL;
    }

    public AuthenticatorClient getAdminServiceAuthentication() {
        return adminServiceAuthentication;
    }

    public void setAuthenticatorClient(AuthenticatorClient authenticatorClient) {
        this.adminServiceAuthentication = authenticatorClient;
    }

    public InstanceVariables getManagerVariables() {
        return managerVariables;
    }


    public void setManagerVariables(InstanceVariables managerVariables) {
        this.managerVariables = managerVariables;
    }

    public InstanceVariables getWorkerVariables() {
        return workerVariables;
    }

    public void setWorkerVariables(InstanceVariables workerVariables) {
        this.workerVariables = workerVariables;
    }

    public FrameworkContext getFrameworkContext() {
        return glbFrameworkContext;
    }

    public void setFrameworkContext(FrameworkContext frameworkContext) {
        this.glbFrameworkContext = frameworkContext;
    }

}
