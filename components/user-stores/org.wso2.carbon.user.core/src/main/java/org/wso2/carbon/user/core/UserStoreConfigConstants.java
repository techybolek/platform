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
package org.wso2.carbon.user.core;

public class UserStoreConfigConstants {
    public static final String RWLDAP_USERSTORE_MANAGER = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager";
    public static final String ROLDAP_USERSTORE_MANAGER = "org.wso2.carbon.user.core.ldap.ReadOnlyLDAPUserStoreManager";
    public static final String ACTIVE_DIRECTORY_USERSTORE_MANAGER = "org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreManager";
    public static final String JDABC_USERSTORE_MANAGER = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    public static final String CASSANDRA_USERSTORE_MANAGER = "org.wso2.carbon.user.cassandra.CassandraUserStoreManager";
    public static final String PRIMARY = "PRIMARY";
    public static final String SECONDARY = "SECONDARY";


    //Common Properties
    public static final String maxRoleNameListLength = "MaxRoleNameListLength" ;
    public static final String maxRoleNameListLengthDescription = "Maximum number of roles retrieved at once.";
    public static final String maxUserNameListLength =  "MaxUserNameListLength";
    public static final String maxUserNameListLengthDescription = "Maximum number of users retrieved at once.";
    public static final String userRolesCacheEnabled = "UserRolesCacheEnabled";
    public static final String userRolesCacheEnabledDescription = "This is to indicate whether to cache the role list of a user.";


    //Mandatory to LDAP user stores
    public static final String connectionURL = "ConnectionURL";
    public static final String connectionURLDescription =  "Connection URL to the ldap server. In the case of default LDAP in carbon, port is mentioned in carbon.xml and referred here.";
    public static final String connectionName = "ConnectionName";
    public static final String connectionNameDescription = "This should be the DN (Distinguish Name) of the admin user in LDAP.";
    public static final String connectionPassword = "ConnectionPassword";
    public static final String connectionPasswordDescription = "Password of the admin user.";
    public static final String userSearchBase =  "UserSearchBase";
    public static final String userSearchBaseDescription = "DN of the context under which user entries are stored in LDAP.";
    public static final String disabled  = "Disabled";
    public static final String disabledDescription = "Whether user store is disabled";


    //Write Group Privilege Properties
    public static final String writeLDAPGroups = "WriteLDAPGroups";
    public static final String writeLDAPGroupsDescription = "Object Class used to construct user entries.";
    public static final String userEntryObjectClass = "UserEntryObjectClass" ;
    public static final String userEntryObjectClassDescription = "Object Class used to construct user entries.";
    public static final String passwordJavaScriptRegEx =  "PasswordJavaScriptRegEx";
    public static final String passwordJavaScriptRegExDescription =  "Policy that defines the password format.";
    public static final String usernameJavaScriptRegEx =  "UsernameJavaScriptRegEx";
    public static final String usernameJavaScriptRegExDescription =  "The regular expression used by the front-end components for username validation.";
    public static final String usernameJavaRegEx =  "UsernameJavaRegEx";
    public static final String usernameJavaRegExDescription =  "A regular expression to validate user names.";
    public static final String roleNameJavaScriptRegEx =  "RoleNameJavaScriptRegEx";
    public static final String roleNameJavaScriptRegExDescription =  "The regular expression used by the front-end components for role name validation.";
    public static final String roleNameJavaRegEx  =  "RoleNameJavaRegEx";
    public static final String roleNameJavaRegExDescription =  "A regular expression to validate role names.";
    public static final String groupEntryObjectClass =  "GroupEntryObjectClass";
    public static final String groupEntryObjectClassDescription =  "Object Class used to construct group entries.";
    public static final String emptyRolesAllowed = "EmptyRolesAllowed";
    public static final String emptyRolesAllowedDescription = "Specifies whether the underlying user store allows empty roles to be added";

    //LDAP Specific Properties
    public static final String passwordHashMethod = "PasswordHashMethod";
    public static final String passwordHashMethodDescription = "Password Hash method to use when storing user entries.";
    public static final String usernameListFilter = "UsernameListFilter";
    public static final String usernameListFilterDescription = "Filtering criteria for listing all the user entries in LDAP.";
    public static final String usernameSearchFilter = "UserNameSearchFilter";
    public static final String usernameSearchFilterDescription = "Filtering criteria for searching a particular user entry.";
    public static final String userNameAttribute = "UsernameAttribute";
    public static final String userNameAttributeDescription = "Attribute used for uniquely identifying a user entry. Users can be authenticated using their email address, uid and etc .";
    public static final String readLDAPGroups = "ReadLDAPGroups";
    public static final String readLDAPGroupsDescription = "Specifies whether groups should be read from LDAP.";
    public static final String groupSearchBase = "GroupSearchBase";
    public static final String groupSearchBaseDescription = "DN of the context under which user entries are stored in LDAP.";
    public static final String groupNameListFilter = "GroupNameListFilter";
    public static final String groupNameListFilterDescription = "Filtering criteria for listing all the group entries in LDAP.";
    public static final String groupNameAttribute = "GroupNameAttribute";
    public static final String groupNameAttributeDescription = "Attribute used for uniquely identifying a user entry.";
    public static final String groupNameSearchFilter = "GroupNameSearchFilter";
    public static final String groupNameSearchFilterDescription = "Filtering criteria for searching a particular group entry.";
    public static final String membershipAttribute = "MembershipAttribute";
    public static final String membershipAttributeDescription = "Attribute used to define members of LDAP groups.";
    public static final String userDNPattern = "UserDNPattern";
    public static final String userDNPatternDescription = "The patten for user's DN. It can be defined to improve the LDAP search.";


}