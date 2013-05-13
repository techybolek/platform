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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.internal.TupleInputMapping;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigurationSyntax;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.InputTransportConfigHelper;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.wso2.carbon.event.builder.core.internal.TupleInputMapping.InputDataType;

/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class EventBuilderConfigurationHelper {

    public static EventBuilderConfiguration fromOM(OMElement ebConfigOMElement) throws MalformedStreamDefinitionException {

        EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration(null);

        OMElement fromElement = ebConfigOMElement.getFirstChildWithName(new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_FROM));
        OMElement mappingElement = ebConfigOMElement.getFirstChildWithName(new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_MAPPING));
        OMElement toElement = ebConfigOMElement.getFirstChildWithName(new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_TO));

        String transportAdaptorType = fromElement.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_TA_TYPE));
        eventBuilderConfiguration.setName(ebConfigOMElement.getAttributeValue(
                new QName(EventBuilderConfigurationSyntax.EB_ATTR_NAME)));
        eventBuilderConfiguration.setType(transportAdaptorType);

        InputTransportMessageConfiguration inputTransportMessageConfiguration = InputTransportConfigHelper.getInputTransportMessageConfiguration(transportAdaptorType);
        inputTransportMessageConfiguration.setTransportAdaptorName(fromElement.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_TA_NAME)));
        Iterator fromElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_PROPERTY)
        );
        while (fromElementPropertyIterator.hasNext()) {
            OMElement fromElementProperty = (OMElement) fromElementPropertyIterator.next();
            String propertyName = fromElementProperty.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_NAME));
            String propertyValue = fromElementProperty.getText();
            inputTransportMessageConfiguration.addInputMessageProperty(propertyName, propertyValue);
        }
        String inputStreamName = inputTransportMessageConfiguration.getInputMessageProperties().get("streamName");
        String inputStreamVersion = inputTransportMessageConfiguration.getInputMessageProperties().get("version");
        StreamDefinition inputStreamDefinition = new StreamDefinition(inputStreamName, inputStreamVersion);
        eventBuilderConfiguration.setStreamDefinition(inputStreamDefinition);

        Iterator mappingElementPropertyIterator = mappingElement.getChildrenWithName(
                new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_PROPERTY));

        // Initialize counters
        Map<InputDataType, Integer> mappingCount = new HashMap<InputDataType, Integer>(3);
        mappingCount.put(InputDataType.META_DATA, 0);
        mappingCount.put(InputDataType.CORRELATION_DATA, 0);
        mappingCount.put(InputDataType.PAYLOAD_DATA, 0);

        while (mappingElementPropertyIterator.hasNext()) {
            OMElement propertyOMElement = (OMElement) mappingElementPropertyIterator.next();
            OMElement propertyFromElement = propertyOMElement.getFirstChildWithName(new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_FROM));
            OMElement propertyToElement = propertyOMElement.getFirstChildWithName(new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_TO));
            String nameFrom = propertyFromElement.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_NAME));
            String inputTypeName = propertyFromElement.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_TYPE));
            String nameTo = propertyToElement.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_NAME));
            String attribTypeName = propertyToElement.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_TYPE));

            InputDataType inputDataType = EventBuilderUtil.STRING_INPUT_DATA_TYPE_MAP.get(inputTypeName);
            AttributeType attributeType = EventBuilderUtil.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName);
            addAttributeToStreamDefinition(inputStreamDefinition, nameFrom, inputDataType, attributeType);
            TupleInputMapping tupleInputMapping = new TupleInputMapping(nameFrom, inputDataType,
                    nameTo, attributeType, inputStreamDefinition, mappingCount.get(inputDataType));
            mappingCount.put(inputDataType, mappingCount.get(inputDataType) + 1);
            eventBuilderConfiguration.addInputMapping(tupleInputMapping);
        }
        eventBuilderConfiguration.setInputTransportMessageConfiguration(inputTransportMessageConfiguration);

        return eventBuilderConfiguration;
    }

    private static void addAttributeToStreamDefinition(StreamDefinition streamDefinition, String attributeName, InputDataType inputDataType, AttributeType attributeType) {
        switch (inputDataType) {
            case META_DATA:
                streamDefinition.addMetaData(attributeName, attributeType);
                break;
            case CORRELATION_DATA:
                streamDefinition.addCorrelationData(attributeName, attributeType);
                break;
            case PAYLOAD_DATA:
            default:
                streamDefinition.addPayloadData(attributeName, attributeType);
        }
    }

    public static OMElement eventBuilderConfigurationToOM(EventBuilderConfiguration eventBuilderConfiguration) {
        //TODO Fix me like my brother
        Map<String, String> eventBuilderProperties = eventBuilderConfiguration.getEventBuilderConfigurationProperties();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventBuilderConfigElement = factory.createOMElement(new QName(
                EventBuilderConfigurationSyntax.EB_CONF_NS,
                EventBuilderConfigurationSyntax.EB_ELEMENT_ROOT_ELEMENT, EventBuilderConfigurationSyntax.EB_ELEMENT_CONF_EB_NS_PREFIX));

        eventBuilderConfigElement.addAttribute(EventBuilderConfigurationSyntax.EB_ATTR_NAME, eventBuilderConfiguration.getName(),
                null);
        eventBuilderConfigElement.addAttribute(EventBuilderConfigurationSyntax.EB_ATTR_TYPE, eventBuilderConfiguration.getType(),
                null);
        OMElement transportTypeElement = factory.createOMElement(new QName(EventBuilderConfigurationSyntax.
                EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_TRANSPORT_TYPE,
                EventBuilderConfigurationSyntax.EB_ELEMENT_CONF_EB_NS_PREFIX));
        transportTypeElement.setText(eventBuilderConfiguration.getInputTransportMessageConfiguration().getTransportAdaptorName());
        eventBuilderConfigElement.addChild(transportTypeElement);

        //Event builder properties
        OMElement commonPropertyElement = factory.createOMElement(new QName(
                EventBuilderConfigurationSyntax.EB_CONF_NS,
                EventBuilderConfigurationSyntax.EB_ELEMENT_PROPERTY, EventBuilderConfigurationSyntax.EB_ELEMENT_CONF_EB_NS_PREFIX));

        eventBuilderConfigElement.addChild(commonPropertyElement);

        for (Map.Entry<String, String> commonPropertyEntry : eventBuilderProperties.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    EventBuilderConfigurationSyntax.EB_CONF_NS,
                    EventBuilderConfigurationSyntax.EB_ELEMENT_PROPERTY, EventBuilderConfigurationSyntax.EB_ELEMENT_CONF_EB_NS_PREFIX));

            propertyElement.addAttribute(EventBuilderConfigurationSyntax.EB_ATTR_NAME, commonPropertyEntry.getKey(), null);
            propertyElement.setText(commonPropertyEntry.getValue());

            commonPropertyElement.addChild(propertyElement);
        }

        return eventBuilderConfigElement;
    }
}
