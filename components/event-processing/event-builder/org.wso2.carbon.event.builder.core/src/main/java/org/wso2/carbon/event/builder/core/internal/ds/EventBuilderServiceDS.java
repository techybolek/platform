/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.builder.core.internal.TransportAdaptorNotificationListenerImpl;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;


/**
 * @scr.component name="eventBuilderService.component" immediate="true"
 * @scr.reference name="transportAdaptor.service"
 * interface="org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unsetTransportAdaptorService"
 * @scr.reference name="transportManager.service"
 * interface="org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorManagerService" unbind="unsetTransportAdaptorManagerService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 */
public class EventBuilderServiceDS {
    private static final Log log = LogFactory.getLog(EventBuilderServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventBuilderService eventBuilderService = new CarbonEventBuilderService();
            EventBuilderServiceValueHolder.registerEventBuilderService(eventBuilderService);
            context.getBundleContext().registerService(EventBuilderService.class.getName(), eventBuilderService, null);
            log.info("Successfully deployed EventBuilderService");

            EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService().registerDeploymentNotifier(new TransportAdaptorNotificationListenerImpl());

        } catch (RuntimeException e) {
            log.error("Could not create EventBuilderService");
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            log.error("Error when registering eventbuilder deployment notifier", e);
        }
    }

    protected void setTransportAdaptorService(InputTransportAdaptorService transportAdaptorService) {
        EventBuilderServiceValueHolder.registerTransportAdaptorService(transportAdaptorService);
    }

    protected void unsetTransportAdaptorService(InputTransportAdaptorService transportAdaptorService) {

    }

    protected void setTransportAdaptorManagerService(InputTransportAdaptorManagerService transportManagerService) {
        EventBuilderServiceValueHolder.registerTransportAdaptorManagerService(transportManagerService);
    }

    protected void unsetTransportAdaptorManagerService(InputTransportAdaptorManagerService transportManagerService) {

    }

    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventBuilderServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventBuilderServiceValueHolder.registerEventStatisticsService(null);
    }

}
