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

package org.wso2.carbon.input.transport.adaptor.manager.core.internal.util.helper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorDto;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.config.InternalInputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.ds.InputTransportAdaptorHolder;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.util.InputTransportAdaptorManagerConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class used to OM element related stuffs and for validating the xml files.
 */

public class InputTransportAdaptorConfigurationHelper {

    private static final Log log = LogFactory.getLog(InputTransportAdaptorConfigurationHelper.class);

    private InputTransportAdaptorConfigurationHelper() {
    }

    public static InputTransportAdaptorConfiguration fromOM(OMElement transportConfigOMElement) {

        InputTransportAdaptorConfiguration transportAdaptorConfiguration = new InputTransportAdaptorConfiguration();
        transportAdaptorConfiguration.setName(transportConfigOMElement.getAttributeValue(
                new QName(InputTransportAdaptorManagerConstants.TM_ATTR_NAME)));
        transportAdaptorConfiguration.setType(transportConfigOMElement.getAttributeValue(
                new QName(InputTransportAdaptorManagerConstants.TM_ATTR_TYPE)));

        if (transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS)) != null && transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS)).equals(InputTransportAdaptorManagerConstants.TM_VALUE_ENABLE)) {
            transportAdaptorConfiguration.setEnableStatistics(true);
        } else if (transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS)) != null && transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS)).equals(InputTransportAdaptorManagerConstants.TM_VALUE_DISABLE)) {
            transportAdaptorConfiguration.setEnableStatistics(false);
        }

        if (transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_TRACING)) != null && transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_TRACING)).equals(InputTransportAdaptorManagerConstants.TM_VALUE_ENABLE)) {
            transportAdaptorConfiguration.setEnableTracing(true);
        } else if (transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_TRACING)) != null && transportConfigOMElement.getAttributeValue(new QName(InputTransportAdaptorManagerConstants.TM_ATTR_TRACING)).equals(InputTransportAdaptorManagerConstants.TM_VALUE_DISABLE)) {
            transportAdaptorConfiguration.setEnableTracing(false);
        }


        //Input Adaptor Properties

        Iterator propertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(InputTransportAdaptorManagerConstants.TM_CONF_NS, InputTransportAdaptorManagerConstants.TM_ELE_PROPERTY));
        InternalInputTransportAdaptorConfiguration internalInputTransportAdaptorConfiguration = new InternalInputTransportAdaptorConfiguration();
        if (propertyIter.hasNext()) {
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(InputTransportAdaptorManagerConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                internalInputTransportAdaptorConfiguration.addTransportAdaptorProperty(name, value);
            }
        }
        transportAdaptorConfiguration.setInputConfiguration(internalInputTransportAdaptorConfiguration);

        return transportAdaptorConfiguration;
    }


    public static OMElement transportAdaptorConfigurationToOM(
            InputTransportAdaptorConfiguration transportAdaptorConfiguration) {
        String transportAdaptorName = transportAdaptorConfiguration.getName();
        String transportAdaptorType = transportAdaptorConfiguration.getType();

        Map<String, String> inputTransportAdaptorProperties = null;

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement transportAdaptorItem = factory.createOMElement(new QName(
                InputTransportAdaptorManagerConstants.TM_ELE_ROOT_ELEMENT));
        transportAdaptorItem.declareDefaultNamespace(InputTransportAdaptorManagerConstants.TM_CONF_NS);
        transportAdaptorItem.addAttribute(InputTransportAdaptorManagerConstants.TM_ATTR_NAME, transportAdaptorName,
                                          null);
        transportAdaptorItem.addAttribute(InputTransportAdaptorManagerConstants.TM_ATTR_TYPE, transportAdaptorType,
                                          null);

        if (transportAdaptorConfiguration.isEnableStatistics()) {
            transportAdaptorItem.addAttribute(InputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS, InputTransportAdaptorManagerConstants.TM_VALUE_ENABLE,
                                              null);
        } else if (!transportAdaptorConfiguration.isEnableStatistics()) {
            transportAdaptorItem.addAttribute(InputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS, InputTransportAdaptorManagerConstants.TM_VALUE_DISABLE,
                                              null);
        }


        if (transportAdaptorConfiguration.isEnableTracing()) {
            transportAdaptorItem.addAttribute(InputTransportAdaptorManagerConstants.TM_ATTR_TRACING, InputTransportAdaptorManagerConstants.TM_VALUE_ENABLE,
                                              null);
        } else if (!transportAdaptorConfiguration.isEnableTracing()) {
            transportAdaptorItem.addAttribute(InputTransportAdaptorManagerConstants.TM_ATTR_TRACING, InputTransportAdaptorManagerConstants.TM_VALUE_DISABLE,
                                              null);
        }

        if (transportAdaptorConfiguration.getInputConfiguration() != null) {
            inputTransportAdaptorProperties = transportAdaptorConfiguration.getInputConfiguration().getProperties();
            for (Map.Entry<String, String> inputPropertyEntry : inputTransportAdaptorProperties.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        InputTransportAdaptorManagerConstants.TM_ELE_PROPERTY));
                propertyElement.declareDefaultNamespace(InputTransportAdaptorManagerConstants.TM_CONF_NS);
                propertyElement.addAttribute(InputTransportAdaptorManagerConstants.TM_ATTR_NAME, inputPropertyEntry.getKey(), null);
                propertyElement.setText(inputPropertyEntry.getValue());
                transportAdaptorItem.addChild(propertyElement);
            }
        }

        return transportAdaptorItem;
    }


    public static boolean validateTransportAdaptorConfiguration(
            InputTransportAdaptorConfiguration transportAdaptorConfiguration)
            throws InputTransportAdaptorManagerConfigurationException {

        InputTransportAdaptorService transportAdaptorService = InputTransportAdaptorHolder.getInstance().getInputTransportAdaptorService();
        InputTransportAdaptorDto transportAdaptorDto = transportAdaptorService.getTransportAdaptorDto(transportAdaptorConfiguration.getType());

        if (transportAdaptorDto == null) {
            return false;
        }


        List<Property> inputTransportAdaptorProperties = transportAdaptorDto.getAdaptorPropertyList();

        Map<String, String> inputAdaptorConfigurationPropertyList = null;

        if (transportAdaptorConfiguration.getInputConfiguration() != null) {
            inputAdaptorConfigurationPropertyList = transportAdaptorConfiguration.getInputConfiguration().getProperties();
        }


        if (inputTransportAdaptorProperties != null && (inputAdaptorConfigurationPropertyList != null)) {
            Iterator propertyIterator = inputTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {
                    if (!inputAdaptorConfigurationPropertyList.containsKey(transportProperty.getPropertyName())) {
                        log.error("Required input property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                        throw new InputTransportAdaptorManagerConfigurationException("Required input property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
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
                    log.error(transportPropertyName + " is not a valid property for this transport adaptor type : " + transportAdaptorConfiguration.getType());
                    throw new InputTransportAdaptorManagerConfigurationException(transportPropertyName + " is not a valid property for this Input Transport Adaptor type : " + transportAdaptorConfiguration.getType());
                }


            }
        }

        return true;
    }
}
