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

package org.wso2.carbon.rssmanager.core.dao;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.*;

public interface DatabaseUserDAO {

    void addDatabaseUser(DatabaseUser user, int tenantId) throws RSSDAOException;

    void removeDatabaseUser(String rssInstanceName, String username,
                            int tenantId) throws RSSDAOException;

    void updateDatabaseUser(DatabasePrivilegeSet privileges, String rssInstanceName,
                            DatabaseUser user, String databaseName) throws RSSDAOException;

    boolean isDatabaseUserExist(String rssInstanceName, String username,
                                int tenantId) throws RSSDAOException;

    DatabaseUser getDatabaseUser(String rssInstanceName, String username,
                                 int tenantId) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsers(int tenantId) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsersByRSSInstance(String rssInstanceName,
                                                 int tenantId) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsersByDatabase(String rssInstanceName, String database,
                                              int tenantId) throws RSSDAOException;

    DatabaseUser[] getAssignedDatabaseUsers(String rssInstanceName, String databaseName,
                                            int tenantId) throws RSSDAOException;

    DatabaseUser[] getAvailableDatabaseUsers(String rssInstanceName, String databaseName,
                                             int tenantId) throws RSSDAOException;

    void removeDatabasePrivileges(String rssInstanceName, String username,
                                  int tenantId) throws RSSDAOException;

    DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
                                                   String username,
                                                   int tenantId) throws RSSDAOException;

    void setUserDatabasePrivileges(int id, DatabasePrivilegeTemplate template,
                                   int tenantId) throws RSSDAOException;

    RSSInstance resolveRSSInstance(String rssInstanceName, String username,
                                   int tenantId) throws RSSDAOException;


}
