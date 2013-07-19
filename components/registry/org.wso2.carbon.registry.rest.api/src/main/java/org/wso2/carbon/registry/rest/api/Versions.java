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
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityConstants;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;
import org.wso2.carbon.registry.rest.api.security.UnAuthorizedException;

@Path("/versions")
public class Versions extends RestSuper {

	private Log log = LogFactory.getLog(Versions.class);

	/**
	 * This method get the comments on the requested resource.
	 * 
	 * @param path
	 *            path of the resource in the registry
	 * @param start
	 *            starting page number
	 * @param size
	 *            number of records to be retrieved
	 * @param username
	 *            username of the enduser
	 * @return array of version IDs
	 * 
	 */
	@GET
	@Produces("application/json")
	public Response getVersionsOnAResource(@QueryParam("path") String path,
	                                       @QueryParam("start") int start,
	                                       @QueryParam("size") int size,
	                                       @HeaderParam("X-JWT-Assertion") String JWTToken) {
		RestAPIAuthContext authContext = RestAPISecurityUtils.isAuthorized
				(PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);
		
		if (authContext.isAuthorized()) {
			String username = authContext.getUserName();
			int tenantID = authContext.getTenantId();
			super.createUserRegistry(username, tenantID);
			
			if (RestPathPaginationValidation.validate(start, size) == -1) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			if (super.getUserRegistry() == null) {
				throw new UnAuthorizedException(RestAPISecurityConstants.UNAUTHORIZED_ERROR);
			}
			try {
				boolean exist = super.getUserRegistry().resourceExists(path);
				if (exist) {
					log.debug("Versioned resource exist for the given path");
					String[] result = super.getUserRegistry().getVersions(path);
					String[] message = new String[result.length];
					for (int i = 0; i < message.length; i++) {
						message[i] = result[i].split(":")[1];
					}
					HashMap<String, String[]> map = new HashMap<String, String[]>();
					map.put("versions", message);
					return Response.ok(map).build();
				} else {
					log.debug("Versioned resource does not exist for the given path");
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
}