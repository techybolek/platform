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
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;

import java.util.List;

public interface InputTransportAdaptor {

    /**
     * @return list of input adaptor configuration properties
     */
    List<Property> getInputAdaptorProperties();

    /**
     * @return list of input message configuration properties
     */
    List<Property> getInputMessageProperties();

    /**
     * subscribe to the connection specified in the transport configuration.
     *
     * @param inputTransportMessageConfiguration
     *                                 - topic name to subscribe
     * @param transportAdaptorListener - transport type will invoke this when it receive events
     * @param inputTransportAdaptorConfiguration
     *                                 - transport adaptor configuration details
     */
    String subscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                     TransportAdaptorListener transportAdaptorListener,
                     InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                     AxisConfiguration axisConfiguration);

    /**
     * this method unsubscribes the subscription from the transport.
     *
     * @param inputTransportMessageConfiguration
     *         - topic name
     * @param inputTransportAdaptorConfiguration
     *         - transport adaptor configuration
     */
    void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration,
                     InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                     AxisConfiguration axisConfiguration, String subscriptionId);


}
