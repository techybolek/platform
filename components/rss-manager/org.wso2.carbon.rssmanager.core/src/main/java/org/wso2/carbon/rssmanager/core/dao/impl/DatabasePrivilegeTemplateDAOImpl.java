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

import org.wso2.carbon.rssmanager.core.dao.DatabasePrivilegeTemplateDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabasePrivilegeTemplateDAOImpl implements DatabasePrivilegeTemplateDAO {


    public void addDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE(NAME, TENANT_ID) VALUES(?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, template.getName());
            stmt.setInt(2, tenantId);
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSDAOException("Database privilege was not created");
            }
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                template.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while creating database privilege " +
                    "template '" + template.getName() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }


    public void removeDatabasePrivilegesTemplate(String templateName,
                                                 int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while dropping the database privilege " +
                    "template '" + templateName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }


    public void updateDatabasePrivilegesTemplate(DatabasePrivilegeTemplate template,
                                                 int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        DatabasePrivilegeSet privileges = template.getPrivileges();
        try {
            conn = RSSDAO.createConnection();
            String sql = "UPDATE RM_DB_PRIVILEGE_TEMPLATE_ENTRY SET SELECT_PRIV = ?, INSERT_PRIV = ?, UPDATE_PRIV = ?, DELETE_PRIV = ?, CREATE_PRIV = ?, DROP_PRIV = ?, GRANT_PRIV = ?, REFERENCES_PRIV = ?, INDEX_PRIV = ?, ALTER_PRIV = ?, CREATE_TMP_TABLE_PRIV = ?, LOCK_TABLES_PRIV = ?, CREATE_VIEW_PRIV = ?, SHOW_VIEW_PRIV = ?, CREATE_ROUTINE_PRIV = ?, ALTER_ROUTINE_PRIV = ?, EXECUTE_PRIV = ?, EVENT_PRIV = ?, TRIGGER_PRIV = ? WHERE TEMPLATE_ID = (SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ?)";
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
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while editing the database privilege " +
                    "template '" + template.getName() + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }


    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(String templateName,
                                                                   int tenantId) throws RSSDAOException {
        DatabasePrivilegeTemplate template = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT p.ID, p.NAME, p.TENANT_ID, e.SELECT_PRIV, e.INSERT_PRIV, e.UPDATE_PRIV, e.DELETE_PRIV, e.CREATE_PRIV, e.DROP_PRIV, e.GRANT_PRIV, e.REFERENCES_PRIV, e.INDEX_PRIV, e.ALTER_PRIV, e.CREATE_TMP_TABLE_PRIV, e.LOCK_TABLES_PRIV, e.CREATE_VIEW_PRIV, e.SHOW_VIEW_PRIV, e.CREATE_ROUTINE_PRIV, e.ALTER_ROUTINE_PRIV, e.EXECUTE_PRIV, e.EVENT_PRIV, e.TRIGGER_PRIV FROM RM_DB_PRIVILEGE_TEMPLATE p, RM_DB_PRIVILEGE_TEMPLATE_ENTRY e WHERE p.ID = e.TEMPLATE_ID AND p.NAME = ? AND p.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                template = createDatabasePrivilegeTemplateFromRS(rs);
            }
            return template;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving database privilege " +
                    "template information : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }


    public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
            int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT p.ID, p.NAME, p.TENANT_ID, e.SELECT_PRIV, e.INSERT_PRIV, e.UPDATE_PRIV, e.DELETE_PRIV, e.CREATE_PRIV, e.DROP_PRIV, e.GRANT_PRIV, e.REFERENCES_PRIV, e.INDEX_PRIV, e.ALTER_PRIV, e.CREATE_TMP_TABLE_PRIV, e.LOCK_TABLES_PRIV, e.CREATE_VIEW_PRIV, e.SHOW_VIEW_PRIV, e.CREATE_ROUTINE_PRIV, e.ALTER_ROUTINE_PRIV, e.EXECUTE_PRIV, e.EVENT_PRIV, e.TRIGGER_PRIV FROM RM_DB_PRIVILEGE_TEMPLATE p, RM_DB_PRIVILEGE_TEMPLATE_ENTRY e WHERE p.ID = e.TEMPLATE_ID AND p.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            List<DatabasePrivilegeTemplate> result = new ArrayList<DatabasePrivilegeTemplate>();
            while (rs.next()) {
                result.add(createDatabasePrivilegeTemplateFromRS(rs));
            }
            return result.toArray(new DatabasePrivilegeTemplate[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving database privilege " +
                    "templates : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }


    public void setPrivilegeTemplateProperties(DatabasePrivilegeTemplate template,
                                               int tenantId) throws RSSDAOException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
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
            throw new RSSDAOException("Error occurred setting database privilege template " +
                    "properties : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }


    public void removeDatabasePrivilegesTemplateEntries(String templateName,
                                                        int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = RSSDAO.createConnection();
            String sql = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE_ENTRY WHERE TEMPLATE_ID = (SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while removing database privilege " +
                    "template entries : " + e.getMessage(), e);
        } finally {
            cleanupResources(null, stmt, conn);
        }
    }


    public boolean isDatabasePrivilegeTemplateExist(String templateName,
                                                    int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isExist = false;
        try {
            conn = RSSDAO.createConnection();
            String sql = "SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int templateId = rs.getInt("ID");
                if (templateId > 0) {
                    isExist = true;
                }
            }
            return isExist;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while checking the existence " +
                    "of database privilege template '" + templateName + "' : " + e.getMessage(), e);
        } finally {
            cleanupResources(rs, stmt, conn);
        }
    }

    private DatabasePrivilegeTemplate createDatabasePrivilegeTemplateFromRS(ResultSet rs) throws
            SQLException, RSSDAOException {
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
