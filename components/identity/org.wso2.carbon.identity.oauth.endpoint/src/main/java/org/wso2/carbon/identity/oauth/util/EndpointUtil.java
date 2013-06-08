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
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;

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

}
