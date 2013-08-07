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

package org.wso2.carbon.input.transport.adaptor.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.CarbonInputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.build.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.ds.InputTransportAdaptorHolder;
import org.wso2.carbon.input.transport.adaptor.manager.core.internal.ds.InputTransportAdaptorManagerValueHolder;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="input.transport.adaptor.manager.component" immediate="true"
 * @scr.reference name="input.transport.adaptor.service"
 * interface="org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unSetTransportAdaptorService"
 */
public class InputTransportAdaptorManagerDS {

    private static final Log log = LogFactory.getLog(InputTransportAdaptorManagerDS.class);

    /**
     * initialize the Transport Adaptor Manager core service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputTransportAdaptorManagerService inputTransportAdaptorProxyService =
                    createTransportAdaptorManagerService();
            context.getBundleContext().registerService(InputTransportAdaptorManagerService.class.getName(),
                                                       inputTransportAdaptorProxyService, null);
            registerAxis2ConfigurationContextObserver(context);
            log.info("Successfully deployed the input transport adaptor manager service");
        } catch (RuntimeException e) {
            log.error("Can not create the input transport adaptor manager service ", e);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            log.error("Error occurred when creating the adaptor manager configuration ", e);
        }
    }

    protected void setTransportAdaptorService(InputTransportAdaptorService inputTransportAdaptorService) {
        InputTransportAdaptorHolder.getInstance().setInputTransportAdaptorService(inputTransportAdaptorService);
    }

    protected void unSetTransportAdaptorService(InputTransportAdaptorService transportAdaptorService) {
        InputTransportAdaptorHolder.getInstance().unSetTInputTransportAdaptorService();
    }

    private void registerAxis2ConfigurationContextObserver(ComponentContext context) {
        context.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                   new Axis2ConfigurationContextObserverImpl(),
                                                   null);
    }

    private InputTransportAdaptorManagerService createTransportAdaptorManagerService()
            throws InputTransportAdaptorManagerConfigurationException {
        CarbonInputTransportAdaptorManagerService carbonTransportAdaptorManagerService = new CarbonInputTransportAdaptorManagerService();
        InputTransportAdaptorManagerValueHolder.registerCarbonTransportAdaptorManagerService(carbonTransportAdaptorManagerService);
        return carbonTransportAdaptorManagerService;
    }

}
