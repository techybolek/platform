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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TransportAdaptorManagerConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorHolder;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorManagerConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * this class is used to read the values of the transport configurations define in the broker-manager-config.xml
 */

public class TransportAdaptorConfigurationHelper {

    private static final Log log = LogFactory.getLog(TransportAdaptorConfigurationHelper.class);

    public static TransportAdaptorConfiguration fromOM(OMElement transportConfigOMElement) {

        TransportAdaptorConfiguration transportAdaptorConfiguration = new TransportAdaptorConfiguration();

        transportAdaptorConfiguration.setName(transportConfigOMElement.getAttributeValue(
                new QName(TransportAdaptorManagerConstants.TM_ATTR_NAME)));

        transportAdaptorConfiguration.setType(transportConfigOMElement.getAttributeValue(
                new QName(TransportAdaptorManagerConstants.TM_ATTR_TYPE)));

        //Input Adaptor Properties
        Iterator inputPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_INPUT_PROPERTY));

        for (; inputPropertyIter.hasNext(); ) {
            OMElement inputPropertyOMElement = (OMElement) inputPropertyIter.next();
            Iterator propertyIter = inputPropertyOMElement.getChildrenWithName(
                    new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_PROPERTY));
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(TransportAdaptorManagerConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                transportAdaptorConfiguration.addInputAdaptorProperty(name, value);
            }
        }

        //Output Adaptor Properties
        Iterator outputPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_OUTPUT_PROPERTY));

        for (; outputPropertyIter.hasNext(); ) {
            OMElement outputPropertyOMElement = (OMElement) outputPropertyIter.next();
            Iterator propertyIter = outputPropertyOMElement.getChildrenWithName(
                    new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_PROPERTY));
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(TransportAdaptorManagerConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                transportAdaptorConfiguration.addOutputAdaptorProperty(name, value);
            }
        }


        //Common Adaptor Properties
        Iterator commonPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_COMMON_PROPERTY));

        for (; commonPropertyIter.hasNext(); ) {
            OMElement commonPropertyOMElement = (OMElement) commonPropertyIter.next();
            Iterator propertyIter = commonPropertyOMElement.getChildrenWithName(
                    new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_PROPERTY));
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(TransportAdaptorManagerConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                transportAdaptorConfiguration.addCommonAdaptorProperty(name, value);
            }
        }


        return transportAdaptorConfiguration;

    }


    public static OMElement transportAdaptorConfigurationToOM(
            TransportAdaptorConfiguration transportAdaptorConfiguration) {
        String transportAdaptorName = transportAdaptorConfiguration.getName();
        String transportAdaptorType = transportAdaptorConfiguration.getType();

        Map<String, String> inputTransportAdaptorProperties = transportAdaptorConfiguration.getInputAdaptorProperties();
        Map<String, String> outputTransportAdaptorProperties = transportAdaptorConfiguration.getOutputAdaptorProperties();
        Map<String, String> commonTransportAdaptorProperties = transportAdaptorConfiguration.getCommonAdaptorProperties();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement transportAdaptorItem = factory.createOMElement(new QName(
                TransportAdaptorManagerConstants.TM_CONF_NS,
                TransportAdaptorManagerConstants.TM_ELE_ROOT_ELEMENT, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, transportAdaptorName,
                                          null);
        transportAdaptorItem.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_TYPE, transportAdaptorType,
                                          null);


        //input transport adaptor properties
        OMElement inputPropertyElement = factory.createOMElement(new QName(
                TransportAdaptorManagerConstants.TM_CONF_NS,
                TransportAdaptorManagerConstants.TM_ELE_INPUT_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(inputPropertyElement);

        for (Map.Entry<String, String> inputPropertyEntry : inputTransportAdaptorProperties.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    TransportAdaptorManagerConstants.TM_CONF_NS,
                    TransportAdaptorManagerConstants.TM_ELE_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

            propertyElement.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, inputPropertyEntry.getKey(), null);
            propertyElement.setText(inputPropertyEntry.getValue());

            inputPropertyElement.addChild(propertyElement);
        }


        //output transport adaptor properties
        OMElement outputPropertyElement = factory.createOMElement(new QName(
                TransportAdaptorManagerConstants.TM_CONF_NS,
                TransportAdaptorManagerConstants.TM_ELE_OUTPUT_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(outputPropertyElement);

        for (Map.Entry<String, String> outputPropertyEntry : outputTransportAdaptorProperties.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    TransportAdaptorManagerConstants.TM_CONF_NS,
                    TransportAdaptorManagerConstants.TM_ELE_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

            propertyElement.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, outputPropertyEntry.getKey(), null);
            propertyElement.setText(outputPropertyEntry.getValue());

            outputPropertyElement.addChild(propertyElement);
        }

        //common transport adaptor properties
        OMElement commonPropertyElement = factory.createOMElement(new QName(
                TransportAdaptorManagerConstants.TM_CONF_NS,
                TransportAdaptorManagerConstants.TM_ELE_COMMON_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(commonPropertyElement);

        for (Map.Entry<String, String> commonPropertyEntry : commonTransportAdaptorProperties.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    TransportAdaptorManagerConstants.TM_CONF_NS,
                    TransportAdaptorManagerConstants.TM_ELE_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

            propertyElement.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, commonPropertyEntry.getKey(), null);
            propertyElement.setText(commonPropertyEntry.getValue());

            commonPropertyElement.addChild(propertyElement);
        }

        return transportAdaptorItem;
    }


    public static boolean validateTransportAdaptorConfiguration(int tenantId,
                                                                TransportAdaptorConfiguration transportAdaptorConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        TransportAdaptorService transportAdaptorService = TransportAdaptorHolder.getInstance().getTransportAdaptorService();
        TransportAdaptorDto transportAdaptorDto = transportAdaptorService.getTransportAdaptorDto(transportAdaptorConfiguration.getType());

        List<Property> inputTransportAdaptorProperties = transportAdaptorDto.getAdaptorInPropertyList();
        List<Property> outputTransportAdaptorProperties = transportAdaptorDto.getAdaptorOutPropertyList();
        List<Property> commonTransportAdaptorProperties = transportAdaptorDto.getAdaptorCommonPropertyList();


        if (inputTransportAdaptorProperties != null) {
            Iterator propertyIterator = inputTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {

                    if (!transportAdaptorConfiguration.getInputAdaptorProperties().containsKey(transportProperty.getPropertyName())) {
                        log.error("Required input property : " + transportProperty.getPropertyName() + " not in the Transport Adaptor Configuration");
                        throw new TransportAdaptorManagerConfigurationException("Required input property : " + transportProperty.getPropertyName() + " not in the Transport Adaptor Configuration");
                    }
                }

            }
        }

        if (outputTransportAdaptorProperties != null) {
            Iterator propertyIterator = outputTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {

                    if (!transportAdaptorConfiguration.getOutputAdaptorProperties().containsKey(transportProperty.getPropertyName())) {
                        log.error("Required output property : " + transportProperty.getPropertyName() + " not in the Transport Adaptor Configuration");
                        throw new TransportAdaptorManagerConfigurationException("Required output property : " + transportProperty.getPropertyName() + " not in the Transport Adaptor Configuration");
                    }
                }

            }
        }

        if (commonTransportAdaptorProperties != null) {
            Iterator propertyIterator = commonTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {

                    if (!transportAdaptorConfiguration.getCommonAdaptorProperties().containsKey(transportProperty.getPropertyName())) {
                        log.error("Required common property : " + transportProperty.getPropertyName() + " not in the Transport Adaptor Configuration");
                        throw new TransportAdaptorManagerConfigurationException("Required common property : " + transportProperty.getPropertyName() + " not in the Transport Adaptor Configuration");
                    }
                }

            }
        }

        return true;

    }
}
