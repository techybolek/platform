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

package org.wso2.carbon.event.builder.core.internal.type.map;

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
import java.util.Iterator;
import java.util.List;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */
public class MapBuilderConfigBuilder extends EventBuilderConfigBuilder {

    private static MapBuilderConfigBuilder instance = new MapBuilderConfigBuilder();

    private MapBuilderConfigBuilder() {

    }

    public static MapBuilderConfigBuilder getInstance() {
        return MapBuilderConfigBuilder.instance;
    }

    //TODO Validation to be moved to validator?
    @SuppressWarnings("unchecked")
    private static boolean validateMapEventMapping(OMElement omElement) {

        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            OMElement childElement = mappingIterator.next();
            String childTag = childElement.getLocalName();
            if (!childTag.equals(EventBuilderConstants.EB_ELEMENT_PROPERTY)) {
                return false;
            }
            count++;
        }

        return count != 0;

    }

    protected OMElement getPropertyOmElement(OMFactory factory,
                                             InputMappingAttribute inputMappingAttribute) {

        OMElement propertyOMElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_PROPERTY));

        OMElement fromElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_FROM));
        fromElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        fromElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getFromElementKey(), null);

        OMElement toElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_TO));
        toElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_NAME, inputMappingAttribute.getToElementKey(), null);
        toElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, getAttributeType(inputMappingAttribute.getToElementType()), null);

        propertyOMElement.addChild(fromElement);
        propertyOMElement.addChild(toElement);

        return propertyOMElement;

    }

    @Override
    public InputMapping fromOM(
            OMElement mappingElement)
            throws EventBuilderValidationException, EventBuilderConfigurationException {


        if (!validateMapEventMapping(mappingElement)) {
            throw new EventBuilderConfigurationException("Map Mapping is not valid, Please check");
        }

        MapInputMapping mapInputMapping = new MapInputMapping();


        if (mappingElement != null) {
            Iterator propertyIterator = mappingElement.getChildrenWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_PROPERTY));
            int positionCount = 0;
            while (propertyIterator.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIterator.next();
                InputMappingAttribute inputMappingAttribute = getInputMappingAttributeFromOM(propertyOMElement);
                inputMappingAttribute.setToStreamPosition(positionCount++);
                mapInputMapping.addInputMappingAttribute(inputMappingAttribute);
            }
        }

        return mapInputMapping;
    }

    @Override
    public OMElement inputMappingToOM(
            InputMapping outputMapping, OMFactory factory) {

        MapInputMapping mapInputMapping = (MapInputMapping) outputMapping;

        List<InputMappingAttribute> outputPropertyConfiguration = mapInputMapping.getInputMappingAttributes();

        OMElement mappingOMElement = factory.createOMElement(new QName(EventBuilderConstants.EB_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventBuilderConstants.EB_CONF_NS);
        mappingOMElement.addAttribute(EventBuilderConstants.EB_ATTR_TYPE, EventBuilderConstants.EB_MAP_MAPPING_TYPE, null);

        if (outputPropertyConfiguration.size() > 0) {
            for (InputMappingAttribute inputMappingAttribute : outputPropertyConfiguration) {
                OMElement propertyOMElement = getPropertyOmElement(factory, inputMappingAttribute);
                propertyOMElement.setNamespace(mappingOMElement.getDefaultNamespace());
                mappingOMElement.addChild(propertyOMElement);
            }
        }
        return mappingOMElement;
    }

    @Override
    protected InputMappingAttribute getInputMappingAttributeFromOM(OMElement omElement) {
        OMElement propertyFromElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_FROM));
        OMElement propertyToElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_TO));

        String name = propertyFromElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        String valueOf = propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_NAME));
        AttributeType type = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(propertyToElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE)));

        if (valueOf == null) {
            valueOf = name;
        }

        return new InputMappingAttribute(name, valueOf, type);
    }


}




