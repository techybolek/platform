/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.amber.oauth2.jwt;

import org.apache.oltu.oauth2.jwt.JWTException;
import org.apache.oltu.oauth2.jwt.JWTProcessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import junit.framework.Assert;

/**
 * This class contains test cases for {@linkJWTProcessor}
 * 
 */
public class JWTProcessorTest extends Assert {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNullJWT() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Not a valid JWT");
        String faultyJWT = null;
        JWTProcessor processor = new JWTProcessor();
        processor.process(faultyJWT);
    }

    @Test
    public void testEmptyJWT() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Not a valid JWT");
        String faultyJWT = "";
        JWTProcessor processor = new JWTProcessor();
        processor.process(faultyJWT);
    }

    @Test
    public void testNoPeriodJWT() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Not a valid JWT");
        String faultyJWT = "eyJhbGciOiJub25lIiwiTXlDdXN0b21IZWFkZXIiOZhbHVlIiwidHlwIjoiSldUIn0=";
        JWTProcessor processor = new JWTProcessor();
        processor.process(faultyJWT);
    }

    @Test
    public void testLeadingPeriodJWT() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Not a valid JWT");
        String faultyJWT = ".eyJhbGciOiJub25lIiwiTXlDdXN0b21IZWFkZXIiOZhbHVlIiwidHlwIjoiSldUIn0=";
        JWTProcessor processor = new JWTProcessor();
        processor.process(faultyJWT);
    }

    @Test
    public void testTrailingPeriodJWT() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Not a valid JWT");
        String faultyJWT = "eyJhbGciOiJub25lIiwiTXlDdXN0b21IZWFkZXIiOZhbHVlIiwidHlwIjoiSldUIn0=.";
        JWTProcessor processor = new JWTProcessor();
        processor.process(faultyJWT);
    }

    @Test
    public void testThreePeriodJWT() throws JWTException {
        thrown.expect(JWTException.class);
        thrown.expectMessage("Not a valid JWT");
        String faultyJWT = "VlIiwidHlwIjoiSldUIn0=.VlIiwidHlwIjoiSldUIn0=.VlIiwidHlwIjoiSldUIn0=.VlIiwidH";
        JWTProcessor processor = new JWTProcessor();
        processor.process(faultyJWT);
    }

}
