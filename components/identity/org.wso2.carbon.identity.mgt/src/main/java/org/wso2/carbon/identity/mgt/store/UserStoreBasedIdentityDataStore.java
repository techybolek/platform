/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.dto.UserIdentityDTO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

/**
 * This module persists data in to user store as user's attribute
 */
public class UserStoreBasedIdentityDataStore extends InMemoryIdentityDataStore {

    private static Log log = LogFactory.getLog(UserStoreBasedIdentityDataStore.class);

    @Override
    public void store(UserIdentityDTO userIdentityDTO, UserStoreManager userStoreManager) throws IdentityException {

        super.store(userIdentityDTO, userStoreManager);

        if(userIdentityDTO.getUserName() == null){
            log.error("Error while persisting user data.  Null user name is provided.");
            return;
        }

        try {
            ((AbstractUserStoreManager) userStoreManager).setUserClaimValues(userIdentityDTO.getUserName(),
                                                                userIdentityDTO.getUserDataMap(), null);
            
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while persisting user data ",e);
        }
    }

    @Override
    public UserIdentityDTO load(String userName, UserStoreManager userStoreManager) throws IdentityException {

        UserIdentityDTO userIdentityDTO = super.load(userName, userStoreManager);
        if(userIdentityDTO != null){
            return userIdentityDTO;
        }

        String[] data = new String[]{
            UserIdentityDataStore.FAIL_LOGIN_ATTEMPTS, UserIdentityDataStore.LAST_LOGON_TIME,
            UserIdentityDataStore.PASSWORD_CHANGE_REQUIRED, UserIdentityDataStore.LAST_FAILED_LOGIN_ATTEMPT_TIME,
            UserIdentityDataStore.TEMPORARY_LOCK, UserIdentityDataStore.UNLOCKING_TIME,
            UserIdentityDataStore.ON_TIME_PASSWORD, UserIdentityDataStore.PASSWORD_TIME_STAMP,
            UserIdentityDataStore.ACCOUNT_LOCK, UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI
        };

        Map<String, String> userDataMap = null;
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(data));
        try {
            String challengeUriString = ((AbstractUserStoreManager)userStoreManager).
                    getUserClaimValue(userName, UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI, null);
            if(challengeUriString != null){
                String[] challengeUris =  challengeUriString.
                        split(IdentityMgtConfig.getInstance().getChallengeQuestionSeparator());
                list.addAll(Arrays.asList(challengeUris));
            }
            userDataMap = ((AbstractUserStoreManager)userStoreManager).
                    getUserClaimValues(userName, list.toArray(new String[list.size()]), null);
        } catch (UserStoreException e) {
            // ignore may be use is not exist
        }

        // if user is exiting there must be at least one user attribute.
        if(userDataMap != null && userDataMap.size() > 0){
            userIdentityDTO = new UserIdentityDTO(userName, userDataMap);
            super.cache.put(CarbonContext.getCurrentContext().getTenantId() + userName, userIdentityDTO);
            return userIdentityDTO;
        }

        return null;
    }


    
}
