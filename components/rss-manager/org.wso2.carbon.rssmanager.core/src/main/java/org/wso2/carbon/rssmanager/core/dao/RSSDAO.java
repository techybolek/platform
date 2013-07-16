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

import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.core.config.DSXMLConfiguration;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Data Access Object interface for WSO2 RSS based database operations.
 */
public abstract class RSSDAO {

    private static DataSource dataSource = null;

        public RSSDAO(DSXMLConfiguration config) {
            dataSource = RSSManagerUtil.createDataSource((RDBMSConfiguration) config);
        }

        public static Connection createConnection() throws RSSDAOException {
            if (dataSource == null) {
                throw new RSSDAOException("RSS meta data repository data source is not " +
                        "initialized and is null");
            }
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new RSSDAOException("Error occurred while creating data source " +
                        "connection : " + e.getMessage(), e);
            }
        }

        public abstract RSSInstanceDAO getRSSInstanceDAO();

        public abstract DatabaseDAO getDatabaseDAO();

        public abstract DatabaseUserDAO getDatabaseUserDAO();

        public abstract DatabasePrivilegeTemplateDAO getDatabasePrivilegeTemplateDAO();

        public abstract UserDatabaseEntryDAO getUserDatabaseEntryDAO();

}
