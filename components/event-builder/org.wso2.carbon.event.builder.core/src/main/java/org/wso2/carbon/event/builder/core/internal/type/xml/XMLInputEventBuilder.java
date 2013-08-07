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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.BasicEventListener;
import org.wso2.carbon.event.builder.core.EventListener;
import org.wso2.carbon.event.builder.core.Wso2EventListener;
import org.wso2.carbon.event.builder.core.config.EventBuilder;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.ReflectionBasedObjectSupplier;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathData;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class XMLInputEventBuilder implements EventBuilder {

    private static final Log log = LogFactory.getLog(XMLInputEventBuilder.class);
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<XPathData> attributeXpathList = new ArrayList<XPathData>();
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private AxisConfiguration axisConfiguration = null;
    private StreamDefinition exportedStreamDefinition = null;
    private String subscriptionId = null;
    private XPathDefinition xPathDefinition = null;
    private ReflectionBasedObjectSupplier reflectionBasedObjectSupplier = new ReflectionBasedObjectSupplier();

    public XMLInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration) {
        this(eventBuilderConfiguration, axisConfiguration, true);
    }

    public XMLInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration, boolean loadConfiguration)
            throws
            EventBuilderConfigurationException {

        this.axisConfiguration = axisConfiguration;
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        if (loadConfiguration) {
            loadEventBuilderConfiguration();
        }
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
                    exportedStreamDefinition.addPayloadData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
                } catch (JaxenException e) {
                    String errMsg = "Error parsing XPath expression: " + xpathExpr;
                    log.error(errMsg, e);
                    throw new EventBuilderConfigurationException(errMsg, e);
                }
            }
            if (!isStreamDefinitionValidForConfiguration(eventBuilderConfiguration, exportedStreamDefinition)) {
                throw new EventBuilderValidationException("Exported stream definition does not match the specified configuration.");
            }
        }
    }

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
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return this.eventBuilderConfiguration;
    }

    @Override
    public void loadEventBuilderConfiguration() {
        if (this.eventBuilderConfiguration != null) {
            String outputStreamName = eventBuilderConfiguration.getToStreamName();
            String outputStreamVersion = eventBuilderConfiguration.getToStreamVersion();
            try {
                this.exportedStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
            } catch (MalformedStreamDefinitionException e) {
                throw new EventBuilderConfigurationException("Could not create stream definition with " + outputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + outputStreamVersion);
            }

            subscribeToTransportAdaptor();
            createMapping(eventBuilderConfiguration, exportedStreamDefinition, attributeXpathList);
            this.eventBuilderConfiguration.setDeploymentStatus(DeploymentStatus.DEPLOYED);
        }
    }

    @Override
    public void subscribe(EventListener eventListener) throws EventBuilderConfigurationException {
        if (eventListener instanceof BasicEventListener) {
            basicEventListeners.add((BasicEventListener) eventListener);
        } else if (eventListener instanceof Wso2EventListener) {
            wso2EventListeners.add((Wso2EventListener) eventListener);
        }
    }

    @Override
    public void unsubscribe(EventListener eventListener) {
        if (eventListener instanceof BasicEventListener) {
            basicEventListeners.remove(eventListener);
        } else if (eventListener instanceof Wso2EventListener) {
            wso2EventListeners.remove(eventListener);
        }
    }

    @Override
    public StreamDefinition getExportedStreamDefinition() {
        return this.exportedStreamDefinition;
    }

    @Override
    public void unsubscribeFromTransportAdaptor(
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration) {
        if (this.subscriptionId != null) {
            EventBuilderServiceValueHolder.getInputTransportAdaptorService().unsubscribe(eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), inputTransportAdaptorConfiguration, axisConfiguration, this.subscriptionId);
            this.subscriptionId = null;
        }
    }

    @Override
    public void subscribeToTransportAdaptor() {
        if (this.subscriptionId == null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(this.axisConfiguration).getTenantId();
            String inputTransportAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName();

            try {
                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService().getInputTransportAdaptorConfiguration(inputTransportAdaptorName, tenantId);
                this.subscriptionId = EventBuilderServiceValueHolder.getInputTransportAdaptorService().subscribe(
                        inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), new XmlInputTransportListener(), axisConfiguration);
            } catch (InputTransportAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input transport adaptor " + inputTransportAdaptorName + ":\n" + e.getMessage());
                throw new EventBuilderConfigurationException(e);
            }
        }

    }

    private void processEvent(Object obj) {
        XMLInputMapping xmlInputMapping = (XMLInputMapping) this.eventBuilderConfiguration.getInputMapping();
        if (xmlInputMapping.isBatchProcessingEnabled()) {
            sendMultipleEvents(obj);
        } else {
            sendEvent(obj);
        }
    }

    private void sendMultipleEvents(Object obj) {
        if (obj instanceof OMElement) {
            OMElement events = (OMElement) obj;
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Received batch of events as OMElement.\n" + events.toString());
            }
            Iterator childIterator = events.getChildElements();
            while (childIterator.hasNext()) {
                Object eventObj = childIterator.next();
                sendEvent(eventObj);
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

    private void sendEvent(Object obj) {
        if (obj instanceof OMElement) {
            OMElement eventOMElement = (OMElement) obj;
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Received event as OMElement.\n" + eventOMElement.toString());
            }

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
            Object[] objArray = objList.toArray(new Object[objList.size()]);
            if (!this.basicEventListeners.isEmpty()) {
                if (eventBuilderConfiguration.isTraceEnabled()) {
                    trace.info("[Event-Builder] Sending event object array " + Arrays.toString(objArray) + " to all registered basic event listeners");
                }
                for (BasicEventListener basicEventListener : basicEventListeners) {
                    basicEventListener.onEvent(objArray);
                }
            }
            if (!this.wso2EventListeners.isEmpty()) {
                Event event = new Event(exportedStreamDefinition.getStreamId(), System.currentTimeMillis(), null, null, objArray);
                if (eventBuilderConfiguration.isTraceEnabled()) {
                    trace.info("[Event-Builder] Sending event " + event.toString() + " to all registered wso2 event listeners");
                }
                for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                    wso2EventListener.onEvent(event);
                }
            }
        }
    }

    private class XmlInputTransportListener extends InputTransportAdaptorListener {
        @Override
        public void addEventDefinition(Object o) {
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Notifying event definition addition " + o.toString() + " to event listeners");
            }
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onAddDefinition(o);
            }
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onAddDefinition(o);
            }
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Successfully notified all registered listeners of adding \n" + o.toString());
            }
        }

        @Override
        public void removeEventDefinition(Object o) {
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Notifying event definition removal " + o.toString() + " to event listeners");
            }
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onRemoveDefinition(o);
            }
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onRemoveDefinition(o);
            }
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Successfully notified all registered listeners of removing \n" + o.toString());
            }
        }

        @Override
        public void onEvent(Object o) {
            try {
                processEvent(o);
            } catch (EventBuilderConfigurationException e) {
                throw new InputTransportAdaptorEventProcessingException("Cannot send create an event from input:", e);
            }
        }
    }

}
