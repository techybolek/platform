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

package org.wso2.carbon.output.transport.adaptor.manager.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;

import java.util.List;

public interface OutputTransportAdaptorManagerService {

    /**
     * use to add a new Transport Configuration instance to the system. A Transport Configuration instance represents the
     * details of a particular transport connection details.
     *
     * @param transportAdaptorConfiguration - transport adaptor configuration to be added
     * @param axisConfiguration
     */

    public void saveOutputTransportAdaptorConfiguration(
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * removes the transport configuration instance from the system.
     *
     * @param name              - transport configuration to be removed
     * @param axisConfiguration
     */
    public void removeOutputTransportAdaptorConfiguration(String name,
                                                          AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * getting all the transport proxy instance deatils. this is used to dispaly all the
     * transport configuration instances.
     *
     * @param axisConfiguration
     * @return - list of available transport configuration
     */
    public List<OutputTransportAdaptorConfiguration> getAllOutputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;


    /**
     * retuns the transport configuration for the given name
     *
     * @param name     - transport configuration name
     * @param tenantId
     * @return - transport configuration
     */
    public OutputTransportAdaptorConfiguration getOutputTransportAdaptorConfiguration(String name,
                                                                                      int tenantId)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of OutputTransportAdaptorFile
     */
    public List<OutputTransportAdaptorFile> getNotDeployedOutputTransportAdaptorConfigurationFiles(
            AxisConfiguration axisConfiguration);

    /**
     * This method used to delete the un-deployed Transport Adaptor File
     *
     * @param filePath FilePath of the Adaptor File that going to be deleted
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void removeOutputTransportAdaptorConfigurationFile(String filePath,
                                                              AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * Use to get the transport adaptor configuration file
     *
     * @param transportAdaptorName transport adaptor name
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public String getOutputTransportAdaptorConfigurationFile(String transportAdaptorName,
                                                             AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * To edit a transport adaptor configuration
     *
     * @param transportAdaptorConfiguration
     * @param transportAdaptorName
     * @param axisConfiguration
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void editOutputTransportAdaptorConfigurationFile(String transportAdaptorConfiguration,
                                                            String transportAdaptorName,
                                                            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;


    /**
     * To get the list of information about the OUT transport adaptors
     *
     * @param tenantId
     * @return
     */
    public List<OutputTransportAdaptorInfo> getOutputTransportAdaptorInfo(int tenantId);

    /**
     * get the transport adaptor file of the not deployed adaptor
     *
     * @param filePath file path of the not deployed transport adaptor
     * @return
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public String getNotDeployedOutputTransportAdaptorConfigurationFile(String filePath)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * Used to save not deployed transport adaptor configuration details
     *
     * @param transportAdaptorConfiguration configuration details
     * @param filePath                      file path of the adaptor
     * @param axisConfiguration
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void editNotDeployedOutputTransportAdaptorConfigurationFile(
            String transportAdaptorConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    void registerDeploymentNotifier(
            OutputTransportAdaptorNotificationListener outputTransportAdaptorNotificationListener)
            throws OutputTransportAdaptorManagerConfigurationException;

    public void disableStatistics(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    public void enableStatistics(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    public void disableTracing(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    public void enableTracing(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

}
