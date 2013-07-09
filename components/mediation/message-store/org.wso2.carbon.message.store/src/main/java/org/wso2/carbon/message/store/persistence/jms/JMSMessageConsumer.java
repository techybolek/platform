/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.message.store.persistence.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.store.MessageStores;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Destination;

public class JMSMessageConsumer implements MessageConsumer  {
    private static final Log log = LogFactory.getLog(JMSMessageConsumer.class);
    private Connection connection;
    private Session session;
    private Destination destination;
    private javax.jms.MessageConsumer consumer;
    private boolean connectionError = false;
    private String storeName;
    private String processorName;

    public boolean setConsumer(javax.jms.MessageConsumer consumer) {
        if (consumer == null) {
            log.error("Message consumer [" + processorName() + "->" + storeName() + "] is null.");
            return false;
        }
        this.consumer = consumer;
        return true;
    }

    public Object getConsumer() {
        return consumer;
    }

    public boolean setConnection(Connection connection) {
        if (connection == null) {
            log.error("Message consumer [" + processorName() + "->"
                      + storeName() + "]. Faulty Connection.");
            return false;
        }
        this.connection = connection;
        return true;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean setSession(Session session) {
        if (session == null) {
            log.error("Message consumer [" + processorName() + "->"
                      + storeName() + "]. Faulty Session.");
            return false;
        }
        this.session = session;
        return true;
    }

    public Session getSession() {
        return session;
    }

    public boolean setDestination(Destination destination) {
        if (destination == null) {
            log.error("Message consumer [" + processorName() + "->"
                      + storeName() + "]. Faulty Destination.");
            return false;
        }
        this.destination = destination;
        return true;
    }

    public Destination getDestination() {
        return destination;
    }

    public boolean isConnectionError() {
        return connectionError;
    }

    public void connectionError() {
        this.connectionError = true;
    }

    public void clearConnectionError() {
        connectionError = false;
    }

    public synchronized boolean cleanup() {
        try {
            if (consumer != null) {
                consumer.close();
            }
            consumer = null;
        } catch (JMSException e) {
            log.error("Message consumer ["+processorName() + "<-"
                      + storeName()+"]. Error while cleaning up message consumer. ", e);
            return false;
        }
        destination = null;
        try {
            if (session != null) {
                session.close();
            }
            session = null;
        } catch (JMSException e) {
            log.error("Message consumer ["+processorName()
                      + "<-" + storeName()+"]. Error while cleaning up connection session.", e);
            return false;
        }
        try {
            synchronized (this) {
                if (connection != null) {
                    connection.close();
                    connection = null;
                }  else {
                    return true;
                }
            }
        } catch (JMSException e) {
            log.error("Message consumer ["
                      + processorName() + "<-" + storeName() + "]. Error while cleaning up connection.", e);
            return false;
        }
        return true;
    }

    public int getConsumerType() {
        return MessageStores.JMS_MS;
    }

    public void setProcessorName(String s) {
        this.processorName = s;
    }

    public String processorName() {
        return processorName;
    }

    public void setStoreName(String s) {
        storeName = s;
    }

    public String storeName() {
        return storeName;
    }
}
