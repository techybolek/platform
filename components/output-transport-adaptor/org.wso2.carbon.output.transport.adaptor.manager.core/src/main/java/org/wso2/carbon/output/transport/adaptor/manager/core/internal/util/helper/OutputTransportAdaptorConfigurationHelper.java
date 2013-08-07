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

package org.wso2.carbon.output.transport.adaptor.manager.core.internal.util.helper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorDto;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.config.InternalOutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.ds.OutputTransportAdaptorHolder;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.util.OutputTransportAdaptorManagerConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class used to OM element related stuffs and for validating the xml files.
 */

public class OutputTransportAdaptorConfigurationHelper {

    private static final Log log = LogFactory.getLog(OutputTransportAdaptorConfigurationHelper.class);

    private OutputTransportAdaptorConfigurationHelper() {
    }

    public static OutputTransportAdaptorConfiguration fromOM(OMElement transportConfigOMElement) {

        OutputTransportAdaptorConfiguration transportAdaptorConfiguration = new OutputTransportAdaptorConfiguration();
        transportAdaptorConfiguration.setName(transportConfigOMElement.getAttributeValue(
                new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_NAME)));
        transportAdaptorConfiguration.setType(transportConfigOMElement.getAttributeValue(
                new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_TYPE)));

        if (transportConfigOMElement.getAttributeValue(new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS)) != null && transportConfigOMElement.getAttributeValue(new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS)).equals("true")) {
            transportAdaptorConfiguration.setEnableStatistics(true);
        }

        if (transportConfigOMElement.getAttributeValue(new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_TRACING)) != null && transportConfigOMElement.getAttributeValue(new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_TRACING)).equals("true")) {
            transportAdaptorConfiguration.setEnableTracing(true);
        }
        //Output Adaptor Properties

        Iterator propertyIter = transportConfigOMElement.getChildrenWithName(
                new QName(OutputTransportAdaptorManagerConstants.TM_CONF_NS, OutputTransportAdaptorManagerConstants.TM_ELE_PROPERTY));
        InternalOutputTransportAdaptorConfiguration outputTransportAdaptorPropertyConfiguration = new InternalOutputTransportAdaptorConfiguration();
        if (propertyIter.hasNext()) {
            for (; propertyIter.hasNext(); ) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(OutputTransportAdaptorManagerConstants.TM_ATTR_NAME));
                String value = propertyOMElement.getText();
                outputTransportAdaptorPropertyConfiguration.addTransportAdaptorProperty(name, value);
            }
        }
        transportAdaptorConfiguration.setOutputConfiguration(outputTransportAdaptorPropertyConfiguration);

        return transportAdaptorConfiguration;
    }


    public static OMElement transportAdaptorConfigurationToOM(
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration) {
        String transportAdaptorName = transportAdaptorConfiguration.getName();
        String transportAdaptorType = transportAdaptorConfiguration.getType();

        Map<String, String> outputTransportAdaptorProperties = null;

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement transportAdaptorItem = factory.createOMElement(new QName(
                OutputTransportAdaptorManagerConstants.TM_ELE_ROOT_ELEMENT));
        transportAdaptorItem.declareDefaultNamespace(OutputTransportAdaptorManagerConstants.TM_CONF_NS);
        transportAdaptorItem.addAttribute(OutputTransportAdaptorManagerConstants.TM_ATTR_NAME, transportAdaptorName,
                                          null);
        transportAdaptorItem.addAttribute(OutputTransportAdaptorManagerConstants.TM_ATTR_TYPE, transportAdaptorType,
                                          null);

        if (transportAdaptorConfiguration.isEnableStatistics()) {
            transportAdaptorItem.addAttribute(OutputTransportAdaptorManagerConstants.TM_ATTR_STATISTICS, "true",
                                              null);
        }

        if (transportAdaptorConfiguration.isEnableTracing()) {
            transportAdaptorItem.addAttribute(OutputTransportAdaptorManagerConstants.TM_ATTR_TRACING, "true",
                                              null);
        }


        if (transportAdaptorConfiguration.getOutputConfiguration() != null) {
            outputTransportAdaptorProperties = transportAdaptorConfiguration.getOutputConfiguration().getProperties();
            for (Map.Entry<String, String> outputPropertyEntry : outputTransportAdaptorProperties.entrySet()) {
                OMElement propertyElement = factory.createOMElement(new QName(
                        OutputTransportAdaptorManagerConstants.TM_ELE_PROPERTY));
                propertyElement.declareDefaultNamespace(OutputTransportAdaptorManagerConstants.TM_CONF_NS);
                propertyElement.addAttribute(OutputTransportAdaptorManagerConstants.TM_ATTR_NAME, outputPropertyEntry.getKey(), null);
                propertyElement.setText(outputPropertyEntry.getValue());
                transportAdaptorItem.addChild(propertyElement);
            }
        }

        return transportAdaptorItem;
    }


    public static boolean validateTransportAdaptorConfiguration(
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        OutputTransportAdaptorService transportAdaptorService = OutputTransportAdaptorHolder.getInstance().getOutputTransportAdaptorService();
        OutputTransportAdaptorDto transportAdaptorDto = transportAdaptorService.getTransportAdaptorDto(transportAdaptorConfiguration.getType());

        if (transportAdaptorDto == null) {
            return false;
        }


        List<Property> outputTransportAdaptorProperties = transportAdaptorDto.getAdaptorPropertyList();

        Map<String, String> outputAdaptorConfigurationPropertyList = null;

        if (transportAdaptorConfiguration.getOutputConfiguration() != null) {
            outputAdaptorConfigurationPropertyList = transportAdaptorConfiguration.getOutputConfiguration().getProperties();
        }


        if (outputTransportAdaptorProperties != null && (outputAdaptorConfigurationPropertyList != null)) {
            Iterator propertyIterator = outputTransportAdaptorProperties.iterator();
            while (propertyIterator.hasNext()) {
                Property transportProperty = (Property) propertyIterator.next();
                if (transportProperty.isRequired()) {
                    if (!outputAdaptorConfigurationPropertyList.containsKey(transportProperty.getPropertyName())) {
                        log.error("Required output property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
                        throw new OutputTransportAdaptorManagerConfigurationException("Required output property : " + transportProperty.getPropertyName() + " not in the transport adaptor configuration");
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
                    log.error(transportPropertyName + " is not a valid property for this transport adaptor type : " + transportAdaptorConfiguration.getType());
                    throw new OutputTransportAdaptorManagerConfigurationException(transportPropertyName + " is not a valid property for this transport adaptor type : " + transportAdaptorConfiguration.getType());
                }

            }
        }

        return true;
    }
}
