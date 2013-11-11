/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.sample.stockquote;

import javax.jms.*;
import javax.naming.NamingException;

public class JsonStockQuoteClient {
    private static final String[] jsonMsgs = new String[]{
            "{StockQuoteEvent:" +
                    "{StockSymbol:LNKD," +
                    "LastTradeAmount:240.36," +
                    "StockChange:0.05," +
                    "OpenAmount:245.05," +
                    "DayHigh:260.46," +
                    "DayLow:230.01," +
                    "StockVolume:20452658," +
                    "PrevCls:240.31," +
                    "ChangePercent:0.20," +
                    "FiftyTwoWeekRange:\"220.73 - 271.58\"," +
                    "EarnPerShare:2.326," +
                    "PE:10.88," +
                    "CompanyName:\"LinkedIn Corp\"," +
                    "QuoteError:false" +
                    "}" +
                    "}",
            "{StockQuoteEvent: " +
                    "{ StockSymbol: FB," +
                    "LastTradeAmount: 41.36, " +
                    "StockChange: 0.15," +
                    "OpenAmount: 40.05," +
                    "DayHigh: 47.46," +
                    "DayLow: 39.36," +
                    "StockVolume: 20502658," +
                    "PrevCls: 41.31," +
                    "ChangePercent: 0.20," +
                    "FiftyTwoWeekRange: \"35.73 - 51.58\"," +
                    "EarnPerShare: -1.326," +
                    "PE: 12.88," +
                    "CompanyName: \"Facebook, Inc\"," +
                    "QuoteError: false" +
                    "}" +
                    "}",
            "{StockQuoteEvent:" +
                    "{StockSymbol:LNKD," +
                    "LastTradeAmount:245.36," +
                    "StockChange:0.05," +
                    "OpenAmount:245.05," +
                    "DayHigh:260.46," +
                    "DayLow:230.01," +
                    "StockVolume:20457000," +
                    "PrevCls:245.46," +
                    "ChangePercent:0.20," +
                    "FiftyTwoWeekRange:\"220.73 - 271.58\"," +
                    "EarnPerShare:2.326," +
                    "PE:10.88," +
                    "CompanyName:\"LinkedIn Corp\"," +
                    "QuoteError:false" +
                    "}" +
                    "}",
            "{StockQuoteEvent:" +
                    "{StockSymbol:LNKD," +
                    "LastTradeAmount:245.60," +
                    "StockChange:0.05," +
                    "OpenAmount:245.05," +
                    "DayHigh:260.46," +
                    "DayLow:230.01," +
                    "StockVolume:20457658," +
                    "PrevCls:245.36," +
                    "ChangePercent:0.20," +
                    "FiftyTwoWeekRange:\"220.73 - 271.58\"," +
                    "EarnPerShare:2.326," +
                    "PE:10.88," +
                    "CompanyName:\"LinkedIn Corp\"," +
                    "QuoteError:false" +
                    "}" +
                    "}",
            "{StockQuoteEvent: " +
                    "{ StockSymbol: FB," +
                    "LastTradeAmount: 49.36, " +
                    "StockChange: 0.15," +
                    "OpenAmount: 40.05," +
                    "DayHigh: 47.46," +
                    "DayLow: 39.36," +
                    "StockVolume: 20502658," +
                    "PrevCls: 41.36," +
                    "ChangePercent: 0.20," +
                    "FiftyTwoWeekRange: \"35.73 - 51.58\"," +
                    "EarnPerShare: -1.326," +
                    "PE: 12.88," +
                    "CompanyName: \"Facebook, Inc\"," +
                    "QuoteError: false" +
                    "}" +
                    "}",
            "{StockQuoteEvent: " +
                    "{ StockSymbol: FB," +
                    "LastTradeAmount: 55.36, " +
                    "StockChange: 0.15," +
                    "OpenAmount: 40.05," +
                    "DayHigh: 55.36," +
                    "DayLow: 39.36," +
                    "StockVolume: 20502658," +
                    "PrevCls: 49.36," +
                    "ChangePercent: 0.20," +
                    "FiftyTwoWeekRange: \"35.73 - 55.36\"," +
                    "EarnPerShare: -1.326," +
                    "PE: 12.88," +
                    "CompanyName: \"Facebook, Inc\"," +
                    "QuoteError: false" +
                    "}" +
                    "}",
            "{StockQuoteEvent:" +
                    "{StockSymbol:LNKD," +
                    "LastTradeAmount:247.01," +
                    "StockChange:0.05," +
                    "OpenAmount:245.05," +
                    "DayHigh:260.46," +
                    "DayLow:230.01," +
                    "StockVolume:20452658," +
                    "PrevCls:247.00," +
                    "ChangePercent:0.20," +
                    "FiftyTwoWeekRange:\"220.73 - 271.58\"," +
                    "EarnPerShare:2.326," +
                    "PE:10.88," +
                    "CompanyName:\"LinkedIn Corp\"," +
                    "QuoteError:false" +
                    "}" +
                    "}"};
    private static TopicConnectionFactory topicConnectionFactory = null;

