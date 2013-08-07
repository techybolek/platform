package org.wso2.carbon.input.transport.adaptor.manager.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.manager.admin.internal.util.InputTransportAdaptorHolder;
import org.wso2.carbon.input.transport.adaptor.manager.admin.internal.util.InputTransportAdaptorManagerHolder;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * this class is used to get the TransportAdaptorManager service.
 *
 * @scr.component name="input.transport.adaptor.manager.admin.component" immediate="true"
 * @scr.reference name="input.transport.adaptor.manager.service"
 * interface="org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorManagerService" unbind="unSetTransportAdaptorManagerService"
 * @scr.reference name="input.transport.adaptor.service"
 * interface="org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unSetTransportAdaptorService"
 */
public class InputTransportAdaptorManagerAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setTransportAdaptorManagerService(
            InputTransportAdaptorManagerService inputTransportAdaptorManagerService) {
        InputTransportAdaptorManagerHolder.getInstance().registerTransportAdaptorManagerService(inputTransportAdaptorManagerService);
    }

    protected void unSetTransportAdaptorManagerService(
            InputTransportAdaptorManagerService inputTransportAdaptorManagerService) {
        InputTransportAdaptorManagerHolder.getInstance().unRegisterTransportAdaptorManagerService(inputTransportAdaptorManagerService);
    }

    protected void setTransportAdaptorService(InputTransportAdaptorService inputTransportAdaptorService) {
        InputTransportAdaptorHolder.getInstance().registerTransportService(inputTransportAdaptorService);
    }

    protected void unSetTransportAdaptorService(InputTransportAdaptorService transportService) {
        InputTransportAdaptorHolder.getInstance().unRegisterTransportService(transportService);
    }
}
