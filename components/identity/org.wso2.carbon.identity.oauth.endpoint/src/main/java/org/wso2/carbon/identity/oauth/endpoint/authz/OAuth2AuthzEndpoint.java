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
package org.wso2.carbon.identity.oauth.endpoint.authz;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.OIDC;
import org.apache.oltu.openidconnect.as.util.OIDCAuthzServerUtil;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.ui.OAuth2Parameters;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.identity.oauth.util.EndpointUtil;
import org.wso2.carbon.identity.oauth.util.OpenIDConnectConstant;
import org.wso2.carbon.identity.oauth.util.OpenIDConnectUserRPStore;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.util.CharacterEncoder;

@Path("/authorize")
public class OAuth2AuthzEndpoint {

    private static Log log = LogFactory.getLog(OAuth2AuthzEndpoint.class);

    @GET
    @Path("/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request, MultivaluedMap<String, String> paramMap) throws URISyntaxException {


            String clientId = CharacterEncoder.getSafeText(request.getParameter("client_id"));
            OAuth2Parameters oauth2Params = (OAuth2Parameters) request.getSession().getAttribute(OAuthConstants.OAUTH2_PARAMS);
            String consent = CharacterEncoder.getSafeText(request.getParameter("consent"));
		try {
			if (clientId != null) { // request from the client
				String redirectURL = CharacterEncoder.getSafeText(request.getParameter("redirect_uri"));
				try {
					redirectURL = handleOAuthAuthorizationRequest(request, clientId, redirectURL);
				} catch (OAuthProblemException e) {
					log.debug(e.getError(), e.getCause());
					redirectURL =
					              OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
					                             .location(redirectURL).buildQueryMessage().getLocationUri();
				}
				return Response.status(HttpServletResponse.SC_FOUND).location(new URI(redirectURL)).build();

			} else if (consent != null) { // request from the consent page
				String returnUrl = handleUserConsent(consent, request, oauth2Params);
				return Response.status(HttpServletResponse.SC_FOUND).location(new URI(returnUrl)).build();
				
			} else if (oauth2Params != null) { // request from the login page
					String redirectURL = handleOAuthRequestParams(request);
					return Response.status(HttpServletResponse.SC_FOUND).location(new URI(redirectURL)).build();

			} else {
				log.error("Invalid Authorization Request");
				return Response.status(HttpServletResponse.SC_FOUND)
				               .location(new URI(EndpointUtil.getErrorPageURL(request,
				                                                              null,
				                                                              OAuth2ErrorCodes.INVALID_REQUEST,
				                                                              "Invalid Authorization Request")))
				               .build();
			}
		} catch (OAuthSystemException e) {
			log.error(e.getMessage(), e);
			return Response.status(HttpServletResponse.SC_FOUND)
			               .location(new URI(EndpointUtil.getErrorPageURL(request, null,
			                                                              OAuth2ErrorCodes.INVALID_REQUEST,
			                                                              e.getMessage()))).build();
		}
    }

	/**
	 * 
	 * @param consent
	 * @param request
	 * @param oauth2Params
	 * @return
	 * @throws OAuthSystemException
	 */
	private String handleUserConsent(String consent, HttpServletRequest request, OAuth2Parameters oauth2Params)
	                                                                                                           throws OAuthSystemException {

		String returnUrl = (String) request.getSession().getAttribute(OpenIDConnectConstant.Session.OIDC_RESPONSE);
		String applicationName = (String) request.getSession().getAttribute(OpenIDConnectConstant.Session.OIDC_RP);
		String loggedInUser = (String) request.getSession().getAttribute(OpenIDConnectConstant.Session.OIDC_LOGGED_IN_USER);

		request.getSession().removeAttribute(OpenIDConnectConstant.Session.OIDC_RESPONSE);
		request.getSession().removeAttribute(OpenIDConnectConstant.Session.OIDC_RP);
		request.getSession().removeAttribute(OAuthConstants.OAUTH2_PARAMS);

		if (OpenIDConnectConstant.Consent.DENY.equals(consent)) {
			// return an error if user denied
			return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
			                      .setError(OAuth2ErrorCodes.ACCESS_DENIED)
			                      .location(oauth2Params.getRedirectURI()).setState(oauth2Params.getState())
			                      .buildQueryMessage().getLocationUri();
		}

		boolean approvedAlways = OpenIDConnectConstant.Consent.APPROVE_ALWAYS.equals(consent) ? true : false;
		OpenIDConnectUserRPStore.getInstance()
		                        .putUserRPToStore(loggedInUser, applicationName, approvedAlways);

		return returnUrl;
	}

