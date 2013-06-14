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
package org.wso2.carbon.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.NotificationDataDTO;
import org.wso2.carbon.identity.mgt.dto.UserDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.store.UserIdentityDataStore;
import org.wso2.carbon.identity.mgt.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;

/**
 *
 *
 */
public class RecoveryProcessor {

    private static final Log log = LogFactory.getLog(RecoveryProcessor.class);

    private Map<String, NotificationSendingModule> modules =
                                                    new HashMap<String, NotificationSendingModule>();

    private NotificationSendingModule defaultModule;

    private UserRecoveryDataStore dataStore;

    private  NotificationSender notificationSender;

    private ChallengeQuestionProcessor questionProcessor;

    public RecoveryProcessor() {

        List<NotificationSendingModule> modules =
                                IdentityMgtConfig.getInstance().getNotificationSendingModules();

        this.defaultModule = modules.get(0);

        for(NotificationSendingModule module : modules){
            this.modules.put(module.getNotificationType(), module);
        }

        this.dataStore = IdentityMgtConfig.getInstance().getRecoveryDataStore();
        this.notificationSender = new NotificationSender();

        questionProcessor = new ChallengeQuestionProcessor();

    }

    /**
     * Processing recovery
     *
     * @param recoveryDTO class that contains user and tenant Information
     * @return true if the reset request is processed successfully.
     * @throws IdentityException  if fails
     */
    public NotificationDataDTO recoverWithNotification(UserRecoveryDTO recoveryDTO) throws IdentityException {

        if(!IdentityMgtConfig.getInstance().isNotificationSending()){
            //return new NotificationDataDTO("Email sending is disabled");
        }

        String notificationAddress;
        String secretKey = null;
        String confirmationKey = null;
        NotificationSendingModule module = null;
        boolean persistData = true;
        String userId = recoveryDTO.getUserId();
        String domainName = recoveryDTO.getTenantDomain();
        int tenantId = recoveryDTO.getTenantId();
        NotificationDataDTO notificationData = new NotificationDataDTO();
        
        String type = recoveryDTO.getNotificationType();
        if(type != null){
            module =  modules.get(type);
        }

        if(module == null){
            module = defaultModule;
        }

        notificationAddress = module.getNotificationAddress(userId, tenantId);

        if ((notificationAddress == null) || (notificationAddress.trim().length() < 0)) {
            log.warn("Notification sending failure. Notification address is not defined for user " + userId);
        }

        //userMgtBean.setEmail(email);

        if(recoveryDTO.getNotification() != null){
            String recoveryType = recoveryDTO.getNotification().trim();

            if(IdentityMgtConstants.Notification.ACCOUNT_CONFORM.equals(recoveryType) ||
                    IdentityMgtConstants.Notification.PASSWORD_RESET_RECOVERY.equals(recoveryType)){

                confirmationKey = UUIDGenerator.generateUUID();
                secretKey = UUIDGenerator.generateUUID();
                notificationData.setNotificationCode(confirmationKey);
            } else if(IdentityMgtConstants.Notification.TEMPORARY_PASSWORD.equals(recoveryType)){
                String temporaryPassword = recoveryDTO.getTemporaryPassword();  // TODO
                if(temporaryPassword == null || temporaryPassword.trim().length() < 1){
                    char[] chars = IdentityMgtConfig.getInstance().getPasswordGenerator().generatePassword();
                    temporaryPassword = new String(chars);
                }
                Utils.updatePassword(userId, tenantId, temporaryPassword);
                notificationData.setNotificationCode(temporaryPassword);
                persistData = false;
            } else if(IdentityMgtConstants.Notification.ACCOUNT_UNLOCK.equals(recoveryType) ||
                    IdentityMgtConstants.Notification.ACCOUNT_ID_RECOVERY.equals(recoveryType)){
                persistData = false;
            }
        }


        notificationData.setNotificationAddress(notificationAddress);
        notificationData.setUserId(userId);
        notificationData.setDomainName(domainName);

        if(persistData){
            UserRecoveryDataDO recoveryDataDO =
                            new UserRecoveryDataDO(userId, tenantId,  secretKey, confirmationKey);
            dataStore.store(recoveryDataDO);

        }

        if(IdentityMgtConfig.getInstance().isNotificationInternallyManaged()){ // TODO?
            module.setNotificationData(notificationData);
            notificationSender.sendNotification(module);
            notificationData.setNotificationSent(true);
        } else {
            notificationData.setNotificationSent(false);
        }

        return notificationData; // TODO
    }

