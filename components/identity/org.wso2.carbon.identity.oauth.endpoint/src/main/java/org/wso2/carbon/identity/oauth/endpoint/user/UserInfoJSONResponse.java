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
/**
 * 
 */
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.identity.oauth.ui.OAuthClientException;
import org.wso2.carbon.user.api.Claim;

/**
 * @author sga
 *
 */
public class UserInfoJSONResponse extends UserInfoResponse {

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.oauth.endpoint.user.UserInfoResponse#getResponseString(org.wso2.carbon.user.api.Claim[])
	 */
	@Override
	public String getResponseString(Claim[] claims) throws OAuthClientException {
		try {
	        return JSONUtils.buildJSON(getClaimMap(claims));
        } catch (JSONException e) {
        	throw new OAuthClientException("Error while generating the response JSON");
        }
	}

}
