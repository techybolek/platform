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

package org.wso2.carbon.event.builder.core.internal.type.json;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class JsonBuilderConfigBuilder extends EventBuilderConfigBuilder {

    private static JsonBuilderConfigBuilder instance = new JsonBuilderConfigBuilder();

    private JsonBuilderConfigBuilder() {

    }

    public static JsonBuilderConfigBuilder getInstance() {
        return JsonBuilderConfigBuilder.instance;
    }

    public OMElement inputMappingToOM(
            InputMapping inputMapping, OMFactory factory) {

        JsonInputMapping jsonInputMapping = (JsonInputMapping) inputMapping;
        OMElement mappingOMElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        mappingOMElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, EventBuilderConstants.EB_JSON_MAPPING_TYPE, null);

        for (InputMappingAttribute inputMappingAttribute : jsonInputMapping.getInputMappingAttributes()) {
            OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
            propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
            mappingOMElement.addChild(propertyOMElement);
        }

        return mappingOMElement;
    }

    @Override
    protected InputMappingAttribute getInputMappingAttributeFromOM(OMElement omElement) {
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        String jsonPath = propertyFromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_JSONPATH));
        String outputPropertyName = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        AttributeType outputPropertyType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE)));

        return new InputMappingAttribute(jsonPath, outputPropertyName, outputPropertyType);
    }

    @Override
    protected OMElement getPropertyOmElement(OMFactory factory,
                                             InputMappingAttribute inputMappingAttribute) {
        OMElement propertyOmElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        fromElement.addAttribute(EventBuilderConstants.EB_ATTR_JSONPATH, inputMappingAttribute.getFromElementKey(), null);

        OMElement toElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, getAttributeType(inputMappingAttribute.getToElementType()), null);

        propertyOmElement.addChild(fromElement);
        propertyOmElement.addChild(toElement);

        return propertyOmElement;
    }

    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventBuilderConfigurationException {
        if (!validateJsonEventMapping(mappingElement)) {
            throw new EventBuilderConfigurationException("JSON Mapping is not valid! Please check.");
        }

        JsonInputMapping jsonInputMapping = new JsonInputMapping();

        String batchProcessingEnabledProperty = mappingElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_BATCH_PROCESSING_ENABLED_TYPE));
        if (batchProcessingEnabledProperty != null && batchProcessingEnabledProperty.equalsIgnoreCase("true")) {
            jsonInputMapping.setBatchProcessingEnabled(true);
        }

        Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
        while (propertyIterator.hasNext()) {
            OMElement propertyOMElement = (OMElement) propertyIterator.next();
            InputMappingAttribute inputMappingAttribute = getInputMappingAttributeFromOM(propertyOMElement);
            jsonInputMapping.addInputMappingAttribute(inputMappingAttribute);
        }

        return jsonInputMapping;
    }

    @SuppressWarnings("unchecked")
    private boolean validateJsonEventMapping(OMElement omElement) {
        List<String> supportedChildTags = new ArrayList<String>();
        supportedChildTags.add(EventBuilderConstants.EB_ELEMENT_PROPERTY);
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
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




