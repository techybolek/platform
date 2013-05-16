/*
 * Copyright (c) 2010 - 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.IdentityMgtException;
import org.wso2.carbon.identity.mgt.beans.UserMgtBean;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

/**
 * PasswordUtil - Utility class with the password related identity-management operations.
 */
public class PasswordUtil {
    
    private static final Log log = LogFactory.getLog(PasswordUtil.class);


    /**
     * Update Password with the user input
     *
     * @param userMgtBean
     * @return true - if password was successfully reset
     * @throws IdentityMgtException
     */
    public static boolean updatePassword(UserMgtBean userMgtBean) throws IdentityMgtException {

        String userName = userMgtBean.getUserId();
        String password = userMgtBean.getUserPassword();
        String tenantDomain = userMgtBean.getTenantDomain();
        int tenant = Utils.getTenantId(tenantDomain);

        if(userName == null || userName.trim().length() < 1 ||
                password == null || password.trim().length() < 1 ){
            String msg = "Unable to find the required information for updating password";
            log.error(msg);
            throw new IdentityMgtException(msg);
        }

        try {
            UserStoreManager userStoreManager = IdentityMgtServiceComponent.
                                    getRealmService().getTenantUserRealm(tenant).getUserStoreManager();

            userStoreManager.updateCredentialByAdmin(userName, password);
            if(log.isDebugEnabled()){
                String msg = "Password is updated for  user: " + userName;
                if (tenantDomain != null && tenantDomain.trim().length() > 0) {
                    msg = msg + "@" + tenantDomain;
                }            
                log.debug(msg);
            }
            return true;
        } catch (UserStoreException e) {
            String msg = "Error in changing the password, user name: " + userName + "  domain: " +
                    tenantDomain + ".";
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }
    }

    /**
     *
     * @param value
     * @return
     * @throws UserStoreException
     */
    public static String doHash(String value) throws UserStoreException {
        try {
            String digsestFunction = "SHA-256";
            MessageDigest dgst = MessageDigest.getInstance(digsestFunction);
            byte[] byteValue = dgst.digest(value.getBytes());
            return Base64.encode(byteValue);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
    }

}
