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

package org.wso2.carbon.event.builder.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigurationFile;

import java.util.List;

public interface EventBuilderService {

    /**
     * Subscribes to a particular event builder identified by the stream definition
     *
     * @param streamDefinition
     * @param eventListener
     */
    public void subscribe(StreamDefinition streamDefinition, EventListener eventListener,
                          int tenantId)
            throws EventBuilderConfigurationException;

    /**
     * Unsubscribes from a particular event builder for the given stream definition and event listener
     *
     * @param streamDefinition
     * @param eventListener
     * @param tenantId
     */
    public void unsubsribe(StreamDefinition streamDefinition, EventListener eventListener,
                           int tenantId);

    /**
     * Used to add a new Event Builder Configuration instance to the system. An event builder configuration instance represents the
     * details of a particular event builder.
     *
     * @param eventBuilderConfiguration - event builder configuration to be added
     * @param axisConfiguration         - axisConfiguration of caller
     */
    public void addEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Removes the event builder configuration instance from the system.
     *
     * @param eventBuilderConfiguration - eventBuilderConfiguration of the event builder configuration to be removed
     * @param tenantId
     */
    public void removeEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                   int tenantId)
            throws EventBuilderConfigurationException;

    /**
     * Getting all the event builder configuration instance details.
     *
     * @param axisConfiguration
     * @return - list of available transport configuration
     */
    public List<EventBuilderConfiguration> getAllEventBuilderConfigurations(
            AxisConfiguration axisConfiguration);

    /**
     * Returns the event builder configuration for the given streamId
     *
     * @param streamId          - streamId associated with the required event builder configuration
     * @param axisConfiguration
     * @return - transport configuration
     */
    public EventBuilderConfiguration getEventBuilderConfiguration(String streamId,
                                                                  AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Returns the {@link EventBuilderConfiguration} for the event builder with given name
     *
     * @param eventBuilderName  the event builder name
     * @param axisConfiguration
     * @return {@link EventBuilderConfiguration} that is associated with the event builder of the name passed in
     */
    public EventBuilderConfiguration getEventBuilderConfigurationFromName(String eventBuilderName,
                                                                          AxisConfiguration axisConfiguration);

    /**
     * Returns a {@link List} of stream definition ids.
     *
     * @param tenantId the tenant id
     * @return A {@link List} of stream definition ids as Strings
     */
    public List<String> getStreamDefinitionsAsString(int tenantId);

    /**
     * Returns a list of supported mapping types
     *
     * @param transportAdaptorType
     * @param tenantId
     * @return a list of strings that represent supported mappings by the EventBuilderService
     */
    public List<String> getSupportedInputMappingTypes(String transportAdaptorType, int tenantId);

    /**
     * Returns a {@link List} of {@link StreamDefinition} objects that are currently accessible for the specified AxisConfiguration
     *
     * @return
     */
    public List<StreamDefinition> getStreamDefinitions(int tenantId);

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of EventBuilderConfigurationFile
     */
    public List<EventBuilderConfigurationFile> getUndeployedFiles(
            AxisConfiguration axisConfiguration);

    /**
     * @return
     */
    public String getEventBuilderConfigurationXml(String eventBuilderName, int tenantId);

    /**
     * When a new OSGi service that needs EventBuilderService becomes active, it must
     * register by providing a notification listener once.
     *
     * @param eventBuilderNotificationListener
     *
     */
    public void registerEventBuilderNotificationListener(
            EventBuilderNotificationListener eventBuilderNotificationListener);

    /**
     * @param eventBuilderConfigXml
     * @param axisConfiguration
     */
    public void updateEventBuilder(String eventBuilderConfigXml,
                                   String originalEventBuilderName,
                                   AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * @param eventBuilderName
     * @param tenantId
     */
    public void removeEventBuilderConfigurationFile(String eventBuilderName, int tenantId);

    /**
     * @param originalEventBuilderName
     * @param updatedEventBuilderConfiguration
     *
     * @param axisConfiguration
     */
    public void updateEventBuilder(String originalEventBuilderName,
                                   EventBuilderConfiguration updatedEventBuilderConfiguration,
                                   AxisConfiguration axisConfiguration);
}
