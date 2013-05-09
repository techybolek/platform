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


import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;
import org.wso2.carbon.transport.adaptor.manager.admin.internal.exception.TransportManagerAdminServiceException;
import org.wso2.carbon.transport.adaptor.manager.admin.internal.util.TransportHolder;
import org.wso2.carbon.transport.adaptor.manager.admin.internal.util.TransportManagerHolder;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorFile;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TMConfigurationException;

import java.util.List;
import java.util.Map;

public class TransportManagerAdminService extends AbstractAdmin {

    /**
     * @return Array of Transport Adaptor names
     * @throws TransportManagerAdminServiceException
     *          if Transport names are empty
     */
    public String[] getTransportNames() throws TransportManagerAdminServiceException {
        TransportHolder transportHolder = TransportHolder.getInstance();
        List<TransportAdaptorDto> transportAdaptorDtoList = transportHolder.getTransportService().getTransportAdaptors();
        if (transportAdaptorDtoList != null) {
            String[] transportNames = new String[transportAdaptorDtoList.size()];
            for (int index = 0; index < transportNames.length; index++) {
                transportNames[index] = transportAdaptorDtoList.get(index).getName();
            }
            return transportNames;
        }
        throw new TransportManagerAdminServiceException("No Transport Names are received.");
    }

    /**
     * Get transport adaptor properties with property parameters such as isRequired,isSecured
     *
     * @param transportName - transport adaptor name
     * @return input transport properties
     * @throws TransportManagerAdminServiceException
     *          if transport properties not found
     */
    public TransportPropertyDto[] getInputTransportProperties(String transportName)
            throws TransportManagerAdminServiceException {
        TransportHolder transportHolder = TransportHolder.getInstance();
        List<TransportAdaptorDto> transportAdaptorDtoList = transportHolder.getTransportService().getTransportAdaptors();
        for (TransportAdaptorDto transportAdaptorDto : transportAdaptorDtoList) {
            // check for transport adaptor with transport name
            if (transportAdaptorDto.getName().equals(transportName)) {
                // get transport adaptor properties

                List<Property> inputPropertyList = transportAdaptorDto.getAdaptorInPropertyList();
                if (inputPropertyList != null) {
                    TransportPropertyDto[] inputTransportPropertyDtoArray = new TransportPropertyDto[inputPropertyList.size()];
                    for (int index = 0; index < inputTransportPropertyDtoArray.length; index++) {
                        Property property = inputPropertyList.get(index);
                        // set transport property parameters
                        inputTransportPropertyDtoArray[index] = new TransportPropertyDto(property.getPropertyName(), "");
                        inputTransportPropertyDtoArray[index].setRequired(property.isRequired());
                        inputTransportPropertyDtoArray[index].setSecured(property.isSecured());
                        inputTransportPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                        inputTransportPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                        inputTransportPropertyDtoArray[index].setHint(property.getHint());
                        inputTransportPropertyDtoArray[index].setOptions(property.getOptions());
                    }
                    return inputTransportPropertyDtoArray;
                }

                return new TransportPropertyDto[0];
            }


        }
        throw new TransportManagerAdminServiceException("No Transport adaptor Input Properties are received.");
    }

    /**
     * @param transportName -transport adaptor name
     * @return output transport properties
     * @throws TransportManagerAdminServiceException
     *
     */
    public TransportPropertyDto[] getOutputTransportProperties(String transportName)
            throws TransportManagerAdminServiceException {
        TransportHolder transportHolder = TransportHolder.getInstance();
        List<TransportAdaptorDto> transportAdaptorDtoList = transportHolder.getTransportService().getTransportAdaptors();
        for (TransportAdaptorDto transportAdaptorDto : transportAdaptorDtoList) {
            // check for transport adaptor with transport name
            if (transportAdaptorDto.getName().equals(transportName)) {
                // get transport adaptor properties

                List<Property> outputPropertyList = transportAdaptorDto.getAdaptorOutPropertyList();
                if (outputPropertyList != null) {
                    TransportPropertyDto[] outputTransportPropertyDtoArray = new TransportPropertyDto[outputPropertyList.size()];
                    for (int index = 0; index < outputTransportPropertyDtoArray.length; index++) {
                        Property property = outputPropertyList.get(index);
                        // set transport property parameters
                        outputTransportPropertyDtoArray[index] = new TransportPropertyDto(property.getPropertyName(), "");
                        outputTransportPropertyDtoArray[index].setRequired(property.isRequired());
                        outputTransportPropertyDtoArray[index].setSecured(property.isSecured());
                        outputTransportPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                        outputTransportPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                        outputTransportPropertyDtoArray[index].setHint(property.getHint());
                        outputTransportPropertyDtoArray[index].setOptions(property.getOptions());
                    }
                    return outputTransportPropertyDtoArray;
                }
                return new TransportPropertyDto[0];
            }


        }
        throw new TransportManagerAdminServiceException("No Transport adaptor Output Properties are received.");
    }


