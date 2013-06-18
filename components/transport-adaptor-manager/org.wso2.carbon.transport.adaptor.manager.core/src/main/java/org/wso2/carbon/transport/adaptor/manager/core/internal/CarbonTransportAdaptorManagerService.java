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

package org.wso2.carbon.transport.adaptor.manager.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.EventBuilderDeploymentListener;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorDeployer;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorFile;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorInfo;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TransportAdaptorManagerConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.NotDeployedTransportAdaptorFile;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorManagerConstants;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.helper.TransportAdaptorConfigurationFilesystemInvoker;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.helper.TransportAdaptorConfigurationHelper;

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
public class CarbonTransportAdaptorManagerService implements TransportAdaptorManagerService {
    private static final Log log = LogFactory.getLog(CarbonTransportAdaptorManagerService.class);

    /**
     * transport configuration map to keep the transport configuration details
     */

    public EventBuilderDeploymentListener eventBuilderDeploymentListener;
    private Map<Integer, Map<String, TransportAdaptorConfiguration>> tenantSpecificTransportAdaptorConfigurationMap;
    private Map<Integer, Map<String, TransportAdaptorInfo>> tenantSpecificInputTransportAdaptorInfoMap;
    private Map<Integer, Map<String, TransportAdaptorInfo>> tenantSpecificOutputTransportAdaptorInfoMap;
    private Map<Integer, List<TransportAdaptorFile>> transportAdaptorFileMap;

    public List<NotDeployedTransportAdaptorFile> notDeployedTransportAdaptorFiles;


    public CarbonTransportAdaptorManagerService() {
        tenantSpecificTransportAdaptorConfigurationMap = new ConcurrentHashMap<Integer, Map<String, TransportAdaptorConfiguration>>();
        transportAdaptorFileMap = new ConcurrentHashMap<Integer, List<TransportAdaptorFile>>();
        tenantSpecificInputTransportAdaptorInfoMap = new HashMap<Integer, Map<String, TransportAdaptorInfo>>();
        tenantSpecificOutputTransportAdaptorInfoMap = new HashMap<Integer, Map<String, TransportAdaptorInfo>>();

    }

    public void registerEventBuilderDeploymentNotifier(EventBuilderDeploymentListener eventBuilderDeploymentListener){
        this.eventBuilderDeploymentListener = eventBuilderDeploymentListener;
    }

    /**
     * This method is used to store all the deployed and non-deployed transport adaptors in a map
     * (A flag used to uniquely identity whether the transport adaptor deployed or not
     *
     * @param tenantId
     * @param transportAdaptorName
     * @param filePath
     * @param flag
     */
    public void addFileConfiguration(int tenantId, String transportAdaptorName, String filePath,
                                     boolean flag) {

        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);

