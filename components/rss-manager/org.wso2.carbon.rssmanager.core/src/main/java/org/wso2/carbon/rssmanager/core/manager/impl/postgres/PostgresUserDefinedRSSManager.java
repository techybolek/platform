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

package org.wso2.carbon.rssmanager.core.manager.impl.postgres;

import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;

public class PostgresUserDefinedRSSManager extends UserDefinedRSSManager {
    
    public PostgresUserDefinedRSSManager(RSSConfig config) {
        super(config);
    }

    @Override
    public Database createDatabase(RSSEnvironmentContext ctx,
                                   Database database) throws RSSManagerException {
        return null;  
    }

    @Override
    public void dropDatabase(RSSEnvironmentContext ctx, String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        
    }

    @Override
    public DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                           DatabaseUser user) throws RSSManagerException {
        return null;  
    }

    @Override
    public void dropDatabaseUser(RSSEnvironmentContext ctx, String rssInstanceName,
                                 String username) throws RSSManagerException {
        
    }

    @Override
    public void editDatabaseUserPrivileges(RSSEnvironmentContext ctx,
                                           DatabasePrivilegeSet privileges,
                                           DatabaseUser databaseUser,
                                           String databaseName) throws RSSManagerException {
        
    }

    @Override
    public void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry ude,
                                     String templateName) throws RSSManagerException {
        
    }

    @Override
    public void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry ude) throws RSSManagerException {
        
    }
}
