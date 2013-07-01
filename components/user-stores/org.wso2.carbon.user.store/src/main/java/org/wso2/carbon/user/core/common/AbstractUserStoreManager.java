/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.dto.RoleDTO;
import org.wso2.carbon.user.core.hybrid.HybridRoleManager;
import org.wso2.carbon.user.core.internal.UMListenerServiceComponent;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserStoreManagerListener;
import org.wso2.carbon.user.core.system.SystemRoleManager;
import org.wso2.carbon.user.core.system.SystemUserManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

public abstract class AbstractUserStoreManager implements UserStoreManager {

	private static Log log = LogFactory.getLog(AbstractUserStoreManager.class);

	protected int tenantId;
	protected DataSource dataSource = null;
	protected RealmConfiguration realmConfig = null;
	protected ClaimManager claimManager = null;
	protected UserRealm userRealm = null;
	protected HybridRoleManager hybridRoleManager = null;

	// User roles cache
	protected UserRolesCache userRolesCache = null;
	protected SystemUserManager systemUserManager = null;
	protected SystemRoleManager systemRoleManager = null;
	protected boolean readGroupsEnabled = false;
	protected boolean writeGroupsEnabled = false;
	private UserStoreManager secondaryUserStoreManager;
	private boolean userRolesCacheEnabled = true;
	private String cacheIdentifier;
	private boolean replaceEscapeCharactersAtUserLogin = true;
	private Map<String, UserStoreManager> userStoreManagerHolder = new HashMap<String, UserStoreManager>();
	private Map<String, Integer> maxUserListCount = null;
	private Map<String, Integer> maxRoleListCount = null;

	private static final String MAX_LIST_LENGTH = "100";

	/**
	 * This method is used by the support system to read properties
	 */
	protected abstract Map<String, String> getUserPropertyValues(String userName,
			String[] propertyNames, String profileName) throws UserStoreException;

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	protected abstract boolean doCheckExistingRole(String roleName) throws UserStoreException;

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract boolean doCheckExistingUser(String userName) throws UserStoreException;

	/**
	 * Retrieves a list of user names for given user's property in user profile
	 * 
	 * @param property user property in user profile
	 * @param value value of property
	 * @param profileName profile name, can be null. If null the default profile is considered.
	 * @return An array of user names
	 * @throws UserStoreException if the operation failed
	 */
	protected abstract String[] getUserListFromProperties(String property, String value,
			String profileName) throws UserStoreException;

	/**
	 * Given the user name and a credential object, the implementation code must validate whether
	 * the user is authenticated.
	 * 
	 * @param userName The user name
	 * @param credential The credential of a user
	 * @return If the value is true the provided credential match with the user name. False is
	 *         returned for invalid credential, invalid user name and mismatching credential with
	 *         user name.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract boolean doAuthenticate(String userName, Object credential)
			throws UserStoreException;

	/**
	 * Add a user to the user store.
	 * 
	 * @param userName User name of the user
	 * @param credential The credential/password of the user
	 * @param roleList The roles that user belongs
	 * @param claims Properties of the user
	 * @param profileName profile name, can be null. If null the default profile is considered.
	 * @param requirePasswordChange whether password required is need
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doAddUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
			throws UserStoreException;

	/**
	 * Update the credential/password of the user
	 * 
	 * @param userName The user name
	 * @param newCredential The new credential/password
	 * @param oldCredential The old credential/password
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateCredential(String userName, Object newCredential,
			Object oldCredential) throws UserStoreException;

	/**
	 * Update credential/password by the admin of another user
	 * 
	 * @param userName The user name
	 * @param newCredential The new credential
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateCredentialByAdmin(String userName, Object newCredential)
			throws UserStoreException;

	/**
	 * Delete the user with the given user name
	 * 
	 * @param userName The user name
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doDeleteUser(String userName) throws UserStoreException;

	/**
	 * Set a single user claim value
	 * 
	 * @param userName The user name
	 * @param claimURI The claim URI
	 * @param claimValue The value
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doSetUserClaimValue(String userName, String claimURI,
			String claimValue, String profileName) throws UserStoreException;

	/**
	 * Set many user claim values
	 * 
	 * @param userName The user name
	 * @param claims Map of claim URIs against values
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doSetUserClaimValues(String userName, Map<String, String> claims,
			String profileName) throws UserStoreException;

	/**
	 * o * Delete a single user claim value
	 * 
	 * @param userName The user name
	 * @param claimURI Name of the claim
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doDeleteUserClaimValue(String userName, String claimURI,
			String profileName) throws UserStoreException;

	/**
	 * Delete many user claim values.
	 * 
	 * @param userName The user name
	 * @param claims URIs of the claims to be deleted.
	 * @param profileName The profile name, can be null. If null the default profile is considered.
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doDeleteUserClaimValues(String userName, String[] claims,
			String profileName) throws UserStoreException;

	/**
	 * Update user list of a particular role
	 * 
	 * @param roleName The role name
	 * @param deletedUsers Array of user names, that is going to be removed from the role
	 * @param newUsers Array of user names, that is going to be added to the role
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateUserListOfRole(String roleName, String[] deletedUsers,
			String[] newUsers) throws UserStoreException;

	/**
	 * Update role list of a particular user
	 * 
	 * @param userName The user name
	 * @param deletedRoles Array of role names, that is going to be removed from the user
	 * @param newRoles Array of role names, that is going to be added to the user
	 * @throws UserStoreException An unexpected exception has occurred
	 */
	protected abstract void doUpdateRoleListOfUser(String userName, String[] deletedRoles,
			String[] newRoles) throws UserStoreException;

	/**
	 * Only gets the internal roles of the user with internal domain name
	 * 
	 * @param userName Name of the user - who we need to find roles.
	 * @return
	 * @throws UserStoreException
	 */
	protected String[] getInternalRoleListOfUser(String userName) throws UserStoreException {
		return hybridRoleManager.getHybridRoleListOfUser(userName);
	}

	/**
	 * Only gets the external roles of the user.
	 * 
	 * @param userName Name of the user - who we need to find roles.
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract String[] getExternalRoleListOfUser(String userName)
			throws UserStoreException;

	/**
	 * Add role with a list of users and permissions provided.
	 * 
	 * @param roleName
	 * @param userList
	 * @param permissions
	 * @throws UserStoreException
	 */
	protected abstract void doAddRole(String roleName, String[] userList,
			org.wso2.carbon.user.api.Permission[] permissions) throws UserStoreException;

	/**
	 * delete the role.
	 * 
	 * @param roleName
	 * @throws UserStoreException
	 */
	protected abstract void doDeleteRole(String roleName) throws UserStoreException;

	/**
	 * update the role name with the new name
	 * 
	 * @param roleName
	 * @param newRoleName
	 * @throws UserStoreException
	 */
	protected abstract void doUpdateRoleName(String roleName, String newRoleName)
			throws UserStoreException;

	/**
	 * This method would returns the role Name actually this must be implemented in interface. As it
	 * is not good to change the API in point release. This has been added to Abstract class
	 * 
	 * @param filter
	 * @param maxItemLimit
	 * @return
	 * @throws .UserStoreException
	 */
	protected abstract String[] doGetRoleNames(String filter, int maxItemLimit)
			throws UserStoreException;

	/**
	 * 
	 * @param filter
	 * @param maxItemLimit
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract String[] doListUsers(String filter, int maxItemLimit)
			throws UserStoreException;

    /*This is to get the display names of users in hybrid role according to the underlying user store, to be shown in UI*/
    protected abstract String[] doGetDisplayNamesForInternalRole(String[] userNames)
            throws UserStoreException;

