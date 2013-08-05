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

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Common class to define utility methods. 
 */
public class Util {

	public static String getCarbonResourcesPath(){		
		return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources";
	}

	public static void createFile(File file, String content) throws IOException {
	   FileUtils.writeStringToFile(file, content);	    
    }
	
	public static String getCarbonTenantPath(){    
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "tenants";
	} 
	
}
