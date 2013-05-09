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
import org.wso2.carbon.transport.adaptor.core.*;
import org.wso2.carbon.transport.adaptor.core.config.*;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;
import org.wso2.carbon.transport.adaptor.agent.util.AgentTransportConstants;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AgentTransportType extends AbstractTransportAdaptor implements InputTransportAdaptor, OutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(AgentTransportType.class);

    private static AgentTransportType agentTransportAdaptor = new AgentTransportType();

    private ResourceBundle resourceBundle;
    private Map<InputTransportMessageConfiguration, Map<String, TransportListener>> inputTransportListenerMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, Map<String, TransportListener>>();
    private Map<InputTransportMessageConfiguration, StreamDefinition> inputStreamDefinitionMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, StreamDefinition>();
    private Map<String, Map<String, TransportListener>> streamIdTransportListenerMap =
            new ConcurrentHashMap<String, Map<String, TransportListener>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>> dataPublisherMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>>();
    private Agent agent;

    private AgentTransportType() {

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
     *
     * @return agent transport adaptor instance
     */
    public static AgentTransportType getInstance() {

        return agentTransportAdaptor;
    }

    /**
     *
     * @return name of the agent transport adaptor
     */
    @Override
    protected String getName() {
        return AgentTransportConstants.TRANSPORT_TYPE_AGENT;
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
     *
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {


        List<Property> propertyList = new ArrayList<Property>();
//
        // set receiver url transport
        Property ipProperty = new Property(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL));
        ipProperty.setRequired(true);
        ipProperty.setHint("Please Enter the Receiver Url");

        // set authenticator url of transport
        Property authenticatorIpProperty = new Property(AgentTransportConstants.
                TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL);
        authenticatorIpProperty.setDisplayName(
                resourceBundle.getString(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL));
        authenticatorIpProperty.setRequired(false);
        authenticatorIpProperty.setHint("Please Enter the Authenticator Url");


        // set connection user name as property
        Property userNameProperty = new Property(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME));
        userNameProperty.setHint("Please Enter the UserName");

        // set connection password as property
        Property passwordProperty = new Property(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD));
        passwordProperty.setHint("Please Enter the Password");
        propertyList.add(ipProperty);
        propertyList.add(authenticatorIpProperty);
        propertyList.add(userNameProperty);
        propertyList.add(passwordProperty);

        return propertyList;
    }

    /**
     *
     * @return  input adaptor configuration property list
     */
    @Override
    public List<Property> getInAdaptorConfig() {

//        List<Property> propertyList = new ArrayList<Property>();
//
//        // set receiver url transport
//        Property ipProperty = new Property(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL);
//        ipProperty.setDisplayName(
//                resourceBundle.getString(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL));
//        ipProperty.setRequired(true);
//        ipProperty.setDefaultValue("localhost");
//        ipProperty.setHint("Please Enter the Receiver url");
//
//        propertyList.add(ipProperty);
//        return propertyList;

        return null;
    }

    /**
     *
     * @return  input message configuration property list
     */
    @Override
    public List<Property> getInMessageConfig() {

        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(AgentTransportConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(AgentTransportConstants.TRANSPORT_MESSAGE_STREAM_DEFINITION));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(AgentTransportConstants.TRANSPORT_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(AgentTransportConstants.TRANSPORT_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;


       // return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     *
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutAdaptorConfig() {
//        List<Property> propertyList = new ArrayList<Property>();
//
//        // set authenticator url of transport
//        Property authenticatorIpProperty = new Property(AgentTransportConstants.
//                TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL);
//        authenticatorIpProperty.setDisplayName(
//                resourceBundle.getString(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL));
//        authenticatorIpProperty.setRequired(false);
//        authenticatorIpProperty.setHint("Please Enter the Authentication url");
//
//        propertyList.add(authenticatorIpProperty);
//
//        return propertyList;

        return null;

    }

    /**
     *
     * @return  output message configuration property list
     */
    @Override
    public List<Property> getOutMessageConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String subscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration, TransportListener transportListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration)
            throws TransportEventProcessingException {
        String subscriptionId = UUID.randomUUID().toString();

//        for(InputTransportMessageConfiguration inputTransportMessageConfigurationKey : inputTransportListenerMap.keySet())
//        {

        if (! inputTransportListenerMap.keySet().contains(inputTransportMessageConfiguration)) {
            Map<String, TransportListener> map = new HashMap<String, TransportListener>();
            map.put(subscriptionId, transportListener);
            inputTransportListenerMap.put(inputTransportMessageConfiguration, map);
        } else {
            inputTransportListenerMap.get(inputTransportMessageConfiguration).put(subscriptionId, transportListener);
            StreamDefinition streamDefinition = inputStreamDefinitionMap.get(inputTransportMessageConfiguration);
            if (streamDefinition != null) {
                transportListener.addEventDefinition(streamDefinition);
            }

        }
//        }
        return subscriptionId;
    }


    /**
     * @param outputTransportMessageConfiguration - topic name to publish messages
     * @param message   - is and Object[]{Event, EventDefinition}
     * @param outputTransportAdaptorConfiguration
     *                  - transport configuration to be used
     * @throws TransportEventProcessingException
     *
     */
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration, Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration)
            throws TransportEventProcessingException {
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

            //String streamId = outputStreamIdMap.get(topicName + tenantId);
            if (!dataPublisher.isStreamDefinitionAdded(streamDefinition)) {
                dataPublisher.addStreamDefinition(streamDefinition);

                //Sending the first Event
                publishEvent(outputTransportAdaptorConfiguration, dataPublisher, event, streamDefinition);
            } else {
                //Sending Events
                publishEvent(outputTransportAdaptorConfiguration, dataPublisher, event, streamDefinition);
            }
        } catch (Exception ex) {
            throw new TransportEventProcessingException(
                    ex.getMessage() + " Error Occurred When Publishing Events", ex);
        }

    }

    private AsyncDataPublisher createDataPublisher(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        if (agent == null) {
            agent = AgentTransportAdaptorServiceValueHolder.getAgent();
        }
        AsyncDataPublisher dataPublisher;
        Map<String, String> adaptorCommonProperties = outputTransportAdaptorConfiguration.getCommonAdaptorProperties();
//
//        Map<String, String> adaptorCommonProperties = outputTransportAdaptorConfiguration.getCommonAdaptorProperties();
//
//        Map<String, String> adaptorCommonProperties = outputTransportAdaptorConfiguration.getCommonAdaptorProperties();

        if (null != adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL) && adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL).length() > 0) {
            dataPublisher = new AsyncDataPublisher(adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_AUTHENTICATOR_URL),
                    adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL),
                    adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME),
                    adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD),
                    agent);
        } else {
            dataPublisher = new AsyncDataPublisher(adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_RECEIVER_URL),
                    adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_USER_NAME),
                    adaptorCommonProperties.get(AgentTransportConstants.TRANSPORT_CONF_AGENT_PROP_PASSWORD),
                    agent);
        }
        return dataPublisher;
    }

    private void throwTransportEventProcessingException(TransportAdaptorConfiguration transportAdaptorConfiguration,
                                                        Exception e)
            throws TransportEventProcessingException {
        throw new TransportEventProcessingException(
                "Cannot create DataPublisher for the transport configuration:" + transportAdaptorConfiguration.getName(), e);
    }

    private void publishEvent(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
                              AsyncDataPublisher dataPublisher,
                              Event event, StreamDefinition streamDefinition)
            throws TransportEventProcessingException {
        try {
            dataPublisher.publish(streamDefinition.getName(), streamDefinition.getVersion(), event);
        } catch (AgentException ex) {
            throw new TransportEventProcessingException(
                    "Cannot publish data via DataPublisher for the transport configuration:" +
                            outputTransportAdaptorConfiguration.getName() + " for the  event " + event, ex);
        }

    }

    @Override
    public void testConnection(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration)
            throws TransportEventProcessingException {
        // no test
    }

    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId)
            throws TransportEventProcessingException {
        Map<String, TransportListener> map = inputTransportListenerMap.get(inputTransportMessageConfiguration);
        if (map != null) {
            map.remove(subscriptionId);
        }

    }


    private class AgentTransportCallback implements AgentCallback {


        @Override
        public void removeStream(StreamDefinition streamDefinition, Credentials credentials) {
            inputStreamDefinitionMap.remove(createTopic(streamDefinition));
            Map<String, TransportListener> transportListeners = inputTransportListenerMap.get(createTopic(streamDefinition));
            if (transportListeners != null) {
                for (TransportListener transportListener : transportListeners.values()) {
                    try {
                        transportListener.removeEventDefinition(streamDefinition);
                    } catch (TransportEventProcessingException e) {
                        log.error("Cannot remove Stream Definition from a transportListener subscribed to " +
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
            Map<String, TransportListener> transportListeners = inputTransportListenerMap.get(inputTransportMessageConfiguration);
            if (transportListeners == null) {
                transportListeners = new HashMap<String, TransportListener>();

                inputTransportListenerMap.put(inputTransportMessageConfiguration, transportListeners);

            }

            for (TransportListener transportListener : transportListeners.values()) {
                try {
                    transportListener.addEventDefinition(streamDefinition);
                } catch (TransportEventProcessingException e) {
                    log.error("Cannot send Stream Definition to a transportListener subscribed to " +
                            streamDefinition.getStreamId(), e);
                }

            }
            streamIdTransportListenerMap.put(streamDefinition.getStreamId(), inputTransportListenerMap.get(inputTransportMessageConfiguration));
        }

        private InputTransportMessageConfiguration createTopic(StreamDefinition streamDefinition) {

            InputTransportMessageConfiguration inputTransportMessageConfiguration = new InputTransportMessageConfiguration();
            Map<String,String> inputMessageProperties = new HashMap<String, String>();
            inputMessageProperties.put("streamName",streamDefinition.getName());
            inputMessageProperties.put("version",streamDefinition.getVersion());
            inputTransportMessageConfiguration.setInputMessageProperties(inputMessageProperties);

            return inputTransportMessageConfiguration;
        }

        @Override
        public void receive(List<Event> events, Credentials credentials) {
            for (Event event : events) {
                Map<String, TransportListener> transportListeners = streamIdTransportListenerMap.get(event.getStreamId());
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
                for (TransportListener transportListener : transportListeners.values()) {
                    try {
                        transportListener.onEvent(event);
                    } catch (TransportEventProcessingException e) {
                        log.error("Cannot send event to a transportListener subscribed to " +
                                event.getStreamId(), e);
                    }

                }

            }
        }

    }


}
