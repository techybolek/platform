/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.message.processors.sampler;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.processors.ScheduledMessageProcessor;
import org.apache.synapse.message.store.MessageStores;
import org.quartz.*;

import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

public class SamplingProcessor extends ScheduledMessageProcessor{
    private Log log = LogFactory.getLog(SamplingProcessor.class);

    public static final String CONCURRENCY = "concurrency";
    public static final String SEQUENCE = "sequence";

    private AtomicBoolean active = new AtomicBoolean(true);

    private SamplingProcessorView view;
    
    @Override
    public void init(SynapseEnvironment se) {

        String thisServerName = se.getServerContextInformation().getServerConfigurationInformation()
                .getServerName();
        Object pinnedServersObj = this.parameters.get("pinnedServers");
        if (pinnedServersObj != null && pinnedServersObj instanceof String) {
            boolean pinned = false;
            String pinnedServers = (String) pinnedServersObj;
            StringTokenizer st = new StringTokenizer(pinnedServers, " ,");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (thisServerName.equals(token)) {
                    pinned = true;
                }
            }
            if (!pinned) {
                log.info("Message processor '" + name + "' pinned on '" + pinnedServers + "' not starting on" +
                        " this server '" + thisServerName + "'");
                active = new AtomicBoolean(false);
            }
        }
        super.init(se);
        view = new SamplingProcessorView(this);
        // register MBean
        org.apache.synapse.commons.jmx.MBeanRegistrar.getInstance().registerMBean(view,
                "Message Sampling Processor view", getName());
    }

    //@Override
    protected JobBuilder getJobBuilder0() {
        JobBuilder jobBuilder = JobBuilder.newJob(SamplingJob.class)
                .withIdentity(name + "-sampling-job", SCHEDULED_MESSAGE_PROCESSOR_GROUP);
        return jobBuilder;
    }

    @Override
    protected JobBuilder getJobBuilder() {
        JobBuilder jobBuilder = JobBuilder
                .newJob(MessageStores.getSamplingHandler(MessageStores.INMEMORY_MS))
                .withIdentity(name + "-sampling-job", SCHEDULED_MESSAGE_PROCESSOR_GROUP);
        return jobBuilder;
    }

    @Override
    protected JobBuilder getJobBuilder(int type) {
         JobBuilder jobBuilder = JobBuilder
                 .newJob(MessageStores.getSamplingHandler(type))
                 .withIdentity(name + "-sampling-job", SCHEDULED_MESSAGE_PROCESSOR_GROUP);
         return jobBuilder;
    }

    @Override
    protected JobDetail getJobDetail() {
        JobDetail jobDetail;
        JobBuilder jobBuilder = getJobBuilder();
        jobDetail = jobBuilder.build();
        return jobDetail;
    }
    
    @Override
    protected JobDataMap getJobDataMap() {
        JobDataMap jdm = new JobDataMap();
        jdm.put(PROCESSOR_INSTANCE,this);
        return jdm;
    }

    @Override
    public void destroy() {
        state = State.DESTROY;
        try {
            boolean result = scheduler.deleteJob(new JobKey(name + "-sampling-job",
                                                            ScheduledMessageProcessor.SCHEDULED_MESSAGE_PROCESSOR_GROUP));
        } catch (SchedulerException e) {
            log.error("Error while destroying the task " + e);
        }
        if (getMessageConsumer() != null) {
            boolean success = getMessageConsumer().cleanup();
            removeCurrentConsumer();
            if (!success) {
                log.error("[" + getName() + "] Could not cleanup message consumer.");
            }
        } else {
            log.warn("[" + getName() + "] Could not find the message consumer to cleanup.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Destroyed message processor [" + getName() + "].");
        }
    }

    public boolean isActive() {
        return active.get();
    }

    public void activate() {
        active.set(true);
    }

    public void deactivate() {
        active.set(false);
    }

    public SamplingProcessorView getView() {
        return view;
    }
}
