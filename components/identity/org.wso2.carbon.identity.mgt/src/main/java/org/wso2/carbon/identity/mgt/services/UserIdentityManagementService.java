package org.wso2.carbon.identity.mgt.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean;
import org.wso2.carbon.captcha.mgt.util.CaptchaUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.beans.UserIdentityMgtBean;
import org.wso2.carbon.identity.mgt.dto.IdentityMetadataDO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.UserIdentityManagementUtil;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class UserIdentityManagementService {

	Log log = LogFactory.getLog(UserIdentityManagementService.class);

	/**
	 * Authenticates the user with the temporary credentials and returns user
	 * identity recovery data such as primary email address, telephone number
	 * and all other identity claims of the user including the identity property
	 * "isUserMustChangePassword". These claims are useful when the user is
	 * recovering the identity using a temporary credential may be after
	 * forgetting their password or after the identity being stolen. Then they
	 * can update the values for these identity claims to keep their identity
	 * safe.
	 * 
	 * @param userName
	 * @param tempCredential
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public UserIdentityClaimDTO[] authenticateWithTemporaryCredentials(String userName, String tempCredential)
	                                                                                                          throws IdentityMgtServiceException {
		try {
			int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));

			boolean isValid =
			                  UserIdentityManagementUtil.isValidIdentityMetadata(userName,
			                                                                     tenantId,
			                                                                     IdentityMetadataDO.METADATA_TEMPORARY_CREDENTIAL,
			                                                                     tempCredential);

			if (!isValid) {
				log.warn("WARNING: Invalidated temporary credential provided by " + userName);
				throw new IdentityMgtServiceException("Invalid temporary credential provided");
			}
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			userStoreManager.authenticate(userName, tempCredential);
			// this credential should not be used again
			UserIdentityManagementUtil.invalidateUserIdentityMetadata(userName,
			                                                          tenantId,
			                                                          IdentityMetadataDO.METADATA_TEMPORARY_CREDENTIAL,
			                                                          tempCredential);
			return UserIdentityManagementUtil.getAllUserIdentityClaims(userName);
		} catch (UserStoreException e) {
			log.error("Error while authenticating", e);
			throw new IdentityMgtServiceException("Error while authenticating the user");
		} catch (IdentityException e) {
			log.error("Error while authenticating", e);
			throw new IdentityMgtServiceException("Error while authenticating the user");
		}
	}

	/**
	 * Validates the confirmation code and then unlock the user account
	 * 
	 * @param userName
	 * @param confirmationCode
	 * @return 
	 * @throws IdentityMgtServiceException
	 */
	public UserIdentityClaimDTO[] confirmUserRegistration(String userName, String confirmationCode)
	                                                                             throws IdentityMgtServiceException {
		try {
			int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
			// throws an exception if invalid
			boolean isValid =
			                  UserIdentityManagementUtil.isValidIdentityMetadata(userName,
			                                                                     tenantId,
			                                                                     IdentityMetadataDO.METADATA_CONFIRMATION_CODE,
			                                                                     confirmationCode);
			if (!isValid) {
				log.warn("WARNING: Invalid confirmation code provided by " + userName);
				throw new IdentityMgtServiceException("Invalid confirmation code provided");
			}
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			// update the user identity claim
			UserIdentityManagementUtil.unlockUserAccount(userName, userStoreManager);
			// invalidate the confirmation code
			UserIdentityManagementUtil.invalidateUserIdentityMetadata(userName,
			                                                          tenantId,
			                                                          IdentityMetadataDO.METADATA_CONFIRMATION_CODE,
			                                                          confirmationCode);
			return UserIdentityManagementUtil.getAllUserIdentityClaims(userName);
		} catch (UserStoreException e) {
			log.error("Error while confirming the account", e);
			throw new IdentityMgtServiceException("Error while confirming the account");
		} catch (IdentityException e) {
			log.error("Error while confirming the account", e);
			throw new IdentityMgtServiceException("Error while confirming the account");
		}
	}

	/**
	 * Checks the security questions and their answerers against the user's
	 * stored questions and answerers. If not all security questions of the user
	 * are answered, an exception will be thrown. After all security questions
	 * are answered properly, then the system will generate a random password,
	 * and reset the user password with it and then will be returned the
	 * resulting DTO containing the temporary password.
	 * 
	 * @param userName
	 * @param secQuesAnsweres
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public void recoverUserIdentityWithSecurityQuestions(String userName,
	                                                     UserIdentityClaimDTO[] secQuesAnsweres)
	                                                                                            throws IdentityMgtServiceException {
		try {
			int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			UserIdentityClaimDTO[] storedSecQuesAnswers =
			                                              UserIdentityManagementUtil.getUserSecurityQuestions(userName,
			                                                                                                  userStoreManager);
			// have not answered all questions of the user
			if (secQuesAnsweres.length < storedSecQuesAnswers.length) {
				throw new IdentityMgtServiceException("All questions must be answered");
			}
			// NOW check the answer for every question
			int numberOfAnsweredQuestions = 0; //
			// for every stored security question
			for (UserIdentityClaimDTO storedSecQues : storedSecQuesAnswers) {
				// for every answered security question
				for (UserIdentityClaimDTO answredSecQues : secQuesAnsweres) {
					// when the questions are equal, check for the answer
					if (answredSecQues.getClaimUri().trim().equals(storedSecQues.getClaimUri().trim())) {
						// if answerers are not equal, throw an exception
						if (!answredSecQues.getClaimValue().trim()
						                   .equals(storedSecQues.getClaimValue().trim())) {
							throw new IdentityMgtServiceException(
							                                      "Invalid answeres. Identity recovery failed");
						}
						numberOfAnsweredQuestions++;
					}
				}
			}
			// not all USER's security questions has been answered
			if (numberOfAnsweredQuestions < storedSecQuesAnswers.length) {
				throw new IdentityMgtServiceException("All questions must be answered");
			}
			// now okay to recover

			// reset the password with a random value
			char[] tempPassword = UserIdentityManagementUtil.generateTemporaryPassword();
			userStoreManager.updateCredentialByAdmin(userName, tempPassword);

			// store the temp password as a Metadata
			IdentityMetadataDO metadataDO = new IdentityMetadataDO();
			metadataDO.setUserName(userName).setTenantId(tenantId).setMetadata(new String(tempPassword))
			          .setMetadataType(IdentityMetadataDO.METADATA_TEMPORARY_CREDENTIAL);
			UserIdentityManagementUtil.storeUserIdentityMetadata(metadataDO);

			// sending an email to the user
			UserIdentityMgtBean bean = new UserIdentityMgtBean();
			String email =
			               userStoreManager.getUserClaimValue(userName,
			                                                  IdentityMgtConfig.getInstance()
			                                                                   .getAccountRecoveryClaim(),
			                                                  null);
			log.debug("Sending email to " + email);
			bean.setUserId(userName).setUserTemporaryPassword(new String(tempPassword)).setEmail(email);
			UserIdentityManagementUtil.notifyViaEmail(bean);

		} catch (UserStoreException e) {
			log.error("Error while recovering user identity", e);
			throw new IdentityMgtServiceException("Error while recovering user identity");
		} catch (IdentityException e) {
			log.error("Error while recovering user identity", e);
			throw new IdentityMgtServiceException("Error while recovering user identity");
		}
	}

	/**
	 * Recovers the account with user email
	 * 
	 * @param userName
	 * @throws IdentityMgtServiceException
	 */
	public void recoverUserIdentityWithEmail(String userName) throws IdentityMgtServiceException {
		int tenantId;
		try {
			tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			// reset the password with a random value
			char[] tempPassword = UserIdentityManagementUtil.generateTemporaryPassword();
			userStoreManager.updateCredentialByAdmin(userName, new String(tempPassword));
			// sending email 
			UserIdentityMgtBean bean = new UserIdentityMgtBean();
			String email =
			               userStoreManager.getUserClaimValue(userName,
			                                                  IdentityMgtConfig.getInstance()
			                                                                   .getAccountRecoveryClaim(),
			                                                  null);
			log.debug("Sending email to " + email);
			bean.setUserId(userName).setUserTemporaryPassword(new String(tempPassword)).setEmail(email);
			UserIdentityManagementUtil.notifyViaEmail(bean);
			
		} catch (IdentityMgtServiceException e) {
			log.error("Error while recovering user identity", e);
			throw new IdentityMgtServiceException("Error while recovering user identity");
		} catch (UserStoreException e) {
			log.error("Error while recovering user identity", e);
			throw new IdentityMgtServiceException("Error while recovering user identity");
		}
	}

	/**
	 * Users can register themselves
	 * 
	 * @param userName
	 * @param credential
	 * @param roleList
	 * @param claims
	 * @param profileName
	 * @throws IdentityMgtServiceException
	 */
	public void registerUser(String userName, Object credential, String[] roleList,
	                         UserIdentityClaimDTO[] claims, String profileName)
	                                                                           throws IdentityMgtServiceException {
		try {
			int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			Map<String, String> claimsMap = new HashMap<String, String>();
			for (UserIdentityClaimDTO claim : claims) {
				if (claim.getClaimUri().contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
					log.warn("WARNING! Possible attack from user " + userName);
					throw new IdentityMgtServiceException("Modification to the " + claim.getClaimUri() +
					                                      " is not allowed");
				}
				claimsMap.put(claim.getClaimUri(), claim.getClaimValue());
			}
			userStoreManager.addUser(userName, credential, roleList, claimsMap, null);
		} catch (UserStoreException e) {
			log.error("Error while reading identity claims", e);
			throw new IdentityMgtServiceException("Error while reading identity claims");
		}
	}

	/**
	 * Returns if the user exist in the system
	 * 
	 * @param userName
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public boolean verifyUserID(String userName) throws IdentityMgtServiceException {
		try {
			int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			return userStoreManager.isExistingUser(userName);
		} catch (UserStoreException e) {
			log.error("Error while reading identity claims", e);
			throw new IdentityMgtServiceException("Error while reading identity claims");
		}
	}

	/**
	 * 
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public CaptchaInfoBean generateRandomCaptcha() throws IdentityMgtServiceException {
		try {
			CaptchaUtil.cleanOldCaptchas();
			return CaptchaUtil.generateCaptchaImage();
		} catch (Exception e) {
			log.error("Error while generating captcha", e);
			throw new IdentityMgtServiceException("Error while generating captcha", e);
		}
	}

	/**
	 * Returns an array of primary security questions. Primary security
	 * questions are the security questions which were configured by the admin
	 * and every user will have to answer selected set of questions from this.
	 * 
	 * @return
	 * @throws IdentityMgtServiceException 
	 */
	public String[] getPrimarySecurityQuestions() throws IdentityMgtServiceException {
		try {
	        return UserIdentityManagementUtil.getPrimaryQuestions(-1234);
        } catch (IdentityException e) {
        	throw new IdentityMgtServiceException("Error while reading security questions");
        }
	}

}
