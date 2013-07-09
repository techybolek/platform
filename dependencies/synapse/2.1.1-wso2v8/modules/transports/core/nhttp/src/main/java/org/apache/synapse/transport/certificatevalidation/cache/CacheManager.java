/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.certificatevalidation.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Cache Manager takes care and maintains an LRU cache which implements ManageableCache Interface.
 * CAUTION!! If CacheManager is too much involved with the cache, other threads will be affected.
 */

public class CacheManager {

    Timer timer;
    ManageableCache cache;
    private int cacheMaxSize;
    private static final Log log = LogFactory.getLog(CacheManager.class);

    /**
     * A new cacheManager will be started on the given ManageableCache object.
     *
     * @param cache        a Manageable Cache which could be managed by this cache manager.
     * @param cacheMaxSize Maximum size of the cache. If the cache exceeds this size, LRU values will be
     *                     removed
     */
    public CacheManager(ManageableCache cache, int cacheMaxSize) {
        timer = new Timer();
        this.cache = cache;
        this.cacheMaxSize = cacheMaxSize;
    }

    /**
     * To Start the CacheManager. Should be called only once per CacheManager.
     *
     * @param seconds CacheManager will run its TimerTask every given number of seconds.
     */
    public void start(int seconds) {
        timer.schedule(new CacheManagingTask(), seconds * 1000, seconds * 1000);
        log.info("Cache Manager Started.....");
    }

    /**
     * This is the TimerTask the CacheManager uses in order to remove invalid cache values and
     * to remove LRU values if the cache reaches cacheMaxSize.
     */
    //todo: use executer service. remove timer task
    private class CacheManagingTask extends TimerTask {

        //TODO: This removes only one LRU entry per run. improve it!!!
        public void run() {

            long start = System.currentTimeMillis();
            log.info("Cache Manager running..");
            long oldestTime = System.currentTimeMillis();
            ManageableCacheValue oldestCacheValue = null;
            ManageableCacheValue nextCacheValue;

            //Start looking at cache entries from the beginning.
            cache.resetIterator();
            //cache.getCacheSize() can vary when new entries are added. So get cache size at this point
            int cacheSize = cache.getCacheSize();
            for (int i = 0; i < cacheSize; i++) {
                nextCacheValue = cache.getNextCacheValue();
                if (nextCacheValue == null) {
                    log.info("Cache manager iteration through Cache values done");
                    break;
                }
                //Updating cache value with of invalid cache values
                if (!nextCacheValue.isValid()) {
                    log.info("Updating Invalid Cache Value by Manager");
                    nextCacheValue.updateCacheWithNewValue();
                }
                //Get the Least recently used (oldest) value to replace
                if (nextCacheValue.getTimeStamp() < oldestTime) {
                    oldestTime = nextCacheValue.getTimeStamp();
                    oldestCacheValue = nextCacheValue;
                }
            }

            //LRU removing validatePath
            cacheSize = cache.getCacheSize();
            if (cacheSize > Math.floor(cacheMaxSize) && oldestCacheValue != null) {
                log.info("Removing LRU value from cache");
                oldestCacheValue.removeThisCacheValue();
            }
            log.info("Cache Manager stopped. Took " + (System.currentTimeMillis() - start) + " ms.");
        }
    }
}
