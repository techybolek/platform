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
package org.wso2.carbon.identity.oauth.util;

public class OpenIDConnectConstant {

	public class Session {
		public static final String OIDC_REQUEST = "oidcRequest";
		public static final String OIDC_RESPONSE = "oidcRespose";
		public static final String OIDC_RP = "oidcApp";
		public static final String OIDC_LOGGED_IN_USER = "loggedInUser";
	}
	
	public class Parameter {
		public static final String SCOPE = "scope";
	}
	
	public class Consent {
		public static final String DENY = "deny";
		public static final String APPROVE = "approve";
		public static final String APPROVE_ALWAYS = "approve_always";
	}

}
