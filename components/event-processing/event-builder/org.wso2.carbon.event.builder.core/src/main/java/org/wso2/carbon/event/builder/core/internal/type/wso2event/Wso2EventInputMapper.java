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

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventDispatcher;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Wso2EventInputMapper implements InputMapper {
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private StreamDefinition exportedStreamDefinition = null;
    private EventDispatcher eventDispatcher = null;
    private Map<InputDataType, int[]> inputDataTypeSpecificPositionMap = null;

    public Wso2EventInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                                EventDispatcher eventDispatcher) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void validateInputStreamAttributes(StreamDefinition inputStreamDefinition)
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
                throw new EventBuilderConfigurationException("Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ");
            }
        }

        for (InputMappingAttribute inputMappingAttribute : correlationInputMappingAttributes) {
            if (!inputStreamCorrelationAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                throw new EventBuilderConfigurationException("Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ");
            }
        }

        for (InputMappingAttribute inputMappingAttribute : payloadInputMappingAttributes) {
            if (!inputStreamPayloadAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                throw new EventBuilderConfigurationException("Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ");
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

    @Override
    public boolean isStreamDefinitionValidForConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            StreamDefinition exportedStreamDefinition) {
        if (!(eventBuilderConfiguration.getToStreamName().equals(exportedStreamDefinition.getName())
              && eventBuilderConfiguration.getToStreamVersion().equals(exportedStreamDefinition.getVersion()))) {
            return false;
        }
        if (eventBuilderConfiguration.getInputMapping() instanceof Wso2EventInputMapping) {
            Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getMetaInputMappingAttributes()) {
                Attribute attribute = new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
                if (!exportedStreamDefinition.getMetaData().contains(attribute)) {
                    return false;
                }
            }
            for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getCorrelationInputMappingAttributes()) {
                Attribute attribute = new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
                if (!exportedStreamDefinition.getCorrelationData().contains(attribute)) {
                    return false;
                }
            }
            for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getPayloadInputMappingAttributes()) {
                Attribute attribute = new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
                if (!exportedStreamDefinition.getPayloadData().contains(attribute)) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public StreamDefinition createExportedStreamDefinition() {
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
            EventBuilderUtil.addAttributesToStreamDefinition(toStreamDefinition, wso2EventInputMapping.getMetaInputMappingAttributes(), EventBuilderConstants.META_DATA_VAL);
            EventBuilderUtil.addAttributesToStreamDefinition(toStreamDefinition, wso2EventInputMapping.getCorrelationInputMappingAttributes(), EventBuilderConstants.CORRELATION_DATA_VAL);
            EventBuilderUtil.addAttributesToStreamDefinition(toStreamDefinition, wso2EventInputMapping.getPayloadInputMappingAttributes(), EventBuilderConstants.PAYLOAD_DATA_VAL);
        }
        this.exportedStreamDefinition = toStreamDefinition;
        return toStreamDefinition;
    }

    @Override
    public void createMapping(Object eventDefinition) throws EventBuilderConfigurationException {
        if (eventDefinition instanceof StreamDefinition) {
            StreamDefinition inputStreamDefinition = (StreamDefinition) eventDefinition;
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
                    throw new EventBuilderConfigurationException("Cannot find a corresponding meta data input attribute '"
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
                    throw new EventBuilderConfigurationException("Cannot find a corresponding correlation data input attribute '"
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
                    throw new EventBuilderConfigurationException("Cannot find a corresponding payload data input attribute '"
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
            if (!isStreamDefinitionValidForConfiguration(eventBuilderConfiguration, exportedStreamDefinition)) {
                throw new EventBuilderValidationException("Exported stream definition does not match the specified configuration.");
            }
        }
    }

    @Override
    public void processInputEvent(Object obj) {
        Object[] outObjArray = null;
        if (obj instanceof Event) {
            Event event = (Event) obj;
            Map<String, String> arbitraryMap = event.getArbitraryDataMap();
            if (arbitraryMap != null && !arbitraryMap.isEmpty()) {
                outObjArray = processArbitraryMap(event);
            } else {
                if (inputDataTypeSpecificPositionMap == null) {
                    throw new EventBuilderConfigurationException("Input mapping is not available for the current input stream definition:");
                }
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
            }
        }
        eventDispatcher.dispatchEvent(outObjArray);
    }

    private Object[] processArbitraryMap(Event inEvent) {
        Map<String, String> arbitraryMap = inEvent.getArbitraryDataMap();
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
                metaData[i] = inEvent.getMetaData()[i] != null ? inEvent.getMetaData()[i] : null;
            }
        }
        for (int i = 0; i < correlationData.length; i++) {
            Attribute correlationAttribute = exportedStreamDefinition.getCorrelationData().get(i);
            String value = arbitraryMap.get(EventBuilderConstants.CORRELATION_DATA_PREFIX + correlationAttribute.getName());
            if (value != null) {
                Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, correlationAttribute.getType());
                correlationData[i] = attributeObj;
            } else {
                correlationData[i] = inEvent.getCorrelationData()[i] != null ? inEvent.getCorrelationData()[i] : null;
            }
        }
        for (int i = 0; i < payloadData.length; i++) {
            Attribute payloadAttribute = exportedStreamDefinition.getPayloadData().get(i);
            String value = arbitraryMap.get(payloadAttribute.getName());
            if (value != null) {
                Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, payloadAttribute.getType());
                payloadData[i] = attributeObj;
            } else {
                payloadData[i] = inEvent.getPayloadData()[i] != null ? inEvent.getPayloadData()[i] : null;
            }
        }
        int indexCount = 0;
        Object[] outObjArray = new Object[metaData.length + correlationData.length + payloadData.length];
        for (Object attributeObj : metaData) {
            outObjArray[indexCount++] = attributeObj;
        }
        for (Object attributeObj : correlationData) {
            outObjArray[indexCount++] = attributeObj;
        }
        for (Object attributeObj : payloadData) {
            outObjArray[indexCount++] = attributeObj;
        }

        return outObjArray;
    }

    private enum InputDataType {
        PAYLOAD_DATA, META_DATA, CORRELATION_DATA
    }
}
