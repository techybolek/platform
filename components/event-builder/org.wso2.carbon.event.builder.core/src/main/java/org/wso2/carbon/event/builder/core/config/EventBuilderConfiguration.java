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

package org.wso2.carbon.event.builder.core.config;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;

import java.util.HashMap;
import java.util.Map;

public class EventBuilderConfiguration {

    private StreamDefinition streamDefinition;
    private InputTransportMessageConfiguration inputTransportMessageConfiguration;
    private String name;
    private String type;
    private Map<String, String> eventBuilderConfigurationProperties = new HashMap<String, String>();

    public EventBuilderConfiguration(InputTransportMessageConfiguration inputTransportMessageConfiguration) {
        this.inputTransportMessageConfiguration = inputTransportMessageConfiguration;
    }

    public Map<String, String> getEventBuilderConfigurationProperties() {
        return eventBuilderConfigurationProperties;
    }

    public void setEventBuilderConfigurationProperties(Map<String, String> eventBuilderConfigurationProperties) {
        this.eventBuilderConfigurationProperties = eventBuilderConfigurationProperties;
    }

    public void addEventBuilderConfigurationProperty(String key, String value) {
        this.eventBuilderConfigurationProperties.put(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public InputTransportMessageConfiguration getInputTransportMessageConfiguration() {
        return inputTransportMessageConfiguration;
    }

    public void setInputTransportMessageConfiguration(InputTransportMessageConfiguration inputTransportMessageConfiguration) {
        this.inputTransportMessageConfiguration = inputTransportMessageConfiguration;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }


}
