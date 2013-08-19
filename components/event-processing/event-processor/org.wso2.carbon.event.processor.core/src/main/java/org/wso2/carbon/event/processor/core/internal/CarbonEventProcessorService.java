/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.processor.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.ExecutionPlan;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfigurationFile;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.listener.ExternalStreamConsumer;
import org.wso2.carbon.event.processor.core.internal.listener.ExternalStreamListener;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiOutputStreamListener;
import org.wso2.carbon.event.processor.core.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.core.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.core.internal.stream.EventProducer;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConfigurationFilesystemInvoker;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorConfigurationHelper;
import org.wso2.carbon.event.processor.core.internal.util.helper.SiddhiExtensionLoader;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.stream.input.InputHandler;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventProcessorService implements EventProcessorService {
    private static final Log log = LogFactory.getLog(CarbonEventProcessorService.class);
    // deployed query plans
    private Map<Integer, Map<String, ExecutionPlan>> tenantSpecificExecutionPlans;
    // not distinguishing between deployed vs failed here.
    private Map<Integer, List<ExecutionPlanConfigurationFile>> tenantSpecificExecutionPlanFiles;

    private Map<Integer, Map<String, EventJunction>> tenantSpecificEventJunctions;


    public CarbonEventProcessorService() {
        tenantSpecificExecutionPlans = new ConcurrentHashMap<Integer, Map<String, ExecutionPlan>>();
        tenantSpecificExecutionPlanFiles = new ConcurrentHashMap<Integer, List<ExecutionPlanConfigurationFile>>();
        tenantSpecificEventJunctions = new ConcurrentHashMap<Integer, Map<String, EventJunction>>();
    }

    @Override
    public void deployExecutionPlanConfiguration(ExecutionPlanConfiguration executionPlanConfiguration, AxisConfiguration axisConfiguration) throws
                                                                                                                                             ExecutionPlanDependencyValidationException,
                                                                                                                                             ExecutionPlanConfigurationException {

        String executionPlanName = executionPlanConfiguration.getName();

        OMElement omElement = EventProcessorConfigurationHelper.toOM(executionPlanConfiguration);
        EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(omElement);

        File directory = new File(axisConfiguration.getRepository().getPath());
        if (!directory.exists()) {
            if (directory.mkdir()) {
                throw new ExecutionPlanConfigurationException("Cannot create directory to add tenant specific execution plan : " + executionPlanName);
            }
        }
        directory = new File(directory.getAbsolutePath() + File.separator + EventProcessorConstants.EP_ELE_DIRECTORY);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new ExecutionPlanConfigurationException("Cannot create directory " + EventProcessorConstants.EP_ELE_DIRECTORY + " to add tenant specific  execution plan :" + executionPlanName);
            }
        }
        String pathInFileSystem = directory.getAbsolutePath() + File.separator + executionPlanName + EventProcessorConstants.XML_EXTENSION;
        EventProcessorConfigurationFilesystemInvoker.save(omElement, executionPlanName, pathInFileSystem, axisConfiguration);


    }

    @Override
    public void undeployInactiveExecutionPlanConfiguration(String filePath, AxisConfiguration axisConfiguration) throws
                                                                                                                 ExecutionPlanConfigurationException {
        EventProcessorConfigurationFilesystemInvoker.delete(filePath, axisConfiguration);
    }

    @Override
    public void undeployActiveExecutionPlanConfiguration(String name, AxisConfiguration axisConfiguration) throws
                                                                                                           ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventProcessorConfigurationFilesystemInvoker.delete(getExecutionPlanConfigurationFileByPlanName(name, tenantId).getFilePath(), axisConfiguration);
    }


    public void editActiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                     String executionPlanName,
                                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        try {
            OMElement omElement = AXIOMUtil.stringToOM(executionPlanConfiguration);
            boolean isValid = EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(omElement);
            if (isValid && executionPlanName != null && executionPlanName.length() > 0) {
                String pathInFileSystem = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId).getFilePath();
                EventProcessorConfigurationFilesystemInvoker.delete(pathInFileSystem, axisConfiguration);
                EventProcessorConfigurationFilesystemInvoker.save(executionPlanConfiguration, executionPlanName, pathInFileSystem, axisConfiguration);
            } else {
                throw new ExecutionPlanConfigurationException("Invalid configuration provided.");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new ExecutionPlanConfigurationException("Not a valid xml object, ", e);
        }
    }

    public void editInactiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                       String filePath,
                                                       AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(executionPlanConfiguration);
            EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(omElement);
            ExecutionPlanConfiguration config = EventProcessorConfigurationHelper.fromOM(omElement);
            EventProcessorConfigurationFilesystemInvoker.delete(filePath, axisConfiguration);
            EventProcessorConfigurationFilesystemInvoker.save(executionPlanConfiguration, config.getName(), filePath, axisConfiguration);
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new ExecutionPlanConfigurationException("Not a valid xml object ", e);
        }
    }


    public void addExecutionPlanConfiguration(ExecutionPlanConfiguration executionPlanConfiguration, int tenantId)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException {

        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }

        for (StreamConfiguration importedStreamConfig : executionPlanConfiguration.getImportedStreams()) {
            EventJunction eventJunction = eventJunctionMap.get(importedStreamConfig.getStreamId());

            if (eventJunction == null) {
                log.info("Cannot deploy execution plan: " + executionPlanConfiguration.getName() + ". Stream definitions not found.");
                log.info("Adding execution plan " + executionPlanConfiguration.getName() + " for later deployment.");
                throw new ExecutionPlanDependencyValidationException(importedStreamConfig.getStreamId(), "Stream definition not found");
            }
        }

        boolean isDistributedProcessingEnabled = false; //todo assign these properly.
        int persistenceTimeInterval = 0;
        SiddhiConfiguration siddhiConfig = new SiddhiConfiguration();
        siddhiConfig.setAsyncProcessing(false);
        siddhiConfig.setQueryPlanIdentifier(executionPlanConfiguration.getName());
        siddhiConfig.setInstanceIdentifier("WSO2CEP-Siddhi-Instance-" + UUID.randomUUID().toString());
        siddhiConfig.setDistributedProcessing(isDistributedProcessingEnabled);
        siddhiConfig.setSiddhiExtensions(SiddhiExtensionLoader.loadSiddhiExtensions());

        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfig);

        // todo persistence.
            /*if (persistenceTimeInterval > 0) {
                if (null == SiddhiBackendRuntimeValueHolder.getInstance().getPersistenceStore()) {
                    Cluster cluster = SiddhiBackendRuntimeValueHolder.getInstance().getDataAccessService().getCluster(SiddhiBackendRuntimeValueHolder.getInstance().getClusterInformation());
                    SiddhiBackendRuntimeValueHolder.getInstance().setClusterName(cluster.getName());
                    CasandraPersistenceStore casandraPersistenceStore = new CasandraPersistenceStore(cluster);
                    SiddhiBackendRuntimeValueHolder.getInstance().setPersistenceStore(casandraPersistenceStore);
                }
                siddhiManager.setPersistStore(SiddhiBackendRuntimeValueHolder.getInstance().getPersistenceStore());
            }*/
