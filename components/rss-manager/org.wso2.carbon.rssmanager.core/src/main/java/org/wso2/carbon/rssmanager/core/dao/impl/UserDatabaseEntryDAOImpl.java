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

import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.UserDatabaseEntryDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.entity.UserDatabaseEntry;

import java.sql.*;

public class UserDatabaseEntryDAOImpl implements UserDatabaseEntryDAO {

	private EntityManager entityManager;

	public UserDatabaseEntryDAOImpl(EntityManager entityManager){
		this.entityManager = entityManager;
	}

    public int addUserDatabaseEntry(String environmentName, UserDatabaseEntry entry,
                                    int tenantId) throws RSSDAOException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = entityManager.createConnection(RSSDAO.getDataSource());

            String sql = "INSERT INTO RM_USER_DATABASE_ENTRY(DATABASE_USER_ID, DATABASE_ID) VALUES(?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, entry.getUserId());
            stmt.setInt(2, entry.getDatabaseId());
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSDAOException("Failed to attach database user '" + entry.getUsername() +
                        "' was not attached to the database '" + entry.getDatabaseName() + "'");
            }
            int key = 0;
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            return key;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while adding new user-database-entry : " +
                    e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    public void removeUserDatabaseEntry(String environmentName, int rssInstanceId, String username,
                                        String type, int tenantId) throws RSSDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            /* now delete the user-database-entry */
            conn = entityManager.createConnection(RSSDAO.getDataSource());

            final int databaseUserId =
                    this.getDatabaseUserId(conn, rssInstanceId, username, type, tenantId);

            String sql = "DELETE FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_USER_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, databaseUserId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while deleting user-database-entry : " +
                    e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }

    }

    public void removeUserDatabaseEntriesByDatabase(String environmentName, int rssInstanceId,
                                                    String databaseName, String type,
                                                    int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = entityManager.createConnection(RSSDAO.getDataSource());
            final int databaseId =
                    this.getDatabaseId(conn, rssInstanceId, databaseName, type, tenantId);

            String sql = "DELETE FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, databaseId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while removing the user database "
                    + "entries: " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    @Override
    public void removeUserDatabaseEntriesByUser(String environmentName, int rssInstanceId,
                                                String username, String type,
                                                int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = entityManager.createConnection(RSSDAO.getDataSource());
            final int userId =
                    this.getDatabaseUserId(conn, rssInstanceId, username, type, tenantId);

            String sql = "DELETE FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_USER_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while removing the user database "
                    + "entries: " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    public UserDatabaseEntry getUserDatabaseEntry(String environmentName, UserDatabaseEntry entry,
                                                  int tenantId) throws RSSDAOException {
        return null;
    }


    public UserDatabaseEntry[] getUserDatabaseEntries(String environmentName,
                                                      UserDatabaseEntry entries,
                                                      int tenantId) throws RSSDAOException {
        return new UserDatabaseEntry[0];
    }

    private int getDatabaseUserId(Connection conn, int rssInstanceId, String username, String type,
                                  int tenantId) throws SQLException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int userId = -1;
        try {
            String sql = "SELECT ID FROM RM_DATABASE_USER WHERE RSS_INSTANCE_ID = ? AND USERNAME = ? AND TYPE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstanceId);
            stmt.setString(2, username);
            stmt.setString(3, type);
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("ID");
            }
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, null);
        }
        return userId;
    }

    private int getDatabaseId(Connection conn, int rssInstanceId, String databaseName, String type,
                              int tenantId) throws SQLException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int databaseId = -1;
        try {
            String sql = "SELECT ID FROM RM_DATABASE WHERE RSS_INSTANCE_ID = ? AND NAME = ? AND TYPE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstanceId);
            stmt.setString(2, databaseName);
            stmt.setString(3, type);
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                databaseId = rs.getInt("ID");
            }
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, null);
        }
        return databaseId;
    }

}
