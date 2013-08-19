package org.wso2.carbon.appfactory.core.task;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Map;

/**
 * Task for initializing App Factory Tenant Cloud  in an environment eg:Dev QA
 */
public class AppFactoryTenantCloudInitializerTask implements Task {
    private static final Log log = LogFactory.getLog(AppFactoryTenantCloudInitializerTask.class);
    public static final String SERVICE_EPR = "epr";
    public static final String TENANT_USAGE_PLAN = "usagePlan";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String SUCCESS_KEY = "successKey";
    public static final String ADMIN_USERNAME = "adminUsername";
    public static final String ADMIN_EMAIL = "email";
    public static final String ADMIN_FIRST_NAME = "firstName";
    public static final String ADMIN_LAST_NAME = "lastName";
    public static final String ORIGINATED_SERVICE = "originatedService";
    public TenantMgtAdminServiceStub stub;
    public Map<String, String> properties;

    @Override
    public void setProperties(Map<String, String> stringStringMap) {
        this.properties = stringStringMap;
    }

    @Override
    public void init() {
        try {
            stub = new TenantMgtAdminServiceStub(ServiceHolder.getInstance().getConfigContextService()
                    .getClientConfigContext(), properties.get(SERVICE_EPR));
        } catch (AxisFault axisFault) {
            String msg = "Error while initializing TenantMgt Admin Service Stub ";
            log.error(msg, axisFault);
        }
    }

    @Override
    public void execute() {
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantInfoBean.setUsagePlan(properties.get(TENANT_USAGE_PLAN));
        tenantInfoBean.setTenantDomain(TENANT_DOMAIN);
        tenantInfoBean.setSuccessKey(SUCCESS_KEY);
        tenantInfoBean.setActive(true);
        tenantInfoBean.setAdmin(ADMIN_USERNAME);
        tenantInfoBean.setAdminPassword(null);
        tenantInfoBean.setEmail(ADMIN_EMAIL);
        tenantInfoBean.setFirstname(ADMIN_FIRST_NAME);
        tenantInfoBean.setLastname(ADMIN_LAST_NAME);
        tenantInfoBean.setOriginatedService(ORIGINATED_SERVICE);
        try {
            stub.addSkeletonTenant(tenantInfoBean);
            if (log.isDebugEnabled()) {
                log.debug("Called TenantMgt Admin Service in " + properties.get
                        (AppFactoryTenantCloudInitializerTask.SERVICE_EPR) + " with " + tenantInfoBean);
            }
        } catch (RemoteException e) {
            String msg = "Error while adding tenant " + tenantInfoBean.getTenantDomain();
            log.error(msg, e);
        } catch (TenantMgtAdminServiceExceptionException e) {
            String msg = "Error while invoking TenantMgtAdminService for " + tenantInfoBean.getTenantDomain();
            log.error(msg, e);
        }
    }
}
