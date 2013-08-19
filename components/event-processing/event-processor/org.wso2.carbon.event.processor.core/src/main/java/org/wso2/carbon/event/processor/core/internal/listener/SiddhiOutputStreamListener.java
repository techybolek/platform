/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.listener;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.core.internal.stream.EventProducer;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import java.util.Arrays;

public class SiddhiOutputStreamListener extends StreamCallback implements EventProducer {
    private Logger trace = Logger.getLogger(EventProcessorConstants.EVENT_TRACE_LOGGER);

    private EventJunction eventJunction;
    private Object owner;
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    private EventStatisticsMonitor statisticsMonitor;
    private String tracerPrefix;

    public SiddhiOutputStreamListener(EventJunction eventJunction, ExecutionPlanConfiguration executionPlanConfiguration, int tenantId) {
        this.eventJunction = eventJunction;
        this.owner = executionPlanConfiguration;
        this.traceEnabled = executionPlanConfiguration.isTracingEnabled();
        this.statisticsEnabled = executionPlanConfiguration.isStatisticsEnabled();
        if (statisticsEnabled) {
            statisticsMonitor = EventProcessorValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventProcessorConstants.EVENT_PROCESSOR, executionPlanConfiguration.getName(), eventJunction.getStreamDefinition().getStreamId());
            this.tracerPrefix = tenantId + ":" + EventProcessorConstants.EVENT_PROCESSOR + ":" + executionPlanConfiguration.getName() + "," + eventJunction.getStreamDefinition().getStreamId() + ", after processing " + System.getProperty("line.separator");
        }
    }

    @Override
    public void receive(Event[] events) {
        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.toString(events));
        }
        if (statisticsEnabled) {
            for (Object obj : events) {
                statisticsMonitor.incrementResponse();
            }
        }
        eventJunction.dispatchEvents(events);

    }

    @Override
    public Object getOwner() {
        return owner;
    }
}
