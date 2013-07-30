package org.wso2.carbon.appfactory.core.cache;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;

import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.core.deploy.Artifact;

/**
 * Stores app versions in a cache against app id.
 * Used for performance ehancement in the UI
 */
public class AppVersionCache {
    
    private static AppVersionCache appVersionCache = new AppVersionCache();       
    private static Cache cache = CacheManager.getInstance().getCache(AppFactoryConstants.APP_VERSION_CACHE);

    private AppVersionCache() {
    }

    public static AppVersionCache getAppVersionCache() {
        return appVersionCache;
    }

    public void addToCache(String appId, Artifact[] artifacts) {
        cache.put(appId, artifacts);
    }

    public Artifact[] getAppVersions(String appId) {
        return (Artifact[]) cache.get(appId);
    }
    
    //Improve this method to clear cache for app Id
    public void clearCacheForAppId(String appId) {
        cache.remove(appId);
    }

}
