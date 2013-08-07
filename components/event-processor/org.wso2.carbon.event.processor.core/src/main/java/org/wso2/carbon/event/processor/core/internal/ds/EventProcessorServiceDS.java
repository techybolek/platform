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
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.internal.CEPEventSource;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.listener.EventBuilderNotificationListenerImpl;

/**
 * @scr.component name="eventProcessorService.component" immediate="true"
 * @scr.reference name="eventBuilderService.service"
 * interface="org.wso2.carbon.event.builder.core.EventBuilderService" cardinality="1..1"
 * policy="dynamic" bind="setEventBuilderService" unbind="unsetEventBuilderService"
 */
public class EventProcessorServiceDS {
    private static final Log log = LogFactory.getLog(EventProcessorServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventProcessorService eventProcessorService = createEventProcessorService();
            context.getBundleContext().registerService(EventProcessorService.class.getName(), eventProcessorService, null);
            context.getBundleContext().registerService(EventSource.class.getName(), new CEPEventSource(), null);
            log.info("Successfully deployed EventProcessorService");
            EventProcessorValueHolder.getEventBuilderService().registerEventBuilderNotificationListener(new EventBuilderNotificationListenerImpl());
            serviceActivated();
        } catch (RuntimeException e) {
            log.error("Could not create EventProcessorService");
        } catch (ExecutionPlanConfigurationException e) {
            log.error("Error occurred when loading event processor service");
        }

    }

    public void setEventBuilderService(EventBuilderService eventBuilderService) {
        EventProcessorValueHolder.registerEventBuilderService(eventBuilderService);
    }

    public void unsetEventBuilderService(EventBuilderService eventBuilderService) {
        EventProcessorValueHolder.registerEventBuilderService(null);
    }

    private EventProcessorService createEventProcessorService()
            throws ExecutionPlanConfigurationException {
        CarbonEventProcessorService carbonEventProcessorService = new CarbonEventProcessorService();
        EventProcessorValueHolder.registerEventProcessorService(carbonEventProcessorService);
        return carbonEventProcessorService;
    }

    private void serviceActivated() {
        CEPEventSource eventSource = new CEPEventSource();
        EventProcessorValueHolder.registerCEPEventSource(eventSource);
    }
}
