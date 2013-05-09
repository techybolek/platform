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

package org.wso2.carbon.transport.adaptor.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.core.*;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransportAdaptor service implementation.
 */
public class CarbonTransportAdaptorService implements TransportAdaptorService {

    private static Log log = LogFactory.getLog(CarbonTransportAdaptorService.class);
    private Map<String, AbstractTransportAdaptor> transportAdaptorMap;
//    private Map<String, InputTransportMessageConfiguration> inputTransportmessageMap;
//    private Map<String, OutputTransportMessageConfiguration> outputTransportmessageMap;

    public CarbonTransportAdaptorService() {
        this.transportAdaptorMap = new ConcurrentHashMap();
//        this.inputTransportmessageMap = new ConcurrentHashMap();
//        this.outputTransportmessageMap = new ConcurrentHashMap();
    }

    public void registerTransportAdaptor(AbstractTransportAdaptor abstractTransportAdaptor) {
        TransportAdaptorDto transportAdaptorDto = abstractTransportAdaptor.getTransportAdaptorDto();
        this.transportAdaptorMap.put(transportAdaptorDto.getName(), abstractTransportAdaptor);
    }

//    public void registerInputTransportMessage(InputTransportMessageConfiguration inputTransportMessageConfiguration) {
//        this.inputTransportmessageMap.put(inputTransportMessageConfiguration.getName(), inputTransportMessageConfiguration);
//    }
//
//    public void registerInputTransportMessage(OutputTransportMessageConfiguration outputTransportMessageConfiguration) {
//        this.outputTransportmessageMap.put(outputTransportMessageConfiguration.getName(), outputTransportMessageConfiguration);
//    }

    @Override
    public List<TransportAdaptorDto> getTransportAdaptors() {
        List<TransportAdaptorDto> transportAdaptorDtos = new ArrayList<TransportAdaptorDto>();
        for (AbstractTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()) {
            transportAdaptorDtos.add(abstractTransportAdaptor.getTransportAdaptorDto());
        }
        return transportAdaptorDtos;
    }

    @Override
    public MessageDto getTransportMessageDto(String transportAdaptorType) {

        for(AbstractTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()){
            if(abstractTransportAdaptor.getTransportAdaptorDto().getTransportAdaptorType().equals(transportAdaptorType)){
                return abstractTransportAdaptor.getMessageDto();
            }
        }

        return null;
    }

    @Override
    public List<String> getTransportAdaptorNames() {
        List<String> transportAdaptorNames = new ArrayList<String>();
        for (AbstractTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()) {
            transportAdaptorNames.add(abstractTransportAdaptor.getTransportAdaptorDto().getName());
        }
        return transportAdaptorNames;
    }

//    @Override
//    public List<Property> getInputMessageTransportProperties(String messageName) {
//        return inputTransportmessageMap.get(messageName);
//    }
//
//    @Override
//    public List<Property> getOutputMessageTransportProperties(String messageName) {
//        return transportAdaptorMap.get(transportAdaptor).getTransportAdaptorDto().getMessageOutPropertyList();
//    }



    @Override
    public List<Property> getInputAdaptorTransportProperties(String transportAdaptor) {
        return transportAdaptorMap.get(transportAdaptor).getTransportAdaptorDto().getAdaptorInPropertyList();
    }


    @Override
    public List<Property> getOutputAdaptorTransportProperties(String transportAdaptor) {
        return transportAdaptorMap.get(transportAdaptor).getTransportAdaptorDto().getAdaptorOutPropertyList();
    }


    @Override
    public List<Property> getCommonAdaptorTransportProperties(String transportAdaptor) {
        return transportAdaptorMap.get(transportAdaptor).getTransportAdaptorDto().getAdaptorCommonPropertyList();
    }

    @Override
    public String subscribe(InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            TransportListener transportListener,
                            AxisConfiguration axisConfiguration) throws TransportEventProcessingException {
        InputTransportAdaptor inputTransportAdaptor = (InputTransportAdaptor) this.transportAdaptorMap.get(inputTransportAdaptorConfiguration.getType());

        try {
            return inputTransportAdaptor.subscribe(inputTransportMessageConfiguration, transportListener, inputTransportAdaptorConfiguration, axisConfiguration);
        } catch (TransportEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void publish(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
                        OutputTransportMessageConfiguration outputTransportMessageConfiguration, Object object) throws TransportEventProcessingException {

        OutputTransportAdaptor outputTransportAdaptor = (OutputTransportAdaptor) this.transportAdaptorMap.get(outputTransportAdaptorConfiguration.getType());
        try {
            outputTransportAdaptor.publish(outputTransportMessageConfiguration, object, outputTransportAdaptorConfiguration);
        } catch (TransportEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void testConnection(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) throws TransportEventProcessingException {
        OutputTransportAdaptor outputTransportAdaptor = (OutputTransportAdaptor) this.transportAdaptorMap.get(outputTransportAdaptorConfiguration.getType());
        try {
            outputTransportAdaptor.testConnection(outputTransportAdaptorConfiguration);
        } catch (TransportEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) throws TransportEventProcessingException {
        InputTransportAdaptor inputTransportAdaptor = (InputTransportAdaptor) this.transportAdaptorMap.get(inputTransportAdaptorConfiguration.getType());
        try {
            inputTransportAdaptor.unsubscribe(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (TransportEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public TransportAdaptorDto.TransportAdaptorType getTransportAdaptorSupportedType(String transportAdaptorType) {
        for(AbstractTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()){
            return abstractTransportAdaptor.getTransportAdaptorDto().getTransportAdaptorType();
        }

        return null;
    }
}
