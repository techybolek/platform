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
package org.wso2.carbon.rssmanager.core.manager.impl.sqlserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLServerSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(SQLServerSystemRSSManager.class);

    public SQLServerSystemRSSManager(RSSConfig config) {
        super(config);
    }

    @Override
    public Database createDatabase(RSSEnvironmentContext ctx,
                                   Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;

        String qualifiedDatabaseName =
                RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());

        boolean isExist =
                this.isDatabaseExist(ctx, database.getRssInstanceName(), qualifiedDatabaseName);
        if (isExist) {
            throw new EntityAlreadyExistsException("Database '" + qualifiedDatabaseName + "' " +
                    "already exists");
        }

        RSSInstance rssInstance = this.getRoundRobinAssignedDatabaseServer(ctx);
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance " + database.getRssInstanceName() +
                    " does not exist");
        }

        try {
            conn = getConnection(ctx, rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);

            inTx = this.getEntityManager().beginTransaction();
            database.setName(qualifiedDatabaseName);
            database.setRssInstanceName(rssInstance.getName());
            String databaseUrl =
                    RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
            database.setUrl(databaseUrl);
            database.setType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);

            final int tenantId = RSSManagerUtil.getTenantId();
            /* creates a reference to the database inside the metadata repository */
            this.getRSSDAO().getDatabaseDAO().addDatabase(ctx.getEnvironmentName(), database,
                    tenantId);
            this.getRSSDAO().getDatabaseDAO().incrementSystemRSSDatabaseCount(
                    ctx.getEnvironmentName(), Connection.TRANSACTION_SERIALIZABLE);

            /* Actual database creation is committed just before committing the meta info into RSS
              * management repository. This is done as it is not possible to control CREATE DATABASE
              * operation within a JTA transaction since those operations are committed
              * implicitly */
            stmt.execute();

            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            return database;
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            try {
                if(conn!=null){
                    conn.rollback();
                }
            } catch (Exception e1) {
                log.error(e1);
            }
            String msg = "Error while creating the database '" + qualifiedDatabaseName +
                    "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void dropDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(ctx, databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance " + rssInstanceName + " does not exist");
        }
        try {
            conn = getConnection(ctx, rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "DROP DATABASE " + databaseName;
            stmt = conn.prepareStatement(sql);

            inTx = this.getEntityManager().beginTransaction();
            int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(
                    ctx.getEnvironmentName(), rssInstance.getId(), databaseName,
                    rssInstance.getInstanceType(), tenantId);
            this.getRSSDAO().getDatabaseDAO().removeDatabase(ctx.getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);

            /* Actual database deletion is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control DROP DATABASE
          * operation within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
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
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                           DatabaseUser user) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());

            boolean isExist =
                    this.isDatabaseUserExist(ctx, user.getRssInstanceName(), qualifiedUsername);
            if (isExist) {
                throw new RSSManagerException("Database user '" + qualifiedUsername + "' " +
                        "already exists");
            }

            /* Sets the fully qualified username */
            user.setName(qualifiedUsername);
            user.setRssInstanceName(user.getRssInstanceName());
            user.setType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);

            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment(ctx).getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                                MultitenantConstants.SUPER_TENANT_ID);
                        rssInstance = getRSSInstance(ctx, wrapper.getName());
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                    conn = getConnection(ctx, rssInstance.getName());
                    conn.setAutoCommit(false);
                    String password = user.getPassword();
                    RSSManagerUtil.checkIfParameterSecured(qualifiedUsername);
                    String sql = "CREATE LOGIN " + qualifiedUsername + " WITH PASSWORD = '" + password + "'";
                    stmt = conn.prepareStatement(sql);

                    /* Initiating the distributed transaction */
                    inTx = this.getEntityManager().beginTransaction();
                    final int tenantId = RSSManagerUtil.getTenantId();
                    user.setRssInstanceName(rssInstance.getName());
                    this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(ctx.getEnvironmentName(), rssInstance, user, tenantId);

                    stmt.execute();

                    /* Committing distributed transaction */
                    if (inTx) {
                        this.getEntityManager().endTransaction();
                    }
                    conn.commit();
                } catch (Exception e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw new RSSManagerException("Error occurred while creating the database " +
                            "user '" + qualifiedUsername + "' on RSS instance '" +
                            wrapper.getName() + "' [" + e.getMessage() + "]", e);
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }
            return user;
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
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
    public void dropDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                 String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;
        try {
            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment(ctx).getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                                MultitenantConstants.SUPER_TENANT_ID);
                        rssInstance = getRSSInstance(ctx, wrapper.getName());
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                    conn = getConnection(ctx, wrapper.getRssInstance().getName());
                    conn.setAutoCommit(false);
                    RSSManagerUtil.checkIfParameterSecured(username);
                    String sql = "DROP LOGIN " + username;
                    stmt = conn.prepareStatement(sql);

                    /* Initiating the transaction */
                    inTx = this.getEntityManager().beginTransaction();
                    final int tenantId = RSSManagerUtil.getTenantId();
                    this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(
                            ctx.getEnvironmentName(), rssInstance.getId(), username, tenantId);
                    this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(
                            ctx.getEnvironmentName(), rssInstance.getId(), username,
                            rssInstance.getInstanceType(), tenantId);
                    this.getRSSDAO().getDatabaseUserDAO().removeDatabaseUser(ctx.getEnvironmentName(),
                            rssInstance.getName(), username, tenantId);

                    stmt.execute();

                    /* committing the distributed transaction */
                    if (inTx) {
                        this.getEntityManager().endTransaction();
                    }
                    conn.commit();
                } catch (RSSManagerException e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw e;
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
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
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    @Override
    public void editDatabaseUserPrivileges(RSSEnvironmentContext ctx,
                                           DatabasePrivilegeSet privileges, DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmtUseDb = null;
        PreparedStatement stmtDetachUser = null;
        PreparedStatement stmtAddUser = null;
        PreparedStatement stmtGrant = null;
        PreparedStatement stmtDeny = null;
        boolean inTx = false;
        String username = user.getName();

        final int tenantId = RSSManagerUtil.getTenantId();
        RSSInstance rssInstance;
        try{
            rssInstance = this.getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByUser(
                        ctx.getEnvironmentName(), user.getRssInstanceName(), user.getName(),
                        tenantId);
        }catch (RSSDAOException ex){
            throw new RSSManagerException("Error while retrieving Server Instance '" +
                    user.getRssInstanceName() + "'");
        }
        if (rssInstance == null) {
            throw new RSSManagerException("Database '" + databaseName + "' does not exist in " +
                    "RSS instance '" + user.getRssInstanceName() + "'");
        }
        Database database = this.getDatabase(ctx, rssInstance.getInstanceType(), databaseName);
        if (database == null) {
            throw new RSSManagerException("Database '" + databaseName + "' does not exist");
        }
        if (privileges == null) {
            throw new RSSManagerException("Database privileges-set is null");
        }

        try {
            conn = getConnection(ctx, rssInstance.getName());
            conn.setAutoCommit(false);
            String sqlUseDb = "USE " + databaseName;
            stmtUseDb = conn.prepareStatement(sqlUseDb);
            String sqlDetachUser = "DROP USER " + username;
            stmtDetachUser = conn.prepareStatement(sqlDetachUser);
            String sqlAddUser = "CREATE USER " + username + " FOR LOGIN " + username;
            stmtAddUser = conn.prepareStatement(sqlAddUser);
            String[] privilegeQueries = getPrivilegeQueries(privileges, username);
            if (privilegeQueries[0] != null) {
                stmtGrant = conn.prepareStatement(privilegeQueries[0]);
            }
            if (privilegeQueries[1] != null) {
                stmtDeny = conn.prepareStatement(privilegeQueries[1]);
            }

            inTx = this.getEntityManager().beginTransaction();
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(user.getRssInstanceName())) {
                user.setRssInstanceName(rssInstance.getName());
            }
		    this.getRSSDAO().getDatabaseUserDAO().updateDatabaseUser(ctx.getEnvironmentName(),
		            privileges, rssInstance, user, databaseName);

            stmtUseDb.execute();
            stmtDetachUser.execute();
            stmtAddUser.execute();
            if (stmtGrant != null) {
                stmtGrant.execute();
            }
            if (stmtDeny != null) {
                stmtDeny.execute();
            }

            /* ending distributed transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            conn.commit();
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while updating privileges of the database user '" +
                    user.getName() + "' for the database '" + databaseName + "' : " +
                    e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
            RSSManagerUtil.cleanupResources(null, stmtDetachUser, null);
            RSSManagerUtil.cleanupResources(null, stmtAddUser, null);
            RSSManagerUtil.cleanupResources(null, stmtGrant, null);
            RSSManagerUtil.cleanupResources(null, stmtDeny, conn);
        }
    }

    @Override
    public void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry entry,
                                     String templateName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmtUseDb = null;
        PreparedStatement stmtAddUser = null;
        PreparedStatement stmtGrant = null;
        PreparedStatement stmtDeny = null;
        boolean inTx = false;

        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(ctx, databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance " + rssInstanceName + " does not exist");
        }

        Database database = this.getDatabase(ctx, rssInstanceName, databaseName);
        if (database == null) {
            throw new EntityNotFoundException("Database '" + entry.getDatabaseName() +
                    "' does not exist");
        }

        DatabaseUser user = this.getDatabaseUser(ctx, rssInstanceName, username);
        if (user == null) {
            String msg = "Database user '" + entry.getUsername() + "' does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        entry.setDatabaseId(database.getId());
        entry.setUserId(user.getId());

        DatabasePrivilegeTemplate template = this.getDatabasePrivilegeTemplate(ctx, templateName);
        if (template == null) {
            throw new EntityNotFoundException("Database privilege template '" + templateName +
                    "' does not exist");
        }
        try {
            conn = getConnection(ctx, rssInstance.getName());
            conn.setAutoCommit(false);
            String sqlUseDb = "USE " + databaseName;
            stmtUseDb = conn.prepareStatement(sqlUseDb);
            String sqlAddUser = "CREATE USER " + username + " FOR LOGIN " + username;
            stmtAddUser = conn.prepareStatement(sqlAddUser);
            String[] privilegeQueries = getPrivilegeQueries(template.getPrivileges(), username);
            if (privilegeQueries[0] != null) {
                stmtGrant = conn.prepareStatement(privilegeQueries[0]);
            }
            if (privilegeQueries[1] != null) {
                stmtDeny = conn.prepareStatement(privilegeQueries[1]);
            }

            inTx = this.getEntityManager().beginTransaction();
            final int tenantId = RSSManagerUtil.getTenantId();
            int id = this.getRSSDAO().getUserDatabaseEntryDAO().addUserDatabaseEntry(
                    ctx.getEnvironmentName(), entry, tenantId);
            this.getRSSDAO().getDatabaseUserDAO().setUserDatabasePrivileges(ctx.getEnvironmentName(),
                    id, template, tenantId);

            stmtUseDb.execute();
            stmtAddUser.execute();
            if (stmtGrant != null) {
                stmtGrant.execute();
            }
            if (stmtDeny != null) {
                stmtDeny.execute();
            }

            /* ending distributed transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            conn.commit();
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
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
        } finally {
            RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
            RSSManagerUtil.cleanupResources(null, stmtAddUser, null);
            RSSManagerUtil.cleanupResources(null, stmtGrant, null);
            RSSManagerUtil.cleanupResources(null, stmtDeny, conn);
        }
    }

    @Override
    public void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry entry) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmtUseDb = null;
        PreparedStatement stmtDetachUser = null;
        Database database = getDatabase(ctx, entry.getRssInstanceName(), entry.getDatabaseName());
        if (database == null) {
            throw new EntityNotFoundException("Database '" + entry.getDatabaseName() +
                    "' does not exist");
        }
        /* Initiating the distributed transaction */
        RSSInstance rssInstance =
                resolveRSSInstanceByDatabase(ctx, entry.getDatabaseName());
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance '" + entry.getRssInstanceName() +
                    "' does not exist");
        }

        try {
            conn = getConnection(ctx, rssInstance.getName());
            conn.setAutoCommit(false);
            String sqlUseDb = "USE " + entry.getDatabaseName();
            stmtUseDb = conn.prepareStatement(sqlUseDb);
            String sqlDetachUser = "DROP USER " + entry.getUsername();
            stmtDetachUser = conn.prepareStatement(sqlDetachUser);

            /* Initiating the distributed transaction */
            inTx = this.getEntityManager().beginTransaction();
            final int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(ctx.getEnvironmentName(),
                    rssInstance.getId(), entry.getUsername(), tenantId);
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntry(
                    ctx.getEnvironmentName(), rssInstance.getId(), entry.getUsername(),
                    rssInstance.getInstanceType(), tenantId);

            stmtUseDb.execute();
            stmtDetachUser.execute();

            /* Committing the transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            conn.commit();
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
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
        } finally {
            RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
            RSSManagerUtil.cleanupResources(null, stmtDetachUser, conn);
        }
    }

    private enum PRIVILEGE {
        SELECT("SELECT"),
        INSERT("INSERT"),
        UPDATE("UPDATE"),
        DELETE("DELETE"),
        CREATE("CREATE AGGREGATE, CREATE ASSEMBLY, CREATE ASYMMETRIC KEY, CREATE CERTIFICATE, " +
                "CREATE CONTRACT, CREATE DEFAULT, CREATE FULLTEXT CATALOG, CREATE FUNCTION, " +
                "CREATE MESSAGE TYPE, CREATE PROCEDURE, CREATE QUEUE, CREATE REMOTE SERVICE BINDING, " +
                "CREATE ROLE, CREATE RULE, CREATE SCHEMA, CREATE SERVICE, CREATE SYMMETRIC KEY, " +
                "CREATE SYNONYM, CREATE TABLE, CREATE TYPE, CREATE XML SCHEMA COLLECTION"),
        DROP(null),
        GRANT("WITH GRANT OPTION"),
        REFERENCES("REFERENCES"),
        INDEX(null),
        ALTER("ALTER, ALTER ANY APPLICATION ROLE, ALTER ANY ASSEMBLY, ALTER ANY ASYMMETRIC KEY, " +
                "ALTER ANY CERTIFICATE, ALTER ANY CONTRACT, ALTER ANY DATABASE AUDIT, " +
                "ALTER ANY DATASPACE, ALTER ANY FULLTEXT CATALOG, ALTER ANY MESSAGE TYPE, " +
                "ALTER ANY REMOTE SERVICE BINDING, ALTER ANY ROLE, ALTER ANY SCHEMA, " +
                "ALTER ANY SERVICE, ALTER ANY SYMMETRIC KEY, ALTER ANY USER"),
        CREATE_TEMP_TABLE(null),
        LOCK_TABLES(null),
        CREATE_VIEW("CREATE VIEW"),
        SHOW_VIEW(null),
        CREATE_ROUTINE("CREATE ROUTE"),
        ALTER_ROUTINE("ALTER ANY ROUTE"),
        EXECUTE("EXEC"),
        EVENT("CREATE DATABASE DDL EVENT NOTIFICATION, ALTER ANY DATABASE EVENT NOTIFICATION"),
        TRIGGER("ALTER ANY DATABASE DDL TRIGGER");

        private String text;

        PRIVILEGE(String text) {
            this.text = text;
        }

        private String getText() {
            return text;
        }
    }

    private String[] getPrivilegeQueries(DatabasePrivilegeSet privilegeSet, String username) {
        String[] queryArray = new String[2];
        List<String> grantList = new ArrayList<String>();
        List<String> denyList = new ArrayList<String>();
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getSelectPriv(),
                PRIVILEGE.SELECT);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getInsertPriv(),
                PRIVILEGE.INSERT);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getUpdatePriv(),
                PRIVILEGE.UPDATE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getDeletePriv(),
                PRIVILEGE.DELETE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreatePriv(),
                PRIVILEGE.CREATE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getDropPriv(),
                PRIVILEGE.DROP);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getReferencesPriv(),
                PRIVILEGE.REFERENCES);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getIndexPriv(),
                PRIVILEGE.INDEX);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getAlterPriv(),
                PRIVILEGE.ALTER);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateTmpTablePriv(),
                PRIVILEGE.CREATE_TEMP_TABLE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getLockTablesPriv(),
                PRIVILEGE.LOCK_TABLES);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateViewPriv(),
                PRIVILEGE.CREATE_VIEW);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getShowViewPriv(),
                PRIVILEGE.SHOW_VIEW);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateRoutinePriv(),
                PRIVILEGE.CREATE_ROUTINE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getAlterRoutinePriv(),
                PRIVILEGE.ALTER_ROUTINE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getExecutePriv(),
                PRIVILEGE.EXECUTE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getEventPriv(),
                PRIVILEGE.EVENT);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getTriggerPriv(),
                PRIVILEGE.TRIGGER);

        //creating grant query
        String grantString = "GRANT ";
        for (String privilegeString : grantList) {
            grantString += privilegeString.concat(",");
        }
        grantString =
                grantString.substring(0, grantString.length() - 1).concat(" TO ").concat(username);
        if (!grantList.isEmpty()) {
            if (isGranted(privilegeSet.getGrantPriv())) {
                grantString = grantString.concat(" ").concat(PRIVILEGE.GRANT.getText());
            }
        } else {
            grantString = null;
        }

        //creating deny query
        String denyString = "DENY ";
        for (String privilegeString : denyList) {
            denyString += privilegeString.concat(",");
        }
        if (!denyList.isEmpty()) {
            denyString =
                    denyString.substring(0, denyString.length() - 1).concat(" TO ").
                            concat(username).concat(" CASCADE");
        } else {
            denyString = null;
        }

        queryArray[0] = grantString;
        queryArray[1] = denyString;
        return queryArray;
    }

    private void addToGrantedListOrDenyList(List<String> grantList, List<String> denyList,
                                            String grantedOrNotString, PRIVILEGE enumPrivilege) {
        if (enumPrivilege.getText() == null) {                // permission is not supported
            return;
        }
        if (isGranted(grantedOrNotString)) {
            grantList.add(enumPrivilege.getText());
        } else {
            denyList.add(enumPrivilege.getText());
        }
    }

    private boolean isGranted(String granted) {
        return granted.equals("Y");
    }
    
    @Override
	public boolean deleteTenantRSSData(RSSEnvironmentContext ctx, int tenantId)
			throws RSSManagerException {
		boolean inTx = false;
		Database[] databases;
		DatabaseUser[] dbUsers;
		DatabasePrivilegeTemplate[] templates;
		try {
			// Delete tenant specific tables along with it's meta data
			databases = getRSSDAO().getDatabaseDAO().getDatabases(
					ctx.getEnvironmentName(), tenantId);
			log.info("Deleting rss tables and meta data");
			for (Database db : databases) {
				String databaseName = db.getName();
				String rssInstanceName = db.getRssInstanceName();
				dropDatabase(ctx, rssInstanceName, databaseName);
			}
			dbUsers = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(
					ctx.getEnvironmentName(), tenantId);
			log.info("Deleting rss users and meta data");
			for (DatabaseUser user : dbUsers) {
				String userName = user.getName();
				String rssInstanceName = user.getRssInstanceName();
				dropDatabaseUser(ctx, rssInstanceName, userName);
			}
			log.info("Deleting rss templates and meta data");
			templates = getRSSDAO().getDatabasePrivilegeTemplateDAO()
					.getDatabasePrivilegesTemplates(ctx.getEnvironmentName(),
							tenantId);
			inTx = this.getEntityManager().beginTransaction();
			for (DatabasePrivilegeTemplate template : templates) {
				dropDatabasePrivilegesTemplate(ctx, template.getName());
			}
			log.info("Successfully deleted rss data");

		} catch (Exception e) {
			if (inTx && getEntityManager().hasNoActiveTransaction()) {
				getEntityManager().rollbackTransaction();
			}
			String msg = "Error occurred while retrieving metadata "
					+ "corresponding to databases, from RSS metadata repository : "
					+ e.getMessage();
			handleException(msg, e);
		} finally {
			if (inTx) {
				getEntityManager().endTransaction();
			}
		}
		return true;	
	}
}
