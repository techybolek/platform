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

import org.wso2.carbon.automation.core.context.platformcontext.Instance;
import org.wso2.carbon.automation.core.context.usermanagementcontext.User;

public class AutomationContextBuilder {
    AutomationContext automationContext;
    String instanceGroup = null;
    String instanceName = null;

    public AutomationContextBuilder(String instanceGroup) {
        this.instanceGroup = instanceGroup;
    }

    public AutomationContextBuilder(String instanceGroup, String instanceName) {
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
        AutomationContextFactory automationContextFactory = new AutomationContextFactory();
        automationContextFactory.createAutomationContext(instanceGroup,instanceName,
                domain,ContextConstants.TENANT_ADMIN_KEY);
    }

    /**
     * Run as a specific user defined in a tenant domain
     *
     * @param domain        The Domain configured in automation.xml
     * @param tenantUserKey The key of the user expect
     */
    public void build(String domain, String tenantUserKey) {
        AutomationContextFactory automationContextFactory = new AutomationContextFactory();
        automationContextFactory.createAutomationContext(instanceGroup,instanceName,
                domain,tenantUserKey);
    }

    /**
     * Runs as random domain with random user selecting admin or not
     *
     * @param runAsSuperAdmin State whether test should run as a carbon.super admin
     */
    public void build(boolean runAsSuperAdmin) {
        AutomationContextFactory automationContextFactory = new AutomationContextFactory();
    }

    private User getTenant(String domain,  String tenantUserKey){
        return automationContext.getUserManagerContext().getTenant(domain).getTenantUsers()
                .get(tenantUserKey);

    }


    public AutomationContext getAutomationContext()
    {
        return automationContext;
    }
}
