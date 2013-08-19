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
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventProcessorAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(EventProcessorAdminService.class);


    public void deployExecutionPlanConfiguration(ExecutionPlanConfigurationDto configurationDto) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {

            ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
            copyConfigurationsFromDto(configuration, configurationDto);

            try {
                eventProcessorService.deployExecutionPlanConfiguration(configuration, getAxisConfig());
            } catch (ExecutionPlanConfigurationException e) {
                log.error("Unable to save the execution plan.", e);
                throw new AxisFault(e.getMessage(), e);
            } catch (ExecutionPlanDependencyValidationException e) {
                log.error("Unable to save the execution plan, Invalid execution plan configuration.", e);
                throw new AxisFault(e.getMessage(), e);
            }
        }
    }


    public void undeployActiveExecutionPlanConfiguration(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.undeployActiveExecutionPlanConfiguration(name, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(),e);
                throw new AxisFault(e.getMessage());
            }
        }
    }

    public void undeployInactiveExecutionPlanConfiguration(String filePath) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.undeployInactiveExecutionPlanConfiguration(filePath, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(),e);
                throw new AxisFault(e.getMessage());
            }
        }
    }

    public void editActiveExecutionPlanConfiguration(String configuration, String name)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            eventProcessorService.editActiveExecutionPlanConfiguration(configuration, name, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to edit the configuration file.");
        }
    }

    public void editInactiveExecutionPlanConfiguration(String configuration, String path)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            eventProcessorService.editInactiveExecutionPlanConfiguration(configuration, path, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to edit the configuration file.");
        }
    }


    public String getInactiveExecutionPlanConfigurationContent(String path) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            return eventProcessorService.getInactiveExecutionPlanConfigurationContent(path, PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId());
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to read the configuration file.");
        }
    }


    public String getActiveExecutionPlanConfigurationContent(String planName) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            return eventProcessorService.getActiveExecutionPlanConfigurationContent(planName, PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId());
        } catch (ExecutionPlanConfigurationException e) {
            throw new AxisFault("Unable to read the configuration file.");
        }
    }


    public ExecutionPlanConfigurationDto[] getAllActiveExecutionPlanConfigurations() throws AxisFault {

        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.getAllActiveExecutionConfigurations(PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId());
            if (executionPlanConfigurations != null) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurations.size()];

                int i = 0;
                for (ExecutionPlanConfiguration planConfiguration : executionPlanConfigurations.values()) {
                    ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                    copyConfigurationsToDto(planConfiguration, dto);
                    configurationDtos[i] = dto;
                    i++;
                }
                return configurationDtos;
            }
        }
        return new ExecutionPlanConfigurationDto[0];
    }

    public ExecutionPlanConfigurationDto getActiveExecutionPlanConfiguration(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            ExecutionPlanConfiguration executionConfiguration = eventProcessorService.getActiveExecutionConfiguration(name, PrivilegedCarbonContext.getCurrentContext(axisConfig).getTenantId());
            ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
            copyConfigurationsToDto(executionConfiguration, dto);
            return dto;
        }
        return null;
    }

    public ExecutionPlanConfigurationFileDto[] getAllInactiveExecutionPlanConigurations() throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            List<ExecutionPlanConfigurationFile> files = eventProcessorService.getAllInactiveExecutionPlanConfiguration(tenantId);
            if (files != null) {
                ExecutionPlanConfigurationFileDto[] fileDtoArray = new ExecutionPlanConfigurationFileDto[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    ExecutionPlanConfigurationFile file = files.get(i);
                    fileDtoArray[i] = new ExecutionPlanConfigurationFileDto();
                    fileDtoArray[i].setName(file.getExecutionPlanName());
                    fileDtoArray[i].setPath(file.getFilePath());
                    if (file.getStatus() != null) {
                        fileDtoArray[i].setStatus(file.getStatus().name());
                    }
                }
                return fileDtoArray;
            }
        }
        return new ExecutionPlanConfigurationFileDto[0];
    }


    public List<String> getStreamNames() throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            return eventProcessorService.getStreamIds(tenantId);
        }
        return new ArrayList<String>();
    }

    public StreamDefinitionDto getStreamDefinition(String streamId) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            StreamDefinition streamDefinition = eventProcessorService.getStreamDefinition(streamId, tenantId);
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

//    public String[] getAllExecutionPlanFileNames() throws AxisFault {
//        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
//        if (eventProcessorService != null) {
//            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
//            List<String> files = eventProcessorService.getAllExecutionPlanConfigurationFileNames(tenantId);
//            if (files != null) {
//                String[] names = new String[files.size()];
//                for (int i = 0; i < files.size(); i++) {
//                    names[i] = files.get(i);
//                }
//                return names;
//            }
//        }
//        return new String[0];
//    }

//    public String[] getAllInactiveExecutionPlanFileNames() throws AxisFault {
//        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
////        if (eventProcessorService != null) {
////            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
////            List<ExecutionPlanConfigurationFile> files = eventProcessorService.getFailedExecutionPlanConfigurationFiles(tenantId);
////            if (files != null) {
////                String[] fileNames = new String[files.size()];
////                for (int i = 0; i < files.size(); i++) {
////                    fileNames[i] = files.get(i).getExecutionPlanName();
////                }
////                return fileNames;
////            }
////        }
//        return new String[0];
//    }

//
//    public ExecutionPlanConfigurationFileDto getExecutionPlanfile(String name) throws AxisFault {
//        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
////        if (eventProcessorService != null) {
////            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
////            ExecutionPlanConfigurationFile file = eventProcessorService.getExecutionPlanConfigurationFileByPlanName(name, tenantId);
////            if (file != null) {
////                ExecutionPlanConfigurationFileDto dto = new ExecutionPlanConfigurationFileDto();
////                dto.setName(file.getExecutionPlanName());
////                dto.setFilePath(file.getFilePath());
////                if (file.getStatus() != null) {
////                    dto.setStatus(file.getStatus().name());
////                }
////                return dto;
////            }
////        }
//        return null;
//    }

    public String getStreamDefinitionAsString(String streamId) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getTenantId();
            StreamDefinition streamDefinition = eventProcessorService.getStreamDefinition(streamId, tenantId);

            String definitionString = "(";
            boolean appendComma = false;

            if (streamDefinition.getMetaData() != null) {

                for (Attribute attribute : streamDefinition.getMetaData()) {
                    if (appendComma) {
                        definitionString = definitionString + ", ";
                    }
                    definitionString = definitionString + attribute.getName() + " " + attribute.getType().name().toLowerCase();
                    appendComma = true;
                }
            }
            if (streamDefinition.getPayloadData() != null) {

                for (Attribute attribute : streamDefinition.getPayloadData()) {
                    if (appendComma) {
                        definitionString = definitionString + ", ";
                    }
                    definitionString = definitionString + attribute.getName() + " " + attribute.getType().name().toLowerCase();
                    appendComma = true;
                }
            }
            if (streamDefinition.getCorrelationData() != null) {

                for (Attribute attribute : streamDefinition.getCorrelationData()) {
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

}
