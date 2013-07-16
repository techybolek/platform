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
import org.wso2.carbon.rssmanager.core.dao.DatabaseDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDAOImpl implements DatabaseDAO {

    
    public void addDatabase(Database database, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "INSERT INTO RM_DATABASE SET NAME = ?, RSS_INSTANCE_ID = (SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ?), TENANT_ID = ?, TYPE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, database.getName());
            stmt.setString(2, database.getRssInstanceName());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, tenantId);
            stmt.setString(5, database.getType());
            stmt.executeUpdate();
            // this.setDatabaseInstanceProperties(conn, database);
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while creating the database " +
                    database.getName() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    
    public void removeDatabase(String rssInstanceName, String databaseName,
                               int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "DELETE FROM RM_DATABASE WHERE NAME = ? AND TENANT_ID = ? AND RSS_INSTANCE_ID = (SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, rssInstanceName);
            stmt.setInt(4, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while dropping the database '" +
                    databaseName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    
    public boolean isDatabaseExist(String rssInstanceName, String databaseName,
                                   int tenantId) throws RSSDAOException {
        Connection conn = RSSDAO.createConnection();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        boolean isExist = false;
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
            String sql = "SELECT d.ID AS DATABASE_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND d.TYPE = ? AND d.TENANT_ID = ? AND d.NAME = ?";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(2, RSSDAOUtil.getTenantId());
                stmt.setString(3, databaseName);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    int databaseId = rs.getInt("DATABASE_ID");
                    if (databaseId > 0) {
                        isExist = true;
                    }
                }
                return isExist;
            } catch (SQLException e) {
                throw new RSSDAOException("Error occurred while retrieving the RSS instance " +
                        "to which the database '" + databaseName + "' belongs to : " +
                        e.getMessage(), e);
            } catch (RSSManagerCommonException e) {
                throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                        "current tenant", e);
            } finally {
                cleanupResources(rs, stmt, conn);
            }
        } else {
            String sql = "SELECT d.ID AS DATABASE_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND s.NAME = ? AND d.TYPE = ? AND d.TENANT_ID = ? AND d.NAME = ?";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, rssInstanceName);
                stmt.setString(2, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(3, RSSDAOUtil.getTenantId());
                stmt.setString(4, databaseName);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    int databaseId = rs.getInt("DATABASE_ID");
                    if (databaseId > 0) {
                        isExist = true;
                    }
                }
                return isExist;
            } catch (SQLException e) {
                throw new RSSDAOException("Error occurred while retrieving the RSS instance " +
                        "to which the database '" + databaseName + "' belongs to : " +
                        e.getMessage(), e);
            } catch (RSSManagerCommonException e) {
                throw new RSSDAOException("Error occurred while retrieving tenant id of the " +
                        "current tenant", e);
            } finally {
                cleanupResources(rs, stmt, conn);
            }
        }
    }

    
    public Database getDatabase(String rssInstanceName, String databaseName,
                                int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Database database = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT d.ID AS DATABASE_ID, d.NAME, d.TENANT_ID, s.NAME AS RSS_INSTANCE_NAME, s.SERVER_URL, s.TENANT_ID AS RSS_INSTANCE_TENANT_ID, d.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND d.NAME = ? AND d.TENANT_ID = ? AND s.NAME = ? AND s.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, rssInstanceName);
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                database = createDatabaseFromRS(rs);
            }
            return database;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving the configuration of " +
                    "database '" + databaseName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    
    public Database[] getDatabases(int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT d.ID AS DATABASE_ID, d.NAME, d.TENANT_ID, s.NAME AS RSS_INSTANCE_NAME, s.SERVER_URL, s.TENANT_ID AS RSS_INSTANCE_TENANT_ID, d.TYPE  FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            List<Database> result = new ArrayList<Database>();
            while (rs.next()) {
                Database entry = this.createDatabaseFromRS(rs);
                if (entry != null) {
                    result.add(entry);
                }
            }
            return result.toArray(new Database[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving all databases : " +
                    e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    
    public void incrementSystemRSSDatabaseCount() throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            String sql = "SELECT * FROM RM_SYSTEM_DATABASE_COUNT";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                sql = "INSERT INTO RM_SYSTEM_DATABASE_COUNT (COUNT) VALUES (0)";
                stmt = conn.prepareStatement(sql);
                stmt.executeUpdate();
            }
            sql = "UPDATE RM_SYSTEM_DATABASE_COUNT SET COUNT = COUNT + 1";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while incrementing system RSS " +
                    "database count : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }
    
    public int getSystemRSSDatabaseCount() throws RSSDAOException {
        int count = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT COUNT FROM RM_SYSTEM_DATABASE_COUNT";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving system RSS database " +
                    "count : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    
    public RSSInstance resolveRSSInstance(String rssInstanceName, String databaseName,
                                          int tenantId) throws RSSDAOException {
        return null;
    }

    private Database createDatabaseFromRS(ResultSet rs) throws SQLException,
            RSSDAOException {
        int id = rs.getInt("DATABASE_ID");
        String dbName = rs.getString("NAME");
        int dbTenantId = rs.getInt("TENANT_ID");
        String rssName = rs.getString("RSS_INSTANCE_NAME");
        String rssServerUrl = rs.getString("SERVER_URL");
        int rssTenantId = rs.getInt("RSS_INSTANCE_TENANT_ID");
        String type = rs.getString("TYPE");

        if (rssTenantId == MultitenantConstants.SUPER_TENANT_ID
                && dbTenantId != MultitenantConstants.SUPER_TENANT_ID) {
            rssName = RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE;
        }
        String url = rssServerUrl + "/" + dbName;
        return new Database(id, dbName, rssName, url, type);
    }

    private void cleanupResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
    }
    
}
