package org.wso2.carbon.event.formatter.core.internal.ds;

import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.config.OutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.event.formatter.core.internal.type.json.JSONOutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.map.MapOutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.text.TextOutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.wso2event.WSO2OutputMapperFactory;
import org.wso2.carbon.event.formatter.core.internal.type.xml.XMLOutputMapperFactory;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.output.transport.adaptor.core.MessageType;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EventFormatterServiceValueHolder {

    private static OutputTransportAdaptorService outputTransportAdaptorService;
    private static CarbonEventFormatterService carbonEventFormatterService;
    private static OutputTransportAdaptorManagerService outputTransportAdaptorManagerService;
    private static ConfigurationContextService configurationContextService;
    private static RegistryService registryService;
    private static List<EventSource> eventSourceList = new ArrayList<EventSource>();
    private static ConcurrentHashMap<String, OutputMapperFactory> mappingFactoryMap = new ConcurrentHashMap<String, OutputMapperFactory>() {
    };

    static {
        mappingFactoryMap.put(MessageType.MAP, new MapOutputMapperFactory());
        mappingFactoryMap.put(MessageType.TEXT, new TextOutputMapperFactory());
        mappingFactoryMap.put(MessageType.WSO2EVENT, new WSO2OutputMapperFactory());
        mappingFactoryMap.put(MessageType.XML, new XMLOutputMapperFactory());
        mappingFactoryMap.put(MessageType.JSON, new JSONOutputMapperFactory());
    }

    private static EventStatisticsService eventStatisticsService;

    private EventFormatterServiceValueHolder() {

    }

    public static CarbonEventFormatterService getCarbonEventFormatterService() {
        return carbonEventFormatterService;
    }

    public static void registerFormatterService(EventFormatterService eventFormatterService) {
        EventFormatterServiceValueHolder.carbonEventFormatterService = (CarbonEventFormatterService) eventFormatterService;

    }

    public static void registerTransportAdaptorService(
            OutputTransportAdaptorService transportAdaptorService) {
        EventFormatterServiceValueHolder.outputTransportAdaptorService = transportAdaptorService;
    }

    public static OutputTransportAdaptorService getOutputTransportAdaptorService() {
        return EventFormatterServiceValueHolder.outputTransportAdaptorService;
    }

    public static void registerTransportAdaptorManagerService(
            OutputTransportAdaptorManagerService transportAdaptorManagerService) {
        EventFormatterServiceValueHolder.outputTransportAdaptorManagerService = transportAdaptorManagerService;
    }

    public static OutputTransportAdaptorManagerService getOutputTransportAdaptorManagerService() {
        return EventFormatterServiceValueHolder.outputTransportAdaptorManagerService;
    }

    public static void unRegisterTransportAdaptorManagerService(
            OutputTransportAdaptorManagerService transportAdaptorManagerService) {
        EventFormatterServiceValueHolder.outputTransportAdaptorManagerService = null;
    }

    public static void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventFormatterServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void addEventSource(EventSource eventSource) {
        eventSourceList.add(eventSource);

    }

    public static void removeEventSource(EventSource eventSource) {
        eventSourceList.remove(eventSource);
    }

    public static List<EventSource> getEventSourceList() {
        return eventSourceList;
    }

    public static void setRegistryService(RegistryService registryService) {
        EventFormatterServiceValueHolder.registryService = registryService;
    }

    public static void unSetRegistryService() {
        EventFormatterServiceValueHolder.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return EventFormatterServiceValueHolder.registryService;
    }

    public static Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static ConcurrentHashMap<String, OutputMapperFactory> getMappingFactoryMap() {
        return mappingFactoryMap;
    }

    public static void registerEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventFormatterServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }
}