    /**
     * Confirm that confirmation key has been sent to the same user.
     *
     * @param confirmationKey confirmation key from the user
     * @return verification result as a bean
     */
    public VerificationBean verifyConfirmationKey(String confirmationKey) {

        UserRecoveryDataDO dataDO = null;

        try {
            dataDO = dataStore.load(confirmationKey);
        } catch (IdentityException e) {
            return new VerificationBean(VerificationBean.ERROR_CODE_INVALID_USER);
        }


        if(dataDO == null){
            return new VerificationBean(VerificationBean.ERROR_CODE_INVALID_CODE);
        }

        if(dataDO.isValid()){
            return new VerificationBean(VerificationBean.ERROR_CODE_EXPIRED_CODE);
        } else {
            return new VerificationBean(dataDO.getUserName(), dataDO.getSecret());
        }

    }

    /**
     * Verifies user id with underline user store
     *
     * @param userDTO  bean class that contains user and tenant Information
     * @return true/false whether user is verified or not. If user is a tenant
     *         user then always return false
     */
    public VerificationBean verifyUserForRecovery(UserDTO userDTO) {

        String userId = userDTO.getUserId();
        int tenantId = userDTO.getTenantId();

        try {
            UserStoreManager userStoreManager = IdentityMgtServiceComponent.getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager();
            TenantManager tenantManager = IdentityMgtServiceComponent.getRealmService().
                    getTenantManager();

            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                if(userStoreManager.isExistingUser(userId)){
                    if(IdentityMgtConfig.getInstance().isAuthPolicyAccountLockCheck()){
                        String accountLock = userStoreManager.
                                getUserClaimValue(userId, UserIdentityDataStore.ACCOUNT_LOCK, null);
                        if(!Boolean.parseBoolean(accountLock)){
                            String code = UUID.randomUUID().toString();
                            String key = UUID.randomUUID().toString();
                            UserRecoveryDataDO  dataDO =
                                            new UserRecoveryDataDO(userId, tenantId, code, key);
                            dataStore.store(dataDO);
                            log.info("User verification successful for user : " + userId +
                                    " from tenant domain " + userDTO.getTenantDomain());
                            return new VerificationBean(userId, code);
                        }
                    }
                }
            } else if (tenantId > 0) {
                if(userStoreManager.isExistingUser(userId)){
                    if(userId.equals(tenantManager.getTenant(tenantId).getAdminName())){
                        String code = UUID.randomUUID().toString();
                        String key = UUID.randomUUID().toString();
                        UserRecoveryDataDO  dataDO =
                                new UserRecoveryDataDO(userId, tenantId, code, key);
                        dataStore.store(dataDO);
                        log.info("User verification successful for user : " + userId +
                                " from tenant domain " + userDTO.getTenantDomain());
                        return new VerificationBean(userId, code);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return  new VerificationBean(VerificationBean.ERROR_CODE_UN_EXPECTED);
        }

        log.error("User verification failed for user : " + userId +
                " from tenant domain " + userDTO.getTenantDomain());

        return new VerificationBean(VerificationBean.ERROR_CODE_INVALID_USER);
    }
    
    public void createConfirmationCode(UserDTO userDTO, String code) throws IdentityException {
        String key = UUID.randomUUID().toString();
        UserRecoveryDataDO  dataDO =
                new UserRecoveryDataDO(userDTO.getUserId(), userDTO.getTenantId(), code, key);
        dataStore.store(dataDO);
    }
    
    
    public ChallengeQuestionProcessor getQuestionProcessor() {
        return questionProcessor;
    }
}
