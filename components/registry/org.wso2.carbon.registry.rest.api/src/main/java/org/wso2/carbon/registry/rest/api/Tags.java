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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.TagModel;

@Path("/tags")
public class Tags extends PaginationCalculation<Tag> {

	private Log log = LogFactory.getLog(Tags.class);
	private String path = null;

	/**
	 * This method tags associated with the given resource
	 * 
	 * @param resourcePath
	 *            resource path
	 * @param start
	 *            starting page number
	 * @param size
	 *            number of tags to be fetched
	 * @param username
	 *            enduser's username
	 * @param tenantID
	 *            enduser's tenant ID
	 * @return JSON tagmodel eg: {"tags":[<array of tag names]}
	 */
	@GET
	@Produces("application/json")
	public Response getTagsOnAResource(@QueryParam("path") String resourcePath,
	                                   @QueryParam("start") int start,
	                                   @QueryParam("size") int size,
	                                   @QueryParam("user") String username) {

		if (username == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			String tenantID = super.getTenantID();
			super.createUserRegistry(username, tenantID);
		}
		if (RestPathPaginationValidation.validate(start, size) == -1) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if (super.getUserRegistry() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		boolean exist;
		path = resourcePath;
		try {
			if (resourcePath.length() == 0) {
				// retrieve all the tags
				return getAllTags();
			}
			// check resource exist
			exist = super.getUserRegistry().resourceExists(resourcePath);
			if (exist) {
				return displayPaginatedResult(start, size);
			} else {
				// resource does not found,HTTP 404
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user doesn't have permission to read the tag names for a given resource", e);
			// if user don't have permission to get tags
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

	}

	/**
	 * This method add array of tags to the specified resource
	 * 
	 * @param resourcePath
	 *            resource path
	 * @param tags
	 *            eg:{"tags":[<array of tag names>]}
	 * @param username
	 *            enduser's username
	 * @param tenantID
	 *            enduser's tenant ID
	 * @return HTTP 201 if success
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response setTagsOnAResource(@QueryParam("path") String resourcePath, TagModel tags,
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
		try {
			boolean exist = super.getUserRegistry().resourceExists(resourcePath);
			if (exist) {
				String[] tagsOnResource = tags.getTags();
				for (int i = 0; i < tagsOnResource.length; i++) {
					super.getUserRegistry().applyTag(resourcePath, tagsOnResource[i]);
				}
				return Response.status(Response.Status.CREATED).build();
			} else {
				// if resource does not exist, returns HTTP 404
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (RegistryException e) {
			log.error("user doesn't have permission to put the tags for the given resource", e);
			// if user does not have permission to access the resource
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	/**
	 * get all the tags in the registry space.
	 * 
	 */
	private Response getAllTags() {
		if (setTagSearchQuery()) {
			Collection collection;
			HashMap<String, String[]> tagCloud = new HashMap<String, String[]>();
			try {
				// execute the custom query
				collection =
				             super.getUserRegistry()
				                  .executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
				                                        RegistryConstants.QUERIES_COLLECTION_PATH +
				                                        "/tags",
				                                Collections.<String, String> emptyMap());
				// create the tagList, extract the tag name from collection and
				// add it to tagList if
				// does not exist already
				List<String> tagList = new ArrayList<String>();
				for (String fullTag : collection.getChildren()) {
					String tag = fullTag.split(";")[1].split(":")[1];
					if (!tagList.contains(tag)) {
						tagList.add(tag);
					}
				}
				int i = 0;
				// iterate through the list and create a String array.
				Iterator<String> itr = tagList.iterator();
				String[] allTags = new String[tagList.size()];
				while (itr.hasNext()) {
					allTags[i] = itr.next();
					i++;
				}
				tagCloud.put("tagCloud", allTags);
			} catch (RegistryException e) {
				log.error(e.getCause(), e);
				Response.status(Response.Status.UNAUTHORIZED).build();
			}
			return Response.ok(tagCloud).build();
		} else {
			log.warn("user is not authorized to access the resource");
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	/**
	 * custom query for all the tags search is set as a resource and saved at
	 * the config registry.space
	 */
	private boolean setTagSearchQuery() {
		if (log.isDebugEnabled()) {
			log.debug("tag search customs query is set");
		}
		String tagsQueryPath =
		                       RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
		                               RegistryConstants.QUERIES_COLLECTION_PATH + "/tags";
		try {
			if (!super.getUserRegistry().resourceExists(tagsQueryPath)) {
				// set-up query for tag-search.
				Resource resource = super.getUserRegistry().newResource();
				resource.setContent("SELECT RT.REG_TAG_ID FROM REG_RESOURCE_TAG RT ORDER BY "
				                    + "RT.REG_TAG_ID");
				resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
				resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
				                     RegistryConstants.TAGS_RESULT_TYPE);
				super.getUserRegistry().put(tagsQueryPath, resource);
			}
			return true;
		} catch (RegistryException e) {
			log.error(e.getCause(), e);
			return false;
		}
	}

	/**
	 * this method returns the array of Tag for the given resource.
	 */

	protected Tag[] getResult() {
		try {
			return super.getUserRegistry().getTags(path);
		} catch (RegistryException e) {
			log.error(e.getMessage());
		}
		return new Tag[0];
	}

	/**
	 * This method bind the array of paginated tag objects
	 */
	protected Response display(Tag[] tagArr, int begin, int end) {
		String[] tagNames = new String[end - begin + 1];
		for (int i = 0, j = begin; i < tagNames.length && j <= end; i++, j++) {
			tagNames[i] = tagArr[j].getTagName();
		}
		TagModel tagModel = new TagModel();
		tagModel.setTags(tagNames);
		return Response.ok(tagModel).build();
	}
}
