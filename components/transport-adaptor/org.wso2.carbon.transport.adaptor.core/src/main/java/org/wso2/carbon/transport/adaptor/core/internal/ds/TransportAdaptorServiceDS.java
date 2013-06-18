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

package org.wso2.carbon.transport.adaptor.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorRegistrationService;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.core.internal.CarbonTransportAdaptorRegistrationService;
import org.wso2.carbon.transport.adaptor.core.internal.CarbonTransportAdaptorService;

/**
 * @scr.component name="transport.adaptor.service.component" immediate="true"
 */


public class TransportAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(TransportAdaptorServiceDS.class);

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            TransportAdaptorRegistrationService transportAdaptorRegistrationService = new CarbonTransportAdaptorRegistrationService();
            TransportAdaptorServiceValueHolder.registerTransportAdaptorRegistrationService(transportAdaptorRegistrationService);

            TransportAdaptorService transportAdaptorService = new CarbonTransportAdaptorService();
            TransportAdaptorServiceValueHolder.registerCarbonTransportService(transportAdaptorService);

            context.getBundleContext().registerService(TransportAdaptorRegistrationService.class.getName(), transportAdaptorRegistrationService, null);
            context.getBundleContext().registerService(TransportAdaptorService.class.getName(), transportAdaptorService, null);

            log.info("Successfully deployed the transport adaptor service");
        } catch (RuntimeException e) {
            log.error("Can not create transport adaptor service ", e);
        }
    }


}
