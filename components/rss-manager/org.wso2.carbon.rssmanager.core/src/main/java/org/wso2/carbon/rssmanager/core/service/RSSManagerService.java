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
package org.wso2.carbon.rssmanager.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;


public class RSSManagerService {

    private static final Log log = LogFactory.getLog(RSSManagerService.class);

    public void createRSSInstance(RSSEnvironmentContext ctx,
                                  RSSInstance rssInstance) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).createRSSInstance(rssInstance);
        } catch (RSSManagerException e) {
            String msg =
                    "Error occurred while creating RSS instance '" + rssInstance.getName() + "'";
            handleException(msg, e);
        }
    }

    public void dropRSSInstance(RSSEnvironmentContext ctx) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).dropRSSInstance(ctx.getRssInstanceName());
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the RSS instance '" +
                    ctx.getRssInstanceName() + "'";
            handleException(msg, e);
        }
    }

    public void editRSSInstance(RSSEnvironmentContext ctx,
                                RSSInstance rssInstance) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).editRSSInstanceConfiguration(rssInstance);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the configuration of RSS instance '" +
                    rssInstance.getName() + "'";
            handleException(msg, e);
        }
    }

    public RSSInstance getRSSInstance(RSSEnvironmentContext ctx) throws RSSManagerException {
        RSSInstance metadata = null;
        try {
            RSSInstance rssInstance =
                    this.getRSSManager(ctx).getRSSInstance(ctx.getRssInstanceName());
            return rssInstance;
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the configuration of RSS instance '" +
                    ctx.getRssInstanceName() + "'";
            handleException(msg, e);
        }
        return metadata;
    }

    public RSSInstance[] getRSSInstances(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        RSSInstance[] rssInstances = new RSSInstance[0];
        try {
            rssInstances = getRSSManager(ctx).getRSSInstances();
        } catch (RSSManagerException e) {
            String msg = "Error occurred in retrieving the RSS instance list";
            handleException(msg, e);
        }
        return rssInstances;
    }

    public Database createDatabase(RSSEnvironmentContext ctx,
                                   Database database) throws RSSManagerException {
        try {
            return getRSSManager(ctx).createDatabase(database);
        } catch (RSSManagerException e) {
            String msg = "Error in creating the database '" + database.getName() + "'";
            handleException(msg, e);
        }
        return null;
    }

    public void dropDatabase(RSSEnvironmentContext ctx, String databaseName) throws
            RSSManagerException {
        try {
            this.getRSSManager(ctx).dropDatabase(ctx.getRssInstanceName(), databaseName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the database '" + databaseName + "'";
            handleException(msg, e);
        }
    }

    public Database[] getDatabases(RSSEnvironmentContext ctx) throws RSSManagerException {
        Database[] databases = new Database[0];
        try {
            databases = getRSSManager(ctx).getDatabases();
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the database list of the tenant";
            handleException(msg, e);
        }
        return databases;
    }

    public Database getDatabase(RSSEnvironmentContext ctx,
                                        String databaseName) throws RSSManagerException {
        Database database = null;

        try {
            database =
                    this.getRSSManager(ctx).getDatabase(ctx.getRssInstanceName(), databaseName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the configuration of the database '" +
                    databaseName + "'";
            handleException(msg, e);
        }
        return database;
    }

    public DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                           DatabaseUser user) throws RSSManagerException {
        try {
            return getRSSManager(ctx).createDatabaseUser(user);
        } catch (RSSManagerException e) {
            String msg =
                    "Error occurred while creating the database user '" + user.getName() + "'";
            handleException(msg, e);
        }
        return null;
    }

    public void dropDatabaseUser(RSSEnvironmentContext ctx,
                                 String username) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).dropDatabaseUser(ctx.getRssInstanceName(), username);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the user '" + username + "'";
            handleException(msg, e);
        }
    }

    public void editDatabaseUserPrivileges(RSSEnvironmentContext ctx,
                                           DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        this.getRSSManager(ctx).editDatabaseUserPrivileges(privileges, user, databaseName);
    }


    public DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx,
                                                String username) throws RSSManagerException {
        DatabaseUser user = null;
        try {
            user = this.getRSSManager(ctx).getDatabaseUser(ctx.getRssInstanceName(), username);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the database privileges of the user '" +
                    username + "'";
            handleException(msg, e);
        }
        return user;
    }

    public DatabaseUser[] getDatabaseUsers(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            users = this.getRSSManager(ctx).getDatabaseUsers();
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database user list";
            handleException(msg, e);
        }
        return users;
    }

    public void createDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).createDatabasePrivilegesTemplate(template);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while creating the database privilege template '" +
                    template.getName() + "'";
            handleException(msg, e);
        }
    }

    public void dropDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
                                               String templateName) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).dropDatabasePrivilegesTemplate(templateName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the database privilege template '" +
                    templateName + "'";
            handleException(msg, e);
        }
    }

    public void editDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).editDatabasePrivilegesTemplate(template);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the database privilege template " +
                    template.getName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager(ctx).getDatabasePrivilegeTemplates();
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException {
        return this.getRSSManager(ctx).getDatabasePrivilegeTemplate(templateName);
    }

