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
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

import java.util.List;
import java.util.Map;

public interface EventProcessorService {

    /**
     * Adds a new execution plan to the system.
     *
     * @param executionPlanConfiguration new execution plan configuration.
     */
    public void deployExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            AxisConfiguration axisConfiguration)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException;

    /**
     * Removes execution plan from the system
     *
     * @param filePath
     * @param axisConfiguration
     */
    public void undeployInactiveExecutionPlanConfiguration(String filePath,
                                                           AxisConfiguration axisConfiguration) throws
                                                                                                ExecutionPlanConfigurationException;

    /**
     * Removes execution plan from the system
     *
     * @param name
     * @param axisConfiguration
     */
    public void undeployActiveExecutionPlanConfiguration(String name,
                                                         AxisConfiguration axisConfiguration) throws
                                                                                              ExecutionPlanConfigurationException;

    /**
     * Edits execution plan from the system
     *
     * @param executionPlanConfiguration
     * @param executionPlanName
     * @param axisConfiguration
     */
    public void editActiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                     String executionPlanName,
                                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;


    /**
     * Edits execution plan from the system
     *
     * @param executionPlanConfiguration
     * @param filePath
     * @param axisConfiguration
     */
    public void editInactiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                       String filePath,
                                                       AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;


    public String getActiveExecutionPlanConfigurationContent(String name, int tenantId)
            throws ExecutionPlanConfigurationException;


    public String getInactiveExecutionPlanConfigurationContent(String path, int tenantId)
            throws ExecutionPlanConfigurationException;


    /**
     * Gets all available active execution plan configurations.
     *
     * @return
     */
    public Map<String, ExecutionPlanConfiguration> getAllActiveExecutionConfigurations(int tenantId);

    /**
     * Gets a active execution plan configurations.
     *
     * @return
     */
    public ExecutionPlanConfiguration getActiveExecutionConfiguration(String name, int tenantId);


    /**
     * Gets all available inactive execution plan configurations files.
     *
     * @return
     */
    public List<ExecutionPlanConfigurationFile> getAllInactiveExecutionPlanConfiguration(
            int tenantId);


    void setTracingEnabled(String executionPlanName, boolean isEnabled,
                           AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    void setStatisticsEnabled(String executionPlanName, boolean isEnabled,
                              AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;


    public List<String> getStreamIds(int tenantId);

    public StreamDefinition getStreamDefinition(String streamId, int tenantId);
}
