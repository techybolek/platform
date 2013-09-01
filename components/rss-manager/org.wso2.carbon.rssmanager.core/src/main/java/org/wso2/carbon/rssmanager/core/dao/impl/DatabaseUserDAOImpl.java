/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.rssmanager.core.dao.DatabaseUserDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseUserDAOImpl implements DatabaseUserDAO {

    public void addDatabaseUser(String environmentName, RSSInstance rssInstance, DatabaseUser user,
                                int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "INSERT INTO RM_DATABASE_USER (USERNAME,RSS_INSTANCE_ID,TYPE,TENANT_ID) VALUES(?,(SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)),?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getName());
            stmt.setString(2, rssInstance.getName());
            stmt.setInt(3, MultitenantConstants.SUPER_TENANT_ID);
            stmt.setString(4, environmentName);
            stmt.setString(5, user.getType());
            stmt.setInt(6, tenantId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while creating the database user '" +
                    user.getName() + "' : " + e.getMessage());
        /*} catch (RSSManagerCommonException e) {
            throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                    "current tenant", e);*/
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    public void removeDatabaseUser(String environmentName, String rssInstanceName, String username,
                                   int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "DELETE FROM RM_DATABASE_USER WHERE USERNAME = ? AND RSS_INSTANCE_ID = (SELECT RSS_INSTANCE_ID FROM RM_SERVER_INSTANCE WHERE NAME = ?) AND TENANT_ID = ?";
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

    public void updateDatabaseUser(String environmentName, DatabasePrivilegeSet privileges,	RSSInstance rssInstance, DatabaseUser user,
                                   String databaseName) throws RSSDAOException {
    	
    	
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
        	int tenantId = RSSManagerUtil.getTenantId();
        	conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "UPDATE RM_USER_DATABASE_PRIVILEGE SET SELECT_PRIV = ?, INSERT_PRIV = ?, UPDATE_PRIV = ?, DELETE_PRIV = ?, CREATE_PRIV = ?, DROP_PRIV = ?, GRANT_PRIV = ?, REFERENCES_PRIV = ?, INDEX_PRIV = ?, ALTER_PRIV = ?, CREATE_TMP_TABLE_PRIV = ?, LOCK_TABLES_PRIV = ?, CREATE_VIEW_PRIV = ?, SHOW_VIEW_PRIV = ?, CREATE_ROUTINE_PRIV = ?, ALTER_ROUTINE_PRIV = ?, EXECUTE_PRIV = ?, EVENT_PRIV = ?, TRIGGER_PRIV = ? WHERE USER_DATABASE_ENTRY_ID = (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID = (SELECT ID FROM RM_DATABASE WHERE NAME = ? AND TENANT_ID = ? AND RSS_INSTANCE_ID = ?) AND DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE USERNAME = ? AND TENANT_ID = ? AND RSS_INSTANCE_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, privileges.getSelectPriv());
            stmt.setString(2, privileges.getInsertPriv());
            stmt.setString(3, privileges.getUpdatePriv());
            stmt.setString(4, privileges.getDeletePriv());
            stmt.setString(5, privileges.getCreatePriv());
            stmt.setString(6, privileges.getDropPriv());
            stmt.setString(7, privileges.getGrantPriv());
            stmt.setString(8, privileges.getReferencesPriv());
            stmt.setString(9, privileges.getIndexPriv());
            stmt.setString(10, privileges.getAlterPriv());
            stmt.setString(11, privileges.getCreateTmpTablePriv());
            stmt.setString(12, privileges.getLockTablesPriv());
            stmt.setString(13, privileges.getCreateViewPriv());
            stmt.setString(14, privileges.getShowViewPriv());
            stmt.setString(15, privileges.getCreateRoutinePriv());
            stmt.setString(16, privileges.getAlterRoutinePriv());
            stmt.setString(17, privileges.getExecutePriv());
            stmt.setString(18, privileges.getEventPriv());
            stmt.setString(19, privileges.getTriggerPriv());
            stmt.setString(20, databaseName);
            stmt.setInt(21, tenantId);
            stmt.setInt(22, rssInstance.getId());
            stmt.setString(23, user.getName());
            stmt.setInt(24, tenantId);
            stmt.setInt(25, rssInstance.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while updating database privileges "
                            + "of the user '" + user.getName() + "' : "
                            + e.getMessage(), e);
        } catch(RSSManagerException e){
        	throw new RSSDAOException(
                    "Error occurred while updating database privileges "
                            + "of the user '" + user.getName() + "' : "
                            + e.getMessage(), e);
        }finally {
        	RSSDAOUtil.cleanupResources(null, stmt, conn);
        }

    }

    public boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                       String username, int tenantId) throws RSSDAOException {
        boolean isExist = false;
        Connection conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
        	String sql = "SELECT u.ID AS DATABASE_USER_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TYPE = ? AND u.TENANT_ID = ? AND u.USERNAME = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(2, tenantId);
                stmt.setString(3, username);
                stmt.setString(4, environmentName);
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
            /*} catch (RSSManagerCommonException e) {
                throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                        "current tenant", e);*/
            } finally {
                RSSDAOUtil.cleanupResources(rs, stmt, conn);
            }
        } else {
        	ResultSet rs = null;
            PreparedStatement stmt = null;
            String sql = "SELECT u.ID AS DATABASE_USER_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND s.NAME = ? AND u.TYPE = ? AND u.TENANT_ID = ? AND u.USERNAME = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, rssInstanceName);
                stmt.setString(2, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(3, tenantId);
                stmt.setString(4, username);
                stmt.setString(5, environmentName);
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
            /*} catch (RSSManagerCommonException e) {
                throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                        "current tenant", e);*/
            } finally {
                RSSDAOUtil.cleanupResources(rs, stmt, conn);
            }
        }
    }

    
    public DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName,
                                        String username, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DatabaseUser user = new DatabaseUser();
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "SELECT u.ID, u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND s.NAME = ? AND s.TENANT_ID = ? AND u.USERNAME = ? AND u.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID); //TODO : rssInstance.getTenantId()
            stmt.setString(3, username);
            stmt.setInt(4, tenantId);
            stmt.setString(5, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                user = this.createDatabaseUserFromRS(rs);
            }
            return user;
        } catch (SQLException e) {
            throw new RSSDAOException("Error while occurred while retrieving information of " +
                    "the database user '" + user.getName() + "' : " + e.getMessage(), e);
        /*} catch (RSSManagerCommonException e) {
            throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                    "current tenant", e);*/
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    
    public DatabaseUser[] getDatabaseUsers(String environmentName,
                                           int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DatabaseUser> result = new ArrayList<DatabaseUser>();
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "SELECT u.ID, u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TENANT_ID = ?  AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, environmentName);
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

    
    public DatabaseUser[] getDatabaseUsersByRSSInstance(String environmentName,
                                                        String rssInstanceName,
                                                        int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }

    
    public DatabaseUser[] getDatabaseUsersByDatabase(String environmentName,
                                                     String rssInstanceName, String database,
                                                     int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }

    
    public DatabaseUser[] getAssignedDatabaseUsers(String environmentName, String rssInstanceName,
                                                   String databaseName,
                                                   int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DatabaseUser> result = new ArrayList<DatabaseUser>();
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
			String sql = "SELECT p.USERNAME FROM RM_USER_DATABASE_ENTRY e, (SELECT t.ID AS DATABASE_ID, u.ID AS DATABASE_USER_ID, u.USERNAME FROM RM_DATABASE_USER u," +
					" (SELECT d.RSS_INSTANCE_ID, d.NAME, d.ID FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND s.NAME = ? AND s.TENANT_ID = ? AND d.NAME = ? AND d.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)) t " +
					" WHERE u.RSS_INSTANCE_ID = t.RSS_INSTANCE_ID AND TENANT_ID = ?) p WHERE e.DATABASE_USER_ID = p.DATABASE_USER_ID AND e.DATABASE_ID = p.DATABASE_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            stmt.setString(3, databaseName);
            stmt.setInt(4, tenantId);
            stmt.setString(5, environmentName);
            stmt.setInt(6, tenantId);
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

    
    public DatabaseUser[] getAvailableDatabaseUsers(String environmentName, String rssInstanceName,
                                                    String databaseName,
                                                    int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }

    
    public void removeDatabasePrivileges(String environmentName, int rssInsanceId,
                                         String username, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            /* delete permissions first */
            String sql = "DELETE FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID IN (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE RSS_INSTANCE_ID = ? AND USERNAME = ? AND TENANT_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInsanceId);
            stmt.setString(2, username);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while deleting user database "
                    + "privileges of the database user '" + username + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    
    public DatabasePrivilegeSet getUserDatabasePrivileges(String environmentName,
                                                          int rssInstanceId,
                                                          String databaseName, String username,
                                                          int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "SELECT * FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID = (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID = (SELECT ID FROM RM_DATABASE WHERE NAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?) AND DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE USERNAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, rssInstanceId);
            stmt.setInt(3, tenantId);
            stmt.setString(4, username);
            stmt.setInt(5, rssInstanceId);
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

    private String parameterized(String param, boolean withComma){
    	String end = "'";
    	if(withComma){
    		end = "',";
    	}
    	return "'"+param+end;
    }
    
    public void setUserDatabasePrivileges(String environmentName, int id,
                                          DatabasePrivilegeTemplate template,
                                          int tenantId) throws RSSDAOException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "INSERT INTO RM_USER_DATABASE_PRIVILEGE(USER_DATABASE_ENTRY_ID, SELECT_PRIV, INSERT_PRIV, UPDATE_PRIV, DELETE_PRIV, CREATE_PRIV, DROP_PRIV, GRANT_PRIV, REFERENCES_PRIV, INDEX_PRIV, ALTER_PRIV, CREATE_TMP_TABLE_PRIV, LOCK_TABLES_PRIV, CREATE_VIEW_PRIV, SHOW_VIEW_PRIV, CREATE_ROUTINE_PRIV, ALTER_ROUTINE_PRIV, EXECUTE_PRIV, EVENT_PRIV, TRIGGER_PRIV) "
            		+ " VALUES(?, "
					+ parameterized(privileges.getSelectPriv(), true)
					+ parameterized(privileges.getInsertPriv(), true)
					+ parameterized(privileges.getUpdatePriv(), true)
					+ parameterized(privileges.getDeletePriv(), true)
					+ parameterized(privileges.getCreatePriv(), true)
					+ parameterized(privileges.getDropPriv(), true)
					+ parameterized(privileges.getGrantPriv(), true)
					+ parameterized(privileges.getReferencesPriv(), true)
					+ parameterized(privileges.getIndexPriv(), true)
					+ parameterized(privileges.getAlterPriv(), true)
					+ parameterized(privileges.getCreateTmpTablePriv(), true)
					+ parameterized(privileges.getLockTablesPriv(), true)
					+ parameterized(privileges.getCreateViewPriv(), true)
					+ parameterized(privileges.getShowViewPriv(), true)
					+ parameterized(privileges.getCreateRoutinePriv(), true)
					+ parameterized(privileges.getAlterRoutinePriv(), true)
					+ parameterized(privileges.getExecutePriv(), true)
					+ parameterized(privileges.getEventPriv(), true)
					+ parameterized(privileges.getTriggerPriv(), false) + ")";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while setting user database " +
                    "privileges: " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    private DatabaseUser createDatabaseUserFromRS(ResultSet rs)
            throws SQLException {
        int id = rs.getInt("ID");
        String username = rs.getString("USERNAME");
        String rssInstName = rs.getString("RSS_INSTANCE_NAME");
        String type = rs.getString("TYPE");
        return new DatabaseUser(id, username, null, rssInstName, type);
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
    
    public void deleteUserDatabasePrivilegeEntriesByDatabase(
            RSSInstance rssInstance, String dbName, int tenantId)
            throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
		PreparedStatement delStmt = null;
		ResultSet rs;
        try {
            /* delete permissions first */
        	conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID IN (SELECT ID FROM RM_DATABASE WHERE RSS_INSTANCE_ID = ? AND NAME = ? AND TENANT_ID = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstance.getId());
            stmt.setString(2, dbName);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

			 Set<Integer> dbIds = new HashSet<Integer>();
            while (rs != null && rs.next()) {
            	Integer databaseId = rs.getInt("ID");
            	dbIds.add(databaseId);
            }
            
            sql = "DELETE FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID = ?";
            delStmt = conn.prepareStatement(sql);
            
            final int batchSize = 500;
            int count = 0;
            
            for(Integer databaseId : dbIds){
            	delStmt.setInt(1, databaseId);
            	delStmt.addBatch();
            	if(++count % batchSize == 0) {
            		delStmt.executeBatch();
            		count = 0;
                }
            }

            delStmt.executeBatch(); // insert remaining records
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while deleting user database "
                            + "privileges assigned to the database '"
                            + dbName + "' : " + e.getMessage(), e);
        } finally {
        	RSSDAOUtil.cleanupResources(null, stmt, conn);
        	RSSDAOUtil.cleanupResources(null, delStmt, null);
        }
    }
    
}
