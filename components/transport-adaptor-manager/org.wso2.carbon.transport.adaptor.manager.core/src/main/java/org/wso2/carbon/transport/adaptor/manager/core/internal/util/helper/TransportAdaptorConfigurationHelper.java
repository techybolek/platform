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

package org.wso2.carbon.transport.adaptor.manager.core.internal.util.helper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.core.config.InternalTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TransportAdaptorManagerConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorHolder;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorManagerConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class used to OM element related stuffs and for validating the xml files.
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
            InternalTransportAdaptorConfiguration inputTransportAdaptorPropertyConfiguration = new InternalTransportAdaptorConfiguration();
            if (propertyIter.hasNext()) {
                for (; propertyIter.hasNext(); ) {
                    OMElement propertyOMElement = (OMElement) propertyIter.next();
                    String name = propertyOMElement.getAttributeValue(
                            new QName(TransportAdaptorManagerConstants.TM_ATTR_NAME));
                    String value = propertyOMElement.getText();
                    inputTransportAdaptorPropertyConfiguration.addTransportAdaptorProperty(name, value);
                }
            }
            transportAdaptorConfiguration.setInputTransportAdaptorConfiguration(inputTransportAdaptorPropertyConfiguration);
        }

        //Output Adaptor Properties
        Iterator outputPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_OUTPUT_PROPERTY));

        for (; outputPropertyIter.hasNext(); ) {
            OMElement outputPropertyOMElement = (OMElement) outputPropertyIter.next();
            Iterator propertyIter = outputPropertyOMElement.getChildrenWithName(
                    new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_PROPERTY));
            InternalTransportAdaptorConfiguration outputTransportAdaptorPropertyConfiguration = new InternalTransportAdaptorConfiguration();
            if (propertyIter.hasNext()) {
                for (; propertyIter.hasNext(); ) {
                    OMElement propertyOMElement = (OMElement) propertyIter.next();
                    String name = propertyOMElement.getAttributeValue(
                            new QName(TransportAdaptorManagerConstants.TM_ATTR_NAME));
                    String value = propertyOMElement.getText();
                    outputTransportAdaptorPropertyConfiguration.addTransportAdaptorProperty(name, value);
                }
            }
            transportAdaptorConfiguration.setOutputTransportAdaptorConfiguration(outputTransportAdaptorPropertyConfiguration);
        }

        //Common Adaptor Properties
        Iterator commonPropertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(TransportAdaptorManagerConstants.TM_CONF_NS, TransportAdaptorManagerConstants.TM_ELE_PROPERTY));
        if (commonPropertyIter.hasNext()) {
            Map<String,String> transportAdaptorCommonProperties = new HashMap<String, String>();
            for (; commonPropertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) commonPropertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(TransportAdaptorManagerConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                transportAdaptorCommonProperties.put(name, value);

            }
            transportAdaptorConfiguration.setTransportAdaptorCommonProperties(transportAdaptorCommonProperties);
        }

        return transportAdaptorConfiguration;
    }


    public static OMElement transportAdaptorConfigurationToOM(
            TransportAdaptorConfiguration transportAdaptorConfiguration) {
        String transportAdaptorName = transportAdaptorConfiguration.getName();
        String transportAdaptorType = transportAdaptorConfiguration.getType();

        Map<String, String> inputTransportAdaptorProperties = null;
        Map<String, String> outputTransportAdaptorProperties = null;
        Map<String, String> commonTransportAdaptorProperties = null;

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement transportAdaptorItem = factory.createOMElement(new QName(
                TransportAdaptorManagerConstants.TM_CONF_NS,
                TransportAdaptorManagerConstants.TM_ELE_ROOT_ELEMENT, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
        transportAdaptorItem.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, transportAdaptorName,
                                          null);
        transportAdaptorItem.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_TYPE, transportAdaptorType,
                                          null);

        if (transportAdaptorConfiguration.getInputTransportAdaptorConfiguration() != null) {
        //input transport adaptor properties
        OMElement inputPropertyElement = factory.createOMElement(new QName(
                TransportAdaptorManagerConstants.TM_CONF_NS,
                TransportAdaptorManagerConstants.TM_ELE_INPUT_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(inputPropertyElement);

            inputTransportAdaptorProperties = transportAdaptorConfiguration.getInputTransportAdaptorConfiguration().getPropertyList();
            for (Map.Entry<String, String> inputPropertyEntry : inputTransportAdaptorProperties.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        TransportAdaptorManagerConstants.TM_CONF_NS,
                        TransportAdaptorManagerConstants.TM_ELE_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
                propertyElement.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, inputPropertyEntry.getKey(), null);
                propertyElement.setText(inputPropertyEntry.getValue());
                inputPropertyElement.addChild(propertyElement);
            }
        }

        if (transportAdaptorConfiguration.getOutputTransportAdaptorConfiguration() != null) {
        //output transport adaptor properties
        OMElement outputPropertyElement = factory.createOMElement(new QName(
                TransportAdaptorManagerConstants.TM_CONF_NS,
                TransportAdaptorManagerConstants.TM_ELE_OUTPUT_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));

        transportAdaptorItem.addChild(outputPropertyElement);

            outputTransportAdaptorProperties = transportAdaptorConfiguration.getOutputTransportAdaptorConfiguration().getPropertyList();
            for (Map.Entry<String, String> outputPropertyEntry : outputTransportAdaptorProperties.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        TransportAdaptorManagerConstants.TM_CONF_NS,
                        TransportAdaptorManagerConstants.TM_ELE_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
                propertyElement.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, outputPropertyEntry.getKey(), null);
                propertyElement.setText(outputPropertyEntry.getValue());
                outputPropertyElement.addChild(propertyElement);
            }
        }

        //common transport adaptor properties

        if (transportAdaptorConfiguration.getTransportAdaptorCommonProperties() != null) {
            commonTransportAdaptorProperties = transportAdaptorConfiguration.getTransportAdaptorCommonProperties();
            for (Map.Entry<String, String> commonPropertyEntry : commonTransportAdaptorProperties.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        TransportAdaptorManagerConstants.TM_CONF_NS,
                        TransportAdaptorManagerConstants.TM_ELE_PROPERTY, TransportAdaptorManagerConstants.TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
                propertyElement.addAttribute(TransportAdaptorManagerConstants.TM_ATTR_NAME, commonPropertyEntry.getKey(), null);
                propertyElement.setText(commonPropertyEntry.getValue());
                transportAdaptorItem.addChild(propertyElement);

            }
        }
        return transportAdaptorItem;
    }


    public static boolean validateTransportAdaptorConfiguration(
            TransportAdaptorConfiguration transportAdaptorConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        TransportAdaptorService transportAdaptorService = TransportAdaptorHolder.getInstance().getTransportAdaptorService();
        TransportAdaptorDto transportAdaptorDto = transportAdaptorService.getTransportAdaptorDto(transportAdaptorConfiguration.getType());

        List<Property> inputTransportAdaptorProperties = transportAdaptorDto.getAdaptorInPropertyList();
        List<Property> outputTransportAdaptorProperties = transportAdaptorDto.getAdaptorOutPropertyList();
        List<Property> commonTransportAdaptorProperties = transportAdaptorDto.getAdaptorCommonPropertyList();

        Map<String, String> inputAdaptorConfigurationPropertyList = null;
        Map<String, String> outputAdaptorConfigurationPropertyList = null;
        Map<String, String> commonAdaptorConfigurationPropertyList = null;

        if (transportAdaptorConfiguration.getInputTransportAdaptorConfiguration() != null) {
            inputAdaptorConfigurationPropertyList = transportAdaptorConfiguration.getInputTransportAdaptorConfiguration().getPropertyList();
        }

        if (transportAdaptorConfiguration.getOutputTransportAdaptorConfiguration() != null) {
            outputAdaptorConfigurationPropertyList = transportAdaptorConfiguration.getOutputTransportAdaptorConfiguration().getPropertyList();
        }

        if (transportAdaptorConfiguration.getTransportAdaptorCommonProperties() != null) {
            commonAdaptorConfigurationPropertyList = transportAdaptorConfiguration.getTransportAdaptorCommonProperties();
        }

        if (transportAdaptorDto.getSupportedTransportAdaptorType().equals(TransportAdaptorDto.TransportAdaptorType.IN)) {
            if (outputAdaptorConfigurationPropertyList != null) {
                throw new TransportAdaptorManagerConfigurationException("Not a valid transport adaptor, This transport adaptor not support for output transports ");
            }
        }

        if (transportAdaptorDto.getSupportedTransportAdaptorType().equals(TransportAdaptorDto.TransportAdaptorType.OUT)) {
            if (inputAdaptorConfigurationPropertyList != null) {
                throw new TransportAdaptorManagerConfigurationException("Not a valid transport adaptor, This transport adaptor not support for input transports ");
            }
        }

        if (inputTransportAdaptorProperties != null && (inputAdaptorConfigurationPropertyList != null)) {
            Iterator propertyIterator = inputTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {
                    if (!inputAdaptorConfigurationPropertyList.containsKey(transportProperty.getPropertyName())) {
                        log.error("Required input property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                        throw new TransportAdaptorManagerConfigurationException("Required input property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                    }
                }
            }

            Iterator inputPropertyIterator = inputTransportAdaptorProperties.iterator();
            List<String> inputPropertyNames = new ArrayList<String>();
            while (inputPropertyIterator.hasNext()) {
                Property inputProperty = (Property) inputPropertyIterator.next();
                inputPropertyNames.add(inputProperty.getPropertyName());
            }


            Iterator propertyConfigurationIterator = inputAdaptorConfigurationPropertyList.keySet().iterator();
            while (propertyConfigurationIterator.hasNext()) {
                String transportPropertyName = (String) propertyConfigurationIterator.next();
                if (!inputPropertyNames.contains(transportPropertyName)) {
                    log.error(transportPropertyName + " is not a valid property for this transport adaptor type : "+ transportAdaptorConfiguration.getType());
                    throw new TransportAdaptorManagerConfigurationException(transportPropertyName + " is not a valid property for this transport adaptor type : " + transportAdaptorConfiguration.getType());
                }


            }
        }

        if (outputTransportAdaptorProperties != null && (outputAdaptorConfigurationPropertyList != null)) {
            Iterator propertyIterator = outputTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {
                    if (!outputAdaptorConfigurationPropertyList.containsKey(transportProperty.getPropertyName())) {
                        log.error("Required output property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                        throw new TransportAdaptorManagerConfigurationException("Required output property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                    }
                }
            }

            Iterator outputPropertyIterator = outputTransportAdaptorProperties.iterator();
            List<String> outputPropertyNames = new ArrayList<String>();
            while (outputPropertyIterator.hasNext()) {
                Property outputProperty = (Property) outputPropertyIterator.next();
                outputPropertyNames.add(outputProperty.getPropertyName());
            }

            Iterator propertyConfigurationIterator = outputAdaptorConfigurationPropertyList.keySet().iterator();
            while (propertyConfigurationIterator.hasNext()) {
                String transportPropertyName = (String) propertyConfigurationIterator.next();
                if (!outputPropertyNames.contains(transportPropertyName)) {
                    log.error(transportPropertyName + " is not a valid property for this transport adaptor type : " +  transportAdaptorConfiguration.getType());
                    throw new TransportAdaptorManagerConfigurationException(transportPropertyName + " is not a valid property for this transport adaptor type : " + transportAdaptorConfiguration.getType());
                }

            }
        }

        if (commonTransportAdaptorProperties != null && (commonAdaptorConfigurationPropertyList != null)) {
            Iterator propertyIterator = commonTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {
                    if (!commonAdaptorConfigurationPropertyList.containsKey(transportProperty.getPropertyName())) {
                        log.error("Required common property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                        throw new TransportAdaptorManagerConfigurationException("Required common property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                    }
                }
            }

            Iterator commonPropertyIterator = commonTransportAdaptorProperties.iterator();
            List<String> commonPropertyNames = new ArrayList<String>();
            while (commonPropertyIterator.hasNext()) {
                Property commonProperty = (Property) commonPropertyIterator.next();
                commonPropertyNames.add(commonProperty.getPropertyName());
            }

            Iterator propertyConfigurationIterator = commonAdaptorConfigurationPropertyList.keySet().iterator();
            while (propertyConfigurationIterator.hasNext()) {
                String transportPropertyName = (String) propertyConfigurationIterator.next();
                if (!commonPropertyNames.contains(transportPropertyName)) {
                    log.error(transportPropertyName + " is not a valid property for this transport adaptor type : "+ transportAdaptorConfiguration.getType());
                    throw new TransportAdaptorManagerConfigurationException(transportPropertyName + " is not a valid property for this transport adaptor type : " + transportAdaptorConfiguration.getType());
                }
            }

        }


        return true;
    }
}
