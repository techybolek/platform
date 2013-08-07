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

package org.wso2.carbon.event.builder.admin.internal.util.dto.converter;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderPropertyDto;
import org.wso2.carbon.event.builder.admin.internal.util.EventBuilderAdminConstants;
import org.wso2.carbon.event.builder.admin.internal.util.EventBuilderAdminUtil;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventBuilderFactory;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.config.InputStreamConfiguration;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventBuilderFactory;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventInputMapping;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Wso2EventDtoConverter extends BasicDtoConverter {

    @Override
    public EventBuilderConfiguration toEventBuilderConfiguration(EventBuilderConfigurationDto eventBuilderConfigurationDto, int tenantId) throws EventBuilderAdminServiceException {
        String eventBuilderType = eventBuilderConfigurationDto.getInputMappingType();
        if (!eventBuilderType.equals(EventBuilderConstants.EB_WSO2EVENT_MAPPING_TYPE)) {
            throw new EventBuilderAdminServiceException("Incorrect mapping type");
        }
        EventBuilderFactory eventBuilderFactory = getEventBuilderFactory();
        EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration(eventBuilderFactory);
        eventBuilderConfiguration.setEventBuilderName(eventBuilderConfigurationDto.getEventBuilderConfigName());

        Wso2EventInputMapping wso2EventInputMapping = new Wso2EventInputMapping();
        InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration = new InputTransportAdaptorMessageConfiguration();
        int mappingPropertyPos = 0;
        for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
            if (eventBuilderPropertyDto.getKey().endsWith(EventBuilderAdminConstants.FROM_SUFFIX)) {
                String propertyName = eventBuilderPropertyDto.getKey().substring(0, eventBuilderPropertyDto.getKey().lastIndexOf(EventBuilderAdminConstants.FROM_SUFFIX));
                inputTransportAdaptorMessageConfiguration.addInputMessageProperty(propertyName, eventBuilderPropertyDto.getValue());
            } else if (eventBuilderPropertyDto.getKey().endsWith(EventBuilderAdminConstants.MAPPING_SUFFIX)) {
                String keyWithoutSuffix = eventBuilderPropertyDto.getKey().substring(0, eventBuilderPropertyDto.getKey().lastIndexOf(EventBuilderAdminConstants.MAPPING_SUFFIX));
                if (keyWithoutSuffix.startsWith(EventBuilderAdminConstants.META_DATA_PREFIX)) {
                    int keyTrimLength = EventBuilderAdminConstants.META_DATA_PREFIX.length();
                    int typeTrimLength = EventBuilderAdminConstants.JAVA_LANG_PACKAGE_PREFIX.length();
                    String propertyKey = keyWithoutSuffix.substring(keyTrimLength);
                    String attribTypeName = eventBuilderPropertyDto.getPropertyType().substring(typeTrimLength).toLowerCase();
                    AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName);
                    InputMappingAttribute metaInputMappingAttribute = new InputMappingAttribute(propertyKey, eventBuilderPropertyDto.getValue(), attributeType, EventBuilderConstants.META_DATA_VAL);
                    metaInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
                    wso2EventInputMapping.addMetaInputEventAttribute(metaInputMappingAttribute);
                } else if (keyWithoutSuffix.startsWith(EventBuilderAdminConstants.CORRELATION_DATA_PREFIX)) {
                    int keyTrimLength = EventBuilderAdminConstants.CORRELATION_DATA_PREFIX.length();
                    int typeTrimLength = EventBuilderAdminConstants.JAVA_LANG_PACKAGE_PREFIX.length();
                    String propertyKey = keyWithoutSuffix.substring(keyTrimLength);
                    String attribTypeName = eventBuilderPropertyDto.getPropertyType().substring(typeTrimLength).toLowerCase();
                    AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName);
                    InputMappingAttribute correlationInputMappingAttribute = new InputMappingAttribute(propertyKey, eventBuilderPropertyDto.getValue(), attributeType, EventBuilderConstants.CORRELATION_DATA_VAL);
                    correlationInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
                    wso2EventInputMapping.addCorrelationInputEventAttribute(correlationInputMappingAttribute);
                } else {
                    int typeTrimLength = EventBuilderAdminConstants.JAVA_LANG_PACKAGE_PREFIX.length();
                    String attribTypeName = eventBuilderPropertyDto.getPropertyType().substring(typeTrimLength).toLowerCase();
                    AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName);
                    InputMappingAttribute payloadInputMappingAttribute = new InputMappingAttribute(keyWithoutSuffix, eventBuilderPropertyDto.getValue(), attributeType, EventBuilderConstants.PAYLOAD_DATA_VAL);
                    payloadInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
                    wso2EventInputMapping.addPayloadInputEventAttribute(payloadInputMappingAttribute);
                }
            }
        }
        eventBuilderConfiguration.setInputMapping(wso2EventInputMapping);
        eventBuilderConfiguration.setToStreamName(eventBuilderConfigurationDto.getToStreamName());
        eventBuilderConfiguration.setToStreamVersion(eventBuilderConfigurationDto.getToStreamVersion());
        eventBuilderConfiguration.setStatisticsEnabled(eventBuilderConfigurationDto.isStatisticsEnabled());
        eventBuilderConfiguration.setTraceEnabled(eventBuilderConfigurationDto.isTraceEnabled());

        InputStreamConfiguration inputStreamConfiguration = new InputStreamConfiguration();
        inputStreamConfiguration.setInputTransportAdaptorMessageConfiguration(inputTransportAdaptorMessageConfiguration);
        inputStreamConfiguration.setTransportAdaptorName(eventBuilderConfigurationDto.getInputTransportAdaptorName());
        String transportAdaptorType = getInputTransportAdaptorType(eventBuilderConfigurationDto.getInputTransportAdaptorName(), tenantId);
        inputStreamConfiguration.setTransportAdaptorType(transportAdaptorType);
        eventBuilderConfiguration.setInputStreamConfiguration(inputStreamConfiguration);

        return eventBuilderConfiguration;
    }

    @Override
    public EventBuilderConfigurationDto fromEventBuilderConfiguration(EventBuilderConfiguration eventBuilderConfiguration) {
        EventBuilderConfigurationDto eventBuilderConfigurationDto = new EventBuilderConfigurationDto();

        eventBuilderConfigurationDto.setEventBuilderConfigName(eventBuilderConfiguration.getEventBuilderName());
        eventBuilderConfigurationDto.setInputMappingType(eventBuilderConfiguration.getInputMapping().getMappingType());

        String deploymentStatus = EventBuilderAdminConstants.DEP_STATUS_MAP.get(eventBuilderConfiguration.getDeploymentStatus());
        eventBuilderConfigurationDto.setDeploymentStatus(deploymentStatus);
        eventBuilderConfigurationDto.setInputTransportAdaptorName(eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName());
        eventBuilderConfigurationDto.setInputTransportAdaptorType(eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorType());
        eventBuilderConfigurationDto.setToStreamName(eventBuilderConfiguration.getToStreamName());
        eventBuilderConfigurationDto.setToStreamVersion(eventBuilderConfiguration.getToStreamVersion());

        eventBuilderConfigurationDto.setTraceEnabled(eventBuilderConfiguration.isTraceEnabled());
        eventBuilderConfigurationDto.setStatisticsEnabled(eventBuilderConfiguration.isStatisticsEnabled());

        EventBuilderPropertyDto[] eventBuilderProperties = getEventBuilderProperties(eventBuilderConfiguration);
        eventBuilderConfigurationDto.setEventBuilderProperties(eventBuilderProperties);

        return eventBuilderConfigurationDto;
    }

    @Override
    protected EventBuilderFactory getEventBuilderFactory() {
        return new Wso2EventBuilderFactory();
    }

    private EventBuilderPropertyDto[] getEventBuilderProperties(EventBuilderConfiguration eventBuilderConfiguration) {
        List<EventBuilderPropertyDto> eventBuilderPropertyDtoList = new ArrayList<EventBuilderPropertyDto>();
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();
        InputStreamConfiguration inputStreamConfiguration = eventBuilderConfiguration.getInputStreamConfiguration();

        for (Map.Entry<String, String> entry : inputStreamConfiguration.getInputTransportAdaptorMessageConfiguration().getInputMessageProperties().entrySet()) {
            eventBuilderPropertyDtoList.add(getFromSectionProperty(entry.getKey(), entry.getValue()));
        }

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getCorrelationInputMappingAttributes()) {
            EventBuilderPropertyDto eventBuilderPropertyDto = getMappingSectionProperty(EventBuilderAdminConstants.CORRELATION_DATA_PREFIX, inputMappingAttribute);
            eventBuilderPropertyDtoList.add(eventBuilderPropertyDto);
        }
        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getMetaInputMappingAttributes()) {
            EventBuilderPropertyDto eventBuilderPropertyDto = getMappingSectionProperty(EventBuilderAdminConstants.META_DATA_PREFIX, inputMappingAttribute);
            eventBuilderPropertyDtoList.add(eventBuilderPropertyDto);
        }
        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getPayloadInputMappingAttributes()) {
            EventBuilderPropertyDto eventBuilderPropertyDto = getMappingSectionProperty("", inputMappingAttribute);
            eventBuilderPropertyDtoList.add(eventBuilderPropertyDto);
        }

        return eventBuilderPropertyDtoList.toArray(new EventBuilderPropertyDto[eventBuilderPropertyDtoList.size()]);
    }

    private EventBuilderPropertyDto getMappingSectionProperty(String typePrefix, InputMappingAttribute inputMappingAttribute) {
        String key = typePrefix + inputMappingAttribute.getFromElementKey() + EventBuilderAdminConstants.MAPPING_SUFFIX;
        EventBuilderPropertyDto eventBuilderPropertyDto = new EventBuilderPropertyDto();
        eventBuilderPropertyDto.setKey(key);
        eventBuilderPropertyDto.setValue(inputMappingAttribute.getToElementKey());
        eventBuilderPropertyDto.setPropertyType(EventBuilderAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        String displayName = EventBuilderAdminUtil.splitCamelCase(inputMappingAttribute.getFromElementKey());
        eventBuilderPropertyDto.setDisplayName(displayName);

        return eventBuilderPropertyDto;
    }

    private EventBuilderPropertyDto getFromSectionProperty(String name, String value) {
        String key = name + EventBuilderAdminConstants.FROM_SUFFIX;
        EventBuilderPropertyDto eventBuilderPropertyDto = new EventBuilderPropertyDto();
        eventBuilderPropertyDto.setKey(key);
        eventBuilderPropertyDto.setValue(value);
        eventBuilderPropertyDto.setDisplayName(name);

        return eventBuilderPropertyDto;
    }
}
