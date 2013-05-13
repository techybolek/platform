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
import org.wso2.carbon.event.builder.core.InputMapping;
import org.wso2.carbon.event.builder.core.internal.TupleInputMapping;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBuilderConfiguration<T extends InputMapping> {

    private StreamDefinition streamDefinition;
    private InputTransportMessageConfiguration inputTransportMessageConfiguration;
    private List<T> inputMappings = new ArrayList<T>();
    private String name;
    private String type;
    private Map<String, String> eventBuilderConfigurationProperties = new ConcurrentHashMap<String, String>();

    public EventBuilderConfiguration() {

    }

    public EventBuilderConfiguration(InputTransportMessageConfiguration inputTransportMessageConfiguration) {
        this.inputTransportMessageConfiguration = inputTransportMessageConfiguration;
    }

    public List<T> getInputMappings() {
        return inputMappings;
    }

    public void addInputMapping(T inputMapping) {
        this.inputMappings.add(inputMapping);
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
