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

package org.wso2.carbon.output.transport.adaptor.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.output.transport.adaptor.core.AbstractOutputTransportAdaptor;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorDto;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.exception.OutputTransportAdaptorEventProcessingException;
import org.wso2.carbon.output.transport.adaptor.core.internal.ds.OutputTransportAdaptorServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransportAdaptor service implementation.
 */
public class CarbonOutputTransportAdaptorService implements OutputTransportAdaptorService {

    private static Log log = LogFactory.getLog(CarbonOutputTransportAdaptorService.class);
    private Map<String, AbstractOutputTransportAdaptor> transportAdaptorMap;

    public CarbonOutputTransportAdaptorService() {
        this.transportAdaptorMap = new ConcurrentHashMap();
    }

    public void registerTransportAdaptor(
            AbstractOutputTransportAdaptor abstractOutputTransportAdaptor) {
        OutputTransportAdaptorDto outputTransportAdaptorDto = abstractOutputTransportAdaptor.getOutputTransportAdaptorDto();
        this.transportAdaptorMap.put(outputTransportAdaptorDto.getTransportAdaptorTypeName(), abstractOutputTransportAdaptor);
        OutputTransportAdaptorServiceValueHolder.getComponentContext().getBundleContext().registerService(AbstractOutputTransportAdaptor.class.getName(), abstractOutputTransportAdaptor, null);
    }

    public void unRegisterTransportAdaptor(
            AbstractOutputTransportAdaptor abstractOutputTransportAdaptor) {
        OutputTransportAdaptorDto outputTransportAdaptorDto = abstractOutputTransportAdaptor.getOutputTransportAdaptorDto();
        this.transportAdaptorMap.remove(outputTransportAdaptorDto.getTransportAdaptorTypeName());
        //OutputTransportAdaptorServiceValueHolder.getComponentContext().getBundleContext().ungetService(AbstractOutputTransportAdaptor.class.getName(),abstractOutputTransportAdaptor,null);
    }


    @Override
    public List<OutputTransportAdaptorDto> getTransportAdaptors() {
        List<OutputTransportAdaptorDto> outputTransportAdaptorDtos = new ArrayList<OutputTransportAdaptorDto>();
        for (AbstractOutputTransportAdaptor abstractTransportAdaptor : this.transportAdaptorMap.values()) {
            outputTransportAdaptorDtos.add(abstractTransportAdaptor.getOutputTransportAdaptorDto());
        }
        return outputTransportAdaptorDtos;
    }

    @Override
    public MessageDto getTransportMessageDto(String transportAdaptorTypeName) {

        for (AbstractOutputTransportAdaptor abstractOutputTransportAdaptor : this.transportAdaptorMap.values()) {
            if (abstractOutputTransportAdaptor.getOutputTransportAdaptorDto().getTransportAdaptorTypeName().equals(transportAdaptorTypeName)) {
                return abstractOutputTransportAdaptor.getMessageDto();
            }
        }
        return null;
    }

    @Override
    public void publish(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
                        OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration,
                        Object object, int tenantId) {
        AbstractOutputTransportAdaptor outputTransportAdaptor = this.transportAdaptorMap.get(outputTransportAdaptorConfiguration.getType());
        try {
            outputTransportAdaptor.publishCall(outputTransportMessageConfiguration, object, outputTransportAdaptorConfiguration,tenantId);
        } catch (OutputTransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new OutputTransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        AbstractOutputTransportAdaptor outputTransportAdaptor = this.transportAdaptorMap.get(outputTransportAdaptorConfiguration.getType());
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            outputTransportAdaptor.testConnection(outputTransportAdaptorConfiguration,tenantId);
        } catch (OutputTransportAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new OutputTransportAdaptorEventProcessingException(e.getMessage(), e);
        }
    }


    @Override
    public OutputTransportAdaptorDto getTransportAdaptorDto(String transportAdaptorType) {

        AbstractOutputTransportAdaptor abstractOutputTransportAdaptor = transportAdaptorMap.get(transportAdaptorType);
        if (abstractOutputTransportAdaptor != null) {
            return abstractOutputTransportAdaptor.getOutputTransportAdaptorDto();
        }
        return null;
    }


}
