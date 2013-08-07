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

package org.wso2.carbon.input.transport.adaptor.jms;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.input.transport.adaptor.core.AbstractInputTransportAdaptor;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorDto;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.input.transport.adaptor.jms.internal.util.JMSConnectionFactory;
import org.wso2.carbon.input.transport.adaptor.jms.internal.util.JMSConstants;
import org.wso2.carbon.input.transport.adaptor.jms.internal.util.JMSMessageListener;
import org.wso2.carbon.input.transport.adaptor.jms.internal.util.JMSTaskManager;
import org.wso2.carbon.input.transport.adaptor.jms.internal.util.JMSTaskManagerFactory;
import org.wso2.carbon.input.transport.adaptor.jms.internal.util.JMSTransportAdaptorConstants;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JMSTransportAdaptorType extends AbstractInputTransportAdaptor {

    private static final Log log = LogFactory.getLog(JMSTransportAdaptorType.class);
    private static JMSTransportAdaptorType jmsTransportAdaptorAdaptor = new JMSTransportAdaptorType();
    private ResourceBundle resourceBundle;

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>> tenantAdaptorDestinationSubscriptionsMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>>();

    private JMSTransportAdaptorType() {

    }


    @Override
    protected List<InputTransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<InputTransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<InputTransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(InputTransportAdaptorDto.MessageType.XML);
        supportInputMessageTypes.add(InputTransportAdaptorDto.MessageType.JSON);
        supportInputMessageTypes.add(InputTransportAdaptorDto.MessageType.MAP);
        supportInputMessageTypes.add(InputTransportAdaptorDto.MessageType.TEXT);
        return supportInputMessageTypes;
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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.input.transport.adaptor.jms.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {

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
        Property userNameProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_USERNAME));
        propertyList.add(userNameProperty);


        // JNDI Password
        Property passwordProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_PASSWORD));
        propertyList.add(passwordProperty);

        // Connection Factory JNDI Name
        Property connectionFactoryNameProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME);
        connectionFactoryNameProperty.setRequired(true);
        connectionFactoryNameProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME));
        connectionFactoryNameProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME_HINT));
        propertyList.add(connectionFactoryNameProperty);


        // Destination Type
        Property destinationTypeProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION_TYPE);
        destinationTypeProperty.setRequired(true);
        destinationTypeProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION_TYPE));
        destinationTypeProperty.setOptions(new String[]{"queue", "topic"});
        destinationTypeProperty.setDefaultValue("topic");
        destinationTypeProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION_TYPE_HINT));
        propertyList.add(destinationTypeProperty);

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
        Property topicProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        return propertyList;

    }


    public String subscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorListener inputTransportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {

        inputTransportAdaptorListener.setStatisticsEnabled(inputTransportAdaptorConfiguration.isEnableStatistics());
        inputTransportAdaptorListener.setTraceEnabled(inputTransportAdaptorConfiguration.isEnableTracing());
        inputTransportAdaptorListener.setTransportAdaptorName(inputTransportAdaptorConfiguration.getName());

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>> adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            adaptorDestinationSubscriptionsMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>();
            if (null != tenantAdaptorDestinationSubscriptionsMap.putIfAbsent(tenantId, adaptorDestinationSubscriptionsMap)) {
                adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
            }
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>> destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(inputTransportAdaptorConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            destinationSubscriptionsMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>();
            if (null != adaptorDestinationSubscriptionsMap.putIfAbsent(inputTransportAdaptorConfiguration.getName(), destinationSubscriptionsMap)) {
                destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(inputTransportAdaptorConfiguration.getName());
            }
        }

        String destination = inputTransportMessageConfiguration.getInputMessageProperties().get(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION);

        ConcurrentHashMap<String, SubscriptionDetails> subscriptionsMap = destinationSubscriptionsMap.get(destination);
        if (subscriptionsMap == null) {
            subscriptionsMap = new ConcurrentHashMap<String, SubscriptionDetails>();
            if (null != destinationSubscriptionsMap.putIfAbsent(destination, subscriptionsMap)) {
                subscriptionsMap = destinationSubscriptionsMap.get(destination);
            }
        }

        String subscriptionId = UUID.randomUUID().toString();


        Map<String, String> adaptorProperties = new HashMap<String, String>();
        adaptorProperties.putAll(inputTransportAdaptorConfiguration.getInputProperties());

        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(new Hashtable<String, String>(adaptorProperties), inputTransportAdaptorConfiguration.getName());

        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, destination);
        JMSTaskManager JMSTaskManager = JMSTaskManagerFactory.createTaskManagerForService(jmsConnectionFactory, inputTransportAdaptorConfiguration.getName(), new NativeWorkerPool(4, 100, 1000, 1000, "JMS Threads", "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        JMSTaskManager.setJmsMessageListener(new JMSMessageListener(inputTransportAdaptorListener));
        JMSTaskManager.start();

        SubscriptionDetails subscriptionDetails = new SubscriptionDetails(jmsConnectionFactory, JMSTaskManager);
        subscriptionsMap.put(subscriptionId, subscriptionDetails);

        return subscriptionId;
    }


    public void unsubscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {

        String destination = inputTransportMessageConfiguration.getInputMessageProperties().get(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION);

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>> adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            throw new InputTransportAdaptorEventProcessingException("There is no subscription for " + destination + " for tenant " + PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantDomain());
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>> destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(inputTransportAdaptorConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            throw new InputTransportAdaptorEventProcessingException("There is no subscription for " + destination + " for transport adaptor " + inputTransportAdaptorConfiguration.getName());
        }

        ConcurrentHashMap<String, SubscriptionDetails> subscriptionsMap = destinationSubscriptionsMap.get(destination);
        if (subscriptionsMap == null) {
            throw new InputTransportAdaptorEventProcessingException("There is no subscription for " + destination);
        }

        SubscriptionDetails subscriptionDetails = subscriptionsMap.get(subscriptionId);
        if (subscriptionDetails == null) {
            throw new InputTransportAdaptorEventProcessingException("There is no subscription for " + destination + " for the subscriptionId:" + subscriptionId);
        }

        try {
            subscriptionDetails.close();
        } catch (JMSException e) {
            throw new InputTransportAdaptorEventProcessingException("Can not unsubscribe from the destination " + destination + " with the transport adaptor " + inputTransportAdaptorConfiguration.getName(), e);
        }

    }

    class SubscriptionDetails {

        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSTaskManager jmsTaskManager;

        public SubscriptionDetails(JMSConnectionFactory jmsConnectionFactory,
                                   JMSTaskManager jmsTaskManager) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsTaskManager = jmsTaskManager;
        }

        public void close() throws JMSException {
            this.jmsTaskManager.stop();
            this.jmsConnectionFactory.stop();
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSTaskManager getJmsTaskManager() {
            return jmsTaskManager;
        }
    }

}