//        siddhiManager.addExecutionPlanConfiguration(executionPlanConfiguration.getQueryExpressions());

        Map<String, InputHandler> inputHandlerMap = new ConcurrentHashMap<String, InputHandler>(executionPlanConfiguration.getImportedStreams().size());
        for (StreamConfiguration importedStreamConfiguration : executionPlanConfiguration.getImportedStreams()) {
            org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
            siddhiStreamDefinition.name(importedStreamConfiguration.getSiddhiStreamName());
            EventJunction eventJunction = eventJunctionMap.get(importedStreamConfiguration.getStreamId());
            StreamDefinition streamDefinition = eventJunction.getStreamDefinition();

            populateAttributes(siddhiStreamDefinition, streamDefinition.getMetaData());
            populateAttributes(siddhiStreamDefinition, streamDefinition.getPayloadData());
            populateAttributes(siddhiStreamDefinition, streamDefinition.getCorrelationData());
            InputHandler inputHandler = siddhiManager.defineStream(siddhiStreamDefinition);
            inputHandlerMap.put(streamDefinition.getStreamId(), inputHandler);
            log.debug("input handler created for " + siddhiStreamDefinition.getStreamId());
        }

        try {
            siddhiManager.addExecutionPlan(executionPlanConfiguration.getQueryExpressions());
        } catch (Exception e) {
            throw new ExecutionPlanConfigurationException("Invalid query specified", e);
        }

        // exported/output streams
        Map<String, EventProducer> producerMap = new ConcurrentHashMap<String, EventProducer>(executionPlanConfiguration.getExportedStreams().size());
        List<String> newStreams = new ArrayList<String>();

        for (StreamConfiguration exportedStreamConfiguration : executionPlanConfiguration.getExportedStreams()) {
            org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = siddhiManager.getStreamDefinition(exportedStreamConfiguration.getSiddhiStreamName());
            StreamDefinition databridgeStreamDefinition;
            if (siddhiStreamDefinition != null) {
                databridgeStreamDefinition = EventProcessorUtil.convertToDatabridgeStreamDefinition(siddhiStreamDefinition, exportedStreamConfiguration);
            } else {
                throw new ExecutionPlanConfigurationException("No matching Siddhi stream for " + exportedStreamConfiguration.getStreamId() + " in the name of " + exportedStreamConfiguration.getSiddhiStreamName());
            }

            // adding callbacks
            EventJunction junction = eventJunctionMap.get(exportedStreamConfiguration.getStreamId());
            if (junction == null) {
                junction = createEventJunctionWithoutSubscriptions(tenantId, databridgeStreamDefinition);
                newStreams.add(junction.getStreamDefinition().getStreamId());

            }
            SiddhiOutputStreamListener streamCallback = new SiddhiOutputStreamListener(junction, executionPlanConfiguration, tenantId);
            siddhiManager.addCallback(exportedStreamConfiguration.getSiddhiStreamName(), streamCallback);
            producerMap.put(exportedStreamConfiguration.getStreamId(), streamCallback);
        }


        //subscribe input to junction
        for (StreamConfiguration importedStreamConfiguration : executionPlanConfiguration.getImportedStreams()) {
            EventJunction eventJunction = eventJunctionMap.get(importedStreamConfiguration.getStreamId());

            InputHandler inputHandler = inputHandlerMap.get(importedStreamConfiguration.getStreamId());
            SiddhiInputEventDispatcher eventDispatcher = new SiddhiInputEventDispatcher(inputHandler, executionPlanConfiguration, tenantId);

            eventJunction.addConsumer(eventDispatcher);
        }

        //add output to junction
        for (StreamConfiguration exportedStreamConfiguration : executionPlanConfiguration.getExportedStreams()) {
            EventJunction eventJunction = eventJunctionMap.get(exportedStreamConfiguration.getStreamId());
            eventJunction.addProducer(producerMap.get(exportedStreamConfiguration.getStreamId()));
        }


        ExecutionPlan executionPlan = new ExecutionPlan(executionPlanConfiguration.getName(), siddhiManager, executionPlanConfiguration);
        Map<String, ExecutionPlan> tenantExecutionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (tenantExecutionPlans == null) {
            tenantExecutionPlans = new ConcurrentHashMap<String, ExecutionPlan>();
            tenantSpecificExecutionPlans.put(tenantId, tenantExecutionPlans);
        }
        tenantExecutionPlans.put(executionPlanConfiguration.getName(), executionPlan);

        for (String streamId : newStreams) {
            activateInactiveExecutionPlanConfigurations(streamId, tenantId);
        }

    }

    public void removeExecutionPlanConfiguration(String name, int tenantId) {
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null && executionPlanMap.containsKey(name)) {
            ExecutionPlan executionPlan = executionPlanMap.remove(name);
            executionPlan.shutdown();

            ExecutionPlanConfiguration executionPlanConfiguration = executionPlan.getExecutionPlanConfiguration();

            // releasing junction listeners.
            // even if the junction has no consumers after this operations, we don't remove the junction
            // it will only be removed when builder notifies of undeployment.
            for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getImportedStreams()) {
                EventJunction junction = getEventJunction(tenantId, streamConfiguration.getStreamId());
                if (junction != null) {
                    for (EventConsumer consumer : junction.getAllEventConsumers()) {
                        if (consumer.getOwner() == executionPlanConfiguration) {
                            junction.removeConsumer(consumer);
                        }
                    }
                }
            }

            // checking producers from this and dropping junction if its the sole producer for the junction
            // if so remove the producer and notifying formatters
            for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getExportedStreams()) {
                EventJunction junction = getEventJunction(tenantId, streamConfiguration.getStreamId());
                if (junction != null) {
                    for (EventProducer producer : junction.getAllEventProducers()) {
                        if (producer.getOwner() == executionPlanConfiguration) {
                            junction.removeProducer(producer);
                        }
                    }

                    if (junction.getAllEventProducers().size() == 0) {
                        deactivateActiveExecutionPlanConfigurations(streamConfiguration.getStreamId(), tenantId);
                    }

                }
            }

        }
    }

    public void addExecutionPlanConfigurationFile(ExecutionPlanConfigurationFile configurationFile,
                                                  int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles == null) {
            executionPlanConfigurationFiles = new ArrayList<ExecutionPlanConfigurationFile>();
            tenantSpecificExecutionPlanFiles.put(tenantId, executionPlanConfigurationFiles);
        }
        executionPlanConfigurationFiles.add(configurationFile);
    }

    /**
     * Just removes the configuration file
     *
     * @param filePath
     * @param tenantId
     */
    public void removeExecutionPlanConfigurationFile(String filePath, int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        for (Iterator<ExecutionPlanConfigurationFile> iterator = executionPlanConfigurationFiles.iterator(); iterator.hasNext(); ) {
            ExecutionPlanConfigurationFile configurationFile = iterator.next();
            if (configurationFile.getFilePath().equals(filePath)) {
                iterator.remove();
                break;
            }
        }
    }

    public String getActiveExecutionPlanConfigurationContent(String planName, int tenantId)
            throws ExecutionPlanConfigurationException {
        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByPlanName(planName, tenantId);
        if (configFile == null) {
            throw new ExecutionPlanConfigurationException("Configuration file doesn't exist.");
        }
        return readExecutionPlanConfigFile(configFile.getFilePath());
    }

    public String getInactiveExecutionPlanConfigurationContent(String path, int tenantId)
            throws ExecutionPlanConfigurationException {
        return readExecutionPlanConfigFile(path);
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllActiveExecutionConfigurations(int tenantId) {
        Map<String, ExecutionPlanConfiguration> configurationMap = new HashMap<String, ExecutionPlanConfiguration>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (Map.Entry<String, ExecutionPlan> entry : executionPlanMap.entrySet()) {
                configurationMap.put(entry.getKey(), entry.getValue().getExecutionPlanConfiguration());
            }
        }
        return configurationMap;
    }

    @Override
    public ExecutionPlanConfiguration getActiveExecutionConfiguration(String name, int tenantId) {
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            ExecutionPlan executionPlan = executionPlanMap.get(name);
            if (executionPlan != null) {
                return executionPlan.getExecutionPlanConfiguration();
            }
        }
        return null;
    }

    @Override
    public List<ExecutionPlanConfigurationFile> getAllInactiveExecutionPlanConfiguration(int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = this.tenantSpecificExecutionPlanFiles.get(tenantId);

        List<ExecutionPlanConfigurationFile> files = new ArrayList<ExecutionPlanConfigurationFile>();
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile configFile : executionPlanConfigurationFiles) {
                if (configFile.getStatus() == ExecutionPlanConfigurationFile.Status.ERROR || configFile.getStatus() == ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY) {
                    files.add(configFile);
                }
            }
        }
        return files;
    }


    public void subscribeStreamListener(String streamId, EventFormatterListener listener,
                                        int tenantId) {
        Map<String, EventJunction> junctionMap = this.tenantSpecificEventJunctions.get(tenantId);
        if (junctionMap == null) {
            junctionMap = new ConcurrentHashMap<String, EventJunction>();
            this.tenantSpecificEventJunctions.put(tenantId, junctionMap);
        }
        EventJunction junction = junctionMap.get(streamId);
        if (junction != null) {
            junction.addConsumer(new ExternalStreamConsumer(listener, listener));
        }
    }

    public void unsubscribeStreamListener(String streamId, EventFormatterListener listener,
                                          int tenantId) {
        Map<String, EventJunction> junctionMap = this.tenantSpecificEventJunctions.get(tenantId);
        if (junctionMap != null) {
            EventJunction junction = junctionMap.get(streamId);
            if (junction != null) {
                for (EventConsumer consumer : junction.getAllEventConsumers()) {
                    if (consumer.getOwner() == listener) {
                        junction.removeConsumer(consumer);
                    }
                }
            }
        }
    }


    @Override
    public void setTracingEnabled(String executionPlanName, boolean isEnabled,
                                  AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, ExecutionPlan> executionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlans != null) {
            ExecutionPlan executionPlan = executionPlans.get(executionPlanName);
            executionPlan.getExecutionPlanConfiguration().setTracingEnabled(isEnabled);
            editExecutionPlanConfiguration(executionPlan.getExecutionPlanConfiguration(), executionPlanName, tenantId, axisConfiguration);

        }
    }

    @Override
    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled,
                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, ExecutionPlan> executionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlans != null) {
            ExecutionPlan executionPlan = executionPlans.get(executionPlanName);
            executionPlan.getExecutionPlanConfiguration().setStatisticsEnabled(isEnabled);
            editExecutionPlanConfiguration(executionPlan.getExecutionPlanConfiguration(), executionPlanName, tenantId, axisConfiguration);

        }
    }


    public ExecutionPlanConfigurationFile getExecutionPlanConfigurationFile(String path,
                                                                            AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile configFile : executionPlanConfigurationFiles) {
                if (path.equals(configFile.getFilePath())) {
                    return configFile;
                }
            }
        }
        return null;
    }


    /**
     * Activate Inactive Execution Plan Configurations
     *
     * @param tenantId
     * @param resolvedStreamId
     */
    public void activateInactiveExecutionPlanConfigurations(String resolvedStreamId, int tenantId)
            throws EventBuilderConfigurationException, ExecutionPlanConfigurationException {

        List<ExecutionPlanConfigurationFile> reloadFileList = new ArrayList<ExecutionPlanConfigurationFile>();

        if (tenantSpecificExecutionPlanFiles != null && tenantSpecificExecutionPlanFiles.size() > 0) {
            List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);

            if (executionPlanConfigurationFiles != null) {
                for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : executionPlanConfigurationFiles) {
                    if ((executionPlanConfigurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && resolvedStreamId.equalsIgnoreCase(executionPlanConfigurationFile.getDependency())) {
                        reloadFileList.add(executionPlanConfigurationFile);
                    }
                }
            }
        }
        for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : reloadFileList) {
            try {
                EventProcessorConfigurationFilesystemInvoker.reload(executionPlanConfigurationFile.getFilePath(), executionPlanConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Execution Plan configuration file : " + new File(executionPlanConfigurationFile.getFilePath()).getName());
            }
        }

    }

    public void deactivateActiveExecutionPlanConfigurations(String streamId, int tenantId)
            throws EventBuilderConfigurationException {

        EventJunction junction = getEventJunction(tenantId, streamId);

        if (junction != null) {
            int producers = junction.getAllEventProducers().size();
            // if more than 1 producers available, still need to keep the junction.
            if (producers == 0) {

                // junction has no active producers. need to be removed.
                EventJunction removedJunction = this.tenantSpecificEventJunctions.get(tenantId).remove(streamId);

                for (EventConsumer consumer : removedJunction.getAllEventConsumers()) {
                    if (consumer instanceof SiddhiInputEventDispatcher) {
                        ExecutionPlanConfiguration executionPlanConfiguration = (ExecutionPlanConfiguration) consumer.getOwner();
                        ExecutionPlanConfigurationFile executionPlanConfigurationFile = getExecutionPlanConfigurationFileByPlanName(executionPlanConfiguration.getName(), tenantId);
                        try {
                            EventProcessorConfigurationFilesystemInvoker.reload(executionPlanConfigurationFile.getFilePath(), executionPlanConfigurationFile.getAxisConfiguration());
                        } catch (ExecutionPlanConfigurationException e) {
                            log.error("Exception occurred while trying to deploy the Execution Plan configuration file : " + new File(executionPlanConfigurationFile.getFilePath()).getName());
                        }

                    }
                }
                if (EventProcessorValueHolder.getNotificationListener() != null) {
                    EventProcessorValueHolder.getNotificationListener().removeEventStream(tenantId, streamId);
                }
            }
        }
    }


    public void addExternalStream(int tenantId, String streamId) throws
                                                                 ExecutionPlanConfigurationException {
        EventJunction junction = getEventJunction(tenantId, streamId);
        StreamDefinition streamDefinition = null;

        EventBuilderService eventBuilderService = EventProcessorValueHolder.getEventBuilderService();
        List<StreamDefinition> definitions = eventBuilderService.getStreamDefinitions(tenantId);
        for (StreamDefinition definition : definitions) {
            if (streamId.equals(definition.getStreamId())) {
                streamDefinition = definition;
                break;
            }
        }


        if (junction == null) {
            junction = createEventJunctionWithoutSubscriptions(tenantId, streamDefinition);

        } else {
            if (!junction.getStreamDefinition().equals(streamDefinition)) {
                //todo handle
                log.error("Externally defined stream: " + streamDefinition + " is different from the exiting stream :" + junction.getStreamDefinition());
            }
        }

        ExternalStreamListener builderListener = new ExternalStreamListener(junction, null);
        junction.addProducer(builderListener);
        EventProcessorValueHolder.getEventBuilderService().subscribe(junction.getStreamDefinition().getStreamId(), builderListener, tenantId);
        junction.setExternalStream(true);

        activateInactiveExecutionPlanConfigurations(streamId, tenantId);
    }

    public void removeExternalStream(int tenantId, String streamId) {
        EventJunction junction = getEventJunction(tenantId, streamId);

        if (junction != null) {
            List<EventProducer> eventProducers = junction.getAllEventProducers();
            for (EventProducer eventProducer : eventProducers) {
                if (eventProducer instanceof ExternalStreamListener) {
                    junction.removeProducer(eventProducer);
                    junction.setExternalStream(false);
                    EventProcessorValueHolder.getEventBuilderService().unsubsribe(streamId, (ExternalStreamListener) eventProducer, tenantId);
                }
            }
        }

        deactivateActiveExecutionPlanConfigurations(streamId, tenantId);
    }

    //for ui

    public List<String> getStreamIds(int tenantId) {
        if (tenantSpecificEventJunctions.get(tenantId) != null) {
            return new ArrayList<String>(tenantSpecificEventJunctions.get(tenantId).keySet());
        }
        return new ArrayList<String>(0);
    }

    /**
     * gets the relevant stream definition if already available with a junction.
     *
     * @param streamId format-  streamName:version
     * @param tenantId
     * @return
     */
    public StreamDefinition getStreamDefinition(String streamId, int tenantId) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction junction = eventJunctionMap.get(streamId);
            if (junction != null) {
                return junction.getStreamDefinition();
            }
        }
        return null;
    }