	/**
	 * http://tools.ietf.org/html/rfc6749#section-4.1.2
	 * 
	 * 4.1.2.1. Error Response
	 * 
	 * If the request fails due to a missing, invalid, or mismatching
	 * redirection URI, or if the client identifier is missing or invalid,
	 * the authorization server SHOULD inform the resource owner of the
	 * error and MUST NOT automatically redirect the user-agent to the
	 * invalid redirection URI.
	 * 
	 * If the resource owner denies the access request or if the request
	 * fails for reasons other than a missing or invalid redirection URI,
	 * the authorization server informs the client by adding the following
	 * parameters to the query component of the redirection URI using the
	 * "application/x-www-form-urlencoded" format
	 * 
	 * @param req
	 * @param clientId 
	 * @param callbackURL 
	 * @return
	 * @throws OAuthSystemException
	 * @throws OAuthProblemException
	 */
	private String handleOAuthAuthorizationRequest(HttpServletRequest req, String clientId, String callbackURL) throws OAuthSystemException,
	                                                                      OAuthProblemException {
		OAuth2ClientValidationResponseDTO clientDTO = null;
		if (clientId != null) {
			clientDTO = validateClient(req, clientId, callbackURL);
		} else {
			log.warn("Client Id is not present in the authorization request.");
			return EndpointUtil.getErrorPageURL(req, clientDTO, OAuth2ErrorCodes.INVALID_REQUEST,
			                                    "Invalid Request. Client Id is not present in the request");
		}
		if (!clientDTO.isValidClient()) {
			return EndpointUtil.getErrorPageURL(req, clientDTO, clientDTO.getErrorCode(),
			                                    clientDTO.getErrorMsg());
		}

		// Now the client is valid, redirect him to the authorization page.
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
			req.getSession().setAttribute(OpenIDConnectConstant.Session.OIDC_REQUEST, "true");
			req.getSession().setAttribute(OpenIDConnectConstant.Session.OIDC_RP, params.getApplicationName());

			params.setNonce(oauthRequest.getParam(OIDC.AuthZRequest.NONCE));
			params.setDisplay(oauthRequest.getParam(OIDC.AuthZRequest.DISPLAY));
			params.setRequest(oauthRequest.getParam(OIDC.AuthZRequest.REQUEST));
			params.setRequestURI(oauthRequest.getParam(OIDC.AuthZRequest.REQUEST_URI));
			params.setIDTokenHint(oauthRequest.getParam(OIDC.AuthZRequest.ID_TOKEN_HINT));
			params.setLoginHint(oauthRequest.getParam(OIDC.AuthZRequest.LOGIN_HINT));
			String prompt = oauthRequest.getParam(OIDC.AuthZRequest.PROMPT);
			params.setPrompt(prompt);

			/**
			 * The prompt parameter can be used by the Client to make sure
			 * that the End-User is still present for the current session or
			 * to bring attention to the request. If this parameter contains
			 * none with any other value, an error is returned
			 * 
			 * http://openid.net/specs/openid-connect-messages-
			 * 1_0-14.html#anchor6
			 * 
			 * prompt : none
			 * The Authorization Server MUST NOT display any authentication or
			 * consent user interface pages. An error is returned if the
			 * End-User is not already authenticated or the Client does not have
			 * pre-configured consent for the requested scopes. This can be used
			 * as a method to check for existing authentication and/or consent.
			 * 
			 * prompt : login
			 * The Authorization Server MUST prompt the End-User for
			 * reauthentication.
			 * 
			 * Error : login_required
			 * The Authorization Server requires End-User authentication. This
			 * error MAY be returned when the prompt parameter in the
			 * Authorization Request is set to none to request that the
			 * Authorization Server should not display any user interfaces to
			 * the End-User, but the Authorization Request cannot be completed
			 * without displaying a user interface for user authentication.
			 * 
			 */
			if (prompt != null) {
				// values {none, login, consent, select_profile}
				String[] prompts = prompt.trim().split(" ");
				if (prompts.length < 1) {
					String error = "Invalid prompt variable value. ";
					log.debug(error + prompt);
					return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
					                      .setError(OAuth2ErrorCodes.INVALID_REQUEST)
					                      .setErrorDescription(error).location(params.getRedirectURI())
					                      .setState(params.getState()).buildQueryMessage().getLocationUri();
				}
				boolean contains_none = prompt.contains(OIDC.Prompt.NONE);
				if (prompts.length > 1 && contains_none) { 
					String error = "Invalid prompt variable combination. The value none cannot be used with others. ";
					log.debug(error + prompt);
					return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
					                      .setError(OAuth2ErrorCodes.INVALID_REQUEST)
					                      .setErrorDescription(error).location(params.getRedirectURI())
					                      .setState(params.getState()).buildQueryMessage().getLocationUri();
				}
				Object logedInUser = req.getSession()
				                        .getAttribute(OpenIDConnectConstant.Session.OIDC_LOGGED_IN_USER);
				
				if (contains_none && logedInUser == null) {
					String error = "Received prompt none but no authenticated user found. ";
					log.debug(error + prompt);
					return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
					                      .setError(OIDC.Error.LOGIN_REQUIRED)
					                      .setErrorDescription(error).location(params.getRedirectURI())
					                      .setState(params.getState()).buildQueryMessage().getLocationUri();
				}
				if (!prompt.contains(OIDC.Prompt.LOGIN)) {
					req.getSession().setAttribute(OAuthConstants.OAUTH2_PARAMS, params);
					return CarbonUIUtil.getAdminConsoleURL("/") + "../oauth2endpoints/authorize";
				}
			}
		}

