/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.rssmanager.core.dao.impl;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.dao.DatabaseUserDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUserDAOImpl implements DatabaseUserDAO {

    public void addDatabaseUser(DatabaseUser user, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "INSERT INTO RM_DATABASE_USER SET USERNAME = ?, RSS_INSTANCE_ID = (SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ?), TYPE = ?, TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getRssInstanceName());
            stmt.setInt(3, tenantId);
            stmt.setString(4, user.getType());
            stmt.setInt(5, RSSDAOUtil.getTenantId());
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while creating the database user '" +
                    user.getName() + "' : " + e.getMessage());
        } catch (RSSManagerCommonException e) {
            throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                    "current tenant", e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    public void removeDatabaseUser(String rssInstanceName, String username,
                                   int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "DELETE FROM RM_DATABASE_USER WHERE USERNAME = ? AND RSS_INSTANCE_ID = (SELECT RSS_INSTANCE_ID FROM RSS_INSTANCE WHERE NAME = ?) AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, rssInstanceName);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while dropping the database user '" +
                    username + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    public void updateDatabaseUser(DatabasePrivilegeSet privileges, String rssInstanceName, DatabaseUser user, String databaseName) throws RSSDAOException {

    }

    public boolean isDatabaseUserExist(String rssInstanceName, String username,
                                       int tenantId) throws RSSDAOException {
        boolean isExist = false;
        Connection conn = RSSDAO.createConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
            String sql = "SELECT u.ID AS DATABASE_USER_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TYPE = ? AND u.TENANT_ID = ? AND u.USERNAME = ?";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(2, RSSDAOUtil.getTenantId());
                stmt.setString(3, username);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    int databaseId = rs.getInt("DATABASE_USER_ID");
                    if (databaseId > 0) {
                        isExist = true;
                    }
                }
                return isExist;
            } catch (SQLException e) {
                throw new RSSDAOException("Error occurred while checking the existence of " +
                        "the database user '" + username + "' : " + e.getMessage(), e);
            } catch (RSSManagerCommonException e) {
                throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                        "current tenant", e);
            } finally {
                RSSDAOUtil.cleanupResources(rs, stmt, conn);
            }
        } else {
            String sql = "SELECT u.ID AS DATABASE_USER_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND s.NAME = ? AND u.TYPE = ? AND u.TENANT_ID = ? AND u.USERNAME = ?";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, rssInstanceName);
                stmt.setString(2, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(3, RSSDAOUtil.getTenantId());
                stmt.setString(4, username);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    int databaseId = rs.getInt("DATABASE_USER_ID");
                    if (databaseId > 0) {
                        isExist = true;
                    }
                }
                return isExist;
            } catch (SQLException e) {
                throw new RSSDAOException("Error occurred while checking the existence of " +
                        "the database user '" + username + "' : " + e.getMessage(), e);
            } catch (RSSManagerCommonException e) {
                throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                        "current tenant", e);
            } finally {
                RSSDAOUtil.cleanupResources(rs, stmt, conn);
            }
        }
    }

    
    public DatabaseUser getDatabaseUser(String rssInstanceName, String username,
                                        int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DatabaseUser user = new DatabaseUser();
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND s.NAME = ? AND s.TENANT_ID = ? AND u.USERNAME = ? AND u.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, username);
            stmt.setInt(4, RSSDAOUtil.getTenantId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                user = createDatabaseUserFromRS(rs);
            }
            return user;
        } catch (SQLException e) {
            throw new RSSDAOException("Error while occurred while retrieving information of " +
                    "the database user '" + user.getName() + "' : " + e.getMessage(), e);
        } catch (RSSManagerCommonException e) {
            throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                    "current tenant", e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    
    public DatabaseUser[] getDatabaseUsers(int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DatabaseUser> result = new ArrayList<DatabaseUser>();
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(createDatabaseUserFromRS(rs));
            }
            return result.toArray(new DatabaseUser[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving the database users : " +
                    e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    
    public DatabaseUser[] getDatabaseUsersByRSSInstance(String rssInstanceName,
                                                        int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }

    
    public DatabaseUser[] getDatabaseUsersByDatabase(String rssInstanceName, String database,
                                                     int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }

    
    public DatabaseUser[] getAssignedDatabaseUsers(String rssInstanceName, String databaseName,
                                                   int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DatabaseUser> result = new ArrayList<DatabaseUser>();
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT p.USERNAME FROM RM_USER_DATABASE_ENTRY e, (SELECT t.ID AS DATABASE_ID, u.ID AS DATABASE_USER_ID, u.USERNAME FROM RM_DATABASE_USER u,(SELECT d.RSS_INSTANCE_ID, d.NAME, d.ID FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND s.NAME = ? AND s.TENANT_ID = ? AND d.NAME = ? AND d.TENANT_ID = ?) t WHERE u.RSS_INSTANCE_ID = t.RSS_INSTANCE_ID AND TENANT_ID = ?) p WHERE e.DATABASE_USER_ID = p.DATABASE_USER_ID AND e.DATABASE_ID = p.DATABASE_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, databaseName);
            stmt.setInt(4, tenantId);
            stmt.setInt(5, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("USERNAME");
                DatabaseUser user = new DatabaseUser(username, null, null, null);
                result.add(user);
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving the users assigned " +
                    "to the database '" + databaseName + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
        return result.toArray(new DatabaseUser[result.size()]);
    }

    
    public DatabaseUser[] getAvailableDatabaseUsers(String rssInstanceName, String databaseName,
                                                    int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }

    
    public void removeDatabasePrivileges(String rssInstanceName, String username,
                                         int tenantId) throws RSSDAOException {

    }

    
    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName,
                                                          String databaseName, String username,
                                                          int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT * FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID = (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID = (SELECT ID FROM RM_DATABASE WHERE NAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?) AND DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE USERNAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setString(2, rssInstanceName);
            stmt.setInt(3, tenantId);
            stmt.setString(4, username);
            stmt.setInt(5, tenantId);
            stmt.setInt(6, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                privileges = createUserDatabasePrivilegeSetFromRS(rs);
            }
            return privileges;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving user permissions " +
                    "granted for the database user '" + username + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    
    public void setUserDatabasePrivileges(int id, DatabasePrivilegeTemplate template,
                                          int tenantId) throws RSSDAOException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "INSERT INTO RM_USER_DATABASE_PRIVILEGE(USER_DATABASE_ENTRY_ID, SELECT_PRIV, INSERT_PRIV, UPDATE_PRIV, DELETE_PRIV, CREATE_PRIV, DROP_PRIV, GRANT_PRIV, REFERENCES_PRIV, INDEX_PRIV, ALTER_PRIV, CREATE_TMP_TABLE_PRIV, LOCK_TABLES_PRIV, CREATE_VIEW_PRIV, SHOW_VIEW_PRIV, CREATE_ROUTINE_PRIV, ALTER_ROUTINE_PRIV, EXECUTE_PRIV, EVENT_PRIV, TRIGGER_PRIV) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, privileges.getSelectPriv());
            stmt.setString(3, privileges.getInsertPriv());
            stmt.setString(4, privileges.getUpdatePriv());
            stmt.setString(5, privileges.getDeletePriv());
            stmt.setString(6, privileges.getCreatePriv());
            stmt.setString(7, privileges.getDropPriv());
            stmt.setString(8, privileges.getGrantPriv());
            stmt.setString(9, privileges.getReferencesPriv());
            stmt.setString(10, privileges.getIndexPriv());
            stmt.setString(11, privileges.getAlterPriv());
            stmt.setString(12, privileges.getCreateTmpTablePriv());
            stmt.setString(13, privileges.getLockTablesPriv());
            stmt.setString(14, privileges.getCreateViewPriv());
            stmt.setString(15, privileges.getShowViewPriv());
            stmt.setString(16, privileges.getCreateRoutinePriv());
            stmt.setString(17, privileges.getAlterRoutinePriv());
            stmt.setString(18, privileges.getExecutePriv());
            stmt.setString(19, privileges.getEventPriv());
            stmt.setString(20, privileges.getTriggerPriv());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while setting user database " +
                    "privileges: " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    
    public RSSInstance resolveRSSInstance(String rssInstanceName, String username,
                                          int tenantId) throws RSSDAOException {
        return null;
    }

    private DatabaseUser createDatabaseUserFromRS(ResultSet rs)
            throws SQLException {
        String username = rs.getString("USERNAME");
        String rssInstName = rs.getString("RSS_INSTANCE_NAME");
        String type = rs.getString("TYPE");
        return new DatabaseUser(username, null, rssInstName, type);
    }

    private DatabasePrivilegeSet createUserDatabasePrivilegeSetFromRS(
            ResultSet rs) throws SQLException {
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        privileges.setSelectPriv(rs.getString("SELECT_PRIV"));
        privileges.setInsertPriv(rs.getString("INSERT_PRIV"));
        privileges.setUpdatePriv(rs.getString("UPDATE_PRIV"));
        privileges.setDeletePriv(rs.getString("DELETE_PRIV"));
        privileges.setCreatePriv(rs.getString("CREATE_PRIV"));
        privileges.setDropPriv(rs.getString("DROP_PRIV"));
        privileges.setGrantPriv(rs.getString("GRANT_PRIV"));
        privileges.setReferencesPriv(rs.getString("REFERENCES_PRIV"));
        privileges.setIndexPriv(rs.getString("INDEX_PRIV"));
        privileges.setAlterPriv(rs.getString("ALTER_PRIV"));
        privileges.setCreateTmpTablePriv(rs.getString("CREATE_TMP_TABLE_PRIV"));
        privileges.setLockTablesPriv(rs.getString("LOCK_TABLES_PRIV"));
        privileges.setCreateViewPriv(rs.getString("CREATE_VIEW_PRIV"));
        privileges.setShowViewPriv(rs.getString("SHOW_VIEW_PRIV"));
        privileges.setCreateRoutinePriv(rs.getString("CREATE_ROUTINE_PRIV"));
        privileges.setAlterRoutinePriv(rs.getString("ALTER_ROUTINE_PRIV"));
        privileges.setExecutePriv(rs.getString("EXECUTE_PRIV"));
        privileges.setEventPriv(rs.getString("EVENT_PRIV"));
        privileges.setTriggerPriv(rs.getString("TRIGGER_PRIV"));

        return privileges;
    }
    
}
