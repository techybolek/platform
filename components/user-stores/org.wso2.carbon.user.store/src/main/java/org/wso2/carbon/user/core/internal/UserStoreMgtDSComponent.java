/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreConstants;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.ldap.*;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;

import java.util.Hashtable;

/**
 * @scr.component name="user.store.mgt.dscomponent" immediate=true
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class UserStoreMgtDSComponent {
    public static final String USER_STORE_MANAGER_TYPE = "user.store.manager,type";
    private static Log log = LogFactory.getLog(UserStoreMgtDSComponent.class);
    private RealmService realmService;


    protected void activate(ComponentContext ctxt) {
        try {

            UserStoreManager jdbcUserStoreManager = new JDBCUserStoreManager();
            Hashtable<String, Property[]> props = new Hashtable<String, Property[]>();
            props.put("org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager", JDBCUserStoreConstants.JDBC_UM_PROPERTIES.
                    toArray(new Property[JDBCUserStoreConstants.JDBC_UM_PROPERTIES.size()]));
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), jdbcUserStoreManager, props);

            UserStoreManager readWriteLDAPUserStoreManager = new ReadWriteLDAPUserStoreManager();
            props.put("org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager", ReadWriteLDAPUserStoreConstants.
                    RWLDAP_USERSTORE_PROPERTIES.toArray(new Property[ReadWriteLDAPUserStoreConstants.RWLDAP_USERSTORE_PROPERTIES.size()]));
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), readWriteLDAPUserStoreManager, props);

            props = new Hashtable<String, Property[]>();

            UserStoreManager readOnlyLDAPUserStoreManager = new ReadOnlyLDAPUserStoreManager();
            props.put("org.wso2.carbon.user.core.ldap.ReadOnlyLDAPUserStoreManager", ReadOnlyLDAPUserStoreConstants.
                    ROLDAP_USERSTORE_PROPERTIES.toArray(new Property[ReadOnlyLDAPUserStoreConstants.ROLDAP_USERSTORE_PROPERTIES.size()]));
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), readOnlyLDAPUserStoreManager, props);

            props = new Hashtable<String, Property[]>();

            UserStoreManager activeDirectoryUserStoreManager = new ActiveDirectoryUserStoreManager();
            props.put("org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreManager", ActiveDirectoryUserStoreConstants.
                    ACTIVE_DIRECTORY_UM_PROPERTIES.toArray(new Property[ActiveDirectoryUserStoreConstants.ACTIVE_DIRECTORY_UM_PROPERTIES.size()]));
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), activeDirectoryUserStoreManager, props);

            UserStoreManagerRegistry.init(ctxt.getBundleContext());

            log.info("Carbon UserStoreMgtDSComponent activated successfully.");
        } catch (Exception e) {
            log.error("Failed to activate Carbon UserStoreMgtDSComponent ", e);
        }
    }


    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Carbon UserStoreMgtDSComponent is deactivated ");
        }
    }

    protected RealmService getRealmService() {
        return realmService;
    }

    protected void setRealmService(RealmService rlmService) {
        realmService = rlmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        realmService = null;
    }

}
