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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorFile;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TransportAdaptorManagerConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.config.TransportAdaptorConfigurationFilesystemInvoker;
import org.wso2.carbon.transport.adaptor.manager.core.internal.config.TransportAdaptorConfigurationHelper;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorInfo;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportAdaptorManagerConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
 * carbon implementation of the transport manager.
 */
public class CarbonTransportAdaptorManagerService implements TransportAdaptorManagerService {
    private static final Log log = LogFactory.getLog(CarbonTransportAdaptorManagerService.class);

    /**
     * transport configuration map to keep the transport configuration details
     */

    private Map<Integer, Map<String, TransportAdaptorConfiguration>> tenantSpecificTransportAdaptorConfigurationMap;
    private Map<Integer, Map<String, TransportAdaptorInfo>> tenantSpecificInputTransportAdaptorInfoMap;
    private Map<Integer, Map<String, TransportAdaptorInfo>> tenantSpecificOutputTransportAdaptorInfoMap;
    private Map<Integer, List<TransportAdaptorFile>> transportAdaptorFileMap;


    public CarbonTransportAdaptorManagerService() {
        tenantSpecificTransportAdaptorConfigurationMap = new ConcurrentHashMap<Integer, Map<String, TransportAdaptorConfiguration>>();
        transportAdaptorFileMap = new ConcurrentHashMap<Integer, List<TransportAdaptorFile>>();
        tenantSpecificInputTransportAdaptorInfoMap = new HashMap<Integer, Map<String, TransportAdaptorInfo>>();
        tenantSpecificOutputTransportAdaptorInfoMap = new HashMap<Integer, Map<String, TransportAdaptorInfo>>();
    }

    public void addFileConfiguration(int tenantId, String transportAdaptorName, String filePath,
                                     boolean flag) {

        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);

