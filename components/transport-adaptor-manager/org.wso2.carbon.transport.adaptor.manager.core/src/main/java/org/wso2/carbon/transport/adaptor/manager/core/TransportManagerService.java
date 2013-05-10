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

package org.wso2.carbon.transport.adaptor.manager.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TransportManagerConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorInfo;

import java.util.List;
import java.util.Map;

public interface TransportManagerService {

    /**
     * use to add a new Transport Configuration instance to the system. A Transport Configuration instance represents the
     * details of a particular transport connection details.
     *
     * @param transportAdaptorConfiguration - transport adaptor configuration to be added
     * @param axisConfiguration
     */

     public void saveTransportConfiguration(TransportAdaptorConfiguration transportAdaptorConfiguration,
                                           AxisConfiguration axisConfiguration) throws TransportManagerConfigurationException;

    /**
     * removes the transport configuration instance from the system.
     *
     * @param name              - transport configuration to be removed
     * @param axisConfiguration
     */
    public void removeTransportConfiguration(String name,
                                             AxisConfiguration axisConfiguration) throws TransportManagerConfigurationException;

    /**
     * getting all the transport proxy instance deatils. this is used to dispaly all the
     * transport configuration instances.
     *
     * @param axisConfiguration
     * @return - list of available transport configuration
     */
    public List<TransportAdaptorConfiguration> getAllTransportConfigurations(AxisConfiguration axisConfiguration);


    /**
     * retuns the transport configuration for the given name
     *
     * @param name              - transport configuration name
     * @param axisConfiguration
     * @return - transport configuration
     */
    public TransportAdaptorConfiguration getTransportConfiguration(String name, AxisConfiguration axisConfiguration) throws TransportManagerConfigurationException;

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of TransportAdaptorFile
     */
    public List<TransportAdaptorFile> getUnDeployedFiles(AxisConfiguration axisConfiguration);

    /**
     * This method used to delete the un-deployed Transport Adaptor File
     *
     * @param filePath FilePath of the Adaptor File that going to be deleted
     * @throws org.wso2.carbon.transport.adaptor.manager.core.exception.TransportManagerConfigurationException
     *
     */
    public void removeTransportAdaptorFile(String filePath, AxisConfiguration axisConfiguration) throws TransportManagerConfigurationException;

    /**
     * Use to get the transport adaptor configuration file
     *
     * @param transportAdaptorName transport adaptor name
     * @param axisConfiguration
     * @return
     * @throws TransportManagerConfigurationException
     */
    public String getTransportConfigurationFile(String transportAdaptorName, AxisConfiguration axisConfiguration) throws TransportManagerConfigurationException;

    /**
     * To edit a transport adaptor configuration
     *
     * @param transportAdaptorConfiguration
     * @param transportAdaptorName
     * @param axisConfiguration
     * @throws TransportManagerConfigurationException
     */
    public void editTransportConfigurationFile(String transportAdaptorConfiguration, String transportAdaptorName, AxisConfiguration axisConfiguration) throws TransportManagerConfigurationException;

    /**
     * To get the input transport adaptor configuration
     *
     * @param transportAdaptorName transport adaptor name
     * @param tenantId
     * @return
     */
    public Map<String, String> getInputTransportAdaptorConfiguration(String transportAdaptorName, int tenantId);

    /**
     * To get the output transport adaptor configuration
     *
     * @param transportAdaptorName  transport adaptor configuration
     * @param tenantId
     * @return
     */
    public Map<String, String> getOutputTransportAdaptorConfiguration(String transportAdaptorName, int tenantId);

    /**
     * To get the list of information about the IN transport adaptors
     *
     * @param tenantId tenant Id
     * @return
     */
    public List<TransportAdaptorInfo> getInputTransportAdaptorInfo(int tenantId);

    /**
     * To get the list of information about the OUT transport adaptors
     *
     * @param tenantId
     * @return
     */
    public List<TransportAdaptorInfo> getOutputTransportAdaptorInfo(int tenantId);


}
