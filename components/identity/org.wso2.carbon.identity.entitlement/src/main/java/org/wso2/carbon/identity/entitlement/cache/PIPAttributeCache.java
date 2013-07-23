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
import org.wso2.carbon.identity.entitlement.PDPConstants;

import java.util.Set;

/**
 *
 */
public class PIPAttributeCache {

    private static PIPAttributeCache pipAttributeCache = null;
    private static final Object lock = new Object();
    private PIPAttributeCache() {
    }

    /**
     * the logger we'll use for all messages
     */
    private static Log log = LogFactory.getLog(EntitlementPolicyClearingCache.class);

    /**
     * Gets a new instance of EntitlementPolicyClearingCache.
     *
     * @return A new instance of EntitlementPolicyClearingCache.
     */
    public static PIPAttributeCache getInstance() {

        if(pipAttributeCache == null){
            synchronized (lock){
                if(pipAttributeCache == null){
                    pipAttributeCache = new PIPAttributeCache();
                }
            }
        }
        return pipAttributeCache;
    }

	/**
	 * Getting existing cache if the cache available, else returns a newly created cache.
	 * This logic handles by javax.cache implementation
	 */
	private Cache<IdentityCacheKey,IdentityCacheEntry> getPIPAttributeCache() {
		CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(PDPConstants.ENTITLEMENT_CACHE_MANAGER);
		Cache<IdentityCacheKey,IdentityCacheEntry> cache = cacheManager.getCache(PDPConstants.PIP_ABSTRACT_ATTRIBUTE_CACHE);
		return cache;
	}
	
    public void addToCache(int tenantId, String key, Set<String> attributes){

        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        IdentityCacheEntry cacheEntry = new IdentityCacheEntry(attributes);
        
        Cache<IdentityCacheKey,IdentityCacheEntry> cache = getPIPAttributeCache();
        if(cache != null) {
	        cache.put(cacheKey, cacheEntry);
	        if (log.isDebugEnabled()) {
	            log.debug("Cache entry is added");
	        }
        }
    }

    public Set<String> getFromCache(int tenantId, String key){

        Set<String> attributes = null;

        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        
        Cache<IdentityCacheKey,IdentityCacheEntry> cache = getPIPAttributeCache();
        if(cache != null) {
        	IdentityCacheEntry cacheEntry = cache.get(cacheKey);
	        if(cacheEntry != null){
	            attributes =  cacheEntry.getCacheEntrySet();
	            if (log.isDebugEnabled()) {
	                log.debug("Cache entry is found");
	            }
	        } else {
	            if (log.isDebugEnabled()) {
	                log.debug("Cache entry is not found");
	            }
	        }
        }

        return attributes;
    }

    public void invalidateCache(int tenantId, String key){

        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        
        Cache<IdentityCacheKey,IdentityCacheEntry> cache = getPIPAttributeCache();
        if(cache != null) {
	        if(cache.containsKey(cacheKey)){
	
	            cache.remove(cacheKey);
	
	            if (log.isDebugEnabled()) {
	                log.debug("Local cache is invalidated");
	            }
            //sending cluster message
//            CacheInvalidator invalidator = EntitlementServiceComponent.getCacheInvalidator();
//            try {
//                if (invalidator != null) {
//                    invalidator.invalidateCache(PDPConstants.PIP_ATTRIBUTE_CACHE, cacheKey);
//                    if (log.isDebugEnabled()) {
//                        log.debug("Calling invalidation cache");
//                    }
//                } else {
//                    if (log.isDebugEnabled()) {
//                        log.debug("Not calling invalidation cache");
//                    }
//                }
//            } catch (CacheException e) {
//                log.error("Error while invalidating cache", e);
//            }
        	}
        }
    }

    public void clearCache(int tenantId){

    	 
        Cache<IdentityCacheKey,IdentityCacheEntry> cache = getPIPAttributeCache();
        if(cache != null) {
	        for (Cache.Entry<IdentityCacheKey, IdentityCacheEntry> entry : cache) {        	
	        	IdentityCacheKey identityCacheKey=entry.getKey();
	            if(tenantId==identityCacheKey.getTenantId()){
	                cache.remove(identityCacheKey);
	            }                                  
	        }
	        if (log.isDebugEnabled()) {
	            log.debug("Local cache is invalidated for tenant : " + tenantId);
	        }

        //sending cluster message
//        CacheInvalidator invalidator = EntitlementServiceComponent.getCacheInvalidator();
//        try {
//            if (invalidator != null) {
//                invalidator.invalidateCache(PDPConstants.PIP_ATTRIBUTE_CACHE, tenantId);
//                if (log.isDebugEnabled()) {
//                    log.debug("Calling invalidation cache");
//                }
//            } else {
//                if (log.isDebugEnabled()) {
//                    log.debug("Not calling invalidation cache");
//                }
//            }
//        } catch (CacheException e) {
//            log.error("Error while invalidating cache", e);
//        }
        }
    }
}
