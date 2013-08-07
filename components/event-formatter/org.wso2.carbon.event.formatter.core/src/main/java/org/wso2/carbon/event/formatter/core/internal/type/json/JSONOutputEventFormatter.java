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

package org.wso2.carbon.event.formatter.core.internal.type.json;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONOutputEventFormatter extends EventFormatter {

    private static final Log log = LogFactory.getLog(JSONOutputEventFormatter.class);
    private List<String> mappingTextList;

    public JSONOutputEventFormatter(EventFormatterConfiguration eventFormatterConfiguration,
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
        validateStreamDefinitionWithOutputProperties(tenantId);

        createOutputTransportAdaptorConfiguration(tenantId);
        EventFormatterListener jsonFormatterListener = new EventFormatterListener(this);
        eventSource.subscribe(tenantId, inputStreamDefinition, jsonFormatterListener);

    }


    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains("{{") && text.indexOf("}}") > 0) {
            mappingTextList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return mappingTextList;
    }

    public void setMappingTextList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains("{{") && text.indexOf("}}") > 0) {
            mappingTextList.add(text.substring(0, text.indexOf("{{")));
            mappingTextList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        mappingTextList.add(text);
        this.mappingTextList = mappingTextList;
    }

    protected void validateStreamDefinitionWithOutputProperties(int tenantId)
            throws EventFormatterConfigurationException {

        JSONOutputMapping jsonOutputMapping = ((JSONOutputMapping) eventFormatterConfiguration.getOutputMapping());
        String actualMappingText = jsonOutputMapping.getMappingText();
        if(jsonOutputMapping.isRegistryResource()){
            actualMappingText = EventFormatterServiceValueHolder.getCarbonEventFormatterService().getRegistryResourceContent(jsonOutputMapping.getMappingText(),tenantId);
        }

        setMappingTextList(actualMappingText);
        List<String> mappingProperties = getOutputMappingPropertyList(actualMappingText);

        Iterator<String> mappingTextListIterator = mappingProperties.iterator();
        for (; mappingTextListIterator.hasNext(); ) {
            String property = mappingTextListIterator.next();
            if (!propertyPositionMap.containsKey(property)) {
                log.error("Property " + property + " is not in the input stream definition. ");
                throw new EventFormatterConfigurationException("Property " + property + " is not in the input stream definition. ");
            }
        }

    }

    public String convert(Object event, List<String> mappingTextList)
            throws EventFormatterConfigurationException {

        String eventText = mappingTextList.get(0);
        for (int i = 1; i < mappingTextList.size(); i++) {
            if (i % 2 == 0) {
                eventText += mappingTextList.get(i);
            } else {
                eventText += getPropertyValue(event, mappingTextList.get(i)).toString();
            }
        }
        return eventText;
    }

    public String getPropertyValue(Object obj, String mappingProperty) {
        Object[] inputObjArray = (Object[]) obj;
        if (inputObjArray.length != 0) {
            int position = propertyPositionMap.get(mappingProperty);
            return inputObjArray[position].toString();
        }
        return "";
    }


    public void sendEvent(Object obj) throws EventFormatterConfigurationException {

        String jsonEvent = convert(obj, mappingTextList);
        addToTracer(eventFormatterConfiguration,"Event before formatting in the Event-Formatter " , obj);
        try {
            new JSONObject(jsonEvent);
        } catch (JSONException e) {
            throw new EventFormatterConfigurationException("Not valid JSON object ", e);
        }
        addToTracer(eventFormatterConfiguration,"Event after formatting in the Event-Formatter ", jsonEvent);
        OutputTransportAdaptorService transportAdaptorService = EventFormatterServiceValueHolder.getOutputTransportAdaptorService();
        transportAdaptorService.publish(outputTransportAdaptorConfiguration, eventFormatterConfiguration.getToPropertyConfiguration().getOutputTransportAdaptorMessageConfiguration(), jsonEvent);

    }


    @Override
    public EventFormatterConfiguration getEventFormatterConfiguration() {
        return this.eventFormatterConfiguration;
    }

}
