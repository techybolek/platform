/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class PolicyEditorEngine {

    private int tenantId;

    private static final Object lock = new Object();

    private static ConcurrentHashMap<String, PolicyEditorEngine> policyEditorEngine =
                                                new ConcurrentHashMap<String, PolicyEditorEngine>();

    private PolicyEditorDataHolder dataHolder;
    
    private DataPersistenceManager manager;
    
    private static Log log = LogFactory.getLog(PolicyEditorEngine.class);

    /**
     * Get a PolicyEditorEngine instance for that tenant. This method will return an
     * PolicyEditorEngine instance if exists, or creates a new one
     *
     * @return EntitlementEngine instance for that tenant
     */
    public static PolicyEditorEngine getInstance() {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        if (!policyEditorEngine.containsKey(Integer.toString(tenantId))) {
            synchronized (lock){
                if (!policyEditorEngine.containsKey(Integer.toString(tenantId))) {
                    policyEditorEngine.put(Integer.toString(tenantId), new PolicyEditorEngine(tenantId));
                }
            }
        }
        return policyEditorEngine.get(Integer.toString(tenantId));
    }

    public PolicyEditorEngine(int tenantId) {
        this.tenantId = tenantId;
        this.manager = new InMemoryPersistenceManager();
        this.dataHolder = this.manager.buildDataHolder();
    }

    public PolicyEditorDataHolder getPolicyEditorData(){

        return dataHolder;
    }

    public void persistData(String xmlConfig){
        manager.persistData(xmlConfig);
        dataHolder = manager.buildDataHolder();
    }
}
