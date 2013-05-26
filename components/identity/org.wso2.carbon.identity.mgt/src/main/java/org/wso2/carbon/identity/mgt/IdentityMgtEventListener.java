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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.beans.UserIdentityMgtBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimsDO;
import org.wso2.carbon.identity.mgt.dto.IdentityMetadataDO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.store.UserIdentityDataStore;
import org.wso2.carbon.identity.mgt.util.UserIdentityManagementUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;

/**
 * This is an implementation of UserOperationEventListener. This defines
 * additional operations
 * for some of the core user management operations
 * 
 */
public class IdentityMgtEventListener extends AbstractUserOperationEventListener {

	private static final Log log = LogFactory.getLog(IdentityMgtEventListener.class);
	
	private static final String EMPTY_PASSWORD_USED = "EmptyPasswordUsed";
	private static final String USER_IDENTITY_DO = "UserIdentityDO";
	
	/*
	 * The thread local variable to hold data with scope only to that variable.
	 * This is to pass data from doPreX() method to doPostX() and to avoid
	 * infinite loops.
	 */
	public static final ThreadLocal<HashMap<String,Object>> threadLocalProperties = new ThreadLocal<HashMap<String,Object>>() {
		@Override
		protected HashMap<String, Object> initialValue() {
		    return new HashMap<String, Object>();
		}
	};
	
	private IdentityMgtProcessor processor;

	private UserIdentityDataStore module;

	public IdentityMgtEventListener() {

		processor = IdentityMgtServiceComponent.getRecoveryProcessor();
		module = IdentityMgtConfig.getInstance().getIdentityDataStore();
		String adminUserName =
		                       IdentityMgtServiceComponent.getRealmService()
		                                                  .getBootstrapRealmConfiguration()
		                                                  .getAdminUserName();
		try {
			IdentityMgtConfig config = IdentityMgtConfig.getInstance();

			if (config.isListenerEnable()) {
				IdentityMgtServiceComponent.getRealmService()
				                           .getBootstrapRealm()
				                           .getUserStoreManager()
				                           .setUserClaimValue(adminUserName,
				                                              UserIdentityDataStore.ACCOUNT_LOCK,
				                                              Boolean.toString(false), null);
			}
		} catch (UserStoreException e) {
			log.error("Error while init identity listener", e);
		}
	}

	/**
	 * What is this ?
	 */
	@Override
	public int getExecutionOrderId() {
		return 1357;
	}

	/**
	 * This method checks if the user account is locked. If the account is
	 * locked, the authentication process will be terminated after this method
	 * returning false.
	 */
	@Override
	public boolean doPreAuthenticate(String userName, Object credential,
	                                 UserStoreManager userStoreManager) throws UserStoreException {

		if (log.isDebugEnabled()) {
			log.debug("Pre authenticator is called in IdentityMgtEventListener");
		}

		IdentityMgtConfig config = IdentityMgtConfig.getInstance();

		if (!config.isListenerEnable()) {
			return true;
		}

		if (!config.isEnableAuthPolicy()) {
			return true;
		}

		UserIdentityClaimsDO userIdentityDTO = null;
		try {
			userIdentityDTO = module.load(userName, userStoreManager);
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPreAuthenticate", e);
		}

		if (userIdentityDTO == null) {
			log.warn("Invalid user name " + userName);
			return false;
		}
		// if the account is locked, should not be able to log in
		if (userIdentityDTO.isAccountLocked()) {
			log.warn("User account is locked for user : " + userName + ". cannot login until the account is unlocked ");
			return false;
		}

		return true;
	}

