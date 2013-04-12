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

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.beans.UserMgtBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserIdentityDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.store.UserIdentityDataStore;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * This is an implementation of UserOperationEventListener.  This defines additional operations
 * for some of core user management operations
 * 
 */
public class IdentityMgtEventListener extends AbstractUserOperationEventListener {

    private static final Log log = LogFactory.getLog(IdentityMgtEventListener.class);

    private IdentityMgtProcessor processor;

    private UserIdentityDataStore module;

    public IdentityMgtEventListener() {

        processor = IdentityMgtServiceComponent.getRecoveryProcessor();
        module = IdentityMgtConfig.getInstance().getIdentityDataStore();
        String adminUserName = IdentityMgtServiceComponent.getRealmService().
                                            getBootstrapRealmConfiguration().getAdminUserName();

        try {
            IdentityMgtConfig config = IdentityMgtConfig.getInstance();

            if(config.isListenerEnable()){
                IdentityMgtServiceComponent.getRealmService().getBootstrapRealm().
                getUserStoreManager().setUserClaimValue(adminUserName, 
                UserIdentityDataStore.ACCOUNT_LOCK, Boolean.toString(false), null);
            }       	
        } catch (UserStoreException e) {
            log.error("Error while init identity listener" , e);
        }
    }

    @Override
    public int getExecutionOrderId() {
        return 1357;   
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential,
                                    UserStoreManager userStoreManager) throws UserStoreException {


        if(log.isDebugEnabled()){
            log.debug("Pre authenticator is called in IdentityMgtEventListener");
        }

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();

        if(!config.isListenerEnable()){
            return true;
        }

        if(!config.isEnableAuthPolicy()){
            return true;    
        }

        UserIdentityDTO userIdentityDTO = module.load(userName, userStoreManager);

        if(userIdentityDTO == null){
            log.warn("Invalid user name " + userName);
            return false;
        }

        if(config.isAuthPolicyAccountLockCheck()){
            if(userIdentityDTO.getAccountLock()){
                log.warn("User account is locked for user : " + userName);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated,
                                      UserStoreManager userStoreManager) throws UserStoreException {

        if(log.isDebugEnabled()){
            log.debug("Post authenticator is called in IdentityMgtEventListener");
        }

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();

        if(!config.isListenerEnable()){
            return true;
        }

        if(!config.isEnableAuthPolicy()){
            return authenticated;
        }

        UserIdentityDTO userIdentityDTO = module.load(userName, userStoreManager);

        if(!authenticated && config.isAuthPolicyAccountLockOnFailure()){
            userIdentityDTO.setFailAttempts();
            if(userIdentityDTO.getFailAttempts() >= config.getAuthPolicyMaxLoginAttempts()){
                if(log.isDebugEnabled()){
                    log.debug("User, " + userName +  " has exceed the max failed login attempts. " +
                            "User account would be locked");
                }
                userIdentityDTO.setAccountLock(true);
                userIdentityDTO.setTemporaryLock(true);
                int lockTime = IdentityMgtConfig.getInstance().getAuthPolicyLockingTime();
                if(lockTime != 0){
                    userIdentityDTO.setUnlockTime(System.currentTimeMillis() + (lockTime*60*1000));
                }                                
            }
            module.store(userIdentityDTO, userStoreManager);
        } else {
            if(userIdentityDTO.getAccountLock() || userIdentityDTO.getFailAttempts() > 0){
                userIdentityDTO.setAccountLock(false);
                userIdentityDTO.setFailAttempts(0);
                userIdentityDTO.setUnlockTime(0);
                module.store(userIdentityDTO, userStoreManager);
            }
        }

        return true;
    }


    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList,
                Map<String, String> claims, String profile, UserStoreManager userStoreManager)
                                                                        throws UserStoreException {

        if(log.isDebugEnabled()){
            log.debug("Pre add user is called in IdentityMgtEventListener");
        }

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();

        if(!config.isListenerEnable()){
            return true;
        }

        processUserChallenges(userName, claims, true, userStoreManager);

        Map<String, String> userData = new HashMap<String, String>();

        for(Map.Entry<String, String> entry : claims.entrySet()){
            if(Arrays.asList(config.getIdentityDataStore().getSupportedTypes()).contains(entry.getKey())){
                String value = entry.getValue();
                if(value != null){
                    if(UserIdentityDataStore.ACCOUNT_LOCK.equals(entry.getKey())){
                        if(UserCoreConstants.USER_LOCKED.equals(value)){
                            value = Integer.toString(UserIdentityDTO.TRUE);
                        } else {
                            value = Integer.toString(UserIdentityDTO.FALSE);
                        }
                    }
                    userData.put(entry.getKey(), value);
                }
            }
        }

        if(userData.size() > 0){
            for(String key : userData.keySet()){
                claims.remove(key);
            }
        }

        if(config.isEnableUserAccountVerification()){

            if(credential == null || (credential instanceof String &&
                                                        ((String)credential).trim().length() < 1)){
                if(log.isDebugEnabled()){
                    log.debug("Credentials are null. Using default user password as credentials");
                }

                String defaultPassword = config.getTemporaryDefaultPassword();

                if(defaultPassword == null || defaultPassword.trim().length() < 1){
                    throw new UserStoreException("Default user password has not been properly configured");
                }

                ((AbstractUserStoreManager)userStoreManager).addUser(userName, defaultPassword,
                                                                        roleList, claims, profile, false);                

                UserMgtBean bean = new UserMgtBean();
                bean.setUserId(userName);
                if(config.isEnableTemporaryPassword()){
                    bean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_TEMPORARY_PASSWORD);
                    // You can not lock account if temporary password is used.
                    UserIdentityDTO userIdentityDTO = new UserIdentityDTO(userName, userData);
                    userIdentityDTO.setAccountLock(false);
                    config.getIdentityDataStore().store(userIdentityDTO, userStoreManager);
                } else {
                    bean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_CONFORM);
                    UserIdentityDTO userIdentityDTO = new UserIdentityDTO(userName, userData);
                    userIdentityDTO.setAccountLock(true);
                    userIdentityDTO.setTemporaryLock(false);
                    config.getIdentityDataStore().store(userIdentityDTO, userStoreManager);
                }
                try {
                    processor.processRecoveryUsingEmail(bean, userStoreManager.getTenantId());
                    if(!config.isEnableTemporaryPassword()){
                        bean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_PASSWORD_RESET);
                        processor.processRecoveryUsingEmail(bean, userStoreManager.getTenantId());
                    }
                } catch (IdentityMgtException e) {
                    log.error("Error while sending temporary password to user's email account");
                }

                return false;
            } else {

                
                ((AbstractUserStoreManager)userStoreManager).addUser(userName, credential, roleList, claims, profile, false);

                //lock account and persist
                UserIdentityDTO userIdentityDTO = new UserIdentityDTO(userName, userData);
                userIdentityDTO.setAccountLock(true);
                userIdentityDTO.setTemporaryLock(false);
                config.getIdentityDataStore().store(userIdentityDTO, userStoreManager);

                UserMgtBean bean = new UserMgtBean();
                bean.setUserId(userName);
                bean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_CONFORM);
                try {
                    processor.processRecoveryUsingEmail(bean, userStoreManager.getTenantId());
                } catch (IdentityMgtException e) {
                    log.error("Error while sending confirmation link to user's email account for user " + userName);
                }

