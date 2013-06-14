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
package org.wso2.carbon.identity.scim.provider.filter;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.scim.provider.util.JAXRSResponseBuilder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.charon.core.encoder.json.JSONEncoder;
import org.wso2.charon.core.exceptions.InternalServerException;
import org.wso2.charon.core.exceptions.UnauthorizedException;
import org.wso2.charon.core.protocol.endpoints.AbstractResourceEndpoint;
import org.wso2.charon.core.schema.SCIMConstants;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.TreeMap;

public class AuthenticationFilter implements RequestHandler {

    private static Log log = LogFactory.getLog(AuthenticationFilter.class);

    public Response handleRequest(Message message, ClassResourceInfo classResourceInfo) {
        if (log.isDebugEnabled()) {
            log.debug("Authenticating SCIM request..");
        }
        //get the map of protocol headers
        TreeMap protocolHeaders = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        //get the value for Authorization Header
        ArrayList authzHeaders = (ArrayList) protocolHeaders.get(SCIMConstants.AUTHORIZATION_HEADER);
        if (authzHeaders != null) {
            //get the authorization header value, if provided
            String authzHeader = (String) authzHeaders.get(0);
            //currently, handle, basic auth in the filter itself.
            //Plan is to add pluggable authenticators here, with default ones being basic auth and oauth

            byte[] decodedAuthHeader = Base64.decode(authzHeader.split(" ")[1]);
            String authHeader = new String(decodedAuthHeader);
            String userName = authHeader.split(":")[0];
            String password = authHeader.split(":")[1];
            if (userName != null && password != null) {
                String tenantLessUserName = null;
                String tenantDomain = MultitenantUtils.getTenantDomain(userName);
                //TODO: use util method to get tenantless username
                String[] userNameArray = userName.split("@");
                if (userNameArray.length > 1) {
                    tenantLessUserName = userNameArray[0];
                } else {
                    tenantLessUserName = userName;
                }

                try {
                    //get super tenant context and get realm service which is an osgi service
                    RealmService realmService = (RealmService)
                            PrivilegedCarbonContext.getCurrentContext().getOSGiService(RealmService.class);
                    if (realmService != null) {
                        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                        //get tenant's user realm
                        UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                        boolean authenticated = userRealm.getUserStoreManager().authenticate(tenantLessUserName, password);
                        if (authenticated) {
                            //authentication success. set the username for authorization header and proceed the REST call
                            authzHeaders.set(0, userName);
                            return null;
                        } else {
                            UnauthorizedException unauthorizedException = new UnauthorizedException(
                                    "Authentication required for this resource.");
                            return new JAXRSResponseBuilder().buildResponse(
                                    AbstractResourceEndpoint.encodeSCIMException(new JSONEncoder(), unauthorizedException));
                        }
                    } else {
                        log.error("Error in getting Realm Service for user: " + userName);
                        InternalServerException internalServerException = new InternalServerException(
                                "Internal server error while authenticating the user.");
                        return new JAXRSResponseBuilder().buildResponse(
                                AbstractResourceEndpoint.encodeSCIMException(new JSONEncoder(), internalServerException));
                    }

                } catch (UserStoreException e) {
                    InternalServerException internalServerException = new InternalServerException(
                            "Internal server error while authenticating the user.");
                    return new JAXRSResponseBuilder().buildResponse(
                            AbstractResourceEndpoint.encodeSCIMException(new JSONEncoder(), internalServerException));
                }
            } else {
                UnauthorizedException unauthorizedException = new UnauthorizedException(
                        "Authentication required for this resource.");
                return new JAXRSResponseBuilder().buildResponse(
                        AbstractResourceEndpoint.encodeSCIMException(new JSONEncoder(), unauthorizedException));
            }
        } else {
            UnauthorizedException unauthorizedException = new UnauthorizedException(
                    "Authentication required for this resource.");
            return new JAXRSResponseBuilder().buildResponse(
                    AbstractResourceEndpoint.encodeSCIMException(new JSONEncoder(), unauthorizedException));
        }
    }
}
