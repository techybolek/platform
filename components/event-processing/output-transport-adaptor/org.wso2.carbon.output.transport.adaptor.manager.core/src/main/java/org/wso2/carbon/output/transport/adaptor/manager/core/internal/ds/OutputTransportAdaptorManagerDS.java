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

package org.wso2.carbon.output.transport.adaptor.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.CarbonOutputTransportAdaptorManagerService;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.build.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="output.transport.adaptor.manager.component" immediate="true"
 * @scr.reference name="output.transport.adaptor.service"
 * interface="org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unSetTransportAdaptorService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 */
public class OutputTransportAdaptorManagerDS {

    private static final Log log = LogFactory.getLog(OutputTransportAdaptorManagerDS.class);

    /**
     * initialize the Transport Adaptor Manager core service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputTransportAdaptorManagerService outputTransportAdaptorProxyService =
                    createTransportAdaptorManagerService();
            context.getBundleContext().registerService(OutputTransportAdaptorManagerService.class.getName(),
                                                       outputTransportAdaptorProxyService, null);
            registerAxis2ConfigurationContextObserver(context);
            log.info("Successfully deployed the output transport adaptor manager service");
        } catch (RuntimeException e) {
            log.error("Can not create the output transport adaptor manager service ", e);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            log.error("Error occurred when creating the output transport adaptor manager configuration ", e);
        }
    }

    protected void setTransportAdaptorService(
            OutputTransportAdaptorService outputTransportAdaptorService) {
        OutputTransportAdaptorHolder.getInstance().setOutputTransportAdaptorService(outputTransportAdaptorService);
    }

    protected void unSetTransportAdaptorService(
            OutputTransportAdaptorService transportAdaptorService) {
        OutputTransportAdaptorHolder.getInstance().unSetTOutputTransportAdaptorService();
    }


    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        OutputTransportAdaptorManagerValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        OutputTransportAdaptorManagerValueHolder.registerEventStatisticsService(null);
    }

    private void registerAxis2ConfigurationContextObserver(ComponentContext context) {
        context.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                   new Axis2ConfigurationContextObserverImpl(),
                                                   null);
    }

    private OutputTransportAdaptorManagerService createTransportAdaptorManagerService()
            throws OutputTransportAdaptorManagerConfigurationException {
        CarbonOutputTransportAdaptorManagerService carbonTransportAdaptorManagerService = new CarbonOutputTransportAdaptorManagerService();
        OutputTransportAdaptorManagerValueHolder.registerCarbonTransportAdaptorManagerService(carbonTransportAdaptorManagerService);
        return carbonTransportAdaptorManagerService;
    }

}
