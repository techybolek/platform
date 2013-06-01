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
package org.wso2.carbon.identity.sso.saml.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.sso.saml.stub.IdentityException;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOServiceStub;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSORespDTO;

import java.rmi.RemoteException;

public class SAMLSSOServiceClient {

    private static Log log = LogFactory.getLog(SAMLSSOServiceClient.class);
    private IdentitySAMLSSOServiceStub stub;
    private static Integer sessionTimeout = null;
    private static Boolean isOpenIDLoginAccepted = null;
    private static Boolean isSAMLSSOLoginAccepted = null;
    /**
     * 
     * @param backendServerURL
     * @param configCtx
     * @throws AxisFault
     */
    public SAMLSSOServiceClient(String backendServerURL, ConfigurationContext configCtx) {
        String serviceURL = backendServerURL + "IdentitySAMLSSOService";
        try {
            stub = new IdentitySAMLSSOServiceStub(configCtx, serviceURL);
        } catch (AxisFault axisFault) {
            log.error("Error while instantiating IdentitySAMLSSOServiceStub", axisFault);
        }
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        try {
            if(sessionTimeout == null){
                sessionTimeout = stub.getSSOSessionTimeout();
            }
            if(isOpenIDLoginAccepted == null){
                isOpenIDLoginAccepted = stub.isOpenIDLoginAccepted();
            }
            if(isSAMLSSOLoginAccepted == null){
                isSAMLSSOLoginAccepted = stub.isSAMLSSOLoginAccepted();
            }
        } catch (RemoteException e) {
            log.error("Error while reading configurations from identity.xml", e);
        }
    }

	/**
	 * 
	 * @param authnRequest
	 * @param sessionId
	 * @param rpSessionId
	 * @param authnMode
	 * @return
	 * @throws IdentityException
	 */
	public SAMLSSOReqValidationResponseDTO validate(String samlReq, String queryString,
	                                                String sessionId, String rpSessionId,
	                                                String authnMode) throws IdentityException {
		try {
			return stub.validateRequest(samlReq, queryString, sessionId, rpSessionId, authnMode);
		} catch (Exception e) {
			log.error("Error validating the Authentication Request", e);
			throw new IdentityException("Error in parsing authentication request", e);
		}
	}

	/**
	 * 
	 * @param authnReqDTO
	 * @param sessionId
	 * @return
	 * @throws IdentityException
	 */
	public SAMLSSORespDTO authenticate(SAMLSSOAuthnReqDTO authnReqDTO, String sessionId)
	                                                                                    throws IdentityException {
		try {
			return stub.authenticate(authnReqDTO, sessionId);
		} catch (Exception e) {
			log.error("Error authenticating the user.", e);
			throw new IdentityException("Authentication Failure", e);
		}
	}

	/**
	 * 
	 * @param sessionId
	 * @return
	 * @throws IdentityException
	 */
	public SAMLSSOReqValidationResponseDTO doSingleLogout(String sessionId)
	                                                                       throws IdentityException {
		try {
			return stub.doSingleLogout(sessionId);
		} catch (Exception ex) {
			log.error("Error performing single logout.", ex);
			throw new IdentityException("Error performing Single Logout", ex);
		}
	}

    /**
     * Gets the SSO_SESSION_EXPIRE time for the SSO provider
     * @return SSO session timeout value
     * @throws IdentityException
     */
    public int getSSOSessionTimeout(){
        return sessionTimeout;
    }

    public boolean isOpenIDLoginAccepted(){
        return isOpenIDLoginAccepted;
    }

    public boolean isSAMLSSOLoginAccepted(){
        return isSAMLSSOLoginAccepted;
    }
}