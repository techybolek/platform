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

package org.wso2.carbon.input.transport.adaptor.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorDto;
import org.wso2.carbon.input.transport.adaptor.core.AbstractInputTransportAdaptor;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.core.internal.ds.InputTransportAdaptorServiceValueHolder;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransportAdaptor service implementation.
 */
public class CarbonInputTransportAdaptorService implements InputTransportAdaptorService {

    private static Log log = LogFactory.getLog(CarbonInputTransportAdaptorService.class);
    private Map<String, AbstractInputTransportAdaptor> transportAdaptorMap;

    public CarbonInputTransportAdaptorService() {
        this.transportAdaptorMap = new ConcurrentHashMap();
    }

    public void registerTransportAdaptor(AbstractInputTransportAdaptor abstractInputTransportAdaptor) {
        InputTransportAdaptorDto inputTransportAdaptorDto = abstractInputTransportAdaptor.getInputTransportAdaptorDto();
        this.transportAdaptorMap.put(inputTransportAdaptorDto.getTransportAdaptorTypeName(), abstractInputTransportAdaptor);
        InputTransportAdaptorServiceValueHolder.getComponentContext().getBundleContext().registerService(AbstractInputTransportAdaptor.class.getName(), abstractInputTransportAdaptor,null);
    }

    public void unRegisterTransportAdaptor(AbstractInputTransportAdaptor abstractInputTransportAdaptor) {
        InputTransportAdaptorDto inputTransportAdaptorDto = abstractInputTransportAdaptor.getInputTransportAdaptorDto();
        this.transportAdaptorMap.remove(inputTransportAdaptorDto.getTransportAdaptorTypeName());
    }


    @Override
    public List<InputTransportAdaptorDto> getTransportAdaptors() {
        List<InputTransportAdaptorDto> inputTransportAdaptorDtos = new ArrayList<InputTransportAdaptorDto>();
        for (AbstractInputTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()) {
            inputTransportAdaptorDtos.add(abstractTransportAdaptor.getInputTransportAdaptorDto());
        }
        return inputTransportAdaptorDtos;
    }

    @Override
    public MessageDto getTransportMessageDto(String transportAdaptorTypeName) {

        for (AbstractInputTransportAdaptor abstractInputTransportAdaptor : this.transportAdaptorMap.values()) {
            if (abstractInputTransportAdaptor.getInputTransportAdaptorDto().getTransportAdaptorTypeName().equals(transportAdaptorTypeName)) {
                return abstractInputTransportAdaptor.getMessageDto();
            }
        }
        return null;
    }

    @Override
    public String subscribe(InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration,
                            InputTransportAdaptorListener inputTransportAdaptorListener,
                            AxisConfiguration axisConfiguration) {
        AbstractInputTransportAdaptor inputTransportAdaptor = this.transportAdaptorMap.get(inputTransportAdaptorConfiguration.getType());

        try {
            return inputTransportAdaptor.subscribe(inputTransportAdaptorMessageConfiguration, inputTransportAdaptorListener, inputTransportAdaptorConfiguration, axisConfiguration);
        } catch (InputTransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new InputTransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe(InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration,
                            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        AbstractInputTransportAdaptor abstractInputTransportAdaptor =  this.transportAdaptorMap.get(inputTransportAdaptorConfiguration.getType());
        try {
            abstractInputTransportAdaptor.unsubscribe(inputTransportAdaptorMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (InputTransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new InputTransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public InputTransportAdaptorDto getTransportAdaptorDto(String transportAdaptorType) {

        AbstractInputTransportAdaptor abstractInputTransportAdaptor = transportAdaptorMap.get(transportAdaptorType);
        if (abstractInputTransportAdaptor != null) {
            return abstractInputTransportAdaptor.getInputTransportAdaptorDto();
        }
        return null;
    }


}
