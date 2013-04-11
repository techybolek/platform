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
package org.apache.amber.oauth2.jwt;

import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.JWTBuilder;
import org.apache.oltu.oauth2.jwt.JWTException;
import org.apache.oltu.oauth2.jwt.JWTProcessor;
import org.junit.Test;

import junit.framework.Assert;

/**
 * This class contains test cases for the JWT end to end scenarios class
 */
public class JWTTest extends Assert {

    /**
     * This test covers the aspects of building plain text JWT
     * 
     * @throws JWTException
     */
    @Test
    public void testPlainTextJWT() throws JWTException {

        String customHeaderName = "MyCustomHeader";
        String customheaderValue = "MyCustomHeaderValue";
        String issuerUri = "http://example1.com";
        String audienceUri = "http://example2.org";
        String subject = "Bob";
        String customClaimUri = "http://example1.com/email";
        String customClaimValue = "bob@example1.com";

        JWTBuilder jwtBuilder = new JWTBuilder();
        // building JWT header JSON
        jwtBuilder.setHeaderParam(JWT.HeaderParam.ALGORITHM, JWT.HeaderParamValue.ALG_NONE)
                .setHeaderParam(customHeaderName, customheaderValue);
        // building JWT payload JSON
        jwtBuilder.setClaim(JWT.ReservedClaim.ISSUER, issuerUri)
                .setClaim(JWT.ReservedClaim.AUDIENCE, audienceUri)
                .setClaim(JWT.ReservedClaim.SUBJECT, subject)
                .setClaim(customClaimUri, customClaimValue);
        // building the JWT
        String jwt = jwtBuilder.buildJWT();

        // processing
        JWTProcessor jwtProcessor = new JWTProcessor();
        jwtProcessor.process(jwt);
        // reading JWT header
        String headerParamAlg = (String) jwtProcessor
                .getHeaderParameterValue(JWT.HeaderParam.ALGORITHM);
        String headerParamCustom = (String) jwtProcessor.getHeaderParameterValue(customHeaderName);
        // reading JWT payload
        String claimIssuer = (String) jwtProcessor.getPayloadClaimValue(JWT.ReservedClaim.ISSUER);
        String claimAudience = (String) jwtProcessor
                .getPayloadClaimValue(JWT.ReservedClaim.AUDIENCE);
        String claimSubject = (String) jwtProcessor.getPayloadClaimValue(JWT.ReservedClaim.SUBJECT);
        String claimCustom = (String) jwtProcessor.getPayloadClaimValue(customClaimUri);

        // asserting
        assertEquals(JWT.HeaderParamValue.ALG_NONE, headerParamAlg);
        assertEquals(customheaderValue, headerParamCustom);
        assertEquals(issuerUri, claimIssuer);
        assertEquals(audienceUri, claimAudience);
        assertEquals(subject, claimSubject);
        assertEquals(customClaimValue, claimCustom);
    }
}
