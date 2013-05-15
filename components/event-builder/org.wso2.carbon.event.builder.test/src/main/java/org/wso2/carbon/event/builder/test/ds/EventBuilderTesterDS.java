/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.builder.test.ds;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.TupleInputEventBuilder;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.TupleInputMapping;
import org.wso2.carbon.event.builder.test.TestBasicEventListener;
import org.wso2.carbon.event.builder.test.util.TestEventBuilderServiceHolder;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * this class is used to get the Transport Adaptor service.
 *
 * @scr.component name="eventBuilder.test.component" immediate="true"
 * @scr.reference name="transportservice.service"
 * interface="org.wso2.carbon.transport.adaptor.core.TransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unsetTransportAdaptorService"
 * @scr.reference name="eventBuilder.service"
 * interface="org.wso2.carbon.event.builder.core.EventBuilderService" cardinality="1..1"
 * policy="dynamic" bind="setEventBuilderService" unbind="unsetEventBuilderService"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EventBuilderTesterDS {
    private static final Log log = LogFactory.getLog(EventBuilderTesterDS.class);

    protected void activate(ComponentContext context) {
        EventBuilderService eventBuilderService = TestEventBuilderServiceHolder.getInstance().getEventBuilderService();
        TransportAdaptorService transportAdaptorService = TestEventBuilderServiceHolder.getInstance().getTransportAdaptorService();
        ConfigurationContextService configurationContextService = TestEventBuilderServiceHolder.getInstance().getConfigurationContextService();
        AxisConfiguration axisConfiguration = configurationContextService.getClientConfigContext().getAxisConfiguration();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new TransportAdaptorConfiguration();
        InputTransportMessageConfiguration inputTransportMessageConfiguration = new InputTransportMessageConfiguration();

        String streamName = "org.wso2.phone.retail.store";
        String streamVersion = "1.2.0";

        configureInputTransportAdaptor(transportAdaptorService, axisConfiguration, streamName, streamVersion,
                inputTransportAdaptorConfiguration, inputTransportMessageConfiguration);
        configureEventBuilder(eventBuilderService, axisConfiguration, inputTransportMessageConfiguration);

        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(streamName, streamVersion);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Error creating stream definition with id " + streamName + ":" + streamVersion);
        }

        TestBasicEventListener testBasicEventListener = new TestBasicEventListener();
        try {
            eventBuilderService.subscribe(streamDefinition, testBasicEventListener, axisConfiguration);
            log.info("Successfully subscribed to event builder");
        } catch (EventBuilderConfigurationException e) {
            log.error("Error when subscribing to event builder:\n" + e);
        }
    }

    private void configureEventBuilder(EventBuilderService eventBuilderService, AxisConfiguration axisConfiguration, InputTransportMessageConfiguration inputTransportMessageConfiguration) {
        EventBuilderConfiguration<TupleInputMapping> eventBuilderConfiguration = new EventBuilderConfiguration<TupleInputMapping>();
        eventBuilderConfiguration.setInputTransportMessageConfiguration(inputTransportMessageConfiguration);
        try {
            eventBuilderService.addEventBuilder(new TupleInputEventBuilder(eventBuilderConfiguration), axisConfiguration);
            log.info("Successfully deployed event builder service tester");
        } catch (EventBuilderConfigurationException e) {
            log.error(e);
        }
    }

    private void configureInputTransportAdaptor(TransportAdaptorService transportAdaptorService,
                                                AxisConfiguration axisConfiguration, String streamName, String streamVersion,
                                                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration, InputTransportMessageConfiguration inputTransportMessageConfiguration) {
        if (inputTransportAdaptorConfiguration != null && inputTransportMessageConfiguration != null) {
            inputTransportAdaptorConfiguration.setName("localAgentBroker");
            inputTransportAdaptorConfiguration.setType("agent");
            inputTransportAdaptorConfiguration.addCommonAdaptorProperty("receiverURL", "tcp://localhost:76111");
            inputTransportAdaptorConfiguration.addCommonAdaptorProperty("authenticatorURL", "ssl://localhost:77111");
            inputTransportAdaptorConfiguration.addCommonAdaptorProperty("username", "admin");
            inputTransportAdaptorConfiguration.addCommonAdaptorProperty("password", "admin");


            inputTransportMessageConfiguration = new InputTransportMessageConfiguration();
            inputTransportMessageConfiguration.addInputMessageProperty("streamName", streamName);
            inputTransportMessageConfiguration.addInputMessageProperty("version", streamVersion);

            try {
                transportAdaptorService.subscribe(inputTransportAdaptorConfiguration, inputTransportMessageConfiguration, null, axisConfiguration);
            } catch (TransportAdaptorEventProcessingException e) {
                log.error("Error subscribing to transport adaptor:\n" + e);
            }
        } else {
            log.error("Cannot create input transport adaptor. Some parameters are null");
        }
    }

    protected void setEventBuilderService(EventBuilderService eventBuilderService) {
        TestEventBuilderServiceHolder.getInstance().registerEventBuilderService(eventBuilderService);
    }

    protected void unsetEventBuilderService(EventBuilderService eventBuilderService) {
        TestEventBuilderServiceHolder.getInstance().unregisterEventBuilderService(eventBuilderService);
    }

    protected void setTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        TestEventBuilderServiceHolder.getInstance().registerTransportAdaptorService(transportAdaptorService);
    }

    protected void unsetTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        TestEventBuilderServiceHolder.getInstance().unregisterTransportAdaptorService(transportAdaptorService);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TestEventBuilderServiceHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TestEventBuilderServiceHolder.getInstance().unregisterConfigurationContextService(configurationContextService);
    }
}
