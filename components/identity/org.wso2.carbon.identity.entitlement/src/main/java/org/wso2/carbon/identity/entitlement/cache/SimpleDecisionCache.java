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

package org.wso2.carbon.identity.entitlement.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.pdp.PolicyDecision;

/**
 * Simple Decision Cache
 */
public class SimpleDecisionCache extends EntitlementBaseCache<IdentityCacheKey, PolicyDecision>{

    private static SimpleDecisionCache decisionCache = null;
    private static final Object lock = new Object();

    private SimpleDecisionCache() {
    	super(PDPConstants.PDP_SIMPLE_DECISION_CACHE);
    }

    /**
     * the logger we'll use for all messages
     */
    private static Log log = LogFactory.getLog(DecisionCache.class);

    /**
     * Gets a new instance of EntitlementPolicyClearingCache.
     *
     * @return A new instance of EntitlementPolicyClearingCache.
     */
    public static SimpleDecisionCache getInstance() {
        if(decisionCache == null){
            synchronized (lock){
                if(decisionCache == null){
                    decisionCache = new SimpleDecisionCache();
                }
            }
        }
        return decisionCache;
    }

    public void addToCache(String key, PolicyDecision decision){
        
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        decisionCache.addToCache(cacheKey, decision);
        if (log.isDebugEnabled()) {
            log.debug("Cache entry is added");
        }
    }

    public PolicyDecision getFromCache(String key){

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        Object entry = decisionCache.getValueFromCache(cacheKey);
        if(entry != null){
            if (log.isDebugEnabled()) {
                log.debug("Cache entry is found");
            }
            return (PolicyDecision) entry;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry is not found");
            }
        }

        return null;
    }

    public void removeFromCache(String key){

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        decisionCache.clearCacheEntry(cacheKey);
        if (log.isDebugEnabled()) {
            log.debug("Cache entry is removed");
        }
    }

    public void clearCache(){
        decisionCache.clear();
    }

}