//    /**
//     * This is for the cases when some execution plans are waiting for a stream which is the output of another
//     * execution plan.
//     *
//     * @param tenantId
//     * @param configuration the execution plan whose output may trigger redeployment of waiting plans.
//     */
//    public void redeployWaitingDependents(int tenantId, ExecutionPlanConfiguration configuration) {
//        ArrayList<ExecutionPlanConfiguration> successfulRedeployments = new ArrayList<ExecutionPlanConfiguration>();
//
//        if (configuration.getExportedStreams() != null) {
//            for (StreamConfiguration streamConfig : configuration.getExportedStreams()) {
//                try {
//                    String streamWithVersion = streamConfig.getStreamId();
//                    if (tenantSpecificExecutionPlanFiles != null && tenantSpecificExecutionPlanFiles.size() > 0) {
//                        Collection<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId).values();
//
//                        if (executionPlanConfigurationFiles != null) {
//                            Iterator<ExecutionPlanConfigurationFile> executionPlanConfigurationFileIterator = executionPlanConfigurationFiles.iterator();
//                            while (executionPlanConfigurationFileIterator.hasNext()) {
//                                ExecutionPlanConfigurationFile executionPlanConfigurationFile = executionPlanConfigurationFileIterator.next();
//                                if ((executionPlanConfigurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && streamWithVersion.equalsIgnoreCase(executionPlanConfigurationFile.getDependency())) {
//                                    ExecutionPlanConfiguration executionPlanConfiguration = failedTenantSpecificExecutionPlanConfigurations.get(tenantId).get(executionPlanConfigurationFile.getExecutionPlanName());
//                                    // filtering out erroneus ones.
//                                    if (executionPlanConfiguration != null) {
//                                        try {
//                                            boolean success = addExecutionPlanConfiguration(executionPlanConfiguration, tenantId);
//                                            if (success) {
//                                                // need to recursively retry the dependents of these plans.
//                                                successfulRedeployments.add(executionPlanConfiguration);
//                                            }
//                                        } catch (ExecutionPlanDependencyValidationException e) {
//                                            updateConfigurationFileStatus(tenantId, executionPlanConfiguration.getName(), e.getDependency());
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception ex) {
//                    log.error("Still unable to redeploy execution plans for the stream:" + streamConfig.getStreamId(), ex);
//                }
//            }
//        }
//
//        // recursive dependency resolving.
//        for (ExecutionPlanConfiguration newExecutionPlan : successfulRedeployments) {
//            redeployWaitingDependents(tenantId, newExecutionPlan);
//        }
//    }


//    private void addToExecutionPlans(int tenantId,
//                                     Map<Integer, Map<String, ExecutionPlanConfiguration>> tenantSpecificExecutionPlanConfigurations,
//                                     ExecutionPlanConfiguration executionPlanConfiguration) {
//        Map<String, ExecutionPlanConfiguration> executionPlanMap = tenantSpecificExecutionPlanConfigurations.get(tenantId);
//        if (executionPlanMap == null) {
//            executionPlanMap = new ConcurrentHashMap<String, ExecutionPlanConfiguration>();
//            tenantSpecificExecutionPlanConfigurations.put(tenantId, executionPlanMap);
//        }
//        executionPlanMap.put(executionPlanConfiguration.getName(), executionPlanConfiguration);
//    }


//    private void removeFromExecutionPlans(int tenantId,
//                                          Map<Integer, Map<String, ExecutionPlanConfiguration>> tenantSpecificExecutionPlanConfigurations,
//                                          ExecutionPlanConfiguration executionPlanConfiguration) {
//        Map<String, ExecutionPlanConfiguration> queryPlanMap = tenantSpecificExecutionPlanConfigurations.get(tenantId);
//        if (queryPlanMap != null) {
//            queryPlanMap.remove(executionPlanConfiguration.getName());
//        }
//    }


//    public List<String> getAllExecutionPlanConfigurationFileNames(int tenantId) {
//        List<ExecutionPlanConfigurationFile> tenantsFiles = this.tenantSpecificExecutionPlanFiles.get(tenantId);
//        if (tenantsFiles != null) {
//            List<String> files = new ArrayList<String>(tenantsFiles.size());
//            for (ExecutionPlanConfigurationFile configurationFile:tenantsFiles){
//
//                files.add(configurationFile.ge);
//            }
//            return files;
//        }
//        return new ArrayList<String>();
//    }


//
//
//
//    public void activateInactiveExecutionPlans(int tenantId, String dependency)
//            throws ExecutionPlanConfigurationException {
//
//        List<ExecutionPlanConfigurationFile> fileList = new ArrayList<ExecutionPlanConfigurationFile>();
//
//        if (tenantSpecificExecutionPlanFiles != null && tenantSpecificExecutionPlanFiles.size() > 0) {
//            List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
//
//            if (executionPlanConfigurationFiles != null) {
//                Iterator<ExecutionPlanConfigurationFile> executionPlanConfigurationFileIterator = executionPlanConfigurationFiles.iterator();
//                while (executionPlanConfigurationFileIterator.hasNext()) {
//                    ExecutionPlanConfigurationFile executionPlanConfigurationFile = executionPlanConfigurationFileIterator.next();
//                    if ((executionPlanConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && executionPlanConfigurationFile.getDependency().equalsIgnoreCase(dependency)) {
//                        fileList.add(executionPlanConfigurationFile);
//                        executionPlanConfigurationFileIterator.remove();
//                    }
//                }
//            }
//        }
//        for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : fileList) {
//            try {
//                EventFormatterConfigurationFilesystemInvoker.reload(executionPlanConfigurationFile.getFilePath(), executionPlanConfigurationFile.getAxisConfiguration());
//            } catch (Exception e) {
//                log.error("Exception occurred while trying to deploy the Execution Plan configuration file : " + new File(executionPlanConfigurationFile.getFilePath()).getName());
//            }
//        }
//
//    }


    private EventJunction getEventJunction(int tenantId, String streamWithVersion) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            return eventJunctionMap.get(streamWithVersion);
        }
        return null;
    }

    public void notifyAllStreamsToFormatter() {
        for (Map.Entry<Integer, Map<String, EventJunction>> entry : tenantSpecificEventJunctions.entrySet()) {
            Map<String, EventJunction> junctions = entry.getValue();
            for (Map.Entry<String, EventJunction> junctionEntry : junctions.entrySet()) {
                EventProcessorValueHolder.getNotificationListener().addedNewEventStream(entry.getKey(), junctionEntry.getValue().getStreamDefinition().getStreamId());
            }
        }
    }


