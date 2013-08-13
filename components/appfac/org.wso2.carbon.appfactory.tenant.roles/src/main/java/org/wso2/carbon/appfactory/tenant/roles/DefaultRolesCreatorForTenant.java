/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.tenant.roles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.tenant.roles.internal.ServiceHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.mgt.UserMgtConstants;

/**
 * Adds roles such as developer,devOps,qa,appOwner,admin,ceo
 * to "User Manager Database" on Tenant(application) creation
 */
public class DefaultRolesCreatorForTenant implements TenantMgtListener {
    private static Log log = LogFactory.getLog(DefaultRolesCreatorForTenant.class);
    private static final int EXEC_ORDER = 40;

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        try {

            Registry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry();
            Collection resource = registry.newCollection();
            resource.setProperty(UserMgtConstants.DISPLAY_NAME, "appfactory");
            registry.put("/permission/admin/appfactory", resource);

            AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
            String[] permissionList = config.getProperties("Permissions.Permission");
            for (String permission : permissionList) {
                String permissionName = config.getFirstProperty("Permissions.Permission." + permission);
                resource = registry.newCollection();
                resource.setProperty(UserMgtConstants.DISPLAY_NAME, permissionName);
                registry.put(permission, resource);
            }

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {
        // Do nothing
    }

    @Override
    public int getListenerOrder() {
        return EXEC_ORDER;
    }
}
