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
import org.wso2.carbon.event.builder.core.BasicEventListener;
import org.wso2.carbon.event.builder.core.EventBuilderNotificationListener;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.Wso2EventListener;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.config.EventBuilderConfigurationFile;
import org.wso2.carbon.event.builder.core.internal.config.EventBuilderStreamJunction;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.ConfigurationValidator;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigurationFileSystemInvoker;
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
    private Map<Integer, Map<String, List<EventBuilder>>> tenantSpecificEventBuilderMap;
    private Map<Integer, Map<String, EventBuilderConfigurationFile>> tenantSpecificEventBuilderConfigFileMap;
    private Map<Integer, Map<String, String>> tenantSpecificEventBuilderNameToFilePathMap;
    private Map<Integer, EventBuilderStreamJunction> tenantSpecificStreamJunctionMap;
    private List<EventBuilderNotificationListener> eventBuilderNotificationListeners;

    public CarbonEventBuilderService() {
        tenantSpecificEventBuilderMap = new ConcurrentHashMap<Integer, Map<String, List<EventBuilder>>>();
        tenantSpecificEventBuilderConfigFileMap = new ConcurrentHashMap<Integer, Map<String, EventBuilderConfigurationFile>>();
        tenantSpecificEventBuilderNameToFilePathMap = new ConcurrentHashMap<Integer, Map<String, String>>();
        tenantSpecificStreamJunctionMap = new ConcurrentHashMap<Integer, EventBuilderStreamJunction>();
        eventBuilderNotificationListeners = new ArrayList<EventBuilderNotificationListener>();
    }

    @Override
    public void subscribe(String streamDefinitionId, Wso2EventListener wso2EventListener,
                          int tenantId) throws EventBuilderConfigurationException {
        EventBuilderStreamJunction eventBuilderStreamJunction = tenantSpecificStreamJunctionMap.get(tenantId);
        eventBuilderStreamJunction.addEventListener(streamDefinitionId, wso2EventListener);
    }

    @Override
    public void unsubsribe(String streamDefinitionId, Wso2EventListener wso2EventListener,
                           int tenantId) {
        EventBuilderStreamJunction eventBuilderStreamJunction = tenantSpecificStreamJunctionMap.get(tenantId);
        eventBuilderStreamJunction.removeEventListener(streamDefinitionId, wso2EventListener);
    }

    @Override
    public void subscribe(String streamDefinitionId, BasicEventListener basicEventListener,
                          int tenantId) throws EventBuilderConfigurationException {
        EventBuilderStreamJunction eventBuilderStreamJunction = tenantSpecificStreamJunctionMap.get(tenantId);
        eventBuilderStreamJunction.addEventListener(streamDefinitionId, basicEventListener);
    }

    @Override
    public void unsubsribe(String streamDefinitionId, BasicEventListener basicEventListener,
                           int tenantId) {
        EventBuilderStreamJunction eventBuilderStreamJunction = tenantSpecificStreamJunctionMap.get(tenantId);
        eventBuilderStreamJunction.removeEventListener(streamDefinitionId, basicEventListener);
    }

    public List<EventBuilderNotificationListener> getEventBuilderNotificationListeners() {
        return eventBuilderNotificationListeners;
    }

    @Override
    public void registerEventBuilderNotificationListener(
            EventBuilderNotificationListener eventBuilderNotificationListener) {
        this.eventBuilderNotificationListeners.add(eventBuilderNotificationListener);
        notifyEventBuilderNotificationListeners();
    }

    @Override
    public void undeployActiveEventBuilderConfiguration(String eventBuilderName,
                                                        AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        Map<String, String> eventBuilderNameToFilePathMap = tenantSpecificEventBuilderNameToFilePathMap.get(tenantId);
        if (eventBuilderConfigurationFileMap != null) {
            String filePath = eventBuilderNameToFilePathMap.get(eventBuilderName);
            EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(filePath);
            if (eventBuilderConfigurationFile != null) {
                try {
                    for (EventBuilderNotificationListener eventBuilderNotificationListener : this.eventBuilderNotificationListeners) {
                        eventBuilderNotificationListener.configurationRemoved(tenantId, eventBuilderConfigurationFile.getStreamWithVersion());
                    }
                } catch (Throwable e) {
                    log.error("Error on notifying notification listeners about removal of event builder: " + eventBuilderName, e);
                } finally {
                    EventBuilderConfigurationFileSystemInvoker.deleteConfigurationFromFileSystem(eventBuilderConfigurationFile.getFilePath());
                    EventBuilderConfigHelper.executeUndeployment(eventBuilderConfigurationFile.getFilePath(), axisConfiguration);
                }
            } else {
                String errMsg = "Cannot find the event builder configuration '" + eventBuilderName + "' for this tenant.";
                log.error(errMsg);
                throw new EventBuilderConfigurationException(errMsg);
            }
        }
    }

    @Override
    public void undeployInactiveEventBuilderConfiguration(String filename,
                                                          AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFileMap != null) {
            String filePath = EventBuilderUtil.generateFilePath(filename, axisConfiguration);
            EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(filePath);
            if (eventBuilderConfigurationFile != null) {
                EventBuilderConfigurationFileSystemInvoker.deleteConfigurationFromFileSystem(eventBuilderConfigurationFile.getFilePath());
                EventBuilderConfigHelper.executeUndeployment(eventBuilderConfigurationFile.getFilePath(), axisConfiguration);
            } else {
                String errMsg = "Cannot find the event builder configuration file for this tenant : " + filename;
                log.error(errMsg);
                throw new EventBuilderConfigurationException(errMsg);
            }
        }
    }

    public void addEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        // Start: Checking preconditions to add the event builder
        Map<String, List<EventBuilder>> eventBuilderListMap
                = tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderListMap == null) {
            eventBuilderListMap = new ConcurrentHashMap<String, List<EventBuilder>>();
            tenantSpecificEventBuilderMap.put(tenantId, eventBuilderListMap);
        }

        EventBuilderStreamJunction eventBuilderStreamJunction = tenantSpecificStreamJunctionMap.get(tenantId);
        if (eventBuilderStreamJunction == null) {
            eventBuilderStreamJunction = new EventBuilderStreamJunction();
            tenantSpecificStreamJunctionMap.put(tenantId, eventBuilderStreamJunction);
        }
        String toStreamDefinitionId = EventBuilderUtil.getStreamIdFrom(eventBuilderConfiguration);
        List<EventBuilder> eventBuilderList = eventBuilderListMap.get(toStreamDefinitionId);
        if (eventBuilderList == null) {
            eventBuilderList = new ArrayList<EventBuilder>();
            eventBuilderListMap.put(toStreamDefinitionId, eventBuilderList);
        }
        // End; Checking preconditions to add the event builder

        EventBuilder eventBuilder = new EventBuilder(eventBuilderConfiguration, axisConfiguration);
        eventBuilderStreamJunction.registerEventSender(eventBuilder);
        eventBuilderList.add(eventBuilder);
    }

    public void removeEventBuilder(String eventBuilderName,
                                   int tenantId)
            throws EventBuilderConfigurationException {
        Map<String, List<EventBuilder>> eventBuilderListMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        EventBuilderStreamJunction eventBuilderStreamJunction = this.tenantSpecificStreamJunctionMap.get(tenantId);
        EventBuilderConfiguration eventBuilderConfiguration = getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        if (eventBuilderListMap != null) {
            int removedCount = 0;
            String exportedStreamDefinitionId = EventBuilderUtil.getStreamIdFrom(eventBuilderConfiguration);
            List<EventBuilder> eventBuilderList = eventBuilderListMap.get(exportedStreamDefinitionId);
            Iterator<EventBuilder> eventBuilderIterator = eventBuilderList.iterator();
            while (eventBuilderIterator.hasNext()) {
                EventBuilder eventBuilder = eventBuilderIterator.next();
                if (eventBuilder.getEventBuilderConfiguration().getEventBuilderName().equals(eventBuilderConfiguration.getEventBuilderName())) {
                    eventBuilder.unsubscribeFromTransportAdaptor(null);
                    eventBuilderStreamJunction.unregisterEventSender(eventBuilder);
                    eventBuilderIterator.remove();
                    removedCount++;
                }
            }
            if (removedCount == 0) {
                throw new EventBuilderConfigurationException("Could not find the specified event builder '"
                        + eventBuilderConfiguration.getEventBuilderName() + "' for removal for the given axis configuration");
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
        Map<String, String> eventBuilderNameToFilePathMap = tenantSpecificEventBuilderNameToFilePathMap.get(tenantId);
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderNameToFilePathMap == null) {
            eventBuilderNameToFilePathMap = new ConcurrentHashMap<String, String>();
            eventBuilderConfigurationFileMap = new ConcurrentHashMap<String, EventBuilderConfigurationFile>();
            tenantSpecificEventBuilderNameToFilePathMap.put(tenantId, eventBuilderNameToFilePathMap);
            tenantSpecificEventBuilderConfigFileMap.put(tenantId, eventBuilderConfigurationFileMap);
        }
        if (eventBuilderNameToFilePathMap.get(eventBuilderName) == null) {
            EventBuilderConfigurationFile eventBuilderConfigurationFile = createEventBuilderConfigurationFile(eventBuilderName, filePath, status, axisConfiguration, deploymentStatusMessage, dependency, streamNameWithVersion, ebConfigElement);
            eventBuilderConfigurationFileMap.put(filePath, eventBuilderConfigurationFile);
            eventBuilderNameToFilePathMap.put(eventBuilderName, filePath);
        } else {
            log.error("Event builder file configuration for the given event builder '" + eventBuilderName + "' already exists.");
        }
    }

    public void removeEventBuilderConfigurationFile(String filePath, int tenantId) {
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        Map<String, String> eventBuilderNameToFilePathMap = tenantSpecificEventBuilderNameToFilePathMap.get(tenantId);
        EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(filePath);
        if (eventBuilderConfigurationFile != null) {
            if ((eventBuilderConfigurationFile.getFilePath().equals(filePath))) {
                if (DeploymentStatus.DEPLOYED.equals(eventBuilderConfigurationFile.getDeploymentStatus())) {
                    removeEventBuilder(eventBuilderConfigurationFile.getEventBuilderName(), tenantId);
                }
                eventBuilderNameToFilePathMap.remove(eventBuilderConfigurationFile.getEventBuilderName());
                eventBuilderConfigurationFileMap.remove(filePath);
                log.info("Removed event builder configuration file for event builder '" + eventBuilderConfigurationFile.getEventBuilderName() + "'");
            }
        } else {
            throw new EventBuilderConfigurationException("Cannot find event builder configuration file for removal:" + EventBuilderUtil.deriveConfigurationFilenameFrom(filePath));
        }
    }

    @Override
    public List<EventBuilderConfiguration> getAllActiveEventBuilderConfigurations(int tenantId) {
        List<EventBuilderConfiguration> eventBuilderConfigurations = new ArrayList<EventBuilderConfiguration>();
        Map<String, List<EventBuilder>> eventBuilderListMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderListMap != null) {
            for (List<EventBuilder> eventBuilderList : eventBuilderListMap.values()) {
                for (EventBuilder eventBuilder : eventBuilderList) {
                    eventBuilderConfigurations.add(eventBuilder.getEventBuilderConfiguration());
                }
            }
        }

        return eventBuilderConfigurations;
    }

    @Override
    public EventBuilderConfiguration getActiveEventBuilderConfiguration(String eventBuilderName,
                                                                        int tenantId) {
        EventBuilderConfiguration eventBuilderConfiguration = null;
        Map<String, List<EventBuilder>> eventBuilderListMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderListMap != null) {
            boolean foundEventBuilder = false;
            Iterator<List<EventBuilder>> eventBuilderListIterator = eventBuilderListMap.values().iterator();
            while (eventBuilderListIterator.hasNext() && !foundEventBuilder) {
                List<EventBuilder> eventBuilderList = eventBuilderListIterator.next();
                for (EventBuilder eventBuilder : eventBuilderList) {
                    if (eventBuilder.getEventBuilderConfiguration().getEventBuilderName().equals(eventBuilderName)) {
                        eventBuilderConfiguration = eventBuilder.getEventBuilderConfiguration();
                        foundEventBuilder = true;
                        break;
                    }
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
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = inputTransportAdaptorManagerService.getActiveInputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
            transportAdaptorType = inputTransportAdaptorConfiguration.getType();
        } catch (InputTransportAdaptorManagerConfigurationException e) {
            log.error("Error while trying to retrieve supported input mapping types.", e);
        }
        if (transportAdaptorType != null) {
            InputTransportAdaptorDto inputTransportAdaptorDto = inputTransportAdaptorService.getTransportAdaptorDto(transportAdaptorType);
            if (inputTransportAdaptorDto != null) {
                for (String messageType : inputTransportAdaptorDto.getSupportedMessageTypes()) {
                    supportedInputMappingTypes.add(EventBuilderConstants.MESSAGE_TYPE_STRING_MAP.get(messageType));
                }
            }
        }
        return supportedInputMappingTypes;
    }

    @Override
    public List<StreamDefinition> getStreamDefinitions(int tenantId) {
        List<StreamDefinition> streamDefinitions = null;
        EventBuilderStreamJunction eventBuilderStreamJunction = this.tenantSpecificStreamJunctionMap.get(tenantId);
        if (eventBuilderStreamJunction != null) {
            streamDefinitions = eventBuilderStreamJunction.getStreamDefinitions();
        }

        return streamDefinitions;
    }

    @Override
    public void deployEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        String filePath = EventBuilderUtil.generateFilePath(eventBuilderConfiguration, axisConfiguration);
        String eventBuilderName = eventBuilderConfiguration.getEventBuilderName();
        String toStreamDefinitionId = EventBuilderUtil.getStreamIdFrom(eventBuilderConfiguration);
        Map<String, String> eventBuilderNameToFilePathMap = this.tenantSpecificEventBuilderNameToFilePathMap.get(tenantId);
        if (eventBuilderNameToFilePathMap == null) {
            eventBuilderNameToFilePathMap = new ConcurrentHashMap<String, String>();
            this.tenantSpecificEventBuilderNameToFilePathMap.put(tenantId, eventBuilderNameToFilePathMap);
            this.tenantSpecificEventBuilderConfigFileMap.put(tenantId, new ConcurrentHashMap<String, EventBuilderConfigurationFile>());
        }
        if (eventBuilderNameToFilePathMap.get(eventBuilderName) == null) {
            String inputMappingType = eventBuilderConfiguration.getInputMapping().getMappingType();
            OMElement ebConfigElement = EventBuilderConfigHelper.getEventBuilderConfigBuilder(inputMappingType).eventBuilderConfigurationToOM(eventBuilderConfiguration);
            EventBuilderConfigurationFile eventBuilderConfigurationFile =
                    createEventBuilderConfigurationFile(eventBuilderConfiguration.getEventBuilderName(), filePath,
                            eventBuilderConfiguration.getDeploymentStatus(), axisConfiguration,
                            null, null, toStreamDefinitionId, ebConfigElement);
            EventBuilderConfigurationFileSystemInvoker.saveConfigurationToFileSystem(eventBuilderConfiguration, eventBuilderConfigurationFile.getFilePath());
            EventBuilderConfigHelper.executeDeployment(eventBuilderConfigurationFile.getFilePath(), axisConfiguration);
        } else {
            throw new EventBuilderConfigurationException("An event builder configuration file of the same name already exists.");
        }
    }

    @Override
    public void setTraceEnabled(String eventBuilderName, boolean traceEnabled,
                                AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventBuilderConfiguration eventBuilderConfiguration = getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        eventBuilderConfiguration.setTraceEnabled(traceEnabled);
        EventBuilderConfigBuilder eventBuilderConfigBuilder = EventBuilderConfigHelper.getEventBuilderConfigBuilder(eventBuilderConfiguration.getInputMapping().getMappingType());
        String ebConfigXml = eventBuilderConfigBuilder.eventBuilderConfigurationToOM(eventBuilderConfiguration).toString();
        editActiveEventBuilderConfiguration(ebConfigXml, eventBuilderName, axisConfiguration);
    }

    @Override
    public void setStatisticsEnabled(String eventBuilderName, boolean statisticsEnabled,
                                     AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventBuilderConfiguration eventBuilderConfiguration = getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        eventBuilderConfiguration.setStatisticsEnabled(statisticsEnabled);
        EventBuilderConfigBuilder eventBuilderConfigBuilder = EventBuilderConfigHelper.getEventBuilderConfigBuilder(eventBuilderConfiguration.getInputMapping().getMappingType());
        String ebConfigXml = eventBuilderConfigBuilder.eventBuilderConfigurationToOM(eventBuilderConfiguration).toString();
        editActiveEventBuilderConfiguration(ebConfigXml, eventBuilderName, axisConfiguration);
    }

    @Override
    public String getActiveEventBuilderConfigurationContent(String eventBuilderName, int tenantId) {
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        Map<String, String> eventBuilderNameToFilePathMap = tenantSpecificEventBuilderNameToFilePathMap.get(tenantId);
        String filePath = eventBuilderNameToFilePathMap.get(eventBuilderName);
        EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(filePath);
        if (eventBuilderConfigurationFile != null && eventBuilderConfigurationFile.getEbConfigOmElement() != null) {
            return eventBuilderConfigurationFile.getEbConfigOmElement().toString();
        }
        return null;
    }

    @Override
    public List<EventBuilderConfigurationFile> getAllInactiveEventBuilderConfigurations(
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
    public String getInactiveEventBuilderConfigurationContent(String filename,
                                                              AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFileMap != null && !eventBuilderConfigurationFileMap.isEmpty()) {
            String filePath = EventBuilderUtil.generateFilePath(filename, axisConfiguration);
            EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(filePath);
            if (eventBuilderConfigurationFile != null && eventBuilderConfigurationFile.getEbConfigOmElement() != null) {
                return eventBuilderConfigurationFile.getEbConfigOmElement().toString();
            } else {
                //TODO Check whether this method leaves a security hole
                return EventBuilderConfigurationFileSystemInvoker.readEventBuilderConfigurationFile(filePath);
            }
        }
        return null;
    }

    @Override
    public void editActiveEventBuilderConfiguration(String eventBuilderConfigXml,
                                                    String originalEventBuilderName,
                                                    AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventBuilderConfigXml);
            ConfigurationValidator.validateEventBuilderConfiguration(omElement);
            String mappingType = EventBuilderConfigHelper.getInputMappingType(omElement);
            if (mappingType != null) {
                EventBuilderConfiguration updatedEventBuilderConfiguration = EventBuilderConfigHelper
                        .getEventBuilderConfigBuilder(mappingType)
                        .getEventBuilderConfiguration(omElement, tenantId, mappingType);
                if (updatedEventBuilderConfiguration != null) {
                    undeployActiveEventBuilderConfiguration(originalEventBuilderName, axisConfiguration);
                    deployEventBuilderConfiguration(updatedEventBuilderConfiguration, axisConfiguration);
                } else {
                    String errMsg = "Error while trying to create an event builder configuration from given XML syntax";
                    log.error(errMsg);
                    throw new EventBuilderConfigurationException(errMsg);
                }
            }
        } catch (XMLStreamException e) {
            String errMsg = "Error while creating the xml object";
            log.error(errMsg);
            throw new EventBuilderConfigurationException(errMsg + ":" + e.getMessage(), e);
        }
    }

    @Override
    public void editInactiveEventBuilderConfiguration(String eventBuilderConfigXml, String filename,
                                                      AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventBuilderConfigXml);
            ConfigurationValidator.validateEventBuilderConfiguration(omElement);
            String mappingType = EventBuilderConfigHelper.getInputMappingType(omElement);
            if (mappingType != null) {
                    EventBuilderConfiguration updatedEventBuilderConfiguration = EventBuilderConfigHelper.getEventBuilderConfigBuilder(mappingType).getEventBuilderConfiguration(omElement, tenantId, mappingType);
                if (updatedEventBuilderConfiguration != null) {
                    undeployInactiveEventBuilderConfiguration(filename, axisConfiguration);
                    deployEventBuilderConfiguration(updatedEventBuilderConfiguration, axisConfiguration);
                } else {
                    String errMsg = "Error while trying to create an event builder configuration from given XML syntax";
                    log.error(errMsg);
                    throw new EventBuilderConfigurationException(errMsg);
                }
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new EventBuilderConfigurationException("Not a valid xml object, ", e);
        }
    }

    private EventBuilderConfigurationFile createEventBuilderConfigurationFile(
            String eventBuilderName, String filePath, DeploymentStatus status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage, String dependency, String streamNameWithVersion,
            OMElement ebConfigElement) {
        EventBuilderConfigurationFile eventBuilderConfigurationFile = new EventBuilderConfigurationFile(filePath);
        eventBuilderConfigurationFile.setEventBuilderName(eventBuilderName);
        eventBuilderConfigurationFile.setDeploymentStatus(status);
        eventBuilderConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventBuilderConfigurationFile.setDependency(dependency);
        eventBuilderConfigurationFile.setAxisConfiguration(axisConfiguration);
        eventBuilderConfigurationFile.setStreamWithVersion(streamNameWithVersion);
        eventBuilderConfigurationFile.setEbConfigOmElement(ebConfigElement);
        return eventBuilderConfigurationFile;
    }

    public boolean isEventBuilderConfigurationFileRegistered(String filePath,
                                                             int tenantId) {
        if (tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            if (eventBuilderConfigurationFileMap != null && eventBuilderConfigurationFileMap.get(filePath) != null) {
                return true;
            }
        }
        return false;
    }

    public void notifyEventBuilderNotificationListeners()
            throws EventBuilderConfigurationException {
        //TODO This code snippet iterates through all tenants and dispatches notifications to listeners of all deployed event builders?
        if (tenantSpecificEventBuilderConfigFileMap != null && tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            for (Map.Entry<Integer, Map<String, EventBuilderConfigurationFile>> tenantEbConfigFileMap : tenantSpecificEventBuilderConfigFileMap.entrySet()) {
                Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantEbConfigFileMap.getValue();
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileMap.values()) {
                    if (eventBuilderConfigurationFile.getDeploymentStatus().equals(DeploymentStatus.DEPLOYED)) {
                        for (EventBuilderNotificationListener eventBuilderNotificationListener : this.eventBuilderNotificationListeners) {
                            int tenantId = PrivilegedCarbonContext.getCurrentContext(eventBuilderConfigurationFile.getAxisConfiguration()).getTenantId();
                            eventBuilderNotificationListener.configurationAdded(tenantId, eventBuilderConfigurationFile.getStreamWithVersion());
                        }
                    }
                }
            }
        }

    }

    public void activateInactiveEventBuilderConfigurations(String transportAdaptorName,
                                                           int tenantId)
            throws EventBuilderConfigurationException {
        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();
        if (tenantSpecificEventBuilderNameToFilePathMap != null && tenantSpecificEventBuilderNameToFilePathMap.size() > 0) {
            Map<String, String> eventBuilderNameToFilePathMap = tenantSpecificEventBuilderNameToFilePathMap.get(tenantId);
            Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            if (eventBuilderNameToFilePathMap != null && eventBuilderConfigurationFileMap != null) {
                Iterator<String> eventBuilderFilePathIterator = eventBuilderNameToFilePathMap.values().iterator();
                while (eventBuilderFilePathIterator.hasNext()) {
                    String filePath = eventBuilderFilePathIterator.next();
                    EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(filePath);
                    if ((eventBuilderConfigurationFile.getDeploymentStatus().equals(DeploymentStatus.WAITING_FOR_DEPENDENCY)) && eventBuilderConfigurationFile.getDependency().equals(transportAdaptorName)) {
                        eventBuilderConfigurationFileMap.remove(filePath);
                        fileList.add(eventBuilderConfigurationFile);
                        eventBuilderFilePathIterator.remove();
                    }
                }
            }
        }
        for (EventBuilderConfigurationFile eventBuilderConfigurationFile : fileList) {
            try {
                EventBuilderConfigHelper.reload(eventBuilderConfigurationFile.getFilePath(), eventBuilderConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                String filePath = eventBuilderConfigurationFile.getFilePath();
                log.error("Exception occurred while trying to deploy the event builder configuration file '" + filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length()) + "'", e);
            }
        }
    }

    public void deactivateActiveEventBuilderConfigurations(
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration, int tenantId)
            throws EventBuilderConfigurationException {

        List<String> eventBuildersToBeRemoved = new ArrayList<String>();
        String transportAdaptorName = inputTransportAdaptorConfiguration.getName();
        if (tenantSpecificEventBuilderMap != null && tenantSpecificEventBuilderMap.size() > 0) {
            Map<String, List<EventBuilder>> eventBuilderMap = tenantSpecificEventBuilderMap.get(tenantId);
            Iterator<List<EventBuilder>> eventBuilderListIterator = eventBuilderMap.values().iterator();
            while (eventBuilderListIterator.hasNext()) {
                List<EventBuilder> eventBuilderList = eventBuilderListIterator.next();
                for (EventBuilder eventBuilder : eventBuilderList) {
                    if (eventBuilder.getEventBuilderConfiguration().getInputStreamConfiguration().getTransportAdaptorName().equals(transportAdaptorName)) {
                        eventBuilder.unsubscribeFromTransportAdaptor(inputTransportAdaptorConfiguration);
                        eventBuildersToBeRemoved.add(eventBuilder.getEventBuilderConfiguration().getEventBuilderName());
                        eventBuilderListIterator.remove();
                    }
                }
            }
        }

        for (String eventBuilderToBeRemoved : eventBuildersToBeRemoved) {
            Map<String, EventBuilderConfigurationFile> eventBuilderConfigurationFileMap = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            Map<String, String> eventBuilderNameToFilePathMap = tenantSpecificEventBuilderNameToFilePathMap.get(tenantId);
            if (eventBuilderConfigurationFileMap != null) {
                String filePath = eventBuilderNameToFilePathMap.get(eventBuilderToBeRemoved);
                EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileMap.get(filePath);
                eventBuilderConfigurationFile.setDeploymentStatus(DeploymentStatus.WAITING_FOR_DEPENDENCY);
                eventBuilderConfigurationFile.setDependency(transportAdaptorName);
                eventBuilderConfigurationFile.setDeploymentStatusMessage("Transport adaptor configuration not found.");
                for (EventBuilderNotificationListener eventBuilderNotificationListener : this.eventBuilderNotificationListeners) {
                    eventBuilderNotificationListener.configurationRemoved(tenantId, eventBuilderConfigurationFile.getStreamWithVersion());
                }
                log.info("Event builder '" + eventBuilderConfigurationFile.getEventBuilderName() + "' undeployed because dependency '" + transportAdaptorName + "' could not be found.");
            }
        }
    }

}
