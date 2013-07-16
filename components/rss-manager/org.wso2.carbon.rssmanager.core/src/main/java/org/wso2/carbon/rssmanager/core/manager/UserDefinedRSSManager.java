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

package org.wso2.carbon.rssmanager.core.manager;

import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public class UserDefinedRSSManager extends RSSManager {

    public UserDefinedRSSManager(RSSEnvironment rssEnvironment) {
        super(rssEnvironment);
    }

    @Override
    public Database createDatabase(Database database) throws RSSManagerException {
        return null;  
    }

    @Override
    public void dropDatabase(String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        
    }

    @Override
    public DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        return null;  
    }

    @Override
    public void dropDatabaseUser(String rssInstanceName,
                                 String username) throws RSSManagerException {
        
    }

    @Override
    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                           DatabaseUser databaseUser,
                                           String databaseName) throws RSSManagerException {
        
    }

    @Override
    public void attachUserToDatabase(UserDatabaseEntry ude,
                                     String templateName) throws RSSManagerException {
        
    }

    @Override
    public void detachUserFromDatabase(UserDatabaseEntry ude) throws RSSManagerException {
        
    }

    @Override
    public RSSInstance resolveRSSInstance(String rssInstanceName,
                                          String databaseName) throws RSSManagerException {
        return null;  
    }

    @Override
    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        return null;  
    }

    @Override
    public Database getDatabase(String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        return null;  
    }

    @Override
    public DatabaseUser[] getUsersAttachedToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        return new DatabaseUser[0];  
    }

    @Override
    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        return new DatabaseUser[0];  
    }

    @Override
    public DatabasePrivilegeSet getUserDatabasePrivileges(
            String rssInstanceName, String databaseName,
            String username) throws RSSManagerException {
        return null;  
    }
}
