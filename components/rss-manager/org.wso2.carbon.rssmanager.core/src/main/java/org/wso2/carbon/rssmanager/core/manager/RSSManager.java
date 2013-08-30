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

import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public interface RSSManager {

    Database createDatabase(RSSEnvironmentContext ctx,
                            Database database) throws RSSManagerException;

    void dropDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                      String databaseName) throws RSSManagerException;

    DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                    DatabaseUser user) throws RSSManagerException;

    void dropDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                          String username) throws RSSManagerException;

    void editDatabaseUserPrivileges(RSSEnvironmentContext ctx,
                                    DatabasePrivilegeSet privileges,
                                    DatabaseUser databaseUser,
                                    String databaseName) throws RSSManagerException;

    void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry ude,
                              String templateName) throws RSSManagerException;

    void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                UserDatabaseEntry ude) throws RSSManagerException;

    RSSInstance resolveRSSInstance(RSSEnvironmentContext ctx,
                                   String databaseName) throws RSSManagerException;

    DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                 String username) throws RSSManagerException;

    Database getDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                         String databaseName) throws RSSManagerException;

    DatabaseUser[] getUsersAttachedToDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException;

    DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String rssInstanceName,
            String databaseName) throws RSSManagerException;

    DatabasePrivilegeSet getUserDatabasePrivileges(
            RSSEnvironmentContext ctx, String rssInstanceName, String databaseName,
            String username) throws RSSManagerException;

    RSSInstance createRSSInstance(RSSEnvironmentContext ctx,
                                  RSSInstance rssInstance) throws RSSManagerException;

    void dropRSSInstance(RSSEnvironmentContext ctx,
                         String rssInstanceName) throws RSSManagerException;

    void editRSSInstanceConfiguration(RSSEnvironmentContext ctx,
                                      RSSInstance rssInstance) throws RSSManagerException;

    RSSInstance[] getRSSInstances(RSSEnvironmentContext ctx) throws RSSManagerException;

    Database[] getDatabases(RSSEnvironmentContext ctx) throws RSSManagerException;

    DatabaseUser[] getDatabaseUsers(RSSEnvironmentContext ctx) throws RSSManagerException;

    RSSInstance getRoundRobinAssignedDatabaseServer(
            RSSEnvironmentContext ctx) throws RSSManagerException;

    void createDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException;

    void editDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException;

    RSSInstance getRSSInstance(RSSEnvironmentContext ctx,
                               String rssInstanceName) throws RSSManagerException;

    boolean isDatabaseExist(RSSEnvironmentContext ctx, String rssInstanceName,
                            String databaseName) throws RSSManagerException;

    boolean isDatabaseUserExist(RSSEnvironmentContext ctx, String rssInstanceName,
                                String databaseUsername) throws RSSManagerException;

    boolean isDatabasePrivilegeTemplateExist(RSSEnvironmentContext ctx,
                                             String templateName) throws RSSManagerException;

    void dropDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
                                        String templateName) throws RSSManagerException;

    DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(
            RSSEnvironmentContext ctx) throws RSSManagerException;

    DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException;


    int getSystemRSSInstanceCount(RSSEnvironmentContext ctx) throws RSSManagerException;

    Database[] getDatabasesRestricted(RSSEnvironmentContext ctx,
                                      int tenantId) throws RSSManagerException;

    String[] getEnvironmentNames();

}
