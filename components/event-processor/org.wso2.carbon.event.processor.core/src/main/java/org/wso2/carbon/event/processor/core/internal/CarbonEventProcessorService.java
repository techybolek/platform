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
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigurationFile;
import org.wso2.carbon.event.formatter.core.EventFormatterListener;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.ExecutionPlan;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfigurationFile;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.config.EventProcessorConfigurationHelper;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.listener.BasicEventListenerImpl;
import org.wso2.carbon.event.processor.core.internal.listener.EventFormatterConsumerImpl;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiOutputStreamListener;
import org.wso2.carbon.event.processor.core.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.core.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.core.internal.stream.EventProducer;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConfigurationFilesystemInvoker;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventProcessorService implements EventProcessorService {
    private static final Log log = LogFactory.getLog(CarbonEventProcessorService.class);
    // successfully deployed query plan configurations
    private Map<Integer, Map<String, ExecutionPlanConfiguration>> tenantSpecificExecutionPlanConfigurations;
    // failed ones. pending deployment and erroneous ones.
    private Map<Integer, Map<String, ExecutionPlanConfiguration>> failedTenantSpecificExecutionPlanConfigurations;
    // deployed query plans
    private Map<Integer, Map<String, ExecutionPlan>> tenantSpecificExecutionPlans;
    // not distinguishing between deployed vs failed here.
    private Map<Integer, Map<String, ExecutionPlanConfigurationFile>> tenantSpecificExecutionPlanFiles;

    private Map<Integer, Map<String, EventJunction>> tenantSpecificEventJunctions;


    public CarbonEventProcessorService() {
        tenantSpecificExecutionPlans = new ConcurrentHashMap<Integer, Map<String, ExecutionPlan>>();
        tenantSpecificExecutionPlanFiles = new ConcurrentHashMap<Integer, Map<String, ExecutionPlanConfigurationFile>>();
        tenantSpecificExecutionPlanConfigurations = new ConcurrentHashMap<Integer, Map<String, ExecutionPlanConfiguration>>();
        failedTenantSpecificExecutionPlanConfigurations = new ConcurrentHashMap<Integer, Map<String, ExecutionPlanConfiguration>>();
        tenantSpecificEventJunctions = new ConcurrentHashMap<Integer, Map<String, EventJunction>>();
    }

    @Override
    public boolean addExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            int tenantId)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException {

        if (EventProcessorValueHolder.getEventBuilderService() != null) {
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
                    addToExecutionPlans(tenantId, failedTenantSpecificExecutionPlanConfigurations, executionPlanConfiguration);
                    throw new ExecutionPlanDependencyValidationException(importedStreamConfig.getStreamId(), "Stream definition not found");
                }
            }


            boolean isDistributedProcessingEnabled = false; //todo assign these properly.
            int persistenceTimeInterval = 0;
            SiddhiConfiguration siddhiConfig = new SiddhiConfiguration();
            siddhiConfig.setAsyncProcessing(false); //todo check which is good?
            siddhiConfig.setQueryPlanIdentifier(executionPlanConfiguration.getName());
            siddhiConfig.setInstanceIdentifier("WSO2CEP-Siddhi-Instance-" + UUID.randomUUID().toString());
            siddhiConfig.setDistributedProcessing(isDistributedProcessingEnabled);
            /*if(null==SiddhiBackendRuntimeValueHolder.getInstance().getSiddhiExtentions()){
                SiddhiBackendRuntimeValueHolder.getInstance().setSiddhiExtentions(SiddhiConfigLoader.loadSiddhiExtensions());
            }*/
            //  siddhiConfig.setSiddhiExtensions(SiddhiBackendRuntimeValueHolder.getInstance().getSiddhiExtentions());
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
//        siddhiManager.addExecutionPlan(executionPlanConfiguration.getQueryExpressions());

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
                log.info("input handler created for " + siddhiStreamDefinition.getStreamId());
            }

            try {
                siddhiManager.addExecutionPlan(executionPlanConfiguration.getQueryExpressions());
            } catch (Exception e) {
                throw new ExecutionPlanConfigurationException("Invalid query specified", e);
            }

            // exported/output streams
            Map<String, EventProducer> producerMap = new ConcurrentHashMap<String, EventProducer>(executionPlanConfiguration.getExportedStreams().size());

            for (StreamConfiguration exportedStreamConfiguration : executionPlanConfiguration.getExportedStreams()) {
                org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = siddhiManager.getStreamDefinition(exportedStreamConfiguration.getSiddhiStreamName());
                StreamDefinition databridgeStreamDefinition;
                if (siddhiStreamDefinition != null) {
                    databridgeStreamDefinition = EventProcessorUtil.convertToDatabridgeStreamDefinition(siddhiStreamDefinition, exportedStreamConfiguration);
                } else {
                    EventJunction eventJunction = eventJunctionMap.get(exportedStreamConfiguration.getStreamId());
                    if (eventJunction != null) {
                        databridgeStreamDefinition = eventJunction.getStreamDefinition();
                    } else {
                        throw new ExecutionPlanConfigurationException("No matching Siddhi stream for " + exportedStreamConfiguration.getStreamId() + " in the name of " + exportedStreamConfiguration.getSiddhiStreamName());
                    }
                }

                // adding callbacks
                EventJunction junction = eventJunctionMap.get(exportedStreamConfiguration.getStreamId());
                if (junction == null) {
                    junction = createEventJunctionWithoutSubscriptions(tenantId, databridgeStreamDefinition);
                    if (EventProcessorValueHolder.getNotificationListener() != null) {
                        EventProcessorValueHolder.getNotificationListener().addedNewEventStream(tenantId, databridgeStreamDefinition.getStreamId());
                    }

                }
                SiddhiOutputStreamListener streamCallback = new SiddhiOutputStreamListener(junction, executionPlanConfiguration);
                siddhiManager.addCallback(exportedStreamConfiguration.getSiddhiStreamName(), streamCallback);
                producerMap.put(exportedStreamConfiguration.getStreamId(), streamCallback);
            }


            //add input to junction
            for (StreamConfiguration importedStreamConfiguration : executionPlanConfiguration.getImportedStreams()) {
                EventJunction eventJunction = eventJunctionMap.get(importedStreamConfiguration.getStreamId());

                InputHandler inputHandler = inputHandlerMap.get(importedStreamConfiguration.getStreamId());
                SiddhiInputEventDispatcher eventDispatcher = new SiddhiInputEventDispatcher(inputHandler, executionPlanConfiguration);

                eventJunction.addConsumer(eventDispatcher);
            }

            //add output to junction
            Map<String, org.wso2.siddhi.query.api.definition.StreamDefinition> streamDefinitionMap = new ConcurrentHashMap<String, org.wso2.siddhi.query.api.definition.StreamDefinition>();
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


            addToExecutionPlans(tenantId, tenantSpecificExecutionPlanConfigurations, executionPlanConfiguration);
            removeFromExecutionPlans(tenantId, failedTenantSpecificExecutionPlanConfigurations, executionPlanConfiguration);

            ExecutionPlanConfigurationFile configurationFile = getExecutionPlanConfigurationFileByName(executionPlanConfiguration.getName(), tenantId);
            if (configurationFile != null) {
                configurationFile.setStatus(ExecutionPlanConfigurationFile.Status.DEPLOYED);
            }

            log.info("Execution plan " + executionPlanConfiguration.getName() + " is deployed successfully.");

            return true;
        } else {
            log.info("Execution plan " + executionPlanConfiguration.getName() + " cannot be deployed.");
            addToExecutionPlans(tenantId, failedTenantSpecificExecutionPlanConfigurations, executionPlanConfiguration);
            return false;
        }
    }

    /**
     * This is for the cases when some execution plans are waiting for a stream which is the output of another
     * execution plan.
     *
     * @param tenantId
     * @param configuration the execution plan whose output may trigger redeployment of waiting plans.
     */
    public void redeployWaitingDependents(int tenantId, ExecutionPlanConfiguration configuration) {
        ArrayList<ExecutionPlanConfiguration> successfulRedeployments = new ArrayList<ExecutionPlanConfiguration>();

        if (configuration.getExportedStreams() != null) {
            for (StreamConfiguration streamConfig : configuration.getExportedStreams()) {
                try {
                    String streamWithVersion = streamConfig.getStreamId();
                    if (tenantSpecificExecutionPlanFiles != null && tenantSpecificExecutionPlanFiles.size() > 0) {
                        Collection<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId).values();

                        if (executionPlanConfigurationFiles != null) {
                            Iterator<ExecutionPlanConfigurationFile> executionPlanConfigurationFileIterator = executionPlanConfigurationFiles.iterator();
                            while (executionPlanConfigurationFileIterator.hasNext()) {
                                ExecutionPlanConfigurationFile executionPlanConfigurationFile = executionPlanConfigurationFileIterator.next();
                                if ((executionPlanConfigurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && streamWithVersion.equalsIgnoreCase(executionPlanConfigurationFile.getDependency())) {
                                    ExecutionPlanConfiguration executionPlanConfiguration = failedTenantSpecificExecutionPlanConfigurations.get(tenantId).get(executionPlanConfigurationFile.getExecutionPlanName());
                                    // filtering out erroneus ones.
                                    if (executionPlanConfiguration != null) {
                                        try {
                                            boolean success = addExecutionPlanConfiguration(executionPlanConfiguration, tenantId);
                                            if (success) {
                                                // need to recursively retry the dependents of these plans.
                                                successfulRedeployments.add(executionPlanConfiguration);
                                            }
                                        } catch (ExecutionPlanDependencyValidationException e) {
                                            updateConfigurationFileStatus(tenantId, executionPlanConfiguration.getName(), e.getDependency());
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error("Still unable to redeploy execution plans for the stream:" + streamConfig.getStreamId(), ex);
                }
            }
        }

        // recursive dependency resolving.
        for (ExecutionPlanConfiguration newExecutionPlan : successfulRedeployments) {
            redeployWaitingDependents(tenantId, newExecutionPlan);
        }
    }


    private void addToExecutionPlans(int tenantId,
                                     Map<Integer, Map<String, ExecutionPlanConfiguration>> tenantSpecificExecutionPlanConfigurations,
                                     ExecutionPlanConfiguration executionPlanConfiguration) {
        Map<String, ExecutionPlanConfiguration> executionPlanMap = tenantSpecificExecutionPlanConfigurations.get(tenantId);
        if (executionPlanMap == null) {
            executionPlanMap = new ConcurrentHashMap<String, ExecutionPlanConfiguration>();
            tenantSpecificExecutionPlanConfigurations.put(tenantId, executionPlanMap);
        }
        executionPlanMap.put(executionPlanConfiguration.getName(), executionPlanConfiguration);
    }

    /**
     * gets the relevant stream definition if already available with a junction.
     *
     * @param tenantId
     * @param streamWithVersion format-  streamName:version
     * @return
     */
    public StreamDefinition getStreamDefinition(int tenantId, String streamWithVersion) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction junction = eventJunctionMap.get(streamWithVersion);
            if (junction != null) {
                return junction.getStreamDefinition();
            }
        }
        return null;
    }

    private void removeFromExecutionPlans(int tenantId,
                                          Map<Integer, Map<String, ExecutionPlanConfiguration>> tenantSpecificExecutionPlanConfigurations,
                                          ExecutionPlanConfiguration executionPlanConfiguration) {
        Map<String, ExecutionPlanConfiguration> queryPlanMap = tenantSpecificExecutionPlanConfigurations.get(tenantId);
        if (queryPlanMap != null) {
            queryPlanMap.remove(executionPlanConfiguration.getName());
        }
    }

    @Override
    public boolean removeExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            int tenantId) {
        Map<String, ExecutionPlanConfiguration> executionPlanMap = tenantSpecificExecutionPlanConfigurations.get(tenantId);
        if (executionPlanConfiguration != null && executionPlanMap != null && executionPlanMap.containsKey(executionPlanConfiguration.getName())) {
            executionPlanMap.remove(executionPlanConfiguration.getName());
            // todo cleanup listeners when query plan destroys

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
            // notifying formatters
            for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getExportedStreams()) {
                EventJunction junction = getEventJunction(tenantId, streamConfiguration.getStreamId());
                if (junction != null) {
                    for (EventProducer producer : junction.getAllEventProducers()) {
                        if (producer.getOwner() == executionPlanConfiguration) {
                            junction.removeProducer(producer);
                        }
                    }

                    if (junction.getAllEventProducers().size() == 0) {
                        unDeployExecutionPlansForInputStream(tenantId, streamConfiguration.getStreamId());
                    }
                }
            }
            Map<String, ExecutionPlan> executionPlans = tenantSpecificExecutionPlans.get(tenantId);
            if (executionPlans != null) {
                ExecutionPlan plan = executionPlans.get(executionPlanConfiguration.getName());
                if (plan != null) {
                    plan.getSiddhiManager().shutdown();
                    executionPlans.remove(executionPlanConfiguration.getName());
                }
            }
            log.info("Execution plan '" + executionPlanConfiguration.getName() + "' was undeployed successfully.");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ExecutionPlanConfiguration getExecutionPlanConfiguration(String name,
                                                                    int tenantId) {
        Map<String, ExecutionPlanConfiguration> executionPlanMap = tenantSpecificExecutionPlanConfigurations.get(tenantId);
        return executionPlanMap.get(name);
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllExecutionPlanConfigurations(
            int tenantId) {
        Map<String, ExecutionPlanConfiguration> executionPlanMap = tenantSpecificExecutionPlanConfigurations.get(tenantId);
        return executionPlanMap;
    }

    public void addExecutionPlanConfigurationFile(ExecutionPlanConfigurationFile configurationFile,
                                                  int tenantId) {
        Map<String, ExecutionPlanConfigurationFile> executionPlanFileMap = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanFileMap == null) {
            executionPlanFileMap = new ConcurrentHashMap<String, ExecutionPlanConfigurationFile>();
            tenantSpecificExecutionPlanFiles.put(tenantId, executionPlanFileMap);
        }
        executionPlanFileMap.put(configurationFile.getPath(), configurationFile);
    }

    /**
     * Just removes the configuration file. Doesn't remove the corresponding execution plan.
     *
     * @param executionPlanConfigurationFile
     * @param tenantId
     */
    public void removeExecutionPlanConfigurationFile(
            ExecutionPlanConfigurationFile executionPlanConfigurationFile,
            int tenantId) {
        Map<String, ExecutionPlanConfigurationFile> executionPlanFileMap = tenantSpecificExecutionPlanFiles.get(tenantId);
        executionPlanFileMap.remove(executionPlanConfigurationFile.getPath());
    }

    public List<ExecutionPlanConfigurationFile> getAllExecutionPlanConfigurationFiles(
            int tenantId) {
        Map<String, ExecutionPlanConfigurationFile> tenantsFiles = this.tenantSpecificExecutionPlanFiles.get(tenantId);
        if (tenantsFiles != null) {
            List<ExecutionPlanConfigurationFile> files = new ArrayList<ExecutionPlanConfigurationFile>(tenantsFiles.size());
            files.addAll(tenantsFiles.values());
            return files;
        }
        return new ArrayList<ExecutionPlanConfigurationFile>();
    }

    public List<String> getAllExecutionPlanConfigurationFileNames(int tenantId) {
        Map<String, ExecutionPlanConfigurationFile> tenantsFiles = this.tenantSpecificExecutionPlanFiles.get(tenantId);
        if (tenantsFiles != null) {
            List<String> files = new ArrayList<String>(tenantsFiles.size());
            files.addAll(tenantsFiles.keySet());
            return files;
        }
        return new ArrayList<String>();
    }

    public List<ExecutionPlanConfigurationFile> getFailedExecutionPlanConfigurationFiles(
            int tenantId) {
        Map<String, ExecutionPlanConfigurationFile> tenantsFiles = this.tenantSpecificExecutionPlanFiles.get(tenantId);

        if (tenantsFiles != null) {
            List<ExecutionPlanConfigurationFile> files = new ArrayList<ExecutionPlanConfigurationFile>();
            for (ExecutionPlanConfigurationFile configFile : tenantsFiles.values()) {
                if (configFile.getStatus() == ExecutionPlanConfigurationFile.Status.ERROR || configFile.getStatus() == ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY) {
                    files.add(configFile);
                }
            }
            return files;
        }
        return new ArrayList<ExecutionPlanConfigurationFile>();
    }


    public ExecutionPlanConfigurationFile getExecutionPlanConfigurationFile(String path,
                                                                            AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, ExecutionPlanConfigurationFile> executionPlanFileMap = tenantSpecificExecutionPlanFiles.get(tenantId);
        return executionPlanFileMap.get(path);
    }

    public boolean addEventFormatterListener(String streamId, EventFormatterListener listener,
                                             int tenantId) {
        Map<String, EventJunction> junctionMap = this.tenantSpecificEventJunctions.get(tenantId);
        if (junctionMap == null) {
            junctionMap = new ConcurrentHashMap<String, EventJunction>();
            this.tenantSpecificEventJunctions.put(tenantId, junctionMap);
        }
        EventJunction junction = junctionMap.get(streamId);
        if (junction != null) {
            return junction.addConsumer(new EventFormatterConsumerImpl(listener, listener));
        }
        return false;
    }

    public boolean removeEventFormatterListener(String streamId, EventFormatterListener listener,
                                                int tenantId) {
        Map<String, EventJunction> junctionMap = this.tenantSpecificEventJunctions.get(tenantId);
        if (junctionMap == null) {
            return false;
        }
        EventJunction junction = junctionMap.get(streamId);
        if (junction == null) {
            return false;
        }
        for (EventConsumer consumer : junction.getAllEventConsumers()) {
            if (consumer.getOwner() == listener) {
                junction.removeConsumer(consumer);
                return false;
            }
        }
        return false;
    }

    public StreamDefinition getStreamDefinition(String name, String version, int tenantId) {
        return getStreamDefinition(tenantId, EventProcessorUtil.getStreamId(name, version));
    }


    /**
     * retries to deploy failed buckets.
     *
     * @param tenantId
     * @param streamWithVersion
     */
    public void onNewEventBuilderDeployment(int tenantId, String streamWithVersion)
            throws EventBuilderConfigurationException, ExecutionPlanConfigurationException {

        EventJunction junction = getEventJunction(tenantId, streamWithVersion);
        StreamDefinition streamDefinition = getStreamDefinitionFromBuilder(tenantId, streamWithVersion);
        if (junction == null) {
            junction = createEventJunctionWithoutSubscriptions(tenantId, streamDefinition);

        }
        subscribeToEventBuilder(tenantId, junction);
        if (EventProcessorValueHolder.getNotificationListener() != null) {
            EventProcessorValueHolder.getNotificationListener().addedNewEventStream(tenantId, streamWithVersion);
        }

        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();

        if (tenantSpecificExecutionPlanFiles != null && tenantSpecificExecutionPlanFiles.size() > 0) {
            Collection<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId).values();

            if (executionPlanConfigurationFiles != null) {
                Iterator<ExecutionPlanConfigurationFile> executionPlanConfigurationFileIterator = executionPlanConfigurationFiles.iterator();
                while (executionPlanConfigurationFileIterator.hasNext()) {
                    ExecutionPlanConfigurationFile executionPlanConfigurationFile = executionPlanConfigurationFileIterator.next();
                    if ((executionPlanConfigurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && streamWithVersion.equalsIgnoreCase(executionPlanConfigurationFile.getDependency())) {
                        ExecutionPlanConfiguration executionPlanConfiguration = failedTenantSpecificExecutionPlanConfigurations.get(tenantId).get(executionPlanConfigurationFile.getExecutionPlanName());
                        if (executionPlanConfiguration != null) {
                            try {
                                addExecutionPlanConfiguration(executionPlanConfiguration, tenantId);
                            } catch (ExecutionPlanDependencyValidationException e) {
                                updateConfigurationFileStatus(tenantId, executionPlanConfiguration.getName(), e.getDependency());
                            }
                        }
                    }
                }
            }
        }
    }

    // gets file by name.
    public ExecutionPlanConfigurationFile getExecutionPlanConfigurationFileByName(String name,
                                                                                  int tenantId) {
        Map<String, ExecutionPlanConfigurationFile> configurationFileMap = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (configurationFileMap != null) {
            for (ExecutionPlanConfigurationFile file : configurationFileMap.values()) {
                if (name.equals(file.getExecutionPlanName())) {
                    return file;
                }
            }
        }
        return null;
    }

    public ExecutionPlanConfigurationFile getExecutionPlanConfigurationFile(String path,
                                                                            int tenantId) {
        Map<String, ExecutionPlanConfigurationFile> configurationFileMap = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (configurationFileMap != null) {
            return configurationFileMap.get(path);

        }
        return null;
    }


    public void unDeployExecutionPlansForInputStream(int tenantId, String streamWithVersion)
            throws EventBuilderConfigurationException {

        EventJunction junction = getEventJunction(tenantId, streamWithVersion);

        if (junction != null) {
            int producers = junction.getAllEventProducers().size();
            // if more than 1 producers available, still need to keep the junction.
            if (producers <= 1) {
                if (producers == 0 || junction.getAllEventProducers().get(0) instanceof BasicEventListenerImpl) {

                    // junction has no active producers. need to be removed.
                    for (EventConsumer consumer : junction.getAllEventConsumers()) {
                        if (consumer instanceof SiddhiInputEventDispatcher) {
                            ExecutionPlanConfiguration owner = (ExecutionPlanConfiguration) consumer.getOwner();
                            removeExecutionPlanConfiguration(owner, tenantId);
                            // adding to failed plans.
                            addToExecutionPlans(tenantId, failedTenantSpecificExecutionPlanConfigurations, owner);
                            updateConfigurationFileStatus(tenantId, owner.getName(), streamWithVersion);
                        }
                    }
                    EventJunction removedJunction = this.tenantSpecificEventJunctions.get(tenantId).remove(streamWithVersion);

                    if (removedJunction != null) {
                        if (producers > 0) {
                            // only one producer exists
                            EventProcessorValueHolder.getEventBuilderService().unsubsribe(junction.getStreamDefinition(), (BasicEventListenerImpl) junction.getAllEventProducers().get(0), tenantId);
                        }
                        if (EventProcessorValueHolder.getNotificationListener() != null) {
                            EventProcessorValueHolder.getNotificationListener().removeEventStream(tenantId, streamWithVersion);
                        }
                    }
                }
            }
        }
    }

    private void updateConfigurationFileStatus(int tenantId, String name, String dependency) {
        ExecutionPlanConfigurationFile configurationFile = getExecutionPlanConfigurationFileByName(name, tenantId);
        if (configurationFile != null) {
            configurationFile.setDependency(dependency);
            configurationFile.setStatus(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY);
        }
    }

    public void redeployFailedExecutionPlans(int tenantId)
            throws ExecutionPlanConfigurationException {
        Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap = this.failedTenantSpecificExecutionPlanConfigurations.get(tenantId);
        Map<String, ExecutionPlanConfigurationFile> executionPlanConfigurationFileMap = this.tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationMap != null) {
            for (Map.Entry<String, ExecutionPlanConfiguration> executionPlanConfigurationEntry : executionPlanConfigurationMap.entrySet()) {
                ExecutionPlanConfiguration executionPlanConfiguration = executionPlanConfigurationEntry.getValue();
                ExecutionPlanConfigurationFile executionPlanConfigurationFile = executionPlanConfigurationFileMap.get(executionPlanConfiguration.getName());
                try {
                    addExecutionPlanConfiguration(executionPlanConfiguration, PrivilegedCarbonContext.getCurrentContext(executionPlanConfigurationFile.getAxisConfiguration()).getTenantId());
                } catch (ExecutionPlanDependencyValidationException e) {
                    updateConfigurationFileStatus(tenantId, executionPlanConfiguration.getName(), e.getDependency());
                }
            }
        }
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


    public void subscribeToEventBuilder(int tenantId, EventJunction junction) {

        BasicEventListenerImpl builderListener = new BasicEventListenerImpl(junction, null);
        junction.addProducer(builderListener);
        EventProcessorValueHolder.getEventBuilderService().subscribe(junction.getStreamDefinition(), builderListener, tenantId);
        junction.setSubscribedToBuilder(true);
    }

    public EventJunction createEventJunctionWithoutSubscriptions(int tenantId,
                                                                 StreamDefinition streamDefinition) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }
        EventJunction junction = new EventJunction(streamDefinition);
        eventJunctionMap.put(streamDefinition.getStreamId(), junction);
        return junction;
    }

    public StreamDefinition getStreamDefinitionFromBuilder(int tenantId, String streamWithVersion) {
        EventBuilderService eventBuilderService = EventProcessorValueHolder.getEventBuilderService();
        List<StreamDefinition> definitions = eventBuilderService.getStreamDefinitions(tenantId);
        for (StreamDefinition definition : definitions) {
            if (streamWithVersion.equals(definition.getStreamId())) {
                return definition;
            }
        }
        return null;
    }

    public EventJunction getEventJunction(int tenantId, String streamWithVersion) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            return eventJunctionMap.get(streamWithVersion);
        }
        return null;
    }

    public void notifyFormatter() {
        for (Map.Entry<Integer, Map<String, EventJunction>> entry : tenantSpecificEventJunctions.entrySet()) {
            Map<String, EventJunction> junctions = entry.getValue();
            for (Map.Entry<String, EventJunction> junctionEntry : junctions.entrySet()) {
                EventProcessorValueHolder.getNotificationListener().addedNewEventStream(entry.getKey(), junctionEntry.getValue().getStreamDefinition().getStreamId());
            }
        }
    }

    public List<String> getStreamNames(int tenantId) {
        if (tenantSpecificEventJunctions.get(tenantId) != null) {
            return new ArrayList<String>(tenantSpecificEventJunctions.get(tenantId).keySet());
        }
        return new ArrayList<String>(0);
    }

    public void saveExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        String executoinPlanName = executionPlanConfiguration.getName();
        OMElement omElement = EventProcessorConfigurationHelper.toOM(executionPlanConfiguration);

        File directory = new File(axisConfiguration.getRepository().getPath());
        if (!directory.exists()) {
            if (directory.mkdir()) {
                throw new ExecutionPlanConfigurationException("Cannot create directory to add tenant specific exec. plan :" + executoinPlanName);
            }
        }

        directory = new File(directory.getAbsolutePath() + File.separator + EventProcessorConstants.EP_ELE_DIRECTORY);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                throw new ExecutionPlanConfigurationException("Cannot create directory to add tenant specific exec. plan :" + executoinPlanName);
            }
        }
        String pathInFileSystem = directory.getAbsolutePath() + File.separator + executoinPlanName + ".xml";
        EventProcessorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, executoinPlanName, pathInFileSystem, axisConfiguration);
    }

    public String getExecutionPlanConfigurationFileContent(int tenantId, String name)
            throws ExecutionPlanConfigurationException {
        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByName(name, tenantId);
        if (configFile == null) {
            throw new ExecutionPlanConfigurationException("Configuration file doesn't exist.");
        }
        return readExecutionPlanConfigFile(configFile.getPath());
    }

    public String getNotDeployedExecutionPlanConfigurationFileContent(int tenantId, String path)
            throws ExecutionPlanConfigurationException {
        return readExecutionPlanConfigFile(path);
    }

    public void editExecutionPlanConfigurationFile(String executionPlanConfiguration,
                                                   String executionPlanName,
                                                   AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        try {
            OMElement omElement = AXIOMUtil.stringToOM(executionPlanConfiguration);
            ExecutionPlanConfiguration config = EventProcessorConfigurationHelper.fromOM(omElement);
            boolean isValid = EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(config);
            if (isValid && executionPlanName != null && executionPlanName.length() > 0) {
                saveEditedExecutionPlanConfiguration(executionPlanName, axisConfiguration, omElement);
            } else {
                throw new ExecutionPlanConfigurationException("Invalid configuration provided.");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new ExecutionPlanConfigurationException("Not a valid xml object, ", e);
        }
    }

    public void editNotDeployedExecutionPlanConfigurationFile(String executionPlanConfiguration,
                                                              String filePath,
                                                              AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(executionPlanConfiguration);
            EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(omElement);
            ExecutionPlanConfiguration config = EventProcessorConfigurationHelper.fromOM(omElement);
            EventProcessorConfigurationFilesystemInvoker.delete(filePath, axisConfiguration);
            EventProcessorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, config.getName(), filePath, axisConfiguration);
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new ExecutionPlanConfigurationException("Not a valid xml object ", e);
        }
    }


    private void saveEditedExecutionPlanConfiguration(String executionPlanName,
                                                      AxisConfiguration axisConfiguration,
                                                      OMElement omElement)
            throws ExecutionPlanConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        ExecutionPlanConfigurationFile configurationFile = getExecutionPlanConfigurationFileByName(executionPlanName, tenantId);
        if (configurationFile == null) {
            throw new ExecutionPlanConfigurationException("Edited file does not exist.");
        }
        String pathInFileSystem = configurationFile.getPath();
        EventProcessorConfigurationFilesystemInvoker.delete(pathInFileSystem, axisConfiguration);
        EventProcessorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, executionPlanName, pathInFileSystem, axisConfiguration);
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
                throw new ExecutionPlanConfigurationException("Error occurred when reading the file ", e);
            }
        }
        return stringBuffer.toString().trim();
    }

    @Override
    public void setTracingEnabled(String executionPlanName, boolean isEnabled,
                                  AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        //todo write to config file

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        ExecutionPlanConfiguration executionPlanConfiguration = getExecutionPlanConfiguration(executionPlanName, tenantId);
        executionPlanConfiguration.setTracingEnabled(isEnabled);
        editTracingStatistics(executionPlanConfiguration, executionPlanName, tenantId, axisConfiguration);
    }

    private void editTracingStatistics(
            ExecutionPlanConfiguration executionPlanConfiguration,
            String executionPlanName, int tenantId, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByName(executionPlanName, tenantId);
        String pathInFileSystem = configFile.getPath();
        removeExecutionPlanConfigurationFile(executionPlanName, axisConfiguration);
        OMElement omElement = EventProcessorConfigurationHelper.toOM(executionPlanConfiguration);
        EventProcessorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, executionPlanName, pathInFileSystem, axisConfiguration);
    }

    public void removeExecutionPlanConfigurationFile(String executionPlanName,
                                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByName(executionPlanName, tenantId);
        EventProcessorConfigurationFilesystemInvoker.delete(configFile.getPath(), axisConfiguration);
    }

    @Override
    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled,
                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

    }

    @Override
    public boolean isTracingEnabled(String executionPlanName, int tenantId)
            throws ExecutionPlanConfigurationException {
        ExecutionPlanConfiguration config = getExecutionPlanConfiguration(executionPlanName, tenantId);
        if (config == null) {
            throw new ExecutionPlanConfigurationException("Execution plan doesn't exist");
        }
        return config.isTracingEnabled();
    }

    @Override
    public boolean isStatisticsEnabled(String executionPlanName, int tenantId)
            throws ExecutionPlanConfigurationException {
        ExecutionPlanConfiguration config = getExecutionPlanConfiguration(executionPlanName, tenantId);
        if (config == null) {
            throw new ExecutionPlanConfigurationException("Execution plan doesn't exist");
        }
        return config.isStatisticsEnabled();
    }
}
