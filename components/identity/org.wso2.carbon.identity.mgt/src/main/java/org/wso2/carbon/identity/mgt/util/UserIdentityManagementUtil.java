package org.wso2.carbon.identity.mgt.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.beans.UserIdentityMgtBean;
import org.wso2.carbon.identity.mgt.dto.IdentityMetadataDO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimDTO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimsDO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityRecoveryDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.mail.EmailSender;
import org.wso2.carbon.identity.mgt.mail.EmailSendingModule;
import org.wso2.carbon.identity.mgt.store.UserIdentityDataStore;
import org.wso2.carbon.identity.mgt.store.UserIdentityMetadataStore;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This is the Utility class used by the admin service to read and write
 * identity data.
 * 
 * @author sga
 * 
 */
public class UserIdentityManagementUtil {

	/**
	 * Returns the registration information such as the temporary password or
	 * the confirmation code
	 * 
	 * @param userName
	 * @param userStoreManager
	 * @param tenantId
	 * @return
	 * @throws IdentityException
	 */
	public static UserIdentityRecoveryDTO getUserIdentityRecoveryData(String userName,
	                                                                  UserStoreManager userStoreManager,
	                                                                  int tenantId)
	                                                                               throws IdentityException {

		UserIdentityMetadataStore metadatStore = new UserIdentityMetadataStore();
		IdentityMetadataDO[] metadataDO = metadatStore.loadMetadata(userName, tenantId);
		UserIdentityRecoveryDTO registrationDTO = new UserIdentityRecoveryDTO(userName);
		// 
		for (IdentityMetadataDO metadata : metadataDO) {
			// only valid metadata should be returned
			if (IdentityMetadataDO.METADATA_CONFIRMATION_CODE.equals(metadata.getMetadataType()) &&
			    metadata.isValid()) {
				registrationDTO.setConfirmationCode(metadata.getMetadata());
			} else if (IdentityMetadataDO.METADATA_TEMPORARY_CREDENTIAL.equals(metadata.getMetadataType()) &&
			           metadata.isValid()) {
				registrationDTO.setTemporaryPassword(metadata.getMetadata());
			}
		}
		return registrationDTO;
	}

	/**
	 * Locks the user account.
	 * 
	 * @param userName
	 * @param userStoreManager
	 * @throws IdentityException
	 */
	public static void lockUserAccount(String userName, UserStoreManager userStoreManager)
	                                                                                      throws IdentityException {
		UserIdentityDataStore store = IdentityMgtConfig.getInstance().getIdentityDataStore();
		UserIdentityClaimsDO userIdentityDO = store.load(userName, userStoreManager);
		if (userIdentityDO != null) {
			userIdentityDO.setAccountLock(true);
			store.store(userIdentityDO, userStoreManager);
		} else {
			throw new IdentityException("No user account found for user " + userName);
		}
	}

	/**
	 * Unlocks the user account
	 * 
	 * @param userName
	 * @param userStoreManager
	 * @throws IdentityException
	 */
	public static void unlockUserAccount(String userName, UserStoreManager userStoreManager)
	                                                                                        throws IdentityException {
		UserIdentityDataStore store = IdentityMgtConfig.getInstance().getIdentityDataStore();
		UserIdentityClaimsDO userIdentityDO = store.load(userName, userStoreManager);
		if (userIdentityDO != null) {
			userIdentityDO.setAccountLock(false);
			store.store(userIdentityDO, userStoreManager);
		} else {
			throw new IdentityException("No user account found for user " + userName);
		}

	}

	/**
	 * Returns an array of primary security questions
	 * 
	 * @return
	 * @throws IdentityException
	 */
	public static String[] getPrimaryQuestions() throws IdentityException {
		UserIdentityMetadataStore store = new UserIdentityMetadataStore();
		IdentityMetadataDO[] metadata =
		                                store.loadMetadata(IdentityMetadataDO.METADATA_PRIMARAY_SECURITY_QUESTION,
		                                                   -1234);
		String[] secQuestions = new String[metadata.length];
		int i = 0;
		for (IdentityMetadataDO dataDO : metadata) {
			secQuestions[i++] = dataDO.getMetadata();
		}
		return secQuestions;
	}

