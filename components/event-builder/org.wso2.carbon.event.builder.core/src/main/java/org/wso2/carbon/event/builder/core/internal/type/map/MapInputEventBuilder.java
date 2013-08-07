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

package org.wso2.carbon.event.builder.core.internal.type.map;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.BasicEventListener;
import org.wso2.carbon.event.builder.core.EventListener;
import org.wso2.carbon.event.builder.core.Wso2EventListener;
import org.wso2.carbon.event.builder.core.config.EventBuilder;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapInputEventBuilder implements EventBuilder {

    private static final Log log = LogFactory.getLog(MapInputEventBuilder.class);
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private StreamDefinition exportedStreamDefinition = null;
    private int[] attributePositionMap = null;
    private AxisConfiguration axisConfiguration = null;
    private String subscriptionId = null;

    public MapInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration) throws
                                                                     EventBuilderConfigurationException {
        this.axisConfiguration = axisConfiguration;
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        loadEventBuilderConfiguration();
    }

    @Override
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return this.eventBuilderConfiguration;
    }

    @Override
    public void loadEventBuilderConfiguration() {
        if (this.eventBuilderConfiguration != null) {
            String outputStreamName = this.eventBuilderConfiguration.getToStreamName();
            String outputStreamVersion = this.eventBuilderConfiguration.getToStreamVersion();

            try {
                this.exportedStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
            } catch (MalformedStreamDefinitionException e) {
                throw new EventBuilderConfigurationException("Could not create stream definition with " + outputStreamName
                                                             + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + outputStreamVersion);
            }
            createMapping();
            subscribeToTransportAdaptor();
            this.eventBuilderConfiguration.setDeploymentStatus(DeploymentStatus.DEPLOYED);
        }
    }

    @Override
    public void subscribe(EventListener eventListener) throws EventBuilderConfigurationException {
        if (eventListener instanceof BasicEventListener) {
            basicEventListeners.add((BasicEventListener) eventListener);
        } else if (eventListener instanceof Wso2EventListener) {
            wso2EventListeners.add((Wso2EventListener) eventListener);
        }
    }

    @Override
    public void unsubscribe(EventListener eventListener) {
        if (eventListener instanceof BasicEventListener) {
            basicEventListeners.remove(eventListener);
        } else if (eventListener instanceof Wso2EventListener) {
            wso2EventListeners.remove(eventListener);
        }
    }

    @Override
    public StreamDefinition getExportedStreamDefinition() {
        return exportedStreamDefinition;
    }

    @Override
    public void unsubscribeFromTransportAdaptor(
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration) {
        if (this.subscriptionId != null) {
            EventBuilderServiceValueHolder.getInputTransportAdaptorService().unsubscribe(
                    eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(),
                    inputTransportAdaptorConfiguration, axisConfiguration, this.subscriptionId);
            this.subscriptionId = null;
        }
    }

    @Override
    public void subscribeToTransportAdaptor() {
        if (this.subscriptionId == null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(this.axisConfiguration).getTenantId();
            String inputTransportAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName();

            try {
                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration =
                        EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService().getInputTransportAdaptorConfiguration(inputTransportAdaptorName, tenantId);
                this.subscriptionId = EventBuilderServiceValueHolder.getInputTransportAdaptorService().subscribe(
                        inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), new MapInputTransportListener(), axisConfiguration);
            } catch (InputTransportAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input transport adaptor " + inputTransportAdaptorName + ":\n" + e.getMessage());
                throw new EventBuilderConfigurationException(e);
            }
        }
    }

    private void createMapping() {
        MapInputMapping mapInputMapping = (MapInputMapping) this.eventBuilderConfiguration.getInputMapping();
        Map<Integer, Integer> payloadDataMap = new HashMap<Integer, Integer>();

        int posCount = 0;
        for (InputMappingAttribute inputMappingAttribute : mapInputMapping.getInputMappingAttributes()) {
            payloadDataMap.put(inputMappingAttribute.getToStreamPosition(), posCount++);
            this.exportedStreamDefinition.addPayloadData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            if (payloadDataMap.get(inputMappingAttribute.getToStreamPosition()) == null) {
                this.attributePositionMap = null;
                throw new EventBuilderConfigurationException("Error creating map mapping.");
            }
        }

        this.attributePositionMap = new int[payloadDataMap.size()];
        for (int i = 0; i < attributePositionMap.length; i++) {
            attributePositionMap[i] = payloadDataMap.get(i);
        }
    }

    public void sendEvent(Object obj) throws EventBuilderConfigurationException {
        if (eventBuilderConfiguration.isTraceEnabled()) {
            trace.info("[Event-Builder] Received event object:\n " + obj.toString());
        }
        if (attributePositionMap == null) {
            throw new EventBuilderConfigurationException("Input mapping is not available for the current input stream definition:");
        }
        Object[] outObjArray = null;
        if (obj instanceof Map) {
            Map eventMap = (Map) obj;
            List<Object> mapValues = new ArrayList<Object>(eventMap.values());
            List<Object> outObjList = new ArrayList<Object>();
            for (int i = 0; i < this.attributePositionMap.length; i++) {
                outObjList.add(mapValues.get(this.attributePositionMap[i]));
            }
            outObjArray = outObjList.toArray();
        } else {
            throw new EventBuilderConfigurationException("Received event object is not of type map." + this.getClass() + " cannot convert this event.");
        }

        if (!basicEventListeners.isEmpty()) {
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Sending object array " + Arrays.toString(outObjArray) + " to all BasicEventListeners");
            }
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onEvent(outObjArray);
            }
        }
        if (!wso2EventListeners.isEmpty()) {
            Event event = new Event(exportedStreamDefinition.getStreamId(), System.currentTimeMillis(), null, null, outObjArray);
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Sending event object " + event.toString() + " for Wso2EventListeners");
            }
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onEvent(event);
            }
        }
    }

    private void notifyEventDefinitionAddition(Object o) {
        if (eventBuilderConfiguration.isTraceEnabled()) {
            trace.info("[Event-Builder] Notifying event definition addition " + o.toString() + " to event listeners");
        }
        for (BasicEventListener basicEventListener : basicEventListeners) {
            basicEventListener.onAddDefinition(o);
        }
        for (Wso2EventListener wso2EventListener : wso2EventListeners) {
            wso2EventListener.onAddDefinition(o);
        }
        if (eventBuilderConfiguration.isTraceEnabled()) {
            trace.info("[Event-Builder] Successfully notified all registered listeners of adding \n" + o.toString());
        }
    }

    private void notifyEventDefinitionRemoval(Object o) {
        if (eventBuilderConfiguration.isTraceEnabled()) {
            trace.info("[Event-Builder] Notifying event definition removal " + o.toString() + " to event listeners");
        }
        for (BasicEventListener basicEventListener : basicEventListeners) {
            basicEventListener.onRemoveDefinition(o);
        }
        for (Wso2EventListener wso2EventListener : wso2EventListeners) {
            wso2EventListener.onRemoveDefinition(o);
        }
        if (eventBuilderConfiguration.isTraceEnabled()) {
            trace.info("[Event-Builder] Successfully notified all registered listeners of removing \n" + o.toString());
        }
    }

    private class MapInputTransportListener extends InputTransportAdaptorListener {

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
                sendEvent(o);
            } catch (EventBuilderConfigurationException e) {
                throw new InputTransportAdaptorEventProcessingException("Cannot send create an event from input:", e);
            }
        }

    }


}
