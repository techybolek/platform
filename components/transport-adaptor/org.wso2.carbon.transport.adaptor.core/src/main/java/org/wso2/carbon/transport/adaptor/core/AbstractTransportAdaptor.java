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

package org.wso2.carbon.transport.adaptor.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorConfigException;
import org.wso2.carbon.transport.adaptor.core.message.MessageDto;

import java.util.List;

/**
 * This is a TransportAdaptor type. these interface let users to publish subscribe messages according to
 * some type. this type can either be local, jms or ws
 */
public abstract class AbstractTransportAdaptor {

    private static final Log log = LogFactory.getLog(AbstractTransportAdaptor.class);
    private TransportAdaptorDto transportAdaptorDto;
    private MessageDto messageDto;

    protected AbstractTransportAdaptor() {

        init();

        this.transportAdaptorDto = new TransportAdaptorDto();
        this.messageDto = new MessageDto();
        this.transportAdaptorDto.setTransportAdaptorTypeName(this.getName());
        this.transportAdaptorDto.setSupportedInputMessageType(this.getSupportedInputMessageTypes());
        this.transportAdaptorDto.setSupportedOutputMessageType(this.getSupportedOutputMessageTypes());
        this.transportAdaptorDto.setSupportedTransportAdaptorType(this.getTransportAdaptorType());

        this.messageDto.setAdaptorName(this.getName());

        if (this instanceof InputTransportAdaptor) {
            transportAdaptorDto.setAdaptorInPropertyList(((InputTransportAdaptor) (this)).getInputAdaptorProperties());
            messageDto.setMessageInPropertyList(((InputTransportAdaptor) (this)).getInputMessageProperties());
        }
        if (this instanceof OutputTransportAdaptor) {
            transportAdaptorDto.setAdaptorOutPropertyList(((OutputTransportAdaptor) (this)).getOutputAdaptorProperties());
            messageDto.setMessageOutPropertyList(((OutputTransportAdaptor) (this)).getOutputMessageProperties());
        }

        if (getCommonAdaptorConfig() != null) {
            transportAdaptorDto.setAdaptorCommonPropertyList(getCommonAdaptorConfig());
        }

        try {
            if (!(this instanceof InputTransportAdaptor) && !(this instanceof OutputTransportAdaptor)) {
                throw new TransportAdaptorConfigException("Transport Adaptor must implement InputTransportAdaptor or/and OutputTransportAdaptor");
            }
        } catch (TransportAdaptorConfigException ex) {
            log.error("Error in creating TransportAdaptorDto for " + this.getName() + ex);
        }


    }

    public TransportAdaptorDto getTransportAdaptorDto() {
        return transportAdaptorDto;
    }

    public MessageDto getMessageDto() {
        return messageDto;
    }

    protected abstract String getName();

    protected abstract List<Property> getCommonAdaptorConfig();

    protected abstract TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType();

    protected abstract List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes();

    protected abstract List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes();

    protected abstract void init();


}
