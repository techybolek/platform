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

package org.wso2.carbon.event.builder.core.internal.util;

import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;

import java.util.List;

public class InputTransportConfigHelper {

    public static InputTransportAdaptorConfiguration getInputTransportAdaptorConfiguration(String transportAdaptorName) {
        List<TransportAdaptorDto> transportAdaptorDtoList = EventBuilderServiceValueHolder.getTransportAdaptorService().getTransportAdaptors();
        for (TransportAdaptorDto transportAdaptorDto : transportAdaptorDtoList) {
            if (transportAdaptorDto.getTransportAdaptorTypeName().equals(transportAdaptorName)) {
                return buildInputTransportAdaptorConfiguration(transportAdaptorDto);
            }
        }

        return null;
    }

    public static InputTransportMessageConfiguration getInputTransportMessageConfiguration(String transportAdaptorTypeName) {
        MessageDto messageDto = EventBuilderServiceValueHolder.getTransportAdaptorService().getTransportMessageDto(transportAdaptorTypeName);
        InputTransportMessageConfiguration inputTransportMessageConfiguration = null;
        if (messageDto != null) {
            inputTransportMessageConfiguration = new InputTransportMessageConfiguration();
            inputTransportMessageConfiguration.setTransportAdaptorName(messageDto.getAdaptorName());
            for(Property property: messageDto.getMessageInPropertyList()) {
                inputTransportMessageConfiguration.addInputMessageProperty(property.getPropertyName(), property.getDefaultValue());
            }
        }

        return inputTransportMessageConfiguration;
    }

    private static InputTransportAdaptorConfiguration buildInputTransportAdaptorConfiguration(TransportAdaptorDto transportAdaptorDto) {
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new TransportAdaptorConfiguration();
        inputTransportAdaptorConfiguration.setName(transportAdaptorDto.getTransportAdaptorTypeName());
        for (Property property : transportAdaptorDto.getAdaptorCommonPropertyList()) {
            inputTransportAdaptorConfiguration.addCommonAdaptorProperty(property.getPropertyName(), property.getDefaultValue());
        }
        for (Property property : transportAdaptorDto.getAdaptorInPropertyList()) {
            inputTransportAdaptorConfiguration.addInputAdaptorProperty(property.getPropertyName(), property.getDefaultValue());
        }

        return inputTransportAdaptorConfiguration;
    }
}
