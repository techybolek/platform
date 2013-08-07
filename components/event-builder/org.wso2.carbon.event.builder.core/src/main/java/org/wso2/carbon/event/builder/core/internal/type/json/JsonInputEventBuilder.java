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

import com.jayway.jsonpath.JsonPath;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
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
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonInputEventBuilder implements EventBuilder {

    private static final Log log = LogFactory.getLog(JsonInputEventBuilder.class);
    private Logger trace = Logger.getLogger(EventBuilderConstants.EVENT_TRACE_LOGGER);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<JsonPathData> attributeJsonPathDataList = new ArrayList<JsonPathData>();
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private AxisConfiguration axisConfiguration = null;
    private StreamDefinition exportedStreamDefinition = null;
    private String subscriptionId = null;

    public JsonInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                 AxisConfiguration axisConfiguration) throws
                                                                      EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.axisConfiguration = axisConfiguration;
        loadEventBuilderConfiguration();
    }

    private void createMapping() {
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof JsonInputMapping) {
            JsonInputMapping jsonInputMapping = (JsonInputMapping) eventBuilderConfiguration.getInputMapping();
            for (InputMappingAttribute inputMappingAttribute : jsonInputMapping.getInputMappingAttributes()) {
                String jsonPathExpr = inputMappingAttribute.getFromElementKey();
                JsonPath compiledJsonPath = JsonPath.compile(jsonPathExpr);
                String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                attributeJsonPathDataList.add(new JsonPathData(compiledJsonPath, type));
                exportedStreamDefinition.addPayloadData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            }
            validateStreamDefinitionWithInputProperties();
        }
    }

    private void validateStreamDefinitionWithInputProperties()
            throws EventBuilderConfigurationException {

    }

    @Override
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return this.eventBuilderConfiguration;
    }

    @Override
    public void loadEventBuilderConfiguration() {
        if (this.eventBuilderConfiguration != null) {
            String outputStreamName = this.eventBuilderConfiguration.getToStreamName();
            String outputStreamVersion = this.eventBuilderConfiguration.getToStreamVersion();
            try {
                this.exportedStreamDefinition = new StreamDefinition(outputStreamName, outputStreamVersion);
            } catch (MalformedStreamDefinitionException e) {
                throw new EventBuilderConfigurationException("Could not create stream definition with " + outputStreamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + outputStreamVersion);
            }

            subscribeToTransportAdaptor();
            createMapping();
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
                        inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputStreamConfiguration().getInputTransportAdaptorMessageConfiguration(), new JsonInputTransportListener(), axisConfiguration);
            } catch (InputTransportAdaptorManagerConfigurationException e) {
                log.error("Cannot subscribe to input transport adaptor " + inputTransportAdaptorName + ":\n" + e.getMessage());
                throw new EventBuilderConfigurationException(e);
            }
        }

    }

    private void processEvent(Object obj) {
        JsonInputMapping jsonInputMapping = (JsonInputMapping) this.eventBuilderConfiguration.getInputMapping();
        if (jsonInputMapping.isBatchProcessingEnabled()) {
            sendMultipleEvents(obj);
        } else {
            sendEvent(obj);
        }
    }

    private void sendMultipleEvents(Object obj) {
        if (obj instanceof String) {
            String events = (String) obj;
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Received batch of events as OMElement.\n" + events);
            }
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(events);
                for (int i = 0; i < jsonArray.length(); i++) {
                    sendEvent(jsonArray.getJSONObject(i).toString());
                }
            } catch (JSONException e) {
                throw new EventBuilderConfigurationException("Error in parsing JSON: ", e);
            }
        }
    }

    private void sendEvent(Object obj) {
        if (obj instanceof String) {
            String jsonString = (String) obj;
            if (eventBuilderConfiguration.isTraceEnabled()) {
                trace.info("[Event-Builder] Received event as String.\n" + jsonString);
            }

            List<Object> objList = new ArrayList<Object>();
            for (JsonPathData jsonPathData : attributeJsonPathDataList) {
                JsonPath jsonPath = jsonPathData.getJsonPath();
                String type = jsonPathData.getType();
                try {
                    Object resultObject = jsonPath.read(jsonString);
                    if (resultObject != null) {
                        Class typeClass = Class.forName(type);
                        Object returnedObj = typeClass.cast(resultObject);
                        objList.add(returnedObj);
                    } else {
                        log.warn("Unable to parse XPath to retrieve required attribute. Skipping to next attribute.");
                    }
                } catch (ClassNotFoundException e) {
                    throw new EventBuilderConfigurationException("Cannot find specified class for type " + type);
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

    private class JsonInputTransportListener extends InputTransportAdaptorListener {
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

    private class JsonPathData {
        private String type;
        private JsonPath jsonPath;

        public JsonPathData(JsonPath jsonPath, String type) {
            this.type = type;
            this.jsonPath = jsonPath;
        }

        private JsonPath getJsonPath() {
            return jsonPath;
        }

        private String getType() {
            return type;
        }
    }

}
