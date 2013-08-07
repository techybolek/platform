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

package org.wso2.carbon.event.formatter.core.config;


import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class EventFormatter {

    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";

    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);

    protected EventFormatterConfiguration eventFormatterConfiguration = null;

    protected Map<String, Integer> propertyPositionMap = new TreeMap<String, Integer>();

    protected OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = null;


    public abstract EventFormatterConfiguration getEventFormatterConfiguration();

    public abstract void sendEvent(Object obj) throws EventFormatterConfigurationException;

    public void sendEvent(Event event) throws EventFormatterConfigurationException {
        Object[] metaData= event.getMetaData();
        Object[] correlationData= event.getCorrelationData();
        Object[] payloadData= event.getPayloadData();

        List arrayList = new ArrayList();
        if(metaData != null && metaData.length > 0){
            arrayList.addAll(Arrays.asList(metaData));
        }
        if(correlationData != null && correlationData.length > 0){
            arrayList.addAll(Arrays.asList(correlationData));
        }
        if(payloadData != null && payloadData.length > 0){
            arrayList.addAll(Arrays.asList(payloadData));
        }
        sendEvent(arrayList.toArray());
    }

    public void addToTracer(EventFormatterConfiguration eventFormatterConfiguration, String message, Object obj){
        if(eventFormatterConfiguration.isEnableTracing()){
            trace.info(message + obj);
        }
    }


    protected void createOutputTransportAdaptorConfiguration(int tenantId) {
        OutputTransportAdaptorManagerService transportAdaptorManagerService = EventFormatterServiceValueHolder.getOutputTransportAdaptorManagerService();

        try {
            this.outputTransportAdaptorConfiguration = transportAdaptorManagerService.getOutputTransportAdaptorConfiguration(eventFormatterConfiguration.getToPropertyConfiguration().getTransportAdaptorName(), tenantId);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while retrieving the output transport adaptor configuration for" + eventFormatterConfiguration.getToPropertyConfiguration().getTransportAdaptorName(), e);
        }

    }

    protected void createPropertyPositionMap(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

        int propertyCount = 0;
        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                propertyPositionMap.put(EventFormatterConstants.PROPERTY_META_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                propertyPositionMap.put(EventFormatterConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (payloadAttributeList != null) {
            for (Attribute attribute : payloadAttributeList) {
                propertyPositionMap.put(attribute.getName(), propertyCount);
                propertyCount++;
            }
        }
    }

    protected EventSource getEventSource(String streamName, String streamVersion, int tenantId) {
        List<EventSource> eventSourceList = EventFormatterServiceValueHolder.getEventSourceList();

        EventSource eventSource = null;
        Iterator<EventSource> eventSourceIterator = eventSourceList.iterator();
        for (; eventSourceIterator.hasNext(); ) {
            EventSource currentEventSource = eventSourceIterator.next();
            List<String> streamList = currentEventSource.getStreamNames(tenantId);
            Iterator<String> stringIterator = streamList.iterator();
            for (; stringIterator.hasNext(); ) {
                String stream = stringIterator.next();
                if (stream.equals(streamName + ":" + streamVersion)) {
                    eventSource = currentEventSource;
                    break;
                }
            }
        }

        return eventSource;

    }

}
