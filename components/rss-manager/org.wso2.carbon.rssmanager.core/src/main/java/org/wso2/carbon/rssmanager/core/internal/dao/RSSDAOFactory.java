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
import org.wso2.carbon.rssmanager.core.config.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DAO factory class for creating RSS DAO objects.
 */
public class RSSDAOFactory {

    private static DataSource dataSource;

    public static void init(RSSManagementRepository repository) {
        RDBMSConfiguration config = repository.getDataSourceConfig();
        dataSource = RSSManagerUtil.createDataSource(
                RSSManagerUtil.loadDataSourceProperties(config), config.getDataSourceClassName());
    }

	public static RSSDAO getRSSDAO() {
		return new RSSDAOImpl();
	}

    public static Connection getConnection() throws RSSManagerException {
        if (dataSource == null) {
            throw new RSSManagerException("RSSDAO data source is not initialized properly");
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating data source " +
                    "connection : " + e.getMessage(), e);
        }
    }
	
}
