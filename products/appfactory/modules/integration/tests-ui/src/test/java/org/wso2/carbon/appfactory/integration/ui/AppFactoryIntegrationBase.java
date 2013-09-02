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

package org.wso2.carbon.appfactory.integration.ui;

import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;

public class AppFactoryIntegrationBase {
    private UserInfo userInfo = UserListCsvReader.getUserInfo(0);
    private String applicationName;
    private String applicationKey;
    private String databaseName;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    protected String getLoginURL() {
        return ProductUrlGeneratorUtil.getProductHomeURL(ProductConstant.APP_FACTORY_SERVER_NAME).
                replaceAll("(carbon)", "");
    }

    protected boolean isRunningOnCloud() {
        return FrameworkFactory.getFrameworkProperties(
                ProductConstant.APP_FACTORY_SERVER_NAME).getEnvironmentSettings().is_runningOnStratos();
    }

    protected void setApplicationName() {
        String applicationName = "wso2App";
        int value = (int) (Math.random() * 999);
        String randomNumber = Integer.toString(value);
        this.applicationName = applicationName + randomNumber;
    }

    protected String getApplicationName() {
        return applicationName;
    }

    protected void setApplicationKey() {
        String applicationKey = "wso2key";
        int value = (int) (Math.random() * 999);
        String randomNumber = Integer.toString(value);
        this.applicationKey = applicationKey + randomNumber;
    }

    protected String getApplicationKey() {
        return applicationKey;
    }

    protected void setDatabaseName() {
        String databaseName = "Db";
        int value = (int) (Math.random() * 999);
        String randomNumber = Integer.toString(value);
        this.databaseName = databaseName + randomNumber;
    }

    protected String getDatabaseName() {
        return databaseName;
    }
}

