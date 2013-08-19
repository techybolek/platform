/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.output.transport.adaptor.manager.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorFile;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorInfo;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorManagerService;
import org.wso2.carbon.output.transport.adaptor.manager.core.OutputTransportAdaptorNotificationListener;
import org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.ds.OutputTransportAdaptorManagerValueHolder;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.util.OutputTransportAdaptorManagerConstants;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.util.helper.OutputTransportAdaptorConfigurationFilesystemInvoker;
import org.wso2.carbon.output.transport.adaptor.manager.core.internal.util.helper.OutputTransportAdaptorConfigurationHelper;

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
import java.util.concurrent.ConcurrentHashMap;

/**
 * carbon implementation of the transport adaptor manager.
 */
public class CarbonOutputTransportAdaptorManagerService
        implements OutputTransportAdaptorManagerService {
    private static final Log log = LogFactory.getLog(CarbonOutputTransportAdaptorManagerService.class);
    private static final String OUTPUT_TRANSPORT_ADAPTOR = "Output Transport Adaptor";

    /**
     * transport configuration map to keep the transport configuration details
     */

    //List that holds all the notification listeners
    public List<OutputTransportAdaptorNotificationListener> outputTransportAdaptorNotificationListener;
    //Holds all the output transport adaptor configuration objects
    private Map<Integer, Map<String, OutputTransportAdaptorConfiguration>> tenantSpecificTransportAdaptorConfigurationMap;
    //Holds the minimum information about the output transport adaptors
    private Map<Integer, Map<String, OutputTransportAdaptorInfo>> tenantSpecificOutputTransportAdaptorInfoMap;
    //Holds all the output transport adaptor file info
    private Map<Integer, List<OutputTransportAdaptorFile>> transportAdaptorFileMap;

    public CarbonOutputTransportAdaptorManagerService() {
        tenantSpecificTransportAdaptorConfigurationMap = new ConcurrentHashMap<Integer, Map<String, OutputTransportAdaptorConfiguration>>();
        transportAdaptorFileMap = new ConcurrentHashMap<Integer, List<OutputTransportAdaptorFile>>();
        tenantSpecificOutputTransportAdaptorInfoMap = new HashMap<Integer, Map<String, OutputTransportAdaptorInfo>>();
        outputTransportAdaptorNotificationListener = new ArrayList<OutputTransportAdaptorNotificationListener>();
    }

    @Override
    public void deployOutputTransportAdaptorConfiguration(
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        String transportName = transportAdaptorConfiguration.getName();
        OMElement omElement = OutputTransportAdaptorConfigurationHelper.transportAdaptorConfigurationToOM(transportAdaptorConfiguration);

        if (OutputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(OutputTransportAdaptorConfigurationHelper.fromOM(omElement))) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new OutputTransportAdaptorManagerConfigurationException("Cannot create directory to add tenant specific Output Transport Adaptor :" + transportName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + OutputTransportAdaptorManagerConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new OutputTransportAdaptorManagerConfigurationException("Cannot create directory " + OutputTransportAdaptorManagerConstants.TM_ELE_DIRECTORY + " to add tenant specific Output Transport Adaptor :" + transportName);
                }
            }
            String pathInFileSystem = directory.getAbsolutePath() + File.separator + transportName + ".xml";
            OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no Output Transport Adaptor type called " + transportAdaptorConfiguration.getType() + " is available");
            throw new OutputTransportAdaptorManagerConfigurationException("There is no Output Transport Adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }

    @Override
    public void undeployActiveOutputTransportAdaptorConfiguration(String transportAdaptorName,
                                                                  AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (outputTransportAdaptorFileList != null) {
            for (OutputTransportAdaptorFile outputTransportAdaptorFile : outputTransportAdaptorFileList) {

                if ((outputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                    String filePath = outputTransportAdaptorFile.getFilePath();
                    OutputTransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
                    break;
                }
            }
        }
    }

    @Override
    public List<OutputTransportAdaptorConfiguration> getAllActiveOutputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        List<OutputTransportAdaptorConfiguration> transportAdaptorConfigurations = new ArrayList<OutputTransportAdaptorConfiguration>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) != null) {
            for (OutputTransportAdaptorConfiguration transportAdaptorConfiguration : tenantSpecificTransportAdaptorConfigurationMap.get(
                    tenantId).values()) {
                transportAdaptorConfigurations.add(transportAdaptorConfiguration);
            }
        }
        return transportAdaptorConfigurations;
    }


    @Override
    public OutputTransportAdaptorConfiguration getActiveOutputTransportAdaptorConfiguration(
            String name,
            int tenantId)
            throws OutputTransportAdaptorManagerConfigurationException {

        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) == null) {
            throw new OutputTransportAdaptorManagerConfigurationException("There is no any configuration exists for " + tenantId);
        }
        return tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).get(name);
    }

    @Override
    public List<OutputTransportAdaptorFile> getAllInactiveOutputTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration) {

        List<OutputTransportAdaptorFile> unDeployedOutputTransportAdaptorFileList = new ArrayList<OutputTransportAdaptorFile>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (transportAdaptorFileMap.get(tenantId) != null) {
            for (OutputTransportAdaptorFile outputTransportAdaptorFile : transportAdaptorFileMap.get(tenantId)) {
                if (!outputTransportAdaptorFile.getStatus().equals(OutputTransportAdaptorFile.Status.DEPLOYED)) {
                    unDeployedOutputTransportAdaptorFileList.add(outputTransportAdaptorFile);
                }
            }
        }
        return unDeployedOutputTransportAdaptorFileList;
    }

    @Override
    public void undeployInactiveOutputTransportAdaptorConfiguration(String filePath,
                                                                    AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        OutputTransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
    }

    @Override
    public void editActiveOutputTransportAdaptorConfiguration(String transportAdaptorConfiguration,
                                                              String transportAdaptorName,
                                                              AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        try {
            OMElement omElement = AXIOMUtil.stringToOM(transportAdaptorConfiguration);
            omElement.toString();
            OutputTransportAdaptorConfiguration transportAdaptorConfigurationObject = OutputTransportAdaptorConfigurationHelper.fromOM(omElement);
            if (!transportAdaptorConfigurationObject.getName().equals(transportAdaptorName)) {
                if (checkAdaptorValidity(tenantId, transportAdaptorConfigurationObject.getName())) {
                    validateTransportAdaptorConfiguration(tenantId, transportAdaptorName, axisConfiguration, omElement);
                } else {
                    throw new OutputTransportAdaptorManagerConfigurationException("There is a Output Transport Adaptor already registered with the same name");
                }
            } else {
                validateTransportAdaptorConfiguration(tenantId, transportAdaptorName, axisConfiguration, omElement);
            }

        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new OutputTransportAdaptorManagerConfigurationException("Not a valid xml object, " + e.getMessage(), e);
        }
    }

    @Override
    public void editInactiveOutputTransportAdaptorConfiguration(
            String transportAdaptorConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(transportAdaptorConfiguration);
            omElement.toString();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
            OutputTransportAdaptorConfiguration transportAdaptorConfigurationObject = OutputTransportAdaptorConfigurationHelper.fromOM(omElement);
            if (checkAdaptorValidity(tenantId, transportAdaptorConfigurationObject.getName())) {
                if (OutputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(transportAdaptorConfigurationObject)) {
                    undeployInactiveOutputTransportAdaptorConfiguration(filePath, axisConfiguration);
                    OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorConfigurationObject.getName(), filePath, axisConfiguration);
                } else {
                    log.error("There is no Output Transport Adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available");
                    throw new OutputTransportAdaptorManagerConfigurationException("There is no Output Transport Adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available ");
                }
            } else {
                throw new OutputTransportAdaptorManagerConfigurationException("There is a Output Transport Adaptor with the same name");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new OutputTransportAdaptorManagerConfigurationException("Not a valid xml object " + e.getMessage(), e);
        }
    }

    @Override
    public String getActiveOutputTransportAdaptorConfigurationContent(String transportAdaptorName,
                                                                      AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        String filePath = "";
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (outputTransportAdaptorFileList != null) {
            for (OutputTransportAdaptorFile outputTransportAdaptorFile : outputTransportAdaptorFileList) {
                if ((outputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {
                    filePath = outputTransportAdaptorFile.getFilePath();
                }
            }
        }
        return readTransportAdaptorConfigurationFile(filePath);
    }

    @Override
    public String getInactiveOutputTransportAdaptorConfigurationContent(String filePath)
            throws OutputTransportAdaptorManagerConfigurationException {

        return readTransportAdaptorConfigurationFile(filePath);
    }

    @Override
    public void setStatisticsEnabled(String transportAdaptorName,
                                     AxisConfiguration axisConfiguration, boolean flag)
            throws OutputTransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = getActiveOutputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        outputTransportAdaptorConfiguration.setEnableStatistics(flag);
        editTracingStatistics(outputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void setTracingEnabled(String transportAdaptorName, AxisConfiguration axisConfiguration,
                                  boolean flag)
            throws OutputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = getActiveOutputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        outputTransportAdaptorConfiguration.setEnableTracing(flag);
        editTracingStatistics(outputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void registerDeploymentNotifier(
            OutputTransportAdaptorNotificationListener outputTransportAdaptorNotificationListener)
            throws OutputTransportAdaptorManagerConfigurationException {
        this.outputTransportAdaptorNotificationListener.add(outputTransportAdaptorNotificationListener);

        notifyActiveTransportAdaptorConfigurationFiles();
    }


    @Override
    public List<OutputTransportAdaptorInfo> getOutputTransportAdaptorInfo(int tenantId) {

        Map<String, OutputTransportAdaptorInfo> outputTransportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);
        if (outputTransportAdaptorInfoMap != null) {
            List<OutputTransportAdaptorInfo> outputTransportAdaptorInfoList = new ArrayList<OutputTransportAdaptorInfo>();
            for (OutputTransportAdaptorInfo outputTransportAdaptorInfo : outputTransportAdaptorInfoMap.values()) {
                outputTransportAdaptorInfoList.add(outputTransportAdaptorInfo);
            }

            return outputTransportAdaptorInfoList;
        }
        return null;
    }

    //Non-Interface public methods below

    /**
     * This method is used to store all the deployed and non-deployed transport adaptors in a map
     * (A flag used to uniquely identity whether the transport adaptor deployed or not
     */
    public void addOutputTransportAdaptorConfigurationFile(int tenantId,
                                                           OutputTransportAdaptorFile outputTransportAdaptorFile) {

        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);

        if (outputTransportAdaptorFileList == null) {
            outputTransportAdaptorFileList = new ArrayList<OutputTransportAdaptorFile>();
        } else {
            for (OutputTransportAdaptorFile anOutputTransportAdaptorFileList : outputTransportAdaptorFileList) {
                if (anOutputTransportAdaptorFileList.getFilePath().equals(outputTransportAdaptorFile.getFilePath())) {
                    return;
                }
            }
        }
        outputTransportAdaptorFileList.add(outputTransportAdaptorFile);
        transportAdaptorFileMap.put(tenantId, outputTransportAdaptorFileList);


    }

    /**
     * To check whether there is a transport adaptor with the same name
     */
    public boolean checkAdaptorValidity(int tenantId, String transportAdaptorName) {

        if (transportAdaptorFileMap.size() > 0) {
            List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            if (outputTransportAdaptorFileList != null) {
                for (OutputTransportAdaptorFile outputTransportAdaptorFile : outputTransportAdaptorFileList) {
                    if ((outputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName)) && (outputTransportAdaptorFile.getStatus().equals(OutputTransportAdaptorFile.Status.DEPLOYED))) {
                        log.error("Transport adaptor " + transportAdaptorName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * to remove the deployed and not-deployed the transport adaptor configuration from the map.
     */
    public void removeOutputTransportAdaptorConfiguration(String filePath, int tenantId) {
        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (outputTransportAdaptorFileList != null) {
            for (OutputTransportAdaptorFile outputTransportAdaptorFile : outputTransportAdaptorFileList) {
                if ((outputTransportAdaptorFile.getFilePath().equals(filePath))) {
                    if (outputTransportAdaptorFile.getStatus().equals(OutputTransportAdaptorFile.Status.DEPLOYED)) {
                        String transportAdaptorName = outputTransportAdaptorFile.getTransportAdaptorName();
                        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) != null) {
                            tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).remove(transportAdaptorName);
                        }

                        removeFromTenantSpecificTransportAdaptorInfoMap(tenantId, transportAdaptorName);
                        Iterator<OutputTransportAdaptorNotificationListener> deploymentListenerIterator = outputTransportAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            OutputTransportAdaptorNotificationListener outputTransportAdaptorNotificationListener = deploymentListenerIterator.next();
                            outputTransportAdaptorNotificationListener.configurationRemoved(tenantId, transportAdaptorName);
                        }
                    }
                    outputTransportAdaptorFileList.remove(outputTransportAdaptorFile);
                    return;
                }
            }
        }
    }

    /**
     * to add to the tenant specific transport adaptor configuration map (only the correctly deployed transport adaptors)
     *
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void addOutputTransportAdaptorConfiguration(
            int tenantId, OutputTransportAdaptorConfiguration transportAdaptorConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {
        Map<String, OutputTransportAdaptorConfiguration> transportAdaptorConfigurationMap =
                tenantSpecificTransportAdaptorConfigurationMap.get(tenantId);

        if (transportAdaptorConfiguration.isEnableStatistics()) {
            OutputTransportAdaptorManagerValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, OUTPUT_TRANSPORT_ADAPTOR, transportAdaptorConfiguration.getName(), null);
        }
        if (transportAdaptorConfigurationMap == null) {
            transportAdaptorConfigurationMap = new ConcurrentHashMap<String, OutputTransportAdaptorConfiguration>();
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
            tenantSpecificTransportAdaptorConfigurationMap.put(tenantId, transportAdaptorConfigurationMap);
        } else {
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
        }
        addToTenantSpecificTransportAdaptorInfoMap(tenantId, transportAdaptorConfiguration);
    }


    public void activateInactiveOutputTransportAdaptorConfiguration()
            throws OutputTransportAdaptorManagerConfigurationException {

        List<OutputTransportAdaptorFile> outputTransportAdaptorFiles = new ArrayList<OutputTransportAdaptorFile>();

        if (transportAdaptorFileMap != null && transportAdaptorFileMap.size() > 0) {

            Iterator transportAdaptorEntryIterator = transportAdaptorFileMap.entrySet().iterator();
            while (transportAdaptorEntryIterator.hasNext()) {
                Map.Entry transportAdaptorEntry = (Map.Entry) transportAdaptorEntryIterator.next();
                Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = ((List<OutputTransportAdaptorFile>) transportAdaptorEntry.getValue()).iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    OutputTransportAdaptorFile outputTransportAdaptorFile = transportAdaptorFileIterator.next();
                    if (outputTransportAdaptorFile.getStatus().equals(OutputTransportAdaptorFile.Status.WAITING_FOR_DEPENDENCY)) {
                        outputTransportAdaptorFiles.add(outputTransportAdaptorFile);
                    }
                }
            }
        }

        for (OutputTransportAdaptorFile outputTransportAdaptorFile : outputTransportAdaptorFiles) {
            try {
                OutputTransportAdaptorConfigurationFilesystemInvoker.reload(outputTransportAdaptorFile.getFilePath(), outputTransportAdaptorFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Output Transport Adaptor configuration file : " + new File(outputTransportAdaptorFile.getFilePath()).getName());
            }
        }
    }

    public void notifyActiveTransportAdaptorConfigurationFiles()
            throws OutputTransportAdaptorManagerConfigurationException {

        if (transportAdaptorFileMap != null && transportAdaptorFileMap.size() > 0) {
            Iterator transportAdaptorEntryIterator = transportAdaptorFileMap.entrySet().iterator();
            while (transportAdaptorEntryIterator.hasNext()) {
                Map.Entry transportAdaptorEntry = (Map.Entry) transportAdaptorEntryIterator.next();
                Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = ((List<OutputTransportAdaptorFile>) transportAdaptorEntry.getValue()).iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    OutputTransportAdaptorFile outputTransportAdaptorFile = transportAdaptorFileIterator.next();
                    if (outputTransportAdaptorFile.getStatus().equals(OutputTransportAdaptorFile.Status.DEPLOYED)) {
                        int tenantId = PrivilegedCarbonContext.getCurrentContext(outputTransportAdaptorFile.getAxisConfiguration()).getTenantId();
                        Iterator<OutputTransportAdaptorNotificationListener> deploymentListenerIterator = outputTransportAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            OutputTransportAdaptorNotificationListener outputTransportAdaptorNotificationListener = deploymentListenerIterator.next();
                            outputTransportAdaptorNotificationListener.configurationAdded(tenantId, outputTransportAdaptorFile.getTransportAdaptorName());
                        }
                    }
                }
            }
        }
    }

    //Private methods are below
    private void editTracingStatistics(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
            String transportAdaptorName, int tenantId, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
        undeployActiveOutputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
        OMElement omElement = OutputTransportAdaptorConfigurationHelper.transportAdaptorConfigurationToOM(outputTransportAdaptorConfiguration);
        OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
    }

    private String readTransportAdaptorConfigurationFile(String filePath)
            throws OutputTransportAdaptorManagerConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new OutputTransportAdaptorManagerConfigurationException("Output Transport Adaptor file not found ", e);
        } catch (IOException e) {
            throw new OutputTransportAdaptorManagerConfigurationException("Cannot read the Output Transport Adaptor file ", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new OutputTransportAdaptorManagerConfigurationException("Error occurred when reading the file ", e);
            }
        }

        return stringBuilder.toString().trim();
    }

    /**
     * to add to the output and output adaptor maps that gives information which transport adaptors supports
     * for transport adaptor configuration
     *
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    private void addToTenantSpecificTransportAdaptorInfoMap(int tenantId,
                                                            OutputTransportAdaptorConfiguration transportAdaptorConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {


        if (transportAdaptorConfiguration.getOutputConfiguration() != null) {
            OutputTransportAdaptorInfo outputTransportAdaptorInfo = new OutputTransportAdaptorInfo();
            outputTransportAdaptorInfo.setTransportAdaptorName(transportAdaptorConfiguration.getName());
            outputTransportAdaptorInfo.setTransportAdaptorType(transportAdaptorConfiguration.getType());
            Map<String, OutputTransportAdaptorInfo> transportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);

            if (transportAdaptorInfoMap != null) {

                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), outputTransportAdaptorInfo);
            } else {
                transportAdaptorInfoMap = new HashMap<String, OutputTransportAdaptorInfo>();
                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), outputTransportAdaptorInfo);
            }
            tenantSpecificOutputTransportAdaptorInfoMap.put(tenantId, transportAdaptorInfoMap);
        }

    }

    /**
     * to remove the transport adaptor configuration when deployed from the map after un-deploy
     */
    private void removeFromTenantSpecificTransportAdaptorInfoMap(int tenantId,
                                                                 String transportAdaptorName) {

        Map<String, OutputTransportAdaptorInfo> outputTransportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);

        if (outputTransportAdaptorInfoMap != null && outputTransportAdaptorInfoMap.containsKey(transportAdaptorName)) {
            outputTransportAdaptorInfoMap.remove(transportAdaptorName);
        }
    }

    /**
     * to get the file path of a transport adaptor
     */
    private String getFilePath(int tenantId, String transportAdaptorName) {

        if (transportAdaptorFileMap.size() > 0) {
            List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            if (outputTransportAdaptorFileList != null) {
                for (OutputTransportAdaptorFile outputTransportAdaptorFile : outputTransportAdaptorFileList) {
                    if ((outputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {
                        return outputTransportAdaptorFile.getFilePath();
                    }
                }
            }
        }
        return null;
    }

    /**
     * this stores the transport adaptor configuration to the file system after validating the transport adaptor when doing editing
     *
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    private void validateTransportAdaptorConfiguration(int tenantId, String transportAdaptorName,
                                                       AxisConfiguration axisConfiguration,
                                                       OMElement omElement)
            throws OutputTransportAdaptorManagerConfigurationException {
        OutputTransportAdaptorConfiguration transportAdaptorConfiguration = OutputTransportAdaptorConfigurationHelper.fromOM(omElement);
        if (OutputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(transportAdaptorConfiguration)) {
            String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
            undeployActiveOutputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
            OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no Output Transport Adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
            throw new OutputTransportAdaptorManagerConfigurationException("There is no Output Transport Adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }

}