
/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.rest.api;

import java.util.List;
import javax.ws.rs.core.PathSegment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class RestSuper { 
	
	protected UserRegistry userRegistry = null;
	protected int pageSize = 10;
	protected int begin = 0;
	protected int end = 0;
	protected Log log = LogFactory.getLog(RestSuper.class);
	
    /**
     * This method creates the registry instance belongs to the particular user to isolate from other users
     * @param username username of the authorized enduser belongs to the access token
     * @param tenantID tenantID of the authorized enduser belongs to the access token
     * @return user registry instance
     */
    @SuppressWarnings("finally")
	protected UserRegistry createUserRegistry(String username,String tenantID){
    	RegistryService registryService = (RegistryService)PrivilegedCarbonContext.getCurrentContext().getOSGiService(RegistryService.class);
    	try {
			userRegistry = registryService.getUserRegistry(username, Integer.parseInt(tenantID)); 
		} catch (NumberFormatException e) {
			log.error("Unable to convert the tenantID to integer",e);
		} catch (RegistryException e) {
			log.error("unable to create registry instance for the respective enduser",e);
		}finally{
			return userRegistry;
		}
    }
    /**
     * This method calculates the string literal of the requested path of the resource.
     * @param path list of path segment (eg: <_system,governance,sample.xml> 
     * @return concatenated string representation of the path variable
     */
	protected String getResourcePath(List<PathSegment> path){
		String resourcePath = "";
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(resourcePath);
		for (PathSegment pathSegment : path) {
			strBuilder.append("/");
			strBuilder.append(pathSegment);
			//resourcePath = resourcePath + "/" + pathSegment;
		}
		resourcePath = strBuilder.toString();
		if(resourcePath.length()== 0){
			resourcePath = "/";
		}
		return resourcePath; 
	}
}