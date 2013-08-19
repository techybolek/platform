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

package org.wso2.carbon.event.builder.core.internal.type.text;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventDispatcher;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.text.config.RegexData;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class TextInputMapper implements InputMapper {
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<RegexData> attributeRegexList = new ArrayList<RegexData>();
    private StreamDefinition exportedStreamDefinition = null;
    private boolean binaryInputEnabled = false;
    private EventDispatcher eventDispatcher = null;

    public TextInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                           EventDispatcher eventDispatcher) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.eventDispatcher = eventDispatcher;
    }

    public void setBinaryInputEnabled(boolean binaryInputEnabled) {
        this.binaryInputEnabled = binaryInputEnabled;
    }

    public void createMapping(EventBuilderConfiguration eventBuilderConfiguration,
                              StreamDefinition exportedStreamDefinition,
                              List<RegexData> attributeRegexList) {
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof TextInputMapping) {
            TextInputMapping textInputMapping = (TextInputMapping) eventBuilderConfiguration.getInputMapping();
            RegexData regexData = null;
            for (InputMappingAttribute inputMappingAttribute : textInputMapping.getInputMappingAttributes()) {
                String regex = inputMappingAttribute.getFromElementKey();
                if (regexData == null || !regex.equals(regexData.getRegex())) {
                    try {
                        regexData = new RegexData(regex);
                        attributeRegexList.add(regexData);
                    } catch (PatternSyntaxException e) {
                        throw new EventBuilderConfigurationException("Error parsing regular expression: " + regex, e);
                    }
                }
                String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                String defaultValue = inputMappingAttribute.getDefaultValue();
                regexData.addMapping(type, defaultValue);
                exportedStreamDefinition.addPayloadData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            }
            if (!isStreamDefinitionValidForConfiguration(eventBuilderConfiguration, exportedStreamDefinition)) {
                throw new EventBuilderValidationException("Exported stream definition does not match the specified configuration.");
            }
        }
    }

    private String preprocessEvent(Object obj) {
        String inputString = null;
        if (binaryInputEnabled) {
            if (obj instanceof byte[]) {
                try {
                    byte[] inputBytes = (byte[]) obj;
                    inputString = new String(inputBytes, EventBuilderConstants.CHARSET_UTF8);
                } catch (UnsupportedEncodingException e) {
                    throw new EventBuilderConfigurationException("Cannot convert input bytes. Unsupported encoding type.", e);
                }
            }
        } else {
            inputString = (String) obj;
        }
        return inputString;
    }

    @Override
    public void processInputEvent(Object obj) {
        String inputString = preprocessEvent(obj);
        List<Object> attributeList = new ArrayList<Object>();
        for (RegexData regexData : attributeRegexList) {
            String formattedInputString = inputString.replaceAll("\\r?\\n", " ");
            regexData.matchInput(formattedInputString);
            while (regexData.hasNext()) {
                Object returnedAttribute = null;
                String value = regexData.next();
                if (value != null) {
                    String type = regexData.getType();
                    try {
                        Class<?> beanClass = Class.forName(type);
                        if (!beanClass.equals(String.class)) {
                            Class<?> stringClass = String.class;
                            Method valueOfMethod = beanClass.getMethod("valueOf", stringClass);
                            returnedAttribute = valueOfMethod.invoke(null, value);
                        } else {
                            returnedAttribute = value;
                        }
                    } catch (ClassNotFoundException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    } catch (InvocationTargetException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    } catch (NoSuchMethodException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    } catch (IllegalAccessException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    }
                }
                attributeList.add(returnedAttribute);
            }
        }
        Object[] convertedObjArray = attributeList.toArray(new Object[attributeList.size()]);
        this.eventDispatcher.dispatchEvent(convertedObjArray);
    }

    @Override
    public boolean isStreamDefinitionValidForConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            StreamDefinition exportedStreamDefinition) {
        if (!(eventBuilderConfiguration.getToStreamName().equals(exportedStreamDefinition.getName())
              && eventBuilderConfiguration.getToStreamVersion().equals(exportedStreamDefinition.getVersion()))) {
            return false;
        }
        if (eventBuilderConfiguration.getInputMapping() instanceof TextInputMapping) {
            TextInputMapping textInputMapping = (TextInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : textInputMapping.getInputMappingAttributes()) {
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
        TextInputMapping textInputMapping = (TextInputMapping) this.eventBuilderConfiguration.getInputMapping();
        try {
            toStreamDefinition = new StreamDefinition(this.eventBuilderConfiguration.getToStreamName(), this.eventBuilderConfiguration.getToStreamVersion());
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException("Could not create stream definition with " + inputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + inputStreamVersion);
        }

        if (textInputMapping != null) {
            EventBuilderUtil.addAttributesToStreamDefinition(toStreamDefinition, textInputMapping.getInputMappingAttributes(), EventBuilderConstants.PAYLOAD_DATA_VAL);
        }
        this.exportedStreamDefinition = toStreamDefinition;
        return toStreamDefinition;
    }

    @Override
    public void createMapping(Object eventDefinition)
            throws EventBuilderConfigurationException {
        createMapping(this.eventBuilderConfiguration, this.exportedStreamDefinition, this.attributeRegexList);
    }

    @Override
    public void validateInputStreamAttributes(StreamDefinition inputStreamDefinition)
            throws EventBuilderConfigurationException {
        // Not applicable to text input mapping
    }

}
