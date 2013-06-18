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

package org.wso2.carbon.transport.adaptor.wsevent;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.broker.BrokerClientException;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
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
import org.wso2.carbon.transport.adaptor.wsevent.internal.ds.WSEventTransportAdaptorServiceValueHolder;
import org.wso2.carbon.transport.adaptor.wsevent.internal.util.Axis2Util;
import org.wso2.carbon.transport.adaptor.wsevent.internal.util.WSEventTransportAdaptorConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WSEventTransportAdaptorType extends AbstractTransportAdaptor
        implements InputTransportAdaptor, OutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSEventTransportAdaptorType.class);
    private static WSEventTransportAdaptorType wsEventTransportAdaptor = new WSEventTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private Map<String, Map<String, String>> adaptorSubscriptionsMap;


    private WSEventTransportAdaptorType() {

    }

    @Override
    protected TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType() {
        return TransportAdaptorDto.TransportAdaptorType.INOUT;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.XML);

        return supportInputMessageTypes;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.XML);

        return supportOutputMessageTypes;
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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.wsevent.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {
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
    public List<Property> getOutputAdaptorProperties() {
        return null;
    }

    @Override
    public List<Property> getOutputMessageProperties() {
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
    public String subscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            TransportAdaptorListener transportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {
        String subscriptionId = UUID.randomUUID().toString();
        try {
            AxisService axisService = Axis2Util.registerAxis2Service(inputTransportMessageConfiguration, transportAdaptorListener, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);

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

            Map<String, String> properties = inputTransportAdaptorConfiguration.getCommonProperties();
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
            throw new TransportAdaptorEventProcessingException("Can not create the adaptor client", e);
        } catch (AuthenticationExceptionException e) {
            throw new TransportAdaptorEventProcessingException("Can not authenticate the adaptor client", e);
        } catch (AxisFault axisFault) {
            throw new TransportAdaptorEventProcessingException("Can not subscribe", axisFault);
        }


    }

    @Override
    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {

        try {
            Axis2Util.removeOperation(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new TransportAdaptorEventProcessingException("Can not unsubscribe from the ws broker", axisFault);
        }

        Map<String, String> localSubscriptionIdSubscriptionsMap =
                this.adaptorSubscriptionsMap.get(inputTransportAdaptorConfiguration.getName());
        if (localSubscriptionIdSubscriptionsMap == null) {
            throw new TransportAdaptorEventProcessingException("There is no subscription for broker "
                                                               + inputTransportAdaptorConfiguration.getName());
        }

        String topicName = inputTransportMessageConfiguration.getInputMessageProperties().get(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME);

        String subscriptionID = localSubscriptionIdSubscriptionsMap.remove(subscriptionId);
        if (subscriptionID == null) {
            throw new TransportAdaptorEventProcessingException("There is no subscriptions for this topic" + topicName);
        }

        try {
            Map<String, String> properties = inputTransportAdaptorConfiguration.getCommonProperties();
            ConfigurationContextService configurationContextService =
                    WSEventTransportAdaptorServiceValueHolder.getConfigurationContextService();
            BrokerClient brokerClient =
                    new BrokerClient(configurationContextService.getClientConfigContext(),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_URI),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_USERNAME),
                                     properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_PASSWORD));
            brokerClient.unsubscribe(subscriptionID);
        } catch (AuthenticationExceptionException e) {
            throw new TransportAdaptorEventProcessingException("Can not authenticate the ws broker, hence not un subscribing from the broker", e);
        } catch (RemoteException e) {
            throw new TransportAdaptorEventProcessingException("Can not connect to the server, hence not un subscribing from the broker", e);
        }

    }


    @Override
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {

        Map<String, String> properties = outputTransportAdaptorConfiguration.getCommonProperties();
        ConfigurationContextService configurationContextService =
                WSEventTransportAdaptorServiceValueHolder.getConfigurationContextService();

        try {
            BrokerClient brokerClient = new BrokerClient(configurationContextService.getClientConfigContext(),
                                                         properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_URI),
                                                         properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_USERNAME),
                                                         properties.get(WSEventTransportAdaptorConstants.TRANSPORT_CONF_WSEVENT_PASSWORD));

            String topicName = outputTransportMessageConfiguration.getOutputMessageProperties().get(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME);

            brokerClient.publish(topicName, ((OMElement) message));


        } catch (AxisFault axisFault) {
            throw new TransportAdaptorEventProcessingException("Can not publish to the ws event broker ", axisFault);
        } catch (AuthenticationExceptionException e) {
            throw new TransportAdaptorEventProcessingException("Can not authenticate the ws broker, hence cannot publish to the broker ", e);
        }

    }

    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        String testMessage = " <transportAdaptorConfigurationTest>\n" +
                             "   <message>This is a test message.</message>\n" +
                             "   </transportAdaptorConfigurationTest>";
        try {
            XMLStreamReader reader1 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(testMessage.getBytes()));
            StAXOMBuilder builder1 = new StAXOMBuilder(reader1);
            OutputTransportMessageConfiguration outputTransportMessageConfiguration = new OutputTransportMessageConfiguration();
            Map<String, String> propertyList = new ConcurrentHashMap<String, String>();
            outputTransportMessageConfiguration.setOutputMessageProperties(propertyList);

            propertyList.put(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME, "test");

            publish(outputTransportMessageConfiguration, builder1.getDocumentElement(), outputTransportAdaptorConfiguration);

        } catch (XMLStreamException e) {
            //ignored as this will not happen
        }
    }
}
