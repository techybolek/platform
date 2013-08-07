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
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventBuilderFactory;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.config.InputStreamConfiguration;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLEventBuilderFactory;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLInputMapping;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XmlDtoConverter extends BasicDtoConverter {
    @Override
    public EventBuilderConfiguration toEventBuilderConfiguration(
            EventBuilderConfigurationDto eventBuilderConfigurationDto, int tenantId)
            throws EventBuilderAdminServiceException {
        String eventBuilderType = eventBuilderConfigurationDto.getInputMappingType();
        if (!eventBuilderType.equals(EventBuilderConstants.EB_XML_MAPPING_TYPE)) {
            throw new EventBuilderAdminServiceException("Incorrect mapping type. Expected: " + EventBuilderConstants.EB_XML_MAPPING_TYPE + ", Found: " + eventBuilderType);
        }
        EventBuilderFactory eventBuilderFactory = getEventBuilderFactory();
        EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration(eventBuilderFactory);
        eventBuilderConfiguration.setEventBuilderName(eventBuilderConfigurationDto.getEventBuilderConfigName());

        XMLInputMapping xmlInputMapping = new XMLInputMapping();
        InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration = new InputTransportAdaptorMessageConfiguration();
        for (EventBuilderPropertyDto eventBuilderPropertyDto : eventBuilderConfigurationDto.getEventBuilderProperties()) {
            if (eventBuilderPropertyDto.getKey().endsWith(EventBuilderAdminConstants.FROM_SUFFIX)) {
                String propertyName = eventBuilderPropertyDto.getKey().substring(0, eventBuilderPropertyDto.getKey().lastIndexOf(EventBuilderAdminConstants.FROM_SUFFIX));
                inputTransportAdaptorMessageConfiguration.addInputMessageProperty(propertyName, eventBuilderPropertyDto.getValue());
            } else if (eventBuilderPropertyDto.getKey().endsWith(EventBuilderAdminConstants.MAPPING_SUFFIX)) {
                String keyWithoutSuffix = eventBuilderPropertyDto.getKey().substring(0, eventBuilderPropertyDto.getKey().lastIndexOf(EventBuilderAdminConstants.MAPPING_SUFFIX));
                if (keyWithoutSuffix.startsWith(EventBuilderAdminConstants.XPATH_PREFIX_NS_PREFIX)) {
                    int keyTrimLength = EventBuilderAdminConstants.XPATH_PREFIX_NS_PREFIX.length();
                    String prefixName = keyWithoutSuffix.substring(keyTrimLength);
                    String namespaceUri = eventBuilderPropertyDto.getValue();
                    XPathDefinition xPathDefinition = new XPathDefinition(prefixName, namespaceUri);
                    xmlInputMapping.setXpathDefinition(xPathDefinition);
                } else {
                    int typeTrimLength = EventBuilderAdminConstants.JAVA_LANG_PACKAGE_PREFIX.length();
                    String attribTypeName = eventBuilderPropertyDto.getPropertyType().substring(typeTrimLength).toLowerCase();
                    AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName);
                    // For XML we use toElementKey as the property key.
                    InputMappingAttribute xmlMappingAttribute = new InputMappingAttribute(eventBuilderPropertyDto.getValue(), keyWithoutSuffix, attributeType);
                    xmlMappingAttribute.setDefaultValue(eventBuilderPropertyDto.getDefaultValue());
                    xmlInputMapping.addInputMappingAttribute(xmlMappingAttribute);
                }
            }
        }
        xmlInputMapping.setBatchProcessingEnabled(eventBuilderConfigurationDto.isBatchProcessingEnabled());
        eventBuilderConfiguration.setInputMapping(xmlInputMapping);
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
    public EventBuilderConfigurationDto fromEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration) {
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
        eventBuilderConfigurationDto.setBatchProcessingEnabled(((XMLInputMapping) eventBuilderConfiguration.getInputMapping()).isBatchProcessingEnabled());

        EventBuilderPropertyDto[] eventBuilderProperties = getEventBuilderProperties(eventBuilderConfiguration);
        eventBuilderConfigurationDto.setEventBuilderProperties(eventBuilderProperties);

        return eventBuilderConfigurationDto;

    }

    private EventBuilderPropertyDto[] getEventBuilderProperties(
            EventBuilderConfiguration eventBuilderConfiguration) {
        List<EventBuilderPropertyDto> eventBuilderPropertyDtoList = new ArrayList<EventBuilderPropertyDto>();
        XMLInputMapping xmlInputMapping = (XMLInputMapping) eventBuilderConfiguration.getInputMapping();
        InputStreamConfiguration inputStreamConfiguration = eventBuilderConfiguration.getInputStreamConfiguration();

        for (Map.Entry<String, String> entry : inputStreamConfiguration.getInputTransportAdaptorMessageConfiguration().getInputMessageProperties().entrySet()) {
            eventBuilderPropertyDtoList.add(getFromSectionProperty(entry.getKey(), entry.getValue()));
        }
        // Add XPathDefinition as a property.
        XPathDefinition xpathDef = xmlInputMapping.getXpathDefinition();
        EventBuilderPropertyDto xpathDefPropertyDto = new EventBuilderPropertyDto();
        xpathDefPropertyDto.setKey(EventBuilderAdminConstants.XPATH_PREFIX_NS_PREFIX + xpathDef.getPrefix() + EventBuilderAdminConstants.MAPPING_SUFFIX);
        xpathDefPropertyDto.setDisplayName(xpathDef.getPrefix());
        xpathDefPropertyDto.setValue(xpathDef.getNamespaceUri());
        eventBuilderPropertyDtoList.add(xpathDefPropertyDto);

        for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
            EventBuilderPropertyDto eventBuilderPropertyDto = getMappingSectionProperty(inputMappingAttribute);
            eventBuilderPropertyDtoList.add(eventBuilderPropertyDto);
        }

        return eventBuilderPropertyDtoList.toArray(new EventBuilderPropertyDto[eventBuilderPropertyDtoList.size()]);
    }

    private EventBuilderPropertyDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        // For XML we use ToElementKey as the key.
        String key = inputMappingAttribute.getToElementKey() + EventBuilderAdminConstants.MAPPING_SUFFIX;
        EventBuilderPropertyDto eventBuilderPropertyDto = new EventBuilderPropertyDto();
        eventBuilderPropertyDto.setKey(key);
        eventBuilderPropertyDto.setValue(inputMappingAttribute.getFromElementKey());
        eventBuilderPropertyDto.setPropertyType(EventBuilderAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventBuilderPropertyDto.setDisplayName(inputMappingAttribute.getToElementKey());
        eventBuilderPropertyDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

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

    @Override
    protected EventBuilderFactory getEventBuilderFactory() {
        return new XMLEventBuilderFactory();
    }
}
