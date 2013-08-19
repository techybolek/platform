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

package org.wso2.carbon.event.builder.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.ds.EventBuilderAdminServiceValueHolder;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConverterFactory;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConvertible;
import org.wso2.carbon.event.builder.admin.internal.util.EventBuilderAdminUtil;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.internal.config.EventBuilderConfigurationFile;
import org.wso2.carbon.event.builder.core.internal.config.DeploymentStatus;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorInfo;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import java.util.ArrayList;
import java.util.List;

public class EventBuilderAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(EventBuilderAdminService.class);
    private DtoConverterFactory dtoConverterFactory;

    public EventBuilderAdminService() {
        dtoConverterFactory = new DtoConverterFactory();
    }

    public String[] getInputTransportNames() throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        InputTransportAdaptorManagerService transportAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputTransportAdaptorManagerService();
        List<InputTransportAdaptorInfo> transportAdaptorInfoList = transportAdaptorManagerService.getInputTransportAdaptorInfo(tenantId);
        if (transportAdaptorInfoList != null && !transportAdaptorInfoList.isEmpty()) {
            String[] inputTransportNames = new String[transportAdaptorInfoList.size()];
            for (int i = 0; i < transportAdaptorInfoList.size(); i++) {
                inputTransportNames[i] = transportAdaptorInfoList.get(i).getTransportAdaptorName();
            }
            return inputTransportNames;
        }

        return new String[0];
    }

    public String[] getSupportedInputMappingTypes(String transportAdaptorName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<String> supportedInputMappingTypes = eventBuilderService.getSupportedInputMappingTypes(transportAdaptorName, tenantId);
        return supportedInputMappingTypes.toArray(new String[supportedInputMappingTypes.size()]);
    }

    public void deployEventBuilderConfiguration(
            EventBuilderConfigurationDto eventBuilderConfigurationDto)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        DtoConvertible dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfigurationDto.getInputMappingType());
        EventBuilderConfiguration eventBuilderConfiguration;
        try {
            eventBuilderConfiguration = dtoConverter.toEventBuilderConfiguration(eventBuilderConfigurationDto, tenantId);
        } catch (EventBuilderAdminServiceException e) {
            String errMsg = "Error converting DTO to corresponding object instance";
            log.error(errMsg, e);
            throw new AxisFault(errMsg, e);
        }
        if (eventBuilderConfiguration != null) {
            eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, getAxisConfig());
        }
    }

    public void undeployActiveConfiguration(String eventBuilderName) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        eventBuilderService.undeployActiveEventBuilderConfiguration(eventBuilderName, getAxisConfig());
    }

    public void undeployInactiveEventBuilderConfiguration(String filename) {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        eventBuilderService.undeployInactiveEventBuilderConfiguration(filename, getAxisConfig());
    }

    public EventBuilderConfigurationDto getActiveEventBuilderConfiguration(String eventBuilderName)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        EventBuilderConfiguration eventBuilderConfiguration = eventBuilderService.getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        DtoConvertible dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());

        try {
            return dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration);
        } catch (EventBuilderAdminServiceException e) {
            throw new AxisFault(e.getMessage());
        }
    }

    public String getActiveEventBuilderConfigurationContent(String eventBuilderName) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        return eventBuilderService.getActiveEventBuilderConfigurationContent(eventBuilderName, tenantId);
    }

    public String getInactiveEventBuilderConfigurationContent(String filePath) {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        return eventBuilderService.getInactiveEventBuilderConfigurationContent(filePath, getAxisConfig());
    }

    public void editActiveEventBuilderConfiguration(String originalEventBuilderName,
                                                    String eventBuilderConfigXml) throws AxisFault {
        if (eventBuilderConfigXml != null && !eventBuilderConfigXml.isEmpty() && originalEventBuilderName != null && !originalEventBuilderName.isEmpty()) {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            eventBuilderService.editActiveEventBuilderConfiguration(eventBuilderConfigXml, originalEventBuilderName, getAxisConfig());
        } else {
            String errMsg = "Some required parameters were null or empty. Cannot proceed with updating.";
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }
    }

    public void editInactiveEventBuilderConfiguration(String filePath, String eventBuilderConfigXml)
            throws AxisFault {
        if (eventBuilderConfigXml != null && !eventBuilderConfigXml.isEmpty() && filePath != null && !filePath.isEmpty()) {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            eventBuilderService.editInactiveEventBuilderConfiguration(eventBuilderConfigXml, filePath, getAxisConfig());
        } else {
            String errMsg = "Some required parameters were null or empty. Cannot proceed with updating.";
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }
    }

    //TODO Check usage of this method
    public EventBuilderConfigurationDto[] getEventBuilderConfigurationsFor(
            String deploymentStatusString)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        List<EventBuilderConfigurationDto> eventBuilderConfigurationDtos = new ArrayList<EventBuilderConfigurationDto>();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfiguration> eventBuilderConfigurations = eventBuilderService.getAllActiveEventBuilderConfigurations(tenantId);
        DeploymentStatus deploymentStatus = EventBuilderAdminUtil.getDeploymentStatusForString(deploymentStatusString);
        if (eventBuilderConfigurations != null && eventBuilderConfigurations.size() > 0) {
            for (EventBuilderConfiguration eventBuilderConfiguration : eventBuilderConfigurations) {
                if (deploymentStatus.equals(eventBuilderConfiguration.getDeploymentStatus())) {
                    DtoConvertible dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());
                    try {
                        eventBuilderConfigurationDtos.add(dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration));
                    } catch (EventBuilderAdminServiceException e) {
                        throw new AxisFault(e.getMessage());
                    }
                }
            }
            return eventBuilderConfigurationDtos.toArray(new EventBuilderConfigurationDto[eventBuilderConfigurationDtos.size()]);
        }

        return new EventBuilderConfigurationDto[0];
    }

    public String[] getAllInactiveEventBuilderConfigurations() {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderService.getAllInactiveEventBuilderConfigurations(getAxisConfig());
        if (eventBuilderConfigurationFileList != null) {
            String[] fileNameList = new String[eventBuilderConfigurationFileList.size()];
            int i = 0;
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileList) {
                fileNameList[i++] = EventBuilderAdminUtil.deriveConfigurationFilenameFrom(eventBuilderConfigurationFile.getFilePath());
            }
            return fileNameList;
        }
        return new String[0];
    }

    public EventBuilderConfigurationDto[] getAllActiveEventBuilderConfigurations()
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfiguration> eventBuilderConfigurationList = eventBuilderService.getAllActiveEventBuilderConfigurations(tenantId);
        if (eventBuilderConfigurationList != null && !eventBuilderConfigurationList.isEmpty()) {
            EventBuilderConfigurationDto[] eventBuilderConfigurationDtos = new EventBuilderConfigurationDto[eventBuilderConfigurationList.size()];
            for (int i = 0; i < eventBuilderConfigurationList.size(); i++) {
                EventBuilderConfiguration eventBuilderConfiguration = eventBuilderConfigurationList.get(i);
                DtoConvertible dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());
                EventBuilderConfigurationDto eventBuilderConfigurationDto;
                try {
                    eventBuilderConfigurationDto = dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration);
                } catch (EventBuilderAdminServiceException e) {
                    throw new AxisFault(e.getMessage());
                }
                eventBuilderConfigurationDtos[i] = eventBuilderConfigurationDto;
            }

            return eventBuilderConfigurationDtos;
        }

        return new EventBuilderConfigurationDto[0];
    }

    public void setTraceEnabled(String eventBuilderName, boolean traceEnabled) {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        eventBuilderService.setTraceEnabled(eventBuilderName, traceEnabled, getAxisConfig());
    }

    public void setStatisticsEnabled(String eventBuilderName, boolean statisticsEnabled) {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        eventBuilderService.setStatisticsEnabled(eventBuilderName, statisticsEnabled, getAxisConfig());
    }

    //TODO Check usage of this method
    public String[] getAllActiveEventBuilderNames() throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfiguration> eventBuilderConfigurationList = eventBuilderService.getAllActiveEventBuilderConfigurations(tenantId);
        String[] eventBuilderNames = new String[0];
        if (eventBuilderConfigurationList != null) {
            eventBuilderNames = new String[eventBuilderConfigurationList.size()];
            for (int i = 0; i < eventBuilderConfigurationList.size(); i++) {
                eventBuilderNames[i] = eventBuilderConfigurationList.get(i).getEventBuilderName();
            }
        }

        return eventBuilderNames;
    }

    public EventBuilderPropertyDto[] getMessageConfigurationProperties(
            String transportAdaptorName) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        InputTransportAdaptorService inputTransportAdaptorService = EventBuilderAdminServiceValueHolder.getInputTransportAdaptorService();
        InputTransportAdaptorManagerService inputTransportAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputTransportAdaptorManagerService();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = null;
        try {
            inputTransportAdaptorConfiguration = inputTransportAdaptorManagerService.getActiveInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            log.error("Error retrieving input transport adaptor configuration with name '" + transportAdaptorName + "'", e);
        }
        if (inputTransportAdaptorConfiguration != null) {
            MessageDto messageDto = inputTransportAdaptorService.getTransportMessageDto(inputTransportAdaptorConfiguration.getType());
            DtoConvertible dtoConverter = dtoConverterFactory.getDtoConverter(null);
            return dtoConverter.getEventBuilderPropertiesFrom(messageDto, null);
        }

        return new EventBuilderPropertyDto[0];
    }

    public EventBuilderPropertyDto[] getMessageConfigurationPropertiesWithValue(
            String eventBuilderName)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        EventBuilderConfiguration eventBuilderConfiguration = eventBuilderService.getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        String transportAdaptorName = eventBuilderConfiguration.getInputStreamConfiguration().getTransportAdaptorName();
        InputTransportAdaptorService inputTransportAdaptorService = EventBuilderAdminServiceValueHolder.getInputTransportAdaptorService();
        InputTransportAdaptorManagerService inputTransportAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputTransportAdaptorManagerService();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = null;
        try {
            inputTransportAdaptorConfiguration = inputTransportAdaptorManagerService.getActiveInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            log.error("Error retrieving input transport adaptor configuration with name '" + transportAdaptorName + "'", e);
        }
        if (inputTransportAdaptorConfiguration != null) {
            MessageDto messageDto = inputTransportAdaptorService.getTransportMessageDto(inputTransportAdaptorConfiguration.getType());
            DtoConvertible dtoConverter = dtoConverterFactory.getDtoConverter(null);
            return dtoConverter.getEventBuilderPropertiesFrom(messageDto, eventBuilderConfiguration);
        }

        return new EventBuilderPropertyDto[0];
    }
}
