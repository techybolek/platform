package org.wso2.carbon.identity.authenticator.mutualssl.internal;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.identity.authenticator.mutualssl.MutualSSLAuthenticator;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 * @scr.component name=
 *                "mutualssl.MutualSSLAuthenticatorServiceComponent"
 *                immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class MutualSSLAuthenticatorServiceComponent {

    private static RealmService realmService = null;
    private static final Log log = LogFactory.getLog(MutualSSLAuthenticatorServiceComponent.class);
    
    protected void activate(ComponentContext cxt) {
        System.out.println("Bundle is activated *********************");
        try {
            MutualSSLAuthenticator authenticator = new MutualSSLAuthenticator();
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
            cxt.getBundleContext().registerService(CarbonServerAuthenticator.class.getName(),
                                                   authenticator, props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Bundle is activated *********************");
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Appfactory common bundle is deactivated");
        }
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService acquired");
        }
        MutualSSLAuthenticatorServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        MutualSSLAuthenticatorServiceComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }
    
    

  

}



//public class Activator implements BundleActivator {
//
//    @Override
//    public void start(BundleContext cxt) throws Exception {
//        
//        System.out.println("Bundle is activated *********************");
//        
//        MutualSSLAuthenticator authenticator = new MutualSSLAuthenticator();
//        Hashtable<String, String> props = new Hashtable<String, String>();
//        props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
//        cxt.registerService(CarbonServerAuthenticator.class.getName(), authenticator, props);
//        
//        System.out.println("Bundle is activated *********************");
//    }
//
//    @Override
//    public void stop(BundleContext cxt) throws Exception {
//        // TODO Auto-generated method stub
//        
//    }
//    
//}
