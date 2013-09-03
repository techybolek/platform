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

package org.wso2.carbon.automation.core.context;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.context.configurationcontext.ConfigurationContext;
import org.wso2.carbon.automation.core.context.contextenum.Platforms;
import org.wso2.carbon.automation.core.context.platformcontext.Instance;
import org.wso2.carbon.automation.core.context.usermanagementcontext.User;
import org.wso2.carbon.automation.core.context.utils.UserAuthenticationUtil;

import java.rmi.RemoteException;
import java.util.List;

/**
 * The builder class for the automation context based on the automation.xml
 */
public class AutomationContextBuilder {
    AutomationContext automationContext;
    AutomationContextFactory automationContextFactory;
    String instanceGroup = null;
    String instanceName = null;
    String domain = null;
    String userId = null;

    public AutomationContextBuilder(String instanceGroup) {
        automationContextFactory = new AutomationContextFactory();
        this.instanceGroup = instanceGroup;
        this.instanceName = getInstanceByGroup().getName();

    }

    public AutomationContextBuilder(String instanceGroup, String instanceName) {
        automationContextFactory = new AutomationContextFactory();
        this.instanceGroup = instanceGroup;
        this.instanceName = instanceName;
    }

    /**
     * Builds the environment with the given domain with either as tenant admin or tenant user
     *
     * @param domain          The Domain configured in automation.xml
     * @param runAsSuperAdmin Whether the test is running as a super tenant or not.
     */
    public void build(String domain, boolean runAsSuperAdmin) {
        automationContext = automationContextFactory.getAutomationContext();
        this.domain = domain;
        this.userId = ContextConstants.TENANT_ADMIN_KEY;
        automationContextFactory.createAutomationContext(instanceGroup, instanceName,
                domain, ContextConstants.TENANT_ADMIN_KEY);

    }

    /**
     * Run as a specific user defined in a tenant domain
     *
     * @param domain        The Domain configured in automation.xml
     * @param tenantUserKey The key of the user expect
     */
    public void build(String domain, String tenantUserKey) {
        this.domain = domain;
        this.userId = tenantUserKey;
        automationContextFactory.createAutomationContext(instanceGroup, instanceName,
                domain, tenantUserKey);
        automationContext = automationContextFactory.getAutomationContext();
    }


    /**
     * Logs with initiated user and returns the session cookie for the respective session.
     *
     * @return sessionCookie
     */
    public String login() throws RemoteException, LoginAuthenticationExceptionException {
        String sessionCookie;
        UserAuthenticationUtil util = new UserAuthenticationUtil
                (automationContext.getEnvironmentContext()
                        .getEnvironmentConfigurations().getBackEndUrl());
        User user = automationContext.getUserManagerContext().getTenant(domain).getTenantUser(userId);
        sessionCookie = util.login(user.getUserName(), user.getPassword(),
                automationContext.getPlatformContext().getInstanceGroup(instanceGroup)
                        .getInstance(instanceName).getHost());
        return sessionCookie;
    }

    private User getTenant(String domain, String tenantUserKey) {
        return automationContext.getUserManagerContext().getTenant(domain).getTenantUsers()
                .get(tenantUserKey);

    }

    private Instance getInstanceByGroup() {
        AutomationContext basicContext = new AutomationContext();
        Instance instance = new Instance();
        basicContext = automationContextFactory.getBasicContext();
        ConfigurationContext configuration = basicContext.getConfigurationContext();
        List<Instance> lbManagerInstanceList = basicContext.getPlatformContext()
                .getInstanceGroup(instanceGroup).getLoadBalanceManagerInstances();
        List<Instance> managerInstanceList = basicContext.getPlatformContext()
                .getInstanceGroup(instanceGroup).getManagerInstances();
        List<Instance> instanceList = basicContext.getPlatformContext()
                .getInstanceGroup(instanceGroup).getInstances();
        List<Instance> lbList = basicContext.getPlatformContext()
                .getInstanceGroup(instanceGroup).getLoadBalancerInstances();
        //If the execution mode is platform it looks for whetherinstance group is clustered
        if (configuration.getConfiguration().getExecutionEnvironment().equals(Platforms.platform.name())) {
           /*if clustered default instance is manager fronted LB
            if no manger fronted LB is assigned it will go for a lb instance
            otherwise it will take general manager instance
            if manager instance in null it goes for normal instance*/
            if (basicContext.getPlatformContext().getInstanceGroup(instanceGroup).isClusteringEnabled()) {
                if (!lbManagerInstanceList.isEmpty()) {
                    instance = lbManagerInstanceList.get(0);
                } else if (!managerInstanceList.isEmpty()) {
                    instance = managerInstanceList.get(0);
                } else {
                    instance = instanceList.get(0);
                }
            } else {
                /*if not clustered default will go for a lb if lb is null
                * selection would be instance*/
                if (!lbManagerInstanceList.isEmpty()) {
                    instance = lbList.get(0);
                } else {
                    instance = instanceList.get(0);
                }
            }
        } else if (configuration.getConfiguration().getExecutionEnvironment().equals(Platforms.cloud.name())) {
            if (!lbManagerInstanceList.isEmpty()) {
                instance = lbManagerInstanceList.get(0);
            } else if (!managerInstanceList.isEmpty()) {
                instance = managerInstanceList.get(0);
            } else {
                instance = instanceList.get(0);
            }
        } else {
            instance = instanceList.get(0);
        }
        return instance;
    }


    public AutomationContext getAutomationContext() {
        return automationContext;
    }
}
