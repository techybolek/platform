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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.ResourceModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityConstants;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;
import org.wso2.carbon.registry.rest.api.security.UnAuthorizedException;

@Path("/metadata")
public class SingleResource extends RestSuper {

	private Log log = LogFactory.getLog(SingleResource.class);

	/**
	 * This method gets the metadata info about the given resource
	 * 
	 * @param rPath
	 *            : resource path
	 * @param username
	 *            : enduser's username
	 * @param tenantID
	 *            : enduser's tenant ID
	 * @return JSON ResourceModel object
	 */
	@GET
	@Produces("application/json")
	public Response getMetaData(@QueryParam("path") String rPath,
			@HeaderParam("X-JWT-Assertion") String JWTToken) {
		RestAPIAuthContext authContext = RestAPISecurityUtils.isAuthorized
				(PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);
		
		if (authContext.isAuthorized()) {
			String username = authContext.getUserName();
			int tenantID = authContext.getTenantId();
			super.createUserRegistry(username, tenantID);
			
			if (RestPathPaginationValidation.validate(rPath) == -1) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if (super.getUserRegistry() == null) {
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
			boolean exists;
			try {
				// check the resource exist
				exists = super.getUserRegistry().resourceExists(rPath);
				if (exists) {
					Resource resource = super.getUserRegistry().get(rPath);
					ResourceModel resourceModel = new ResourceModel(resource);
					return Response.ok(resourceModel).build();
				} else {
					log.debug("resource not found at the given path");
					// if resource is not found
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			} catch (RegistryException e) {
				log.error("user doesn't have permission to read the resource", e);
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
		} else {
			throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
		}
	}
}

