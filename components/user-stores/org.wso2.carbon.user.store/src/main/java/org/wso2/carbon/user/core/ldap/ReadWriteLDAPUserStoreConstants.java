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

public class ReadWriteLDAPUserStoreConstants {


        //Properties for Read Write LDAP User Store Manager
        public static final ArrayList<Property> RWLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionName,"uid=admin,ou=system",UserStoreConfigConstants.connectionNameDescription,null);
        setMandatoryProperty(UserStoreConfigConstants.connectionURL,"ldap://localhost:${Ports.EmbeddedLDAP.LDAPServerPort}",UserStoreConfigConstants.connectionURLDescription,null);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword,"admin",UserStoreConfigConstants.connectionPasswordDescription,null);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase,"ou=Users,dc=wso2,dc=org",UserStoreConfigConstants.userSearchBaseDescription,null);
        setMandatoryProperty(UserStoreConfigConstants.disabled,"false",UserStoreConfigConstants.disabledDescription,null);

        setProperty(UserStoreConfigConstants.maxUserNameListLength, "100", UserStoreConfigConstants.maxUserNameListLengthDescription,null);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "100", UserStoreConfigConstants.maxRoleNameListLengthDescription,null);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "true", UserStoreConfigConstants.userRolesCacheEnabledDescription,null);

        Property[] writeGroupDependProperties = {createProperty(UserStoreConfigConstants.userEntryObjectClass, "wso2Person", UserStoreConfigConstants.userEntryObjectClassDescription),
        createProperty(UserStoreConfigConstants.passwordJavaScriptRegEx, "^[\\S]{5,30}$", UserStoreConfigConstants.passwordJavaScriptRegExDescription),
        createProperty(UserStoreConfigConstants.usernameJavaScriptRegEx, "^[\\S]{3,30}$", UserStoreConfigConstants.usernameJavaScriptRegExDescription),
        createProperty(UserStoreConfigConstants.usernameJavaRegEx, "[a-zA-Z0-9._-|//]{3,30}$", UserStoreConfigConstants.usernameJavaRegExDescription),
        createProperty(UserStoreConfigConstants.roleNameJavaRegEx, "[a-zA-Z0-9._-|//]{3,30}$", UserStoreConfigConstants.roleNameJavaRegExDescription),
        createProperty(UserStoreConfigConstants.roleNameJavaScriptRegEx, "^[\\S]{3,30}$", UserStoreConfigConstants.roleNameJavaScriptRegExDescription),
        createProperty(UserStoreConfigConstants.groupEntryObjectClass, "groupOfNames", UserStoreConfigConstants.groupEntryObjectClassDescription),
        createProperty(UserStoreConfigConstants.emptyRolesAllowed, "true", UserStoreConfigConstants.emptyRolesAllowedDescription)};

        setProperty(UserStoreConfigConstants.writeLDAPGroups,"true",UserStoreConfigConstants.writeLDAPGroupsDescription,writeGroupDependProperties);

//        RWLDAP Specific Properties
        setProperty(UserStoreConfigConstants.passwordHashMethod,"SHA",UserStoreConfigConstants.passwordHashMethodDescription,null);
        setProperty(UserStoreConfigConstants.usernameListFilter,"(objectClass=person)",UserStoreConfigConstants.usernameListFilterDescription,null);
        setProperty(UserStoreConfigConstants.usernameSearchFilter,"(&amp;(objectClass=person)(uid=?))",UserStoreConfigConstants.usernameSearchFilterDescription,null);
        setProperty(UserStoreConfigConstants.userNameAttribute,"uid",UserStoreConfigConstants.userNameAttributeDescription,null);
        setProperty(UserStoreConfigConstants.readLDAPGroups,"true",UserStoreConfigConstants.readLDAPGroupsDescription,null);
        setProperty(UserStoreConfigConstants.groupSearchBase,"ou=Groups,dc=wso2,dc=org",UserStoreConfigConstants.groupSearchBaseDescription,null);
        setProperty(UserStoreConfigConstants.groupNameListFilter,"(objectClass=groupOfNames)",UserStoreConfigConstants.groupNameListFilterDescription,null);
        setProperty(UserStoreConfigConstants.groupNameAttribute,"cn",UserStoreConfigConstants.groupNameAttributeDescription,null);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter,"(&amp;(objectClass=groupOfNames)(cn=?))",UserStoreConfigConstants.groupNameSearchFilterDescription,null);
        setProperty(UserStoreConfigConstants.membershipAttribute,"member",UserStoreConfigConstants.membershipAttributeDescription,null);
        setProperty(UserStoreConfigConstants.userDNPattern,"uid={0},ou=Users,dc=wso2,dc=org",UserStoreConfigConstants.userDNPatternDescription,null);
    }


    private static void setMandatoryProperty(String name,String value,String description,Property[] childProperties){
        Property property = new Property(name,value,description,true,childProperties);
        RWLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static void setProperty(String name,String value,String description,Property[] childProperties){
        Property property = new Property(name,value,description,false,childProperties);
        RWLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static Property createProperty(String name, String value, String description){
        return new Property(name,value,description,false,null);

    }

}
