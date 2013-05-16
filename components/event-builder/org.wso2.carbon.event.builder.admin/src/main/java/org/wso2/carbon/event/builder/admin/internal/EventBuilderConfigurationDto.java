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

package org.wso2.carbon.event.builder.admin.internal;

import org.wso2.carbon.event.builder.core.EventBuilderProperty;
import org.wso2.carbon.event.builder.core.InputMapping;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.MessageDto;

/**
 * Event Builder Configuration Details are stored in this class
 */
public class EventBuilderConfigurationDto {
    private EventBuilderConfiguration<? extends InputMapping> eventBuilderConfiguration;

    public EventBuilderConfigurationDto(EventBuilderConfiguration<? extends InputMapping> eventBuilderConfiguration) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
    }

    public void addEventBuilderProperty(String key, String value) {

    }

    public MessageDto getInputMessageDto() {
        return null;
    }

    public EventBuilderProperty[] getEventBuilderProperties() {
        int propertyMapSize = eventBuilderConfiguration.getEventBuilderConfigurationProperties().size();
        EventBuilderProperty[] eventBuilderProperties = new EventBuilderProperty[propertyMapSize];

        int i = 0;
        for (EventBuilderProperty eventBuilderProperty : eventBuilderConfiguration.getEventBuilderConfigurationProperties().values()) {
            eventBuilderProperties[i++] = eventBuilderProperty;
        }

        return eventBuilderProperties;
    }
}
