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
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EventFormatter {

    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);
    private EventFormatterConfiguration eventFormatterConfiguration = null;
    private int tenantId;
    private Map<String, Integer> propertyPositionMap = new TreeMap<String, Integer>();
    private OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = null;
    private OutputMapper outputMapper = null;
    private Object[] eventObject = null;
    private boolean metaFlag = false;
    private boolean correlationFlag = false;
    private boolean payloadFlag = false;
    private List<EventSource> eventSourceList = null;
    private EventFormatterListener eventFormatterListener = null;
    private String streamId = null;
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;
    private boolean dynamicMessagePropertyEnabled = false;
    List<String> dynamicMessagePropertyList = new ArrayList<String>();

    public EventFormatter(EventFormatterConfiguration eventFormatterConfiguration,
                          int tenantId) throws EventFormatterConfigurationException {

        this.eventFormatterConfiguration = eventFormatterConfiguration;
        this.tenantId = tenantId;

        String inputStreamName = eventFormatterConfiguration.getFromStreamName();
        String inputStreamVersion = eventFormatterConfiguration.getFromStreamVersion();
        eventSourceList = getEventSource(inputStreamName, inputStreamVersion, tenantId);

        if (eventSourceList == null) {
            throw new EventFormatterConfigurationException("There is no any event source for the corresponding stream name or version : " + inputStreamName + "-" + inputStreamVersion);
        }
        //Stream Definition must same for any event source, There are cannot be different stream definition for same stream id in multiple event sourced
        StreamDefinition inputStreamDefinition = eventSourceList.get(0).getStreamDefinition(inputStreamName, inputStreamVersion, tenantId);
        this.eventObject = createEventTemplate(inputStreamDefinition);
        this.streamId = inputStreamDefinition.getStreamId();
        createPropertyPositionMap(inputStreamDefinition);
        outputMapper = EventFormatterServiceValueHolder.getMappingFactoryMap().get(eventFormatterConfiguration.getOutputMapping().getMappingType()).constructOutputMapper(eventFormatterConfiguration, propertyPositionMap, tenantId);
        setOutputTransportAdaptorConfiguration(tenantId);
        eventFormatterListener = new EventFormatterListener(this);

        Map<String, String> messageProperties = eventFormatterConfiguration.getToPropertyConfiguration().getOutputTransportAdaptorMessageConfiguration().getOutputMessageProperties();
        Iterator it = messageProperties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            getDynamicOutputMessageProperties(pairs.getValue().toString());
        }

        if (dynamicMessagePropertyList.size() > 0) {
            dynamicMessagePropertyEnabled = true;
        }

        for (EventSource eventSource : eventSourceList) {
            eventSource.subscribe(tenantId, streamId, eventFormatterListener);
        }

        this.traceEnabled = eventFormatterConfiguration.isEnableTracing();
        this.statisticsEnabled = eventFormatterConfiguration.isEnableStatistics();
        if (statisticsEnabled) {
            this.statisticsMonitor = EventFormatterServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventFormatterConstants.EVENT_FORMATTER, eventFormatterConfiguration.getEventFormatterName(), null);
            this.beforeTracerPrefix = tenantId + ":" + EventFormatterConstants.EVENT_FORMATTER + ":" + eventFormatterConfiguration.getFromStreamName() + ", before processing " + System.getProperty("line.separator");
            this.afterTracerPrefix = tenantId + ":" + EventFormatterConstants.EVENT_FORMATTER + ":" + eventFormatterConfiguration.getFromStreamName() + ", after processing " + System.getProperty("line.separator");
        }
    }

    public EventFormatterConfiguration getEventFormatterConfiguration() {
        return eventFormatterConfiguration;
    }

    public void sendEvent(Object inObject) throws EventFormatterConfigurationException {

        if (traceEnabled) {
            if (inObject instanceof Object[]) {
                trace.info(beforeTracerPrefix + Arrays.toString((Object[]) inObject));
            } else {
                trace.info(beforeTracerPrefix + inObject);
            }
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementResponse();
        }

        Object outObject = outputMapper.convert(inObject);

        if (traceEnabled) {
            if (outObject instanceof Object[]) {
                trace.info(afterTracerPrefix + Arrays.toString((Object[]) outObject));
            } else {
                trace.info(afterTracerPrefix + outObject);
            }
        }

        if (dynamicMessagePropertyEnabled) {
            changeDynamicTransportMessageProperties(inObject);
        }

        OutputTransportAdaptorService transportAdaptorService = EventFormatterServiceValueHolder.getOutputTransportAdaptorService();
        transportAdaptorService.publish(outputTransportAdaptorConfiguration, eventFormatterConfiguration.getToPropertyConfiguration().getOutputTransportAdaptorMessageConfiguration(), outObject, tenantId);

    }

    public void sendEvent(Event event) throws EventFormatterConfigurationException {

        int count = 0;
        Object[] metaData = event.getMetaData();
        Object[] correlationData = event.getCorrelationData();
        Object[] payloadData = event.getPayloadData();

        if (metaFlag) {
            System.arraycopy(metaData, 0, eventObject, 0, metaData.length);
            count += metaData.length;
        }

        if (correlationFlag) {
            System.arraycopy(correlationData, 0, eventObject, count, correlationData.length);
            count += correlationData.length;
        }

        if (payloadFlag) {
            System.arraycopy(payloadData, 0, eventObject, count, payloadData.length);
            count += payloadData.length;
        }

        sendEvent(eventObject);
    }

    private void setOutputTransportAdaptorConfiguration(int tenantId) {
        OutputTransportAdaptorManagerService transportAdaptorManagerService = EventFormatterServiceValueHolder.getOutputTransportAdaptorManagerService();

        try {
            this.outputTransportAdaptorConfiguration = transportAdaptorManagerService.getActiveOutputTransportAdaptorConfiguration(eventFormatterConfiguration.getToPropertyConfiguration().getTransportAdaptorName(), tenantId);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while retrieving the output transport adaptor configuration of : " + eventFormatterConfiguration.getToPropertyConfiguration().getTransportAdaptorName(), e);
        }

    }

    private void createPropertyPositionMap(StreamDefinition streamDefinition) {
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

    private List<EventSource> getEventSource(String streamName, String streamVersion,
                                             int tenantId) {
        List<EventSource> eventSourceList = EventFormatterServiceValueHolder.getEventSourceList();

        List<EventSource> eventSources = new ArrayList<EventSource>();
        Iterator<EventSource> eventSourceIterator = eventSourceList.iterator();
        for (; eventSourceIterator.hasNext(); ) {
            EventSource currentEventSource = eventSourceIterator.next();
            List<String> streamList = currentEventSource.getAllStreamId(tenantId);
            Iterator<String> stringIterator = streamList.iterator();
            for (; stringIterator.hasNext(); ) {
                String stream = stringIterator.next();
                if (stream.equals(streamName + ":" + streamVersion)) {
                    eventSources.add(currentEventSource);
                }
            }
        }
        return eventSources;
    }

    private Object[] createEventTemplate(StreamDefinition inputStreamDefinition) {
        int attributesCount = 0;
        if (inputStreamDefinition.getMetaData() != null) {
            attributesCount += inputStreamDefinition.getMetaData().size();
            metaFlag = true;
        }
        if (inputStreamDefinition.getCorrelationData() != null) {
            attributesCount += inputStreamDefinition.getCorrelationData().size();
            correlationFlag = true;
        }

        if (inputStreamDefinition.getPayloadData() != null) {
            attributesCount += inputStreamDefinition.getPayloadData().size();
            payloadFlag = true;
        }

        return new Object[attributesCount];
    }

    public String getStreamId() {
        return streamId;
    }

    public List<EventSource> getEventSourceList() {
        return eventSourceList;
    }

    public EventFormatterListener getEventFormatterListener() {
        return eventFormatterListener;
    }

    private List<String> getDynamicOutputMessageProperties(String messagePropertyValue) {

        String text = messagePropertyValue;

        dynamicMessagePropertyList.clear();
        while (text.contains("{{") && text.indexOf("}}") > 0) {
            dynamicMessagePropertyList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return dynamicMessagePropertyList;
    }

    private void changeDynamicTransportMessageProperties(Object obj) {
        Object[] inputObjArray = (Object[]) obj;

        for (String dynamicMessageProperty : dynamicMessagePropertyList) {
            if (inputObjArray.length != 0) {
                int position = propertyPositionMap.get(dynamicMessageProperty);
                changePropertyValue(position,dynamicMessageProperty,obj);
            }
        }
    }

    private void changePropertyValue(int position,String messageProperty,Object obj){
        Object[] inputObjArray = (Object[]) obj;
        Map<String, String> outputMessageProperties = eventFormatterConfiguration.getToPropertyConfiguration().getOutputTransportAdaptorMessageConfiguration().getOutputMessageProperties();

        List<String> keys = new ArrayList<String>();

        for (Map.Entry<String, String> entry : outputMessageProperties.entrySet()) {
            String mapValue ="{{"+messageProperty+"}}";
            if (mapValue.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }

        for (String key : keys){
            outputMessageProperties.put(key,inputObjArray[position].toString());
        }

    }
}