		req.getSession().setAttribute(OAuthConstants.OAUTH2_PARAMS, params);
		return EndpointUtil.getLoginPageURL(req, clientDTO, params);
	}

	/**
	 * Validates the client using the oauth2 service
	 * 
	 * @param req
	 * @param clientId
	 * @param callbackURL
	 * @return
	 */
	private OAuth2ClientValidationResponseDTO validateClient(HttpServletRequest req, String clientId,
	                                                         String callbackURL) {
		return EndpointUtil.getOAuth2Service().validateClientInfo(clientId, callbackURL);
	}

    public String handleOAuthRequestParams(HttpServletRequest request) throws OAuthSystemException {

		OAuth2Parameters oauth2Params = (OAuth2Parameters) request.getSession()
		                                                          .getAttribute(OAuthConstants.OAUTH2_PARAMS);

		// user has denied the authorization. Send back the error code.
		if ("true".equals(request.getParameter("deny"))) {
			return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
			                      .setError(OAuth2ErrorCodes.ACCESS_DENIED)
			                      .location(oauth2Params.getRedirectURI()).setState(oauth2Params.getState())
			                      .buildQueryMessage().getLocationUri();
		}

        OAuth2AuthorizeRespDTO authzRespDTO = authorize(request, oauth2Params);
        // Authentication Failure, send back to the login page
        if (!authzRespDTO.isAuthenticated()) {
            String loginPageInSession = (String) request.getSession().getAttribute("loginPage");
            return loginPageInSession+"&auth_status=failed";
        }
		OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
		                                                            OAuthASResponse.authorizationResponse(request,
		                                                                                                  HttpServletResponse.SC_FOUND);
        OAuthResponse oauthResponse;
        // user is authorized.
        if (authzRespDTO.isAuthorized()) {
            if (ResponseType.CODE.toString().equals(oauth2Params.getResponseType())) {
                builder.setCode(authzRespDTO.getAuthorizationCode());
            } else if (ResponseType.TOKEN.toString().equals(oauth2Params.getResponseType())) {
                builder.setAccessToken(authzRespDTO.getAccessToken());
                builder.setExpiresIn(String.valueOf(60 * 60));
            }
            builder.setParam("state", oauth2Params.getState());
            String redirectURL = authzRespDTO.getCallbackURI();
            oauthResponse = builder.location(redirectURL).buildQueryMessage();

        } else {
			OAuthProblemException oauthException =
			                                       OAuthProblemException.error(authzRespDTO.getErrorCode(),
			                                                                   authzRespDTO.getErrorMsg());
			oauthResponse =
			                OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(oauthException)
			                               .location(authzRespDTO.getCallbackURI())
			                               .setState(oauth2Params.getState()).buildQueryMessage();
        }
        return handleOpenIDConnectParams(request, oauthResponse.getLocationUri(), oauth2Params);
    }

	/**
	 * prompt : none
	 * The Authorization Server MUST NOT display any authentication
	 * or consent user interface pages. An error is returned if the
	 * End-User is not already authenticated or the Client does not
	 * have pre-configured consent for the requested scopes. This
	 * can be used as a method to check for existing authentication
	 * and/or consent.
	 * 
	 * prompt : consent
	 * The Authorization Server MUST prompt the End-User for consent before
	 * returning information to the Client.
	 * 
	 * prompt Error : consent_required
	 * The Authorization Server requires End-User consent. This
	 * error MAY be returned when the prompt parameter in the
	 * Authorization Request is set to none to request that the
	 * Authorization Server should not display any user
	 * interfaces to the End-User, but the Authorization Request
	 * cannot be completed without displaying a user interface
	 * for End-User consent.
	 * 
	 * @param request
	 * @param redirectUrl
	 * @param oauth2Params
	 * @return
	 * @throws OAuthSystemException
	 */
	private String handleOpenIDConnectParams(HttpServletRequest request, String redirectUrl,
	                                         OAuth2Parameters oauth2Params) throws OAuthSystemException {

		if ("true".equals(request.getSession().getAttribute(OpenIDConnectConstant.Session.OIDC_REQUEST))) {
			request.getSession().removeAttribute(OpenIDConnectConstant.Session.OIDC_REQUEST);
			// store the logged in user in session to support prompt = none
			String loggedInUser = request.getParameter(OAuthConstants.REQ_PARAM_OAUTH_USER_NAME);
			if (loggedInUser != null) {
				request.getSession().setAttribute(OpenIDConnectConstant.Session.OIDC_LOGGED_IN_USER,
				                                  loggedInUser);
			} else {
				loggedInUser = (String) request.getSession()
				                               .getAttribute(OpenIDConnectConstant.Session.OIDC_LOGGED_IN_USER);
			}
			if (oauth2Params.getPrompt() == null || oauth2Params.getPrompt().contains(OIDC.Prompt.CONSENT)) {
				request.getSession().setAttribute(OpenIDConnectConstant.Session.OIDC_RESPONSE, redirectUrl);
				return EndpointUtil.getUserConsentURL(oauth2Params, loggedInUser);
				
			} else if (oauth2Params.getPrompt().contains(OIDC.Prompt.NONE)) {
				// load the users approved applications
				String appName = oauth2Params.getApplicationName();
				boolean skipConsent = EndpointUtil.getOAuthServerConfiguration()
				                                  .getOpenIDConnectSkipeUserConsentConfig();
				boolean hasUserApproved = true;
				if (!skipConsent) {
					hasUserApproved = OpenIDConnectUserRPStore.getInstance().hasUserApproved(loggedInUser,
					                                                                         appName);
				} 
				if (hasUserApproved) {
					return redirectUrl; // should not prompt for consent
				} else {
					// returning error
					return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
					                      .setError(OIDC.Error.CONSENT_REQUIRED)
					                      .location(oauth2Params.getRedirectURI())
					                      .setState(oauth2Params.getState()).buildQueryMessage()
					                      .getLocationUri();
				}
			}
		} else {
			request.getSession().removeAttribute(OAuthConstants.OAUTH2_PARAMS);
		}
		return redirectUrl;
	}

	/**
	 * 
	 * @param req
	 * @param oauth2Params
	 * @return
	 */
    private OAuth2AuthorizeRespDTO authorize(HttpServletRequest req, OAuth2Parameters oauth2Params) {
        // authenticate and issue the authorization code
        OAuth2AuthorizeReqDTO authzReqDTO = new OAuth2AuthorizeReqDTO();
        authzReqDTO.setCallbackUrl(oauth2Params.getRedirectURI());
        authzReqDTO.setConsumerKey(oauth2Params.getClientId());
        authzReqDTO.setResponseType(oauth2Params.getResponseType());
        authzReqDTO.setScopes(oauth2Params.getScopes().toArray(new String[oauth2Params.getScopes().size()]));
        String username = req.getParameter(OAuthConstants.REQ_PARAM_OAUTH_USER_NAME);
        if (username == null) {
            username = (String) req.getSession().getAttribute(OpenIDConnectConstant.Session.OIDC_LOGGED_IN_USER);
        }
        authzReqDTO.setUsername(username);
        authzReqDTO.setPassword(req.getParameter(OAuthConstants.REQ_PARAM_OAUTH_USER_PASSWORD));
        return EndpointUtil.getOAuth2Service().authorize(authzReqDTO);
    }
}
