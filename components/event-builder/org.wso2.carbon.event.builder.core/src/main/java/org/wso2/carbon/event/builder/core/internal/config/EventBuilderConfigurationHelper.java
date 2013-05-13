/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.builder.core.internal.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigurationSyntax;
import org.wso2.carbon.event.builder.core.internal.util.InputTransportConfigHelper;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;

/**
 * this class is used to read the values of the transport configurations define in the broker-manager-config.xml
 */

public class EventBuilderConfigurationHelper {

    public static EventBuilderConfiguration fromOM(OMElement ebConfigOMElement) {

        EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration(null);

        eventBuilderConfiguration.setName(ebConfigOMElement.getAttributeValue(
                new QName(EventBuilderConfigurationSyntax.EB_ATTR_NAME)));
        eventBuilderConfiguration.setType(ebConfigOMElement.getAttributeValue(
                new QName(EventBuilderConfigurationSyntax.EB_ATTR_TYPE)));
        OMElement transportTypeElement = ebConfigOMElement.getFirstChildWithName(new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_TRANSPORT_TYPE));
        InputTransportMessageConfiguration inputTransportMessageConfiguration = InputTransportConfigHelper.getInputTransportMessageConfiguration(transportTypeElement.getText());

        //Event Builder properties
        Iterator eventBuilderPropertyIterator = ebConfigOMElement.getChildrenWithName(
                new QName(EventBuilderConfigurationSyntax.EB_CONF_NS, EventBuilderConfigurationSyntax.EB_ELEMENT_PROPERTY));

        while (eventBuilderPropertyIterator.hasNext()) {
            OMElement propertyOMElement = (OMElement) eventBuilderPropertyIterator.next();
            String name = propertyOMElement.getAttributeValue(
                    new QName(EventBuilderConfigurationSyntax.EB_ATTR_NAME));
            String value = propertyOMElement.getText();
            inputTransportMessageConfiguration.addInputMessageProperty(name, value);
        }
        eventBuilderConfiguration.setInputTransportMessageConfiguration(inputTransportMessageConfiguration);

        return eventBuilderConfiguration;
    }

    public static OMElement eventBuilderConfigurationToOM(EventBuilderConfiguration eventBuilderConfiguration) {
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
