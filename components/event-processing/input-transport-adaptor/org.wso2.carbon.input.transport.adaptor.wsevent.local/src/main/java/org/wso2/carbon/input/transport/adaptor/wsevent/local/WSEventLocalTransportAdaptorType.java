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

package org.wso2.carbon.input.transport.adaptor.wsevent.local;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.input.transport.adaptor.core.AbstractInputTransportAdaptor;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.MessageType;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.input.transport.adaptor.wsevent.local.internal.util.Axis2Util;
import org.wso2.carbon.input.transport.adaptor.wsevent.local.internal.util.WSEventLocalTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;


public final class WSEventLocalTransportAdaptorType extends AbstractInputTransportAdaptor {

    private static final Log log = LogFactory.getLog(WSEventLocalTransportAdaptorType.class);
    private static WSEventLocalTransportAdaptorType wsEventLocalTransportAdaptor = new WSEventLocalTransportAdaptorType();
    private ResourceBundle resourceBundle;

    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);

    private WSEventLocalTransportAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);

        return supportInputMessageTypes;
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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.input.transport.adaptor.wsevent.local.i18n.Resources", Locale.getDefault());
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
    public String subscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorListener inputTransportAdaptorListener,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {

        String subscriptionId = UUID.randomUUID().toString();

        try {
            Axis2Util.registerAxis2Service(inputTransportMessageConfiguration, inputTransportAdaptorListener,
                                           inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputTransportAdaptorEventProcessingException("Can not create " +
                                                               "the axis2 service to receive events", axisFault);
        }
        return subscriptionId;

    }

    @Override
    public void unsubscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        try {
            Axis2Util.removeOperation(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputTransportAdaptorEventProcessingException("Can not remove operation ", axisFault);
        }

    }



}
