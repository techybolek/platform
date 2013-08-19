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

package org.wso2.carbon.input.transport.adaptor.wsevent;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.broker.BrokerClientException;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.input.transport.adaptor.core.AbstractInputTransportAdaptor;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.MessageType;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.input.transport.adaptor.wsevent.internal.ds.WSEventTransportAdaptorServiceValueHolder;
import org.wso2.carbon.input.transport.adaptor.wsevent.internal.util.Axis2Util;
import org.wso2.carbon.input.transport.adaptor.wsevent.internal.util.WSEventTransportAdaptorConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WSEventTransportAdaptorType extends AbstractInputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSEventTransportAdaptorType.class);
    private static WSEventTransportAdaptorType wsEventTransportAdaptor = new WSEventTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<String, Map<String, String>> adaptorSubscriptionsMap;
    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);

    private WSEventTransportAdaptorType() {

    }

    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);

        return supportInputMessageTypes;
    }

    /**
     * @return WS Event transport adaptor instance
     */
    public static WSEventTransportAdaptorType getInstance() {
        return wsEventTransportAdaptor;
    }

    /**
     * @return name of the WS Event transport adaptor
     */
    @Override
    protected String getName() {
        return WSEventTransportAdaptorConstants.TRANSPORT_TYPE_WSEVENT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        this.adaptorSubscriptionsMap = new ConcurrentHashMap<String, Map<String, String>>();
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.input.transport.adaptor.wsevent.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // URI
        Property uri = new Property(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_URI);
        uri.setDisplayName(
                resourceBundle.getString(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_URI));
        uri.setRequired(true);
        uri.setHint(resourceBundle.getString(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_URI_HINT));
        propertyList.add(uri);


        // Username
        Property userNameProperty = new Property(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_USERNAME));
        propertyList.add(userNameProperty);


        // Password
        Property passwordProperty = new Property(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_PASSWORD));
        propertyList.add(passwordProperty);

        return propertyList;
    }


    /**
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // topic name
        Property topicProperty = new Property(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME);
        topicProperty.setDisplayName(
                resourceBundle.getString(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_HINT_TOPIC_NAME));

        propertyList.add(topicProperty);

        return propertyList;

    }


    @Override
    public String subscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorListener inputTransportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {


        String subscriptionId = UUID.randomUUID().toString();
        try {
            AxisService axisService = Axis2Util.registerAxis2Service(inputTransportMessageConfiguration, inputTransportAdaptorListener, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);

            String httpEpr = null;
            for (String epr : axisService.getEPRs()) {
                if (epr.startsWith("http")) {
                    httpEpr = epr;
                    break;
                }
            }

            if (httpEpr != null && !httpEpr.endsWith("/")) {
                httpEpr += "/";
            }

            String topicName = inputTransportMessageConfiguration.getInputMessageProperties().get(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME);
            httpEpr += topicName.replaceAll("/", "");

            Map<String, String> properties = inputTransportAdaptorConfiguration.getInputProperties();
            BrokerClient brokerClient =
                    new BrokerClient(properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_URI),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_USERNAME),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_PASSWORD));
            brokerClient.subscribe(topicName, httpEpr);

            String subscriptionID = brokerClient.subscribe(topicName, httpEpr);

            // keep the subscription id to unsubscribe
            Map<String, String> localSubscriptionIdSubscriptionsMap =
                    this.adaptorSubscriptionsMap.get(inputTransportAdaptorConfiguration.getName());
            if (localSubscriptionIdSubscriptionsMap == null) {
                localSubscriptionIdSubscriptionsMap = new ConcurrentHashMap<String, String>();
                this.adaptorSubscriptionsMap.put(inputTransportAdaptorConfiguration.getName(), localSubscriptionIdSubscriptionsMap);
            }

            localSubscriptionIdSubscriptionsMap.put(subscriptionId, subscriptionID);
            return subscriptionId;

        } catch (BrokerClientException e) {
            throw new InputTransportAdaptorEventProcessingException("Can not create the adaptor client", e);
        } catch (AuthenticationExceptionException e) {
            throw new InputTransportAdaptorEventProcessingException("Can not authenticate the adaptor client", e);
        } catch (AxisFault axisFault) {
            throw new InputTransportAdaptorEventProcessingException("Can not subscribe", axisFault);
        }


    }

    @Override
    public void unsubscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {

        try {
            Axis2Util.removeOperation(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputTransportAdaptorEventProcessingException("Can not unsubscribe from the ws broker", axisFault);
        }

        Map<String, String> localSubscriptionIdSubscriptionsMap =
                this.adaptorSubscriptionsMap.get(inputTransportAdaptorConfiguration.getName());
        if (localSubscriptionIdSubscriptionsMap == null) {
            throw new InputTransportAdaptorEventProcessingException("There is no subscription for broker "
                                                               + inputTransportAdaptorConfiguration.getName());
        }

        String topicName = inputTransportMessageConfiguration.getInputMessageProperties().get(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME);

        String subscriptionID = localSubscriptionIdSubscriptionsMap.remove(subscriptionId);
        if (subscriptionID == null) {
            throw new InputTransportAdaptorEventProcessingException("There is no subscriptions for this topic" + topicName);
        }

        try {
            Map<String, String> properties = inputTransportAdaptorConfiguration.getInputProperties();
            ConfigurationContextService configurationContextService =
                    WSEventTransportAdaptorServiceValueHolder.getConfigurationContextService();
            BrokerClient brokerClient =
                    new BrokerClient(configurationContextService.getClientConfigContext(),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_URI),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_USERNAME),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_PASSWORD));
            brokerClient.unsubscribe(subscriptionID);
        } catch (AuthenticationExceptionException e) {
            throw new InputTransportAdaptorEventProcessingException("Can not authenticate the ws broker, hence not un subscribing from the broker", e);
        } catch (RemoteException e) {
            throw new InputTransportAdaptorEventProcessingException("Can not connect to the server, hence not un subscribing from the broker", e);
        }

    }

}
