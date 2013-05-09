package org.wso2.carbon.transport.adaptor.core;/*
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

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;

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
     *
     * @return message DTO
     */
    MessageDto getTransportMessageDto(String transportAdaptorType);

    /**
     * get all the names of the transport types
     *
     * @return - list of transport names
     */
    List<String> getTransportAdaptorNames();

     /**
     *
     * @param transportAdaptor name of the transport adaptor
     * @return input adaptor property list
     */
    List<Property> getInputAdaptorTransportProperties(String transportAdaptor);

    /**
     *
     * @param transportAdaptor name of the transport adaptor
     * @return output adaptor property list
     */
    List<Property> getOutputAdaptorTransportProperties(String transportAdaptor);

    /**
     *
     * @param transportAdaptor nam eof the transport adaptor
     * @return common adaptor property lists
     */
    List<Property> getCommonAdaptorTransportProperties(String transportAdaptor);

    /**
     * subscribe to a particular transport configuration. When the TransportAdaptor receives the
     * message it send that to the user through the listener interface.
     *
     * @param inputTransportAdaptorConfiguration
     *                          - Configuration details of the transport
     * @param inputTransportMessageConfiguration         - topic to subscribe
     * @param transportListener - listener interface to notify
     * @throws org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException
     *          - if problem happen when subscribing
     */
    String subscribe(InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                     InputTransportMessageConfiguration inputTransportMessageConfiguration,
                     TransportListener transportListener,
                     AxisConfiguration axisConfiguration) throws TransportEventProcessingException;


    /**
     * publishes the message using the given transport proxy to the given topic.
     *
     * @param OutputTransportAdaptorConfiguration
     *                  - Configuration Details of the transport
     * @param outputTransportMessageConfiguration - topic to publish
     * @param object    - message to send
     * @throws org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException
     *          - if problem happen when publishing
     */
    void publish(OutputTransportAdaptorConfiguration OutputTransportAdaptorConfiguration,
                 OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                 Object object) throws TransportEventProcessingException;

    /**
     * publish testConnection message using the given transport proxy.
     *
     * @param outputTransportAdaptorConfiguration
     *         - Configuration Details of the transport
     * @throws org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException
     *          - if problem happen when publishing
     */
    void testConnection(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) throws TransportEventProcessingException;

    /**
     * un subscribes from the transport.
     *
     * @param inputTransportMessageConfiguration         - topic name to which previously subscribed
     * @param inputTransportAdaptorConfiguration
     *                          - transport configuration to be used
     * @param axisConfiguration - acis configuration
     * @throws TransportEventProcessingException
     *
     */
    void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                     InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                     AxisConfiguration axisConfiguration, String subscriptionId)
            throws TransportEventProcessingException;



    TransportAdaptorDto.TransportAdaptorType getTransportAdaptorSupportedType(String transportAdaptorType);
}
