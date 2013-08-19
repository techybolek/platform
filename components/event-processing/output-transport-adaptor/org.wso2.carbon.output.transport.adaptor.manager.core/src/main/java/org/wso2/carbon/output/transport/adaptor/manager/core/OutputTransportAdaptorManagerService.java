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
     */

    public void deployOutputTransportAdaptorConfiguration(
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * removes the transport configuration instance from the system.
     */
    public void undeployActiveOutputTransportAdaptorConfiguration(String name,
                                                                  AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * getting all the transport proxy instance deatils. this is used to dispaly all the
     * transport configuration instances.
     *
     * @return - list of available transport configuration
     */
    public List<OutputTransportAdaptorConfiguration> getAllActiveOutputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;


    /**
     * retuns the transport configuration for the given name
     *
     * @return - transport configuration
     */
    public OutputTransportAdaptorConfiguration getActiveOutputTransportAdaptorConfiguration(
            String name,
            int tenantId)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of OutputTransportAdaptorFile
     */
    public List<OutputTransportAdaptorFile> getAllInactiveOutputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration);

    /**
     * This method used to delete the un-deployed Transport Adaptor File
     *
     * @param filePath FilePath of the Adaptor File that going to be deleted
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void undeployInactiveOutputTransportAdaptorConfiguration(String filePath,
                                                                    AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * To edit a transport adaptor configuration
     *
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void editActiveOutputTransportAdaptorConfiguration(String transportAdaptorConfiguration,
                                                              String transportAdaptorName,
                                                              AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;


    /**
     * Used to save not deployed transport adaptor configuration details
     *
     * @param transportAdaptorConfiguration configuration details
     * @param filePath                      file path of the adaptor
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void editInactiveOutputTransportAdaptorConfiguration(
            String transportAdaptorConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * Use to get the transport adaptor configuration file
     *
     * @param transportAdaptorName transport adaptor name
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public String getActiveOutputTransportAdaptorConfigurationContent(String transportAdaptorName,
                                                                      AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * get the transport adaptor file of the not deployed adaptor
     *
     * @param filePath file path of the not deployed transport adaptor
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public String getInactiveOutputTransportAdaptorConfigurationContent(String filePath)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * Method used to enable or disable the statistics for a output transport adaptor configuration
     *
     * @throws OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void setStatisticsEnabled(String transportAdaptorName,
                                     AxisConfiguration axisConfiguration, boolean flag)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * Method used to enable or disable the tracing for a output transport adaptor configuration
     *
     * @throws OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void setTracingEnabled(String transportAdaptorName, AxisConfiguration axisConfiguration,
                                  boolean flag)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * Method used to register output transport adaptor notification listener object
     *
     * @throws OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void registerDeploymentNotifier(
            OutputTransportAdaptorNotificationListener outputTransportAdaptorNotificationListener)
            throws OutputTransportAdaptorManagerConfigurationException;

    /**
     * To get the list of information about the OUT transport adaptors
     */
    public List<OutputTransportAdaptorInfo> getOutputTransportAdaptorInfo(int tenantId);

}
