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

package org.wso2.carbon.transport.adaptor.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TransportAdaptorManagerConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.CarbonTransportAdaptorManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.internal.build.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorHolder;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorManagerValueHolder;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="transportmanager.component" immediate="true"
 * @scr.reference name="transport.adaptor.service"
 * interface="org.wso2.carbon.transport.adaptor.core.TransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unSetTransportAdaptorService"
 */
public class TransportAdaptorManagerDS {

    private static final Log log = LogFactory.getLog(TransportAdaptorManagerDS.class);

    /**
     * initialize the Transport Adaptor Manager core service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            TransportAdaptorManagerService transportAdaptorProxyService =
                    createTransportAdaptorManagerService();
            context.getBundleContext().registerService(TransportAdaptorManagerService.class.getName(),
                                                       transportAdaptorProxyService, null);
            registerAxis2ConfigurationContextObserver(context);
            log.info("Successfully deployed the transport manager service");
        } catch (RuntimeException e) {
            log.error("Can not create the transport manager service ", e);
        } catch (TransportAdaptorManagerConfigurationException e) {
            log.error("Error occurred when creating the Adaptor manager configuration ", e);
        }
    }

    protected void setTransportAdaptorService(TransportAdaptorService transportService) {
        TransportAdaptorHolder.getInstance().setTransportAdaptorService(transportService);
    }

    protected void unSetTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        TransportAdaptorHolder.getInstance().unSetTransportAdaptorService();
    }

    private void registerAxis2ConfigurationContextObserver(ComponentContext context) {
        context.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                   new Axis2ConfigurationContextObserverImpl(),
                                                   null);
    }

    private TransportAdaptorManagerService createTransportAdaptorManagerService()
            throws TransportAdaptorManagerConfigurationException {
        CarbonTransportAdaptorManagerService carbonTransportAdaptorManagerService = new CarbonTransportAdaptorManagerService();
        TransportAdaptorManagerValueHolder.registerCarbonTransportAdaptorManagerService(carbonTransportAdaptorManagerService);
        return carbonTransportAdaptorManagerService;
    }

}
