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


import java.util.HashMap;

/*
 * this class represents the data structure of the tools->selenium node in automation.xml file
 */
public class Selenium {

    private String remoteDriverURL;
    private Boolean remoteDriverEnable;
    private HashMap<String, Browser> browserList = new HashMap<String, Browser>();

    public String getRemoteDriverURL() {
        return remoteDriverURL;
    }

    public void setRemoteDriverURL(String remoteDriverURL) {
        this.remoteDriverURL = remoteDriverURL;
    }

    public Boolean getRemoteDriverEnable() {
        return remoteDriverEnable;
    }

    public void setRemoteDriverEnable(Boolean remoteDriverEnable) {
        this.remoteDriverEnable = remoteDriverEnable;
    }

    public HashMap<String, Browser> getBrowserList() {
        return browserList;
    }

    public void setBrowserList(HashMap<String, Browser> browserList) {
        this.browserList = browserList;
    }

    public Browser getBrowser(String browserId) {

        return browserList.get(browserId);
    }
}
