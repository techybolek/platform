package org.apache.synapse.transport.certificatevalidation.cache;

public class CacheController implements CacheControllerMBean{

    private ManageableCache cache;
    private CacheManager cacheManager;

    public CacheController(ManageableCache cache, CacheManager cacheManager){
        this.cache = cache;
        this.cacheManager = cacheManager;
    }

    public boolean stopCacheManager() {
        return cacheManager.stop();
    }

    public boolean wakeUpCacheManager() {
        return cacheManager.wakeUpNow();
    }

    public boolean changeCacheManagerDelayMins(int delay){
        return cacheManager.changeDelay(delay);
    }

    public boolean isCacheManagerRunning() {
        return cacheManager.isRunning();
    }

    public int getCacheSize() {
        return cache.getCacheSize();
    }

    public int getCacheManagerDelayMins(){
        return cacheManager.getDelay();
    }
}
