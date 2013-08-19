package org.wso2.carbon.input.transport.adaptor.manager.admin.internal;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may notgetAllActiveInputTransportAdaptorProperties use this file except in compliance with the License.
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
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorDto;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.config.InternalInputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.manager.admin.internal.util.InputTransportAdaptorHolder;
import org.wso2.carbon.input.transport.adaptor.manager.admin.internal.util.InputTransportAdaptorManagerHolder;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorFile;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InputTransportAdaptorManagerAdminService extends AbstractAdmin {

    /**
     * Get the Transport Adaptor names
     *
     * @return Array of Transport Adaptor type names
     * @throws AxisFault if Transport names are empty
     */
    public String[] getAllInputTransportAdaptorTypeNames() throws AxisFault {
        InputTransportAdaptorHolder inputTransportAdaptorHolder = InputTransportAdaptorHolder.getInstance();
        List<InputTransportAdaptorDto> transportAdaptorDtoList = inputTransportAdaptorHolder.getTransportAdaptorService().getTransportAdaptors();
        if (transportAdaptorDtoList != null) {
            String[] transportAdaptorNames = new String[transportAdaptorDtoList.size()];
            for (int index = 0; index < transportAdaptorNames.length; index++) {
                transportAdaptorNames[index] = transportAdaptorDtoList.get(index).getTransportAdaptorTypeName();
            }

            Arrays.sort(transportAdaptorNames);
            return transportAdaptorNames;

        }
        throw new AxisFault("No Input Transport Adaptor type names are received");
    }

    /**
     * To get the all transport adaptor property object
     *
     * @param transportAdaptorName transport adaptor name
     * @return transport adaptor properties
     * @throws AxisFault
     */
    public InputTransportAdaptorPropertiesDto getInputTransportAdaptorProperties(
            String transportAdaptorName) throws AxisFault {

        InputTransportAdaptorHolder inputTransportAdaptorHolder = InputTransportAdaptorHolder.getInstance();
        InputTransportAdaptorDto transportAdaptorDto = inputTransportAdaptorHolder.getTransportAdaptorService().getTransportAdaptorDto(transportAdaptorName);

        if (transportAdaptorDto != null) {

            InputTransportAdaptorPropertiesDto inputTransportAdaptorPropertiesDto = new InputTransportAdaptorPropertiesDto();
            inputTransportAdaptorPropertiesDto.setInputTransportAdaptorPropertyDtos(getInputTransportAdaptorProperties(transportAdaptorDto));

            return inputTransportAdaptorPropertiesDto;
        } else {
            throw new AxisFault("Input Transport Adaptor Dto not found for " + transportAdaptorName);
        }
    }


    /**
     * Add Transport Adaptor Configuration
     *
     * @param transportAdaptorName          -name of the transport adaptor to be added
     * @param transportAdaptorType          -transport adaptor type; jms,ws-event
     * @param inputAdaptorPropertyDtoInputs - input adaptor properties with values
     */
    public void deployInputTransportAdaptorConfiguration(String transportAdaptorName,
                                                         String transportAdaptorType,
                                                         InputTransportAdaptorPropertyDto[] inputAdaptorPropertyDtoInputs)
            throws AxisFault {

        if (checkInputTransportAdaptorValidity(transportAdaptorName)) {
            try {
                InputTransportAdaptorManagerHolder inputTransportAdaptorManagerHolder = InputTransportAdaptorManagerHolder.getInstance();

                InputTransportAdaptorConfiguration transportAdaptorConfiguration = new InputTransportAdaptorConfiguration();
                transportAdaptorConfiguration.setName(transportAdaptorName);
                transportAdaptorConfiguration.setType(transportAdaptorType);
                AxisConfiguration axisConfiguration = getAxisConfig();


                InternalInputTransportAdaptorConfiguration inputTransportAdaptorPropertyConfiguration = new InternalInputTransportAdaptorConfiguration();
                if (inputAdaptorPropertyDtoInputs.length != 0) {
                    for (InputTransportAdaptorPropertyDto inputTransportAdaptorPropertyDto : inputAdaptorPropertyDtoInputs) {
                        if (!inputTransportAdaptorPropertyDto.getValue().trim().equals("")) {
                            inputTransportAdaptorPropertyConfiguration.addTransportAdaptorProperty(inputTransportAdaptorPropertyDto.getKey(), inputTransportAdaptorPropertyDto.getValue());
                        }
                    }
                }
                transportAdaptorConfiguration.setInputConfiguration(inputTransportAdaptorPropertyConfiguration);

                inputTransportAdaptorManagerHolder.getInputTransportAdaptorManagerService().deployInputTransportAdaptorConfiguration(transportAdaptorConfiguration, axisConfiguration);

            } catch (InputTransportAdaptorManagerConfigurationException e) {
                throw new AxisFault("Error in adding Input Transport Adaptor configuration : " + e.getMessage());
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
    public void undeployActiveInputTransportAdaptorConfiguration(String transportAdaptorName)
            throws AxisFault {
        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputTransportAdaptorManager.getInputTransportAdaptorManagerService().undeployActiveInputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in removing Input Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * to edit a transport adaptor configuration
     *
     * @param transportAdaptorConfiguration transport adaptor configuration of the edited adaptor
     * @param transportAdaptorName          transport adaptor name
     * @throws AxisFault
     */
    public void editActiveInputTransportAdaptorConfiguration(String transportAdaptorConfiguration,
                                                             String transportAdaptorName)
            throws AxisFault {
        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputTransportAdaptorManager.getInputTransportAdaptorManagerService().editActiveInputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportAdaptorName, axisConfiguration);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when editing Input Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * to edit not deployed transport adaptor configuration
     *
     * @param transportAdaptorConfiguration transport adaptor configuration of the edited adaptor
     * @param filePath                      file path of the configuration file
     * @throws AxisFault
     */
    public void editInactiveInputTransportAdaptorConfiguration(
            String transportAdaptorConfiguration,
            String filePath)
            throws AxisFault {

        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputTransportAdaptorManager.getInputTransportAdaptorManagerService().editInactiveInputTransportAdaptorConfiguration(transportAdaptorConfiguration, filePath, axisConfiguration);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when editing INput Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * To get the transport adaptor configuration file from the file system
     *
     * @param transportAdaptorName transport adaptor name
     * @return Transport adaptor configuration file
     * @throws AxisFault
     */
    public String getActiveInputTransportAdaptorConfigurationContent(String transportAdaptorName)
            throws AxisFault {
        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return inputTransportAdaptorManager.getInputTransportAdaptorManagerService().getActiveInputTransportAdaptorConfigurationContent(transportAdaptorName, axisConfiguration);

        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when retrieving Input Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * To get the not deployed transport adaptor configuration file from the file system
     *
     * @param filePath file path of the configuration file
     * @return Transport adaptor configuration file
     * @throws AxisFault
     */

    public String getInactiveInputTransportAdaptorConfigurationContent(String filePath)
            throws AxisFault {
        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        try {
            String transportAdaptorConfigurationFile = inputTransportAdaptorManager.getInputTransportAdaptorManagerService().getInactiveInputTransportAdaptorConfigurationContent(filePath);
            return transportAdaptorConfigurationFile.trim();
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when retrieving transport adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * to remove a transport adaptor configuration file from the file system
     *
     * @param filePath filePath of the Transport Adaptor file
     * @throws AxisFault
     */
    public void undeployInactiveInputTransportAdaptorConfiguration(String filePath)
            throws AxisFault {
        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            inputTransportAdaptorManager.getInputTransportAdaptorManagerService().undeployInactiveInputTransportAdaptorConfiguration(filePath, axisConfiguration);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when removing Input Transport Adaptor configurations : " + e.getMessage());
        }
    }

    /**
     * This method is used to get the transport adaptor name and type
     *
     * @throws AxisFault
     */
    public InputTransportAdaptorConfigurationInfoDto[] getAllActiveInputTransportAdaptorConfiguration()
            throws AxisFault {
        try {
            InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            // get transport adaptor configurations
            List<InputTransportAdaptorConfiguration> transportAdaptorConfigurationList;
            transportAdaptorConfigurationList = inputTransportAdaptorManager.getInputTransportAdaptorManagerService().
                    getAllActiveInputTransportAdaptorConfiguration(axisConfiguration);

            if (transportAdaptorConfigurationList != null) {
                // create transport adaptor configuration details array
                InputTransportAdaptorConfigurationInfoDto[] inputTransportAdaptorConfigurationInfoDtoArray = new
                        InputTransportAdaptorConfigurationInfoDto[transportAdaptorConfigurationList.size()];
                for (int index = 0; index < inputTransportAdaptorConfigurationInfoDtoArray.length; index++) {
                    InputTransportAdaptorConfiguration transportAdaptorConfiguration = transportAdaptorConfigurationList.get(index);
                    String transportAdaptorName = transportAdaptorConfiguration.getName();
                    String transportAdaptorType = transportAdaptorConfiguration.getType();

                    // create transport adaptor configuration details with transport adaptor name and type
                    inputTransportAdaptorConfigurationInfoDtoArray[index] = new InputTransportAdaptorConfigurationInfoDto();
                    inputTransportAdaptorConfigurationInfoDtoArray[index].setTransportAdaptorName(transportAdaptorName);
                    inputTransportAdaptorConfigurationInfoDtoArray[index].setTransportAdaptorType(transportAdaptorType);
                    inputTransportAdaptorConfigurationInfoDtoArray[index].setEnableTracing(transportAdaptorConfiguration.isEnableTracing());
                    inputTransportAdaptorConfigurationInfoDtoArray[index].setEnableStats(transportAdaptorConfiguration.isEnableStatistics());
                }
                return inputTransportAdaptorConfigurationInfoDtoArray;
            } else {
                return new InputTransportAdaptorConfigurationInfoDto[0];
            }
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("No Input Transport Adaptor configurations received : " + e.getMessage());
        }

    }

    /**
     * Get the Transport Adaptor files which are undeployed
     */
    public InputTransportAdaptorFileDto[] getAllInactiveInputTransportAdaptorConfigurationFile()
            throws AxisFault {

        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<InputTransportAdaptorFile> inputTransportAdaptorFileList = inputTransportAdaptorManager.getInputTransportAdaptorManagerService().
                getAllInactiveInputTransportAdaptorConfiguration(axisConfiguration);
        if (inputTransportAdaptorFileList != null) {

            // create transport adaptor file details array
            InputTransportAdaptorFileDto[] inputTransportAdaptorFileDtoArray = new
                    InputTransportAdaptorFileDto[inputTransportAdaptorFileList.size()];

            for (int index = 0; index < inputTransportAdaptorFileDtoArray.length; index++) {
                InputTransportAdaptorFile inputTransportAdaptorFile = inputTransportAdaptorFileList.get(index);
                String filePath = inputTransportAdaptorFile.getFilePath();
                String transportAdaptorName = inputTransportAdaptorFile.getTransportAdaptorName();

                // create transport adaptor file with file path and adaptor name
                inputTransportAdaptorFileDtoArray[index] = new InputTransportAdaptorFileDto(filePath, transportAdaptorName);
            }
            return inputTransportAdaptorFileDtoArray;
        } else {
            return new InputTransportAdaptorFileDto[0];
        }
    }

    /**
     * To get the transport adaptor configuration details with values and necessary properties
     *
     * @param transportAdaptorName transport adaptor name
     * @throws AxisFault
     */
    public InputTransportAdaptorPropertiesDto getActiveInputTransportAdaptorConfiguration(
            String transportAdaptorName) throws AxisFault {

        try {
            InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();

            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            InputTransportAdaptorConfiguration transportAdaptorConfiguration;

            transportAdaptorConfiguration = inputTransportAdaptorManager.getInputTransportAdaptorManagerService().
                    getActiveInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);

            if (transportAdaptorConfiguration != null) {
                InputTransportAdaptorHolder inputTransportAdaptorHolder = InputTransportAdaptorHolder.getInstance();
                InputTransportAdaptorDto transportAdaptorDto = inputTransportAdaptorHolder.getTransportAdaptorService().getTransportAdaptorDto(transportAdaptorConfiguration.getType());

                if (transportAdaptorDto != null) {
                    InputTransportAdaptorPropertiesDto inputTransportAdaptorPropertiesDto = new InputTransportAdaptorPropertiesDto();
                    inputTransportAdaptorPropertiesDto.setInputTransportAdaptorPropertyDtos(getInputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportAdaptorDto));

                    return inputTransportAdaptorPropertiesDto;
                } else {
                    return null;
                }
            } else {
                throw new AxisFault("Cannot retrieve Input Transport Adaptor details for " + transportAdaptorName);
            }
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Cannot retrieve Input Transport Adaptor details : " + e.getMessage());
        }
    }

    /**
     * Method used to enable or disable statistics for input transport adaptor
     */
    public void setStatisticsEnabled(String transportAdaptorName, boolean flag) throws AxisFault {

        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputTransportAdaptorManager.getInputTransportAdaptorManagerService().setStatisticsEnabled(transportAdaptorName, axisConfiguration, flag);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * Method used to enable or disable tracing for input transport adaptor
     */
    public void setTracingEnabled(String transportAdaptorName, boolean flag) throws AxisFault {
        InputTransportAdaptorManagerHolder inputTransportAdaptorManager = InputTransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            inputTransportAdaptorManager.getInputTransportAdaptorManagerService().setTracingEnabled(transportAdaptorName, axisConfiguration, flag);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }

    }

    //Private methods are below

    private InputTransportAdaptorPropertyDto[] getInputTransportAdaptorProperties(
            InputTransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        List<Property> inputPropertyList = transportAdaptorDto.getAdaptorPropertyList();
        if (inputPropertyList != null) {
            InputTransportAdaptorPropertyDto[] inputTransportAdaptorPropertyDtoArray = new InputTransportAdaptorPropertyDto[inputPropertyList.size()];
            for (int index = 0; index < inputTransportAdaptorPropertyDtoArray.length; index++) {
                Property property = inputPropertyList.get(index);
                // set transport property parameters
                inputTransportAdaptorPropertyDtoArray[index] = new InputTransportAdaptorPropertyDto(property.getPropertyName(), "");
                inputTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                inputTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                inputTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                inputTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                inputTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                inputTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
            }
            return inputTransportAdaptorPropertyDtoArray;
        }
        return new InputTransportAdaptorPropertyDto[0];
    }


    private InputTransportAdaptorPropertyDto[] getInputTransportAdaptorConfiguration(
            InputTransportAdaptorConfiguration transportAdaptorConfiguration,
            InputTransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        if (transportAdaptorConfiguration != null) {

            // get input transport adaptor properties
            List<Property> inputPropertyList = transportAdaptorDto.getAdaptorPropertyList();
            if (transportAdaptorConfiguration.getInputConfiguration() != null) {
                Map<String, String> inputTransportProperties = transportAdaptorConfiguration.getInputConfiguration().getProperties();
                if (inputPropertyList != null) {
                    InputTransportAdaptorPropertyDto[] inputTransportAdaptorPropertyDtoArray = new InputTransportAdaptorPropertyDto[inputPropertyList.size()];
                    int index = 0;
                    for (Property property : inputPropertyList) {
                        // create input transport property
                        inputTransportAdaptorPropertyDtoArray[index] = new InputTransportAdaptorPropertyDto(property.getPropertyName(),
                                                                                                            inputTransportProperties.get(property.
                                                                                                                    getPropertyName()));
                        // set input transport property parameters
                        inputTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                        inputTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                        inputTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                        inputTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                        inputTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                        inputTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());

                        index++;
                    }
                    return inputTransportAdaptorPropertyDtoArray;
                }
                return new InputTransportAdaptorPropertyDto[0];
            }
        }
        return new InputTransportAdaptorPropertyDto[0];

    }


    private boolean checkInputTransportAdaptorValidity(String transportAdaptorName)
            throws AxisFault {
        try {
            InputTransportAdaptorManagerHolder inputTransportAdaptorManagerHolder = InputTransportAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<InputTransportAdaptorConfiguration> transportAdaptorConfigurationList;
            transportAdaptorConfigurationList = inputTransportAdaptorManagerHolder.getInputTransportAdaptorManagerService().getAllActiveInputTransportAdaptorConfiguration(axisConfiguration);
            for (InputTransportAdaptorConfiguration transportAdaptorConfiguration : transportAdaptorConfigurationList) {
                if (transportAdaptorConfiguration.getName().equalsIgnoreCase(transportAdaptorName)) {
                    return false;
                }
            }

        } catch (InputTransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in validating the Input Transport Adaptor : " + e.getMessage());
        }
        return true;
    }

}