/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.appfactory.tenant.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.tenant.mgt.AppFactoryTenantPersistor;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.tenant.mgt.core.internal.TenantMgtCoreServiceComponent;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.tenant.mgt.util.TenantMgtUtil;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;

/**
 * This is the admin Web service which is used for managing tenants
 */
public class AppFactoryTenantMgtAdminService extends TenantMgtAdminService {
    private static final Log log = LogFactory.getLog(AppFactoryTenantMgtAdminService.class);

    public void doPostTenantActivation(TenantInfoBean tenantInfoBean) throws Exception {
        AppFactoryTenantPersistor persistor = new AppFactoryTenantPersistor();
        //  Tenant tenant = TenantMgtUtil.initializeTenant(tenantInfoBean);
        // not validating the domain ownership, since created by super tenant
        TenantManager tenantManager = TenantMgtCoreServiceComponent.getTenantManager();
        try {
            int tenantId = tenantManager.getTenantId(tenantInfoBean.getTenantDomain());
            if (tenantId > 0) {
                tenantInfoBean.setTenantId(tenantId);
                //tenant.setId(tenantInfoBean.getTenantId());
            } else {
                String msg = "Tenant is not available";
                log.error(msg);
                throw new Exception();
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error in getting tenant id.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        try {
            persistor.persistTenantWithOutRealm(tenantInfoBean.getTenantDomain(), false, tenantInfoBean.getSuccessKey(), tenantInfoBean.getOriginatedService());
        } catch (Exception e) {
            String msg = "Error in persisting tenant.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        //Notify tenant addition
        try {
            TenantMgtUtil.triggerAddTenant(tenantInfoBean);
        } catch (StratosException e) {
            String msg = "Error in notifying tenant addition.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        return;
    }
}
