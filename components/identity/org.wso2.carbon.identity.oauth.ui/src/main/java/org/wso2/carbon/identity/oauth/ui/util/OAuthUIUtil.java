/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.oauth.ui.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth.ui.OAuth2Parameters;
import org.wso2.carbon.identity.oauth.ui.OAuthClientException;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for OAuth FE functionality.
 */
public class OAuthUIUtil {

    private static Log log = LogFactory.getLog(OAuthUIUtil.class);

    /**
     * Returns the corresponding absolute endpoint URL. e.g. https://localhost:9443/oauth2/access-token
     * @param endpointType It could be request-token endpoint, callback-token endpoint or access-token endpoint
     * @param oauthVersion OAuth version whether it is 1.0a or 2.0
     * @param request HttpServletRequest coming to the FE jsp
     * @return Absolute endpoint URL.
     */
    public static String getAbsoluteEndpointURL(String endpointType, String oauthVersion, HttpServletRequest request){
        // derive the hostname:port from the admin console url
        String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
        String endpointURL = adminConsoleURL.substring(0, adminConsoleURL.indexOf("/carbon"));

        // get the servlet context from the OAuth version.
        String oauthServletContext = "/oauth2";
        if ("/token".equals(endpointType)) {
        	oauthServletContext = "/oauth2endpoints";
        }
        if(oauthVersion.equals(OAuthConstants.OAuthVersions.VERSION_1A)){
            oauthServletContext = "/oauth";
        }
        return (endpointURL + oauthServletContext + endpointType);
    }

    /**
     * Extracts the username and password info from the HTTP Authorization Header
     * @param authorizationHeader "Basic " + base64encode(username + ":" + password)
     * @return String array with username and password.
     * @throws IdentityException If the decoded data is null.
     */
    public static String[] extractCredentialsFromAuthzHeader(String authorizationHeader)
            throws OAuthClientException{
        String[] splitValues = authorizationHeader.trim().split(" ");
        byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
        if (decodedBytes != null) {
            String userNamePassword = new String(decodedBytes);
            return userNamePassword.split(":");
        } else {
            String errMsg = "Error decoding authorization header. " +
                    "Could not retrieve user name and password.";
            log.debug(errMsg);
            throw new OAuthClientException(errMsg);
        }
    }

    public static String getRealmInfo(){
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String hostname = serverConfig.getFirstProperty("HostName");
        return "Basic realm=" + hostname;
    }
    
    /**
     * Returns the schema and the credential of authorization header
     * @param authorizationHeader
     * @return
     */
    public static String[] extractAuthorizationHeaderInfo(String authorizationHeader) {
    	  String[] splitValues = authorizationHeader.trim().split(" ");
    	  byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
    	  if (decodedBytes != null) {
              splitValues[1] = new String(decodedBytes);
          }
    	  return splitValues;
    }

    public static OAuthConsumerAppDTO[] doPaging(int pageNumber, OAuthConsumerAppDTO[] oAuthConsumerAppDTOSet) {

           int itemsPerPageInt = OAuthConstants.DEFAULT_ITEMS_PER_PAGE;
        OAuthConsumerAppDTO[] returnedOAuthConsumerSet;

        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        if (itemsPerPageInt < oAuthConsumerAppDTOSet.length) {
            returnedOAuthConsumerSet = new OAuthConsumerAppDTO[itemsPerPageInt];
        } else {
            returnedOAuthConsumerSet = new OAuthConsumerAppDTO[oAuthConsumerAppDTOSet.length];
        }
        for (int i = startIndex, j = 0; i < endIndex && i < oAuthConsumerAppDTOSet.length; i++, j++) {
            returnedOAuthConsumerSet[j] = oAuthConsumerAppDTOSet[i];
        }

        return returnedOAuthConsumerSet;
    }

