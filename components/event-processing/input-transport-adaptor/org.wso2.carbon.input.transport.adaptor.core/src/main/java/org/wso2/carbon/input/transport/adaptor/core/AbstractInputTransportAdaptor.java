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

package org.wso2.carbon.input.transport.adaptor.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;

import java.util.List;

/**
 * This is a TransportAdaptor type. these interface let users to publish subscribe messages according to
 * some type. this type can either be local, jms or ws
 */
public abstract class AbstractInputTransportAdaptor {

    private static final Log log = LogFactory.getLog(AbstractInputTransportAdaptor.class);
    private InputTransportAdaptorDto inputTransportAdaptorDto;
    private MessageDto messageDto;

    protected AbstractInputTransportAdaptor() {

        init();

        this.inputTransportAdaptorDto = new InputTransportAdaptorDto();
        this.messageDto = new MessageDto();
        this.inputTransportAdaptorDto.setTransportAdaptorTypeName(this.getName());
        this.inputTransportAdaptorDto.setSupportedMessageTypes(this.getSupportedInputMessageTypes());

        this.messageDto.setAdaptorName(this.getName());

        inputTransportAdaptorDto.setAdaptorPropertyList(((this)).getInputAdaptorProperties());
        messageDto.setMessageInPropertyList(((this)).getInputMessageProperties());
    }

    public InputTransportAdaptorDto getInputTransportAdaptorDto() {
        return inputTransportAdaptorDto;
    }

    public MessageDto getMessageDto() {
        return messageDto;
    }

    /**
     * returns the name of the input transport adaptor type
     *
     * @return transport adaptor type name
     */
    protected abstract String getName();

    /**
     * To get the information regarding supported message types transport adaptor
     *
     * @return List of supported input message types
     */
    protected abstract List<String> getSupportedInputMessageTypes();

    /**
     * any initialization can be done in this method
     */
    protected abstract void init();

    /**
     * the information regarding the adaptor related properties of a specific transport adaptor type
     *
     * @return List of properties related to input transport adaptor
     */
    protected abstract List<Property> getInputAdaptorProperties();

    /**
     * to get message related input configuration details
     *
     * @return list of input message configuration properties
     */
    protected abstract List<Property> getInputMessageProperties();

    /**
     * subscribe to the connection specified in the transport adaptor configuration.
     *
     * @param inputTransportAdaptorMessageConfiguration
     *                                      - message specific configuration to subscribe
     * @param inputTransportAdaptorListener - transport type will invoke this when it receive events
     * @param inputTransportAdaptorConfiguration
     *                                      - transport adaptor configuration details
     */
    public abstract String subscribe(
            InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration,
            InputTransportAdaptorListener inputTransportAdaptorListener,
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
            AxisConfiguration axisConfiguration);

    /**
     * this method unsubscribes the subscription from the transport adaptor.
     *
     * @param inputTransportAdaptorMessageConfiguration
     *         - configuration related to message
     * @param inputTransportAdaptorConfiguration
     *         - transport adaptor configuration
     */
    public abstract void unsubscribe(
            InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration,
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId);


}
