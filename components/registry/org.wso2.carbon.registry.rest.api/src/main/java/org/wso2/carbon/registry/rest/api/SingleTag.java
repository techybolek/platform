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

import java.util.HashMap;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityConstants;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;
import org.wso2.carbon.registry.rest.api.security.UnAuthorizedException;

@Path("/tag")
public class SingleTag extends PaginationCalculation<TaggedResourcePath> {

	private Log log = LogFactory.getLog(SingleTag.class);
	private String tag = null;

	/**
	 * log.error(e.getMessage());
	 * This method retrieves the resource paths for a given tag name
	 * 
	 * @param tagName
	 *            name of the tag
	 * @param start
	 *            page start number
	 * @param size
	 *            number of records to be fetched
	 * @param username
	 *            enduser's username
	 * @param tenantID
	 *            enduser's tenant ID
	 * @return JSON object eg: {"path":[<array of resource paths tagged by the
	 *         tagname>]}protected
	 */
	@GET
	@Produces("application/json")
	public Response getTaggedResources(@QueryParam("name") String tagName,
	                                   @QueryParam("start") int start,
	                                   @QueryParam("size") int size,
	                                   @HeaderParam("X-JWT-Assertion") String JWTToken) {
		RestAPIAuthContext authContext = RestAPISecurityUtils.isAuthorized
				(PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);
		
		if (authContext.isAuthorized()) {
			String username = authContext.getUserName();
			int tenantID = authContext.getTenantId();
			super.createUserRegistry(username, tenantID);
			
			if (tagName.length() == 0) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if (RestPathPaginationValidation.validate(start, size) == -1) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if (super.getUserRegistry() == null) {
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			} else {
				tag = tagName;
				return displayPaginatedResult(start, size);
			}
		} else {
			throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
		}

	}

	/**
	 * This method deletes the specified tag on the given resource
	 * 
	 * @param resourcePath
	 *            path of the resource
	 * @param tagName
	 *            name of the tag
	 * @param username
	 *            enduser's usernamedisplayPaginatedResult
	 * @param tenantID
	 *            enduser's tenant ID
	 * @return HTTP 201 response
	 */
	@DELETE
	@Produces("application/json")
	public Response deleteTag(@QueryParam("path") String resourcePath,
	                          @QueryParam("name") String tagName,
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
				boolean tagFound = false;
				if (exist) {
					Tag[] tags = super.getUserRegistry().getTags(resourcePath);
					for (int j = 0; j < tags.length; j++) {
						// if tag has been found remove the tag,set the tag found
						// variable to true
						if (tagName.equals(tags[j].getTagName())) {
							super.getUserRegistry().removeTag(resourcePath, tagName);
							tagFound = true;
						}
					}
					if (tagFound) {
						// if tag deleted
						return Response.status(Response.Status.NO_CONTENT).build();
					} else {
						log.debug("tag not found");
						// if the specified tag is not found,returns http 404
						return Response.status(Response.Status.NOT_FOUND).build();
					}
				} else {
					log.debug("resource not found");
					// if resource is not found,returns http 404
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			} catch (RegistryException e) {
				log.error("user doesn't have permission to delete a tag", e);
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
		} else {
			throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
		}
	}

	/**
	 * this method returns the array of Tag for the given resource.
	 */
	protected TaggedResourcePath[] getResult() {
		try {
			// get the array of resources tagged with the specific tag name
			return super.getUserRegistry().getResourcePathsWithTag(tag);
		} catch (RegistryException e) {
			log.error("user doesn't have permission to get the tagged paths", e);
		}
		return new TaggedResourcePath[0];
	}

	/**
	 * This method bind the array of paginated tag objects
	 */
	protected Response display(TaggedResourcePath[] tagPathArr, int begin, int end) {
		String[] tagPaths = new String[end - begin + 1];
		for (int i = 0, j = begin; i < tagPaths.length && j <= end; i++, j++) {
			tagPaths[i] = tagPathArr[j].getResourcePath();
		}
		HashMap<String, String[]> pathMap = new HashMap<String, String[]>();
		pathMap.put("resourcePaths", tagPaths);
		return Response.ok(pathMap).build();
	}
}

