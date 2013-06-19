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
package org.wso2.carbon.identity.scim.provider.auth;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.charon.core.schema.SCIMConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class OAuthHandler implements SCIMAuthenticationHandler {

    private static Log log = LogFactory.getLog(BasicAuthHandler.class);

    private String remoteServiceURL = "https://localhost:9443/services";
    private int priority = 5;
    private String userName = "admin";
    private String password = "admin";

    private final String BEARER_AUTH_HEADER = "Bearer";

    //Ideally this should be configurable. For the moment, hard code the priority.
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean canHandle(Message message, ClassResourceInfo classResourceInfo) {
        //check the "Authorization" header and if "Bearer" is there, can be handled.

        //get the map of protocol headers
        TreeMap protocolHeaders = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        //get the value for Authorization Header
        ArrayList authzHeaders = (ArrayList) protocolHeaders.get(SCIMConstants.AUTHORIZATION_HEADER);
        if (authzHeaders != null) {
            //get the authorization header value, if provided
            String authzHeader = (String) authzHeaders.get(0);
            if (authzHeader != null && authzHeader.contains(BEARER_AUTH_HEADER)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAuthenticated(Message message, ClassResourceInfo classResourceInfo) {
        //get the map of protocol headers
        TreeMap protocolHeaders = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        //get the value for Authorization Header
        ArrayList authzHeaders = (ArrayList) protocolHeaders.get(SCIMConstants.AUTHORIZATION_HEADER);
        if (authzHeaders != null) {
            //get the authorization header value, if provided
            String authzHeader = (String) authzHeaders.get(0);

            //extract access token
            String accessToken = authzHeader.substring(7).trim();
            //validate access token
            try {
                OAuth2TokenValidationResponseDTO validationResponse = this.validateAccessToken(accessToken);
                if (validationResponse != null) {
                    if (validationResponse.getValid()) {
                        String userName = validationResponse.getAuthorizedUser();
                        authzHeaders.set(0, userName);
                        return true;
                    }
                }
            } catch (Exception e) {
                String error = "Error in validating OAuth access token.";
                log.error(error, e);
            }
        }
        return false;
    }

    private String getOAuthAuthzServerURL() {
        if (remoteServiceURL != null) {
            if (!remoteServiceURL.endsWith("/")) {
                remoteServiceURL += "/";
            }
        }
        return remoteServiceURL;
    }

    private OAuth2TokenValidationResponseDTO validateAccessToken(String accessToken)
            throws Exception {
        String carbonHome = CarbonUtils.getCarbonHome();
        String axis2ClientRepo = carbonHome + File.separator + "repository" + File.separator +
                                 "deployment" + File.separator + "client";
        try {
            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            OAuthServiceClient oauthClient = new OAuthServiceClient(getOAuthAuthzServerURL(),
                                                                    userName, password, configContext);
            OAuth2TokenValidationResponseDTO validationResponse = oauthClient.validateAccessToken(accessToken);
            return validationResponse;
        } catch (AxisFault axisFault) {
            throw axisFault;
        } catch (Exception exception) {
            throw exception;
        }
    }
}