	/**
	 * {@inheritDoc}
	 */
	public final boolean authenticate(String userName, Object credential) throws UserStoreException {
        if (userName == null || credential == null) {
            log.error("Authentication failure. Either Username or Password is null");
            return false;
        }
        int index = userName != null ? userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
		boolean domainProvided = index > 0;
		return authenticate(userName, credential, domainProvided);
	}

	/**
	 * 
	 * @param userName
	 * @param credential
	 * @param domainProvided
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean authenticate(String userName, Object credential, boolean domainProvided)
			throws UserStoreException {

		boolean authenticated = false;

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().authenticate(userStore.getDomainFreeName(),
					credential, domainProvided);
		}

		// #################### Domain Name Free Zone Starts Here ################################

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.authenticate(userName, credential, this)) {
				return true;
			}
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreAuthenticate(userName, credential, this)) {
				return false;
			}
		}
		// #################### </Listeners> #####################################################

		// We are here due to two reason. Either there is no secondary UserStoreManager or no
		// domain name provided with user name.
		
		try {
			// Let's authenticate with the primary UserStoreManager.
			authenticated = doAuthenticate(userName, credential);
		} catch (Exception e) {
			// We can ignore and proceed. Ignore the results from this user store.
			log.error(e);
			authenticated = false;
		}
		
		if (authenticated) {
			// Set domain in thread local variable for subsequent operations
            String domain = UserCoreUtil.getDomainName(this.realmConfig);
            if (domain != null) {
                UserCoreUtil.setDomainInThreadLocal(domain.toUpperCase());
            }
        }

		// If authentication fails in the previous step and if the user has not specified a
		// domain- then we need to execute chained UserStoreManagers recursively.
		if (!authenticated && !domainProvided && this.getSecondaryUserStoreManager() != null) {
			authenticated = ((AbstractUserStoreManager) this.getSecondaryUserStoreManager())
					.authenticate(userName, credential, domainProvided);
		}

		// You cannot change authentication decision in post handler to TRUE
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostAuthenticate(userName, authenticated, this)) {
				return false;
			}
		}

		if (log.isDebugEnabled()) {
			if (!authenticated) {
				log.debug("Authentication failure. Wrong username or password is provided.");
			}
		}

		return authenticated;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getUserClaimValue(String userName, String claim, String profileName)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserClaimValue(userStore.getDomainFreeName(),
					claim, profileName);
		}

		// #################### Domain Name Free Zone Starts Here ################################

		String value = doGetUserClaimValue(userName, claim, profileName);

		// #################### <Listeners> #####################################################
		List<String> list = new ArrayList<String>();
		if (value != null) {
			list.add(value);
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (listener instanceof AbstractUserOperationEventListener) {
				AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
				if (!newListener.doPostGetUserClaimValue(userName, claim, list, profileName, this)) {
					break;
				}
			}
		}
		// #################### </Listeners> #####################################################

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Claim[] getUserClaimValues(String userName, String profileName)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserClaimValues(
					userStore.getDomainFreeName(), profileName);
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (profileName == null) {
			profileName = UserCoreConstants.DEFAULT_PROFILE;
		}

		String[] claims;
		try {
			claims = claimManager.getAllClaimUris();
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		}

		Map<String, String> values = this.getUserClaimValues(userName, claims, profileName);
		Claim[] finalValues = new Claim[values.size()];
		int i = 0;
		for (Iterator<Map.Entry<String, String>> ite = values.entrySet().iterator(); ite.hasNext();) {
			Map.Entry<String, String> entry = ite.next();
			Claim claim = new Claim();
			claim.setValue(entry.getValue());
			claim.setClaimUri(entry.getKey());
			String displayTag;
			try {
				displayTag = claimManager.getClaim(entry.getKey()).getDisplayTag();
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}
			claim.setDisplayTag(displayTag);
			finalValues[i] = claim;
			i++;
		}

		return finalValues;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<String, String> getUserClaimValues(String userName, String[] claims,
			String profileName) throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserClaimValues(
					userStore.getDomainFreeName(), claims, profileName);
		}

		// #################### Domain Name Free Zone Starts Here ################################

		Map<String, String> finalValues = doGetUserClaimValues(userName, claims, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (listener instanceof AbstractUserOperationEventListener) {
				AbstractUserOperationEventListener newListener = (AbstractUserOperationEventListener) listener;
				if (!newListener.doPostGetUserClaimValues(userName, claims, profileName,
						finalValues, this)) {
					break;
				}
			}
		}
		// #################### </Listeners> #####################################################

		return finalValues;
	}

	/**
	 * {@inheritDoc} TODO : This does not support multiple user stores yet.
	 */
	public final String[] getUserList(String claim, String claimValue, String profileName)
			throws UserStoreException {

		String property;
		try {
			// Attributed id from corresponding to the underlying user store, corresponding to the
			// domain which this user store manager belongs to.
			property = claimManager.getAttributeName(claim);
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		}

		return getUserListFromProperties(property, claimValue, profileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateCredential(String userName, Object newCredential, Object oldCredential)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateCredential(userStore.getDomainFreeName(),
					newCredential, oldCredential);
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.updateCredential(userName, newCredential, oldCredential, this)) {
				return;
			}
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateCredential(userName, newCredential, oldCredential, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		// This user name here is domain-less.
		// We directly authenticate user against the selected UserStoreManager.
		boolean isAuth = this.doAuthenticate(userName, oldCredential);

		if (isAuth) {

			this.doUpdateCredential(userName, newCredential, oldCredential);

			// #################### <Listeners> ##################################################
			for (UserOperationEventListener listener : UMListenerServiceComponent
					.getUserOperationEventListeners()) {
				if (!listener.doPostUpdateCredential(userName, this)) {
					return;
				}
			}
			// #################### </Listeners> ##################################################

			return;
		} else {
			throw new UserStoreException(
					"Old credential does not match with the existing credentials.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateCredentialByAdmin(String userName, Object newCredential)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateCredentialByAdmin(userStore.getDomainFreeName(),
					newCredential);
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.updateCredentialByAdmin(userName, newCredential, this)) {
				return;
			}
		}
		// using string buffers to allow the password to be changed by listener
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if(newCredential == null) { // a default password will be set
				StringBuffer credBuff = new StringBuffer();
				if (!listener.doPreUpdateCredentialByAdmin(userName, newCredential, this)) {
					return;
				}
				newCredential = credBuff.toString(); // reading the modified value
			} else if (newCredential instanceof String) {
				StringBuffer credBuff = new StringBuffer((String)newCredential);
				if (!listener.doPreUpdateCredentialByAdmin(userName, newCredential, this)) {
					return;
				}
				newCredential = credBuff.toString(); // reading the modified value
			}
		}
		// #################### </Listeners> #####################################################

