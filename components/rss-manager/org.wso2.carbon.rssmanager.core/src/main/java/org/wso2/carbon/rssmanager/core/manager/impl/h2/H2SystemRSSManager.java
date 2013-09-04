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
package org.wso2.carbon.rssmanager.core.manager.impl.h2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.entity.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;

public class H2SystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(H2SystemRSSManager.class);

    public H2SystemRSSManager(RSSConfig config) {
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
    
    @Override
	public boolean deleteTenantRSSData(RSSEnvironmentContext ctx, int tenantId)
			throws RSSManagerException {
		boolean inTx = false;
		Database[] databases;
		DatabaseUser[] dbUsers;
		DatabasePrivilegeTemplate[] templates;
		try {
			// Delete tenant specific tables along with it's meta data
			databases = getRSSDAO().getDatabaseDAO().getDatabases(
					ctx.getEnvironmentName(), tenantId);
			log.info("Deleting rss tables and meta data");
			for (Database db : databases) {
				String databaseName = db.getName();
				String rssInstanceName = db.getRssInstanceName();
				dropDatabase(ctx, rssInstanceName, databaseName);
			}
			dbUsers = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(
					ctx.getEnvironmentName(), tenantId);
			log.info("Deleting rss users and meta data");
			for (DatabaseUser user : dbUsers) {
				String userName = user.getName();
				String rssInstanceName = user.getRssInstanceName();
				dropDatabaseUser(ctx, rssInstanceName, userName);
			}
			log.info("Deleting rss templates and meta data");
			templates = getRSSDAO().getDatabasePrivilegeTemplateDAO()
					.getDatabasePrivilegesTemplates(ctx.getEnvironmentName(),
							tenantId);
			inTx = this.getEntityManager().beginTransaction();
			for (DatabasePrivilegeTemplate template : templates) {
				dropDatabasePrivilegesTemplate(ctx, template.getName());
			}
			log.info("Successfully deleted rss data");

		} catch (Exception e) {
			if (inTx && getEntityManager().hasNoActiveTransaction()) {
				getEntityManager().rollbackTransaction();
			}
			String msg = "Error occurred while retrieving metadata "
					+ "corresponding to databases, from RSS metadata repository : "
					+ e.getMessage();
			handleException(msg, e);
		} finally {
			if (inTx) {
				getEntityManager().endTransaction();
			}
		}
		return true;	
	}


}
