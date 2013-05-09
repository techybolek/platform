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
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.manager.core.TransportAdaptorFile;
import org.wso2.carbon.transport.adaptor.manager.core.TransportManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TMConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.config.TransportConfigurationFilesystemInvoker;
import org.wso2.carbon.transport.adaptor.manager.core.internal.config.TransportConfigurationHelper;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TMConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * carbon implementation of the transport manager.
 */
public class CarbonTransportManagerService implements TransportManagerService {
    private static final Log log = LogFactory.getLog(CarbonTransportManagerService.class);

    /**
     * transport configuration map to keep the transport configuration details
     */

    private Map<Integer, Map<String, TransportAdaptorConfiguration>> tenantSpecificTransportConfigurationMap;
    private Map<Integer, List<TransportAdaptorFile>> transportAdaptorFileMap;
    private Map<Integer, List<String>> cancelDeploymentMap;
    private Map<Integer, List<String>> cancelUnDeploymentMap;


    public CarbonTransportManagerService() {
        tenantSpecificTransportConfigurationMap = new ConcurrentHashMap<Integer, Map<String, TransportAdaptorConfiguration>>();
        transportAdaptorFileMap = new ConcurrentHashMap<Integer, List<TransportAdaptorFile>>();
        cancelDeploymentMap = new ConcurrentHashMap<Integer, List<String>>();
        cancelUnDeploymentMap = new ConcurrentHashMap<Integer, List<String>>();
    }

    public void addFileConfiguration(int tenantId, String transportAdaptorName, String filePath, boolean flag) {

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

    public void editTransportConfigurationFile(String transportAdaptorConfiguration, String transportAdaptorName, AxisConfiguration axisConfiguration) throws TMConfigurationException {

        try {
            OMElement omElement = AXIOMUtil.stringToOM(transportAdaptorConfiguration);
            omElement.toString();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

            String pathInFileSystem = getFilePath(tenantId, transportAdaptorName);
            addToCancelDeployMap(tenantId, transportAdaptorName);
            addToCancelUnDeployMap(tenantId, pathInFileSystem);
            removeTransportConfiguration(transportAdaptorName, axisConfiguration);
            TransportConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportAdaptorName, pathInFileSystem, axisConfiguration);
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
        }
//        OMElement omElement =  TransportConfigurationHelper.transportAdaptorConfigurationToOM(transportAdaptorConfiguration);


    }


