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
package org.wso2.carbon.transport.adaptor.manager.core.internal.util;


import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;

/**
 * This method is used to hold the transport adaptor service
 */
public class TransportAdaptorHolder {

    private static TransportAdaptorHolder instance = new TransportAdaptorHolder();

    private TransportAdaptorService transportAdaptorService;

    public static TransportAdaptorHolder getInstance() {
        return instance;
    }

    public void setTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        this.transportAdaptorService = transportAdaptorService;
    }

    public void unSetTransportAdaptorService() {
        this.transportAdaptorService = null;
    }

    public TransportAdaptorService getTransportAdaptorService() {
        return this.transportAdaptorService;
    }
}
