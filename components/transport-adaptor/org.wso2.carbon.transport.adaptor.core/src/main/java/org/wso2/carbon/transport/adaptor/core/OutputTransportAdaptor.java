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

import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;

import java.util.List;

public interface OutputTransportAdaptor {

    /**
     *
     * @return output adaptor configuration property list
     */
    List<Property> getOutAdaptorConfig();

    /**
     *
     * @return output message configuration property list
     */
    List<Property> getOutMessageConfig();

    /**
     * publish a message to a given connection with the transport configuration.
     *
     * @param outputTransportMessageConfiguration                    - topic name to publish messages
     * @param message                       - message to send
     * @param outputTransportConfiguration - transport adaptor configuration to be used
     * @throws TransportEventProcessingException
     *          - if the message can not publish
     */
    void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                 Object message,
                 OutputTransportAdaptorConfiguration outputTransportConfiguration) throws TransportEventProcessingException;

    /**
     * publish test message to check the connection with the transport configuration.
     *
     * @param outputTransportConfiguration - transport configuration to be used
     * @throws TransportEventProcessingException
     *          - if the message can not publish
     */
    void testConnection(OutputTransportAdaptorConfiguration outputTransportConfiguration) throws TransportEventProcessingException;


}
