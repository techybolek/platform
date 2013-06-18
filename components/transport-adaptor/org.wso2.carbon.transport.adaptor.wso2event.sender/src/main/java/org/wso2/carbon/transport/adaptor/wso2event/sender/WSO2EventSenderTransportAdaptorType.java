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

package org.wso2.carbon.transport.adaptor.wso2event.sender;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.transport.adaptor.core.AbstractTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.OutputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.wso2event.sender.internal.ds.WSO2EventSenderTransportAdaptorServiceValueHolder;
import org.wso2.carbon.transport.adaptor.wso2event.sender.internal.util.WSO2EventSenderTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class WSO2EventSenderTransportAdaptorType extends AbstractTransportAdaptor
        implements OutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSO2EventSenderTransportAdaptorType.class);
    private static WSO2EventSenderTransportAdaptorType wso2EventSenderTransportAdaptor = new WSO2EventSenderTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>> inputTransportListenerMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>> dataPublisherMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>>();
    private Agent agent;

    private WSO2EventSenderTransportAdaptorType() {

    }

    @Override
    protected TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType() {
        return TransportAdaptorDto.TransportAdaptorType.OUT;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.WSO2EVENT);

        return supportInputMessageTypes;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.WSO2EVENT);

        return supportOutputMessageTypes;
    }

    /**
     * @return WSO2EventSender transport adaptor instance
     */
    public static WSO2EventSenderTransportAdaptorType getInstance() {

        return wso2EventSenderTransportAdaptor;
    }

    /**
     * @return name of the WSO2EventSender transport adaptor
     */
    @Override
    protected String getName() {
        return WSO2EventSenderTransportAdaptorConstants.TRANSPORT_TYPE_WSO2EVENT_SENDER;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        //To change body of implemented methods use File | Settings | File Templates.
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.wso2event.sender.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {

        return null;
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set receiver url transport
        Property ipProperty = new Property(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_RECEIVER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_RECEIVER_URL));
        ipProperty.setRequired(true);
        ipProperty.setHint(resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_HINT_RECEIVER_URL));

        // set authenticator url of transport
        Property authenticatorIpProperty = new Property(WSO2EventSenderTransportAdaptorConstants.
                                                                TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_AUTHENTICATOR_URL);
        authenticatorIpProperty.setDisplayName(
                resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_AUTHENTICATOR_URL));
        authenticatorIpProperty.setRequired(false);
        authenticatorIpProperty.setHint(resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_HINT_AUTHENTICATOR_URL));


        // set connection user name as property
        Property userNameProperty = new Property(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_USER_NAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_USER_NAME));
        userNameProperty.setHint(resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_HINT_USER_NAME));

        // set connection password as property
        Property passwordProperty = new Property(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_PASSWORD));
        passwordProperty.setHint(resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_HINT_PASSWORD));
        propertyList.add(ipProperty);
        propertyList.add(authenticatorIpProperty);
        propertyList.add(userNameProperty);
        propertyList.add(passwordProperty);

        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_NAME);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_NAME));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;
    }

    /**
     * @param outputTransportMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputTransportAdaptorConfiguration
     *                - transport configuration to be used
     */
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
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
            agent = WSO2EventSenderTransportAdaptorServiceValueHolder.getAgent();
        }
        AsyncDataPublisher dataPublisher;
        Map<String, String> adaptorCommonProperties = outputTransportAdaptorConfiguration.getCommonProperties();

        if (null != adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_AUTHENTICATOR_URL) && adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_AUTHENTICATOR_URL).length() > 0) {
            dataPublisher = new AsyncDataPublisher(adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_AUTHENTICATOR_URL),
                                                   adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_RECEIVER_URL),
                                                   adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_USER_NAME),
                                                   adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_PASSWORD),
                                                   agent);
        } else {
            dataPublisher = new AsyncDataPublisher(adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_RECEIVER_URL),
                                                   adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_USER_NAME),
                                                   adaptorCommonProperties.get(WSO2EventSenderTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_SENDER_PROP_PASSWORD),
                                                   agent);
        }
        return dataPublisher;
    }


    private void publishEvent(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
            AsyncDataPublisher dataPublisher,
            Event event, StreamDefinition streamDefinition) {
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

}
