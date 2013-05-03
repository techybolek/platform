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

import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.Context;
import org.wso2.carbon.automation.core.environmentcontext.NodeReader;
import org.wso2.carbon.automation.core.globalcontext.frameworkcontext.FrameworkContextProvider;
import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.FrameworkContext;

import java.util.HashMap;
import java.util.Map;


public class GlobalContextInitiator {
    private static GlobalContextInitiator frameworkInstance;
    private static GlobalContextInitiator nodeInstance;
    private static FrameworkContext globalContext;
    private static Map<String, Context> nodeMap = new HashMap();

    public GlobalContextInitiator() {
    }

    public GlobalContextInitiator getContext() {
        if (frameworkInstance == null) {
            synchronized (GlobalContextInitiator.class) {
                if (frameworkInstance == null) {
                    frameworkInstance = new GlobalContextInitiator();
                    FrameworkContextProvider frameworkContext = new FrameworkContextProvider();
                    globalContext = new FrameworkContext();
                    globalContext.setCoverageSettings(frameworkContext.getCoverageSettings());
                    globalContext.setDashboardVariables(frameworkContext.getDashboardVariables());
                    globalContext.setDataSource(frameworkContext.getDataSource());
                    globalContext.setEnvironmentSettings(frameworkContext.getEnvironmentSettings());
                    globalContext.setEnvironmentVariables(frameworkContext.getEnvironmentVariables());
                    globalContext.setSelenium(frameworkContext.getSelenium());
                }
            }
        }
        return frameworkInstance;
    }

    public GlobalContextInitiator getNodeContext() {
        if (nodeInstance == null) {
            synchronized (GlobalContextInitiator.class) {
                if (nodeInstance == null) {
                    nodeInstance = new GlobalContextInitiator();
                    NodeReader reader = new NodeReader();
                    nodeMap = reader.getNodeContext();
                }
            }
        }
        return nodeInstance;
    }

    public FrameworkContext getFrameworkContext() {
        return globalContext;
    }

    public Map<String, Context> getInstanceNodeMap() {
        return nodeMap;
    }

}