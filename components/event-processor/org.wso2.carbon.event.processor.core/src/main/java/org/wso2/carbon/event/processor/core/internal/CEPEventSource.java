/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.EventSourceNotificationListener;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;

import java.util.List;


public class CEPEventSource implements EventSource {


    public CEPEventSource() {
    }


    @Override
    public List<String> getStreamNames(int tenantId) {
        return EventProcessorValueHolder.getEventProcessorService().getStreamNames(tenantId);
    }


    @Override
    public StreamDefinition getStreamDefinition(String name, String version, int tenantId) {
        return EventProcessorValueHolder.getEventProcessorService().getStreamDefinition(name, version, tenantId);
    }


    @Override
    public void subscribe(int tenantId, StreamDefinition streamDefinition,
                          EventFormatterListener eventFormatterListener)
            throws EventFormatterConfigurationException {
        EventProcessorValueHolder.getEventProcessorService().addEventFormatterListener(streamDefinition.getStreamId(),
                                                                                       eventFormatterListener, tenantId);
    }

    @Override
    public void subscribeToListener(
            EventSourceNotificationListener eventSourceNotificationListener) {
        EventProcessorValueHolder.registerNotificationListener(eventSourceNotificationListener);
        EventProcessorValueHolder.getEventProcessorService().notifyFormatter();
    }

    public void unsubscribe(StreamDefinition streamDefinition,
                            EventFormatterListener eventFormatterListener)
            throws EventFormatterConfigurationException {
    }
}
