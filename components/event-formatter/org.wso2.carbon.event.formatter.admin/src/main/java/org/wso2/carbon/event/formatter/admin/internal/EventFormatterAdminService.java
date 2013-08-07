/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.formatter.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.admin.internal.util.EventFormatterAdminServiceValueHolder;
import org.wso2.carbon.event.formatter.admin.internal.util.PropertyAttributeTypeConstants;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.config.EventOutputProperty;
import org.wso2.carbon.event.formatter.core.internal.config.ToPropertyConfiguration;
import org.wso2.carbon.event.formatter.core.internal.type.json.JSONFormatterFactory;
import org.wso2.carbon.event.formatter.core.internal.type.json.JSONOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.map.MapEventFormatterFactory;
import org.wso2.carbon.event.formatter.core.internal.type.map.MapOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.text.TextEventFormatterFactory;
import org.wso2.carbon.event.formatter.core.internal.type.text.TextOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.wso2event.WSO2EventFormatterFactory;
import org.wso2.carbon.event.formatter.core.internal.type.wso2event.WSO2EventOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.type.xml.XMLEventFormatterFactory;
import org.wso2.carbon.event.formatter.core.internal.type.xml.XMLOutputMapping;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorDto;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorService;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorInfo;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventFormatterAdminService extends AbstractAdmin {


    public EventFormatterConfigurationInfoDto[] getAllEventFormatterConfigurationInfo()
            throws AxisFault {

        try {
            EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event formatter configurations
            List<EventFormatterConfiguration> eventFormatterConfigurationList = null;
            eventFormatterConfigurationList = eventFormatterService.getAllEventFormatterConfiguration(axisConfiguration);

            if (eventFormatterConfigurationList != null) {
                // create event formatter configuration details array
                EventFormatterConfigurationInfoDto[] eventFormatterConfigurationInfoDtoArray = new
                        EventFormatterConfigurationInfoDto[eventFormatterConfigurationList.size()];
                for (int index = 0; index < eventFormatterConfigurationInfoDtoArray.length; index++) {
                    EventFormatterConfiguration eventFormatterConfiguration = eventFormatterConfigurationList.get(index);
                    String eventFormatterName = eventFormatterConfiguration.getEventFormatterName();
                    String mappingType = eventFormatterConfiguration.getOutputMapping().getMappingType();
                    String outputTransportAdaptorName = eventFormatterConfiguration.getToPropertyConfiguration().getTransportAdaptorName();
                    String outputTransportAdaptorType = eventFormatterConfiguration.getToPropertyConfiguration().getTransportAdaptorType();


                    eventFormatterConfigurationInfoDtoArray[index] = new EventFormatterConfigurationInfoDto();
                    eventFormatterConfigurationInfoDtoArray[index].setEventFormatterName(eventFormatterName);
                    eventFormatterConfigurationInfoDtoArray[index].setMappingType(mappingType);
                    eventFormatterConfigurationInfoDtoArray[index].setOutTransportAdaptorName(outputTransportAdaptorName);
                    eventFormatterConfigurationInfoDtoArray[index].setOutputTransportAdaptorType(outputTransportAdaptorType);
                    eventFormatterConfigurationInfoDtoArray[index].setEnableStats(eventFormatterConfiguration.isEnableStatistics());
                    eventFormatterConfigurationInfoDtoArray[index].setEnableTracing(eventFormatterConfiguration.isEnableTracing());
                }
                return eventFormatterConfigurationInfoDtoArray;
            } else {
                return new EventFormatterConfigurationInfoDto[0];
            }
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault("No event formatter configurations received, " + e.getMessage());
        }
    }

    public void removeEventFormatterConfiguration(String eventFormatterName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.removeEventFormatterConfiguration(eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault("Error in removing event formatter configurations , " + e.getMessage());
        }
    }


    public EventFormatterConfigurationFileDto[] getNotDeployedEventFormatterConfigurationFiles()
            throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterService.getNotDeployedEventFormatterConfigurationFiles(axisConfiguration);
        if (eventFormatterConfigurationFileList != null) {

            // create event formatter file details array
            EventFormatterConfigurationFileDto[] eventFormatterFileDtoArray = new
                    EventFormatterConfigurationFileDto[eventFormatterConfigurationFileList.size()];

            for (int index = 0; index < eventFormatterFileDtoArray.length; index++) {
                EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileList.get(index);
                String filePath = eventFormatterConfigurationFile.getFilePath();
                String eventFormatterName = eventFormatterConfigurationFile.getEventFormatterName();

                eventFormatterFileDtoArray[index] = new EventFormatterConfigurationFileDto(filePath, eventFormatterName);
            }
            return eventFormatterFileDtoArray;
        } else {
            return new EventFormatterConfigurationFileDto[0];
        }
    }

    public void removeEventFormatterConfigurationFile(String filePath)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            eventFormatterService.removeEventFormatterConfigurationFile(filePath, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault("Error when removing event formatter configurations , " + e.getMessage());
        }
    }

    public String getNotDeployedEventFormatterConfigurationFile(String filePath)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        try {
            String eventFormatterConfigurationFile = eventFormatterService.getNotDeployedEventFormatterConfigurationFile(filePath);
            return eventFormatterConfigurationFile.trim();
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault("Error when retrieving event formatter configurations , " + e.getMessage());
        }
    }

    public void editNotDeployedEventFormatterConfigurationFile(
            String eventFormatterConfiguration,
            String filePath)
            throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.editNotDeployedEventFormatterConfigurationFile(eventFormatterConfiguration, filePath, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault("Error when editing event formatter configurations , " + e.getMessage());
        }
    }

    public String getEventFormatterConfigurationFile(String eventFormatterName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            String transportAdaptorConfigurationFile = eventFormatterService.getEventFormatterConfigurationFile(eventFormatterName, axisConfiguration);
            return transportAdaptorConfigurationFile;
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault("Error when retrieving event formatter configurations , " + e.getMessage());
        }
    }

    public void editEventFormatterConfigurationFile(String eventFormatterConfiguration,
                                                    String eventFormatterName)
            throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.editEventFormatterConfigurationFile(eventFormatterConfiguration, eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault("Error when editing event formatter configurations , " + e.getMessage());
        }
    }


    public String[] getAllEventStreamNames() throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            List<String> streamNames = eventFormatterService.getAllEventStreams(axisConfiguration);
            return streamNames.toArray(new String[0]);
        } catch (EventFormatterConfigurationException ex) {
            throw new AxisFault("Error while retrieving the stream names" + ex.getMessage());
        }
    }

    public String getStreamDefinition(String streamNameWithVersion) throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            StreamDefinition streamDefinition = eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration);
            return getStreamAttributes(streamDefinition);
        } catch (EventFormatterConfigurationException ex) {
            throw new AxisFault("Error while retrieving the stream definition" + ex.getMessage());
        }

    }

    public String[] getOutputTransportAdaptorNames() throws AxisFault {

        OutputTransportAdaptorManagerService transportAdaptorManagerService = EventFormatterAdminServiceValueHolder.getOutputTransportAdaptorManagerService();
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        try {
            List<OutputTransportAdaptorInfo> transportAdaptorInfoList = transportAdaptorManagerService.getOutputTransportAdaptorInfo(tenantId);
            if (transportAdaptorInfoList != null) {
                String[] transportAdaptorNames = new String[transportAdaptorInfoList.size()];
                for (int index = 0; index < transportAdaptorNames.length; index++) {

                    OutputTransportAdaptorInfo transportAdaptorInfo = transportAdaptorInfoList.get(index);
                    transportAdaptorNames[index] = transportAdaptorInfo.getTransportAdaptorName();
                }
                return transportAdaptorNames;
            }
            return new String[0];
        } catch (Exception ex) {
            throw new AxisFault("Error while retrieving the transport adaptor names");
        }

    }

    public String[] getSupportedMappingTypes(String transportAdaptorName) throws AxisFault {

        OutputTransportAdaptorManagerService transportAdaptorManagerService = EventFormatterAdminServiceValueHolder.getOutputTransportAdaptorManagerService();
        OutputTransportAdaptorService transportAdaptorService = EventFormatterAdminServiceValueHolder.getOutputTransportAdaptorService();
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            List<OutputTransportAdaptorInfo> transportAdaptorInfoList = transportAdaptorManagerService.getOutputTransportAdaptorInfo(tenantId);
            Iterator<OutputTransportAdaptorInfo> transportAdaptorInfoIterator = transportAdaptorInfoList.iterator();
            while (transportAdaptorInfoIterator.hasNext()) {
                OutputTransportAdaptorInfo transportAdaptorInfo = transportAdaptorInfoIterator.next();
                if (transportAdaptorInfo.getTransportAdaptorName().equals(transportAdaptorName)) {
                    String transportAdaptorType = transportAdaptorInfo.getTransportAdaptorType();
                    OutputTransportAdaptorDto transportAdaptorDto = transportAdaptorService.getTransportAdaptorDto(transportAdaptorType);
                    List<OutputTransportAdaptorDto.MessageType> supportedOutputMessageTypes = transportAdaptorDto.getSupportedMessageTypes();
                    String[] supportedMappingTypes = new String[supportedOutputMessageTypes.size()];
                    for (int index = 0; index < supportedMappingTypes.length; index++) {
                        OutputTransportAdaptorDto.MessageType mappingInfo = supportedOutputMessageTypes.get(index);
                        supportedMappingTypes[index] = mappingInfo.toString().toLowerCase();
                    }
                    return supportedMappingTypes;
                }
            }
        } catch (Exception ex) {
            throw new AxisFault("Error while retrieving the mapping types");
        }

        return new String[0];

    }

    public EventFormatterPropertyDto[] getEventFormatterProperties(String transportAdaptorName)
            throws AxisFault {

        OutputTransportAdaptorService transportAdaptorService = EventFormatterAdminServiceValueHolder.getOutputTransportAdaptorService();

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        String transportAdaptorType = "";

        try {
            transportAdaptorType = getTransportAdaptorType(transportAdaptorName, tenantId);
            MessageDto messageDto = transportAdaptorService.getTransportMessageDto(transportAdaptorType);

            List<Property> propertyList = messageDto.getMessageOutPropertyList();
            if (propertyList != null) {
                EventFormatterPropertyDto[] eventFormatterPropertyDtoArray = new EventFormatterPropertyDto[propertyList.size()];
                for (int index = 0; index < eventFormatterPropertyDtoArray.length; index++) {
                    Property property = propertyList.get(index);
                    // set event formatter property parameters
                    eventFormatterPropertyDtoArray[index] = new EventFormatterPropertyDto(property.getPropertyName(), "");
                    eventFormatterPropertyDtoArray[index].setRequired(property.isRequired());
                    eventFormatterPropertyDtoArray[index].setSecured(property.isSecured());
                    eventFormatterPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    eventFormatterPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    eventFormatterPropertyDtoArray[index].setHint(property.getHint());
                    eventFormatterPropertyDtoArray[index].setOptions(property.getOptions());
                }
                return eventFormatterPropertyDtoArray;
            }
        } catch (Exception ex) {
            throw new AxisFault("Error while retrieving the transport adaptor names");
        }
        return new EventFormatterPropertyDto[0];
    }

    private String getTransportAdaptorType(String transportAdaptorName, int tenantId) {

        OutputTransportAdaptorManagerService transportAdaptorManagerService = EventFormatterAdminServiceValueHolder.getOutputTransportAdaptorManagerService();

        String transportAdaptorType = "";

        List<OutputTransportAdaptorInfo> transportAdaptorInfoList = transportAdaptorManagerService.getOutputTransportAdaptorInfo(tenantId);
        Iterator<OutputTransportAdaptorInfo> transportAdaptorInfoIterator = transportAdaptorInfoList.iterator();
        while (transportAdaptorInfoIterator.hasNext()) {
            OutputTransportAdaptorInfo transportAdaptorInfo = transportAdaptorInfoIterator.next();
            if (transportAdaptorInfo.getTransportAdaptorName().equals(transportAdaptorName)) {
                transportAdaptorType = transportAdaptorInfo.getTransportAdaptorType();
                break;
            }
        }
        return transportAdaptorType;
    }

    public void addWSO2EventFormatter(String eventFormatterName, String streamNameWithVersion,
                                      String transportAdaptorName,
                                      EventOutputPropertyConfigurationDto[] metaData,
                                      EventOutputPropertyConfigurationDto[] correlationData,
                                      EventOutputPropertyConfigurationDto[] payloadData,
                                      EventFormatterPropertyDto[] outputPropertyConfiguration)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration(new WSO2EventFormatterFactory());

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = CarbonContext.getCurrentContext().getTenantId();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setTransportAdaptorName(transportAdaptorName);
                toPropertyConfiguration.setTransportAdaptorType(getTransportAdaptorType(transportAdaptorName, tenantId));

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration = new OutputTransportAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputTransportMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputTransportAdaptorMessageConfiguration(outputTransportMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                WSO2EventOutputMapping wso2EventOutputMapping = new WSO2EventOutputMapping();
                List<String> outputEventAttributes = new ArrayList<String>();

                if (metaData != null && metaData.length != 0) {
                    for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : metaData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(wso2EventOutputPropertyConfiguration.getType()));
                        wso2EventOutputMapping.addMetaWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                    }

                }

                if (correlationData != null && correlationData.length != 0) {
                    for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : correlationData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(wso2EventOutputPropertyConfiguration.getType()));
                        wso2EventOutputMapping.addCorrelationWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                    }

                }

                if (payloadData != null && payloadData.length != 0) {
                    for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : payloadData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(wso2EventOutputPropertyConfiguration.getType()));
                        wso2EventOutputMapping.addPayloadWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                    }

                }

                eventFormatterConfiguration.setOutputMapping(wso2EventOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.saveEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                throw new AxisFault("Error in adding event formatter configuration , ", e);
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }


    public void addTextEventFormatter(String eventFormatterName, String streamNameWithVersion,
                                      String transportAdaptorName,
                                      String textData,
                                      EventFormatterPropertyDto[] outputPropertyConfiguration,
                                      String dataFrom)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration(new TextEventFormatterFactory());

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = CarbonContext.getCurrentContext().getTenantId();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setTransportAdaptorName(transportAdaptorName);
                toPropertyConfiguration.setTransportAdaptorType(getTransportAdaptorType(transportAdaptorName, tenantId));

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration = new OutputTransportAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputTransportMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputTransportAdaptorMessageConfiguration(outputTransportMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                TextOutputMapping textOutputMapping = new TextOutputMapping();
                textOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                textOutputMapping.setMappingText(textData);
                if (dataFrom.equalsIgnoreCase("registry")) {
                    textData = eventFormatterService.getRegistryResourceContent(textData, tenantId);
                }
                List<String> outputEventAttributes = getOutputMappingPropertyList(textData);

                eventFormatterConfiguration.setOutputMapping(textOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.saveEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                throw new AxisFault("Error in adding event formatter configuration , ", e);
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public void addXmlEventFormatter(String eventFormatterName, String streamNameWithVersion,
                                     String transportAdaptorName,
                                     String textData,
                                     EventFormatterPropertyDto[] outputPropertyConfiguration,
                                     String dataFrom)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration(new XMLEventFormatterFactory());

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = CarbonContext.getCurrentContext().getTenantId();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setTransportAdaptorName(transportAdaptorName);
                toPropertyConfiguration.setTransportAdaptorType(getTransportAdaptorType(transportAdaptorName, tenantId));

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration = new OutputTransportAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputTransportMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputTransportAdaptorMessageConfiguration(outputTransportMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                XMLOutputMapping xmlOutputMapping = new XMLOutputMapping();
                xmlOutputMapping.setMappingXMLText(textData);
                xmlOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                List<String> outputEventAttributes = getOutputMappingPropertyList(textData);

                eventFormatterConfiguration.setOutputMapping(xmlOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.saveEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                throw new AxisFault("Error in adding event formatter configuration , ", e);
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }


    public void addMapEventFormatter(String eventFormatterName, String streamNameWithVersion,
                                     String transportAdaptorName,
                                     EventOutputPropertyConfigurationDto[] mapData,
                                     EventFormatterPropertyDto[] outputPropertyConfiguration)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration(new MapEventFormatterFactory());

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = CarbonContext.getCurrentContext().getTenantId();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setTransportAdaptorName(transportAdaptorName);
                toPropertyConfiguration.setTransportAdaptorType(getTransportAdaptorType(transportAdaptorName, tenantId));

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration = new OutputTransportAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputTransportMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputTransportAdaptorMessageConfiguration(outputTransportMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                MapOutputMapping mapOutputMapping = new MapOutputMapping();
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mapData != null && mapData.length != 0) {
                    for (EventOutputPropertyConfigurationDto eventOutputPropertyConfiguration : mapData) {
                        EventOutputProperty eventOutputProperty = new EventOutputProperty(eventOutputPropertyConfiguration.getName(), eventOutputPropertyConfiguration.getValueOf());
                        mapOutputMapping.addOutputPropertyConfiguration(eventOutputProperty);
                        outputEventAttributes.add(eventOutputPropertyConfiguration.getValueOf());
                    }

                }

                eventFormatterConfiguration.setOutputMapping(mapOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.saveEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                throw new AxisFault("Error in adding event formatter configuration , ", e);
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public void addJsonEventFormatter(String eventFormatterName, String streamNameWithVersion,
                                      String transportAdaptorName,
                                      String jsonData,
                                      EventFormatterPropertyDto[] outputPropertyConfiguration,
                                      String dataFrom)
            throws AxisFault {

        if (checkEventFormatterValidity(eventFormatterName)) {
            try {
                EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();

                EventFormatterConfiguration eventFormatterConfiguration = new EventFormatterConfiguration(new JSONFormatterFactory());

                eventFormatterConfiguration.setEventFormatterName(eventFormatterName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventFormatterConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventFormatterConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = CarbonContext.getCurrentContext().getTenantId();

                ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
                toPropertyConfiguration.setTransportAdaptorName(transportAdaptorName);
                toPropertyConfiguration.setTransportAdaptorType(getTransportAdaptorType(transportAdaptorName, tenantId));

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration = new OutputTransportAdaptorMessageConfiguration();

                    for (EventFormatterPropertyDto eventFormatterProperty : outputPropertyConfiguration) {
                        if (!eventFormatterProperty.getValue().trim().equals("")) {
                            outputTransportMessageConfiguration.addOutputMessageProperty(eventFormatterProperty.getKey().trim(), eventFormatterProperty.getValue().trim());
                        }
                    }
                    toPropertyConfiguration.setOutputTransportAdaptorMessageConfiguration(outputTransportMessageConfiguration);
                }

                eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

                JSONOutputMapping jsonOutputMapping = new JSONOutputMapping();
                jsonOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                jsonOutputMapping.setMappingText(jsonData);
                List<String> outputEventAttributes = getOutputMappingPropertyList(jsonData);

                eventFormatterConfiguration.setOutputMapping(jsonOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventFormatterService.saveEventFormatterConfiguration(eventFormatterConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventFormatterConfigurationException e) {
                throw new AxisFault("Error in adding event formatter configuration , ", e);
            }
        } else {
            throw new AxisFault(eventFormatterName + " is already registered for this tenant");
        }

    }

    public EventFormatterConfigurationDto getEventFormatterConfigurationDto(
            String eventFormatterName) throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        AxisConfiguration axisConfiguration = getAxisConfig();

        try {
            EventFormatterConfiguration eventFormatterConfiguration = eventFormatterService.getEventFormatterConfiguration(eventFormatterName, tenantId);
            if (eventFormatterConfiguration != null) {
                EventFormatterConfigurationDto eventFormatterConfigurationDto = new EventFormatterConfigurationDto();
                eventFormatterConfigurationDto.setEventFormatterName(eventFormatterConfiguration.getEventFormatterName());
                String streamNameWithVersion = eventFormatterConfiguration.getFromStreamName() + ":" + eventFormatterConfiguration.getFromStreamVersion();
                eventFormatterConfigurationDto.setFromStreamNameWithVersion(streamNameWithVersion);
                eventFormatterConfigurationDto.setStreamDefinition(getStreamAttributes(eventFormatterService.getStreamDefinition(streamNameWithVersion, axisConfiguration)));

                ToPropertyConfiguration toPropertyConfiguration = eventFormatterConfiguration.getToPropertyConfiguration();
                if (toPropertyConfiguration != null) {
                    ToPropertyConfigurationDto toPropertyConfigurationDto = new ToPropertyConfigurationDto();
                    toPropertyConfigurationDto.setTransportAdaptorName(toPropertyConfiguration.getTransportAdaptorName());
                    toPropertyConfigurationDto.setTransportAdaptorType(toPropertyConfiguration.getTransportAdaptorType());
                    OutputTransportAdaptorMessageConfiguration outputTransportAdaptorMessageConfiguration = toPropertyConfiguration.getOutputTransportAdaptorMessageConfiguration();
                    if (outputTransportAdaptorMessageConfiguration != null && outputTransportAdaptorMessageConfiguration.getOutputMessageProperties().size() > 0) {
                        EventFormatterPropertyDto[] eventFormatterPropertyDtos = getOutputEventFormatterMessageConfiguration(outputTransportAdaptorMessageConfiguration.getOutputMessageProperties(), toPropertyConfiguration.getTransportAdaptorType());
                        toPropertyConfigurationDto.setOutputTransportAdaptorMessageConfiguration(eventFormatterPropertyDtos);
                    }

                    eventFormatterConfigurationDto.setToPropertyConfigurationDto(toPropertyConfigurationDto);
                }

                if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_JSON_MAPPING_TYPE)) {
                    JSONOutputMapping jsonOutputMapping = (JSONOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    JSONOutputMappingDto jsonOutputMappingDto = new JSONOutputMappingDto();
                    jsonOutputMappingDto.setMappingText(jsonOutputMapping.getMappingText());
                    jsonOutputMappingDto.setRegistryResource(jsonOutputMapping.isRegistryResource());
                    eventFormatterConfigurationDto.setJsonOutputMappingDto(jsonOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("json");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_XML_MAPPING_TYPE)) {
                    XMLOutputMapping xmlOutputMapping = (XMLOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    XMLOutputMappingDto xmlOutputMappingDto = new XMLOutputMappingDto();
                    xmlOutputMappingDto.setMappingXMLText(xmlOutputMapping.getMappingXMLText());
                    xmlOutputMappingDto.setRegistryResource(xmlOutputMapping.isRegistryResource());
                    eventFormatterConfigurationDto.setXmlOutputMappingDto(xmlOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("xml");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_TEXT_MAPPING_TYPE)) {
                    TextOutputMapping textOutputMapping = (TextOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    TextOutputMappingDto textOutputMappingDto = new TextOutputMappingDto();
                    textOutputMappingDto.setMappingText(textOutputMapping.getMappingText());
                    textOutputMappingDto.setRegistryResource(textOutputMapping.isRegistryResource());
                    eventFormatterConfigurationDto.setTextOutputMappingDto(textOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("text");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_MAP_MAPPING_TYPE)) {
                    MapOutputMapping mapOutputMapping = (MapOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    MapOutputMappingDto mapOutputMappingDto = new MapOutputMappingDto();
                    List<EventOutputProperty> outputPropertyList = mapOutputMapping.getOutputPropertyConfiguration();
                    if (outputPropertyList != null && outputPropertyList.size() > 0) {
                        EventOutputPropertyDto[] eventOutputPropertyDtos = new EventOutputPropertyDto[outputPropertyList.size()];
                        int index = 0;
                        Iterator<EventOutputProperty> outputPropertyIterator = outputPropertyList.iterator();
                        while (outputPropertyIterator.hasNext()) {
                            EventOutputProperty eventOutputProperty = outputPropertyIterator.next();
                            eventOutputPropertyDtos[index].setName(eventOutputProperty.getName());
                            eventOutputPropertyDtos[index].setValueOf(eventOutputProperty.getValueOf());
                            index++;
                        }
                        mapOutputMappingDto.setOutputPropertyConfiguration(eventOutputPropertyDtos);
                    }

                    eventFormatterConfigurationDto.setMapOutputMappingDto(mapOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("map");
                } else if (eventFormatterConfiguration.getOutputMapping().getMappingType().equals(EventFormatterConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
                    WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventFormatterConfiguration.getOutputMapping();
                    WSO2EventOutputMappingDto wso2EventOutputMappingDto = new WSO2EventOutputMappingDto();
                    List<EventOutputProperty> metaOutputPropertyList = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> correlationOutputPropertyList = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> payloadOutputPropertyList = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

                    wso2EventOutputMappingDto.setMetaWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(metaOutputPropertyList));
                    wso2EventOutputMappingDto.setCorrelationWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(correlationOutputPropertyList));
                    wso2EventOutputMappingDto.setPayloadWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(payloadOutputPropertyList));

                    eventFormatterConfigurationDto.setWso2EventOutputMappingDto(wso2EventOutputMappingDto);
                    eventFormatterConfigurationDto.setMappingType("wso2event");
                }

                return eventFormatterConfigurationDto;
            }

        } catch (EventFormatterConfigurationException ex) {
            throw new AxisFault("Error while retrieving the event formatter configuration" + ex.getMessage());
        }
        return null;
    }

    public void enableStatistics(String eventFormatterName) throws AxisFault {

        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.enableStatistics(eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }
    }

    public void disableStatistics(String eventFormatterName) throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.disableStatistics(eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }

    }

    public void enableTracing(String eventFormatterName) throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.enableTracing(eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }

    }

    public void disableTracing(String eventFormatterName) throws AxisFault {
        EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventFormatterService.disableTracing(eventFormatterName, axisConfiguration);
        } catch (EventFormatterConfigurationException e) {
            throw new AxisFault(e.getMessage());
        }

    }


    private EventOutputPropertyDto[] getEventPropertyDtoArray(
            List<EventOutputProperty> eventOutputPropertyList) {

        if (eventOutputPropertyList != null && eventOutputPropertyList.size() > 0) {
            EventOutputPropertyDto[] eventOutputPropertyDtos = new EventOutputPropertyDto[eventOutputPropertyList.size()];
            int index = 0;
            Iterator<EventOutputProperty> outputPropertyIterator = eventOutputPropertyList.iterator();
            while (outputPropertyIterator.hasNext()) {
                EventOutputProperty eventOutputProperty = outputPropertyIterator.next();
                eventOutputPropertyDtos[index] = new EventOutputPropertyDto(eventOutputProperty.getName(), eventOutputProperty.getValueOf(), eventOutputProperty.getType().toString());
                index++;
            }

            return eventOutputPropertyDtos;
        }
        return null;
    }

    private EventFormatterPropertyDto[] getOutputEventFormatterMessageConfiguration(
            Map<String, String> messageProperties, String transportAdaptorType) {

        OutputTransportAdaptorService outputTransportAdaptorService = EventFormatterAdminServiceValueHolder.getOutputTransportAdaptorService();
        List<Property> outputMessagePropertyList = outputTransportAdaptorService.getTransportMessageDto(transportAdaptorType).getMessageOutPropertyList();
        if (outputMessagePropertyList != null) {
            EventFormatterPropertyDto[] eventFormatterPropertyDtoArray = new EventFormatterPropertyDto[outputMessagePropertyList.size()];
            int index = 0;
            for (Property property : outputMessagePropertyList) {
                // create output transport property
                eventFormatterPropertyDtoArray[index] = new EventFormatterPropertyDto(property.getPropertyName(),
                                                                                      messageProperties.get(property.getPropertyName()));
                // set output transport property parameters
                eventFormatterPropertyDtoArray[index].setSecured(property.isSecured());
                eventFormatterPropertyDtoArray[index].setRequired(property.isRequired());
                eventFormatterPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                eventFormatterPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                eventFormatterPropertyDtoArray[index].setHint(property.getHint());
                eventFormatterPropertyDtoArray[index].setOptions(property.getOptions());

                index++;
            }
            return eventFormatterPropertyDtoArray;
        }
        return new EventFormatterPropertyDto[0];
    }


    private boolean checkStreamAttributeValidity(List<String> outputEventAttributes,
                                                 StreamDefinition streamDefinition) {

        List<String> inComingStreamAttributes = new ArrayList<String>();
        final String PROPERTY_META_PREFIX = "meta_";
        final String PROPERTY_CORRELATION_PREFIX = "correlation_";

        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();


        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                inComingStreamAttributes.add(PROPERTY_META_PREFIX + attribute.getName());
            }
        }
        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                inComingStreamAttributes.add(PROPERTY_CORRELATION_PREFIX + attribute.getName());
            }
        }
        if (payloadAttributeList != null) {
            for (Attribute attribute : payloadAttributeList) {
                inComingStreamAttributes.add(attribute.getName());
            }
        }


        if (outputEventAttributes.size() > 0) {
            if (inComingStreamAttributes.containsAll(outputEventAttributes)) {
                return true;
            } else {
                return false;
            }
        }

        return true;

    }

    private String getStreamAttributes(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();
        final String PROPERTY_META_PREFIX = "meta_";
        final String PROPERTY_CORRELATION_PREFIX = "correlation_";
        String attributes = "";

        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                attributes += PROPERTY_META_PREFIX + attribute.getName().toString() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }
        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                attributes += PROPERTY_CORRELATION_PREFIX + attribute.getName().toString() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }
        if (payloadAttributeList != null) {
            for (Attribute attribute : payloadAttributeList) {
                attributes += attribute.getName().toString() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }

        if (!attributes.equals("")) {
            return attributes.substring(0, attributes.lastIndexOf(","));
        } else {
            return attributes;
        }
    }

    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains("{{") && text.indexOf("}}") > 0) {
            mappingTextList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return mappingTextList;
    }


    private boolean checkEventFormatterValidity(String eventFormatterName) throws AxisFault {
        try {
            EventFormatterService eventFormatterService = EventFormatterAdminServiceValueHolder.getEventFormatterService();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<EventFormatterConfiguration> eventFormatterConfigurationList = null;
            eventFormatterConfigurationList = eventFormatterService.getAllEventFormatterConfiguration(axisConfiguration);
            Iterator eventFormatterConfigurationIterator = eventFormatterConfigurationList.iterator();
            while (eventFormatterConfigurationIterator.hasNext()) {

                EventFormatterConfiguration eventFormatterConfiguration = (EventFormatterConfiguration) eventFormatterConfigurationIterator.next();
                if (eventFormatterConfiguration.getEventFormatterName().equalsIgnoreCase(eventFormatterName)) {
                    return false;
                }
            }

        } catch (EventFormatterConfigurationException e) {
            new AxisFault("Error in validating the event formatter");
        }
        return true;
    }

    private boolean validateRegistrySource(String fromData) {

        if (fromData.equalsIgnoreCase("inline")) {
            return false;
        } else {
            return true;
        }
    }


}