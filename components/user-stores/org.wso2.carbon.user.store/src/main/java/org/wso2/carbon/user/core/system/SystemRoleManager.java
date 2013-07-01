/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.system;

import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.common.UserRolesCache;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemRoleManager {

    private static Log log = LogFactory.getLog(SystemRoleManager.class);

    private DataSource dataSource;
    int tenantId;
    private RealmConfiguration realmConfig;
    protected UserRealm userRealm = null;
    protected UserRolesCache userRolesCache = null;
    private boolean userRolesCacheEnabled = true;
    private String cacheIdentifier;

    public SystemRoleManager(DataSource dataSource, int tenantId, RealmConfiguration realmConfig,
                             UserRealm realm) throws UserStoreException {
        super();
        this.dataSource = dataSource;
        this.tenantId = tenantId;
        this.realmConfig = realmConfig;
        this.userRealm = realm;
        //persist system domain
        UserCoreUtil.persistDomainWithId(UserCoreConstants.SYSTEM_DOMAIN_ID, null, this.tenantId, 
                                         this.dataSource);
    }

    public void addSystemRole(String roleName, String[] userList) throws UserStoreException {
        Connection dbConnection = null;
        try {
            if (realmConfig.getEveryOneRoleName().equals(roleName)) {
                // this role exist automatically.
                return;
            }

            dbConnection = getDBConnection();
            if (!this.isExistingRole(roleName)) {
                DatabaseUtil.updateDatabase(dbConnection, SystemJDBCConstants.ADD_ROLE_SQL,
                        roleName, tenantId);
            }
            if (userList != null) {
                String sql = SystemJDBCConstants.ADD_USER_TO_ROLE_SQL;
                String type = DatabaseCreator.getDatabaseType(dbConnection);
                if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
                    sql = SystemJDBCConstants.ADD_USER_TO_ROLE_SQL_MSSQL;
                }
                if(UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    sql = SystemJDBCConstants.ADD_USER_TO_ROLE_SQL_OPENEDGE;
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sql, userList,
                            tenantId, roleName, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sql, userList,
                            roleName, tenantId, tenantId);
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        //if existing users are added to role, need to update user role cache
        if ((userList != null) && (userList.length > 0)) {
            clearUserRolesCacheByTenant(this.tenantId);
        }
    }

    protected void clearUserRolesCacheByTenant(int tenantID) {
        if (userRolesCache != null) {
            userRolesCache.clearCacheByTenant(tenantID);
            AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
            authorizationCache.clearCacheByTenant(tenantID);
        }
    }

    public boolean isExistingRole(String roleName) throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isExisting = false;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SystemJDBCConstants.GET_ROLE_ID);
            prepStmt.setString(1, roleName);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                int value = rs.getInt(1);
                if (value > -1) {
                    isExisting = true;
                }
            }
            return isExisting;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    public String[] getSystemRoles() throws UserStoreException {
        String sqlStmt = SystemJDBCConstants.GET_ROLES;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            return DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt,
                    tenantId);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public String[] getUserListOfSystemRole(String roleName) throws UserStoreException {
        if (realmConfig.getEveryOneRoleName().equals(roleName)) {
            throw new UserStoreException(
                    "Invalid operation. You are trying to retrieve all users from the external userstore.");
        }

        String sqlStmt = SystemJDBCConstants.GET_USER_LIST_OF_ROLE_SQL;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String[] names = DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt,
                    roleName, tenantId, tenantId);
            return names;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public void updateUserListOfSystemRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        String sqlStmt1 = SystemJDBCConstants.REMOVE_USER_FROM_ROLE_SQL;
        String sqlStmt2 = SystemJDBCConstants.ADD_USER_TO_ROLE_SQL;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String type = DatabaseCreator.getDatabaseType(dbConnection);
            if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
                sqlStmt2 = SystemJDBCConstants.ADD_USER_TO_ROLE_SQL_MSSQL;
            }
            if (deletedUsers != null && deletedUsers.length > 0) {
                DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, deletedUsers,
                        roleName, tenantId, tenantId);
                //authz cache of deleted users from role, needs to be updated
                for (String deletedUser : deletedUsers) {
                    userRealm.getAuthorizationManager().clearUserAuthorization(deletedUser);
                }
            }
            if (newUsers != null && newUsers.length > 0) {
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    sqlStmt2 = SystemJDBCConstants.ADD_USER_TO_ROLE_SQL_OPENEDGE;
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, newUsers,
                            tenantId, roleName, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, newUsers,
                            roleName, tenantId, tenantId);
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public String[] getSystemRoleListOfUser(String userName) throws UserStoreException {
        String sqlStmt = SystemJDBCConstants.GET_ROLE_LIST_OF_USER_SQL;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            return DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt,
                    userName, tenantId, tenantId);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public void updateSystemRoleListOfUser(String user, String[] deletedRoles, String[] addRoles)
            throws UserStoreException {

        String sqlStmt1 = SystemJDBCConstants.REMOVE_ROLE_FROM_USER_SQL;
        String sqlStmt2 = SystemJDBCConstants.ADD_ROLE_TO_USER_SQL;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String type = DatabaseCreator.getDatabaseType(dbConnection);
            if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
                sqlStmt2 = SystemJDBCConstants.ADD_ROLE_TO_USER_SQL_MSSQL;
            }
            if (deletedRoles != null && deletedRoles.length > 0) {
                DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, deletedRoles,
                        tenantId, user, tenantId);
            }
            if (addRoles != null && addRoles.length > 0) {
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    sqlStmt2 = SystemJDBCConstants.ADD_ROLE_TO_USER_SQL_OPENEDGE;
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, user,
                            tenantId, addRoles, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, addRoles,
                            tenantId, user, tenantId);
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        //authz cache of user should also be updated if deleted roles are involved
        if (deletedRoles != null && deletedRoles.length > 0) {
            userRealm.getAuthorizationManager().clearUserAuthorization(user);
        }
    }

    public void deleteSystemRole(String roleName) throws UserStoreException {
        if (realmConfig.getEveryOneRoleName().equals(roleName)) {
            throw new UserStoreException("Invalid operation");
        }
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            DatabaseUtil.updateDatabase(dbConnection,
                    SystemJDBCConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL, roleName, tenantId,
                    tenantId);
            DatabaseUtil.updateDatabase(dbConnection, SystemJDBCConstants.DELETE_ROLE_SQL,
                    roleName, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        //also need to clear role authorization
        userRealm.getAuthorizationManager().clearRoleAuthorization(roleName);
    }

    public void updateSystemRoleName(String roleName, String newRoleName) throws UserStoreException {

        if (realmConfig.getAdminRoleName().equals(roleName)) {
            throw new UserStoreException("Cannot rename admin role");
        }
        if (realmConfig.getEveryOneRoleName().equals(roleName)) {
            throw new UserStoreException("Cannot rename everyone role");
        }
        if (this.isExistingRole(newRoleName)) {
            throw new UserStoreException(
                    "Role name: "+newRoleName+" in the system. Please pick another role name.");
        }
        String sqlStmt = SystemJDBCConstants.UPDATE_ROLE_NAME_SQL;
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for update system role name is null");
        }
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName, tenantId);
            } else {
                DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName);
            }
            dbConnection.commit();
            this.userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(roleName,
                    newRoleName);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {
        if (realmConfig.getEveryOneRoleName().equals(roleName)) {
            return true;
        }
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isUserInRole = false;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SystemJDBCConstants.IS_USER_IN_ROLE_SQL);
            prepStmt.setString(1, userName);
            prepStmt.setString(2, roleName);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                int value = rs.getInt(1);
                if (value != -1) {
                    isUserInRole = true;
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return isUserInRole;
    }
    /*If a user is added to a system role, that entry should be deleted upon deletion of the user*/
    public void deleteUser(String userName) throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(SystemJDBCConstants.REMOVE_USER_SQL);
            preparedStatement.setString(1, userName);
            preparedStatement.execute();
            //needs to clear authz cache of user
            this.userRealm.getAuthorizationManager().clearUserAuthorization(userName);
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, preparedStatement);
        }
    }

    private Connection getDBConnection() throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        return dbConnection;
    }

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

}
