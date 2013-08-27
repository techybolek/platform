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

import org.wso2.carbon.automation.core.context.environmentcontext.EnvironmentContextFactory;

public class AutomationContextFactory {
    AutomationContext automationContext;
    public void createAutomationContext(String instanceGroupName,String instanceName,String tenantId)
    {
        automationContext = new AutomationContext();
        AutomationContextReader automationContextReader = new AutomationContextReader();
        automationContextReader.readAutomationContext();
        automationContext = automationContextReader.getAutomationContext();
        EnvironmentContextFactory environmentContextFactory = new EnvironmentContextFactory();
        environmentContextFactory.createEnvironmentContext(automationContext,instanceGroupName,instanceName,tenantId);
        automationContext.setEnvironmentContext(environmentContextFactory.getEnvironmentContext());
    }



}
