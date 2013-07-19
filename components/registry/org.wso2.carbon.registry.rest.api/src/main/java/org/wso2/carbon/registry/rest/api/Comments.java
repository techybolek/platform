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
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.CommentModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityConstants;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;
import org.wso2.carbon.registry.rest.api.security.UnAuthorizedException;

/**
 * This class retrieves the comments of the requested resource according to the
 * parameters passed with the request.
 */
@Path("/comments")
public class Comments extends PaginationCalculation<Comment> {

	private Log log = LogFactory.getLog(Comments.class);
	private String path = null;

	/**
	 * This method get the comments on the requested resource.
	 * 
	 * @param resourcePath
	 *            path of the resource in the registry
	 * @param start
	 *            starting page number
	 * @param size
	 *            number of records to be retrieved
	 * @param username
	 *            username of the enduser
	 * @return array of CommentModel objects
	 * 
	 */
	@GET
	@Produces("application/json")
	public Response getCommentsOnAResource(@QueryParam("path") String resourcePath,
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
			// check for null instance of user registry unless can not be created.
			if (super.getUserRegistry() == null) {
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}

			boolean exist;
			try {
				// check whether resource exist.
				exist = super.getUserRegistry().resourceExists(resourcePath);
				if (exist) {
					return displayPaginatedResult(start, size);
				} else {
					// if resource does not exist
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			} catch (RegistryException e) {
				log.error("User is not authorized to access the resource", e);
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
		} else {
			throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
		}
	}

	/**
	 * this method returns the array of comment for the given resource.
	 */
	protected Comment[] getResult() {
		try {
			return super.getUserRegistry().getComments(path);
		} catch (RegistryException e) {
			log.error("User is not authrorized to read the comments on the given resource", e);
		}
		return new Comment[0];
	}

	/**
	 * This method bind the array of paginated comment objects
	 */
	protected Response display(Comment[] commentArr, int begin, int end) {
		CommentModel[] message = new CommentModel[end - begin + 1];
		for (int i = end, j = 0; i >= begin && j < message.length; i--, j++) {
			message[j] = new CommentModel(commentArr[i]);
		}
		return Response.ok(message).build();
	}
	
}
