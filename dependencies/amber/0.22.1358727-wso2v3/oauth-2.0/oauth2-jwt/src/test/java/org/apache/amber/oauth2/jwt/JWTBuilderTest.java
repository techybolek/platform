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

import junit.framework.Assert;

import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.JWTBuilder;
import org.apache.oltu.oauth2.jwt.JWTException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * This class contains test cases for {@linkJWTBuilder}.
 * 
 */
public class JWTBuilderTest extends Assert {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNullHeaderParam() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Empty JWT header parameters NOT allowed");
        JWTBuilder builder = new JWTBuilder();
        builder.setHeaderParam("custom", null);
    }

    @Test
    public void testEmptyHeaderParam() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Empty JWT header parameters NOT allowed");
        JWTBuilder builder = new JWTBuilder();
        builder.setHeaderParam("custom", "");
    }


    public void testNoAlgoHeaderParam() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("The 'alg' parameter MUST be in the JWT Header");
        JWTBuilder builder = new JWTBuilder();
        builder.setHeaderParam("customParam", "customValue");
        builder.buildJWT();
    }
    
    @Test
    public void testEmptyClaimValue() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Empty JWT claims NOT allowed");
        JWTBuilder builder = new JWTBuilder();
        builder.setHeaderParam(JWT.HeaderParam.ALGORITHM, JWT.HeaderParamValue.ALG_NONE);
        builder.setClaim("customParam", "");
        builder.buildJWT();
    }
    
    @Test
    public void testNullClaimValue() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Empty JWT claims NOT allowed");
        JWTBuilder builder = new JWTBuilder();
        builder.setHeaderParam(JWT.HeaderParam.ALGORITHM, JWT.HeaderParamValue.ALG_NONE);
        builder.setClaim("customParam", null);
        builder.buildJWT();
    }
    
}
