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

package org.wso2.carbon.event.builder.core.internal.type.text;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
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
public class TextBuilderConfigBuilder extends EventBuilderConfigBuilder {

    private static TextBuilderConfigBuilder instance = new TextBuilderConfigBuilder();

    private TextBuilderConfigBuilder() {

    }

    public static TextBuilderConfigBuilder getInstance() {
        return TextBuilderConfigBuilder.instance;
    }

    @Override
    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventBuilderValidationException, EventBuilderConfigurationException {
        if (!validateTextMapping(mappingElement)) {
            throw new EventBuilderConfigurationException("Text Mapping is not valid! Please check.");
        }

        TextInputMapping textInputMapping = new TextInputMapping();
        Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
        List<InputMappingAttribute> inputMappingAttributeList = new ArrayList<InputMappingAttribute>();
        while (propertyIterator.hasNext()) {
            OMElement propertyOMElement = (OMElement) propertyIterator.next();
            inputMappingAttributeList.addAll(getInputMappingAttributesFromOM(propertyOMElement));
        }
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
            textInputMapping.addInputMappingAttribute(inputMappingAttribute);
        }

        return textInputMapping;
    }

    @SuppressWarnings("unchecked")
    private boolean validateTextMapping(OMElement omElement) {
        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            OMElement childElement = mappingIterator.next();
            if (!childElement.getLocalName().equals(EventBuilderConstants.EB_ELEMENT_PROPERTY)) {
                return false;
            }
        }

        return count != 0;
    }

    @Override
    public OMElement inputMappingToOM(
            InputMapping inputMapping, OMFactory factory) {

        TextInputMapping textInputMapping = (TextInputMapping) inputMapping;

        OMElement mappingOMElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        mappingOMElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, EventBuilderConstants.EB_TEXT_MAPPING_TYPE, null);

        List<InputMappingAttribute> inputMappingAttributes = textInputMapping.getInputMappingAttributes();
        InputMappingAttribute prevInputMappingAttribute = null;
        OMElement propertyOMElement = null;
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (prevInputMappingAttribute != null && prevInputMappingAttribute.getFromElementKey().equals(inputMappingAttribute.getFromElementKey())) {
                addAnotherToProperty(factory, propertyOMElement, inputMappingAttribute);
            } else {
                propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
            prevInputMappingAttribute = inputMappingAttribute;
        }

        return mappingOMElement;
    }

    @Override
    protected InputMappingAttribute getInputMappingAttributeFromOM(OMElement omElement) {
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        String regex = propertyFromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_REGEX));
        String outputPropertyName = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        AttributeType outputPropertyType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE)));
        String defaultValue = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_DEFAULT_VALUE));

        InputMappingAttribute inputMappingAttribute = new InputMappingAttribute(regex, outputPropertyName, outputPropertyType);
        inputMappingAttribute.setDefaultValue(defaultValue);

        return inputMappingAttribute;
    }

    protected List<InputMappingAttribute> getInputMappingAttributesFromOM(OMElement omElement) {
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        Iterator toElementIterator = omElement.getChildrenWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        List<InputMappingAttribute> inputMappingAttributeList = new ArrayList<InputMappingAttribute>();
        while (toElementIterator.hasNext()) {
            OMElement propertyToElement = (OMElement) toElementIterator.next();
            String regex = propertyFromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_REGEX));
            String outputPropertyName = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
            AttributeType outputPropertyType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE)));
            String defaultValue = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_DEFAULT_VALUE));

            InputMappingAttribute inputMappingAttribute = new InputMappingAttribute(regex, outputPropertyName, outputPropertyType);
            inputMappingAttribute.setDefaultValue(defaultValue);
            inputMappingAttributeList.add(inputMappingAttribute);
        }
        return inputMappingAttributeList;
    }

    @Override
    protected OMElement getPropertyOmElement(OMFactory factory,
                                             InputMappingAttribute inputMappingAttribute) {
        OMElement propertyOmElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        fromElement.addAttribute(EventBuilderConstants.EB_ATTR_REGEX, inputMappingAttribute.getFromElementKey(), null);

        OMElement toElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, getAttributeType(inputMappingAttribute.getToElementType()), null);
        if (inputMappingAttribute.getDefaultValue() != null && !inputMappingAttribute.getDefaultValue().isEmpty()) {
            toElement.addAttribute(EventBuilderConstants.EB_ATTR_DEFAULT_VALUE, inputMappingAttribute.getDefaultValue(), null);
        }

        propertyOmElement.addChild(fromElement);
        propertyOmElement.addChild(toElement);

        return propertyOmElement;
    }

    protected void addAnotherToProperty(OMFactory factory, OMElement propertyOmElement,
                                        InputMappingAttribute inputMappingAttribute) {
        OMElement toElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, getAttributeType(inputMappingAttribute.getToElementType()), null);
        if (inputMappingAttribute.getDefaultValue() != null && !inputMappingAttribute.getDefaultValue().isEmpty()) {
            toElement.addAttribute(EventBuilderConstants.EB_ATTR_DEFAULT_VALUE, inputMappingAttribute.getDefaultValue(), null);
        }

        propertyOmElement.addChild(toElement);
    }

}




