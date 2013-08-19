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

package org.wso2.carbon.event.builder.core.internal.type.wso2event;

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
 * This class is used to read the values of the event builder configuration defined in XML configuration files.
 * This class extends has methods to read syntax specific to Wso2EventInputMapping
 */
public class Wso2EventBuilderConfigBuilder extends EventBuilderConfigBuilder {

    private static Wso2EventBuilderConfigBuilder instance = new Wso2EventBuilderConfigBuilder();

    private Wso2EventBuilderConfigBuilder() {

    }

    public static Wso2EventBuilderConfigBuilder getInstance() {
        return Wso2EventBuilderConfigBuilder.instance;
    }

    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventBuilderConfigurationException {

        if (!validateWso2EventMapping(mappingElement)) {
            throw new EventBuilderConfigurationException("Wso2Event Mapping is not valid, Please check");
        }

        Wso2EventInputMapping wso2EventInputMapping = new Wso2EventInputMapping();

        int metaAttribCount = 0, correlationAttribCount = 0, payloadAttribCount = 0;
        Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
        while (propertyIterator.hasNext()) {
            OMElement propertyOMElement = (OMElement) propertyIterator.next();
            InputMappingAttribute inputMappingAttribute = getInputMappingAttributeFromOM(propertyOMElement);
            if (inputMappingAttribute.getFromElementType().equals(EventBuilderConstants.META_DATA_VAL)) {
                inputMappingAttribute.setToStreamPosition(metaAttribCount++);
                wso2EventInputMapping.addMetaInputEventAttribute(inputMappingAttribute);
            } else if (inputMappingAttribute.getFromElementType().equals(EventBuilderConstants.CORRELATION_DATA_VAL)) {
                inputMappingAttribute.setToStreamPosition(correlationAttribCount++);
                wso2EventInputMapping.addCorrelationInputEventAttribute(inputMappingAttribute);
            } else {
                inputMappingAttribute.setToStreamPosition(payloadAttribCount++);
                wso2EventInputMapping.addPayloadInputEventAttribute(inputMappingAttribute);
            }
        }

        return wso2EventInputMapping;
    }

    @Override
    protected InputMappingAttribute getInputMappingAttributeFromOM(OMElement omElement) {

        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        String name = propertyFromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        String dataType = propertyFromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_DATA_TYPE));
        String valueOf = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        AttributeType type = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE)));

        if (valueOf == null) {
            valueOf = name;
        }

        return new InputMappingAttribute(name, valueOf, type, dataType);
    }

    @SuppressWarnings("unchecked")
    private boolean validateWso2EventMapping(OMElement omElement) {
        List<String> supportedChildTags = new ArrayList<String>();
        supportedChildTags.add(EventBuilderConstants.EB_ELEMENT_PROPERTY);

        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            OMElement childElement = mappingIterator.next();
            String childTag = childElement.getLocalName();
            if (!supportedChildTags.contains(childTag)) {
                return false;
            }
        }

        return count != 0;
    }

    public OMElement inputMappingToOM(
            InputMapping inputMapping, OMFactory factory) {

        Wso2EventInputMapping Wso2EventInputMapping = (Wso2EventInputMapping) inputMapping;

        List<InputMappingAttribute> metaWSO2EventPropertyConfiguration = Wso2EventInputMapping.getMetaInputMappingAttributes();
        List<InputMappingAttribute> correlationWSO2EventPropertyConfiguration = Wso2EventInputMapping.getCorrelationInputMappingAttributes();
        List<InputMappingAttribute> payloadWSO2EventPropertyConfiguration = Wso2EventInputMapping.getPayloadInputMappingAttributes();

        OMElement mappingOMElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        mappingOMElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, EventBuilderConstants.EB_WSO2EVENT_MAPPING_TYPE, null);

        if (metaWSO2EventPropertyConfiguration.size() > 0) {
            for (InputMappingAttribute inputMappingAttribute : metaWSO2EventPropertyConfiguration) {
                OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
        }

        if (correlationWSO2EventPropertyConfiguration.size() > 0) {
            for (InputMappingAttribute inputMappingAttribute : correlationWSO2EventPropertyConfiguration) {
                OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
        }

        if (payloadWSO2EventPropertyConfiguration.size() > 0) {
            for (InputMappingAttribute inputMappingAttribute : payloadWSO2EventPropertyConfiguration) {
                OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
        }

        return mappingOMElement;
    }

    protected OMElement getPropertyOmElement(OMFactory factory,
                                             InputMappingAttribute inputMappingAttribute) {
        OMElement propertyOMElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        fromElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getFromElementKey(), null);
        fromElement.addAttribute(EventBuilderConstants.EB_ATTR_DATA_TYPE, inputMappingAttribute.getFromElementType(), null);

        OMElement toElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, getAttributeType(inputMappingAttribute.getToElementType()), null);

        propertyOMElement.addChild(fromElement);
        propertyOMElement.addChild(toElement);

        return propertyOMElement;
    }

}




