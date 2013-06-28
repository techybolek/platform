
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This class retrieves the properties of the requested resource according to the parameters passed with the request.
 */
@Path("/properties")
public class Properties extends RestSuper {

	protected Log log = LogFactory.getLog(Properties.class);
	/**
	 * This method get the properties on the requested resource. 
	 * @param resourcePath - path of the resource in the registry.
	 * @param start - starting page number
	 * @param size - number of records to be retrieved
	 * @param username - username of the enduser
	 * @param tenantID - tenant ID of the enduser belongs to username
	 * @return array of properties
	 */
	@GET
	@Produces("application/json")
	public Response getPropertiesOnAResource(@QueryParam("path")String resourcePath, 
			@QueryParam("start")int start,@QueryParam("size")int size,
			@QueryParam("username")String username,@QueryParam("tenantid")String tenantID){
		
		if(RestPathPaginationValidation.validate(resourcePath, start, size)== -1){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if(super.createUserRegistry(username,tenantID)== null){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {					
			exist = userRegistry.resourceExists(resourcePath);
			if (exist){
				return getResultForPagination(start, size, resourcePath);	 
			} else{	
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("User doesn't allow to access the resource",e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
	/**
	 * pagination result for the properties
	 * @param start start page number
	 * @param size number of records to fetch
	 * @param resourcePath path of the resource
	 * @return 
	 */
	private Response getResultForPagination(int start,int size,String resourcePath){
		try{
			Resource resource = userRegistry.get(resourcePath);
			java.util.Properties prop = resource.getProperties();
			HashMap<String, String[]> propertyMap = getPropertyMap(prop);
			if(start == 0 && size == 0){
				if(propertyMap != null){
					return Response.ok(propertyMap).build();
				} else {
					//if resource does not have properties returns HTTP 204. 
					return Response.status(Response.Status.NO_CONTENT).build();
				}		
			} else if(start > 0 && size > 0){ 
				//assume that 10 records per page will be displayed
				Set<String> propertyKeys = propertyMap.keySet();
				Object[] arrayPropKeys = propertyKeys.toArray();
				HashMap<Object,Object> map = new HashMap<Object, Object>();
				int startRecordNum = (start-1)*pageSize;
				int finishRecordNum = startRecordNum+size;	
				try{
					for(int i = startRecordNum; i < finishRecordNum; i++){
						map.put(arrayPropKeys[i], propertyMap.get(arrayPropKeys[i]));  
					}
					return Response.ok(map).build();
				} catch(ArrayIndexOutOfBoundsException e){
					log.error("User requested the non existing number of results",e);
					return Response.ok(map).build();
				}
			} else{
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		} catch(RegistryException e){
			log.error("User doesn't have permission to read the properties for the given resource",e);
			Response.status(Response.Status.UNAUTHORIZED).build();
		}
		return null;
	}
	
	private HashMap<String, String[]> getPropertyMap(java.util.Properties prop){	
		HashMap<String,String[]> map = new HashMap<String,String[]>();
		if (prop != null) {
			Enumeration<Object> propName = prop.keys();
			while(propName.hasMoreElements()){
				String property = propName.nextElement().toString();
				Object propertyValue = prop.get(property);
				String propValue = propertyValue.toString();
				int startIndex = propValue.indexOf("[");
				int lastIndex = propValue.indexOf("]");
				propValue = propValue.substring(startIndex+1, lastIndex);
				String[] propString = propValue.split(",");
				map.put(property, propString);
			}
		}
		return map;			
	}
}