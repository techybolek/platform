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
import org.wso2.carbon.event.builder.core.internal.config.EventBuilderConfigurationFile;

import java.util.List;

public interface EventBuilderService {

    /**
     * Subscribes to a particular event builder identified by the stream definition. After subscribing, the passed in
     * event listener will receive events from the event builder associated with the event builder
     *
     * @param streamDefinitionId the stream definition id of the {@link StreamDefinition} that is associated with the event builder
     * @param wso2EventListener  {@link Wso2EventListener} that will listen to events from a particular event builder
     * @param tenantId           tenant id of the particular event builder
     */
    public void subscribe(String streamDefinitionId, Wso2EventListener wso2EventListener,
                          int tenantId)
            throws EventBuilderConfigurationException;

    /**
     * Unsubscribes from a particular event builder for the given stream definition and event listener
     *
     * @param streamDefinitionId the stream definition id of {@link StreamDefinition} that is associated with the event builder
     * @param wso2EventListener  the {@link Wso2EventListener} that needs to be unsubscribed
     * @param tenantId           tenant id of the particular event builder
     */
    public void unsubsribe(String streamDefinitionId, Wso2EventListener wso2EventListener,
                           int tenantId);

    /**
     * Subscribes to a particular event builder identified by the stream definition. After subscribing, the passed in
     * event listener will receive events from the event builder associated with the event builder
     *
     * @param streamDefinitionId the stream definition id of {@link StreamDefinition} that is associated with the event builder
     * @param basicEventListener {@link BasicEventListener} that will listen to events from a particular event builder
     * @param tenantId           tenant id of the particular event builder
     */
    public void subscribe(String streamDefinitionId, BasicEventListener basicEventListener,
                          int tenantId)
            throws EventBuilderConfigurationException;

    /**
     * Unsubscribes from a particular event builder for the given stream definition and event listener
     *
     * @param streamDefinitionId the stream definition id of {@link StreamDefinition} instance that is associated with the event builder
     * @param basicEventListener the {@link BasicEventListener} that needs to be unsubscribed
     * @param tenantId           tenant id of the particular event builder
     */
    public void unsubsribe(String streamDefinitionId, BasicEventListener basicEventListener,
                           int tenantId);


    /**
     * Updates the event builder with the given syntax
     *
     * @param eventBuilderConfigXml the XML configuration of the event builder as a string
     * @param axisConfiguration     the axis configuration of the particular tenant to which this event builder belongs
     */
    public void editInactiveEventBuilderConfiguration(String eventBuilderConfigXml,
                                                      String filename,
                                                      AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Updates the event builder according to the passed in {@link EventBuilderConfiguration}
     *
     * @param originalEventBuilderName the original name of the event builder
     * @param axisConfiguration        the axis configuration of the tenant which owns the event builder
     */
    public void editActiveEventBuilderConfiguration(String eventBuilderConfigXml,
                                                    String originalEventBuilderName,
                                                    AxisConfiguration axisConfiguration);

    /**
     * Getting all the event builder configuration instance details.
     *
     * @param tenantId@return - list of available transport configuration
     */
    public List<EventBuilderConfiguration> getAllActiveEventBuilderConfigurations(int tenantId);

    /**
     * Returns the {@link EventBuilderConfiguration} for the event builder with given name
     *
     * @param eventBuilderName the event builder name
     * @param tenantId         the tenant id
     * @return {@link EventBuilderConfiguration} that is associated with the event builder of the name passed in
     */
    public EventBuilderConfiguration getActiveEventBuilderConfiguration(String eventBuilderName,
                                                                        int tenantId);

    /**
     * Returns a {@link List} of stream definition ids.
     *
     * @param tenantId the tenant id
     * @return A {@link List} of stream definition ids as Strings
     */
    public List<String> getStreamDefinitionsAsString(int tenantId);

    /**
     * Returns a {@link List} of {@link StreamDefinition} objects that are currently accessible for the specified tenant id
     *
     * @return a list of all stream definitions for a particular tenant id
     */
    public List<StreamDefinition> getStreamDefinitions(int tenantId);

    /**
     * Returns a list of supported mapping types
     *
     * @param transportAdaptorName the transport adaptor name
     * @param tenantId             the tenant id to which this transport adaptor belongs to
     * @return a list of strings that represent supported mappings by the EventBuilderService
     */
    public List<String> getSupportedInputMappingTypes(String transportAdaptorName, int tenantId);

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of EventBuilderConfigurationFile
     */
    public List<EventBuilderConfigurationFile> getAllInactiveEventBuilderConfigurations(
            AxisConfiguration axisConfiguration);

    /**
     * Returns the event builder XML configuration for the given event builder name and tenant id
     *
     * @param eventBuilderName the name of the event builder
     * @param tenantId         the tenant id
     * @return the XML configuration syntax as a string
     */
    public String getActiveEventBuilderConfigurationContent(String eventBuilderName, int tenantId);

    /**
     * Returns the event builder XML configuration for the given filePath and tenant id
     *
     * @return the XML configuration syntax as a string
     */
    public String getInactiveEventBuilderConfigurationContent(String filename,
                                                              AxisConfiguration axisConfiguration);

    /**
     * When a new OSGi service that needs EventBuilderService becomes active, it must
     * register by providing a notification listener once.
     *
     * @param eventBuilderNotificationListener
     *         the instance that implements the {@link EventBuilderNotificationListener}
     */
    public void registerEventBuilderNotificationListener(
            EventBuilderNotificationListener eventBuilderNotificationListener);

    /**
     * Undeploys an active event builder configuration of the given name for the axis configuration
     * and deletes it from the file system.
     *
     * @param eventBuilderName  the event builder name
     * @param axisConfiguration the axis configuration
     */
    public void undeployActiveEventBuilderConfiguration(String eventBuilderName,
                                                        AxisConfiguration axisConfiguration);

    /**
     * Removes the event builder configuration file from the file system and memory
     *
     * @param filename          the name of the event builder configuration file
     * @param axisConfiguration the tenant id of the tenant which owns this event builder
     */
    public void undeployInactiveEventBuilderConfiguration(String filename,
                                                          AxisConfiguration axisConfiguration);

    /**
     * Deploys an event builder configuration and saves the associated configuration file to the filesystem.
     *
     * @param eventBuilderConfiguration the {@link EventBuilderConfiguration} object
     * @param axisConfiguration         the axis configuration
     */
    public void deployEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            AxisConfiguration axisConfiguration);

    public void setTraceEnabled(String eventBuilderName, boolean traceEnabled,
                                AxisConfiguration axisConfiguration);

    public void setStatisticsEnabled(String eventBuilderName, boolean statisticsEnabled,
                                     AxisConfiguration axisConfiguration);
}
