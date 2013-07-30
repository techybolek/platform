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
package org.wso2.carbon.appfactory.core.services;

import org.wso2.carbon.appfactory.core.TenantBuildManagerInitializer;
import org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * Service used to initialize all the 3rd party tools on tenant creation
 * .
 */
public class AppFactoryTenantToolInitializerService extends AbstractAdmin {
    /**
     * Used to initialize a repository manager
     *
     * @param tenantDomain
     * @param usagePlan
     * @return
     */
    public boolean initializeRepositoryManager(String tenantDomain, String usagePlan) {
        for (TenantRepositoryManagerInitializer initializer : ServiceHolder.getInstance().
                getTenantRepositoryManagerInitializerList()) {
            initializer.onTenantCreation(tenantDomain, usagePlan);
        }
        return true;

    }

    /**
     * Used to initialize build manager
     *
     * @param tenantDomain
     * @param usagePlan
     * @return
     */
    public boolean initializeBuildManager(String tenantDomain, String usagePlan) {
        for (TenantBuildManagerInitializer initializer : ServiceHolder.getInstance().
                getTenantBuildManagerInitializerList()) {
            initializer.onTenantCreation(tenantDomain, usagePlan);
        }
        return true;
    }
}
