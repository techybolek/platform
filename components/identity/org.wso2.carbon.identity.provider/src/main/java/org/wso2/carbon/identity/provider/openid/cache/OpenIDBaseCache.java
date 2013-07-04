package org.wso2.carbon.identity.provider.openid.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Date: Oct 1, 2010 Time: 2:47:14 PM
 */

/**
 * A base class for all cache implementations in user core module.
 */
public abstract class OpenIDBaseCache {

	private static Log log = LogFactory.getLog(OpenIDBaseCache.class);
	
	protected Cache<OpenIDCacheKey, OpenIDCacheEntry> cache = null;
    private static final String OPENID_CACHE_MANAGER = "OpenIDCacheManager";

	protected OpenIDBaseCache(String cacheName) {
    	CacheManager manager = Caching.getCacheManagerFactory().getCacheManager(OpenIDBaseCache.OPENID_CACHE_MANAGER);
        if(manager != null){
        	this.cache = manager.getCache(cacheName);
        } else {
        	this.cache = Caching.getCacheManager().getCache(cacheName);
        }
//        this.cache = CacheManager.getInstance().getCache(cacheName);
        if(this.cache != null) {
            if (log.isDebugEnabled()) {
            	log.debug("Successfully created "+cacheName+" under "+OpenIDBaseCache.OPENID_CACHE_MANAGER); 
            }
        }
        else {
        	log.error("Error while creating "+cacheName);
        }
	}

    

	/**
	 * Add a cache entry.
	 * 
	 * @param key
	 *            Key which cache entry is indexed.
	 * @param entry
	 *            Actual object where cache entry is placed.
	 */
	public void addToCache(OpenIDCacheKey key, OpenIDCacheEntry entry) {
		if (this.cache.containsKey(key)) {
			// Element already in the cache. Remove it first
			this.cache.remove(key);
		}

		this.cache.put(key, entry);
	}

	/**
	 * Retrieves a cache entry.
	 * 
	 * @param key
	 *            CacheKey
	 * @return Cached entry.
	 */
	public OpenIDCacheEntry getValueFromCache(OpenIDCacheKey key) {

		if (this.cache.containsKey(key)) {
			return (OpenIDCacheEntry) this.cache.get(key);
		}

		return null;

	}

	/**
	 * Clears a cache entry.
	 * 
	 * @param key
	 *            Key to clear cache.
	 */
	public void clearCacheEntry(OpenIDCacheKey key) {
		if (this.cache.containsKey(key)) {
			this.cache.remove(key);
		}
	}

	/**
	 * Remove everything in the cache.
	 */
	public void clear() {
		this.cache.removeAll();
	}

}
