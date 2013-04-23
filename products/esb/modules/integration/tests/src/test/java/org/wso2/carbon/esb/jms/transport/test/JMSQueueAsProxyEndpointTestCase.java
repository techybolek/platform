/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.esb.jms.transport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.Utils;

import java.util.List;

public class JMSQueueAsProxyEndpointTestCase extends ESBIntegrationTest {
    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/jms/transport/jms_endpoint_proxy_service.xml");
    }

    @Test(groups = {"wso2.esb"}, description = "Test sending message to jms endpoint with pox format from proxy service")
    public void testJMSEndpointPox() throws Exception {
        AxisServiceClient client = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("JMS"), getProxyServiceURL("proxyWithJmsEndpointPox"), "getQuote");
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect("TestQueuePox");
            List<String> messageListInQueue = consumer.getMessages();
            for (String message : messageListInQueue) {
                Assert.assertNotNull(message);
                Assert.assertEquals(message, "<ns:getQuote xmlns:ns=\"http://services.samples\"><" +
                                             "ns:request><ns:symbol>JMS</ns:symbol></ns:request></ns:getQuote>"
                        , "Message Contains mismatched");
            }
        } finally {
            consumer.disconnect();
        }

    }

    @Test(groups = {"wso2.esb"}, description = "Test sending message to jms endpoint with soap11 format from proxy service")
    public void testJMSEndpointSoap11() throws Exception {
        AxisServiceClient client = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("JMS"), getProxyServiceURL("proxyWithJmsEndpointSoap11"), "getQuote");
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect("TestQueueSoap11");
            List<String> messageListInQueue = consumer.getMessages();
            for (String message : messageListInQueue) {
                Assert.assertNotNull(message);
                Assert.assertEquals(message, "<?xml version='1.0' encoding='UTF-8'?>" +
                                             "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                                             "<soapenv:Body><ns:getQuote xmlns:ns=\"http://services.samples\">" +
                                             "<ns:request><ns:symbol>JMS</ns:symbol></ns:request>" +
                                             "</ns:getQuote></soapenv:Body></soapenv:Envelope>"
                        , "Message Contains mismatched");
            }
        } finally {
            consumer.disconnect();
        }

    }

    @Test(groups = {"wso2.esb"}, description = "Test sending message to jms endpoint with soap12 format from proxy service")
    public void testJMSEndpointSoap12() throws Exception {
        AxisServiceClient client = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("JMS"), getProxyServiceURL("proxyWithJmsEndpointSoap12"), "getQuote");
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect("TestQueueSoap12");
            List<String> messageListInQueue = consumer.getMessages();
            for (String message : messageListInQueue) {

                Assert.assertNotNull(message);
                Assert.assertEquals(message, "<?xml version='1.0' encoding='UTF-8'?>" +
                                             "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
                                             "<soapenv:Body><ns:getQuote xmlns:ns=\"http://services.samples\">" +
                                             "<ns:request><ns:symbol>JMS</ns:symbol></ns:request>" +
                                             "</ns:getQuote></soapenv:Body></soapenv:Envelope>"
                        , "Message Contains mismatched");
            }
        } finally {
            consumer.disconnect();
        }

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
