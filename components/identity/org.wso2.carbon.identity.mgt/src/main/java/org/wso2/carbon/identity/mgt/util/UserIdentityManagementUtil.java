package org.wso2.carbon.identity.mgt.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.NotificationSender;
import org.wso2.carbon.identity.mgt.beans.UserIdentityMgtBean;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimDTO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimsDO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.mail.AbstractEmailSendingModule;
import org.wso2.carbon.identity.mgt.store.UserIdentityDataStore;
import org.wso2.carbon.identity.mgt.store.JDBCUserRecoveryDataStore;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
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
	public static UserRecoveryDTO getUserIdentityRecoveryData(String userName,
	                                                                  UserStoreManager userStoreManager,
	                                                                  int tenantId)
	                                                                               throws IdentityException {

		JDBCUserRecoveryDataStore metadatStore = new JDBCUserRecoveryDataStore();
		UserRecoveryDataDO[] metadataDO = metadatStore.load(userName, tenantId);
		UserRecoveryDTO registrationDTO = new UserRecoveryDTO(userName);
		// 
		for (UserRecoveryDataDO metadata : metadataDO) {
			// only valid metadata should be returned
//			if (UserRecoveryDataDO.METADATA_CONFIRMATION_CODE.equals(metadata.getMetadataType()) &&
//			    metadata.isValid()) {
//				registrationDTO.setConfirmationCode(metadata.getMetadata());
//			} else if (UserRecoveryDataDO.METADATA_TEMPORARY_CREDENTIAL.equals(metadata.getMetadataType()) &&
//			           metadata.isValid()) {
//				registrationDTO.setTemporaryPassword(metadata.getMetadata());
//			}
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
	 * @param tenantId 
	 * 
	 * @return
	 * @throws IdentityException
	 */
	public static String[] getPrimaryQuestions(int tenantId) throws IdentityException {
		JDBCUserRecoveryDataStore store = new JDBCUserRecoveryDataStore();
		UserRecoveryDataDO[] metadata = store.load("TENANT", tenantId);
		if(metadata.length < 1) {
			return null;
		}
		List<String> validSecurityQuestions = new ArrayList<String>();
		for (UserRecoveryDataDO metadataDO : metadata) {
			// only valid primary security questions are returned
//			if(UserRecoveryDataDO.METADATA_PRIMARAY_SECURITY_QUESTION.equals(metadataDO.getMetadataType()) && metadataDO.isValid()) {
//				validSecurityQuestions.add(metadataDO.getMetadata());
//			}
		}
		String[] questionsList = new String[validSecurityQuestions.size()];
		return validSecurityQuestions.toArray(questionsList);
	}

	/**
	 * Add or update primary security questions
	 * 
	 * @param primarySecurityQuestion
	 * @param tenantId 
	 * @throws IdentityException 
	 */
	public static void addPrimaryQuestions(String[] primarySecurityQuestion, int tenantId) throws IdentityException {
		JDBCUserRecoveryDataStore store = new JDBCUserRecoveryDataStore();
		UserRecoveryDataDO[] metadata = new UserRecoveryDataDO[primarySecurityQuestion.length];
		int i = 0;
		for(String secQuestion : primarySecurityQuestion) {
			if(!secQuestion.contains(UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI)) {
				throw new IdentityException("One or more security questions does not contain the namespace " +
				                            UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI);
			}
			metadata[i++] =
			                new UserRecoveryDataDO("TENANT", tenantId,
			                                       UserRecoveryDataDO.METADATA_PRIMARAY_SECURITY_QUESTION,
			                                       secQuestion);
		}
		store.store(metadata);
	}

	/**
	 * Remove primary security questions
	 * @param tenantId 
	 *
	 * @throws IdentityException 
	 */
	public static void removePrimaryQuestions(String[] primarySecurityQuestion, int tenantId) throws IdentityException {
		JDBCUserRecoveryDataStore store = new JDBCUserRecoveryDataStore();
		UserRecoveryDataDO[] metadata = new UserRecoveryDataDO[primarySecurityQuestion.length];
		int i = 0;
		for(String secQuestion : primarySecurityQuestion) {
			if(!secQuestion.contains(UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI)) {
				throw new IdentityException("One or more security questions does not contain the namespace " +
				                            UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI);
			}
			metadata[i++] =
			                new UserRecoveryDataDO("TENANT", tenantId,
			                                       UserRecoveryDataDO.METADATA_PRIMARAY_SECURITY_QUESTION,
			                                       secQuestion);
		}

	}

	// ---- Util methods for authenticated users ----///

	/**
	 * Update security questions of the logged in user.
	 * 
	 * @param securityQuestion
	 * @param userStoreManager
	 * @throws IdentityException
	 */
	public static void updateUserSecurityQuestions(String userName, UserIdentityClaimDTO[] securityQuestion,
	                                               UserStoreManager userStoreManager)
	                                                                                 throws IdentityException {
		UserIdentityDataStore store = IdentityMgtConfig.getInstance().getIdentityDataStore();
		UserIdentityClaimsDO userIdentityDO = store.load(userName, userStoreManager);
		if (userIdentityDO != null) {
			userIdentityDO.updateUserSequeiryQuestions(securityQuestion);
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
        userIdentityDO = store.load(userName, userStoreManager);
        if (userIdentityDO != null) {
            return userIdentityDO.getUserSequeiryQuestions();
        } else {
            throw new IdentityMgtServiceException("No user account found for user " + userName);
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
		JDBCUserRecoveryDataStore store = new JDBCUserRecoveryDataStore();
		UserRecoveryDataDO metadataDO = store.load(userName, tenantId, metadataType);
		if (metadataDO != null && metadataDO.isValid()) {
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
		JDBCUserRecoveryDataStore store = new JDBCUserRecoveryDataStore();
		UserRecoveryDataDO metadataDO =
		                                    new UserRecoveryDataDO(userName, tenantId, metadataType,
		                                                               metadata);
		store.invalidate(metadataDO);

	}

	/**
	 * Stores new metadata
	 * @param metadata
	 * @throws IdentityException
	 */
	public static void storeUserIdentityMetadata(UserRecoveryDataDO metadata) throws IdentityException {
		JDBCUserRecoveryDataStore store = new JDBCUserRecoveryDataStore();
		metadata.setValid(true);
		store.store(metadata);
	}
	
	
	public static void storeUserIdentityClaims(UserIdentityClaimsDO identityClaims,
	                                           org.wso2.carbon.user.core.UserStoreManager userStoreManager)
	                                                                                                       throws IdentityException {
		IdentityMgtConfig.getInstance().getIdentityDataStore()
		                 .store(identityClaims, userStoreManager);
	}

	public static UserRecoveryDataDO getUserIdentityMetadata(String userName, int tenantId,
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
			List<UserIdentityClaimDTO> allDefaultClaims = new ArrayList<UserIdentityClaimDTO>();
			for (Claim claim : claims) {
				if (claim.getClaimUri().contains(UserCoreConstants.DEFAULT_CARBON_DIALECT)) {
					UserIdentityClaimDTO claimDTO = new UserIdentityClaimDTO();
					claimDTO.setClaimUri(claim.getClaimUri());
					claimDTO.setClaimValue(claim.getValue());
					allDefaultClaims.add(claimDTO);
				}
			}
			UserIdentityClaimDTO[] claimDTOs = new UserIdentityClaimDTO[allDefaultClaims.size()];
			return allDefaultClaims.toArray(claimDTOs);
		} catch (UserStoreException e) {
			throw new IdentityMgtServiceException("Error while getting user identity claims");
		}
	}

	

	public static void notifyViaEmail(UserIdentityMgtBean bean) { //TODO
		// if not module is defined, the default will be loaded
		//AbstractEmailSendingModule emailModule = IdentityMgtConfig.getInstance().getEmailSendingModule();
		//emailModule.setBean(bean);
		NotificationSender sender = new NotificationSender();
		//sender.sendNotification(emailModule);
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
