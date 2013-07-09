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

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.message.processors.MessageProcessor;
import org.apache.synapse.message.processors.forward.BlockingMessageSender;
import org.apache.synapse.message.processors.forward.ForwardingProcessorConstants;
import org.apache.synapse.message.processors.forward.ScheduledMessageForwardingProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Set;

public class ForwardingHandler extends MessageProcessingHandler implements Job {
    private static final Log log = LogFactory.getLog(ForwardingHandler.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (!init(jobExecutionContext)) {
            return;
        }
        MessageStore messageStore = getMessageStore();
        MessageProcessor processor = getMessageProcessor();
        ScheduledMessageForwardingProcessor schedProcessor;
        if (processor instanceof ScheduledMessageForwardingProcessor) {
            schedProcessor = (ScheduledMessageForwardingProcessor) processor;
        } else {
            log.error("Message processor is not a Scheduled Message Forwarding Processor.");
            return;
        }
        if (getMaxAttempts() == 0) {
            log.warn("Max delivery attempts is 0. Deactivating message processor.");
            schedProcessor.deactivate();
        }
        if (!schedProcessor.isActive() || messageStore == null) {
            return;
        }
        if (!(messageStore instanceof JMSMessageStore)) {
            log.error("Message store is not a JMSMessageStore.");
            return;
        }
        boolean isContinue = true;
        try {
            while (isContinue) {
                if (!getMessageProcessor().isDestroyed()) {
                    isContinue = ((JMSMessageStore) messageStore).fetchInto(this);
                } else {
                    isContinue = false;
                }
            }
        } catch (RuntimeException e) {
            log.error("Caught a runtime exception. MESSAGE:" + e.getMessage());
        }
    }

    public boolean receive(MessageContext synCtx) {
        if (synCtx == null) { // No Messages in the queue.
            return false;
        }
        if (!checkServerName(synCtx)) {
            log.error("Server names do not match.");
            return false;
        }
        cleanupErrors(synCtx);
        if (!hasEndpoint(synCtx)) {
            log.warn("Property " + ForwardingProcessorConstants.TARGET_ENDPOINT
                     + " not found in the message context. Removing the message.");
            return true;
        }
        // true: acknowledge the message
        return sendAndMediate(synCtx);
    }

    private boolean sendAndMediate(MessageContext synCtx) {
        BlockingMessageSender sender = getMessageSender();
        MessageProcessor processor = getMessageProcessor();
        ScheduledMessageForwardingProcessor schedProcessor;
        if (processor instanceof ScheduledMessageForwardingProcessor) {
            schedProcessor = (ScheduledMessageForwardingProcessor) processor;
        } else {
            log.error("Message processor is not a Scheduled Message Forwarding Processor. "
                      + "Cannot send and mediate message.");
            return false;
        }
        Endpoint ep = synCtx.getEndpoint(getEndpoint(synCtx));
        if (!(ep instanceof AddressEndpoint)) {
            log.warn("Address endpoint named " + getEndpoint(synCtx) + " not found. "
                     + "Removing the message form store");
            // Could not find an address endpoint.
            // Currently only Address Endpoint delivery is supported
            return true;
        }
        MessageContext outCtx;
        try {
            outCtx = sender.send(((AddressEndpoint) ep).getDefinition(), synCtx);
        } catch (Exception e) {
            log.error("BlockingMessageSender failed to send message. "
                      + synCtx.getMessageID() + ". " + e);
            checkAndDeactivateProcessor(schedProcessor, getMaxAttempts());
            // Sender failed to send to the address endpoint.
            return false;
        }
        if (outCtx != null
            && "true".equals(outCtx.getProperty(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR))) {
            mediateSequence(outCtx, ForwardingProcessorConstants.FAULT_SEQUENCE);
            checkAndDeactivateProcessor(schedProcessor, getMaxAttempts());
            // TODO: comment
            return false;
        } else if (outCtx == null) {
            schedProcessor.resetSentAttemptCount();
            // This Means we have invoked an out only operation.
            // remove the message and reset the count
            return true;
        } else if (!"true".equals(outCtx.getProperty(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR))) {
            mediateSequence(outCtx, ForwardingProcessorConstants.REPLY_SEQUENCE);
            schedProcessor.resetSentAttemptCount();
            // send receive ok.
            return true;
        }
        return true;
    }

    private boolean checkServerName(MessageContext synCtx) {
        //If The Message not belongs to this server we ignore it.
        String serverName = (String) synCtx.getProperty(SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME);
        if (serverName != null && synCtx instanceof Axis2MessageContext) {
            AxisConfiguration configuration = ((Axis2MessageContext) synCtx).
                    getAxis2MessageContext().
                    getConfigurationContext().getAxisConfiguration();
            String myServerName = getAxis2ParameterValue(configuration,
                                                         SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME);
            if (!serverName.equals(myServerName)) {
                return false;
            }
        }
        return true;
    }

    private boolean cleanupErrors(MessageContext synCtx) {
        Set properties = synCtx.getPropertyKeySet();
        if (properties != null) {
            if (properties.contains(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR)) {
                return properties.remove(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR);
            }
        }
        return true;
    }

    private String getEndpoint(MessageContext synCtx) {
        return (String) synCtx.getProperty(ForwardingProcessorConstants.TARGET_ENDPOINT);
    }

    private boolean hasEndpoint(MessageContext synCtx) {
        return getEndpoint(synCtx) != null;
    }

    private void mediateSequence(MessageContext synCtx, String sequence) {
        String seq = getStrParam(sequence);
        if (seq == null) {
            return;
        }
        Mediator mediator = synCtx.getSequence(seq);
        if (mediator != null) {
            mediator.mediate(synCtx);
        } else {
            log.warn("Cannot send message to '" + seq + "'. Sequence does not exist. MessageID: "
                     + synCtx.getMessageID());
        }
    }

    private void checkAndDeactivateProcessor(ScheduledMessageForwardingProcessor processor,
                                             int maxAttempts) {
        if (maxAttempts > 0) {
            processor.incrementSendAttemptCount();
            if (processor.getSendAttemptCount() >= maxAttempts) {
                processor.deactivate();
            }
        }
    }
}
