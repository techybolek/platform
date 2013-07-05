/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.provider.openid.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.DirectError;
import org.openid4java.message.ParameterList;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityConstants.OpenId;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.provider.dto.OpenIDAuthRequestDTO;
import org.wso2.carbon.identity.provider.dto.OpenIDAuthResponseDTO;
import org.wso2.carbon.identity.provider.dto.OpenIDClaimDTO;
import org.wso2.carbon.identity.provider.dto.OpenIDParameterDTO;
import org.wso2.carbon.identity.provider.dto.OpenIDUserProfileDTO;
import org.wso2.carbon.identity.provider.openid.OpenIDConstants;
import org.wso2.carbon.identity.provider.openid.client.OpenIDAdminClient;
import org.wso2.carbon.identity.provider.openid.util.OpenIDUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Handles functionality related OpenID association,
 * authentication,checkid_immediate checkid_setup. check_authentication [POST] :
 * Ask an Identity Provider if a message is valid. For dumb, state-less
 * Consumers or when verifying an invalidate_handle response. checkid_setup
 * [GET] : Ask an Identity Provider if a End User owns the Claimed Identifier,
 * but be willing to wait for the reply. The Consumer will pass the User-Agent
 * to the Identity Provider for a short period of time which will return either
 * a "yes" or "cancel" answer. checkid_immediate [GET] : Ask an Identity
 * Provider if a End User owns the Claimed Identifier, getting back an immediate
 * "yes" or "can't say" answer. associate [POST] : Establish a shared secret
 * between Consumer and Identity Provider
 */
public class OpenIDHandler {

	private String frontEndUrl;
	private String opAddress;

	// Guaranteed to be thread safe
	private static OpenIDHandler provider;
	private static Log log = LogFactory.getLog(OpenIDHandler.class);
	
	private static final String TRUE = "true";
	private static final String NULL = "null";
	
	/**
	 * Configure the OpenID Provider's end-point URL
	 * 
	 * @param serverUrl
	 */
	private OpenIDHandler(String serverUrl) {
		opAddress = serverUrl;
	}

	/**
	 * 
	 * @param serverUrl
	 * @return
	 */
	public static OpenIDHandler getInstance(String serverUrl) {
		if (provider == null) {
			provider = new OpenIDHandler(serverUrl);
		}
		return provider;
	}

	/**
	 * This is the page the user will be redirected for authentication.
	 * 
	 * @param frontEndUrl Authentication page
	 */
	public void setFrontEndUrl(String frontEndUrl) {
		this.frontEndUrl = frontEndUrl;
		if (log.isDebugEnabled()) {
			log.debug("Authentication page set to :" + this.frontEndUrl);
		}
	}

	/**
	 * @return OpenID Provider server URL.
	 */
	public String getOpAddress() {
		return opAddress;
	}

	/**
	 * Process the request message and handles the logic
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IdentityException
	 */
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	                                                                                      throws IdentityException {
		if (request.getParameter(OpenIDConstants.RequestParameter.LOGOU_URL) != null) {
			return handleSingleLogout(request, response);
		}
		
		if(request.getAttribute("nonlogin") == null && 
				request.getParameter(OpenIDConstants.RequestParameter.PASSWORD) != null){
	        handleRequestFromLoginPage(request, response);
		} else {
			if(request.getParameter(OpenIDConstants.RequestParameter.HAS_APPROVED_ALWAYS) != null){
				request.getSession().setAttribute(OpenIDConstants.SessionAttribute.USER_APPROVED, "true");
			}
			
			String responseText = null;
			try {
				HttpSession session = request.getSession();
				OpenIDAdminClient client = OpenIDUtil.getOpenIDAdminClient(session);
				ParameterList paramList = getParameterList(request);
				String mode = getOpenIDMessageMode(paramList, response, request);
				
				if (OpenId.ASSOCIATE.equals(mode)) {
					OpenIDParameterDTO[] reqDTO = OpenIDUtil.getOpenIDAuthRequest(request);
					responseText = client.getOpenIDAssociationResponse(reqDTO);
					if (log.isDebugEnabled()) {
						log.debug("Association created successfully");
					}
				} else if (OpenId.CHECKID_SETUP.equals(mode) || OpenId.CHECKID_IMMEDIATE.equals(mode)) {
					return checkSetupOrImmediate(request, response, paramList, client);
				} else if (OpenId.CHECK_AUTHENTICATION.equals(mode)) {
					responseText = client.verify(OpenIDUtil.getOpenIDAuthRequest(request));
					if (log.isDebugEnabled()) {
						log.debug("Authentication verified successfully");
					}
				} else {
					responseText = getErrorResponseText("Not a valid OpenID request");
					if (log.isDebugEnabled()) {
						log.debug("No valid MODE found : " + request.getQueryString());
					}
				}
			} catch (Exception e) {
				responseText = getErrorResponseText(e.getMessage());
			}

			try { // Return the result to the user.
				directResponse(response, responseText);
			} catch (IOException e) {
				log.error(e.getMessage());
				throw new IdentityException("OpenID redirect reponse failed");
			}
		}
		
		return null;
	}

