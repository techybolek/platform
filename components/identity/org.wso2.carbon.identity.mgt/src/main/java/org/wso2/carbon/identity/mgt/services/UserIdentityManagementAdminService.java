package org.wso2.carbon.identity.mgt.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimDTO;
import org.wso2.carbon.identity.mgt.dto.UserIdentityRecoveryDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.UserIdentityManagementUtil;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This is the admin service for the identity management. Some of these
 * operations are can only be carried out by admins. The other operations are
 * allowed to all logged in users.
 * 
 * @author sga
 * 
 */
public class UserIdentityManagementAdminService {

	private static Log log = LogFactory.getLog(UserIdentityManagementAdminService.class);

	// --------Operations require Admin permissions ---------//

/*	*//**
	 * Admin adds a user to the system. The returning
	 * {@code UserIdentityRecoveryDTO} contains the temporary password or the
	 * account confirmation code to be sent to the user to complete the
	 * registration process.
	 * 
	 * @param userName
	 * @param credential
	 * @param roleList
	 * @param claims
	 * @param profileName
	 * @return
	 * @throws IdentityMgtServiceException
	 *//*
	public UserIdentityRecoveryDTO addUser(String userName, String credential, String[] roleList,
	                                       UserIdentityClaimDTO[] claims, String profileName)
	                                                                                         throws IdentityMgtServiceException {
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			Map<String, String> claimsMap = new HashMap<String, String>();
			for (UserIdentityClaimDTO claim : claims) {
				// claims with "http://wso2.org/claims/identity" cannot be modified   
				if (claim.getClaimUri().contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
					throw new IdentityMgtServiceException("Modification to the " + claim.getClaimUri() +
					                                      " is not allowed");
				}
				claimsMap.put(claim.getClaimUri(), claim.getClaimValue());
			}
			userStoreManager.addUser(userName, credential, roleList, claimsMap, profileName);
			return UserIdentityManagementUtil.getUserIdentityRecoveryData(userName, userStoreManager,
			                                                              tenantId);
		} catch (UserStoreException e) {
			log.error("Error while adding user", e);
			throw new IdentityMgtServiceException("Add user operation failed");
		} catch (IdentityException e) {
			log.error("Error while reading registration info", e);
			throw new IdentityMgtServiceException("Unable to read registration info");
		}
	}*/

