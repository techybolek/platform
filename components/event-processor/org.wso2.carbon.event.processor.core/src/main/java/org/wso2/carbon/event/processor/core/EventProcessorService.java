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

package org.wso2.carbon.event.processor.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

import java.util.List;
import java.util.Map;

public interface EventProcessorService {

    /**
     * Adds a new query plan.
     *
     * @param executionPlanConfiguration new query plan configuration.
     */
    public boolean addExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration, int tenantId)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException;

    /**
     * Removes an existing query plan configuration.
     *
     * @param executionPlanConfiguration existing query plan configuration.
     */
    public boolean removeExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration, int tenantId);

    /**
     * Gets an existing query plan configuraiton
     *
     * @param name given name of the query plan.
     * @return the query plan configuration if it exists.
     */
    public ExecutionPlanConfiguration getExecutionPlanConfiguration(String name, int tenantId);

    /**
     * Gets all available query plan configurations.
     *
     * @return
     */
    public Map<String, ExecutionPlanConfiguration> getAllExecutionPlanConfigurations(int tenantId);


    /**
     * Adds an event formatter listener to the specified stream. It subscribes to all query plans that contains the specified
     * output stream.
     *
     * @param streamId output stream to subscribe to
     * @param listener output event  listener.
     * @param tenantId
     * @return true if successful, false if the listener instance already exists.
     */
    public boolean addEventFormatterListener(String streamId, EventFormatterListener listener,
                                             int tenantId);


    /**
     * Removes the given event listener from the specified stream.
     *
     * @param streamId
     * @param listener
     * @param tenantId
     * @return true if successful.
     */
    public boolean removeEventFormatterListener(String streamId, EventFormatterListener listener,
                                                int tenantId);


    public ExecutionPlanConfigurationFile getExecutionPlanConfigurationFileByName(String name,
                                                                                  int tenantId);

    public ExecutionPlanConfigurationFile getExecutionPlanConfigurationFile(String path,
                                                                            int tenantId);

    public void removeExecutionPlanConfigurationFile(
            ExecutionPlanConfigurationFile executionPlanConfigurationFile,
            int tenantId);

    public void addExecutionPlanConfigurationFile(ExecutionPlanConfigurationFile configurationFile,
                                                  int tenantId);

    public List<String> getStreamNames(int tenantId);

    public StreamDefinition getStreamDefinition(int tenantId, String streamWithVersion);

    public void saveExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;


    public List<ExecutionPlanConfigurationFile> getAllExecutionPlanConfigurationFiles(int tenantId);

    public List<String> getAllExecutionPlanConfigurationFileNames(int tenantId);

    public List<ExecutionPlanConfigurationFile> getFailedExecutionPlanConfigurationFiles(
            int tenantId);


    public String getExecutionPlanConfigurationFileContent(int tenantId, String name)
            throws ExecutionPlanConfigurationException;


    public void editExecutionPlanConfigurationFile(String executionPlanConfiguration,
                                                   String executionPlanName,
                                                   AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;


    public void editNotDeployedExecutionPlanConfigurationFile(
            String executionPlanConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;


    public String getNotDeployedExecutionPlanConfigurationFileContent(int tenantId, String path)
            throws ExecutionPlanConfigurationException;

    void setTracingEnabled(String executionPlanName, boolean isEnabled,
                           AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    void setStatisticsEnabled(String executionPlanName, boolean isEnabled,
                              AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    boolean isTracingEnabled(String executionPlanName, int tenantId)
            throws ExecutionPlanConfigurationException;

    boolean isStatisticsEnabled(String executionPlanName, int tenantId)
            throws ExecutionPlanConfigurationException;
}
