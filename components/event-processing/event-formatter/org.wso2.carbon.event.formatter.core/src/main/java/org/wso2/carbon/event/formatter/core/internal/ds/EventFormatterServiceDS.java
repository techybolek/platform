package org.wso2.carbon.event.formatter.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.EventSourceNotificationListener;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.event.formatter.core.internal.TransportAdaptorNotificationListenerImpl;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="eventFormatterService.component" immediate="true"
 * @scr.reference name="transportAdaptor.service"
 * interface="org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unsetTransportAdaptorService"
 * @scr.reference name="transportmanager.service"
 * interface="org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorManagerService" unbind="unSetTransportAdaptorManagerService"
 * @scr.reference name="event.source.service"
 * interface="org.wso2.carbon.event.formatter.core.EventSource" cardinality="0..n"
 * policy="dynamic" bind="notifyNewEventSource" unbind="notifyRemovalEventSource"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 */
public class EventFormatterServiceDS {
    private static final Log log = LogFactory.getLog(EventFormatterServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventFormatterService eventFormatterService = createTransportAdaptorManagerService();
            context.getBundleContext().registerService(EventFormatterService.class.getName(), eventFormatterService, null);
            EventFormatterServiceValueHolder.getOutputTransportAdaptorManagerService().registerDeploymentNotifier(new TransportAdaptorNotificationListenerImpl());
            log.info("Successfully deployed EventFormatterService");
        } catch (RuntimeException e) {
            log.error("Could not create EventFormatterService : "+e.getMessage(),e);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            log.error("Could not register deployment notifier to transport adaptor service");
        }
    }

    protected void setTransportAdaptorService(
            OutputTransportAdaptorService transportAdaptorService) {
        EventFormatterServiceValueHolder.registerTransportAdaptorService(transportAdaptorService);
    }

    protected void unsetTransportAdaptorService(
            OutputTransportAdaptorService transportAdaptorService) {

    }

    protected void setTransportAdaptorManagerService(
            OutputTransportAdaptorManagerService transportAdaptorManagerService) {
        EventFormatterServiceValueHolder.registerTransportAdaptorManagerService(transportAdaptorManagerService);
    }

    protected void unSetTransportAdaptorManagerService(
            OutputTransportAdaptorManagerService transportAdaptorManagerService) {
        EventFormatterServiceValueHolder.unRegisterTransportAdaptorManagerService(transportAdaptorManagerService);

    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventFormatterServiceValueHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventFormatterServiceValueHolder.registerConfigurationContextService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventFormatterServiceValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventFormatterServiceValueHolder.unSetRegistryService();
    }

    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventFormatterServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventFormatterServiceValueHolder.registerEventStatisticsService(null);
    }

    private EventFormatterService createTransportAdaptorManagerService()
            throws EventFormatterConfigurationException {
        CarbonEventFormatterService carbonEventFormatterService = new CarbonEventFormatterService();
        EventFormatterServiceValueHolder.registerFormatterService(carbonEventFormatterService);
        return carbonEventFormatterService;
    }

    protected void notifyNewEventSource(
            EventSource eventSource)
            throws EventFormatterConfigurationException {
        EventFormatterServiceValueHolder.addEventSource(eventSource);
        eventSource.subscribeNotificationListener(new EventSourceNotificationListener());
    }

    protected void notifyRemovalEventSource(
            EventSource eventSource) {

    }
}
