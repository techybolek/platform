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


import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;

import java.util.List;

public interface OutputTransportAdaptor {

    /**
     * @return output adaptor configuration property list
     */
    List<Property> getOutputAdaptorProperties();

    /**
     * @return output message configuration property list
     */
    List<Property> getOutputMessageProperties();

    /**
     * publish a message to a given connection with the transport configuration.
     *
     * @param outputTransportMessageConfiguration
     *                - topic name to publish messages
     * @param message - message to send
     * @param outputTransportAdaptorConfiguration
     *                - transport adaptor configuration to be used
     */
    void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                 Object message,
                 OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration);

    /**
     * publish test message to check the connection with the transport configuration.
     *
     * @param outputTransportAdaptorConfiguration
     *         - transport configuration to be used
     */
    void testConnection(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration);


}
