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

package org.wso2.carbon.input.transport.adaptor.core.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;

/**
 * common place to hold some OSGI bundle references.
 */
public final class InputTransportAdaptorServiceValueHolder {

    private static InputTransportAdaptorService carbonInputTransportAdaptorService;
    private static ComponentContext  componentContext;
    private static EventStatisticsService eventStatisticsService;

    private InputTransportAdaptorServiceValueHolder() {
    }

    public static void registerCarbonTransportService(
            InputTransportAdaptorService carbonInputTransportAdaptorService) {

        InputTransportAdaptorServiceValueHolder.carbonInputTransportAdaptorService = carbonInputTransportAdaptorService;
    }

    public static InputTransportAdaptorService getCarbonInputTransportAdaptorService() {
        return InputTransportAdaptorServiceValueHolder.carbonInputTransportAdaptorService;
    }

    public static void registerComponentContext(
            ComponentContext componentContext) {

        InputTransportAdaptorServiceValueHolder.componentContext = componentContext;
    }

    public static ComponentContext getComponentContext() {
        return InputTransportAdaptorServiceValueHolder.componentContext;
    }


    public static void registerEventStatisticsService(EventStatisticsService eventStatisticsService) {
        InputTransportAdaptorServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }
}
