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

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.policy.search.SearchResult;

/**
 *
 */
public class PolicySearchCache {

	private Cache<IdentityCacheKey,SearchResult> cache = null;

    private static PolicySearchCache policySearchCache = null;

    private static final Object lock = new Object();  

    private PolicySearchCache() {
    	CacheManager manager = Caching.getCacheManagerFactory().getCacheManager(EntitlementConstants.ENTITLEMENT_CACHE_MANAGER);
        if(manager != null){
        	this.cache = manager.getCache(EntitlementConstants.POLICY_SEARCH_CACHE);
        } else {
        	this.cache = Caching.getCacheManager().getCache(EntitlementConstants.POLICY_SEARCH_CACHE);
        }
//        this.cache =  CarbonUtils.getLocalCache(EntitlementConstants.POLICY_SEARCH_CACHE);
        if(this.cache != null) {
            if (log.isDebugEnabled()) {
            	log.debug("Successfully created POLICY_SEARCH_CACHE under ENTITLEMENT_CACHE_MANAGER"); 
            }
        }
        else {
        	log.error("Error while creating POLICY_SEARCH_CACHE");
        }
    }

    /**
     * the logger we'll use for all messages
     */
    private static Log log = LogFactory.getLog(PolicySearchCache.class);

    /**
     * Gets a new instance of EntitlementPolicyClearingCache.
     *
     * @return A new instance of EntitlementPolicyClearingCache.
     */
    public static PolicySearchCache getInstance() {
        if(policySearchCache == null){
            synchronized (lock){
                if(policySearchCache == null){
                    policySearchCache = new PolicySearchCache();
                }
            }
        }
        return policySearchCache;
    }

    public void addToCache(String key, SearchResult result){
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        cache.put(cacheKey, result);
        if (log.isDebugEnabled()) {
            log.debug("Cache entry is added");
        }
    }

    public SearchResult getFromCache(String key){

        SearchResult searchResult = null;
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        Object entry = this.cache.get(cacheKey);
        if(entry != null){
            searchResult = (SearchResult) entry;
            if (log.isDebugEnabled()) {
                log.debug("Cache entry is found");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry is not found");
            }
        }

        return searchResult;
    }

    public void invalidateCache(){
        cache.removeAll();
    }

}
