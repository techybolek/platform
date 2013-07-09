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

import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.processors.MessageProcessor;
import org.apache.synapse.message.processors.MessageProcessorConsents;
import org.apache.synapse.message.processors.ScheduledMessageProcessor;
import org.apache.synapse.message.processors.forward.BlockingMessageSender;
import org.apache.synapse.message.processors.forward.ScheduledMessageForwardingProcessor;
import org.apache.synapse.message.processors.sampler.SamplingProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import javax.jms.Message;
import java.util.Map;

public abstract class MessageProcessingHandler {
    private static Log log = LogFactory.getLog(MessageProcessingHandler.class);
    //
    private int maxAttempts = 1;
    //
    private int concurrencyLevel = 1;
    //
    private MessageStore messageStore;
    //
    private MessageProcessor messageProcessor;
    //
    private BlockingMessageSender messageSender;
    //
    private Map parameters;
    //
    private JobDataMap jobDataMap;

    private boolean initialized = false;

    /**
     * Receivs a message from a Message Store.
     * @param synCtx Synapse Message context of the received message
     * @return
     */
    public abstract boolean receive(MessageContext synCtx);

    protected final boolean init(JobExecutionContext jobExecutionContext) {
        if (jobExecutionContext == null) {
            log.error("Cannot initialize MessageProcessingHandler. Job Execution context is null.");
            return false;
        }
        // get job data
        jobDataMap = jobExecutionContext.getMergedJobDataMap();
        if (jobDataMap == null) {
            log.error("Cannot initialize MessageProcessingHandler. Cannot find job data object.");
            return false;
        }
        // set message processor.
        Object item = jobDataMap.get(ScheduledMessageProcessor.PROCESSOR_INSTANCE);
        if (item instanceof MessageProcessor) {
            messageProcessor = (MessageProcessor) item;
        } else {
            log.error("Cannot initialize MessageProcessingHandler. Cannot find Message Processor.");
            return false;
        }
        // set message store.
        item = jobDataMap.get(MessageProcessorConsents.MESSAGE_STORE);
        if (item instanceof MessageStore) {
            messageStore = (MessageStore) item;
        } else {
            log.error("Cannot initialize MessageProcessingHandler. Cannot find Message Store.");
            return false;
        }
        // set concurrency level.
        item = jobDataMap.get(SamplingProcessor.CONCURRENCY);
        if (item instanceof Integer) {
            concurrencyLevel = (Integer) item;
        }
        // set max delivery attempts.
        item = jobDataMap.get(MessageProcessorConsents.PARAMETERS);
        if (item instanceof Map) {
            parameters = (Map<String, Object>) item;
            item = parameters.get(MessageProcessorConsents.MAX_DELIVER_ATTEMPTS);
            if (item instanceof String) {
                maxAttempts = Integer.parseInt((String) item);
            }
        }
        //
        item = jobDataMap.get(ScheduledMessageForwardingProcessor.BLOCKING_SENDER);
        if (item instanceof BlockingMessageSender) {
            messageSender = (BlockingMessageSender) item;
        }
        ////:~
        initialized = true;
        return true;
    }

    protected MessageStore getMessageStore() {
        checkInitialized();
        return messageStore;
    }

    protected String messageProcessorName() {
        if (messageProcessor != null) {
            return messageProcessor.getName();
        }
        return "__UNKNOWN_MESSAGE_PROCESSOR__";
    }

    protected MessageProcessor getMessageProcessor() {
        checkInitialized();
        return messageProcessor;
    }

    protected int getConcurrencyLevel() {
        checkInitialized();
        return concurrencyLevel;
    }

    protected int getMaxAttempts() {
        checkInitialized();
        return maxAttempts;
    }

    protected BlockingMessageSender getMessageSender() {
        checkInitialized();
        return messageSender;
    }

    protected String getStrParam(String param) {
        if (param == null) {
            return null;
        }
        checkInitialized();
        Object item = parameters.get(param);
        if (item instanceof String) {
            return (String) item;
        }
        return null;
    }

    protected void checkInitialized() {
        if (!initialized) {
            log.warn("Message Processing Handler has not been initialized.");
        }
    }

    /**
     * Helper method to get a value of a parameters in the AxisConfiguration
     *
     * @param axisConfiguration AxisConfiguration instance
     * @param paramKey The name / key of the parameter
     * @return The value of the parameter
     */
    protected static String getAxis2ParameterValue(AxisConfiguration axisConfiguration,
                                                 String paramKey) {
        Parameter parameter = axisConfiguration.getParameter(paramKey);
        if (parameter == null) {
            return null;
        }
        Object value = parameter.getValue();
        if (value != null && value instanceof String) {
            return (String) parameter.getValue();
        } else {
            return null;
        }
    }

    public Message getMessage() {
        Object msg = messageProcessor.getCurrentMessage();
        if (msg instanceof Message) {
            return (Message) msg;
        }
        return null;
    }

    public MessageContext getMessageContext() {
        return messageProcessor.getCurrentMessageContext();
    }

    public MessageConsumer getMessageConsumer() {
        return messageProcessor.getMessageConsumer();
    }

    public boolean cleanupConsumer() {
        if (messageProcessor != null) {
           return messageProcessor.removeCurrentConsumer();
        }
        return false;
    }

    public boolean registerMessageConsumer(MessageConsumer consumer) {
        return messageProcessor.setMessageConsumer(consumer);
    }

    public boolean setCurrentMessage(Message message) {
        messageProcessor.setCurrentMessage(message);
        return true;
    }

    public boolean setCurrentMessageContext(MessageContext messageContext) {
        return messageProcessor.setCurrentMessageContext(messageContext);
    }

    public boolean clearCurrentMessage() {
        return messageProcessor.clearCurrentMessage();
    }

    public boolean incrementProcessedCount() {
        return messageProcessor.incrementProcessed();
    }

    public long getProcessedCount() {
        return messageProcessor.getProcessed();
    }

    protected boolean isRetrying() {
        return messageProcessor.isRetrying();
    }

    protected void setRetrying() {
        messageProcessor.setRetrying();
    }

    protected void unsetRetrying() {
        messageProcessor.unsetRetrying();
    }

    public boolean isConnected() {
        if (messageProcessor.getMessageConsumer() != null) {
            return messageProcessor.getMessageConsumer().getConsumer() != null;
        }
        if (messageProcessor.getMessageConsumer() != null) {
            return !messageProcessor.getMessageConsumer().isConnectionError();
        }
        return false;
    }
}
