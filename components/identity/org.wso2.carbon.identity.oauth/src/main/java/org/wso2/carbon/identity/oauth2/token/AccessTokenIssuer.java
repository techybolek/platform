/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.oauth2.token;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jsr107cache.Cache;

import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.util.OIDCAuthzServerUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.handlers.clientauth.BasicAuthClientAuthentcationHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.clientauth.ClientAuthenticationHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.clientauth.SAM2BearerClientAuthenticationHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationCodeHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationGrantHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.ClientCredentialsGrantHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.PasswordGrantHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.RefreshGrantTypeHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.SAML2BearerGrantTypeHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.IDTokenGenerator;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.config.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

public class AccessTokenIssuer {

    private Map<String, AuthorizationGrantHandler> authzGrantHandlers =
            new Hashtable<String, AuthorizationGrantHandler>();
    private Map<String, ClientAuthenticationHandler> clientAuthenticationHandlers =
            new Hashtable<String, ClientAuthenticationHandler>();

    private List<String> supportedGrantTypes;
    private List<String> supportedClientAuthenticationMethods;

    private static AccessTokenIssuer instance;

    private static Log log = LogFactory.getLog(AccessTokenIssuer.class);
    private Cache userClaimsCache;
    private Cache appInfoCache;

    public static AccessTokenIssuer getInstance() throws IdentityOAuth2Exception {

        CarbonUtils.checkSecurity();

        if (instance == null) {
            synchronized (AccessTokenIssuer.class) {
                if (instance == null) {
                    instance = new AccessTokenIssuer();
                }
            }
        }
        return instance;
    }

    private AccessTokenIssuer() throws IdentityOAuth2Exception {

        supportedGrantTypes = OAuthServerConfiguration.getInstance().getSupportedGrantTypes();

        authzGrantHandlers.put(GrantType.AUTHORIZATION_CODE.toString(),
                new AuthorizationCodeHandler());
        authzGrantHandlers.put(GrantType.PASSWORD.toString(),
                new PasswordGrantHandler());
        authzGrantHandlers.put(GrantType.CLIENT_CREDENTIALS.toString(),
                new ClientCredentialsGrantHandler());
        authzGrantHandlers.put(GrantType.REFRESH_TOKEN.toString(),
                new RefreshGrantTypeHandler());
        authzGrantHandlers.put(org.wso2.carbon.identity.oauth.common.GrantType.SAML20_BEARER.toString(),
                new SAML2BearerGrantTypeHandler());

        supportedClientAuthenticationMethods = OAuthServerConfiguration.getInstance().getSupportedClientAuthMethods();

        clientAuthenticationHandlers.put(OAuth2Constants.ClientAuthMethods.BASIC, new BasicAuthClientAuthentcationHandler());
        clientAuthenticationHandlers.put(OAuth2Constants.ClientAuthMethods.SAML_20_BEARER, new SAM2BearerClientAuthenticationHandler());

        //TODO: check userClaimsCache = PrivilegedCarbonContext.getCurrentContext().getCache("UserClaimsCache");
        //in org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever
        userClaimsCache = PrivilegedCarbonContext.getCurrentContext().getCache("UserClaimsCache");
        appInfoCache = PrivilegedCarbonContext.getCurrentContext().getCache("AppInfoCache");
    }

    public OAuth2AccessTokenRespDTO issue(OAuth2AccessTokenReqDTO tokenReqDTO)
            throws IdentityException, InvalidOAuthClientException {

    	 if (tokenReqDTO.getResourceOwnerUsername() !=null) {
	        //identify,whether the ResourceOwner used ordinal username/email
	        String resourceOwner = getResourceOwnerName(tokenReqDTO.getResourceOwnerUsername());
	        tokenReqDTO.setResourceOwnerUsername(resourceOwner);
        }
		String grantType = tokenReqDTO.getGrantType();
        String clientAssertionType = tokenReqDTO.getClientAssertionType();
        String clientAssertion = tokenReqDTO.getClientAssertion();
        String consumerKey = tokenReqDTO.getClientId();
        String consumerSecret = tokenReqDTO.getClientSecret();
        String clientAuthMethod = null;
        OAuth2AccessTokenRespDTO tokenRespDTO;


        if (!supportedGrantTypes.contains(grantType)) {
            //Do not change this log format as these logs use by external applications
            log.debug("Unsupported Grant Type : " + grantType +
                    " for client id : " + tokenReqDTO.getClientId());
            tokenRespDTO = handleError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE,
                    "Unsupported Grant Type!", tokenReqDTO);
            return tokenRespDTO;
        }

        if(clientAssertionType != null && clientAssertion != null){
            clientAuthMethod = OAuth2Constants.ClientAuthMethods.SAML_20_BEARER;
        }else if(consumerKey != null && consumerSecret != null){
            clientAuthMethod = OAuth2Constants.ClientAuthMethods.BASIC;
        }

