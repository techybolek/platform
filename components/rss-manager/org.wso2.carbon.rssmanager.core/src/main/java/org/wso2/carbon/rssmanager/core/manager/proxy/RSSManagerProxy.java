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

package org.wso2.carbon.rssmanager.core.manager.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.*;

public class RSSManagerProxy implements RSSManager {

    private SystemRSSManager systemRM;
    private UserDefinedRSSManager userDefinedRM;
    private static final Log log = LogFactory.getLog(RSSManagerProxy.class);

    public RSSManagerProxy(String type, RSSConfig config) {
        RSSManagerFactory rmFactory = RSSManagerFactoryLoader.getRMFactory(type, config);
        this.systemRM = rmFactory.getSystemRSSManager();
        this.userDefinedRM = rmFactory.getUserDefinedRSSManager();
        if (systemRM == null) {
            String msg =
                    "Configured System RSS Manager is null, thus RSS Manager cannot be initialized";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (userDefinedRM == null) {
            String msg = "Configured User Defined RSS Manager is null. RSS Manager " +
                    "initialization will not be interrupted as a proper System RSS Manager is " +
                    "available. But any task related to User Defined RSS Manager would not be " +
                    "functional";
            log.warn(msg);
        }
    }

    public SystemRSSManager getSystemRM() {
        return systemRM;
    }

    public UserDefinedRSSManager getUserDefinedRM() {
        return userDefinedRM;
    }

    public RSSManager resolveRM(RSSEnvironmentContext ctx) {
        if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(
                ctx.getRssInstanceName())) {
            return getSystemRM();
        } else if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equals(
                ctx.getRssInstanceName())) {
            return getUserDefinedRM();
        } else {
            throw new IllegalArgumentException("Invalid RSS instance name provided");
        }
    }

    public Database createDatabase(RSSEnvironmentContext ctx,
                                   Database database) throws RSSManagerException {
        return this.resolveRM(ctx).createDatabase(ctx, database);
    }

    public void dropDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        this.resolveRM(ctx).dropDatabase(ctx, rssInstanceName, databaseName);
    }

    public DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                           DatabaseUser user) throws RSSManagerException {
        return this.resolveRM(ctx).createDatabaseUser(ctx, user);
    }

    public void dropDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                 String username) throws RSSManagerException {
        this.resolveRM(ctx).dropDatabaseUser(ctx, rssInstanceName, username);
    }

    public void editDatabaseUserPrivileges(
            RSSEnvironmentContext ctx, DatabasePrivilegeSet privileges, DatabaseUser databaseUser,
            String databaseName) throws RSSManagerException {
        this.resolveRM(ctx).editDatabaseUserPrivileges(ctx, privileges, databaseUser, databaseName);
    }

    public void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry ude,
                                     String templateName) throws RSSManagerException {
        this.resolveRM(ctx).attachUserToDatabase(ctx, ude, templateName);
    }

    public void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry ude) throws RSSManagerException {
        this.resolveRM(ctx).detachUserFromDatabase(ctx, ude);
    }

    public DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                        String username) throws RSSManagerException {
        return this.resolveRM(ctx).getDatabaseUser(ctx, rssInstanceName, username);
    }

    public Database getDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        return this.resolveRM(ctx).getDatabase(ctx, rssInstanceName, databaseName);
    }

    public DatabaseUser[] getUsersAttachedToDatabase(
            RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException {
        return this.resolveRM(ctx).getUsersAttachedToDatabase(ctx, rssInstanceName, databaseName);
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException {
        return this.resolveRM(ctx).getAvailableUsersToAttachToDatabase(ctx, rssInstanceName,
                databaseName);
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(RSSEnvironmentContext ctx,
                                                          String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
        return this.resolveRM(ctx).getUserDatabasePrivileges(ctx, rssInstanceName, databaseName,
                username);
    }

    public RSSInstance createRSSInstance(RSSEnvironmentContext ctx,
                                         RSSInstance rssInstance) throws RSSManagerException {
        return this.resolveRM(ctx).createRSSInstance(ctx, rssInstance);
    }

    public void dropRSSInstance(RSSEnvironmentContext ctx,
                                String rssInstanceName) throws RSSManagerException {
        this.resolveRM(ctx).dropRSSInstance(ctx, rssInstanceName);
    }

    public void editRSSInstanceConfiguration(RSSEnvironmentContext ctx,
                                             RSSInstance rssInstance) throws RSSManagerException {
        this.resolveRM(ctx).editRSSInstanceConfiguration(ctx, rssInstance);
    }

    public RSSInstance[] getRSSInstances(RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.resolveRM(ctx).getRSSInstances(ctx);
    }

    public Database[] getDatabases(RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.resolveRM(ctx).getDatabases(ctx);
    }

    public DatabaseUser[] getDatabaseUsers(RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.resolveRM(ctx).getDatabaseUsers(ctx);
    }

    public void createDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.resolveRM(ctx).createDatabasePrivilegesTemplate(ctx, template);
    }

    public void editDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.resolveRM(ctx).editDatabasePrivilegesTemplate(ctx, template);
    }

    public RSSInstance getRSSInstance(RSSEnvironmentContext ctx,
                                      String rssInstanceName) throws RSSManagerException {
        return this.resolveRM(ctx).getRSSInstance(ctx, rssInstanceName);
    }

    public boolean isDatabaseExist(RSSEnvironmentContext ctx, String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        return this.resolveRM(ctx).isDatabaseExist(ctx, rssInstanceName, databaseName);
    }

    public boolean isDatabaseUserExist(RSSEnvironmentContext ctx, String rssInstanceName,
                                       String username) throws RSSManagerException {
        return this.resolveRM(ctx).isDatabaseUserExist(ctx, rssInstanceName, username);
    }

    public boolean isDatabasePrivilegeTemplateExist(RSSEnvironmentContext ctx,
                                                    String templateName) throws RSSManagerException {
        return this.resolveRM(ctx).isDatabasePrivilegeTemplateExist(ctx, templateName);
    }

    public void dropDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
                                               String templateName) throws RSSManagerException {
        this.resolveRM(ctx).dropDatabasePrivilegesTemplate(ctx, templateName);
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.resolveRM(ctx).getDatabasePrivilegeTemplates(ctx);
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException {
        return this.resolveRM(ctx).getDatabasePrivilegeTemplate(ctx, templateName);
    }

    public Database[] getDatabasesRestricted(RSSEnvironmentContext
            ctx, int tenantId) throws RSSManagerException {
        return this.resolveRM(ctx).getDatabasesRestricted(ctx, tenantId);
    }

    public String[] getEnvironmentNames() {
        //TODO Fix this
        return this.resolveRM(null).getEnvironmentNames();
    }

	@Override
    public boolean deleteTenantRSSData(RSSEnvironmentContext ctx,
                                       int tenantId) throws RSSManagerException {
	    // TODO Auto-generated method stub
	    return false;
    }
}