//    public void createCarbonDataSource(String databaseName, String username) throws
//            RSSManagerException {
//        RSSDAO dao = RSSDAOFactory.getRSSDAO();
//
//        int tenantId = this.getCurrentTenantId();
//        PrivilegedCarbonContext.startTenantFlow();
//        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
//
//        Database database;
//        DatabaseUser user;
//        String dsName = null;
//        try {
//            database = dao.getDatabase(databaseName);
//            user = dao.getDatabaseUser(username);
//
//            DataSourceMetaInfo.DataSourceDefinition dsDef =
//                    new DataSourceMetaInfo.DataSourceDefinition();
//            dsDef.setDsXMLConfiguration(null);
//            dsDef.setType(null);
//
//            DataSourceMetaInfo metaInfo = new DataSourceMetaInfo();
//            dsName = database.getName() + "_" + user.getUsername();
//            metaInfo.setName(dsName);
//            metaInfo.setDefinition(dsDef);
//
//            RSSManagerServiceComponent.getDataSourceService().addDataSource(metaInfo);
//        } catch (RSSManagerException e) {
//            String msg = "Error occurred while creating datasource'" +
//                    username + "'";
//            handleException(msg, e);
//        } catch (DataSourceException e) {
//            String msg = "Error occurred while creating the datasource '" + dsName + "'";
//            handleException(msg, e);
//        } finally {
//            PrivilegedCarbonContext.endTenantFlow();
//        }
//    }

    private void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    public int getSystemRSSInstanceCount(RSSEnvironmentContext ctx) throws RSSManagerException {
        int count = 0;
        try {
            count = getRSSManager(ctx).getSystemRSSInstanceCount();
            return count;
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the system RSS instance count";
            handleException(msg, e);
        }
        return count;
    }

    public void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry ude,
                                     String templateName) throws RSSManagerException {
        try {
            getRSSManager(ctx).attachUserToDatabase(ude, templateName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while attaching database user '" + ude.getUsername() +
                    "' to the database '" + ude.getDatabaseName() + "' with the database user " +
                    "privileges define in the database privilege template '" + templateName + "'";
            handleException(msg, e);
        }
    }

    public void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry ude) throws RSSManagerException {
        try {
            this.getRSSManager(ctx).detachUserFromDatabase(ude);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while detaching the database user '" + ude.getUsername() +
                    "' from the database '" + ude.getDatabaseName() + "'";
            handleException(msg, e);
        }
    }

    public DatabaseUser[] getUsersAttachedToDatabase(
            RSSEnvironmentContext ctx, String databaseName) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            users = this.getRSSManager(ctx).getUsersAttachedToDatabase(ctx.getRssInstanceName(),
                    databaseName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database users attached to " +
                    "the database '" + databaseName + "'";
            handleException(msg, e);
        }
        return users;
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String databaseName) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            users = this.getRSSManager(ctx).getAvailableUsersToAttachToDatabase(
                    ctx.getRssInstanceName(), databaseName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database users available to be " +
                    "attached to the database '" + databaseName + "'";
            handleException(msg, e);
        }
        return users;
    }


    private RSSManager getRSSManager(RSSEnvironmentContext ctx) throws RSSManagerException {
        RSSConfig config = RSSConfig.getInstance();
        if (config == null) {
            throw new RSSManagerException("RSSConfig is not properly initialized and is null");
        }
        return config.getRSSManager(ctx);
    }

    public void createCarbonDataSource(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry entry) throws RSSManagerException {
        Database database = this.getRSSManager(ctx).getDatabase(entry.getRssInstanceName(),
                entry.getDatabaseName());
        DataSourceMetaInfo metaInfo =
                RSSManagerUtil.createDSMetaInfo(database, entry.getUsername());
        try {
            RSSManagerDataHolder.getInstance().getDataSourceService().addDataSource(metaInfo);
        } catch (DataSourceException e) {
            String msg = "Error occurred while creating carbon datasource for the database '" +
                    entry.getDatabaseName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeSet getUserDatabasePermissions(
            RSSEnvironmentContext ctx, String databaseName,
            String username) throws RSSManagerException {
        DatabasePrivilegeSet privileges = null;
        try {
            privileges = this.getRSSManager(ctx).getUserDatabasePrivileges(ctx.getRssInstanceName(),
                    databaseName, username);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the permissions granted to the user '" +
                    username + "' on database '" + databaseName + "'";
            handleException(msg, e);
        }
        return privileges;
    }

}
