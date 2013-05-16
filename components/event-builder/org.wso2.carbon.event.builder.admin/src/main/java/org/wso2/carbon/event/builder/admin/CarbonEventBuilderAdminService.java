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

package org.wso2.carbon.event.builder.admin;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.builder.admin.internal.util.EventBuilderAdminServiceValueHolder;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorInfo;

import java.util.List;

public class CarbonEventBuilderAdminService extends AbstractAdmin {

    public CarbonEventBuilderAdminService() {
    }

    public String[] getInputTransportNames(int tenantId) throws AxisFault {
        String[] inputTransportNames = null;
        TransportAdaptorManagerService transportAdaptorManagerService = EventBuilderAdminServiceValueHolder.getTransportAdaptorManagerService();
        List<TransportAdaptorInfo> transportAdaptorInfoList = transportAdaptorManagerService.getInputTransportAdaptorInfo(tenantId);
        if (!transportAdaptorInfoList.isEmpty()) {
            inputTransportNames = new String[transportAdaptorInfoList.size()];
            for (int i = 0; i < transportAdaptorInfoList.size(); i++) {
                inputTransportNames[i] = transportAdaptorInfoList.get(i).getTransportAdaptorName();
            }
        }

        return inputTransportNames;
    }

}