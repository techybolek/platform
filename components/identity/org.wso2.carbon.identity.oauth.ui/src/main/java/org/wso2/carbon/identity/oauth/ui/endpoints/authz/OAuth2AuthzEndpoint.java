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

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.OIDC;
import org.apache.oltu.openidconnect.as.util.OIDCAuthzServerUtil;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.ui.OAuth2Parameters;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.identity.oauth.ui.client.OAuth2ServiceClient;
import org.wso2.carbon.identity.oauth.ui.internal.OAuthUIServiceComponentHolder;
import org.wso2.carbon.identity.oauth.ui.util.OAuthUIUtil;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.util.CharacterEncoder;

/**
 * This servlet handles the authorization endpoint and token endpoint.
 */
public class OAuth2AuthzEndpoint extends HttpServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2962823648421720546L;
	private static final Log log = LogFactory.getLog(OAuth2AuthzEndpoint.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
			// requests coming for authorization.
			if (req.getRequestURI().endsWith("authorize")) {
				String redirectURL = handleOAuthAuthorizationRequest(req);
				resp.sendRedirect(redirectURL);
			} else {
				String errorPageURL =
				                      OAuthUIUtil.getErrorPageURL(req, null,
				                                                  OAuth2ErrorCodes.INVALID_OAUTH_URL,
				                                                  "Invalid OAuth request URL.");
				resp.sendRedirect(errorPageURL);
			}
        } catch (OAuthSystemException e) {
            log.error("Error when processing the authorization request.", e);
            HttpSession session = req.getSession();
            session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE, OAuth2ErrorCodes.SERVER_ERROR);
            session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE, "Error when processing the authorization request.");
            String errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth-error.jsp";
            errorPageURL = errorPageURL.replace("/oauth2/authorize", "");
            resp.sendRedirect(errorPageURL);
        }
    }

    private String handleOAuthAuthorizationRequest(HttpServletRequest req) throws IOException, OAuthSystemException {
        OAuth2ClientValidationResponseDTO clientDTO = null;
        try {
            // Extract the client_id and callback url from the request, because constructing an Amber
            // Authz request can cause an OAuthProblemException exception. In that case, that error
            // needs to be passed back to client. Before that we need to validate the client_id and callback URL
            String clientId = CharacterEncoder.getSafeText(req.getParameter("client_id"));
            String callbackURL = CharacterEncoder.getSafeText(req.getParameter("redirect_uri"));

            if (clientId != null) {
                clientDTO = validateClient(req, clientId, callbackURL);
            } else { // Client Id is not present in the request.
				log.warn("Client Id is not present in the authorization request.");
				return OAuthUIUtil.getErrorPageURL(req, clientDTO, OAuth2ErrorCodes.INVALID_REQUEST,
				                                   "Invalid Request. Client Id is not present in the request");
            }
			// Client is not valid. Do not send this error back to client, send
			// to an error page instead.
			if (!clientDTO.getValidClient()) {
				return OAuthUIUtil.getErrorPageURL(req, clientDTO, clientDTO.getErrorCode(),
				                                   clientDTO.getErrorMsg());
			}

            // Now the client is valid, redirect him for authorization page.
            OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(req);
            OAuth2Parameters params = new OAuth2Parameters();
            params.setApplicationName(clientDTO.getApplicationName());
            params.setRedirectURI(clientDTO.getCallbackURL());
            params.setResponseType(oauthRequest.getResponseType());
            params.setScopes(oauthRequest.getScopes());
            params.setState(oauthRequest.getState());
            params.setClientId(clientId);
            
			// OpenID Connect request parameters
			if (OIDCAuthzServerUtil.isOIDCAuthzRequest(oauthRequest.getScopes())) {
				// these parameters must be processed in the complete implementation
				params.setNonce(oauthRequest.getParam(OIDC.AuthZRequest.NONCE));
				params.setDisplay(oauthRequest.getParam(OIDC.AuthZRequest.DISPLAY));
				params.setRequest(oauthRequest.getParam(OIDC.AuthZRequest.REQUEST));
				params.setRequestURI(oauthRequest.getParam(OIDC.AuthZRequest.REQUEST_URI));
				params.setIDTokenHint(oauthRequest.getParam(OIDC.AuthZRequest.ID_TOKEN_HINT));
				params.setLoginHint(oauthRequest.getParam(OIDC.AuthZRequest.LOGIN_HINT));
				String prompt = oauthRequest.getParam(OIDC.AuthZRequest.PROMPT);
				params.setPrompt(prompt);
				req.getSession().setAttribute(OAuthConstants.OIDCSessionConstant.OIDC_REQUEST, "true");
				req.getSession().setAttribute(OAuthConstants.OIDCSessionConstant.OIDC_RP, params.getApplicationName());
				if(prompt != null) { // processing prompt
					// prompt can be four values {none, login, consent, select_profile}
					String[] prompts = prompt.trim().split(" ");
					boolean contains_none = prompt.contains("none");
					if (prompts.length > 1 && contains_none) { // invalid combination
						log.error("Invalid prompt variable combination. " + prompt);
						OAuthUIUtil.getErrorPageURL(req, clientDTO, OAuth2ErrorCodes.INVALID_REQUEST,
						                            "Invalid prompt combination. The valune none cannot be used with others");
					}
					Object logedInUser = req.getSession().getAttribute(OAuthConstants.OIDCSessionConstant.OIDC_LOGGED_IN_USER);
					if (contains_none && logedInUser == null) {
						log.error("User not authenticated. " + prompt);
						OAuthUIUtil.getErrorPageURL(req, clientDTO, OAuthConstants.OAUTH_ERROR_CODE,
						                            "Received prompt none but no authenticated user found");
					}
					if(!prompt.contains("login")) { // we should not log the user
						req.getSession().setAttribute(OAuthConstants.OAUTH2_PARAMS, params);
			            String loginPage = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth2-authn-finish.jsp";
			            loginPage = loginPage.replace("/oauth2/authorize", "");
			            return loginPage;
					}
				}
			}
            req.getSession().setAttribute(OAuthConstants.OAUTH2_PARAMS, params);
            return OAuthUIUtil.getLoginPageURL(req, clientDTO, params);

        } catch (OAuthProblemException e) {
			log.error(e.getError(), e.getCause());
			return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
			                      .location(clientDTO.getCallbackURL()).buildQueryMessage().getLocationUri();
        }
    }

	private OAuth2ClientValidationResponseDTO validateClient(HttpServletRequest req, String clientId,
	                                                         String callbackURL) throws OAuthSystemException {

		String backendServerURL =
		                          CarbonUIUtil.getServerURL(OAuthUIServiceComponentHolder.getInstance()
		                                                                                 .getServerConfigurationService());
		ConfigurationContext configContext =
		                                     OAuthUIServiceComponentHolder.getInstance()
		                                                                  .getConfigurationContextService()
		                                                                  .getServerConfigContext();

		try {
			OAuth2ServiceClient oauth2ServiceClient =
			                                          new OAuth2ServiceClient(backendServerURL, configContext);
			return oauth2ServiceClient.validateClient(clientId, callbackURL);
		} catch (RemoteException e) {
			log.error("Error when invoking the OAuth2Service for client validation.");
			throw new OAuthSystemException(e.getMessage(), e);
		}
	}

}
