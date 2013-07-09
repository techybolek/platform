package org.wso2.carbon.identity.entitlement.cache;

import java.io.Serializable;

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
 * A base class for all cache implementations in user entitlement module.
 */
public class EntitlementBaseCache<K extends IdentityCacheKey, V extends Serializable> {

	private static Log log = LogFactory.getLog(EntitlementBaseCache.class);
	
    private static final String ENTITLEMENT_CACHE_MANAGER = "ENTITLEMENT_CACHE_MANAGER";
    private String Entitlement_CACHE_NAME;

	public EntitlementBaseCache(String cacheName) {
		this.Entitlement_CACHE_NAME = cacheName;
	}
	
	/**
	 * Getting existing cache if the cache available, else returns a newly created cache.
	 * This logic handles by javax.cache implementation
	 */
	private Cache<K, V> getEntitlementCache() {
		CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(ENTITLEMENT_CACHE_MANAGER);
		Cache<K, V>	cache = cacheManager.getCache(Entitlement_CACHE_NAME);
		return cache;
	}

	/**
	 * Add a cache entry.
	 * 
	 * @param key
	 *            Key which cache entry is indexed.
	 * @param entry
	 *            Actual object where cache entry is placed.
	 */
	public void addToCache(K key, V entry) {
		// Element already in the cache. Remove it first
		clearCacheEntry(key);
		
		Cache<K,V> cache = getEntitlementCache();
		if (cache != null) {
			cache.put(key, entry);
		}
	}

	/**
	 * Retrieves a cache entry.
	 * 
	 * @param key
	 *            CacheKey
	 * @return Cached entry.
	 */
	public V getValueFromCache(K key) {
		Cache<K,V> cache = getEntitlementCache();
		if (cache != null) {
			if (cache.containsKey(key)) {
				return (V) cache.get(key);
			}
		}
		return null;
	}

	/**
	 * Clears a cache entry.
	 * 
	 * @param key
	 *            Key to clear cache.
	 */
	public void clearCacheEntry(K key) {
		Cache<K,V> cache = getEntitlementCache();
		if (cache != null) {
			if (cache.containsKey(key)) {
				cache.remove(key);
			}
		}
	}

	/**
	 * Remove everything in the cache.
	 */
	public void clear() {
		Cache<K,V> cache = getEntitlementCache();
		if (cache != null) {
			cache.removeAll();
		}
	}
	

}