	/**
	 * This method locks the accounts after a configured number of
	 * authentication failure attempts. And unlocks accounts based on successful
	 * authentications.
	 */
	@Override
	public boolean doPostAuthenticate(String userName, boolean authenticated,
	                                  UserStoreManager userStoreManager) throws UserStoreException {

		if (log.isDebugEnabled()) {
			log.debug("Post authenticator is called in IdentityMgtEventListener");
		}

		IdentityMgtConfig config = IdentityMgtConfig.getInstance();

		if (!config.isListenerEnable()) {
			return true;
		}

		if (!config.isEnableAuthPolicy()) {
			return authenticated;
		}

		UserIdentityClaimsDO userIdentityDTO = null;
		try {
			userIdentityDTO = module.load(userName, userStoreManager);
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPostAuthenticate", e);
		}

		if (!authenticated && config.isAuthPolicyAccountLockOnFailure()) {
			userIdentityDTO.setFailAttempts();
			// reading the max allowed #of failure attempts
			if (userIdentityDTO.getFailAttempts() >= config.getAuthPolicyMaxLoginAttempts()) {
				if (log.isDebugEnabled()) {
					log.debug("User, " + userName + " has exceed the max failed login attempts. " +
					          "User account would be locked");
				}
				userIdentityDTO.setAccountLock(true);
				// lock time from the config
				int lockTime = IdentityMgtConfig.getInstance().getAuthPolicyLockingTime();
				if (lockTime != 0) {
					userIdentityDTO.setUnlockTime(System.currentTimeMillis() +
					                              (lockTime * 60 * 1000));
				}
			}
			try {
				module.store(userIdentityDTO, userStoreManager);
			} catch (IdentityException e) {
				throw new UserStoreException("Error while doPostAuthenticate", e);
			}
		} else {
			// if the account was locked due to account verification process,
			// the unlock the account and reset the number of failedAttempts
			if (userIdentityDTO.isAccountLocked() || userIdentityDTO.getFailAttempts() > 0) {
				userIdentityDTO.setAccountLock(false);
				userIdentityDTO.setFailAttempts(0);
				userIdentityDTO.setUnlockTime(0);
				try {
					module.store(userIdentityDTO, userStoreManager);
				} catch (IdentityException e) {
					throw new UserStoreException("Error while doPostAuthenticate", e);
				}
			}
		}

		return true;
	}

	/**
	 * This method will set the default/random password if the password provided is
	 * null. The thread local parameter EMPTY_PASSWORD_USED will be used to
	 * track if the password empty in the doPostAddUser.
	 * This method will filter the security question URIs from claims and put those 
	 * to the thread local properties. 
	 */
	@Override
	public boolean doPreAddUser(String userName, Object credential, String[] roleList,
	                            Map<String, String> claims, String profile,
	                            UserStoreManager userStoreManager) throws UserStoreException {

		if (log.isDebugEnabled()) {
			log.debug("Pre add user is called in IdentityMgtEventListener");
		}
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		// empty password account creation
		if (credential == null ||
		    (credential instanceof StringBuffer && ((StringBuffer) credential).toString().trim()
		                                                                      .length() < 1)) {
			if(!config.isEnableTemporaryPassword()) {
				log.error("Empty passwords are not allowed");
				return false;
			}
			if (log.isDebugEnabled()) {
				log.debug("Credentials are null. Using a temporary password as credentials");
			}
			// setting the thread-local to check in doPostAddUser
			threadLocalProperties.get().put(EMPTY_PASSWORD_USED, true); 
			// temporary passwords will be used
			char[] temporaryPassword = UserIdentityManagementUtil.generateTemporaryPassword();
			
			// setting the password value
			((StringBuffer) credential).replace(0, temporaryPassword.length, new String(temporaryPassword));
		}
		// Filtering security question URIs from claims and add them to the thread local dto
		UserIdentityClaimsDO identityDTO = new UserIdentityClaimsDO(userName);
		Map<String,String> userDataMap = new HashMap<String, String>();
		
		for (Map.Entry<String, String> entry : claims.entrySet()) {
			if (entry.getKey().contains(UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI) ||
			    entry.getKey().contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
				userDataMap.put(entry.getKey(), entry.getValue());
				claims.remove(entry.getKey());
			}
		}
		// adding dto to thread local to be read again from the doPostAddUser method
		threadLocalProperties.get().put(USER_IDENTITY_DO, identityDTO);
		return true;
	}

