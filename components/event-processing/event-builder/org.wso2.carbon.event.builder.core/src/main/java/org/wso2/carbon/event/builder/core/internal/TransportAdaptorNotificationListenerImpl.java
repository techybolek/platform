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

package org.wso2.carbon.event.builder.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorNotificationListener;


public class TransportAdaptorNotificationListenerImpl implements
                                                      InputTransportAdaptorNotificationListener {

    private static final Log log = LogFactory.getLog(TransportAdaptorNotificationListenerImpl.class);


    @Override
    public void configurationAdded(int tenantId, String transportAdaptorName) {
        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        try {
            carbonEventBuilderService.activateInactiveEventBuilderConfigurations(transportAdaptorName, tenantId);
        } catch (EventBuilderConfigurationException e) {
            log.error("Exception occurred while deploying the Event Builder configuration files");
        }
    }

    @Override
    public void configurationRemoved(int tenantId,
                                     InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration) {
        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        try {
            carbonEventBuilderService.deactivateActiveEventBuilderConfigurations(inputTransportAdaptorConfiguration, tenantId);
        } catch (EventBuilderConfigurationException e) {
            log.error("Exception occurred while deploying the Event Builder configuration files");
        }
    }

}
