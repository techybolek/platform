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

package org.wso2.carbon.event.builder.core.config;


import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.EventListener;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;

public interface EventBuilder {

    /**
     * Returns the event builder configuration associated with this event builder
     *
     * @return the {@link EventBuilderConfiguration} instance
     */
    public EventBuilderConfiguration getEventBuilderConfiguration();

    /**
     * Subscribe to this particular event builder
     *
     * @param eventListener the {@link EventListener} instance that will be listening
     * @throws EventBuilderConfigurationException
     *
     */
    public void subscribe(EventListener eventListener)
            throws EventBuilderConfigurationException;

    /**
     * Unsubscribe this particular {@link EventListener}
     *
     * @param eventListener the {@link EventListener} instance that will unsubscribe
     */
    public void unsubscribe(EventListener eventListener);

    /**
     * Returns the stream definition that is exported by this event builder.
     * This stream definition will available to any object that consumes the event builder service
     * (e.g. EventProcessors)
     *
     * @return the {@link StreamDefinition} of the stream that will be
     *         sending out events from this event builder
     */
    public StreamDefinition getExportedStreamDefinition();


    public void unsubscribeFromTransportAdaptor(
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration);

    public void subscribeToTransportAdaptor();

    public void loadEventBuilderConfiguration();
}
