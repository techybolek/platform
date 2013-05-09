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

package org.wso2.carbon.transport.adaptor.manager.core.internal.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TMConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;

/**
 * this class is used to read the values of the transport configurations define in the broker-manager-config.xml
 */

public class TransportConfigurationHelper {

    public static TransportAdaptorConfiguration fromOM(OMElement transportConfigOMElement) {

        TransportAdaptorConfiguration transportAdaptorConfiguration = new TransportAdaptorConfiguration();

        transportAdaptorConfiguration.setName(transportConfigOMElement.getAttributeValue(
                new QName(TMConstants.TM_ATTR_NAME)));

        transportAdaptorConfiguration.setType(transportConfigOMElement.getAttributeValue(
                new QName(TMConstants.TM_ATTR_TYPE)));

        //Input Adaptor Properties
        Iterator inputPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TMConstants.TM_CONF_NS, TMConstants.TM_ELE_INPUT_PROPERTY));

        for (; inputPropertyIter.hasNext(); ) {
            OMElement inputPropertyOMElement = (OMElement) inputPropertyIter.next();
            Iterator propertyIter = inputPropertyOMElement.getChildrenWithName(
                    new QName(TMConstants.TM_CONF_NS, TMConstants.TM_ELE_PROPERTY));
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(TMConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                transportAdaptorConfiguration.addInputAdaptorProperty(name, value);
            }
        }

        //Output Adaptor Properties
        Iterator outputPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TMConstants.TM_CONF_NS, TMConstants.TM_ELE_OUTPUT_PROPERTY));

        for (; outputPropertyIter.hasNext(); ) {
            OMElement outputPropertyOMElement = (OMElement) outputPropertyIter.next();
            Iterator propertyIter = outputPropertyOMElement.getChildrenWithName(
                    new QName(TMConstants.TM_CONF_NS, TMConstants.TM_ELE_PROPERTY));
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(TMConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                transportAdaptorConfiguration.addOutputAdaptorProperty(name, value);
            }
        }


        //Common Adaptor Properties
        Iterator commonPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TMConstants.TM_CONF_NS, TMConstants.TM_ELE_COMMON_PROPERTY));

        for (; commonPropertyIter.hasNext(); ) {
            OMElement commonPropertyOMElement = (OMElement) commonPropertyIter.next();
            Iterator propertyIter = commonPropertyOMElement.getChildrenWithName(
                    new QName(TMConstants.TM_CONF_NS, TMConstants.TM_ELE_PROPERTY));
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(TMConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                transportAdaptorConfiguration.addCommonAdaptorProperty(name, value);
            }
        }


        return transportAdaptorConfiguration;

    }


    public static OMElement transportAdaptorConfigurationToOM(TransportAdaptorConfiguration transportAdaptorConfiguration) {
        String transportAdaptorName = transportAdaptorConfiguration.getName();
        String transportAdaptorType = transportAdaptorConfiguration.getType();

        Map<String, String> inputTransportAdaptorProperties = transportAdaptorConfiguration.getInputAdaptorProperties();
        Map<String, String> outputTransportAdaptorProperties = transportAdaptorConfiguration.getOutputAdaptorProperties();
        Map<String, String> commonTransportAdaptorProperties = transportAdaptorConfiguration.getCommonAdaptorProperties();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement transportAdaptorItem = factory.createOMElement(new QName(
                TMConstants.TM_CONF_NS,
                TMConstants.TM_ELE_ROOT_ELEMENT,TMConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addAttribute(TMConstants.TM_ATTR_NAME, transportAdaptorName,
                null);
        transportAdaptorItem.addAttribute(TMConstants.TM_ATTR_TYPE, transportAdaptorType,
                null);


        //input transport adaptor properties
        OMElement inputPropertyElement = factory.createOMElement(new QName(
                TMConstants.TM_CONF_NS,
                TMConstants.TM_ELE_INPUT_PROPERTY,TMConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(inputPropertyElement);

        for (Map.Entry<String, String> inputPropertyEntry : inputTransportAdaptorProperties.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    TMConstants.TM_CONF_NS,
                    TMConstants.TM_ELE_PROPERTY,TMConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

            propertyElement.addAttribute(TMConstants.TM_ATTR_NAME, inputPropertyEntry.getKey(), null);
            propertyElement.setText(inputPropertyEntry.getValue());

            inputPropertyElement.addChild(propertyElement);
        }


        //output transport adaptor properties
        OMElement outputPropertyElement = factory.createOMElement(new QName(
                TMConstants.TM_CONF_NS,
                TMConstants.TM_ELE_OUTPUT_PROPERTY,TMConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(outputPropertyElement);

        for (Map.Entry<String, String> outputPropertyEntry : outputTransportAdaptorProperties.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    TMConstants.TM_CONF_NS,
                    TMConstants.TM_ELE_PROPERTY,TMConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

            propertyElement.addAttribute(TMConstants.TM_ATTR_NAME, outputPropertyEntry.getKey(), null);
            propertyElement.setText(outputPropertyEntry.getValue());

            outputPropertyElement.addChild(propertyElement);
        }

        //common transport adaptor properties
        OMElement commonPropertyElement = factory.createOMElement(new QName(
                TMConstants.TM_CONF_NS,
                TMConstants.TM_ELE_COMMON_PROPERTY,TMConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(commonPropertyElement);

        for (Map.Entry<String, String> commonPropertyEntry : commonTransportAdaptorProperties.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    TMConstants.TM_CONF_NS,
                    TMConstants.TM_ELE_PROPERTY,TMConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

            propertyElement.addAttribute(TMConstants.TM_ATTR_NAME, commonPropertyEntry.getKey(), null);
            propertyElement.setText(commonPropertyEntry.getValue());

            commonPropertyElement.addChild(propertyElement);
        }

        return transportAdaptorItem;
    }
}
