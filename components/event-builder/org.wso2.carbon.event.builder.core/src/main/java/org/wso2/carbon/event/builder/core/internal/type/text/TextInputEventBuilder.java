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

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
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
import org.wso2.carbon.event.builder.core.internal.type.text.config.RegexData;
import org.wso2.carbon.event.builder.core.internal.util.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class TextInputEventBuilder implements EventBuilder {

    private static final Log log = LogFactory.getLog(TextInputEventBuilder.class);
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<RegexData> attributeRegexList = new ArrayList<RegexData>();
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private AxisConfiguration axisConfiguration = null;
    private StreamDefinition exportedStreamDefinition = null;
    private String subscriptionId = null;
    private boolean binaryInputEnabled = false;

    public TextInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                 AxisConfiguration axisConfiguration) throws
                                                                      EventBuilderConfigurationException {
        this(eventBuilderConfiguration, axisConfiguration, true);
    }

    public TextInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                 AxisConfiguration axisConfiguration, boolean loadConfiguration)
            throws
            EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.axisConfiguration = axisConfiguration;
        if (loadConfiguration) {
            loadEventBuilderConfiguration();
        }
    }

    public void createMapping(EventBuilderConfiguration eventBuilderConfiguration,
                              StreamDefinition exportedStreamDefinition,
                              List<RegexData> attributeRegexList) {
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof TextInputMapping) {
            TextInputMapping textInputMapping = (TextInputMapping) eventBuilderConfiguration.getInputMapping();
            RegexData regexData = null;
            for (InputMappingAttribute inputMappingAttribute : textInputMapping.getInputMappingAttributes()) {
                String regex = inputMappingAttribute.getFromElementKey();
                if (regexData == null || !regex.equals(regexData.getRegex())) {
                    try {
                        regexData = new RegexData(regex);
                        attributeRegexList.add(regexData);
                    } catch (PatternSyntaxException e) {
                        String errMsg = "Error parsing regular expression: " + regex;
                        log.error(errMsg, e);
                        throw new EventBuilderConfigurationException(errMsg, e);
                    }
                }
                String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                String defaultValue = inputMappingAttribute.getDefaultValue();
                regexData.addMapping(type, defaultValue);
                exportedStreamDefinition.addPayloadData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            }
            if (!isStreamDefinitionValidForConfiguration(eventBuilderConfiguration, exportedStreamDefinition)) {
                throw new EventBuilderValidationException("Exported stream definition does not match the specified configuration.");
            }
        }
    }

    public boolean isStreamDefinitionValidForConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            StreamDefinition exportedStreamDefinition) {
        return true;
    }

    @Override
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return this.eventBuilderConfiguration;
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
            String inputTransportAdaptorType = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorType();
            if (EventBuilderConstants.ITA_EMAIL.equals(inputTransportAdaptorType)) {
                this.binaryInputEnabled = true;
            }
            try {
                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService().getInputTransportAdaptorConfiguration(inputTransportAdaptorName, tenantId);
                this.subscriptionId = EventBuilderServiceValueHolder.getInputTransportAdaptorService().subscribe(
                        inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), new TextInputTransportListener(), axisConfiguration);
            } catch (InputTransportAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input transport adaptor " + inputTransportAdaptorName + ":\n" + e.getMessage());
                throw new EventBuilderConfigurationException(e);
            }
        }
    }

    @Override
    public void loadEventBuilderConfiguration() {
        if (eventBuilderConfiguration != null) {
            String outputStreamName = eventBuilderConfiguration.getToStreamName();
            String outputStreamVersion = eventBuilderConfiguration.getToStreamVersion();

            try {
                this.exportedStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
            } catch (MalformedStreamDefinitionException e) {
                throw new EventBuilderConfigurationException("Could not create stream definition with " + outputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + outputStreamVersion);
            }
            subscribeToTransportAdaptor();
            createMapping(this.eventBuilderConfiguration, this.exportedStreamDefinition, this.attributeRegexList);
            this.eventBuilderConfiguration.setDeploymentStatus(DeploymentStatus.DEPLOYED);
        }
    }

    private void processEvent(Object obj) {
        if (obj instanceof String) {
            sendEvent((String) obj);
        } else if (binaryInputEnabled) {
            if (obj instanceof byte[]) {
                try {
                    byte[] inputBytes = (byte[]) obj;
                    String inputString = new String(inputBytes, EventBuilderConstants.CHARSET_UTF8);
                    sendEvent(inputString);
                } catch (UnsupportedEncodingException e) {
                    throw new EventBuilderConfigurationException("Cannot convert input bytes. Unsupported encoding type.", e);
                }
            }
        }
    }

    private void sendEvent(String inputString) {
        List<Object> attributeList = new ArrayList<Object>();
        for (RegexData regexData : attributeRegexList) {
            String formattedInputString = inputString.replaceAll("\\r?\\n", " ");
            regexData.matchInput(formattedInputString);
            while (regexData.hasNext()) {
                Object returnedAttribute = null;
                String value = regexData.next();
                if (value != null) {
                    String type = regexData.getType();
                    try {
                        Class<?> beanClass = Class.forName(type);
                        if (!beanClass.equals(String.class)) {
                            Class<?> stringClass = String.class;
                            Method valueOfMethod = beanClass.getMethod("valueOf", stringClass);
                            returnedAttribute = valueOfMethod.invoke(null, value);
                        } else {
                            returnedAttribute = value;
                        }
                    } catch (ClassNotFoundException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    } catch (InvocationTargetException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    } catch (NoSuchMethodException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    } catch (IllegalAccessException e) {
                        throw new EventBuilderConfigurationException("Cannot convert " + value + " to type " + type, e);
                    }
                }
                attributeList.add(returnedAttribute);
            }
        }
        Object[] objArray = attributeList.toArray(new Object[attributeList.size()]);
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

    private class TextInputTransportListener extends InputTransportAdaptorListener {
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
