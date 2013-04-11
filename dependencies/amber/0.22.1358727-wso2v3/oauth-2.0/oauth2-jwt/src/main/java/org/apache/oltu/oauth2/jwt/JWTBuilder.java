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
package org.apache.oltu.oauth2.jwt;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.codehaus.jettison.json.JSONException;

/**
 * This class represents the JSON Web Token Generator. The JWTs are generated
 * according to the JSON specification. This generator is capable of generating
 * Plain Text JWT, JWS, JWE and JWT. All three types of claims are supported by
 * this generator : Reserved claims, Private Claims and Public claims.
 */
public class JWTBuilder {

	private static Log log = LogFactory.getLog(JWTBuilder.class);
	private static boolean debug = log.isDebugEnabled();

	private Map<String, Object> headerParams = new HashMap<String, Object>();
	private Map<String, Object> payloadClaims = new HashMap<String, Object>();
	private String headerJson = null;
	private String payloadJson = null;
	private String signatureJson = null;
	private String encodedHeader = null;
	private String encodedPayload = "";
	private String encodedSignature = "";
	private Key sigKey = null;
	private String sigAlg = null;
	private Key encKey = null;
	private String encAlg = null;
	private boolean isSignAndEncrypt = false;

	/**
	 * This methods is used to add header parameters to the JWT header. Custom
	 * JWT headers can be defined. The builder does not evaluate the semantic
	 * meanings of the parameters. The JWT receiver should process the semantic
	 * meanings of those parameters. However builder does not allow empty valued
	 * parameters and will throw a {@link JWTException} in such an encounter. In
	 * case of duplicate params, the older value will be replaced by the new
	 * value.
	 * 
	 * @param headerParamName
	 * @param headeParamValue
	 * @return {@link JWTBuilder}
	 * @throws JWTException
	 */
	public JWTBuilder setHeaderParam(String headerParamName, String headeParamValue)
	                                                                                throws JWTException {
		if (headeParamValue == null || headeParamValue.equals("")) {
			throw new JWTException("Empty JWT header parameters NOT allowed");
		}
		headerParams.put(headerParamName, headeParamValue);
		return this;
	}

	/**
	 * This method is set to add header parameters to the JWT header.
	 * 
	 * @param headerParams
	 * @return
	 */
	public JWTBuilder setHeaderParams(Map<String, Object> headerParams) {
		this.headerParams = headerParams;
		return this;
	}

	/**
	 * This method is used to add claims to the JWT. In case of duplicate
	 * claims, the older value will be replaced by the new value. Empty claim
	 * values are not allowed.
	 * 
	 * @param payloadParam
	 * @return
	 * @throws JWTException
	 */
	public JWTBuilder setClaim(String claimName, String claimvalue) throws JWTException {
		if (claimvalue == null || claimvalue.equals("")) {
			throw new JWTException("Empty JWT claims NOT allowed");
		}
		payloadClaims.put(claimName, claimvalue);
		return this;
	}

	/**
	 * This method is used to set claim values to the JWT
	 * 
	 * @param claims
	 * @return
	 */
	public JWTBuilder setClaims(Map<String, Object> claims) {
		payloadClaims = claims;
		return this;
	}

	/**
	 * Sign the JWT headerParams and payloadClaims with the key provided using
	 * the signature Algorithm provided.
	 * 
	 * @param sigKey
	 * @param sigAlg
	 * @return {@linkSignedJWTBuilder}
	 */
	public JWTBuilder signJWT(Key sigKey, String sigAlg) {
		this.sigKey = sigKey;
		this.sigAlg = sigAlg;
		return this;
	}

	/**
	 * Encrypt the JWT headerParams and payloadClaims with the key provided
	 * using
	 * the encryption Algorithm provided
	 * 
	 * @param encKey
	 * @param encAlg
	 * @return {@linkSignedJWTBuilder}
	 */
	public JWTBuilder encryptJWT(Key encKey, String encAlg) {
		this.encKey = encKey;
		this.encAlg = encAlg;
		return this;
	}

	public JWTBuilder doSignAndEnctypt(boolean signAndEncrypt) {
		this.isSignAndEncrypt = signAndEncrypt;
		return this;
	}

	/**
	 * This method returns the completed JWT which is the concatenation of the
	 * base 64 encoded header JSON, payload JSON and signature with the period
	 * (".") between them.
	 * 
	 * @return
	 * @throws JWTException
	 */
	public String buildJWT() throws JWTException {
		buildJWTHeader();
		buildJWTPayload();
		return concatenateParts();
	}

	/**
	 * This method builds the JWT payload. The JWT payload is a JSON with
	 * claims. The payload cannot be NULL. For null payloads the Builder throws
	 * a {@linkJWTException}.
	 * 
	 * @throws JWTException
	 */
	private void buildJWTPayload() throws JWTException {
		if (!payloadClaims.isEmpty()) {
			try {
				payloadJson = JSONUtils.buildJSON(payloadClaims);
				encodedPayload = JWTUtil.encodeJSON(payloadJson);
				if (debug) {
					log.debug("JWT payload :" + payloadJson);
					log.debug("Encoded JWT payload" + encodedPayload);
				}
			} catch (JSONException e) {
				log.debug(e);
				throw new JWTException("Error while building JWTPayload", e);
			}
		} else {
			throw new JWTException("JWT Payload cannot be NULL");
		}
	}

	/**
	 * This method builds the JWT Header. JWT must have a header and the 'alg'
	 * parameter must be in the HWT Header. This method throws a
	 * {@linkJWTException} if the JWT Header doesn't meet those requirements.
	 * 
	 * @throws JWTException
	 */
	private void buildJWTHeader() throws JWTException {
		// The alg parameter MUST have a value
		if (sigAlg == null && !headerParams.containsKey(JWT.HeaderParam.ALGORITHM)) {
			log.warn("No signature algorithm defined. Building a plain-text JWT");
			headerParams.put(JWT.HeaderParam.ALGORITHM, JWT.HeaderParamValue.ALG_NONE);
		}
		// The type parameter MUST have the value JWT
		if (!headerParams.containsKey(JWT.HeaderParam.TYPE)) {
			headerParams.put(JWT.HeaderParam.TYPE, JWT.HeaderParamValue.TYPE_JWT);
		} else if (headerParams.get(JWT.HeaderParam.CONTENT_TYPE) != JWT.HeaderParamValue.TYPE_JWT) {
			headerParams.put(JWT.HeaderParam.CONTENT_TYPE, JWT.HeaderParamValue.TYPE_JWT);
		}
		try {
			headerJson = JSONUtils.buildJSON(headerParams);
			encodedHeader = JWTUtil.encodeJSON(headerJson);
			if (debug) {
				log.debug("JWT header :" + headerJson);
				log.debug("Encoded JWT header" + encodedHeader);
			}
		} catch (JSONException e) {
			log.debug(e);
			throw new JWTException("Error while building JWTHeader", e);
		}
	}

	/**
	 * This method concatenates the headerParams, payloadClaims and signature
	 * according to the JWT specification.
	 * 
	 * @return
	 */
	private String concatenateParts() {
		StringBuilder jwt = new StringBuilder();
		jwt.append(encodedHeader + ".");
		jwt.append(encodedPayload + ".");
		jwt.append(encodedSignature);
		return jwt.toString();
	}
}
