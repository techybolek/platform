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

import org.wso2.carbon.automation.core.context.configurationcontext.Configuration;
import org.wso2.carbon.automation.core.context.configurationcontext.ConfigurationContext;
import org.wso2.carbon.automation.core.context.databasecontext.Database;
import org.wso2.carbon.automation.core.context.databasecontext.DatabaseContext;
import org.wso2.carbon.automation.core.context.environmentcontext.EnvironmentContext;
import org.wso2.carbon.automation.core.context.featuremanagementContext.FeatureManagementContext;
import org.wso2.carbon.automation.core.context.platformcontext.PlatformContext;
import org.wso2.carbon.automation.core.context.securitycontext.SecurityContext;
import org.wso2.carbon.automation.core.context.toolcontext.ToolContext;
import org.wso2.carbon.automation.core.context.usermanagementcontext.UserManagerContext;

public class AutomationContext {
    public void testMethod() {
    }

    private ConfigurationContext configurationContext;
    private DatabaseContext databaseContext;
    private EnvironmentContext environmentContext;
    private FeatureManagementContext featureManagementContext;
    private PlatformContext platformContext;
    private SecurityContext securityContext;
    private ToolContext toolContext;
    private UserManagerContext userManagerContext;

    public void setConfigurationContext(ConfigurationContext context) {

        this.configurationContext = context;

    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public void setDatabaseContext(DatabaseContext context) {

        this.databaseContext = context;

    }

    public DatabaseContext getDatabaseContext() {
        return databaseContext;
    }

    public void setFeatureManagementContext(FeatureManagementContext context) {

        this.featureManagementContext = context;

    }

    public FeatureManagementContext getFeatureManagementContext() {
        return featureManagementContext;
    }

    public void setPlatformContext(PlatformContext context) {

        this.platformContext = context;

    }

    public PlatformContext getPlatformContext() {
        return platformContext;
    }

    public void setSecurityContext(SecurityContext context) {

        this.securityContext = context;

    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setEnvironmentContext(EnvironmentContext context) {

        this.environmentContext = context;

    }

    public EnvironmentContext getEnvironmentContext() {
        return environmentContext;
    }

    public void setToolContext(ToolContext context) {

        this.toolContext = context;

    }

    public ToolContext getToolContext() {
        return toolContext;
    }

    public void setUserManagerContext(UserManagerContext context) {

        this.userManagerContext = context;

    }

    public UserManagerContext getUserManagerContext() {
        return userManagerContext;
    }
}
