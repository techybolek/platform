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
package org.wso2.carbon.event.builder.test.util;

import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * singleton class to hold the transport adaptor service
 */
public final class TestEventBuilderServiceHolder {
    private static TestEventBuilderServiceHolder instance = new TestEventBuilderServiceHolder();
    private EventBuilderService eventBuilderService;
    private TransportAdaptorService transportAdaptorService;
    private ConfigurationContextService configurationContextService;

    private TestEventBuilderServiceHolder() {
    }

    public static TestEventBuilderServiceHolder getInstance() {
        return instance;
    }

    public TransportAdaptorService getTransportAdaptorService() {
        return transportAdaptorService;
    }

    public EventBuilderService getEventBuilderService() {
        return eventBuilderService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void registerEventBuilderService(EventBuilderService eventBuilderService) {
        this.eventBuilderService = eventBuilderService;
    }

    public void unregisterEventBuilderService(EventBuilderService eventBuilderService) {
        this.eventBuilderService = null;
    }

    public void registerTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        this.transportAdaptorService = transportAdaptorService;
    }

    public void unregisterTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        this.transportAdaptorService = null;
    }

    public void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public void unregisterConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = null;
    }


}