	/**
	 * Add or update primary security questions
	 * 
	 * @param primarySecurityQuestions
	 * @throws IdentityException 
	 */
	public static void setPrimaryQuestions(String[] primarySecurityQuestions) throws IdentityException {
		UserIdentityMetadataStore store = new UserIdentityMetadataStore();
		IdentityMetadataDO[] metadata = new IdentityMetadataDO[primarySecurityQuestions.length];
		int i = 0;
		for(String secQuestion : primarySecurityQuestions) {
			metadata[i++] = new IdentityMetadataDO(null, -1234, IdentityMetadataDO.METADATA_PRIMARAY_SECURITY_QUESTION, secQuestion, true);
		}
		store.storeMetadataSet(metadata);
	}

	/**
	 * Remove primary security questions
	 * 
	 * @param securityQuestions
	 */
	public static void removePrimaryQuestions(String[] primarySecurityQuestions) {
		//store.removePrimarySecurityQuestions(primarySecurityQuestions);
	}

	// ---- Util methods for authenticated users ----///

	/**
	 * Update security questions of the logged in user.
	 * 
	 * @param securityQuestions
	 * @param userStoreManager
	 * @throws IdentityException
	 */
	public static void updateUserSecurityQuestions(String userName,
	                                               UserIdentityClaimDTO[] securityQuestions,
	                                               UserStoreManager userStoreManager)
	                                                                                 throws IdentityException {
		UserIdentityDataStore store = IdentityMgtConfig.getInstance().getIdentityDataStore();
		UserIdentityClaimsDO userIdentityDO = store.load(userName, userStoreManager);
		if (userIdentityDO != null) {
			userIdentityDO.updateUserSequeiryQuestions(securityQuestions);
			store.store(userIdentityDO, userStoreManager);
		} else {
			throw new IdentityException("No user account found for user " + userName);
		}
	}

	/**
	 * Returns security questions of the logged in user
	 * 
	 * @param userStoreManager
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public static UserIdentityClaimDTO[] getUserSecurityQuestions(String userName,
	                                                              UserStoreManager userStoreManager)
	                                                                                                throws IdentityMgtServiceException {
		UserIdentityDataStore store = IdentityMgtConfig.getInstance().getIdentityDataStore();
		UserIdentityClaimsDO userIdentityDO;
		try {
			userIdentityDO = store.load(userName, userStoreManager);
			if (userIdentityDO != null) {
				return userIdentityDO.getUserSequeiryQuestions();
			} else {
				throw new IdentityMgtServiceException("No user account found for user " + userName);
			}
		} catch (IdentityException e) {
			throw new IdentityMgtServiceException(
			                                      "Error while loading security questions for user " +
			                                              userName);
		}
	}

	/**
	 * Updates users recovery data such as the phone number, email etc
	 * 
	 * @param userStoreManager
	 * @param userIdentityRecoveryData
	 * @throws IdentityException
	 */
	public static void updateUserIdentityClaims(String userName, UserStoreManager userStoreManager,
	                                            UserIdentityClaimDTO[] userIdentityRecoveryData)
	                                                                                            throws IdentityException {

		UserIdentityDataStore store = IdentityMgtConfig.getInstance().getIdentityDataStore();
		UserIdentityClaimsDO userIdentityDO = store.load(userName, userStoreManager);
		if (userIdentityDO != null) {
			userIdentityDO.updateUserIdentityRecoveryData(userIdentityRecoveryData);
			store.store(userIdentityDO, userStoreManager);
		} else {
			throw new IdentityException("No user account found for user " + userName);
		}

	}

	/**
	 * Returns all user claims which can be used in the identity recovery
	 * process
	 * 
	 * @param userName
	 * @param userStoreManager
	 * @return
	 * @throws IdentityException
	 */
	public static UserIdentityClaimDTO[] getUserIdentityClaims(String userName,
	                                                           UserStoreManager userStoreManager)
	                                                                                             throws IdentityException {
		UserIdentityDataStore store = IdentityMgtConfig.getInstance().getIdentityDataStore();
		UserIdentityClaimsDO userIdentityDO = store.load(userName, userStoreManager);
		if (userIdentityDO != null) {
			return userIdentityDO.getUserIdentityRecoveryData();
		} else {
			throw new IdentityException("No user account found for user " + userName);
		}
	}

