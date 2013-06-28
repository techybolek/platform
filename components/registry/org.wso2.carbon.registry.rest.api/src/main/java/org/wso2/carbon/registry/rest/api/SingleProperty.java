
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
import java.util.Properties;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.PropertyModel;

@Path("/property")
public class SingleProperty extends RestSuper{
	
	protected Log log = LogFactory.getLog(SingleProperty.class);
	/**
	 * This method add array of JSON property model for the specified resource
	 * @param resourcePath - resource path
	 * @param addProperty - array of PropertyModel objects
	 * @param username - enduser's username.
	 * @param tenantID - enduser's tenantID
	 * @return HTTP 201 response
	 */
	@POST
	@Consumes("application/json")
	public Response addPropertyOnAResource(@QueryParam("path")String resourcePath,
			PropertyModel[] addProperty,@QueryParam("username")String username,
			@QueryParam("tenantid")String tenantID){

		if(RestPathPaginationValidation.validate(resourcePath)== -1){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if(super.createUserRegistry(username,tenantID)== null){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {
			//check whether the resource exist 
			exist = userRegistry.resourceExists(resourcePath);
			if (exist) {
				//get the resource instance for the given path
				Resource resource = userRegistry.get(resourcePath);
				//save the resource after set the properties
			    userRegistry.put(resourcePath, setPropertyOnAResource(addProperty, "add", resource)); 
			    if(log.isDebugEnabled()){
			    	log.debug("specified property added for the given resource");
			    }
			    return Response.status(Response.Status.NO_CONTENT).build();
			} else{	
				//if the resource is not found
				return Response.status(Response.Status.NOT_FOUND).build(); 
			}
		} catch(RegistryException e){
			log.error("user is not allowed to add properties to a resource",e);
			//if user unauthorized to add property to a resource
			return Response.status(Response.Status.UNAUTHORIZED).build(); 
		}	
	}
	/**
	 * This method delete the specified property of the resource
	 * @param resourcePath - path of the resource
	 * @param name - property name to be deleted
	 * @param username - enduser's username
	 * @param tenantID - enduser's tenantID
	 * @return JSON array of property model
	 */
	@DELETE
	@Produces("application/json")
	public Response deleteProperty(@QueryParam("path") String resourcePath, 
			@QueryParam("name") String name,@QueryParam("username")String username,
			@QueryParam("tenantid")String tenantID){
			
		if(RestPathPaginationValidation.validate(resourcePath)== -1){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if(super.createUserRegistry(username,tenantID)== null){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {
			exist = userRegistry.resourceExists(resourcePath);
			if (exist){
				Resource resource = userRegistry.get(resourcePath);
				resource.removeProperty(name);
				userRegistry.put(resourcePath, resource);
				resource = userRegistry.get(resourcePath);	
				java.util.Properties prop = resource.getProperties();
				if (prop != null) {
					return getResult(prop);
				} else{
					log.debug("No properties are available to display");
					return Response.status(Response.Status.NOT_FOUND).build();	
				}
			} else{
				log.debug("Resource does not exist at the given path");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch(RegistryException e){
			log.error("user is not allowed to delete properties on a resource",e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
	/**
	 * Method retrieves the specified property for the given resource
	 * @param resourcePath - resource path in the registry space
	 * @param propertyName - name of the property to be fetched
	 * @param username - enduser's username
	 * @param tenantID - enduser's tenantID
	 * @return JSON property model element
	 */
	@GET
	@Produces("application/json")
	public Response getPropertyOfAResource(@QueryParam("path")String resourcePath,
			@QueryParam("name")String propertyName,@QueryParam("username")String username,
			@QueryParam("tenantid")String tenantID){
		
		if(RestPathPaginationValidation.validate(resourcePath)== -1){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if(super.createUserRegistry(username,tenantID)== null){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {					
			exist = userRegistry.resourceExists(resourcePath);
			if (exist){
				Resource resource = userRegistry.get(resourcePath);	
				java.util.Properties prop = resource.getProperties();
				if(prop.containsKey(propertyName)) {
					return getSingleProperty(propertyName, prop);							
				}	
				else{
					return Response.status(Response.Status.NOT_FOUND).build();
				}		
				
			} else{	
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch(RegistryException e){
			log.error("user is not allowed to read a specified property",e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
	/**
	 * This method updates the existing property
	 * @param resourcePath - path of the resource
	 * @param editProperty - array of JSON Properties [{"name":"<prop name>","value"=["<array of values>"]}]
	 * @param username - enduser's username
	 * @param tenantID - enduser's tenantID
	 * @return HTTP 201 if success
	 */
	@PUT
	@Produces("application/json")
	public Response editPropertyOfAResource(@QueryParam("path")String resourcePath,
			PropertyModel[] editProperty,@QueryParam("username")String username,
			@QueryParam("tenantid")String tenantID){
		
		if(RestPathPaginationValidation.validate(resourcePath)== -1){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if(super.createUserRegistry(username,tenantID)== null){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {					
			exist = userRegistry.resourceExists(resourcePath);
			if(exist){
				Resource resource = userRegistry.get(resourcePath);
				setPropertyOnAResource(editProperty, "edit", resource);
				return Response.status(Response.Status.NO_CONTENT).build();
			} else{	
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch(RegistryException e){
			log.error("user is not allowed to update the existing property",e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
	/**
	 * Method list all the properties
	 * @param prop java.util.Properties variable
	 * @return hashmap <properties>
	 */
	private Response getResult(Properties prop){
		HashMap<String,String[]> map = new HashMap<String,String[]>();
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
		return Response.ok(map).build();
	}
	/**
	 * Method get the specific property
	 * @param prop java.util.Properties variable
	 * @return hashmap <property>
	 */
	private Response getSingleProperty(String propName, Properties prop){
		HashMap<Object,Object> map = new HashMap<Object,Object>();
		String propVal = prop.get(propName).toString();
		int start = propVal.indexOf("[");
		int end = propVal.lastIndexOf("]");
		propVal = propVal.substring(start+1, end);
		String[] propValues = propVal.split(",");
		map.put(propName,propValues);
		return Response.ok(map).build();
	}
	/**
	 * Method adds the property to a resource
	 * @param property array of propert model as payload
	 * @param operation operation review
	 * @param resource resource instance for the resource path 
	 * @return resource instance
	 */
	private Resource setPropertyOnAResource(PropertyModel[] property,String operation,Resource resource){
		
		java.util.Properties prop = resource.getProperties();
		for(int i = 0; i < property.length; i++){
			String prop_value = "";
			String propKey = property[i].getName();
			if(prop.containsKey(propKey) && operation.equals("edit")) { 
				resource.removeProperty(propKey);
			}
			String[] propValue = property[i].getValue();
			prop_value = getPropValue(propValue);		
			resource.addProperty(propKey, prop_value);	
		}
		return resource;
	}
	/**
	 * method creates the string property value from the property array 
	 * @param propValue
	 * @return
	 */
	private String getPropValue(String[] propValue){
		String propertyValue = "";
		for(int j = 0; j < propValue.length; j++){
			if (j == 0){
				propertyValue = propValue[j];
			} else{
				propertyValue += ","+ propValue[j];
			}
		}
		return propertyValue;
	}
}
