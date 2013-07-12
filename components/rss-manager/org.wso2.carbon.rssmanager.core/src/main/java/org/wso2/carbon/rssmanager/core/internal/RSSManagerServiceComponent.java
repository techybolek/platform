/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.rssmanager.core.service.RSSManagerService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.transaction.manager.TransactionManagerDummyService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

/**
 * This class activates the RSS manager core bundle
 *
 * @scr.component name="rss.manager" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="datasources.service"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="transactionmanager"
 * interface="org.wso2.carbon.transaction.manager.TransactionManagerDummyService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setTransactionManagerDummyService"
 * unbind="unsetTransactionManagerDummyService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setSecretCallbackHandlerService"
 * unbind="unsetSecretCallbackHandlerService"
 */
public class RSSManagerServiceComponent {

    private static Log log = LogFactory.getLog(RSSManagerServiceComponent.class);

    private static DataSourceService dataSourceService;

    private static RealmService realmService;

    private static SecretCallbackHandlerService secretCallbackHandlerService;

    /**
     * Activates the RSS Manager Core bundle.
     *
     * @param componentContext ComponentContext
     */
    protected void activate(ComponentContext componentContext) {
        BundleContext bundleContext = componentContext.getBundleContext();

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                MultitenantConstants.SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        try {
            /* Loading tenant specific data */
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new RSSManagerAxis2ConfigContextObserver(), null);
            /* Registers RSSManager service */
            bundleContext.registerService(RSSManagerService.class.getName(),
                    new RSSManagerService(), null);
            bundleContext.registerService(TransactionManagerDummyService.class.getName(),
                    new TransactionManagerDummyService(), null);
            /* Looks up for the JNDI registered transaction manager */
            RSSManagerUtil.setTransactionManager(this.lookupTransactionManager());
            /* Initializes the RSS configuration */
            RSSConfig.init();
            /* Initializing RSSDAO Factory */
            RSSDAOFactory.init(RSSConfig.getInstance().getRSSManagementRepository());
            /* Initializing RSS environments */
            RSSConfig.getInstance().initRSSEnvironments();
        } catch (Throwable e) {
            String msg = "Error occurred while initializing RSS Manager core bundle";
            log.error(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Deactivates the bundle. The content of this method is intentionally left blank as the
     * underlying OSGi layer handles the corresponding task.
     *
     * @param componentContext ComponentContext
     */
    protected void deactivate(ComponentContext componentContext) {
        //intentionally left blank
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Data Sources Service");
        }
        RSSManagerServiceComponent.dataSourceService = dataSourceService;
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Data Sources Service");
        }
        RSSManagerServiceComponent.dataSourceService = null;
    }

    /**
     * Provides access to DataSource service
     *
     * @return DataSourceService instance
     */
    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    /**
     * Exposes the current realm service
     *
     * @return realmService
     */
    private static RealmService getRealmService() {
        return realmService;
    }

    /**
     * Sets Realm Service
     *
     * @param realmService associated realm service
     */
    protected void setRealmService(RealmService realmService) {
        RSSManagerServiceComponent.realmService = realmService;
    }

    /**
     * Unsets Realm Service
     *
     * @param realmService associated realm service
     */
    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }

    /**
     * Retrieves the associated TenantManager
     *
     * @return TenantManager
     */
    public static TenantManager getTenantManager() {
        return getRealmService().getTenantManager();
    }

    private TransactionManager lookupTransactionManager() {
        TransactionManager transactionManager = null;
        try {
            Object txObj = InitialContext.doLookup(
                    RSSManagerConstants.STANDARD_USER_TRANSACTION_JNDI_NAME);
            if (txObj instanceof TransactionManager) {
                transactionManager = (TransactionManager) txObj;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find transaction manager at: "
                        + RSSManagerConstants.STANDARD_USER_TRANSACTION_JNDI_NAME, e);
            }
            /* ignore, move onto next step */
        }
        if (transactionManager == null) {
            try {
                transactionManager = InitialContext.doLookup(
                        RSSManagerConstants.STANDARD_TRANSACTION_MANAGER_JNDI_NAME);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find transaction manager at: " +
                            RSSManagerConstants.STANDARD_TRANSACTION_MANAGER_JNDI_NAME, e);
                }
                /* we'll do the lookup later, maybe user provided a custom JNDI name */
            }
        }
        return transactionManager;
    }

    protected void setTransactionManagerDummyService(TransactionManagerDummyService dummyService) {
        //do nothing
    }

    protected void unsetTransactionManagerDummyService(TransactionManagerDummyService dummyService) {
        //do nothing
    }

    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
		if (log.isDebugEnabled()) {
			log.debug("Setting SecretCallbackHandlerService");
		}
		RSSManagerServiceComponent.secretCallbackHandlerService = secretCallbackHandlerService;
	}

	protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
			log.debug("Unsetting SecretCallbackHandlerService");
		}
		RSSManagerServiceComponent.secretCallbackHandlerService = null;
	}
	
	public static SecretCallbackHandlerService getSecretCallbackHandlerService(){
		return secretCallbackHandlerService;
	}

}