	/**
	 * Validates user identity metadata to be valid or invalid. 
	 * @param userName
	 * @param tenantId
	 * @param metadataType
	 * @param metadata
	 * @return
	 * @throws IdentityException
	 */
	public static boolean isValidIdentityMetadata(String userName, int tenantId, String metadataType,
	                                                   String metadata) throws IdentityException {
		UserIdentityMetadataStore store = new UserIdentityMetadataStore();
		IdentityMetadataDO metadataDO = store.loadMetadata(userName, tenantId, metadataType, metadata);
		if (metadataDO.isValid()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Invalidates the identity metadata
	 * 
	 * @param userName
	 * @param tenantId
	 * @param metadataType
	 * @param metadata
	 * @throws IdentityException
	 */
	public static void invalidateUserIdentityMetadata(String userName, int tenantId, String metadataType,
	                                                  String metadata) throws IdentityException {
		UserIdentityMetadataStore store = new UserIdentityMetadataStore();
		IdentityMetadataDO metadataDO =
		                                    new IdentityMetadataDO(userName, tenantId, metadataType,
		                                                               metadata, false);
		store.invalidateMetadata(metadataDO);

	}

	/**
	 * Stores new metadata
	 * @param metadata
	 * @throws IdentityException
	 */
	public static void storeUserIdentityMetadata(IdentityMetadataDO metadata) throws IdentityException {
		UserIdentityMetadataStore store = new UserIdentityMetadataStore();
		metadata.setValid(true);
		store.storeMetadata(metadata);
	}
	
	
	public static void storeUserIdentityClaims(UserIdentityClaimsDO identityClaims,
	                                           org.wso2.carbon.user.core.UserStoreManager userStoreManager)
	                                                                                                       throws IdentityException {
		IdentityMgtConfig.getInstance().getIdentityDataStore()
		                 .store(identityClaims, userStoreManager);
	}

	public static IdentityMetadataDO getUserIdentityMetadata(String userName, int tenantId,
	                                                             String metadataType) {
		return null;
	}

	/**
	 * Returns all user claims
	 * 
	 * @param userName
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public static UserIdentityClaimDTO[] getAllUserIdentityClaims(String userName)
	                                                                              throws IdentityMgtServiceException {
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			// read all claims and convert them to UserIdentityClaimDTO
			Claim[] claims = userStoreManager.getUserClaimValues(userName, null);
			UserIdentityClaimDTO[] claimDTOs = new UserIdentityClaimDTO[claims.length];
			int i = 0;
			for (Claim claim : claims) {
				UserIdentityClaimDTO claimDTO = new UserIdentityClaimDTO();
				claimDTO.setClaimUri(claim.getClaimUri());
				claimDTO.setClaimValue(claim.getValue());
				claimDTOs[i++] = claimDTO;
			}
			return claimDTOs;
		} catch (UserStoreException e) {
			throw new IdentityMgtServiceException("Error while getting user identity claims");
		}
	}

	

	public static void notifyViaEmail(UserIdentityMgtBean bean) {
		// if not module is defined, the default will be loaded
		EmailSendingModule emailModule = IdentityMgtConfig.getInstance().getEmailSendingModule();
		emailModule.setBean(bean);
		EmailSender sender = new EmailSender();
		sender.sendEmail(emailModule);
	}

	// ------ other Util methods

	public static String getLoggedInUser() {
		MessageContext msgContext = MessageContext.getCurrentMessageContext();
		HttpServletRequest request =
		                             (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			return (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);
		}
		return null;
	}

	/**
	 * Generates a random password
	 * 
	 * @return
	 */
	public static char[] generateTemporaryPassword() {
		IdentityMgtConfig config = IdentityMgtConfig.getInstance();
		return config.getPasswordGenerator().generatePassword();

	}

	/**
	 * Returns a random confirmation code
	 * 
	 * @return
	 */
	public static String generateRandomConfirmationCode() {
		return new String(generateTemporaryPassword());
	}
}
