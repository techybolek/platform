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

package org.wso2.carbon.transport.adaptor.jms;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.transport.adaptor.core.AbstractTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.OutputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.jms.util.JMSTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JMSTransportAdaptorType extends AbstractTransportAdaptor
        implements InputTransportAdaptor, OutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(JMSTransportAdaptorType.class);
    private static JMSTransportAdaptorType jmsTransportAdaptorAdaptor = new JMSTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>> inputTransportListenerMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>> dataPublisherMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>>();
    private Agent agent;

    private JMSTransportAdaptorType() {

//        AgentTransportAdaptorServiceValueHolder.getDataBridgeSubscriberService().subscribe(new AgentTransportCallback());

    }

    @Override
    protected TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType() {
        return TransportAdaptorDto.TransportAdaptorType.INOUT;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.XML);
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.JSON);
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.MAP);
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.XML);
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.JSON);
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.MAP);
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return agent transport adaptor instance
     */
    public static JMSTransportAdaptorType getInstance() {

        return jmsTransportAdaptorAdaptor;
    }

    /**
     * @return name of the agent transport adaptor
     */
    @Override
    protected String getName() {
        return JMSTransportAdaptorConstants.TRANSPORT_TYPE_JMS;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.jms.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {


        List<Property> propertyList = new ArrayList<Property>();

        // JNDI initial context factory class
        Property initialContextProperty = new Property(JMSTransportAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS);
        initialContextProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS));
        initialContextProperty.setRequired(true);
        initialContextProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT));
        propertyList.add(initialContextProperty);


        // JNDI Provider URL
        Property javaNamingProviderUrlProperty = new Property(JMSTransportAdaptorConstants.JAVA_NAMING_PROVIDER_URL);
        javaNamingProviderUrlProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_PROVIDER_URL));
        javaNamingProviderUrlProperty.setRequired(true);
        javaNamingProviderUrlProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_PROVIDER_URL_HINT));
        propertyList.add(javaNamingProviderUrlProperty);


        // JNDI Username
        Property userNameProperty = new Property(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_PRINCIPAL);
        userNameProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_PRINCIPAL));
        propertyList.add(userNameProperty);


        // JNDI Password
        Property passwordProperty = new Property(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_CREDENTIALS);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_CREDENTIALS));
        propertyList.add(passwordProperty);

        // Connection Factory JNDI Name
        Property connectionFactoryNameProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME);
        connectionFactoryNameProperty.setRequired(true);
        connectionFactoryNameProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME));
        javaNamingProviderUrlProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME_HINT));
        propertyList.add(connectionFactoryNameProperty);

        return propertyList;
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // Enable Durable Subscription
        Property isDurableSubscriptionProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_SUBSCRIPTION_DURABLE);
        isDurableSubscriptionProperty.setRequired(false);
        isDurableSubscriptionProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_SUBSCRIPTION_DURABLE));
        isDurableSubscriptionProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_SUBSCRIPTION_DURABLE_HINT));
        isDurableSubscriptionProperty.setOptions(new String[]{"true", "false"});
        isDurableSubscriptionProperty.setDefaultValue("false");
        propertyList.add(isDurableSubscriptionProperty);

        // Connection Factory JNDI Name
        Property subscriberNameProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_DURABLE_SUBSCRIBER_NAME);
        subscriberNameProperty.setRequired(false);
        subscriberNameProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DURABLE_SUBSCRIBER_NAME));
        subscriberNameProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DURABLE_SUBSCRIBER_NAME_HINT));
        propertyList.add(subscriberNameProperty);

        return propertyList;
    }

    /**
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // Topic
        Property topicProperty = new Property(JMSTransportAdaptorConstants.JMS_TOPIC);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JMS_TOPIC));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        return propertyList;

    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        return null;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // Topic
        Property topicProperty = new Property(JMSTransportAdaptorConstants.JMS_TOPIC);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JMS_TOPIC));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        return propertyList;

    }

    public String subscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            TransportAdaptorListener transportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration)
            throws TransportEventProcessingException {
        String subscriptionId = UUID.randomUUID().toString();
//
//        if (!inputTransportListenerMap.keySet().contains(inputTransportMessageConfiguration)) {
//            Map<String, TransportAdaptorListener> map = new HashMap<String, TransportAdaptorListener>();
//            map.put(subscriptionId, transportAdaptorListener);
//            inputTransportListenerMap.put(inputTransportMessageConfiguration, map);
//        } else {
//            inputTransportListenerMap.get(inputTransportMessageConfiguration).put(subscriptionId, transportAdaptorListener);
//            StreamDefinition streamDefinition = inputStreamDefinitionMap.get(inputTransportMessageConfiguration);
//            if (streamDefinition != null) {
//                transportAdaptorListener.addEventDefinition(streamDefinition);
//            }
//
//        }

        return subscriptionId;
    }


    /**
     * @param outputTransportMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputTransportAdaptorConfiguration
     *                - transport configuration to be used
     * @throws org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException
     *
     */
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration)
            throws TransportEventProcessingException {
//        Integer tenantId = CarbonContext.getCurrentContext().getTenantId();
//        ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher> dataPublishers = dataPublisherMap.get(tenantId);
//        if (dataPublishers == null) {
//            dataPublishers = new ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>();
//            dataPublisherMap.putIfAbsent(tenantId, dataPublishers);
//            dataPublishers = dataPublisherMap.get(tenantId);
//        }
//        AsyncDataPublisher dataPublisher = dataPublishers.get(outputTransportAdaptorConfiguration);
//        if (dataPublisher == null) {
//            synchronized (this) {
//                dataPublisher = dataPublishers.get(outputTransportAdaptorConfiguration);
//                if (dataPublisher == null) {
//                    dataPublisher = createDataPublisher(outputTransportAdaptorConfiguration);
//                    dataPublishers.putIfAbsent(outputTransportAdaptorConfiguration, dataPublisher);
//                }
//            }
//        }
//
//        try {
//            Event event = (Event) ((Object[]) message)[0];
//            StreamDefinition streamDefinition = (StreamDefinition) ((Object[]) message)[1];
//
//            if (!dataPublisher.isStreamDefinitionAdded(streamDefinition)) {
//                dataPublisher.addStreamDefinition(streamDefinition);
//
//                //Sending the first Event
//                publishEvent(outputTransportAdaptorConfiguration, dataPublisher, event, streamDefinition);
//            } else {
//                //Sending Events
//                publishEvent(outputTransportAdaptorConfiguration, dataPublisher, event, streamDefinition);
//            }
//        } catch (Exception ex) {
//            throw new TransportEventProcessingException(
//                    ex.getMessage() + " Error Occurred When Publishing Events", ex);
//        }

    }


    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration)
            throws TransportEventProcessingException {
        // no test
    }

    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId)
            throws TransportEventProcessingException {
//        Map<String, TransportAdaptorListener> map = inputTransportListenerMap.get(inputTransportMessageConfiguration);
//        if (map != null) {
//            map.remove(subscriptionId);
//        }

    }

}
