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

package org.wso2.carbon.event.builder.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.EventBuilderNotificationListener;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.EventListener;
import org.wso2.carbon.event.builder.core.config.EventBuilder;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderValidationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigurationFile;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.ConfigurationValidator;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorDto;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;
import org.wso2.carbon.input.transport.adaptor.manager.core.exception.InputTransportAdaptorManagerConfigurationException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventBuilderService implements EventBuilderService {

    private static final Log log = LogFactory.getLog(CarbonEventBuilderService.class);
    private Map<Integer, Map<String, EventBuilder>> tenantSpecificEventBuilderMap;
    private Map<Integer, Map<String, EventBuilderConfigurationFile>> tenantSpecificEventBuilderConfigFileMap;
    private Map<Integer, Map<String, StreamDefinition>> tenantSpecificStreamMap;
    private EventBuilderNotificationListener eventBuilderNotificationListener;

    public CarbonEventBuilderService() {
        tenantSpecificEventBuilderMap = new ConcurrentHashMap<Integer, Map<String, EventBuilder>>();
        tenantSpecificEventBuilderConfigFileMap = new ConcurrentHashMap<Integer, Map<String, EventBuilderConfigurationFile>>();
        tenantSpecificStreamMap = new ConcurrentHashMap<Integer, Map<String, StreamDefinition>>();
    }

    public EventBuilderNotificationListener getEventBuilderNotificationListener() {
        return eventBuilderNotificationListener;
    }

    public void registerEventBuilderNotificationListener(
            EventBuilderNotificationListener eventBuilderNotificationListener) {
        this.eventBuilderNotificationListener = eventBuilderNotificationListener;
        notifyEventBuilderNotificationListeners();
    }

    @Override
    public void subscribe(StreamDefinition streamDefinition, EventListener eventListener,
                          int tenantId)
            throws EventBuilderConfigurationException {
        Map<String, EventBuilder> eventBuilderMap = tenantSpecificEventBuilderMap.get(tenantId);
        EventBuilder eventBuilder = eventBuilderMap.get(streamDefinition.getStreamId());
        if (eventBuilder == null) {
            throw new EventBuilderConfigurationException("No event builder exists for the stream definition '" + streamDefinition.getStreamId() + "' provided for this tenant");
        }
        eventBuilder.subscribe(eventListener);
    }

    @Override
    public void unsubsribe(StreamDefinition streamDefinition, EventListener eventListener,
                           int tenantId) {
        if (tenantSpecificEventBuilderMap.get(tenantId) != null) {
            Map<String, EventBuilder> eventBuilderMap = tenantSpecificEventBuilderMap.get(tenantId);
            if (eventBuilderMap.get(streamDefinition.getStreamId()) != null) {
                eventBuilderMap.get(streamDefinition.getStreamId()).unsubscribe(eventListener);
            }
        }
    }

    @Override
    public void addEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, EventBuilder> eventBuilderMap
                = tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderMap == null) {
            eventBuilderMap = new ConcurrentHashMap<String, EventBuilder>();
            tenantSpecificEventBuilderMap.put(tenantId, eventBuilderMap);
        }
        Map<String, StreamDefinition> streamDefinitionMap = tenantSpecificStreamMap.get(tenantId);
        if (streamDefinitionMap == null) {
            streamDefinitionMap = new ConcurrentHashMap<String, StreamDefinition>();
            tenantSpecificStreamMap.put(tenantId, streamDefinitionMap);
        }

        String eventBuilderName = eventBuilderConfiguration.getEventBuilderName();
        StreamDefinition toStreamDefinition;
        EventBuilder eventBuilder;
        try {
            toStreamDefinition = new StreamDefinition(eventBuilderConfiguration.getToStreamName(), eventBuilderConfiguration.getToStreamVersion());
            eventBuilder = eventBuilderMap.get(toStreamDefinition.getStreamId());
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException(e);
        }

        if (eventBuilder == null) {
            eventBuilder = eventBuilderConfiguration.getEventBuilderFactory().constructEventBuilder(axisConfiguration, eventBuilderConfiguration);
        } else if (!eventBuilder.getEventBuilderConfiguration().getEventBuilderName().equals(eventBuilderConfiguration.getEventBuilderName())) {
            throw new EventBuilderConfigurationException("A different event builder already exists with the same 'To Stream'");
        } else {
            eventBuilder.subscribeToTransportAdaptor();
        }
        eventBuilderMap.put(toStreamDefinition.getStreamId(), eventBuilder);
        streamDefinitionMap.put(eventBuilder.getExportedStreamDefinition().getStreamId(), eventBuilder.getExportedStreamDefinition());

        String streamNameWithVersion;
        try {
            streamNameWithVersion = new StreamDefinition(eventBuilderConfiguration.getToStreamName(), eventBuilderConfiguration.getToStreamVersion()).getStreamId();
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException("Could not create stream definition with stream name " + eventBuilderConfiguration.getToStreamName()
                                                         + " and version " + eventBuilderConfiguration.getToStreamVersion(), e);
        }

        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFileMap == null) {
            eventBuilderConfigurationFileMap = new ConcurrentHashMap<String, EventBuilderConfigurationFile>();
            this.tenantSpecificEventBuilderConfigFileMap.put(tenantId, eventBuilderConfigurationFileMap);
        }
        if (eventBuilderConfigurationFileMap.get(eventBuilderName) == null) {
            String inputMappingType = eventBuilderConfiguration.getInputMapping().getMappingType();
            OMElement ebConfigElement = EventBuilderConfigHelper.getEventBuilderConfigBuilder(inputMappingType).eventBuilderConfigurationToOM(eventBuilderConfiguration);
            EventBuilderConfigurationFile eventBuilderConfigurationFile = createEventBuilderConfigurationFile(eventBuilderConfiguration.getEventBuilderName(),
                                                                                                              generateFilePath(eventBuilderConfiguration, axisConfiguration), eventBuilderConfiguration.getDeploymentStatus(), axisConfiguration, null, null, streamNameWithVersion, ebConfigElement);
            eventBuilderConfigurationFileMap.put(eventBuilderName, eventBuilderConfigurationFile);
            EventBuilderConfigHelper.saveConfigurationToFileSystem(eventBuilderConfiguration, eventBuilderConfigurationFile.getFilePath());
        }
    }

    @Override
    public void removeEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                   int tenantId)
            throws EventBuilderConfigurationException {
        EventBuilder removedEventBuilder;
        Map<String, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderMap != null) {
            String exportedStreamDefinitionId = eventBuilderConfiguration.getToStreamName() + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + eventBuilderConfiguration.getToStreamVersion();
            removedEventBuilder = eventBuilderMap.remove(exportedStreamDefinitionId);
            if (removedEventBuilder == null) {
                throw new EventBuilderConfigurationException("Could not find the specified event builder '" + eventBuilderConfiguration.getEventBuilderName() + "' for removal for the given axis configuration");
            }
        }
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        EventBuilderConfigurationFile eventBuilderConfigurationFile = getEventBuilderConfigurationFile(eventBuilderConfiguration.getEventBuilderName(), tenantId);
        if (eventBuilderConfigurationFileMap.remove(eventBuilderConfiguration.getEventBuilderName()) != null) {
            log.info("Event builder '" + eventBuilderConfiguration.getEventBuilderName() + "' undeployed.");
            EventBuilderConfigHelper.deleteEventBuilderConfigurationFile(eventBuilderConfigurationFile);
        } else {
            throw new EventBuilderConfigurationException("Could not find the event builder configuration file for event builder '" + eventBuilderConfiguration.getEventBuilderName() + "'");
        }
    }

    @Override
    public List<EventBuilderConfiguration> getAllEventBuilderConfigurations(
            AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<EventBuilderConfiguration> eventBuilderConfigurations = new ArrayList<EventBuilderConfiguration>();
        Map<String, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderMap != null) {
            for (EventBuilder eventBuilder : eventBuilderMap.values()) {
                eventBuilderConfigurations.add(eventBuilder.getEventBuilderConfiguration());
            }
        }

        return eventBuilderConfigurations;
    }

    @Override
    public EventBuilderConfiguration getEventBuilderConfiguration(String streamId,
                                                                  AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventBuilderConfiguration eventBuilderConfiguration = null;
        Map<String, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderMap != null) {
            for (String streamDefinitionId : eventBuilderMap.keySet()) {
                if (streamDefinitionId.equals(streamId)) {
                    EventBuilder eventBuilder = eventBuilderMap.get(streamId);
                    if (eventBuilder != null) {
                        eventBuilderConfiguration = eventBuilder.getEventBuilderConfiguration();
                    }
                }
            }
        }

        return eventBuilderConfiguration;
    }

    @Override
    public EventBuilderConfiguration getEventBuilderConfigurationFromName(String eventBuilderName,
                                                                          AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventBuilderConfiguration eventBuilderConfiguration = null;
        Map<String, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderMap != null) {
            for (EventBuilder eventBuilder : eventBuilderMap.values()) {
                if (eventBuilder.getEventBuilderConfiguration().getEventBuilderName().equals(eventBuilderName)) {
                    eventBuilderConfiguration = eventBuilder.getEventBuilderConfiguration();
                    break;
                }
            }
        }

        return eventBuilderConfiguration;
    }

    @Override
    public List<String> getStreamDefinitionsAsString(int tenantId) {
        List<StreamDefinition> streamDefinitions = getStreamDefinitions(tenantId);
        List<String> streamDefinitionsAsString = null;
        if (streamDefinitions != null && !streamDefinitions.isEmpty()) {
            streamDefinitionsAsString = new ArrayList<String>();
            for (StreamDefinition streamDefinition : streamDefinitions) {
                streamDefinitionsAsString.add(streamDefinition.getStreamId());
            }
        }

        return streamDefinitionsAsString;
    }

    @Override
    public List<String> getSupportedInputMappingTypes(String transportAdaptorName, int tenantId) {
        List<String> supportedInputMappingTypes = new ArrayList<String>();
        InputTransportAdaptorService inputTransportAdaptorService = EventBuilderServiceValueHolder.getInputTransportAdaptorService();
        InputTransportAdaptorManagerService inputTransportAdaptorManagerService = EventBuilderServiceValueHolder.getInputTransportAdaptorManagerService();
        String transportAdaptorType = null;
        try {
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = inputTransportAdaptorManagerService.getInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
            transportAdaptorType = inputTransportAdaptorConfiguration.getType();
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            log.error("Error while trying to retrieve supported input mapping types.", e);
        }
        if (transportAdaptorType != null) {
            InputTransportAdaptorDto inputTransportAdaptorDto = inputTransportAdaptorService.getTransportAdaptorDto(transportAdaptorType);
            if (inputTransportAdaptorDto != null) {
                for (InputTransportAdaptorDto.MessageType messageType : inputTransportAdaptorDto.getSupportedMessageTypes()) {
                    supportedInputMappingTypes.add(EventBuilderConstants.MESSAGE_TYPE_STRING_MAP.get(messageType));
                }
            }
        }
        return supportedInputMappingTypes;
    }

    @Override
    public List<StreamDefinition> getStreamDefinitions(int tenantId) {
        List<StreamDefinition> streamDefinitions = null;
        Map<String, StreamDefinition> streamDefinitionMap = this.tenantSpecificStreamMap.get(tenantId);
        if (streamDefinitionMap != null && !streamDefinitionMap.isEmpty()) {
            streamDefinitions = new ArrayList<StreamDefinition>(streamDefinitionMap.values());
        }

        return streamDefinitions;
    }

    @Override
    public void removeEventBuilderConfigurationFile(String eventBuilderName, int tenantId) {
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFileMap != null) {
            EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.remove(eventBuilderName);
            if (eventBuilderConfigurationFile != null) {
                EventBuilderConfigHelper.deleteEventBuilderConfigurationFile(eventBuilderConfigurationFile);
            } else {
                String errMsg = "Cannot find the event builder configuration '" + eventBuilderName + "' for this tenant.";
                log.error(errMsg);
                throw new EventBuilderConfigurationException(errMsg);
            }
        }
    }

    @Override
    public List<EventBuilderConfigurationFile> getUndeployedFiles(
            AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFileMap != null) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = new ArrayList<EventBuilderConfigurationFile>();
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileMap.values()) {
                if (eventBuilderConfigurationFile.getDeploymentStatus() != DeploymentStatus.DEPLOYED) {
                    eventBuilderConfigurationFileList.add(eventBuilderConfigurationFile);
                }
            }
            return eventBuilderConfigurationFileList;
        }

        return null;
    }

    @Override
    public String getEventBuilderConfigurationXml(String eventBuilderName, int tenantId) {
        EventBuilderConfigurationFile eventBuilderConfigurationFile = getEventBuilderConfigurationFile(eventBuilderName, tenantId);
        if (eventBuilderConfigurationFile != null && eventBuilderConfigurationFile.getEbConfigOmElement() != null) {
            return eventBuilderConfigurationFile.getEbConfigOmElement().toString();
        }
        return null;
    }

    @Override
    public void updateEventBuilder(String originalEventBuilderName,
                                   String eventBuilderConfigXml,
                                   AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventBuilderConfigXml);
            ConfigurationValidator.validateEventBuilderConfiguration(omElement);
            String mappingType = EventBuilderConfigHelper.getInputMappingType(omElement);
            if (mappingType != null) {
                EventBuilderConfiguration updatedEventBuilderConfiguration = EventBuilderConfigHelper.getEventBuilderConfigBuilder(mappingType).getEventBuilderConfiguration(omElement, tenantId, mappingType);
                updateEventBuilder(originalEventBuilderName, updatedEventBuilderConfiguration, axisConfiguration);
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new EventBuilderConfigurationException("Not a valid xml object, ", e);
        }
    }

    @Override
    public void updateEventBuilder(String originalEventBuilderName,
                                   EventBuilderConfiguration updatedEventBuilderConfiguration,
                                   AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        String mappingType = updatedEventBuilderConfiguration.getInputMapping().getMappingType();
        if (mappingType != null) {
            try {
                String updatedEbName = updatedEventBuilderConfiguration.getEventBuilderName();
                if (!updatedEbName.equals(originalEventBuilderName)) {
                    EventBuilderConfiguration originalEventBuilder = getEventBuilderConfigurationFromName(originalEventBuilderName, axisConfiguration);
                    removeEventBuilder(originalEventBuilder, tenantId);
                    addEventBuilder(updatedEventBuilderConfiguration, axisConfiguration);
                } else {
                    int tenantId1 = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
                    Map<String, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId1);
                    if (eventBuilderMap != null) {
                        String toStreamId = updatedEventBuilderConfiguration.getToStreamName() + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + updatedEventBuilderConfiguration.getToStreamVersion();
                        EventBuilder eventBuilderToUpdate = eventBuilderMap.get(toStreamId);
                        if (eventBuilderToUpdate != null) {
                            eventBuilderToUpdate.loadEventBuilderConfiguration();
                        }
                    }
                    Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
                    EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(updatedEbName);
                    if (eventBuilderConfigurationFile != null) {
                        OMElement ebConfigOmElement = EventBuilderConfigHelper.getEventBuilderConfigBuilder(mappingType).eventBuilderConfigurationToOM(updatedEventBuilderConfiguration);
                        EventBuilderConfigurationFile updatedEbConfigFile = createEventBuilderConfigurationFile(updatedEventBuilderConfiguration, ebConfigOmElement, "Event builder configuration updated.", axisConfiguration);
                        eventBuilderConfigurationFileMap.put(updatedEbName, updatedEbConfigFile);
                        EventBuilderConfigHelper.saveConfigurationToFileSystem(updatedEventBuilderConfiguration, updatedEbConfigFile.getFilePath());
                    } else {
                        throw new EventBuilderConfigurationException("Cannot find associated event builder configuration file for '" + updatedEbName + "'.");
                    }
                }
            } catch (EventBuilderValidationException e) {
                String msg = "Event builder configuration for '" + originalEventBuilderName + ".xml' is invalid";
                log.error(msg, e);
                throw new EventBuilderConfigurationException(msg, e);
            }
        }
    }

    public void addEventBuilderConfigurationFile(String eventBuilderName,
                                                 String filePath,
                                                 DeploymentStatus status,
                                                 String deploymentStatusMessage, String dependency,
                                                 String streamNameWithVersion,
                                                 OMElement ebConfigElement,
                                                 AxisConfiguration axisConfiguration) {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);

        if (eventBuilderConfigurationFileMap == null) {
            eventBuilderConfigurationFileMap = new ConcurrentHashMap<String, EventBuilderConfigurationFile>();
            tenantSpecificEventBuilderConfigFileMap.put(tenantId, eventBuilderConfigurationFileMap);
        }
        if (eventBuilderConfigurationFileMap.get(eventBuilderName) == null) {
            EventBuilderConfigurationFile eventBuilderConfigurationFile = createEventBuilderConfigurationFile(eventBuilderName, filePath, status, axisConfiguration, deploymentStatusMessage, dependency, streamNameWithVersion, ebConfigElement);
            eventBuilderConfigurationFileMap.put(eventBuilderName, eventBuilderConfigurationFile);
        } else {
            log.error("Event builder file configuration for the given event builder '" + eventBuilderName + "' already exists.");
        }
    }

    private String generateFilePath(EventBuilderConfiguration eventBuilderConfiguration,
                                    AxisConfiguration axisConfiguration) {
        String eventBuilderName = eventBuilderConfiguration.getEventBuilderName();
        File repoDir = new File(axisConfiguration.getRepository().getPath());
        if (!repoDir.exists()) {
            if (repoDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory to add tenant specific event builder :" + eventBuilderName);
            }
        }
        File subDir = new File(repoDir.getAbsolutePath() + File.separator + EventBuilderConstants.EB_CONFIG_DIRECTORY);
        if (!subDir.exists()) {
            if (!subDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory " + EventBuilderConstants.EB_CONFIG_DIRECTORY + " to add tenant specific transport adaptor :" + eventBuilderName);
            }
        }
        return subDir.getAbsolutePath() + File.separator + eventBuilderName + ".xml";
    }

    private EventBuilderConfigurationFile createEventBuilderConfigurationFile(
            String eventBuilderName, String filePath, DeploymentStatus status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage, String dependency, String streamNameWithVersion,
            OMElement ebConfigElement) {
        EventBuilderConfigurationFile eventBuilderConfigurationFile = new EventBuilderConfigurationFile();
        eventBuilderConfigurationFile.setFilePath(filePath);
        eventBuilderConfigurationFile.setEventBuilderName(eventBuilderName);
        eventBuilderConfigurationFile.setDeploymentStatus(status);
        eventBuilderConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventBuilderConfigurationFile.setDependency(dependency);
        eventBuilderConfigurationFile.setAxisConfiguration(axisConfiguration);
        eventBuilderConfigurationFile.setStreamWithVersion(streamNameWithVersion);
        eventBuilderConfigurationFile.setEbConfigOmElement(ebConfigElement);
        return eventBuilderConfigurationFile;
    }

    private EventBuilderConfigurationFile createEventBuilderConfigurationFile(
            EventBuilderConfiguration eventBuilderConfiguration, OMElement ebConfigElement,
            String deploymentStatusMessage, AxisConfiguration axisConfiguration) {
        String streamNameWithVersion = eventBuilderConfiguration.getToStreamName() + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + eventBuilderConfiguration.getToStreamVersion();
        return createEventBuilderConfigurationFile(eventBuilderConfiguration.getEventBuilderName(), generateFilePath(eventBuilderConfiguration, axisConfiguration),
                                                   null, axisConfiguration, deploymentStatusMessage, null, streamNameWithVersion, ebConfigElement);
    }

    public void removeEventBuilderWithFilePath(String filePath, int tenantId) {
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);

        for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileMap.values()) {
            if ((eventBuilderConfigurationFile.getFilePath().equals(filePath))) {
                if (DeploymentStatus.DEPLOYED.equals(eventBuilderConfigurationFile.getDeploymentStatus())) {
                    tenantSpecificEventBuilderMap.get(tenantId).remove(eventBuilderConfigurationFile.getStreamWithVersion());
                    eventBuilderNotificationListener.configurationRemoved(tenantId, eventBuilderConfigurationFile.getStreamWithVersion());
                }
                eventBuilderConfigurationFileMap.remove(eventBuilderConfigurationFile.getEventBuilderName());
                log.info("Removed event builder configuration file for event builder '" + eventBuilderConfigurationFile.getEventBuilderName() + "'");
                break;
            }
        }
    }

    public boolean isEventBuilderConfigurationFileRegistered(int tenantId,
                                                             String eventBuilderName) {
        if (tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            if (eventBuilderConfigurationFileMap != null && eventBuilderConfigurationFileMap.get(eventBuilderName) != null) {
                return true;
            }
        }
        return false;
    }

    public EventBuilderConfigurationFile getEventBuilderConfigurationFile(String eventBuilderName,
                                                                          int tenantId) {
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        EventBuilderConfigurationFile eventBuilderConfigurationFile = null;
        if (eventBuilderConfigurationFileMap != null) {
            eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(eventBuilderName);
        }

        return eventBuilderConfigurationFile;
    }

    @Deprecated
    public void redeployEventBuilderConfigurationFiles(int tenantId)
            throws EventBuilderConfigurationException {

        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();

        if (tenantSpecificEventBuilderConfigFileMap != null && tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);

            if (eventBuilderConfigurationFileMap != null) {
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileMap.values()) {
                    if ((eventBuilderConfigurationFile.getDeploymentStatus().equals(DeploymentStatus.WAITING_FOR_DEPENDENCY))) {
                        fileList.add(eventBuilderConfigurationFile);
                    }
                }

                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : fileList) {
                    eventBuilderConfigurationFileMap.remove(eventBuilderConfigurationFile.getEventBuilderName());
                }

                if (eventBuilderConfigurationFileMap.size() == 0) {
                    tenantSpecificEventBuilderConfigFileMap.remove(tenantId);
                }
            }
        }

        if (fileList.size() > 0) {
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : fileList) {
                EventBuilderUtil.executeDeployment(eventBuilderConfigurationFile.getFilePath(), eventBuilderConfigurationFile.getAxisConfiguration());
            }
        }

    }

    public void notifyEventBuilderNotificationListeners()
            throws EventBuilderConfigurationException {
        //TODO This code snippet iterates through all tenants and dispatches notifications to listeners of all deployed event builders?
        if (tenantSpecificEventBuilderConfigFileMap != null && tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            for (Map.Entry<Integer, Map<String, EventBuilderConfigurationFile>> tenantEbConfigFileMap : tenantSpecificEventBuilderConfigFileMap.entrySet()) {
                Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantEbConfigFileMap.getValue();
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileMap.values()) {
                    if (eventBuilderConfigurationFile.getDeploymentStatus().equals(DeploymentStatus.DEPLOYED)) {
                        int tenantId = PrivilegedCarbonContext.getCurrentContext(eventBuilderConfigurationFile.getAxisConfiguration()).getTenantId();
                        this.eventBuilderNotificationListener.configurationAdded(tenantId, eventBuilderConfigurationFile.getStreamWithVersion());
                    }
                }
            }
        }

    }

    public void redeployEventBuilderConfigurationFile(int tenantId, String transportAdaptorName)
            throws EventBuilderConfigurationException {

        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();

        if (tenantSpecificEventBuilderConfigFileMap != null && tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);

            if (eventBuilderConfigurationFileMap != null) {
                Iterator<EventBuilderConfigurationFile> eventBuilderConfigurationFileIterator = eventBuilderConfigurationFileMap.values().iterator();
                while (eventBuilderConfigurationFileIterator.hasNext()) {
                    EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileIterator.next();
                    if ((eventBuilderConfigurationFile.getDeploymentStatus().equals(DeploymentStatus.WAITING_FOR_DEPENDENCY)) && eventBuilderConfigurationFile.getDependency().equals(transportAdaptorName)) {
                        fileList.add(eventBuilderConfigurationFile);
                        eventBuilderConfigurationFileIterator.remove();
                    }
                }
            }
        }

        for (EventBuilderConfigurationFile eventBuilderConfigurationFile : fileList) {
            try {
                EventBuilderUtil.executeDeployment(eventBuilderConfigurationFile.getFilePath(), eventBuilderConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                String filePath = eventBuilderConfigurationFile.getFilePath();
                log.error("Exception occurred while trying to deploy the event builder configuration file '" + filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length()) + "'", e);
            }

        }

    }

    public void undeployEventBuilderConfigurationFile(int tenantId,
                                                      InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration)
            throws EventBuilderConfigurationException {

        List<String> fileList = new ArrayList<String>();
        String transportAdaptorName = inputTransportAdaptorConfiguration.getName();
        if (tenantSpecificEventBuilderMap != null && tenantSpecificEventBuilderMap.size() > 0) {
            Map<String, EventBuilder> eventBuilderMap = tenantSpecificEventBuilderMap.get(tenantId);
            Iterator<EventBuilder> eventBuilderIterator = eventBuilderMap.values().iterator();
            while (eventBuilderIterator.hasNext()) {
                EventBuilder eventBuilder = eventBuilderIterator.next();
                if (eventBuilder.getEventBuilderConfiguration().getInputStreamConfiguration().getTransportAdaptorName().equals(transportAdaptorName)) {
                    eventBuilder.unsubscribeFromTransportAdaptor(inputTransportAdaptorConfiguration);
                    fileList.add(eventBuilder.getEventBuilderConfiguration().getEventBuilderName());
                    eventBuilderIterator.remove();
                }
            }
        }

        for (String aFileList : fileList) {
            Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            if (eventBuilderConfigurationFileMap != null) {
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileMap.values()) {
                    if ((eventBuilderConfigurationFile.getDeploymentStatus().equals(DeploymentStatus.DEPLOYED)) && eventBuilderConfigurationFile.getEventBuilderName().equalsIgnoreCase(aFileList)) {
                        eventBuilderConfigurationFile.setDeploymentStatus(DeploymentStatus.WAITING_FOR_DEPENDENCY);
                        eventBuilderConfigurationFile.setDependency(transportAdaptorName);
                        eventBuilderConfigurationFile.setDeploymentStatusMessage("Transport adaptor configuration not found.");
                        eventBuilderNotificationListener.configurationRemoved(tenantId, eventBuilderConfigurationFile.getStreamWithVersion());
                        log.info("Event builder '" + eventBuilderConfigurationFile.getEventBuilderName() + "' undeployed because dependency '" + transportAdaptorName + "' could not be found.");
                    }
                }
            }
        }
    }


}
