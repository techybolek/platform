package org.wso2.carbon.appfactory.jenkinsext;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class AppfactoryConfigurationDeployer {
	protected void copyBundledConfigs(String src, String dest) throws IOException {
		 
		 String configPath = src + File.separator + JenkinsTenantConstants.COMMON_CONFIGS_DIR;
		 File source = new File(configPath);
		 
		 if(!source.exists()){
			 throw new IllegalArgumentException("Common plugin location cannot be found at " + configPath );
		 }
		 
		 File destination = new File(dest, JenkinsTenantConstants.COMMON_CONFIG_DESTINATION);
		 FileUtils.copyDirectory(source, destination);
	 }
}
