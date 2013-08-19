/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.input.transport.adaptor.manager.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.List;

public interface InputTransportAdaptorManagerService {

    /**
     * use to add a new Transport Configuration instance to the system. A Transport Configuration instance represents the
     * details of a particular transport connection details.
     *
     * @param transportAdaptorConfiguration - transport adaptor configuration to be added
     */
    public void deployInputTransportAdaptorConfiguration(
            InputTransportAdaptorConfiguration transportAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * removes the transport configuration instance from the system.
     *
     * @param name              - transport configuration to be removed
     */
    public void undeployActiveInputTransportAdaptorConfiguration(String name,
                                                                 AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * This method used to delete the un-deployed Transport Adaptor File
     *
     * @param filePath FilePath of the Adaptor File that going to be deleted
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    public void undeployInactiveInputTransportAdaptorConfiguration(String filePath,
                                                                   AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * To edit a transport adaptor configuration
     *
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    public void editActiveInputTransportAdaptorConfiguration(String transportAdaptorConfiguration,
                                                             String transportAdaptorName,
                                                             AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException;


    /**
     * Used to save not deployed transport adaptor configuration details
     *
     * @param transportAdaptorConfiguration configuration details
     * @param filePath                      file path of the adaptor
     * @param axisConfiguration
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    public void editInactiveInputTransportAdaptorConfiguration(
            String transportAdaptorConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * getting all the transport proxy instance deatils. this is used to dispaly all the
     * transport configuration instances.
     * @return - list of available transport configuration
     */
    public List<InputTransportAdaptorConfiguration> getAllActiveInputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * retuns the transport configuration for the given name
     *
     * @param name     - transport configuration name
     * @return - transport configuration
     */
    public InputTransportAdaptorConfiguration getActiveInputTransportAdaptorConfiguration(
            String name,
            int tenantId)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of InputTransportAdaptorFile
     */
    public List<InputTransportAdaptorFile> getAllInactiveInputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration);

    /**
     * Use to get the transport adaptor configuration file
     *
     * @param transportAdaptorName transport adaptor name
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    public String getActiveInputTransportAdaptorConfigurationContent(String transportAdaptorName,
                                                                     AxisConfiguration axisConfiguration)
            throws InputTransportAdaptorManagerConfigurationException;


    /**
     * get the transport adaptor file of the not deployed adaptor
     *
     * @param filePath file path of the not deployed transport adaptor
     * @throws org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException
     *
     */
    public String getInactiveInputTransportAdaptorConfigurationContent(String filePath)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * Method used enable or disable the statistics for a input transport adaptor configuration
     * @throws InputTransportAdaptorManagerConfigurationException
     */
    public void setStatisticsEnabled(String transportAdaptorName,
                                     AxisConfiguration axisConfiguration, boolean flag)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * Method used to enable or disable tracing for input transport adaptor configuration
     * @throws InputTransportAdaptorManagerConfigurationException
     */
    public void setTracingEnabled(String transportAdaptorName, AxisConfiguration axisConfiguration,
                                  boolean flag)
            throws InputTransportAdaptorManagerConfigurationException;

    /**
     * To get the list of information about the IN transport adaptors
     *
     * @param tenantId tenant Id
     */
    public List<InputTransportAdaptorInfo> getInputTransportAdaptorInfo(int tenantId);

    /**
     * Method used to register a input transport adaptor deployment notifier
     * @throws InputTransportAdaptorManagerConfigurationException
     */
    public void registerDeploymentNotifier(
            InputTransportAdaptorNotificationListener inputTransportAdaptorNotificationListener)
            throws InputTransportAdaptorManagerConfigurationException;
}
