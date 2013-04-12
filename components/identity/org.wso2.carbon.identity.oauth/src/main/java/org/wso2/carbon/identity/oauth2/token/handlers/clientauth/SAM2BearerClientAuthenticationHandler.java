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

package org.wso2.carbon.identity.oauth2.token.handlers.clientauth;

import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

public class SAM2BearerClientAuthenticationHandler extends BasicAuthClientAuthentcationHandler {

    /**
     * We have to override this method in order to set the resource owner username to OAuth token that we create. Token
     * is accessed through the token request message context.
     */
    @Override
    public boolean authenticateClient(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
        try {
            if(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId() != null &&
                    tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientSecret() != null){

                boolean isAuthenticated = OAuth2Util.authenticateClient(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId(),
                        tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientSecret());
                if(!isAuthenticated){
                    return false;
                }
            }
        } catch (IdentityOAuthAdminException e){
            throw new IdentityOAuth2Exception(e.getMessage(),e);
        }
        return true;
    }

}