        if (transportAdaptorFileList == null) {
            transportAdaptorFileList = new ArrayList<TransportAdaptorFile>();
        } else {
            Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                if (transportAdaptorFileIterator.next().getFilePath().equals(filePath)) {
                    return;
                }
            }
        }

        TransportAdaptorFile transportAdaptorFile = new TransportAdaptorFile();
        transportAdaptorFile.setFilePath(filePath);
        transportAdaptorFile.setTransportAdaptorName(transportAdaptorName);
        transportAdaptorFile.setSuccess(flag);
        transportAdaptorFileList.add(transportAdaptorFile);
        transportAdaptorFileMap.put(tenantId, transportAdaptorFileList);


    }

    public void editTransportAdaptorConfigurationFile(String transportAdaptorConfiguration,
                                                      String transportAdaptorName,
                                                      AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        try {
            OMElement omElement = AXIOMUtil.stringToOM(transportAdaptorConfiguration);
            omElement.toString();
            TransportAdaptorConfiguration transportAdaptorConfigurationObject = TransportAdaptorConfigurationHelper.fromOM(omElement);
            if (!transportAdaptorConfigurationObject.getName().equals(transportAdaptorName)) {
                if (checkAdaptorValidity(tenantId, transportAdaptorConfigurationObject.getName())) {
                    validateTransportAdaptorConfiguration(tenantId, transportAdaptorName, axisConfiguration, omElement);
                } else {
                    throw new TransportAdaptorManagerConfigurationException("There is a transport adaptor already registered with the same name");
                }
            } else {
                validateTransportAdaptorConfiguration(tenantId, transportAdaptorName, axisConfiguration, omElement);
            }

        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new TransportAdaptorManagerConfigurationException("Not a valid xml object, " + e.getMessage());
        }
    }

    public void editNotDeployedTransportAdaptorConfigurationFile(
            String transportAdaptorConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(transportAdaptorConfiguration);
            omElement.toString();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
            TransportAdaptorConfiguration transportAdaptorConfigurationObject = TransportAdaptorConfigurationHelper.fromOM(omElement);
            if (checkAdaptorValidity(tenantId, transportAdaptorConfigurationObject.getName())) {
                if (TransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(transportAdaptorConfigurationObject)) {
                    removeTransportAdaptorConfigurationFile(filePath, axisConfiguration);
                    TransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorConfigurationObject.getName(), filePath, axisConfiguration);
                } else {
                    log.error("There is no transport adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available");
                    throw new TransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfigurationObject.getType() + " is available ");
                }
            } else {
                throw new TransportAdaptorManagerConfigurationException("There is a transport adaptor with the same name");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new TransportAdaptorManagerConfigurationException("Not a valid xml object " + e.getMessage());
        }
    }


    public void saveTransportAdaptorConfiguration(
            TransportAdaptorConfiguration transportAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        String transportName = transportAdaptorConfiguration.getName();
        OMElement omElement = TransportAdaptorConfigurationHelper.transportAdaptorConfigurationToOM(transportAdaptorConfiguration);

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (TransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(TransportAdaptorConfigurationHelper.fromOM(omElement))) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new TransportAdaptorManagerConfigurationException("Cannot create directory to add tenant specific transport adaptor :" + transportName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + TransportAdaptorManagerConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new TransportAdaptorManagerConfigurationException("Cannot create directory " + TransportAdaptorManagerConstants.TM_ELE_DIRECTORY + " to add tenant specific transport adaptor :" + transportName);
                }
            }
            String pathInFileSystem = directory.getAbsolutePath() + File.separator + transportName + ".xml";
            TransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available");
            throw new TransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }


    public void removeTransportAdaptorConfiguration(String transportAdaptorName,
                                                    AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
        while (transportAdaptorFileIterator.hasNext()) {

            TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
            if ((transportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                String filePath = transportAdaptorFile.getFilePath();
                TransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
                break;
            }
        }
    }

    public void removeTransportAdaptorConfigurationFile(String filePath,
                                                        AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        TransportAdaptorConfigurationFilesystemInvoker.deleteTransportAdaptorFile(filePath, axisConfiguration);
    }

    @Override
    public String getTransportAdaptorConfigurationFile(String transportAdaptorName,
                                                       AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        String filePath = "";
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
        while (transportAdaptorFileIterator.hasNext()) {
            TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
            if ((transportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                filePath = transportAdaptorFile.getFilePath();

            }
        }
        return readTransportAdaptorConfigurationFile(filePath);
    }

    public String getNotDeployedTransportAdaptorConfigurationFile(String filePath)
            throws TransportAdaptorManagerConfigurationException {

        return readTransportAdaptorConfigurationFile(filePath);
    }

    private String readTransportAdaptorConfigurationFile(String filePath)
            throws TransportAdaptorManagerConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new TransportAdaptorManagerConfigurationException("Transport adaptor file not found ", e);
        } catch (IOException e) {
            throw new TransportAdaptorManagerConfigurationException("Cannot read the transport adaptor file ", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new TransportAdaptorManagerConfigurationException("Error occurred when reading the file ", e);
            }
        }

        return stringBuffer.toString().trim();
    }


    public List<TransportAdaptorConfiguration> getAllTransportAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        List<TransportAdaptorConfiguration> transportAdaptorConfigurations = new ArrayList<TransportAdaptorConfiguration>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) != null) {
            for (TransportAdaptorConfiguration transportAdaptorConfiguration : tenantSpecificTransportAdaptorConfigurationMap.get(
                    tenantId).values()) {
                transportAdaptorConfigurations.add(transportAdaptorConfiguration);
            }
        }
        return transportAdaptorConfigurations;
    }

    public List<TransportAdaptorFile> getNotDeployedTransportAdaptorConfigurationFiles(
            AxisConfiguration axisConfiguration) {

        List<TransportAdaptorFile> unDeployedTransportAdaptorFileList = new ArrayList<TransportAdaptorFile>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (transportAdaptorFileMap.get(tenantId) != null) {
            for (TransportAdaptorFile transportAdaptorFile : transportAdaptorFileMap.get(tenantId)) {
                if (!transportAdaptorFile.isSuccess()) {
                    unDeployedTransportAdaptorFileList.add(transportAdaptorFile);
                }
            }
        }
        return unDeployedTransportAdaptorFileList;
    }


    @Override
    public TransportAdaptorConfiguration getTransportAdaptorConfiguration(String name,
                                                                          int tenantId)
            throws TransportAdaptorManagerConfigurationException {

        if (tenantSpecificTransportAdaptorConfigurationMap.get(tenantId) == null) {
            throw new TransportAdaptorManagerConfigurationException("There is no any configuration exists for " + tenantId);
        }
        return tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).get(name);
    }

    @Override
    public Map<String, String> getInputTransportAdaptorConfiguration(String
                                                                             transportAdaptorName,
                                                                     int tenantId) {

        Map<String, TransportAdaptorConfiguration> transportAdaptors = tenantSpecificTransportAdaptorConfigurationMap.get(tenantId);
        Map<String, String> inProperties = null;
        if (transportAdaptors.containsKey(transportAdaptorName)) {
            TransportAdaptorConfiguration transportAdaptorConfiguration = transportAdaptors.get(transportAdaptorName);

            if (transportAdaptorConfiguration.getInputConfiguration() != null) {
                inProperties = transportAdaptorConfiguration.getInputConfiguration().getProperties();
            }

            if (inProperties != null) {
                Iterator commonAdaptorPropertyIterator = transportAdaptorConfiguration.getCommonProperties().entrySet().iterator();
                while (commonAdaptorPropertyIterator.hasNext()) {
                    Map.Entry thisEntry = (Map.Entry) commonAdaptorPropertyIterator.next();
                    inProperties.put(thisEntry.getKey().toString(), thisEntry.getValue().toString());
                }
            } else {
                inProperties = transportAdaptorConfiguration.getCommonProperties();

            }
        }
        return inProperties;
    }


    @Override
    public Map<String, String> getOutputTransportAdaptorConfiguration(String
                                                                              transportAdaptorName,
                                                                      int tenantId) {

        Map<String, TransportAdaptorConfiguration> transportAdaptors = tenantSpecificTransportAdaptorConfigurationMap.get(tenantId);
        Map<String, String> outProperties = null;
        if (transportAdaptors.containsKey(transportAdaptorName)) {
            TransportAdaptorConfiguration transportAdaptorConfiguration = transportAdaptors.get(transportAdaptorName);

            if (transportAdaptorConfiguration.getOutputConfiguration() != null) {
                outProperties = transportAdaptorConfiguration.getOutputConfiguration().getProperties();
            }

            if (outProperties != null) {
                Iterator commonAdaptorPropertyIterator = transportAdaptorConfiguration.getCommonProperties().entrySet().iterator();
                while (commonAdaptorPropertyIterator.hasNext()) {
                    Map.Entry thisEntry = (Map.Entry) commonAdaptorPropertyIterator.next();
                    outProperties.put(thisEntry.getKey().toString(), thisEntry.getValue().toString());
                }
            } else {
                outProperties = transportAdaptorConfiguration.getCommonProperties();

            }
        }
        return outProperties;
    }

    @Override
    public List<TransportAdaptorInfo> getInputTransportAdaptorInfo(int tenantId) {

        Map<String, TransportAdaptorInfo> inputTransportAdaptorInfoMap = tenantSpecificInputTransportAdaptorInfoMap.get(tenantId);

        if (inputTransportAdaptorInfoMap != null) {
            List<TransportAdaptorInfo> transportAdaptorInfoList = new ArrayList<TransportAdaptorInfo>();
            for (TransportAdaptorInfo transportAdaptorInfo : inputTransportAdaptorInfoMap.values()) {
                transportAdaptorInfoList.add(transportAdaptorInfo);
            }
            return transportAdaptorInfoList;
        }
        return null;
    }

    @Override
    public List<TransportAdaptorInfo> getOutputTransportAdaptorInfo(int tenantId) {

        Map<String, TransportAdaptorInfo> outputTransportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);
        if (outputTransportAdaptorInfoMap != null) {
            List<TransportAdaptorInfo> transportAdaptorInfoList = new ArrayList<TransportAdaptorInfo>();
            for (TransportAdaptorInfo transportAdaptorInfo : outputTransportAdaptorInfoMap.values()) {
                transportAdaptorInfoList.add(transportAdaptorInfo);
            }

            return transportAdaptorInfoList;
        }
        return null;
    }


    /**
     * to add to the input and output adaptor maps that gives information which transport adaptors supports
     * for transport adaptor configuration
     *
     * @param tenantId
     * @param transportAdaptorConfiguration
     * @throws TransportAdaptorManagerConfigurationException
     *
     */
    private void addToTenantSpecificTransportAdaptorInfoMap(int tenantId,
                                                            TransportAdaptorConfiguration transportAdaptorConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        if (transportAdaptorConfiguration.getInputConfiguration() != null) {
            TransportAdaptorInfo transportAdaptorInfo = new TransportAdaptorInfo();
            transportAdaptorInfo.setTransportAdaptorName(transportAdaptorConfiguration.getName());
            transportAdaptorInfo.setTransportAdaptorType(transportAdaptorConfiguration.getType());
            Map<String, TransportAdaptorInfo> transportAdaptorInfoMap = tenantSpecificInputTransportAdaptorInfoMap.get(tenantId);

            if (transportAdaptorInfoMap != null) {

                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), transportAdaptorInfo);
            } else {
                transportAdaptorInfoMap = new HashMap<String, TransportAdaptorInfo>();
                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), transportAdaptorInfo);
            }
            tenantSpecificInputTransportAdaptorInfoMap.put(tenantId, transportAdaptorInfoMap);

        }
        if (transportAdaptorConfiguration.getOutputConfiguration() != null) {
            TransportAdaptorInfo transportAdaptorInfo = new TransportAdaptorInfo();
            transportAdaptorInfo.setTransportAdaptorName(transportAdaptorConfiguration.getName());
            transportAdaptorInfo.setTransportAdaptorType(transportAdaptorConfiguration.getType());
            Map<String, TransportAdaptorInfo> transportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);

            if (transportAdaptorInfoMap != null) {

                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), transportAdaptorInfo);
            } else {
                transportAdaptorInfoMap = new HashMap<String, TransportAdaptorInfo>();
                transportAdaptorInfoMap.put(transportAdaptorConfiguration.getName(), transportAdaptorInfo);
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

        Map<String, TransportAdaptorInfo> inputTransportAdaptorInfoMap = tenantSpecificInputTransportAdaptorInfoMap.get(tenantId);
        Map<String, TransportAdaptorInfo> outputTransportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);

        if (inputTransportAdaptorInfoMap != null && inputTransportAdaptorInfoMap.containsKey(transportAdaptorName)) {
            inputTransportAdaptorInfoMap.remove(transportAdaptorName);
        }

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
            List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
                if ((transportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName)) && (transportAdaptorFile.isSuccess())) {
                    log.error("Transport adaptor " + transportAdaptorName + " is already registered with this tenant");
                    return false;
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
            List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
            Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
            while (transportAdaptorFileIterator.hasNext()) {
                TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
                if ((transportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {
                    return transportAdaptorFile.getFilePath();
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
     * @throws TransportAdaptorManagerConfigurationException
     *
     */
    private void validateTransportAdaptorConfiguration(int tenantId, String transportAdaptorName,
                                                       AxisConfiguration axisConfiguration,
                                                       OMElement omElement)
            throws TransportAdaptorManagerConfigurationException {
        TransportAdaptorConfiguration transportAdaptorConfiguration = TransportAdaptorConfigurationHelper.fromOM(omElement);
        if (TransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(transportAdaptorConfiguration)) {
            String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
            removeTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
            TransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
        } else {
            log.error("There is no transport adaptor type called "+ transportAdaptorConfiguration.getType() + " is available ");
            throw new TransportAdaptorManagerConfigurationException("There is no transport adaptor type called " + transportAdaptorConfiguration.getType() + " is available ");
        }
    }

    /**
     * to remove the deployed and not-deployed the transport adaptor configuration from the map.
     *
     * @param filePath
     * @param tenantId
     */
    public void removeTransportAdaptorConfigurationFromMap(String filePath, int tenantId) {
        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);

        Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
        while (transportAdaptorFileIterator.hasNext()) {
            TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
            if ((transportAdaptorFile.getFilePath().equals(filePath))) {
                if (transportAdaptorFile.isSuccess()) {
                    String transportAdaptorName = transportAdaptorFile.getTransportAdaptorName();
                    tenantSpecificTransportAdaptorConfigurationMap.get(tenantId).remove(transportAdaptorName);
                    removeFromTenantSpecificTransportAdaptorInfoMap(tenantId, transportAdaptorName);
                }
                transportAdaptorFileList.remove(transportAdaptorFile);
                return;
            }
        }
    }

    /**
     * to add to the tenant specific transport adaptor configuration map (only the correctly deployed transport adaptors)
     *
     * @param tenantId
     * @param transportAdaptorConfiguration
     * @throws TransportAdaptorManagerConfigurationException
     *
     */
    public void addTransportAdaptorConfigurationForTenant(
            int tenantId, TransportAdaptorConfiguration transportAdaptorConfiguration)
            throws TransportAdaptorManagerConfigurationException {
        Map<String, TransportAdaptorConfiguration> transportAdaptorConfigurationMap
                = tenantSpecificTransportAdaptorConfigurationMap.get(tenantId);

        if (transportAdaptorConfigurationMap == null) {
            transportAdaptorConfigurationMap = new ConcurrentHashMap<String, TransportAdaptorConfiguration>();
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
            tenantSpecificTransportAdaptorConfigurationMap.put(tenantId, transportAdaptorConfigurationMap);
        } else {
            transportAdaptorConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
        }
        addToTenantSpecificTransportAdaptorInfoMap(tenantId, transportAdaptorConfiguration);
    }


    public void deployTransportAdaptorConfigurationFiles()
            throws TransportAdaptorManagerConfigurationException {

        if (notDeployedTransportAdaptorFiles != null && notDeployedTransportAdaptorFiles.size() > 0) {
            List<NotDeployedTransportAdaptorFile> filePathList = new ArrayList<NotDeployedTransportAdaptorFile>(notDeployedTransportAdaptorFiles);
            notDeployedTransportAdaptorFiles = null;
            for(int i =0 ; i<filePathList.size() ; i++ ) {
                NotDeployedTransportAdaptorFile transportAdaptorFile = filePathList.remove(0);
                AxisConfiguration axisConfiguration = transportAdaptorFile.getAxisConfiguration();
                String filePath = transportAdaptorFile.getFilePath();
                executeDeploy(filePath, axisConfiguration);
            }
        }
    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

    private static void executeDeploy(String transportPath, AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {
        TransportAdaptorDeployer deployer = (TransportAdaptorDeployer) getDeployer(axisConfiguration, TransportAdaptorManagerConstants.TM_ELE_DIRECTORY);
        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(transportPath));
        deployer.manualDeploy(deploymentFileData, transportPath);

    }
}