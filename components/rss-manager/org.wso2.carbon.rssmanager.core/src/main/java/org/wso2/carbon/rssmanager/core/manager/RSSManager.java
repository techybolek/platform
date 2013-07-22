package org.wso2.carbon.rssmanager.core.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSInstanceDSWrapperRepository;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.XAConnection;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

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
        try {
            this.rssDAO = RSSDAOFactory.getRSSDAO(null);
        } catch (RSSDAOException e) {
            log.error("");
        }
        this.repository = new RSSInstanceDSWrapperRepository(getRSSEnvironment().getRSSInstances());
        this.init();
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

    public abstract void dropDatabase(String rssInstanceName,
                                      String databaseName) throws RSSManagerException;

    public abstract DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException;

    public abstract void dropDatabaseUser(String rssInstanceName,
                                          String username) throws RSSManagerException;

    public abstract void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                                    DatabaseUser databaseUser,
                                                    String databaseName) throws RSSManagerException;

    public abstract void attachUserToDatabase(UserDatabaseEntry ude, 
                                              String templateName) throws RSSManagerException;

    public abstract void detachUserFromDatabase(UserDatabaseEntry ude) throws RSSManagerException;

    public abstract RSSInstance resolveRSSInstance(String rssInstanceName,
                                                   String databaseName) throws RSSManagerException;

    public void createRSSEnvironment() throws RSSManagerException {
//        boolean inTx = beginTransaction();
//        try {
//            int tenantId = RSSManagerUtil.getTenantId();
//            if (!getRSSDAO().isEnvironmentExists(getRSSEnvironmentName(), tenantId)) {
//                getRSSDAO().createRSSEnvironment(getRSSEnvironment(), tenantId);
//            }
//            Map<String, RSSInstance> rssInstances = new HashMap<String, RSSInstance>();
//            for (RSSInstance rssInstance : getRSSEnvironment().getRSSInstances()) {
//                rssInstances.put(rssInstance.getName(), rssInstance);
//            }
//            for (RSSInstance tmpInst : getRSSDAO().getAllSystemRSSInstances(getRSSEnvironmentName())) {
//                RSSInstance reloadedRssInst = rssInstances.get(tmpInst.getName());
//                RSSInstance prevKey = rssInstances.remove(tmpInst.getName());
//                if (prevKey == null) {
//                    log.warn("Configuration corresponding to RSS instance named '" + tmpInst.getName() +
//                            "' is missing in the rss-config.xml");
//                    continue;
//                }
//                getRSSDAO().getRSSInstanceDAO().updateRSSInstance(getRSSEnvironmentName(),
//                        reloadedRssInst, tenantId);
//            }
//            for (RSSInstance inst : rssInstances.values()) {
//                getRSSDAO().getRSSInstanceDAO().addRSSInstance(getRSSEnvironmentName(), inst,
//                        tenantId);
//            }
//        } catch (RSSManagerException e) {
//            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
//                this.rollbackTransaction();
//            }
//            throw e;
//        } finally {
//            if (inTx) {
//                endTransaction();
//            }
//        }
    }

    public RSSInstance createRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
