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
import org.wso2.carbon.rssmanager.core.config.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RSSInstanceDAOImpl implements RSSInstanceDAO {

    
    public void addRSSInstance(RSSInstance rssInstance, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "INSERT INTO RM_SERVER_INSTANCE (NAME, SERVER_URL, DBMS_TYPE, INSTANCE_TYPE, SERVER_CATEGORY, ADMIN_USERNAME, ADMIN_PASSWORD, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getName());
            stmt.setString(2, rssInstance.getDataSourceConfig().getUrl());
            stmt.setString(3, rssInstance.getDbmsType());
            stmt.setString(4, rssInstance.getInstanceType());
            stmt.setString(5, rssInstance.getServerCategory());
            stmt.setString(6, rssInstance.getDataSourceConfig().getUsername());
            stmt.setString(7, rssInstance.getDataSourceConfig().getPassword());
            stmt.setInt(8, tenantId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while creating the RSS instance '"
                            + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    
    public void removeRSSInstance(String name, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        RSSInstance rssInstance = getRSSInstance(name, tenantId);
        try {
            conn = RSSDAO.createConnection();
//            List<DatabaseUser> users = getDatabaseUsersByRSSInstance(conn, rssInstance);
//            if (users.size() > 0) {
//                for (DatabaseUser user : users) {
//                    dropDatabaseUser(rssInstance, user.getUsername(), tenantId);
//                }
//            }
            String sql = "DELETE FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while dropping the RSS instance '"
                            + name + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    
    public void updateRSSInstance(RSSInstance rssInstance, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "UPDATE RM_SERVER_INSTANCE SET SERVER_URL = ?, DBMS_TYPE = ?, INSTANCE_TYPE = ?, SERVER_CATEGORY = ?, ADMIN_USERNAME = ?, ADMIN_PASSWORD = ? WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getDataSourceConfig().getUrl());
            stmt.setString(2, rssInstance.getDbmsType());
            stmt.setString(3, rssInstance.getInstanceType());
            stmt.setString(4, rssInstance.getServerCategory());
            stmt.setString(5, rssInstance.getDataSourceConfig().getUsername());
            stmt.setString(6, rssInstance.getDataSourceConfig().getPassword());
            stmt.setString(7, rssInstance.getName());
            stmt.setInt(8, tenantId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while editing the RSS instance '"
                            + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    
    public RSSInstance getRSSInstance(String name, int tenantId) throws RSSDAOException {
        RSSInstance rssInstance = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT * FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                rssInstance = createRSSInstanceFromRS(rs);
            }
            return rssInstance;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving the configuration of "
                    + "RSS instance '" + name + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    
    public RSSInstance[] getSystemRSSInstances(int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            stmt = conn.prepareStatement("SELECT * FROM RM_SERVER_INSTANCE WHERE INSTANCE_TYPE = ? AND TENANT_ID = ?");
            stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            return result.toArray(new RSSInstance[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while retrieving system RSS "
                            + "instances : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    
    public RSSInstance[] getRSSInstances(int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT * FROM RM_SERVER_INSTANCE WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            return result.toArray(new RSSInstance[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while retrieving all RSS instances : "
                            + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    private RSSInstance createRSSInstanceFromRS(ResultSet rs)
            throws SQLException {
        int id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String serverURL = rs.getString("SERVER_URL");
        String instanceType = rs.getString("INSTANCE_TYPE");
        String serverCategory = rs.getString("SERVER_CATEGORY");
        String adminUsername = rs.getString("ADMIN_USERNAME");
        String adminPassword = rs.getString("ADMIN_PASSWORD");
        String dbmsType = rs.getString("DBMS_TYPE");
        
        RDBMSConfiguration config = new RDBMSConfiguration();
        config.setUrl(serverURL);
        config.setUsername(adminUsername);
        config.setPassword(adminPassword);
        return new RSSInstance(id, name, dbmsType, instanceType, serverCategory, config);
    }
    
}
