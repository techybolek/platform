/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.internal.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLRSSManager extends RSSManager {

    private static final Log log = LogFactory.getLog(MySQLRSSManager.class);

    public MySQLRSSManager(RSSEnvironment rssEnvironment) {
        super(rssEnvironment);
    }

    @Override
    public Database createDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTransaction = false;

        String qualifiedDatabaseName =
                RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());

        boolean isExist =
                this.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName);
        if (isExist) {
            throw new RSSManagerException("Database '" + qualifiedDatabaseName + "' " +
                    "already exists");
        }

        RSSInstance rssInstance = this.lookupRSSInstance(database.getRssInstanceName());
        if (rssInstance == null) {
            throw new RSSManagerException("RSS instance " + database.getRssInstanceName() +
                    " does not exist");
        }

        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);

            if (!this.isInTransaction()) {
                if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.beginTransaction();
                    inTransaction = true;
                }
            }
            database.setName(qualifiedDatabaseName);
            database.setRssInstanceName(rssInstance.getName());
            String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
            database.setUrl(databaseUrl);
            database.setType(this.inferEntityType(rssInstance.getName()));

            int tenantId = RSSManagerUtil.getTenantId();
            /* creates a reference to the database inside the metadata repository */
            this.getDAO().createDatabase(getRSSEnvironmentName(), database, tenantId);
            this.getDAO().incrementSystemRSSDatabaseCount(getRSSEnvironmentName());

            /* Actual database creation is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            if (inTransaction) {
                this.endTransaction();
            }
            /* committing the changes to RSS instance */
            conn.commit();

            return database;
        } catch (SQLException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }

            throw new RSSManagerException("Error while creating the database '" +
                    qualifiedDatabaseName + "' on RSS instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            try {
                conn.rollback();
            } catch (Exception e1) {
                log.error(e1);
            }
            throw e;
        } finally {
            this.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void dropDatabase(String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTransaction = false;
        PreparedStatement delStmt = null;

        RSSInstance rssInstance =
                this.getRSSInstanceDatabaseBelongTo(rssInstanceName, databaseName);
        if (rssInstance == null) {
            throw new RSSManagerException("RSS instance " + rssInstanceName + " does not exist");
        }
        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            String sql = "DROP DATABASE " + databaseName;
            stmt = conn.prepareStatement(sql);
            //delete from mysql.db
            delStmt = deletePreparedStatement(conn, databaseName);

            if (!this.isInTransaction()) {
                if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.beginTransaction();
                    inTransaction = true;
                }
            }
            int tenantId = RSSManagerUtil.getTenantId();
            this.getDAO().deleteUserDatabasePrivilegeEntriesByDatabase(rssInstance, databaseName, tenantId);
            this.getDAO().removeUserDatabaseEntriesByDatabase(getRSSEnvironmentName(), rssInstance,
                    databaseName, tenantId);
            this.getDAO().dropDatabase(getRSSEnvironmentName(), rssInstance, databaseName, tenantId);

            /* Actual database creation is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();
            delStmt.execute();

            if (inTransaction) {
                this.endTransaction();
            }
            conn.commit();
        } catch (SQLException e) {
            if (this.isInTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error while dropping the database '" + databaseName +
                    "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw e;
        } finally {
            this.cleanupResources(null, delStmt, null);
            this.cleanupResources(null, stmt, conn);
        }
    }

    private RSSInstance getRSSInstanceDatabaseBelongTo(String rssInstanceName,
                                                       String databaseName) throws RSSManagerException {
        RSSInstance rssInstance;
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            rssInstance = this.getDAO().findRSSInstanceDatabaseBelongsTo(getRSSEnvironmentName(),
                    rssInstanceName, databaseName, tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
            return rssInstance;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    @Override
    public DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTransaction = false;
        try {
            String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getUsername());

            boolean isExist =
                    this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername);
            if (isExist) {
                throw new RSSManagerException("Database user '" + qualifiedUsername + "' " +
                        "already exists");
            }

            /* Sets the fully qualified username */
            user.setUsername(qualifiedUsername);
            user.setRssInstanceName(user.getRssInstanceName());
            user.setType(this.inferUserType(user.getRssInstanceName()));

            for (RSSInstanceDSWrapper wrapper :
                    getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
//                    try {
//                        PrivilegedCarbonContext.startTenantFlow();
//                        PrivilegedCarbonContext.getCurrentContext().setTenantId(
//                                MultitenantConstants.SUPER_TENANT_ID);

                    rssInstance = getSystemRSSInstance(wrapper.getName());
//                    } finally {
//                        PrivilegedCarbonContext.endTenantFlow();
//                    }

                    conn = getConnection(rssInstance);
                    conn.setAutoCommit(false);

                    String sql = "INSERT INTO mysql.user (Host, User, Password, ssl_cipher, x509_issuer, x509_subject, authentication_string) VALUES (?, ?, PASSWORD(?), ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, "%");
                    stmt.setString(2, qualifiedUsername);
                    stmt.setString(3, user.getPassword());
                    stmt.setBlob(4, new ByteArrayInputStream(new byte[0]));
                    stmt.setBlob(5, new ByteArrayInputStream(new byte[0]));
                    stmt.setBlob(6, new ByteArrayInputStream(new byte[0]));
                    stmt.setString(7, "");

                    /* Initiating the distributed transaction */
                    if (!this.isInTransaction()) {
                        if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                            this.beginTransaction();
                            inTransaction = true;
                        }
                    }
                    int tenantId = RSSManagerUtil.getTenantId();
                    user.setRssInstanceName(rssInstance.getName());
                    this.getDAO().createDatabaseUser(getRSSEnvironmentName(), rssInstance, user,
                            tenantId);

                    /* Actual database user creation is committed just before committing the meta
              * info into RSS management repository. This is done as it is not possible to
              * control CREATE, DROP, ALTER, etc operations within a JTA transaction since
              * those operations are committed implicitly */
                    stmt.execute();

                    /* Committing distributed transaction */
                    if (inTransaction) {
                        this.endTransaction();
                    }
                    conn.commit();
                } catch (SQLException e) {
                    if (this.isInTransaction()) {
                        this.rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw new RSSManagerException("Error occurred while creating the database " +
                            "user '" + qualifiedUsername + "' on RSS instance '" +
                            wrapper.getName() + "'", e);
                } catch (RSSManagerException e) {
                    if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                        this.rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw new RSSManagerException("Error occurred while creating the database " +
                            "user '" + qualifiedUsername + "' on RSS instance '" +
                            wrapper.getName() + "'", e);
                } finally {
                    this.cleanupResources(null, stmt, conn);
                }
            }
            for (RSSInstanceDSWrapper wrapper :
                    getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                this.flushPrivileges(wrapper.getRssInstance());
            }

            return user;
        } catch (SQLException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            String msg = "Error while creating the database user '" +
                    user.getUsername() + "' on RSS instance '" + user.getRssInstanceName() +
                    "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    @Override
    public void dropDatabaseUser(String rssInstanceName, String username) throws
            RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTransaction = false;
        try {
            for (RSSInstanceDSWrapper wrapper :
                    getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance = getSystemRSSInstance(wrapper.getName());
                    conn = getConnection(wrapper.getRssInstance());
                    conn.setAutoCommit(false);

                    String sql = "DELETE FROM mysql.user WHERE User = ? AND Host = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, username);
                    stmt.setString(2, "%");

                    /* Initiating the transaction */
                    if (!this.isInTransaction()) {
                        if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                            this.beginTransaction();
                            inTransaction = true;
                        }
                    }
                    int tenantId = RSSManagerUtil.getTenantId();
                    this.getDAO().deleteUserDatabasePrivilegeEntriesByDatabaseUser(
                            getRSSEnvironmentName(), rssInstance, username, tenantId);
                    this.getDAO().removeUserDatabaseEntriesByDatabaseUser(getRSSEnvironmentName(),
                            rssInstance, username, tenantId);
                    this.getDAO().dropDatabaseUser(getRSSEnvironmentName(), rssInstance, username,
                            tenantId);

                    /* Actual database creation is committed just before committing the meta info into RSS
                  * management repository. This is done as it is not possible to control CREATE, DROP,
                  * ALTER operations within a JTA transaction since those operations are committed
                  * implicitly */
                    stmt.execute();

                    /* committing the distributed transaction */
                    if (inTransaction) {
                        this.endTransaction();
                    }
                    conn.commit();
                } catch (RSSManagerException e) {
                    if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                        this.rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw e;
                } finally {
                    this.cleanupResources(null, stmt, conn);
                }
            }
            for (RSSInstanceDSWrapper wrapper :
                    getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                this.flushPrivileges(wrapper.getRssInstance());
            }
        } catch (SQLException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error while dropping the database user '" + username +
                    "' on RSS instances : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw e;
        } finally {
            this.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    this.getDAO().findRSSInstanceDatabaseUserBelongsTo(getRSSEnvironmentName(),
                            user.getRssInstanceName(), user.getUsername(), tenantId);
            if (rssInstance == null) {
                if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + user.getRssInstanceName() + "'");
            }
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(user.getRssInstanceName())) {
                user.setRssInstanceName(rssInstance.getName());
            }
            this.getDAO().updateDatabaseUser(getRSSEnvironmentName(), privileges, rssInstance, user,
                    databaseName);
            if (inTransaction) {
                this.endTransaction();
            }
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    @Override
    public void attachUserToDatabase(String rssInstanceName,
                                     String databaseName,
                                     String username,
                                     String templateName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTransaction = false;

        RSSInstance rssInstance = this.getRSSInstanceDatabaseBelongTo(rssInstanceName, databaseName);
        if (rssInstance == null) {
            throw new RSSManagerException("RSS instance " + rssInstanceName + " does not exist");
        }

        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            throw new RSSManagerException("Database '" + databaseName + "' does not exist");
        }

        DatabasePrivilegeTemplate template = this.getDatabasePrivilegeTemplate(templateName);
        if (template == null) {
            throw new RSSManagerException("Database privilege template '" + templateName +
                    "' does not exist");
        }
        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            stmt = this.composePreparedStatement(conn, databaseName, username, template);

            if (!this.isInTransaction()) {
                if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.beginTransaction();
                    inTransaction = true;
                }
            }
            int tenantId = RSSManagerUtil.getTenantId();
            UserDatabaseEntry ude =
                    this.getDAO().createUserDatabaseEntry(getRSSEnvironmentName(), rssInstance,
                            database, username, tenantId);
            this.getDAO().setUserDatabasePrivileges(getRSSEnvironmentName(), ude, template,
                    tenantId);

            /* Actual database user attachment is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* ending distributed transaction */
            if (inTransaction) {
                this.endTransaction();
            }
            conn.commit();

            this.flushPrivileges(rssInstance);
        } catch (SQLException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw e;
        } finally {
            this.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void detachUserFromDatabase(String rssInstanceName, String databaseName,
                                       String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTransaction = false;
        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            throw new RSSManagerException("Database '" + databaseName + "' does not exist");
        }
        /* Initiating the distributed transaction */
        RSSInstance rssInstance = this.getRSSInstanceDatabaseBelongTo(rssInstanceName, databaseName);
        if (rssInstance == null) {
            throw new RSSManagerException("RSS instance '" + rssInstanceName + "' does not exist");
        }

        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            String sql = "DELETE FROM mysql.db WHERE host = ? AND user = ? AND db = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%");
            stmt.setString(2, username);
            stmt.setString(3, databaseName);

            /* Initiating the distributed transaction */
            if (!this.isInTransaction()) {
                if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.beginTransaction();
                    inTransaction = true;
                }
            }
            int tenantId = RSSManagerUtil.getTenantId();
            this.getDAO().deleteUserDatabasePrivileges(getRSSEnvironmentName(), rssInstance,
                    username, tenantId);
            this.getDAO().deleteUserDatabaseEntry(getRSSEnvironmentName(), rssInstance, username,
                    tenantId);

            /* Actual database user detachment is committed just before committing the meta info
          * into RSS management repository. This is done as it is not possible to control CREATE,
          * DROP, ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* Committing the transaction */
            if (isInTransaction()) {
                this.endTransaction();
            }
            conn.commit();

            this.flushPrivileges(rssInstance);
        } catch (SQLException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw e;
        } finally {
            this.cleanupResources(null, stmt, conn);
        }
    }
    
    private PreparedStatement composePreparedStatement(Connection con,
                                                       String databaseName,
                                                       String username,
                                                       DatabasePrivilegeTemplate template) throws
            SQLException, RSSManagerException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        String sql = "INSERT INTO mysql.db VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, "%");
        stmt.setString(2, databaseName);
        stmt.setString(3, username);
        stmt.setString(4, privileges.getSelectPriv());
        stmt.setString(5, privileges.getInsertPriv());
        stmt.setString(6, privileges.getUpdatePriv());
        stmt.setString(7, privileges.getDeletePriv());
        stmt.setString(8, privileges.getCreatePriv());
        stmt.setString(9, privileges.getDropPriv());
        stmt.setString(10, privileges.getGrantPriv());
        stmt.setString(11, privileges.getReferencesPriv());
        stmt.setString(12, privileges.getIndexPriv());
        stmt.setString(13, privileges.getAlterPriv());
        stmt.setString(14, privileges.getCreateTmpTablePriv());
        stmt.setString(15, privileges.getLockTablesPriv());
        stmt.setString(16, privileges.getCreateViewPriv());
        stmt.setString(17, privileges.getShowViewPriv());
        stmt.setString(18, privileges.getCreateRoutinePriv());
        stmt.setString(19, privileges.getAlterRoutinePriv());
        stmt.setString(20, privileges.getExecutePriv());
        stmt.setString(21, privileges.getEventPriv());
        stmt.setString(22, privileges.getTriggerPriv());

        return stmt;
    }

    private PreparedStatement deletePreparedStatement(final Connection con,
                                                      final String databaseName) throws SQLException {
        String sql = " DELETE FROM mysql.db where Db=?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, databaseName);

        return stmt;
    }

    private void flushPrivileges(RSSInstance rssInstance) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection(rssInstance);
            String sql = "FLUSH PRIVILEGES";
            stmt = conn.prepareStatement(sql);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while flushing privileges on RSS " +
                    "instance '" + rssInstance.getName() + "' : " + e.getMessage(), e);
        } finally {
            this.cleanupResources(null, stmt, conn);
        }
    }

    private RSSInstance lookupRSSInstance(String rssInstanceName) throws RSSManagerException {
        return (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) ?
                this.getRoundRobinAssignedDatabaseServer() : this.getRSSInstance(rssInstanceName);
    }

    private String inferEntityType(String rssInstanceName) throws RSSManagerException {
        return (getDSWrapperRepository().getRSSInstanceDSWrapper(rssInstanceName) != null) ?
                RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE :
                RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE;
    }

    private String inferUserType(String rssInstanceName) throws RSSManagerException {
        return (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) ?
                RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE :
                RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE;
    }

    private Connection getConnection(RSSInstance rssInstance) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper = getDSWrapperRepository().getRSSInstanceDSWrapper(rssInstance.getName());
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper associated with '" + rssInstance.getName() + "' RSS instance is null.");
        }
        return dsWrapper.getConnection();
    }

}
