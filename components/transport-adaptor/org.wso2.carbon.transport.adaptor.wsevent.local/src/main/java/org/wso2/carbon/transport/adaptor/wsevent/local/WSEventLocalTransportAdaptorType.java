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

package org.wso2.carbon.transport.adaptor.wsevent.local;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
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
import org.wso2.carbon.transport.adaptor.wsevent.local.internal.ds.WSEventLocalTransportAdaptorServiceValueHolder;
import org.wso2.carbon.transport.adaptor.wsevent.local.internal.util.Axis2Util;
import org.wso2.carbon.transport.adaptor.wsevent.local.internal.util.WSEventLocalTransportAdaptorConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WSEventLocalTransportAdaptorType extends AbstractTransportAdaptor
        implements InputTransportAdaptor, OutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSEventLocalTransportAdaptorType.class);
    private static WSEventLocalTransportAdaptorType wsEventLocalTransportAdaptor = new WSEventLocalTransportAdaptorType();
    private ResourceBundle resourceBundle;

    private WSEventLocalTransportAdaptorType() {

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
     * @return WS Local transport adaptor instance
     */
    public static WSEventLocalTransportAdaptorType getInstance() {

        return wsEventLocalTransportAdaptor;
    }

    /**
     * @return name of the WS Local transport adaptor
     */
    @Override
    protected String getName() {
        return WSEventLocalTransportAdaptorConstants.TRANSPORT_TYPE_WSEVENT_LOCAL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.wsevent.local.i18n.Resources", Locale.getDefault());
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

        // set receiver url transport
        Property topicProperty = new Property(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME);
        topicProperty.setDisplayName(
                resourceBundle.getString(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_HINT_TOPIC_NAME));

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

        // set receiver url transport
        Property topicProperty = new Property(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME);
        topicProperty.setDisplayName(
                resourceBundle.getString(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_HINT_TOPIC_NAME));

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
            Axis2Util.registerAxis2Service(inputTransportMessageConfiguration, transportAdaptorListener,
                                           inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new TransportAdaptorEventProcessingException("Can not create " +
                                                               "the axis2 service to receive events", axisFault);
        }
        return subscriptionId;

    }

    @Override
    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        try {
            Axis2Util.removeOperation(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new TransportAdaptorEventProcessingException("Can not remove operation ", axisFault);
        }

    }


    @Override
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {

        EventBroker eventTransportAdaptor = WSEventLocalTransportAdaptorServiceValueHolder.getEventBroker();
        Message eventMessage = new Message();
        eventMessage.setMessage(((OMElement) message));
        try {
            eventTransportAdaptor.publishRobust(eventMessage, outputTransportAdaptorConfiguration.getOutputProperties().get(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME));
        } catch (EventBrokerException e) {
            throw new TransportAdaptorEventProcessingException("Can not publish the to local broker ", e);
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

            propertyList.put(WSEventLocalTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME, "test");

            publish(outputTransportMessageConfiguration, builder1.getDocumentElement(), outputTransportAdaptorConfiguration);

        } catch (XMLStreamException e) {
            //ignored as this will not happen
        }
    }
}
