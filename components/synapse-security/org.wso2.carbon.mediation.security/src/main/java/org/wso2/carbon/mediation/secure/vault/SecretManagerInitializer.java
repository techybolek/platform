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

package org.wso2.carbon.mediation.secure.vault;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;


/**
 * 
 */
public class SecretManagerInitializer {

	private static SecretManagerInitializer secretManagerInitializer = null;
	private SecretManager secretManager = SecretManager.getInstance();
	private static final Log log = LogFactory.getLog(SecretManagerInitializer.class);
	public static final String CARBON_HOME = "carbon.home";
	private String SECRET_CONF = "secret-conf.properties";
	private static String CONF_DIR = "conf";
	private static String REPOSITORY_DIR = "repository";
	private static final String SECURITY_DIR = "security";
	private static String GLOBAL_PREFIX = "carbon.";

	private SecretManagerInitializer() {
		super();
	}
	
	public static SecretManagerInitializer getInstance(){
		if(secretManagerInitializer == null){
			secretManagerInitializer = new SecretManagerInitializer();
		}
		return secretManagerInitializer;
	}

	public void init(Properties registryProperties,Resource resource) {

		Properties properties = new Properties();

		if (secretManager.isInitialized()) {
			if (log.isDebugEnabled()) {
				log.debug("SecretManager already has been initialized.");
			}
		} else {
			properties = loadProperties();
			secretManager.init(properties,registryProperties,resource);
		}

	}
	
	public void refresh() throws RegistryException {
		try {
			SecurityServiceHolder registryProperties = SecurityServiceHolder
					.getInstance();
			UserRegistry registry = registryProperties.getRegistryService()
					.getConfigSystemRegistry();
			Resource resource = registry.get("connector-secure-vault-config");
			Properties properties = new Properties();

			properties = loadProperties();
			secretManager.resetInit();
			secretManager.init(properties, resource.getProperties(), resource);
		} catch (Exception e) {
			throw new RegistryException("Error while refresh the registry");
		}

	}

	private Properties loadProperties() {
		Properties properties = new Properties();
		String carbonHome = System.getProperty(CARBON_HOME);
		String filePath = carbonHome + File.separator + REPOSITORY_DIR + File.separator
				+ CONF_DIR + File.separator + SECURITY_DIR + File.separator + SECRET_CONF;

		File dataSourceFile = new File(filePath);
		if (!dataSourceFile.exists()) {
			return properties;
		}

		InputStream in = null;
		try {
			in = new FileInputStream(dataSourceFile);
			properties.load(in);
		} catch (IOException e) {
			String msg = "Error loading properties from a file at :" + filePath;
			log.warn(msg, e);
			return properties;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {

				}
			}
		}
		return properties;
	}

	
}
