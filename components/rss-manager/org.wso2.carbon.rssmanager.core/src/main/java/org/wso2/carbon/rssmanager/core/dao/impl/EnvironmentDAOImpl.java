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

import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.*;

public class EnvironmentDAOImpl implements EnvironmentDAO {

    public void addEnvironment(RSSEnvironment environment) throws RSSDAOException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "INSERT INTO RM_ENVIRONMENT (NAME, TENANT_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, environment.getName());
            stmt.setInt(2, RSSDAOUtil.getTenantId());
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSDAOException("Failed to add metada related to RSS Environment'" +
                        environment.getName() + "'");
            }
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                environment.setId(rs.getInt(1));
            }
        } catch (Exception e) {
            throw new RSSDAOException("Error occurred while creating metadata related to  RSS " +
                    "environment '" + environment.getName() + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    public void removeEnvironment(String environmentName) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "DELETE FROM RM_ENVIRONMENT WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            stmt.setInt(8, RSSDAOUtil.getTenantId());
            stmt.execute();
        } catch (Exception e) {
            throw new RSSDAOException("Error occurred while deleting metadata related to RSS " +
                    "environment '" + environmentName + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    public void updateEnvironment(RSSEnvironment environment) throws RSSDAOException {

    }

    public RSSEnvironment getEnvironment(String environmentName) throws RSSDAOException {
        return null;
    }

    public RSSEnvironment[] getEnvironments() throws RSSDAOException {
        return new RSSEnvironment[0];
    }

    @Override
    public boolean isEnvironmentExists(String environmentName) throws RSSDAOException {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.getEntityManager().createConnection(RSSDAO.getDataSource());
            String sql = "SELECT 1 AS IS_EXIST FROM RM_ENVIRONMENT WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            rs = stmt.executeQuery();
            int i = 0;
            if (rs.next()) {
                i = rs.getInt("IS_EXIST");
            }
            return (i == 1);
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while checking the existance of RSS " +
                    "environment '" + environmentName + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

}
