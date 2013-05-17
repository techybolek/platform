package org.wso2.carbon.transport.adaptor.manager.admin.internal.util;


import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;

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
 * This class is used to hold the transport adaptor service
 */
public final class TransportAdaptorHolder {
    private TransportAdaptorService transportAdaptorService;
    private static TransportAdaptorHolder instance = new TransportAdaptorHolder();

    private TransportAdaptorHolder() {
    }

    public TransportAdaptorService getTransportAdaptorService() {
        return transportAdaptorService;
    }

    public static TransportAdaptorHolder getInstance() {
        return instance;
    }

    public void registerTransportService(TransportAdaptorService transportService) {
        this.transportAdaptorService = transportService;
    }

    public void unRegisterTransportService(TransportAdaptorService transportService) {
        this.transportAdaptorService = null;
    }
}
