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

package org.wso2.carbon.input.transport.adaptor.wso2event;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.input.transport.adaptor.core.AbstractInputTransportAdaptor;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorDto;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.input.transport.adaptor.wso2event.internal.ds.WSO2EventTransportAdaptorServiceValueHolder;
import org.wso2.carbon.input.transport.adaptor.wso2event.internal.util.WSO2EventTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WSO2EventTransportAdaptorType extends AbstractInputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSO2EventTransportAdaptorType.class);
    private static WSO2EventTransportAdaptorType wso2EventTransportAdaptor = new WSO2EventTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<InputTransportAdaptorMessageConfiguration, Map<String, InputTransportAdaptorListener>> inputTransportListenerMap =
            new ConcurrentHashMap<InputTransportAdaptorMessageConfiguration, Map<String, InputTransportAdaptorListener>>();
    private Map<InputTransportAdaptorMessageConfiguration, StreamDefinition> inputStreamDefinitionMap =
            new ConcurrentHashMap<InputTransportAdaptorMessageConfiguration, StreamDefinition>();
    private Map<String, Map<String, InputTransportAdaptorListener>> streamIdTransportListenerMap =
            new ConcurrentHashMap<String, Map<String, InputTransportAdaptorListener>>();

    private WSO2EventTransportAdaptorType() {

        WSO2EventTransportAdaptorServiceValueHolder.getDataBridgeSubscriberService().subscribe(new AgentTransportCallback());

    }


    @Override
    protected List<InputTransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<InputTransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<InputTransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(InputTransportAdaptorDto.MessageType.WSO2EVENT);

        return supportInputMessageTypes;
    }

    /**
     * @return WSO2EventReceiver transport adaptor instance
     */
    public static WSO2EventTransportAdaptorType getInstance() {

        return wso2EventTransportAdaptor;
    }

    /**
     * @return name of the WSO2EventReceiver transport adaptor
     */
    @Override
    protected String getName() {
        return WSO2EventTransportAdaptorConstants.TRANSPORT_TYPE_WSO2EVENT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.input.transport.adaptor.wso2event.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {
        return null;
    }

    /**
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_NAME);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_NAME));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;

    }

    public String subscribe(InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration,
                            InputTransportAdaptorListener inputTransportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {
        String subscriptionId = UUID.randomUUID().toString();

        inputTransportAdaptorListener.setStatisticsEnabled(inputTransportAdaptorConfiguration.isEnableStatistics());
        inputTransportAdaptorListener.setTraceEnabled(inputTransportAdaptorConfiguration.isEnableTracing());
        inputTransportAdaptorListener.setTransportAdaptorName(inputTransportAdaptorConfiguration.getName());

        if (!inputTransportListenerMap.keySet().contains(inputTransportAdaptorMessageConfiguration)) {
            Map<String, InputTransportAdaptorListener> map = new HashMap<String, InputTransportAdaptorListener>();
            map.put(subscriptionId, inputTransportAdaptorListener);
            inputTransportListenerMap.put(inputTransportAdaptorMessageConfiguration, map);
        } else {
            inputTransportListenerMap.get(inputTransportAdaptorMessageConfiguration).put(subscriptionId, inputTransportAdaptorListener);
            StreamDefinition streamDefinition = inputStreamDefinitionMap.get(inputTransportAdaptorMessageConfiguration);
            if (streamDefinition != null) {
                inputTransportAdaptorListener.addEventDefinitionCall(streamDefinition);
            }

        }

        return subscriptionId;
    }

    public void unsubscribe(InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        Map<String, InputTransportAdaptorListener> map = inputTransportListenerMap.get(inputTransportAdaptorMessageConfiguration);
        if (map != null) {
            map.remove(subscriptionId);
        }

    }


    private class AgentTransportCallback implements AgentCallback {


        @Override
        public void removeStream(StreamDefinition streamDefinition, Credentials credentials) {
            inputStreamDefinitionMap.remove(createTopic(streamDefinition));
            Map<String, InputTransportAdaptorListener> inputTransportAdaptorListenerMap = inputTransportListenerMap.get(createTopic(streamDefinition));
            if (inputTransportAdaptorListenerMap != null) {
                for (InputTransportAdaptorListener inputTransportAdaptorListener : inputTransportAdaptorListenerMap.values()) {
                    try {
                        inputTransportAdaptorListener.removeEventDefinitionCall(streamDefinition);
                    } catch (InputTransportAdaptorEventProcessingException e) {
                        log.error("Cannot remove Stream Definition from a transportAdaptorListener subscribed to " +
                                  streamDefinition.getStreamId(), e);
                    }

                }
            }
            streamIdTransportListenerMap.remove(streamDefinition.getStreamId());
        }

        @Override
        public void definedStream(StreamDefinition streamDefinition, Credentials credentials) {
            InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration = createTopic(streamDefinition);

            inputStreamDefinitionMap.put(inputTransportAdaptorMessageConfiguration, streamDefinition);
            Map<String, InputTransportAdaptorListener> transportListeners = inputTransportListenerMap.get(inputTransportAdaptorMessageConfiguration);
            if (transportListeners == null) {
                transportListeners = new HashMap<String, InputTransportAdaptorListener>();

                inputTransportListenerMap.put(inputTransportAdaptorMessageConfiguration, transportListeners);

            }

            for (InputTransportAdaptorListener transportAdaptorListener : transportListeners.values()) {
                try {
                    transportAdaptorListener.addEventDefinitionCall(streamDefinition);
                } catch (InputTransportAdaptorEventProcessingException e) {
                    log.error("Cannot send Stream Definition to a transportAdaptorListener subscribed to " +
                              streamDefinition.getStreamId(), e);
                }

            }
            streamIdTransportListenerMap.put(streamDefinition.getStreamId(), inputTransportListenerMap.get(inputTransportAdaptorMessageConfiguration));
        }

        private InputTransportAdaptorMessageConfiguration createTopic(StreamDefinition streamDefinition) {

            InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration = new InputTransportAdaptorMessageConfiguration();
            Map<String, String> inputMessageProperties = new HashMap<String, String>();
            inputMessageProperties.put(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_NAME, streamDefinition.getName());
            inputMessageProperties.put(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION, streamDefinition.getVersion());
            inputTransportAdaptorMessageConfiguration.setInputMessageProperties(inputMessageProperties);

            return inputTransportAdaptorMessageConfiguration;
        }

        @Override
        public void receive(List<Event> events, Credentials credentials) {
            for (Event event : events) {
                Map<String, InputTransportAdaptorListener> transportListeners = streamIdTransportListenerMap.get(event.getStreamId());
                if (transportListeners == null ) {
                    try {
                        definedStream(WSO2EventTransportAdaptorServiceValueHolder.getDataBridgeSubscriberService().getStreamDefinition(credentials, event.getStreamId()), credentials);
                    } catch (StreamDefinitionNotFoundException e) {
                        log.error("No Stream definition store found for the event " +
                                  event.getStreamId(), e);
                        return;
                    } catch (StreamDefinitionStoreException e) {
                        log.error("No Stream definition store found when checking stream definition for " +
                                  event.getStreamId(), e);
                        return;
                    }
                    transportListeners = streamIdTransportListenerMap.get(event.getStreamId());
                    if (transportListeners == null) {
                        log.error("No transport listeners for  " + event.getStreamId());
                        return;
                    }
                }
                for (InputTransportAdaptorListener transportAdaptorListener : transportListeners.values()) {
                    try {
                        transportAdaptorListener.onEventCall(event);
                    } catch (InputTransportAdaptorEventProcessingException e) {
                        log.error("Cannot send event to a transportAdaptorListener subscribed to " +
                                  event.getStreamId(), e);
                    }

                }

            }
        }

    }

}
