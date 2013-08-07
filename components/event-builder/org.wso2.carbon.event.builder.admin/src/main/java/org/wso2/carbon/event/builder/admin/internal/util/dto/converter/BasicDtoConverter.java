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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderPropertyDto;
import org.wso2.carbon.event.builder.admin.internal.ds.EventBuilderAdminServiceValueHolder;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConvertible;
import org.wso2.carbon.event.builder.admin.internal.util.EventBuilderAdminConstants;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventBuilderFactory;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.List;
import java.util.Map;

public class BasicDtoConverter implements DtoConvertible {
    //TODO Is there a pattern here somewhere without implementing dummy abstract methods ?

    private static final Log log = LogFactory.getLog(BasicDtoConverter.class);

    @Override
    public EventBuilderPropertyDto[] getEventBuilderPropertiesFrom(MessageDto messageDto,
                                                                   EventBuilderConfiguration eventBuilderConfiguration) {
        List<Property> messageDtoPropertyList = messageDto.getMessageInPropertyList();
        EventBuilderPropertyDto[] eventBuilderPropertyDtos = new EventBuilderPropertyDto[messageDtoPropertyList.size()];
        int i = 0;
        if (eventBuilderConfiguration != null) {
            Map<String, String> propertyValueMap = eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration().getInputMessageProperties();
            for (Property property : messageDtoPropertyList) {
                String value = propertyValueMap.get(property.getPropertyName());
                EventBuilderPropertyDto eventBuilderPropertyDto = getEventBuilderPropertyFrom(property, value);
                eventBuilderPropertyDtos[i++] = eventBuilderPropertyDto;
            }
        } else {
            for (Property property : messageDtoPropertyList) {
                EventBuilderPropertyDto eventBuilderPropertyDto = getEventBuilderPropertyFrom(property, null);
                eventBuilderPropertyDtos[i++] = eventBuilderPropertyDto;
            }
        }

        return eventBuilderPropertyDtos;
    }

    @Override
    public EventBuilderConfiguration toEventBuilderConfiguration(
            EventBuilderConfigurationDto eventBuilderConfigurationDto, int tenantId)
            throws EventBuilderAdminServiceException {
        throw new EventBuilderAdminServiceException("Cannot create EventBuilderConfiguration: Type specific DtoConverter should be used.");
    }

    @Override
    public EventBuilderConfigurationDto fromEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration)
            throws EventBuilderAdminServiceException {
        throw new EventBuilderAdminServiceException("Cannot create EventBuilderConfiguration: Type specific DtoConverter should be used.");
    }

    private EventBuilderPropertyDto getEventBuilderPropertyFrom(Property msgDtoProperty,
                                                                String value) {
        String key = msgDtoProperty.getPropertyName() + EventBuilderAdminConstants.FROM_SUFFIX;
        EventBuilderPropertyDto eventBuilderPropertyDto = new EventBuilderPropertyDto();
        eventBuilderPropertyDto.setKey(key);
        eventBuilderPropertyDto.setDefaultValue(msgDtoProperty.getDefaultValue());
        eventBuilderPropertyDto.setDisplayName(msgDtoProperty.getDisplayName());
        eventBuilderPropertyDto.setHint(msgDtoProperty.getHint());
        eventBuilderPropertyDto.setRequired(msgDtoProperty.isRequired());
        eventBuilderPropertyDto.setSecured(msgDtoProperty.isSecured());
        if (value != null) {
            eventBuilderPropertyDto.setValue(value);
        } else {
            eventBuilderPropertyDto.setValue(msgDtoProperty.getDefaultValue());
        }
        eventBuilderPropertyDto.setOptions(msgDtoProperty.getOptions());

        return eventBuilderPropertyDto;
    }

    protected String getInputTransportAdaptorType(String inputTransportAdaptorName, int tenantId)
            throws EventBuilderAdminServiceException {
        InputTransportAdaptorManagerService inputTransportAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputTransportAdaptorManagerService();
        try {
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = inputTransportAdaptorManagerService.getInputTransportAdaptorConfiguration(inputTransportAdaptorName, tenantId);
            return inputTransportAdaptorConfiguration.getType();
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            String errMsg = "Error retrieving input transport adaptor configuration";
            log.error(errMsg, e);
            throw new EventBuilderAdminServiceException(errMsg, e);
        }
    }

    protected EventBuilderFactory getEventBuilderFactory()
            throws EventBuilderAdminServiceException {
        throw new EventBuilderAdminServiceException("Cannot create EventBuilderConfiguration: Type specific DtoConverter should be used.");
    }
}
