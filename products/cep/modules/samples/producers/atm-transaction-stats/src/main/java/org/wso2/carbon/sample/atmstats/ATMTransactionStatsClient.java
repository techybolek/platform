package org.wso2.carbon.sample.atmstats;/*
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisServer;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.broker.BrokerClientException;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ATMTransactionStatsClient {

    private AxisServer axisServer;
    private BrokerClient brokerClient;

    private static final List<String> xmlMsgs = new ArrayList<String>();

    static {
        String xmlElement1 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                             " <atmdata:ATMTransactionStat>\n" +
                             " <atmdata:CardNo>ED936784773</atmdata:CardNo>\n" +
                             " <atmdata:CardHolderName>Mohan</atmdata:CardHolderName>\n" +
                             " <atmdata:AmountWithdrawed>80</atmdata:AmountWithdrawed>\n" +
                             " <atmdata:TransactionTime>PDT 11:00</atmdata:TransactionTime>\n" +
                             " <atmdata:Location>Newyork</atmdata:Location>\n" +
                             " <atmdata:BankName>Bank of Newyork</atmdata:BankName>\n" +
                             " <atmdata:AccountNo>8957836745</atmdata:AccountNo>\n" +
                             " <atmdata:CardHolderMobile>9667339388</atmdata:CardHolderMobile>\n" +
                             " </atmdata:ATMTransactionStat>\n" +
                             " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement1);

        String xmlElement2 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                             " <atmdata:ATMTransactionStat>\n" +
                             " <atmdata:CardNo>BC78946623</atmdata:CardNo>\n" +
                             " <atmdata:CardHolderName>John</atmdata:CardHolderName>\n" +
                             " <atmdata:AmountWithdrawed>1000</atmdata:AmountWithdrawed>\n" +
                             " <atmdata:TransactionTime>PDT 01:00</atmdata:TransactionTime>\n" +
                             " <atmdata:Location>California</atmdata:Location>\n" +
                             " <atmdata:BankName>Bank of Wonder</atmdata:BankName>\n" +
                             " <atmdata:AccountNo>PDT 12:00</atmdata:AccountNo>\n" +
                             " <atmdata:CardHolderMobile>94729327932</atmdata:CardHolderMobile>\n" +
                             " </atmdata:ATMTransactionStat>\n" +
                             " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement2);

        String xmlElement3 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                             " <atmdata:ATMTransactionStat>\n" +
                             " <atmdata:CardNo>GH679893232</atmdata:CardNo>\n" +
                             " <atmdata:CardHolderName>Tom</atmdata:CardHolderName>\n" +
                             " <atmdata:AmountWithdrawed>900</atmdata:AmountWithdrawed>\n" +
                             " <atmdata:TransactionTime>PDT 02:00</atmdata:TransactionTime>\n" +
                             " <atmdata:Location>Texas</atmdata:Location>\n" +
                             " <atmdata:BankName>Bank of Greenwich</atmdata:BankName>\n" +
                             " <atmdata:AccountNo>783233422</atmdata:AccountNo>\n" +
                             " <atmdata:CardHolderMobile>98434345532</atmdata:CardHolderMobile>\n" +
                             " </atmdata:ATMTransactionStat>\n" +
                             " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement3);

        String xmlElement4 = "<atmdata:ATMTransactionStatsStream xmlns:atmdata=\"http://samples.wso2.org/\">\n" +
                             " <atmdata:ATMTransactionStat>\n" +
                             " <atmdata:CardNo>ED936784773</atmdata:CardNo>\n" +
                             " <atmdata:CardHolderName>Mohan</atmdata:CardHolderName>\n" +
                             " <atmdata:AmountWithdrawed>15000</atmdata:AmountWithdrawed>\n" +
                             " <atmdata:TransactionTime>PDT 03:00</atmdata:TransactionTime>\n" +
                             " <atmdata:Location>Newyork</atmdata:Location>\n" +
                             " <atmdata:BankName>Bank of Newyork</atmdata:BankName>\n" +
                             " <atmdata:AccountNo>8957836745</atmdata:AccountNo>\n" +
                             " <atmdata:CardHolderMobile>9667339388</atmdata:CardHolderMobile>\n" +
                             " </atmdata:ATMTransactionStat>\n" +
                             " </atmdata:ATMTransactionStatsStream>";

        xmlMsgs.add(xmlElement4);

    }


    public void start() {
        try {
            KeyStoreUtil.setTrustStoreParams();
            this.axisServer = new AxisServer();
            //this.axisServer.deployService(EventSinkService.class.getName());
            this.brokerClient = new BrokerClient("https://localhost:9444/services/EventBrokerService", "admin", "admin");
            // give time to start the simple http server
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        } catch (AxisFault axisFault) {
            System.out.println("Can not start the server");
        } catch (AuthenticationExceptionException e) {
            e.printStackTrace();
        }
    }

    public String subscribe() {
        // set the properties for ssl
        try {
            return this.brokerClient.subscribe("foo/bar", "http://localhost:9763/services/LogService/log");
        } catch (BrokerClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void publish() throws XMLStreamException {
        try {
            for (int i = 0, msgsLength = xmlMsgs.size(); i < msgsLength; i++) {
                String xmlMessage = xmlMsgs.get(i);
                XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                        xmlMessage.getBytes()));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement OMMessage = builder.getDocumentElement();
                this.brokerClient.publish("ATMTransactionStats", OMMessage);
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    public void unsubscribe(String subscriptionID) {
        try {
            this.brokerClient.unsubscribe(subscriptionID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            this.axisServer.stop();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    public static void main(String[] args) throws XMLStreamException {
        ATMTransactionStatsClient ATMTransactionStatsClient = new ATMTransactionStatsClient();
        ATMTransactionStatsClient.start();
        String subscriptionId = ATMTransactionStatsClient.subscribe();
        ATMTransactionStatsClient.publish();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        ATMTransactionStatsClient.unsubscribe(subscriptionId);
        ATMTransactionStatsClient.stop();
    }

}
