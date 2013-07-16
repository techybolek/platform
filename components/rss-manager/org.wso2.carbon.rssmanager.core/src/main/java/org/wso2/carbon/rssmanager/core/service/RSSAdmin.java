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
package org.wso2.carbon.rssmanager.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerServiceComponent;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RSSAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(RSSAdmin.class);

    public void createRSSInstance(RSSEnvironmentContext ctx,
                                  RSSInstance rssInstance) throws RSSManagerException {
        this.getRSSManager(ctx).createRSSInstance(rssInstance);
    }

    public void dropRSSInstance(RSSEnvironmentContext ctx,
                                String rssInstanceName) throws RSSManagerException {
        this.getRSSManager(ctx).dropRSSInstance(rssInstanceName);
    }

    public void editRSSInstance(RSSEnvironmentContext ctx,
                                RSSInstance rssInstance) throws RSSManagerException {
        this.getRSSManager(ctx).editRSSInstanceConfiguration(rssInstance);
    }

    public RSSInstance getRSSInstance(RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager(ctx).getRSSInstance(ctx.getRssInstanceName());
    }

    public RSSInstance[] getRSSInstances(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        RSSInstance[] rssInstances = new RSSInstance[0];
        try {
            rssInstances = this.getRSSManager(ctx).getRSSInstances();
        } catch (RSSManagerException e) {
            String msg = "Error occurred in retrieving the RSS instance list of the tenant '" +
                    getTenantDomain() + "'";
            handleException(msg, e);
        }
        return rssInstances;
    }

    public Database createDatabase(RSSEnvironmentContext ctx,
                               Database database) throws RSSManagerException {
        return this.getRSSManager(ctx).createDatabase(database);
    }

    public void dropDatabase(RSSEnvironmentContext ctx,
                             String databaseName) throws RSSManagerException {
        this.getRSSManager(ctx).dropDatabase(ctx.getRssInstanceName(), databaseName);
    }

    public Database[] getDatabases(RSSEnvironmentContext ctx) throws RSSManagerException {
        Database[] databases = new Database[0];
        try {
            databases = this.getRSSManager(ctx).getDatabases();
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the database list of the tenant '" +
                    getTenantDomain() + "'";
            handleException(msg, e);
        }
        return databases;
    }

    public Database getDatabase(RSSEnvironmentContext ctx,
                                        String databaseName) throws RSSManagerException {
        return this.getRSSManager(ctx).getDatabase(ctx.getRssInstanceName(), databaseName);
    }

    public boolean isDatabaseExist(RSSEnvironmentContext ctx,
                                   String databaseName) throws RSSManagerException {
        return this.getRSSManager(ctx).isDatabaseExist(ctx.getRssInstanceName(), databaseName);
    }

    public boolean isDatabaseUserExist(RSSEnvironmentContext ctx,
                                       String databaseUsername) throws RSSManagerException {
        return this.getRSSManager(ctx).isDatabaseUserExist(ctx.getRssInstanceName(),
                databaseUsername);
    }

    public boolean isDatabasePrivilegesTemplateExist(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException {
        return this.getRSSManager(ctx).isDatabasePrivilegeTemplateExist(templateName);
    }

    public DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                   DatabaseUser user) throws RSSManagerException {
        return this.getRSSManager(ctx).createDatabaseUser(user);
    }

    public void dropDatabaseUser(RSSEnvironmentContext ctx,
                                 String username) throws RSSManagerException {
        this.getRSSManager(ctx).dropDatabaseUser(ctx.getRssInstanceName(), username);
    }

    public void editDatabaseUserPrivileges(RSSEnvironmentContext ctx,
                                           DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        this.getRSSManager(ctx).editDatabaseUserPrivileges(privileges, user, databaseName);
    }

    public DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx,
                                                String username) throws RSSManagerException {
        return this.getRSSManager(ctx).getDatabaseUser(ctx.getRssInstanceName(), username);
    }

    public DatabaseUser[] getDatabaseUsers(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager(ctx).getDatabaseUsers();
    }

    public void createDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getRSSManager(ctx).createDatabasePrivilegesTemplate(template);
    }

    public void dropDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
                                               String templateName) throws RSSManagerException {
        this.getRSSManager(ctx).dropDatabasePrivilegesTemplate(templateName);
    }

    public void editDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getRSSManager(ctx).editDatabasePrivilegesTemplate(template);
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager(ctx).getDatabasePrivilegeTemplates();
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException {
        return this.getRSSManager(ctx).getDatabasePrivilegeTemplate(templateName);
    }

    /**
     * Test the RSS instance connection using a mock database connection test.
     *
     * @param driverClass JDBC Driver class
     * @param jdbcURL     JDBC url
     * @param username    username
     * @param password    password
     * @return Success or failure message
     * @throws RSSManagerException RSSDAOException
     */
    public void testConnection(String driverClass, String jdbcURL, String username,
                               String password) throws RSSManagerException {
        Connection conn = null;
        int tenantId = RSSManagerUtil.getTenantId();

        if (driverClass == null || driverClass.length() == 0) {
            String msg = "Driver class is missing";
            throw new RSSManagerException(msg);
        }
        if (jdbcURL == null || jdbcURL.length() == 0) {
            String msg = "Driver connection URL is missing";
            throw new RSSManagerException(msg);
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            Class.forName(driverClass).newInstance();
            conn = DriverManager.getConnection(jdbcURL, username, password);
            if (conn == null) {
                String msg = "Unable to establish a JDBC connection with the database server";
                throw new RSSManagerException(msg);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while testing the JDBC connection";
            handleException(msg, e);
        } catch (ClassNotFoundException e) {
            throw new RSSManagerException("Error occurred while testing database connectivity : " +
                    e.getMessage(), e);
        } catch (InstantiationException e) {
        	throw new RSSManagerException("Error occurred while testing database connectivity : " +
                    e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RSSManagerException("Error occurred while testing database connectivity : " +
                    e.getMessage(), e);
		} finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    public int getSystemRSSInstanceCount(RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager(ctx).getSystemRSSInstanceCount();
    }

    public void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry ude,
                                     String templateName) throws RSSManagerException {
        this.getRSSManager(ctx).attachUserToDatabase(ude, templateName);
    }

    public void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry ude) throws RSSManagerException {
        this.getRSSManager(ctx).detachUserFromDatabase(ude);
    }

    public DatabaseUser[] getUsersAttachedToDatabase(
            RSSEnvironmentContext ctx, String databaseName) throws RSSManagerException {
        return this.getRSSManager(ctx).getUsersAttachedToDatabase(ctx.getRssInstanceName(),
                databaseName);
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String databaseName) throws RSSManagerException {
        return this.getRSSManager(ctx).getAvailableUsersToAttachToDatabase(ctx.getRssInstanceName(),
                databaseName);
    }

    private RSSManager getRSSManager(RSSEnvironmentContext ctx) throws RSSManagerException {
        RSSManager rssManager = RSSConfig.getInstance().getRSSManager(ctx);
        if (rssManager == null) {
            throw new RSSManagerException("RSS Manager does not exist for the RSS Environment '" +
                    ctx.getEnvironmentName() + "'");
        }
        return rssManager;
    }

    public void createCarbonDataSource(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry entry) throws RSSManagerException {
        Database database = this.getRSSManager(ctx).getDatabase(entry.getRssInstanceName(),
                entry.getDatabaseName());
        DataSourceMetaInfo metaInfo =
                RSSManagerUtil.createDSMetaInfo(database, entry.getUsername());
        try {
            RSSManagerServiceComponent.getDataSourceService().addDataSource(metaInfo);
        } catch (DataSourceException e) {
            String msg = "Error occurred while creating carbon datasource for the database '" +
                    entry.getDatabaseName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeSet getUserDatabasePermissions(
            RSSEnvironmentContext ctx, String databaseName,
            String username) throws RSSManagerException {
        return this.getRSSManager(ctx).getUserDatabasePrivileges(ctx.getRssInstanceName(),
                databaseName, username);
    }

    public boolean isInitializedTenant(String tenantDomain) throws RSSManagerException {
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized to perform " +
                    "this operation permission denied";
            throw new RSSManagerException(msg);
        }
        int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        boolean initialized = false;

        return initialized;
    }

    public void initializeTenant(String tenantDomain) throws RSSManagerException {
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized. " +
                    "Tenant domain :" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() +
                    " permission denied";
            throw new RSSManagerException(msg);
        }
        int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
    }

    public Database[] getDatabasesForTenant(RSSEnvironmentContext ctx,
                                                    String tenantDomain) throws RSSManagerException {
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized. " +
                    "Tenant domain :" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() +
                    " permission denied";
            throw new RSSManagerException(msg);
        }
        int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        Database[] databases = null;
        try {
            databases = this.getDatabases(ctx);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return databases;
    }

    public void createDatabaseForTenant(RSSEnvironmentContext ctx, Database database,
                                        String tenantDomain) throws RSSManagerException {
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized to perform " +
                    "this operation permission denied";
            log.error(msg);
            throw new RSSManagerException(msg);
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            try {
                createDatabase(ctx, database);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (RSSManagerException e) {
            log.error("Error occurred while creating database for tenant : " + e.getMessage(), e);
            throw e;
        }
    }

    public Database getDatabaseForTenant(
            RSSEnvironmentContext ctx, String databaseName,
            String tenantDomain) throws RSSManagerException {
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized to perform " +
                    "this operation permission denied";
            log.error(msg);
            throw new RSSManagerException(msg);
        }
        int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        Database metaData = null;
        try {
            metaData = this.getDatabase(ctx, databaseName);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return metaData;
    }

    public String[] getRSSEnvironmentNames() throws RSSManagerException {
        return RSSManagerUtil.getRSSEnvironmentNames(
                RSSConfig.getInstance().getRSSEnvironments());
    }

    private boolean isSuperTenantUser() throws RSSManagerException {
        return (getCurrentTenantId() == MultitenantConstants.SUPER_TENANT_ID);
    }

    private int getCurrentTenantId() throws RSSManagerException {
        try {
            return RSSManagerServiceComponent.getTenantManager().getTenantId(getTenantDomain());
        } catch (UserStoreException e) {
            throw new RSSManagerException("Error occurred while retrieving tenant id from the " +
                    "current tenant domain : " + e.getMessage(), e);
        }
    }

}
