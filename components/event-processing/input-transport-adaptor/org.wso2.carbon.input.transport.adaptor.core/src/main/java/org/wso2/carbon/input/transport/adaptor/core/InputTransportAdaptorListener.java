/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.input.transport.adaptor.core;


import org.apache.log4j.Logger;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;

/**
 * listener class to receive the events from the transport proxy
 */

public abstract class InputTransportAdaptorListener {


    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);

    private Boolean statisticsEnabled;
    private Boolean traceEnabled;
    private String transportAdaptorName;

    private String tracerPrefix = "";
    private EventStatisticsMonitor statisticsMonitor;

    /**
     * when an event definition is defined, transport calls this method with the definition.
     *
     * @param object - received event definition
     */
    public abstract void addEventDefinition(Object object);

    /**
     * when an event definition is removed transport proxy call this method with the definition.
     *
     * @param object - received event definition
     */
    public abstract void removeEventDefinition(Object object);

    /**
     * when an event happens transport proxy call this method with the received event.
     *
     * @param object - received event
     */
    public abstract void onEvent(Object object);


    /**
     * when an event definition is defined, transport calls this method with the definition.
     *
     * @param object - received event definition
     */
    public void addEventDefinitionCall(Object object) {
        if (traceEnabled) {
            trace.info(tracerPrefix + " Add EventDefinition " + System.getProperty("line.separator") + object);
        }
        addEventDefinition(object);
    }

    /**
     * when an event definition is removed transport proxy call this method with the definition.
     *
     * @param object - received event definition
     */
    public void removeEventDefinitionCall(Object object) {
        if (traceEnabled) {
            trace.info(tracerPrefix + " Remove EventDefinition " + System.getProperty("line.separator") + object);
        }
        removeEventDefinition(object);
    }

    /**
     * when an event happens transport proxy call this method with the received event.
     *
     * @param object - received event
     */
    public void onEventCall(Object object) {
        if (traceEnabled) {
            trace.info(tracerPrefix + object);
        }
        if(statisticsEnabled){
            statisticsMonitor.incrementRequest();
        }
        onEvent(object);
    }


    public Boolean getStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(Boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public Boolean getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(Boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public String getTransportAdaptorName() {
        return transportAdaptorName;
    }

    public void setTransportAdaptorName(String transportAdaptorName) {
        this.transportAdaptorName = transportAdaptorName;
    }

    public void setStatisticsMonitor(EventStatisticsMonitor statisticsMonitor) {
        this.statisticsMonitor = statisticsMonitor;
    }

    public EventStatisticsMonitor getStatisticsMonitor() {
        return statisticsMonitor;
    }

    public void setTracerPrefix(String tracerPrefix) {
        this.tracerPrefix = tracerPrefix;
    }

    public String getTracerPrefix() {
        return tracerPrefix;
    }
}
