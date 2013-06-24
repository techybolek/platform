/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.scim.provider.impl;

import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.scim.provider.auth.SCIMAuthenticatorRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This performs one-time initialization tasks at the application startup.
 */
public class ApplicationInitializer implements ServletContextListener {
    private Log logger = LogFactory.getLog(ApplicationInitializer.class);
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if(logger.isDebugEnabled()){
            logger.debug("Initializing SCIM Webapp...");
        }
        //Initialize Authentication Registry
        initSCIMAuthenticatorRegistry();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private void initSCIMAuthenticatorRegistry(){
        SCIMAuthenticatorRegistry scimAuthRegistry = SCIMAuthenticatorRegistry.getInstance();
        if (scimAuthRegistry != null) {
            System.out.println("Initializing the webapp.......................................");    
        }
    }

    private void buildSCIMAuthenticatorConfig(){
        try {
            IdentityConfigParser identityConfig = IdentityConfigParser.getInstance();
            //identityConfig.getConfigElement("SCIMAuthenticators");
            //TODO:parse the authenticators config and build the authenticators with properties. 
        } catch (ServerConfigurationException e) {
            logger.error("Error in reading authenticator config from " +
                         "identity.xml when initializing the SCIM webapp...");
        }
    }
}
