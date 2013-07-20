/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.identity.provider.mgt.util;

public class IdentityProviderMgtConstants {

    public static class SQLQueries {

        public static final String GET_TENANT_IDPS_SQL = "SELECT UM_TENANT_IDP_ISSUER FROM UM_TENANT_IDP WHERE UM_TENANT_ID=?";
        public static final String GET_TENANT_IDP_SQL = "SELECT UM_ID, UM_TENANT_IDP_URL, UM_TENANT_IDP_THUMBPRINT, UM_TENANT_IDP_PRIMARY FROM UM_TENANT_IDP WHERE UM_TENANT_ID=? AND UM_TENANT_IDP_ISSUER=?";
        public static final String GET_TENANT_IDP_ROLE_MAPPINGS_SQL = "SELECT UM_TENANT_ROLE FROM UM_TENANT_IDP_ROLES_MAPPING WHERE UM_TENANT_IDP_ROLE_ID=?";
        public static final String UPDATE_TENANT_IDP_SQL = "UPDATE UM_TENANT_IDP SET UM_TENANT_IDP_ISSUER=?, UM_TENANT_IDP_URL=?, UM_TENANT_IDP_THUMBPRINT=?, UM_TENANT_IDP_PRIMARY=? WHERE UM_TENANT_ID=? AND UM_TENANT_IDP_ISSUER=?";
        public static final String ADD_TENANT_IDP_ROLES_SQL = "INSERT INTO UM_TENANT_IDP_ROLES (UM_TENANT_IDP_ID, UM_TENANT_IDP_ROLE) VALUES (?, ?)";
        public static final String DELETE_TENANT_IDP_ROLES_SQL = "DELETE FROM UM_TENANT_IDP_ROLES WHERE (UM_TENANT_IDP_ID=? AND UM_TENANT_IDP_ROLE=?)";
        public static final String UPDATE_TENANT_IDP_ROLES_SQL = "UPDATE UM_TENANT_IDP_ROLES SET UM_TENANT_IDP_ROLE=? WHERE (UM_TENANT_IDP_ID=? AND UM_TENANT_IDP_ROLE=?)";
        public static final String GET_TENANT_IDP_ROLES_SQL = "SELECT UM_ID, UM_TENANT_IDP_ROLE FROM UM_TENANT_IDP_ROLES WHERE UM_TENANT_IDP_ID=?";
        public static final String DELETE_TENANT_IDP_ROLES_MAPPING_SQL = "DELETE FROM UM_TENANT_IDP_ROLES_MAPPING WHERE (UM_TENANT_IDP_ROLE_ID=? AND UM_TENANT_ID=? AND UM_TENANT_ROLE=?)";
        public static final String ADD_TENANT_IDP_ROLES_MAPPING_SQL = "INSERT INTO UM_TENANT_IDP_ROLES_MAPPING (UM_TENANT_IDP_ROLE_ID, UM_TENANT_ID, UM_TENANT_ROLE) VALUES (?, ?, ?)";
        public static final String ADD_TENANT_IDP_SQL = "INSERT INTO UM_TENANT_IDP (UM_TENANT_ID, UM_TENANT_IDP_ISSUER, UM_TENANT_IDP_URL, UM_TENANT_IDP_THUMBPRINT, UM_TENANT_IDP_PRIMARY) VALUES (?, ?, ?, ?, ?)";
        public static final String DELETE_TENANT_IDP_SQL = "DELETE FROM UM_TENANT_IDP WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_ISSUER=?)";
        public static final String IS_EXISTING_TENANT_IDP_SQL = "SELECT UM_ID FROM UM_TENANT_IDP WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_ISSUER=?)";
        public static final String IS_EXISTING_PRIMARY_TENANT_IDP_SQL = "SELECT UM_ID FROM UM_TENANT_IDP WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_PRIMARY=?)";
        public static final String SWITCH_TENANT_IDP_PRIMARY_SQL = "UPDATE UM_TENANT_IDP SET UM_TENANT_IDP_PRIMARY=? WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_PRIMARY=?)";
        public static final String SWITCH_TENANT_IDP_PRIMARY_ON_DELETE_SQL = "UPDATE UM_TENANT_IDP SET UM_TENANT_IDP_PRIMARY=? WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_ISSUER=? AND UM_TENANT_IDP_PRIMARY=?)";
        public static final String DELETE_TENANT_ROLE_SQL = "DELETE FROM UM_TENANT_IDP_ROLES WHERE UM_TENANT_ROLE=?";

    }
}
