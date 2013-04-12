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

import org.wso2.carbon.identity.mgt.dto.UserIdentityDTO;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;

/**
 * This interface provides to plug module for preferred persistence store.
 */
public abstract class UserIdentityDataStore {

    public static final String ON_TIME_PASSWORD = "http://wso2.org/claims/onTimePassword";

    public static final String PASSWORD_CHANGE_REQUIRED = "http://wso2.org/claims/passwordChangeRequired";

    public static final String TEMPORARY_LOCK = "http://wso2.org/claims/temporaryLock";

    public static final String LAST_FAILED_ATTEMPT_TIME = "http://wso2.org/claims/lastFailedAttemptTime";

    public static final String FAIL_LOGIN_ATTEMPTS = "http://wso2.org/claims/failedLoginAttempts";

    public static final String LAST_LOGON_TIME = "http://wso2.org/claims/lastLogonTime";

    public static final String UNLOCKING_TIME = "http://wso2.org/claims/unlockTime";

    public static final String PASSWORD_TIME_STAMP = "http://wso2.org/claims/passwordTimestamp";

    public static final String ACCOUNT_LOCK = "http://wso2.org/claims/accountLock";

    /**
     * Get all claim types that is need to persist in the store
     *  
     * @return
     */
    public String[] getSupportedTypes() {

        return new String[] {ON_TIME_PASSWORD, PASSWORD_CHANGE_REQUIRED, TEMPORARY_LOCK,
                             LAST_FAILED_ATTEMPT_TIME, FAIL_LOGIN_ATTEMPTS, LAST_LOGON_TIME,
                             UNLOCKING_TIME, PASSWORD_TIME_STAMP, ACCOUNT_LOCK,
                             UserCoreConstants.ClaimTypeURIs.CHALLENGES_URI,
                             UserCoreConstants.ClaimTypeURIs.PRIMARY_CHALLENGES};

    }


    /**
     * Stores data
     *
     * @param userIdentityDTO
     * @param userStoreManager
     */
    public abstract void store(UserIdentityDTO userIdentityDTO, UserStoreManager userStoreManager);

    /**
     * Loads
     *
     * @param userName
     * @param userStoreManager
     * @return
     */
    public abstract UserIdentityDTO load(String userName, UserStoreManager userStoreManager);

    /**
     * Removes
     *
     * @param userName
     * @param userStoreManager
     */
    public abstract void remove(String userName, UserStoreManager userStoreManager);
}
