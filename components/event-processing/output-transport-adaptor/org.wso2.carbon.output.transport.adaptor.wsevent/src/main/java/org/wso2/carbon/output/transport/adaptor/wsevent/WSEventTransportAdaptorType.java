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

package org.wso2.carbon.output.transport.adaptor.wsevent;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.output.transport.adaptor.core.AbstractOutputTransportAdaptor;
import org.wso2.carbon.output.transport.adaptor.core.MessageType;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.exception.OutputTransportAdaptorEventProcessingException;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.output.transport.adaptor.wsevent.internal.ds.WSEventTransportAdaptorServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.wsevent.internal.util.WSEventTransportAdaptorConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class WSEventTransportAdaptorType extends AbstractOutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSEventTransportAdaptorType.class);
    private static WSEventTransportAdaptorType wsEventTransportAdaptor = new WSEventTransportAdaptorType();
    private ResourceBundle resourceBundle;

    private WSEventTransportAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);

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

        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.output.transport.adaptor.wsevent.i18n.Resources", Locale.getDefault());
    }


    @Override
    public List<Property> getOutputAdaptorProperties() {
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
    public void publish(
            OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration,
            Object message,
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration, int tenantId) {

        Map<String, String> properties = outputTransportAdaptorConfiguration.getOutputProperties();
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
            throw new OutputTransportAdaptorEventProcessingException("Can not publish to the ws event broker ", axisFault);
        } catch (AuthenticationExceptionException e) {
            throw new OutputTransportAdaptorEventProcessingException("Can not authenticate the ws broker, hence cannot publish to the broker ", e);
        }

    }

    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration, int tenantId) {
        String testMessage = " <transportAdaptorConfigurationTest>\n" +
                             "   <message>This is a test message.</message>\n" +
                             "   </transportAdaptorConfigurationTest>";
        try {
            XMLStreamReader reader1 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(testMessage.getBytes()));
            StAXOMBuilder builder1 = new StAXOMBuilder(reader1);
            OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration = new OutputTransportAdaptorMessageConfiguration();
            Map<String, String> propertyList = new ConcurrentHashMap<String, String>();
            outputTransportMessageConfiguration.setOutputMessageProperties(propertyList);

            propertyList.put(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME, "test");

            publish(outputTransportMessageConfiguration, builder1.getDocumentElement(), outputTransportAdaptorConfiguration, tenantId);

        } catch (XMLStreamException e) {
            throw new OutputTransportAdaptorEventProcessingException(e.getMessage());
        } catch (OutputTransportAdaptorEventProcessingException e) {
            throw new OutputTransportAdaptorEventProcessingException(e);
        }

    }
}
