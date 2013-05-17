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

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;

import java.util.List;

/**
 * OSGI interface for the TransportAdaptor Service
 */

public interface TransportAdaptorService {


    /**
     * this method returns all the available transport types. UI use this details to
     * show the types and the properties to be set to the user when creating the
     * transport objects.
     *
     * @return list of available types
     */
    List<TransportAdaptorDto> getTransportAdaptors();

    /**
     * @return message DTO
     */
    MessageDto getTransportMessageDto(String transportAdaptorTypeName);

    /**
     * subscribe to a particular transport configuration. When the TransportAdaptor receives the
     * message it send that to the user through the listener interface.
     *
     * @param inputTransportAdaptorConfiguration
     *                                 - Configuration details of the transport
     * @param inputTransportMessageConfiguration
     *                                 - topic to subscribe
     * @param transportAdaptorListener - listener interface to notify
     */
    String subscribe(InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                     InputTransportMessageConfiguration inputTransportMessageConfiguration,
                     TransportAdaptorListener transportAdaptorListener,
                     AxisConfiguration axisConfiguration);


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
                 OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                 Object object);

    /**
     * publish testConnection message using the given transport proxy.
     *
     * @param outputTransportAdaptorConfiguration
     *         - Configuration Details of the transport
     */
    void testConnection(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration);

    /**
     * un subscribes from the transport.
     *
     * @param inputTransportMessageConfiguration
     *                          - topic name to which previously subscribed
     * @param inputTransportAdaptorConfiguration
     *                          - transport configuration to be used
     * @param axisConfiguration - acis configuration
     */
    void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                     InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                     AxisConfiguration axisConfiguration, String subscriptionId);


    /**
     * This method returns the transport adaptor dto for a specific transport adaptor type
     *
     * @param transportAdaptorType
     * @return
     */
    TransportAdaptorDto getTransportAdaptorDto(String transportAdaptorType);


}
