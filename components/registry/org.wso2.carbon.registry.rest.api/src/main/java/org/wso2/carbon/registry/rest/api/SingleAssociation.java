
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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.AssociationModel;

@Path("/association")
public class SingleAssociation extends RestSuper {
	
	protected Log log = LogFactory.getLog(SingleAssociation.class);
	/**
	 * This method add the array of association sent as payload with the request for the given source
	 * @param resourcePath - path of the source
	 * @param association - Json array of association objects[{"target":"<target path>","type":"<association type>"}]
	 * @param username - username of the end user
	 * @param tenantID - tenant ID of the end user
	 * @return an array of association model
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response AddAssociationOnAResource( @QueryParam("path") String resourcePath,
			AssociationModel[] association,@QueryParam("username")String username,
			@QueryParam("tenantid")String tenantID){
		
		if(RestPathPaginationValidation.validate(resourcePath) == -1){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if(super.createUserRegistry(username,tenantID)== null){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean existResourcePath;
		try {
			//check for resource exist
			existResourcePath = userRegistry.resourceExists(resourcePath);
			if (existResourcePath){
				for(int i = 0; i < association.length; i++){
					//check for target resource exist
					boolean existTargetPath = userRegistry.resourceExists(association[i].getTarget());
					if(existTargetPath){
						//add association for the given resource
						userRegistry.addAssociation(resourcePath, 
								association[i].getTarget(), 
								association[i].getType());	
						if(log.isDebugEnabled()){
							log.debug("association has been added");
						}
					} else{
						if(log.isDebugEnabled()){
							log.debug("target resource path does not exist");
						}
						return Response.status(Response.Status.BAD_REQUEST).build();
					}
				}
				//get all the associations after added
				Association[] result = userRegistry.getAllAssociations(resourcePath);
				//ArrayList<AssociationModel> list = new ArrayList<AssociationModel>();
				AssociationModel[] message = new AssociationModel[result.length];
				for (int i = 0; i < result.length; i++) {
					message[i] = new AssociationModel(result[i]);
				}
				//return the associations in JSON
				return Response.ok(message).build();
			} else {
				if(log.isDebugEnabled()){
					log.debug("The source resource path does not exist");
				}
				//if resource is not found
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user is not allowed to add associations to a resource",e);
			//if user is not authorized
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
	/**
	 * This method delete the association sent as query param in the request.
	 * @param resourcePath - path of the source resource
	 * @param type - type of association
	 * @param target - path of the target resource
	 * @param username - username of the enduser 
	 * @param tenantID - tenantID of the enduser
	 * @return array of association model.
	 */
	@DELETE
	@Produces("application/json")
	public Response DeleteAssociationOnAResource(@QueryParam("path") String resourcePath,
			@QueryParam("type") String type,@QueryParam("target") String target,
			@QueryParam("username")String username,@QueryParam("tenantid")String tenantID){
		
		if(RestPathPaginationValidation.validate(resourcePath)== -1){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if(super.createUserRegistry(username,tenantID)== null){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean existResourcePath;
		try {
			//check for the existence of the resource
			existResourcePath = userRegistry.resourceExists(resourcePath);
			boolean existTargetPath = userRegistry.resourceExists(target);
			if (existResourcePath && existTargetPath){
				//removes the association on the resource
				userRegistry.removeAssociation(resourcePath,target, type);
				if(log.isDebugEnabled()){
					log.debug("resource has been deleted");
				}
				Association[] result = userRegistry.getAllAssociations(resourcePath);
				AssociationModel[] message = new AssociationModel[result.length];
				//bind the association to a model object array
				for (int i = 0; i < result.length; i++) {
					message[i] = new AssociationModel(result[i]);
				}
				return Response.ok(message).build();
			} else {
				if(log.isDebugEnabled()){
					log.debug("source and/or target resource path don't exist");
				}
				//if resource not found
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user is not allowed to delete association on the resource",e);
			//if user is not authorized to access the resource
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}