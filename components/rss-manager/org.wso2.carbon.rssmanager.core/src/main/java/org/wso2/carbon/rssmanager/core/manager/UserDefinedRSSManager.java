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
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
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

public abstract class UserDefinedRSSManager extends AbstractRSSManager {

    public UserDefinedRSSManager(RSSConfig config) {
        super(config);
    }

    public DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                        String username) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist = getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(ctx.getEnvironmentName(),
                    rssInstanceName, username, tenantId);
            if (isExist) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' already exists " +
                        "in the RSS instance '" + rssInstanceName + "'");
            }
            RSSInstance rssInstance = getRSSInstance(ctx, rssInstanceName);
            if (rssInstance == null) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseUserDAO().getDatabaseUser(ctx.getEnvironmentName(),
                    rssInstance.getName(), username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database user '" + username + "' belongs to the RSS instance '" +
                    rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }

    }

    public Database getDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByDatabase(ctx.getEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            if (rssInstance == null) {
                if (inTx && getEntityManager().hasNoActiveTransaction()) {
                    this.getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseDAO().getDatabase(ctx.getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database '" + databaseName + "' belongs to the RSS instance '" +
                    rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public DatabaseUser[] getUsersAttachedToDatabase(RSSEnvironmentContext ctx,
                                                     String rssInstanceName,
                                                     String databaseName) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().resolveRSSInstanceByDatabase(ctx.getEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            if (rssInstance == null) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database '" + databaseName
                        + "' does not exist " + "in RSS instance '"
                        + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(ctx.getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database users already attached to database '" + databaseName + "' which " +
                    "belongs to the RSS instance '" + rssInstanceName + ", from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            DatabaseUser[] existingUsers =
                    getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(ctx.getEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            Set<String> usernames = new HashSet<String>();
            for (DatabaseUser user : existingUsers) {
                usernames.add(user.getName());
            }
            DatabaseUser[] tmp =
                    getRSSDAO().getDatabaseUserDAO().getDatabaseUsersByRSSInstance(ctx.getEnvironmentName(),
                            rssInstanceName, tenantId);
            List<DatabaseUser> availableUsers = new ArrayList<DatabaseUser>();
            for (DatabaseUser user : tmp) {
                String username = user.getName();
                if (!usernames.contains(username)) {
                    availableUsers.add(user);
                }
            }
            return availableUsers.toArray(new DatabaseUser[availableUsers.size()]);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database users available to be attached to database '" + databaseName +
                    "' which belongs to the RSS instance '" + rssInstanceName + ", from RSS " +
                    "metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(RSSEnvironmentContext ctx,
                                                          String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            RSSInstance rssInstance =
                    getRSSDAO().getRSSInstanceDAO().getRSSInstance(ctx.getEnvironmentName(),
                            rssInstanceName, tenantId);
            if (rssInstance == null) {
                if (inTx && getEntityManager().hasNoActiveTransaction()) {
                    this.getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseUserDAO().getUserDatabasePrivileges(ctx.getEnvironmentName(),
                    rssInstance.getId(), databaseName, username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "the privileges assigned to database user '" + username + "' which " +
                    "belongs to the RSS instance '" + rssInstanceName + " upon the database '" +
                    databaseName + "', from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
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
                            ctx.getEnvironmentName(), RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE, databaseName, tenantId);
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
