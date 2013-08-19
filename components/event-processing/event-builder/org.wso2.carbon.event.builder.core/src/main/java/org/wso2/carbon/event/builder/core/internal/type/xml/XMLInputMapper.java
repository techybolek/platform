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

package org.wso2.carbon.event.builder.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventDispatcher;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.ReflectionBasedObjectSupplier;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathData;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XMLInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(XMLInputMapper.class);
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<XPathData> attributeXpathList = new ArrayList<XPathData>();
    private StreamDefinition exportedStreamDefinition = null;
    private EventDispatcher eventDispatcher = null;
    private XPathDefinition xPathDefinition = null;
    private ReflectionBasedObjectSupplier reflectionBasedObjectSupplier = new ReflectionBasedObjectSupplier();

    public XMLInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                          EventDispatcher eventDispatcher) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.eventDispatcher = eventDispatcher;
    }

    public void createMapping(EventBuilderConfiguration eventBuilderConfiguration,
                              StreamDefinition exportedStreamDefinition,
                              List<XPathData> attributeXpathList) {
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof XMLInputMapping) {
            XMLInputMapping xmlInputMapping = (XMLInputMapping) eventBuilderConfiguration.getInputMapping();
            XPathDefinition xPathDefinition = xmlInputMapping.getXpathDefinition();
            for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
                String xpathExpr = inputMappingAttribute.getFromElementKey();
                try {
                    AXIOMXPath xpath = new AXIOMXPath(xpathExpr);
                    if (xPathDefinition != null && !xPathDefinition.isEmpty()) {
                        xpath.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
                    }
                    String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                    attributeXpathList.add(new XPathData(xpath, type, inputMappingAttribute.getDefaultValue()));
                } catch (JaxenException e) {
                    throw new EventBuilderConfigurationException("Error parsing XPath expression: " + xpathExpr, e);
                }
            }
            if (!isStreamDefinitionValidForConfiguration(eventBuilderConfiguration, exportedStreamDefinition)) {
                throw new EventBuilderValidationException("Exported stream definition does not match the specified configuration.");
            }
        }
    }

    @Override
    public void processInputEvent(Object obj) {
        XMLInputMapping xmlInputMapping = (XMLInputMapping) this.eventBuilderConfiguration.getInputMapping();
        if (xmlInputMapping.isBatchProcessingEnabled()) {
            processMultipleEvents(obj);
        } else {
            processSingleEvent(obj);
        }
    }

    @Override
    public boolean isStreamDefinitionValidForConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            StreamDefinition exportedStreamDefinition) {
        if (!(eventBuilderConfiguration.getToStreamName().equals(exportedStreamDefinition.getName())
              && eventBuilderConfiguration.getToStreamVersion().equals(exportedStreamDefinition.getVersion()))) {
            return false;
        }
        if (eventBuilderConfiguration.getInputMapping() instanceof XMLInputMapping) {
            XMLInputMapping xmlInputMapping = (XMLInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
                Attribute attribute = new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
                if (!exportedStreamDefinition.getPayloadData().contains(attribute)) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public StreamDefinition createExportedStreamDefinition() {
        String inputStreamName = this.eventBuilderConfiguration.getToStreamName();
        String inputStreamVersion = this.eventBuilderConfiguration.getToStreamVersion();
        StreamDefinition toStreamDefinition;
        XMLInputMapping xmlInputMapping = (XMLInputMapping) this.eventBuilderConfiguration.getInputMapping();
        try {
            toStreamDefinition = new StreamDefinition(this.eventBuilderConfiguration.getToStreamName(), this.eventBuilderConfiguration.getToStreamVersion());
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException("Could not create stream definition with " + inputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + inputStreamVersion);
        }

        if (xmlInputMapping != null) {
            EventBuilderUtil.addAttributesToStreamDefinition(toStreamDefinition, xmlInputMapping.getInputMappingAttributes(), EventBuilderConstants.PAYLOAD_DATA_VAL);
        }
        this.exportedStreamDefinition = toStreamDefinition;
        return toStreamDefinition;
    }

    @Override
    public void createMapping(Object eventDefinition) throws EventBuilderConfigurationException {
        createMapping(this.eventBuilderConfiguration, this.exportedStreamDefinition, this.attributeXpathList);
    }

    @Override
    public void validateInputStreamAttributes(StreamDefinition inputStreamDefinition)
            throws EventBuilderConfigurationException {
        //Not applicable to XML Input Mapping
    }

    private void processMultipleEvents(Object obj) {
        if (obj instanceof OMElement) {
            OMElement events = (OMElement) obj;
            Iterator childIterator = events.getChildElements();
            while (childIterator.hasNext()) {
                Object eventObj = childIterator.next();
                processSingleEvent(eventObj);
                /**
                 * Usually the global lookup '//' is used in the XPATH expression which works fine for 'single event mode'.
                 * However, if global lookup is used, it will return the first element from the whole document as specified in
                 * XPATH-2.0 Specification. Therefore the same XPATH expression that works fine in 'single event mode' will
                 * always return the first element of a batch in 'batch mode'. Therefore to return what the
                 * user expects, each child element is removed after sending to simulate an iteration for the
                 * global lookup.
                 */
                childIterator.remove();
            }
        }
    }

    private void processSingleEvent(Object obj) {
        Object[] outObjArray = null;
        if (obj instanceof OMElement) {
            OMElement eventOMElement = (OMElement) obj;
            OMNamespace omNamespace = null;
            if (this.xPathDefinition == null || this.xPathDefinition.isEmpty()) {
                omNamespace = eventOMElement.getNamespace();
            }
            List<Object> objList = new ArrayList<Object>();
            for (XPathData xpathData : attributeXpathList) {
                AXIOMXPath xpath = xpathData.getXpath();
                OMElement omElementResult = null;
                String type = xpathData.getType();
                try {
                    if (omNamespace != null) {
                        xpath.addNamespaces(eventOMElement);
                    }
                    omElementResult = (OMElement) xpath.selectSingleNode(eventOMElement);
                    Class<?> beanClass = Class.forName(type);
                    Object returnedObj = null;
                    if (omElementResult != null) {
                        returnedObj = BeanUtil.deserialize(beanClass,
                                                           omElementResult, reflectionBasedObjectSupplier, null);
                    } else if (xpathData.getDefaultValue() != null) {
                        if (!beanClass.equals(String.class)) {
                            Class<?> stringClass = String.class;
                            Method valueOfMethod = beanClass.getMethod("valueOf", stringClass);
                            returnedObj = valueOfMethod.invoke(null, xpathData.getDefaultValue());
                        } else {
                            returnedObj = xpathData.getDefaultValue();
                        }
                        log.warn("Unable to parse XPath to retrieve required attribute. Sending defaults.");
                    } else {
                        log.warn("Unable to parse XPath to retrieve required attribute. Skipping to next attribute.");
                    }
                    objList.add(returnedObj);
                } catch (JaxenException e) {
                    throw new EventBuilderConfigurationException("Error parsing xpath for " + xpath, e);
                } catch (ClassNotFoundException e) {
                    throw new EventBuilderConfigurationException("Cannot find specified class for type " + type);
                } catch (AxisFault axisFault) {
                    throw new EventBuilderConfigurationException("Error deserializing OMElement " + omElementResult, axisFault);
                } catch (NoSuchMethodException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                } catch (InvocationTargetException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                } catch (IllegalAccessException e) {
                    throw new EventBuilderConfigurationException("Error trying to convert default value to specified target type.", e);
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        eventDispatcher.dispatchEvent(outObjArray);
    }
}
