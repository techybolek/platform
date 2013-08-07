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

import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.EventSourceNotificationListener;
import org.wso2.carbon.event.processor.core.internal.CEPEventSource;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;

public class EventProcessorValueHolder {
    private static CarbonEventProcessorService eventProcessorService;
    private static EventBuilderService eventBuilderService;
    private static EventFormatterService eventFormatterService;
    private static CEPEventSource cepEventSource;
    private static EventSourceNotificationListener eventSourceNotificationListener;

    public static void registerEventProcessorService(CarbonEventProcessorService service) {
        eventProcessorService = service;
    }

    public static CarbonEventProcessorService getEventProcessorService() {
        return eventProcessorService;
    }

    public static void registerEventBuilderService(EventBuilderService eventBuilderService) {
        EventProcessorValueHolder.eventBuilderService = eventBuilderService;
    }

    public static EventBuilderService getEventBuilderService() {
        return eventBuilderService;
    }

    public static void registerEventFormatterService(EventFormatterService eventFormatterService) {
        EventProcessorValueHolder.eventFormatterService = eventFormatterService;
    }

    public static EventFormatterService getEventFormatterService() {
        return EventProcessorValueHolder.eventFormatterService;
    }

    public static void registerCEPEventSource(CEPEventSource cepEventSource) {
        EventProcessorValueHolder.cepEventSource = cepEventSource;
    }

    public static CEPEventSource getCepEventSource() {
        return cepEventSource;
    }

    public static void registerNotificationListener(
            EventSourceNotificationListener eventSourceNotificationListener) {
        EventProcessorValueHolder.eventSourceNotificationListener = eventSourceNotificationListener;
    }

    public static EventSourceNotificationListener getNotificationListener() {
        return eventSourceNotificationListener;
    }
}
