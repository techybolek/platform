/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth.ui.endpoints.authz;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.identity.oauth.ui.client.OAuth2AuthzClient;
import org.wso2.carbon.identity.oauth.ui.util.OAuthUIUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet handles the authentication endpoint 
 */
public class OAuth2AuthnEndpoint extends HttpServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2962823648421720546L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                                                                        IOException {
		OAuth2AuthzClient authzClient = new OAuth2AuthzClient();
		String redirectUrl;
		try {
			redirectUrl = authzClient.handleAuthorizationRequest(req, resp);
		} catch (OAuthSystemException e) {
			redirectUrl =
			              OAuthUIUtil.getErrorPageURL(req, null, "server_error",
			                                          "Error when completing the user authorization");
		}
		resp.sendRedirect(redirectUrl);
	}

}
