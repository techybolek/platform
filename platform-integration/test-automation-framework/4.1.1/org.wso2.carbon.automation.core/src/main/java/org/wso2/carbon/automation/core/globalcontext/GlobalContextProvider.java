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

package org.wso2.carbon.automation.core.globalcontext;

import org.wso2.carbon.automation.core.globalcontext.frameworkcontext.FrameworkPropertyContext;
import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.*;

public class GlobalContextProvider implements FrameworkPropertyContext{
    FrameworkContext frameworkContext;
    public void GlobalContextProvider()
    {
        frameworkContext = new FrameworkContext();
        GlobalContextInitiator globalContextInitiator = null;
        frameworkContext=globalContextInitiator.getContext().getFrameworkContext();
    }
    public DataSource getDataSource() {
        return frameworkContext.getDataSource();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return frameworkContext.getEnvironmentSettings();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EnvironmentVariables getEnvironmentVariables() {
        return frameworkContext.getEnvironmentVariables();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Selenium getSelenium() {
        return frameworkContext.getSelenium();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public FrameworkContext getFrameworkProperties() {
        return frameworkContext;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
