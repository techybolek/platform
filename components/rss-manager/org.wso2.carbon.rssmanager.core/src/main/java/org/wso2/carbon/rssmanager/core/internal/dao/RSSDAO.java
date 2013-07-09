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
package org.wso2.carbon.rssmanager.core.internal.dao;

import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.entity.*;

/**
 * Data Access Object interface for WSO2 RSS based database operations.
 */
public interface RSSDAO {

    RSSEnvironment createRSSEnvironment(RSSEnvironment rssEnvironment,
                                        int tenantId) throws RSSManagerException;

    void dropRSSEnvironment(String rssEnvironmentName, int tenantId) throws RSSManagerException;

    void updateRSSEnvironment(RSSEnvironment rssEnvironment, int tenantId) throws RSSManagerException;

    boolean isEnvironmentExists(String name, int tenantId) throws RSSManagerException;

    void createRSSInstance(String environmentName, RSSInstance rssInstance,
                           int tenantId) throws RSSManagerException;

    RSSInstance[] getAllSystemRSSInstances(String environmentName) throws RSSManagerException;

    void dropRSSInstance(String environmentName, String rssInstanceName,
                         int tenantId) throws RSSManagerException;

    void updateRSSInstance(String environmentName, RSSInstance rssInstance,
                           int tenantId) throws RSSManagerException;

    void createDatabase(String environmentName, Database database,
                        int tenantId) throws RSSManagerException;

    Database[] getAllDatabases(String environmentName, int tenantId) throws RSSManagerException;

    void dropDatabase(String environmentName, RSSInstance rssInstance, String databaseName,
                      int tenantId) throws RSSManagerException;

    void createDatabaseUser(String environmentName, RSSInstance rssInstance, DatabaseUser user,
                            int tenantId) throws RSSManagerException;

    void dropDatabaseUser(String environmentName, RSSInstance rssInstance, String username,
                          int tenantId) throws RSSManagerException;

    UserDatabaseEntry createUserDatabaseEntry(String environmentName, RSSInstance rssInstance,
                                              Database database, String username,
                                              int tenantId) throws RSSManagerException;

    void deleteUserDatabaseEntry(String environmentName, RSSInstance rssInstance, String username,
                                 int tenantId) throws RSSManagerException;

    int getSystemRSSDatabaseCount(String environmentName) throws RSSManagerException;

    RSSInstance[] getAllRSSInstances(String environmentName,
                                     int tenantId) throws RSSManagerException;

    RSSInstance getRSSInstance(String environmentName, String rssInstanceName,
                               int tenantId) throws RSSManagerException;

    RSSInstance getSystemRSSInstance(String environmentName,
                                     String rssInstanceName) throws RSSManagerException;

    Database getDatabase(String environmentName, RSSInstance rssInstance, String databaseName,
                         int tenantId) throws RSSManagerException;

    DatabaseUser getDatabaseUser(String environmentName, RSSInstance rssInstance, String username,
                                 int tenantId) throws RSSManagerException;

    DatabasePrivilegeSet getUserDatabasePrivileges(String environmentName, RSSInstance rssInstance,
                                                   String databaseName, String username,
                                                   int tenantId) throws RSSManagerException;

    void updateDatabaseUser(String environmentName, DatabasePrivilegeSet privileges,
                            RSSInstance rssInstance, DatabaseUser user,
                            String databaseName) throws RSSManagerException;

    DatabasePrivilegeTemplate createDatabasePrivilegesTemplate(
            String environmentName, DatabasePrivilegeTemplate template,
            int tenantId) throws RSSManagerException;

    void dropDatabasePrivilegesTemplate(String environmentName, String templateName,
                                        int tenantId) throws RSSManagerException;

    void editDatabasePrivilegesTemplate(String environmentName, DatabasePrivilegeTemplate template,
                                        int tenantId) throws RSSManagerException;

    DatabasePrivilegeTemplate[] getAllDatabasePrivilegesTemplates(
            String environmentName, int tenantId) throws RSSManagerException;

    DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            String environmentName, String templateName, int tenantId) throws RSSManagerException;

    DatabaseUserMetaData[] getAllDatabaseUsers(String environmentName,
                                               int tenantId) throws RSSManagerException;

    DatabaseUser[] getUsersByRSSInstance(String environmentName, RSSInstance rssInstance,
                                         int tenantId) throws RSSManagerException;

    DatabaseUserMetaData[] getUsersAssignedToDatabase(String environmentName,
                                                      RSSInstance rssInstance, String databaseName,
                                                      int tenantId) throws RSSManagerException;

    DatabaseUser[] getSystemCreatedDatabaseUsers(String environmentName) throws RSSManagerException;

    DatabaseUserMetaData[] getSystemUsersAssignedToDatabase(
            String environmentName, RSSInstance rssInstance,
            String databaseName) throws RSSManagerException;

    DatabasePrivilegeSet getSystemUserDatabasePrivileges(
            String environmentName, RSSInstance rssInstance, String databaseName,
            String username) throws RSSManagerException;

    RSSInstance findRSSInstanceDatabaseBelongsTo(
            String environmentName, String rssInstanceName, String databaseName,
            int tenantId) throws RSSManagerException;

    RSSInstance findRSSInstanceDatabaseUserBelongsTo(
            String environmentName, String rssInstanceName, String username,
            int tenantId) throws RSSManagerException;

    void setUserDatabasePrivileges(String environmentName, UserDatabaseEntry userDBEntry,
                                   DatabasePrivilegeTemplate template,
                                   int tenantId) throws RSSManagerException;

    void deleteUserDatabasePrivilegeEntriesByDatabaseUser(
            String environmentName, RSSInstance rssInstance, String username,
            int tenantId) throws RSSManagerException;

    void removeUserDatabaseEntriesByDatabaseUser(String environmentName, RSSInstance rssInstance,
                                                 String username,
                                                 int tenantId) throws RSSManagerException;

    void removeUserDatabaseEntriesByDatabase(String environmentName, RSSInstance rssInstance,
                                             String databaseName,
                                             int tenantId) throws RSSManagerException;

    void removeDatabasePrivilegesTemplateEntries(String environmentName, String templateName,
                                                 int tenantId) throws RSSManagerException;

    void incrementSystemRSSDatabaseCount(String environmentName) throws RSSManagerException;

    void deleteUserDatabasePrivileges(String environmentName, RSSInstance rssInstance, 
                                      String username, int tenantId) throws RSSManagerException;

    void setDatabasePrivilegeTemplateProperties(String environmentName,
                                                DatabasePrivilegeTemplate template,
                                                int tenantId) throws RSSManagerException;

    boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
                            int tenantId) throws RSSManagerException;

    boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                String databaseUsername, int tenantId) throws RSSManagerException;

    boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName,
                                             int tenantId) throws RSSManagerException;

	void deleteUserDatabasePrivilegeEntriesByDatabase(RSSInstance rssInstance,
			String databaseName, int tenantId)throws RSSManagerException;

}
