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
package org.wso2.carbon.appfactory.tenant.mgt;

import org.wso2.carbon.appfactory.tenant.mgt.internal.AppFactoryTenantMgtServiceComponent;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.tenant.mgt.core.DefaultTenantPersistor;
import org.wso2.carbon.tenant.mgt.core.internal.TenantMgtCoreServiceComponent;
import org.wso2.carbon.tenant.mgt.core.util.TenantCoreUtil;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This tenant persistor is only persisting non UM related changes.
 */
public class AppFactoryTenantPersistor extends DefaultTenantPersistor {
    public void persistTenantWithOutRealm(String tenantDomain, boolean checkDomainValidation, String successKey,
                                          String originatedService) throws Exception {
        Tenant tenant = new Tenant();
        tenant.setDomain(tenantDomain);
        tenant.setId(AppFactoryTenantMgtServiceComponent.getTenantManager().getTenantId(tenantDomain));
        if (checkDomainValidation) {
            if (successKey != null) {
                if (CommonUtil.validateDomainFromSuccessKey(TenantMgtCoreServiceComponent.
                        getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID),
                        tenantDomain, successKey)) {
                    storeDomainValidationFlagToRegistry(tenant);
                } else {
                    String msg = "Failed to validate domain";
                    throw new Exception(msg);
                }
            }
        } else {
            storeDomainValidationFlagToRegistry(tenant);
        }

        try {
            TenantMgtCoreServiceComponent.getRegistryLoader().loadTenantRegistry(tenant.getId());
            copyUIPermissions(tenant.getId());

            TenantCoreUtil.setOriginatedService(tenant.getId(), originatedService);
            setActivationFlags(tenant.getId(), originatedService);

            TenantCoreUtil.initializeRegistry(tenant.getId());
        } catch (Exception e) {
            String msg = "Error performing post tenant creation actions";
            throw new Exception(msg, e);
        }
    }
}
