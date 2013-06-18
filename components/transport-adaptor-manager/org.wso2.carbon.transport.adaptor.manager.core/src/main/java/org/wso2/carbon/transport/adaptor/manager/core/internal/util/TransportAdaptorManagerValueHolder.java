package org.wso2.carbon.transport.adaptor.manager.core.internal.util;/*
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


import org.wso2.carbon.transport.adaptor.manager.core.internal.CarbonTransportAdaptorManagerService;

/**
 * To hold the transport adaptor manager service
 */
public class TransportAdaptorManagerValueHolder {

    private static CarbonTransportAdaptorManagerService carbonTransportAdaptorManagerService;

    private TransportAdaptorManagerValueHolder(){}

    public static void registerCarbonTransportAdaptorManagerService(
            CarbonTransportAdaptorManagerService carbonTransportAdaptorManagerService) {

        TransportAdaptorManagerValueHolder.carbonTransportAdaptorManagerService = carbonTransportAdaptorManagerService;
    }

    public static CarbonTransportAdaptorManagerService getCarbonTransportAdaptorManagerService() {
        return TransportAdaptorManagerValueHolder.carbonTransportAdaptorManagerService;
    }
}
