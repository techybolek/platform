/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.sso.saml.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.SAMLSSOService;
import org.wso2.carbon.identity.sso.saml.common.SAMLSSOProviderConstants;
import org.wso2.carbon.identity.sso.saml.common.Util;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSORespDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOSessionDTO;
import org.wso2.carbon.identity.sso.saml.logout.LogoutRequestSender;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.util.CharacterEncoder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This is the entry point for authentication process in an SSO scenario. This servlet is registered
 * with the URL pattern /samlsso and act as the control servlet. The message flow of an SSO scenario
 * is as follows.
 * <ol>
 * <li>SP sends a SAML Request via HTTP POST to the https://<ip>:<port>/samlsso endpoint.</li>
 * <li>IdP validates the SAML Request and checks whether this user is already authenticated.</li>
 * <li>If the user is authenticated, it will generate a SAML Response and send it back the SP via
 * the samlsso_redirect_ajaxprocessor.jsp.</li>
 * <li>If the user is not authenticated, it will send him to the login page and prompts user to
 * enter his credentials.</li>
 * <li>If these credentials are valid, then the user will be redirected back the SP with a valid
 * SAML Assertion. If not, he will be prompted again for credentials.</li>
 * </ol>
 */
public class SAMLSSOProviderServlet extends HttpServlet {

	private static final long serialVersionUID = -5182312441482721905L;
	private static Log log = LogFactory.getLog(SAMLSSOProviderServlet.class);

	private SAMLSSOService samlSsoService = new SAMLSSOService();

	@Override
	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException, IOException {
		doPost(httpServletRequest, httpServletResponse);
	}

