/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.hybrid.HybridRoleManager;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.system.SystemRoleManager;
import org.wso2.carbon.user.core.system.SystemUserManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.*;

import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.*;
import javax.naming.ldap.LdapContext;
import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReadOnlyLDAPUserStoreManager extends AbstractUserStoreManager {

	protected LDAPConnectionContext connectionSource = null;
	private final int MAX_USER_CACHE = 200;

	private Map<String, String> userCache = new ConcurrentHashMap<String, String>(MAX_USER_CACHE);

	private static Log log = LogFactory.getLog(ReadOnlyLDAPUserStoreManager.class);

	protected String userSearchBase = null;
	protected String groupSearchBase = null;

	/*
	 * following is by default true since embedded-ldap allows it. If connected to an external ldap
	 * where empty roles not allowed, then following property should be set accordingly in
	 * user-mgt.xml
	 */
	protected boolean emptyRolesAllowed = false;

    public ReadOnlyLDAPUserStoreManager(){

    }

	public ReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig,
			Map<String, Object> properties, ClaimManager claimManager,
			ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
			throws UserStoreException {
		this(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);
	}

	/**
	 * Constructor with Hybrid Role Manager
	 * 
	 * @param realmConfig
	 * @param properties
	 * @param claimManager
	 * @param profileManager
	 * @param realm
	 * @param tenantId
	 * @throws UserStoreException
	 */
	public ReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig,
			Map<String, Object> properties, ClaimManager claimManager,
			ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId,
			boolean skipInitData) throws UserStoreException {

		if (log.isDebugEnabled()) {
			log.debug("Initializing Started " + System.currentTimeMillis());
		}

		this.realmConfig = realmConfig;
		this.claimManager = claimManager;
		this.userRealm = realm;
		this.tenantId = tenantId;
		
		if (isReadOnly() && realmConfig.isPrimary()) {
			String adminRoleName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName());
			realmConfig.setAdminRoleName(UserCoreUtil.addInternalDomainName(adminRoleName));
		} 

		// check if required configurations are in the user-mgt.xml
		checkRequiredUserStoreConfigurations();

		dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
		if (dataSource == null) {
			// avoid returning null
			dataSource = DatabaseUtil.getRealmDataSource(realmConfig);
		}
		if (dataSource == null) {
			throw new UserStoreException("Data Source is null");
		}
		properties.put(UserCoreConstants.DATA_SOURCE, dataSource);

		hybridRoleManager = new HybridRoleManager(dataSource, tenantId, realmConfig, userRealm);

		systemRoleManager = new SystemRoleManager(dataSource, tenantId, realmConfig, userRealm);

		systemUserManager = new SystemUserManager(dataSource, tenantId, realmConfig, userRealm,
				systemRoleManager, claimManager);

		/*
		 * obtain the ldap connection source that was created in DefaultRealmService.
		 */

		connectionSource = new LDAPConnectionContext(realmConfig);

		try {
			connectionSource.getContext();
			if (this.isReadOnly()) {
				log.info("LDAP connection created successfully in read-only mode");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new UserStoreException("Cannot create connection to LDAP server. Error message "
					+ e.getMessage());
		}
		this.userRealm = realm;

		if (!skipInitData) {
            //persist domain
            this.persistDomain();
			if (realmConfig.isPrimary()) {
				if ("true".equals(realmConfig.getAddAdmin())) {
					this.addInitialAdminData();
				}
				addInitialInternalData();
				doInitialDataCheck();
			}
			/*
			 * Initialize user roles cache as implemented in AbstractUserStoreManager
			 */
			initUserRolesCache();
		}

		if (log.isDebugEnabled()) {
			log.debug("Initializing Ended " + System.currentTimeMillis());
		}

	}

	/**
	 * This operates in the pure read-only mode without a connection to a database. No handling of
	 * Internal roles.
	 */
	public ReadOnlyLDAPUserStoreManager(RealmConfiguration realmConfig, ClaimManager claimManager,
			ProfileConfigurationManager profileManager) throws UserStoreException {

		if (log.isDebugEnabled()) {
			log.debug("Started " + System.currentTimeMillis());
		}
		this.realmConfig = realmConfig;
		this.claimManager = claimManager;

		// check if required configurations are in the user-mgt.xml
		checkRequiredUserStoreConfigurations();

		this.connectionSource = new LDAPConnectionContext(realmConfig);
	}

	/**
	 * 
	 * @throws UserStoreException
	 */
	protected void checkRequiredUserStoreConfigurations() throws UserStoreException {

		log.debug("Checking LDAP configurations ..");

		String connectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
        String DNSURL = realmConfig.getUserStoreProperty(LDAPConstants.DNS_URL);

        if ((connectionURL == null || connectionURL.equals("")) && ((DNSURL == null || DNSURL.equals("")))) {
            throw new UserStoreException(
                    "Required ConnectionURL property is not set at the LDAP configurations");
        }
        String connectionName = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_NAME);
		if (connectionName == null || connectionName.equals("")) {
			throw new UserStoreException(
					"Required ConnectionNme property is not set at the LDAP configurations");
		}
		String connectionPassword = realmConfig
				.getUserStoreProperty(LDAPConstants.CONNECTION_PASSWORD);
		if (connectionPassword == null || connectionPassword.equals("")) {
			throw new UserStoreException(
					"Required ConnectionPassword property is not set at the LDAP configurations");
		}
		userSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
		if (userSearchBase == null || userSearchBase.equals("")) {
			throw new UserStoreException(
					"Required UserSearchBase property is not set at the LDAP configurations");
		}
		String usernameListFilter = realmConfig
				.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
		if (usernameListFilter == null || usernameListFilter.equals("")) {
			throw new UserStoreException(
					"Required UserNameListFilter property is not set at the LDAP configurations");
		}
		String usernameAttribute = realmConfig
				.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
		if (usernameAttribute == null || usernameAttribute.equals("")) {
			throw new UserStoreException(
					"Required UserNameAttribute property is not set at the LDAP configurations");
		}
		
		writeGroupsEnabled = false;

		// Groups properties
		if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED) != null) {
			readGroupsEnabled = Boolean.parseBoolean(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
		} else {
			if (realmConfig.getUserStoreProperty(LDAPConstants.READ_LDAP_GROUPS) != null) {
				readGroupsEnabled = Boolean.parseBoolean(realmConfig
						.getUserStoreProperty(LDAPConstants.READ_LDAP_GROUPS));
			} else {
				readGroupsEnabled = false;
			}
		}
		
		if (readGroupsEnabled) {
			groupSearchBase = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
			if (groupSearchBase == null || groupSearchBase.equals("")) {
				throw new UserStoreException(
						"Required GroupSearchBase property is not set at the LDAP configurations");
			}
			String groupNameListFilter = realmConfig
					.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
			if (groupNameListFilter == null || groupNameListFilter.equals("")) {
				throw new UserStoreException(
						"Required GroupNameListFilter property is not set at the LDAP configurations");
			}
			String groupNameAttribute = realmConfig
					.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
			if (groupNameAttribute == null || groupNameAttribute.equals("")) {
				throw new UserStoreException(
						"Required GroupNameAttribute property is not set at the LDAP configurations");
			}
			String memebershipAttribute = realmConfig
					.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
			if (memebershipAttribute == null || memebershipAttribute.equals("")) {
				throw new UserStoreException(
						"Required MembershipAttribute property is not set at the LDAP configurations");
			}
		}
	}

	/**
	 * 
	 */
	public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

		if (userName == null || credential == null) {
			return false;
		}

		userName = userName.trim();
		// if replace escape characters enabled, modify username by replacing
		// escape characters.
		userName = replaceEscapeCharacters(userName);
		String password = (String) credential;
		password = password.trim();

		if (userName.equals("") || password.equals("")) {
			return false;
		}

		boolean bValue = false;
		String name = null;
		// read list of patterns from user-mgt.xml
		String patterns = realmConfig.getUserStoreProperty(LDAPConstants.USER_DN_PATTERN);

		if (patterns != null && !patterns.isEmpty()) {

			if ((name = userCache.get(userName)) != null) {
				try {
					bValue = this.bindAsUser(name, (String) credential);
				} catch (NamingException e) {
					// do nothing if bind fails since we check for other DN
					// patterns as well.
					if (log.isDebugEnabled()) {
						log.debug("Checking authentication with UserDN " + name + "failed "
								+ e.getStackTrace());
					}
				}

				if (bValue) {
					return bValue;
				}
			}

			// if the property is present, split it using # to see if there are
			// multiple patterns specified.
			String[] userDNPatternList = patterns.split("#");
			if (userDNPatternList.length > 0) {
				for (String userDNPattern : userDNPatternList) {
					name = MessageFormat.format(userDNPattern, userName);
					try {
						if (name != null) {
							bValue = this.bindAsUser(name, (String) credential);
							if (bValue) {
								userCache.put(userName, name);
								break;
							}
						}
					} catch (NamingException e) {
						// do nothing if bind fails since we check for other DN
						// patterns as well.
						if (log.isDebugEnabled()) {
							log.debug("Checking authentication with UserDN " + userDNPattern
									+ "failed " + e.getStackTrace());
						}
					}
				}
			}
		} else {
			name = getNameInSpaceForUserName(userName);
			try {
				if (name != null) {
					bValue = this.bindAsUser(name, (String) credential);
				}
			} catch (NamingException e) {
				log.error(e.getMessage(), e);
				throw new UserStoreException(e.getMessage(), e);
			}
		}

		return bValue;
	}

	/**
	 * We do not have multiple profile support with LDAP.
	 */
	public String[] getAllProfileNames() throws UserStoreException {
		return new String[] { UserCoreConstants.DEFAULT_PROFILE };
	}

	/**
	 * We do not have multiple profile support with LDAP.
	 */
	public String[] getProfileNames(String userName) throws UserStoreException {
		return new String[] { UserCoreConstants.DEFAULT_PROFILE };
	}

	/**
	 * 
	 */
	public Map<String, String> getUserPropertyValues(String userName, String[] propertyNames,
			String profileName) throws UserStoreException {
		Map<String, String> values = new HashMap<String, String>();
		String searchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
		String userNameProperty = realmConfig
				.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
		// if user name contains domain name, remove domain name
		String[] userNames = userName.split(CarbonConstants.DOMAIN_SEPARATOR);
		if (userNames.length > 1) {
			userName = userNames[1];
		}
		searchFilter = "(&" + searchFilter + "(" + userNameProperty + "=" + userName + "))";

		DirContext dirContext = this.connectionSource.getContext();
		NamingEnumeration<?> answer = null;
		NamingEnumeration<?> attrs = null;
		try {
			answer = this.searchForUser(searchFilter, propertyNames, dirContext);
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				Attributes attributes = sr.getAttributes();
				if (attributes != null) {
					for (String name : propertyNames) {
                        if (name != null) {
                            Attribute attribute = attributes.get(name);
                            if (attribute != null) {
                                StringBuffer attrBuffer = new StringBuffer();
                                for (attrs = attribute.getAll(); attrs.hasMore();) {
                                    String attr = (String) attrs.next();
                                    if (attr != null && attr.trim().length() > 0) {
                                        attrBuffer.append(attr + ",");
                                    }
                                }
                                String value = attrBuffer.toString();
                                /*
                                 * Length needs to be more than one for a valid attribute, since we
                                 * attach ",".
                                 */
                                if (value != null && value.trim().length() > 1) {
                                    value = value.substring(0, value.length() - 1);
                                    values.put(name, value);
                                }
                            }
                        }
                    }
				}
			}

		} catch (NamingException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			// close the naming enumeration and free up resources
			JNDIUtil.closeNamingEnumeration(attrs);
			JNDIUtil.closeNamingEnumeration(answer);
			// close directory context
			JNDIUtil.closeContext(dirContext);
		}
		return values;
	}

	/**
	 * 
	 */
	public boolean doCheckExistingRole(String roleName) throws UserStoreException {

		boolean isExisting = false;

		String searchFilter = realmConfig
				.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
		String roleNameProperty = realmConfig
				.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
		searchFilter = "(&" + searchFilter + "(" + roleNameProperty + "=" + roleName + "))";
		String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);

		SearchControls searchCtls = new SearchControls();
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchCtls.setReturningAttributes(new String[] { roleNameProperty });
		if (this.getListOfNames(searchBase, searchFilter, searchCtls, roleNameProperty).size() > 0) {
			isExisting = true;
		}

		return isExisting;
	}

	/**
	 * 
	 */
	public boolean doCheckExistingUser(String userName) throws UserStoreException {

		boolean bFound = false;
		try {
			String name = getNameInSpaceForUserName(userName);
			if (name != null && name.length() > 0) {
				bFound = true;
			}
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		}

		return bFound;
	}

	/**
	 * 
	 */
	public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {
		String[] userNames = new String[0];

		if (maxItemLimit == 0) {
			return userNames;
		}

		int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
		int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

		try {
			givenMax = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
		} catch (Exception e) {
			givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
		}

		try {
			searchTime = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
		} catch (Exception e) {
			searchTime = UserCoreConstants.MAX_SEARCH_TIME;
		}

		if (maxItemLimit < 0 || maxItemLimit > givenMax) {
			maxItemLimit = givenMax;
		}

		SearchControls searchCtls = new SearchControls();
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchCtls.setCountLimit(maxItemLimit);
		searchCtls.setTimeLimit(searchTime);

		if (filter.contains("?") || filter.contains("**")) {
			throw new UserStoreException(
					"Invalid character sequence entered for user serch. Please enter valid sequence.");
		}

		StringBuffer searchFilter = null;
		searchFilter = new StringBuffer(
				realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
		String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

		String userNameProperty = realmConfig
				.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);

		String serviceNameAttribute = "sn";

		StringBuffer buff = new StringBuffer();

		// read the display name attribute - if provided
		String displayNameAttribute = realmConfig
				.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);

		String[] returnedAtts = null;

		if (displayNameAttribute != null) {
			returnedAtts = new String[] { userNameProperty, serviceNameAttribute,
					displayNameAttribute };
			buff.append("(&").append(searchFilter).append("(").append(displayNameAttribute)
					.append("=").append(filter).append("))");
		} else {
			returnedAtts = new String[] { userNameProperty, serviceNameAttribute };
			buff.append("(&").append(searchFilter).append("(").append(userNameProperty).append("=")
					.append(filter).append("))");
		}

		searchCtls.setReturningAttributes(returnedAtts);
		DirContext dirContext = null;
		NamingEnumeration<SearchResult> answer = null;

		try {
			dirContext = connectionSource.getContext();
			answer = dirContext.search(searchBase, buff.toString(), searchCtls);
			List<String> list = new ArrayList<String>();
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				if (sr.getAttributes() != null) {
					Attribute attr = sr.getAttributes().get(userNameProperty);

					/*
					 * If this is a service principle, just ignore and iterate rest of the array.
					 * The entity is a service if value of surname is Service
					 */
					Attribute attrSurname = sr.getAttributes().get(serviceNameAttribute);

					if (attrSurname != null) {
						String serviceName = (String) attrSurname.get();
						if (serviceName != null
								&& serviceName
										.equals(LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE)) {
							continue;
						}
					}

					/*
					 * if display name is provided, read that attribute
					 */
					Attribute displayName = null;
					if (displayNameAttribute != null) {
						displayName = sr.getAttributes().get(displayNameAttribute);
					}

					if (attr != null) {
						String name = (String) attr.get();
						String display = null;
						if (displayName != null) {
							display = (String) displayName.get();
						}
						// append the domain if exist
						String domain = this.getRealmConfiguration().getUserStoreProperty(
								UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
						// get the name in the format of domainName/userName|domainName/displayName
						name = UserCoreUtil.getCombinedName(domain, name, display);
						list.add(name);
					}
				}
			}
			userNames = list.toArray(new String[list.size()]);

			Arrays.sort(userNames);
		} catch (NamingException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			JNDIUtil.closeNamingEnumeration(answer);
			JNDIUtil.closeContext(dirContext);
		}
		return userNames;
	}

    @Override
    protected String[] doGetDisplayNamesForInternalRole(String[] userNames)
            throws UserStoreException {
        //search the user with UserNameAttribute, retrieve their DisplayNameAttribute combine and return
        String displayNameAttribute = this.realmConfig.getUserStoreProperty(
                LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
        if (displayNameAttribute != null) {
            String userNameAttribute = this.realmConfig.getUserStoreProperty(
                    LDAPConstants.USER_NAME_ATTRIBUTE);
            String userSearchBase = this.realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
            String userNameListFilter = this.realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);

            String[] returningAttributes = {displayNameAttribute};
            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(returningAttributes);

            List<String> combinedNames = new ArrayList<String>();
            if (userNames != null && userNames.length > 0) {
                for (String userName : userNames) {
                    String searchFilter = "(&" + userNameListFilter + "(" + userNameAttribute + "=" + userName + "))";
                    List<String> displayNames = this.getListOfNames(userSearchBase, searchFilter,
                                                                    searchControls, displayNameAttribute);
                    //we expect only one display name
                    String name = UserCoreUtil.getCombinedName(
                            this.realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME),
                            userName, displayNames.get(0));
                    combinedNames.add(name);
                }
                return combinedNames.toArray(new String[combinedNames.size()]);
            } else {
                return userNames;
            }
        } else {
            return userNames;
        }
    }

    /**
	 * 
	 * @param dn
	 * @param credentials
	 * @return
	 * @throws NamingException
	 * @throws UserStoreException
	 */
	protected boolean bindAsUser(String dn, String credentials) throws NamingException,
			UserStoreException {
		boolean isAuthed = false;

		/*Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, LDAPConstants.DRIVER_NAME);
		env.put(Context.SECURITY_PRINCIPAL, dn);
		env.put(Context.SECURITY_CREDENTIALS, credentials);
		env.put("com.sun.jndi.ldap.connect.pool", "true");*/
		/**
		 * In carbon JNDI context we need to by pass specific tenant context and we need the base
		 * context for LDAP operations.
		 */
		//env.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");

		/*String rawConnectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
		String portInfo = rawConnectionURL.split(":")[2];

		String connectionURL = null;
		String port = null;
		// if the port contains a template string that refers to carbon.xml
		if ((portInfo.contains("${")) && (portInfo.contains("}"))) {
			port = Integer.toString(CarbonUtils.getPortFromServerConfig(portInfo));
			connectionURL = rawConnectionURL.replace(portInfo, port);
		}
		if (port == null) { // if not enabled, read LDAP url from user.mgt.xml
			connectionURL = realmConfig.getUserStoreProperty(LDAPConstants.CONNECTION_URL);
		}*/
		/*env.put(Context.PROVIDER_URL, connectionURL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");*/

		LdapContext cxt = null;
		try {
			//cxt = new InitialLdapContext(env, null);
            cxt = this.connectionSource.getContextWithCredentials(dn, credentials);
			isAuthed = true;
		} catch (AuthenticationException e) {
			/*
			 * StringBuilder stringBuilder = new StringBuilder("Authentication failed for user ");
			 * stringBuilder.append(dn).append(" ").append(e.getMessage());
			 */

			// we avoid throwing an exception here since we throw that exception
			// in a one level above this.
			if (log.isDebugEnabled()) {
				log.debug("Authentication failed " + e.getExplanation().toString());
			}

		} finally {
			JNDIUtil.closeContext(cxt);
		}
		return isAuthed;
	}

	/**
	 * 
	 * @param searchFilter
	 * @param returnedAtts
	 * @param dirContext
	 * @return
	 * @throws UserStoreException
	 */
	protected NamingEnumeration<SearchResult> searchForUser(String searchFilter,
			String[] returnedAtts, DirContext dirContext) throws UserStoreException {
		SearchControls searchCtls = new SearchControls();
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
		if (returnedAtts != null && returnedAtts.length > 0) {
			searchCtls.setReturningAttributes(returnedAtts);
		}
		try {
			NamingEnumeration<SearchResult> answer = dirContext.search(searchBase, searchFilter,
					searchCtls);
			return answer;
		} catch (NamingException e) {
			throw new UserStoreException(e.getMessage());
		}
	}

	/**
	 * 
	 */
	public void doAddRole(String roleName, String[] userList, org.wso2.carbon.user.api.Permission[] permissions)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * LDAP user store does not support bulk import.
	 * 
	 * @return Always returns <code>false<code>.
	 */
	public boolean isBulkImportSupported() {
		return false;
	}

	/**
	 * This method is to check whether multiple profiles are allowed with a particular user-store.
	 * For an example, currently, JDBC user store supports multiple profiles and where as ApacheDS
	 * does not allow. LDAP currently does not allow multiple profiles.
	 * 
	 * @return boolean
	 */
	public boolean isMultipleProfilesAllowed() {
		return false;
	}

	/**
	 * 
	 */
	public void doDeleteRole(String roleName) throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {

		if (maxItemLimit == 0) {
			return new String[0];
		}

		int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;

		int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

		try {
			givenMax = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST));
		} catch (Exception e) {
			givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
		}

		try {
			searchTime = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
		} catch (Exception e) {
			searchTime = UserCoreConstants.MAX_SEARCH_TIME;
		}

		if (maxItemLimit < 0 || maxItemLimit > givenMax) {
			maxItemLimit = givenMax;
		}

		List<String> externalRoles = new ArrayList<String>();

		if (readGroupsEnabled) {

			SearchControls searchCtls = new SearchControls();
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchCtls.setCountLimit(maxItemLimit);
			searchCtls.setTimeLimit(searchTime);

			String searchFilter = realmConfig
					.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
			String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);

			String roleNameProperty = realmConfig
					.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
			String returnedAtts[] = { roleNameProperty };
			searchCtls.setReturningAttributes(returnedAtts);

			// / search filter TODO
			StringBuffer buff = new StringBuffer();
			buff.append("(&").append(searchFilter).append("(").append(roleNameProperty).append("=")
					.append(filter).append("))");

			DirContext dirContext = null;
			NamingEnumeration<SearchResult> answer = null;

			try {
				dirContext = connectionSource.getContext();
				answer = dirContext.search(searchBase, buff.toString(), searchCtls);
				while (answer.hasMoreElements()) {
					SearchResult sr = (SearchResult) answer.next();
					if (sr.getAttributes() != null) {
						Attribute attr = sr.getAttributes().get(roleNameProperty);
						if (attr != null) {
							String name = (String) attr.get();
							// append the domain if exist
							String domain = this.getRealmConfiguration().getUserStoreProperty(
									UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
							name = UserCoreUtil.addDomainToName(name, domain);
							externalRoles.add(name);
						}
					}
				}
			} catch (NamingException e) {
				throw new UserStoreException(e.getMessage(), e);
			} finally {
				JNDIUtil.closeNamingEnumeration(answer);
				JNDIUtil.closeContext(dirContext);
			}

		}

		return externalRoles.toArray(new String[externalRoles.size()]);
	}

	/**
	 * 
	 */
	public RealmConfiguration getRealmConfiguration() {
		return this.realmConfig;
	}

	/**
	 * 
	 */
	public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {

		String[] names = new String[0];

		int givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;

		int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

		try {
			givenMax = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
		} catch (Exception e) {
			givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
		}

		try {
			searchTime = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
		} catch (Exception e) {
			searchTime = UserCoreConstants.MAX_SEARCH_TIME;
		}

		try {
			SearchControls searchCtls = new SearchControls();
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchCtls.setTimeLimit(searchTime);
			searchCtls.setCountLimit(givenMax);

			String searchFilter = realmConfig
					.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
			String roleNameProperty = realmConfig
					.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
			searchFilter = "(&" + searchFilter + "(" + roleNameProperty + "=" + roleName + "))";
			String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);

			String membershipProperty = realmConfig
					.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);
			String returnedAtts[] = { membershipProperty };
			searchCtls.setReturningAttributes(returnedAtts);

			List<String> userDNList = new ArrayList<String>();

			// read the DN of users who are members of the group

			DirContext dirContext = connectionSource.getContext();
			NamingEnumeration<SearchResult> answer = dirContext.search(searchBase, searchFilter,
					searchCtls);
			int count = 0;
			SearchResult sr = null;
			while (answer.hasMore()) {
				if (count > 0) {
					throw new UserStoreException("More than one group exist with name");
				}
				sr = (SearchResult) answer.next();
				count++;
			}
			// read the member attribute and get DNs of the users
			Attributes attributes = sr.getAttributes();

			if (attributes != null) {
				NamingEnumeration attributeEntry = null;
				for (attributeEntry = attributes.getAll(); attributeEntry.hasMore();) {
					Attribute valAttribute = (Attribute) attributeEntry.next();
					if (membershipProperty == null
							|| membershipProperty.equals(valAttribute.getID())) {
						NamingEnumeration values = null;
						for (values = valAttribute.getAll(); values.hasMore();) {
							String value = values.next().toString();
							userDNList.add(value);
						}
					}
				}
			}
			/*
			 * List<String> list = this.getAttributeListOfOneElement(searchBase, searchFilter,
			 * searchCtls);
			 */

			List<String> userList = new ArrayList<String>();

			String domainName = realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

			// iterate over users' DN list and get userName and display name attribute values

			String userNameProperty = realmConfig
					.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
			String displayNameAttribute = realmConfig
					.getUserStoreProperty(LDAPConstants.DISPLAY_NAME_ATTRIBUTE);
			String[] returnedAttributes = { userNameProperty, displayNameAttribute };

			for (String user : userDNList) {

				dirContext = this.connectionSource.getContext();
				Attributes userAttributes = dirContext.getAttributes(user, returnedAttributes);

				String displayName = null;
				String userName = null;
				if (userAttributes != null) {
					Attribute userNameAttribute = userAttributes.get(userNameProperty);
					if (userNameAttribute != null) {
						userName = (String) userNameAttribute.get();
					}
					if (displayNameAttribute != null) {
						Attribute displayAttribute = userAttributes.get(displayNameAttribute);
						if (displayAttribute != null) {
							displayName = (String) displayAttribute.get();
						}
					}
				}
				user = UserCoreUtil.getCombinedName(domainName, userName, displayName);
				userList.add(user);
			}
			names = userList.toArray(new String[userList.size()]);
		} catch (PartialResultException e) {
			// can be due to referrals in AD. so just ignore error
			if (log.isDebugEnabled()) {
				log.debug(e);
			}
		} catch (NamingException e) {
			throw new UserStoreException("Error in reading user information in the user store.");
		}

		return names;
	}

	/**
	 * This method will check whether back link support is enabled and will return the effective
	 * search base. Read http://www.frickelsoft.net/blog/?p=130 for more details.
	 * 
	 * @return The search base based on back link support. If back link support is enabled this will
	 *         return user search base, else group search base.
	 */
	protected String getEffectiveSearchBase() {

		String backLinksEnabled = realmConfig
				.getUserStoreProperty(LDAPConstants.BACK_LINKS_ENABLED);
		boolean isBackLinkEnabled = false;

		if (backLinksEnabled != null && !backLinksEnabled.equals("")) {
			isBackLinkEnabled = Boolean.parseBoolean(backLinksEnabled);
		}

		if (isBackLinkEnabled) {
			return realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
		} else {
			return realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getExternalRoleListOfUser(String userName) throws UserStoreException {

		List<String> list = new ArrayList<String>();
		/*
		 * do not search REGISTRY_ANONNYMOUS_USERNAME or REGISTRY_SYSTEM_USERNAME in LDAP because it
		 * causes warn logs printed from embedded-ldap.
		 */
		if (readGroupsEnabled && (!UserCoreUtil.isRegistryAnnonymousUser(userName))
				&& (!UserCoreUtil.isRegistrySystemUser(userName))) {

			SearchControls searchCtls = new SearchControls();
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// Get the effective search base
			String searchBase = this.getEffectiveSearchBase();

			String memberOfProperty = realmConfig
					.getUserStoreProperty(LDAPConstants.MEMBEROF_ATTRIBUTE);
			if (memberOfProperty != null && memberOfProperty.length() > 0) {
				String searchFilter = realmConfig
						.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
				String userNameProperty = realmConfig
						.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
				searchFilter = "(&" + searchFilter + "(" + userNameProperty + "=" + userName + "))";

				String binaryAttribute = realmConfig
						.getUserStoreProperty(LDAPConstants.LDAP_ATTRIBUTES_BINARY);
				String primaryGroupId = realmConfig
						.getUserStoreProperty(LDAPConstants.PRIMARY_GROUP_ID);

				String returnedAtts[] = { memberOfProperty };

				if (binaryAttribute != null && primaryGroupId != null) {
					returnedAtts = new String[] { memberOfProperty, binaryAttribute, primaryGroupId };
				}

				searchCtls.setReturningAttributes(returnedAtts);

				if (binaryAttribute != null && primaryGroupId != null) {
					list = this.getAttributeListOfOneElementWithPrimarGroup(searchBase,
							searchFilter, searchCtls, binaryAttribute, primaryGroupId,
							userNameProperty, memberOfProperty);
				} else {
					// get DNs of the groups to which this user belongs
					List<String> groupDNs = this.getListOfNames(searchBase, searchFilter,
							searchCtls, memberOfProperty);
					/*
					 * to be compatible with AD as well, we need to do a search over the groupsand
					 * find those groups' attribute value defined for group name attribute and
					 * return
					 */
					list = this.getGroupNameAttributeValuesOfGroups(groupDNs);
				}

			} else {
				// read the roles with this membership property
				String searchFilter = realmConfig
						.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER);
				String membershipProperty = realmConfig
						.getUserStoreProperty(LDAPConstants.MEMBERSHIP_ATTRIBUTE);

				if (membershipProperty == null || membershipProperty.length() < 1) {
					throw new UserStoreException(
							"Please set member of attribute or membership attribute");
				}
				String nameInSpace = this.getNameInSpaceForUserName(userName);
				searchFilter = "(&" + searchFilter + "(" + membershipProperty + "=" + nameInSpace
						+ "))";
				String roleNameProperty = realmConfig
						.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
				String returnedAtts[] = { roleNameProperty };
				searchCtls.setReturningAttributes(returnedAtts);
				list = this.getListOfNames(searchBase, searchFilter, searchCtls, roleNameProperty);
			}
		} else if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
			// returning a REGISTRY_ANONNYMOUS_ROLE_NAME for REGISTRY_ANONNYMOUS_USERNAME
			list.add(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);
		}

		return list.toArray(new String[list.size()]);
	}

	/**
	 * {@inheritDoc}
	 */

	public boolean isReadOnly() throws UserStoreException {
		return true;
	}

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	protected String getNameInSpaceForUserName(String userName) throws UserStoreException {
		StringBuffer searchFilter = new StringBuffer(
				realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
		String userNameProperty = realmConfig
				.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
		StringBuffer buff = new StringBuffer();
		buff.append("(&").append(searchFilter).append("(").append(userNameProperty).append("=")
				.append(userName).append("))");

		if (log.isDebugEnabled()) {
			log.debug("Searching for " + buff.toString());
		}
		DirContext dirContext = this.connectionSource.getContext();
		NamingEnumeration<SearchResult> answer = null;
		try {
			String name = null;
			answer = this.searchForUser(buff.toString(), null, dirContext);
			int count = 0;
			SearchResult userObj = null;
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				if (count > 0) {
					log.error("More than one user exist for the same name");
				}
				count++;
				userObj = sr;
			}
			if (userObj != null) {
				name = userObj.getNameInNamespace();
			}

			return name;
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			JNDIUtil.closeNamingEnumeration(answer);
			JNDIUtil.closeContext(dirContext);
		}

	}

	/**
	 * 
	 * @param sr
	 * @param groupAttributeName
	 * @return
	 */
	private List<String> parseSearchResult(SearchResult sr, String groupAttributeName) {
		List<String> list = new ArrayList<String>();
		Attributes attrs = sr.getAttributes();

		if (attrs != null) {
			try {
				NamingEnumeration ae = null;
				for (ae = attrs.getAll(); ae.hasMore();) {
					Attribute attr = (Attribute) ae.next();
					if (groupAttributeName == null || groupAttributeName.equals(attr.getID())) {
						NamingEnumeration e = null;
						for (e = attr.getAll(); e.hasMore();) {
							String value = e.next().toString();
							int begin = value.indexOf("=") + 1;
							int end = value.indexOf(",");
							if (begin > -1 && end > -1) {
								value = value.substring(begin, end);
							}
							list.add(value);
						}
						JNDIUtil.closeNamingEnumeration(e);
					}
				}
				JNDIUtil.closeNamingEnumeration(ae);
			} catch (NamingException e) {
				log.error(e.getMessage(), e);
			}
		}
		return list;
	}

	/**
	 * 
	 * @param searchBase
	 * @param searchFilter
	 * @param searchCtls
	 * @param objectSid
	 * @param primaryGroupID
	 * @param userAttributeId
	 * @param groupAttributeName
	 * @return
	 * @throws UserStoreException
	 */
	private List<String> getAttributeListOfOneElementWithPrimarGroup(String searchBase,
			String searchFilter, SearchControls searchCtls, String objectSid,
			String primaryGroupID, String userAttributeId, String groupAttributeName)
			throws UserStoreException {
		List<String> list = new ArrayList<String>();
		DirContext dirContext = null;
		NamingEnumeration<SearchResult> answer = null;
		try {
			dirContext = connectionSource.getContext();
			answer = dirContext.search(searchBase, searchFilter, searchCtls);
			int count = 0;
			while (answer.hasMore()) {
				if (count > 0) {
					log.error("More than element user exist with name");
					throw new UserStoreException("More than element user exist with name");
				}
				SearchResult sr = (SearchResult) answer.next();
				count++;

				list = parseSearchResult(sr, groupAttributeName);

				String primaryGroupSID = LDAPUtil.getPrimaryGroupSID(sr, objectSid, primaryGroupID);
				String primaryGroupName = LDAPUtil.findGroupBySID(dirContext, searchBase,
						primaryGroupSID, userAttributeId);
				if (primaryGroupName != null) {
					list.add(primaryGroupName);
				}
			}

		} catch (PartialResultException e) {
			// can be due to referrals in AD. so just ignore error
			if (log.isDebugEnabled()) {
				log.debug(e);
			}
		} catch (NamingException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			JNDIUtil.closeNamingEnumeration(answer);
			JNDIUtil.closeContext(dirContext);
		}
		return list;
	}

	// ****************************************************

	@SuppressWarnings("rawtypes")
	protected List<String> getAttributeListOfOneElement(String searchBase, String searchFilter,
			SearchControls searchCtls) throws UserStoreException {
		List<String> list = new ArrayList<String>();
		DirContext dirContext = null;
		NamingEnumeration<SearchResult> answer = null;
		try {
			dirContext = connectionSource.getContext();
			answer = dirContext.search(searchBase, searchFilter, searchCtls);
			int count = 0;
			while (answer.hasMore()) {
				if (count > 0) {
					log.error("More than element user exist with name");
					throw new UserStoreException("More than element user exist with name");
				}
				SearchResult sr = (SearchResult) answer.next();
				count++;
				list = parseSearchResult(sr, null);
			}
		} catch (PartialResultException e) {
			// can be due to referrals in AD. so just ignore error
			if (log.isDebugEnabled()) {
				log.debug(e);
			}
		} catch (NamingException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			JNDIUtil.closeNamingEnumeration(answer);
			JNDIUtil.closeContext(dirContext);
		}
		return list;
	}

	/**
	 * 
	 * @param searchBase
	 * @param searchFilter
	 * @param searchCtls
	 * @param property
	 * @return
	 * @throws UserStoreException
	 */
	private List<String> getListOfNames(String searchBase, String searchFilter,
			SearchControls searchCtls, String property) throws UserStoreException {

		List<String> names = new ArrayList<String>();
		DirContext dirContext = null;
		NamingEnumeration<SearchResult> answer = null;
		try {
			dirContext = connectionSource.getContext();
			answer = dirContext.search(searchBase, searchFilter, searchCtls);
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				if (sr.getAttributes() != null) {
					Attribute attr = sr.getAttributes().get(property);
					if (attr != null) {
						String name = (String) attr.get();
						names.add(name);
					}
				}
			}
			return names;
		} catch (NamingException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			JNDIUtil.closeNamingEnumeration(answer);
			JNDIUtil.closeContext(dirContext);
		}
	}

	/**
	 * 
	 */
	public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant)
			throws org.wso2.carbon.user.api.UserStoreException {
		return getProperties((Tenant) tenant);
	}

	/**
	 * 
	 */
	public int getTenantId() throws UserStoreException {
		return this.tenantId;
	}

	/* TODO: support for multiple user stores */
	public String[] getUserListFromProperties(String property, String value, String profileName)
			throws UserStoreException {

		List<String> values = new ArrayList<String>();
		String searchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER);
		String userPropertyName = realmConfig
				.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);

		searchFilter = "(&" + searchFilter + "(" + property + "=" + value + "))";

		DirContext dirContext = this.connectionSource.getContext();
		NamingEnumeration<?> answer = null;
		NamingEnumeration<?> attrs = null;
		try {
			answer = this
					.searchForUser(searchFilter, new String[] { userPropertyName }, dirContext);
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				Attributes attributes = sr.getAttributes();
				if (attributes != null) {
					Attribute attribute = attributes.get(userPropertyName);
					if (attribute != null) {
						StringBuffer attrBuffer = new StringBuffer();
						for (attrs = attribute.getAll(); attrs.hasMore();) {
							String attr = (String) attrs.next();
							if (attr != null && attr.trim().length() > 0) {
								attrBuffer.append(attr + ",");
							}
						}
						String propertyValue = attrBuffer.toString();
						// Length needs to be more than one for a valid
						// attribute, since we
						// attach ",".
						if (propertyValue != null && propertyValue.trim().length() > 1) {
							propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
							values.add(propertyValue);
						}
					}
				}
			}

		} catch (NamingException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			// close the naming enumeration and free up resources
			JNDIUtil.closeNamingEnumeration(attrs);
			JNDIUtil.closeNamingEnumeration(answer);
			// close directory context
			JNDIUtil.closeContext(dirContext);
		}

		return values.toArray(new String[values.size()]);
	}

	// ************** NOT GOING TO IMPLEMENT ***************

	/**
	 * 
	 */
	public Date getPasswordExpirationTime(String username) throws UserStoreException {
		return null;
	}

	/**
	 * 
	 */
	public int getTenantId(String username) throws UserStoreException {
		throw new UserStoreException("Invalid operation");
	}

	/**
	 * //TODO:remove this method
	 * 
	 * @deprecated
	 * @param username
	 * @return
	 * @throws UserStoreException
	 */
	public int getUserId(String username) throws UserStoreException {
		throw new UserStoreException("Invalid operation");
	}

	/**
	 * 
	 */
	public void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");

	}

	/**
	 * 
	 */
	public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");

	}

	/**
	 * 
	 * @param userName
	 * @param credential
	 * @param roleList
	 * @param claims
	 * @param profileName
	 * @throws UserStoreException
	 */
	public void doAddUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName) throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public void doAddUser(String userName, Object credential, String[] roleList,
			Map<String, String> claims, String profileName, boolean requirePasswordChange)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public void doDeleteUser(String userName) throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public void doSetUserClaimValue(String userName, String claimURI, String claimValue,
			String profileName) throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");

	}

	/**
	 * 
	 */
	public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public void doUpdateCredentialByAdmin(String userName, Object newCredential)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");

	}

	/*
	 * ****************Unsupported methods list over***********************************************
	 */

	/**
	 * 
	 */
	public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
			throws UserStoreException {
		throw new UserStoreException(
				"User store is operating in read only mode. Cannot write into the user store.");
	}

	/**
	 * 
	 */
	public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
		return this.realmConfig.getUserStoreProperties();
	}

	/**
	 * 
	 */
	public void addRememberMe(String userName, String token)
			throws org.wso2.carbon.user.api.UserStoreException {
		JDBCUserStoreManager jdbcUserStore = new JDBCUserStoreManager(dataSource, realmConfig,
				realmConfig.getTenantId(), false);
		jdbcUserStore.addRememberMe(userName, token);
	}

	/**
	 * 
	 */
	public boolean isValidRememberMeToken(String userName, String token)
			throws org.wso2.carbon.user.api.UserStoreException {
		try {
			if (this.isExistingUser(userName)) {
				JDBCUserStoreManager jdbcUserStore = new JDBCUserStoreManager(dataSource,
						realmConfig, realmConfig.getTenantId(), false);
				return jdbcUserStore.isExistingRememberMeToken(userName, token);
			}
		} catch (Exception e) {
			log.error("Validating remember me token failed for" + userName);
			/*
			 * not throwing exception. because we need to seamlessly direct them to login uis
			 */
		}
		return false;
	}

	/**
	 * This method checks if the user belongs to the given role
	 * 
	 * @param userName
	 * @param roleName
	 * @return
	 * @throws UserStoreException
	 */
	protected boolean isUserInRole(String userName, String roleName) throws UserStoreException {
		String[] roles = this.getRoleListOfUser(userName);
		for (String role : roles) {
			if (role.toLowerCase().indexOf(roleName.toLowerCase()) > -1
					&& roleName.equalsIgnoreCase((UserCoreUtil.removeDomainFromName(role)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @throws UserStoreException
	 */
	private void addInitialAdminData() throws UserStoreException {

		if (realmConfig.getAdminRoleName() == null || realmConfig.getAdminUserName() == null) {
			throw new UserStoreException(
					"Admin user name or role name is not valid. Please provide valid values.");
		}

        String adminUserName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminUserName());
        String adminRoleName = UserCoreUtil.removeDomainFromName(realmConfig.getAdminRoleName());

        
		if (!doCheckExistingUser(adminUserName)) {
			throw new UserStoreException("Admin user does not exist.");
		}

		// Add administrator role, if not already added.
		// Here we are using name with the domain, since we call AbstractUserStoreManager.
		if (!isExistingRole(realmConfig.getAdminRoleName())) {

			if (log.isDebugEnabled()) {
				log.debug("Admin role does not exist. Hence creating the role.");
			}

			try {
				this.addRole(realmConfig.getAdminRoleName(),
						new String[] { realmConfig.getAdminUserName() }, null);
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				throw new UserStoreException(e);
			}
		}
        
		// Here we are using name without the domain, since we call a method in RO.
		if (!isUserInRole(adminUserName, adminRoleName)) {
			// This is when the admin role is changed in the user-mgt.xml
			if (log.isDebugEnabled()) {
				log.debug("Admin user is not in the Admin role. Adding the Admin user to the Admin role");
			}
			// Here we are using name with the domain, since we call AbstractUserStoreManager.
			String[] roles = { realmConfig.getAdminRoleName() };
			this.updateRoleListOfUser(realmConfig.getAdminUserName(), null, roles);
		}
	}

	/**
	 * 
	 * @param groupDNs
	 * @return
	 * @throws UserStoreException
	 */
	private List<String> getGroupNameAttributeValuesOfGroups(List<String> groupDNs)
			throws UserStoreException {

		// get the DNs of the groups to which user belongs to, as per the search parameters
		String groupNameAttribute = realmConfig
				.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE);
		String[] returnedAttributes = { groupNameAttribute };
		List<String> groupNameAttributeValues = new ArrayList<String>();
		try {
			DirContext dirContext = this.connectionSource.getContext();

			for (String group : groupDNs) {
				Attributes groupAttributes = dirContext.getAttributes(group, returnedAttributes);
				if (groupAttributes != null) {
					Attribute groupAttribute = groupAttributes.get(groupNameAttribute);
					if (groupAttribute != null) {
						String groupNameAttributeValue = (String) groupAttribute.get();
						groupNameAttributeValues.add(groupNameAttributeValue);
					}
				}
			}
		} catch (UserStoreException e) {
			throw new UserStoreException("Error in getting group name attribute values of groups");
		} catch (NamingException e) {
			throw new UserStoreException("Error in getting group name attribute values of groups");
		}
		return groupNameAttributeValues;
	}

    @Override
    public Property[] getDefaultUserStoreProperties() {
        return ReadOnlyLDAPUserStoreConstants.ROLDAP_USERSTORE_PROPERTIES.toArray(new Property[ReadOnlyLDAPUserStoreConstants.ROLDAP_USERSTORE_PROPERTIES.size()]);
    }
}