		doUpdateCredentialByAdmin(userName, newCredential);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateCredentialByAdmin(userName, newCredential, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void deleteUser(String userName) throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteUser(userStore.getDomainFreeName());
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (UserCoreUtil.isPrimaryAdminUser(userName, realmConfig)) {
			throw new UserStoreException("Cannot delete admin user");
		}

		if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
			throw new UserStoreException("Cannot delete anonymous user");
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.deleteUser(userName, this)) {
				return;
			}
		}

		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteUser(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if (!doCheckExistingUser(userName)) {
			throw new UserStoreException("Cannot delete user who is not exist");
		}

		// Remove users from internal role mapping
		hybridRoleManager.deleteUser(UserCoreUtil.addDomainToName(
                userName, realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME)));

		doDeleteUser(userName);

		// Needs to clear roles cache upon deletion of a user
		clearUserRolesCacheByTenant(this.tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteUser(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void setUserClaimValue(String userName, String claimURI, String claimValue,
			String profileName) throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().setUserClaimValue(userStore.getDomainFreeName(),
					claimURI, claimValue, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreSetUserClaimValue(userName, claimURI, claimValue, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doSetUserClaimValue(userName, claimURI, claimValue, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostSetUserClaimValue(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void setUserClaimValues(String userName, Map<String, String> claims,
			String profileName) throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().setUserClaimValues(userStore.getDomainFreeName(),
					claims, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreSetUserClaimValues(userName, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doSetUserClaimValues(userName, claims, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostSetUserClaimValues(userName, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void deleteUserClaimValue(String userName, String claimURI, String profileName)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteUserClaimValue(userStore.getDomainFreeName(),
					claimURI, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteUserClaimValue(userName, claimURI, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doDeleteUserClaimValue(userName, claimURI, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteUserClaimValue(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################
	}

	/**
	 * {@inheritDoc}
	 */
	public final void deleteUserClaimValues(String userName, String[] claims, String profileName)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteUserClaimValues(userStore.getDomainFreeName(),
					claims, profileName);
			return;
		}

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteUserClaimValues(userName, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		doDeleteUserClaimValues(userName, claims, profileName);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteUserClaimValues(userName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void addUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
			throws UserStoreException {

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().addUser(userStore.getDomainFreeName(), credential,
					roleList, claims, profileName, requirePasswordChange);
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (isReadOnly()) {
			throw new UserStoreException("Invalid operation. User store is read only");
		}

		// This happens only once during first startup - adding administrator user/role.
		if (userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
			userName = userStore.getDomainFreeName();
			roleList = UserCoreUtil.removeDomainFromNames(roleList);
		}

		// #################### <Listeners> #####################################################
		for (UserStoreManagerListener listener : UMListenerServiceComponent
				.getUserStoreManagerListeners()) {
			if (!listener.addUser(userName, credential, roleList, claims, profileName, this)) {
				return;
			}
		}
		// String buffers are used to let listeners to modify passwords
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if(credential == null) { // a default password will be set
				StringBuffer credBuff = new StringBuffer();
				if (!listener.doPreAddUser(userName, credBuff, roleList, claims, profileName,
				                           this)) {
					return;
				}
				credential = credBuff.toString(); // reading the modified value
			} else if (credential instanceof String) {
				StringBuffer credBuff = new StringBuffer((String)credential);
				if (!listener.doPreAddUser(userName, credBuff, roleList, claims, profileName,
				                           this)) {
					return;
				}
				credential = credBuff.toString(); // reading the modified value
			}
		}
		// #################### </Listeners> #####################################################

		if (!checkUserNameValid(userStore.getDomainFreeName())) {
			String message = "Username "+ userStore.getDomainFreeName() +" is not valid. User name must be a non null string with following format, ";
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
			throw new UserStoreException(message + regEx);
		}

		if (!checkUserPasswordValid(credential)) {
			String message = "Credential not valid. Credential must be a non null string with following format, ";
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
			throw new UserStoreException(message + regEx);
		}

		if (doCheckExistingUser(userStore.getDomainFreeName())) {
			throw new UserStoreException("Username '" + userName
					+ "' already exists in the system. Please pick another username.");
		}

		List<String> internalRoles = new ArrayList<String>();
		List<String> externalRoles = new ArrayList<String>();
		int index = 0;
		if (roleList != null) {
			for (String role : roleList) {
				index = role.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
				if (index > 0) {
					String domain = role.substring(0, index);
					if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
						internalRoles.add(UserCoreUtil.removeDomainFromName(role));
						continue;
					}
				}
				externalRoles.add(UserCoreUtil.removeDomainFromName(role));
			}
		}

		doAddUser(userName, credential, externalRoles.toArray(new String[externalRoles.size()]),
				claims, profileName, requirePasswordChange);

		if (internalRoles.size() > 0) {
			hybridRoleManager.updateHybridRoleListOfUser(userName, null,
					internalRoles.toArray(new String[internalRoles.size()]));
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostAddUser(userName, credential, roleList, claims, profileName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################
	}

	/**
	 * {@inheritDoc}
	 */
	public void addUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName) throws UserStoreException {
		this.addUser(userName, credential, roleList, claims, profileName, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
			throws UserStoreException {

		String primaryDomain = getMyDomainName();
		if (primaryDomain != null) {
			primaryDomain += CarbonConstants.DOMAIN_SEPARATOR;
		}

		if (deletedUsers != null && deletedUsers.length > 0) {
			Arrays.sort(deletedUsers);
			// Updating the user list of a role belong to the primary domain.
			if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)) {
				for (int i = 0; i < deletedUsers.length; i++) {
					if (deletedUsers[i].equalsIgnoreCase(realmConfig.getAdminUserName())
							|| (primaryDomain + deletedUsers[i]).equalsIgnoreCase(realmConfig
									.getAdminUserName())) {
						throw new UserStoreException("Cannot remove Admin user from Admin role");
					}

				}
			}
		}

		UserStore userStore = getUserStore(roleName);

        if (userStore.isHybridRole()) {
			// Check whether someone is trying to update Everyone role.
			if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
				throw new UserStoreException("Cannot update everyone role");
			}

            hybridRoleManager.updateUserListOfHybridRole(userStore.getDomainFreeName(),
                                                         deletedUsers, newUsers);
            clearUserRolesCacheByTenant(this.tenantId);
			return;
		}

		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateUserListOfRole(userStore.getDomainFreeName(),
					UserCoreUtil.removeDomainFromNames(deletedUsers),
					UserCoreUtil.removeDomainFromNames(newUsers));
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateUserListOfRole(roleName, deletedUsers, newUsers, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if ((deletedUsers != null && deletedUsers.length > 0)
				|| (newUsers != null && newUsers.length > 0)) {
			if (!isReadOnly() && writeGroupsEnabled) {
				doUpdateUserListOfRole(userStore.getDomainFreeName(),
						UserCoreUtil.removeDomainFromNames(deletedUsers),
						UserCoreUtil.removeDomainFromNames(newUsers));
			} else {
				throw new UserStoreException(
						"Read-only user store.Roles cannot be added or modfified");
			}
		}

		// need to clear user roles cache upon roles update
		clearUserRolesCacheByTenant(this.tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
			throws UserStoreException {

		String primaryDomain = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
		if (primaryDomain != null) {
			primaryDomain += CarbonConstants.DOMAIN_SEPARATOR;
		}

		if (deletedRoles != null && deletedRoles.length > 0) {
			Arrays.sort(deletedRoles);
			if (UserCoreUtil.isPrimaryAdminUser(userName, realmConfig)) {
				for (int i = 0; i < deletedRoles.length; i++) {
					if (deletedRoles[i].equalsIgnoreCase(realmConfig.getAdminRoleName())
							|| (primaryDomain + deletedRoles[i]).equalsIgnoreCase(realmConfig
									.getAdminRoleName())) {
						throw new UserStoreException("Cannot remove Admin user from Admin role");
					}
				}
			}
		}

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateRoleListOfUser(userStore.getDomainFreeName(),
					UserCoreUtil.removeDomainFromNames(deletedRoles),
					UserCoreUtil.removeDomainFromNames(newRoles));
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		// This happens only once during first startup - adding administrator user/role.
		if (userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
			userName = userStore.getDomainFreeName();
			deletedRoles = UserCoreUtil.removeDomainFromNames(deletedRoles);
			newRoles = UserCoreUtil.removeDomainFromNames(newRoles);
		}

		List<String> internalRoleDel = new ArrayList<String>();
		List<String> internalRoleNew = new ArrayList<String>();

		List<String> roleDel = new ArrayList<String>();
		List<String> roleNew = new ArrayList<String>();

		if (deletedRoles != null && deletedRoles.length > 0) {
			for (String deleteRole : deletedRoles) {
                if (UserCoreUtil.isEveryoneRole(deleteRole, realmConfig)) {
					throw new UserStoreException("Everyone role cannot be updated");
				}
				String domain = null;
				int index1 = deleteRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
				if (index1 > 0) {
					domain = deleteRole.substring(0, index1);
				}
				if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || this.isReadOnly()) {
					internalRoleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
				} else {
					// This is domain free role name.
					roleDel.add(UserCoreUtil.removeDomainFromName(deleteRole));
				}
			}
			deletedRoles = roleDel.toArray(new String[roleDel.size()]);
		}

		if (newRoles != null && newRoles.length > 0) {
			for (String newRole : newRoles) {
				if (UserCoreUtil.isEveryoneRole(newRole, realmConfig)) {
					throw new UserStoreException("Everyone role cannot be updated");
				}
				String domain = null;
				int index2 = newRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
				if (index2 > 0) {
					domain = newRole.substring(0, index2);
				}
				if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || this.isReadOnly()) {
					internalRoleNew.add(UserCoreUtil.removeDomainFromName(newRole));
				} else {
					roleNew.add(UserCoreUtil.removeDomainFromName(newRole));
				}
			}
			newRoles = roleNew.toArray(new String[roleNew.size()]);
		}

		if (internalRoleDel.size() > 0 || internalRoleNew.size() > 0) {
			hybridRoleManager.updateHybridRoleListOfUser(userStore.getDomainFreeName(),
					internalRoleDel.toArray(new String[internalRoleDel.size()]),
					internalRoleNew.toArray(new String[internalRoleNew.size()]));
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateRoleListOfUser(userName, deletedRoles, newRoles, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if ((deletedRoles != null && deletedRoles.length > 0)
				|| (newRoles != null && newRoles.length > 0)) {
			if (!isReadOnly() && writeGroupsEnabled) {
				doUpdateRoleListOfUser(userName, deletedRoles, newRoles);
			} else {
				throw new UserStoreException("Read-only user store. Cannot add/modify roles.");
			}
		}

		clearUserRolesCacheByTenant(this.tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateRoleListOfUser(userName, deletedRoles, newRoles, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateRoleName(String roleName, String newRoleName) throws UserStoreException {

		if (UserCoreUtil.isPrimaryAdminRole(newRoleName, realmConfig)) {
			throw new UserStoreException("Cannot rename admin role");
		}

		if (UserCoreUtil.isEveryoneRole(newRoleName, realmConfig)) {
			throw new UserStoreException("Cannot rename everyone role");
		}

		UserStore userStore = getUserStore(roleName);
		UserStore userStoreNew = getUserStore(newRoleName);

		if (!UserCoreUtil.canRoleBeRenamed(userStore, userStoreNew, realmConfig)) {
			throw new UserStoreException("The role cannot be renamed");
		}

		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().updateRoleName(userStore.getDomainFreeName(),
					userStoreNew.getDomainFreeName());
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (userStore.isHybridRole()) {
			hybridRoleManager.updateHybridRoleName(userStore.getDomainFreeName(),
					userStoreNew.getDomainFreeName());

			// This is a special case. We need to pass roles with domains.
			userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
					userStore.getDomainAwareName(), userStoreNew.getDomainAwareName());

			// Need to update user role cache upon update of role names
			clearUserRolesCacheByTenant(this.tenantId);
			return;
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreUpdateRoleName(roleName, newRoleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if (isExistingRole(newRoleName)) {
			throw new UserStoreException("Role name: " + newRoleName
					+ " in the system. Please pick another role name.");
		}

		if (!isReadOnly() && writeGroupsEnabled) {
			doUpdateRoleName(userStore.getDomainFreeName(), userStoreNew.getDomainFreeName());
		} else {
			throw new UserStoreException(
					"Read-only UserStoreManager. Roles cannot be added or modified.");
		}

		clearUserRolesCacheByTenant(tenantId);

		// This is a special case. We need to pass domain aware name.
		userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(
				userStore.getDomainAwareName(), userStoreNew.getDomainAwareName());

		// need to update user role cache upon update of role names
		clearUserRolesCacheByTenant(this.tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostUpdateRoleName(roleName, newRoleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isExistingRole(String roleName) throws UserStoreException {

		UserStore userStore = getUserStore(roleName);

		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().isExistingRole(userStore.getDomainFreeName());
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (userStore.isHybridRole()) {
			return hybridRoleManager.isExistingRole(userStore.getDomainFreeName());
		}

		// This happens only once during first startup - adding administrator user/role.
		roleName = userStore.getDomainFreeName();

		boolean isExisting = doCheckExistingRole(roleName);

		if (!isExisting && (isReadOnly() || !readGroupsEnabled)) {
			isExisting = hybridRoleManager.isExistingRole(roleName);
		}

		if (!isExisting) {
			if (systemRoleManager.isExistingRole(roleName)) {
				isExisting = true;
			}
		}

		return isExisting;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExistingUser(String userName) throws UserStoreException {

		if (UserCoreUtil.isRegistrySystemUser(userName)) {
			return true;
		}

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().isExistingUser(userStore.getDomainFreeName());
		}

		// #################### Domain Name Free Zone Starts Here ################################

		boolean isExist = doCheckExistingUser(userStore.getDomainFreeName());

		if (!isExist) {
			if (realmConfig.isPrimary()) {
				isExist = systemUserManager.isExistingSystemUser(userStore.getDomainFreeName());
			}
		}

		return isExist;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {

		int index;
		index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);

		// Check whether we have a secondary UserStoreManager setup.
		if (index > 0) {
			// Using the short-circuit. User name comes with the domain name.
			String domain = filter.substring(0, index);

			UserStoreManager secManager = getSecondaryUserStoreManager(domain);
			if (secManager != null) {
				// We have a secondary UserStoreManager registered for this domain.
				filter = filter.substring(index + 1);
				if (secManager instanceof AbstractUserStoreManager) {
					return ((AbstractUserStoreManager) secManager)
							.doListUsers(filter, maxItemLimit);
				}
			} else {
				// Exception is not need to as listing of users
				// throw new UserStoreException("Invalid Domain Name");
			}
		} else if (index == 0) {
			return doListUsers(filter.substring(index + 1), maxItemLimit);
		}

		String[] userList = doListUsers(filter, maxItemLimit);

		String primaryDomain = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

		if (this.getSecondaryUserStoreManager() != null) {
			for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
					continue;
				}
				UserStoreManager storeManager = entry.getValue();
				if (storeManager instanceof AbstractUserStoreManager) {
					try {
						String[] secondUserList = ((AbstractUserStoreManager) storeManager)
								.doListUsers(filter, maxItemLimit);
						userList = UserCoreUtil.combineArrays(userList, secondUserList);
					} catch (UserStoreException ex) {
						// We can ignore and proceed. Ignore the results from this user store.
						log.error(ex);
					}
				}
			}
		}

		return userList;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getUserListOfRole(String roleName) throws UserStoreException {

		String[] userNames = new String[0];

		// If role does not exit, just return
		if (!isExistingRole(roleName)) {
			return userNames;
		}

		UserStore userStore = getUserStore(roleName);

		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getUserListOfRole(userStore.getDomainFreeName());
		}

		// #################### Domain Name Free Zone Starts Here ################################
        String[] userNamesInHybrid = new String[0]; 
		if (userStore.isHybridRole()) {
            userNamesInHybrid = hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
            //remove domain
            List<String> domainLessNames = new ArrayList<String>();
            List<String> finalNameList = new ArrayList<String>();
            for (String userName : userNamesInHybrid) {
                String domainName = UserCoreUtil.extractDomainFromName(userName);
                UserStoreManager userManager = userStoreManagerHolder.get(domainName);
                userName = UserCoreUtil.removeDomainFromName(userName);
                //get displayNames
                String[] displayNames = ((AbstractUserStoreManager) userManager).doGetDisplayNamesForInternalRole(
                        new String[]{userName});
                for (String displayName : displayNames) {
                    //if domain names are not added by above method, add it here
                    String nameWithDomain = UserCoreUtil.addDomainToName(displayName, domainName);
                    finalNameList.add(nameWithDomain);
                }
            }
            return finalNameList.toArray(new String[finalNameList.size()]);
			//return hybridRoleManager.getUserListOfHybridRole(userStore.getDomainFreeName());
		}

		if (readGroupsEnabled) {
			userNames = doGetUserListOfRole(roleName, "*");
		}

		return userNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getRoleListOfUser(String userName) throws UserStoreException {

		String[] roleNames = null;

		// If user does not exist, just return
		if (!isExistingUser(userName)) {
			return new String[0];
		}

		// Check whether roles exist in cache
		try {
			roleNames = getRoleListOfUserFromCache(this.tenantId, userName);
			if (roleNames != null) {
				return roleNames;
			}
		} catch (Exception e) {
			// If not exist in cache, continue
		}

		UserStore userStore = getUserStore(userName);
		if (userStore.isRecurssive()) {
			return userStore.getUserStoreManager().getRoleListOfUser(userStore.getDomainFreeName());
		}

		// #################### Domain Name Free Zone Starts Here ################################

		roleNames = doGetRoleListOfUser(userName, "*");

		addToUserRolesCache(this.tenantId,
				UserCoreUtil.addDomainToName(userName, getMyDomainName()), roleNames);

		return roleNames;
	}

	/**
	 * Getter method for claim manager property specifically to be used in the implementations of
	 * UserOperationEventListener implementations
	 * 
	 * @return
	 */
	public ClaimManager getClaimManager() {
		return claimManager;
	}

	/**
	 * 
	 */
	public final void addRole(String roleName, String[] userList,
			org.wso2.carbon.user.api.Permission[] permissions)
			throws org.wso2.carbon.user.api.UserStoreException {

		UserStore userStore = getUserStore(roleName);

        if (userStore.isHybridRole()) {
			doAddInternalRole(userStore.getDomainFreeName(), userList, (Permission[]) permissions);
			return;
		}
        
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().addRole(userStore.getDomainFreeName(),
					UserCoreUtil.removeDomainFromNames(userList), permissions);
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		// This happens only once during first startup - adding administrator user/role.
		if (roleName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
			roleName = userStore.getDomainFreeName();
			userList = UserCoreUtil.removeDomainFromNames(userList);
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreAddRole(roleName, userList, permissions, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		// Check for validations
		if (isReadOnly()) {
			throw new UserStoreException(
					"Cannot add role to Read Only user store unless it is primary");
		}

		if (!isRoleNameValid(roleName)) {
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
			throw new UserStoreException(
					"Role name not valid. Role name must be a non null string with following format, "
							+ regEx);
		}

		if (doCheckExistingRole(roleName)) {
			throw new UserStoreException("Role name: " + roleName
					+ " in the system. Please pick another role name.");
		}

		String roleWithDomain = null;
		if (!isReadOnly() && writeGroupsEnabled) {
			// add role in to actual user store
			doAddRole(roleName, userList, permissions);
			roleWithDomain = UserCoreUtil.addDomainToName(roleName, getMyDomainName());
		} else {
			throw new UserStoreException(
					"Role cannot be added. User store is read only or cannot write groups.");
		}

		// add permission in to the the permission store
		if (permissions != null) {
			for (org.wso2.carbon.user.api.Permission permission : permissions) {
				String resourceId = permission.getResourceId();
				String action = permission.getAction();
				// This is a special case. We need to pass domain aware name.
				userRealm.getAuthorizationManager().authorizeRole(roleWithDomain, resourceId,
						action);
			}
		}

		// if existing users are added to role, need to update user role cache
		if ((userList != null) && (userList.length > 0)) {
			clearUserRolesCacheByTenant(tenantId);
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostAddRole(roleName, userList, permissions, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * Delete the role with the given role name
	 * 
	 * @param roleName The role name
	 * @throws org.wso2.carbon.user.core.UserStoreException
	 * 
	 */
	public final void deleteRole(String roleName) throws UserStoreException {

		if (UserCoreUtil.isPrimaryAdminRole(roleName, realmConfig)) {
			throw new UserStoreException("Cannot delete admin role");
		}
		if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
			throw new UserStoreException("Cannot delete everyone role");
		}

		UserStore userStore = getUserStore(roleName);
		if (userStore.isRecurssive()) {
			userStore.getUserStoreManager().deleteRole(userStore.getDomainFreeName());
			return;
		}

		// #################### Domain Name Free Zone Starts Here ################################

		if (userStore.isHybridRole()) {
			hybridRoleManager.deleteHybridRole(userStore.getDomainFreeName());
            clearUserRolesCacheByTenant(tenantId);
			return;
		}

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPreDeleteRole(roleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

		if (!doCheckExistingRole(roleName)) {
			throw new UserStoreException("Can not delete non exiting role");
		}

		if (!isReadOnly() && writeGroupsEnabled) {
			doDeleteRole(roleName);
		} else {
			throw new UserStoreException(
					"Role cannot be deleted. User store is read only or cannot write groups.");
		}

		clearUserRolesCacheByTenant(tenantId);

		// #################### <Listeners> #####################################################
		for (UserOperationEventListener listener : UMListenerServiceComponent
				.getUserOperationEventListeners()) {
			if (!listener.doPostDeleteRole(roleName, this)) {
				return;
			}
		}
		// #################### </Listeners> #####################################################

	}

	/**
	 * 
	 * @param input
	 * @return
	 * @throws UserStoreException
	 */
	private UserStore getUserStore(String user) throws UserStoreException {

		int index;
		index = user.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
		UserStore userStore = new UserStore();
		String domainFreeName = null;

		// Check whether we have a secondary UserStoreManager setup.
		if (index > 0) {
			// Using the short-circuit. User name comes with the domain name.
			String domain = user.substring(0, index);
			UserStoreManager secManager = getSecondaryUserStoreManager(domain);
			domainFreeName = user.substring(index + 1);

			if (secManager != null) {
				userStore.setUserStoreManager((AbstractUserStoreManager) secManager);
				userStore.setDomainAwareName(user);
				userStore.setDomainFreeName(domainFreeName);
				userStore.setDomainName(domain);
				userStore.setRecurssive(true);
				return userStore;
			} else {
				if (!domain.equalsIgnoreCase(getMyDomainName())) {
					if ((UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain))) {
						userStore.setHybridRole(true);
					} else {
						throw new UserStoreException("Invalid Domain Name");
					}
				}
			}
		}

		String domain = UserCoreUtil.getDomainName(realmConfig);
		userStore.setUserStoreManager(this);
		if (index > 0) {
			userStore.setDomainAwareName(user);
			userStore.setDomainFreeName(domainFreeName);
		} else {
			userStore.setDomainAwareName(domain + CarbonConstants.DOMAIN_SEPARATOR + user);
			userStore.setDomainFreeName(user);
		}
		userStore.setRecurssive(false);
		userStore.setDomainName(domain);
        if (this.isReadOnly()) {
            userStore.setHybridRole(true);
        }

        return userStore;
	}

	/**
	 * {@inheritDoc}
	 */
	public final UserStoreManager getSecondaryUserStoreManager() {
		return secondaryUserStoreManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public final UserStoreManager getSecondaryUserStoreManager(String userDomain) {
		if (userDomain == null) {
			return null;
		}
		return userStoreManagerHolder.get(userDomain.toUpperCase());
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setSecondaryUserStoreManager(UserStoreManager secondaryUserStoreManager) {
		this.secondaryUserStoreManager = secondaryUserStoreManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addSecondaryUserStoreManager(String userDomain,
			UserStoreManager userStoreManager) {
		if (userDomain != null) {
			userStoreManagerHolder.put(userDomain.toUpperCase(), userStoreManager);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getAllSecondaryRoles() throws UserStoreException {
		UserStoreManager secondary = this.getSecondaryUserStoreManager();
		List<String> roleList = new ArrayList<String>();
		while (secondary != null) {
			String[] roles = secondary.getRoleNames(true);
			if (roles != null && roles.length > 0) {
				Collections.addAll(roleList, roles);
			}
			secondary = secondary.getSecondaryUserStoreManager();
		}
		return roleList.toArray(new String[roleList.size()]);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSCIMEnabled() {
		String scimEnabled = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_SCIM_ENABLED);
		if (scimEnabled != null) {
			return Boolean.parseBoolean(scimEnabled);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getHybridRoles() throws UserStoreException {
		return hybridRoleManager.getHybridRoles("*");
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getRoleNames() throws UserStoreException {
		return getRoleNames(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getRoleNames(boolean noHybridRoles) throws UserStoreException {
		return getRoleNames("*", -1, noHybridRoles);
	}

	/**
	 * 
	 * @param roleName
	 * @param userList
	 * @param permissions
	 * @throws UserStoreException
	 */
	protected void doAddInternalRole(String roleName, String[] userList, Permission[] permissions)
			throws UserStoreException {

		// #################### Domain Name Free Zone Starts Here ################################

		if (hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(roleName))) {
			throw new UserStoreException("Role name: " + roleName
					+ " in the system. Please pick another role name.");
		}

		hybridRoleManager.addHybridRole(UserCoreUtil.removeDomainFromName(roleName), userList);

		if (permissions != null) {
			for (Permission permission : permissions) {
				String resourceId = permission.getResourceId();
				String action = permission.getAction();
				// This is a special case. We need to pass domain aware name.
				userRealm.getAuthorizationManager().authorizeRole(
						UserCoreUtil.addInternalDomainName(roleName), resourceId, action);
			}
		}

		if ((userList != null) && (userList.length > 0)) {
			clearUserRolesCacheByTenant(this.tenantId);
		}
	}

	/**
	 * TODO This method would returns the role Name actually this must be implemented in interface.
	 * As it is not good to change the API in point release. This has been added to Abstract class
	 * 
	 * @param filter
	 * @param maxItemLimit
	 * @param noHybridRoles
	 * @return
	 * @throws UserStoreException
	 */
	public final String[] getRoleNames(String filter, int maxItemLimit, boolean noHybridRoles)
			throws UserStoreException {

		String[] roleList = new String[0];

		if (!noHybridRoles) {
			roleList = hybridRoleManager.getHybridRoles(UserCoreUtil.removeDomainFromName(filter));
		}

		int index;
		index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);

		// Check whether we have a secondary UserStoreManager setup.
		if (index > 0) {
			// Using the short-circuit. User name comes with the domain name.
			String domain = filter.substring(0, index);

			UserStoreManager secManager = getSecondaryUserStoreManager(domain);
			if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)) {
				return new String[0];
			}
			if (secManager != null) {
				// We have a secondary UserStoreManager registered for this domain.
				filter = filter.substring(index + 1);
				if (secManager instanceof AbstractUserStoreManager) {
					String[] externalRoles = ((AbstractUserStoreManager) secManager)
							.doGetRoleNames(filter, maxItemLimit);
					return UserCoreUtil.combineArrays(roleList, externalRoles);
				}
			} else {
				throw new UserStoreException("Invalid Domain Name");
			}
		} else if (index == 0) {
			String[] externalRoles = doGetRoleNames(filter.substring(index + 1), maxItemLimit);
			return UserCoreUtil.combineArrays(roleList, externalRoles);
		}

		String[] externalRoles = doGetRoleNames(filter, maxItemLimit);
		roleList = UserCoreUtil.combineArrays(externalRoles, roleList);

		String primaryDomain = getMyDomainName();

		if (this.getSecondaryUserStoreManager() != null) {
			for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(primaryDomain)) {
					continue;
				}
				UserStoreManager storeManager = entry.getValue();
				if (storeManager instanceof AbstractUserStoreManager) {
					try {
						String[] secondRoleList = ((AbstractUserStoreManager) storeManager)
								.doGetRoleNames(filter, maxItemLimit);
						roleList = UserCoreUtil.combineArrays(roleList, secondRoleList);
					} catch (UserStoreException e) {
						// We can ignore and proceed. Ignore the results from this user store.
						log.error(e);
					}
				}
			}
		}
		return roleList;
	}

	/**
	 * 
	 * 
	 * @param userName
	 * @param claim
	 * @param profileName
	 * @return
	 * @throws UserStoreException
	 */
	private String doGetUserClaimValue(String userName, String claim, String profileName)
			throws UserStoreException {

		// User-name here is domain-less.

		String property = null;
		try {
			// This will return the attribute name corresponding to the given claim, corresponding
			// to the domain this user store manager belongs to.
			property = claimManager.getAttributeName(claim);
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			throw new UserStoreException(e);
		}

		// Get the value of the claim from the provided profile.
		String value = this.getUserPropertyValues(userName, new String[] { property }, profileName)
				.get(property);

		return value;
	}

	/**
	 * 
	 * @param userName
	 * @param claims
	 * @param domainName
	 * @return
	 * @throws UserStoreException
	 */
	private Map<String, String> doGetUserClaimValues(String userName, String[] claims,
			String domainName) throws UserStoreException {

		// Here the user name should be domain-less.

		boolean requireRoles = false;
		boolean requireIntRoles = false;
		boolean requireExtRoles = false;
		String roleClaim = null;

		Set<String> propertySet = new HashSet<String>();
		for (String claim : claims) {
			ClaimMapping mapping = null;
			try {
				mapping = (ClaimMapping) claimManager.getClaimMapping(claim);
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}

			// There can be cases some claim values being requested for claims
			// we don't have.
			if (mapping != null
					&& (!UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)
							|| !UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim) || !UserCoreConstants.EXT_ROLE_CLAIM
								.equalsIgnoreCase(claim))) {
				if (domainName != null) {
					Map<String, String> attrMap = mapping.getMappedAttributes();
					if (attrMap != null) {
						String attr = null;
						if ((attr = attrMap.get(domainName.toUpperCase())) != null) {
							propertySet.add(attr);
						} else {
							propertySet.add(mapping.getMappedAttribute());
						}
					}
				} else {
					propertySet.add(mapping.getMappedAttribute());
				}
			}

			if (UserCoreConstants.ROLE_CLAIM.equalsIgnoreCase(claim)) {
				requireRoles = true;
				roleClaim = claim;
			} else if (UserCoreConstants.INT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
				requireIntRoles = true;
				roleClaim = claim;
			} else if (UserCoreConstants.EXT_ROLE_CLAIM.equalsIgnoreCase(claim)) {
				requireExtRoles = true;
				roleClaim = claim;
			}
		}

		String[] properties = propertySet.toArray(new String[propertySet.size()]);
		Map<String, String> uerProperties = this.getUserPropertyValues(userName, properties,
				domainName);

		List<String> getAgain = new ArrayList<String>();
		Map<String, String> finalValues = new HashMap<String, String>();

		String profileName = UserCoreConstants.DEFAULT_PROFILE;

		for (String claim : claims) {
			ClaimMapping mapping;
			try {
				mapping = (ClaimMapping) claimManager.getClaimMapping(claim);
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}
			if (mapping != null) {
				String property = null;

				if (domainName != null) {
					Map<String, String> attrMap = mapping.getMappedAttributes();
					if (attrMap != null) {
						String attr = null;
						if ((attr = attrMap.get(domainName.toUpperCase())) != null) {
							property = attr;
						} else {
							property = mapping.getMappedAttribute();
						}
					}
				} else {
					property = mapping.getMappedAttribute();
				}

				String value = uerProperties.get(property);

				if (profileName.equals(UserCoreConstants.DEFAULT_PROFILE)) {
					// Check whether we have a value for the requested attribute
					if (value != null && value.trim().length() > 0) {
						finalValues.put(claim, value);
					}
				} else {
					if (value != null && value.trim().length() > 0) {
						finalValues.put(claim, value);
					}
				}
			}
		}

		if (getAgain.size() > 0) {
			// oh the beautiful recursion
			Map<String, String> mapClaimValues = this.getUserClaimValues(userName,
					(String[]) getAgain.toArray(new String[getAgain.size()]),
					UserCoreConstants.DEFAULT_PROFILE);

			Iterator<Map.Entry<String, String>> ite3 = mapClaimValues.entrySet().iterator();
			while (ite3.hasNext()) {
				Map.Entry<String, String> entry = ite3.next();
				if (entry.getValue() != null) {
					finalValues.put(entry.getKey(), entry.getValue());
				}
			}
		}

		// We treat roles claim in special way.
		String[] roles = null;

		if (requireRoles) {
			roles = getRoleListOfUser(userName);
		} else if (requireIntRoles) {
			roles = getInternalRoleListOfUser(userName);
		} else if (requireExtRoles) {
			roles = getExternalRoleListOfUser(userName);
		}

		if (roles != null && roles.length > 0) {
			String delim = "";
			StringBuffer roleBf = new StringBuffer();
			for (String role : roles) {
				roleBf.append(delim).append(role);
				delim = ",";
			}
			finalValues.put(roleClaim, roleBf.toString());
		}

		return finalValues;
	}

	/**
	 * 
	 * @return
	 */
	protected String getEveryOneRoleName() {
		return realmConfig.getEveryOneRoleName();
	}

	/**
	 * 
	 * @return
	 */
	protected String getAdminRoleName() {
		return realmConfig.getAdminRoleName();
	}

	/**
	 * 
	 * @param credential
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean checkUserPasswordValid(Object credential) throws UserStoreException {

		if (credential == null) {
			return false;
		}

		if (!(credential instanceof String)) {
			throw new UserStoreException("Can handle only string type credentials");
		}

		String password = ((String) credential).trim();

		if (password.length() < 1) {
			return false;
		}

		String regularExpression = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
		return regularExpression == null || isFormatCorrect(regularExpression, password);
	}

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean checkUserNameValid(String userName) throws UserStoreException {

		if (userName == null || CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
			return false;
		}

		userName = userName.trim();

		if (userName.length() < 1) {
			return false;
		}

        String regularExpression = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
        if (regularExpression != null){
             regularExpression = regularExpression.trim();
        }

		return regularExpression == null || regularExpression.equals("")
				|| isFormatCorrect(regularExpression, userName);

	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	protected boolean isRoleNameValid(String roleName) {
		if (roleName == null) {
			return false;
		}

		if (roleName.length() < 1) {
			return false;
		}

		String regularExpression = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
		if (regularExpression != null) {
			if (!isFormatCorrect(regularExpression, roleName)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 
	 * @param tenantID
	 * @param userName
	 * @return
	 */
	protected String[] getRoleListOfUserFromCache(int tenantID, String userName) {
		if (userRolesCache != null) {
			return userRolesCache.getRolesListOfUser(cacheIdentifier, tenantID, userName);
		}
		return null;
	}

	/**
	 * 
	 * @param tenantID
	 */
	protected void clearUserRolesCacheByTenant(int tenantID) {
		if (userRolesCache != null) {
			userRolesCache.clearCacheByTenant(tenantID);
		}
		AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
		authorizationCache.clearCacheByTenant(tenantID);
	}

	/**
	 * 
	 * @param tenantID
	 * @param userName
	 * @param roleList
	 */
	protected void addToUserRolesCache(int tenantID, String userName, String[] roleList) {
		if (userRolesCache != null) {
			userRolesCache.addToCache(cacheIdentifier, tenantID, userName, roleList);
			AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
			authorizationCache.clearCacheByTenant(tenantID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initUserRolesCache() {

		String userRolesCacheEnabledString = (realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED));

		String userCoreCacheIdentifier = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER);

		if (userCoreCacheIdentifier != null && userCoreCacheIdentifier.trim().length() > 0) {
			cacheIdentifier = userCoreCacheIdentifier;
		}

		if (userRolesCacheEnabledString != null && !userRolesCacheEnabledString.equals("")) {
			userRolesCacheEnabled = Boolean.parseBoolean(userRolesCacheEnabledString);
			if (log.isDebugEnabled()) {
				log.debug("User Roles Cache is configured to:" + userRolesCacheEnabledString);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.info("User Roles Cache is not configured. Default value: "
						+ userRolesCacheEnabled + " is taken.");
			}
		}

		if (userRolesCacheEnabled) {
			userRolesCache = UserRolesCache.getInstance();
		}

	}

	/**
	 * 
	 * @param regularExpression
	 * @param attribute
	 * @return
	 */
	private boolean isFormatCorrect(String regularExpression, String attribute) {
		Pattern p2 = Pattern.compile(regularExpression);
		Matcher m2 = p2.matcher(attribute);
		return m2.matches();
	}

	/**
	 * This is to replace escape characters in user name at user login if replace escape characters
	 * enabled in user-mgt.xml. Some User Stores like ApacheDS stores user names by replacing escape
	 * characters. In that case, we have to parse the username accordingly.
	 * 
	 * @param userName
	 */
	protected String replaceEscapeCharacters(String userName) {
		String replaceEscapeCharactersAtUserLoginString = realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_REPLACE_ESCAPE_CHARACTERS_AT_USER_LOGIN);

		if (replaceEscapeCharactersAtUserLoginString != null) {
			replaceEscapeCharactersAtUserLogin = Boolean
					.parseBoolean(replaceEscapeCharactersAtUserLoginString);
			if (log.isDebugEnabled()) {
				log.debug("Replace escape characters at userlogin is condifured to: "
						+ replaceEscapeCharactersAtUserLoginString);
			}
			if (replaceEscapeCharactersAtUserLogin) {
				// Currently only '\' & '\\' are identified as escape characters that needs to be
				// replaced.
				return userName.replaceAll("\\\\", "\\\\\\\\");
			}
		}
		return userName;
	}

	/**
	 * TODO: Remove this method. We should not use DTOs
	 * 
	 * @return
	 * @throws UserStoreException
	 */
	public RoleDTO[] getAllSecondaryRoleDTOs() throws UserStoreException {
		UserStoreManager secondary = this.getSecondaryUserStoreManager();
		List<RoleDTO> roleList = new ArrayList<RoleDTO>();
		while (secondary != null) {
			String domain = secondary.getRealmConfiguration().getUserStoreProperty(
					UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
			String[] roles = secondary.getRoleNames(true);
			if (roles != null && roles.length > 0) {
				Collections.addAll(roleList, UserCoreUtil.convertRoleNamesToRoleDTO(roles, domain));
			}
			secondary = secondary.getSecondaryUserStoreManager();
		}
		return roleList.toArray(new RoleDTO[roleList.size()]);
	}

	/**
	 * 
	 * @param userName
	 * @param credential
	 * @param roleList
	 * @param claims
	 * @param profileName
	 * @param requirePasswordChange
	 * @throws UserStoreException
	 */
	public final void addSystemUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
			throws UserStoreException {

		/* We don't need this at all. 
		 * if (!checkUserNameValid(userName)) {
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JAVA_REG_EX);
			throw new UserStoreException(
					"Username not valid. User name must be a non null string with following format, "
							+ regEx);

		}

		if (!checkUserPasswordValid(credential)) {
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX);
			throw new UserStoreException(
					"Credential not valid. Credential must be a non null string with following format, "
							+ regEx);

		}*/

		boolean isExisting = systemUserManager.isExistingSystemUser(userName);

		if (isExisting) {
			throw new UserStoreException("Username '" + userName
					+ "' already exists in the system. Please pick another username.");
		}

		systemUserManager.addSystemUser(userName, credential, roleList, claims, profileName,
				requirePasswordChange);
	}

	/**
	 * 
	 * @param roleName
	 * @param userList
	 * @param permissions
	 * @throws UserStoreException
	 */
	public void addSystemRole(String roleName, String[] userList, Permission[] permissions)
			throws UserStoreException {

		if (!isRoleNameValid(roleName)) {
			String regEx = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JAVA_REG_EX);
			throw new UserStoreException(
					"Role name not valid. Role name must be a non null string with following format, "
							+ regEx);
		}

		if (systemRoleManager.isExistingRole(roleName)) {
			throw new UserStoreException("Role name: " + roleName
					+ " in the system. Please pick another role name.");
		}
		systemRoleManager.addSystemRole(roleName, userList);
	}

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	public final boolean isSystemUserExisting(String userName) throws UserStoreException {
		return systemUserManager.isExistingSystemUser(userName);
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 * @throws UserStoreException
	 */
	public final boolean isSystemRoleExisting(String roleName) throws UserStoreException {
		return systemRoleManager.isExistingRole(roleName);
	}

	/**
	 * Check whether the basic requirements to initialize and function this UserStoreManager is
	 * fulfilled.
	 * 
	 * @throws UserStoreException
	 */
	protected void doInitialDataCheck() throws UserStoreException {

		if (!isExistingUser(realmConfig.getAdminUserName())) {
			String error = "Initial data has been changed after first start up. AdminUser does"
					+ "not exist.";
			throw new UserStoreException(error);
		}

		// if this is not the first start up, check admin role
		if (!isExistingRole(realmConfig.getAdminRoleName())) {
			String error = "Initial data has been changed after first start up. AdminRole does"
					+ " not exist.";
			throw new UserStoreException(error);
		}

		// if this is not the first start up, check wso2.anonymous.user
		if (!isSystemRoleExisting(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME)) {
			String error = "Initial data has been changed after first start up. Registry "
					+ "Anonymous Role does not exist.";
			throw new UserStoreException(error);
		}
		// if this is not the first start up, check wso2.anonymous.role
		if (!isSystemUserExisting(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME)) {
			String error = "Initial data has been changed after first start up. Registry "
					+ "Anonymous User does not exist.";
			throw new UserStoreException(error);
		}
	}

	/**
	 * 
	 * 
	 * @param roleName
	 * @param filter
	 * @return
	 * @throws UserStoreException
	 */
	protected abstract String[] doGetUserListOfRole(String roleName, String filter)
			throws UserStoreException;

	/**
	 * 
	 * 
	 * @param userName
	 * @param filter
	 * @return
	 * @throws UserStoreException
	 */
	public final String[] doGetRoleListOfUser(String userName, String filter)
			throws UserStoreException {

		String[] roleList;

		String[] internalRoles = getInternalRoleListOfUser(userName);

		String[] modifiedExternalRoleList = new String[0];

		if (readGroupsEnabled) {
			String[] externalRoles = getExternalRoleListOfUser(userName);
			modifiedExternalRoleList = UserCoreUtil.addDomainToNames(externalRoles,
					getMyDomainName());
		}

		roleList = UserCoreUtil.combine(internalRoles, Arrays.asList(modifiedExternalRoleList));

		return roleList;
	}

	/**
	 * 
	 * @param filter
	 * @return
	 * @throws UserStoreException
	 */
	public final String[] getHybridRoles(String filter) throws UserStoreException {
		return hybridRoleManager.getHybridRoles(filter);
	}

	/**
	 * 
	 * @param claimList
	 * @return
	 * @throws UserStoreException
	 */
	protected List<String> getMappingAttributeList(List<String> claimList)
			throws UserStoreException {
		ArrayList<String> attributeList = null;
		Iterator<String> claimIter = null;

		attributeList = new ArrayList<String>();
		if (claimList == null) {
			return attributeList;
		}
		claimIter = claimList.iterator();
		while (claimIter.hasNext()) {
			try {
				attributeList.add(claimManager.getAttributeName(claimIter.next()));
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}
		}
		return attributeList;
	}

	/**
	 * 
	 * @throws UserStoreException
	 */
	protected void addInitialInternalData() throws UserStoreException {

		if (!isSystemUserExisting(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME)) {
			addSystemUser(
					CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
					UserCoreUtil
							.getPolicyFriendlyRandomPassword(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME),
					null, null, null, false);
		}
		if (!isSystemRoleExisting(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME)) {
			addSystemRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
					new String[] { CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME }, null);
		}

		if (!hybridRoleManager.isExistingRole(UserCoreUtil.removeDomainFromName(realmConfig
				.getEveryOneRoleName()))) {
			hybridRoleManager.addHybridRole(
					UserCoreUtil.removeDomainFromName(realmConfig.getEveryOneRoleName()), null);
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 * @throws UserStoreException
	 */
	public Map<String, Integer> getMaxListCount(String type) throws UserStoreException {

		if (!type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)
				&& !type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)) {
			throw new UserStoreException("Invalid count parameter");
		}

		if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)
				&& maxUserListCount != null) {
			return maxUserListCount;
		}

		if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)
				&& maxRoleListCount != null) {
			return maxRoleListCount;
		}

		Map<String, Integer> maxListCount = new HashMap<String, Integer>();
		for (Map.Entry<String, UserStoreManager> entry : userStoreManagerHolder.entrySet()) {
			UserStoreManager storeManager = entry.getValue();
			if (storeManager instanceof AbstractUserStoreManager) {
				String maxConfig = storeManager.getRealmConfiguration().getUserStoreProperty(type);
				if (maxConfig == null) {
					// set a default value
					maxConfig = MAX_LIST_LENGTH;
				}
				maxListCount.put(entry.getKey(), Integer.parseInt(maxConfig));
			}
		}

		if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME) == null) {
			String maxConfig = realmConfig.getUserStoreProperty(type);
			if (maxConfig == null) {
				// set a default value
				maxConfig = MAX_LIST_LENGTH;
			}
			maxListCount.put(null, Integer.parseInt(maxConfig));
		}

		if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST)) {
			this.maxUserListCount = maxListCount;
			return this.maxUserListCount;
		} else if (type.equals(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)) {
			this.maxRoleListCount = maxListCount;
			return this.maxRoleListCount;
		} else {
			throw new UserStoreException("Invalid count parameter");
		}
	}

	/**
	 * 
	 * @return
	 */
	protected String getMyDomainName() {
		return UserCoreUtil.getDomainName(realmConfig);
	}

    protected void persistDomain() throws UserStoreException {
        String domain = UserCoreUtil.getDomainName(this.realmConfig);
        if (this.realmConfig.isPrimary()) {
            UserCoreUtil.persistDomainWithId(UserCoreConstants.PRIMARY_DOMAIN_ID, domain,
                                             this.tenantId, this.dataSource);
        } else if (domain != null) {
            UserCoreUtil.persistDomain(domain, this.tenantId, this.dataSource);
        }
    }
}
