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
package org.wso2.carbon.rssmanager.core.manager.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(MySQLSystemRSSManager.class);

    public MySQLSystemRSSManager(RSSEnvironment rssEnvironment) {
        //super(rssEnvironment);
        super(null);
    }

    @Override
    public Database createDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;

        String qualifiedDatabaseName =
                RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());

        boolean isExist =
                this.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName);
        if (isExist) {
            throw new EntityAlreadyExistsException("Database '" + qualifiedDatabaseName + "' " +
                    "already exists");
        }

        RSSInstance rssInstance = this.lookupRSSInstance(database.getRssInstanceName());
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance " + database.getRssInstanceName() +
                    " does not exist");
        }

        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);

            inTx = beginTransaction();
            database.setName(qualifiedDatabaseName);
            database.setRssInstanceName(rssInstance.getName());
            String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
            database.setUrl(databaseUrl);
            database.setType(this.inferEntityType(rssInstance.getName()));

            int tenantId = RSSManagerUtil.getTenantId();
            /* creates a reference to the database inside the metadata repository */
            this.getRSSDAO().getDatabaseDAO().addDatabase(database, tenantId);
            this.getRSSDAO().getDatabaseDAO().incrementSystemRSSDatabaseCount();

            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
            stmt.execute();

            if (inTx) {
                endTransaction();
            }
            /* committing the changes to RSS instance */
            conn.commit();

            return database;
        } catch (SQLException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw new RSSManagerException("Error while creating the database '" +
                    qualifiedDatabaseName + "' on RSS instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } catch (RSSDAOException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            try {
                conn.rollback();
            } catch (Exception e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error occurred while creating database: " +
                    e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void dropDatabase(String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement delStmt = null;

        RSSInstance rssInstance = resolveRSSInstance(rssInstanceName, databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance " + rssInstanceName + " does not exist");
        }
        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            String sql = "DROP DATABASE " + databaseName;
            stmt = conn.prepareStatement(sql);
            //delete from mysql.db
            delStmt = deletePreparedStatement(conn, databaseName);

            inTx = beginTransaction();
            int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(
                    rssInstance.getName(), databaseName, tenantId);
            this.getRSSDAO().getDatabaseDAO().removeDatabase(rssInstance.getName(), databaseName,
                    tenantId);

            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
            stmt.execute();
            delStmt.execute();

            if (inTx) {
                endTransaction();
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
        } catch (RSSDAOException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error occurred while dropping database: " +
                    e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, delStmt, null);
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());

            boolean isExist =
                    this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername);
            if (isExist) {
                throw new RSSManagerException("Database user '" + qualifiedUsername + "' " +
                        "already exists");
            }

            /* Sets the fully qualified username */
            user.setName(qualifiedUsername);
            user.setRssInstanceName(user.getRssInstanceName());
            user.setType(this.inferUserType(user.getRssInstanceName()));

            for (RSSInstanceDSWrapper wrapper :
                    getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                                MultitenantConstants.SUPER_TENANT_ID);
                        rssInstance = getRSSInstance(wrapper.getName());
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

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
                    inTx = beginTransaction();
                    int tenantId = RSSManagerUtil.getTenantId();
                    user.setRssInstanceName(rssInstance.getName());
                    this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(user, tenantId);

                    /* Actual database user creation is committed just before committing the meta
                     * info into RSS management repository. This is done as it is not possible to
                     * control CREATE, DROP, ALTER, etc operations within a JTA transaction since
                     * those operations are committed implicitly */
                    stmt.execute();

                    /* Committing distributed transaction */
                    if (inTx) {
                        endTransaction();
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
                } catch (RSSDAOException e) {
                    if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                        this.rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw new RSSManagerException("Error occurred while creating the database " +
                            "user '" + qualifiedUsername + "' on RSS instance '" +
                            wrapper.getName() + "'", e);
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }
            for (RSSInstanceDSWrapper wrapper :
                    getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                this.flushPrivileges(wrapper.getRssInstance());
            }

            return user;
        } catch (SQLException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            String msg = "Error while creating the database user '" +
                    user.getName() + "' on RSS instance '" + user.getRssInstanceName() +
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
                    RSSInstance rssInstance;
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                                MultitenantConstants.SUPER_TENANT_ID);
                        rssInstance = getRSSInstance(wrapper.getName());
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

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
                    this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(
                            rssInstance.getName(), username, tenantId);
                    this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(
                            rssInstance.getName(), username, tenantId);
                    this.getRSSDAO().getDatabaseUserDAO().removeDatabaseUser(rssInstance.getName(),
                            username, tenantId);

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
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
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
        } catch (RSSDAOException e) {
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
            throw new RSSManagerException("Error occurred while dropping database user: " +
                    e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    this.getRSSDAO().getDatabaseUserDAO().resolveRSSInstance(
                            user.getRssInstanceName(), user.getName(), tenantId);
            if (rssInstance == null) {
                if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + user.getRssInstanceName() + "'");
            }
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(user.getRssInstanceName())) {
                user.setRssInstanceName(rssInstance.getName());
            }
            this.getRSSDAO().getDatabaseUserDAO().updateDatabaseUser(privileges,
                    rssInstance.getName(), user, databaseName);
        } catch (RSSDAOException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while updating database user " +
                    "privileges: " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    @Override
    public void attachUserToDatabase(UserDatabaseEntry entry,
                                     String templateName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;

        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        RSSInstance rssInstance = resolveRSSInstance(rssInstanceName, databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance " + rssInstanceName + " does not exist");
        }

        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            throw new EntityNotFoundException("Database '" + entry.getDatabaseName() +
                    "' does not exist");
        }

        DatabasePrivilegeTemplate template = this.getDatabasePrivilegeTemplate(templateName);
        if (template == null) {
            throw new EntityNotFoundException("Database privilege template '" + templateName +
                    "' does not exist");
        }
        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            stmt = this.composePreparedStatement(conn, databaseName, username, template);

            inTx = beginTransaction();
            int tenantId = RSSManagerUtil.getTenantId();
            int id = this.getRSSDAO().getUserDatabaseEntryDAO().addUserDatabaseEntry(entry, tenantId);
            this.getRSSDAO().getDatabaseUserDAO().setUserDatabasePrivileges(id, template, tenantId);

            /* Actual database user attachment is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* ending distributed transaction */
            if (inTx) {
                this.endTransaction();
            }
            conn.commit();

            this.flushPrivileges(rssInstance);
        } catch (SQLException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
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
        } catch (RSSDAOException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error occurred while attaching database user '" +
                    username + "' to database '" + databaseName + "' : " + e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void detachUserFromDatabase(UserDatabaseEntry entry) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        Database database = getDatabase(entry.getRssInstanceName(), entry.getDatabaseName());
        if (database == null) {
            throw new EntityNotFoundException("Database '" + entry.getDatabaseName() +
                    "' does not exist");
        }
        /* Initiating the distributed transaction */
        RSSInstance rssInstance =
                resolveRSSInstance(entry.getRssInstanceName(), entry.getDatabaseName());
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance '" + entry.getRssInstanceName() +
                    "' does not exist");
        }

        try {
            conn = getConnection(rssInstance);
            conn.setAutoCommit(false);
            String sql = "DELETE FROM mysql.db WHERE host = ? AND user = ? AND db = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%");
            stmt.setString(2, entry.getUsername());
            stmt.setString(3, entry.getDatabaseName());

            /* Initiating the distributed transaction */
            inTx = beginTransaction();
            int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(rssInstance.getName(),
                    entry.getUsername(), tenantId);
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntry(
                    rssInstance.getName(), entry.getUsername(), tenantId);

            /* Actual database user detachment is committed just before committing the meta info
          * into RSS management repository. This is done as it is not possible to control CREATE,
          * DROP, ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* Committing the transaction */
            if (inTx) {
                endTransaction();
            }
            conn.commit();

            this.flushPrivileges(rssInstance);
        } catch (SQLException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" +
                    entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
                    "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSDAOException e) {
            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error occurred while detaching database user '" +
                    entry.getUsername() + "' from database '" + entry.getDatabaseName() + "' : " +
                    e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
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
            RSSManagerUtil.cleanupResources(null, stmt, conn);
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
