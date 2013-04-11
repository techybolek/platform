/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.apache.oltu.oauth2.jwt;

import java.security.Key;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * This class represents the JWT processor. The processor takes a JWT string as
 * an input and provides various methods to retrieve header parameters, payload
 * claims and signature from the JWT.
 * 
 */
public class JWTProcessor {

    private static Log log = LogFactory.getLog(JWTProcessor.class);
    private static boolean debug = log.isDebugEnabled();

    private String jwt = null;
    private String jwtHeader = null;
    private String jwtPayload = null;
    private String jwtSignature = null;
    private Key sigKey = null;
    private Key encKey = null;
    private JSONObject headerJSON = null;
    private JSONObject payloadJSON = null;
    private JSONObject signaturePayload = null;
    private Map<String, Object> headerParams = null;
    private Map<String, Object> payloadClaims = null;

    /**
     * Constructor for plain text JWTs
     */
    public JWTProcessor() {

    }

    /**
     * Constructor for encrypted or/and signed JWTs
     * 
     * @param sigKey
     * @param encKey
     */
    public JWTProcessor(Key sigKey, Key encKey) {
        this.sigKey = sigKey;
        this.encKey = encKey;
    }

    /**
     * Processor starts processing the JWT. Creating internal data structures
     * and performs various cryptographic operations on the JWT.
     * 
     * @param jwtString
     * @return
     * @throws JWTException
     */
    public JWTProcessor process(String jwtString) throws JWTException {
        jwt = jwtString;
        if (debug) {
            log.debug("Received JWT " + jwtString);
        }
        splitJWT();
        processJWTHeader();
        processJWTPayload();
        return this;
    }

    /**
     * Returns the JWT header plain text JSON object
     * 
     * @return
     */
    public JSONObject getJWTHeaderJSON() {
        return headerJSON;
    }

    /**
     * Returns the JWT payload JSON object
     * 
     * @return
     */
    public JSONObject getJWTPayloadJSON() {
        return payloadJSON;
    }

    public JSONObject getJWTSignatureJSON() {
        return signaturePayload;
    }

    /**
     * Returns header parameter and their values in a Map
     * 
     * @return
     */
    public Map<String, Object> getHeaderParams() {
        return headerParams;
    }

    /**
     * Returns payload claims and their values in a Map
     * 
     * @return
     */
    public Map<String, Object> getPayloadClaims() {
        return payloadClaims;
    }

    /**
     * Returns the header parameter value for the given parameter name.
     * 
     * @param parameter
     * @return
     */
    public Object getHeaderParameterValue(String parameter) {
        return headerParams.get(parameter);
    }

    /**
     * Returns payload claim value for the given claim uri.
     * 
     * @param claimUri
     * @return
     */
    public Object getPayloadClaimValue(String claimUri) {
        return payloadClaims.get(claimUri);
    }

    /**
     * Split the JWT into header, payload and signature JWTException
     * 
     * @throws JWTException
     * 
     */
    private void splitJWT() throws JWTException {
        checkJWTPeriodsValidity(jwt);
        String[] parts = jwt.split("\\.");
        if(parts.length ==1) {
            jwtHeader = parts[0];
        } else if (parts.length >= 2) {
            jwtHeader = parts[0];
            jwtPayload = parts[1];
        } else if (parts.length == 3) {
            jwtHeader = parts[0];
            jwtPayload = parts[1];
            jwtSignature = parts[2];
        } else {
            throw new JWTException("Not a valid JWT");
        }
    }

    /**
     * Processes the JWT header. Builds the JSON object and parameter map.
     * 
     * @throws JWTException
     */
    private void processJWTHeader() throws JWTException {
        try {
            String decodedHeader = JWTUtil.decodeJSON(jwtHeader);
            headerJSON = new JSONObject(decodedHeader);
            headerParams = JWTUtil.parseJSON(decodedHeader);
        } catch (JSONException e) {
            log.debug(e);
            throw new JWTException("Error while processing JWT header");
        }
    }

    /**
     * Processes the JWT payload. Builds the JSON object and claims map.
     * 
     * @throws JWTException
     */
    private void processJWTPayload() throws JWTException {
        try {
            String decodedPayload = JWTUtil.decodeJSON(jwtPayload);
            payloadJSON = new JSONObject(decodedPayload);
            payloadClaims = JWTUtil.parseJSON(decodedPayload);
        } catch (JSONException e) {
            log.debug(e);
            throw new JWTException("Error while processing JWT header");
        }
    }
    
    /**
     * Checks for the periods validity in the JWT
     * 
     * @param jwt
     * @throws JWTException
     */
    private void checkJWTPeriodsValidity(String jwt) throws JWTException {
        if (jwt == null || jwt.equals("") || jwt.startsWith(".")) {
            throw new JWTException("Not a valid JWT");
        }
        int dotCount = 0;
        int idx = 0;
        while ((idx = jwt.indexOf(".", idx)) != -1) {
            dotCount++;
            idx++;
        }
        if (dotCount != 2)
            throw new JWTException("Not a valid JWT");
    }

}
