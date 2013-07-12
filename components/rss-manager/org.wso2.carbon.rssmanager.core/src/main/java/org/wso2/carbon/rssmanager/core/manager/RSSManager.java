package org.wso2.carbon.rssmanager.core.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSInstanceDSWrapperRepository;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.XAConnection;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class RSSManager {

    private RSSDAO rssDAO;

    private String rssEnvironmentName;

    private RSSEnvironment rssEnvironment;

    private RSSTransactionManager txManager;

    private RSSInstanceDSWrapperRepository repository;

    private static final Log log = LogFactory.getLog(RSSManager.class);

    public RSSManager(RSSEnvironment rssEnvironment) {
        this.rssEnvironment = rssEnvironment;
        this.rssEnvironmentName = getRSSEnvironment().getName();
        this.rssDAO = RSSDAOFactory.getRSSDAO();
        this.repository = new RSSInstanceDSWrapperRepository(getRSSEnvironment().getRSSInstances());
        try {
            this.init();
        } catch (RSSManagerException e) {
            log.error("RSS manager associated with RSS environment '" +
                    getRSSEnvironment().getName() + "' is not initialized properly and is null");
        }
    }

    /**
     * Thread local variable to track the status of active nested transactions
     */
    private static ThreadLocal<Integer> activeNestedTransactions = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return 0;
        }
    };

    /**
     * This is used to keep the enlisted XADatasource objects
     */
    private static ThreadLocal<Set<XAResource>> enlistedXADataSources = new ThreadLocal<Set<XAResource>>() {
        protected Set<XAResource> initialValue() {
            return new HashSet<XAResource>();
        }
    };

    public abstract Database createDatabase(Database database) throws RSSManagerException;

    public abstract void dropDatabase(String rssInstanceName, String databaseName) throws
            RSSManagerException;

    public abstract DatabaseUser createDatabaseUser(DatabaseUser databaseUser) throws RSSManagerException;

    public abstract void dropDatabaseUser(String rssInstanceName, String username) throws
            RSSManagerException;

    public abstract void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                                    DatabaseUser databaseUser,
                                                    String databaseName) throws RSSManagerException;

    public abstract void attachUserToDatabase(String rssInstanceName, String databaseName,
                                              String username, String templateName) throws
            RSSManagerException;

    public abstract void detachUserFromDatabase(String rssInstanceName, String databaseName,
                                                String username) throws RSSManagerException;

    public void createRSSEnvironment() throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            if (!getDAO().isEnvironmentExists(getRSSEnvironmentName(), tenantId)) {
                getDAO().createRSSEnvironment(getRSSEnvironment(), tenantId);
            }
            Map<String, RSSInstance> rssInstances = new HashMap<String, RSSInstance>();
            for (RSSInstance rssInstance : getRSSEnvironment().getRSSInstances()) {
                rssInstances.put(rssInstance.getName(), rssInstance);
            }
            for (RSSInstance tmpInst : getDAO().getAllSystemRSSInstances(getRSSEnvironmentName())) {
                RSSInstance reloadedRssInst = rssInstances.get(tmpInst.getName());
                RSSInstance prevKey = rssInstances.remove(tmpInst.getName());
                if (prevKey == null) {
                    log.warn("Configuration corresponding to RSS instance named '" + tmpInst.getName() +
                            "' is missing in the rss-config.xml");
                    continue;
                }
                getDAO().updateRSSInstance(getRSSEnvironmentName(), reloadedRssInst, tenantId);
            }
            for (RSSInstance inst : rssInstances.values()) {
                getDAO().createRSSInstance(getRSSEnvironmentName(), inst, tenantId);
            }
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

    public RSSInstance createRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            this.getDAO().createRSSInstance(getRSSEnvironmentName(), rssInstance, tenantId);

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

    public void dropRSSInstance(String rssInstanceName) throws RSSManagerException {
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
                    this.getDAO().getRSSInstance(getRSSEnvironmentName(), rssInstanceName, tenantId);

            this.getDAO().dropRSSInstance(getRSSEnvironmentName(), rssInstanceName,
                    RSSManagerUtil.getTenantId());
            //TODO : Drop dependent databases etc.
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

    public void editRSSInstanceConfiguration(RSSInstance rssInstance) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            this.getDAO().updateRSSInstance(getRSSEnvironmentName(), rssInstance, tenantId);
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

    public RSSInstanceMetaData[] getRSSInstances() throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance[] tmpList =
                    this.getDAO().getAllRSSInstances(getRSSEnvironmentName(), tenantId);
            List<RSSInstanceMetaData> rssInstances = new ArrayList<RSSInstanceMetaData>();
            for (RSSInstance tmpIns : tmpList) {
                RSSInstanceMetaData rssIns =
                        RSSManagerUtil.convertToRSSInstanceMetadata(tmpIns, tenantId);
                rssInstances.add(rssIns);
            }
            if (inTransaction) {
                this.endTransaction();
            }
            if (!(tenantId == MultitenantConstants.SUPER_TENANT_ID)) {
                RSSInstance tmp = getRoundRobinAssignedDatabaseServer();
                tmp.setName(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                RSSInstanceMetaData rssInsST =
                        RSSManagerUtil.convertToRSSInstanceMetadata(tmp, tenantId);
                rssInstances.add(rssInsST);
            }
            return rssInstances.toArray(new RSSInstanceMetaData[rssInstances.size()]);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public DatabaseMetaData[] getDatabases() throws RSSManagerException {
        List<DatabaseMetaData> databases = new ArrayList<DatabaseMetaData>();
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            Database[] tmpList = this.getDAO().getAllDatabases(getRSSEnvironmentName(), tenantId);
            for (Database database : tmpList) {
                DatabaseMetaData metadata =
                        RSSManagerUtil.convertToDatabaseMetadata(database, tenantId);
                databases.add(metadata);
            }
            if (inTransaction) {
                this.endTransaction();
            }
            return databases.toArray(new DatabaseMetaData[databases.size()]);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public DatabaseUserMetaData[] getDatabaseUsers() throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            DatabaseUserMetaData[] users =
                    this.getDAO().getAllDatabaseUsers(getRSSEnvironmentName(), tenantId);

            if (inTransaction) {
                this.endTransaction();
            }
            return users;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public RSSInstance getRoundRobinAssignedDatabaseServer() throws
            RSSManagerException {
        RSSInstance rssIns = null;
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            RSSInstance[] rssInstances =
                    this.getDAO().getAllSystemRSSInstances(getRSSEnvironmentName());
            int count = this.getDAO().getSystemRSSDatabaseCount(getRSSEnvironmentName());

            for (int i = 0; i < rssInstances.length; i++) {
                if (i == count % rssInstances.length) {
                    rssIns = rssInstances[i];
                    if (rssIns != null) {
                        return rssIns;
                    }
                }
            }
            if (inTransaction) {
                this.endTransaction();
            }
            return rssIns;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public void createDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            if (template == null) {
                this.rollbackTransaction();
                throw new RSSManagerException("Database privilege template information " +
                        "cannot be null");
            }

            int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =
                    this.getDAO().isDatabasePrivilegeTemplateExist(getRSSEnvironmentName(),
                            template.getName(), tenantId);
            if (isExist) {
                this.rollbackTransaction();
                throw new RSSManagerException("Database privilege template '" + template.getName() +
                        "' already exists");
            }

            template = this.getDAO().createDatabasePrivilegesTemplate(getRSSEnvironmentName(),
                    template, tenantId);
            this.getDAO().setDatabasePrivilegeTemplateProperties(getRSSEnvironmentName(), template,
                    tenantId);
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

    public void editDatabasePrivilegesTemplate(DatabasePrivilegeTemplate template) throws
            RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            if (template == null) {
                this.rollbackTransaction();
                throw new RSSManagerException("Database privilege template information " +
                        "cannot be null");
            }

            int tenantId = RSSManagerUtil.getTenantId();
            this.getDAO().editDatabasePrivilegesTemplate(getRSSEnvironmentName(), template,
                    tenantId);

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

    public RSSInstance getRSSInstance(String rssInstanceName) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance = this.getDAO().getRSSInstance(getRSSEnvironmentName(),
                    rssInstanceName, tenantId);
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

    public RSSInstance getSystemRSSInstance(String rssInstanceName) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            RSSInstance rssInstance = this.getDAO().getSystemRSSInstance(getRSSEnvironmentName(),
                    rssInstanceName);
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

    public Database getDatabase(String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        Database database;
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
                    this.getDAO().findRSSInstanceDatabaseBelongsTo(getRSSEnvironmentName(),
                            rssInstanceName, databaseName,
                            tenantId);
            if (rssInstance == null) {
                if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }

            database = this.getDAO().getDatabase(getRSSEnvironmentName(), rssInstance, databaseName,
                    tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
        return database;
    }

    public boolean isDatabaseExist(String rssInstanceName, String databaseName) throws
            RSSManagerException {
        boolean isExists;
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            isExists = this.getDAO().isDatabaseExist(getRSSEnvironmentName(), rssInstanceName,
                    databaseName, tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
            return isExists;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public boolean isDatabaseUserExist(String rssInstanceName, String databaseUsername) throws
            RSSManagerException {
        boolean isExists;
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            isExists =
                    this.getDAO().isDatabaseUserExist(getRSSEnvironmentName(), rssInstanceName,
                            databaseUsername, tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
            return isExists;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public boolean isDatabasePrivilegeTemplateExist(String templateName) throws
            RSSManagerException {
        boolean isExist;
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            isExist = this.getDAO().isDatabasePrivilegeTemplateExist(getRSSEnvironmentName(),
                    templateName, tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
            return isExist;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        DatabaseUser user;
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist = this.getDAO().isDatabaseUserExist(getRSSEnvironmentName(),
                    rssInstanceName, username, tenantId);
            if (!isExist) {
                this.rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' already exists " +
                        "in the RSS instance '" + rssInstanceName + "'");
            }

            RSSInstance rssInstance =
                    this.getDAO().findRSSInstanceDatabaseUserBelongsTo(getRSSEnvironmentName(),
                            rssInstanceName, username,
                            tenantId);
            if (rssInstance == null) {
                this.rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            user = this.getDAO().getDatabaseUser(getRSSEnvironmentName(), rssInstance, username,
                    tenantId);

            if (inTransaction) {
                this.endTransaction();
            }
            return user;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public void dropDatabasePrivilegesTemplate(String templateName) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            this.getDAO().removeDatabasePrivilegesTemplateEntries(getRSSEnvironmentName(),
                    templateName, tenantId);
            this.getDAO().dropDatabasePrivilegesTemplate(getRSSEnvironmentName(), templateName,
                    tenantId);

            if (inTransaction) {
                this.endTransaction();
            }
        } catch (RSSManagerException e) {
            this.rollbackTransaction();
            throw e;
        }
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates() throws
            RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            DatabasePrivilegeTemplate[] templates =
                    this.getDAO().getAllDatabasePrivilegesTemplates(getRSSEnvironmentName(),
                            tenantId);

            if (inTransaction) {
                this.endTransaction();
            }
            return templates;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            String templateName) throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            DatabasePrivilegeTemplate template =
                    this.getDAO().getDatabasePrivilegesTemplate(getRSSEnvironmentName(),
                            templateName, tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
            return template;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public int getSystemRSSInstanceCount() throws RSSManagerException {
        boolean inTransaction = false;
        if (!this.isInTransaction()) {
            if (!this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.beginTransaction();
                inTransaction = true;
            }
        }
        try {
            int count = this.getDAO().getAllSystemRSSInstances(getRSSEnvironmentName()).length;
            if (inTransaction) {
                this.endTransaction();
            }
            return count;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public DatabaseUserMetaData[] getUsersAttachedToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
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
                    this.getDAO().findRSSInstanceDatabaseBelongsTo(getRSSEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            if (rssInstance == null) {
                this.rollbackTransaction();
                throw new RSSManagerException("Database '" + databaseName
                        + "' does not exist " + "in RSS instance '"
                        + rssInstanceName + "'");
            }
            DatabaseUserMetaData[] existingUsers;
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
                existingUsers =
                        this.getDAO().getSystemUsersAssignedToDatabase(getRSSEnvironmentName(),
                                rssInstance, databaseName);
                return existingUsers;
            }
            existingUsers = this.getDAO().getUsersAssignedToDatabase(getRSSEnvironmentName(),
                    rssInstance, databaseName, tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
            return existingUsers;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public DatabaseUserMetaData[] getAvailableUsersToAttachToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
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
                    this.getDAO().findRSSInstanceDatabaseBelongsTo(getRSSEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            List<DatabaseUserMetaData> availableUsers = new ArrayList<DatabaseUserMetaData>();

            DatabaseUserMetaData[] existingUsers;
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
                existingUsers =
                        this.getDAO().getSystemUsersAssignedToDatabase(getRSSEnvironmentName(),
                                rssInstance, databaseName);
            } else {
                existingUsers =
                        this.getDAO().getUsersAssignedToDatabase(getRSSEnvironmentName(),
                                rssInstance, databaseName,
                                tenantId);
            }
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
                for (DatabaseUser user : this.getDAO().getSystemCreatedDatabaseUsers(
                        getRSSEnvironmentName())) {
                    if (!isDatabaseUserExist(existingUsers, user)) {
                        availableUsers.add(
                                RSSManagerUtil.convertToDatabaseUserMetadata(user, tenantId));
                    }
                }
            } else {
                for (DatabaseUser user : this.getDAO().getUsersByRSSInstance(getRSSEnvironmentName(),
                        rssInstance, tenantId)) {
                    if (!isDatabaseUserExist(existingUsers, user)) {
                        availableUsers.add(
                                RSSManagerUtil.convertToDatabaseUserMetadata(user, tenantId));
                    }
                }
            }
            if (inTransaction) {
                this.endTransaction();
            }
            return availableUsers.toArray(new DatabaseUserMetaData[availableUsers.size()]);
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
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
                    this.getDAO().findRSSInstanceDatabaseBelongsTo(getRSSEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            if (rssInstance == null) {
                if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                    this.rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
                return this.getDAO().getSystemUserDatabasePrivileges(getRSSEnvironmentName(),
                        rssInstance, databaseName, username);
            }
            DatabasePrivilegeSet privileges =
                    this.getDAO().getUserDatabasePrivileges(getRSSEnvironmentName(), rssInstance,
                            databaseName, username,
                            tenantId);
            if (inTransaction) {
                this.endTransaction();
            }
            return privileges;
        } catch (RSSManagerException e) {
            if (inTransaction && this.getRSSTransactionManager().hasNoActiveTransaction()) {
                this.rollbackTransaction();
            }
            throw e;
        }
    }

    private void init() throws RSSManagerException {
        TransactionManager txMgr = RSSManagerUtil.getTransactionManager();
        this.txManager = new RSSTransactionManager(txMgr);
    }

    public RSSTransactionManager getRSSTransactionManager() {
        return txManager;
    }

    public boolean isInTransaction() {
        return activeNestedTransactions.get() > 0;
    }

    public synchronized void beginTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("beginTransaction()");
        }
        if (activeNestedTransactions.get() == 0) {
            this.getRSSTransactionManager().begin();
        }
        activeNestedTransactions.set(activeNestedTransactions.get() + 1);
    }

    public synchronized void endTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("endTransaction()");
        }
        activeNestedTransactions.set(activeNestedTransactions.get() - 1);
        /* commit all only if we are at the outer most transaction */
        if (activeNestedTransactions.get() == 0) {
            this.getRSSTransactionManager().commit();
        } else if (activeNestedTransactions.get() < 0) {
            activeNestedTransactions.set(0);
        }
    }

    public synchronized void rollbackTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("rollbackTransaction()");
        }
        if (log.isDebugEnabled()) {
            log.debug("this.getRSSTxManager().rollback()");
        }
        this.getRSSTransactionManager().rollback();
        activeNestedTransactions.set(0);
    }

    public synchronized Connection createConnection(javax.sql.DataSource dataSource) throws RSSManagerException {
        Connection conn;
        try {
            conn = dataSource.getConnection();
            if (conn instanceof XAConnection && isInTransaction()) {
                Transaction tx =
                        this.getRSSTransactionManager().getTransactionManager().getTransaction();
                XAResource xaRes = ((XAConnection) conn).getXAResource();
                if (!isXAResourceEnlisted(xaRes)) {
                    tx.enlistResource(xaRes);
                    addToEnlistedXADataSources(xaRes);
                }
            }
            return conn;
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating datasource connection : " +
                    e.getMessage(), e);
        } catch (SystemException e) {
            throw new RSSManagerException("Error occurred while creating datasource connection : " +
                    e.getMessage(), e);
        } catch (RollbackException e) {
            throw new RSSManagerException("Error occurred while creating datasource connection : " +
                    e.getMessage(), e);
        }
    }

    /**
     * This method adds XAResource object to enlistedXADataSources Threadlocal set
     *
     * @param resource XA resource associated with the connection
     */
    private synchronized void addToEnlistedXADataSources(XAResource resource) {
        enlistedXADataSources.get().add(resource);
    }

    private synchronized boolean isXAResourceEnlisted(XAResource resource) {
        return enlistedXADataSources.get().contains(resource);
    }

    private static boolean isDatabaseUserExist(DatabaseUserMetaData[] users,
                                               DatabaseUser targetUser) {
        for (DatabaseUserMetaData user : users) {
            if (user.getUsername().equals(targetUser.getUsername())) {
                return true;
            }
        }
        return false;
    }

    public void cleanupResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
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

    public RSSEnvironment getRSSEnvironment() {
        return rssEnvironment;
    }


    public RSSDAO getDAO() {
        return rssDAO;
    }

    public RSSInstanceDSWrapperRepository getDSWrapperRepository() {
        return repository;
    }

    public String getRSSEnvironmentName() {
        return rssEnvironmentName;
    }



}
