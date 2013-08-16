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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.tenant.roles.util.Util;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tenant level roles defined in appfactory.xml are created through this
 * class.
 * All the permissions defined are assigned to the roles and if the role is
 * existing, permissions
 * are updated.
 */
public class DefaultRolesCreatorForTenant implements TenantMgtListener {
    private static Log log = LogFactory.getLog(DefaultRolesCreatorForSuperTenant.class);
    private List<RoleBean> roleBeanList = null;
    private static final int EXEC_ORDER = 40;
   
    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        roleBeanList = new ArrayList<RoleBean>();
        try {
            createDefaultRoles(tenantInfoBean);
        } catch (UserStoreException e) {
            String message = "Failed to read roles from appfactory configuration.";
            log.error(message);
            throw new StratosException(message, e);
        }
    }

    private void loadPlatformDefaultRoleConfigurations(AppFactoryConfiguration configuration,
                                                       String adminUser) {
        String[] roles = configuration.getProperties("TenantRoles.DefaultUserRole");
        for (String role : roles) {
            String permissionIdString =configuration.
            getFirstProperty("TenantRoles.DefaultUserRole." +
                             role + ".Permission");
            String[] permissionIds = permissionIdString.split(",");
            RoleBean roleBean = new RoleBean(role.trim());
            roleBean.addUser(adminUser);
            for (String permissionId : permissionIds) {
                String[] resourceAndActionParts = permissionId.trim().split(":");
                if (resourceAndActionParts.length == 2) {
                    Permission permission =new Permission(resourceAndActionParts[0],
                                                           resourceAndActionParts[1]);
                    roleBean.addPermission(permission);

                } else if (resourceAndActionParts.length == 1) {
                    Permission permission =new Permission(resourceAndActionParts[0],
                                                          CarbonConstants.UI_PERMISSION_ACTION);
                    roleBean.addPermission(permission);
                }
            }
            roleBeanList.add(roleBean);
        }
    }

    private void loadPlatformRoleConfigurations(AppFactoryConfiguration configuration,
                                                String adminUser) {
        String[] roles = configuration.getProperties("TenantRoles.Role");
        for (String role : roles) {
            String permissionIdString =configuration.getFirstProperty("TenantRoles.Role." +
                                                                       role + ".Permission");
            String[] permissionIds = permissionIdString.split(",");
            RoleBean roleBean = new RoleBean(role.trim());
            roleBean.addUser(adminUser);
            for (String permissionId : permissionIds) {
                String[] resourceAndActionParts = permissionId.trim().split(":");
                if (resourceAndActionParts.length == 2) {
                    Permission permission =new Permission(resourceAndActionParts[0],
                                                           resourceAndActionParts[1]);
                    roleBean.addPermission(permission);

                } else if (resourceAndActionParts.length == 1) {
                    Permission permission =new Permission(resourceAndActionParts[0],
                                                           CarbonConstants.UI_PERMISSION_ACTION);
                    roleBean.addPermission(permission);
                }
            }
            roleBeanList.add(roleBean);
        }
    }

    private void createDefaultRoles(TenantInfoBean tenantInfoBean) throws UserStoreException {
    	AppFactoryConfiguration configuration = Util.getConfiguration();
    	String adminUser =tenantInfoBean.getAdmin();
    	
    	loadPlatformDefaultRoleConfigurations(configuration, adminUser);
        loadPlatformRoleConfigurations(configuration, adminUser);
    	
    	UserStoreManager userStoreManager = Util.getRealmService().getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();
        AuthorizationManager authorizationManager =Util.getRealmService().getTenantUserRealm(tenantInfoBean.getTenantId()).
        											getAuthorizationManager();
        for (RoleBean roleBean : roleBeanList) {
            if (!userStoreManager.isExistingRole(roleBean.getRoleName())) {
                userStoreManager.addRole(roleBean.getRoleName(),
                                         roleBean.getUsers().toArray(new String[roleBean.getUsers().size()]),
                                         roleBean.getPermissions().toArray(new Permission[roleBean.getPermissions().size()]));
            } else {
                for (Permission permission : roleBean.getPermissions()) {
                    if (!authorizationManager.isRoleAuthorized(roleBean.getRoleName(),
                                                               permission.getResourceId(),
                                                               permission.getAction())) {
                        authorizationManager.authorizeRole(roleBean.getRoleName(),
                                                           permission.getResourceId(),
                                                           permission.getAction());
                    }
                }
            }
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
