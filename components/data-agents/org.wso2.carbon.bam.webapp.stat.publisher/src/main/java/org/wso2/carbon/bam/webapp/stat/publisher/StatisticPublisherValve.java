package org.wso2.carbon.bam.webapp.stat.publisher;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.data.CarbonDataHolder;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEvent;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEventData;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.EventPublisher;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.WebappAgentUtil;
import org.wso2.carbon.bam.webapp.stat.publisher.util.BrowserInfoUtils;
import org.wso2.carbon.bam.webapp.stat.publisher.util.TenantEventConfigData;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.Map;

/**
 * User: KasunG
 * Date: 5/2/13
 */
public class StatisticPublisherValve extends ValveBase {

    private static Log log = LogFactory.getLog(StatisticPublisherValve.class);
    private static final String ENABLE_STATISTICS = "enable.statistics";
    private static final String UID_REPLACE_CHAR = "..";
    private static final String UID_REPLACE_CHAR_REGEX = "\\.\\.";

    public StatisticPublisherValve() {
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
      //todo add url maping scenario
        if (!Boolean.parseBoolean(request.getContext().findParameter(ENABLE_STATISTICS)) ||
                requestURI.startsWith("/services") ) {
            getNext().invoke(request, response);
            return;
        }

        Long startTime = System.nanoTime();
//        Long startTime = request.getCoyoteRequest().getStartTime();
        
        getNext().invoke(request, response);
        
        Long responseTime = System.nanoTime() - startTime;
        Timestamp timestamp = new Timestamp(startTime);
        try {
            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);
            ConfigurationContext currentCtx;
            if (tenantDomain != null) {
                currentCtx = getTenantConfigurationContext(tenantDomain);
            } else {
                currentCtx = CarbonDataHolder.getServerConfigContext();
            }

            int tenantID = MultitenantUtils.getTenantId(currentCtx);
            Map<Integer, InternalEventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
            InternalEventingConfigData eventingConfigData = tenantSpecificEventConfig.get(tenantID);

            WebappStatEventData webappStatEventData = getWebappStatEventData(request, response);
            webappStatEventData.setTimestamp(timestamp);

            if (eventingConfigData != null && eventingConfigData.isWebappStatsEnabled()) {

                WebappStatEvent event = WebappAgentUtil.makeEventList(webappStatEventData, eventingConfigData);
                EventPublisher publisher = new EventPublisher();
                publisher.publish(event, eventingConfigData);

                if (log.isDebugEnabled()) {
                    log.debug("Web app stats are successfully published to bam for tenant " + tenantID);
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish web app stat events to bam.", e);
        }


    }

/*
    private int getTenantId () {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantID == MultitenantConstants.INVALID_TENANT_ID) {
            AxisConfiguration axisConfiguration = msgContext.getConfigurationContext().getAxisConfiguration();
            tenantID = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        }
        return tenantID;

    }
*/

    private WebappStatEventData getWebappStatEventData(Request request, Response response) {
        //todo get these extracted values from request using a utility method. please check  the comments at extractTenantDomainFromInternalUsername

        WebappStatEventData webappStatEventData = new WebappStatEventData();
        String consumerName = "anonymous.user";
        String consumerTenantDomain = "anonymous.tenant";
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            consumerName = principal.getName();
            try {
                consumerTenantDomain = extractTenantDomainFromInternalUsername(consumerName);
            } catch (Exception e) {
                log.error("Failed to extract tenant domain of user:" + consumerName +
                        ". tenant domain is set as anonymous.tenant only for publishing data to bam.", e);
                consumerTenantDomain = "anonymous.tenant";
            }
        }

        webappStatEventData.setUserId(consumerName);
        webappStatEventData.setUserTenant(consumerTenantDomain);

        String requestedURI = request.getRequestURI();

        // /t/wso2.com/webapps/MySuperWebapp/index.jsp
        if (requestedURI != null) {
            requestedURI = requestedURI.trim();
            String[] requestedUriParts = requestedURI.split("/");
            if (!requestedURI.startsWith("/t/")) {
                if (requestedUriParts.length > 4) {
                    webappStatEventData.setWebappName(requestedUriParts[4]);
                    webappStatEventData.setWebappOwnerTenant(requestedUriParts[2]);
                    //todo get web app version from utility and check the comments at extractTenantDomainFromInternalUsername
                    String webappServletVersion = request.getContext().getEffectiveMajorVersion() + "." +
                            request.getContext().getEffectiveMinorVersion();
                    webappStatEventData.setWebappVersion(webappServletVersion);
                    
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    webappStatEventData.setTimestamp(timestamp);
                    webappStatEventData.setResourcePath(request.getPathInfo()); //request.getServletPath()

                    String[] browserInfo = BrowserInfoUtils.getBrowserInfo(request.getHeader("user-agent"));
                    webappStatEventData.setBrowser(browserInfo[0]);
                    webappStatEventData.setBrowserVersion(browserInfo[1]);
                    webappStatEventData.setOperatingSystem("Other"); //todo get OS data
                    webappStatEventData.setOperatingSystemVersion("Other");

                    webappStatEventData.setHttpMethod(request.getMethod());
                    webappStatEventData.setContentType(request.getContentType());
                    webappStatEventData.setResponseContentType(response.getContentType());
                    webappStatEventData.setResponseHttpStatusCode(response.getStatus());
                    webappStatEventData.setRemoteAddress(request.getRemoteAddr());
                    webappStatEventData.setReferer("referer"); //todo get referer
                    webappStatEventData.setRemoteUser(request.getRemoteUser());
                    webappStatEventData.setAuthType(request.getAuthType());
                    webappStatEventData.setUserAgent(request.getHeader("user-agent"));

                }
            } else {
                webappStatEventData.setWebappOwnerTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                webappStatEventData.setWebappName(requestedUriParts[1]);


            }
        }
        return webappStatEventData;
    }

    private ConfigurationContext getTenantConfigurationContext(String tenantDomain) {
        return TenantAxisUtils.
                getTenantConfigurationContext(tenantDomain, CarbonDataHolder.getServerConfigContext());
    }

    // todo: Due to the additional jars that we have to copy to non osgi environment, we have duplicated code here.
    // todo: with the upcoming release, we can use utility method directly without copying all osgi bundles.
    public static String extractTenantDomainFromInternalUsername(String username) throws Exception {
        if (username == null || "".equals(username.trim()) ||
                !username.contains(UID_REPLACE_CHAR) || "".equals(username.split(UID_REPLACE_CHAR_REGEX)[1].trim())) {
            throw new Exception("Invalid username.");
        }
        return username.split(UID_REPLACE_CHAR_REGEX)[1].trim();
    }

}
