/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.scim.sample.utils;

import org.apache.axiom.om.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utils class for SCIM Samples related operations.
 */
public class SCIMSamplesUtils {

    public static final String CONTENT_TYPE = "application/json";

    public static final String IS_HOME = ".." + File.separator + ".." + File.separator;

    public static final String TRUST_STORE_PATH = IS_HOME + "repository" + File.separator + "resources" +
                                                  File.separator + "security" + File.separator + "wso2carbon.jks";

    public static final String TRUST_STORE_PASS = "wso2carbon";

    /*to be read from properties file*/
    public static final String USER_ENDPOINT_URL = "";
    

    public static void setKeyStore() {
        System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASS);
    }

    public static String getBase64EncodedBasicAuthHeader(String userName, String password) {
        String concatenatedCredential = userName + ":" + password;
        byte[] byteValue = concatenatedCredential.getBytes();
        String encodedAuthHeader = Base64.encode(byteValue);
        encodedAuthHeader = "Basic " + encodedAuthHeader;
        return encodedAuthHeader;
    }

    /*private static void loadConfiguration() throws IOException {
        Properties properties = new Properties();
		FileInputStream freader = new FileInputStream(RemoteUMSampleConstants.PROPERTIES_FILE_NAME);
		properties.load(freader);

        serverUrl = properties.getProperty(RemoteUMSampleConstants.REMOTE_SERVER_URL);
        username = properties.getProperty(RemoteUMSampleConstants.USER_NAME);
        password = properties.getProperty(RemoteUMSampleConstants.PASSWORD);
    }*/
}
