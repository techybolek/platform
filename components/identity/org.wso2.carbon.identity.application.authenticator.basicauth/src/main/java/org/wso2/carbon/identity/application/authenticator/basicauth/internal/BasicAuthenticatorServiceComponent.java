package org.wso2.carbon.identity.application.authenticator.basicauth.internal;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticator;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="identity.application.authenticator.basicauth.component" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class BasicAuthenticatorServiceComponent{

    private static Log log = LogFactory.getLog(BasicAuthenticatorServiceComponent.class);
    
    private static RegistryService registryService;
	private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {
    	BasicAuthenticator basicAuth = new BasicAuthenticator(registryService, realmService);
    	Hashtable<String, String> props = new Hashtable<String, String>();
    	
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(), basicAuth, props);
        
        if (log.isDebugEnabled()) {
            log.info("BasicAuthenticator bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("BasicAuthenticator bundle is deactivated");
        }
    }
    
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in BasicAuthenticator bundle");
        }
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in BasicAuthenticator bundle");
        }
        registryService = null;
    }

    protected void setRealmService(RealmService realmService){
        if(log.isDebugEnabled()){
            log.debug("Realm Service is set in the BasicAuthenticator bundle");
        }
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService){
        if(log.isDebugEnabled()){
            log.debug("Realm Service is set in the BasicAuthenticator bundle");
        }
        this.realmService = null;
    }
}
