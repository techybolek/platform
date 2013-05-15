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

package org.wso2.carbon.transport.adaptor.wso2eventreceiver.ds;

import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorRegistrationService;

/**
 * common place to hold some OSGI bundle references.
 */
public final class WSO2EventReceiverTransportAdaptorServiceValueHolder {

    private static DataBridgeSubscriberService dataBridgeSubscriberService;
    private static Agent agent;
    private static TransportAdaptorRegistrationService agentTransportAdaptorRegistrationService;

    private WSO2EventReceiverTransportAdaptorServiceValueHolder() {
    }

    public static void registerAgentTransportRegistrationService(
            TransportAdaptorRegistrationService agentTransportAdaptorRegistrationService) {
        WSO2EventReceiverTransportAdaptorServiceValueHolder.agentTransportAdaptorRegistrationService = agentTransportAdaptorRegistrationService;
    }

    public static TransportAdaptorRegistrationService getAgentTransportRegistrationService() {
        return WSO2EventReceiverTransportAdaptorServiceValueHolder.agentTransportAdaptorRegistrationService;
    }

    public static void registerDataBridgeSubscriberService(
            DataBridgeSubscriberService agentServer) {
        WSO2EventReceiverTransportAdaptorServiceValueHolder.dataBridgeSubscriberService = agentServer;
    }

    public static DataBridgeSubscriberService getDataBridgeSubscriberService() {
        return dataBridgeSubscriberService;
    }

    public static void registerAgent(Agent agent) {
        WSO2EventReceiverTransportAdaptorServiceValueHolder.agent = agent;
    }

    public static Agent getAgent() {
        return agent;
    }
}
