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
package org.wso2.carbon.appfactory.tenant.build.integration;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.tenant.build.integration.buildserver.BuildServerApp;
import org.wso2.carbon.appfactory.tenant.build.integration.buildserver.JenkinsBuildSeverApp;
import org.wso2.carbon.appfactory.tenant.build.integration.internal.ServiceContainer;
import org.wso2.carbon.appfactory.tenant.build.integration.uploder.BuildServerUploader;
import org.wso2.carbon.appfactory.tenant.build.integration.uploder.DirectUploader;
import org.wso2.carbon.appfactory.tenant.build.integration.utils.Util;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * Implementation of {@link BuildServerManagementService}. Class provides
 * implementations to do build server managing activities through a WSO2
 * Appserver.
 */
public class AppServerBasedBuildServerManager extends AbstractAdmin implements
		BuildServerManagementService {

	private Log log = LogFactory.getLog(AppServerBasedBuildServerManager.class);

	@Override
	public void createTenant(String tenantDomain)
			throws BuildServerManagementException {
		log.debug("[Invoked] Tennat creation for build server.");
		try {

			String appPath = Util.getCarbonResourcesPath() + File.separator
					+ JenkinsBuildSeverApp.DEFAULT_JENKINS_APP_NAME;
			BuildServerApp serverApp = new JenkinsBuildSeverApp(appPath);
			String modifiedAppPath = serverApp.getModifiedAppPath(tenantDomain);

			ServiceContainer.getInstance().getTenantManager()
					.getTenantId(tenantDomain);
			BuildServerUploader uploader = new DirectUploader(tenantDomain);
			uploader.uploadBuildServerApp(new File(modifiedAppPath));

		} catch (Exception e) {
			String msg = "Error while creating tenant in build server.";
			log.error(msg);
			throw new BuildServerManagementException(msg, e,
					BuildServerManagementException.Code.ERROR_CREATING_TENANT);
		}

	}

}
