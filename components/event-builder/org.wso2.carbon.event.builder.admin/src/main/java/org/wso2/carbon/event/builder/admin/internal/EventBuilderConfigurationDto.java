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

import java.util.ArrayList;
import java.util.List;

/**
 * Event Builder Configuration Details are stored in this class
 */
public class EventBuilderConfigurationDto {
    private String configName;
    private String eventBuilderType;
    private List<EventBuilderPropertyDto> eventBuilderPropertyDtos = new ArrayList<EventBuilderPropertyDto>();

    public EventBuilderConfigurationDto(String configName, String eventBuilderType) {
        this.configName = configName;
        this.eventBuilderType = eventBuilderType;
    }

    public String getConfigName() {
        return configName;
    }

    public String getEventBuilderType() {
        return eventBuilderType;
    }

    public EventBuilderPropertyDto[] getEventBuilderPropertyDtos() {
        EventBuilderPropertyDto[] ebPropertyDtoArray = new EventBuilderPropertyDto[0];
        return eventBuilderPropertyDtos.toArray(ebPropertyDtoArray);
    }

    public void addEventBuilderProperty(String key, String value) {
        eventBuilderPropertyDtos.add(new EventBuilderPropertyDto(key, value));
    }

    public List<EventBuilderProperty> getEventBuilderProperties() {
        //TODO Create a list to hold event builder properties and return
        return null;
    }
}
