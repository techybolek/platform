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
package org.wso2.carbon.identity.authenticator.openid;

import javax.servlet.http.HttpSession;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.services.authentication.AbstractAuthenticator;
import org.wso2.carbon.core.services.authentication.AuthenticationFailureException;
import org.wso2.carbon.core.services.authentication.AuthenticatorHelper;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.authenticator.openid.internal.OpenIDBEDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class OpenIDAuthenticator extends AbstractAuthenticator {

    private static final String AUTHENTICATOR_NAME = "OpenIDAuthenticator";
    private static final String OPENID_USER_NAME = "OPENID_USER";

    private static final Log log = LogFactory.getLog(OpenIDAuthenticator.class);

    /**
     * 
     * @param windowsLoggedInUser
     * @param remoteAddress
     * @return
     * @throws AuthenticationException
     */
    public boolean login(String openid, String remoteAddress)
            throws AuthenticationException {
        HttpSession httpSession = getHttpSession();
        int tenantId = -1;
        try {

            RegistryService registryService = OpenIDBEDataHolder.getInstance().getRegistryService();
            RealmService realmService = OpenIDBEDataHolder.getInstance().getRealmService();
            String userName = "";

            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            userName = MultitenantUtils.getTenantAwareUsername(userName);

            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService,
                    realmService, tenantDomain);

            try {
                doAuthentication(openid, tenantId,
                        MessageContext.getCurrentMessageContext());

            } catch (AuthenticationFailureException e) {
                CarbonAuthenticationUtil.onFailedAdminLogin(httpSession, openid,
                        tenantId, remoteAddress, "Data");
                log.error(e.getMessage(), e);
                return false;
            }

            boolean isAuthorized = realm.getAuthorizationManager().isUserAuthorized(
                    userName, "/permission/admin/login",
                    CarbonConstants.UI_PERMISSION_ACTION);

            if (isAuthorized) {
                CarbonAuthenticationUtil.onSuccessAdminLogin(httpSession, userName,
                        tenantId, tenantDomain, remoteAddress);
                if (log.isDebugEnabled()) {
                    log.debug(userName + " logged in from IP address " + remoteAddress);
                }
                return true;
            } else {
                CarbonAuthenticationUtil
                        .onFailedAdminLogin(httpSession, userName, tenantId,
                                remoteAddress, "User is not authorized to login using delegation");
                return false;
            }

        } catch (AuthenticationException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            String msg = "System error while Authenticating/Authorizing User : " + e.getMessage();
            log.error(msg, e);
            return false;
        }
    }

    @Override
    public boolean canHandle(MessageContext msgContext) {
        HttpSession httpSession = getHttpSession(msgContext);
        if (httpSession != null) {
            String userLoggedIn = (String) httpSession.getAttribute(OPENID_USER_NAME);
            if (userLoggedIn != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAuthenticatorName() {
        return AUTHENTICATOR_NAME;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    protected void doAuthentication(String userNameInRequest, int tenantId,
            MessageContext msgContext) throws AuthenticationFailureException {

        RegistryService registryService;
        UserRealm realm;
        try {
            registryService = OpenIDBEDataHolder.getInstance().getRegistryService();
            realm = AuthenticatorHelper.getUserRealm(tenantId, getRealmService(), registryService);

        } catch (Exception e) {
            log.error("Error retrieving user realm for authentication. Tenant id " + tenantId
                    + " user name " + userNameInRequest, e);
            throw new AuthenticationFailureException(
                    AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR,
                    userNameInRequest);
        }

        String userName = MultitenantUtils.getTenantAwareUsername(userNameInRequest);

        try {
            boolean isAuthenticated = realm.getUserStoreManager().isExistingUser(userName);
            msgContext.setProperty(OPENID_USER_NAME, userName);

            if (!isAuthenticated) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed authentication for user " + userNameInRequest);
                }
                msgContext.removeProperty(OPENID_USER_NAME);
                throw new AuthenticationFailureException(
                        AuthenticationFailureException.AuthenticationFailureReason.INVALID_USER_NAME,
                        userNameInRequest);
            }

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Unable to get UserStoreManager for authentication. User - "
                    + userNameInRequest, e);
            throw new AuthenticationFailureException(
                    AuthenticationFailureException.AuthenticationFailureReason.SYSTEM_ERROR,
                    userNameInRequest);

        }
    }

    @Override
    protected BundleContext getBundleContext() throws Exception {
        return OpenIDBEDataHolder.getInstance().getContext();
    }

    @Override
    protected RealmService getRealmService() throws Exception {
        return OpenIDBEDataHolder.getInstance().getRealmService();
    }

    @Override
    protected String getUserNameFromRequest(MessageContext msgContext) {
        return (String) msgContext.getProperty(OPENID_USER_NAME);
    }

}