                return false;
            }
        }

        // adding claim value based on property
        if(config.isAuthPolicyAccountLockOnCreation()){
            claims.put(UserIdentityDataStore.ACCOUNT_LOCK, "true");
        } else {
            claims.put(UserIdentityDataStore.ACCOUNT_LOCK, "false");
        }
        claims.put(UserIdentityDataStore.PASSWORD_TIME_STAMP,
                    Long.toString(System.currentTimeMillis()));
        
        return true;
    }


    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList,
                Map<String, String> claims, String profile, UserStoreManager userStoreManager)
                                                                    throws UserStoreException {

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();
        if(!config.isListenerEnable()){
            return true;
        }

        return true;

    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                    UserStoreManager userStoreManager) throws UserStoreException {

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();
        if(!config.isListenerEnable()){
            return true;
        }

        if(newCredential == null || (newCredential instanceof String &&
                                                    ((String)newCredential).trim().length() < 1)){
            if(log.isDebugEnabled()){
                log.debug("Credentials are null. Using default user password as credentials");
            }

            String defaultPassword = config.getTemporaryDefaultPassword();

            if(defaultPassword == null || defaultPassword.trim().length() < 1){
                throw new UserStoreException("Default user password has not been properly configured");
            }

            ((AbstractUserStoreManager)userStoreManager).updateCredentialByAdmin(userName, defaultPassword);

            UserMgtBean bean = new UserMgtBean();
            bean.setUserId(userName);
            
            if(config.isEnableTemporaryPassword()){
                bean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_TEMPORARY_PASSWORD);
            } else {
                bean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_PASSWORD_RESET);
            }
            try {
                processor.processRecoveryUsingEmail(bean, userStoreManager.getTenantId());
            } catch (IdentityMgtException e) {
                log.error("Error while sending temporary password to user's email account");
            }

            return false;
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue,
                String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();

        if(!config.isListenerEnable()){
            return true;
        }

        UserIdentityDTO identityDTO = null;
        UserIdentityDataStore identityDataStore = IdentityMgtConfig.getInstance().getIdentityDataStore();

        if(Arrays.asList(identityDataStore.getSupportedTypes()).contains(claimURI)){
            identityDTO = identityDataStore.load(userName, userStoreManager);
        }

        if(UserCoreConstants.ClaimTypeURIs.PRIMARY_CHALLENGES.equals(claimURI)){

            throw new UserStoreException("Primary challenges can not be modified");

        } else if(UserIdentityDataStore.ACCOUNT_LOCK.equals(claimURI)){

            if(isLoggedInUser(userName)){
                throw new UserStoreException("You can not change your own account status");
            }

            if(identityDTO != null){
                identityDTO.setAccountLock(Boolean.parseBoolean(claimValue));
                identityDataStore.store(identityDTO, userStoreManager);
            }
            return false;

        } else {

            List<String> challengesUri = processor.getQuestionProcessor().
                            getChallengeQuestionUris(userName, userStoreManager.getTenantId());
            String separator = IdentityMgtConfig.getInstance().getChallengeQuestionSeparator();
            if(challengesUri.contains(claimURI)){
                if(isNewAnswer(userName, claimURI, claimValue, userStoreManager)){
                    if(claimValue != null){
                        claimValue = claimValue.trim();
                        String question = claimValue.substring(0,
                                claimValue.indexOf(separator));
                        String answer = claimValue.
                                substring(claimValue.indexOf(separator)+ 1);
                        if(question != null && answer != null){
                            question = question.trim();
                            answer = answer.trim();
                            claimValue = question + separator + doHash(answer.toLowerCase());
                            if(identityDTO != null){
                                identityDTO.getUserDataMap().put(claimURI, claimValue);
                                identityDataStore.store(identityDTO, userStoreManager);
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims,
                String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();

        if(!config.isListenerEnable()){
            return true;
        }

        UserIdentityDataStore identityDataStore = IdentityMgtConfig.getInstance().getIdentityDataStore();

        if(claims.containsKey(UserCoreConstants.ClaimTypeURIs.PRIMARY_CHALLENGES)){

            throw new UserStoreException("Primary challenges can not be modified");

        }

        // check whether there are already created challenge questions for user

        boolean primaryChallenge = true;
        try{
            String value =userStoreManager.getUserClaimValue(userName,
                                UserCoreConstants.ClaimTypeURIs.PRIMARY_CHALLENGES, null);
            if(value != null && value.trim().length() > 0){
                primaryChallenge = false;
            }
        } catch (Exception e){
            //
        }

        processUserChallenges(userName, claims, primaryChallenge, userStoreManager);

        Map<String, String> userData = new HashMap<String, String>();

        for(Map.Entry<String, String> entry : claims.entrySet()){
            if(Arrays.asList(identityDataStore.getSupportedTypes()).contains(entry.getKey())){
                String value = entry.getValue();
                if(value != null){
                    userData.put(entry.getKey(), value);
                }
            }
        }

        if(userData.size() > 0){
            UserIdentityDTO identityDTO = new UserIdentityDTO(userName, userData);
            identityDataStore.store(identityDTO, userStoreManager);
            for(String key : userData.keySet()){
                claims.remove(key);
            }
        }

        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager)
                                                                        throws UserStoreException {

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();
        if(!config.isListenerEnable()){
            return true;
        }

        IdentityMgtConfig.getInstance().getIdentityDataStore().remove(userName, userStoreManager);

        UserRegistry registry = null;
        try{
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(userStoreManager.getTenantId());
            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
                    RegistryConstants.PATH_SEPARATOR + userStoreManager.getTenantId() +
                    RegistryConstants.PATH_SEPARATOR + userName;

            if (registry.resourceExists(identityKeyMgtPath)) {
                registry.delete(identityKeyMgtPath);
            }
        } catch (RegistryException e) {
            log.error("Error while deleting recovery data for user : " + userName + " in tenant : "
                                                        + userStoreManager.getTenantId(), e);
        }

        return true;
    }

    private boolean isLoggedInUser(String userName){

        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            String loggedInUser = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);
            if(loggedInUser != null && loggedInUser.equals(userName)){
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
                                    Map<String, String> claimMap, UserStoreManager storeManager) {

        IdentityMgtConfig config = IdentityMgtConfig.getInstance();
        if(!config.isListenerEnable()){
            return true;
        }

        if(claimMap == null){
            claimMap = new HashMap<String, String>();
        }

        UserIdentityDataStore identityDataStore = IdentityMgtConfig.getInstance().getIdentityDataStore();
        UserIdentityDTO identityDTO = identityDataStore.load(userName, storeManager);
        for(String  claim : claims){
            if(identityDTO.getUserDataMap().containsKey(claim)){
                claimMap.put(claim, identityDTO.getUserDataMap().get(claim));
            }
        }

        return false;
    }

    @Override
    public boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue,
                                           String profileName, UserStoreManager storeManager) {
        
        IdentityMgtConfig config = IdentityMgtConfig.getInstance();
        if(!config.isListenerEnable()){
            return true;
        }

        UserIdentityDataStore identityDataStore = IdentityMgtConfig.getInstance().getIdentityDataStore();
        UserIdentityDTO identityDTO = identityDataStore.load(userName, storeManager);
        if(identityDTO.getUserDataMap().containsKey(claim)){
            claimValue.add(identityDTO.getUserDataMap().get(claim));
        }

        return false;
    }

    private void processUserChallenges(String userName, Map<String, String> claims,
                           boolean primary, UserStoreManager manager) throws UserStoreException {

        String[] challengeUris = Utils.getChallengeUris();
        List<String> selectedUris = new ArrayList<String>();
        String separator = IdentityMgtConfig.getInstance().getChallengeQuestionSeparator();
        if(challengeUris != null){
            Map<String, String> challengeMap = new HashMap<String, String>();
            for(String challenge : challengeUris){
                challenge = challenge.trim();
                String challengeValue = claims.get(challenge);
                if(challengeValue != null){
                    selectedUris.add(challenge);
                    if(!isNewAnswer(userName, challenge, challengeValue, manager)){
                        continue;
                    }
                    challengeValue = challengeValue.trim();
                    String question = challengeValue.
                        substring(0,challengeValue.indexOf(separator));
                    String answer = challengeValue.
                        substring(challengeValue.indexOf(separator)+ 1);
                    if(question != null && answer != null){
                        question = question.trim();
                        answer = answer.trim();
                        challengeMap.put(question, answer);
                        claims.put(challenge, question + separator +
                                doHash(answer.toLowerCase()));
                    } else {
                        claims.remove(challenge);
                    }
                }
            }

            if(primary){
                String value = "";
                for(Map.Entry<String, String> entry : challengeMap.entrySet()){
                    if("".equals(value)){
                        value = entry.getKey() + separator +
                                doHash(entry.getValue().toLowerCase());
                    } else {
                        value = value + separator + entry.getKey() + separator +
                                                            doHash(entry.getValue().toLowerCase());
                    }
                }
    
                if(!"".equals(value)){
                    claims.put(UserCoreConstants.ClaimTypeURIs.PRIMARY_CHALLENGES, value);
                }

                String selectedUriString = "";
                
                if(selectedUris.size() == 0){
                    selectedUris.addAll(Arrays.asList(challengeUris));
                }

                for(String uri :  selectedUris){
                    if("".equals(selectedUriString)){
                        selectedUriString = uri;
                    } else {
                        selectedUriString = selectedUriString + separator + uri;
                    }
                }

                if(!"".equals(selectedUriString)){
                    claims.put(UserCoreConstants.ClaimTypeURIs.CHALLENGES_URI, selectedUriString);
                }
            }
        }
    }

    private String doHash(String input) throws UserStoreException {
        try {
            MessageDigest dgst;

            String digsestFunction = IdentityMgtConfig.getInstance().getDigsestFunction();

            if (digsestFunction != null) {
                dgst = MessageDigest.getInstance(digsestFunction);
            } else {
                dgst = MessageDigest.getInstance("SHA-256");
            }
            byte[] byteValue = dgst.digest(input.getBytes());
            input = Base64.encode(byteValue);
            return input;
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    private boolean isNewAnswer(String userName, String uri, String value, UserStoreManager manager)
                                                                        throws UserStoreException {

        String oldValue = manager.getUserClaimValue(userName, uri, null);
        return !(oldValue != null && oldValue.equals(value));

    }
}
