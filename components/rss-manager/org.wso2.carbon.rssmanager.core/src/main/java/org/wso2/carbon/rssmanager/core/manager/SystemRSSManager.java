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

package org.wso2.carbon.rssmanager.core.manager;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfiguration;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SystemRSSManager extends AbstractRSSManager {

    public SystemRSSManager(RSSConfiguration config) {
        super(config);
    }

    public DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                        String username) throws RSSManagerException {
        DatabaseUser user = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByUser(
                            ctx.getEnvironmentName(), rssInstanceName, username, tenantId);
            if (rssInstance == null) {
                getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            user = getRSSDAO().getDatabaseUserDAO().getDatabaseUser(ctx.getEnvironmentName(),
                    rssInstance.getName(), username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg ="Error occurred while retrieving metadata " +
                    "corresponding to the database user '" + username + "' from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return user;
    }

    public Database getDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        Database database = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByDatabase(
                            ctx.getEnvironmentName(), rssInstanceName, databaseName, tenantId);
            if (rssInstance == null) {
                if (inTx && getEntityManager().hasNoActiveTransaction()) {
                    getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            database = getRSSDAO().getDatabaseDAO().getDatabase(ctx.getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to the database '" + databaseName + "' from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return database;
    }

    public DatabaseUser[] getUsersAttachedToDatabase(RSSEnvironmentContext ctx,
                                                     String rssInstanceName,
                                                     String databaseName) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByDatabase(
                            ctx.getEnvironmentName(), rssInstanceName, databaseName, tenantId);
            if (rssInstance == null) {
                getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database '" + databaseName
                        + "' does not exist " + "in RSS instance '"
                        + rssInstanceName + "'");
            }
            users = getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(ctx.getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to the database users attached to the database '" +
                    databaseName + "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return users;
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByDatabase(
                            ctx.getEnvironmentName(), rssInstanceName, databaseName, tenantId);
            DatabaseUser[] existingUsers =
                    getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(
                            ctx.getEnvironmentName(), rssInstance.getName(), databaseName, tenantId);
            Set<String> usernames = new HashSet<String>();
            for (DatabaseUser user : existingUsers) {
                usernames.add(user.getName());
            }

            List<DatabaseUser> availableUsers = new ArrayList<DatabaseUser>();
            for (DatabaseUser user : getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(
                    ctx.getEnvironmentName(), tenantId)) {
                String username = user.getName();
                if (!usernames.contains(username)) {
                    availableUsers.add(user);
                }
            }
            users = availableUsers.toArray(new DatabaseUser[availableUsers.size()]);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to available database users to be attached to the database'" +
                    databaseName + "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return users;
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(RSSEnvironmentContext ctx,
                                                          String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
        DatabasePrivilegeSet privileges = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByDatabase(ctx.getEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            if (rssInstance == null) {
                if (inTx) {
                    getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            privileges =
                    getRSSDAO().getDatabaseUserDAO().getUserDatabasePrivileges(ctx.getEnvironmentName(),
                    rssInstance.getId(), databaseName, username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to the database privileges assigned to database user '" +
                    username + "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return privileges;
    }

    @Override
    public RSSInstance resolveRSSInstance(RSSEnvironmentContext ctx, String databaseName)
            throws RSSManagerException {
        RSSInstance rssInstance;
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            rssInstance =
                    this.getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByDatabase(
                            ctx.getEnvironmentName(), RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE, databaseName, tenantId);
            return rssInstance;
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while resolving RSS instance", e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }
}
