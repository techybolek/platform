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

package org.wso2.carbon.event.formatter.core.internal.type.map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.config.EventOutputProperty;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapOutputEventFormatter extends EventFormatter {

    private static final Log log = LogFactory.getLog(MapOutputEventFormatter.class);


    public MapOutputEventFormatter(EventFormatterConfiguration eventFormatterConfiguration,
                                   int tenantId) throws
                                                 EventFormatterConfigurationException {
        this.eventFormatterConfiguration = eventFormatterConfiguration;

        String inputStreamName = eventFormatterConfiguration.getFromStreamName();
        String inputStreamVersion = eventFormatterConfiguration.getFromStreamVersion();

        EventSource eventSource = getEventSource(inputStreamName, inputStreamVersion, tenantId);

        if (eventSource == null) {
            log.error("There is no any event source for the corresponding stream name or version");
            throw new EventFormatterConfigurationException("There is no any event source for the corresponding stream name or version");
        }

        StreamDefinition inputStreamDefinition = eventSource.getStreamDefinition(inputStreamName, inputStreamVersion, tenantId);
        createPropertyPositionMap(inputStreamDefinition);
        validateStreamDefinitionWithOutputProperties();
        createOutputTransportAdaptorConfiguration(tenantId);

        EventFormatterListener mapFormatterListener = new EventFormatterListener(this);
        eventSource.subscribe(tenantId, inputStreamDefinition, mapFormatterListener);

    }

    protected void validateStreamDefinitionWithOutputProperties()
            throws EventFormatterConfigurationException {

        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventFormatterConfiguration.getOutputMapping();
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        Iterator<EventOutputProperty> outputPropertyConfigurationIterator = outputPropertyConfiguration.iterator();
        for (; outputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty outputProperty = outputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(outputProperty.getValueOf())) {
                log.error("Property " + outputProperty.getValueOf() + " is not in the input stream definition. ");
                throw new EventFormatterConfigurationException("Property " + outputProperty.getValueOf() + " is not in the input stream definition. ");
            }
        }
    }


    public void sendEvent(Object obj) throws EventFormatterConfigurationException {

        Object[] inputObjArray = (Object[]) obj;
        Map<Object, Object> eventMapObject = new TreeMap<Object, Object>();
        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventFormatterConfiguration.getOutputMapping();
        addToTracer(eventFormatterConfiguration,"Event before formatting in the Event-Formatter " , inputObjArray);
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        if (outputPropertyConfiguration.size() != 0) {
            for (EventOutputProperty eventOutputProperty : outputPropertyConfiguration) {
                int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                eventMapObject.put(eventOutputProperty.getValueOf(), inputObjArray[position]);
            }
        }
        addToTracer(eventFormatterConfiguration,"Event after formatting in the Event-Formatter " , eventMapObject);
        OutputTransportAdaptorService transportAdaptorService = EventFormatterServiceValueHolder.getOutputTransportAdaptorService();
        transportAdaptorService.publish(outputTransportAdaptorConfiguration, eventFormatterConfiguration.getToPropertyConfiguration().getOutputTransportAdaptorMessageConfiguration(), eventMapObject);

    }

    @Override
    public EventFormatterConfiguration getEventFormatterConfiguration() {
        return this.eventFormatterConfiguration;
    }


}
