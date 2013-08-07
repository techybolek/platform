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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorFactory;
import org.wso2.carbon.input.transport.adaptor.core.internal.CarbonInputTransportAdaptorService;

/**
 * @scr.component name="input.transport.adaptor.service.tracker.component" immediate="true"
 * @scr.reference name="input.transport.adaptor.tracker.service"
 * interface="org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorFactory" cardinality="0..n"
 * policy="dynamic" bind="setTransportAdaptorType" unbind="unSetTransportAdaptorType"
 */

public class InputTransportAdaptorServiceTrackerDS {

    private static final Log log = LogFactory.getLog(InputTransportAdaptorServiceTrackerDS.class);

    /**
     * initialize the Transport Adaptor Manager core service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        try {
            log.info("Successfully deployed the input transport adaptor tracker service");
        } catch (RuntimeException e) {
            log.error("Can not create the input transport adaptor tracker service ", e);
        }
    }

    protected void setTransportAdaptorType(
            InputTransportAdaptorFactory inputTransportAdaptorFactory) {
        try {
            ((CarbonInputTransportAdaptorService) InputTransportAdaptorServiceValueHolder.getCarbonInputTransportAdaptorService()).registerTransportAdaptor(inputTransportAdaptorFactory.getTransportAdaptor());
        } catch (Throwable t) {
            log.error(t);
        }
    }

    protected void unSetTransportAdaptorType(
            InputTransportAdaptorFactory inputTransportAdaptorFactory) {
        ((CarbonInputTransportAdaptorService) InputTransportAdaptorServiceValueHolder.getCarbonInputTransportAdaptorService()).unRegisterTransportAdaptor(inputTransportAdaptorFactory.getTransportAdaptor());
    }
}

