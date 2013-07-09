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
package org.apache.synapse.message.processors;

import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.processors.sampler.SamplingProcessor;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public abstract class ScheduledMessageProcessor extends AbstractMessageProcessor {


    public static final String SCHEDULED_MESSAGE_PROCESSOR_GROUP =
            "synapse.message.processor.quartz";
    public static final String PROCESSOR_INSTANCE = "processor.instance";

    /**
     * The scheduler, run the the processor
     */
    protected Scheduler scheduler = null;

    /**
     * The interval at which this processor runs , default value is 1000ms
     */
    protected long interval = 1000;


    protected enum State {
        INITIALIZED,
        START,
        STOP,
        DESTROY
    }

    /**
     * The quartz configuration file if specified as a parameter
     */
    protected String quartzConfig = null;

    /**
     * A cron expression to run the sampler
     */
    protected String cronExpression = null;

    /**
     * Keep the state of the message processor
     */
    protected State state = State.DESTROY;


    public void start() {
        //Stopping and destroying will prevent issues when re-init synapse env
        State initialState = state;
        stop();
        destroy();
        state = initialState;
        Trigger trigger;
        TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(name + "-trigger-");
        if (interval <= 0) {
            log.warn("Interval has been assigned an invalid value of ["
                     + interval + "ms]. Setting the default value [1000ms].");
            interval = 1000;
        }
        if (interval < 1000 && (this instanceof SamplingProcessor)) {
            log.warn("Interval of [" + interval
                     + "ms] is not sufficient for Sampling processor ["
                     + getName()
                     + "]. A lower interval value might cause broker " +
                     "connection errors when updating the message processor configuration. " +
                     "Minimum of 1000ms is recommended.");
        }
        if (cronExpression == null || "".equals(cronExpression)) {
            trigger = triggerBuilder.withSchedule(simpleSchedule()
                                                          .withIntervalInMilliseconds(interval)
                                                          .repeatForever())
                    .build();
        } else {
            trigger = triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();
        }
        JobDataMap jobDataMap = getJobDataMap();
        jobDataMap.put(MessageProcessorConstants.MESSAGE_STORE,
                       configuration.getMessageStore(messageStore));
        jobDataMap.put(MessageProcessorConstants.PARAMETERS, parameters);
        JobBuilder jobBuilder = getJobBuilder(getMessageStoreType());
        JobDetail jobDetail = jobBuilder.usingJobData(jobDataMap).build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new SynapseException("Error scheduling job : " + jobDetail
                                       + " with trigger " + trigger, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Started message processor. [" + getName() + "].");
        }
    }

    public void stop() {
        if (state == State.START) {
            try {
                if (scheduler != null && scheduler.isStarted()) {
                    if (log.isDebugEnabled()) {
                        log.debug("ShuttingDown Message Processor Scheduler : " + scheduler.getMetaData());
                    }
                    scheduler.standby();
                }
                state = State.STOP;
            } catch (SchedulerException e) {
                throw new SynapseException("Error ShuttingDown Message processor scheduler ", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Stopped message processor [" + getName() + "].");
        }
    }


    public void setParameters(Map<String, Object> parameters) {
        super.setParameters(parameters);
        if (parameters != null && !parameters.isEmpty()) {
            Object o = parameters.get(MessageProcessorConstants.CRON_EXPRESSION);
            if (o != null) {
                cronExpression = o.toString();
            }
            o = parameters.get(MessageProcessorConstants.INTERVAL);
            if (o != null) {
                interval = Integer.parseInt(o.toString());
            }
            o = parameters.get(MessageProcessorConstants.QUARTZ_CONF);
            if (o != null) {
                quartzConfig = o.toString();
            }

        }
    }

    public void init(SynapseEnvironment se) {
        super.init(se);
        StdSchedulerFactory sf = new StdSchedulerFactory();
        try {
            if (quartzConfig != null && !"".equals(quartzConfig)) {
                if (log.isDebugEnabled()) {
                    log.debug("Initiating a Scheduler with configuration : " + quartzConfig);
                }
                sf.initialize(quartzConfig);
            }
        } catch (SchedulerException e) {
            throw new SynapseException("Error initiating scheduler factory "
                    + sf + "with configuration loaded from " + quartzConfig, e);
        }
        try {
            scheduler = sf.getScheduler();
        } catch (SchedulerException e) {
            throw new SynapseException("Error getting a  scheduler instance form scheduler" +
                    " factory " + sf, e);
        }
        try {
            scheduler.start();
            state = State.INITIALIZED;
            this.start();
        } catch (SchedulerException e) {
            throw new SynapseException("Error starting the scheduler", e);
        }
    }

    protected abstract JobDetail getJobDetail();

    protected abstract JobBuilder getJobBuilder();

    protected JobDataMap getJobDataMap() {
        return new JobDataMap();
    }

    protected JobBuilder getJobBuilder(int messageStoreType) {
        return getJobBuilder();
    }

    public void destroy() {
        try {
            scheduler.deleteJob(new JobKey(name + "-trigger", SCHEDULED_MESSAGE_PROCESSOR_GROUP));
        } catch (SchedulerException e) {
            log.error("Error while destroying the task " + e);
        }
        if (getMessageConsumer() != null) {
            boolean success = getMessageConsumer().cleanup();
            if (!success) {
                log.error("[" + getName() + "] Could not cleanup message consumer.");
            }
        } else {
            log.warn("[" + getName() + "] Could not find the message consumer to cleanup.");
        }
        state = State.DESTROY;
        if (log.isDebugEnabled()) {
            log.debug("Destroyed message processor [" + getName() + "].");
        }
    }

    public boolean isDestroyed() {
        return state == State.DESTROY;
    }

    public void  setDestroyed() {
        state = State.DESTROY;
    }
}