	@Override
	/**
	 * The main logic is handled in the this doPost method. If the request does not contain 
	 * username password, then it means this is a request from a Service Provider with a 
	 * SAMLRequest. In case of an authentication request in that case we need to check if the 
	 * user already has a session. If there is no session found the user will be redirected to
	 * the authentication page, from the authentication page use will be again redirected back
	 * to this Servlet. Then the after successful authentication user will be redirected back 
	 * the service provider. In case of logout requests, the IDP will send logout requests
	 * to the other session participants and then send the logout response back to the initiator.  
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String username =
		                  CharacterEncoder.getSafeText(req.getParameter(SAMLSSOProviderConstants.USERNAME));
		String password = req.getParameter(SAMLSSOProviderConstants.PASSWORD);
		
		String federatedIdp = req.getParameter(SAMLSSOProviderConstants.FEDERATED_IDP);
		if (federatedIdp == null) {
			federatedIdp = req.getHeader(SAMLSSOProviderConstants.FEDERATED_IDP);
		}
		federatedIdp = org.wso2.carbon.identity.authenticator.saml2.sso.common.Util.getIdentityProviderSSOServiceURL(federatedIdp);
		
		HttpSession session = req.getSession();
		String ssoTokenID = session.getId();
		Cookie tokenCookie = getSSOTokenCookie(req);
		if (tokenCookie != null) {
			ssoTokenID = tokenCookie.getValue();
		}
		
		try {
			if (federatedIdp != null) {
				handleFederatedLogin(req, resp);
			} else if (username == null && password == null) {// SAMLRequest received.
				// if an openid authentication or password authentication
				String authMode = req.getParameter("authMode");
				if (!SAMLSSOProviderConstants.AuthnModes.OPENID.equals(authMode)) {
					authMode = SAMLSSOProviderConstants.AuthnModes.USERNAME_PASSWORD;
				}
				String relayState = req.getParameter(SAMLSSOProviderConstants.RELAY_STATE);
				/*if (relayState == null) {
					log.warn("RelayState is not present in the request.");
				}*/
				String samlRequest = req.getParameter("SAMLRequest");
				if (samlRequest != null) {
					handleSAMLRequest(req, resp, ssoTokenID, samlRequest, relayState, authMode);
				} else {
					log.debug("Invalid request message or single logout message " + samlRequest);
					ssoTokenID = req.getSession().getId();
					tokenCookie = getSSOTokenCookie(req);
					if (tokenCookie != null) {
						ssoTokenID = tokenCookie.getValue();
					}
					// Instantiate the service client.
					// Non-SAML request are assumed to be logout requests
					if (ssoTokenID!=null){
						samlSsoService.doSingleLogout(ssoTokenID);
					}
					sendNotification(SAMLSSOProviderConstants.Notification.INVALID_MESSAGE_STATUS,
							SAMLSSOProviderConstants.Notification.INVALID_MESSAGE_MESSAGE, req,
							resp);
					return;
				}
			} else {
				handleRequestFromLoginPage(req, resp, ssoTokenID, 
				                           req.getParameter(SAMLSSOProviderConstants.SESSION_DATA_KEY));
			}
		} catch (IdentityException e) {
			log.error("Error when processing the authentication request!", e);
			sendNotification(SAMLSSOProviderConstants.Notification.EXCEPTION_STATUS,
					SAMLSSOProviderConstants.Notification.EXCEPTION_MESSAGE, req, resp);
		}
	}

	/**
	 * Federated IDP scenario. This will redirect the user to the IDP of the users domain in the
	 * federation.
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleFederatedLogin(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	}

	/**
	 * Prompts user a notification with the status and message
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void sendNotification(String status, String message, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String redirectURL = CarbonUIUtil.getAdminConsoleURL(req);
		redirectURL = redirectURL.replace("samlsso/carbon/",
		                                  "authenticationendpoint/samlsso/notification_ajaxprocessor.jsp");
		//TODO Send status codes rather than full messages in the GET request
		String queryParams = "?" + SAMLSSOProviderConstants.STATUS + "=" + status + "&" +
		                             SAMLSSOProviderConstants.STATUS_MSG + "=" + message;
		resp.sendRedirect(redirectURL + queryParams);
	}

	/**
	 * If the SAMlRequest is a Logout request then IDP will send logout requests to other session
	 * participants and then sends the logout Response back to the initiator. In case of
	 * authentication request, check if there is a valid session for the user, if there is, the user
	 * will be redirected directly to the Service Provider, if not the user will be redirected to
	 * the login page.
	 * 
	 * @param req
	 * @param resp
	 * @param ssoTokenID
	 * @param samlRequest
	 * @param relayState
	 * @param authMode
	 * @throws IdentityException
	 * @throws IOException
	 * @throws ServletException
	 * @throws org.wso2.carbon.identity.base.IdentityException
	 */
	private void handleSAMLRequest(HttpServletRequest req, HttpServletResponse resp,
			String ssoTokenID, String samlRequest, String relayState, String authMode)
			throws IdentityException, IOException, ServletException {
		String queryString = req.getQueryString();
		if (log.isDebugEnabled()) {
			log.debug("Query string : " + queryString);
		}
		String rpSessionId = req.getParameter(MultitenantConstants.SSO_AUTH_SESSION_ID);
		SAMLSSOService client = getSAMLSSOServiceClient(req);
		SAMLSSOReqValidationResponseDTO signInRespDTO = client.validateRequest(samlRequest,
				queryString, ssoTokenID, rpSessionId, authMode);
		if (!signInRespDTO.isLogOutReq()) { // an <AuthnRequest> received
			if (signInRespDTO.isValid() && signInRespDTO.getResponse() != null && !signInRespDTO.isPassive()) {
                // user already has an existing SSO session, redirect
                if (SAMLSSOProviderConstants.AuthnModes.OPENID.equals(authMode)) {

                    storeSSOTokenCookie(ssoTokenID, req, resp, client.getSSOSessionTimeout());
                }
                if(client.isSAMLSSOLoginAccepted()){
                    req.getSession().setAttribute("authenticatedOpenID",Util.getOpenID(signInRespDTO.getSubject()));
                    req.getSession().setAttribute("openId",Util.getOpenID(signInRespDTO.getSubject()));
                }
                sendResponse(req, resp, relayState, signInRespDTO.getResponse(),
                        signInRespDTO.getAssertionConsumerURL(), signInRespDTO.getSubject());
            } else if (signInRespDTO.isValid() && samlSsoService.isOpenIDLoginAccepted() &&
                    req.getSession().getAttribute("authenticatedOpenID") != null){
                handleRequestWithOpenIDLogin(req,resp,signInRespDTO,relayState,ssoTokenID);
            } else if(signInRespDTO.isValid() && signInRespDTO.getResponse() != null && signInRespDTO.isPassive()){
                sendResponse(req, resp, relayState, signInRespDTO.getResponse(),
            	signInRespDTO.getAssertionConsumerURL(), signInRespDTO.getSubject());
            } else if (signInRespDTO.isValid() && signInRespDTO.getResponse() == null && !signInRespDTO.isPassive()) {
				// user doesn't have an existing SSO session, so authenticate
				sendToAuthenticate(req, resp, signInRespDTO, relayState);
            } else {
                log.debug("Invalid SAML SSO Request");
                throw new IdentityException("Invalid SAML SSO Request");
            }
        } else { // a <LogoutRequest> received
			// sending LogoutRequests to other session participants
			LogoutRequestSender.getInstance().sendLogoutRequests(signInRespDTO.getLogoutRespDTO());
            if(client.isSAMLSSOLoginAccepted()){
                req.getSession().removeAttribute("authenticatedOpenID");
                req.getSession().removeAttribute("openId");
            }
			// sending LogoutResponse back to the initiator
			sendResponse(req, resp, relayState, signInRespDTO.getLogoutResponse(),
					signInRespDTO.getAssertionConsumerURL(), signInRespDTO.getSubject());

		}
	}

	/**
	 * Returns the service client. First if there is a client already in this session, it will be
	 * returned, otherwise a new client will be created, added to the session and returned.
	 * 
	 * @param req
	 * @return
	 * @throws AxisFault
	 */
	private SAMLSSOService getSAMLSSOServiceClient(HttpServletRequest req) throws AxisFault {
		return samlSsoService;
	}

	/**
	 * Sends the user for authentication to the login page
	 * 
	 * @param req
	 * @param resp
	 * @param signInRespDTO
	 * @param relayState
	 * @throws ServletException
	 * @throws IOException
	 */
	private void sendToAuthenticate(HttpServletRequest req, HttpServletResponse resp,
			SAMLSSOReqValidationResponseDTO signInRespDTO, String relayState)
			throws ServletException, IOException {

		SAMLSSOSessionDTO sessionDTO = new SAMLSSOSessionDTO();
		sessionDTO.setHttpQueryString(req.getQueryString());
		sessionDTO.setDestination(signInRespDTO.getDestination());
		sessionDTO.setRelayState(relayState);
		sessionDTO.setRequestMessageString(signInRespDTO.getRequestMessageString());
		sessionDTO.setIssuer(signInRespDTO.getIssuer());
		sessionDTO.setRequestID(signInRespDTO.getId());
		sessionDTO.setSubject(signInRespDTO.getSubject());
		sessionDTO.setRelyingPartySessionId(signInRespDTO.getRpSessionId());
		sessionDTO.setAssertionConsumerURL(signInRespDTO.getAssertionConsumerURL());
		
		String sessionDataKey = UUIDGenerator.generateUUID();
		HttpSession session = req.getSession();
		session.setAttribute(sessionDataKey, sessionDTO);
		
		String redirectURL = CarbonUIUtil.getAdminConsoleURL(req);
		redirectURL = redirectURL.replace("samlsso/carbon/",
		                                  getLoginPage(signInRespDTO.getLoginPageURL()));
		
		resp.sendRedirect(redirectURL + "?" + SAMLSSOProviderConstants.SESSION_DATA_KEY + "=" + sessionDataKey);
	}

	/**
	 * Sends the Response message back to the Service Provider.
	 * 
	 * @param req
	 * @param resp
	 * @param signInRespDTO
	 * @param relayState
	 * @throws ServletException
	 * @throws IOException
	 */
	private void sendResponse(HttpServletRequest req, HttpServletResponse resp, String relayState,
			String response, String acUrl, String subject) throws ServletException, IOException {
		
		HttpSession session = req.getSession();
		session.removeAttribute(SAMLSSOProviderConstants.SESSION_DATA_KEY);
		
		if(relayState != null){
		    relayState = URLDecoder.decode(relayState, "UTF-8");
		    relayState = relayState.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").
		            replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace("\n", "");
		}
		
		acUrl = getACSUrlWithTenantPartitioning(acUrl, subject);
		
		PrintWriter out = resp.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<p>You are now redirected back to " + acUrl);
		out.println(" If the redirection fails, please click the post button.</p>");
		out.println("<form method='post' action='" + acUrl + "'>");
		out.println("<p>");
		out.println("<input type='hidden' name='SAMLResponse' value='" + response + "'>");
		out.println("<input type='hidden' name='RelayState' value='" + relayState + "'>");
		out.println("<button type='submit'>POST</button>");
		out.println("</p>");
		out.println("</form>");
		out.println("<script type='text/javascript'>");
		out.println("document.forms[0].submit();");
		out.println("</script>");
		out.println("</body>");
		out.println("</html>");	
	}

	/**
	 * This method handles authentication and sends authentication Response message back to the
	 * Service Provider after successful authentication. In case of authentication failure the user
	 * is prompted back for authentication.
	 * 
	 * @param req
	 * @param resp
	 * @param ssoTokenID
	 * @throws IdentityException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void handleRequestFromLoginPage(HttpServletRequest req, HttpServletResponse resp,
			String ssoTokenID, String sessionDataKey) throws IdentityException, IOException, ServletException {

		String relayState = req.getParameter(SAMLSSOProviderConstants.RELAY_STATE);
		SAMLSSOAuthnReqDTO authnReqDTO = new SAMLSSOAuthnReqDTO();
		populateAuthnReqDTO(req, authnReqDTO, sessionDataKey);
		SAMLSSOService ssoServiceClient = getSAMLSSOServiceClient(req);
        SAMLSSORespDTO authRespDTO = null;
        if(ssoServiceClient.isOpenIDLoginAccepted() && req.getSession().getAttribute("authenticatedOpenID") != null){
            authnReqDTO.setUsername(Util.getUserNameFromOpenID(
                    (String)req.getSession().getAttribute("authenticatedOpenID")));
            authRespDTO = ssoServiceClient.authenticate(authnReqDTO, ssoTokenID, true, SAMLSSOConstants.AuthnModes.OPENID);
        } else {
            authRespDTO = ssoServiceClient.authenticate(authnReqDTO, ssoTokenID, false, SAMLSSOConstants.AuthnModes.USERNAME_PASSWORD);
        }

		if (authRespDTO.isSessionEstablished()) { // authenticated
            if(req.getParameter("chkRemember") != null && req.getParameter("chkRemember").equals("on")){
                storeSSOTokenCookie(ssoTokenID, req, resp, ssoServiceClient.getSSOSessionTimeout());
            }
            if(ssoServiceClient.isSAMLSSOLoginAccepted()){
                req.getSession().setAttribute("authenticatedOpenID",Util.getOpenID(authRespDTO.getSubject()));
                req.getSession().setAttribute("openId",Util.getOpenID(authRespDTO.getSubject()));
            }
            sendResponse(req, resp, relayState, authRespDTO.getRespString(),
					authRespDTO.getAssertionConsumerURL(), authRespDTO.getSubject());
		} else { // authentication FAILURE
			// send back to the login.page for the next authentication attempt.
			sendToReAuthenticate(req, resp, authRespDTO);
		}
	}
	
	/**
	 * 
	 * @param req
	 * @param authnReqDTO
	 */
	private void populateAuthnReqDTO(HttpServletRequest req, SAMLSSOAuthnReqDTO authnReqDTO, 
	                                 String sessionDataKey) {
		HttpSession session = req.getSession();
		SAMLSSOSessionDTO sessionDTO = (SAMLSSOSessionDTO)session.getAttribute(sessionDataKey);
		
		authnReqDTO.setAssertionConsumerURL(sessionDTO.getAssertionConsumerURL());
		authnReqDTO.setId(sessionDTO.getRequestID());
		authnReqDTO.setIssuer(sessionDTO.getIssuer());
		authnReqDTO.setSubject(sessionDTO.getSubject());
		authnReqDTO.setRpSessionId(sessionDTO.getRelyingPartySessionId());
		authnReqDTO.setRequestMessageString(sessionDTO.getRequestMessageString());
		authnReqDTO.setQueryString(sessionDTO.getHttpQueryString());
		authnReqDTO.setDestination(sessionDTO.getDestination());
		
		authnReqDTO.setUsername(getRequestParameter(req, SAMLSSOProviderConstants.USERNAME));
		authnReqDTO.setPassword(getRequestParameter(req, SAMLSSOProviderConstants.PASSWORD));
	}

	/**
	 * 
	 * @param req
	 * @return
	 */
	private Cookie getSSOTokenCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(SAMLSSOProviderConstants.SSO_TOKEN_ID)) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param ssoTokenID
	 * @param req
	 * @param resp
	 */
	private void storeSSOTokenCookie(String ssoTokenID, HttpServletRequest req, HttpServletResponse resp,
                                     int sessionTimeout) {
		Cookie ssoTokenCookie = getSSOTokenCookie(req);
		if (ssoTokenCookie == null) {
			ssoTokenCookie = new Cookie(SAMLSSOProviderConstants.SSO_TOKEN_ID, ssoTokenID);
		}
		ssoTokenCookie.setMaxAge(sessionTimeout);
		resp.addCookie(ssoTokenCookie);
	}

	/**
	 * 
	 * @param customLoginPage
	 * @return
	 */
	private String getLoginPage(String customLoginPage) {
		if (customLoginPage != null && customLoginPage.length() != 0) {
			return "/carbon/" + customLoginPage.trim();
		} else {
			return "authenticationendpoint/" + "samlsso/samlsso_auth_ajaxprocessor.jsp";
		}
	}

	/**
	 * 
	 * @param req
	 * @param paramName
	 * @return
	 */
	private String getRequestParameter(HttpServletRequest req, String paramName) {
		// This is to handle "null" values coming as the parameter values from the JSP.
        if(req.getParameter(paramName) != null && !req.getParameter(paramName).equals("null")){
            return req.getParameter(paramName);
        } else if (req.getAttribute(paramName) != null && !req.getAttribute(paramName).equals("null")) {
            return (String)req.getAttribute(paramName);
        }
        return null;
	}

    private void handleRequestWithOpenIDLogin(HttpServletRequest req, HttpServletResponse resp,
                                              SAMLSSOReqValidationResponseDTO signInRespDTO, String relayState, String ssoTokenId)
            throws ServletException, IOException, IdentityException {
    	
    	SAMLSSOSessionDTO sessionDTO = new SAMLSSOSessionDTO();
    	
		sessionDTO.setHttpQueryString(req.getQueryString());
		sessionDTO.setDestination(signInRespDTO.getDestination());
		sessionDTO.setRelayState(relayState);
		sessionDTO.setRequestMessageString(signInRespDTO.getRequestMessageString());
		sessionDTO.setIssuer(signInRespDTO.getIssuer());
		sessionDTO.setRequestID(signInRespDTO.getId());
		sessionDTO.setSubject(signInRespDTO.getSubject());
		sessionDTO.setRelyingPartySessionId(signInRespDTO.getRpSessionId());
		sessionDTO.setAssertionConsumerURL(signInRespDTO.getAssertionConsumerURL());
		
		String sessionDataKey = UUIDGenerator.generateUUID();
		HttpSession session = req.getSession();
		session.setAttribute(sessionDataKey, sessionDTO);
    	
        handleRequestFromLoginPage(req,resp,ssoTokenId, sessionDataKey);
    }
    
	private String getACSUrlWithTenantPartitioning(String acsUrl, String subject) {
		String domain = null;
		String acsUrlWithTenantDomain = acsUrl;
		if (subject != null && MultitenantUtils.getTenantDomain(subject) != null) {
			domain = MultitenantUtils.getTenantDomain(subject);
		}
		if (domain != null &&
		    "true".equals(IdentityUtil.getProperty((IdentityConstants.ServerConfig.SSO_TENANT_PARTITIONING_ENABLED)))) {
			acsUrlWithTenantDomain =
			                         acsUrlWithTenantDomain + "?" +
			                                 MultitenantConstants.TENANT_DOMAIN + "=" + domain;
		}
		return acsUrlWithTenantDomain;
	}
	
	private void sendToReAuthenticate(HttpServletRequest req, HttpServletResponse resp, 
	                                    SAMLSSORespDTO authRespDTO) throws IOException {
      
      String redirectURL = CarbonUIUtil.getAdminConsoleURL(req);
		redirectURL = redirectURL.replace("samlsso/carbon/",
		                                  getLoginPage(authRespDTO.getLoginPageURL()));
		
		String queryParams = "?" + SAMLSSOProviderConstants.AUTH_FAILURE + "=" + "true" + "&" +
      		SAMLSSOProviderConstants.AUTH_FAILURE_MSG + "=" + authRespDTO.getErrorMsg() + 
      		"&" + SAMLSSOProviderConstants.SESSION_DATA_KEY + "=" + 
      		req.getParameter(SAMLSSOProviderConstants.SESSION_DATA_KEY);
		
		resp.sendRedirect(redirectURL + queryParams);
	}
}
