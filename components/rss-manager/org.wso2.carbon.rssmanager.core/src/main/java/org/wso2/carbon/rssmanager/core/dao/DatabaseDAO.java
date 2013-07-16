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
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;

public interface DatabaseDAO {

    /**
        * Method to add database information to the RSS meta data repository.
        *
        * @param database              Database configuration
        * @param tenantId              Tenant ID
        *
        * @throws RSSDAOException  If some error occurs while adding database configuration
        *                              information to RSS meta data repository
        */
       void addDatabase(Database database, int tenantId) throws RSSDAOException;

       /**
        * Method to remove database configuration information from RSS metadata repository.
        *
        * @param rssInstanceName       Name of the RSS instance in which the database has been created
        * @param databaseName          Name of the database to be removed
        * @param tenantId              Tenant ID
        *
        * @throws RSSDAOException  If some error occurs while removing database configuration
        *                              information from RSS meta data repository
        */
       void removeDatabase(String rssInstanceName, String databaseName,
                           int tenantId) throws RSSDAOException;

       /**
        * Method to check whether a database with the given name exists in a particular RSS instance.
        *
        * @param rssInstanceName       Name of the RSS instance
        * @param databaseName          Name of the database
        * @param tenantId              Tenant ID
        *
        * @return                      true | false depending on the existence of the database
        * @throws RSSDAOException  If some error occurs while checking the existence of a database
        *                              configuration in a particular RSS instance
        */
       boolean isDatabaseExist(String rssInstanceName, String databaseName,
                               int tenantId) throws RSSDAOException;

       /**
        * Method to get configuration information of a particular database.
        *
        * @param rssInstanceName       Name of the RSS instance in which the database exists
        * @param databaseName          Name of the database
        * @param tenantId              Tenant ID
        *
        * @return                      Configuration information of the requested database
        * @throws RSSDAOException  If some error occurs while retrieving the configuration
        *                              information of a given database
        */
       Database getDatabase(String rssInstanceName, String databaseName,
                            int tenantId) throws RSSDAOException;

       /**
        * Method to retrieve all the database configurations belong to a particular tenant.
        *
        * @param tenantId              Tenant ID
        *
        * @return                      Array of database configurations belong to a tenant
        * @throws RSSDAOException  If some error occurs while retrieving the configurations of
        *                              the database belong to a tenant
        */
       Database[] getDatabases(int tenantId) throws RSSDAOException;

       /**
        * Method to increment the System RSS database count which is being maintained to determine the
        * RSS instance in which the next database in-line should be created.
        *
        * @throws RSSDAOException  If some error occurs while incrementing the system RSS database
        *                              count
        */
       void incrementSystemRSSDatabaseCount() throws RSSDAOException;

       /**
        * Method to retrieve the system RSS database count which is being maintained to determine the
        * RSS instance in which the next database in-line should be created.
        *
        * @return                      Current system RSS database count
        * @throws RSSDAOException  If some error occurs while retrieving the current system RSS
        *                              database count
        */
       int getSystemRSSDatabaseCount() throws RSSDAOException;

       /**
        * Method to resolve the RSS instance to which a particular database belongs to.
        *
        * @param rssInstanceName       Name of the RSS instance
        * @param databaseName          Name of the database
        * @param tenantId              Tenant ID
        *
        * @return                      RSS instance to which the database belong to
        * @throws RSSDAOException  If some error occurs while resolving the RSS instance to which
        *                              the database belongs to
        */
       RSSInstance resolveRSSInstance(String rssInstanceName, String databaseName,
                                      int tenantId) throws RSSDAOException;

}
