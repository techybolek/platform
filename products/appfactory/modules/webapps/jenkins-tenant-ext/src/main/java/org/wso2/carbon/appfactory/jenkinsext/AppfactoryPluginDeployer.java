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
package org.wso2.carbon.appfactory.jenkinsext;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class AppfactoryPluginDeployer {

	/**
	 * Deploys Jenkins plugins to given {@code dest}
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	 protected void copyBundledPlugin(String src, String dest) throws IOException {
		 
		 String pluginPath = src + File.separator + JenkinsTenantConstants.COMMON_PLUGINS_DIR;
		 File source = new File(pluginPath);
		 
		 if(!source.exists()){
			 throw new IllegalArgumentException("Common plugin location cannot be found at " + pluginPath );
		 }
		 
		 File destination = new File(dest, "plugins");
		 FileUtils.copyDirectory(source, destination);
	 }
	
}
