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

package org.wso2.carbon.input.transport.adaptor.wsevent.local.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorFactory;
import org.wso2.carbon.input.transport.adaptor.wsevent.local.WSEventLocalTransportAdaptorFactory;


/**
 * @scr.component name="input.WSEventLocalTransportAdaptorService.component" immediate="true"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker" cardinality="1..1"
 * policy="dynamic" bind="setWSEventTransportAdaptorService" unbind="unSetWSEventTransportAdaptorService"
 */


public class WSEventLocalTransportAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(WSEventLocalTransportAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputTransportAdaptorFactory wsEventLocalTransportAdaptorFactory = new WSEventLocalTransportAdaptorFactory();
            context.getBundleContext().registerService(InputTransportAdaptorFactory.class.getName(), wsEventLocalTransportAdaptorFactory, null);
            log.info("Successfully deployed the input WSEventLocal transport service");
        } catch (RuntimeException e) {
            log.error("Can not create the input WSEventLocal transport service ", e);
        }
    }

    protected void setWSEventTransportAdaptorService(EventBroker eventBroker) {
        WSEventLocalTransportAdaptorServiceValueHolder.registerEventBroker(eventBroker);
    }

    protected void unSetWSEventTransportAdaptorService(EventBroker eventBroker) {

    }

}
