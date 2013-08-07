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

package org.wso2.carbon.output.transport.adaptor.core;

import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;

import java.util.List;

/**
 * OSGI interface for the TransportAdaptor Service
 */

public interface OutputTransportAdaptorService {


    /**
     * this method returns all the available transport types. UI use this details to
     * show the types and the properties to be set to the user when creating the
     * transport objects.
     *
     * @return list of available types
     */
    List<OutputTransportAdaptorDto> getTransportAdaptors();

    /**
     * @return message DTO
     */
    MessageDto getTransportMessageDto(String transportAdaptorTypeName);

    /**
     * publishes the message using the given transport proxy to the given topic.
     *
     * @param outputTransportAdaptorConfiguration
     *               - Configuration Details of the transport
     * @param outputTransportMessageConfiguration
     *               - topic to publish
     * @param object - message to send
     */
    void publish(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
                 OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration,
                 Object object);

    /**
     * publish testConnection message using the given transport proxy.
     *
     * @param outputTransportAdaptorConfiguration
     *         - Configuration Details of the transport
     */
    void testConnection(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration);


    /**
     * This method returns the transport adaptor dto for a specific transport adaptor type
     *
     * @param transportAdaptorType
     * @return
     */
    OutputTransportAdaptorDto getTransportAdaptorDto(String transportAdaptorType);


}
