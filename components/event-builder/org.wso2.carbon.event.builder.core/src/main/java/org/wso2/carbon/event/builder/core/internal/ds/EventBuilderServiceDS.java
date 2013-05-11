package org.wso2.carbon.event.builder.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.builder.core.internal.build.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderServiceValueHolder;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="eventBuilderService.component" immediate="true"
 * @scr.reference name="dataBridgeSubscriberService.service"
 * interface="org.wso2.carbon.databridge.core.DataBridgeSubscriberService" cardinality="1..1"
 * policy="dynamic" bind="setDataBridgeSubscriberService" unbind="unsetDataBridgeSubscriberService"
 * @scr.reference name="transportAdaptor.service"
 * interface="org.wso2.carbon.transport.adaptor.core.TransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unsetTransportAdaptorService"
 */
public class EventBuilderServiceDS {
    private static final Log log = LogFactory.getLog(EventBuilderServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventBuilderService eventBuilderService = new CarbonEventBuilderService();
            EventBuilderServiceValueHolder.registerEventBuilderService(eventBuilderService);
            context.getBundleContext().registerService(EventBuilderService.class.getName(), eventBuilderService, null);
            log.info("Successfully deployed EventBuilderService");
        } catch (RuntimeException e) {
            log.error("Could not create EventBuilderService");
        }
    }

    protected void setDataBridgeSubscriberService(DataBridgeSubscriberService dataBridgeSubscriberService) {
        EventBuilderServiceValueHolder.registerDataBridgeSubscriberService(dataBridgeSubscriberService);
    }

    protected void unsetDataBridgeSubscriberService(DataBridgeSubscriberService dataBridgeSubscriberService) {

    }

    protected void setTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        EventBuilderServiceValueHolder.registerTransportAdaptorService(transportAdaptorService);
    }

    protected void unsetTransportAdaptorService(TransportAdaptorService transportAdaptorService) {

    }
}
