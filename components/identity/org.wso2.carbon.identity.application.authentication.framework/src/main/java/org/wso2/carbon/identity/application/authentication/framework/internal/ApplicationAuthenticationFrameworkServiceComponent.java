/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.internal;

import java.util.Arrays;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticatorsComparator;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticatorsConfiguration;
import org.wso2.carbon.identity.application.authentication.framework.CommonAuthenticationServlet;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="identity.application.authentication.framework.component" immediate="true"
 * @scr.reference name="osgi.httpservice" interface="org.osgi.service.http.HttpService"
 *                cardinality="1..1" policy="dynamic" bind="setHttpService"  
 *                unbind="unsetHttpService"
 */
public class ApplicationAuthenticationFrameworkServiceComponent{

    private static Log log = LogFactory.getLog(ApplicationAuthenticationFrameworkServiceComponent.class);
    
    private ServiceTracker authTracker;
    private Object lock = new Object();
    public static ApplicationAuthenticator[] authenticators;
    
    private HttpService httpService;	
    
    public static final String COMMON_SERVLET_URL = "/commonauth";

    protected void activate(ComponentContext ctxt) {
        
    	getAuthenticators(ctxt.getBundleContext());
    	
        // Register Common servlet
        Servlet commonServlet = new ContextPathServletAdaptor(new CommonAuthenticationServlet(), COMMON_SERVLET_URL);
        try {
            httpService.registerServlet(COMMON_SERVLET_URL, commonServlet, null, null);
        } catch (Exception e) {
            String errMsg = "Error when registering Common Servlet via the HttpService.";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        
        // Register a AuthenticatorsConfiguration object as an OSGi Service
        ctxt.getBundleContext().registerService(ApplicationAuthenticatorsConfiguration.class.getName(),
                                                ApplicationAuthenticatorsConfiguration.getInstance(), null);
        
        if (log.isDebugEnabled()) {
            log.info("Application Authentication Framework bundle is activated");
        }
    }
    
	private void getAuthenticators(BundleContext bc) {
		authTracker = new ServiceTracker(bc, ApplicationAuthenticator.class.getName(), null);
		authTracker.open();

		if (authenticators == null || authenticators.length == 0 || authenticators[0] == null) {
			synchronized (lock) {
				if (authenticators == null || authenticators.length == 0 ||
				    authenticators[0] == null) {
					Object[] objects = authTracker.getServices();
					// cast each object - cannot cast object array
					authenticators = new ApplicationAuthenticator[objects.length];
					int i = 0;
					for (Object obj : objects) {
						authenticators[i] = (ApplicationAuthenticator) obj;
						i++;
					}
					Arrays.sort(authenticators, new ApplicationAuthenticatorsComparator());
				}
			}
		}
	}

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Application Authentication Framework bundle is deactivated");
        }
    }
    
    protected void setHttpService(HttpService httpService){
        if(log.isDebugEnabled()){
            log.debug("HTTP Service is set in the Application Authentication Framework bundle");
        }
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService){
        if(log.isDebugEnabled()){
            log.debug("HTTP Service is unset in the Application Authentication Framework bundle");
        }
        this.httpService = null;
    }
}