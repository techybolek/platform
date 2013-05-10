package org.wso2.carbon.event.builder.core.internal.util;

import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;

public class EventBuilderServiceValueHolder {
    private static EventBuilderService eventBuilderService;
    private static DataBridgeSubscriberService dataBridgeSubscriberService;
    private static TransportAdaptorService transportAdaptorService;
    private static CarbonEventBuilderService carbonEventBuilderService;

    private EventBuilderServiceValueHolder() {

    }

    public static CarbonEventBuilderService getCarbonEventBuilderService() {
        return carbonEventBuilderService;
    }

    public static void registerEventBuilderService(EventBuilderService eventBuilderService) {
        EventBuilderServiceValueHolder.eventBuilderService = eventBuilderService;
        if (eventBuilderService instanceof CarbonEventBuilderService) {
            EventBuilderServiceValueHolder.carbonEventBuilderService = (CarbonEventBuilderService) eventBuilderService;
        }
    }

    public static EventBuilderService getEventBuilderService() {
        return EventBuilderServiceValueHolder.eventBuilderService;
    }

    public static void registerDataBridgeSubscriberService(DataBridgeSubscriberService dataBridgeSubscriberService) {
        EventBuilderServiceValueHolder.dataBridgeSubscriberService = dataBridgeSubscriberService;
    }

    public static DataBridgeSubscriberService getDataBridgeSubscriberService() {
        return EventBuilderServiceValueHolder.dataBridgeSubscriberService;
    }

    public static void registerTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        EventBuilderServiceValueHolder.transportAdaptorService = transportAdaptorService;
    }

    public static TransportAdaptorService getTransportAdaptorService() {
        return EventBuilderServiceValueHolder.transportAdaptorService;
    }
}
