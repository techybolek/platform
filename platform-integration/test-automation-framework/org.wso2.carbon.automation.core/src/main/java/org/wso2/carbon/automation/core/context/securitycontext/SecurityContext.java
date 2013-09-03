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

package org.wso2.carbon.automation.core.context.securitycontext;

import org.wso2.carbon.automation.core.context.AutomationContext;

import java.util.HashMap;

/*
 * Represents the data structure for Cluster node in automation.xml
 */
public class SecurityContext {

    private HashMap<String, KeyStore> keyStoreList = new HashMap<String, KeyStore>();
    private HashMap<String, TrustStore> trustStoreList = new HashMap<String, TrustStore>();


    public HashMap<String, KeyStore> getKeyStoreList() {
        return keyStoreList;
    }

    public void setKeyStoreList(HashMap<String, KeyStore> keyStoreList) {
        this.keyStoreList = keyStoreList;
    }

    public HashMap<String, TrustStore> getTrustStoreList() {
        return trustStoreList;
    }

    public void setTrustStoreList(HashMap<String, TrustStore> trustStoreList) {
        this.trustStoreList = trustStoreList;
    }

    public KeyStore getKeyStore(String keyStoreId) {

        return keyStoreList.get(keyStoreId);
    }

    public TrustStore getTrustStore(String trustStoreId) {

        return trustStoreList.get(trustStoreId);
    }
}
