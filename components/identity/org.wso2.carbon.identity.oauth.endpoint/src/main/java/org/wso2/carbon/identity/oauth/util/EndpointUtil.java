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
package org.wso2.carbon.identity.oauth.util;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.ui.OAuth2Parameters;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EndpointUtil {

	/**
	 * Returns the {@code OAuth2Service} instance
	 * 
	 * @return
	 */
	public static OAuth2Service getOAuth2Service() {
		return (OAuth2Service) PrivilegedCarbonContext.getCurrentContext()
		                                              .getOSGiService(OAuth2Service.class);
	}

	/**
	 * Returns the {@code OAuthServerConfiguration} instance
	 * 
	 * @return
	 */
	public static OAuthServerConfiguration getOAuthServerConfiguration() {
		return (OAuthServerConfiguration) PrivilegedCarbonContext.getCurrentContext()
		                                                         .getOSGiService(OAuthServerConfiguration.class);
	}

	/**
	 * Returns the {@code OAuthServerConfiguration} instance
	 * 
	 * @return
	 */
	public static OAuth2TokenValidationService getOAuth2TokenValidationService() {
		return (OAuth2TokenValidationService) PrivilegedCarbonContext.getCurrentContext()
		                                                             .getOSGiService(OAuth2TokenValidationService.class);
	}

	/**
	 * Returns the request validator class name
	 * @return
	 * @throws OAuthSystemException 
	 */
	public static String getUserInfoRequestValidator() throws OAuthSystemException {
		OAuthServerConfiguration serverConfigService = getOAuthServerConfiguration();
		if(serverConfigService != null) {
			return serverConfigService.getOpenIDConnectUserInfoEndpointRequestValidator();
		} else {
			throw new OAuthSystemException("OAuthServerConfiguration not found");
		}
    }

	/**
	 * Returns the access token validator class name
	 * @return
	 */
	public static String getAccessTokenValidator() {
	    return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointAccessTokenValidator();
    }

	/**
	 * Returns the response builder class name
	 * @return
	 */
	public static String getUserInfoResponseBuilder() {
	    return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointResponseBuilder();
    }
	
	/**
	 * Returns the claim retriever class name
	 * @return
	 */
	public static String getUserInfoClaimRetriever() {
		return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimRetriever();
	}
	
	/**
	 * Return the claim dialect for the claim retriever 
	 * @return
	 */
	public static String getUserInfoClaimDialect() {
		return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimDialect();
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

        String errorPageUrl = CarbonUIUtil.getAdminConsoleURL("/") + "../authenticationendpoint/oauth2_login.do";
        try {
            errorPageUrl += "?" + OAuthConstants.OAUTH_ERROR_CODE + "=" + URLEncoder.encode(errorCode, "UTF-8") + "&" +
                    OAuthConstants.OAUTH_ERROR_MESSAGE + "=" + URLEncoder.encode(errorMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return errorPageUrl;
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
    	String oidcRequest = (String) req.getSession().getAttribute(OpenIDConnectConstant.Session.OIDC_REQUEST);
        String loginPage = CarbonUIUtil.getAdminConsoleURL("/") + "../authenticationendpoint/oauth2_login.do";
        try {
            loginPage =
                loginPage + "?" + OAuthConstants.SCOPE + "=" +
                        URLEncoder.encode(getScope(params), "UTF-8") + "&" + "application" + "=" +
                        URLEncoder.encode(params.getApplicationName(), "UTF-8");
            if("true".equals(oidcRequest)) {
            	loginPage = loginPage + "&oidcRequest=true";
            }
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        req.getSession().setAttribute("loginPage", loginPage);
        return loginPage;
    }

	/**
	 * Returns the consent page URL. If no custom page is defined, then the
	 * default will be returned.
	 * 
	 * @param params
	 * @param loggedInUser
	 * @return
	 */
	public static String getUserConsentURL(OAuth2Parameters params, String loggedInUser) {
		String consentPage =
		                     CarbonUIUtil.getAdminConsoleURL("/") +
		                             "../authenticationendpoint/oauth2_login.do";
		StringBuffer scopes = new StringBuffer();
		for(String scope : params.getScopes()) {
			scopes.append(scope);
		}
		try {
			consentPage +=
			               "?" + OpenIDConnectConstant.Session.OIDC_LOGGED_IN_USER + "=" +
			                       URLEncoder.encode(loggedInUser, "UTF-8") + "&" +
			                       OpenIDConnectConstant.Session.OIDC_RP + "=" +
			                       URLEncoder.encode(params.getApplicationName(), "UTF-8");
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
