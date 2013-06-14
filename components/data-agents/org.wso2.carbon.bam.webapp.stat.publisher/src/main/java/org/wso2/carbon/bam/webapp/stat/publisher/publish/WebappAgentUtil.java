package org.wso2.carbon.bam.webapp.stat.publisher.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.Property;
import org.wso2.carbon.bam.webapp.stat.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEvent;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEventData;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps info on whether the webapp stat publishing is enabled or not and
 * the event publisher configurations which includes the data publisher, stream def etc.
 */
public class WebappAgentUtil {

    private static Log log = LogFactory.getLog(WebappAgentUtil.class);

    private static Map<String,EventPublisherConfig> eventPublisherConfigMap =
            new HashMap<String, EventPublisherConfig>();

    private static boolean isPublishingEnabled = false;

    public static void setPublishingEnabled(boolean isPublishingEnabled) {
        WebappAgentUtil.isPublishingEnabled = isPublishingEnabled;
    }

    public static boolean getPublishingEnabled() {
        return isPublishingEnabled;
    }

    public static EventPublisherConfig getEventPublisherConfig(String key) {
        return eventPublisherConfigMap.get(key);
    }

    public static Map<String, EventPublisherConfig> getEventPublisherConfigMap() {
        return eventPublisherConfigMap;
    }

    public static void removeExistingEventPublisherConfigValue(String key) {
        if (eventPublisherConfigMap != null) {
            eventPublisherConfigMap.put(key, null);
        }
    }

    public static WebappStatEvent makeEventList(WebappStatEventData webappStatEventData,
                                      InternalEventingConfigData eventingConfigData) {

//        EventData event = publishData.getEventData();

        List<Object> correlationData = new ArrayList<Object>();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> eventData = new ArrayList<Object>();

//        StatisticsType statisticsType = findTheStatisticType(event);

        addCommonEventData(webappStatEventData, eventData);

        addStatisticEventData(webappStatEventData, eventData);
        addStatisticsMetaData(webappStatEventData, metaData);

        addPropertiesAsMetaData(eventingConfigData, metaData);

        WebappStatEvent publishEvent = new WebappStatEvent();
        publishEvent.setCorrelationData(correlationData);
        publishEvent.setMetaData(metaData);
        publishEvent.setEventData(eventData);

        return publishEvent;
    }

    private static void addPropertiesAsMetaData(InternalEventingConfigData eventingConfigData,
                                                List<Object> metaData) {
        Property[] properties = eventingConfigData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaData.add(property.getValue());
                }
            }
        }
    }

/*
    private static StatisticsType findTheStatisticType(EventData event) {
        StatisticsType statisticsType = null;
        if ((event.getMessageId() != null) &&
                event.getSystemStatistics() == null) {
            statisticsType = StatisticsType.ACTIVITY_STATS;
        } else if (event.getMessageId() == null &&
                event.getSystemStatistics() != null) {
            statisticsType = StatisticsType.SERVICE_STATS;
        } else if ((event.getMessageId() != null) &&
                event.getSystemStatistics() != null) {
            statisticsType = StatisticsType.ACTIVITY_SERVICE_STATS;
        }
        return statisticsType;
    }
*/

/*
    public static StatisticsType findTheStatisticType(InternalEventingConfigData eventingConfigData) {
        StatisticsType statisticsType = null;
        if (!eventingConfigData.isServiceStatsEnable() && eventingConfigData.isMsgDumpingEnable()) {
            statisticsType = StatisticsType.ACTIVITY_STATS;
        } else if (eventingConfigData.isServiceStatsEnable() && !eventingConfigData.isMsgDumpingEnable()) {
            statisticsType = StatisticsType.SERVICE_STATS;
        } else if (eventingConfigData.isMsgDumpingEnable() && eventingConfigData.isServiceStatsEnable()) {
            statisticsType = StatisticsType.ACTIVITY_SERVICE_STATS;
        }
        return statisticsType;
    }
*/

    private static void addCommonEventData(WebappStatEventData event, List<Object> eventData) {
        eventData.add(event.getWebappName());
        eventData.add(event.getWebappOwnerTenant());
        eventData.add(event.getWebappVersion());
        eventData.add(event.getUserId());
        eventData.add(event.getUserTenant());
//        eventData.add(event.getResource());
        eventData.add(1);
//        eventData.add(event.getRequestTime());
    }

/*
    private static void addActivityMetaData(EventData event, List<Object> metaData) {
        // adding server host or more correctly monitored server url
        metaData.add(PublisherUtil.getHostAddress());

        metaData.add(event.getRequestURL());
        metaData.add(event.getRemoteAddress());
        metaData.add(event.getContentType());
        metaData.add(event.getUserAgent());

        metaData.add(event.getReferer());
    }
*/


//    private static void addActivityEventData(EventData event, List<Object> eventData) {
//        eventData.add(event.getMessageId());
//        eventData.add(event.getSOAPHeader());
//        eventData.add(event.getSOAPBody());
//        eventData.add(event.getMessageDirection());
//    }

//    private static void addActivityCorrelationData(EventData event,
//                                                   List<Object> correlationData) {
//        correlationData.add(event.getActivityId());
//    }

/*    private static void addActivityOutEventData(EventData event, List<Object> eventData) {
        eventData.add(event.getOutMessageId());
        eventData.add(event.getOutMessageBody());
    }*/


    private static void addStatisticEventData(WebappStatEventData event, List<Object> eventData) {
//        SystemStatistics systemStatistics = event.getSystemStatistics();
//        eventData.add(systemStatistics.getCurrentInvocationResponseTime());
//        eventData.add(systemStatistics.getCurrentInvocationRequestCount());
//        eventData.add(systemStatistics.getCurrentInvocationResponseCount());
//        eventData.add(systemStatistics.getCurrentInvocationFaultCount());
    }

    private static void addStatisticsMetaData(WebappStatEventData event, List<Object> metaData) {
        metaData.add("external");
/*
        metaData.add(event.getRequestURL());
        metaData.add(event.getRemoteAddress());
        metaData.add(event.getContentType());
        metaData.add(event.getUserAgent());
//        adding server host or more correctly monitored server url
        metaData.add(PublisherUtil.getHostAddress());

        metaData.add(event.getReferer());
*/
    }




    public static void extractInfoFromHttpHeaders(WebappStatEventData eventData, Object requestProperty) {

        if (requestProperty instanceof HttpServletRequest) {
//            HttpServletRequest httpServletRequest = (HttpServletRequest) requestProperty;
//            eventData.setRequestURL(httpServletRequest.getRequestURL().toString());
//            eventData.setRemoteAddress(PublisherUtil.getHostAddress());
//            eventData.setContentType(httpServletRequest.getContentType());
//            eventData.setUserAgent(httpServletRequest.getHeader(
//                    BAMDataPublisherConstants.HTTP_HEADER_USER_AGENT));
        //            eventData.setHost(httpServletRequest.getHeader(
        //                    BAMDataPublisherConstants.HTTP_HEADER_HOST));
//            eventData.setReferer(httpServletRequest.getHeader(
//                    BAMDataPublisherConstants.HTTP_HEADER_REFERER));
        }

    }

    public static BAMServerInfo addBAMServerInfo(InternalEventingConfigData eventingConfigData) {
        BAMServerInfo bamServerInfo = new BAMServerInfo();
        bamServerInfo.setBamServerURL(eventingConfigData.getUrl());
        bamServerInfo.setBamUserName(eventingConfigData.getUserName());
        bamServerInfo.setBamPassword(eventingConfigData.getPassword());
        return bamServerInfo;
    }

}