	/**
	 * Returns the mode field of the OpenID message.
	 * 
	 * @param paramList
	 * @param response
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private String getOpenIDMessageMode(ParameterList paramList, HttpServletResponse response,
	                                    HttpServletRequest request) throws IOException {
		String mode = null;
		if (paramList == null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid OpenID message :" + request.getQueryString());
			}
			directResponse(response, getErrorResponseText("Invalid OpenID message"));
			return null;
		}
		mode =
		       paramList.hasParameter(OpenId.ATTR_MODE)
		                                               ? paramList.getParameterValue(OpenId.ATTR_MODE)
		                                               : null;
		if (log.isDebugEnabled()) {
			log.debug("OpenID authentication mode :" + mode);
		}
		return mode;
	}

	/**
	 * This method returns the OpenID ParameterList object.
	 * If the first request, then list is taken from the http request else will
	 * be taken from the http session.
	 * 
	 * @param request
	 * @return {@link ParameterList}
	 */
	private ParameterList getParameterList(HttpServletRequest request) {

		if (OpenId.COMPLETE.equals(request.getSession().getAttribute(OpenId.ACTION)) ||
		    OpenId.CANCEL.equals(request.getSession().getAttribute(OpenId.ACTION))) {
			// not the first visit, get from the session
			return (ParameterList) request.getSession().getAttribute(OpenId.PARAM_LIST);

		} else {
			// its the fist visit, get from the request
			return new ParameterList(request.getParameterMap());
		}
	}

