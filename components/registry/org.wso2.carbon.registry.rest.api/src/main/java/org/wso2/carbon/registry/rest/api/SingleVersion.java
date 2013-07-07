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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

@Path("/version")
public class SingleVersion extends RestSuper {

	private Log log = LogFactory.getLog(SingleVersion.class);

	/**
	 * This method get the comments on the requested resource.
	 * 
	 * @param path
	 *            path of the resource in the registry
	 * @param id
	 *            version ID
	 * @param username
	 *            username of the enduser
	 * @return the resource
	 * 
	 */
	@GET
	@Produces("application/octet-stream")
	public Response getAVersionedResource(@QueryParam("path") String path,
	                                      @QueryParam("id") long versionID,
	                                      @QueryParam("user") String username) {

		if (username == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			String tenantID = super.getTenantID();
			super.createUserRegistry(username, tenantID);
		}
		// null check for resource path
		if (RestPathPaginationValidation.validate(path) == -1) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		// null check for user registry instance
		if (super.getUserRegistry() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		String versionPath = getVersionPath(path, versionID);
		try {
			boolean exist = super.getUserRegistry().resourceExists(versionPath);
			if (exist) {
				if (log.isDebugEnabled()) {
					log.debug("Versioned resource exist for the given path");
				}
				Resource resource = super.getUserRegistry().get(versionPath);
				if (resource instanceof Collection) {
					resource.setMediaType("application/json");
				}
				return Response.ok().entity(resource.getContent()).type(resource.getMediaType())
				               .build();
			} else {
				log.debug("Versioned resource does not exist for the given path");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user is not allowed to get a specific resource version", e);
			// if the user is not allowed
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	/**
	 * This method get the comments on the requested resource.
	 * 
	 * @param path
	 *            path of the resource in the registry
	 * @param id
	 *            version ID
	 * @param username
	 *            username of the enduser
	 * 
	 */
	@DELETE
	@Produces("application/json")
	public Response deleteAVersionedResource(@QueryParam("path") String path,
	                                         @QueryParam("id") long versionID,
	                                         @QueryParam("user") String username) {

		if (username == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			String tenantID = super.getTenantID();
			super.createUserRegistry(username, tenantID);
		}
		if (RestPathPaginationValidation.validate(path) == -1) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if (super.getUserRegistry() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		String versionPath = getVersionPath(path, versionID);
		try {
			boolean exist = super.getUserRegistry().resourceExists(versionPath);
			if (exist) {
				super.getUserRegistry().removeVersionHistory(path, versionID);
				exist = super.getUserRegistry().resourceExists(versionPath);
				if (exist) {
					log.debug("Versioned resource can not be deleted");
					return Response.status(Response.Status.BAD_REQUEST).build();
				} else {
					log.debug("requested versioned resource has been deleted");
					return Response.status(Response.Status.OK).build();
				}
			} else {
				log.debug("requested versioned resource does not exist");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user is not allowed to get a specific resource version", e);
			// if the user is not allowed
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	private String getVersionPath(String resourcePath, long versionID) {
		/* /_system/governance/test4;version:3 */
		return resourcePath + ";version:" + versionID;
	}
}