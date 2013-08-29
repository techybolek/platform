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

package org.wso2.carbon.automation.core.context.environmentcontext;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.context.AutomationContext;
import org.wso2.carbon.automation.core.context.usermanagementcontext.Tenant;
import org.wso2.carbon.automation.core.context.utils.UrlGenerationUtil;
import org.wso2.carbon.automation.core.context.utils.UserAuthenticationUtil;

import java.rmi.RemoteException;

public class EnvironmentContextFactory {
    private static final Log log = LogFactory.getLog(EnvironmentContextFactory.class);
    EnvironmentContext context;
    Environment environment = new Environment();

    public void EnvironmentContextFactory() {
        context = new EnvironmentContext();
    }

    /**
     * Creates Environment context based on the selected platform instance and selected tenant
     *
     * @param automationContext Current Automation context
     * @param instanceGroupName Instance Group
     * @param instanceName      Instance of the group needed to perform
     * @param domain          Id of the tenant which platform is created
     * @param tenantUserId          Id of the tenant which platform is created
     */
    public void createEnvironmentContext(AutomationContext automationContext,
                                         String instanceGroupName,
                                         String instanceName,
                                         String domain, String tenantUserId) {
        UrlGenerationUtil urlGenerationUtil =
                new UrlGenerationUtil(automationContext, instanceGroupName
                        , instanceName, domain, tenantUserId);
        try {
            UserAuthenticationUtil authenticationUtil =
                    new UserAuthenticationUtil(urlGenerationUtil.getBackendUrl());
            Tenant tenant = automationContext.getUserManagerContext().getTenant(domain);
            environment.setSessionCookie(authenticationUtil.login(tenant.getTenantUser(tenantUserId).getUserName()
                    , tenant.getTenantAdmin().getPassword(), automationContext.getPlatformContext()
                    .getInstanceGroup(instanceGroupName).getInstance(instanceName).getHost()));
        } catch (AxisFault axisFault) {
            log.error("Authentication of the user " + tenantUserId + " failed:-" + axisFault.getMessage());
        } catch (RemoteException e) {
            log.error("Authentication of the user " + tenantUserId + " failed:-" + e.getMessage());
        } catch (LoginAuthenticationExceptionException e) {
            log.error("Authentication of the user " + tenantUserId + " failed:-" + e.getMessage());
        }
        environment.setBackEndUrl(urlGenerationUtil.getBackendUrl());
        environment.setSecureServiceUrl(urlGenerationUtil.getHttpsServiceURL());
        environment.setServiceUrl(urlGenerationUtil.getHttpServiceURL());
        environment.setWebAppURL(urlGenerationUtil.getWebAppURL());
        context.setEnvironmentConfigurations(environment);

    }


    public EnvironmentContext getEnvironmentContext() {

        return context;
    }

}
