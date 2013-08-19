package org.wso2.carbon.output.transport.adaptor.manager.admin.internal;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorDto;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.config.InternalOutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.admin.internal.util.OutputTransportAdaptorHolder;
import org.wso2.carbon.output.transport.adaptor.manager.admin.internal.util.OutputTransportAdaptorManagerHolder;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorFile;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OutputTransportAdaptorManagerAdminService extends AbstractAdmin {

    /**
     * Get the output Transport Adaptor type names
     * @return Array of Transport Adaptor type names
     * @throws AxisFault if Transport names are empty
     */
    public String[] getOutputTransportAdaptorTypeNames() throws AxisFault {
        OutputTransportAdaptorHolder outputTransportAdaptorHolder = OutputTransportAdaptorHolder.getInstance();
        List<OutputTransportAdaptorDto> transportAdaptorDtoList = outputTransportAdaptorHolder.getTransportAdaptorService().getTransportAdaptors();
        if (transportAdaptorDtoList != null) {
            String[] transportAdaptorNames = new String[transportAdaptorDtoList.size()];
            for (int index = 0; index < transportAdaptorNames.length; index++) {
                transportAdaptorNames[index] = transportAdaptorDtoList.get(index).getTransportAdaptorTypeName();
            }

            Arrays.sort(transportAdaptorNames);
            return transportAdaptorNames;

        }
        throw new AxisFault("No Output Transport Adaptor type names are received.");
    }

    /**
     * To get the all transport adaptor property object
     *
     * @param transportAdaptorName transport adaptor name
     * @return transport adaptor properties
     * @throws AxisFault
     */
    public OutputTransportAdaptorPropertiesDto getOutputTransportAdaptorProperties(
            String transportAdaptorName) throws AxisFault {

        OutputTransportAdaptorHolder outputTransportAdaptorHolder = OutputTransportAdaptorHolder.getInstance();
        OutputTransportAdaptorDto transportAdaptorDto = outputTransportAdaptorHolder.getTransportAdaptorService().getTransportAdaptorDto(transportAdaptorName);

        if (transportAdaptorDto != null) {

            OutputTransportAdaptorPropertiesDto outputTransportAdaptorPropertiesDto = new OutputTransportAdaptorPropertiesDto();
            outputTransportAdaptorPropertiesDto.setOutputTransportAdaptorPropertyDtos(getOutputTransportAdaptorProperties(transportAdaptorDto));

            return outputTransportAdaptorPropertiesDto;
        } else {
            throw new AxisFault("Output Transport Adaptor Dto not found for " + transportAdaptorName);
        }
    }


    /**
     * Add Transport Adaptor Configuration
     *
     * @param transportAdaptorName -name of the transport adaptor to be added
     * @param transportAdaptorType -transport adaptor type; jms,ws-event
     * @param outputAdaptorPropertyDtoOutputs
     *                             - output adaptor properties with values
     */
    public void deployOutputTransportAdaptorConfiguration(String transportAdaptorName,
                                                          String transportAdaptorType,
                                                          OutputTransportAdaptorPropertyDto[] outputAdaptorPropertyDtoOutputs)
            throws AxisFault {

        if (checkOutputTransportAdaptorValidity(transportAdaptorName)) {
            try {
                OutputTransportAdaptorManagerHolder outputTransportAdaptorManagerHolder = OutputTransportAdaptorManagerHolder.getInstance();

                OutputTransportAdaptorConfiguration transportAdaptorConfiguration = new OutputTransportAdaptorConfiguration();
                transportAdaptorConfiguration.setName(transportAdaptorName);
                transportAdaptorConfiguration.setType(transportAdaptorType);
                AxisConfiguration axisConfiguration = getAxisConfig();


                InternalOutputTransportAdaptorConfiguration outputTransportAdaptorPropertyConfiguration = new InternalOutputTransportAdaptorConfiguration();
                if (outputAdaptorPropertyDtoOutputs.length != 0) {
                    for (OutputTransportAdaptorPropertyDto outputTransportAdaptorPropertyDto : outputAdaptorPropertyDtoOutputs) {
                        if (!outputTransportAdaptorPropertyDto.getValue().trim().equals("")) {
                            outputTransportAdaptorPropertyConfiguration.addTransportAdaptorProperty(outputTransportAdaptorPropertyDto.getKey(), outputTransportAdaptorPropertyDto.getValue());
                        }
                    }
                }
                transportAdaptorConfiguration.setOutputConfiguration(outputTransportAdaptorPropertyConfiguration);

                outputTransportAdaptorManagerHolder.getOutputTransportAdaptorManagerService().deployOutputTransportAdaptorConfiguration(transportAdaptorConfiguration, axisConfiguration);

            } catch (OutputTransportAdaptorManagerConfigurationException e) {
                throw new AxisFault("Error in adding Output Transport Adaptor configuration : " +e.getMessage());
            }
        } else {
            throw new AxisFault(transportAdaptorName + " is already registered for this tenant");
        }
    }


    /**
     * To remove a transport adaptor configuration by its name
     *
     * @param transportAdaptorName transport Adaptor's name
     * @throws AxisFault
     */
    public void undeployActiveOutputTransportAdaptorConfiguration(String transportAdaptorName)
            throws AxisFault {
        OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().undeployActiveOutputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in removing Output Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * to edit a transport adaptor configuration
     *
     * @param transportAdaptorConfiguration transport adaptor configuration of the edited adaptor
     * @param transportAdaptorName          transport adaptor name
     * @throws AxisFault
     */
    public void editActiveOutputTransportAdaptorConfiguration(String transportAdaptorConfiguration,
                                                              String transportAdaptorName)
            throws AxisFault {
        OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().editActiveOutputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportAdaptorName, axisConfiguration);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when editing Output Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * to edit not deployed transport adaptor configuration
     *
     * @param transportAdaptorConfiguration transport adaptor configuration of the edited adaptor
     * @param filePath                      file path of the configuration file
     * @throws AxisFault
     */
    public void editInactiveOutputTransportAdaptorConfiguration(
            String transportAdaptorConfiguration,
            String filePath)
            throws AxisFault {

        OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().editInactiveOutputTransportAdaptorConfiguration(transportAdaptorConfiguration, filePath, axisConfiguration);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when editing Output Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * To get the transport adaptor configuration file from the file system
     *
     * @param transportAdaptorName transport adaptor name
     * @return Transport adaptor configuration file
     * @throws AxisFault
     */
    public String getActiveOutputTransportAdaptorConfigurationContent(String transportAdaptorName)
            throws AxisFault {
        OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().getActiveOutputTransportAdaptorConfigurationContent(transportAdaptorName, axisConfiguration);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when retrieving Output Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * To get the not deployed transport adaptor configuration file from the file system
     *
     * @param filePath file path of the configuration file
     * @return Transport adaptor configuration file
     * @throws AxisFault
     */

    public String getInactiveOutputTransportAdaptorConfigurationContent(String filePath)
            throws AxisFault {
        OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
        try {
            String transportAdaptorConfigurationFile = outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().getInactiveOutputTransportAdaptorConfigurationContent(filePath);
            return transportAdaptorConfigurationFile.trim();
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when retrieving Output Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * to remove a transport adaptor configuration file from the file system
     *
     * @param filePath filePath of the Transport Adaptor file
     * @throws AxisFault
     */
    public void undeployInactiveOutputTransportAdaptorConfiguration(String filePath)
            throws AxisFault {
        OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().undeployInactiveOutputTransportAdaptorConfiguration(filePath, axisConfiguration);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when removing Output Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * This method is used to get the transport adaptor name and type
     * @throws AxisFault
     */
    public OutputTransportAdaptorConfigurationInfoDto[] getAllActiveOutputTransportAdaptorConfiguration()
            throws AxisFault {
        try {
            OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            // get transport adaptor configurations
            List<OutputTransportAdaptorConfiguration> transportAdaptorConfigurationList;
            transportAdaptorConfigurationList = outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().
                    getAllActiveOutputTransportAdaptorConfiguration(axisConfiguration);

            if (transportAdaptorConfigurationList != null) {
                // create transport adaptor configuration details array
                OutputTransportAdaptorConfigurationInfoDto[] outputTransportAdaptorConfigurationInfoDtoArray = new
                        OutputTransportAdaptorConfigurationInfoDto[transportAdaptorConfigurationList.size()];
                for (int index = 0; index < outputTransportAdaptorConfigurationInfoDtoArray.length; index++) {
                    OutputTransportAdaptorConfiguration transportAdaptorConfiguration = transportAdaptorConfigurationList.get(index);
                    String transportAdaptorName = transportAdaptorConfiguration.getName();
                    String transportAdaptorType = transportAdaptorConfiguration.getType();

                    // create transport adaptor configuration details with transport adaptor name and type
                    outputTransportAdaptorConfigurationInfoDtoArray[index] = new OutputTransportAdaptorConfigurationInfoDto();
                    outputTransportAdaptorConfigurationInfoDtoArray[index].setTransportAdaptorName(transportAdaptorName);
                    outputTransportAdaptorConfigurationInfoDtoArray[index].setTransportAdaptorType(transportAdaptorType);
                    outputTransportAdaptorConfigurationInfoDtoArray[index].setEnableTracing(transportAdaptorConfiguration.isEnableTracing());
                    outputTransportAdaptorConfigurationInfoDtoArray[index].setEnableStats(transportAdaptorConfiguration.isEnableStatistics());
                }
                return outputTransportAdaptorConfigurationInfoDtoArray;
            } else {
                return new OutputTransportAdaptorConfigurationInfoDto[0];
            }
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("No Output Transport Adaptor configurations received : " + e.getMessage());
        }

    }

    /**
     * Get the Transport Adaptor files which are undeployed
     *
     *
     */
    public OutputTransportAdaptorFileDto[] getAllInactiveOutputTransportAdaptorConfiguration()
            throws AxisFault {

        OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().
                getAllInactiveOutputTransportAdaptorConfiguration(axisConfiguration);
        if (outputTransportAdaptorFileList != null) {

            // create transport adaptor file details array
            OutputTransportAdaptorFileDto[] outputTransportAdaptorFileDtoArray = new
                    OutputTransportAdaptorFileDto[outputTransportAdaptorFileList.size()];

            for (int index = 0; index < outputTransportAdaptorFileDtoArray.length; index++) {
                OutputTransportAdaptorFile outputTransportAdaptorFile = outputTransportAdaptorFileList.get(index);
                String filePath = outputTransportAdaptorFile.getFilePath();
                String transportAdaptorName = outputTransportAdaptorFile.getTransportAdaptorName();

                // create transport adaptor file with file path and adaptor name
                outputTransportAdaptorFileDtoArray[index] = new OutputTransportAdaptorFileDto(filePath, transportAdaptorName);
            }
            return outputTransportAdaptorFileDtoArray;
        } else {
            return new OutputTransportAdaptorFileDto[0];
        }
    }

    /**
     * To get the transport adaptor configuration details with values and necessary properties
     *
     */
    public OutputTransportAdaptorPropertiesDto getActiveOutputTransportAdaptorConfiguration(
            String transportAdaptorName) throws AxisFault {

        try {
            OutputTransportAdaptorManagerHolder outputTransportAdaptorManager = OutputTransportAdaptorManagerHolder.getInstance();

            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration;

            transportAdaptorConfiguration = outputTransportAdaptorManager.getOutputTransportAdaptorManagerService().
                    getActiveOutputTransportAdaptorConfiguration(transportAdaptorName, tenantId);

            OutputTransportAdaptorHolder outputTransportAdaptorHolder = OutputTransportAdaptorHolder.getInstance();
            OutputTransportAdaptorDto transportAdaptorDto = outputTransportAdaptorHolder.getTransportAdaptorService().getTransportAdaptorDto(transportAdaptorConfiguration.getType());

            if (transportAdaptorDto != null) {
                OutputTransportAdaptorPropertiesDto outputTransportAdaptorPropertiesDto = new OutputTransportAdaptorPropertiesDto();
                outputTransportAdaptorPropertiesDto.setOutputTransportAdaptorPropertyDtos(getOutputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportAdaptorDto));

                return outputTransportAdaptorPropertiesDto;
            } else {
                return null;
            }
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Cannot retrieve Output Transport Adaptor details : " + e.getMessage());
        }
    }


    public void testConnection(String transportAdaptorName,
                               String transportAdaptorType,
                               OutputTransportAdaptorPropertyDto[] outputAdaptorPropertyDtoOutputs)
            throws AxisFault {

        try {
            OutputTransportAdaptorHolder outputTransportAdaptorHolder = OutputTransportAdaptorHolder.getInstance();

            OutputTransportAdaptorConfiguration transportAdaptorConfiguration = new OutputTransportAdaptorConfiguration();
            transportAdaptorConfiguration.setName(transportAdaptorName);
            transportAdaptorConfiguration.setType(transportAdaptorType);

            InternalOutputTransportAdaptorConfiguration outputTransportAdaptorPropertyConfiguration = new InternalOutputTransportAdaptorConfiguration();
            if (outputAdaptorPropertyDtoOutputs.length != 0) {
                for (OutputTransportAdaptorPropertyDto outputTransportAdaptorPropertyDto : outputAdaptorPropertyDtoOutputs) {
                    if (!outputTransportAdaptorPropertyDto.getValue().trim().equals("")) {
                        outputTransportAdaptorPropertyConfiguration.addTransportAdaptorProperty(outputTransportAdaptorPropertyDto.getKey(), outputTransportAdaptorPropertyDto.getValue());
                    }
                }
            }
            transportAdaptorConfiguration.setOutputConfiguration(outputTransportAdaptorPropertyConfiguration);

            outputTransportAdaptorHolder.getTransportAdaptorService().testConnection(transportAdaptorConfiguration);

        } catch (Exception e) {
            throw new AxisFault(e.getMessage());
        }

    }

    public void setStatisticsEnabled(String transportAdaptorName, boolean flag) throws AxisFault {

        OutputTransportAdaptorManagerHolder outputTransportAdaptorManagerHolder = OutputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputTransportAdaptorManagerHolder.getOutputTransportAdaptorManagerService().setStatisticsEnabled(transportAdaptorName, axisConfiguration, flag);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }
    }

    public void setTracingEnabled(String transportAdaptorName, boolean flag) throws AxisFault {
        OutputTransportAdaptorManagerHolder outputTransportAdaptorManagerHolder = OutputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            outputTransportAdaptorManagerHolder.getOutputTransportAdaptorManagerService().setTracingEnabled(transportAdaptorName, axisConfiguration, flag);
        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }

    }

    //Private methods are below
    private OutputTransportAdaptorPropertyDto[] getOutputTransportAdaptorProperties(
            OutputTransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        List<Property> outputPropertyList = transportAdaptorDto.getAdaptorPropertyList();
        if (outputPropertyList != null) {
            OutputTransportAdaptorPropertyDto[] outputTransportAdaptorPropertyDtoArray = new OutputTransportAdaptorPropertyDto[outputPropertyList.size()];
            for (int index = 0; index < outputTransportAdaptorPropertyDtoArray.length; index++) {
                Property property = outputPropertyList.get(index);
                // set transport property parameters
                outputTransportAdaptorPropertyDtoArray[index] = new OutputTransportAdaptorPropertyDto(property.getPropertyName(), "");
                outputTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                outputTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                outputTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                outputTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                outputTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                outputTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
            }
            return outputTransportAdaptorPropertyDtoArray;
        }
        return new OutputTransportAdaptorPropertyDto[0];
    }

    private OutputTransportAdaptorPropertyDto[] getOutputTransportAdaptorConfiguration(
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration,
            OutputTransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        if (transportAdaptorConfiguration != null) {

            // get output transport adaptor properties
            List<Property> outputPropertyList = transportAdaptorDto.getAdaptorPropertyList();
            if (transportAdaptorConfiguration.getOutputConfiguration() != null) {
                Map<String, String> outputTransportProperties = transportAdaptorConfiguration.getOutputConfiguration().getProperties();
                if (outputPropertyList != null) {
                    OutputTransportAdaptorPropertyDto[] outputTransportAdaptorPropertyDtoArray = new OutputTransportAdaptorPropertyDto[outputPropertyList.size()];
                    int index = 0;
                    for (Property property : outputPropertyList) {
                        // create output transport property
                        outputTransportAdaptorPropertyDtoArray[index] = new OutputTransportAdaptorPropertyDto(property.getPropertyName(),
                                                                                                              outputTransportProperties.get(property.
                                                                                                                      getPropertyName()));
                        // set output transport property parameters
                        outputTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                        outputTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                        outputTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                        outputTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                        outputTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                        outputTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());

                        index++;
                    }
                    return outputTransportAdaptorPropertyDtoArray;
                }
                return new OutputTransportAdaptorPropertyDto[0];
            }
        }
        return new OutputTransportAdaptorPropertyDto[0];

    }


    private boolean checkOutputTransportAdaptorValidity(String transportAdaptorName)
            throws AxisFault {
        try {
            OutputTransportAdaptorManagerHolder outputTransportAdaptorManagerHolder = OutputTransportAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<OutputTransportAdaptorConfiguration> transportAdaptorConfigurationList;
            transportAdaptorConfigurationList = outputTransportAdaptorManagerHolder.getOutputTransportAdaptorManagerService().getAllActiveOutputTransportAdaptorConfiguration(axisConfiguration);
            for (OutputTransportAdaptorConfiguration transportAdaptorConfiguration : transportAdaptorConfigurationList) {
                if (transportAdaptorConfiguration.getName().equalsIgnoreCase(transportAdaptorName)) {
                    return false;
                }
            }

        } catch (OutputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in validating the Output Transport Adaptor : "+e.getMessage());
        }
        return true;
    }

}