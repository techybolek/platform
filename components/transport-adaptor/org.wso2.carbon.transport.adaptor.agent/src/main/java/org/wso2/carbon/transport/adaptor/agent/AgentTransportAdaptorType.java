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

package org.wso2.carbon.transport.adaptor.agent;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.transport.adaptor.agent.ds.AgentTransportAdaptorServiceValueHolder;
import org.wso2.carbon.transport.adaptor.agent.util.AgentTransportAdaptorConstants;
import org.wso2.carbon.transport.adaptor.core.AbstractTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.OutputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AgentTransportAdaptorType extends AbstractTransportAdaptor
        implements InputTransportAdaptor, OutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(AgentTransportAdaptorType.class);
    private static AgentTransportAdaptorType agentTransportAdaptorAdaptor = new AgentTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>> inputTransportListenerMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>>();
    private Map<InputTransportMessageConfiguration, StreamDefinition> inputStreamDefinitionMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, StreamDefinition>();
    private Map<String, Map<String, TransportAdaptorListener>> streamIdTransportListenerMap =
            new ConcurrentHashMap<String, Map<String, TransportAdaptorListener>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>> dataPublisherMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>>();
    private Agent agent;

    private AgentTransportAdaptorType() {

        AgentTransportAdaptorServiceValueHolder.getDataBridgeSubscriberService().subscribe(new AgentTransportCallback());

    }

    @Override
    protected TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType() {
        return TransportAdaptorDto.TransportAdaptorType.INOUT;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.TUPLE);

        return supportInputMessageTypes;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.TUPLE);

        return supportOutputMessageTypes;
    }

    /**
     * @return agent transport adaptor instance
     */
    public static AgentTransportAdaptorType getInstance() {

        return agentTransportAdaptorAdaptor;
    }

    /**
     * @return name of the agent transport adaptor
     */
    @Override
    protected String getName() {
        return AgentTransportAdaptorConstants.TRANSPORT_TYPE_AGENT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        //To change body of implemented methods use File | Settings | File Templates.
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.agent.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {


        List<Property> propertyList = new ArrayList<Property>();

//        // set receiver url transport
//        Property ipProperty = new Property(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL);
//        ipProperty.setDisplayName(
//                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL));
//        ipProperty.setRequired(true);
//        ipProperty.setHint(resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_HINT_RECEIVER_URL));
//
//        // set authenticator url of transport
//        Property authenticatorIpProperty = new Property(AgentTransportAdaptorConstants.
//                                                                TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL);
//        authenticatorIpProperty.setDisplayName(
//                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL));
//        authenticatorIpProperty.setRequired(false);
//        authenticatorIpProperty.setHint(resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_HINT_AUTHENTICATOR_URL));


        // set connection user name as property
        Property userNameProperty = new Property(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME));
        userNameProperty.setHint(resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_HINT_USER_NAME));

        // set connection password as property
        Property passwordProperty = new Property(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD));
        passwordProperty.setHint(resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_HINT_PASSWORD));
