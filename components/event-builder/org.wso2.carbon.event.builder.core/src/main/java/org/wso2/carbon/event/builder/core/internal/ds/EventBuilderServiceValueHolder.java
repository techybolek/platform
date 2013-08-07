package org.wso2.carbon.event.builder.core.internal.ds;

import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;

public class EventBuilderServiceValueHolder {
    private static EventBuilderService eventBuilderService;
    private static InputTransportAdaptorManagerService inputTransportAdaptorManagerService;
    private static InputTransportAdaptorService inputTransportAdaptorService;
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

    public static void registerTransportAdaptorService(
            InputTransportAdaptorService transportAdaptorService) {
        EventBuilderServiceValueHolder.inputTransportAdaptorService = transportAdaptorService;
    }

    public static InputTransportAdaptorService getInputTransportAdaptorService() {
        return EventBuilderServiceValueHolder.inputTransportAdaptorService;
    }

    public static void registerTransportAdaptorManagerService(
            InputTransportAdaptorManagerService transportAdaptorManagerService) {
        EventBuilderServiceValueHolder.inputTransportAdaptorManagerService = transportAdaptorManagerService;
    }

    public static InputTransportAdaptorManagerService getInputTransportAdaptorManagerService() {
        return EventBuilderServiceValueHolder.inputTransportAdaptorManagerService;
    }


}
