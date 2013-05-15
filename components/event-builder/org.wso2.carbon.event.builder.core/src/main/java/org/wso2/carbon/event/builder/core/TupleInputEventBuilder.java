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

package org.wso2.carbon.event.builder.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.TupleInputMapping;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderServiceValueHolder;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TupleInputEventBuilder implements EventBuilder {
    private static final Log log = LogFactory.getLog(TupleInputEventBuilder.class);
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private Map<TupleInputMapping.InputDataType, int[]> inputDataTypeMap = null;

    public TupleInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
    }

    private void createMapping(StreamDefinition inputStreamDefinition) throws MalformedStreamDefinitionException {
        List<TupleInputMapping> tupleInputMappings = this.eventBuilderConfiguration.getInputMappings();
        this.inputDataTypeMap = new HashMap<TupleInputMapping.InputDataType, int[]>();
        Map<Integer, Integer> payloadDataMap = new TreeMap<Integer, Integer>();
        Map<Integer, Integer> metaDataMap = new TreeMap<Integer, Integer>();
        Map<Integer, Integer> correlationDataMap = new TreeMap<Integer, Integer>();

        for (TupleInputMapping tupleInputMapping : tupleInputMappings) {
            switch (tupleInputMapping.getInputDataType()) {
                case META_DATA:
                    List<Attribute> metaAttributes = inputStreamDefinition.getMetaData();
                    for (int i = 0; i < metaAttributes.size(); i++) {
                        if (metaAttributes.get(i).getName().equals(tupleInputMapping.getInputName())) {
                            metaDataMap.put(tupleInputMapping.getStreamPosition(), i);
                            break;
                        }
                    }
                    if (metaDataMap.get(tupleInputMapping.getStreamPosition()) == null) {
                        this.inputDataTypeMap = null;
                        throw new MalformedStreamDefinitionException("Cannot find a corresponding input attribute '"
                                + tupleInputMapping.getInputName() + "' in stream with id " + inputStreamDefinition.getStreamId());
                    }
                    break;
                case CORRELATION_DATA:
                    List<Attribute> correlationAttributes = inputStreamDefinition.getCorrelationData();
                    for (int i = 0; i < correlationAttributes.size(); i++) {
                        if (correlationAttributes.get(i).getName().equals(tupleInputMapping.getInputName())) {
                            correlationDataMap.put(tupleInputMapping.getStreamPosition(), i);
                            break;
                        }
                    }
                    if (correlationDataMap.get(tupleInputMapping.getStreamPosition()) == null) {
                        this.inputDataTypeMap = null;
                        throw new MalformedStreamDefinitionException("Cannot find a corresponding input attribute '"
                                + tupleInputMapping.getInputName() + "' in stream with id " + inputStreamDefinition.getStreamId());
                    }
                    break;
                case PAYLOAD_DATA:
                default:
                    List<Attribute> payloadAttributes = inputStreamDefinition.getPayloadData();
                    for (int i = 0; i < payloadAttributes.size(); i++) {
                        if (payloadAttributes.get(i).getName().equals(tupleInputMapping.getInputName())) {
                            payloadDataMap.put(tupleInputMapping.getStreamPosition(), i);
                            break;
                        }
                    }
                    if (payloadDataMap.get(tupleInputMapping.getStreamPosition()) == null) {
                        this.inputDataTypeMap = null;
                        throw new MalformedStreamDefinitionException("Cannot find a corresponding input attribute '"
                                + tupleInputMapping.getInputName() + "' in stream with id : " + inputStreamDefinition.getStreamId());
                    }
            }
        }

        int[] payloadPositions = new int[payloadDataMap.size()];
        for (int i = 0; i < payloadPositions.length; i++) {
            payloadPositions[i] = payloadDataMap.get(i);
        }
        inputDataTypeMap.put(TupleInputMapping.InputDataType.PAYLOAD_DATA, payloadPositions);
        int[] metaPositions = new int[metaDataMap.size()];
        for (int i = 0; i < metaPositions.length; i++) {
            metaPositions[i] = metaDataMap.get(i);
        }
        inputDataTypeMap.put(TupleInputMapping.InputDataType.META_DATA, metaPositions);
        int[] correlationPositions = new int[correlationDataMap.size()];
        for (int i = 0; i < correlationPositions.length; i++) {
            correlationPositions[i] = correlationDataMap.get(i);
        }
        inputDataTypeMap.put(TupleInputMapping.InputDataType.CORRELATION_DATA, correlationPositions);
    }

    @Override
    public void subscribe(EventListener eventListener, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        if (eventListener instanceof BasicEventListener) {
            basicEventListeners.add((BasicEventListener) eventListener);
        } else if (eventListener instanceof Wso2EventListener) {
            wso2EventListeners.add((Wso2EventListener) eventListener);
        }
        try {
            //TODO Type is duplicated in many places. Need to refactor
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new TransportAdaptorConfiguration();
            inputTransportAdaptorConfiguration.setType(eventBuilderConfiguration.getType());
            EventBuilderServiceValueHolder.getTransportAdaptorService().subscribe(inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputTransportMessageConfiguration(), new TupleInputTransportListener(), axisConfiguration);
        } catch (TransportAdaptorEventProcessingException e) {
            log.error("Cannot subscribe to " + this.getClass().getName() + ":\n" + e.getMessage());
            throw new EventBuilderConfigurationException(e);
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
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return eventBuilderConfiguration;
    }

    @Override
    public void configureEventBuilder(EventBuilderConfiguration builderConfiguration) {
        this.eventBuilderConfiguration = builderConfiguration;
    }

    public void sendEvent(Object obj) throws EventBuilderConfigurationException {
        for (BasicEventListener basicEventListener : basicEventListeners) {
            sendEvent(basicEventListener, obj);
        }
        for (Wso2EventListener wso2EventListener : wso2EventListeners) {
            sendEvent(wso2EventListener, (Event) obj);
        }
    }

    public void sendEvent(Wso2EventListener wso2EventListener, Event event) {
        wso2EventListener.onEvent(event);
    }

    public void sendEvent(BasicEventListener basicEventListener, Object obj) throws EventBuilderConfigurationException {
        if (inputDataTypeMap == null) {
            throw new EventBuilderConfigurationException("Input mapping is not available for the current input stream definition:");
        }
        Object[] outObjArray = null;
        if (obj instanceof Event) {
            Event event = (Event) obj;
            List<Object> outObjList = new ArrayList<Object>();
            int[] metaPositions = inputDataTypeMap.get(TupleInputMapping.InputDataType.META_DATA);
            for (int i = 0; i < metaPositions.length; i++) {
                outObjList.add(event.getMetaData()[metaPositions[i]]);
            }
            int[] correlationPositions = inputDataTypeMap.get(TupleInputMapping.InputDataType.CORRELATION_DATA);
            for (int i = 0; i < correlationPositions.length; i++) {
                outObjList.add(event.getCorrelationData()[correlationPositions[i]]);
            }
            int[] payloadPositions = inputDataTypeMap.get(TupleInputMapping.InputDataType.PAYLOAD_DATA);
            for (int i = 0; i < payloadPositions.length; i++) {
                outObjList.add(event.getPayloadData()[payloadPositions[i]]);
            }
            outObjArray = outObjList.toArray();
        }

        basicEventListener.onEvent(outObjArray);
    }

    private void notifyEventAddition(Object o) {
        for (BasicEventListener basicEventListener : basicEventListeners) {
            basicEventListener.onAddDefinition(o);
        }
        for (Wso2EventListener wso2EventListener : wso2EventListeners) {
            wso2EventListener.onAddDefinition(o);
        }
    }

    private void notifyEventRemoval(Object o) {
        for (BasicEventListener basicEventListener : basicEventListeners) {
            basicEventListener.onRemoveDefinition(o);
        }
        for (Wso2EventListener wso2EventListener : wso2EventListeners) {
            wso2EventListener.onRemoveDefinition(o);
        }
    }

    private class TupleInputTransportListener implements TransportAdaptorListener {

        @Override
        public void addEventDefinition(Object o) {
            if (o instanceof StreamDefinition) {
                try {
                    createMapping((StreamDefinition) o);
                } catch (MalformedStreamDefinitionException e) {
                    throw new TransportAdaptorEventProcessingException("Cannot create mapping for input stream with id '"
                            + ((StreamDefinition) o).getStreamId() + "':", e);
                }
            }
            notifyEventAddition(o);
        }

        @Override
        public void removeEventDefinition(Object o) {
            notifyEventRemoval(o);
        }

        @Override
        public void onEvent(Object o) {
            try {
                sendEvent(o);
            } catch (EventBuilderConfigurationException e) {
                throw new TransportAdaptorEventProcessingException("Cannot send create an event from input:", e);
            }
        }
    }
}
