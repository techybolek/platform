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
package org.wso2.carbon.rssmanager.core.dao;

import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.config.datasource.DSXMLConfiguration;
import org.wso2.carbon.rssmanager.core.config.datasource.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

/**
 * Data Access Object interface for WSO2 RSS based database operations.
 */
public abstract class RSSDAO {

    private static DataSource dataSource = null;
    private static EntityManager entityManager;

    public RSSDAO(RSSManagementRepository repository, EntityManager entityManager) {
        RSSManagementRepository.RepositoryDataSource dataSourceDef = repository.getDataSource();
        if(dataSourceDef != null){
            List<RSSManagementRepository.RepositoryDataSource.JNDILookupDef.JNDIProperty> jndiPropertyList = dataSourceDef.getJndiLookupDef().getJndiProperties();
            if(jndiPropertyList != null){
                final Hashtable<Object,Object> jndiProperties = new Hashtable<Object,Object>();
                for(RSSManagementRepository.RepositoryDataSource.JNDILookupDef.JNDIProperty prop : jndiPropertyList){
                    jndiProperties.put(prop.getName(),prop.getValue());
                }
                RSSDAO.dataSource = RSSManagerUtil.lookupDataSource(dataSourceDef.getJndiLookupDef().getJndiName(),jndiProperties);
            }else{
                RSSDAO.dataSource = RSSManagerUtil.lookupDataSource(dataSourceDef.getJndiLookupDef().getJndiName(), null);
            }
        } else{
            RDBMSConfiguration config = repository.getDataSourceConfig();
            RSSDAO.dataSource = RSSManagerUtil.createDataSource(RSSManagerUtil.loadDataSourceProperties(config),
                    config.getDataSourceClassName());
        }
        RSSDAO.entityManager = entityManager;
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static DataSource getDataSource() throws RSSDAOException {
        if (dataSource == null) {
            throw new RSSDAOException("RSSDAO data source is not initialized properly");
        }
        return dataSource;
    }

    public abstract EnvironmentDAO getEnvironmentDAO();

    public abstract RSSInstanceDAO getRSSInstanceDAO();

    public abstract DatabaseDAO getDatabaseDAO();

    public abstract DatabaseUserDAO getDatabaseUserDAO();

    public abstract DatabasePrivilegeTemplateDAO getDatabasePrivilegeTemplateDAO();

    public abstract UserDatabaseEntryDAO getUserDatabaseEntryDAO();

}
