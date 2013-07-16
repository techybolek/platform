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

package org.wso2.carbon.rssmanager.core.entity;

import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class RSSInstanceDSWrapper {

    private String name;

    private RSSInstance rssInstance;

    private DataSource dataSource;

    public RSSInstanceDSWrapper(RSSInstance rssInstance) {
        this.name = rssInstance.getName();
        this.rssInstance = rssInstance;
        this.dataSource = initDataSource();
    }

    public Connection getConnection() throws RSSManagerException {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            throw new RSSManagerException("Error while acquiring datasource connection : " +
                    e.getMessage(), e);
        }
    }

    private DataSource initDataSource() {
        org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration config = new RDBMSConfiguration();
        config.setUrl(getRssInstance().getDataSourceConfig().getUrl());
        config.setUsername(getRssInstance().getDataSourceConfig().getUsername());
        config.setPassword(getRssInstance().getDataSourceConfig().getPassword());
        config.setDriverClassName(getRssInstance().getDataSourceConfig().getDriverClassName());
        config.setTestOnBorrow(true);
        config.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;org.wso2.carbon.ndatasource.rdbms.ConnectionRollbackOnReturnInterceptor");
        return RSSManagerUtil.createDataSource(config);
    }

    public void closeDataSource() {
        ((org.apache.tomcat.jdbc.pool.DataSource) getDataSource()).close();
    }

    private DataSource getDataSource() {
        return dataSource;
    }

    public RSSInstance getRssInstance() {
        return rssInstance;
    }

    public String getName() {
        return name;
    }


}
