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

package org.wso2.carbon.event.builder.core.internal.type.wso2event;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
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
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Wso2EventInputEventBuilder implements EventBuilder {

    private static final Log log = LogFactory.getLog(Wso2EventInputEventBuilder.class);
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private Map<InputDataType, int[]> inputDataTypeSpecificPositionMap = null;
    private AxisConfiguration axisConfiguration;
    private StreamDefinition exportedStreamDefinition;
    private String subscriptionId;

    public Wso2EventInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                      AxisConfiguration axisConfiguration) throws
                                                                           EventBuilderConfigurationException {
        this.axisConfiguration = axisConfiguration;
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        loadEventBuilderConfiguration();
    }

    public StreamDefinition getExportedStreamDefinition() {
        return exportedStreamDefinition;
    }

    public void subscribeToTransportAdaptor() {
        if (this.subscriptionId == null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(this.axisConfiguration).getTenantId();
            String inputTransportAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName();

            try {
                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService().getInputTransportAdaptorConfiguration(inputTransportAdaptorName, tenantId);
                this.subscriptionId = EventBuilderServiceValueHolder.getInputTransportAdaptorService().subscribe(inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), new Wso2EventInputTransportListener(), axisConfiguration);
            } catch (InputTransportAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input transport adaptor " + inputTransportAdaptorName + ":\n" + e.getMessage());
                throw new EventBuilderConfigurationException(e);
            }
        }
    }

    @Override
    public void subscribe(EventListener eventListener)
            throws EventBuilderConfigurationException {
        if (eventListener instanceof BasicEventListener) {
            basicEventListeners.add((BasicEventListener) eventListener);
        } else if (eventListener instanceof Wso2EventListener) {
            wso2EventListeners.add((Wso2EventListener) eventListener);
        }
    }

    public void unsubscribeFromTransportAdaptor(
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration) {
        if (this.subscriptionId != null) {
            EventBuilderServiceValueHolder.getInputTransportAdaptorService().unsubscribe(eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), inputTransportAdaptorConfiguration, axisConfiguration, this.subscriptionId);
            this.subscriptionId = null;
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

    private void validateExpectedAttributesWith(StreamDefinition inputStreamDefinition)
            throws EventBuilderConfigurationException {

        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> metaInputMappingAttributes = wso2EventInputMapping.getMetaInputMappingAttributes();
        List<InputMappingAttribute> correlationInputMappingAttributes = wso2EventInputMapping.getCorrelationInputMappingAttributes();
        List<InputMappingAttribute> payloadInputMappingAttributes = wso2EventInputMapping.getPayloadInputMappingAttributes();

        List<String> inputStreamMetaAttributeNames = getAttributeNamesList(inputStreamDefinition.getMetaData());
        List<String> inputStreamCorrelationAttributeNames = getAttributeNamesList(inputStreamDefinition.getCorrelationData());
        List<String> inputStreamPayloadAttributeNames = getAttributeNamesList(inputStreamDefinition.getPayloadData());

        for (InputMappingAttribute inputMappingAttribute : metaInputMappingAttributes) {
            if (!inputStreamMetaAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                String errMsg = "Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ";
                log.error(errMsg);
                throw new EventBuilderConfigurationException(errMsg);
            }
        }

        for (InputMappingAttribute inputMappingAttribute : correlationInputMappingAttributes) {
            if (!inputStreamCorrelationAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                String errMsg = "Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ";
                log.error(errMsg);
                throw new EventBuilderConfigurationException(errMsg);
            }
        }

        for (InputMappingAttribute inputMappingAttribute : payloadInputMappingAttributes) {
            if (!inputStreamPayloadAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                String errMsg = "Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ";
                log.error(errMsg);
                throw new EventBuilderConfigurationException(errMsg);
            }
        }
    }

    private List<String> getAttributeNamesList(List<Attribute> attributeList) {
        List<String> attributeNamesList = new ArrayList<String>();
        if (attributeList != null) {
            for (Attribute attribute : attributeList) {
                attributeNamesList.add(attribute.getName());
            }
        }

        return attributeNamesList;
    }

    private void addAttributeToStreamDefinition(StreamDefinition streamDefinition,
                                                List<InputMappingAttribute> wso2InputMappingAttributeList,
                                                String propertyType) {
        if (propertyType.equals("meta")) {
            for (InputMappingAttribute wso2InputMappingAttribute : wso2InputMappingAttributeList) {
                streamDefinition.addMetaData(wso2InputMappingAttribute.getToElementKey(), wso2InputMappingAttribute.getToElementType());
            }
        } else if (propertyType.equals("correlation")) {
            for (InputMappingAttribute wso2InputMappingAttribute : wso2InputMappingAttributeList) {
                streamDefinition.addCorrelationData(wso2InputMappingAttribute.getToElementKey(), wso2InputMappingAttribute.getToElementType());
            }
        } else if (propertyType.equals("payload")) {
            for (InputMappingAttribute wso2InputMappingAttribute : wso2InputMappingAttributeList) {
                streamDefinition.addPayloadData(wso2InputMappingAttribute.getToElementKey(), wso2InputMappingAttribute.getToElementType());
            }
        }
    }

    public void sendEvent(Object obj) throws EventBuilderConfigurationException {
        if (eventBuilderConfiguration.isTraceEnabled()) {
            trace.info("[Event-Builder] Received event object:\n " + obj.toString());
        }
        Event event = (Event) obj;
        Map<String, String> arbitraryMap = event.getArbitraryDataMap();
        if (arbitraryMap != null && !arbitraryMap.isEmpty()) {
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.debug("[Event-Builder] Received arbitrary map :" + arbitraryMap.toString());
            }
            Object[] metaData = new Object[exportedStreamDefinition.getMetaData().size()];
            Object[] correlationData = new Object[exportedStreamDefinition.getCorrelationData().size()];
            Object[] payloadData = new Object[exportedStreamDefinition.getPayloadData().size()];
            for (int i = 0; i < metaData.length; i++) {
                Attribute metaAttribute = exportedStreamDefinition.getMetaData().get(i);
                String value = arbitraryMap.get(EventBuilderConstants.META_DATA_PREFIX + metaAttribute.getName());
                if (value != null) {
                    Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, metaAttribute.getType());
                    metaData[i] = attributeObj;
                } else {
                    metaData[i] = event.getMetaData()[i] != null ? event.getMetaData()[i] : null;
                }
            }
            for (int i = 0; i < correlationData.length; i++) {
                Attribute correlationAttribute = exportedStreamDefinition.getCorrelationData().get(i);
                String value = arbitraryMap.get(EventBuilderConstants.CORRELATION_DATA_PREFIX + correlationAttribute.getName());
                if (value != null) {
                    Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, correlationAttribute.getType());
                    correlationData[i] = attributeObj;
                } else {
                    correlationData[i] = event.getCorrelationData()[i] != null ? event.getCorrelationData()[i] : null;
                }
            }
            for (int i = 0; i < payloadData.length; i++) {
                Attribute payloadAttribute = exportedStreamDefinition.getPayloadData().get(i);
                String value = arbitraryMap.get(payloadAttribute.getName());
                if (value != null) {
                    Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, payloadAttribute.getType());
                    payloadData[i] = attributeObj;
                } else {
                    payloadData[i] = event.getPayloadData()[i] != null ? event.getPayloadData()[i] : null;
                }
            }
            event.setMetaData(metaData);
            event.setCorrelationData(correlationData);
            event.setPayloadData(payloadData);
        }

        if (!basicEventListeners.isEmpty()) {
            if (inputDataTypeSpecificPositionMap == null) {
                throw new EventBuilderConfigurationException("Input mapping is not available for the current input stream definition:");
            }
            Object[] outObjArray = null;
            if (obj instanceof Event) {
                List<Object> outObjList = new ArrayList<Object>();
                int[] metaPositions = inputDataTypeSpecificPositionMap.get(InputDataType.META_DATA);
                for (int i = 0; i < metaPositions.length; i++) {
                    outObjList.add(event.getMetaData()[metaPositions[i]]);
                }
                int[] correlationPositions = inputDataTypeSpecificPositionMap.get(InputDataType.CORRELATION_DATA);
                for (int i = 0; i < correlationPositions.length; i++) {
                    outObjList.add(event.getCorrelationData()[correlationPositions[i]]);
                }
                int[] payloadPositions = inputDataTypeSpecificPositionMap.get(InputDataType.PAYLOAD_DATA);
                for (int i = 0; i < payloadPositions.length; i++) {
                    outObjList.add(event.getPayloadData()[payloadPositions[i]]);
                }
                outObjArray = outObjList.toArray();
                if (eventBuilderConfiguration.isTraceEnabled()) {
                    trace.info("[Event-Builder] Sending array object " + Arrays.toString(outObjArray) + " for BasicEventListeners");
                }
            }
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onEvent(outObjArray);
            }
        }
        if (!wso2EventListeners.isEmpty()) {
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Sending event object " + event.toString() + " for Wso2EventListeners");
            }
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onEvent(event);
            }
        }
    }

    @Override
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return this.eventBuilderConfiguration;
    }

    @Override
    public void loadEventBuilderConfiguration() {
        if (this.eventBuilderConfiguration != null) {
            String inputStreamName = this.eventBuilderConfiguration.getToStreamName();
            String inputStreamVersion = this.eventBuilderConfiguration.getToStreamVersion();
            StreamDefinition toStreamDefinition;
            Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) this.eventBuilderConfiguration.getInputMapping();
            try {
                toStreamDefinition = new StreamDefinition(this.eventBuilderConfiguration.getToStreamName(), this.eventBuilderConfiguration.getToStreamVersion());
            } catch (MalformedStreamDefinitionException e) {
                throw new EventBuilderConfigurationException("Could not create stream definition with " + inputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + inputStreamVersion);
            }

            if (wso2EventInputMapping != null) {
                addAttributeToStreamDefinition(toStreamDefinition, wso2EventInputMapping.getMetaInputMappingAttributes(), EventBuilderConstants.META_DATA_VAL);
                addAttributeToStreamDefinition(toStreamDefinition, wso2EventInputMapping.getCorrelationInputMappingAttributes(), EventBuilderConstants.CORRELATION_DATA_VAL);
                addAttributeToStreamDefinition(toStreamDefinition, wso2EventInputMapping.getPayloadInputMappingAttributes(), EventBuilderConstants.PAYLOAD_DATA_VAL);
            }

            this.exportedStreamDefinition = toStreamDefinition;

            subscribeToTransportAdaptor();
            this.eventBuilderConfiguration.setDeploymentStatus(DeploymentStatus.DEPLOYED);
        }
    }

    private void createMapping(StreamDefinition inputStreamDefinition)
            throws MalformedStreamDefinitionException {
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) this.eventBuilderConfiguration.getInputMapping();
        this.inputDataTypeSpecificPositionMap = new HashMap<InputDataType, int[]>();
        Map<Integer, Integer> payloadDataMap = new TreeMap<Integer, Integer>();
        Map<Integer, Integer> metaDataMap = new TreeMap<Integer, Integer>();
        Map<Integer, Integer> correlationDataMap = new TreeMap<Integer, Integer>();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getMetaInputMappingAttributes()) {
            List<Attribute> metaAttributes = inputStreamDefinition.getMetaData();
            for (int i = 0; i < metaAttributes.size(); i++) {
                if (metaAttributes.get(i).getName().equals(inputMappingAttribute.getFromElementKey())) {
                    metaDataMap.put(inputMappingAttribute.getToStreamPosition(), i);
                    break;
                }
            }
            if (metaDataMap.get(inputMappingAttribute.getToStreamPosition()) == null) {
                this.inputDataTypeSpecificPositionMap = null;
                throw new MalformedStreamDefinitionException("Cannot find a corresponding meta data input attribute '"
                                                             + inputMappingAttribute.getFromElementKey() + "' in stream with id " + inputStreamDefinition.getStreamId());
            }
        }

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getCorrelationInputMappingAttributes()) {
            List<Attribute> correlationAttributes = inputStreamDefinition.getCorrelationData();
            for (int i = 0; i < correlationAttributes.size(); i++) {
                if (correlationAttributes.get(i).getName().equals(inputMappingAttribute.getFromElementKey())) {
                    correlationDataMap.put(inputMappingAttribute.getToStreamPosition(), i);
                    break;
                }
            }
            if (correlationDataMap.get(inputMappingAttribute.getToStreamPosition()) == null) {
                this.inputDataTypeSpecificPositionMap = null;
                throw new MalformedStreamDefinitionException("Cannot find a corresponding correlation data input attribute '"
                                                             + inputMappingAttribute.getFromElementKey() + "' in stream with id " + inputStreamDefinition.getStreamId());
            }

        }

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getPayloadInputMappingAttributes()) {
            List<Attribute> payloadAttributes = inputStreamDefinition.getPayloadData();
            for (int i = 0; i < payloadAttributes.size(); i++) {
                if (payloadAttributes.get(i).getName().equals(inputMappingAttribute.getFromElementKey())) {
                    payloadDataMap.put(inputMappingAttribute.getToStreamPosition(), i);
                    break;
                }
            }
            if (payloadDataMap.get(inputMappingAttribute.getToStreamPosition()) == null) {
                this.inputDataTypeSpecificPositionMap = null;
                throw new MalformedStreamDefinitionException("Cannot find a corresponding payload data input attribute '"
                                                             + inputMappingAttribute.getFromElementKey() + "' in stream with id : " + inputStreamDefinition.getStreamId());
            }
        }

        int[] payloadPositions = new int[payloadDataMap.size()];
        for (int i = 0; i < payloadPositions.length; i++) {
            payloadPositions[i] = payloadDataMap.get(i);
        }
        inputDataTypeSpecificPositionMap.put(InputDataType.PAYLOAD_DATA, payloadPositions);
        int[] metaPositions = new int[metaDataMap.size()];
        for (int i = 0; i < metaPositions.length; i++) {
            metaPositions[i] = metaDataMap.get(i);
        }
        inputDataTypeSpecificPositionMap.put(InputDataType.META_DATA, metaPositions);
        int[] correlationPositions = new int[correlationDataMap.size()];
        for (int i = 0; i < correlationPositions.length; i++) {
            correlationPositions[i] = correlationDataMap.get(i);
        }
        inputDataTypeSpecificPositionMap.put(InputDataType.CORRELATION_DATA, correlationPositions);
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

    private enum InputDataType {
        PAYLOAD_DATA, META_DATA, CORRELATION_DATA
    }

    private class Wso2EventInputTransportListener extends InputTransportAdaptorListener {

        @Override
        public void addEventDefinition(Object o) {
            if (o instanceof StreamDefinition) {
                try {
                    StreamDefinition inputStreamDefinition = (StreamDefinition) o;
                    validateExpectedAttributesWith(inputStreamDefinition);
                    createMapping(inputStreamDefinition);
                } catch (MalformedStreamDefinitionException e) {
                    throw new InputTransportAdaptorEventProcessingException("Cannot create mapping for input stream with id '"
                                                                            + ((StreamDefinition) o).getStreamId() + "':", e);
                }
            }
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
