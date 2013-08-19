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

package org.wso2.carbon.event.builder.core.internal.type.json;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.List;

public class JsonInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(JsonInputMapper.class);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<JsonPathData> attributeJsonPathDataList = new ArrayList<JsonPathData>();
    private StreamDefinition exportedStreamDefinition = null;
    private EventDispatcher eventDispatcher = null;

    public JsonInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                           EventDispatcher eventDispatcher) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void createMapping(Object eventDefinition) throws EventBuilderConfigurationException {
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof JsonInputMapping) {
            JsonInputMapping jsonInputMapping = (JsonInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : jsonInputMapping.getInputMappingAttributes()) {
                String jsonPathExpr = inputMappingAttribute.getFromElementKey();
                JsonPath compiledJsonPath = JsonPath.compile(jsonPathExpr);
                String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                attributeJsonPathDataList.add(new JsonPathData(compiledJsonPath, type));
                exportedStreamDefinition.addPayloadData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            }
        }
    }

    @Override
    public void validateInputStreamAttributes(StreamDefinition inputStreamDefinition)
            throws EventBuilderConfigurationException {
        //Not applicable to JSON input mapping
    }

    @Override
    public void processInputEvent(Object obj) {
        JsonInputMapping jsonInputMapping = (JsonInputMapping) this.eventBuilderConfiguration.getInputMapping();
        if (jsonInputMapping.isBatchProcessingEnabled()) {
            processMultipleEvents(obj);
        } else {
            processSingleEvent(obj);
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
        if (eventBuilderConfiguration.getInputMapping() instanceof JsonInputMapping) {
            JsonInputMapping jsonInputMapping = (JsonInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : jsonInputMapping.getInputMappingAttributes()) {
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
        JsonInputMapping jsonInputMapping = (JsonInputMapping) this.eventBuilderConfiguration.getInputMapping();
        try {
            toStreamDefinition = new StreamDefinition(this.eventBuilderConfiguration.getToStreamName(), this.eventBuilderConfiguration.getToStreamVersion());
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException("Could not create stream definition with " + inputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + inputStreamVersion);
        }

        if (jsonInputMapping != null) {
            EventBuilderUtil.addAttributesToStreamDefinition(toStreamDefinition, jsonInputMapping.getInputMappingAttributes(), EventBuilderConstants.PAYLOAD_DATA_VAL);
        }
        this.exportedStreamDefinition = toStreamDefinition;
        return toStreamDefinition;
    }

    private void processMultipleEvents(Object obj) {
        if (obj instanceof String) {
            String events = (String) obj;
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(events);
                for (int i = 0; i < jsonArray.length(); i++) {
                    processSingleEvent(jsonArray.getJSONObject(i).toString());
                }
            } catch (JSONException e) {
                throw new EventBuilderConfigurationException("Error in parsing JSON: ", e);
            }
        }
    }

    private void processSingleEvent(Object obj) {
        Object[] outObjArray = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            List<Object> objList = new ArrayList<Object>();
            for (JsonPathData jsonPathData : attributeJsonPathDataList) {
                JsonPath jsonPath = jsonPathData.getJsonPath();
                String type = jsonPathData.getType();
                try {
                    Object resultObject = jsonPath.read(jsonString);
                    if (resultObject != null) {
                        Class typeClass = Class.forName(type);
                        Object returnedObj = typeClass.cast(resultObject);
                        objList.add(returnedObj);
                    } else {
                        log.warn("Unable to parse XPath to retrieve required attribute. Skipping to next attribute.");
                    }
                } catch (ClassNotFoundException e) {
                    throw new EventBuilderConfigurationException("Cannot find specified class for type " + type);
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        eventDispatcher.dispatchEvent(outObjArray);
    }

    private class JsonPathData {
        private String type;
        private JsonPath jsonPath;

        public JsonPathData(JsonPath jsonPath, String type) {
            this.type = type;
            this.jsonPath = jsonPath;
        }

        private JsonPath getJsonPath() {
            return jsonPath;
        }

        private String getType() {
            return type;
        }
    }

}
