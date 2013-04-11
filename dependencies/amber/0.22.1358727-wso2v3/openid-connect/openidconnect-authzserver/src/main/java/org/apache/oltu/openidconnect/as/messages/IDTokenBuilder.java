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
package org.apache.oltu.openidconnect.as.messages;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.JWTBuilder;
import org.apache.oltu.oauth2.jwt.JWTException;

/**
 * This class represents an IDToken Builder. This IDToken Builder utilize the
 * native JWTBuilder to build the JWT.
 * 
 */
public class IDTokenBuilder {

	private static Log log = LogFactory.getLog(IDTokenBuilder.class);
	private static boolean debug = log.isDebugEnabled();

	// extensions
	private Map<String, Object> claims = new HashMap<String, Object>();
	private Map<String, Object> header = new HashMap<String, Object>();
	// Configurations
	private Key sigKey = null;
	private Key encKey = null;
	private String sigAlg = "none";
	private String encAlg = null;

	/**
	 * @param iss
	 *            the iss to set
	 * 
	 */
	public IDTokenBuilder setIssuer(String iss) {
		claims.put(IDToken.ISS, iss);
		return this;
	}

	/**
	 * @param sub
	 *            the sub to set
	 */
	public IDTokenBuilder setSubject(String sub) {
		claims.put(IDToken.SUB, sub);
		return this;
	}

	/**
	 * @param aud
	 *            the aud to set
	 */
	public IDTokenBuilder setAudience(String aud) {
		claims.put(IDToken.AUD, aud);
		return this;
	}

	/**
	 * @param exp
	 *            the exp to set
	 */
	public IDTokenBuilder setExpiration(int exp) {
		claims.put(IDToken.EXP, exp);
		return this;
	}

	/**
	 * @param iat
	 *            the iat to set
	 */
	public IDTokenBuilder setIssuedAt(int iat) {
		claims.put(IDToken.IAT, iat);
		return this;
	}

	/**
	 * @param nonce
	 *            the nonce to set
	 */
	public IDTokenBuilder setNonce(String nonce) {
		claims.put(IDToken.NONCE, nonce);
		return this;
	}

	/**
	 * @param azp
	 *            the azp to set
	 */
	public IDTokenBuilder setAuthorizedParty(String azp) {
		claims.put(IDToken.AZP, azp);
		return this;
	}

	/**
	 * @param acr
	 *            the acr to set
	 */
	public IDTokenBuilder setAuthenticationContextClassReference(String acr) {
		claims.put(IDToken.ACR, acr);
		return this;
	}

	/**
	 * @param auth_time
	 *            the auth_time to set
	 */
	public IDTokenBuilder setAuthTime(String authTime) {
		claims.put(IDToken.AUTH_TIME, authTime);
		return this;
	}

	/**
	 * @param at_hash
	 *            the at_hash to set
	 */
	public IDTokenBuilder setAtHash(String at_hash) {
		claims.put(IDToken.AT_HASH, at_hash);
		return this;
	}

	/**
	 * @param c_hash
	 *            the c_hash to set
	 */
	public IDTokenBuilder setCHash(String c_hash) {
		claims.put(IDToken.C_HASH, c_hash);
		return this;
	}

	/**
	 * Use this method to set custom claims
	 * 
	 * @param claimKey
	 * @param claimValue
	 * @return
	 */
	public IDTokenBuilder setClaim(String claimKey, String claimValue) {
		if (claimKey == null || claimValue == null) {
			log.error("Key or Value cannot be null");
		}
		claims.put(claimKey, claimValue);
		return this;
	}

	/**
	 * Use this method to set custom JWT headers
	 */
	public IDTokenBuilder setHeaderParam(String key, String value) {
		if (key == null || value == null) {
			log.error("Key or Value cannot be null");
		}
		header.put(key, value);
		return this;
	}

	/**
	 * @param sigKey
	 *            the sigKey to set
	 * @param sigAlg
	 *            TODO
	 */
	public IDTokenBuilder setSigKey(Key sigKey, String sigAlg) {
		this.sigKey = sigKey;
		this.sigAlg = sigAlg;
		return this;
	}

	/**
	 * @param encKey
	 *            the encKey to set
	 * @param encAlg
	 *            TODO
	 */
	public IDTokenBuilder setEncKey(Key encKey, String encAlg) {
		this.encKey = encKey;
		this.encAlg = encAlg;
		return this;
	}

	/**
	 * 
	 * @return
	 * @throws IDTokenException
	 */
	public String buildIDToken() throws IDTokenException {
		checkSpecCompliance();
		// setting algorithm parameter
		header.put(JWT.HeaderParam.ALGORITHM, sigAlg);

		try {
			return new JWTBuilder().setClaims(claims).setHeaderParams(header)
			                       .signJWT(sigKey, sigAlg).encryptJWT(encKey, encAlg).buildJWT();
		} catch (JWTException e) {
			throw new IDTokenException("Error while building IDToken", e);
		}
	}

	/**
	 * Check for spec compliance
	 * 
	 * @throws IDTokenException
	 */
	private void checkSpecCompliance() throws IDTokenException {
		if (debug) {
			if (claims.get(IDToken.ISS) == null) {
				log.error("iss claim not set");
			}
			if (claims.get(IDToken.SUB) == null) {
				log.error("sub claim not set");
			}
			if (claims.get(IDToken.AUD) == null) {
				log.error("aud claim not set");
			}
			if (claims.get(IDToken.EXP) == null) {
				log.error("exp claim not set");
			}
			if (claims.get(IDToken.IAT) == null) {
				log.error("iat claim not set");
			}
		}
		if (claims.get(IDToken.ISS) == null || claims.get(IDToken.SUB) == null ||
		    claims.get(IDToken.AUD) == null || claims.get(IDToken.EXP) == null ||
		    claims.get(IDToken.IAT) == null) {
			throw new IDTokenException("One or more required claims missing");
		}
	}

}
