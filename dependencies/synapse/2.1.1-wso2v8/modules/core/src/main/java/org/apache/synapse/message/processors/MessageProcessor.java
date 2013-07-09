/*
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
package org.apache.synapse.message.processors;

import org.apache.synapse.*;
import org.apache.synapse.message.MessageConsumer;

import java.util.Map;

/**
 *All Synapse Message Processors must implement <code>MessageProcessor</code> interface
 *Message processors will process the Message using a Message Store.
 *Message processing logic and process will depend on the
 *concrete implementation of the MessageStore
 */
public interface MessageProcessor extends ManagedLifecycle , Nameable , SynapseArtifact{

    /**
     * Start Message Processor
     */
    public void start();

    /**
     * Stop MessageProcessor
     */
    public void stop();

    /**
     * Set the Message Store name that backs the Message processor
     * @param messageStore name the underlying MessageStore instance
     */
    public void setMessageStoreName(String  messageStore);

    /**
     * Get message store name associated with the Message processor
     * @return  message store name associated with message processor
     */
    public String getMessageStoreName();

    /**
     * Set the Message processor parameters that will be used by the specific implementation
     * @param parameters
     */
    public void setParameters(Map<String,Object> parameters);

    /**
     * Get the Message processor Parameters
     * @return
     */
    public Map<String , Object> getParameters();

    /**
     * Returns weather a Message processor is started or not
     * @return
     */
    public boolean isStarted();

     /**
     * Set the name of the file that the Message Processor is configured
     *
     * @param filename Name of the file where this artifact is defined
     */
    public void setFileName(String filename);

    /**
     * get the file name that the message processor is configured
     *
     * @return Name of the file where this artifact is defined
     */
    public String getFileName();

    /**
     * Returns the message consumer that is associated with this message processor. <br/>
     * This message consumer can be used to retrieve the original message consumer associated with
     * the message store and in turn, it can be used to consume messages from the store. <br/>
     * For example, if the Message store that is associated with this message processor is of type JMS,
     * this method will return a JMS Message Consumer that can consume from the queue of the
     * message store.
     * @return Synapse message consumer that encapsulates the original message consumer.
     */
    public MessageConsumer getMessageConsumer();

    /**
     * Saves the current message that is being processed.
     * @param message Message object.
     * @return
     */
    public boolean setCurrentMessage(Object message);

    /**
     * Saves the Synapse message context of the message that is being
     * processed currently.
     * @param messageContext Synapse message context of the current message.
     * @return
     */
    public boolean setCurrentMessageContext(MessageContext messageContext);

    /**
     * Returns the Current message that is being processed. <br/>
     * It can be a JMS Message
     * @return
     */
    public Object getCurrentMessage();

    /**
     * Returns the Synapse message context pertaining to the current message
     * that is being processed.
     * @return
     */
    public MessageContext getCurrentMessageContext();

    /**
     * Wipes out the last/current message that was processed.
     * @return
     */
    public boolean clearCurrentMessage();

    /**
     * If the current message was successfully processed, increments the
     * successful message count.
     * @return
     */
    public boolean incrementProcessed();

    /**
     * Returns the number of successfully processed messages.
     * @return
     */
    public long getProcessed();

    /**
     * Registers a new message consumer for this processor after cleaning up the
     * existing message and the message consumer.
     * @param consumer Synapse message consumer
     * @return
     */
    public boolean setMessageConsumer(MessageConsumer consumer);

    /**
     * Sets the state of this processor as retrying to connect to a message store.
     */
    public void setRetrying();

    /**
     * Set the state of this processor as not retrying to connect to a message store.
     */
    public void unsetRetrying();

    /**
     * Returns if currently the processor is retrying to connect to a message store.
     *
     * @return
     */
    public boolean isRetrying();

    /**
     * Closes the current message consumer connection and deletes the synapse message consumer.
     * @return
     */
    public boolean removeCurrentConsumer();

    /**
     * Returns if the current state of this message processor is destroyed. <br/>If this value is true
     * The message handlers will not attempt to fetch message from the store.
     * @return
     */
    public boolean isDestroyed();

    public void setDestroyed();
}
