package org.wso2.carbon.event.processor.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.admin.internal.ds.EventProcessorAdminValueHolder;
import org.wso2.carbon.event.processor.admin.internal.util.EventProcessorAdminUtil;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfigurationFile;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventProcessorAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(EventProcessorAdminService.class);

    public ExecutionPlanConfigurationDto[] getAllExecutionPlans() throws AxisFault {

        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap = eventProcessorService.getAllExecutionPlanConfigurations(PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId());
            if (executionPlanConfigurationMap != null) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurationMap.size()];

                int i = 0;
                for (ExecutionPlanConfiguration config : executionPlanConfigurationMap.values()) {
                    ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                    copyConfigurationsToDto(config, dto);
                    configurationDtos[i] = dto;
                    i++;
                }
                return configurationDtos;
            }
        }
        return new ExecutionPlanConfigurationDto[0];
    }

    public ExecutionPlanConfigurationDto getExecutionPlan(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            ExecutionPlanConfiguration configuration = eventProcessorService.getExecutionPlanConfiguration(name, PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId());
            ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
            copyConfigurationsToDto(configuration, dto);
            return dto;
        }
        return null;
    }

    public void removeExecutionPlanConfiguration(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            ExecutionPlanConfiguration config = eventProcessorService.getExecutionPlanConfiguration(name, tenantId);
            eventProcessorService.removeExecutionPlanConfiguration(config, tenantId);
        }
    }


    public ExecutionPlanConfigurationFileDto getExecutionPlanConfigurationFile(String name)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            ExecutionPlanConfigurationFile configurationFile = eventProcessorService.getExecutionPlanConfigurationFileByName(name, tenantId);
            ExecutionPlanConfigurationFileDto dto = new ExecutionPlanConfigurationFileDto();
            dto.setName(name);
            dto.setPath(configurationFile.getPath());
            dto.setStatus(configurationFile.getStatus().toString());
            dto.setDeploymentStatusMessage(configurationFile.getDeploymentStatusMessage());
            return dto;
        }
        return null;
    }

    public String readExecutionPlanConfigurationFile(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            return eventProcessorService.getExecutionPlanConfigurationFileContent(PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId(),
                                                                                  name);
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to read the configuration file.");
        }
    }

    public String readNotDeployedExecutionPlanConfigurationFile(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            return eventProcessorService.getNotDeployedExecutionPlanConfigurationFileContent(PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId(),
                                                                                             name);
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to read the configuration file.");
        }
    }


    public void editExecutionPlanConfigurationFile(String configuration, String name)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            eventProcessorService.editExecutionPlanConfigurationFile(configuration, name, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to edit the configuration file.");
        }
    }

    public void editNotDeployedExecutionPlanConfigurationFile(String configuration, String path)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            eventProcessorService.editNotDeployedExecutionPlanConfigurationFile(configuration, path, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to edit the configuration file.");
        }
    }


    public void removeExecutionPlanConfigurationFile(String path) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            ExecutionPlanConfigurationFile configurationFile = eventProcessorService.getExecutionPlanConfigurationFile(path, tenantId);
            eventProcessorService.removeExecutionPlanConfigurationFile(configurationFile, tenantId);
        }
    }


    public List<String> getStreamNames() throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            return eventProcessorService.getStreamNames(tenantId);

        }
        return new ArrayList<String>();
    }

    public StreamDefinitionDto getStreamDefinition(String streamId) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            StreamDefinition streamDefinition = eventProcessorService.getStreamDefinition(tenantId, streamId);
            StreamDefinitionDto dto = new StreamDefinitionDto();
            dto.setName(streamId);
            ArrayList<String> attributeList = new ArrayList<String>();
            if (streamDefinition.getPayloadData() != null) {
                for (Attribute attribute : streamDefinition.getPayloadData()) {
                    String definition = attribute.getName() + " " + attribute.getType().name().toLowerCase();
                    attributeList.add(definition);
                }
            }
            dto.setAttributeList(attributeList);
            return dto;
        }
        return null;
    }


    public void addExecutionPlan(ExecutionPlanConfigurationDto configurationDto) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {

            ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
            copyConfigurationsFromDto(configuration, configurationDto);

            try {
                eventProcessorService.saveExecutionPlanConfiguration(configuration, getAxisConfig());
            } catch (ExecutionPlanConfigurationException e) {
                log.error("Unable to save the execution plan.", e);
                throw new AxisFault(e.getMessage(), e);
            }
        }
    }

    public String[] getAllExecutionPlanFileNames() throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            List<String> files = eventProcessorService.getAllExecutionPlanConfigurationFileNames(tenantId);
            if (files != null) {
                String[] names = new String[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    names[i] = files.get(i);
                }
                return names;
            }
        }
        return new String[0];
    }

    public String[] getFailedExecutionPlanFileNames() throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            List<ExecutionPlanConfigurationFile> files = eventProcessorService.getFailedExecutionPlanConfigurationFiles(tenantId);
            if (files != null) {
                String[] fileNames = new String[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    fileNames[i] = files.get(i).getExecutionPlanName();
                }
                return fileNames;
            }
        }
        return new String[0];
    }

    public ExecutionPlanConfigurationFileDto[] getFailedExecutionPlanFiles() throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            List<ExecutionPlanConfigurationFile> files = eventProcessorService.getFailedExecutionPlanConfigurationFiles(tenantId);
            if (files != null) {
                ExecutionPlanConfigurationFileDto[] fileDtoArray = new ExecutionPlanConfigurationFileDto[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    ExecutionPlanConfigurationFile file = files.get(i);
                    fileDtoArray[i] = new ExecutionPlanConfigurationFileDto();
                    fileDtoArray[i].setName(file.getExecutionPlanName());
                    fileDtoArray[i].setPath(file.getPath());
                    if (file.getStatus() != null) {
                        fileDtoArray[i].setStatus(file.getStatus().name());
                    }
                }
                return fileDtoArray;
            }
        }
        return new ExecutionPlanConfigurationFileDto[0];
    }


    public ExecutionPlanConfigurationFileDto getExecutionPlanfile(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            ExecutionPlanConfigurationFile file = eventProcessorService.getExecutionPlanConfigurationFileByName(name, tenantId);
            if (file != null) {
                ExecutionPlanConfigurationFileDto dto = new ExecutionPlanConfigurationFileDto();
                dto.setName(file.getExecutionPlanName());
                dto.setPath(file.getPath());
                if (file.getStatus() != null) {
                    dto.setStatus(file.getStatus().name());
                }
                return dto;
            }
        }
        return null;
    }

    public String getStreamDefinitionAsString(String streamId) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            StreamDefinition streamDefinition = eventProcessorService.getStreamDefinition(tenantId, streamId);

            String definitionString = "(";
            if (streamDefinition.getPayloadData() != null) {
                boolean appendComma = false;
                for (Attribute attribute : streamDefinition.getPayloadData()) {
                    if (appendComma) {
                        definitionString = definitionString + ", ";
                    }
                    definitionString = definitionString + attribute.getName() + " " + attribute.getType().name().toLowerCase();
                    appendComma = true;
                }
            }
            definitionString = definitionString + ")";
            return definitionString;
        }
        return null;
    }

    private void copyConfigurationsFromDto(ExecutionPlanConfiguration config,
                                           ExecutionPlanConfigurationDto dto) {
        config.setName(dto.getName());
        config.setDescription(dto.getDescription());
        config.setQueryExpressions(dto.getQueryExpressions());
        config.setStatisticsEnabled(dto.isStatisticsEnabled());
        config.setTracingEnabled(dto.isTracingEnabled());
        if (dto.getSiddhiConfigurations() != null) {
            for (SiddhiConfigurationDto siddhiConfig : dto.getSiddhiConfigurations()) {
                config.addSiddhiConfigurationProperty(siddhiConfig.getKey(), siddhiConfig.getValue());
            }
        }

        if (dto.getImportedStreams() != null) {
            for (StreamConfigurationDto streamConfigurationDto : dto.getImportedStreams()) {
                StreamConfiguration streamConfig = new StreamConfiguration(EventProcessorAdminUtil.getStreamName(streamConfigurationDto.getStreamId()), streamConfigurationDto.getSiddhiStreamName(), EventProcessorAdminUtil.getVersion(streamConfigurationDto.getStreamId()));

                config.addImportedStream(streamConfig);
            }
        }

        if (dto.getExportedStreams() != null) {
            for (StreamConfigurationDto streamConfigurationDto : dto.getExportedStreams()) {
                StreamConfiguration streamConfig = new StreamConfiguration(EventProcessorAdminUtil.getStreamName(streamConfigurationDto.getStreamId()), streamConfigurationDto.getSiddhiStreamName(), EventProcessorAdminUtil.getVersion(streamConfigurationDto.getStreamId()));
                config.addExportedStream(streamConfig);
            }
        }
    }

    private void copyConfigurationsToDto(ExecutionPlanConfiguration config,
                                         ExecutionPlanConfigurationDto dto) {
        dto.setName(config.getName());
        dto.setDescription(config.getDescription());
        dto.setQueryExpressions(config.getQueryExpressions());
        dto.setStatisticsEnabled(config.isStatisticsEnabled());
        dto.setTracingEnabled(config.isTracingEnabled());
        if (config.getSiddhiConfigurationProperties() != null) {
            SiddhiConfigurationDto[] siddhiConfigs = new SiddhiConfigurationDto[config.getSiddhiConfigurationProperties().size()];
            int i = 0;
            for (Map.Entry<String, String> configEntry : config.getSiddhiConfigurationProperties().entrySet()) {
                SiddhiConfigurationDto siddhiConfigurationDto = new SiddhiConfigurationDto();
                siddhiConfigurationDto.setKey(configEntry.getKey());
                siddhiConfigurationDto.setValue(configEntry.getValue());
                siddhiConfigs[i] = siddhiConfigurationDto;
                i++;
            }
            dto.setSiddhiConfigurations(siddhiConfigs);
        }

        if (config.getImportedStreams() != null) {
            StreamConfigurationDto[] importedStreamDtos = new StreamConfigurationDto[config.getImportedStreams().size()];
            for (int i = 0; i < config.getImportedStreams().size(); i++) {
                StreamConfiguration streamConfiguration = config.getImportedStreams().get(i);
                StreamConfigurationDto streamDto = new StreamConfigurationDto(streamConfiguration.getStreamId(), streamConfiguration.getSiddhiStreamName());
                importedStreamDtos[i] = streamDto;
            }
            dto.setImportedStreams(importedStreamDtos);
        }

        if (config.getExportedStreams() != null) {
            StreamConfigurationDto[] exportedStreamDtos = new StreamConfigurationDto[config.getExportedStreams().size()];
            for (int i = 0; i < config.getExportedStreams().size(); i++) {
                StreamConfiguration streamConfiguration = config.getExportedStreams().get(i);
                StreamConfigurationDto streamDto = new StreamConfigurationDto(streamConfiguration.getStreamId(), streamConfiguration.getSiddhiStreamName());
                exportedStreamDtos[i] = streamDto;
            }
            dto.setExportedStreams(exportedStreamDtos);
        }
    }

    public void setTracingEnabled(String executionPlanName, boolean isEnabled) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.setTracingEnabled(executionPlanName, isEnabled, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault("Event processor is not loaded.");
        }
    }

    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.setStatisticsEnabled(executionPlanName, isEnabled, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault("Event processor is not loaded.");
        }
    }
}
