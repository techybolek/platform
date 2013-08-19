/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal;


import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventDispatcher;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.config.StreamEventDispatcher;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.type.text.TextInputMapper;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventInputMapper;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.Arrays;

public class EventBuilder {

    private static final Log log = LogFactory.getLog(EventBuilder.class);
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private AxisConfiguration axisConfiguration;
    private StreamDefinition exportedStreamDefinition;
    private InputMapper inputMapper = null;
    private String subscriptionId;
    private StreamEventDispatcher streamEventDispatcher = null;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;

    public EventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                        AxisConfiguration axisConfiguration) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.axisConfiguration = axisConfiguration;
        loadEventBuilderConfiguration();
        this.traceEnabled = eventBuilderConfiguration.isTraceEnabled();
        this.statisticsEnabled = eventBuilderConfiguration.isStatisticsEnabled();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (statisticsEnabled) {
            this.statisticsMonitor = EventBuilderServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventBuilderConstants.EVENT_BUILDER, eventBuilderConfiguration.getEventBuilderName(), null);
            this.beforeTracerPrefix = tenantId + ":" + EventBuilderConstants.EVENT_BUILDER + ":" + eventBuilderConfiguration.getEventBuilderName() + ", before processing " + System.getProperty("line.separator");
            this.afterTracerPrefix = tenantId + ":" + EventBuilderConstants.EVENT_BUILDER + ":" + eventBuilderConfiguration.getEventBuilderName() + ", after processing " + System.getProperty("line.separator");
        }
    }

    public void setStreamEventDispatcher(StreamEventDispatcher streamEventDispatcher) {
        this.streamEventDispatcher = streamEventDispatcher;
    }

    /**
     * Returns the stream definition that is exported by this event builder.
     * This stream definition will available to any object that consumes the event builder service
     * (e.g. EventProcessors)
     *
     * @return the {@link StreamDefinition} of the stream that will be
     *         sending out events from this event builder
     */
    public StreamDefinition getExportedStreamDefinition() {
        return exportedStreamDefinition;
    }

    /**
     * Returns the event builder configuration associated with this event builder
     *
     * @return the {@link EventBuilderConfiguration} instance
     */
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return this.eventBuilderConfiguration;
    }

    /**
     * Sets the event builder configuration. This method will not load the configuration.
     * {@link EventBuilder#loadEventBuilderConfiguration()} needs to be called to load the configuration
     *
     * @param eventBuilderConfiguration the event builder configuration for this event builder
     */
    public void setEventBuilderConfiguration(EventBuilderConfiguration eventBuilderConfiguration) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
    }

    /**
     * Loads an event builder configuration that is already set and valid
     */
    public void loadEventBuilderConfiguration() {
        if (this.eventBuilderConfiguration != null) {
            // Please note that the order of the following statements is important.
            String mappingType = eventBuilderConfiguration.getInputMapping().getMappingType();
            this.inputMapper = EventBuilderServiceValueHolder.getMappingFactoryMap().get(mappingType).constructInputMapper(eventBuilderConfiguration, new EventDispatcherImpl());
            this.exportedStreamDefinition = this.inputMapper.createExportedStreamDefinition();
            //TODO Fix the irregular method call
            if (!(this.inputMapper instanceof Wso2EventInputMapper)) {
                this.inputMapper.createMapping(null);
            }
            subscribeToTransportAdaptor();
            this.eventBuilderConfiguration.setDeploymentStatus(DeploymentStatus.DEPLOYED);
        }
    }

    /**
     * Subscribes to a transport adaptor according to the current event builder configuration
     */
    public void subscribeToTransportAdaptor() {
        if (this.eventBuilderConfiguration == null || this.inputMapper == null || this.exportedStreamDefinition == null) {
            throw new EventBuilderConfigurationException("Cannot subscribe to input transport adaptor. Event builder has not been initialized properly.");
        }
        if (this.subscriptionId == null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(this.axisConfiguration).getTenantId();
            String inputTransportAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName();
            String inputTransportAdaptorType = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorType();
            if (EventBuilderConstants.ITA_EMAIL.equals(inputTransportAdaptorType) && (this.inputMapper instanceof TextInputMapper)) {
                ((TextInputMapper) this.inputMapper).setBinaryInputEnabled(true);
            }
            try {
                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService().getActiveInputTransportAdaptorConfiguration(inputTransportAdaptorName, tenantId);
                this.subscriptionId = EventBuilderServiceValueHolder.getInputTransportAdaptorService().subscribe(inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), new InputTransportListenerImpl(), axisConfiguration);
            } catch (InputTransportAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input transport adaptor " + inputTransportAdaptorName + ":\n" + e.getMessage(), e);
                throw new EventBuilderConfigurationException(e);
            }
        }
    }

    /**
     * Unsubscribes from the input transport adaptor that corresponds to the passed in configuration
     *
     * @param inputTransportAdaptorConfiguration
     *         the configuration of the input transport adaptor from which unsubscribing happens
     */
    public void unsubscribeFromTransportAdaptor(
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration) {
        if (inputTransportAdaptorConfiguration == null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(this.axisConfiguration).getTenantId();
            String inputTransportAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName();
            try {
                inputTransportAdaptorConfiguration = EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService().getActiveInputTransportAdaptorConfiguration(inputTransportAdaptorName, tenantId);
            } catch (InputTransportAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input transport adaptor " + inputTransportAdaptorName + ":\n" + e.getMessage(), e);
                throw new EventBuilderConfigurationException(e);
            }
        }
        if (this.subscriptionId != null) {
            EventBuilderServiceValueHolder.getInputTransportAdaptorService().unsubscribe(eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), inputTransportAdaptorConfiguration, axisConfiguration, this.subscriptionId);
            this.subscriptionId = null;
        }
    }

    protected void processEvent(Object obj) {
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + obj.toString());
        }
        this.inputMapper.processInputEvent(obj);
    }

    protected void sendEvent(Object[] outObjArray) {
        if (traceEnabled) {
            trace.info(afterTracerPrefix + Arrays.toString(outObjArray));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        this.streamEventDispatcher.dispatchEvent(outObjArray);
    }

    protected void notifyEventDefinitionAddition(Object definition) {
        if (log.isDebugEnabled()) {
            log.debug("EventBuilder: " + eventBuilderConfiguration.getEventBuilderName() + ", notifying event definition addition :" + definition.toString());
        }
        //TODO expose the event additions in a better way
        if (definition instanceof StreamDefinition) {
            StreamDefinition inputStreamDefinition = (StreamDefinition) definition;
            this.inputMapper.validateInputStreamAttributes(inputStreamDefinition);
            this.inputMapper.createMapping(definition);
        }
    }

    protected void notifyEventDefinitionRemoval(Object definition) {
        if (log.isDebugEnabled()) {
            log.debug("EventBuilder: " + eventBuilderConfiguration.getEventBuilderName() + ", notifying event definition addition :" + definition.toString());
        }
        //TODO expose event removal in a better way
    }

    private class InputTransportListenerImpl extends InputTransportAdaptorListener {

        @Override
        public void addEventDefinition(Object o) {
            notifyEventDefinitionAddition(o);
        }

        @Override
        public void removeEventDefinition(Object o) {
            notifyEventDefinitionRemoval(o);
        }

        @Override
        public void onEvent(Object o) {
            try {
                processEvent(o);
            } catch (EventBuilderConfigurationException e) {
                throw new InputTransportAdaptorEventProcessingException("Cannot send create an event from input:", e);
            }
        }
    }

    private class EventDispatcherImpl implements EventDispatcher {
        @Override
        public void dispatchEvent(Object[] convertedEventObjArray) {
            sendEvent(convertedEventObjArray);
        }
    }


}
