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
package org.wso2.carbon.rssmanager.core.internal.dao;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.config.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.*;
import java.util.*;


/**
 * DAO implementation for RSSDAO interface.
 */
public class RSSDAOImpl implements RSSDAO {

    @Override
    public RSSEnvironment createRSSEnvironment(
            RSSEnvironment rssEnvironment, int tenantId) throws RSSManagerException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "INSERT INTO RM_ENVIRONMENT (NAME, TENANT_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, rssEnvironment.getName());
            stmt.setInt(2, tenantId);
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSManagerException("Failed to add metada related to RSS Environment'" +
                        rssEnvironment.getName() + "'");
            }
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                rssEnvironment.setId(rs.getInt(1));
            }
            return rssEnvironment;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating metadata related to  RSS " +
                    "environment '" + rssEnvironment.getName() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    @Override
    public void dropRSSEnvironment(String rssEnvironmentName,
                                   int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "DELETE FROM RM_ENVIRONMENT WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssEnvironmentName);
            stmt.setInt(8, tenantId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while deleting metadata related to RSS " +
                    "environment '" + rssEnvironmentName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void updateRSSEnvironment(RSSEnvironment rssEnvironment,
                                     int tenantId) throws RSSManagerException {

    }

    @Override
    public boolean isEnvironmentExists(String environmentName,
                                       int tenantId) throws RSSManagerException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT 1 AS IS_EXIST FROM RM_ENVIRONMENT WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            int i = 0;
            if (rs.next()) {
                i = rs.getInt("IS_EXIST");
            }
            return (i == 1);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while checking the existance of RSS " +
                    "environment '" + environmentName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void createRSSInstance(String environmentName, RSSInstance rssInstance, int tenantId)
            throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "INSERT INTO RM_SERVER_INSTANCE SET NAME = ?, SERVER_URL = ?, DBMS_TYPE = ?, INSTANCE_TYPE = ?, SERVER_CATEGORY = ?, ADMIN_USERNAME = ?, ADMIN_PASSWORD = ?, TENANT_ID = ?, ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getName());
            stmt.setString(2, rssInstance.getDataSourceConfig().getUrl());
            stmt.setString(3, rssInstance.getDbmsType());
            stmt.setString(4, rssInstance.getInstanceType());
            stmt.setString(5, rssInstance.getServerCategory());
            stmt.setString(6, rssInstance.getDataSourceConfig().getUsername());
            stmt.setString(7, rssInstance.getDataSourceConfig().getPassword());
            stmt.setInt(8, tenantId);
            stmt.setString(9, environmentName);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating the RSS instance '" +
                    rssInstance.getName() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public boolean isDatabaseExist(String environmentName, String rssInstanceName,
                                   String databaseName, int tenantId)
            throws RSSManagerException {
        boolean isExist = false;
        Connection conn = RSSDAOFactory.getConnection();
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String sql = "SELECT d.ID AS DATABASE_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND d.TYPE = ? AND d.TENANT_ID = ? AND d.NAME = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(2, tenantId);
                stmt.setString(3, databaseName);
                stmt.setString(4, environmentName);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    int databaseId = rs.getInt("DATABASE_ID");
                    if (databaseId > 0) {
                        isExist = true;
                    }
                }
                return isExist;
            } catch (SQLException e) {
                throw new RSSManagerException("Error occurred while retrieving the RSS instance " +
                        "to which the database '" + databaseName + "' belongs to : " +
                        e.getMessage(), e);
            } finally {
                cleanupResources(rs, stmt, conn);
            }
        } else {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String sql = "SELECT d.ID AS DATABASE_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND s.NAME = ? AND d.TYPE = ? AND d.TENANT_ID = ? AND d.NAME = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, rssInstanceName);
                stmt.setString(2, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(3, tenantId);
                stmt.setString(4, databaseName);
                stmt.setString(5, environmentName);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    int databaseId = rs.getInt("DATABASE_ID");
                    if (databaseId > 0) {
                        isExist = true;
                    }
                }
                return isExist;
            } catch (SQLException e) {
                throw new RSSManagerException("Error occurred while retrieving the RSS instance " +
                        "to which the database '" + databaseName + "' belongs to : " +
                        e.getMessage(), e);
            } finally {
                cleanupResources(rs, stmt, conn);
            }
        }
    }

    public boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                       String databaseUsername,
                                       int tenantId) throws RSSManagerException {
        boolean isExist = false;
        Connection conn = RSSDAOFactory.getConnection();
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
            String sql = "SELECT u.ID AS DATABASE_USER_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TYPE = ? AND u.TENANT_ID = ? AND u.USERNAME = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(2, tenantId);
                stmt.setString(3, databaseUsername);
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
                throw new RSSManagerException("Error occurred while checking the existence of " +
                        "the database user '" + databaseUsername + "' : " + e.getMessage(), e);
            } finally {
                cleanupResources(rs, stmt, conn);
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
                stmt.setString(4, databaseUsername);
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
                throw new RSSManagerException("Error occurred while checking the existence of " +
                        "the database user '" + databaseUsername + "' : " + e.getMessage(), e);
            } finally {
                cleanupResources(rs, stmt, conn);
            }
        }
    }

    public boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName,
                                                    int tenantId) throws RSSManagerException {
        Connection conn = null;
        boolean isExist = false;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int templateId = rs.getInt("ID");
                if (templateId > 0) {
                    isExist = true;
                }
            }
            return isExist;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while checking the existence " +
                    "of database privilege template '" + templateName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void updateRSSInstance(String environmentName, RSSInstance rssInstance,
                                  int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "UPDATE RM_SERVER_INSTANCE SET SERVER_URL = ?, DBMS_TYPE = ?, INSTANCE_TYPE = ?, SERVER_CATEGORY = ?, ADMIN_USERNAME = ?, ADMIN_PASSWORD = ? WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getDataSourceConfig().getUrl());
            stmt.setString(2, rssInstance.getDbmsType());
            stmt.setString(3, rssInstance.getInstanceType());
            stmt.setString(4, rssInstance.getServerCategory());
            stmt.setString(5, rssInstance.getDataSourceConfig().getUsername());
            stmt.setString(6, rssInstance.getDataSourceConfig().getPassword());
            stmt.setString(7, rssInstance.getName());
            stmt.setInt(8, tenantId);
            stmt.setString(9, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while editing the RSS instance '" +
                    rssInstance.getName() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public RSSInstance getRSSInstance(String environmentName, String rssInstanceName,
                                      int tenantId) throws RSSManagerException {
        RSSInstance rssInstance = null;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT * FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                rssInstance = this.createRSSInstanceFromRS(rs);
            }
            return rssInstance;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the configuration of " +
                    "RSS instance '" + rssInstanceName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    @Override
    public RSSInstance getSystemRSSInstance(String environmentName, String rssInstanceName) throws RSSManagerException {
        RSSInstance rssInstance = null;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT * FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                rssInstance = this.createRSSInstanceFromRS(rs);
            }
            return rssInstance;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the configuration of " +
                    "RSS instance '" + rssInstanceName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public DatabaseUser getDatabaseUser(String environmentName, RSSInstance rssInstance,
                                        String username, int tenantId) throws RSSManagerException {
        DatabaseUser user = new DatabaseUser();
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND s.NAME = ? AND s.TENANT_ID = ? AND u.USERNAME = ? AND u.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getName());
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
            throw new RSSManagerException("Error while occurred while retrieving information of "
                    + "the database user '" + user.getUsername() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void incrementSystemRSSDatabaseCount(String environmentName) throws RSSManagerException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            String sql = "SELECT * FROM RM_SYSTEM_DATABASE_COUNT WHERE ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                sql = "INSERT INTO RM_SYSTEM_DATABASE_COUNT SET ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?), COUNT = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, environmentName);
                stmt.setInt(2, 0);
                stmt.executeUpdate();
            }
            sql = "UPDATE RM_SYSTEM_DATABASE_COUNT SET COUNT = COUNT + 1";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while incrementing system RSS " +
                    "database count : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public RSSInstance[] getAllSystemRSSInstances(String environmentName) throws RSSManagerException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM RM_SERVER_INSTANCE WHERE INSTANCE_TYPE = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)");
            stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            return result.toArray(new RSSInstance[result.size()]);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving system RSS " +
                    "instances : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void dropRSSInstance(String environmentName, String rssInstanceName,
                                int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        RSSInstance rssInstance = this.getRSSInstance(environmentName, rssInstanceName, tenantId);
        try {
            conn = RSSDAOFactory.getConnection();
            List<DatabaseUser> users = this.getDatabaseUsersByRSSInstance(conn, environmentName,
                    rssInstance);
            if (users.size() > 0) {
                for (DatabaseUser user : users) {
                    this.dropDatabaseUser(environmentName, rssInstance, user.getUsername(),
                            tenantId);
                }
            }
            String sql = "DELETE FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstanceName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while dropping the RSS instance '" +
                    rssInstanceName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    private List<DatabaseUser> getDatabaseUsersByRSSInstance(
            Connection conn, String environmentName,
            RSSInstance rssInstance) throws RSSManagerException {
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        int tenantId = RSSManagerUtil.getTenantId();
        try {
            String sql = "SELECT u.USERNAME AS USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE AS TYPE AS TENANT_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_SERVER_INSTANCE AND u.TENANT_ID = ? AND s.NAME = ? AND s.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, rssInstance.getName());
            stmt.setInt(3, tenantId);
            stmt.setString(4, environmentName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(this.createDatabaseUserFromRS(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the database users " +
                    "in RSS instance '" + rssInstance.getName() + "'");
        }
        return users;
    }

    public Database[] getAllDatabases(String environmentName, int tenantId) throws RSSManagerException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT d.ID AS DATABASE_ID, d.NAME, d.TENANT_ID, s.NAME AS RSS_INSTANCE_NAME, s.SERVER_URL, s.TENANT_ID AS RSS_INSTANCE_TENANT_ID, d.TYPE  FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND d.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, environmentName);
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
            throw new RSSManagerException("Error occurred while retrieving all databases : " +
                    e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public Database getDatabase(String environmentName, RSSInstance rssInstance, String databaseName,
                                int tenantId) throws RSSManagerException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        Database database = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT d.ID AS DATABASE_ID, d.NAME, d.TENANT_ID, s.NAME AS RSS_INSTANCE_NAME, s.SERVER_URL, s.TENANT_ID AS RSS_INSTANCE_TENANT_ID, d.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND d.NAME = ? AND d.TENANT_ID = ? AND s.NAME = ? AND s.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, rssInstance.getName());
            stmt.setInt(4, MultitenantConstants.SUPER_TENANT_ID); //TODO : rssInstance.getTenantId()
            stmt.setString(5, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                database = this.createDatabaseFromRS(rs);
            }
            return database;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the configuration of " +
                    "database '" + databaseName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void createDatabase(String environmentName, Database database,
                               int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            int rssInstanceTenantId = (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE
                    .equals(database.getType())) ? MultitenantConstants.SUPER_TENANT_ID
                    : tenantId;

            String sql = "INSERT INTO RM_DATABASE SET NAME = ?, RSS_INSTANCE_ID = (SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)), TENANT_ID = ?, TYPE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, database.getName());
            stmt.setString(2, database.getRssInstanceName());
            stmt.setInt(3, rssInstanceTenantId);
            stmt.setString(4, environmentName);
            stmt.setInt(5, tenantId);
            stmt.setString(6, database.getType());
            stmt.executeUpdate();
            // this.setDatabaseInstanceProperties(conn, database);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating the database " +
                    database.getName() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public void dropDatabase(String environmentName, RSSInstance rssInstance, String databaseName,
                             int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "DELETE FROM RM_DATABASE WHERE NAME = ? AND TENANT_ID = ? AND RSS_INSTANCE_ID = (SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, rssInstance.getName());
            stmt.setInt(4, MultitenantConstants.SUPER_TENANT_ID); //TODO : rssInstance.getTenantId()
            stmt.setString(5, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while dropping the database '" +
                    databaseName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public void createDatabaseUser(String environmentName, RSSInstance rssInstance, DatabaseUser user, int tenantId)
            throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "INSERT INTO RM_DATABASE_USER SET USERNAME = ?, RSS_INSTANCE_ID = (SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)), TYPE = ?, TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, rssInstance.getName());
            stmt.setInt(3, MultitenantConstants.SUPER_TENANT_ID);
            stmt.setString(4, environmentName);
            stmt.setString(5, user.getType());
            stmt.setInt(6, tenantId);
            stmt.execute();
        } catch (Throwable e) {
            throw new RSSManagerException("Error occurred while creating the database user '" +
                    user.getUsername() + "' : " + e.getMessage());
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public void dropDatabaseUser(String environmentName, RSSInstance rssInstance, String username,
                                 int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "DELETE FROM RM_DATABASE_USER WHERE USERNAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, rssInstance.getId());
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while dropping the database user '" +
                    username + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public DatabaseUserMetaData[] getAllDatabaseUsers(String environmentName,
                                                      int tenantId) throws RSSManagerException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        List<DatabaseUserMetaData> users = new ArrayList<DatabaseUserMetaData>();
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, environmentName);
            rs = stmt.executeQuery();
            while (rs.next()) {
                DatabaseUser user = this.createDatabaseUserFromRS(rs);
                users.add(RSSManagerUtil.convertToDatabaseUserMetadata(user, tenantId));
            }
            return users.toArray(new DatabaseUserMetaData[users.size()]);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the database users : " +
                    e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public DatabaseUser[] getSystemCreatedDatabaseUsers(String environmentName) throws RSSManagerException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT u.USERNAME, u.TYPE, u.TENANT_ID, s.NAME AS RSS_INSTANCE_NAME FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TYPE = ? AND u.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            stmt.setInt(2, RSSManagerUtil.getTenantId());
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            while (rs.next()) {
                DatabaseUser user = this.createDatabaseUserFromRS(rs);
                users.add(user);
            }
            return users.toArray(new DatabaseUser[users.size()]);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the system created " +
                    "database user list : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    @Override
    public DatabaseUserMetaData[] getSystemUsersAssignedToDatabase(
            String environmentName, RSSInstance rssInstance,
            String databaseName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DatabaseUserMetaData> users = new ArrayList<DatabaseUserMetaData>();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT DISTINCT u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TYPE AS TYPE FROM RM_DATABASE_USER u, RM_USER_DATABASE_ENTRY e, RM_SERVER_INSTANCE s WHERE u.ID = e.DATABASE_USER_ID AND u.TYPE = ? AND u.TENANT_ID = ? AND s.ID = u.RSS_INSTANCE_ID AND  e.DATABASE_ID = (SELECT ID FROM RM_DATABASE WHERE RSS_INSTANCE_ID = ? AND NAME = ? AND TENANT_ID = ?) AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, rssInstance.getId());
            stmt.setString(4, databaseName);
            stmt.setInt(5, tenantId);
            stmt.setString(6, environmentName);
            rs = stmt.executeQuery();
            while (rs.next()) {
                DatabaseUser user = createDatabaseUserFromRSWithUsername(rs);
                users.add(RSSManagerUtil.convertToDatabaseUserMetadata(user, tenantId));
            }
            return users.toArray(new DatabaseUserMetaData[users.size()]);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the system created " +
                    "database user list : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    @Override
    public DatabasePrivilegeSet getSystemUserDatabasePrivileges(
            String environmentName, RSSInstance rssInstance, String databaseName,
            String username) throws RSSManagerException {
        DatabasePrivilegeSet privileges = null;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT * FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID = (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID = (SELECT ID FROM RM_DATABASE WHERE NAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?) AND DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE USERNAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, rssInstance.getId());
            stmt.setInt(3, tenantId);
            stmt.setString(4, username);
            stmt.setInt(5, rssInstance.getId());
            stmt.setInt(6, tenantId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                privileges = this.createUserDatabasePrivilegeSetFromRS(rs);
            }
            return privileges;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the database " +
                    "privileges assigned to the user '" + username + "' upon the database '" +
                    databaseName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public RSSInstance findRSSInstanceDatabaseBelongsTo(String environmentName,
                                                        String rssInstanceName, String databaseName,
                                                        int tenantId) throws RSSManagerException {
        RSSInstance rssInstance = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
            Connection conn = RSSDAOFactory.getConnection();
            String sql = "SELECT s.ID, s.NAME, s.SERVER_URL, s.DBMS_TYPE, s.INSTANCE_TYPE, s.SERVER_CATEGORY, s.TENANT_ID, s.ADMIN_USERNAME, s.ADMIN_PASSWORD FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND d.TYPE = ? AND d.TENANT_ID = ? AND d.NAME = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(2, tenantId);
                stmt.setString(3, databaseName);
                stmt.setString(4, environmentName);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    rssInstance = this.createRSSInstanceFromRS(rs);
                }
            } catch (SQLException e) {
                throw new RSSManagerException("Error occurred while retrieving the RSS instance " +
                        "to which the database '" + databaseName + "' belongs to : " +
                        e.getMessage(), e);
            } finally {
                cleanupResources(rs, stmt, conn);
            }
        } else {
            rssInstance = this.getRSSInstance(environmentName, rssInstanceName, tenantId);
        }
        return rssInstance;
    }

    public RSSInstance findRSSInstanceDatabaseUserBelongsTo(
            String environmentName, String rssInstanceName, String username,
            int tenantId) throws RSSManagerException {
        RSSInstance rssInstance = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
            Connection conn = RSSDAOFactory.getConnection();
            String sql = "SELECT s.ID, s.NAME, s.SERVER_URL, s.DBMS_TYPE, s.INSTANCE_TYPE, s.SERVER_CATEGORY, s.ADMIN_USERNAME, s.ADMIN_PASSWORD, s.TENANT_ID FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND u.TYPE = ? AND u.TENANT_ID = ? AND u.USERNAME = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                stmt.setInt(2, tenantId);
                stmt.setString(3, username);
                stmt.setString(4, environmentName);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    rssInstance = this.createRSSInstanceFromRS(rs);
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                throw new RSSManagerException("Error occurred while retrieving the RSS instance " +
                        "to which the database user '" + username + "'belongs to : " +
                        e.getMessage(), e);
            } finally {
                cleanupResources(rs, stmt, conn);
            }
        } else {
            rssInstance = this.getRSSInstance(environmentName, rssInstanceName, tenantId);
        }
        return rssInstance;
    }

    public DatabaseUser[] getUsersByRSSInstance(String environmentName, RSSInstance rssInstance,
                                                int tenantId) throws RSSManagerException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT u.USERNAME, s.NAME AS RSS_INSTANCE_NAME, u.TENANT_ID, u.TYPE FROM RM_SERVER_INSTANCE s, RM_DATABASE_USER u WHERE s.ID = u.RSS_INSTANCE_ID AND s.NAME = ? AND s.TENANT_ID = ? AND u.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getName());
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID); //TODO : rssInstance.getTenantId()
            stmt.setInt(3, tenantId);
            stmt.setString(4, environmentName);
            rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(this.createDatabaseUserFromRS(rs));
            }
            return users.toArray(new DatabaseUser[users.size()]);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the database users : " +
                    e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public DatabaseUserMetaData[] getUsersAssignedToDatabase(
            String environmentName, RSSInstance rssInstance, String databaseName,
            int tenantId) throws RSSManagerException {
        List<DatabaseUserMetaData> attachedUsers = new ArrayList<DatabaseUserMetaData>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT p.USERNAME FROM RM_USER_DATABASE_ENTRY e, (SELECT t.ID AS DATABASE_ID, u.ID AS DATABASE_USER_ID, u.USERNAME FROM RM_DATABASE_USER u,(SELECT d.RSS_INSTANCE_ID, d.NAME, d.ID FROM RM_SERVER_INSTANCE s, RM_DATABASE d WHERE s.ID = d.RSS_INSTANCE_ID AND s.NAME = ? AND s.TENANT_ID = ? AND d.NAME = ? AND d.TENANT_ID = ? AND s.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)) t WHERE u.RSS_INSTANCE_ID = t.RSS_INSTANCE_ID AND TENANT_ID = ?) p WHERE e.DATABASE_USER_ID = p.DATABASE_USER_ID AND e.DATABASE_ID = p.DATABASE_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getName());
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID); //TODO : rssInstance.getTenantId()
            stmt.setString(3, databaseName);
            stmt.setInt(4, tenantId);
            stmt.setString(5, environmentName);
            stmt.setInt(6, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                DatabaseUser user = createDatabaseUserFromRSWithUsername(rs);
                attachedUsers.add(RSSManagerUtil.convertToDatabaseUserMetadata(user, tenantId));
            }
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving the users assigned " +
                    "to the database '" + databaseName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
        return attachedUsers.toArray(new DatabaseUserMetaData[attachedUsers.size()]);
    }

    public UserDatabaseEntry createUserDatabaseEntry(String environmentName, RSSInstance rssInstance,
                                                     Database database, String username,
                                                     int tenantId) throws RSSManagerException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        UserDatabaseEntry ude = new UserDatabaseEntry(-1, -1, username,
                database.getId(), database.getName(), rssInstance.getId(),
                rssInstance.getName());
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "INSERT INTO RM_USER_DATABASE_ENTRY SET DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE RSS_INSTANCE_ID = ? AND TENANT_ID = ? AND USERNAME = ?), DATABASE_ID = ?";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, rssInstance.getId());
            stmt.setInt(2, tenantId);
            stmt.setString(3, username);
            stmt.setInt(4, database.getId());
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSManagerException("Failed to attach database user '" + username +
                        "' was not attached to the database '" + database.getName() + "'");
            }
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                ude.setId(rs.getInt(1));
            }
            return ude;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while adding new user-database-entry : " +
                    e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void deleteUserDatabaseEntry(String environmentName, RSSInstance rssInstance,
                                        String username, int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            /* now delete the user-database-entry */
            conn = RSSDAOFactory.getConnection();
            String sql = "DELETE FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE RSS_INSTANCE_ID = ? AND USERNAME = ? AND TENANT_ID = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstance.getId());
            stmt.setString(2, username);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while deleting user-database-entry : " +
                    e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public void deleteUserDatabasePrivileges(String environmentName, RSSInstance rssInstance,
                                             String username, int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            /* delete permissions first */
            String sql = "DELETE FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID IN (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE RSS_INSTANCE_ID = ? AND USERNAME = ? AND TENANT_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstance.getId());
            stmt.setString(2, username);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while deleting user database "
                    + "privileges of the database user '" + username + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public int getSystemRSSDatabaseCount(String environmentName) throws RSSManagerException {
        int count = 0;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT COUNT FROM RM_SYSTEM_DATABASE_COUNT WHERE ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving system RSS database " +
                    "count : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(
            String environmentName, RSSInstance rssInstance, String databaseName, String username,
            int tenantId) throws RSSManagerException {
        Connection conn = null;
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT * FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID = (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID = (SELECT ID FROM RM_DATABASE WHERE NAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?) AND DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE USERNAME = ? AND RSS_INSTANCE_ID = ? AND TENANT_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, rssInstance.getId());
            stmt.setInt(3, tenantId);
            stmt.setString(4, username);
            stmt.setInt(5, MultitenantConstants.SUPER_TENANT_ID); //TODO : rssInstance.getTenantId()
            stmt.setInt(6, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                privileges = this.createUserDatabasePrivilegeSetFromRS(rs);
            }
            return privileges;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while retrieving user permissions " +
                    "granted for the database user '" + username + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }

    }

    public RSSInstance[] getAllRSSInstances(String environmentName,
                                            int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT * FROM RM_SERVER_INSTANCE WHERE TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, environmentName);
            rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            return result.toArray(new RSSInstance[result.size()]);
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while retrieving all RSS instances : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void removeUserDatabaseEntriesByDatabase(String environmentName, RSSInstance rssInstance,
                                                    String databaseName, int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT DISTINCT d.ID FROM RM_USER_DATABASE_ENTRY e, RM_DATABASE d WHERE d.ID = e.DATABASE_ID AND d.NAME = ? AND d.RSS_INSTANCE_ID = ? AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, rssInstance.getId());
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            int databaseId = -1;
            if (rs.next()) {
                databaseId = rs.getInt("ID");
            }
            rs.close();
            stmt.close();

            sql = "DELETE FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, databaseId);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while removing the user database "
                            + "entries : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void removeUserDatabaseEntriesByDatabaseUser(String environmentName,
                                                        RSSInstance rssInstance, String username,
                                                        int tenantId) throws RSSManagerException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "SELECT DISTINCT u.ID FROM RM_USER_DATABASE_ENTRY e, RM_DATABASE_USER u WHERE u.ID = e.DATABASE_USER_ID AND u.USERNAME = ? AND u.RSS_INSTANCE_ID = ? AND u.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, rssInstance.getId());
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            int databaseId = -1;
            if (rs.next()) {
                databaseId = rs.getInt("ID");
            }

            sql = "DELETE FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_USER_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, databaseId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while removing the user database "
                            + "entries : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void deleteUserDatabasePrivilegeEntriesByDatabaseUser(
            String environmentName, RSSInstance rssInstance, String username, int tenantId)
            throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            /* delete permissions first */
            conn = RSSDAOFactory.getConnection();
            String sql = "DELETE FROM RM_USER_DATABASE_PRIVILEGE WHERE USER_DATABASE_ENTRY_ID IN (SELECT ID FROM RM_USER_DATABASE_ENTRY WHERE DATABASE_USER_ID = (SELECT ID FROM RM_DATABASE_USER WHERE RSS_INSTANCE_ID = ? AND USERNAME = ? AND TENANT_ID = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstance.getId());
            stmt.setString(2, username);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while deleting user database "
                            + "privileges assigned to the database user '"
                            + username + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

	    public void deleteUserDatabasePrivilegeEntriesByDatabase(
            RSSInstance rssInstance, String dbName, int tenantId)
            throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
		PreparedStatement delStmt = null;
		ResultSet rs = null;
        try {
            /* delete permissions first */
            conn = RSSDAOFactory.getConnection();
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
            throw new RSSManagerException(
                    "Error occurred while deleting user database "
                            + "privileges assigned to the database '"
                            + dbName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
			cleanupResources(null, delStmt, null);
        }
    }

    public void updateDatabaseUser(String environmentName, DatabasePrivilegeSet privileges,
                                   RSSInstance rssInstance, DatabaseUser user, String databaseName)
            throws RSSManagerException {
        int tenantId = RSSManagerUtil.getTenantId();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
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
            stmt.setString(23, user.getUsername());
            stmt.setInt(24, tenantId);
            stmt.setInt(25, rssInstance.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while updating database privileges "
                            + "of the user '" + user.getUsername() + "' : "
                            + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public DatabasePrivilegeTemplate createDatabasePrivilegesTemplate(
            String environmentName, DatabasePrivilegeTemplate template,
            int tenantId) throws RSSManagerException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE SET NAME = ?, TENANT_ID = ?, ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, template.getName());
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSManagerException(
                        "Database privilege was not created");
            }
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                template.setId(rs.getInt(1));
            }
            return template;
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while creating database privilege "
                            + "template '" + template.getName() + "' : "
                            + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    public void setDatabasePrivilegeTemplateProperties(
            String environmentName, DatabasePrivilegeTemplate template,
            int tenantId) throws RSSManagerException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE_ENTRY(TEMPLATE_ID, SELECT_PRIV, INSERT_PRIV, UPDATE_PRIV, DELETE_PRIV, CREATE_PRIV, DROP_PRIV, GRANT_PRIV, REFERENCES_PRIV, INDEX_PRIV, ALTER_PRIV, CREATE_TMP_TABLE_PRIV, LOCK_TABLES_PRIV, CREATE_VIEW_PRIV, SHOW_VIEW_PRIV, CREATE_ROUTINE_PRIV, ALTER_ROUTINE_PRIV, EXECUTE_PRIV, EVENT_PRIV, TRIGGER_PRIV) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, template.getId());
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
            throw new RSSManagerException(
                    "Error occurred setting database privilege template "
                            + "properties : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    public void setUserDatabasePrivileges(String environmentName, UserDatabaseEntry entry,
                                          DatabasePrivilegeTemplate template,
                                          int tenantId) throws RSSManagerException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "INSERT INTO RM_USER_DATABASE_PRIVILEGE(USER_DATABASE_ENTRY_ID, SELECT_PRIV, INSERT_PRIV, UPDATE_PRIV, DELETE_PRIV, CREATE_PRIV, DROP_PRIV, GRANT_PRIV, REFERENCES_PRIV, INDEX_PRIV, ALTER_PRIV, CREATE_TMP_TABLE_PRIV, LOCK_TABLES_PRIV, CREATE_VIEW_PRIV, SHOW_VIEW_PRIV, CREATE_ROUTINE_PRIV, ALTER_ROUTINE_PRIV, EXECUTE_PRIV, EVENT_PRIV, TRIGGER_PRIV) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, entry.getId());
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
            throw new RSSManagerException(
                    "Error occurred while setting user database "
                            + "privileges for the database user '"
                            + entry.getUsername() + "' on database '"
                            + entry.getDatabaseName() + "' : " + e.getMessage(),
                    e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    /**
     * Drops a database privilege template carriying the given name which
     * belongs to the currently logged in tenant.
     *
     * @param templateName Name of the database privilege template to be deleted
     * @throws RSSManagerException Is thrown in case of an unexpected error such as database
     *                             access failure, etc.
     */
    public void dropDatabasePrivilegesTemplate(String environmentName, String templateName,
                                               int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while dropping the database privilege "
                            + "template '" + templateName + "' : "
                            + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    /**
     * Remotes the permission entries assigned to a particular database
     * privilege template.
     *
     * @param templateName Name of the database template associated with the permissions
     * @param tenantId     Id of the currently logged in tenant
     * @throws RSSManagerException Is thrown if any unexpected error occurs
     */
    public void removeDatabasePrivilegesTemplateEntries(String environmentName, String templateName,
                                                        int tenantId) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAOFactory.getConnection();
            String sql = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE_ENTRY WHERE TEMPLATE_ID = (SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while removing database privilege "
                            + "template entries : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    /**
     * Updates the database permissions enabled in a particular database
     * privilege template.
     *
     * @param template Name of the associated database privilege template
     * @throws RSSManagerException Is thrown if any unexpected error occurs
     */
    public void editDatabasePrivilegesTemplate(String environmentName,
                                               DatabasePrivilegeTemplate template,
                                               int tenantId) throws RSSManagerException {
        Connection conn = RSSDAOFactory.getConnection();
        PreparedStatement stmt = null;
        DatabasePrivilegeSet privileges = template.getPrivileges();
        try {
            String sql = "UPDATE RM_DB_PRIVILEGE_TEMPLATE_ENTRY SET SELECT_PRIV = ?, INSERT_PRIV = ?, UPDATE_PRIV = ?, DELETE_PRIV = ?, CREATE_PRIV = ?, DROP_PRIV = ?, GRANT_PRIV = ?, REFERENCES_PRIV = ?, INDEX_PRIV = ?, ALTER_PRIV = ?, CREATE_TMP_TABLE_PRIV = ?, LOCK_TABLES_PRIV = ?, CREATE_VIEW_PRIV = ?, SHOW_VIEW_PRIV = ?, CREATE_ROUTINE_PRIV = ?, ALTER_ROUTINE_PRIV = ?, EXECUTE_PRIV = ?, EVENT_PRIV = ?, TRIGGER_PRIV = ? WHERE TEMPLATE_ID = (SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?))";
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
            stmt.setString(20, template.getName());
            stmt.setInt(21, tenantId);
            stmt.setString(22, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while editing the database privilege "
                            + "template '" + template.getName() + "' : "
                            + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }

    /**
     * Retrieves all the database privilege template entries created by the
     * tenant which carries the given id.
     *
     * @param tenantId Id of the logged in tenant
     * @return The list of database privilege templates belong to the given
     *         tenant
     * @throws RSSManagerException Is thrown if any unexpected error occurs
     */
    public DatabasePrivilegeTemplate[] getAllDatabasePrivilegesTemplates(
            String environmentName, int tenantId) throws RSSManagerException {
        Connection conn = RSSDAOFactory.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT p.ID, p.NAME, p.TENANT_ID, e.SELECT_PRIV, e.INSERT_PRIV, e.UPDATE_PRIV, e.DELETE_PRIV, e.CREATE_PRIV, e.DROP_PRIV, e.GRANT_PRIV, e.REFERENCES_PRIV, e.INDEX_PRIV, e.ALTER_PRIV, e.CREATE_TMP_TABLE_PRIV, e.LOCK_TABLES_PRIV, e.CREATE_VIEW_PRIV, e.SHOW_VIEW_PRIV, e.CREATE_ROUTINE_PRIV, e.ALTER_ROUTINE_PRIV, e.EXECUTE_PRIV, e.EVENT_PRIV, e.TRIGGER_PRIV FROM RM_DB_PRIVILEGE_TEMPLATE p, RM_DB_PRIVILEGE_TEMPLATE_ENTRY e WHERE p.ID = e.TEMPLATE_ID AND p.TENANT_ID = ? AND p.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, environmentName);
            rs = stmt.executeQuery();
            List<DatabasePrivilegeTemplate> result = new ArrayList<DatabasePrivilegeTemplate>();
            while (rs.next()) {
                result.add(this.createDatabasePrivilegeTemplateFromRS(rs));
            }
            return result.toArray(new DatabasePrivilegeTemplate[result.size()]);
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while retrieving database privilege "
                            + "templates : " + e.getMessage(), e);
        } finally {
           cleanupResources(rs, stmt, conn);
        }
    }

    /**
     * Retrieves the database privilege information associated with the template
     * which carries the given template name.
     *
     * @param templateName Name of the database privilege template to be retrieved
     * @return Database privilege template information
     * @throws RSSManagerException Is thrown if any unexpected error occurs
     */
    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            String environmentName, String templateName, int tenantId) throws RSSManagerException {
        Connection conn = RSSDAOFactory.getConnection();
        PreparedStatement stmt = null;
        DatabasePrivilegeTemplate template = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT p.ID, p.NAME, p.TENANT_ID, e.SELECT_PRIV, e.INSERT_PRIV, e.UPDATE_PRIV, e.DELETE_PRIV, e.CREATE_PRIV, e.DROP_PRIV, e.GRANT_PRIV, e.REFERENCES_PRIV, e.INDEX_PRIV, e.ALTER_PRIV, e.CREATE_TMP_TABLE_PRIV, e.LOCK_TABLES_PRIV, e.CREATE_VIEW_PRIV, e.SHOW_VIEW_PRIV, e.CREATE_ROUTINE_PRIV, e.ALTER_ROUTINE_PRIV, e.EXECUTE_PRIV, e.EVENT_PRIV, e.TRIGGER_PRIV FROM RM_DB_PRIVILEGE_TEMPLATE p, RM_DB_PRIVILEGE_TEMPLATE_ENTRY e WHERE p.ID = e.TEMPLATE_ID AND p.NAME = ? AND p.TENANT_ID = ? AND p.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                template = this.createDatabasePrivilegeTemplateFromRS(rs);
            }
            return template;
        } catch (SQLException e) {
            throw new RSSManagerException(
                    "Error occurred while retrieving database privilege "
                            + "template information : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    /**
     * Extracts the assigned values for the database permissions from a result
     * set.
     *
     * @param rs Result set carrying the database permission information
     * @return Database privilege set wrapping the returned result
     * @throws SQLException Is thrown if any unexpected error occurs while retrieving the
     *                      values from the result set
     */
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

    /**
     * Extracts the database privilege template information from a result set.
     *
     * @param rs Result set carrying the database privilege template
     *           information
     * @return Database privilege template object wrapping the returned result
     * @throws SQLException        Is thrown if any unexpected error occurs while retrieving the
     *                             values from the result set
     * @throws RSSManagerException Is thrown if any unexpected error occurs while retrieving the
     *                             values from the result set
     */
    private DatabasePrivilegeTemplate createDatabasePrivilegeTemplateFromRS(
            ResultSet rs) throws SQLException, RSSManagerException {
        int id = rs.getInt("ID");
        String templateName = rs.getString("NAME");
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

        return new DatabasePrivilegeTemplate(id, templateName, privileges);
    }

    private Database createDatabaseFromRS(ResultSet rs) throws SQLException,
            RSSManagerException {
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

    private RSSInstance createRSSInstanceFromRS(ResultSet rs)
            throws SQLException, RSSManagerException {
        int id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String serverURL = rs.getString("SERVER_URL");
        String instanceType = rs.getString("INSTANCE_TYPE");
        String serverCategory = rs.getString("SERVER_CATEGORY");
        String adminUsername = rs.getString("ADMIN_USERNAME");
        String adminPassword = rs.getString("ADMIN_PASSWORD");
        String dbmsType = rs.getString("DBMS_TYPE");

        RDBMSConfiguration dataSourceConfig = new RDBMSConfiguration();
        dataSourceConfig.setUrl(serverURL);
        dataSourceConfig.setUsername(adminUsername);
        dataSourceConfig.setPassword(adminPassword);
        return new RSSInstance(id, name, dbmsType, instanceType, serverCategory, dataSourceConfig);
    }

    private DatabaseUser createDatabaseUserFromRS(ResultSet rs)
            throws SQLException {
        String username = rs.getString("USERNAME");
        String rssInstName = rs.getString("RSS_INSTANCE_NAME");
        String type = rs.getString("TYPE");
        return new DatabaseUser(username, null, rssInstName, type);
    }

    private boolean isSystemHostedDatabaseExist(String environmentName, String databaseName,
                                                int tenantId) throws RSSManagerException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "SELECT 1 INTO IS_EXIST FROM RM_DATABASE WHERE NAME = ? AND TYPE = ? AND TENANT_ID = ?";
        try {
            conn = RSSDAOFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setString(2, RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            int i = 0;
            if (rs.next()) {
                i = rs.getInt("IS_EXIST");
            }
            return (i == 1);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while checking wether a WSO2_RSS " +
                    "hosted database already exists of the name '" + databaseName + "' : " +
                    e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
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

    private DatabaseUser createDatabaseUserFromRSWithUsername(ResultSet rs) throws SQLException {
        String username = rs.getString("USERNAME");
        String type = rs.getString("TYPE");
        return new DatabaseUser(username, null, null, type);
    }

}
