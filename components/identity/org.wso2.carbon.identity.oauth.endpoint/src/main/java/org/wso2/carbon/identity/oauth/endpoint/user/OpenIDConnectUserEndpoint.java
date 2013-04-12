/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth.endpoint.user;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.ui.OAuthClientException;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.user.api.Claim;

@Path("/userinfo")
public class OpenIDConnectUserEndpoint {

	private static Log log = LogFactory.getLog(OpenIDConnectUserEndpoint.class);

	@GET
	@Path("/")
	@Produces("application/json")
	public Response getUserClaims(@Context HttpHeaders headers, @QueryParam("schema") String schema) {

		if (!"openid".equals(schema)) {
			/*
			 * send error
			 * http://openid.net/specs/openid-connect-basic-1_0-22.html#anchor7
			 * http://tools.ietf.org/html/rfc6750#section-3.1
			 */
		}

		List authorizationHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeaders == null) {
			/*
			 * send error
			 * http://openid.net/specs/openid-connect-basic-1_0-22.html#anchor7
			 * http://tools.ietf.org/html/rfc6750#section-3.1
			 */
		}
		try {
			String[] headerInfo = ((String) authorizationHeaders.get(0)).trim().split(" ");
			if (!"Bearer".equals(headerInfo[0])) {
				/*
				 * send error
				 * http://openid.net/specs/openid-connect-basic-1_0-22.html#anchor7
				 * http://tools.ietf.org/html/rfc6750#section-3.1
				 */
			}
			Claim[] claims = new OAuth2UserClient().getUserClaims(headerInfo[1]);
			String response = getUserInfoResponseObject().getResponseString(claims);

			ResponseBuilder respBuilder =
			                              Response.status(HttpServletResponse.SC_OK)
			                                      .header(OAuthConstants.HTTP_RESP_HEADER_CACHE_CONTROL,
			                                              OAuthConstants.HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE)
			                                      .header(OAuthConstants.HTTP_RESP_HEADER_PRAGMA,
			                                              OAuthConstants.HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE);
			return respBuilder.entity(response).build();

		} catch (OAuthClientException e) {
			/*
			 * send error
			 * http://openid.net/specs/openid-connect-basic-1_0-22.html#anchor7
			 * http://tools.ietf.org/html/rfc6750#section-3.1
			 */
		}
		return null;
	}

	/**
	 * Get the correct type of response. Can be JSON or JWT. By default the type
	 * is JSON.
	 * http://openid.net/specs/openid-connect-basic-1_0-22.html#id_res
	 * 
	 * @return
	 */
	private UserInfoResponse getUserInfoResponseObject() {
		// TODO pic the type based on config
		return new UserInfoJSONResponse();
	}

}
