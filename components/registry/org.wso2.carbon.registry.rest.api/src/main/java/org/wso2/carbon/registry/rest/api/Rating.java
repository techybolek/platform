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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.RatingModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityConstants;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;
import org.wso2.carbon.registry.rest.api.security.UnAuthorizedException;

/**
 * This class retrieved the rating of the requested resource.
 */
@Path("/rating")
public class Rating extends RestSuper {

	private Log log = LogFactory.getLog(Rating.class);
	private Response response = null;

	/**
	 * This method get the rating given for the requested resource.
	 * 
	 * @param resourcePath
	 *            - path of the resource in the registry.
	 * @param username
	 *            - username of the enduser
	 * @param tenantID
	 *            - tenant ID of the enduser belongs to username
	 * @return RatingModel object
	 */
	@GET
	@Produces("application/json")
	public Response getRatingOfAResource(@QueryParam("path") String resourcePath,
			@HeaderParam("X-JWT-Assertion") String JWTToken) {

		RestAPIAuthContext authContext = RestAPISecurityUtils.isAuthorized
				(PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);
		
		if (authContext.isAuthorized()) {
			String username = authContext.getUserName();
			int tenantID = authContext.getTenantId();
			super.createUserRegistry(username, tenantID);
			
			if (RestPathPaginationValidation.validate(resourcePath) == -1) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if (super.getUserRegistry() == null) {
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
			boolean exist;
			try {
				exist = super.getUserRegistry().resourceExists(resourcePath);
				if (exist) {
					RatingModel result =
					                     new RatingModel(super.getUserRegistry()
					                                          .getRating(resourcePath, username),
					                                     super.getUserRegistry()
					                                          .getAverageRating(resourcePath));
					response = Response.ok().entity(result).build();
					return response;
				} else {
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			} catch (RegistryException e) {
				log.error("User does not have permission to read the rating of the resource", e);
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
		} else {
			throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
		}
	}
}
