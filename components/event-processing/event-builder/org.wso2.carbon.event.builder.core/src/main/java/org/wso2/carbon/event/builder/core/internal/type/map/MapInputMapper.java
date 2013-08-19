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

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventDispatcher;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapInputMapper implements InputMapper {
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private StreamDefinition exportedStreamDefinition = null;
    private EventDispatcher eventDispatcher = null;
    private int[] attributePositionMap = null;

    public MapInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                          EventDispatcher eventDispatcher) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void processInputEvent(Object obj) {
        if (attributePositionMap == null) {
            throw new EventBuilderConfigurationException("Input mapping is not available for the current input stream definition:");
        }
        Object[] outObjArray;
        if (obj instanceof Map) {
            Map eventMap = (Map) obj;
            List<Object> mapValues = new ArrayList<Object>(eventMap.values());
            List<Object> outObjList = new ArrayList<Object>();
            for (int i = 0; i < this.attributePositionMap.length; i++) {
                outObjList.add(mapValues.get(this.attributePositionMap[i]));
            }
            outObjArray = outObjList.toArray();
            eventDispatcher.dispatchEvent(outObjArray);
        } else {
            throw new EventBuilderConfigurationException("Received event object is not of type map." + this.getClass() + " cannot convert this event.");
        }
    }

    @Override
    public boolean isStreamDefinitionValidForConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            StreamDefinition exportedStreamDefinition) {
        if (!(eventBuilderConfiguration.getToStreamName().equals(exportedStreamDefinition.getName())
              && eventBuilderConfiguration.getToStreamVersion().equals(exportedStreamDefinition.getVersion()))) {
            return false;
        }
        if (eventBuilderConfiguration.getInputMapping() instanceof MapInputMapping) {
            MapInputMapping mapInputMapping = (MapInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : mapInputMapping.getInputMappingAttributes()) {
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
        MapInputMapping mapInputMapping = (MapInputMapping) this.eventBuilderConfiguration.getInputMapping();
        try {
            toStreamDefinition = new StreamDefinition(this.eventBuilderConfiguration.getToStreamName(), this.eventBuilderConfiguration.getToStreamVersion());
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException("Could not create stream definition with " + inputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + inputStreamVersion);
        }

        if (mapInputMapping != null) {
            EventBuilderUtil.addAttributesToStreamDefinition(toStreamDefinition, mapInputMapping.getInputMappingAttributes(), EventBuilderConstants.PAYLOAD_DATA_VAL);
        }
        this.exportedStreamDefinition = toStreamDefinition;
        return toStreamDefinition;
    }

    @Override
    public void createMapping(Object eventDefinition) throws EventBuilderConfigurationException {
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

    @Override
    public void validateInputStreamAttributes(StreamDefinition inputStreamDefinition)
            throws EventBuilderConfigurationException {
        //Not applicable to Map input mapping
    }

}