	/**
	 * Returns the error page URL. If no custom page is defined, then the
	 * default will be returned.
	 * 
	 * @param req
	 * @param clienDTO
	 * @param errorCode
	 * @param errorMessage
	 * @return
	 */
	public static String getErrorPageURL(HttpServletRequest req, OAuth2ClientValidationResponseDTO clienDTO,
	                                     String errorCode, String errorMessage) {
		String errorPageURL = null;
		String errorPageInSession = (String) req.getSession().getAttribute("errorPageURL");
		// if there is a configured custom page, then user it
		if (clienDTO != null && clienDTO.getErrorPageURL() != null || errorPageInSession != null ||
		    !clienDTO.getErrorPageURL().equals("")) {
			errorPageURL = clienDTO.getErrorPageURL();
			if (errorPageURL == null) {
				errorPageURL = errorPageInSession;
				req.getSession().setAttribute("errorPageURL", errorPageURL);
			}
			try {
				errorPageURL =
				               errorPageURL + "?" + OAuthConstants.OAUTH_ERROR_CODE + "=" +
				                       URLEncoder.encode(errorCode, "UTF-8") + "&" +
				                       OAuthConstants.OAUTH_ERROR_MESSAGE + "=" +
				                       URLEncoder.encode(errorMessage, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// ignore, UTF-8 must be supported
			}
		} else { // else use the default 
			errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth-error.jsp";
			errorPageURL = errorPageURL.replace("/oauth2/authorize", "");
			req.getSession().setAttribute(OAuthConstants.OAUTH_ERROR_CODE, errorCode);
			req.getSession().setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE, errorMessage);
		}
		return errorPageURL;
	}
    
	/**
	 * Returns the login page URL. If no custom page is defined, then the
	 * default will be returned.
	 * 
	 * @param req
	 * @param params
	 * @param clientDTO
	 * @return
	 */
	public static String getLoginPageURL(HttpServletRequest req, OAuth2ClientValidationResponseDTO clientDTO,
	                                     OAuth2Parameters params) {
		String loginPage = null;
		// if there is a configured custom page, then use it
		if (clientDTO != null && clientDTO.getLoginPageURL() != null && !clientDTO.getLoginPageURL().equals("")) {
			
			loginPage = clientDTO.getLoginPageURL();
			try {
				loginPage =
				            loginPage + "?" + OAuthConstants.SCOPE + "=" +
				                    URLEncoder.encode(getScope(params), "UTF-8") + "&" + "application" + "=" +
				                    URLEncoder.encode(params.getApplicationName(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// ignore, this is not going to happen 
			}
		} else { // else use the default
			loginPage = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth2_authn_ajaxprocessor.jsp";
			loginPage = loginPage.replace("/oauth2/authorize", "");
		}
		req.getSession().setAttribute("loginPage", loginPage);
		return loginPage;
	}
	
	/**
	 * Returns the consent page URL. If no custom page is defined, then the
	 * default will be returned.
	 * 
	 * @param req
	 * @param params
	 * @param clientDTO
	 * @param redirectUrl 
	 * @param loggedInUser 
	 * @return
	 */
	public static String getUserConsentURL(HttpServletRequest req, OAuth2ClientValidationResponseDTO clientDTO,
	                                     OAuth2Parameters params, String loggedInUser, String redirectUrl) {
		String consentPage = null;
		// if there is a configured custom page, then use it 
		if (clientDTO != null && clientDTO.getConsentPageUrl() != null && !clientDTO.getConsentPageUrl().equals("")) {
			consentPage = clientDTO.getConsentPageUrl();
		} else { // else use the default 
			consentPage = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth2_consent_ajaxprocessor.jsp";
			consentPage = consentPage.replace("/oauth2/authenticate", "");
		}
		req.getSession().setAttribute("consentPage", consentPage);
		try {
			consentPage =
			              consentPage + "?" + OAuthConstants.OIDCSessionConstant.OIDC_LOGGED_IN_USER + "=" +
			                      URLEncoder.encode(loggedInUser, "UTF-8") + "&" +
			                      OAuthConstants.OIDCSessionConstant.OIDC_RP + "=" +
			                      URLEncoder.encode(params.getApplicationName(), "UTF-8") + "&" +
			                      OAuthConstants.OIDCSessionConstant.OIDC_RESPONSE + "=" +
			                      URLEncoder.encode(redirectUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore
		}
		return consentPage;
	}
	
	private static String getScope(OAuth2Parameters params) {
		StringBuffer scopes = new StringBuffer();
		for(String scope : params.getScopes() ) {
			scopes.append(scope+" ");
		}
		return scopes.toString();
	}
}
