package org.wso2.carbon.transport.adaptor.manager.admin.internal;


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
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.manager.admin.internal.util.TransportAdaptorHolder;
import org.wso2.carbon.transport.adaptor.manager.admin.internal.util.TransportAdaptorManagerHolder;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorFile;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TransportAdaptorManagerConfigurationException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TransportAdaptorManagerAdminService extends AbstractAdmin {

    /**
     * Get the Transport Adaptor names
     *
     * @return Array of Transport Adaptor type names
     * @throws AxisFault if Transport names are empty
     */
    public String[] getTransportAdaptorNames() throws AxisFault {
        TransportAdaptorHolder transportAdaptorHolder = TransportAdaptorHolder.getInstance();
        List<TransportAdaptorDto> transportAdaptorDtoList = transportAdaptorHolder.getTransportService().getTransportAdaptors();
        if (transportAdaptorDtoList != null) {
            String[] transportAdaptorNames = new String[transportAdaptorDtoList.size()];
            for (int index = 0; index < transportAdaptorNames.length; index++) {
                transportAdaptorNames[index] = transportAdaptorDtoList.get(index).getTransportAdaptorTypeName();
            }
            return transportAdaptorNames;
        }
        throw new AxisFault("No Transport adaptor type names are received.");
    }

    /**
     * To get the all transport adaptor property object
     *
     * @param transportAdaptorName transport adaptor name
     * @return transport adaptor properties
     * @throws AxisFault
     */
    public TransportAdaptorPropertiesDto getAllTransportAdaptorPropertiesDto(
            String transportAdaptorName) throws AxisFault {

        TransportAdaptorHolder transportAdaptorHolder = TransportAdaptorHolder.getInstance();
        TransportAdaptorDto transportAdaptorDto = transportAdaptorHolder.getTransportService().getTransportAdaptorDto(transportAdaptorName);

        if (transportAdaptorDto != null) {
            TransportAdaptorPropertiesDto transportAdaptorPropertiesDto = new TransportAdaptorPropertiesDto();
            transportAdaptorPropertiesDto.setCommonTransportAdaptorPropertyDtos(getCommonTransportAdaptorProperties(transportAdaptorDto));
            transportAdaptorPropertiesDto.setInputTransportAdaptorPropertyDtos(getInputTransportAdaptorProperties(transportAdaptorDto));
            transportAdaptorPropertiesDto.setOutputTransportAdaptorPropertyDtos(getOutputTransportAdaptorProperties(transportAdaptorDto));

            return transportAdaptorPropertiesDto;
        } else {
            throw new AxisFault("Transport Adaptor Dto not found for " + transportAdaptorName);
        }

    }


    /**
     * Add Transport Adaptor Configuration
     *
     * @param transportAdaptorName      -name of the transport adaptor to be added
     * @param transportAdaptorType      -transport adaptor type; jms,ws-event
     * @param inputAdaptorPropertyDtos  - input adaptor properties with values
     * @param outputAdaptorPropertyDtos - output adaptor properties with values
     * @param commonAdaptorPropertyDtos - common adaptor properties with values
     */
    public void addTransportAdaptorConfiguration(String transportAdaptorName,
                                                 String transportAdaptorType,
                                                 TransportAdaptorPropertyDto[] inputAdaptorPropertyDtos,
                                                 TransportAdaptorPropertyDto[] outputAdaptorPropertyDtos,
                                                 TransportAdaptorPropertyDto[] commonAdaptorPropertyDtos)
            throws AxisFault {

        if (checkTransportAdaptorValidity(transportAdaptorName)) {
            try {
                TransportAdaptorManagerHolder transportAdaptorManagerHolder = TransportAdaptorManagerHolder.getInstance();
                TransportAdaptorConfiguration transportAdaptorConfiguration = new TransportAdaptorConfiguration();
                transportAdaptorConfiguration.setName(transportAdaptorName);
                transportAdaptorConfiguration.setType(transportAdaptorType);

                AxisConfiguration axisConfiguration = getAxisConfig();

                // add input transport adaptor properties
                for (TransportAdaptorPropertyDto transportAdaptorPropertyDto : inputAdaptorPropertyDtos) {
                    transportAdaptorConfiguration.addInputAdaptorProperty(transportAdaptorPropertyDto.getKey(), transportAdaptorPropertyDto.getValue());
                }

                // add output transport adaptor properties
                for (TransportAdaptorPropertyDto transportAdaptorPropertyDto : outputAdaptorPropertyDtos) {
                    transportAdaptorConfiguration.addOutputAdaptorProperty(transportAdaptorPropertyDto.getKey(), transportAdaptorPropertyDto.getValue());
                }

                // add common transport adaptor properties
                for (TransportAdaptorPropertyDto transportAdaptorPropertyDto : commonAdaptorPropertyDtos) {
                    transportAdaptorConfiguration.addCommonAdaptorProperty(transportAdaptorPropertyDto.getKey(), transportAdaptorPropertyDto.getValue());
                }


                transportAdaptorManagerHolder.getTransportAdaptorManagerService().saveTransportAdaptorConfiguration(transportAdaptorConfiguration, axisConfiguration);

            } catch (TransportAdaptorManagerConfigurationException e) {
                throw new AxisFault("Error in adding transport Configuration , ", e);
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
    public void removeTransportAdaptorConfiguration(String transportAdaptorName)
            throws AxisFault {
        TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            transportAdaptorManager.getTransportAdaptorManagerService().removeTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in removing transport adaptor configurations , " + e);
        }
    }

    /**
     * to edit a transport adaptor configuration
     *
     * @param transportAdaptorConfiguration transport adaptor configuration of the edited adaptor
     * @param transportAdaptorName          transport adaptor name
     * @throws AxisFault
     */
    public void editTransportAdaptorConfigurationFile(String transportAdaptorConfiguration,
                                                      String transportAdaptorName)
            throws AxisFault {

        TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            transportAdaptorManager.getTransportAdaptorManagerService().editTransportAdaptorConfigurationFile(transportAdaptorConfiguration, transportAdaptorName, axisConfiguration);

        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when editing transport adaptor configurations , " + e);
        }

    }

    /**
     * to edit not deployed transport adaptor configuration
     *
     * @param transportAdaptorConfiguration transport adaptor configuration of the edited adaptor
     * @param filePath          file path of the configuration file
     * @throws AxisFault
     */
    public void editNotDeployedTransportAdaptorConfigurationFile(String transportAdaptorConfiguration,
                                                      String filePath)
            throws AxisFault {

        TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            transportAdaptorManager.getTransportAdaptorManagerService().editNotDeployedTransportAdaptorConfigurationFile(transportAdaptorConfiguration, filePath, axisConfiguration);

        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error when editing transport adaptor configurations , " + e);
        }

    }

    /**
     * To get the transport adaptor configuration file from the file system
     *
     * @param transportAdaptorName transport adaptor name
     * @return Transport adaptor configuration file
     * @throws AxisFault
     */
    public String getTransportAdaptorConfigurationFile(String transportAdaptorName)
            throws AxisFault {
        TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            String transportConfigurationFile = transportAdaptorManager.getTransportAdaptorManagerService().getTransportAdaptorConfigurationFile(transportAdaptorName, axisConfiguration);
            return transportConfigurationFile;
        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in getting transport adaptor configurations , " + e);
        }
    }

    /**
     * To get the not deployed transport adaptor configuration file from the file system
     *
     * @param filePath file path of the configuration file
     * @return Transport adaptor configuration file
     * @throws AxisFault
     */

    public String getNotDeployedTransportAdaptorConfigurationFile(String filePath)
            throws AxisFault {
        TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();

        try {
            String transportConfigurationFile = transportAdaptorManager.getTransportAdaptorManagerService().getNotDeployedTransportAdaptorConfigurationFile(filePath);
            return transportConfigurationFile.trim();
        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in getting transport adaptor configurations , " + e);
        }
    }

    /**
     * to remove a transport adaptor configuration file from the file system
     *
     * @param filePath filePath of the Transport Adaptor file
     * @throws AxisFault
     */
    public void removeTransportAdaptorConfigurationFile(String filePath)
            throws AxisFault {
        TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();

        try {
            transportAdaptorManager.getTransportAdaptorManagerService().removeTransportAdaptorConfigurationFile(filePath, axisConfig);
        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Error in removing transport adaptor configurations , " + e);
        }
    }


    /**
     * This method is used to get the transport adaptor name and type
     *
     * @return
     * @throws AxisFault
     */
    public TransportAdaptorConfigurationInfoDto[] getAllTransportAdaptorConfigurationInfo()
            throws AxisFault {
        try {
            TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();

            AxisConfiguration axisConfiguration = getAxisConfig();
            // get transport adaptor configurations
            List<TransportAdaptorConfiguration> transportAdaptorConfigurationList = null;

            transportAdaptorConfigurationList = transportAdaptorManager.getTransportAdaptorManagerService().
                    getAllTransportAdaptorConfiguration(axisConfiguration);

            if (transportAdaptorConfigurationList != null) {
                // create transport adaptor configuration details array
                TransportAdaptorConfigurationInfoDto[] transportAdaptorConfigurationInfoDtoArray = new
                        TransportAdaptorConfigurationInfoDto[transportAdaptorConfigurationList.size()];
                for (int index = 0; index < transportAdaptorConfigurationInfoDtoArray.length; index++) {
                    TransportAdaptorConfiguration transportAdaptorConfiguration = transportAdaptorConfigurationList.get(index);
                    String transportAdaptorName = transportAdaptorConfiguration.getName();
                    String transportAdaptorType = transportAdaptorConfiguration.getType();

                    // create transport adaptor configuration details with transport adaptor name and type
                    transportAdaptorConfigurationInfoDtoArray[index] = new TransportAdaptorConfigurationInfoDto();
                    transportAdaptorConfigurationInfoDtoArray[index].setTransportAdaptorName(transportAdaptorName);
                    transportAdaptorConfigurationInfoDtoArray[index].setTransportAdaptorType(transportAdaptorType);

                }
                return transportAdaptorConfigurationInfoDtoArray;
            } else {
                return new TransportAdaptorConfigurationInfoDto[0];
            }
        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("No Transport Adaptor Configurations received. "+e);
        }

    }

    /**
     * Get the Transport Adaptor files which are undeployed
     *
     * @return Transport Adaptor File Array
     * @throws org.wso2.carbon.transport.adaptor.manager.admin.internal.exception.TransportAdaptorManagerAdminServiceException
     *
     */
    public TransportAdaptorFileDto[] getNotDeployedTransportAdaptorConfigurationFiles()
            throws AxisFault {

        TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();

        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorManager.getTransportAdaptorManagerService().
                getNotDeployedTransportAdaptorConfigurationFiles(axisConfiguration);
        if (transportAdaptorFileList != null) {

            // create transport adaptor file details array
            TransportAdaptorFileDto[] transportAdaptorFileDtoArray = new
                    TransportAdaptorFileDto[transportAdaptorFileList.size()];

            for (int index = 0; index < transportAdaptorFileDtoArray.length; index++) {
                TransportAdaptorFile transportAdaptorFile = transportAdaptorFileList.get(index);
                String filePath = transportAdaptorFile.getFilePath();
                String transportAdaptorName = transportAdaptorFile.getTransportAdaptorName();

                // create transport adaptor file with file path and adaptor name
                transportAdaptorFileDtoArray[index] = new TransportAdaptorFileDto(filePath, transportAdaptorName);


            }
            return transportAdaptorFileDtoArray;
        } else {
            return new TransportAdaptorFileDto[0];
        }


    }

    /**
     * To get the transport adaptor configuration details with values and necessary properties
     *
     * @param transportAdaptorName  transport adaptor name
     * @return
     * @throws AxisFault
     */
    public TransportAdaptorPropertiesDto getTransportAdaptorConfigurationDetails(
            String transportAdaptorName) throws AxisFault {

        try {
            TransportAdaptorManagerHolder transportAdaptorManager = TransportAdaptorManagerHolder.getInstance();

            // get transport adaptor to get input transport properties with parameters isSecured, isRequired
            AxisConfiguration axisConfiguration = getAxisConfig();
            TransportAdaptorConfiguration transportAdaptorConfiguration = null;

            transportAdaptorConfiguration = transportAdaptorManager.getTransportAdaptorManagerService().
                    getTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);

            TransportAdaptorHolder transportAdaptorHolder = TransportAdaptorHolder.getInstance();
            TransportAdaptorDto transportAdaptorDto = transportAdaptorHolder.getTransportService().getTransportAdaptorDto(transportAdaptorConfiguration.getType());

            if (transportAdaptorDto != null) {
                TransportAdaptorPropertiesDto transportAdaptorPropertiesDto = new TransportAdaptorPropertiesDto();
                transportAdaptorPropertiesDto.setCommonTransportAdaptorPropertyDtos(getCommonTransportAdaptorConfiguration(transportAdaptorConfiguration, transportAdaptorDto));
                transportAdaptorPropertiesDto.setInputTransportAdaptorPropertyDtos(getInputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportAdaptorDto));
                transportAdaptorPropertiesDto.setOutputTransportAdaptorPropertyDtos(getOutputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportAdaptorDto));

                return transportAdaptorPropertiesDto;
            } else {
                return null;
            }
        } catch (TransportAdaptorManagerConfigurationException e) {
            throw new AxisFault("Cannot retrieve transport adaptor details " + e);
        }

    }


    //*****************************************************************************************************************
    //Private Methods


    private TransportAdaptorPropertyDto[] getInputTransportAdaptorProperties(
            TransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        List<Property> inputPropertyList = transportAdaptorDto.getAdaptorInPropertyList();
        if (inputPropertyList != null) {
            TransportAdaptorPropertyDto[] inputTransportAdaptorPropertyDtoArray = new TransportAdaptorPropertyDto[inputPropertyList.size()];
            for (int index = 0; index < inputTransportAdaptorPropertyDtoArray.length; index++) {
                Property property = inputPropertyList.get(index);
                // set transport property parameters
                inputTransportAdaptorPropertyDtoArray[index] = new TransportAdaptorPropertyDto(property.getPropertyName(), "");
                inputTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                inputTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                inputTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                inputTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                inputTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                inputTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
            }
            return inputTransportAdaptorPropertyDtoArray;
        }

        return new TransportAdaptorPropertyDto[0];

    }

    /**
     * @param transportAdaptorDto
     * @return
     * @throws AxisFault
     */
    private TransportAdaptorPropertyDto[] getOutputTransportAdaptorProperties(
            TransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        List<Property> outputPropertyList = transportAdaptorDto.getAdaptorOutPropertyList();
        if (outputPropertyList != null) {
            TransportAdaptorPropertyDto[] outputTransportAdaptorPropertyDtoArray = new TransportAdaptorPropertyDto[outputPropertyList.size()];
            for (int index = 0; index < outputTransportAdaptorPropertyDtoArray.length; index++) {
                Property property = outputPropertyList.get(index);
                // set transport property parameters
                outputTransportAdaptorPropertyDtoArray[index] = new TransportAdaptorPropertyDto(property.getPropertyName(), "");
                outputTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                outputTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                outputTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                outputTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                outputTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                outputTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
            }
            return outputTransportAdaptorPropertyDtoArray;
        }
        return new TransportAdaptorPropertyDto[0];

    }


    /**
     * @param transportAdaptorDto
     * @return
     * @throws AxisFault
     */
    private TransportAdaptorPropertyDto[] getCommonTransportAdaptorProperties(
            TransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        List<Property> commonPropertyList = transportAdaptorDto.getAdaptorCommonPropertyList();
        if (commonPropertyList != null) {
            TransportAdaptorPropertyDto[] commonTransportAdaptorPropertyDtoArray = new TransportAdaptorPropertyDto[commonPropertyList.size()];
            for (int index = 0; index < commonTransportAdaptorPropertyDtoArray.length; index++) {
                Property property = commonPropertyList.get(index);
                // set transport property parameters
                commonTransportAdaptorPropertyDtoArray[index] = new TransportAdaptorPropertyDto(property.getPropertyName(), "");
                commonTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                commonTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                commonTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                commonTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                commonTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                commonTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
            }
            return commonTransportAdaptorPropertyDtoArray;
        }

        return new TransportAdaptorPropertyDto[0];

    }


    /**
     * @param transportAdaptorConfiguration
     * @param transportAdaptorDto
     * @return
     * @throws AxisFault
     */
    private TransportAdaptorPropertyDto[] getInputTransportAdaptorConfiguration(
            TransportAdaptorConfiguration transportAdaptorConfiguration,
            TransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        if (transportAdaptorConfiguration != null) {

            // get input transport adaptor properties
            List<Property> inputPropertyList = transportAdaptorDto.getAdaptorInPropertyList();

            Map<String, String> inputTransportProperties = transportAdaptorConfiguration.getInputAdaptorProperties();
            TransportAdaptorPropertyDto[] inputTransportAdaptorPropertyDtoArray = new TransportAdaptorPropertyDto[inputTransportProperties.size()];
            int index = 0;
            if (inputPropertyList != null) {
                for (Property property : inputPropertyList) {
                    // create input transport property
                    inputTransportAdaptorPropertyDtoArray[index] = new TransportAdaptorPropertyDto(property.getPropertyName(),
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

            }
            return inputTransportAdaptorPropertyDtoArray;
        } else {
            return new TransportAdaptorPropertyDto[0];
        }

    }

    /**
     * @param transportAdaptorConfiguration
     * @param transportAdaptorDto
     * @return
     * @throws AxisFault
     */

    private TransportAdaptorPropertyDto[] getOutputTransportAdaptorConfiguration(
            TransportAdaptorConfiguration transportAdaptorConfiguration,
            TransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        if (transportAdaptorConfiguration != null) {

            // get output adaptor properties
            List<Property> outPropertyList = transportAdaptorDto.getAdaptorOutPropertyList();
            Map<String, String> outputTransportProperties = transportAdaptorConfiguration.getOutputAdaptorProperties();
            TransportAdaptorPropertyDto[] outputTransportAdaptorPropertyDtoArray = new TransportAdaptorPropertyDto[outputTransportProperties.size()];
            int index = 0;
            if (outPropertyList != null) {
                for (Property property : outPropertyList) {
                    // create transport adaptor property
                    outputTransportAdaptorPropertyDtoArray[index] = new TransportAdaptorPropertyDto(property.getPropertyName(),
                                                                                                    outputTransportProperties.get(property.getPropertyName()));

                    // set output transport adaptor property parameters
                    outputTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                    outputTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                    outputTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    outputTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    outputTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                    outputTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
                    index++;
                }
            }
            return outputTransportAdaptorPropertyDtoArray;
        } else {
            return new TransportAdaptorPropertyDto[0];
        }

    }


    /**
     * @param transportAdaptorConfiguration
     * @param transportAdaptorDto
     * @return
     * @throws AxisFault
     */
    private TransportAdaptorPropertyDto[] getCommonTransportAdaptorConfiguration(
            TransportAdaptorConfiguration transportAdaptorConfiguration,
            TransportAdaptorDto transportAdaptorDto)
            throws AxisFault {

        if (transportAdaptorConfiguration != null) {

            // get transport adaptor properties
            List<Property> commonPropertyList = transportAdaptorDto.getAdaptorCommonPropertyList();
            Map<String, String> commonTransportProperties = transportAdaptorConfiguration.getCommonAdaptorProperties();
            TransportAdaptorPropertyDto[] commonTransportAdaptorPropertyDtoArray = new TransportAdaptorPropertyDto[commonTransportProperties.size()];
            int index = 0;
            if (commonPropertyList != null) {
                for (Property property : commonPropertyList) {
                    // create common transport adaptor  property
                    commonTransportAdaptorPropertyDtoArray[index] = new TransportAdaptorPropertyDto(property.getPropertyName(),
                                                                                                    commonTransportProperties.get(property.
                                                                                                            getPropertyName()));
                    // set common transport adaptor property parameters
                    commonTransportAdaptorPropertyDtoArray[index].setSecured(property.isSecured());
                    commonTransportAdaptorPropertyDtoArray[index].setRequired(property.isRequired());
                    commonTransportAdaptorPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    commonTransportAdaptorPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    commonTransportAdaptorPropertyDtoArray[index].setHint(property.getHint());
                    commonTransportAdaptorPropertyDtoArray[index].setOptions(property.getOptions());
                    index++;
                }
            }
            return commonTransportAdaptorPropertyDtoArray;
        } else {
            return new TransportAdaptorPropertyDto[0];
        }

    }

    private boolean checkTransportAdaptorValidity(String transportAdaptorName) throws AxisFault {
        try {
            TransportAdaptorManagerHolder transportAdaptorManagerHolder = TransportAdaptorManagerHolder.getInstance();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<TransportAdaptorConfiguration> transportAdaptorConfigurationList = null;

            transportAdaptorConfigurationList = transportAdaptorManagerHolder.getTransportAdaptorManagerService().getAllTransportAdaptorConfiguration(axisConfiguration);


            Iterator transportAdaptorConfigurationListIterator = transportAdaptorConfigurationList.iterator();
            while (transportAdaptorConfigurationListIterator.hasNext()) {

                TransportAdaptorConfiguration transportAdaptorConfiguration = (TransportAdaptorConfiguration) transportAdaptorConfigurationListIterator.next();
                if (transportAdaptorConfiguration.getName().equalsIgnoreCase(transportAdaptorName)) {
                    return false;
                }
            }

        } catch (TransportAdaptorManagerConfigurationException e) {
            new AxisFault("Error in validating the transport adaptor");
        }
        return true;
    }

}