    public void saveTransportConfiguration(TransportAdaptorConfiguration transportAdaptorConfiguration,
                                           AxisConfiguration axisConfiguration) throws TMConfigurationException {


        String transportName = transportAdaptorConfiguration.getName();
        OMElement omElement = TransportConfigurationHelper.transportAdaptorConfigurationToOM(transportAdaptorConfiguration);
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        File directory = new File(axisConfiguration.getRepository().getPath());
        if (!directory.exists()) {
            if (directory.mkdir()) {
                throw new TMConfigurationException("Cannot create directory to add tenant specific transport adaptor :" + transportName);
            }
        }
        directory = new File(directory.getAbsolutePath() + File.separator + TMConstants.TM_ELE_DIRECTORY);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new TMConfigurationException("Cannot create directory " + TMConstants.TM_ELE_DIRECTORY + " to add tenant specific transport adaptor :" + transportName);
            }
        }

        addToCancelDeployMap(tenantId, transportName);
        String pathInFileSystem = directory.getAbsolutePath() + File.separator + transportName + ".xml";
        TransportConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportName, pathInFileSystem, axisConfiguration);

    }


    public void updateTransportConfiguration(TransportAdaptorConfiguration transportAdaptorConfiguration,
                                             AxisConfiguration axisConfiguration) throws TMConfigurationException {


        String transportName = transportAdaptorConfiguration.getName();
        OMElement omElement = TransportConfigurationHelper.transportAdaptorConfigurationToOM(transportAdaptorConfiguration);
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        String pathInFileSystem = getFilePath(tenantId, transportName);
        addToCancelDeployMap(tenantId, transportName);
        addToCancelUnDeployMap(tenantId, pathInFileSystem);
        TransportConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, transportName, pathInFileSystem, axisConfiguration);

    }


    public void removeTransportConfiguration(String transportAdaptorName,
                                             AxisConfiguration axisConfiguration) throws TMConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);
        Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
        while (transportAdaptorFileIterator.hasNext()) {

            TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
            if ((transportAdaptorFile.getTransportAdaptorName().equals(transportAdaptorName))) {

                String filePath = transportAdaptorFile.getFilePath();
                File file = new File(filePath);
                if (file.exists()) {
                    boolean fileDeleted = file.delete();
                    if (!fileDeleted) {
                        log.error("Could not delete " + filePath);
                    } else {
                        log.info(filePath + " is deleted from the file system");
                        TransportConfigurationFilesystemInvoker.executeUnDeploy(filePath, axisConfiguration);

                    }
                    break;
                }

            }
        }

    }

    public void removeTransportAdaptorFile(String filePath, AxisConfiguration axisConfiguration) throws TMConfigurationException {

        File file = new File(filePath);
        if (file.exists()) {
            boolean fileDeleted = file.delete();
            if (!fileDeleted) {
                log.error("Could not delete " + filePath);
            } else {

                log.info(filePath + " is deleted from the file system");
                TransportConfigurationFilesystemInvoker.executeUnDeploy(filePath, axisConfiguration);
            }
        }

    }

    @Override
    public String getTransportConfigurationFile(String transportAdaptorName, AxisConfiguration axisConfiguration) throws TMConfigurationException {
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
//                    boolean fileDeleted = file.delete();
//                    if (!fileDeleted) {
//                        log.error("Could not delete " + filePath);
//                    } else {
//                        log.info(filePath + " is deleted from the file system");
//                    }

                    transportAdaptorOMElement = getTransportOMElement(filePath, file);

                }

            }
        }
        return transportAdaptorOMElement.toString();
    }


    public List<TransportAdaptorConfiguration> getAllTransportConfigurations(AxisConfiguration axisConfiguration) {
        List<TransportAdaptorConfiguration> transportAdaptorConfigurations = new ArrayList<TransportAdaptorConfiguration>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (tenantSpecificTransportConfigurationMap.get(tenantId) != null) {
            for (TransportAdaptorConfiguration transportAdaptorConfiguration : tenantSpecificTransportConfigurationMap.get(
                    tenantId).values()) {
                transportAdaptorConfigurations.add(transportAdaptorConfiguration);
            }
        }
        return transportAdaptorConfigurations;
    }

    public List<TransportAdaptorFile> getUnDeployedFiles(AxisConfiguration axisConfiguration) {

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

    public List<String> getAllTransportConfigurationNames(int tenantId) {
        List<String> transportProxyNames = new ArrayList<String>();
        if (tenantSpecificTransportConfigurationMap.get(tenantId) != null) {
            for (TransportAdaptorConfiguration transportAdaptorConfiguration : tenantSpecificTransportConfigurationMap.get(
                    tenantId).values()) {
                transportProxyNames.add(transportAdaptorConfiguration.getName());
            }
        }
        return transportProxyNames;
    }

    public TransportAdaptorConfiguration getTransportConfiguration(String name, AxisConfiguration axisConfiguration) throws TMConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        if (tenantSpecificTransportConfigurationMap.get(tenantId) == null) {
            throw new TMConfigurationException("There is no any configuration exists for " + tenantId);
        }
        return tenantSpecificTransportConfigurationMap.get(tenantId).get(name);
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


    public void removeFromCancelDeployMap(int tenantId, String transportAdaptorName) {

        if (cancelDeploymentMap.size() != 0) {

            List<String> transportAdaptorList = cancelDeploymentMap.get(tenantId);
            Iterator<String> transportAdaptorIterator = transportAdaptorList.iterator();
            while (transportAdaptorIterator.hasNext()) {

                if (transportAdaptorIterator.next().equals(transportAdaptorName)) {
                    transportAdaptorList.remove(transportAdaptorName);
                    return;
                }
            }

        }

    }


    public void removeFromCancelUnDeployMap(int tenantId, String pathInFileSystem) {

        if (cancelUnDeploymentMap.size() != 0) {

            List<String> transportAdaptorList = cancelUnDeploymentMap.get(tenantId);
            Iterator<String> transportAdaptorIterator = transportAdaptorList.iterator();
            while (transportAdaptorIterator.hasNext()) {

                if (transportAdaptorIterator.next().equals(pathInFileSystem)) {
                    transportAdaptorList.remove(pathInFileSystem);
                    return;
                }
            }

        }

    }

    private OMElement getTransportOMElement(String path, File transportAdaptorFile) throws TMConfigurationException {
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
            throw new TMConfigurationException(errorMessage, e);
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

    public void removeTransportConfigurationFromMap(String filePath, int tenantId) {
        List<TransportAdaptorFile> transportAdaptorFileList = transportAdaptorFileMap.get(tenantId);

        Iterator<TransportAdaptorFile> transportAdaptorFileIterator = transportAdaptorFileList.iterator();
        while (transportAdaptorFileIterator.hasNext()) {

            TransportAdaptorFile transportAdaptorFile = transportAdaptorFileIterator.next();
            if ((transportAdaptorFile.getFilePath().equals(filePath))) {
                if (transportAdaptorFile.isSuccess()) {
                    tenantSpecificTransportConfigurationMap.get(tenantId).remove(transportAdaptorFile.getTransportAdaptorName());
                }
                transportAdaptorFileList.remove(transportAdaptorFile);
                return;
            }
        }


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


    private void addToCancelDeployMap(int tenantId, String pathInFileSystem) {

        //This is used to maintain the UI responsiveness
        if (cancelDeploymentMap.size() > 0) {
            List<String> transportAdaptorList = cancelDeploymentMap.get(tenantId);
            if (transportAdaptorList == null) {
                transportAdaptorList = new ArrayList<String>();
                transportAdaptorList.add(pathInFileSystem);
                cancelDeploymentMap.put(tenantId, transportAdaptorList);

            } else {
                transportAdaptorList.add(pathInFileSystem);
            }

        }
    }

    private void addToCancelUnDeployMap(int tenantId, String transportAdaptorName) {

        //This is used to maintain the UI responsiveness
        if (cancelUnDeploymentMap.size() > 0) {
            List<String> transportAdaptorList = cancelUnDeploymentMap.get(tenantId);
            if (transportAdaptorList == null) {
                transportAdaptorList = new ArrayList<String>();
                transportAdaptorList.add(transportAdaptorName);
                cancelUnDeploymentMap.put(tenantId, transportAdaptorList);

            } else {
                transportAdaptorList.add(transportAdaptorName);
            }

        }
    }


    public void addTransportConfigurationForTenant(
            int tenantId, TransportAdaptorConfiguration transportAdaptorConfiguration) throws TMConfigurationException {
        Map<String, TransportAdaptorConfiguration> transportConfigurationMap
                = tenantSpecificTransportConfigurationMap.get(tenantId);
        if (transportConfigurationMap == null) {
            transportConfigurationMap = new ConcurrentHashMap<String, TransportAdaptorConfiguration>();
            transportConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
            tenantSpecificTransportConfigurationMap.put(tenantId, transportConfigurationMap);
        } else {
            transportConfigurationMap.put(transportAdaptorConfiguration.getName(), transportAdaptorConfiguration);
        }

    }


    @Override
    public Map<String, String> getInputTransportAdaptorConfiguration(String transportAdaptorName, int tenantId) {

        Map<String, TransportAdaptorConfiguration> transportAdaptors = tenantSpecificTransportConfigurationMap.get(tenantId);
        Map<String, String> inProperties = null;
        if (transportAdaptors.containsKey(transportAdaptorName)) {
            TransportAdaptorConfiguration transportAdaptorConfiguration = transportAdaptors.get(transportAdaptorName);


            if (!transportAdaptorConfiguration.getInputAdaptorProperties().isEmpty()) {
                inProperties = transportAdaptorConfiguration.getInputAdaptorProperties();
            }

            if (!transportAdaptorConfiguration.getCommonAdaptorProperties().isEmpty()) {

                if (inProperties != null) {
                    Iterator commonAdaptorPropertyIterator = transportAdaptorConfiguration.getCommonAdaptorProperties().entrySet().iterator();
                    while (commonAdaptorPropertyIterator.hasNext()) {
                        Map.Entry thisEntry = (Map.Entry) commonAdaptorPropertyIterator.next();
                        inProperties.put(thisEntry.getKey().toString(), thisEntry.getValue().toString());
                    }
                } else {
                    inProperties = transportAdaptorConfiguration.getCommonAdaptorProperties();
                }
            }


        }
        return inProperties;
    }

    @Override
    public List<String> getInputTransportAdaptorNames(int tenantId) {

        List<String> inputTransportAdaptors = null;
        Map<String, TransportAdaptorConfiguration> transportAdaptors = tenantSpecificTransportConfigurationMap.get(tenantId);

        if(transportAdaptors != null){

            Iterator transportAdaptorIterator =  transportAdaptors.entrySet().iterator();
            while (transportAdaptorIterator.hasNext()){

//                Map.Entry thisEntry = (Map.Entry) transportAdaptorIterator.next();
//                TransportAdaptorConfiguration transportAdaptorConfiguration = (TransportAdaptorConfiguration)thisEntry.getValue();
//                String transportAdaptorType = transportAdaptorConfiguration.getType();
//                TransportAdaptorType
            }

        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
