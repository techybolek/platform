package org.wso2.carbon.appfactory.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * Task for initializing AppFactory Tenant Build Manager
 */
public class AppFactoryTenantBuildManagerInitializerTask implements Task {
    private static final Log log = LogFactory.getLog(AppFactoryTenantBuildManagerInitializerTask
            .class);
    public static String TENANT_DOMAIN = "tenantDomain";
    public static String TENANT_USAGE_PLAN = "usagePlan";
    private Map<String, String> properties;

    @Override
    public void setProperties(Map<String, String> stringStringMap) {
        this.properties = stringStringMap;
    }

    @Override
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing AppFactoryTenantBuildManagerInitializerTask for " + properties.get
                    (AppFactoryTenantBuildManagerInitializerTask.TENANT_DOMAIN));
        }
    }

    @Override
    public void execute() {
        for (TenantRepositoryManagerInitializer initializer : ServiceHolder.getInstance().
                getTenantRepositoryManagerInitializerList()) {
            initializer.onTenantCreation(properties.get(TENANT_DOMAIN), properties.get(TENANT_USAGE_PLAN));
        }
    }
}
