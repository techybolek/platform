/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.AssociationModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityConstants;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;
import org.wso2.carbon.registry.rest.api.security.UnAuthorizedException;

/**
 * This class retrieved the associations of the requested resource.
 */
@Path("/associations")
public class Associations extends PaginationCalculation<Association> {

	private Log log = LogFactory.getLog(Associations.class);
	private String path = null;
	private String associationType = "";

	/**
	 * This method takes the following parameters:
	 * 
	 * @param resourcePath
	 *            path of the resource,
	 * @param type
	 *            type of association,
	 * @param start
	 *            start page number,
	 * @param size
	 *            number of records to be retrieved
	 * @param username
	 *            username of the enduser,
	 * @param tenantD
	 *            tenantID of the enduser belongs to the username
	 * @returns the array of AssociationModel.
	 */
	@GET
	@Produces("application/json")
	public Response getAssociationsOnAResource(@QueryParam("path") String resourcePath,
	                                           @QueryParam("type") String type,
	                                           @QueryParam("start") int start,
	                                           @QueryParam("size") int size,
	                                           @HeaderParam("X-JWT-Assertion") String JWTToken) {
		RestAPIAuthContext authContext = RestAPISecurityUtils.isAuthorized
				(PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);
		
		if (authContext.isAuthorized()) {
			path = resourcePath;
			String username = authContext.getUserName();
			int tenantID = authContext.getTenantId();
			super.createUserRegistry(username, tenantID);
			
			if (RestPathPaginationValidation.validate(resourcePath, start, size) == -1) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if (super.getUserRegistry() == null) {
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
			boolean exist;
			associationType = type;
			try {
				exist = super.getUserRegistry().resourceExists(resourcePath);
				if (exist) {
					log.debug("processing the pagination result");
					return displayPaginatedResult(start, size);
					// return response;
				} else {
					log.debug("resource not found");
					// if resource not found
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			} catch (RegistryException e) {
				log.error("User does not have required permission to access the resource", e);
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
		} else {
			throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
		}

	}

	/**
	 * this method returns the array of Association for the given resource.
	 */

	protected Association[] getResult() {

		try {
			if (associationType != null) {
				// returns the given type of associations for the given resource
				return super.getUserRegistry().getAssociations(path, associationType);
			} else {
				// returns all the associations of the given resource
				return super.getUserRegistry().getAllAssociations(path);
			}
		} catch (RegistryException e) {
			log.error("User is not authrorized to read the associations on the given resource", e);
		}
		return new Association[0];
	}

	/**
	 * This method bind the array of paginated association objects
	 */
	protected Response display(Association[] associationArr, int begin, int end) {
		AssociationModel[] message = new AssociationModel[end - begin + 1];
		for (int i = 0, j = begin; i < message.length && j <= end; i++, j++) {
			message[i] = new AssociationModel(associationArr[j]);
		}
		return Response.ok(message).build();
	}
}
