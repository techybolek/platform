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

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.BasicEventListener;
import org.wso2.carbon.event.builder.core.Wso2EventListener;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

import java.util.ArrayList;
import java.util.List;

public class StreamEventDispatcher {
    private StreamDefinition streamDefinition = null;
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();

    public StreamEventDispatcher(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void addEventListener(BasicEventListener basicEventListener) {
        basicEventListeners.add(basicEventListener);
    }

    public void removeEventListener(BasicEventListener basicEventListener) {
        if (!basicEventListeners.contains(basicEventListener)) {
            throw new EventBuilderConfigurationException("The given basic event listener is not registered to the stream:" + this.streamDefinition.getStreamId());
        }
        basicEventListeners.remove(basicEventListener);
    }

    public void addEventListener(Wso2EventListener wso2EventListener) {
        wso2EventListeners.add(wso2EventListener);
    }

    public void removeEventListener(Wso2EventListener wso2EventListener) {
        if (!wso2EventListeners.contains(wso2EventListener)) {
            throw new EventBuilderConfigurationException("The given wso2 event listener is not registered to the stream:" + this.streamDefinition.getStreamId());
        }
        wso2EventListeners.remove(wso2EventListener);
    }

    public void dispatchEvent(Object[] outObjArray) {
        if (!basicEventListeners.isEmpty()) {
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onEvent(outObjArray);
            }
        }
        if (!wso2EventListeners.isEmpty()) {
            Event event = convertToWso2Event(outObjArray);
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onEvent(event);
            }
        }
    }

    private Event convertToWso2Event(Object[] objArray) {
        int metaSize = streamDefinition.getMetaData().size();
        int correlationSize = streamDefinition.getCorrelationData().size();
        int payloadSize = streamDefinition.getPayloadData().size();
        Object[] metaAttributes = new Object[metaSize];
        Object[] correlationAttributes = new Object[correlationSize];
        Object[] payloadAttributes = new Object[payloadSize];
        int attributeIndex = 0;
        for (int i = 0; i < metaSize; i++) {
            metaAttributes[i] = objArray[attributeIndex++];
        }
        for (int i = 0; i < correlationSize; i++) {
            correlationAttributes[i] = objArray[attributeIndex++];
        }
        for (int i = 0; i < payloadSize; i++) {
            payloadAttributes[i] = objArray[attributeIndex++];
        }

        return new Event(streamDefinition.getStreamId(), System.currentTimeMillis(), metaAttributes, correlationAttributes, payloadAttributes);
    }

    public void cleanup() {
        if (!basicEventListeners.isEmpty()) {
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onRemoveDefinition(streamDefinition);
            }
        }
        if (!wso2EventListeners.isEmpty()) {
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onRemoveDefinition(streamDefinition);
            }
        }
        basicEventListeners.clear();
        wso2EventListeners.clear();
    }

    /**
    public void notifyEventAddition(StreamDefinition streamDefinition) {
        if (!basicEventListeners.isEmpty()) {
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onAddDefinition(streamDefinition);
            }
        }
        if (!wso2EventListeners.isEmpty()) {
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onAddDefinition(streamDefinition);
            }
        }
    }
     **/

}