//        boolean inTx = beginTransaction();
//        try {
//            int tenantId = RSSManagerUtil.getTenantId();
//            this.getRSSDAO().getRSSInstanceDAO().addRSSInstance(getRSSEnvironmentName(),
//                    rssInstance, tenantId);
//            return rssInstance;
//        } catch (RSSManagerException e) {
//            if (inTx && this.getRSSTransactionManager().hasNoActiveTransaction()) {
//                this.rollbackTransaction();
//            }
//            throw e;
//        } finally {
//            if (inTx) {
//                endTransaction();
//            }
//        }
        return null;
    }

    public void dropRSSInstance(String rssInstanceName) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            getDSWrapperRepository().removeRSSInstanceDSWrapper(rssInstanceName);
            getRSSDAO().getRSSInstanceDAO().removeRSSInstance(rssInstanceName, tenantId);
            //TODO : Drop dependent databases etc.
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while removing metadata related to " +
                    "RSS instance '" + rssInstanceName + "' from RSS metadata repository : " +
                    e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public void editRSSInstanceConfiguration(RSSInstance rssInstance) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            getRSSDAO().getRSSInstanceDAO().updateRSSInstance(rssInstance, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while updating metadata related to " +
                    "RSS instance '" + rssInstance.getName() + "' in RSS metadata repository : " +
                    e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public RSSInstance[] getRSSInstances() throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance[] rssInstances = getRSSDAO().getRSSInstanceDAO().getRSSInstances(tenantId);

            if (!(tenantId == MultitenantConstants.SUPER_TENANT_ID)) {
                RSSInstance tmp = getRoundRobinAssignedDatabaseServer();
                tmp.setName(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                rssInstances[rssInstances.length] = tmp;
            }
            return rssInstances;
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "RSS instances from RSS metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public Database[] getDatabases() throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabaseDAO().getDatabases(tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata " +
                    "corresponding to database users, from RSS metadata repository : " +
                    e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public RSSInstance getRoundRobinAssignedDatabaseServer() throws RSSManagerException {
        RSSInstance rssInstance = null;
        boolean inTx = beginTransaction();
        try {
            RSSInstance[] rssInstances =
                    getRSSDAO().getRSSInstanceDAO().getRSSInstances(
                            MultitenantConstants.SUPER_TENANT_ID);
            int count = getRSSDAO().getDatabaseDAO().getSystemRSSDatabaseCount();

            int rssInstanceCount = rssInstances.length;
            for (int i = 0; i < rssInstanceCount; i++) {
                if (i == count % rssInstanceCount) {
                    rssInstance = rssInstances[i];
                    if (rssInstance != null) {
                        return rssInstance;
                    }
                }
            }
            return rssInstance;
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata " +
                    "corresponding to the round robin assigned RSS instance, from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public void createDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            if (template == null) {
                rollbackTransaction();
                throw new RSSManagerException("Database privilege template information " +
                        "cannot be null");
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =
                    getRSSDAO().getDatabasePrivilegeTemplateDAO().isDatabasePrivilegeTemplateExist(
                            template.getName(), tenantId);
            if (isExist) {
                rollbackTransaction();
                throw new RSSManagerException("A database privilege template named '" +
                        template.getName() + "' already exists");
            }
            getRSSDAO().getDatabasePrivilegeTemplateDAO().addDatabasePrivilegesTemplate(template,
                    tenantId);
            getRSSDAO().getDatabasePrivilegeTemplateDAO().setPrivilegeTemplateProperties(template,
                    tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while adding metadata related to " +
                    "database privilege template '" + template.getName() + "', to RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public void editDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            if (template == null) {
                rollbackTransaction();
                throw new RSSManagerException("Database privilege template information " +
                        "cannot be null");
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            getRSSDAO().getDatabasePrivilegeTemplateDAO().updateDatabasePrivilegesTemplate(template,
                    tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while updating metadata " +
                    "corresponding to database privilege template '" + template.getName() +
                    "', in RSS metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public RSSInstance getRSSInstance(String rssInstanceName) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getRSSInstanceDAO().getRSSInstance(rssInstanceName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata " +
                    "corresponding to RSS instance '" + rssInstanceName + "', from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

//    public Database getDatabase(String rssInstanceName,
//                                String databaseName) throws RSSManagerException {
//        Database database;
//        boolean inTx = beginTransaction();
//        try {
//            RSSInstance rssInstance =
//                    getRSSDAO().findRSSInstanceDatabaseBelongsTo(rssInstanceName, databaseName);
//            if (rssInstance == null) {
//                if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
//                    rollbackTransaction();
//                }
//                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
//                        "in RSS instance '" + rssInstanceName + "'");
//            }
//            database = getRSSDAO().getDatabaseDAO().getDatabase(rssInstance, databaseName,
//                    RSSManagerDataHolder.getInstance().getTenantId());
//
//            if (inTx) {
//                endTransaction();
//            }
//        } catch (RSSManagerException e) {
//            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
//                rollbackTransaction();
//            }
//            throw e;
//        }
//        return database;
//    }

    public boolean isDatabaseExist(String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabaseDAO().isDatabaseExist(rssInstanceName, databaseName,
                    tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while checking whether the database " +
                    "named '" + databaseName + "' exists in RSS instance '" + rssInstanceName +
                    "' : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public boolean isDatabaseUserExist(String rssInstanceName,
                                       String databaseUsername) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(rssInstanceName,
                    databaseUsername, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while checking whether the database " +
                    "user named '" + databaseUsername + "' already exists in RSS instance '" +
                    rssInstanceName + "' : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public boolean isDatabasePrivilegeTemplateExist(String templateName) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabasePrivilegeTemplateDAO().isDatabasePrivilegeTemplateExist(
                    templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while checking whether the database " +
                    "privilege template named '" + templateName + "' already exists : " +
                    e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

//    public DatabaseUser getDatabaseUser(String rssInstanceName,
//                                        String username) throws RSSManagerException {
//        DatabaseUser user;
//        boolean inTx = beginTransaction();
//        try {
//            int tenantId = RSSManagerDataHolder.getInstance().getTenantId();
//            boolean isExist = getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(
//                    rssInstanceName, username, tenantId);
//            if (isExist) {
//                rollbackTransaction();
//                throw new RSSManagerException("Database user '" + username + "' already exists " +
//                        "in the RSS instance '" + rssInstanceName + "'");
//            }
//
//            RSSInstance rssInstance =
//                    getRSSDAO().findRSSInstanceDatabaseUserBelongsTo(rssInstanceName, username);
//            if (rssInstance == null) {
//                rollbackTransaction();
//                throw new RSSManagerException("Database user '" + username + "' does not exist " +
//                        "in RSS instance '" + rssInstanceName + "'");
//            }
//            user = getRSSDAO().getDatabaseUserDAO().getDatabaseUser(rssInstance.getName(),
//                    username, tenantId);
//
//            if (inTx) {
//                endTransaction();
//            }
//            return user;
//        } catch (RSSManagerException e) {
//            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
//                rollbackTransaction();
//            }
//            throw e;
//        }
//    }

    public void dropDatabasePrivilegesTemplate(String templateName) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            getRSSDAO().getDatabasePrivilegeTemplateDAO().
                    removeDatabasePrivilegesTemplateEntries(templateName, tenantId);
            getRSSDAO().getDatabasePrivilegeTemplateDAO().removeDatabasePrivilegesTemplate(
                    templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while removing metadata related to " +
                    "database privilege template '" + templateName + "', from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates() throws
            RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabasePrivilegeTemplateDAO().getDatabasePrivilegesTemplates(
                    tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata " +
                    "corresponding to database privilege templates : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            String templateName) throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabasePrivilegeTemplateDAO().getDatabasePrivilegesTemplate(
                    templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata " +
                    "corresponding to database privilege template '" + templateName +
                    "', from RSS metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

    public int getSystemRSSInstanceCount() throws RSSManagerException {
        boolean inTx = beginTransaction();
        try {
            RSSInstance[] sysRSSInstances =
                    getRSSDAO().getRSSInstanceDAO().getRSSInstances(
                            MultitenantConstants.SUPER_TENANT_ID);
            return sysRSSInstances.length;
        } catch (RSSDAOException e) {
            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
                rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving the system RSS " +
                    "instance count : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                endTransaction();
            }
        }
    }

//    public String[] getUsersAttachedToDatabase(
//            String rssInstanceName, String databaseName) throws RSSManagerException {
//        boolean inTx = beginTransaction();
//        try {
//            RSSInstance rssInstance =
//                    getRSSDAO().findRSSInstanceDatabaseBelongsTo(rssInstanceName, databaseName);
//            if (rssInstance == null) {
//                rollbackTransaction();
//                throw new RSSManagerException("Database '" + databaseName
//                        + "' does not exist " + "in RSS instance '"
//                        + rssInstanceName + "'");
//            }
//            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
//                List<String> systemUsers =
//                        getRSSDAO().getSystemUsersAssignedToDatabase(rssInstance, databaseName);
//                return systemUsers.toArray(new String[systemUsers.size()]);
//            }
//            List<String> existingUsers =
//                    getRSSDAO().getUsersAssignedToDatabase(rssInstance, databaseName,
//                            RSSManagerDataHolder.getInstance().getTenantId());
//
//            if (inTx) {
//                endTransaction();
//            }
//            return existingUsers.toArray(new String[existingUsers.size()]);
//        } catch (RSSManagerException e) {
//            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
//                rollbackTransaction();
//            }
//            throw e;
//        }
//    }

//    public String[] getAvailableUsersToAttachToDatabase(
//            String rssInstanceName, String databaseName) throws RSSManagerException {
//        boolean inTx = beginTransaction();
//        try {
//            RSSInstance rssInstance =
//                    getRSSDAO().findRSSInstanceDatabaseBelongsTo(rssInstanceName, databaseName);
//            List<String> availableUsers = new ArrayList<String>();
//
//            List<String> existingUsers;
//            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
//                existingUsers = getRSSDAO().getSystemUsersAssignedToDatabase(rssInstance, databaseName);
//            } else {
//                existingUsers =
//                        getRSSDAO().getUsersAssignedToDatabase(rssInstance, databaseName,
//                                RSSManagerDataHolder.getInstance().getTenantId());
//            }
//
////            List<String> existingUsers =
////                    getUsersAttachedToDatabase(rssInstanceName, databaseName);
//            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
//                for (DatabaseUser user : getRSSDAO().getSystemCreatedDatabaseUsers()) {
//                    String username = user.getUsername();
//                    if (!existingUsers.contains(username)) {
//                        availableUsers.add(username);
//                    }
//                }
//            } else {
//                for (DatabaseUser user : getRSSDAO().getUsersByRSSInstance(rssInstance,
//                        RSSManagerDataHolder.getInstance().getTenantId())) {
//                    String username = user.getUsername();
//                    if (!existingUsers.contains(username)) {
//                        availableUsers.add(username);
//                    }
//                }
//            }
//            if (inTx) {
//                endTransaction();
//            }
//            return availableUsers.toArray(new String[availableUsers.size()]);
//        } catch (RSSManagerException e) {
//            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
//                rollbackTransaction();
//            }
//            throw e;
//        }
//    }

//    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName,
//                                                          String databaseName,
//                                                          String username) throws RSSManagerException {
//        boolean inTx = beginTransaction();
//        try {
//            RSSInstance rssInstance =
//                    getRSSDAO().findRSSInstanceDatabaseBelongsTo(rssInstanceName, databaseName);
//            if (rssInstance == null) {
//                if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
//                    rollbackTransaction();
//                }
//                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
//                        "in RSS instance '" + rssInstanceName + "'");
//            }
//            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) {
//                return getRSSDAO().getSystemUserDatabasePrivileges(rssInstance,
//                        databaseName, username);
//            }
//            DatabasePrivilegeSet privileges =
//                    getRSSDAO().getUserDatabasePrivileges(rssInstance, databaseName, username,
//                            RSSManagerDataHolder.getInstance().getTenantId());
//            if (inTx) {
//                endTransaction();
//            }
//            return privileges;
//        } catch (RSSManagerException e) {
//            if (inTx && getRSSTransactionManager().hasNoActiveTransaction()) {
//                rollbackTransaction();
//            }
//            throw e;
//        }
//    }

    //Newly added methods   TODO : Properly handle these

    public abstract DatabaseUser getDatabaseUser(String rssInstanceName,
                                                 String username) throws RSSManagerException;

    public abstract Database getDatabase(String rssInstanceName,
                                         String databaseName) throws RSSManagerException;

    public abstract DatabaseUser[] getUsersAttachedToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException;

    public abstract DatabaseUser[] getAvailableUsersToAttachToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException;

    public abstract DatabasePrivilegeSet getUserDatabasePrivileges(
            String rssInstanceName, String databaseName, String username) throws RSSManagerException;

    private void init() {
        TransactionManager txMgr = RSSManagerDataHolder.getInstance().getTransactionManager();
        txManager = new RSSTransactionManager(txMgr);
    }

    public RSSTransactionManager getRSSTransactionManager() {
        return txManager;
    }

    public boolean isInTransaction() {
        return activeNestedTransactions.get() > 0;
    }

    public boolean beginTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("beginTransaction()");
        }
        if (isInTransaction() || !getRSSTransactionManager().hasNoActiveTransaction()) {
            return false;
        }
        if (activeNestedTransactions.get() == 0) {
            getRSSTransactionManager().begin();
        }
        activeNestedTransactions.set(activeNestedTransactions.get() + 1);
        return true;
    }

    public void endTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("endTransaction()");
        }
        activeNestedTransactions.set(activeNestedTransactions.get() - 1);
        /* commit all only if we are at the outer most transaction */
        if (activeNestedTransactions.get() == 0) {
            getRSSTransactionManager().commit();
        } else if (activeNestedTransactions.get() < 0) {
            activeNestedTransactions.set(0);
        }
    }

    public void rollbackTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("rollbackTransaction()");
        }
        if (log.isDebugEnabled()) {
            log.debug("getRSSTxManager().rollback()");
        }
        getRSSTransactionManager().rollback();
        activeNestedTransactions.set(0);
    }

    public synchronized Connection createConnection(DataSource dataSource) throws RSSManagerException {
        Connection conn;
        try {
            conn = dataSource.getConnection();
            if (conn instanceof XAConnection && isInTransaction()) {
                Transaction tx =
                        getRSSTransactionManager().getTransactionManager().getTransaction();
                XAResource xaRes = ((XAConnection) conn).getXAResource();
                if (!isXAResourceEnlisted(xaRes)) {
                    tx.enlistResource(xaRes);
                    addToEnlistedXADataSources(xaRes);
                }
            }
            return conn;
        } catch (Exception e) {
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

    public RSSDAO getRSSDAO() {
        return rssDAO;
    }

    public RSSInstanceDSWrapperRepository getDSWrapperRepository() {
        return repository;
    }

    public RSSEnvironment getRSSEnvironment() {
        return rssEnvironment;
    }

    public String getRSSEnvironmentName() {
        return rssEnvironmentName;
    }

}