    /**
     * @param transportName - transport adaptor name
     * @return common transport properties
     * @throws TransportManagerAdminServiceException
     *
     */
    public TransportPropertyDto[] getCommonTransportProperties(String transportName)
            throws TransportManagerAdminServiceException {
        TransportHolder transportHolder = TransportHolder.getInstance();
        List<TransportAdaptorDto> transportAdaptorDtoList = transportHolder.getTransportService().getTransportAdaptors();
        for (TransportAdaptorDto transportAdaptorDto : transportAdaptorDtoList) {
            // check for transport adaptor with transport name
            if (transportAdaptorDto.getName().equals(transportName)) {
                // get transport adaptor properties

                List<Property> commonPropertyList = transportAdaptorDto.getAdaptorCommonPropertyList();

                if (commonPropertyList != null) {

                    TransportPropertyDto[] commonTransportPropertyDtoArray = new TransportPropertyDto[commonPropertyList.size()];
                    for (int index = 0; index < commonTransportPropertyDtoArray.length; index++) {
                        Property property = commonPropertyList.get(index);
                        // set transport property parameters
                        commonTransportPropertyDtoArray[index] = new TransportPropertyDto(property.getPropertyName(), "");
                        commonTransportPropertyDtoArray[index].setRequired(property.isRequired());
                        commonTransportPropertyDtoArray[index].setSecured(property.isSecured());
                        commonTransportPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                        commonTransportPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                        commonTransportPropertyDtoArray[index].setHint(property.getHint());
                        commonTransportPropertyDtoArray[index].setOptions(property.getOptions());
                    }
                    return commonTransportPropertyDtoArray;
                }

                return new TransportPropertyDto[0];
            }


        }
        throw new TransportManagerAdminServiceException("No Transport adaptor Common Properties are received.");
    }

    /**
     * @param transportName transport adaptor name
     * @return transport adaptor properties
     * @throws TransportManagerAdminServiceException
     *
     */
    public TransportAdaptorProperties getAllTransportAdaptorProperties(String transportName) throws TransportManagerAdminServiceException {
        TransportAdaptorProperties transportAdaptorProperties = new TransportAdaptorProperties();
        transportAdaptorProperties.setCommonTransportPropertyDtos(getCommonTransportProperties(transportName));
        transportAdaptorProperties.setInputTransportPropertyDtos(getInputTransportProperties(transportName));
        transportAdaptorProperties.setOutputTransportPropertyDtos(getOutputTransportProperties(transportName));

        return transportAdaptorProperties;

    }


    /**
     * Add Transport Adaptor Configuration
     *
     * @param transportAdaptorName -name of the transport adaptor to be added
     * @param transportAdaptorType -transport adaptor type; jms,ws-event
     * @param inputPropertyDtos    - input adaptor properties with values
     * @param outputPropertyDtos   - output adaptor properties with values
     * @param commonPropertyDtos   - common adaptor properties with values
     */
    public void addTransportConfiguration(String transportAdaptorName, String transportAdaptorType,
                                          TransportPropertyDto[] inputPropertyDtos, TransportPropertyDto[] outputPropertyDtos, TransportPropertyDto[] commonPropertyDtos)
            throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManagerHolder = TransportManagerHolder.getInstance();
        TransportAdaptorConfiguration transportAdaptorConfiguration = new TransportAdaptorConfiguration();
        transportAdaptorConfiguration.setName(transportAdaptorName);
        transportAdaptorConfiguration.setType(transportAdaptorType);

        AxisConfiguration axisConfiguration = getAxisConfig();

        // add input transport adaptor properties
        for (TransportPropertyDto transportPropertyDto : inputPropertyDtos) {
            transportAdaptorConfiguration.addInputAdaptorProperty(transportPropertyDto.getKey(), transportPropertyDto.getValue());
        }

