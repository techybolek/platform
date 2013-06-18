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

package org.wso2.carbon.transport.adaptor.wso2event.receiver;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.transport.adaptor.core.AbstractTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.wso2event.receiver.internal.ds.WSO2EventReceiverTransportAdaptorServiceValueHolder;
import org.wso2.carbon.transport.adaptor.wso2event.receiver.internal.util.WSO2EventReceiverTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WSO2EventReceiverTransportAdaptorType extends AbstractTransportAdaptor
        implements InputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSO2EventReceiverTransportAdaptorType.class);
    private static WSO2EventReceiverTransportAdaptorType wso2EventReceiverTransportAdaptor = new WSO2EventReceiverTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>> inputTransportListenerMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>>();
    private Map<InputTransportMessageConfiguration, StreamDefinition> inputStreamDefinitionMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, StreamDefinition>();
    private Map<String, Map<String, TransportAdaptorListener>> streamIdTransportListenerMap =
            new ConcurrentHashMap<String, Map<String, TransportAdaptorListener>>();

    private WSO2EventReceiverTransportAdaptorType() {

        WSO2EventReceiverTransportAdaptorServiceValueHolder.getDataBridgeSubscriberService().subscribe(new AgentTransportCallback());

    }

    @Override
    protected TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType() {
        return TransportAdaptorDto.TransportAdaptorType.IN;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.WSO2EVENT);

        return supportInputMessageTypes;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        return null;
    }

    /**
     * @return WSO2EventReceiver transport adaptor instance
     */
    public static WSO2EventReceiverTransportAdaptorType getInstance() {

        return wso2EventReceiverTransportAdaptor;
    }

    /**
     * @return name of the WSO2EventReceiver transport adaptor
     */
    @Override
    protected String getName() {
        return WSO2EventReceiverTransportAdaptorConstants.TRANSPORT_TYPE_WSO2EVENT_RECEIVER;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.wso2event.receiver.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {
        return null;
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
        Property streamDefinitionProperty = new Property(WSO2EventReceiverTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventReceiverTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventReceiverTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventReceiverTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;

    }

    public String subscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            TransportAdaptorListener transportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {
        String subscriptionId = UUID.randomUUID().toString();

        if (!inputTransportListenerMap.keySet().contains(inputTransportMessageConfiguration)) {
            Map<String, TransportAdaptorListener> map = new HashMap<String, TransportAdaptorListener>();
            map.put(subscriptionId, transportAdaptorListener);
            inputTransportListenerMap.put(inputTransportMessageConfiguration, map);
        } else {
            inputTransportListenerMap.get(inputTransportMessageConfiguration).put(subscriptionId, transportAdaptorListener);
            StreamDefinition streamDefinition = inputStreamDefinitionMap.get(inputTransportMessageConfiguration);
            if (streamDefinition != null) {
                transportAdaptorListener.addEventDefinition(streamDefinition);
            }

        }

        return subscriptionId;
    }

    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        Map<String, TransportAdaptorListener> map = inputTransportListenerMap.get(inputTransportMessageConfiguration);
        if (map != null) {
            map.remove(subscriptionId);
        }

    }


    private class AgentTransportCallback implements AgentCallback {


        @Override
        public void removeStream(StreamDefinition streamDefinition, Credentials credentials) {
            inputStreamDefinitionMap.remove(createTopic(streamDefinition));
            Map<String, TransportAdaptorListener> transportListeners = inputTransportListenerMap.get(createTopic(streamDefinition));
            if (transportListeners != null) {
                for (TransportAdaptorListener transportAdaptorListener : transportListeners.values()) {
                    try {
                        transportAdaptorListener.removeEventDefinition(streamDefinition);
                    } catch (TransportAdaptorEventProcessingException e) {
                        log.error("Cannot remove Stream Definition from a transportAdaptorListener subscribed to " +
                                  streamDefinition.getStreamId(), e);
                    }

                }
            }
            streamIdTransportListenerMap.remove(streamDefinition.getStreamId());
        }

        @Override
        public void definedStream(StreamDefinition streamDefinition, Credentials credentials) {
            InputTransportMessageConfiguration inputTransportMessageConfiguration = createTopic(streamDefinition);

            inputStreamDefinitionMap.put(inputTransportMessageConfiguration, streamDefinition);
            Map<String, TransportAdaptorListener> transportListeners = inputTransportListenerMap.get(inputTransportMessageConfiguration);
            if (transportListeners == null) {
                transportListeners = new HashMap<String, TransportAdaptorListener>();

                inputTransportListenerMap.put(inputTransportMessageConfiguration, transportListeners);

            }

            for (TransportAdaptorListener transportAdaptorListener : transportListeners.values()) {
                try {
                    transportAdaptorListener.addEventDefinition(streamDefinition);
                } catch (TransportAdaptorEventProcessingException e) {
                    log.error("Cannot send Stream Definition to a transportAdaptorListener subscribed to " +
                              streamDefinition.getStreamId(), e);
                }

            }
            streamIdTransportListenerMap.put(streamDefinition.getStreamId(), inputTransportListenerMap.get(inputTransportMessageConfiguration));
        }

        private InputTransportMessageConfiguration createTopic(StreamDefinition streamDefinition) {

            InputTransportMessageConfiguration inputTransportMessageConfiguration = new InputTransportMessageConfiguration();
            Map<String, String> inputMessageProperties = new HashMap<String, String>();
            inputMessageProperties.put(WSO2EventReceiverTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION, streamDefinition.getName());
            inputMessageProperties.put(WSO2EventReceiverTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION, streamDefinition.getVersion());
            inputTransportMessageConfiguration.setInputMessageProperties(inputMessageProperties);

            return inputTransportMessageConfiguration;
        }

        @Override
        public void receive(List<Event> events, Credentials credentials) {
            for (Event event : events) {
                Map<String, TransportAdaptorListener> transportListeners = streamIdTransportListenerMap.get(event.getStreamId());
                if (transportListeners == null) {
                    try {
                        definedStream(WSO2EventReceiverTransportAdaptorServiceValueHolder.getDataBridgeSubscriberService().getStreamDefinition(credentials, event.getStreamId()), credentials);
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
                for (TransportAdaptorListener transportAdaptorListener : transportListeners.values()) {
                    try {
                        transportAdaptorListener.onEvent(event);
                    } catch (TransportAdaptorEventProcessingException e) {
                        log.error("Cannot send event to a transportAdaptorListener subscribed to " +
                                  event.getStreamId(), e);
                    }

                }

            }
        }

    }

}
