/**
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
package org.apache.oltu.openidconnect.as.messages;

/**
 * This class contains the constants used in the IDToken
 * 
 */
public class IDToken {

	// REQUIRED
	public static final String ISS = "iss";
	public static final String SUB = "sub";
	public static final String AUD = "aud";
	public static final String EXP = "exp";
	public static final String IAT = "iat";
	// REQUIRED with Implicit flow, OPTIONAL with code flow
	public static final String AT_HASH = "at_hash";
	public static final String C_HASH = "c_hash";
	public static final String NONCE = "nonce";
	// OPTIONAL
	public static final String AZP = "azp";
	public static final String ACR = "acr";
	public static final String AUTH_TIME = "auth_time";

}
