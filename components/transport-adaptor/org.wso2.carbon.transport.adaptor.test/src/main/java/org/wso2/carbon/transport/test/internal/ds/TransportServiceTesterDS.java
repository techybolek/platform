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

package org.wso2.carbon.transport.test.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.test.TestTransportAdaptorAdaptorListener;
import org.wso2.carbon.transport.test.internal.util.TransportAdaptorHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;

/**
 * this class is used to get the Transport Adaptor service.
 *
 * @scr.component name="transport.test.component" immediate="true"
 * @scr.reference name="transportservice.service"
 * interface="org.wso2.carbon.transport.adaptor.core.TransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unSetTransportAdaptorService"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class TransportServiceTesterDS {
    private static final Log log = LogFactory.getLog(TransportServiceTesterDS.class);

    protected void activate(ComponentContext context) {

        try {

        } catch (Throwable t) {
            log.error("Cannot deploy transport adaptor service tester", t);
        }
        TransportAdaptorService transportAdaptorService = TransportAdaptorHolder.getInstance().getTransportAdaptorService();

//        registerWso2EventReceiver(transportAdaptorService);
        registerJMS(transportAdaptorService);


        log.info("successfully deployed transport adaptor service tester");

    }

    private void registerWso2EventReceiver(TransportAdaptorService transportAdaptorService) {
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new TransportAdaptorConfiguration();
        inputTransportAdaptorConfiguration.setName("localAgentBroker");
        inputTransportAdaptorConfiguration.setType("wso2eventreceiver");

//        inputTransportAdaptorConfiguration.setType("agent");
//        inputTransportAdaptorConfiguration.addCommonAdaptorProperty("receiverURL", "tcp://localhost:76111");
//        inputTransportAdaptorConfiguration.addCommonAdaptorProperty("authenticatorURL", "ssl://localhost:77111");
//        inputTransportAdaptorConfiguration.addCommonAdaptorProperty("username", "admin1");
//        inputTransportAdaptorConfiguration.addCommonAdaptorProperty("password", "admin1");


        InputTransportMessageConfiguration inputTransportMessageConfiguration = new InputTransportMessageConfiguration();
        inputTransportMessageConfiguration.addInputMessageProperty("stream", "org.wso2.phone.retail.store");
        inputTransportMessageConfiguration.addInputMessageProperty("version", "1.2.0");

        try {
            transportAdaptorService.subscribe(inputTransportAdaptorConfiguration, inputTransportMessageConfiguration, new TestTransportAdaptorAdaptorListener("wso2eventreceiver", "org.wso2.phone.retail.store:1.2.0"), TransportAdaptorHolder.getInstance().getConfigurationContextService().getServerConfigContext().getAxisConfiguration());
        } catch (TransportAdaptorEventProcessingException e) {
            log.error("Error occurred when subscribing " + e);
        }
    }

    private void registerJMS(TransportAdaptorService transportAdaptorService) {
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new TransportAdaptorConfiguration();
        inputTransportAdaptorConfiguration.setName("jmsTransportAdaptor");
        inputTransportAdaptorConfiguration.setType("jms");

        Map<String, String> transportAdaptorCommonPropertyList = new HashMap<String, String>();
        transportAdaptorCommonPropertyList.put("java.naming.security.principal", "admin");
        transportAdaptorCommonPropertyList.put("java.naming.provider.url", "tcp://localhost:61616");
        transportAdaptorCommonPropertyList.put("java.naming.security.credentials", "admin");
        transportAdaptorCommonPropertyList.put("transport.jms.ConnectionFactoryJNDIName", "TopicConnectionFactory");
        transportAdaptorCommonPropertyList.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        transportAdaptorCommonPropertyList.put("transport.jms.DestinationType", "topic");

        inputTransportAdaptorConfiguration.setCommonProperties(transportAdaptorCommonPropertyList);

        InputTransportMessageConfiguration inputTransportMessageConfiguration = new InputTransportMessageConfiguration();
        inputTransportMessageConfiguration.addInputMessageProperty("transport.jms.Destination", "org.wso2.phone.retail.store");
        inputTransportMessageConfiguration.addInputMessageProperty("version", "1.2.0");

        try {
            transportAdaptorService.subscribe(inputTransportAdaptorConfiguration, inputTransportMessageConfiguration, new TestTransportAdaptorAdaptorListener("wso2eventreceiver", "org.wso2.phone.retail.store:1.2.0"), TransportAdaptorHolder.getInstance().getConfigurationContextService().getServerConfigContext().getAxisConfiguration());
        } catch (TransportAdaptorEventProcessingException e) {
            log.error("Error occurred when subscribing " + e);
        }
    }

    protected void setTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        TransportAdaptorHolder.getInstance().registerTransportService(transportAdaptorService);
    }

    protected void unSetTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        TransportAdaptorHolder.getInstance().unRegisterTransportService(transportAdaptorService);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TransportAdaptorHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

        TransportAdaptorHolder.getInstance().unRegisterConfigurationContextService(configurationContextService);

    }
}
