/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.mgt.store;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.core.CacheInvalidator;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.dto.UserIdentityDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;

/**
 *
 */
public class InMemoryIdentityDataStore extends UserIdentityDataStore {

	protected Cache cache = CarbonUtils.getLocalCache("IDENTITY_LOGIN_DATA_CACHE");

	private static Log log = LogFactory.getLog(InMemoryIdentityDataStore.class);

	@Override
	public void store(UserIdentityDTO userIdentityDTO, UserStoreManager userStoreManager)
	                                                                                     throws IdentityException {
		if (userIdentityDTO != null && userIdentityDTO.getUserName() != null) {
			String key =
			             CarbonContext.getCurrentContext().getTenantId() +
			                     userIdentityDTO.getUserName();
			if (cache.containsKey(key)) {
				invalidateCache(userIdentityDTO.getUserName());
			}
			cache.put(key, userIdentityDTO);
		}
	}

	@Override
	public UserIdentityDTO load(String userName, UserStoreManager userStoreManager)
	                                                                               throws IdentityException {

		if (userName != null) {
			return (UserIdentityDTO) cache.get(CarbonContext.getCurrentContext().getTenantId() +
			                                   userName);
		}
		return null;
	}

	public void remove(String userName, UserStoreManager userStoreManager) throws IdentityException {

		if (userName == null) {
			return;
		}

		cache.remove(CarbonContext.getCurrentContext().getTenantId() + userName);

		invalidateCache(userName);
	}

	public void invalidateCache(String userName) throws IdentityException {

		if (log.isDebugEnabled()) {
			log.debug("Init invalidation caching process");
		}
		// sending cluster message
		CacheInvalidator invalidator = IdentityMgtServiceComponent.getCacheInvalidator();
		try {
			if (invalidator != null) {
				invalidator.invalidateCache("IDENTITY_LOGIN_DATA_CACHE",
				                            CarbonContext.getCurrentContext().getTenantId() +
				                                    userName);
				if (log.isDebugEnabled()) {
					log.debug("Calling invalidation cache");
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Not calling invalidation cache");
				}
			}
		} catch (CacheException e) {
			log.error("Error while invalidating cache", e);
		}
	}
}
