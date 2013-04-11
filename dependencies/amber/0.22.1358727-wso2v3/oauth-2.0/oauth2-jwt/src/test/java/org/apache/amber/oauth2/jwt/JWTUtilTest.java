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

import org.apache.oltu.oauth2.jwt.JWTException;
import org.apache.oltu.oauth2.jwt.JWTProcessor;
import org.apache.oltu.oauth2.jwt.JWTUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * This class contains test cases for {@linkJWTUtill}
 * 
 */
public class JWTUtilTest extends Assert {

    /**
     * Tests the encoding and decoding methods of the JWTUtil
     * @throws JWTException 
     */
    @Test
    public void testEncodeDecode() throws JWTException {
        StringBuilder jwtBuilder = new StringBuilder();
        jwtBuilder.append("{");
        jwtBuilder.append("\"iss\":\"");
        jwtBuilder.append("http://example.com");
        jwtBuilder.append("\",");
        jwtBuilder.append("\"");
        jwtBuilder.append("enduser\":\"");
        jwtBuilder.append("bob");
        jwtBuilder.append("\"");
        jwtBuilder.append("}");
        
        String jwt = jwtBuilder.toString();
        String encodedString = JWTUtil.encodeJSON(jwt);
        String decodedString = JWTUtil.decodeJSON(encodedString);
        
        assertEquals(jwt, decodedString);

    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    /**
     * This test is skipped for now. Because the underlaying JSON implementation 
     * does not support this. We should reject JWTs we receive with duplicate
     * fileds.
    @Test
    public void testDuplicateFieldsJWT() throws JWTException {
        StringBuilder jwtBuilder = new StringBuilder();
        jwtBuilder.append("{");
        jwtBuilder.append("\"iss\":\"");
        jwtBuilder.append("http://example.com");
        jwtBuilder.append("\",");
        jwtBuilder.append("\"");
        jwtBuilder.append("iss\":\"");
        jwtBuilder.append("bob");
        jwtBuilder.append("\"");
        jwtBuilder.append("}");
        String invalidJWT = jwtBuilder.toString();
        
        thrown.expect(JWTException.class);
        thrown.expectMessage("Dupilcate field found " + "iss");
        
        JWTUtil.parseJSON(invalidJWT);
    }
    */
}
