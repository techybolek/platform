package org.wso2.carbon.event.builder.core.internal.build;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class Axis2ConfigurationContextObserverImpl
        extends AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {

        //TODO check the usage of this method
        String tenantDomain = PrivilegedCarbonContext.getCurrentContext(
                configurationContext).getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(
                configurationContext).getTenantId();
        log.info("Loading event builders specific to tenant when the tenant logged in");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getCurrentContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getCurrentContext().getTenantDomain(true);
            //EventBuilderServiceBuilder.loadConfigurationsFromRegistry();
        } catch (Exception e) {
            log.error("Unable to load event builders from registry ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
