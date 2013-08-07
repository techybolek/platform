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

    /**
     * transport configuration map to keep the transport configuration details
     */

    public List<OutputTransportAdaptorNotificationListener> outputTransportAdaptorNotificationListener;
    private Map<Integer, Map<String, OutputTransportAdaptorConfiguration>> tenantSpecificTransportAdaptorConfigurationMap;
    private Map<Integer, Map<String, OutputTransportAdaptorInfo>> tenantSpecificOutputTransportAdaptorInfoMap;
    private Map<Integer, List<OutputTransportAdaptorFile>> transportAdaptorFileMap;

    public CarbonOutputTransportAdaptorManagerService() {
        tenantSpecificTransportAdaptorConfigurationMap = new ConcurrentHashMap<Integer, Map<String, OutputTransportAdaptorConfiguration>>();
        transportAdaptorFileMap = new ConcurrentHashMap<Integer, List<OutputTransportAdaptorFile>>();
        tenantSpecificOutputTransportAdaptorInfoMap = new HashMap<Integer, Map<String, OutputTransportAdaptorInfo>>();
        outputTransportAdaptorNotificationListener = new ArrayList<OutputTransportAdaptorNotificationListener>();
    }

    public void registerDeploymentNotifier(
            OutputTransportAdaptorNotificationListener outputTransportAdaptorNotificationListener)
            throws OutputTransportAdaptorManagerConfigurationException {
        this.outputTransportAdaptorNotificationListener.add(outputTransportAdaptorNotificationListener);

        notifyTransportAdaptorConfigurationFiles();
    }

    /**
     * This method is used to store all the deployed and non-deployed transport adaptors in a map
     * (A flag used to uniquely identity whether the transport adaptor deployed or not
     *
     * @param tenantId
     * @param transportAdaptorName
     * @param filePath
     * @param status
     */
    public void addFileConfiguration(int tenantId, String transportAdaptorName, String filePath,
                                     OutputTransportAdaptorFile.Status status,
                                     AxisConfiguration axisConfiguration,
                                     String deploymentStatusMessage, String dependency) {

        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);

        if (outputTransportAdaptorFileList == null) {
            outputTransportAdaptorFileList = new ArrayList<OutputTransportAdaptorFile>();
        } else {
            Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = outputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                if (transportAdaptorFileIterator.next().getFilePath().equals(filePath)) {
                    return;
                }
            }
        }

        OutputTransportAdaptorFile outputTransportAdaptorFile = new OutputTransportAdaptorFile();
        outputTransportAdaptorFile.setFilePath(filePath);
        outputTransportAdaptorFile.setTransportAdaptorName(transportAdaptorName);
        outputTransportAdaptorFile.setAxisConfiguration(axisConfiguration);
        outputTransportAdaptorFile.setDependency(dependency);
        outputTransportAdaptorFile.setDeploymentStatusMessage(deploymentStatusMessage);
        outputTransportAdaptorFile.setStatus(status);
        outputTransportAdaptorFileList.add(outputTransportAdaptorFile);
        transportAdaptorFileMap.put(tenantId, outputTransportAdaptorFileList);


    }

    @Override
    public void disableStatistics(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = getOutputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        outputTransportAdaptorConfiguration.setEnableStatistics(false);
        editTracingStatistics(outputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void enableStatistics(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = getOutputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        outputTransportAdaptorConfiguration.setEnableStatistics(true);
        editTracingStatistics(outputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void disableTracing(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = getOutputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        outputTransportAdaptorConfiguration.setEnableTracing(false);
        editTracingStatistics(outputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void enableTracing(String transportAdaptorName, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration = getOutputTransportAdaptorConfiguration(transportAdaptorName, tenantId);
        outputTransportAdaptorConfiguration.setEnableTracing(true);
        editTracingStatistics(outputTransportAdaptorConfiguration, transportAdaptorName, tenantId, axisConfiguration);
    }

    private void editTracingStatistics(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
            String transportAdaptorName, int tenantId, AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
        removeOutputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
        OMElement omElement = OutputTransportAdaptorConfigurationHelper.transportAdaptorConfigurationToOM(outputTransportAdaptorConfiguration);
        OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
    }


    public void editOutputTransportAdaptorConfigurationFile(String transportAdaptorConfiguration,
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
                    throw new OutputTransportAdaptorManagerConfigurationException("There is a transport adaptor already registered with the same name");
                }
            } else {
                validateTransportAdaptorConfiguration(tenantId, transportAdaptorName, axisConfiguration, omElement);
            }

        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new OutputTransportAdaptorManagerConfigurationException("Not a valid xml object, " + e.getMessage());
        }
    }

    public void editNotDeployedOutputTransportAdaptorConfigurationFile(
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
                    removeOutputTransportAdaptorConfigurationFile(filePath, axisConfiguration);
                    OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorConfigurationObject.getName(), filePath, axisConfiguration);
                } else {
                    log.error("There is no transport adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available");
                    throw new OutputTransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available ");
                }
            } else {
                throw new OutputTransportAdaptorManagerConfigurationException("There is a transport adaptor with the same name");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new OutputTransportAdaptorManagerConfigurationException("Not a valid xml object " + e.getMessage());
        }
    }


    public void saveOutputTransportAdaptorConfiguration(
            OutputTransportAdaptorConfiguration transportAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        String transportName = transportAdaptorConfiguration.getName();
        OMElement omElement = OutputTransportAdaptorConfigurationHelper.transportAdaptorConfigurationToOM(transportAdaptorConfiguration);

        if (OutputTransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(OutputTransportAdaptorConfigurationHelper.fromOM(omElement))) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new OutputTransportAdaptorManagerConfigurationException("Cannot create directory to add tenant specific transport adaptor :" + transportName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + OutputTransportAdaptorManagerConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new OutputTransportAdaptorManagerConfigurationException("Cannot create directory " + OutputTransportAdaptorManagerConstants.TM_ELE_DIRECTORY + " to add tenant specific transport adaptor :" + transportName);
                }
            }
            String pathInFileSystem = directory.getAbsolutePath() + File.separator + transportName + ".xml";
            OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available");
            throw new OutputTransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }


    public void removeOutputTransportAdaptorConfiguration(String transportAdaptorName,
                                                          AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (outputTransportAdaptorFileList != null) {
            Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = outputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {

                OutputTransportAdaptorFile outputTransportAdaptorFile = transportAdaptorFileIterator.next();
                if ((outputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                    String filePath = outputTransportAdaptorFile.getFilePath();
                    OutputTransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
                    break;
                }
            }
        }
    }

    public void removeOutputTransportAdaptorConfigurationFile(String filePath,
                                                              AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        OutputTransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
    }

    @Override
    public String getOutputTransportAdaptorConfigurationFile(String transportAdaptorName,
                                                             AxisConfiguration axisConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {

        String filePath = "";
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (outputTransportAdaptorFileList != null) {
            Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = outputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                OutputTransportAdaptorFile outputTransportAdaptorFile = transportAdaptorFileIterator.next();
                if ((outputTransportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                    filePath = outputTransportAdaptorFile.getFilePath();

                }
            }
        }
        return readTransportAdaptorConfigurationFile(filePath);
    }

    public String getNotDeployedOutputTransportAdaptorConfigurationFile(String filePath)
            throws OutputTransportAdaptorManagerConfigurationException {

        return readTransportAdaptorConfigurationFile(filePath);
    }

    private String readTransportAdaptorConfigurationFile(String filePath)
            throws OutputTransportAdaptorManagerConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new OutputTransportAdaptorManagerConfigurationException("Transport adaptor file not found ", e);
        } catch (IOException e) {
            throw new OutputTransportAdaptorManagerConfigurationException("Cannot read the transport adaptor file ", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new OutputTransportAdaptorManagerConfigurationException("Error occurred when reading the file ", e);
            }
        }

        return stringBuffer.toString().trim();
    }


    public List<OutputTransportAdaptorConfiguration> getAllOutputTransportAdaptorConfiguration(
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

    public List<OutputTransportAdaptorFile> getNotDeployedOutputTransportAdaptorConfigurationFiles(
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
    public OutputTransportAdaptorConfiguration getOutputTransportAdaptorConfiguration(String name,
                                                                                      int tenantId)
            throws OutputTransportAdaptorManagerConfigurationException {

        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) == null) {
            throw new OutputTransportAdaptorManagerConfigurationException("There is no any configuration exists for " + tenantId);
        }
        return tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).get(name);
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


    /**
     * to add to the output and output adaptor maps that gives information which transport adaptors supports
     * for transport adaptor configuration
     *
     * @param tenantId
     * @param transportAdaptorConfiguration
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
     *
     * @param tenantId
     * @param transportAdaptorName
     */
    private void removeFromTenantSpecificTransportAdaptorInfoMap(int tenantId,
                                                                 String transportAdaptorName) {

        Map<String, OutputTransportAdaptorInfo> outputTransportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);

        if (outputTransportAdaptorInfoMap != null && outputTransportAdaptorInfoMap.containsKey(transportAdaptorName)) {
            outputTransportAdaptorInfoMap.remove(transportAdaptorName);
        }
    }

    /**
     * To check whether there is a transport adaptor with the same name
     *
     * @param tenantId
     * @param transportAdaptorName
     * @return
     */
    public boolean checkAdaptorValidity(int tenantId, String transportAdaptorName) {

        if (transportAdaptorFileMap.size() > 0) {
            List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            if (outputTransportAdaptorFileList != null) {
                Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = outputTransportAdaptorFileList.iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    OutputTransportAdaptorFile outputTransportAdaptorFile = transportAdaptorFileIterator.next();
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
     * to get the file path of a transport adaptor
     *
     * @param tenantId
     * @param transportAdaptorName
     * @return
     */
    private String getFilePath(int tenantId, String transportAdaptorName) {

        if (transportAdaptorFileMap.size() > 0) {
            List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            if (outputTransportAdaptorFileList != null) {
                Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = outputTransportAdaptorFileList.iterator();
                while (transportAdaptorFileIterator.hasNext()) {
                    OutputTransportAdaptorFile outputTransportAdaptorFile = transportAdaptorFileIterator.next();
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
     * @param tenantId
     * @param transportAdaptorName
     * @param axisConfiguration
     * @param omElement
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
            removeOutputTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
            OutputTransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
            throw new OutputTransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }

    /**
     * to remove the deployed and not-deployed the transport adaptor configuration from the map.
     *
     * @param filePath
     * @param tenantId
     */
    public void removeTransportAdaptorConfigurationFromMap(String filePath, int tenantId) {
        List<OutputTransportAdaptorFile> outputTransportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        if (outputTransportAdaptorFileList != null) {
            Iterator<OutputTransportAdaptorFile> transportAdaptorFileIterator = outputTransportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                OutputTransportAdaptorFile outputTransportAdaptorFile = transportAdaptorFileIterator.next();
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
     * @param tenantId
     * @param transportAdaptorConfiguration
     * @throws org.wso2.carbon.output.transport.adaptor.manager.core.exception.OutputTransportAdaptorManagerConfigurationException
     *
     */
    public void addTransportAdaptorConfigurationForTenant(
            int tenantId, OutputTransportAdaptorConfiguration transportAdaptorConfiguration)
            throws OutputTransportAdaptorManagerConfigurationException {
        Map<String, OutputTransportAdaptorConfiguration> transportAdaptorConfigurationMap
                = tenantSpecificTransportAdaptorConfigurationMap.get(tenantId);

        if (transportAdaptorConfigurationMap == null) {
            transportAdaptorConfigurationMap = new ConcurrentHashMap<String, OutputTransportAdaptorConfiguration>();
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
            tenantSpecificTransportAdaptorConfigurationMap.put(tenantId, transportAdaptorConfigurationMap);
        } else {
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
        }
        addToTenantSpecificTransportAdaptorInfoMap(tenantId, transportAdaptorConfiguration);
    }


    public void tryReDeployingTransportAdaptorConfigurationFiles()
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
                        transportAdaptorFileIterator.remove();
                    }
                }
            }
        }

        for (int i = 0; i < outputTransportAdaptorFiles.size(); i++) {
            OutputTransportAdaptorFile outputTransportAdaptorFile = outputTransportAdaptorFiles.get(i);
            try {
                OutputTransportAdaptorConfigurationFilesystemInvoker.reload(outputTransportAdaptorFile.getFilePath(), outputTransportAdaptorFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Output Transport Adaptor configuration file : " + new File(outputTransportAdaptorFile.getFilePath()).getName());
            }
        }
    }

    public void notifyTransportAdaptorConfigurationFiles()
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


}