//        propertyList.add(ipProperty);
//        propertyList.add(authenticatorIpProperty);
        propertyList.add(userNameProperty);
        propertyList.add(passwordProperty);

        return propertyList;
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();
        // set receiver url transport
        Property ipProperty = new Property(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL));
        ipProperty.setRequired(true);
        ipProperty.setHint(resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_HINT_RECEIVER_URL));


        propertyList.add(ipProperty);
        return propertyList;

        //return null;
    }

    /**
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(AgentTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(AgentTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;

    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();
        // set authenticator url of transport
        Property authenticatorIpProperty = new Property(AgentTransportAdaptorConstants.
                                                                TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL);
        authenticatorIpProperty.setDisplayName(
                resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL));
        authenticatorIpProperty.setRequired(false);
        authenticatorIpProperty.setHint(resourceBundle.getString(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_HINT_AUTHENTICATOR_URL));

        propertyList.add(authenticatorIpProperty);

        return propertyList;
        //return null;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        return null;
    }

    public String subscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            TransportAdaptorListener transportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration)
            throws TransportAdaptorEventProcessingException {
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


    /**
     * @param outputTransportMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputTransportAdaptorConfiguration
     *                - transport configuration to be used
     * @throws org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException
     *
     */
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration)
            throws TransportAdaptorEventProcessingException {
        Integer tenantId = CarbonContext.getCurrentContext().getTenantId();
        ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher> dataPublishers = dataPublisherMap.get(tenantId);
        if (dataPublishers == null) {
            dataPublishers = new ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>();
            dataPublisherMap.putIfAbsent(tenantId, dataPublishers);
            dataPublishers = dataPublisherMap.get(tenantId);
        }
        AsyncDataPublisher dataPublisher = dataPublishers.get(outputTransportAdaptorConfiguration);
        if (dataPublisher == null) {
            synchronized (this) {
                dataPublisher = dataPublishers.get(outputTransportAdaptorConfiguration);
                if (dataPublisher == null) {
                    dataPublisher = createDataPublisher(outputTransportAdaptorConfiguration);
                    dataPublishers.putIfAbsent(outputTransportAdaptorConfiguration, dataPublisher);
                }
            }
        }

        try {
            Event event = (Event) ((Object[]) message)[0];
            StreamDefinition streamDefinition = (StreamDefinition) ((Object[]) message)[1];

            if (!dataPublisher.isStreamDefinitionAdded(streamDefinition)) {
                dataPublisher.addStreamDefinition(streamDefinition);

                //Sending the first Event
                publishEvent(outputTransportAdaptorConfiguration, dataPublisher, event, streamDefinition);
            } else {
                //Sending Events
                publishEvent(outputTransportAdaptorConfiguration, dataPublisher, event, streamDefinition);
            }
        } catch (Exception ex) {
            throw new TransportAdaptorEventProcessingException(
                    ex.getMessage() + " Error Occurred When Publishing Events", ex);
        }

    }

    private AsyncDataPublisher createDataPublisher(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        if (agent == null) {
            agent = AgentTransportAdaptorServiceValueHolder.getAgent();
        }
        AsyncDataPublisher dataPublisher;
        Map<String, String> adaptorCommonProperties = outputTransportAdaptorConfiguration.getTransportAdaptorCommonProperties();

        if (null != adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL) && adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL).length() > 0) {
            dataPublisher = new AsyncDataPublisher(adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL),
                                                   adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL),
                                                   adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME),
                                                   adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD),
                                                   agent);
        } else {
            dataPublisher = new AsyncDataPublisher(adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL),
                                                   adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME),
                                                   adaptorCommonProperties.get(AgentTransportAdaptorConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD),
                                                   agent);
        }
        return dataPublisher;
    }


    private void publishEvent(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
            AsyncDataPublisher dataPublisher,
            Event event, StreamDefinition streamDefinition)
            throws TransportAdaptorEventProcessingException {
        try {
            dataPublisher.publish(streamDefinition.getName(), streamDefinition.getVersion(), event);
        } catch (AgentException ex) {
            throw new TransportAdaptorEventProcessingException(
                    "Cannot publish data via DataPublisher for the transport configuration:" +
                    outputTransportAdaptorConfiguration.getName() + " for the  event " + event, ex);
        }

    }

    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        // no test
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
            inputMessageProperties.put(AgentTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION, streamDefinition.getName());
            inputMessageProperties.put(AgentTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION, streamDefinition.getVersion());
            inputTransportMessageConfiguration.setInputMessageProperties(inputMessageProperties);

            return inputTransportMessageConfiguration;
        }

        @Override
        public void receive(List<Event> events, Credentials credentials) {
            for (Event event : events) {
                Map<String, TransportAdaptorListener> transportListeners = streamIdTransportListenerMap.get(event.getStreamId());
                if (transportListeners == null) {
                    try {
                        definedStream(AgentTransportAdaptorServiceValueHolder.getDataBridgeSubscriberService().getStreamDefinition(credentials, event.getStreamId()), credentials);
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
