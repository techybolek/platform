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

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This class retrieves the requested resource according to the path parameters
 * passed with the request.
 */

@Path("/artifact")
public class Resources extends RestSuper {

	private Log log = LogFactory.getLog(Resources.class);

	/**
	 * This method get the resource content of the requested resource.
	 * 
	 * @param path
	 *            - path of the resource in the registry.
	 * @param username
	 *            - username of the enduser.
	 * @param tenantID
	 *            - tenant ID of the enduser belongs to username.
	 * @return resource content stream.
	 */
	@GET
	@Path("/{path:.*}")
	@Produces("application/octet-stream")
	public Response getResource(@PathParam("path") List<PathSegment> path,
	                            @QueryParam("user") String username) {

		String resourcePath = getResourcePath(path);
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

		try {
			boolean exist = super.getUserRegistry().resourceExists(resourcePath);
			if (exist) {
				Resource resource = super.getUserRegistry().get(resourcePath);
				// check whether the resource is a collection
				if (resource instanceof Collection) {
					return Response.ok().entity(resource.getContent()).type("application/json")
					               .build();
				} else {
					// get the content of the resource as a stream
					String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
					return Response.ok(resource.getContentStream())
					               .type(resource.getMediaType())
					               .header("Content-Disposition",
					                       "attachment; filename=" + fileName).build();
				}
			} else {
				// the resource is not found
				return Response.status(Response.Status.NOT_FOUND).type("application/json").build();
			}
		} catch (RegistryException e) {
			log.error("user is not authorized to read the given resource", e);
			// the user is not authorized to access the resourcereturn
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	/**
	 * This method put the resource sent an payload to the registry.
	 * 
	 * @param path
	 *            - List of path segment of the resource path after the
	 *            (/resource) in the URI
	 * @param username
	 *            - username of the enduser
	 * @param tenantID
	 *            - tenant ID of the enduser belongs to username
	 * @param type
	 *            - mediatype of the resource will be in this
	 *            format(<mediatype>;<encoding type>)
	 * @param input
	 *            - resource content sent as input stream
	 * @return HTTP 204 response
	 */

	@PUT
	@Path("/{path:.*}")
	@Produces("application/json")
	public Response createResource(@PathParam("path") List<PathSegment> path, InputStream input,
	                               @HeaderParam("Content-Type") String type,
	                               @QueryParam("user") String username) {

		String resourcePath = getResourcePath(path);
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
				// check if collection already exists
				if (type.contains("application/atomcoll+xml")) {
					return Response.status(Response.Status.CONFLICT).build();
				} else {
					// check if resource already exists.
					return Response.status(Response.Status.CONFLICT).build();
				}
			} else if (type != null) {

				Resource resource = null;
				// extract the <mediatype> from the <mediatype>;<encoding
				// format>
				if (type.indexOf(';') > 0) {
					type = type.substring(0, type.indexOf(';'));
				}
				// check for collection media type
				if (type.contains("application/atomcoll+xml")) {
					resource = super.getUserRegistry().newCollection();
					resource.setMediaType(type);
				} else {
					// otherwise create a resource instance
					resource = super.getUserRegistry().newResource();
					resource.setContentStream(input);
				}
				try {
					super.getUserRegistry().put(resourcePath, resource);
					return Response.status(Response.Status.CREATED).build();
				} catch (RegistryException e) {
					log.error(e.getCause(), e);
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
			} else {
				// if media type of the resource not specified
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		} catch (RegistryException e) {
			log.error("user doesn't have permission to put the resource into the registry", e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	/**
	 * This method delete the requested resource.
	 * 
	 * @param path
	 *            - path segment of the resource path
	 * @param username
	 *            - username of the enduser.
	 * @param tenantID
	 *            - tenant ID of the enduser belongs to username.
	 * @return
	 */
	@DELETE
	@Path("/{path:.*}")
	@Produces("application/json")
	public Response deleteResource(@PathParam("path") List<PathSegment> path,
	                               @QueryParam("user") String username) {

		String resourcePath = getResourcePath(path);
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
				// if resource exists delete the resource
				super.getUserRegistry().delete(resourcePath);
				// check whether the resource deleted
				exist = super.getUserRegistry().resourceExists(resourcePath);
				if (exist) {
					// some internal error occurred during the deletion
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
				} else {
					// the resource has been deleted
					return Response.status(Response.Status.NO_CONTENT).build();
				}
			} else {
				// requested resource is not found
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user is not authorized to delete the resource", e);
			// user is not authorized to delete the resource
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}

