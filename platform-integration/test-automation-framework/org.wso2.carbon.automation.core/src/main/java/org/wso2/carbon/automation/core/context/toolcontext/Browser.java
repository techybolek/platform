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
package org.wso2.carbon.automation.core.context.toolcontext;


/*
 * this class represents the data structure of the tools->selenium->browser node in automation.xml file
 */
public class Browser {

    private String browserType;
    private String webDriverPath;
    private Boolean webDriverEnabled;


    public String getBrowserType() {
        return browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public String getWebDriverPath() {
        return webDriverPath;
    }

    public void setWebDriverPath(String webDriverPath) {
        this.webDriverPath = webDriverPath;
    }

    public Boolean getWebDriverEnabled() {
        return webDriverEnabled;
    }

    public void setWebDriverEnabled(Boolean webDriverEnabled) {
        this.webDriverEnabled = webDriverEnabled;
    }
}
