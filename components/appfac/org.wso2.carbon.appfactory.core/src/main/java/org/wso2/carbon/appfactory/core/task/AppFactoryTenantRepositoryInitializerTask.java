package org.wso2.carbon.appfactory.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.ntask.core.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Task for initializing App Factory Tenant Repository
 */
public class AppFactoryTenantRepositoryInitializerTask implements Task {
    private static final Log log = LogFactory.getLog(AppFactoryTenantRepositoryInitializerTask
            .class);
    public static String TENANT_DOMAIN = "tenantDomain";
    public static String TENANT_USAGE_PLAN = "usagePlan";
    private Map<String, String> properties = new HashMap<String, String>();

    @Override
    public void setProperties(Map<String, String> stringStringMap) {
        this.properties = stringStringMap;
    }

    @Override
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing AppFactoryTenantRepositoryInitializerTask for " + properties.get
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
