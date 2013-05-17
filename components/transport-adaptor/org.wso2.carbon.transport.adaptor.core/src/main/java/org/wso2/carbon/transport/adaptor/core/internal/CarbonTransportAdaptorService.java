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
import org.wso2.carbon.transport.adaptor.core.AbstractTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.OutputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;

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

    public CarbonTransportAdaptorService() {
        this.transportAdaptorMap = new ConcurrentHashMap();
    }

    public void registerTransportAdaptor(AbstractTransportAdaptor abstractTransportAdaptor) {
        TransportAdaptorDto transportAdaptorDto = abstractTransportAdaptor.getTransportAdaptorDto();
        this.transportAdaptorMap.put(transportAdaptorDto.getTransportAdaptorTypeName(), abstractTransportAdaptor);
    }


    @Override
    public List<TransportAdaptorDto> getTransportAdaptors() {
        List<TransportAdaptorDto> transportAdaptorDtos = new ArrayList<TransportAdaptorDto>();
        for (AbstractTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()) {
            transportAdaptorDtos.add(abstractTransportAdaptor.getTransportAdaptorDto());
        }
        return transportAdaptorDtos;
    }

    @Override
    public MessageDto getTransportMessageDto(String transportAdaptorTypeName) {

        for (AbstractTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()) {
            if (abstractTransportAdaptor.getTransportAdaptorDto().getTransportAdaptorTypeName().equals(transportAdaptorTypeName)) {
                return abstractTransportAdaptor.getMessageDto();
            }
        }
        return null;
    }

    @Override
    public String subscribe(InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            TransportAdaptorListener transportAdaptorListener,
                            AxisConfiguration axisConfiguration) {
        InputTransportAdaptor inputTransportAdaptor = (InputTransportAdaptor) this.transportAdaptorMap.get(inputTransportAdaptorConfiguration.getType());

        try {
            return inputTransportAdaptor.subscribe(inputTransportMessageConfiguration, transportAdaptorListener, inputTransportAdaptorConfiguration, axisConfiguration);
        } catch (TransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void publish(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
                        OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object object) {

        OutputTransportAdaptor outputTransportAdaptor = (OutputTransportAdaptor) this.transportAdaptorMap.get(outputTransportAdaptorConfiguration.getType());
        try {
            outputTransportAdaptor.publish(outputTransportMessageConfiguration, object, outputTransportAdaptorConfiguration);
        } catch (TransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        OutputTransportAdaptor outputTransportAdaptor = (OutputTransportAdaptor) this.transportAdaptorMap.get(outputTransportAdaptorConfiguration.getType());
        try {
            outputTransportAdaptor.testConnection(outputTransportAdaptorConfiguration);
        } catch (TransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        InputTransportAdaptor inputTransportAdaptor = (InputTransportAdaptor) this.transportAdaptorMap.get(inputTransportAdaptorConfiguration.getType());
        try {
            inputTransportAdaptor.unsubscribe(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (TransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new TransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public TransportAdaptorDto getTransportAdaptorDto(String transportAdaptorType) {

        AbstractTransportAdaptor abstractTransportAdaptor = transportAdaptorMap.get(transportAdaptorType);
        if (abstractTransportAdaptor != null) {
            return abstractTransportAdaptor.getTransportAdaptorDto();
        }
        return null;
    }
}
