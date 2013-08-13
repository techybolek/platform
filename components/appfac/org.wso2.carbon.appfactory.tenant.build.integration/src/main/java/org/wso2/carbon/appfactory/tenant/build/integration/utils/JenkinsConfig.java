/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appfactory.tenant.build.integration.utils;

import java.io.File;
import java.io.IOException;


/**
 * Singleton Class represents the configurations related to Jenkins.
 */
public class JenkinsConfig {

	private static final String TENANTS_FOLDER_NAME = "tenants";

	private final static String JENKINS_TENANT_HOME_ENV = "JENKINS_TENANT_HOME";

	private static JenkinsConfig INSTANCE = null;
	
	private final String jenkinsHome;
	
	private final String jenkinsTenantsLocation;
	
	/**
	 * Constructor to initiate Jenkins configurations.
	 * @exception NullPointerException if no JENKINS_TENANT_HOME can be found.
	 * @throws IOException if unable to create jenkins tenant folder .
	 */
	private JenkinsConfig() throws IOException{
		jenkinsHome = checkJenkinsTenantHome();
		jenkinsTenantsLocation = checkJenkinsTenantsLocation();
	}
	
    /**
     * Checks whether the JENKINS_TENANT_HOME is properly set as a env variable.
     * @exception NullPointerException if no JENKINS_TENANT_HOME can be found.
     * @return JENKINS_TENANT_HOME path
     */
	private String checkJenkinsTenantHome() {	   
		String home = System.getenv(JENKINS_TENANT_HOME_ENV);
		if(home == null){
			throw new NullPointerException(JENKINS_TENANT_HOME_ENV + " cannot be found.");
		}
		return home;
    }
	
	/**
	 * Checks whether the Jenkins tenant's folder exists. i.e <JENKINS_HOME>/tenants 
	 * If not creates the folder. 
	 * @return path to jenkin's tenants folder
	 * @throws IOException - if unable to create jenkins tenant folder 
	 */
	private String checkJenkinsTenantsLocation() throws IOException {
		File tenantSpace = new File(jenkinsHome, TENANTS_FOLDER_NAME);
		if(!tenantSpace.exists()){
			if(!tenantSpace.mkdirs()){
				throw new IOException("Problem creating tenant Location for Jenkins. " + tenantSpace.getAbsolutePath());
			}
		}
		return tenantSpace.getAbsolutePath();
	}
	
	/**
	 * Returns the common tenant location for Jenkins.
	 * This location holds different JENKINS_HOME locations for different tenants. 
	 * @return path to jenkins tenant space.
	 */
	public String getTenantsLocation() {
		return jenkinsTenantsLocation;
	}

	public synchronized static JenkinsConfig getIinstance() throws IOException{
		if(INSTANCE == null){
			INSTANCE = new JenkinsConfig();
		}
		return INSTANCE;
	}
		

}
