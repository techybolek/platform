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
package org.wso2.carbon.appfactory.tenant.build.integration.buildserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.tenant.build.integration.BuildServerManagementException;
import org.wso2.carbon.appfactory.tenant.build.integration.utils.JenkinsConfig;
import org.wso2.carbon.appfactory.tenant.build.integration.utils.Util;

/**
 * Represent the Jenkins Build Server App.
 */
public class JenkinsBuildSeverApp extends BuildServerApp {

	private Log log = LogFactory.getLog(JenkinsBuildSeverApp.class);

	private static final String TMP_FOLDER_NAME = "tmp";

	public final static String DEFAULT_JENKINS_APP_NAME = "jenkins.war";

	public JenkinsBuildSeverApp(String filePath) throws FileNotFoundException {
		super("Jenkins", filePath);
	}

	@Override
	public String getModifiedAppPath(String tenant) throws IOException,
	                                               BuildServerManagementException {
		File tenantLocation = new File(JenkinsConfig.getInstance().getTenantsLocation(), tenant);
		File tenantTmpLocation = new File(tenantLocation, TMP_FOLDER_NAME);
		File jenkinsFileLocation = new File(tenantTmpLocation, DEFAULT_JENKINS_APP_NAME);

		if (log.isDebugEnabled()) {
			log.debug("Jenkins web app is making ready to modify with " + tenant +
			          " tenant information " + jenkinsFileLocation.getAbsolutePath());
		}
		if (jenkinsFileLocation.exists()) {
			String msg =
			             "Jenkins web app is already exist at the location " +
			                     jenkinsFileLocation.getAbsolutePath() + ". For tenant " + tenant;
			log.error(msg);
			new BuildServerManagementException(
			                                   msg,
			                                   BuildServerManagementException.Code.TENANT_APP_ALREADY_EXISTS);
		}
		log.info("Copying Jenkins web app to tenant location " +
		         tenantTmpLocation.getAbsolutePath());

		FileUtils.copyFile(getFile(), jenkinsFileLocation);

		try {
			updateWebApp(tenantTmpLocation.getAbsolutePath(), tenantLocation.getAbsolutePath(),
			             jenkinsFileLocation.getAbsolutePath());

			log.info("Jenkins web app updated with tenant " + tenant + " tenant information " +
			         jenkinsFileLocation.getAbsolutePath());
			return jenkinsFileLocation.getAbsolutePath();

		} catch (InterruptedException e) {
			String msg = "Error while updating web application with tenant context.";
			log.error(msg, e);
			throw new BuildServerManagementException(
			                                         msg,
			                                         BuildServerManagementException.Code.ERROR_CREATING_TENANT_APP);
		}

	}

	/**
	 * Updates Jenkins web app by inserting context.xml. This context.xml tells
	 * the web app where the Jenkins home is. Jenkins home needs to be different
	 * from tenant to tenant.
	 * 
	 * @param tmpLocation
	 * @param tenantHomePath
	 * @param jenkinsWebApp
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void updateWebApp(String tmpLocation, String tenantHomePath, String jenkinsWebApp)
	                                                                                          throws IOException,
	                                                                                          InterruptedException {

		String content =
		                 "<?xml version='1.0' encoding='utf-8'?>" + "<Context>" +
		                         "<Environment name='JENKINS_HOME' value='" + tenantHomePath +
		                         "' type='java.lang.String' />" + "</Context>";

		File contextFile = new File(tmpLocation, "META-INF" + File.separator + "context.xml");

		if (log.isDebugEnabled()) {
			log.debug("Creating Context.xml for the with tenant information at " +
			          contextFile.getParent());
		}
		if (contextFile.exists()) {
			if (log.isDebugEnabled()) {
				log.debug("Context.xml is already exist at " + contextFile.getAbsolutePath() +
				          " Deleting.");
			}
			contextFile.delete();
		}

		Util.createFile(contextFile, content);

		log.info("Context.xml is created at  " + contextFile.getParent() +
		         ". Updating war file with context.xml");

		Process p =
		            Runtime.getRuntime().exec("jar -uf " + jenkinsWebApp + " -C " + tmpLocation +
		                                              " META-INF" + File.separator + "context.xml");
		p.waitFor();
	}

}
