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

package org.wso2.carbon.identity.oauth2.util;

public class OAuth2Constants {
    public static class TokenStates {
        public final static String TOKEN_STATE_ACTIVE = "ACTIVE";
        public final static String TOKEN_STATE_REVOKED = "REVOKED";
        public final static String TOKEN_STATE_EXPIRED = "EXPIRED";
    }
    public static long UNASSIGNED_VALIDITY_PERIOD = -1l;

    public static String OAUTH_SAML2_BEARER_METHOD = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
    public static String OAUTH_SAML2_BEARER_GRANT_ENUM = "SAML20_BEARER";
    public static final String OAUTH_SAML2_ASSERTION = "SAML2Assertion";

    public static String ACCESS_TOKEN_STORE_TABLE = "IDN_OAUTH2_ACCESS_TOKEN";
    

    public static class ClientAuthMethods {
        public static final String BASIC = "basic";
        public static final String SAML_20_BEARER = "saml-2.0-bearer";
    }

    public static class OAuthError{
        public static class TokenResponse{
            public static final String UNSUPPRTED_CLIENT_AUTHENTICATION_METHOD = "unsupported_client_authentication_method";
        }
    }

    public static final String OAUTH_CACHE_MANAGER = "OAuthCacheManager";

}
