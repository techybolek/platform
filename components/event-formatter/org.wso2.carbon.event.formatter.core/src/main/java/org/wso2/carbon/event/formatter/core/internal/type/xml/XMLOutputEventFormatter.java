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

package org.wso2.carbon.event.formatter.core.internal.type.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XMLOutputEventFormatter extends EventFormatter {

    private static final Log log = LogFactory.getLog(XMLOutputEventFormatter.class);
    private String outputXMLText = "";

    public XMLOutputEventFormatter(EventFormatterConfiguration eventFormatterConfiguration,
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

        EventFormatterListener xmlFormatterListener = new EventFormatterListener(this);
        eventSource.subscribe(tenantId, inputStreamDefinition, xmlFormatterListener);


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

    protected void validateStreamDefinitionWithOutputProperties(int tenantId)
            throws EventFormatterConfigurationException {

        XMLOutputMapping textOutputMapping = ((XMLOutputMapping) eventFormatterConfiguration.getOutputMapping());
        String actualMappingText = textOutputMapping.getMappingXMLText();
        if (textOutputMapping.isRegistryResource()) {
            actualMappingText = EventFormatterServiceValueHolder.getCarbonEventFormatterService().getRegistryResourceContent(textOutputMapping.getMappingXMLText(), tenantId);
        }
        this.outputXMLText = actualMappingText;
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

    public String getPropertyValue(Object obj, String mappingProperty) {
        Object[] inputObjArray = (Object[]) obj;
        if (inputObjArray.length != 0) {
            int position = propertyPositionMap.get(mappingProperty);
            return inputObjArray[position].toString();
        }
        return "";
    }

    public OMElement buildOuputOMElement(Object event, OMElement omElement)
            throws EventFormatterConfigurationException {
        Iterator<OMElement> iterator = omElement.getChildElements();
        if (iterator.hasNext()) {
            for (; iterator.hasNext(); ) {
                OMElement childElement = iterator.next();
                Iterator<OMAttribute> iteratorAttr = childElement.getAllAttributes();
                while (iteratorAttr.hasNext()) {
                    OMAttribute omAttribute = iteratorAttr.next();
                    String text = omAttribute.getAttributeValue();
                    if (text != null) {
                        if (text.contains("{{") && text.indexOf("}}") > 0) {
                            String propertyToReplace = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
                            String value = getPropertyValue(event, propertyToReplace).toString();
                            omAttribute.setAttributeValue(value);
                        }
                    }
                }

                String text = childElement.getText();
                if (text != null) {
                    if (text.contains("{{") && text.indexOf("}}") > 0) {
                        String propertyToReplace = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
                        String value = getPropertyValue(event, propertyToReplace).toString();
                        childElement.setText(value);
                    }
                }

                buildOuputOMElement(event, childElement);
            }
        }else{
            String text = omElement.getText();
            if (text != null) {
                if (text.contains("{{") && text.indexOf("}}") > 0) {
                    String propertyToReplace = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
                    String value = getPropertyValue(event, propertyToReplace).toString();
                    omElement.setText(value);
                }
            }
        }
        return omElement;
    }


    public void sendEvent(Object obj) throws EventFormatterConfigurationException {

        Object[] inputObjArray = (Object[]) obj;
        addToTracer(eventFormatterConfiguration, "Event before formatting in the Event-Formatter ", inputObjArray);
        XMLOutputMapping xmlOutputMapping = (XMLOutputMapping) eventFormatterConfiguration.getOutputMapping();
        try {
            Object message = buildOuputOMElement(inputObjArray, AXIOMUtil.stringToOM(outputXMLText));
            addToTracer(eventFormatterConfiguration, "Event after formatting in the Event-Formatter ", outputXMLText);
            OutputTransportAdaptorService transportAdaptorService = EventFormatterServiceValueHolder.getOutputTransportAdaptorService();
            transportAdaptorService.publish(outputTransportAdaptorConfiguration, eventFormatterConfiguration.getToPropertyConfiguration().getOutputTransportAdaptorMessageConfiguration(), message);
        } catch (XMLStreamException e) {
            throw new EventFormatterConfigurationException("XML mapping is not in XML format :" + outputXMLText, e);
        }

    }


    @Override
    public EventFormatterConfiguration getEventFormatterConfiguration() {
        return this.eventFormatterConfiguration;
    }


}
