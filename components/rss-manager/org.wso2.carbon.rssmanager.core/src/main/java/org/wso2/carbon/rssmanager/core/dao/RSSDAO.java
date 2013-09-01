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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.rssmanager.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.rssmanager.core.config.datasource.RDBMSConfig;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.List;

/**
 * Data Access Object interface for WSO2 RSS based database operations.
 */
public abstract class RSSDAO {

    private static DataSource dataSource = null;
    private static EntityManager entityManager;
    private static final Log log = LogFactory.getLog(RSSDAO.class);

    public RSSDAO(RSSManagementRepository repository, EntityManager entityManager) {
        RSSDAO.dataSource = this.resolveDataSource(repository);
        RSSDAO.entityManager = entityManager;
    }

    public abstract EnvironmentDAO getEnvironmentDAO();

    public abstract RSSInstanceDAO getRSSInstanceDAO();

    public abstract DatabaseDAO getDatabaseDAO();

    public abstract DatabaseUserDAO getDatabaseUserDAO();

    public abstract DatabasePrivilegeTemplateDAO getDatabasePrivilegeTemplateDAO();

    public abstract UserDatabaseEntryDAO getUserDatabaseEntryDAO();

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static DataSource getDataSource() throws RSSDAOException {
        if (dataSource == null) {
            throw new RSSDAOException("RSSDAO data source is not initialized properly");
        }
        return dataSource;
    }

    private DataSource resolveDataSource(RSSManagementRepository repository) {
        DataSource dataSource;
        DataSourceConfig dataSourceDef = repository.getDataSourceConfig();
        if (dataSourceDef == null) {
            throw new RuntimeException("RSS Management Repository data source configuration is " +
                    "null and thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = dataSourceDef.getJndiLookupDefintion();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing RSS Management Repository data source using the JNDI " +
                        "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource =
                        RSSManagerUtil.lookupDataSource(jndiConfig.getJndiName(),
                                jndiProperties);
            } else {
                dataSource =
                        RSSManagerUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No JNDI Lookup Definition found in the RSS Management Repository " +
                        "data source configuration. Thus, continuing with in-line data source " +
                        "configuration processing.");
            }
            RDBMSConfig rdbmsConfig = dataSourceDef.getRdbmsConfiguration();
            if (rdbmsConfig == null) {
                throw new RuntimeException("No JNDI/In-line data source configuration found. " +
                        "Thus, RSS Management Repository DAO is not initialized");
            }
            dataSource =
                    RSSManagerUtil.createDataSource(
                            RSSManagerUtil.loadDataSourceProperties(rdbmsConfig),
                            rdbmsConfig.getDataSourceClassName());
        }
        return dataSource;
    }

}
