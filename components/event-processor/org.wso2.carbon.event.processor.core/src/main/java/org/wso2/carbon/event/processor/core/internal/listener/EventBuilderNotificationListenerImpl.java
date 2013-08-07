/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.processor.core.internal.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.core.EventBuilderNotificationListener;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;

public class EventBuilderNotificationListenerImpl implements EventBuilderNotificationListener {

    private static final Log log = LogFactory.getLog(EventBuilderNotificationListenerImpl.class);

    @Override
    public void tryRedeployingEventProcessorConfigurationFiles(int tenantId) {
        try {
            EventProcessorValueHolder.getEventProcessorService().redeployFailedExecutionPlans(tenantId);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void configurationAdded(int tenantId, String nameWithVersion) {
        try {
            log.info("Trying to redeploy configuration files for stream: " + nameWithVersion);
            EventProcessorValueHolder.getEventProcessorService().onNewEventBuilderDeployment(tenantId, nameWithVersion);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void configurationRemoved(int tenantId, String nameWithVersion) {
        try {
            log.info("Trying to undeploy execution plans for stream: " + nameWithVersion);
            EventProcessorValueHolder.getEventProcessorService().unDeployExecutionPlansForInputStream(tenantId, nameWithVersion);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
}
