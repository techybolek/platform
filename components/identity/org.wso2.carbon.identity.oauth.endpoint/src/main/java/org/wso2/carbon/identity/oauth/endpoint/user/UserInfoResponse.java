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
package org.wso2.carbon.identity.oauth.endpoint.user;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.identity.oauth.ui.OAuthClientException;
import org.wso2.carbon.user.api.Claim;

/**
 * Abstract representation of the UserInfoResponse. The response can be a JSON
 * or a JWT
 * 
 */
public abstract class UserInfoResponse {

	/**
	 * 
	 * @param claims
	 * @return
	 * @throws OAuthClientException
	 */
	public abstract String getResponseString(Claim[] claims) throws OAuthClientException;

	/**
	 * Builds the claims defined in
	 * http://openid.net/specs/openid-connect-basic-1_0-22.html#id_res
	 * 
	 * @param claims
	 * @return
	 */
	public Map<String, Object> getClaimMap(Claim[] claims) {
		Map<String, Object> claimMap = new HashMap<String, Object>();
		for (Claim curClaim : claims) {
			claimMap.put(curClaim.getClaimUri(), curClaim.getValue());
		}
		return claimMap;
	}
}
