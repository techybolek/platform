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

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.RatingModel;

@Path("/rate")
public class SingleRate extends RestSuper {

	private Log log = LogFactory.getLog(SingleRate.class);

	/**
	 * This method put a rating to a resource
	 * 
	 * @param resourcePath
	 *            - path of the resource in the registry space
	 * @param value
	 *            - user's rating
	 * @param username
	 *            - enduser's username
	 * @param tenantID
	 *            - enduser's tenantID
	 * @return JSON RatingModel object eg:{"average":<value>,"myRating":<value>}
	 */
	@PUT
	@Produces("application/json")
	public Response RateResource(@QueryParam("path") String resourcePath,
	                             @QueryParam("value") int value, @QueryParam("user") String username) {

		if (username == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			String tenantID = super.getTenantID();
			super.createUserRegistry(username, tenantID);
		}
		if (RestPathPaginationValidation.validate(resourcePath) == -1) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if (super.getUserRegistry() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {
			exist = super.getUserRegistry().resourceExists(resourcePath);
			if (exist) {
				// if user try to rate a resource with value greater than 5,
				// will throws HTTP 400.
				if (value > 5) {
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
				super.getUserRegistry().rateResource(resourcePath, value);
				RatingModel result =
				                     new RatingModel(super.getUserRegistry()
				                                          .getRating(resourcePath, username),
				                                     super.getUserRegistry()
				                                          .getAverageRating(resourcePath));
				return Response.ok(result).build();
			} else {
				log.debug("resource does not exist on the path");
				// if resource is not found, returns HTTP 404
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user doesn't have permission to rate a resource", e);
			// if user does not have permission to read the resource
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	/**
	 * this method delete the user's rating on the given resource
	 * 
	 * @param : resourcePath - path of the resource
	 * @param : username enduser's username
	 * @param : tenantID enduser's tenant ID
	 * @return JSON RatingModel object
	 */
	@DELETE
	@Produces("application/json")
	public Response deleteRating(@QueryParam("path") String resourcePath,
	                             @QueryParam("user") String username) {

		if (username == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			String tenantID = super.getTenantID();
			super.createUserRegistry(username, tenantID);
		}
		if (RestPathPaginationValidation.validate(resourcePath) == -1) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if (super.getUserRegistry() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		try {
			exist = super.getUserRegistry().resourceExists(resourcePath);
			if (exist) {
				// set the user specific rating to 0
				super.getUserRegistry().rateResource(resourcePath, 0);
				RatingModel result =
				                     new RatingModel(super.getUserRegistry()
				                                          .getRating(resourcePath, username),
				                                     super.getUserRegistry()
				                                          .getAverageRating(resourcePath));
				return Response.ok(result).build();
			} else {
				log.debug("resource does not exist on the path");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user doesn't have permission to delete the rating", e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}