	/**
	 * This method handles single logout requests.
	 * Basically we are invalidating the session data and cookies.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private String handleSingleLogout(HttpServletRequest request, HttpServletResponse response) {

		log.info("OpenID Single Logout for " +
		         request.getSession().getAttribute("authenticatedOpenID"));

		// removing the authenticated user from the session
		request.getSession().setAttribute("authenticatedOpenID", null);

		// removing the remember-me cookie
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			Cookie curCookie = null;
			for (int x = 0; x < cookies.length; x++) {
				curCookie = cookies[x];
				if (curCookie.getName().equalsIgnoreCase("openidtoken")) {
					curCookie.setMaxAge(0);// removing the cookie
					response.addCookie(curCookie);
					break;
				}
			}
		}
		// TODO : Must invalidate the cookie from backend too
		return (String) request.getParameter("logoutUrl");
	}

	/**
	 * checkid_immediate : Ask an Identity Provider if an End User owns the
	 * Claimed Identifier, getting back an immediate "yes" or "can't say"
	 * answer. checkid_setup Description: Ask an Identity Provider if a End User
	 * owns the Claimed Identifier, but be willing to wait for the reply. The
	 * Consumer will pass the User-Agent to the Identity Provider for a short
	 * period of time which will return either a "yes" or "cancel" answer.
	 * 
	 * @param request
	 * @param params
	 * @param client 
	 * @throws Exception
	 */
	private String checkSetupOrImmediate(HttpServletRequest request, HttpServletResponse response, ParameterList params, OpenIDAdminClient client)
	                                                                                      throws Exception {
		boolean authenticated = false;
		String profileName = null;
		HttpSession session = request.getSession();
		String openId =
		         params.hasParameter(IdentityConstants.OpenId.ATTR_IDENTITY)
		                                                                    ? params.getParameterValue(IdentityConstants.OpenId.ATTR_IDENTITY)
		                                                                    : null;
		if (openId == null) {
			throw new IdentityException("Required attribute openid.identity is missing");
		}
		if (openId.endsWith("/openid/")) {
			String openIdInSession = (String) session.getAttribute(OpenIDConstants.SessionAttribute.OPENID);
			if (openIdInSession != null && !"".equals(openIdInSession.trim())) {
				openId = openIdInSession;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Authentication check for user " + openId);
		}

		boolean completed =
		                    IdentityConstants.OpenId.COMPLETE.equals(session.getAttribute(IdentityConstants.OpenId.ACTION));
		boolean approved = TRUE.equals(session.getAttribute(IdentityConstants.USER_APPROVED));

		if (completed && approved) {
			session.removeAttribute(IdentityConstants.USER_APPROVED);
			session.removeAttribute(IdentityConstants.OpenId.ACTION);
			session.removeAttribute(OpenIDConstants.SessionAttribute.PROFILE);
			authenticated = true;
			if (log.isDebugEnabled()) {
				log.debug("Authenticated and user confirmed :" + openId);
			}
			profileName = (String) session.getAttribute(OpenIDConstants.SessionAttribute.PROFILE);
			if (profileName == null) {
				profileName = OpenIDConstants.SessionAttribute.DEFAULT_PROFILE;
			}
			if (log.isDebugEnabled()) {
				log.debug("Selected profile : " + profileName);
			}
			updateRPInfo(openId, profileName, params, client, request, request);
		}

		if (IdentityConstants.OpenId.CANCEL.equals(session.getAttribute(IdentityConstants.OpenId.ACTION))) {
			if (log.isDebugEnabled()) {
				log.debug("User cancelled :" + openId);
			}
			authenticated = false;
		} else if (!authenticated) {
			if (log.isDebugEnabled()) {
                log.debug(openId + " not authenticated. Redirecting for authentication");
			}
			session.setAttribute(IdentityConstants.OpenId.PARAM_LIST, params);
			return getLoginPageUrl(openId, request, response, params);
		}

		//session.removeAttribute(IdentityConstants.OpenId.PARAM_LIST);
		OpenIDAuthRequestDTO openIDAuthRequest = new OpenIDAuthRequestDTO();

		if (IdentityConstants.TRUE.equals(session.getAttribute(IdentityConstants.PHISHING_RESISTANCE))) {
			openIDAuthRequest.setPhishiingResistanceAuthRequest(true);
			// Clear the session.
			session.removeAttribute(IdentityConstants.PHISHING_RESISTANCE);
		}
		if (IdentityConstants.TRUE.equals(session.getAttribute(IdentityConstants.MULTI_FACTOR_AUTH))) {
			openIDAuthRequest.setMultiFactorAuthRequested(true);
			// Clear the cache.
			session.removeAttribute(IdentityConstants.MULTI_FACTOR_AUTH);
		}
		openIDAuthRequest.setParams(OpenIDUtil.getOpenIDAuthRequest(params));
        openIDAuthRequest.setOpLocalId(openId);
        openIDAuthRequest.setUserSelectedClaimedId(openId);
		openIDAuthRequest.setAuthenticated(authenticated);
		openIDAuthRequest.setOpenID(openId);
		openIDAuthRequest.setProfileName(profileName);
		OpenIDAuthResponseDTO openIDAuthResponse = client.getOpenIDAuthResponse(openIDAuthRequest);

		if (openIDAuthResponse != null) {
			return openIDAuthResponse.getDestinationUrl();
		}
		return null;
	}

	/**
	 * Updates the RP information of user in the database
	 * @param openId
	 * @param profileName
	 * @param params
	 * @param client
	 * @param request
	 * @param session
	 * @throws Exception
	 */
	private void updateRPInfo(String openId, String profileName, ParameterList params,
	                          OpenIDAdminClient client, ServletRequest request,
	                          ServletRequest session) throws Exception {
		if (!client.isOpenIDUserApprovalBypassEnabled()) {
			boolean alwaysApprovedRp;
			if (Boolean.parseBoolean((String) session.getAttribute(OpenIDConstants.SessionAttribute.USER_APPROVED_ALWAYS))) {
				alwaysApprovedRp = true;
			} else {
				alwaysApprovedRp =
				                   Boolean.parseBoolean((String) request.getParameter(OpenIDConstants.SessionAttribute.HAS_APPROVED_ALWAYS));
			}

			client.updateOpenIDUserRPInfo(params.getParameterValue(IdentityConstants.OpenId.ATTR_RETURN_TO),
			                              alwaysApprovedRp, profileName, openId);
		}
	}

	/**
	 * Returns the login page URL. User will be redirected to this URL when they
	 * are not authenticated.
	 * 
	 * @param openId
	 * @param request
	 * @param params
	 * @return loginPageUrl
	 * @throws IdentityException 
	 * @throws IOException 
	 */
	private String getLoginPageUrl(String openId, HttpServletRequest request,
	                               HttpServletResponse response, ParameterList params)
	                                                                                  throws IdentityException,
	                                                                                  IOException {
		String loginPageUrl = frontEndUrl;
		String tenant = MultitenantUtils.getDomainNameFromOpenId(openId);
		// checking for OpenID remember me cookie
		Cookie[] cookies = request.getCookies();
		String token = null;
		if (cookies != null) {
			Cookie curCookie = null;
			for (Cookie cookie : cookies) {
				curCookie = cookie;
				params.getParameterValue(IdentityConstants.OpenId.ATTR_RETURN_TO);
				if (curCookie.getName().equalsIgnoreCase(OpenIDConstants.Cookie.OPENID_TOKEN)) {
					token = curCookie.getValue();
					break;
				}
			}
		}
		// if cookie found
        if (token != null && !NULL.equals(token) ||
                (openId.equals(request.getSession(false).getAttribute(OpenIDConstants.SessionAttribute.AUTHENTICATED_OPENID)))) {
            request.getSession(false).setAttribute(OpenIDConstants.SessionAttribute.OPENID, openId);
			handleRequestFromLoginPage(request, response);
			return null;
		} else {
    		loginPageUrl = loginPageUrl + OpenIDUtil.getLoginPageQueryParams(params);
			request.getSession().setAttribute(OpenIDConstants.SessionAttribute.OPENID, 
			                                  params.getParameterValue(IdentityConstants.OpenId.ATTR_IDENTITY));
		}
//		if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
//			return loginPageUrl.replace("/carbon/", "/t/" + tenant + "/carbon/");
//		}
		
		return loginPageUrl;
	}

	/**
	 * Return the error response message based on the given message
	 * 
	 * @param message
	 *            Error message
	 * @return Direct error
	 */
	private String getErrorResponseText(String message) {
		log.error(message);
		return DirectError.createDirectError(message).keyValueFormEncoding();
	}

	/**
	 * Send a direct response to the RP.
	 * 
	 * @param httpResp
	 *            HttpServletResponse
	 * @param response
	 *            Response message
	 * @return
	 * @throws IOException
	 */
	private void directResponse(HttpServletResponse httpResp, String response) throws IOException {
		ServletOutputStream stream = null;
		try {
			stream = httpResp.getOutputStream();
			stream.write(response.getBytes());
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	private void handleRequestFromLoginPage(HttpServletRequest req, HttpServletResponse resp) throws IdentityException {

		try {
			HttpSession session = req.getSession();

			String rememberMe = req.getParameter(OpenIDConstants.RequestParameter.REMEMBER);
			boolean isRemembered = false;
			String openid =
			                (req.getParameter(OpenIDConstants.RequestParameter.OPENID) != null)
                                   ? req.getParameter(OpenIDConstants.RequestParameter.OPENID)
                                   : (String) session.getAttribute(OpenIDConstants.SessionAttribute.OPENID);

			// Directed Identity handling
			String userName = req.getParameter(OpenIDConstants.SessionAttribute.USERNAME);
			if ((userName == null || "".equals(userName.trim())) && openid.endsWith("/openid/")) {
				userName = (String) session.getAttribute(OpenIDConstants.SessionAttribute.USERNAME);
				
				if (userName == null) {
					Cookie cookie = OpenIDUtil.getCookie(userName, req);
					
					if(cookie != null){
						userName = cookie.getValue();
					}
				}
			} else {
				session.setAttribute(OpenIDConstants.SessionAttribute.USERNAME, userName);
			}

			if (userName != null && !"".equals(userName.trim())) {
				openid = openid + userName;
			}

			session.setAttribute(OpenIDConstants.SessionAttribute.OPENID, openid);

			if ("true".equals(rememberMe)) {
				isRemembered = true;
			}

			OpenIDAdminClient client = OpenIDUtil.getOpenIDAdminClient(session);
			boolean isAuthenticated = client.authenticateWithOpenID(openid,
                                                req.getParameter(OpenIDConstants.RequestParameter.PASSWORD),
                                                session, req, resp,
                                                isRemembered);

			if (isAuthenticated ||
			    openid.equals(session.getAttribute(OpenIDConstants.SessionAttribute.AUTHENTICATED_OPENID))) {
				session.setAttribute(OpenIDConstants.SessionAttribute.IS_OPENID_AUTHENTICATED, "true");
				session.setAttribute(OpenIDConstants.SessionAttribute.AUTHENTICATED_OPENID, openid);

				// user approval is always bypased based on the identity.xml
				// config
				if (client.isOpenIDUserApprovalBypassEnabled()) {
					session.setAttribute(OpenIDConstants.SessionAttribute.SELECTED_PROFILE, "default");
					session.setAttribute(OpenIDConstants.SessionAttribute.ACTION, "complete");
					session.setAttribute(OpenIDConstants.SessionAttribute.USER_APPROVED, "true");
					session.removeAttribute(OpenIDConstants.SessionAttribute.IS_OPENID_AUTHENTICATED);

					if (client.getNewCookieValue() != null) {
						OpenIDUtil.setCookie("openidtoken", client.getNewCookieValue(),
						          client.getOpenIDSessionTimeout(), "/", "", true, resp);
					}

					if (userName != null) {
						OpenIDUtil.setCookie("openidrememberme", userName, client.getOpenIDSessionTimeout(),
						          "/", "", true, resp);
					}

					req.setAttribute("nonlogin", "true");
					RequestDispatcher dispatcher = req.getRequestDispatcher("../../openidserver");
					dispatcher.forward(req, resp);
				} else { // reading RP info from the database
					String[] rpInfo = client.getOpenIDUserRPInfo(openid,
					                      ((ParameterList) session.getAttribute(OpenId.PARAM_LIST)).
					                      			getParameterValue(OpenId.ATTR_RETURN_TO));
					
					if (rpInfo[0].equals("true")) { // approve always
						session.setAttribute(OpenIDConstants.SessionAttribute.ACTION, "complete");
						session.setAttribute(OpenIDConstants.SessionAttribute.USER_APPROVED, "true");
						session.setAttribute(OpenIDConstants.SessionAttribute.USER_APPROVED_ALWAYS, "true");
						session.setAttribute(OpenIDConstants.SessionAttribute.SELECTED_PROFILE, rpInfo[1]);
						session.removeAttribute(OpenIDConstants.SessionAttribute.IS_OPENID_AUTHENTICATED);

						if (client.getNewCookieValue() != null) {
							OpenIDUtil.setCookie("openidtoken", client.getNewCookieValue(),
							          client.getOpenIDSessionTimeout(), "/", "", true, resp);
						}

						if (userName != null) {
							OpenIDUtil.setCookie("openidrememberme", userName,
							          client.getOpenIDSessionTimeout(), "/", "", true, resp);
						}
						
						req.setAttribute("nonlogin", "true");
						RequestDispatcher dispatcher = req.getRequestDispatcher("../../openidserver");
						dispatcher.forward(req, resp);
					} else { // redirect to user approval page
						if (client.getNewCookieValue() != null) {
							OpenIDUtil.setCookie("openidtoken", client.getNewCookieValue(),
							          client.getOpenIDSessionTimeout(), "/", "", true, resp);
						}
						if (userName != null) {
							OpenIDUtil.setCookie("openidrememberme", userName,
							          client.getOpenIDSessionTimeout(), "/", "", true, resp);
						}

						sendToApprovalPage(req, resp);
					}
				}

			} else {
				session.removeAttribute(OpenIDConstants.SessionAttribute.IS_OPENID_AUTHENTICATED);

				OpenIDUtil.deleteCookie("openidtoken", "/", req);
				OpenIDUtil.deleteCookie("openidrememberme", "/", req);
				
				String frontEndUrl = OpenIDUtil.getAdminConsoleURL(req);
				frontEndUrl = frontEndUrl.replace("carbon/", "authenticationendpoint/openid/openid_auth.jsp");
				
				resp.sendRedirect(frontEndUrl +
				                      OpenIDUtil.getLoginPageQueryParams(
				                         (ParameterList) req.getSession().getAttribute(OpenId.PARAM_LIST)) +
				                      "&errorMsg=error.while.user.auth");
			}
		} catch (Exception e) {
			throw new IdentityException("Exception while handling request from the login page", e);
		}
	}
	
	private void sendToApprovalPage(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		HttpSession session = req.getSession();
		
		PrintWriter out = resp.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<p>You are now redirected back to Profile Approval Page.");
		out.println(" If the redirection fails, please click the post button.</p>");
		out.println("<form method='post' action='authenticationendpoint/openid/openid_profile_view.jsp'>");
		out.println("<p>");
		
		OpenIDUserProfileDTO[] profiles = null;

		if (session.getAttribute("profiles") == null) {
			ParameterList requestedClaims = null;
			String isPhishingResistance = (String) session.getAttribute("papePhishingResistance");
			String isMultiFactorAuthEnabled = (String) session.getAttribute("multiFactorAuth");
			String infoCardAuthStatus = (String) session.getAttribute("infoCardAuthenticated");
			boolean infoCardAuthenticated = false;

			if (infoCardAuthStatus != null && infoCardAuthStatus.equals("true")) {
				infoCardAuthenticated = true;
			}

			String openid = (req.getParameter("openid") != null)
                                    ? req.getParameter("openid")
                                    : (String) session.getAttribute("openId");
			try {
				// we get the openid admin client from the session. This is
				// created once per session
				OpenIDAdminClient client = OpenIDUtil.getOpenIDAdminClient(session);

				if (infoCardAuthenticated || session.getAttribute("isOpenIDAuthenticated") != null) {
					requestedClaims = (ParameterList) session.getAttribute(IdentityConstants.OpenId.PARAM_LIST);
					profiles = client.getUserProfiles(openid, requestedClaims);
					session.setAttribute("profiles", profiles);
					session.setAttribute("selectedProfile", "default");
					session.setAttribute("_action", "complete");
					session.removeAttribute("isOpenIDAuthenticated");

					if (isPhishingResistance != null && isPhishingResistance.equals("true")) {
						session.setAttribute(IdentityConstants.PHISHING_RESISTANCE, IdentityConstants.TRUE);
					}

					if (isMultiFactorAuthEnabled != null && isMultiFactorAuthEnabled.equals("true")) {
						session.setAttribute(IdentityConstants.MULTI_FACTOR_AUTH, IdentityConstants.TRUE);
					}
				} else {
					String frontEndUrl = OpenIDUtil.getAdminConsoleURL(req);
					frontEndUrl = frontEndUrl.replace("carbon/",
					                                  "authenticationendpoint/openid/openid_auth.jsp");

					resp.sendRedirect(frontEndUrl +
					                  OpenIDUtil.getLoginPageQueryParams((ParameterList) req.getSession()
					                                                                        .getAttribute(OpenId.PARAM_LIST)) +
					                  "&errorMsg=invalid.credentials");
					return;
				}
			} catch (Exception e) {
				String frontEndUrl = OpenIDUtil.getAdminConsoleURL(req);
				resp.sendRedirect(frontEndUrl + "admin/login.jsp");
				return;
			} finally {
				session.removeAttribute("papePhishingResistance");
				session.removeAttribute("multiFactorAuth");
				session.removeAttribute("infoCardBasedMultiFacotrAuth");
				session.removeAttribute("xmppBasedMultiFacotrAuth");
				session.removeAttribute("infoCardAuthenticated");
				session.removeAttribute("isOpenIDAuthenticated");
			}
		} else {
			profiles = (OpenIDUserProfileDTO[]) session.getAttribute("profiles");
		}

		String selectedProfile = req.getParameter("selectedProfile") == null
                                            ? "default"
                                            : req.getParameter("selectedProfile");
		out.println("<input type='hidden' name='selectedProfile' value='" + selectedProfile + "'>");

		for (int i = 0; i < profiles.length; i++) {
			OpenIDUserProfileDTO profile = profiles[i];
			out.println("<input type='hidden' name='profile' value='" + profile.getProfileName() + "'>");

			if (profile.getProfileName().equals(selectedProfile)) {
				OpenIDClaimDTO[] claimSet = profile.getClaimSet();

				if (claimSet != null) {
					for (int j = 0; j < claimSet.length; j++) {
                        OpenIDClaimDTO claimDto = claimSet[j];
                        out.println("<input type='hidden' name='claimTag' value='" + claimDto.getDisplayTag() + "'>");
                        out.println("<input type='hidden' name='claimValue' value='" + claimDto.getClaimValue() != null ? claimDto.getClaimValue() : "" + "'>");
                    }
                }
            }
        }
	    
		out.println("<button type='submit'>POST</button>");
		out.println("</p>");
		out.println("</form>");
		out.println("<script type='text/javascript'>");
		out.println("document.forms[0].submit();");
		out.println("</script>");
		out.println("</body>");
		out.println("</html>");	
		
		return;
	}
}
