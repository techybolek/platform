/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.oauth.endpoint.user.impl;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.endpoint.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.oauth.util.EndpointUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Retrieving claims from the user store for the given claims dialect
 * 
 * 
 */
public class UserInfoUserStoreClaimRetriever implements UserInfoClaimRetriever {

	/**
	 * Retrieving claims from the Identity Server user store with the given
	 * claim dialect
	 */
	public Map<String, Object> getClaimsMap(OAuth2TokenValidationResponseDTO tokenResponse)
	                                                                                       throws OAuthSystemException {
		String tenantUser = MultitenantUtils.getTenantAwareUsername(tokenResponse.getAuthorizedUser());
		String domainName = MultitenantUtils.getTenantDomain(tokenResponse.getAuthorizedUser());
		Claim[] claims;
		try {
			claims =
			         IdentityTenantUtil.getRealm(domainName, tenantUser).getUserStoreManager()
			                           .getUserClaimValues(tenantUser, null);
		} catch (Exception e) {
			throw new OAuthSystemException("Error while reading user claims for the user " + tenantUser);
		}

		String claimDialect =
		                      EndpointUtil.getOAuthServerConfiguration()
		                                  .getOpenIDConnectUserInfoEndpointClaimDialect();
		Map<String, Object> dialectClaims = new HashMap<String, Object>();
		// lets always return the sub claim
		dialectClaims.put("sub", tenantUser);
		// add only the claims with the requested dialect
		for (Claim curClaim : claims) {
			if (curClaim.getClaimUri().contains(claimDialect)) {
				dialectClaims.put(curClaim.getClaimUri(), curClaim.getValue());
			}
		}
		return dialectClaims;
	}

}
