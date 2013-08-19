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

package org.wso2.carbon.event.builder.core.internal.config;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.BasicEventListener;
import org.wso2.carbon.event.builder.core.Wso2EventListener;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.EventBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBuilderStreamJunction {
    private Map<String, StreamEventDispatcher> streamEventDispatcherMap = new ConcurrentHashMap<String, StreamEventDispatcher>();
    private Map<String, List<String>> eventBuilderNameMap = new ConcurrentHashMap<String, List<String>>();
    private Map<String, StreamDefinition> streamDefinitionMap = new ConcurrentHashMap<String, StreamDefinition>();

    /**
     * @param eventBuilder the stream definition of the event builder
     */
    public void registerEventSender(EventBuilder eventBuilder) {
        StreamDefinition streamDefinition = eventBuilder.getExportedStreamDefinition();
        String streamId = streamDefinition.getStreamId();
        if (!streamEventDispatcherMap.containsKey(streamId)) {
            streamDefinitionMap.put(streamId, streamDefinition);
            streamEventDispatcherMap.put(streamId, new StreamEventDispatcher(streamDefinition));
        } else if (!streamEventDispatcherMap.get(streamId).getStreamDefinition().equals(streamDefinition)) {
            throw new EventBuilderConfigurationException("Stream definition already exists for the same stream ID with different attributes.");
        }
        eventBuilder.setStreamEventDispatcher(streamEventDispatcherMap.get(streamId));

        if (!eventBuilderNameMap.containsKey(streamId)) {
            eventBuilderNameMap.put(streamId, new ArrayList<String>());
        }
        eventBuilderNameMap.get(streamId).add(eventBuilder.getEventBuilderConfiguration().getEventBuilderName());
    }

    public void unregisterEventSender(EventBuilder eventBuilder) {
        String streamId = eventBuilder.getExportedStreamDefinition().getStreamId();
        String eventBuilderName = eventBuilder.getEventBuilderConfiguration().getEventBuilderName();
        List<String> eventBuilderNameList = eventBuilderNameMap.get(streamId);
        eventBuilderNameList.remove(eventBuilderName);
        if (eventBuilderNameList.isEmpty()) {
            eventBuilderNameMap.remove(streamId);
            streamEventDispatcherMap.get(streamId).cleanup();
            streamEventDispatcherMap.remove(streamId);
            streamDefinitionMap.remove(streamId);
        }
    }

    public void addEventListener(String streamId, BasicEventListener basicEventListener) {
        if (!streamEventDispatcherMap.containsKey(streamId)) {
            throw new EventBuilderConfigurationException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventDispatcherMap.get(streamId).addEventListener(basicEventListener);
    }

    public void addEventListener(String streamId, Wso2EventListener wso2EventListener) {
        if (!streamEventDispatcherMap.containsKey(streamId)) {
            throw new EventBuilderConfigurationException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventDispatcherMap.get(streamId).addEventListener(wso2EventListener);
    }

    public void removeEventListener(String streamId, BasicEventListener basicEventListener) {
        if (!streamEventDispatcherMap.containsKey(streamId)) {
            throw new EventBuilderConfigurationException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventDispatcherMap.get(streamId).removeEventListener(basicEventListener);
    }

    public void removeEventListener(String streamId, Wso2EventListener wso2EventListener) {
        if (!streamEventDispatcherMap.containsKey(streamId)) {
            throw new EventBuilderConfigurationException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventDispatcherMap.get(streamId).removeEventListener(wso2EventListener);
    }

    public List<StreamDefinition> getStreamDefinitions() {
        return new ArrayList<StreamDefinition>(streamDefinitionMap.values());
    }
}
