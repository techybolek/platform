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

package org.wso2.carbon.apimgt.hostobjects;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.*;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.UserAwareAPIConsumer;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.client.APIAuthenticationServiceClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.stub.types.carbon.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.dto.APIVersionUserUsageDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.URL;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class APIStoreHostObject extends ScriptableObject {

    private static final long serialVersionUID = -3169012616750937045L;
    private static final Log log = LogFactory.getLog(APIStoreHostObject.class);
    private static final String hostObjectName = "APIStore";
    private static final String httpPort = "mgt.transport.http.port";
    private static final String httpsPort = "mgt.transport.https.port";
    private static final String hostName = "carbon.local.ip";

    private APIConsumer apiConsumer;

    private String username;

    public String getUsername() {
        return username;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    // The zero-argument constructor used for create instances for runtime
    public APIStoreHostObject() throws APIManagementException {
        apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
    }

    public APIStoreHostObject(String loggedUser) throws APIManagementException {
        this.username = loggedUser;
        apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws ScriptException, APIManagementException {

        if (args!=null && args.length != 0) {
            String username = (String) args[0];
            return new APIStoreHostObject(username);
        }
        return new APIStoreHostObject();
    }

    private static String getUsernameFromObject(Scriptable obj) {
        return ((APIStoreHostObject) obj).getUsername();
    }

    public APIConsumer getApiConsumer() {
        return apiConsumer;
    }

    private static APIConsumer getAPIConsumer(Scriptable thisObj) {
        return ((APIStoreHostObject) thisObj).getApiConsumer();
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }


    private static APIAuthenticationServiceClient getAPIKeyManagementClient() throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }

        String username = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
        if (username == null || password == null) {
            handleException("Authentication credentials for API key manager unspecified");
        }

        try {
            return new APIAuthenticationServiceClient(url, username, password);
        } catch (Exception e) {
            handleException("Error while initializing the subscriber key management client", e);
            return null;
        }
    }

    public static String jsFunction_getAuthServerURL(Context cx, Scriptable thisObj,
                                                     Object[] args, Function funObj) throws APIManagementException {

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }
        return url;
    }

    public static String jsFunction_getHTTPsURL(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj)
            throws APIManagementException {
        String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
        String backendHttpsPort = HostObjectUtils.getBackendPort("https");
        if (hostName == null) {
            hostName = System.getProperty("carbon.local.ip");
        }
        return "https://" + hostName + ":" + backendHttpsPort;

    }


    public static String jsFunction_getHTTPURL(Context cx, Scriptable thisObj,
                                               Object[] args, Function funObj)
            throws APIManagementException {
        return "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort);
    }

    /*
	 * getting key for API subscriber args[] list String subscriberID, String
	 * api, String apiVersion, String Date
	 */
    public static String jsFunction_getKey(Context cx, Scriptable thisObj,
                                           Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if(args!=null&& isStringArray(args)){
        APIInfoDTO apiInfo = new APIInfoDTO();
        apiInfo.setProviderId((String) args[0]);
        apiInfo.setApiName((String) args[1]);
        apiInfo.setVersion((String) args[2]);
        apiInfo.setContext((String) args[3]);
        try {
            SubscriberKeyMgtClient keyMgtClient = HostObjectUtils.getKeyManagementClient();
            return keyMgtClient.getAccessKey((String) args[5], apiInfo, (String) args[4], (String) args[6], (String) args[7]);
        } catch (Exception e) {
            String msg = "Error while obtaining access tokens";
            handleException(msg, e);
            return null;
        }
        }else{
            handleException("Invalid input parameters.");
            return null;
        }
    }

    /*
	 * getting key for a subscribed Application - args[] list String subscriberID, String
	 * application name, String keyType
	 */
    public static NativeObject jsFunction_getApplicationKey(Context cx, Scriptable thisObj,
                                                            Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        if (args != null && args.length!=0) {
            NativeArray accessAllowDomainsArr = (NativeArray) args[4]; // args[4] is not mandatory
            String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];
            for (Object domain : accessAllowDomainsArr.getIds()) {
                int index = (Integer) domain;
                accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
            }
            try {
                SubscriberKeyMgtClient keyMgtClient = HostObjectUtils.getKeyManagementClient();
                ApplicationKeysDTO dto = keyMgtClient.getApplicationAccessKey((String) args[0],
                                                                              (String) args[1], (String) args[2], (String) args[3], accessAllowDomainsArray);
                NativeObject row = new NativeObject();
                String authorizedDomains = "";
                boolean first = true;
                for (String anAccessAllowDomainsArray : accessAllowDomainsArray) {
                    if (first) {
                        authorizedDomains = anAccessAllowDomainsArray;
                        first = false;
                    } else {
                        authorizedDomains = authorizedDomains + ", " + anAccessAllowDomainsArray;
                    }
                }

                row.put("accessToken", row, dto.getApplicationAccessToken());
                row.put("consumerKey", row, dto.getConsumerKey());
                row.put("consumerSecret", row, dto.getConsumerSecret());
                boolean isRegenarateOptionEnabled = true;
                if (getApplicationAccessTokenValidityPeriod() < 0) {
                    isRegenarateOptionEnabled = false;
                }
                row.put("enableRegenarate", row, isRegenarateOptionEnabled);
                row.put("accessallowdomains", row, authorizedDomains);
                return row;
            } catch (Exception e) {
                String msg = "Error while obtaining the application access token for the application:"+ args[1];
                log.error(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            handleException("Invalid input parameters.");
            return null;
        }
    }

    public static NativeObject jsFunction_login(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
            APIManagementException {
        if (args==null || args.length == 0||!isStringArray(args)) {
            handleException("Invalid input parameters for the login method");
        }

        String username = (String) args[0];
        String password = (String) args[1];

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }

        NativeObject row = new NativeObject();

        try {
            UserAdminStub userAdminStub = new UserAdminStub(url + "UserAdmin");
            CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                    true, userAdminStub._getServiceClient());
            //If multiple user stores are in use, and if the user hasn't specified the domain to which
            //he needs to login to
            /* Below condition is commented out as per new multiple users-store implementation,users from
            different user-stores not needed to input domain names when tried to login, APIMANAGER-1392*/
            /*if (userAdminStub.hasMultipleUserStores() && !username.contains("/")) {
                handleException("Domain not specified. Please provide your username as domain/username");
            } */
        } catch (APIManagementException e) {
            row.put("error", row, true);
            row.put("detail", row, e.getMessage());
            return row;
        } catch (Exception e) {
            log.error("Error occurred while checking for multiple user stores");
        }

        try {
            AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, url + "AuthenticationAdmin");
            ServiceClient client = authAdminStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);

            String host = new URL(url).getHost();
            if (!authAdminStub.login(username, password, host)) {
                handleException("Authentication failed. Invalid username or password.");
            }
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

            boolean authorized =
                    APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_SUBSCRIBE);

            UserAdminStub userAdminStub = new UserAdminStub(url + "UserAdmin");
            CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                    true, userAdminStub._getServiceClient());
            
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            boolean isSuperTenant = false;
            
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            	isSuperTenant = true;
            }

            if (authorized) {
                row.put("user", row, username);
                row.put("sessionId", row, sessionCookie);
                row.put("isSuperTenant", row, isSuperTenant);
                row.put("error", row, false);
            } else {
                handleException("Insufficient privileges");
            }
        } catch (Exception e) {
            row.put("error", row, true);
            row.put("detail", row, e.getMessage());
        }

        return row;
    }

    public static NativeArray jsFunction_getTopRatedAPIs(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getTopRatedAPIs(limit);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting Top Rated APIs Information", e);
                return myn;
            } catch (Exception e) {
                log.error("Error while getting Top Rated APIs Information", e);
                return myn;
            }
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("description", row, api.getDescription());
                row.put("rates", row, api.getRating());
                myn.put(i, myn, row);
                i++;
            }

        }// end of the if
        return myn;
    }

    public static NativeArray jsFunction_getRecentlyAddedAPIs(Context cx,
                                                              Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && args.length!=0) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            String requestedTenantDomain=(String)args[1];
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getRecentlyAddedAPIs(limit,requestedTenantDomain);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting Recently Added APIs Information", e);
                return apiArray;
            }catch (Exception e) {
                log.error("Error while getting Recently Added APIs Information", e);
                return apiArray;
            }

            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi,APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("rates", currentApi, api.getRating());
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                apiArray.put(i, apiArray, currentApi);
                i++;
            }

        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_searchAPIbyType(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null&&args.length!=0) {
            String searchValue =(String) args[0];
            String tenantDomain = (String)args[1];
            String searchTerm;
            String searchType;
            Set<API> apiSet = null;
            boolean noSearchTerm = false;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                if (searchValue.contains(":")) {
                    if (searchValue.split(":").length > 1) {
                        searchType = searchValue.split(":")[0];
                        searchTerm = searchValue.split(":")[1];
                        if ("*".equals(searchTerm) || searchTerm.startsWith("*")) {
                            searchTerm = searchTerm.replaceFirst("\\*", ".*");
                        }
                        apiSet = apiConsumer.searchAPI(searchTerm, searchType,tenantDomain);
                    } else {
                        noSearchTerm = true;
                    }

                } else {
                    if ("*".equals(searchValue) || searchValue.startsWith("*")) {
                        searchValue = searchValue.replaceFirst("\\*", ".*");
                    }
                    apiSet = apiConsumer.searchAPI(searchValue,"Name",tenantDomain);
                }

            } catch (APIManagementException e) {
                log.error("Error while searching APIs by type", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while searching APIs by type", e);
                return apiArray;
            }

            if (noSearchTerm) {
                throw new APIManagementException("Search term is missing. Try again with valid search query.");
            }
            if(apiSet!=null){
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {

                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi,
                               APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("rates", currentApi, api.getRating());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("endpoint", currentApi, api.getUrl());
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                apiArray.put(i, apiArray, currentApi);
                i++;
            }
            }

        }// end of the if
        return apiArray;
    }

    public static boolean jsFunction_isSelfSignupEnabled(){
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        return Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
    }

    public static NativeArray jsFunction_getAPIsWithTag(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String tagName = args[0].toString();
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getAPIsWithTag(tagName);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting APIs With Tag Information", e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting APIs With Tag Information", e);
                return apiArray;
            }
            if (apiSet != null) {
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject currentApi = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    currentApi.put("name", currentApi, apiIdentifier.getApiName());
                    currentApi.put("provider", currentApi,
                                   APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                    currentApi.put("version", currentApi,
                                   apiIdentifier.getVersion());
                    currentApi.put("description", currentApi, api.getDescription());
                    currentApi.put("rates", currentApi, api.getRating());
                    if (api.getThumbnailUrl() == null) {
                        currentApi.put("thumbnailurl", currentApi,
                                       "images/api-default.png");
                    } else {
                        currentApi.put("thumbnailurl", currentApi,
                                       APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                    }
                    currentApi.put("visibility", currentApi, api.getVisibility());
                    currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                    apiArray.put(i, apiArray, currentApi);
                    i++;
                }
            }

        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_getSubscribedAPIs(Context cx,
                                                           Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String limitArg = args[0].toString();
            int limit = Integer.parseInt(limitArg);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                Set<API> apiSet = apiConsumer.getTopRatedAPIs(limit);
                if (apiSet != null) {
                    Iterator it = apiSet.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        NativeObject currentApi = new NativeObject();
                        Object apiObject = it.next();
                        API api = (API) apiObject;
                        APIIdentifier apiIdentifier = api.getId();
                        currentApi.put("name", currentApi, apiIdentifier.getApiName());
                        currentApi.put("provider", currentApi,
                                       apiIdentifier.getProviderName());
                        currentApi.put("version", currentApi,
                                       apiIdentifier.getVersion());
                        currentApi.put("description", currentApi, api.getDescription());
                        currentApi.put("rates", currentApi, api.getRating());
                        apiArray.put(i, apiArray, currentApi);
                        i++;
                    }
                }
            } catch (APIManagementException e) {
                log.error("Error while getting API list", e);
                return apiArray;
            }
        }// end of the if
        return apiArray;
    }

    public static NativeArray jsFunction_getAllTags(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        NativeArray tagArray = new NativeArray(0);
        Set<Tag> tags;
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            tags = apiConsumer.getAllTags();
        } catch (APIManagementException e) {
            log.error("Error from registry while getting API tags.", e);
            return tagArray;
        } catch (Exception e) {
            log.error("Error while getting API tags", e);
            return tagArray;
        }
        if(tags!=null){
        Iterator tagsI = tags.iterator();
        int i = 0;
        while (tagsI.hasNext()) {

            NativeObject currentTag = new NativeObject();
            Object tagObject = tagsI.next();
            Tag tag = (Tag) tagObject;

            currentTag.put("name", currentTag, tag.getName());
            currentTag.put("count", currentTag, tag.getNoOfOccurrences());

            tagArray.put(i, tagArray, currentTag);
            i++;
        }
        }

        return tagArray;
    }

    public static NativeArray jsFunction_getAllPublishedAPIs(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        Set<API> apiSet;
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            String tenantDomain;
            if (args != null) {
                tenantDomain = (String) args[0];
            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            apiSet = apiConsumer.getAllPublishedAPIs(tenantDomain);

        } catch (APIManagementException e) {
            log.error("Error from Registry API while getting API Information", e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting API Information", e);
            return myn;
        }
        if (apiSet != null) {
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", row, apiIdentifier.getApiName());
                row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("version", row, apiIdentifier.getVersion());
                row.put("context", row, api.getContext());
                row.put("status", row, "Deployed"); // api.getStatus().toString()
                if (api.getThumbnailUrl() == null) {
                    row.put("thumbnailurl", row, "images/api-default.png");
                } else {
                    row.put("thumbnailurl", row, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                row.put("visibility", row, api.getVisibility());
                row.put("visibleRoles", row, api.getVisibleRoles());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
                                                Object[] args, Function funObj) throws ScriptException,
            APIManagementException {

        String providerName;
        String apiName;
        String version;
        String username = null;
        boolean isSubscribed = false;
        NativeArray myn = new NativeArray(0);
        if (args!=null && args.length != 0) {

        providerName = APIUtil.replaceEmailDomain((String) args[0]);
        apiName = (String) args[1];
        version = (String) args[2];
        if (args[3] != null) {
        username = (String) args[3];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        API api;
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            api = apiConsumer.getAPI(apiIdentifier);
            if (username != null) {
                //TODO @sumedha : remove hardcoded tenant Id
                isSubscribed = apiConsumer.isSubscribed(apiIdentifier, username);
            }

            if(api!=null){
            NativeObject row = new NativeObject();
            apiIdentifier = api.getId();
            row.put("name", row, apiIdentifier.getApiName());
            row.put("provider", row, APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            row.put("version", row, apiIdentifier.getVersion());
            row.put("description", row, api.getDescription());
            row.put("rates", row, api.getRating());
            row.put("endpoint", row, api.getUrl());
            row.put("wsdl", row, api.getWsdlUrl());
            row.put("wadl", row, api.getWadlUrl());
            DateFormat dateFormat= new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z");
            String dateFormatted= dateFormat.format(api.getLastUpdated());
            row.put("updatedDate", row, dateFormatted);
            row.put("context", row, api.getContext());
            row.put("status", row, api.getStatus().getStatus());

            String user = getUsernameFromObject(thisObj);
            if(user!=null){
            int userRate = apiConsumer.getUserRating(apiIdentifier, user);
            row.put("userRate", row, userRate);
            }
            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            row.put("serverURL", row, config.getFirstProperty(APIConstants.API_GATEWAY_API_ENDPOINT));

            //TODO : need to pass in the full available tier list to front end
            StringBuffer tiersSet = new StringBuffer("");
            StringBuffer tiersDescSet = new StringBuffer("");
            Set<Tier> tierSet = api.getAvailableTiers();
            if(tierSet!=null){
            Iterator it = tierSet.iterator();
            int j = 0;
            while (it.hasNext()) {
                Object tierObject = it.next();
                Tier tier = (Tier) tierObject;
                tiersSet.append(tier.getName());
                tiersDescSet.append(tier.getDescription());
                if (j != tierSet.size() - 1) {
                    tiersSet.append(",");
                    tiersDescSet.append(",");
                }
                j++;
            }
            row.put("tierName", row, tiersSet.toString());
            row.put("tierDescription", row, tiersDescSet.toString());
            }
            row.put("subscribed", row, isSubscribed);
            if (api.getThumbnailUrl() == null) {
                row.put("thumbnailurl", row, "images/api-default.png");
            } else {
                row.put("thumbnailurl", row, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
            }
            row.put("bizOwner", row, api.getBusinessOwner());
            row.put("bizOwnerMail", row, api.getBusinessOwnerEmail());
            row.put("techOwner", row, api.getTechnicalOwner());
            row.put("techOwnerMail", row, api.getTechnicalOwnerEmail());
            row.put("visibility", row, api.getVisibility());
            row.put("visibleRoles", row, api.getVisibleRoles());
            myn.put(0, myn, row);
            }


        } catch (APIManagementException e) {
            handleException("Error from Registry API while getting get API Information on " + apiName, e);

        } catch (Exception e) {
            handleException(e.getMessage(), e);

        }
        }
        return myn;
    }

    public static boolean jsFunction_isSubscribed(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj)
            throws ScriptException,
                   APIManagementException {

        String username = null;
        if (args != null && args.length != 0) {
            String providerName = (String) args[0];
            String apiName = (String) args[1];
            String version = (String) args[2];
            if (args[3] != null) {
                username = (String) args[3];
            }
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            return username != null && apiConsumer.isSubscribed(apiIdentifier, username);
        } else {
            throw new APIManagementException("No input username value.");
        }
    }

    public static NativeArray jsFunction_getAllDocumentation(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        java.util.List<Documentation> doclist = null;
        String providerName = "";
        String apiName = "";
        String version = "";
        String username = "";
        if (args!=null && args.length!=0 ) {
            providerName = APIUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
            username = (String)args[3];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            doclist = apiConsumer.getAllDocumentation(apiIdentifier, username);
        } catch (APIManagementException e) {
            handleException("Error from Registry API while getting All Documentation on " + apiName, e);
        } catch (Exception e) {
            handleException("Error while getting All Documentation " + apiName, e);
        }
        if(doclist!=null){
        Iterator it = doclist.iterator();
        int i = 0;
        while (it.hasNext()) {
            NativeObject row = new NativeObject();
            Object docObject = it.next();
            Documentation documentation = (Documentation) docObject;
            Object objectSourceType = documentation.getSourceType();
            String strSourceType = objectSourceType.toString();
            row.put("name", row, documentation.getName());
            row.put("sourceType", row, strSourceType);
            row.put("summary", row, documentation.getSummary());
            String content;
            if (strSourceType.equals("INLINE")) {
                content = apiConsumer.getDocumentationContent(apiIdentifier, documentation.getName());
                row.put("content", row, content);
            }
            row.put("sourceUrl", row, documentation.getSourceUrl());
            row.put("filePath", row, documentation.getFilePath());
            DocumentationType documentationType = documentation.getType();
            row.put("type", row, documentationType.getType());

            if (documentationType == DocumentationType.OTHER) {
                row.put("otherTypeName", row, documentation.getOtherTypeName());
            }

            myn.put(i, myn, row);
            i++;
        }
        }
        return myn;

    }


    public static NativeArray jsFunction_getComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        Comment[] commentlist = new Comment[0];
        String providerName = "";
        String apiName = "";
        String version = "";
        if (args!=null && args.length!=0 ) {
            providerName = APIUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName,
                version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            commentlist = apiConsumer.getComments(apiIdentifier);
        } catch (APIManagementException e) {
            handleException("Error from registry while getting  comments for " + apiName, e);
        } catch (Exception e) {
            handleException("Error while getting comments for " + apiName, e);
        }

        int i = 0;
        if(commentlist!=null){
        for (Comment n : commentlist) {
            NativeObject row = new NativeObject();
            row.put("userName", row, n.getUser());
            row.put("comment", row, n.getText());
            row.put("createdTime", row, n.getCreatedTime().getTime());
            myn.put(i, myn, row);
            i++;
        }
        }
        return myn;

    }

    public static NativeArray jsFunction_addComments(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        String providerName = "";
        String apiName = "";
        String version = "";
        String commentStr = "";
        if (args!=null&& args.length!=0 &&isStringArray(args)) {
            providerName = APIUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
            commentStr = (String)args[3];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.addComment(apiIdentifier, commentStr, getUsernameFromObject(thisObj));
        } catch (APIManagementException e) {
            handleException("Error from registry while adding comments for " + apiName, e);
        } catch (Exception e) {
            handleException("Error while adding comments for " + apiName, e);
        }

        int i = 0;
        NativeObject row = new NativeObject();
        row.put("userName", row, providerName);
        row.put("comment", row, commentStr);
        myn.put(i, myn, row);

        return myn;
    }


    public static boolean jsFunction_addSubscription(Context cx,
                                                     Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        if (args==null ||args.length==0) {
            return false;
        }

        String providerName = (String)args[0];
        providerName=APIUtil.replaceEmailDomain(providerName);
        String apiName = (String)args[1];
        String version = (String)args[2];
        String tier = (String)args[3];
        int applicationId = ((Number) args[4]).intValue();
        String userId = (String)args[5];
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setTier(tier);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.addSubscription(apiIdentifier, userId, applicationId);
            return true;
        } catch (APIManagementException e) {
            handleException("Error while adding subscription for user: " + userId, e);
            return false;
        }
    }

    public static boolean jsFunction_addAPISubscription(Context cx,
			Scriptable thisObj, Object[] args, Function funObj) {
        if(!isStringArray(args)) {
            return false;
        }

        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String tier = args[3].toString();
        String applicationName= ((String) args[4]);
        String userId = args[5].toString();
		APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setTier(tier);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try {
            int applicationId=APIUtil.getApplicationId(applicationName,userId);
			apiConsumer.addSubscription(apiIdentifier, userId, applicationId);
            return true;
		} catch (APIManagementException e) {
			log.error("Error while adding subscription for user: " + userId, e);
            return false;
		}
	}

    public static boolean jsFunction_removeSubscriber(Context cx,
                                                      Scriptable thisObj, Object[] args, Function funObj)
            throws APIManagementException {
        String providerName = "";
        String apiName = "";
        String version = "";
        String application = "";
        String userId = "";
        if (args!=null && args.length!=0 ) {
            providerName = (String)args[0];
            apiName = (String)args[1];
            version = (String)args[2];
            application = (String) args[3];
            userId = (String)args[4];
        }
        APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setApplicationId(application);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeSubscriber(apiIdentifier, userId);
            return true;
        } catch (APIManagementException e) {
            handleException("Error while removing subscriber: " + userId, e);
            return false;
        }

    }


    public static NativeArray jsFunction_rateAPI(Context cx,
                                                 Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && args.length!=0 ) {
            String providerName = APIUtil.replaceEmailDomain((String)args[0]);
            String apiName = (String)args[1];
            String version = (String)args[2];
            String rateStr = (String)args[3];
            int rate;
            try {
                rate = Integer.parseInt(rateStr.substring(0, 1));
            } catch (NumberFormatException e) {
                log.error("Rate must to be number " + rateStr, e);
                return myn;
            } catch (Exception e) {
                log.error("Error from while Rating API " + rateStr, e);
                return myn;
            }
            APIIdentifier apiId;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiId = new APIIdentifier(providerName, apiName, version);
                String user = getUsernameFromObject(thisObj);
                switch (rate) {
                    //Below case 0[Rate 0] - is to remove ratings from a user
                    case 0: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_ZERO, user);
                        break;
                    }
                    case 1: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_ONE, user);
                        break;
                    }
                    case 2: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_TWO, user);
                        break;
                    }
                    case 3: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_THREE, user);
                        break;
                    }
                    case 4: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_FOUR, user);
                        break;
                    }
                    case 5: {
                        apiConsumer.rateAPI(apiId, APIRating.RATING_FIVE, user);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Can't handle " + rate);
                    }

                }
            } catch (APIManagementException e) {
                log.error("Error while Rating API " + apiName
                        + e);
                return myn;
            } catch (Exception e) {
                log.error("Error while Rating API " + apiName + e);
                return myn;
            }

            NativeObject row = new NativeObject();
            row.put("name", row, apiName);
            row.put("provider", row, APIUtil.replaceEmailDomainBack(providerName));
            row.put("version", row, version);
            row.put("rates", row, rateStr);
            row.put("newRating", row, Float.toString(apiConsumer.getAPI(apiId).getRating()));
            myn.put(0, myn, row);

        }// end of the if
        return myn;
    }

    public static void jsFunction_removeAPIRating(Context cx,
                                                 Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args != null && args.length != 0) {
            String providerName = APIUtil.replaceEmailDomain((String) args[0]);
            String apiName = (String) args[1];
            String version = (String) args[2];

            APIIdentifier apiId;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiId = new APIIdentifier(providerName, apiName, version);
                String user = getUsernameFromObject(thisObj);
                apiConsumer.removeAPIRating(apiId, user);

            } catch (APIManagementException e) {
                throw new APIManagementException("Error while remove User Rating of the API " + apiName
                          + e);

            } catch (Exception e) {
                throw new APIManagementException("Error while remove User Rating of the API  " + apiName + e);

            }



        }// end of the if

    }


    public static NativeArray jsFunction_getSubscriptions(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && args.length!=0 ) {
            String providerName = (String)args[0];
            String apiName = (String)args[1];
            String version = (String)args[2];
            String user = (String)args[3];

            APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
            Subscriber subscriber = new Subscriber(user);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> apis = apiConsumer.getSubscribedIdentifiers(subscriber, apiIdentifier);
            int i = 0;
            if(apis!=null){
            for (SubscribedAPI api : apis) {
                NativeObject row = new NativeObject();
                row.put("application", row, api.getApplication().getName());
                row.put("applicationId", row, api.getApplication().getId());
                row.put("prodKey", row, getKey(api, APIConstants.API_KEY_TYPE_PRODUCTION));
                row.put("sandboxKey", row, getKey(api, APIConstants.API_KEY_TYPE_SANDBOX));
                myn.put(i++, myn, row);
            }
            }
        }
        return myn;
    }

    public static String jsFunction_getSwaggerDiscoveryUrl(Context cx,
                                                           Scriptable thisObj, Object[] args,
                                                           Function funObj)
            throws APIManagementException {
        String apiName;
        String version;
        String providerName;
        
        if (args != null && args.length != 0 ) {

            apiName = (String) args[0];
            version = (String) args[1];
            providerName = (String) args[2];
            
            String apiDefinitionFilePath = APIUtil.getAPIDefinitionFilePath(apiName, version);
            apiDefinitionFilePath = RegistryConstants.PATH_SEPARATOR + "registry"
            		+ RegistryConstants.PATH_SEPARATOR + "resource"
            		+ RegistryConstants.PATH_SEPARATOR + "_system"
            		+ RegistryConstants.PATH_SEPARATOR + "governance"
            		+ apiDefinitionFilePath;
            
            apiDefinitionFilePath = APIUtil.prependTenantPrefix(apiDefinitionFilePath, providerName);
            
            return APIUtil.prependWebContextRoot(apiDefinitionFilePath);
            
        } else {
            handleException("Invalid input parameters.");
            return null;
        }
    }

    private static APIKey getKey(SubscribedAPI api, String keyType) {
        List<APIKey> apiKeys = api.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    private static APIKey getAppKey(Application app, String keyType) {
        List<APIKey> apiKeys = app.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    private static APIKey getKeyOfType(List<APIKey> apiKeys, String keyType) {
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key;
            }
        }
        return null;
    }

    public static NativeArray jsFunction_getAllSubscriptions(Context cx,
                                                             Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args==null || args.length == 0 || !isStringArray(args)) {
            return null;
        }
        String user = (String) args[0];
        Subscriber subscriber = new Subscriber(user);
        Map<Integer, NativeArray> subscriptionsMap = new HashMap<Integer, NativeArray>();
        NativeArray appsObj = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
        int i = 0;
        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            NativeArray apisArray = subscriptionsMap.get(subscribedAPI.getApplication().getId());
            if (apisArray == null) {
                apisArray = new NativeArray(1);
                NativeObject appObj = new NativeObject();
                appObj.put("id", appObj, subscribedAPI.getApplication().getId());
                appObj.put("name", appObj, subscribedAPI.getApplication().getName());
                appObj.put("callbackUrl", appObj, subscribedAPI.getApplication().getCallbackUrl());

                APIKey prodKey = getAppKey(subscribedAPI.getApplication(), APIConstants.API_KEY_TYPE_PRODUCTION);
                boolean prodEnableRegenarateOption = true;
                if (prodKey != null) {
                    appObj.put("prodKey", appObj, prodKey.getAccessToken());
                    appObj.put("prodConsumerKey", appObj, prodKey.getConsumerKey());
                    appObj.put("prodConsumerSecret", appObj, prodKey.getConsumerSecret());
                    if (prodKey.getValidityPeriod() == Long.MAX_VALUE) {
                        prodEnableRegenarateOption = false;
                    }
                    appObj.put("prodRegenarateOption", appObj, prodEnableRegenarateOption);
                    appObj.put("prodAuthorizedDomains", appObj, prodKey.getAuthorizedDomains());
                } else {
                    appObj.put("prodKey", appObj, null);
                    appObj.put("prodConsumerKey", appObj, null);
                    appObj.put("prodConsumerSecret", appObj, null);
                    appObj.put("prodRegenarateOption", appObj, prodEnableRegenarateOption);
                    appObj.put("prodAuthorizedDomains", appObj, null);
                }

                APIKey sandboxKey = getAppKey(subscribedAPI.getApplication(), APIConstants.API_KEY_TYPE_SANDBOX);
                boolean sandEnableRegenarateOption = true;
                if (sandboxKey != null) {
                    appObj.put("sandboxKey", appObj, sandboxKey.getAccessToken());
                    appObj.put("sandboxConsumerKey", appObj, sandboxKey.getConsumerKey());
                    appObj.put("sandboxConsumerSecret", appObj, sandboxKey.getConsumerSecret());
                    if (sandboxKey.getValidityPeriod() == Long.MAX_VALUE) {
                        sandEnableRegenarateOption = false;
                    }
                    appObj.put("sandboxAuthorizedDomains", appObj, sandboxKey.getAuthorizedDomains());
                } else {
                    appObj.put("sandboxKey", appObj, null);
                    appObj.put("sandboxConsumerKey", appObj, null);
                    appObj.put("sandboxConsumerSecret", appObj, null);
                    appObj.put("sandRegenarateOption", appObj, sandEnableRegenarateOption);
                    appObj.put("sandboxAuthorizedDomains", appObj, null);
                }

                addAPIObj(subscribedAPI, apisArray, thisObj);
                appObj.put("subscriptions", appObj, apisArray);
                appsObj.put(i++, appsObj, appObj);
                //keep a subscriptions map in order to efficiently group appObj vice.
                subscriptionsMap.put(subscribedAPI.getApplication().getId(), apisArray);
            } else {
                addAPIObj(subscribedAPI, apisArray, thisObj);
            }
        }
        return appsObj;
    }

    private static void addAPIObj(SubscribedAPI subscribedAPI, NativeArray apisArray,
                                  Scriptable thisObj) throws APIManagementException {
        NativeObject apiObj = new NativeObject();
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            API api = apiConsumer.getAPI(subscribedAPI.getApiId());
            apiObj.put("name", apiObj, subscribedAPI.getApiId().getApiName());
            apiObj.put("provider", apiObj, APIUtil.replaceEmailDomainBack(subscribedAPI.getApiId().getProviderName()));
            apiObj.put("version", apiObj, subscribedAPI.getApiId().getVersion());
            apiObj.put("status", apiObj, api.getStatus().toString());
            apiObj.put("tier", apiObj, subscribedAPI.getTier().getName());
            apiObj.put("subStatus", apiObj, subscribedAPI.getSubStatus());
            apiObj.put("thumburl", apiObj, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
            apiObj.put("context", apiObj, api.getContext());
            APIKey prodKey = getAppKey(subscribedAPI.getApplication(), APIConstants.API_KEY_TYPE_PRODUCTION);
            if (prodKey != null) {
                apiObj.put("prodKey", apiObj, prodKey.getAccessToken());
                apiObj.put("prodConsumerKey", apiObj, prodKey.getConsumerKey());
                apiObj.put("prodConsumerSecret", apiObj, prodKey.getConsumerSecret());
                apiObj.put("prodAuthorizedDomains", apiObj, prodKey.getAuthorizedDomains());
            } else {
                apiObj.put("prodKey", apiObj, null);
                apiObj.put("prodConsumerKey", apiObj, null);
                apiObj.put("prodConsumerSecret", apiObj, null);
                apiObj.put("prodAuthorizedDomains", apiObj, null);
            }

            APIKey sandboxKey = getAppKey(subscribedAPI.getApplication(), APIConstants.API_KEY_TYPE_SANDBOX);
            if (sandboxKey != null) {
                apiObj.put("sandboxKey", apiObj, sandboxKey.getAccessToken());
                apiObj.put("sandboxConsumerKey", apiObj, sandboxKey.getConsumerKey());
                apiObj.put("sandboxConsumerSecret", apiObj, sandboxKey.getConsumerSecret());
                apiObj.put("sandAuthorizedDomains", apiObj, sandboxKey.getAuthorizedDomains());
            } else {
                apiObj.put("sandboxKey", apiObj, null);
                apiObj.put("sandboxConsumerKey", apiObj, null);
                apiObj.put("sandboxConsumerSecret", apiObj, null);
                apiObj.put("sandAuthorizedDomains", apiObj, null);
            }
            apiObj.put("hasMultipleEndpoints", apiObj, String.valueOf(api.getSandboxUrl() != null));
            apisArray.put(apisArray.getIds().length, apisArray, apiObj);
        } catch (APIManagementException e) {
            handleException("Error while obtaining application metadata", e);
        }
    }

    public static NativeObject jsFunction_getSubscriber(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args != null && isStringArray(args)) {
            NativeObject user = new NativeObject();
            String userName = args[0].toString();
            Subscriber subscriber = null;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                subscriber = apiConsumer.getSubscriber(userName);
            } catch (APIManagementException e) {
                handleException("Error while getting Subscriber", e);
            } catch (Exception e) {
                handleException("Error while getting Subscriber", e);
            }

            if (subscriber != null) {
                user.put("name", user, subscriber.getName());
                user.put("id", user, subscriber.getId());
                user.put("email", user, subscriber.getEmail());
                user.put("subscribedDate", user, subscriber.getSubscribedDate());
                return user;
            }
        }
        return null;
    }

    public static boolean jsFunction_addSubscriber(Context cx,
                                                   Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException, UserStoreException {

        if (args!=null && isStringArray(args)) {
            Subscriber subscriber = new Subscriber((String) args[0]);
            subscriber.setSubscribedDate(new Date());
            //TODO : need to set the proper email
            subscriber.setEmail("");
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                int tenantId=ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(MultitenantUtils.getTenantDomain((String) args[0]));
                subscriber.setTenantId(tenantId);
                apiConsumer.addSubscriber(subscriber);
            } catch (APIManagementException e) {
                handleException("Error while adding the subscriber"+subscriber.getName(), e);
                return false;
            } catch (Exception e) {
                handleException("Error while adding the subscriber"+subscriber.getName(), e);
                return false;
            }
            return true;
        }
        return false;
    }

    public static NativeArray jsFunction_getApplications(Context cx,
                                                         Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String username = args[0].toString();
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] applications = apiConsumer.getApplications(new Subscriber(username));
            if (applications != null) {
                int i = 0;
                for (Application application : applications) {
                    NativeObject row = new NativeObject();
                    row.put("name", row, application.getName());
                    row.put("tier", row, application.getTier());
                    row.put("id", row, application.getId());
                    row.put("callbackUrl", row, application.getCallbackUrl());
                    myn.put(i++, myn, row);
                }
            }
        }
        return myn;
    }

    public static boolean jsFunction_addApplication(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args!=null && isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            String tier = (String) args[2];
            String callbackUrl = (String) args[3];

            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Subscriber subscriber = new Subscriber(username);

            Application[] apps = apiConsumer.getApplications(subscriber);
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    handleException("A duplicate application already exists by the name - " + name);
                }
            }

            Application application = new Application(name, subscriber);
            application.setTier(tier);
            application.setCallbackUrl(callbackUrl);
            apiConsumer.addApplication(application, username);
            return true;
        }
        return false;
    }

    public static boolean jsFunction_sleep(Context cx,
                                           Scriptable thisObj, Object[] args, Function funObj){
        if (isStringArray(args)) {
            String millis = (String) args[0];
            try {
                Thread.sleep( Long.valueOf(millis));
            } catch (InterruptedException e) {
                log.error("Sleep Thread Interrupted");
                return false;
            }
        }
        return true;
    }

    public static boolean jsFunction_removeApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args!=null && isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber);
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    apiConsumer.removeApplication(app);
                    return true;
                }
            }
        }
        return false;
    }

    public static NativeArray jsFunction_getSubscriptionsByApplication(Context cx,
                                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
            int i = 0;
            for (SubscribedAPI api : subscribedAPIs) {
                if (api.getApplication().getName().equals(name)) {
                    NativeObject row = new NativeObject();
                    row.put("apiName", row, api.getApiId().getApiName());
                    row.put("apiVersion", row, api.getApiId().getVersion());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        }
        return myn;
    }

    public static boolean jsFunction_updateApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args!=null && isStringArray(args)) {
            String name = (String) args[0];
            String oldName = (String) args[1];
            String username = (String) args[2];
            String tier = (String) args[3];
            String callbackUrl = (String) args[4];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber);
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(oldName)) {
                    Application application = new Application(name, subscriber);
                    application.setId(app.getId());
                    application.setTier(tier);
                    application.setCallbackUrl(callbackUrl);
                    apiConsumer.updateApplication(application);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean jsFunction_updateApplicationTier(Context cx,
                                                           Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (args!=null && isStringArray(args)) {
            String name = (String) args[0];
            String tier = (String) args[1];
            String username = (String) args[2];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber);
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    app.setTier(tier);
                    apiConsumer.updateApplication(app);
                    return true;
                }
            }
        }
        return false;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String content = null;
        NativeArray myn = new NativeArray(0);


        if (args!=null && isStringArray(args)) {
            providerName = APIUtil.replaceEmailDomain((String)args[0]);
            apiName = (String)args[1];
            version = (String)args[2];
            docName = (String)args[3];
            APIIdentifier apiId = new APIIdentifier(providerName, apiName,
                    version);
            try {
                APIConsumer apiConsumer = getAPIConsumer(thisObj);
                content = apiConsumer.getDocumentationContent(apiId, docName);
            } catch (Exception e) {
                handleException("Error while getting Inline Document Content ", e);
            }

            if(content == null){
                content = "";
            }

            NativeObject row = new NativeObject();
            row.put("providerName", row, providerName);
            row.put("apiName", row, apiName);
            row.put("apiVersion", row, version);
            row.put("docName", row, docName);
            row.put("content", row, content);
            myn.put(0, myn, row);

        }
        return myn;
    }

    /*
      * here return boolean with checking all objects in array is string
      */
    public static boolean isStringArray(Object[] args) {
        int argsCount = args.length;
        for (int i = 0; i < argsCount; i++) {
            if (!(args[i] instanceof String)) {
                return false;
            }
        }
        return true;

    }

    public static boolean jsFunction_hasSubscribePermission(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws ScriptException {
        APIConsumer consumer = getAPIConsumer(thisObj);
        if (consumer instanceof UserAwareAPIConsumer) {
            try {
                ((UserAwareAPIConsumer) consumer).checkSubscribePermission();
                return true;
            } catch (APIManagementException e) {
                return false;
            }
        }
        return false;
    }

    public static void jsFunction_addUser(Context cx, Scriptable thisObj,
                                          Object[] args,
                                          Function funObj) throws APIManagementException {

        if(args!=null && isStringArray(args)){
        String username = args[0].toString();
        String password = args[1].toString();
        String fields = args[2].toString();

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        boolean enabled = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
        if (!enabled) {
            handleException("Self sign up has been disabled on this server");
        }
        String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        String adminUsername = config.getFirstProperty(APIConstants.AUTH_MANAGER_USERNAME);
        String adminPassword = config.getFirstProperty(APIConstants.AUTH_MANAGER_PASSWORD);
        if (serverURL == null || adminUsername == null || adminPassword == null) {
            handleException("Required parameter missing to connect to the" +
                            " authentication manager");
        }

        String role = config.getFirstProperty(APIConstants.SELF_SIGN_UP_ROLE);
        if (role == null) {
            handleException("Subscriber role undefined for self registration");
        }

        /* fieldValues will contain values up to last field user entered*/
        String fieldValues[] = fields.split("\\|");
        UserFieldDTO[] userFields = getOrderedUserFieldDTO();
        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] != null) {
                userFields[i].setFieldValue(fieldValues[i]);
            }
        }
        /* assign empty string for rest of the user fields */
        for (int i = fieldValues.length; i < userFields.length; i++) {
            userFields[i].setFieldValue("");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserFields(userFields);
        userDTO.setUserName(username);
        userDTO.setPassword(password);

        try {

            UserRegistrationAdminServiceStub stub = new UserRegistrationAdminServiceStub(null, serverURL
                                                                                               + "UserRegistrationAdminService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);

            stub.addUser(userDTO);
            /* update users role list with SELF_SIGN_UP_ROLE role */
            updateRolesOfUser(serverURL, adminUsername, adminPassword, username, role);
        } catch (RemoteException e) {
            handleException(e.getMessage(), e);
        } catch (Exception e) {
            handleException("Error while adding the user: " + username, e);
        }
        } else{
            handleException("Invalid input parameters.");
        }
    }

    public static boolean jsFunction_isUserExists(Context cx, Scriptable thisObj,
                                                  Object[] args, Function funObj)
            throws ScriptException,
            APIManagementException {
        if (args==null || args.length == 0) {
            handleException("Invalid input parameters to the isUserExists method");
        }

        String username = (String) args[0];
        boolean exists = false;
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm = realmService.getBootstrapRealm();
            UserStoreManager manager = realm.getUserStoreManager();
            if (manager.isExistingUser(username)) {
                exists = true;
            }
        } catch (UserStoreException e) {
            handleException("Error while checking user existence for " + username);
        }
        return exists;
    }

    public static boolean jsFunction_removeSubscription(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws APIManagementException {
        if (args==null|| args.length == 0) {
            handleException("Invalid number of input parameters.");
        }
        String username = (String)args[0];
        int applicationId = ((Number) args[1]).intValue();
        NativeObject apiData = (NativeObject) args[2];
        String provider = APIUtil.replaceEmailDomain((String) apiData.get("provider", apiData));
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            apiConsumer.removeSubscription(apiId, username, applicationId);
            return true;
        } catch (APIManagementException e) {
            handleException("Error while removing the subscription of" + name + "-" + version, e);
            return false;
        }
    }

    public static NativeArray jsFunction_getPublishedAPIsByProvider(Context cx, Scriptable thisObj,
                                                                    Object[] args,
                                                                    Function funObj)
            throws APIManagementException {
        NativeArray apiArray = new NativeArray(0);
        if (args!=null && isStringArray(args)) {
            String providerName = APIUtil.replaceEmailDomain(args[0].toString());
            String username = args[1].toString();
            Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiSet = apiConsumer.getPublishedAPIsByProvider(providerName, username, 5);
            } catch (APIManagementException e) {
                handleException("Error while getting published APIs information of the provider - " +
                        providerName, e);
                return null;
            } catch (Exception e) {
                handleException("Error while getting published APIs information of the provider", e);
                return null;
            }
            if(apiSet!=null){
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi,
                               APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                //Rating should retrieve from db
                currentApi.put("rates", currentApi, ApiMgtDAO.getAverageRating(api.getId()));
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
                }
                currentApi.put("visibility", currentApi, api.getVisibility());
                currentApi.put("visibleRoles", currentApi, api.getVisibleRoles());
                apiArray.put(i, apiArray, currentApi);
                i++;
            }
            }
            return apiArray;

        } else {
            handleException("Invalid types of input parameters.");
            return null;
        }
    }

    public static NativeObject jsFunction_refreshToken(Context cx, Scriptable thisObj,
                                                       Object[] args,
                                                       Function funObj)
            throws APIManagementException, AxisFault {

        NativeObject row = new NativeObject();
        if (args!=null && args.length!=0) {
        String userId = (String) args[0];
        String applicationName = (String) args[1];
        String tokenType = (String) args[2];
        String oldAccessToken = (String) args[3];
        NativeArray accessAllowDomainsArr = (NativeArray) args[4];
        String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];
        String clientId = (String) args[5];
        String clientSecret = (String) args[6];
        
        for (Object domain : accessAllowDomainsArr.getIds()) {
            int index = (Integer) domain;
            accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
        }

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        //Check whether old access token is already available
        if (apiConsumer.isApplicationTokenExists(oldAccessToken)) {
            SubscriberKeyMgtClient keyMgtClient = HostObjectUtils.getKeyManagementClient();
            ApplicationKeysDTO dto = new ApplicationKeysDTO();
            String accessToken;
            try {
                //Regenerate the application access key
                accessToken = keyMgtClient.regenerateApplicationAccessKey(tokenType, oldAccessToken,
                        accessAllowDomainsArray,clientId,clientSecret);
                if (accessToken != null) {
                    //If a new access token generated successfully,remove the old access token from cache.
                    APIAuthenticationServiceClient authKeyMgtClient = getAPIKeyManagementClient();
                    authKeyMgtClient.invalidateKey(oldAccessToken);
                    //Set newly generated application access token
                    dto.setApplicationAccessToken(accessToken);
                }
                row.put("accessToken", row, dto.getApplicationAccessToken());
                row.put("consumerKey", row, dto.getConsumerKey());
                row.put("consumerSecret", row, dto.getConsumerSecret());
                boolean isRegenarateOptionEnabled = true;
                if (getApplicationAccessTokenValidityPeriod() < 0) {
                    isRegenarateOptionEnabled = false;
                }
                row.put("enableRegenarate", row, isRegenarateOptionEnabled);
            } catch (APIManagementException e) {
                handleException("Error while refreshing the access token.", e);
            } catch (Exception e) {
                handleException(e.getMessage(), e);
            }
        } else {
            handleException("Cannot regenerate a new access token. There's no access token available as : " + oldAccessToken);
        }
            return row;
        } else {
            handleException("Invalid types of input parameters.");
            return null;
        }
    }

    public static void jsFunction_updateAccessAllowDomains(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws APIManagementException, AxisFault {
        String accessToken = (String) args[0];
        NativeArray accessAllowDomainsArr = (NativeArray) args[1];
        String[] accessAllowDomainsArray = new String[(int) accessAllowDomainsArr.getLength()];
        for (Object domain : accessAllowDomainsArr.getIds()) {
            int index = (Integer) domain;
            accessAllowDomainsArray[index] = (String) accessAllowDomainsArr.get(index, null);
        }
        try {
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            apiConsumer.updateAccessAllowDomains(accessToken, accessAllowDomainsArray);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }



    public static NativeArray jsFunction_getAPIUsageforSubscriber(Context cx, Scriptable thisObj,
                                                                  Object[] args, Function funObj)
            throws APIManagementException {
        List<APIVersionUserUsageDTO> list = null;
        if (args==null || args.length == 0) {
            handleException("Invalid number of parameters.");
        }
        NativeArray myn = new NativeArray(0);
        if (!HostObjectUtils.checkDataPublishingEnabled()) {
            return myn;
        }
        String subscriberName = (String) args[0];
        String period = (String) args[1];

        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient(((APIStoreHostObject) thisObj).getUsername());
            list = client.getUsageBySubscriber(subscriberName, period);
        } catch (APIMgtUsageQueryServiceClientException e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        } catch (Exception e) {
            handleException("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }

        Iterator it = null;

        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIVersionUserUsageDTO usage = (APIVersionUserUsageDTO) usageObject;
                row.put("api", row, usage.getApiname());
                row.put("version", row, usage.getVersion());
                row.put("count", row, usage.getCount());
                row.put("costPerAPI", row, usage.getCostPerAPI());
                row.put("cost", row, usage.getCost());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }


    /**
     * Check the APIs' adding comment is turned on or off
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     */
    public static boolean jsFunction_isCommentActivated() throws APIManagementException {

        boolean commentActivated = false;
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();

        commentActivated = Boolean.valueOf(config.getFirstProperty(APIConstants.API_STORE_DISPLAY_COMMENTS));

        if (commentActivated) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check the APIs' adding rating facility is turned on or off
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws APIManagementException
     */
    public static boolean jsFunction_isRatingActivated() throws APIManagementException {

        boolean ratingActivated = false;
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();

        ratingActivated = Boolean.valueOf(config.getFirstProperty(APIConstants.API_STORE_DISPLAY_RATINGS));

        if (ratingActivated) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if billing enabled else false
     * @throws APIManagementException
     */
    public static boolean jsFunction_isBillingEnabled()
            throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String billingConfig = config.getFirstProperty(APIConstants.BILLING_AND_USAGE_CONFIGURATION);
        return Boolean.parseBoolean(billingConfig);
    }

    public static NativeArray jsFunction_getTiers(Context cx, Scriptable thisObj,
                                                  Object[] args,
                                                  Function funObj) {
        NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            Set<Tier> tiers = apiConsumer.getTiers();
            int i = 0;
            for (Tier tier : tiers) {
                NativeObject row = new NativeObject();
                row.put("tierName", row, tier.getName());
                row.put("tierDescription", row,
                        tier.getDescription() != null ? tier.getDescription() : "");
                myn.put(i, myn, row);
                i++;
            }
        } catch (Exception e) {
            log.error("Error while getting available tiers", e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getUserFields(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        UserFieldDTO[] userFields = getOrderedUserFieldDTO();
        NativeArray myn = new NativeArray(0);
        int limit = userFields.length;
        for (int i = 0; i < limit; i++) {
            NativeObject row = new NativeObject();
            row.put("fieldName", row, userFields[i].getFieldName());
            row.put("claimUri", row, userFields[i].getClaimUri());
            row.put("required", row, userFields[i].getRequired());
            myn.put(i, myn, row);
        }
        return myn;
    }

    public static boolean jsFunction_hasUserPermissions(Context cx,
                                                        Scriptable thisObj, Object[] args,
                                                        Function funObj)
            throws ScriptException, APIManagementException {
        if (args!=null && isStringArray(args)) {
            String username = args[0].toString();
            return APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_SUBSCRIBE);
        } else {
            handleException("Invalid types of input parameters.");
        }
        return false;
    }

    private static UserFieldDTO[] getOrderedUserFieldDTO() {
        UserRegistrationAdminServiceStub stub;
        UserFieldDTO[] userFields = null;
        try {
            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
            if (url == null) {
                handleException("API key manager URL unspecified");
            }
            stub = new UserRegistrationAdminServiceStub(null, url + "UserRegistrationAdminService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            userFields = stub.readUserFieldsForUserRegistration(UserCoreConstants.DEFAULT_CARBON_DIALECT);
            Arrays.sort(userFields, new HostObjectUtils.RequiredUserFieldComparator());
            Arrays.sort(userFields, new HostObjectUtils.UserFieldComparator());
        } catch (Exception e) {
            log.error("Error while retrieving User registration Fields", e);
        }
        return userFields;
    }

    private static void updateRolesOfUser(String serverURL, String adminUsername,
                                          String adminPassword, String userName, String role) throws Exception {
        String url = serverURL + "UserAdmin";

        UserAdminStub userAdminStub = new UserAdminStub(url);
        CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
                true, userAdminStub._getServiceClient());
        FlaggedName[] flaggedNames = userAdminStub.getRolesOfUser(userName, "*", -1);
        List<String> roles = new ArrayList<String>();
        if (flaggedNames != null) {
            for (int i = 0; i < flaggedNames.length; i++) {
                if (flaggedNames[i].getSelected()) {
                    roles.add(flaggedNames[i].getItemName());
                }
            }
        }
        roles.add(role);
        userAdminStub.updateRolesOfUser(userName, roles.toArray(new String[roles.size()]));
    }

    private static long getApplicationAccessTokenValidityPeriod(){
        return OAuthServerConfiguration.getInstance().getDefaultApplicationAccessTokenValidityPeriodInSeconds();
    }


}