//    private void saveEditedExecutionPlanConfiguration(String executionPlanName,
//                                                      AxisConfiguration axisConfiguration,
//                                                      OMElement omElement)
//            throws ExecutionPlanConfigurationException {
//
//        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
//        ExecutionPlanConfigurationFile configurationFile = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId);
//        if (configurationFile == null) {
//            throw new ExecutionPlanConfigurationException("Edited file does not exist.");
//        }
//        String pathInFileSystem = configurationFile.getFilePath();
//        EventProcessorConfigurationFilesystemInvoker.delete(pathInFileSystem, axisConfiguration);
//        EventProcessorConfigurationFilesystemInvoker.save(omElement, executionPlanName, pathInFileSystem, axisConfiguration);
//    }


    // gets file by name.
    private ExecutionPlanConfigurationFile getExecutionPlanConfigurationFileByPlanName(String name,
                                                                                       int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile file : executionPlanConfigurationFiles) {
                if (name.equals(file.getExecutionPlanName())) {
                    return file;
                }
            }
        }
        return null;
    }

    private String readExecutionPlanConfigFile(String filePath)
            throws ExecutionPlanConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new ExecutionPlanConfigurationException("Execution plan file not found ", e);
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException("Cannot read the execution plan file ", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file ", e);
            }
        }
        return stringBuffer.toString().trim();
    }

    private void editExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            String executionPlanName, int tenantId, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId);
        String pathInFileSystem = configFile.getFilePath();
        EventProcessorConfigurationFilesystemInvoker.delete(configFile.getFilePath(), axisConfiguration);
        OMElement omElement = EventProcessorConfigurationHelper.toOM(executionPlanConfiguration);
        EventProcessorConfigurationFilesystemInvoker.save(omElement, executionPlanName, pathInFileSystem, axisConfiguration);
    }

    private static void populateAttributes(
            org.wso2.siddhi.query.api.definition.StreamDefinition streamDefinition,
            List<Attribute> attributes) {
        if (attributes != null) {
            for (Attribute attribute : attributes) {
                org.wso2.siddhi.query.api.definition.Attribute siddhiAttribute = EventProcessorUtil.convertToSiddhiAttribute(attribute);
                streamDefinition.attribute(siddhiAttribute.getName(), siddhiAttribute.getType());
            }
        }
    }

    private EventJunction createEventJunctionWithoutSubscriptions(int tenantId,
                                                                  StreamDefinition streamDefinition) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }
        EventJunction junction = new EventJunction(streamDefinition);
        eventJunctionMap.put(streamDefinition.getStreamId(), junction);
        if (EventProcessorValueHolder.getNotificationListener() != null) {
            EventProcessorValueHolder.getNotificationListener().addedNewEventStream(tenantId, streamDefinition.getStreamId());
        }
        return junction;
    }


//    @Override
//    public boolean isTracingEnabled(String executionPlanName, int tenantId)
//            throws ExecutionPlanConfigurationException {
//        ExecutionPlanConfiguration config = getExecutionPlan(executionPlanName, tenantId);
//        if (config == null) {
//            throw new ExecutionPlanConfigurationException("Execution plan doesn't exist");
//        }
//        return config.isTracingEnabled();
//    }
//
//    @Override
//    public boolean isStatisticsEnabled(String executionPlanName, int tenantId)
//            throws ExecutionPlanConfigurationException {
//        ExecutionPlanConfiguration config = getExecutionPlan(executionPlanName, tenantId);
//        if (config == null) {
//            throw new ExecutionPlanConfigurationException("Execution plan doesn't exist");
//        }
//        return config.isStatisticsEnabled();
//    }
}
