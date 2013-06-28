/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.interceptor.valve;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.interceptor.valve.internal.APIManagerInterceptorComponent;
import org.wso2.carbon.apimgt.interceptor.valve.internal.DataHolder;
import org.wso2.carbon.apimgt.core.authenticate.APITokenValidator;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class APIManagerInterceptorValve implements CarbonTomcatValve {
	
	private static final Log log = LogFactory.getLog(APIManagerInterceptorValve.class);
	
	APIKeyValidationInfoDTO apiKeyValidationDTO;
	
	public void invoke(HttpServletRequest request, HttpServletResponse response) {
        String context = request.getContextPath();
        String externalAPIManagerURL = APIManagerInterceptorComponent.getExternalAPIManagerURL();
        String manageAPIs = APIManagerInterceptorComponent.getAPIManagementEnabled();

        if (manageAPIs.equals("true") && !ApiMgtDAO.isContextExist(context)) {
                return;
        }
        if (manageAPIs.equals("true") && externalAPIManagerURL == null) {  //use internal api management
            log.info("API Manager Interceptor Valve Got invoked!!");
            String bearerToken = request.getHeader(APIConstants.AuthParameter.AUTH_PARAM_NAME);
            String accessToken = null;
            if (bearerToken != null) {
                String[] token = bearerToken.split("Bearer");
                if (token.length > 0 && token[1] != null) {
                    accessToken = token[1].trim();
                }
            }
            boolean isAuthorized = false;
            try {
                /*
                TODO:
                API Version is hardcoded as 1.0.0 since this need to be test with GReg rest API and currently it don't have the version support.
                we can change this to get the version as  getAPIVersion(request) later.
                 */
                isAuthorized = doAuthenticate(context, "1.0.0", accessToken, request.getAuthType(),
                        request.getHeader(APITokenValidator.getAPIManagerClientDomainHeader()));
            } catch (APIManagementException e) {
                //ignore
            }
            if (isAuthorized) {
                if (log.isDebugEnabled()) {
                    log.debug("Authorized..");
                }
            } else {
                try {
                    response.sendError(403, "Unauthorized");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!doThrottle(request,accessToken)) {
	        	try {
					response.sendError(405, "Message Throttled Out You have exceeded your quota");
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
            publishStatistics();
        } else if (APIManagerInterceptorComponent.getAPIManagementEnabled().equals("true") &&
                APIManagerInterceptorComponent.getExternalAPIManagerURL() != null) { //user external api-manager for api management
            //TODO
        }
    }

	private boolean doAuthenticate(String context, String version, String accessToken, String requiredAuthenticationLevel, String clientDomain)
            throws APIManagementException {
            APITokenValidator tokenValidator = new APITokenValidator();
            apiKeyValidationDTO = tokenValidator.validateKey(context, version,accessToken, APIConstants.AUTH_APPLICATION_LEVEL_TOKEN,
                    clientDomain);
            return apiKeyValidationDTO.isAuthorized();
	}
	
	private boolean doThrottle(HttpServletRequest request, String accessToken) {
				
		String apiName = request.getContextPath();
		String apiVersion = getAPIVersion(request);
		String apiIdentifier = apiName + "-" + apiVersion;
		
		APIThrottleHandler throttleHandler = null;
		ConfigurationContext cc = DataHolder.getServerConfigContext();
		
		if (cc.getProperty(apiIdentifier) == null) {
			throttleHandler = new APIThrottleHandler();
			/* Add the Throttle handler to ConfigContext against API Identifier */
			cc.setProperty(apiIdentifier, throttleHandler);
		} else {
			throttleHandler = (APIThrottleHandler) cc.getProperty(apiIdentifier);
		}
		return throttleHandler.doThrottle(request, apiKeyValidationDTO, accessToken);
		
	}
	
	private boolean publishStatistics() {
		return true;
	}
	
	private String getAPIVersion(HttpServletRequest request) {
		int contextStartsIndex = (request.getRequestURI()).indexOf(request.getContextPath());
		int length = request.getContextPath().length();
		String afterContext = (request.getRequestURI()).substring(contextStartsIndex + length);
		int SlashIndex = afterContext.indexOf(("/"));
		
		return afterContext.substring(SlashIndex + 1);
		
	}
	
}
