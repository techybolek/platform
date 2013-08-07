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

package org.wso2.carbon.event.builder.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorDto;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigurationValidator {
    private static Log log = LogFactory.getLog(ConfigurationValidator.class);

    public static boolean isValidTransportAdaptor(String transportAdaptorName,
                                                  String transportAdaptorType, int tenantId) {

        InputTransportAdaptorManagerService transportAdaptorManagerService = EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService();
        try {
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = transportAdaptorManagerService.getInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
            if (inputTransportAdaptorConfiguration != null && inputTransportAdaptorConfiguration.getType().equals(transportAdaptorType)) {
                return true;
            }
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            log.error("Exception when retrieving configuration for InputTransportAdaptor '" + transportAdaptorName + "'.", e);
        }

        return false;
    }

    public static boolean validateEventBuilderConfiguration(OMElement ebConfigOmElement) {
        if (!ebConfigOmElement.getLocalName().equals(EventBuilderConstants.EB_ELEMENT_ROOT_ELEMENT)) {
            return false;
        }

        String eventBuilderName = ebConfigOmElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        OMElement fromElement = ebConfigOmElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        OMElement mappingElement = ebConfigOmElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_MAPPING));
        OMElement toElement = ebConfigOmElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        if (eventBuilderName == null || eventBuilderName.isEmpty() || fromElement == null || mappingElement == null || toElement == null) {
            return false;
        }

        String fromTransportAdaptorName = fromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TA_NAME));
        String fromTransportAdaptorType = fromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TA_TYPE));

        if (fromTransportAdaptorName == null || fromTransportAdaptorName.isEmpty() ||
            fromTransportAdaptorType == null || fromTransportAdaptorType.isEmpty()) {
            return false;
        }

        InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration = EventBuilderConfigHelper.getInputTransportMessageConfiguration(fromTransportAdaptorType);

        Iterator fromElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
        Map<String, String> fromPropertyMap = new HashMap<String, String>();
        while (fromElementPropertyIterator.hasNext()) {
            OMElement fromElementProperty = (OMElement) fromElementPropertyIterator.next();
            String propertyName = fromElementProperty.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
            String propertyValue = fromElementProperty.getText();
            fromPropertyMap.put(propertyName, propertyValue);
        }
        for (String propertyKey : inputTransportMessageConfiguration.getInputMessageProperties().keySet()) {
            if (fromPropertyMap.get(propertyKey) == null) {
                return false;
            }
        }

        String mappingType = mappingElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE));
        if (mappingType == null || mappingType.isEmpty() || !validateMappingProperties(mappingElement)) {
            return false;
        }

        String toStreamName = toElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_STREAM_NAME));
        String toStreamVersion = toElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_VERSION));

        if (toStreamName == null || toStreamName.isEmpty() || toStreamVersion == null || toStreamVersion.isEmpty()) {
            return false;
        }

        return true;
    }

    public static void isValidEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration, int tenantId) {
        String fromTransportAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName();
        String fromTransportAdaptorType = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorType();

        if (!ConfigurationValidator.isValidTransportAdaptor(fromTransportAdaptorName, fromTransportAdaptorType, tenantId)) {
            throw new EventBuilderValidationException("Could not validate the input transport adaptor configuration " + fromTransportAdaptorName + " which is a " + fromTransportAdaptorType, fromTransportAdaptorName);
        }

    }

    public static boolean validateSupportedMapping(String transportAdaptorType,
                                                   InputTransportAdaptorDto.MessageType messageType) {

        InputTransportAdaptorService transportAdaptorService = EventBuilderServiceValueHolder.getInputTransportAdaptorService();
        InputTransportAdaptorDto transportAdaptorDto = transportAdaptorService.getTransportAdaptorDto(transportAdaptorType);

        if (transportAdaptorDto == null) {
            return false;
        }

        List<InputTransportAdaptorDto.MessageType> supportedInputMessageTypes = transportAdaptorDto.getSupportedMessageTypes();
        return supportedInputMessageTypes.contains(messageType);
    }

    public static boolean validateMappingProperties(OMElement mappingElement) {
        List<String> supportedChildTags = new ArrayList<String>();
        supportedChildTags.add(EventBuilderConstants.EB_ELEMENT_PROPERTY);

        Iterator<OMElement> mappingIterator = mappingElement.getChildElements();
        while (mappingIterator.hasNext()) {
            OMElement childElement = mappingIterator.next();
            String childTag = childElement.getLocalName();
            if (!supportedChildTags.contains(childTag)) {
                return false;
            }
        }

        return true;
    }
}