        // add output transport adaptor properties
        for (TransportPropertyDto transportPropertyDto : outputPropertyDtos) {
            transportAdaptorConfiguration.addOutputAdaptorProperty(transportPropertyDto.getKey(), transportPropertyDto.getValue());
        }

        // add common transport adaptor properties
        for (TransportPropertyDto transportPropertyDto : commonPropertyDtos) {
            transportAdaptorConfiguration.addCommonAdaptorProperty(transportPropertyDto.getKey(), transportPropertyDto.getValue());
        }


        // add transport adaptor configuration
        try {
            transportManagerHolder.getTransportManagerService().saveTransportConfiguration(transportAdaptorConfiguration, axisConfiguration);

        } catch (TMConfigurationException e) {
            throw new TransportManagerAdminServiceException("Error in adding transport Configuration", e);
        }
    }

    /**
     * Remove given transport adaptor configuration
     *
     * @param transportAdaptorName transport adaptor to be removed
     */
    public void removeTransportConfiguration(String transportAdaptorName)
            throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            transportManager.getTransportManagerService().removeTransportConfiguration(transportAdaptorName, axisConfiguration);
        } catch (TMConfigurationException e) {
            throw new TransportManagerAdminServiceException("Error in removing transport adaptor configurations" + e);
        }
    }

    public void editTransportAdaptorConfigurationFile(String transportAdaptorConfiguration, String transportAdaptorName) throws TransportManagerAdminServiceException {

        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            transportManager.getTransportManagerService().editTransportConfigurationFile(transportAdaptorConfiguration, transportAdaptorName, axisConfiguration);

        } catch (TMConfigurationException e) {
            throw new TransportManagerAdminServiceException("Error when editing transport adaptor configurations" + e);
        }

    }


    public String getTransportAdaptorConfigurationFile(String transportAdaptorName) throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            String transportConfigurationFile = transportManager.getTransportManagerService().getTransportConfigurationFile(transportAdaptorName, axisConfiguration);
            return transportConfigurationFile;
        } catch (TMConfigurationException e) {
            throw new TransportManagerAdminServiceException("Error in getting transport adaptor configurations" + e);
        }
    }

    public void removeTransportAdaptorFile(String filePath)
            throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            transportManager.getTransportManagerService().removeTransportAdaptorFile(filePath,axisConfig);
        } catch (TMConfigurationException e) {
            throw new TransportManagerAdminServiceException("Error in removing transport adaptor configurations" + e);
        }
    }

    /**
     * Get transport adaptor configurations and convert to TransportConfigurationDto
     *
     * @return Array of TransportConfigurationDto
     * @throws TransportManagerAdminServiceException
     *
     */
    public TransportConfigurationDto[] getAllTransportConfigurationNamesAndTypes()
            throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();

        AxisConfiguration axisConfiguration = getAxisConfig();
        // get transport adaptor configurations
        List<TransportAdaptorConfiguration> transportAdaptorConfigurationList = transportManager.getTransportManagerService().
                getAllTransportConfigurations(axisConfiguration);
        if (transportAdaptorConfigurationList != null) {
            // create transport adaptor configuration details array
            TransportConfigurationDto[] transportConfigurationDtoArray = new
                    TransportConfigurationDto[transportAdaptorConfigurationList.size()];
            for (int index = 0; index < transportConfigurationDtoArray.length; index++) {
                TransportAdaptorConfiguration transportAdaptorConfiguration = transportAdaptorConfigurationList.get(index);
                String transportAdaptorName = transportAdaptorConfiguration.getName();
                String transportAdaptorType = transportAdaptorConfiguration.getType();

                Map<String, String> inputPropertiesMap = transportAdaptorConfiguration.getInputAdaptorProperties();
                Map<String, String> outputPropertiesMap = transportAdaptorConfiguration.getOutputAdaptorProperties();
                Map<String, String> commonPropertiesMap = transportAdaptorConfiguration.getCommonAdaptorProperties();

                // create transport adaptor configuration details with transport adaptor name and type
                transportConfigurationDtoArray[index] = new TransportConfigurationDto(
                        transportAdaptorName, transportAdaptorType, inputPropertiesMap.size(), outputPropertiesMap.size(), commonPropertiesMap.size());

                // add input transport adaptor properties
                for (Map.Entry entry : inputPropertiesMap.entrySet()) {
                    transportConfigurationDtoArray[index].addInputTransportProperty(entry.getKey().toString(),
                            entry.getValue().toString());
                }

                // add output transport adaptor properties
                for (Map.Entry entry : outputPropertiesMap.entrySet()) {
                    transportConfigurationDtoArray[index].addOutputTransportProperty(entry.getKey().toString(),
                            entry.getValue().toString());
                }

                // add common transport adaptor properties
                for (Map.Entry entry : commonPropertiesMap.entrySet()) {
                    transportConfigurationDtoArray[index].addCommonTransportProperty(entry.getKey().toString(),
                            entry.getValue().toString());
                }

            }
            return transportConfigurationDtoArray;
        } else {
            throw new TransportManagerAdminServiceException("No Transport Adaptor Configurations received.");
        }

    }

    /**
     * Get the Transport Adaptor files which are undeployed
     *
     * @return Transport Adaptor File Array
     * @throws TransportManagerAdminServiceException
     *
     */
    public TransportAdaptorFileDto[] getUnDeployedFiles() throws TransportManagerAdminServiceException {

        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();

        List<TransportAdaptorFile> transportAdaptorFileList = transportManager.getTransportManagerService().
                getUnDeployedFiles(axisConfiguration);
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
            throw new TransportManagerAdminServiceException("No Transport Adaptor Files received.");
        }


    }

    /**
     * Get Transport adaptor details
     *
     * @param transportName
     * @return Array of transport adaptor properties including parameters such as isSecured, isRequired
     * @throws TransportManagerAdminServiceException
     *          if Transport adaptor configuration not found
     */
    public TransportPropertyDto[] getInputTransportConfiguration(String transportName)
            throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();

        // get transport adaptor to get input transport properties with parameters isSecured, isRequired
        TransportHolder transportHolder = TransportHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        TransportAdaptorConfiguration transportAdaptorConfiguration = null;
        try {
            transportAdaptorConfiguration = transportManager.getTransportManagerService().
                    getTransportConfiguration(transportName, axisConfiguration);
        } catch (TMConfigurationException e) {
            new TransportManagerAdminServiceException("No Transport configuration for " + transportName);
        }
        if (transportAdaptorConfiguration != null) {
            // get transport adaptor type
            String transportType = transportAdaptorConfiguration.getType();
            // get input transport adaptor properties
            List<Property> inputPropertyList = transportHolder.getTransportService().getInputAdaptorTransportProperties(
                    transportType);
            Map<String, String> inputTransportProperties = transportAdaptorConfiguration.getInputAdaptorProperties();
            TransportPropertyDto[] inputTransportPropertyDtoArray = new TransportPropertyDto[inputTransportProperties.size()];
            int index = 0;
            if (inputPropertyList != null) {
                for (Property property : inputPropertyList) {
                    // create input transport property
                    inputTransportPropertyDtoArray[index] = new TransportPropertyDto(property.getPropertyName(),
                            inputTransportProperties.get(property.
                                    getPropertyName()));
                    // set input transport property parameters
                    inputTransportPropertyDtoArray[index].setSecured(property.isSecured());
                    inputTransportPropertyDtoArray[index].setRequired(property.isRequired());
                    inputTransportPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    inputTransportPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    inputTransportPropertyDtoArray[index].setHint(property.getHint());
                    inputTransportPropertyDtoArray[index].setOptions(property.getOptions());

                    index++;
                }

            }
            return inputTransportPropertyDtoArray;
        } else {
            throw new TransportManagerAdminServiceException("No such transport adaptor exists.");
        }
    }


    public TransportPropertyDto[] getOutputTransportConfiguration(String transportName)
            throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();

        // get transport adaptor to get output transport properties with parameters isSecured, isRequired
        TransportHolder transportHolder = TransportHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        TransportAdaptorConfiguration transportAdaptorConfiguration = null;
        try {
            transportAdaptorConfiguration = transportManager.getTransportManagerService().
                    getTransportConfiguration(transportName, axisConfiguration);
        } catch (TMConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if (transportAdaptorConfiguration != null) {
            // get transport adaptor type
            String transportType = transportAdaptorConfiguration.getType();
            // get output adaptor properties
            List<Property> propertyList = transportHolder.getTransportService().getOutputAdaptorTransportProperties(
                    transportType);
            Map<String, String> outputTransportProperties = transportAdaptorConfiguration.getOutputAdaptorProperties();
            TransportPropertyDto[] outputTransportPropertyDtoArray = new TransportPropertyDto[outputTransportProperties.size()];
            int index = 0;
            if (outputTransportProperties != null) {
                for (Property property : propertyList) {
                    // create transport adaptor property
                    outputTransportPropertyDtoArray[index] = new TransportPropertyDto(property.getPropertyName(),
                            outputTransportProperties.get(property.getPropertyName()));

                    // set output transport adaptor property parameters
                    outputTransportPropertyDtoArray[index].setSecured(property.isSecured());
                    outputTransportPropertyDtoArray[index].setRequired(property.isRequired());
                    outputTransportPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    outputTransportPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    outputTransportPropertyDtoArray[index].setHint(property.getHint());
                    outputTransportPropertyDtoArray[index].setOptions(property.getOptions());
                    index++;
                }
            }
            return outputTransportPropertyDtoArray;
        } else {
            throw new TransportManagerAdminServiceException("No such transport adaptor exists.");
        }
    }


    public TransportPropertyDto[] getCommonTransportConfiguration(String transportName)
            throws TransportManagerAdminServiceException {
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();

        // get transport adaptor to get common transport properties with parameters isSecured, isRequired
        TransportHolder transportHolder = TransportHolder.getInstance();
        AxisConfiguration axisConfiguration = getAxisConfig();
        TransportAdaptorConfiguration transportAdaptorConfiguration = null;
        try {
            transportAdaptorConfiguration = transportManager.getTransportManagerService().
                    getTransportConfiguration(transportName, axisConfiguration);
        } catch (TMConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if (transportAdaptorConfiguration != null) {
            // get transport adaptor type
            String transportType = transportAdaptorConfiguration.getType();
            // get transport adaptor properties
            List<Property> propertyList = transportHolder.getTransportService().getCommonAdaptorTransportProperties(
                    transportType);
            Map<String, String> commonTransportProperties = transportAdaptorConfiguration.getCommonAdaptorProperties();
            TransportPropertyDto[] commonTransportPropertyDtoArray = new TransportPropertyDto[commonTransportProperties.size()];
            int index = 0;
            if (commonTransportProperties != null) {
                for (Property property : propertyList) {
                    // create common transport adaptor  property
                    commonTransportPropertyDtoArray[index] = new TransportPropertyDto(property.getPropertyName(),
                            commonTransportProperties.get(property.
                                    getPropertyName()));
                    // set common transport adaptor property parameters
                    commonTransportPropertyDtoArray[index].setSecured(property.isSecured());
                    commonTransportPropertyDtoArray[index].setRequired(property.isRequired());
                    commonTransportPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    commonTransportPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    commonTransportPropertyDtoArray[index].setHint(property.getHint());
                    commonTransportPropertyDtoArray[index].setOptions(property.getOptions());
                    index++;
                }
            }
            return commonTransportPropertyDtoArray;
        } else {
            throw new TransportManagerAdminServiceException("No such transport adaptor exists.");
        }
    }

    private void testTransportConfiguration(String transportName)
            throws TransportManagerAdminServiceException {
        TransportHolder transportHolder = TransportHolder.getInstance();
        TransportManagerHolder transportManager = TransportManagerHolder.getInstance();

        AxisConfiguration axisConfiguration = getAxisConfig();
        TransportAdaptorConfiguration transportAdaptorConfiguration =
                null;
        try {
            transportAdaptorConfiguration = transportManager.getTransportManagerService().getTransportConfiguration(transportName, axisConfiguration);
        } catch (TMConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration configuration =
                new org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration();

        configuration.setName(transportAdaptorConfiguration.getName());
        configuration.setType(transportAdaptorConfiguration.getType());
        configuration.setInputAdaptorProperties(transportAdaptorConfiguration.getInputAdaptorProperties());
        configuration.setOutputAdaptorProperties(transportAdaptorConfiguration.getOutputAdaptorProperties());
        configuration.setCommonAdaptorProperties(transportAdaptorConfiguration.getCommonAdaptorProperties());
        try {
            transportHolder.getTransportService().testConnection(configuration);
        } catch (TransportEventProcessingException e) {
            removeTransportConfiguration(transportName);
            throw new TransportManagerAdminServiceException("Error at testing transport adaptor configuration with name"
                    + transportName + ". " + e.getMessage(), e);
        }
    }


}