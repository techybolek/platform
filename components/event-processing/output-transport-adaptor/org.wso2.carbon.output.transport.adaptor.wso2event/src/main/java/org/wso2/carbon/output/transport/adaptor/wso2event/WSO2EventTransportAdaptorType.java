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

package org.wso2.carbon.output.transport.adaptor.wso2event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.output.transport.adaptor.core.AbstractOutputTransportAdaptor;
import org.wso2.carbon.output.transport.adaptor.core.MessageType;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.exception.OutputTransportAdaptorEventProcessingException;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.output.transport.adaptor.wso2event.internal.ds.WSO2EventTransportAdaptorServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.wso2event.internal.util.WSO2EventTransportAdaptorConstants;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class WSO2EventTransportAdaptorType extends AbstractOutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSO2EventTransportAdaptorType.class);
    private static WSO2EventTransportAdaptorType wso2EventTransportAdaptor = new WSO2EventTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>> dataPublisherMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>>();
    private Agent agent;

    private WSO2EventTransportAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.WSO2EVENT);

        return supportOutputMessageTypes;
    }

    /**
     * @return WSO2Event transport adaptor instance
     */
    public static WSO2EventTransportAdaptorType getInstance() {

        return wso2EventTransportAdaptor;
    }

    /**
     * @return name of the WSO2Event transport adaptor
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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.output.transport.adaptor.wso2event.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set receiver url transport
        Property ipProperty = new Property(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_RECEIVER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_RECEIVER_URL));
        ipProperty.setRequired(true);
        ipProperty.setHint(resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_HINT_RECEIVER_URL));

        // set authenticator url of transport
        Property authenticatorIpProperty = new Property(WSO2EventTransportAdaptorConstants.
                                                                TRANSPORT_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL);
        authenticatorIpProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL));
        authenticatorIpProperty.setRequired(false);
        authenticatorIpProperty.setHint(resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_HINT_AUTHENTICATOR_URL));


        // set connection user name as property
        Property userNameProperty = new Property(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_USER_NAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_USER_NAME));
        userNameProperty.setHint(resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_HINT_USER_NAME));

        // set connection password as property
        Property passwordProperty = new Property(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_PASSWORD));
        passwordProperty.setHint(resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_HINT_PASSWORD));

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
        Property streamDefinitionProperty = new Property(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_NAME);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_NAME));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventTransportAdaptorConstants.TRANSPORT_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setDefaultValue("1.0.0");
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
     * @param tenantId
     */
    public void publish(
            OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration,
            Object message,
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration, int tenantId) {
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
            throw new OutputTransportAdaptorEventProcessingException(
                    ex.getMessage() + " Error Occurred When Publishing Events", ex);
        }

    }

    private AsyncDataPublisher createDataPublisher(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        if (agent == null) {
            agent = WSO2EventTransportAdaptorServiceValueHolder.getAgent();
        }
        AsyncDataPublisher dataPublisher;
        Map<String, String> adaptorOutputProperties = outputTransportAdaptorConfiguration.getOutputProperties();

        if (null != adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL) && adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL).length() > 0) {
            dataPublisher = new AsyncDataPublisher(adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL),
                                                   adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_RECEIVER_URL),
                                                   adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_USER_NAME),
                                                   adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_PASSWORD),
                                                   agent);
        } else {
            dataPublisher = new AsyncDataPublisher(adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_RECEIVER_URL),
                                                   adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_USER_NAME),
                                                   adaptorOutputProperties.get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_PASSWORD),
                                                   agent);
        }
        return dataPublisher;
    }


    private void publishEvent(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
            AsyncDataPublisher dataPublisher,
            Event event, StreamDefinition streamDefinition) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("At publisher of the Output WSO2Event Transport Adaptor " + event);
            }
            dataPublisher.publish(streamDefinition.getName(), streamDefinition.getVersion(), event);
        } catch (AgentException ex) {
            throw new OutputTransportAdaptorEventProcessingException(
                    "Cannot publish data via DataPublisher for the transport configuration:" +
                    outputTransportAdaptorConfiguration.getName() + " for the  event " + event, ex);
        }

    }

    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration, int tenantId) {
        try {
            DataPublisher dataPublisher = new DataPublisher(outputTransportAdaptorConfiguration.getOutputProperties().get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_RECEIVER_URL),outputTransportAdaptorConfiguration.getOutputProperties().get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_USER_NAME),outputTransportAdaptorConfiguration.getOutputProperties().get(WSO2EventTransportAdaptorConstants.TRANSPORT_CONF_WSO2EVENT_PROP_PASSWORD));
            dataPublisher.findStreamId("TestStream","1.0.0");
        } catch (MalformedURLException e) {
            throw new OutputTransportAdaptorEventProcessingException(e);
        } catch (AgentException e) {
            throw new OutputTransportAdaptorEventProcessingException(e);
        } catch (AuthenticationException e) {
            throw new OutputTransportAdaptorEventProcessingException(e);
        } catch (TransportException e) {
            throw new OutputTransportAdaptorEventProcessingException(e);
        }

    }


}
