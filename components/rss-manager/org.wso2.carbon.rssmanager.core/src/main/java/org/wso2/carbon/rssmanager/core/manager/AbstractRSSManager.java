/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.config.RSSConfiguration;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRSSManager implements RSSManager {

    private RSSConfiguration config;
    private RSSDAO rssDAO;
    private EntityManager entityManager;
    private static final Log log = LogFactory.getLog(RSSManager.class);

    public AbstractRSSManager(RSSConfiguration config) {
        this.config = config;

        /* Initializing RSS transaction manager wrapper */
        RSSTransactionManager rssTxManager =
                new RSSTransactionManager(RSSManagerDataHolder.getInstance().
                        getTransactionManager());
        this.entityManager = new EntityManager(rssTxManager);
        try {
            this.rssDAO = RSSDAOFactory.getRSSDAO(config.getRSSManagementRepository(), getEntityManager());
        } catch (RSSDAOException e) {
            throw new RuntimeException("Error occurred while initializing RSSDAO", e);
        }

        /* Initializing RSS environments managed by the RSS Manager */
        try {
            this.initEnvironments();
        } catch (RSSManagerException e) {
            throw new RuntimeException("Error occurred while initializing the RSS Manager " +
                    "environments", e);
        }
    }

    private void initEnvironments() throws RSSManagerException {
        for (RSSEnvironment environment : config.getRSSEnvironments()) {
            this.initRSSEnvironment(environment);
            environment.init();
        }
    }

    public abstract Database createDatabase(RSSEnvironmentContext ctx,
                                            Database database) throws RSSManagerException;

    public abstract void dropDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                                      String databaseName) throws RSSManagerException;

    public abstract DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                                    DatabaseUser user) throws RSSManagerException;

    public abstract void dropDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                          String username) throws RSSManagerException;

    public abstract void editDatabaseUserPrivileges(RSSEnvironmentContext ctx,
                                                    DatabasePrivilegeSet privileges,
                                                    DatabaseUser databaseUser,
                                                    String databaseName) throws RSSManagerException;

    public abstract void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry ude,
                                              String templateName) throws RSSManagerException;

    public abstract void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                                UserDatabaseEntry ude) throws RSSManagerException;

    public abstract RSSInstance resolveRSSInstance(RSSEnvironmentContext ctx, String databaseName)
            throws RSSManagerException;

    public abstract DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                                 String username) throws RSSManagerException;

    public abstract Database getDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                                         String databaseName) throws RSSManagerException;

    public abstract DatabaseUser[] getUsersAttachedToDatabase(
            RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException;

    public abstract DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException;

    public abstract DatabasePrivilegeSet getUserDatabasePrivileges(
            RSSEnvironmentContext ctx, String rssInstanceName, String databaseName,
            String username) throws RSSManagerException;

    //TODO
    public void initRSSEnvironment(RSSEnvironment environment) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            if (!getRSSDAO().getEnvironmentDAO().isEnvironmentExists(environment.getName())) {
                getRSSDAO().getEnvironmentDAO().addEnvironment(environment);
            }
            Map<String, RSSInstance> rssInstances = new HashMap<String, RSSInstance>();
            for (RSSInstance rssInstance : environment.getRSSInstances()) {
                rssInstances.put(rssInstance.getName(), rssInstance);
            }
            for (RSSInstance tmpInst :
                    getRSSDAO().getRSSInstanceDAO().getSystemRSSInstances(environment.getName(),
                            tenantId)) {
                RSSInstance reloadedRssInst = rssInstances.get(tmpInst.getName());
                RSSInstance prevKey = rssInstances.remove(tmpInst.getName());
                if (prevKey == null) {
                    log.warn("Configuration corresponding to RSS instance named '" + tmpInst.getName() +
                            "' is missing in the rss-config.xml");
                    continue;
                }
                getRSSDAO().getRSSInstanceDAO().updateRSSInstance(environment.getName(),
                        reloadedRssInst, tenantId);
            }
            for (RSSInstance inst : rssInstances.values()) {
                //Checks if the rss instance is one of wso2's rss instance's or if it is a user defined instance. Throws an error if it is neither.
                if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(inst.getInstanceType()) ||
                        RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE.equals(inst.getInstanceType())) {
                    getRSSDAO().getRSSInstanceDAO().addRSSInstance(environment.getName(), inst, tenantId);
                } else {
                    throw new RSSManagerException("The instance type '" + inst.getInstanceType() + "' is invalid.");
                }
            }
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while initialize RSS environment '" +environment.getName()
                    + "'";
            handleException(msg, e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public RSSInstance createRSSInstance(RSSEnvironmentContext ctx,
                                         RSSInstance rssInstance) throws RSSManagerException {
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

    public void dropRSSInstance(RSSEnvironmentContext ctx,
                                String rssInstanceName) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            //getDSWrapperRepository().removeRSSInstanceDSWrapper(rssInstanceName);
            getRSSDAO().getRSSInstanceDAO().removeRSSInstance(ctx.getEnvironmentName(),
                    rssInstanceName, tenantId);
            //TODO : Drop dependent databases etc.
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while removing metadata related to " +
                    "RSS instance '" + rssInstanceName + "' from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public void editRSSInstanceConfiguration(RSSEnvironmentContext ctx,
                                             RSSInstance rssInstance) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            getRSSDAO().getRSSInstanceDAO().updateRSSInstance(ctx.getEnvironmentName(), rssInstance,
                    tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while updating metadata related to " +
                    "RSS instance '" + rssInstance.getName() + "' in RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public RSSInstance[] getRSSInstances(RSSEnvironmentContext ctx) throws RSSManagerException {
        RSSInstance[] rssInstances = new RSSInstance[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstances =
                    getRSSDAO().getRSSInstanceDAO().getRSSInstances(ctx.getEnvironmentName(),
                            tenantId);
            if (!(tenantId == MultitenantConstants.SUPER_TENANT_ID)) {
                RSSInstance rrInst = getRoundRobinAssignedDatabaseServer(ctx);
                rrInst.setName(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
                rssInstances = Arrays.copyOf(rssInstances, rssInstances.length + 1);
                rssInstances[rssInstances.length - 1] = rrInst;
            }
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata related to " +
                    "RSS instances from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return rssInstances;
    }

    public Database[] getDatabases(RSSEnvironmentContext ctx) throws RSSManagerException {
        Database[] databases = new Database[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            databases =
                    getRSSDAO().getDatabaseDAO().getDatabases(ctx.getEnvironmentName(), tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return databases;
    }

    public DatabaseUser[] getDatabaseUsers(RSSEnvironmentContext ctx) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            users = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(ctx.getEnvironmentName(),
                    tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database users, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return users;
    }

    public RSSInstance getRoundRobinAssignedDatabaseServer(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        RSSInstance rssInstance = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            RSSInstance[] rssInstances =
                    getRSSDAO().getRSSInstanceDAO().getRSSInstances(ctx.getEnvironmentName(),
                            MultitenantConstants.SUPER_TENANT_ID);
            int count = getRSSDAO().getDatabaseDAO().getSystemRSSDatabaseCount(
                    ctx.getEnvironmentName());

            int rssInstanceCount = rssInstances.length;
            for (int i = 0; i < rssInstanceCount; i++) {
                if (i == count % rssInstanceCount) {
                    rssInstance = rssInstances[i];
                    if (rssInstance != null) {
                        return rssInstance;
                    }
                }
            }
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to the round robin assigned RSS instance, from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return rssInstance;
    }

    public void createDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            if (template == null) {
                getEntityManager().rollbackTransaction();
                String msg = "Database privilege template information cannot be null";
                log.error(msg);
                throw new RSSManagerException(msg);
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =
                    getRSSDAO().getDatabasePrivilegeTemplateDAO().isDatabasePrivilegeTemplateExist(
                            ctx.getEnvironmentName(), template.getName(), tenantId);
            if (isExist) {
                getEntityManager().rollbackTransaction();
                String msg = "A database privilege template named '" + template.getName() +
                        "' already exists";
                log.error(msg);
                throw new RSSManagerException(msg);
            }
            getRSSDAO().getDatabasePrivilegeTemplateDAO().addDatabasePrivilegesTemplate(
                    ctx.getEnvironmentName(), template, tenantId);
            getRSSDAO().getDatabasePrivilegeTemplateDAO().setPrivilegeTemplateProperties(
                    ctx.getEnvironmentName(), template, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while adding metadata related to " +
                    "database privilege template '" + template.getName() + "', to RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public void editDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            if (template == null) {
                getEntityManager().rollbackTransaction();
                String msg = "Database privilege template information cannot be null";
                log.error(msg);
                throw new RSSManagerException(msg);
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            getRSSDAO().getDatabasePrivilegeTemplateDAO().updateDatabasePrivilegesTemplate(
                    ctx.getEnvironmentName(), template, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while updating metadata " +
                    "corresponding to database privilege template '" + template.getName() +
                    "', in RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public RSSInstance getRSSInstance(RSSEnvironmentContext ctx,
                                      String rssInstanceName) throws RSSManagerException {
        RSSInstance rssInstance = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstance = getRSSDAO().getRSSInstanceDAO().getRSSInstance(ctx.getEnvironmentName(),
                    rssInstanceName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to RSS instance '" + rssInstanceName + "', from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return rssInstance;
    }

    public boolean isDatabaseExist(RSSEnvironmentContext ctx, String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseDAO().isDatabaseExist(ctx.getEnvironmentName(),
                    rssInstanceName, databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "named '" + databaseName + "' exists in RSS instance '" + rssInstanceName +
                    "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

    public boolean isDatabaseUserExist(RSSEnvironmentContext ctx, String rssInstanceName,
                                       String databaseUsername) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(ctx.getEnvironmentName(),
                    rssInstanceName, databaseUsername, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "user named '" + databaseUsername + "' already exists in RSS instance '" +
                    rssInstanceName + "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

    public boolean isDatabasePrivilegeTemplateExist(RSSEnvironmentContext ctx,
                                                    String templateName) throws RSSManagerException {
        boolean isExist = false;
        final boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabasePrivilegeTemplateDAO().isDatabasePrivilegeTemplateExist(
                    ctx.getEnvironmentName(), templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "privilege template named '" + templateName + "' already exists : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

    public void dropDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
                                               String templateName) throws RSSManagerException {
        final boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            getRSSDAO().getDatabasePrivilegeTemplateDAO().
                    removeDatabasePrivilegesTemplateEntries(ctx.getEnvironmentName(), templateName,
                            tenantId);
            getRSSDAO().getDatabasePrivilegeTemplateDAO().removeDatabasePrivilegesTemplate(
                    ctx.getEnvironmentName(), templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while removing metadata related to " +
                    "database privilege template '" + templateName + "', from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        DatabasePrivilegeTemplate[] templates = new DatabasePrivilegeTemplate[0];
        final boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            templates =
                    getRSSDAO().getDatabasePrivilegeTemplateDAO().getDatabasePrivilegesTemplates(
                            ctx.getEnvironmentName(), tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database privilege templates : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return templates;
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException {
        DatabasePrivilegeTemplate template = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            template = getRSSDAO().getDatabasePrivilegeTemplateDAO().getDatabasePrivilegesTemplate(
                    ctx.getEnvironmentName(), templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database privilege template '" + templateName +
                    "', from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return template;
    }

    public int getSystemRSSInstanceCount(RSSEnvironmentContext ctx) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            RSSInstance[] sysRSSInstances =
                    getRSSDAO().getRSSInstanceDAO().getRSSInstances(ctx.getEnvironmentName(),
                            MultitenantConstants.SUPER_TENANT_ID);
            return sysRSSInstances.length;
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving the system RSS instance count : " +
                    e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public Database[] getDatabasesRestricted(RSSEnvironmentContext ctx,
                                             int tenantId) throws RSSManagerException {
        boolean inTx = false;
        Database[] databases = new Database[0];
        try {
            inTx = getEntityManager().beginTransaction();
            databases =
                    getRSSDAO().getDatabaseDAO().getAllDatabases(ctx.getEnvironmentName(), tenantId);
        } catch (RSSDAOException e) {
            getEntityManager().rollbackTransaction();
            String msg = "Error occurred while retrieving databases list";
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return databases;
    }

    public RSSDAO getRSSDAO() {
        return rssDAO;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public String[] getEnvironmentNames() {
        String[] environments = new String[config.getRSSEnvironments().length];
        int i = 0;
        for (RSSEnvironment environment : config.getRSSEnvironments()) {
            environments[i] = environment.getName();
        }
        return environments;
    }

    public RSSEnvironment getEnvironment(RSSEnvironmentContext ctx) {
        for (RSSEnvironment environment : config.getRSSEnvironments()) {
            if (environment.getName().equals(ctx.getEnvironmentName())) {
                return environment;
            }
        }
        return null;
    }

    public void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    public void handleException(String msg) throws RSSManagerException {
        log.error(msg);
        throw new RSSManagerException(msg);
    }

    protected RSSInstance lookupRSSInstance(RSSEnvironmentContext ctx,
                                          String rssInstanceName) throws RSSManagerException {
        return (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) ?
                this.getRoundRobinAssignedDatabaseServer(ctx) :
                this.getRSSInstance(ctx, rssInstanceName);
    }

    protected String inferEntityType(RSSEnvironmentContext ctx,
                                   String rssInstanceName) throws RSSManagerException {
        return (getEnvironment(ctx).getDSWrapperRepository().getRSSInstanceDSWrapper(rssInstanceName) != null) ?
                RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE :
                RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE;
    }

    protected String inferUserType(String rssInstanceName) throws RSSManagerException {
        return (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(rssInstanceName)) ?
                RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE :
                RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE;
    }

    protected Connection getConnection(RSSEnvironmentContext ctx, String rssInstanceName) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper = getEnvironment(ctx).getDSWrapperRepository().
                getRSSInstanceDSWrapper(rssInstanceName);
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                    "associated with '" + ctx.getRssInstanceName() + "' RSS instance is null.");
        }
        return dsWrapper.getConnection();
    }

    protected Connection getConnection(RSSEnvironmentContext ctx, String rssInstanceName,
                                     String dbName) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper =
                getEnvironment(ctx).getDSWrapperRepository().getRSSInstanceDSWrapper(rssInstanceName);
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                    "associated with '" + rssInstanceName + "' RSS instance is null.");
        }
        return dsWrapper.getConnection(dbName);
    }
    
}
