package org.wso2.carbon.output.transport.adaptor.manager.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.manager.admin.internal.util.OutputTransportAdaptorHolder;
import org.wso2.carbon.output.transport.adaptor.manager.admin.internal.util.OutputTransportAdaptorManagerHolder;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;

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
 * @scr.component name="output.transport.adaptor.manager.admin.component" immediate="true"
 * @scr.reference name="output.transport.adaptor.manager.service"
 * interface="org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorManagerService" unbind="unSetTransportAdaptorManagerService"
 * @scr.reference name="output.transport.adaptor.service"
 * interface="org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unSetTransportAdaptorService"
 */
public class OutputTransportAdaptorManagerAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setTransportAdaptorManagerService(
            OutputTransportAdaptorManagerService outputTransportAdaptorManagerService) {
        OutputTransportAdaptorManagerHolder.getInstance().registerTransportAdaptorManagerService(outputTransportAdaptorManagerService);
    }

    protected void unSetTransportAdaptorManagerService(
            OutputTransportAdaptorManagerService outputTransportAdaptorManagerService) {
        OutputTransportAdaptorManagerHolder.getInstance().unRegisterTransportAdaptorManagerService(outputTransportAdaptorManagerService);
    }

    protected void setTransportAdaptorService(
            OutputTransportAdaptorService outputTransportAdaptorService) {
        OutputTransportAdaptorHolder.getInstance().registerTransportService(outputTransportAdaptorService);
    }

    protected void unSetTransportAdaptorService(OutputTransportAdaptorService transportService) {
        OutputTransportAdaptorHolder.getInstance().unRegisterTransportService(transportService);
    }
}
