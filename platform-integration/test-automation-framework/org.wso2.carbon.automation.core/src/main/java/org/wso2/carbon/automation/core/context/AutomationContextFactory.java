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

import org.wso2.carbon.automation.core.context.environmentcontext.EnvironmentContext;
import org.wso2.carbon.automation.core.context.environmentcontext.EnvironmentContextFactory;

public class AutomationContextFactory {
    private AutomationContext automationContext;
    private AutomationContextReader automationContextReader = new AutomationContextReader();
    AutomationContext tempContext;

    public AutomationContextFactory() {
        automationContext = new AutomationContext();
        automationContextReader = new AutomationContextReader();
        tempContext = new AutomationContext();
    }

    public void createAutomationContext(String instanceGroupName, String instanceName, String domain, String tenantId) {

        automationContextReader.readAutomationContext();
        tempContext = automationContextReader.getAutomationContext();
        EnvironmentContextFactory environmentContextFactory = new EnvironmentContextFactory();
        environmentContextFactory.createEnvironmentContext(tempContext, instanceGroupName, instanceName, domain, tenantId);
        automationContext.setConfigurationContext(tempContext.getConfigurationContext());
        automationContext.setUserManagerContext(tempContext.getUserManagerContext());
        automationContext.setEnvironmentContext(environmentContextFactory.getEnvironmentContext());
        automationContext.setToolContext(tempContext.getToolContext());
        automationContext.setDatabaseContext(tempContext.getDatabaseContext());
        automationContext.setPlatformContext(tempContext.getPlatformContext());
        automationContext.setFeatureManagementContext(tempContext.getFeatureManagementContext());
        automationContext.setSecurityContext(tempContext.getSecurityContext());

    }

    protected AutomationContext getBasicContext() {
        automationContextReader.readAutomationContext();
        tempContext = automationContextReader.getAutomationContext();
        return tempContext;
    }

    public AutomationContext getAutomationContext() {
        return automationContext;
    }
}