        if(clientAuthMethod == null || !supportedClientAuthenticationMethods.contains(clientAuthMethod)){
            log.debug("Unsupported Client Authentication Method : " + clientAuthMethod +
                    " for client id : " + tokenReqDTO.getClientId());
            tokenRespDTO = handleError(OAuth2Constants.OAuthError.TokenResponse.UNSUPPRTED_CLIENT_AUTHENTICATION_METHOD,
                    "Unsupported Client Authentication Method!", tokenReqDTO);
            return tokenRespDTO;
        }

        AuthorizationGrantHandler authzGrantHandler = authzGrantHandlers.get(grantType);
        ClientAuthenticationHandler clientAuthHandler = clientAuthenticationHandlers.get(clientAuthMethod);

        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenReqDTO);
        boolean isAuthenticated = clientAuthHandler.authenticateClient(tokReqMsgCtx);
        boolean isValidGrant = authzGrantHandler.validateGrant(tokReqMsgCtx);
        boolean isAuthorized = authzGrantHandler.authorizeAccessDelegation(tokReqMsgCtx);
        boolean isValidScope = authzGrantHandler.validateScope(tokReqMsgCtx);

        OAuthAppDO oAuthAppDO = getAppInformation(tokenReqDTO);
        String applicationName = oAuthAppDO.getApplicationName();
        String userName = tokReqMsgCtx.getAuthorizedUser();
	    if(grantType.equals(GrantType.CLIENT_CREDENTIALS.toString()))
        {
            tokReqMsgCtx.setAuthorizedUser(oAuthAppDO.getUserName());
            tokReqMsgCtx.setTenantID(oAuthAppDO.getTenantId());
        }

        //boolean isAuthenticated = true;
        if (!isAuthenticated) {
            //Do not change this log format as these logs use by external applications
            log.debug("Client Authentication Failed for client id=" + tokenReqDTO.getClientId() + ", " +
                    "user-name=" + userName + " to application=" + applicationName);
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_CLIENT,
                    "Client credentials are invalid.", tokenReqDTO);
            return tokenRespDTO;
        }

        //boolean isValidGrant = true;
        if (!isValidGrant) {
            //Do not change this log format as these logs use by external applications
            log.debug("Invalid Grant provided by the client, id=" + tokenReqDTO.getClientId() + ", " +
                    "" + "user-name=" + userName + " to application=" + applicationName);
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_GRANT,
                    "Provided Authorization Grant is invalid.", tokenReqDTO);
            return tokenRespDTO;
        }

        //boolean isAuthorized = true;
        if (!isAuthorized) {
            //Do not change this log format as these logs use by external applications
            log.debug("Resource owner is not authorized to grant access, client-id="
                    + tokenReqDTO.getClientId() + " " + "user-name=" + userName + " to application=" + applicationName);
            tokenRespDTO = handleError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT,
                    "Unauthorized Client!", tokenReqDTO);
            return tokenRespDTO;
        }

        //boolean isValidScope = true;
        if (!isValidScope) {
            //Do not change this log format as these logs use by external applications
            log.debug("Invalid Scope provided. client-id=" + tokenReqDTO.getClientId() + " " +
                    "" + "user-name=" + userName + " to application=" + applicationName);
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_SCOPE, "Invalid Scope!", tokenReqDTO);
            return tokenRespDTO;
        }

        int tenantId;
        
        ArrayList<ResponseHeader> respHeaders = new ArrayList<ResponseHeader>();
        if (tokenReqDTO.getGrantType() != null && tokenReqDTO.getGrantType().equals(GrantType.PASSWORD.toString()) &&
                tokenReqDTO.getResourceOwnerUsername() != null) {  // this is only with the resource owner grant type
            try {
                tenantId = IdentityUtil.getTenantIdOFUser(tokenReqDTO.getResourceOwnerUsername());
                RealmService realmService = OAuthComponentServiceHolder.getRealmService();
                UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId)
                        .getUserStoreManager();

                // Read the required claim configuration.
                List<String> reqRespHeaderClaims = getClaimUrisRequiredInResponseHeader();

                if (reqRespHeaderClaims != null && reqRespHeaderClaims.size() > 0) {
                    // Get user's claim values from the default profile.
                    Claim[] mapClaimValues = getUserClaimValues(tokenReqDTO, userStoreManager);
                    ResponseHeader header;
                    for (Iterator<String> iterator = reqRespHeaderClaims.iterator(); iterator.hasNext(); ) {

                        String claimUri = iterator.next();

                        for (int j = 0; j < mapClaimValues.length; j++) {
                            Claim claim = mapClaimValues[j];
                            if (claimUri.equals(claim.getClaimUri())) {
                                header = new ResponseHeader();
                                header.setKey(claim.getDisplayTag());
                                header.setValue(claim.getValue());
                                respHeaders.add(header);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new IdentityOAuth2Exception(e.getMessage(), e);
            }
        }

        tokenRespDTO = authzGrantHandler.issue(tokReqMsgCtx);
        tokenRespDTO.setCallbackURI(oAuthAppDO.getCallbackUrl());

        ResponseHeader[] respHeadersArr = new ResponseHeader[respHeaders.size()];

        tokenRespDTO.setRespHeaders(respHeaders.toArray(respHeadersArr));

        //Do not change this log format as these logs use by external applications
        if (log.isDebugEnabled()) {
            log.debug("Access Token issued to client. client-id=" + tokenReqDTO.getClientId() + " " +
                    "" + "user-name=" + userName + " to application=" + applicationName);
        }
        
        if(tokReqMsgCtx.getScope() != null && OIDCAuthzServerUtil.isOIDCAuthzRequest(tokReqMsgCtx.getScope())) {
        	// TODO : We should allow to plug-in many generators 
        	IDTokenGenerator generator = new IDTokenGenerator(tokReqMsgCtx, tokenRespDTO);
			tokenRespDTO.setIDToken(generator.generateToken());
        }
        return tokenRespDTO;
    }

    private OAuthAppDO getAppInformation(OAuth2AccessTokenReqDTO tokenReqDTO) throws IdentityOAuthAdminException, InvalidOAuthClientException {
        OAuthAppDO oAuthAppDO;
        Object obj = appInfoCache.get(tokenReqDTO.getClientId());
        if(obj != null){
            oAuthAppDO = (OAuthAppDO)obj;
            return oAuthAppDO;
        }else{
            oAuthAppDO = new OAuthAppDAO().getAppInformation(tokenReqDTO.getClientId());
            appInfoCache.put(tokenReqDTO.getClientId(),oAuthAppDO);
            return oAuthAppDO;
        }
    }

    private Claim[] getUserClaimValues(OAuth2AccessTokenReqDTO tokenReqDTO, UserStoreManager userStoreManager) throws UserStoreException {
        Claim[] userClaims;
        Object obj = userClaimsCache.get(tokenReqDTO.getResourceOwnerUsername());
        if(obj != null){
            userClaims = (Claim[])obj;
            return userClaims;
        }else{
            if(log.isDebugEnabled()){
                log.debug("Cache miss for user claims. Username :" + tokenReqDTO.getResourceOwnerUsername());
            }
            userClaims = userStoreManager.getUserClaimValues(
                    tokenReqDTO.getResourceOwnerUsername(), null);
            userClaimsCache.put(tokenReqDTO.getResourceOwnerUsername(),userClaims);
            return userClaims;
        }
    }

    private List<String> getClaimUrisRequiredInResponseHeader() {
        return OAuthServerConfiguration.getInstance().getRequiredHeaderClaimUris();
    }

    private OAuth2AccessTokenRespDTO handleError(String errorCode,
                                                 String errorMsg,
                                                 OAuth2AccessTokenReqDTO tokenReqDTO) {
        if (log.isDebugEnabled()) {
            log.debug("OAuth-Error-Code=" + errorCode + " client-id=" + tokenReqDTO.getClientId()
                    + " grant-type=" + tokenReqDTO.getGrantType()
                    + " scope=" + OAuth2Util.buildScopeString(tokenReqDTO.getScope()));
        }
        OAuth2AccessTokenRespDTO tokenRespDTO;
        tokenRespDTO = new OAuth2AccessTokenRespDTO();
        tokenRespDTO.setError(true);
        tokenRespDTO.setErrorCode(errorCode);
        tokenRespDTO.setErrorMsg(errorMsg);
        return tokenRespDTO;
    }
  	/**
	 * Identify whether the ResourceOwner used his ordinal username or email
	 * 
	 * @param userId
	 * @return
	 */
	private boolean isResourceOwnerUsedEmail(String userId) {

		if (userId.contains("@")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the username using email address of the user. Email is a claim
	 * attribute of an user. So, in the userstore it is users responsibility TO
	 * MAINTAIN THE EMAIL AS UNIQUE for each and every users. If it is not
	 * unique we pick the very first entry from the userlist.
	 * 
	 * @param email
	 * @return
	 */
	private String getUserfromEmail(String email) {
		String claim= "http://wso2.org/claims/emailaddress";
		String username = null;
		  try {
			    RealmService realmSvc = OAuthComponentServiceHolder.getRealmService();
				RealmConfiguration config = new  RealmConfiguration();
				UserRealm realm = realmSvc.getUserRealm(config);
				org.wso2.carbon.user.core.UserStoreManager storeManager = realm.getUserStoreManager();
				String user[] = storeManager.getUserList(claim, email, null);
				if(user.length>0){
					username = user[0].toString();
				}
			} catch (UserStoreException e) {
				log.error("Error while retrieving the username using the email : "+email, e);
			}
		return username;
	}
	
	/**
	 * identify the resource owner
	 * @param userID
	 * @return
	 */
	private String getResourceOwnerName(String userID){
		String resourceOwner = userID;
		if (isResourceOwnerUsedEmail(userID)) {
			resourceOwner = getUserfromEmail(userID);
		}
		return resourceOwner;
	}
	
}
