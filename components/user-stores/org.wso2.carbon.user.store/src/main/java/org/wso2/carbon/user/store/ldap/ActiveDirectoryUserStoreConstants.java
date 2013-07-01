/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.user.core.ldap;


import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserStoreConfigConstants;

import java.util.ArrayList;

public class ActiveDirectoryUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();

    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionName, "uid=admin,ou=system", UserStoreConfigConstants.connectionNameDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionURL, "ldap://localhost:${Ports.EmbeddedLDAP.LDAPServerPort}", UserStoreConfigConstants.connectionURLDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword, "admin", UserStoreConfigConstants.connectionPasswordDescription);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "ou=Users,dc=wso2,dc=org", UserStoreConfigConstants.userSearchBaseDescription);
        setMandatoryProperty(UserStoreConfigConstants.disabled, "false", UserStoreConfigConstants.disabledDescription);

        setProperty(UserStoreConfigConstants.maxUserNameListLength, "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);


//      LDAP Specific Properties
        setProperty(UserStoreConfigConstants.passwordHashMethod, "SHA", UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty(UserStoreConfigConstants.usernameListFilter, "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription);
        setProperty(UserStoreConfigConstants.usernameSearchFilter, "(&amp;(objectClass=person)(uid=?))", UserStoreConfigConstants.usernameSearchFilterDescription);
        setProperty(UserStoreConfigConstants.userNameAttribute, "uid", UserStoreConfigConstants.userNameAttributeDescription);
        setProperty(UserStoreConfigConstants.readLDAPGroups, "true", UserStoreConfigConstants.readLDAPGroupsDescription);
        setProperty(UserStoreConfigConstants.groupSearchBase, "ou=Groups,dc=wso2,dc=org", UserStoreConfigConstants.groupSearchBaseDescription);
        setProperty(UserStoreConfigConstants.groupNameListFilter, "(objectClass=groupOfNames)", UserStoreConfigConstants.groupNameListFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameAttribute, "cn", UserStoreConfigConstants.groupNameAttributeDescription);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter, "(&amp;(objectClass=groupOfNames)(cn=?))", UserStoreConfigConstants.groupNameSearchFilterDescription);
        setProperty(UserStoreConfigConstants.membershipAttribute, "member", UserStoreConfigConstants.membershipAttributeDescription);
        setProperty(UserStoreConfigConstants.userDNPattern, "uid={0},ou=Users,dc=wso2,dc=org", UserStoreConfigConstants.userDNPatternDescription);
    }


    private static void setMandatoryProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, true, null);
        ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);

    }

    private static void setProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, false, null);
        ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);

    }


}
