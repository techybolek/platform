/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appfactory.jenkinsext;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to initialize multi-tenanted Jenkins when the first request comes.
 */
public class PostInitializer implements ServletContextListener {

	private static final Log log = LogFactory.getLog(PostInitializer.class);

	public void contextInitialized(ServletContextEvent event) {

		if (log.isDebugEnabled()) {
			log.debug("App factory post initialization invoked when initializing the context.");
			log.debug("Context path - " + event.getServletContext().getContextPath());
		}

		try {
			String tenantHome = System.getenv(JenkinsTenantConstants.JENKINS_TENANT_HOME);
			if (tenantHome == null) {
				String erroMsg =
				                 JenkinsTenantConstants.JENKINS_TENANT_HOME +
				                         " is not set or not pointed to valid location.";
				log.fatal(erroMsg);
				throw new NullPointerException(erroMsg);
			} else {
				log.info(JenkinsTenantConstants.JENKINS_TENANT_HOME + " is set to " + tenantHome);
			}

			InitialContext iniCtxt = new InitialContext();
			Context env = (Context) iniCtxt.lookup("java:comp/env");
			String jenkinsHome = (String) env.lookup("JENKINS_HOME");

			AppfactoryPluginDeployer pluginDeloyer = new AppfactoryPluginDeployer();
			pluginDeloyer.copyBundledPlugin(tenantHome, jenkinsHome);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("App factory post initialization invoked when destroying the context.");
		}
	}
}
