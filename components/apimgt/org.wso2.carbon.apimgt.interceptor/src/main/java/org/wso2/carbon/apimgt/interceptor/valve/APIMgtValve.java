package org.wso2.carbon.apimgt.interceptor.valve;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIContextCache;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;
import org.wso2.carbon.apimgt.interceptor.valve.internal.DataHolder;
import org.wso2.carbon.apimgt.core.authenticate.APITokenValidator;
import org.wso2.carbon.apimgt.core.usage.APIStatsPublisher;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.CarbonUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class APIMgtValve extends ValveBase {

    private static final Log log = LogFactory.getLog(APIManagerInterceptorValve.class);

    private static final String RESTAPI_CONTEXT = "/resource";

    private APIKeyValidationInfoDTO apiKeyValidationDTO;

    private boolean statsPublishingEnabled;

    private String statsPublisherClass;

    private volatile APIMgtUsageDataPublisher publisher;

    private boolean initialized = false;

    private String hostName;

    private String externalAPIManagerURL = null;

    private String manageAPIs;

    LRUCache<String, Boolean> contextCache = null;

    public APIMgtValve(){
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        if(!initialized){
            statsPublishingEnabled = UsageComponent.getApiMgtConfigReaderService().isEnabled();
            statsPublisherClass = UsageComponent.getApiMgtConfigReaderService().getPublisherClass();
            hostName = DataPublisherUtil.getHostAddress();
            externalAPIManagerURL = CarbonUtils.getServerConfiguration().getFirstProperty("APIGateway");
            manageAPIs = CarbonUtils.getServerConfiguration().getFirstProperty("EnableAPIManagement");
            contextCache = APIContextCache.getInstance().getApiContexts();
            initialized = true;
        }

        String context = request.getContextPath();

        if (context == null || context.equals("")) {
            //Invoke next valve in pipe.
            getNext().invoke(request, response);
            //return;
        }

        boolean contextExist;
        Boolean contextValueInCache = contextCache.get(context) ;

        if (contextValueInCache != null) {
            contextExist = contextValueInCache;
        } else {
            contextExist = ApiMgtDAO.isContextExist(context);
            contextCache.put(context, contextExist);
        }

        if (!contextExist) {
            //Invoke next valve in pipe.
            getNext().invoke(request, response);
            //return;
        }

        long requestTime = System.currentTimeMillis();

        if ("true".equals(manageAPIs) && contextExist) {

            //If external API Manager url is null
            if (externalAPIManagerURL == null) {

                //Use embedded API Management
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
                    System.out.println("Authorized...");
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

                if(statsPublishingEnabled){
                    publishRequestStatistics(request, requestTime);
                }
            }
            else { //user external api-manager for api management
                //TODO
            }
        }

        //Invoke next valve in pipe.
        getNext().invoke(request, response);

        //Handle Responses
        if("true".equals(manageAPIs) && ApiMgtDAO.isContextExist(context) && statsPublishingEnabled){
            publishResponseStatistics(request, requestTime);
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

    private boolean publishRequestStatistics(HttpServletRequest request, long currentTime) {

        if (publisher == null) {
            synchronized (this){
                if (publisher == null) {
                    try {
                        log.debug("Instantiating Data Publisher");
                        publisher = (APIMgtUsageDataPublisher)Class.forName(statsPublisherClass).newInstance();
                        publisher.init();
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + statsPublisherClass);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + statsPublisherClass);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + statsPublisherClass);
                    }
                }
            }
        }

        APIStatsPublisher statsPublisher = new APIStatsPublisher(publisher, hostName);
        statsPublisher.publishRequestStatistics(apiKeyValidationDTO, request.getRequestURI(), request.getContextPath(),
                request.getPathInfo(), request.getMethod(), currentTime);

        return true;
    }

    private boolean publishResponseStatistics(HttpServletRequest request, long requestTime) {

        if (publisher == null) {
            synchronized (this){
                if (publisher == null) {
                    try {
                        log.debug("Instantiating Data Publisher");
                        publisher = (APIMgtUsageDataPublisher)Class.forName(statsPublisherClass).newInstance();
                        publisher.init();
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + statsPublisherClass);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + statsPublisherClass);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + statsPublisherClass);
                    }
                }
            }
        }

        APIStatsPublisher statsPublisher = new APIStatsPublisher(publisher, hostName);
        statsPublisher.publishResponseStatistics(apiKeyValidationDTO, request.getRequestURI(), request.getContextPath(),
                request.getPathInfo(), request.getMethod(), requestTime);

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