	/**
	 * Admin can get the user account registration data if it was not read from
	 * the above {@link addUser()} method. The returning
	 * {@code UserIdentityRecoveryDTO} contains the temporary password or the
	 * confirmation code.
	 * 
	 * @param userName
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public UserIdentityRecoveryDTO getUserIdentityRegistrationData(String userName)
	                                                                               throws IdentityMgtServiceException {
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			return UserIdentityManagementUtil.getUserIdentityRecoveryData(userName, userStoreManager,
			                                                              tenantId);
		} catch (UserStoreException e) {
			log.error("Error while loading user store", e);
			throw new IdentityMgtServiceException("Unable to read registration info");
		} catch (IdentityException e) {
			log.error("Error while reading registration info", e);
			throw new IdentityMgtServiceException("Unable to read registration info");
		}
	}

	/**
	 * Admin deletes a user from the system. This is an irreversible operation.
	 * 
	 * @param userName
	 * @throws IdentityMgtServiceException
	 */
	public void deleteUser(String userName) throws IdentityMgtServiceException {
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			userStoreManager.deleteUser(userName);
		} catch (UserStoreException e) {
			log.error("Error while deleting user", e);
			throw new IdentityMgtServiceException("Error while deleting user");
		}
	}

	/**
	 * Admin locks the user account. Only the admin can unlock the account using
	 * the {@literal unlockUserAccount} method.
	 * 
	 * @param userName
	 * @throws IdentityMgtServiceException
	 */
	public void lockUserAccount(String userName) throws IdentityMgtServiceException {
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			UserIdentityManagementUtil.lockUserAccount(userName, userStoreManager);
			log.info("User account " + userName + " locked");
		} catch (UserStoreException e) {
			log.error("Error while loading user store", e);
			throw new IdentityMgtServiceException("Unable to lock the account");
		} catch (IdentityException e) {
			log.error("Error while reading registration info", e);
			throw new IdentityMgtServiceException("Unable to lock the account");
		}
	}

	/**
	 * Admin unlocks the user account.
	 * 
	 * @param userName
	 * @throws IdentityMgtServiceException
	 */
	public void unlockUserAccount(String userName) throws IdentityMgtServiceException {
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			UserIdentityManagementUtil.unlockUserAccount(userName, userStoreManager);
		} catch (UserStoreException e) {
			log.error("Error while loading user store", e);
			throw new IdentityMgtServiceException("Unable to unlock the account");
		} catch (IdentityException e) {
			log.error("Error while reading registration info", e);
			throw new IdentityMgtServiceException("Unable to unlock the account");
		}
	}

	/**
	 * Admin resets the password of the user.
	 * 
	 * @param userName
	 * @param newPassword
	 * @throws IdentityMgtServiceException
	 */
	public void resetUserPassword(String userName, String newPassword)
	                                                                  throws IdentityMgtServiceException {
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			userStoreManager.updateCredentialByAdmin(userName, newPassword);
		} catch (UserStoreException e) {
			log.error("Error while resetting the password", e);
			throw new IdentityMgtServiceException("Unable reset the password");
		}
	}

	/**
	 * Admin adds more security questions to the the system. These questions
	 * will be available for all the users.
	 * TODO : these must be store in the database
	 * @param securityQuestion
	 * @throws IdentityMgtServiceException
	 */
	public void addPrimarySecurityQuestions(String[] securityQuestion) throws IdentityMgtServiceException {
		String userName = UserIdentityManagementUtil.getLoggedInUser();
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
	        UserIdentityManagementUtil.addPrimaryQuestions(securityQuestion, tenantId);
        } catch (IdentityException e) {
        	throw new IdentityMgtServiceException("Error while reading security question");
        }
	}

	/**
	 * Admin removes a primary security questions
	 * 
	 * @param securityQuestion
	 * @throws IdentityMgtServiceException 
	 */
	public void removePrimarySecurityQuestion(String[] securityQuestion) throws IdentityMgtServiceException {
		String userName = UserIdentityManagementUtil.getLoggedInUser();
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
	        UserIdentityManagementUtil.removePrimaryQuestions(securityQuestion, tenantId);
        } catch (IdentityException e) {
        	throw new IdentityMgtServiceException("Error while removing identity security question");
        }
	}
	
	
	// ------ Operations require only login permissions --------//

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
			String userName = UserIdentityManagementUtil.getLoggedInUser();
			int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
	        return UserIdentityManagementUtil.getPrimaryQuestions(tenantId);
        } catch (IdentityException e) {
        	throw new IdentityMgtServiceException("Error while reading security questions");
        }
	}
	
	/**
	 * The users changes their answers to the security questions or adding more
	 * security questions.
	 * 
	 * @param securityQuestions
	 * 
	 * @throws IdentityMgtServiceException
	 */
	public void updateUserSecurityQuestion(UserIdentityClaimDTO[] securityQuestion)
	                                                                                throws IdentityMgtServiceException {
		String userName = UserIdentityManagementUtil.getLoggedInUser();
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			UserIdentityManagementUtil.updateUserSecurityQuestions(userName, securityQuestion,
			                                                       userStoreManager);
		} catch (UserStoreException e) {
			log.error("Error while updating security questions", e);
			throw new IdentityMgtServiceException("Error while updating security questions");
		} catch (IdentityException e) {
			log.error("Error while updating security questions", e);
			throw new IdentityMgtServiceException("Error while updating security questions");
		}
	}

	/**
	 * Returns all security questions and their answers of the user
	 * 
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public UserIdentityClaimDTO[] getUserSecurityQuestions() throws IdentityMgtServiceException {
		String userName = UserIdentityManagementUtil.getLoggedInUser();
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			return UserIdentityManagementUtil.getUserSecurityQuestions(userName, userStoreManager);
		} catch (UserStoreException e) {
			log.error("Error while reading security questions", e);
			throw new IdentityMgtServiceException("Error while reading security questions");
		}
	}

	/**
	 * User updates/add account recovery data such as the email address or the
	 * phone number etc.
	 * 
	 * @param userIdentityClaims
	 * @throws IdentityMgtServiceException
	 */
	public void updateUserIdentityClaims(UserIdentityClaimDTO[] userIdentityClaims)
	                                                                               throws IdentityMgtServiceException {
		String userName = UserIdentityManagementUtil.getLoggedInUser();
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();

			Map<String, String> claims = new HashMap<String, String>();
			for (UserIdentityClaimDTO dto : userIdentityClaims) {
				if (dto.getClaimUri().contains(UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI)) {
					log.warn("WARNING! User " + userName + " tried to alter " + dto.getClaimUri());
					throw new IdentityException("Updates to the claim " + dto.getClaimUri() +
					                            " are not allowed");
				}
				claims.put(dto.getClaimUri(), dto.getClaimValue());
			}
			userStoreManager.setUserClaimValues(userName, claims, null);

		} catch (UserStoreException e) {
			log.error("Error while updating user identity recovery data", e);
			throw new IdentityMgtServiceException(
			                                      "Error while updating user identity recovery data");
		} catch (IdentityException e) {
			log.error("Error while updating user identity recovery data", e);
			throw new IdentityMgtServiceException(
			                                      "Error while updating user identity recovery data");
		}
	}

	/**
	 * Returns all user claims which can be used in the identity recovery
	 * process
	 * such as the email address, telephone number etc
	 * 
	 * @return
	 * @throws IdentityMgtServiceException
	 */
	public UserIdentityClaimDTO[] getAllUserIdentityClaims() throws IdentityMgtServiceException {
		String userName = UserIdentityManagementUtil.getLoggedInUser();
		return UserIdentityManagementUtil.getAllUserIdentityClaims(userName);
	}
	
	/**
	 * User change the password of the user.
	 * 
	 * @param newPassword
	 * @throws IdentityMgtServiceException
	 */
	public void changeUserPassword(String newPassword, String oldPassword) throws IdentityMgtServiceException {
		String userName = UserIdentityManagementUtil.getLoggedInUser();
		int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));
		try {
			UserStoreManager userStoreManager =
			                                    IdentityMgtServiceComponent.getRealmService()
			                                                               .getTenantUserRealm(tenantId)
			                                                               .getUserStoreManager();
			userStoreManager.updateCredential(userName, newPassword, oldPassword);
		} catch (UserStoreException e) {
			log.error("Error while resetting the password", e);
			throw new IdentityMgtServiceException("Unable reset the password");
		}
	}

}
