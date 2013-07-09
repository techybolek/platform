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
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.message.processors.MessageProcessor;
import org.apache.synapse.message.processors.sampler.SamplingProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.concurrent.ExecutorService;

public class SamplingHandler extends MessageProcessingHandler implements Job {
    private static Log log = LogFactory.getLog(SamplingHandler.class);

    /**
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (!init(jobExecutionContext)) {
            return;
        }
        // if processor is not active we do not proceed with the processing
        MessageProcessor processor = getMessageProcessor();
        SamplingProcessor schedProcessor;
        if (processor instanceof SamplingProcessor) {
            schedProcessor = (SamplingProcessor) processor;
        } else {
            log.error("Message processor is not a Sampling Processor.");
            return;
        }
        if (!schedProcessor.isActive()) {
            return;
        }
        int concurrencyLevel = getConcurrencyLevel();
        MessageStore messageStore = getMessageStore();
        if (!(messageStore instanceof JMSMessageStore)) {
            log.error("Message store is not a JMSMessageStore.");
            return;
        }
        for (int i = 0; i < concurrencyLevel; ++i) {
            synchronized (messageStore) {
                if (!getMessageProcessor().isDestroyed()) {
                    boolean successful = ((JMSMessageStore) messageStore).fetchInto(this);
                    if (!successful) {
                        //log.warn("JMS Message store could not process next message.");
                    }
                } else {
                    log.info("SKIPPING ITERATION.");
                }
            }
        }
    }

    public boolean receive(MessageContext synCtx) {
        if (synCtx == null) { // No Messages in the queue.
            return false;
        }
        final String sequence = getStrParam(SamplingProcessor.SEQUENCE);
        final MessageContext messageContext = synCtx;
        final ExecutorService executor = messageContext.getEnvironment().getExecutorService();
        executor.submit(new Runnable() {
            public void run() {
                try {
                    Mediator processingSequence = messageContext.getSequence(sequence);
                    boolean mediateSuccess = false;
                    if (processingSequence != null) {
                        mediateSuccess = processingSequence.mediate(messageContext);
                    } else {
                        log.error("Could not find sequence '" + sequence
                                  + "' to process message with MessageID:"
                                  + messageContext.getMessageID());
                    }
                    if (!mediateSuccess) {
                        log.warn("Processing sequence returned 'false' for message with MessageID:"
                                 + messageContext.getMessageID());
                    }
                } catch (Throwable t) {
                    log.error("Error occurred while processing message with MessageID:"
                              + messageContext.getMessageID(), t);
                }
            }
        });
        return true;
    }
}
