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
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.NotificationDataDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param tenantId  tenant id
     * @return true if the reset request is processed successfully.
     * @throws IdentityException  if fails
     */
    public NotificationDataDTO processRecoveryWithNotification(UserRecoveryDTO recoveryDTO, int tenantId)
            throws IdentityException {

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

        if(recoveryDTO.getRecoveryType() != null){
            String recoveryType = recoveryDTO.getRecoveryType().trim();

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
        }

        return notificationData; // TODO
    }

    public ChallengeQuestionProcessor getQuestionProcessor() {
        return questionProcessor;
    }
}
