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

package org.apache.synapse.message.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.message.processors.forward.ForwardingJob;
import org.apache.synapse.message.processors.sampler.SamplingJob;

public final class MessageStores {
    /** JMS Message Store */
    public static final int JMS_MS       = 0;
    /** JDBC Message Store */
    public static final int JDBC_MS      = 1;
    /** In Memory Message Store */
    public static final int INMEMORY_MS  = 2;
    /** Types of JMS message processing handlers */
    private static final String FORWARDING_HANDLER = "org.wso2.carbon.message.store.persistence.jms.ForwardingHandler";
    private static final String SAMPLING_HANDLER = "org.wso2.carbon.message.store.persistence.jms.SamplingHandler";
    /** Types of message stores */
    private static final String JMS_MESSAGE_STORE = "org.wso2.carbon.message.store.persistence.jms.JMSMessageStore";

    private static final Log log = LogFactory.getLog(MessageStores.class);

    private MessageStores() { }

    /**
     * Returns a new instance of a MessageStore object of the given type.
     * @param type Type of the message store
     * @return A new instance of the message store.
     */
    public static MessageStore getNewInstance(final int type) {
        if (INMEMORY_MS == type) {
            return new InMemoryMessageStore();
        }
        if (JMS_MS == type) {
            return getNewInstanceFromImpl(JMS_MESSAGE_STORE);
        }
        log.error("Could not create message store of type '" + type
                  + "'. Returning an In-memory message store instance.");
        return new InMemoryMessageStore();
    }

    /**
     * Returns a new instance of a MessageStore object of the given implementation.
     * @param impl Full qualified name of the message store implementation
     * @return New instance of a message store
     */
    public static MessageStore getNewInstanceFromImpl(final String impl) {
        Class clazz;
        try {
            clazz = Class.forName(impl);
        } catch (ClassNotFoundException e) {
            log.error("Could not create message store from '" + impl + "'. "
                      + e.getLocalizedMessage());
            log.error("Returning an In-Memory message store instance.");
            return new InMemoryMessageStore();
        }
        try {
            return (MessageStore) clazz.newInstance();
        } catch (Exception e) {
            log.error("Could not create message store from '" + impl + "'. "
                      + e.getLocalizedMessage());
            log.error("Returning an In-Memory message store instance.");
            return new InMemoryMessageStore();
        }
    }

    /**
     * Returns the type of the ForwardingJob for the given type.
     * @param type message store type.
     * @return Forwarding job class
     */
    public static Class getForwardingHandler(final int type) {
        if (type == INMEMORY_MS) {
            return ForwardingJob.class;
        }
        if (type == JMS_MS) {
            Class clazz;
            try {
                clazz = Class.forName(FORWARDING_HANDLER);
                return clazz;
            } catch (ClassNotFoundException e) {
                log.error("Could not create a forwarding message handler from '" + FORWARDING_HANDLER  + "'. "
                          + e.getLocalizedMessage());
                //return ForwardingJob.class;
            }
        }
        if (type == JDBC_MS) {
            log.warn("Could not create a forwarding message handler from '" + "JDBCForwardingHandler"  + "'. "
                          + "Returning default forwarding job instead.");
            //return ForwardingJob.class;
        }
        return ForwardingJob.class;
    }

    /**
     * Returns a Sampling job class according to the message store type.
     * @param type Type of the message store
     * @return Sampling job class
     */
    public static Class getSamplingHandler(final int type) {
        if (type == INMEMORY_MS) {
            return SamplingJob.class;
        }
        if (type == JMS_MS) {
            Class clazz;
            try {
                clazz = Class.forName(SAMPLING_HANDLER);
                return clazz;
            } catch (ClassNotFoundException e) {
                log.error("Could not create a sampling message handler from '" + SAMPLING_HANDLER + "'. "
                          + e.getLocalizedMessage());
            }
        }
        if (type == JDBC_MS) {
            log.warn("Could not create a sampling message handler from '" + "JDBCSamplingHandler" + "'. "
                          + "Returning default sampling job instead.");
        }
        return SamplingJob.class;
    }

    /**
     * Returns the type of the message store as a string given the int constant value.
     * @param type
     * @return
     */
    public static String getTypeAsString(int type) {
        if (type == INMEMORY_MS) {
            return "In-Memory";
        }
        if (type == JMS_MS) {
            return "JMS";
        }
        if (type == JDBC_MS) {
            return "JDBC";
        }
        return "UNKNOWN";
    }
}