	/**
	 * This method locks the created accounts based on the account policies or
	 * based on the account confirmation method being used. Two account
	 * confirmation methods are used : Temporary Password and Verification Code.
	 * In the case of temporary password is used the temporary password will be
	 * emailed to the user. In the case of verification code, the code will be
	 * emailed to the user. The security questions filter ad doPreAddUser will
	 * be persisted in this method.
	 */
	@Override
	public boolean doPostAddUser(String userName, Object credential, String[] roleList,
	                             Map<String, String> claims, String profile,
	                             UserStoreManager userStoreManager) throws UserStoreException {
		if (log.isDebugEnabled()) {
			log.debug("Pre add user is called in IdentityMgtEventListener");
		}
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		// reading the value from the thread local
		UserIdentityClaimsDO userIdentityClaimsDO = (UserIdentityClaimsDO) threadLocalProperties.get().get(USER_IDENTITY_DO);

		if (config.isEnableUserAccountVerification()) { // empty password account creation
			if (threadLocalProperties.get().containsKey(EMPTY_PASSWORD_USED)) {
				// store identity data
				userIdentityClaimsDO.setAccountLock(false).setPasswordTimeStamp(System.currentTimeMillis());
				try {
					UserIdentityManagementUtil.storeUserIdentityClaims(userIdentityClaimsDO, userStoreManager);
				} catch (IdentityException e) {
					throw new UserStoreException("Error while doPreAddUser", e);
				}
				// store identity metadata
				IdentityMetadataDO metadataDO = new IdentityMetadataDO();
				metadataDO.setUserName(userName).setTenantId(userStoreManager.getTenantId())
				          .setMetadataType(IdentityMetadataDO.METADATA_TEMPORARY_CREDENTIAL)
				          .setMetadata((String) credential);
				try {
	                UserIdentityManagementUtil.storeUserIdentityMetadata(metadataDO);
                } catch (IdentityException e) {
                	throw new UserStoreException("Error while doPreAddUser", e);
                }
				// preparing a bean to send the email
				UserIdentityMgtBean bean = new UserIdentityMgtBean();
				bean.setUserId(userName).setUserTemporaryPassword(credential)
				    .setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_TEMPORARY_PASSWORD)
				    .setEmail(claims.get(config.getAccountRecoveryClaim()));
				// sending email
				UserIdentityManagementUtil.notifyViaEmail(bean);
				return true;
			} else {
				// none-empty passwords. lock account and persist
				userIdentityClaimsDO.setAccountLock(true)
				                    .setPasswordTimeStamp(System.currentTimeMillis());
				try {
					UserIdentityManagementUtil.storeUserIdentityClaims(userIdentityClaimsDO, userStoreManager);
				} catch (IdentityException e) {
					throw new UserStoreException("Error while doPreAddUser", e);
				}
				String confirmationCode = UserIdentityManagementUtil.generateRandomConfirmationCode();
				// store identity metadata
				IdentityMetadataDO metadataDO = new IdentityMetadataDO();
				metadataDO.setUserName(userName).setTenantId(userStoreManager.getTenantId())
				          .setMetadataType(IdentityMetadataDO.METADATA_CONFIRMATION_CODE)
				          .setMetadata(confirmationCode);
				try {
	                UserIdentityManagementUtil.storeUserIdentityMetadata(metadataDO);
                } catch (IdentityException e) {
                	throw new UserStoreException("Error while doPreAddUser", e);
                }
				// sending a mail with the confirmation code
				UserIdentityMgtBean bean = new UserIdentityMgtBean();
				bean.setUserId(userName)
				    .setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_ACCOUNT_CONFORM)
				    .setConfirmationCode(confirmationCode);
				UserIdentityManagementUtil.notifyViaEmail(bean);
				return true;
			}
		}
		// No account recoveries are defined, no email will be sent. 
		if (config.isAuthPolicyAccountLockOnCreation()) {
			// accounts are locked. Admin should unlock
			userIdentityClaimsDO.setAccountLock(true);
			userIdentityClaimsDO.setPasswordTimeStamp(System.currentTimeMillis());
			try {
				config.getIdentityDataStore().store(userIdentityClaimsDO, userStoreManager);
			} catch (IdentityException e) {
				throw new UserStoreException("Error while doPreAddUser", e);
			}
		} 
		return true;	
	}

	/**
	 * This method is used when the admin is updating the credentials with an
	 * empty credential. A random password will be generated and will be mailed
	 * to the user. 
	 */
	@Override
	public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
	                                            UserStoreManager userStoreManager)
	                                                                              throws UserStoreException {

		if (log.isDebugEnabled()) {
			log.debug("Pre add user is called in IdentityMgtEventListener");
		}
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		if (newCredential == null ||
		    (newCredential instanceof StringBuffer && ((StringBuffer) newCredential).toString()
		                                                                            .trim()
		                                                                            .length() < 1)) {

			if (!config.isEnableTemporaryPassword()) {
				log.error("Empty passwords are not allowed");
				return false;
			}
			if (log.isDebugEnabled()) {
				log.debug("Credentials are null. Using a temporary password as credentials");
			}
			// temporary passwords will be used
			char[] temporaryPassword = UserIdentityManagementUtil.generateTemporaryPassword();
			// setting the password value
			((StringBuffer) newCredential).replace(0, temporaryPassword.length, new String(temporaryPassword));

			UserIdentityMgtBean bean = new UserIdentityMgtBean();
			bean.setUserId(userName);
			bean.setConfirmationCode(newCredential.toString());
			bean.setRecoveryType(IdentityMgtConstants.RECOVERY_TYPE_TEMPORARY_PASSWORD);
			try {
				log.debug("Sending the tempory password to the user " + userName);
				processor.processRecoveryUsingEmail(bean, userStoreManager.getTenantId());
			} catch (IdentityMgtServiceException e) {
				log.error("Error while sending temporary password to user's email account");
			}
		} else {
			log.debug("Updating credentials of user " + userName + " by admin with a non-empty password");
		}
		return true;
	}

	/**
	 * This method checks if the updating claim is an user identity data or
	 * security question. Identity data and security questions are updated by
	 * the identity store, therefore they will not be added to the user store.
	 * Other claims are skipped to the set or update.
	 */
	@Override
	public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue,
	                                      String profileName, UserStoreManager userStoreManager)
	                                                                                            throws UserStoreException {
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		UserIdentityDataStore identityDataStore = IdentityMgtConfig.getInstance()
		                                                           .getIdentityDataStore();
		UserIdentityClaimsDO identityDTO = null;
		try {
			// security questions and identity claims are updated at the identity store
			if (claimURI.contains(UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI) ||
			    claimURI.contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
				identityDTO = identityDataStore.load(userName, userStoreManager);
				if (identityDTO == null) { // no such user is added to the system
					return false;
				}
				identityDTO.setUserIdentityDataClaim(claimURI, claimValue);
				return false; // this claim will not be added to the user store
			} else {
				// a simple user claim. add it to the user store
				return true;
			}
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPreAddUser", e);
		}
	}

	/**
	 * As in the above method the user account lock claim, primary challenges
	 * claim will be separately handled. Identity claims will be removed from
	 * the claim set before adding claims to the user store.
	 */
	@Override
	public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims,
	                                       String profileName, UserStoreManager userStoreManager)
	                                                                                             throws UserStoreException {
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		UserIdentityDataStore identityDataStore =
		                                          IdentityMgtConfig.getInstance()
		                                                           .getIdentityDataStore();
		UserIdentityClaimsDO identityDTO;
		try {
			identityDTO = identityDataStore.load(userName, userStoreManager);
			if (identityDTO == null) { // user doesn't exist in the system
				return false;
			}
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPreSetUserClaimValues", e);
		}

		// removing identity claims and security questions
		for (Map.Entry<String, String> entry : claims.entrySet()) {
			if (entry.getKey().contains(UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI) ||
			    entry.getKey().contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
				identityDTO.setUserIdentityDataClaim(entry.getKey(), entry.getValue());
				claims.remove(entry.getKey());
			}
		}
		// storing the identity claims and security questions
		try {
			identityDataStore.store(identityDTO, userStoreManager);
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPreSetUserClaimValues", e);
		}
		return true;
	}

	/**
	 * Deleting user from the identity database. What are the registry keys ?
	 */
	@Override
	public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager)
	                                                                                   throws UserStoreException {

		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		// remove from the identity store
		try {
			IdentityMgtConfig.getInstance().getIdentityDataStore().remove(userName, userStoreManager);
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPostDeleteUser", e);
		}
		// deleting registry meta-data
		UserRegistry registry = null;
		try {
			registry =
			           IdentityMgtServiceComponent.getRegistryService()
			                                      .getConfigSystemRegistry(userStoreManager.getTenantId());
			String identityKeyMgtPath =
			                            IdentityMgtConstants.IDENTITY_MANAGEMENT_KEYS +
			                                    RegistryConstants.PATH_SEPARATOR +
			                                    userStoreManager.getTenantId() +
			                                    RegistryConstants.PATH_SEPARATOR + userName;

			if (registry.resourceExists(identityKeyMgtPath)) {
				registry.delete(identityKeyMgtPath);
			}
		} catch (RegistryException e) {
			log.error("Error while deleting recovery data for user : " + userName +
			          " in tenant : " + userStoreManager.getTenantId(), e);
		}
		return true;
	}

	/**
	 * Adding the user identity data to the claims set
	 */
	@Override
	public boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
	                                        Map<String, String> claimMap,
	                                        UserStoreManager storeManager)
	                                                                      throws UserStoreException {
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		if (claimMap == null) {
			claimMap = new HashMap<String, String>();
		}
		UserIdentityDataStore identityDataStore =
		                                          IdentityMgtConfig.getInstance()
		                                                           .getIdentityDataStore();
		// check if there are identity claims 
		boolean containsIdentityClaims = false;
		for (String claim : claims) {
			if (claim.contains(UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI) ||
			    claim.contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
				containsIdentityClaims = true;
				break;
			}
		}
		// if there are no identity claims, let it go
		if(!containsIdentityClaims) {
			return true;
		}
		// there is/are identity claim/s . load the dto
		UserIdentityClaimsDO identityDTO;
		try {
			identityDTO = identityDataStore.load(userName, storeManager);
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPostGetUserClaimValues", e);
		}
		// if no user identity data found, just continue
		if(identityDTO == null) {
			return true;
		}
		// data found, add the values for security questions and identity claims
		for (String claim : claims) {
			if (identityDTO.getUserDataMap().containsKey(claim)) {
				claimMap.put(claim, identityDTO.getUserDataMap().get(claim));
			} 
		}
		return true;
	}

	/**
	 * Returning the user identity data as a claim
	 */
	@Override
	public boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue,
	                                       String profileName, UserStoreManager storeManager)
	                                                                                         throws UserStoreException {

		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		if (!config.isListenerEnable()) {
			return true;
		}
		UserIdentityDataStore identityDataStore =
		                                          IdentityMgtConfig.getInstance()
		                                                           .getIdentityDataStore();
		UserIdentityClaimsDO identityDTO;
		try {
			identityDTO = identityDataStore.load(userName, storeManager);
		} catch (IdentityException e) {
			throw new UserStoreException("Error while doPostGetUserClaimValue", e);
		}
		// check if its a security question or identity claim 
		if (identityDTO.getUserDataMap().containsKey(claim)) {
			claimValue.add(identityDTO.getUserDataMap().get(claim));
			return false;
		} 
		return true;
	}

}
