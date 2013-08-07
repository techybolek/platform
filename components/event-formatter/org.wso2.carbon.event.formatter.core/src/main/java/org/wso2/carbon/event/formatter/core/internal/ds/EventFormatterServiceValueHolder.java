package org.wso2.carbon.event.formatter.core.internal.ds;

import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.imageio.spi.RegisterableService;
import java.util.ArrayList;
import java.util.List;

public class EventFormatterServiceValueHolder {

    private static OutputTransportAdaptorService outputTransportAdaptorService;
    private static CarbonEventFormatterService carbonEventFormatterService;
    private static OutputTransportAdaptorManagerService outputTransportAdaptorManagerService;
    private static ConfigurationContextService configurationContextService;
    private static RegistryService registryService;
    private static List<EventSource> eventSourceList = new ArrayList<EventSource>();

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
}
