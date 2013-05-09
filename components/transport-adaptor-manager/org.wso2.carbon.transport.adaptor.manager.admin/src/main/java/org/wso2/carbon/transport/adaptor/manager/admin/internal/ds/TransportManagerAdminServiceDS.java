package org.wso2.carbon.transport.adaptor.manager.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.manager.admin.internal.util.TransportHolder;
import org.wso2.carbon.transport.adaptor.manager.admin.internal.util.TransportManagerHolder;
import org.wso2.carbon.transport.adaptor.manager.core.TransportManagerService;

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
 * this class is used to get the BrokerManager service.
 *
 * @scr.component name="transportmanageradmin.component" immediate="true"
 * @scr.reference name="transportmanager.service"
 * interface="org.wso2.carbon.transport.adaptor.manager.core.TransportManagerService" cardinality="1..1"
 * policy="dynamic" bind="setTransportManagerService" unbind="unSetTransportManagerService"
 * @scr.reference name="transportservice.service"
 * interface="org.wso2.carbon.transport.adaptor.core.TransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportService" unbind="unSetTransportService"
 */
public class TransportManagerAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setTransportManagerService(TransportManagerService transportManagerService) {
        TransportManagerHolder.getInstance().registerTransportManagerService(transportManagerService);
    }

    protected void unSetTransportManagerService(TransportManagerService transportManagerService) {
        TransportManagerHolder.getInstance().unRegisterTransportManagerService(transportManagerService);
    }

    protected void setTransportService(TransportAdaptorService transportService) {
        TransportHolder.getInstance().registerTransportService(transportService);
    }

    protected void unSetTransportService(TransportAdaptorService transportService) {
        TransportHolder.getInstance().unRegisterTransportService(transportService);
    }
}