    public static void main(String[] args)
            throws InterruptedException, NamingException {
//        Properties initialContextProperties = new Properties();
//        initialContextProperties.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
//        initContext = new InitialContext(initialContextProperties);
        topicConnectionFactory = JNDIContext.getInstance().getTopicConnectionFactory();
        JsonStockQuoteClient publisher = new JsonStockQuoteClient();
        String topicName = args[0];
        boolean batchedEvents = args[1].equalsIgnoreCase("true");
        Thread.sleep(2000);

        if (batchedEvents) {
            publisher.publishBatchedMessage(topicName, jsonMsgs);
        } else {
            publisher.publish(topicName, jsonMsgs);
        }

        System.out.println("All Stock Messages sent");

    }

    /**
     * Publish message to given topic
     *
     * @param topicName - topic name to publish messages
     * @param messages  - messages to send
     */
    public void publish(String topicName, String[] messages) {
        // create topic connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            System.out.println("Can not create topic connection." + e);
            return;
        }
        Session session = null;
        try {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            System.out.println("Sending JSON messages on '" + topicName + "' topic");
            for (int i = 0, jsonMsgsLength = messages.length; i < jsonMsgsLength; i++) {
                String message = messages[i];
                TextMessage jmsMessage = session.createTextMessage();
                jmsMessage.setText(message);
                producer.send(jmsMessage);
                System.out.println("Stock Message " + (i + 1) + " sent");
            }
            producer.close();
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }

    /**
     * Publish message to given topic
     *
     * @param topicName - topic name to publish messages
     * @param messages  - messages to send
     */
    public void publishBatchedMessage(String topicName, String[] messages) {
        // create topic connection
        TopicConnection topicConnection = null;
        try {
            topicConnection = topicConnectionFactory.createTopicConnection();
            topicConnection.start();
        } catch (JMSException e) {
            System.out.println("Can not create topic connection." + e);
            return;
        }
        Session session = null;
        try {
            session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            System.out.println("Sending JSON messages on '" + topicName + "' topic");
            String jsonArray = "[";
            for (int i = 0, jsonMsgsLength = messages.length; i < jsonMsgsLength; i++) {
                String message = messages[i];
                jsonArray = jsonArray + message + ",";
                System.out.println("Added Stock Message " + (i + 1) + " to batch");
            }
            jsonArray = jsonArray.replaceFirst(",$","]");
            TextMessage jmsMessage = session.createTextMessage();
            jmsMessage.setText(jsonArray);
            producer.send(jmsMessage);
            System.out.println("Batched Stock message with " + messages.length + " sent");
            producer.close();
            session.close();
            topicConnection.stop();
            topicConnection.close();
        } catch (JMSException e) {
            System.out.println("Can not subscribe." + e);
        }
    }

}