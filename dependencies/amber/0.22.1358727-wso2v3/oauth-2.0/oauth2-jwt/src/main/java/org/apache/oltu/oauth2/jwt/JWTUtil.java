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

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.codehaus.jettison.json.JSONException;

/**
 * This class contains utility methods required for the JWT building and
 * processing.
 * 
 */
public class JWTUtil {

    private static Log log = LogFactory.getLog(JWTUtil.class);

    /**
     * UTF-8 Base64 encoding the JWT
     * 
     * @param jwtMember
     * @return
     * @throws JWTException
     */
    public static String encodeJSON(String jwtMember) throws JWTException {
        try {
            byte[] utf8json = jwtMember.getBytes("UTF-8");
            byte[] base64json = new Base64().encode(utf8json);
            return new String(base64json);
        } catch (UnsupportedEncodingException e) {
            log.debug(e);
            throw new JWTException("Error while encoding payload", e);
        }
    }

    /**
     * UTF-8 Base64 decoding of the JWT
     * 
     * @param base64jsonString
     * @return
     * @throws JWTException
     */
    public static String decodeJSON(String base64jsonString) throws JWTException {
        try {
            byte[] utf8json = base64jsonString.getBytes("UTF-8");
            byte[] decodedString = new Base64().decode(utf8json);
            return new String(decodedString);
        } catch (UnsupportedEncodingException e) {
            log.debug(e);
            throw new JWTException("Error while encoding payload", e);
        }
    }

    /**
     * Builds a Map using the JSON string. Rejects JSON objects with duplicate
     * fields.
     * 
     * @param jsonBody
     * @return
     * @throws JWTException
     * @throws JSONException
     */
    public static Map<String, Object> parseJSON(String jsonBody) throws JWTException {
        try {
            //TODO: Duplicate fields MUST be rejected- specification 
            return JSONUtils.parseJSON(jsonBody);
        } catch (JSONException e) {
            throw new JWTException("Error while parsing JWT");
        }
    }

}
