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

package org.wso2.carbon.event.processor.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.internal.CEPEventSource;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.listener.EventBuilderNotificationListenerImpl;
import org.wso2.carbon.event.statistics.EventStatisticsService;

/**
 * @scr.component name="eventProcessorService.component" immediate="true"
 * @scr.reference name="eventBuilderService.service"
 * interface="org.wso2.carbon.event.builder.core.EventBuilderService" cardinality="1..1"
 * policy="dynamic" bind="setEventBuilderService" unbind="unsetEventBuilderService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 */
public class EventProcessorServiceDS {
    private static final Log log = LogFactory.getLog(EventProcessorServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            CarbonEventProcessorService carbonEventProcessorService = new CarbonEventProcessorService();
            EventProcessorValueHolder.registerEventProcessorService(carbonEventProcessorService);

            CEPEventSource eventSource = new CEPEventSource(carbonEventProcessorService);

            context.getBundleContext().registerService(EventProcessorService.class.getName(), carbonEventProcessorService, null);
            context.getBundleContext().registerService(EventSource.class.getName(), eventSource, null);
            log.info("Successfully deployed EventProcessorService");
            EventProcessorValueHolder.getEventBuilderService().registerEventBuilderNotificationListener(new EventBuilderNotificationListenerImpl());


        } catch (RuntimeException e) {
            log.error("Could not create EventProcessorService");
        }

    }

    public void setEventBuilderService(EventBuilderService eventBuilderService) {
        EventProcessorValueHolder.registerEventBuilderService(eventBuilderService);
    }

    public void unsetEventBuilderService(EventBuilderService eventBuilderService) {
        EventProcessorValueHolder.registerEventBuilderService(null);
    }

    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(null);
    }

}