        if (transportAdaptorFileList == null) {
            transportAdaptorFileList = new ArrayList<TransportAdaptorFile>();
            TransportAdaptorFile transportAdaptorFile = new TransportAdaptorFile();
            transportAdaptorFile.setFilePath(filePath);
            transportAdaptorFile.setTransportAdaptorName(transportAdaptorName);
            transportAdaptorFile.setSuccess(flag);
            transportAdaptorFileList.add(transportAdaptorFile);

            transportAdaptorFileMap.put(tenantId, transportAdaptorFileList);
        } else {
            TransportAdaptorFile transportAdaptorFile = new TransportAdaptorFile();
            transportAdaptorFile.setFilePath(filePath);
            transportAdaptorFile.setTransportAdaptorName(transportAdaptorName);
            transportAdaptorFile.setSuccess(flag);
            transportAdaptorFileList.add(transportAdaptorFile);
            transportAdaptorFileMap.put(tenantId, transportAdaptorFileList);

        }
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
                    TransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, TransportAdaptorConfigurationHelper.fromOM(omElement).getName(), filePath, axisConfiguration);
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

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        OMElement transportAdaptorOMElement = null;
        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
        while (transportAdaptorFileIterator.hasNext()) {
            TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
            if ((transportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                String filePath = transportAdaptorFile.getFilePath();
                File file = new File(transportAdaptorFile.getFilePath());
                if (file.exists()) {
                    transportAdaptorOMElement = getTransportOMElement(filePath, file);
                }
            }
        }
        return transportAdaptorOMElement.toString();
    }

    public String getNotDeployedTransportAdaptorConfigurationFile(String filePath)
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
            throw new TransportAdaptorManagerConfigurationException("Transport Adaptor file not found", e);
        } catch (IOException e) {
            throw new TransportAdaptorManagerConfigurationException("Cannot read the transport Adaptor file", e);
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
                                                                          AxisConfiguration axisConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
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

            if (transportAdaptorConfiguration.getInputTransportAdaptorConfiguration() != null) {
                inProperties = transportAdaptorConfiguration.getInputTransportAdaptorConfiguration().getPropertyList();
            }

            if (inProperties != null) {
                Iterator commonAdaptorPropertyIterator = transportAdaptorConfiguration.getTransportAdaptorCommonProperties().entrySet().iterator();
                while (commonAdaptorPropertyIterator.hasNext()) {
                    Map.Entry thisEntry = (Map.Entry) commonAdaptorPropertyIterator.next();
                    inProperties.put(thisEntry.getKey().toString(), thisEntry.getValue().toString());
                }
            } else {
                inProperties = transportAdaptorConfiguration.getTransportAdaptorCommonProperties();

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

            if (transportAdaptorConfiguration.getOutputTransportAdaptorConfiguration() != null) {
                outProperties = transportAdaptorConfiguration.getOutputTransportAdaptorConfiguration().getPropertyList();
            }

            if (outProperties != null) {
                Iterator commonAdaptorPropertyIterator = transportAdaptorConfiguration.getTransportAdaptorCommonProperties().entrySet().iterator();
                while (commonAdaptorPropertyIterator.hasNext()) {
                    Map.Entry thisEntry = (Map.Entry) commonAdaptorPropertyIterator.next();
                    outProperties.put(thisEntry.getKey().toString(), thisEntry.getValue().toString());
                }
            } else {
                outProperties = transportAdaptorConfiguration.getTransportAdaptorCommonProperties();

            }
        }
        return outProperties;
    }

    @Override
    public List<TransportAdaptorInfo> getInputTransportAdaptorInfo(int tenantId) {

        Map<String, TransportAdaptorInfo> inputTransportAdaptorInfoMap = tenantSpecificInputTransportAdaptorInfoMap.get(tenantId);

        if (inputTransportAdaptorInfoMap != null) {
            return (List<TransportAdaptorInfo>) inputTransportAdaptorInfoMap.values();
        }
        return null;
    }

    @Override
    public List<TransportAdaptorInfo> getOutputTransportAdaptorInfo(int tenantId) {

        Map<String, TransportAdaptorInfo> outputTransportAdaptorInfoMap = tenantSpecificOutputTransportAdaptorInfoMap.get(tenantId);
        if (outputTransportAdaptorInfoMap != null) {
            return (List<TransportAdaptorInfo>) outputTransportAdaptorInfoMap.values();
        }
        return null;
    }


    private void addToTenantSpecificTransportAdaptorInfoMap(int tenantId,
                                                            TransportAdaptorConfiguration transportAdaptorConfiguration)
            throws TransportAdaptorManagerConfigurationException {

        if (transportAdaptorConfiguration.getInputTransportAdaptorConfiguration() != null) {
            addToInputTransportInfoMap(tenantId, transportAdaptorConfiguration);

        } else if (transportAdaptorConfiguration.getOutputTransportAdaptorConfiguration() != null) {
            addToOutputTransportInfoMap(tenantId, transportAdaptorConfiguration);
        }

    }

    private void addToInputTransportInfoMap(int tenantId,
                                            TransportAdaptorConfiguration transportAdaptorConfiguration) {

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

    private void addToOutputTransportInfoMap(int tenantId,
                                             TransportAdaptorConfiguration transportAdaptorConfiguration) {

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


    private OMElement getTransportOMElement(String path, File transportAdaptorFile)
            throws TransportAdaptorManagerConfigurationException {
        OMElement transportAdaptorElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(transportAdaptorFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            transportAdaptorElement = builder.getDocumentElement();
            transportAdaptorElement.build();
        } catch (Exception e) {
            String errorMessage = " .xml file cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new TransportAdaptorManagerConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
        return transportAdaptorElement;
    }


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


    private void validateTransportAdaptorConfiguration(int tenantId, String transportAdaptorName,
                                                       AxisConfiguration axisConfiguration,
                                                       OMElement omElement)
            throws TransportAdaptorManagerConfigurationException {
        if (TransportAdaptorConfigurationHelper.validateTransportAdaptorConfiguration(TransportAdaptorConfigurationHelper.fromOM(omElement))) {
            String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
            removeTransportAdaptorConfiguration(transportAdaptorName, axisConfiguration);
            TransportAdaptorConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
        }
    }

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
}