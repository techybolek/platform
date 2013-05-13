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

package org.wso2.carbon.transport.adaptor.agent.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.transport.adaptor.agent.AgentTransportAdaptorFactory;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorRegistrationService;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorConfigException;


/**
 * @scr.component name="agentTransportAdaptorService.component" immediate="true"
 * @scr.reference name="agentserverservice.service"
 * interface="org.wso2.carbon.databridge.core.DataBridgeSubscriberService" cardinality="1..1"
 * policy="dynamic" bind="setDataBridgeSubscriberService" unbind="unSetDataBridgeSubscriberService"
 * @scr.reference name="agentservice.service"
 * interface="org.wso2.carbon.databridge.agent.thrift.Agent" cardinality="1..1"
 * policy="dynamic" bind="setAgent" unbind="unSetAgent"
 * @scr.reference name="transport.adaptor.service"
 * interface="org.wso2.carbon.transport.adaptor.core.TransportAdaptorRegistrationService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorRegistrationService" unbind="unSetTransportAdaptorRegistrationService"
 */


public class AgentTransportAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(AgentTransportAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {

            AgentTransportAdaptorServiceValueHolder.getAgentTransportRegistrationService().registerTransportAdaptor(AgentTransportAdaptorFactory.class.getName());
            log.info("Successfully deployed the agent transport service");
        } catch (RuntimeException e) {
            log.error("Can not create transport service ", e);
        } catch (TransportAdaptorConfigException e) {
            log.error("Transport adaptor configuration error occurred ");
        }
    }

    protected void setTransportAdaptorRegistrationService(
            TransportAdaptorRegistrationService transportAdaptorRegistrationService) {
        AgentTransportAdaptorServiceValueHolder.registerAgentTransportRegistrationService(transportAdaptorRegistrationService);
    }

    protected void unSetTransportAdaptorRegistrationService(
            TransportAdaptorRegistrationService transportAdaptorRegistrationServicee) {

    }

    protected void setDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {
        AgentTransportAdaptorServiceValueHolder.registerDataBridgeSubscriberService(dataBridgeSubscriberService);
    }

    protected void unSetDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {

    }

    protected void setAgent(Agent agent) {
        AgentTransportAdaptorServiceValueHolder.registerAgent(agent);
    }

    protected void unSetAgent(Agent agent) {

    }

}
