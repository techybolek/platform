package org.wso2.carbon.event.formatter.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.EventSource;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;
import org.wso2.carbon.event.formatter.core.internal.util.FormatterConfigurationBuilder;
import org.wso2.carbon.event.formatter.core.internal.util.helper.EventFormatterConfigurationFilesystemInvoker;
import org.wso2.carbon.event.formatter.core.internal.util.helper.EventFormatterConfigurationHelper;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventFormatterService implements EventFormatterService {

    private static final Log log = LogFactory.getLog(CarbonEventFormatterService.class);

    private Map<Integer, Map<String, EventFormatter>> tenantSpecificEventFormatterConfigurationMap;
    private Map<Integer, List<EventFormatterConfigurationFile>> eventFormatterConfigurationFileMap;


    public CarbonEventFormatterService() {
        tenantSpecificEventFormatterConfigurationMap = new ConcurrentHashMap<Integer, Map<String, EventFormatter>>();
        eventFormatterConfigurationFileMap = new ConcurrentHashMap<Integer, List<EventFormatterConfigurationFile>>();
    }

    @Override
    public List<EventFormatterConfiguration> getAllEventFormatterConfiguration(
            AxisConfiguration axisConfiguration) throws EventFormatterConfigurationException {
        List<EventFormatterConfiguration> eventFormatterConfigurations = new ArrayList<EventFormatterConfiguration>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (tenantSpecificEventFormatterConfigurationMap.get(tenantId) != null) {
            for (EventFormatter eventFormatter : tenantSpecificEventFormatterConfigurationMap.get(
                    tenantId).values()) {
                eventFormatterConfigurations.add(eventFormatter.getEventFormatterConfiguration());
            }
        }
        return eventFormatterConfigurations;
    }

    public void removeEventFormatterConfiguration(String eventFormatterName,
                                                  AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<EventFormatterConfigurationFile> eventFormatterFileList = eventFormatterConfigurationFileMap.get(tenantId);
        if (eventFormatterFileList != null) {
            Iterator<EventFormatterConfigurationFile> eventFormatterFileIterator = eventFormatterFileList.iterator();
            while (eventFormatterFileIterator.hasNext()) {

                EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterFileIterator.next();
                if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName))) {

                    String filePath = eventFormatterConfigurationFile.getFilePath();
                    EventFormatterConfigurationFilesystemInvoker.delete(filePath, axisConfiguration);
                    break;
                }
            }
        }
    }

    public List<EventFormatterConfigurationFile> getNotDeployedEventFormatterConfigurationFiles(
            AxisConfiguration axisConfiguration) {

        List<EventFormatterConfigurationFile> unDeployedEventFormatterFileList = new ArrayList<EventFormatterConfigurationFile>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (eventFormatterConfigurationFileMap.get(tenantId) != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileMap.get(tenantId)) {
                if (!eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) {
                    unDeployedEventFormatterFileList.add(eventFormatterConfigurationFile);
                }
            }
        }
        return unDeployedEventFormatterFileList;
    }

    public void removeEventFormatterConfigurationFile(String filePath,
                                                      AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        EventFormatterConfigurationFilesystemInvoker.delete(filePath, axisConfiguration);
    }


    public String getNotDeployedEventFormatterConfigurationFile(String filePath)
            throws EventFormatterConfigurationException {
        return readEventFormatterConfigurationFile(filePath);
    }

    private String readEventFormatterConfigurationFile(String filePath)
            throws EventFormatterConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventFormatterConfigurationException("Event formatter file not found ", e);
        } catch (IOException e) {
            throw new EventFormatterConfigurationException("Cannot read the event formatter file ", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new EventFormatterConfigurationException("Error occurred when reading the file ", e);
            }
        }
        return stringBuffer.toString().trim();
    }

    public void editNotDeployedEventFormatterConfigurationFile(
            String eventFormatterConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventFormatterConfiguration);
            omElement.toString();
            int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
            EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
            String mappingType = EventFormatterConfigurationHelper.getOutputMappingType(omElement);

            if (mappingType != null) {
                EventFormatterConfiguration eventFormatterConfigurationObject = FormatterConfigurationBuilder.getEventFormatterConfiguration(omElement, tenantId, mappingType);
                if (checkEventFormatterValidity(tenantId, eventFormatterConfigurationObject.getEventFormatterName())) {
                    removeEventFormatterConfigurationFile(filePath, axisConfiguration);
                    EventFormatterConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, eventFormatterConfigurationObject.getEventFormatterName(), filePath, axisConfiguration);

                } else {
                    throw new EventFormatterConfigurationException("There is a event formatter with the same name");
                }
            }

        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new EventFormatterConfigurationException("Not a valid xml object ", e);
        }
    }


    public void editEventFormatterConfigurationFile(String eventFormatterConfiguration,
                                                    String eventFormatterName,
                                                    AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventFormatterConfiguration);
            omElement.toString();
            EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
            String mappingType = EventFormatterConfigurationHelper.getOutputMappingType(omElement);
            if (mappingType != null) {
                EventFormatterConfiguration eventFormatterConfigurationObject = FormatterConfigurationBuilder.getEventFormatterConfiguration(omElement, tenantId, mappingType);
                if (!eventFormatterConfigurationObject.getEventFormatterName().equals(eventFormatterName)) {
                    if (checkEventFormatterValidity(tenantId, eventFormatterConfigurationObject.getEventFormatterName())) {
                        saveEditedEvenTFormatterConfiguration(tenantId, eventFormatterName, axisConfiguration, omElement);
                    } else {
                        throw new EventFormatterConfigurationException("There is a event formatter already registered with the same name");
                    }
                } else {
                    saveEditedEvenTFormatterConfiguration(tenantId, eventFormatterName, axisConfiguration, omElement);
                }
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new EventFormatterConfigurationException("Not a valid xml object, ", e);
        } catch (EventFormatterValidationException ex) {
            throw new EventFormatterConfigurationException(ex);
        }
    }


    public String getEventFormatterConfigurationFile(String eventFormatterName,
                                                     AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        String filePath = "";
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();

        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
        if (eventFormatterConfigurationFileList != null) {
            Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFileList.iterator();
            while (eventFormatterConfigurationFileIterator.hasNext()) {
                EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
                if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName))) {
                    filePath = eventFormatterConfigurationFile.getFilePath();
                    break;
                }
            }
        }
        return readEventFormatterConfigurationFile(filePath);
    }

    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        List<String> streamList = new ArrayList<String>();

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<EventSource> eventSourceList = EventFormatterServiceValueHolder.getEventSourceList();
        if (eventSourceList != null) {
            Iterator<EventSource> eventSourceIterator = eventSourceList.iterator();
            while (eventSourceIterator.hasNext()) {
                EventSource eventSource = eventSourceIterator.next();
                List<String> streamNameList = eventSource.getStreamNames(tenantId);
                if (streamNameList != null) {
                    Iterator<String> streamNameIterator = streamNameList.iterator();
                    while (streamNameIterator.hasNext()) {
                        String streamName = streamNameIterator.next();
                        streamList.add(streamName);
                    }
                }
            }
        }

        return streamList;

    }

    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventSource eventSource = getEventSource(streamNameWithVersion, tenantId);
        String streamDetail[] = streamNameWithVersion.trim().split(":");

        StreamDefinition streamDefinition = eventSource.getStreamDefinition(streamDetail[0], streamDetail[1], tenantId);

        return streamDefinition;
    }


    public void saveEventFormatterConfiguration(
            EventFormatterConfiguration eventFormatterConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        String eventFormatterName = eventFormatterConfiguration.getEventFormatterName();
        OMElement omElement = null;

        omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
        EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
        if (EventFormatterConfigurationHelper.getOutputMappingType(omElement) != null) {

            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new EventFormatterConfigurationException("Cannot create directory to add tenant specific event formatter :" + eventFormatterName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + EventFormatterConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new EventFormatterConfigurationException("Cannot create directory " + EventFormatterConstants.TM_ELE_DIRECTORY + " to add tenant specific transport adaptor :" + eventFormatterName);
                }
            }
            String pathInFileSystem = directory.getAbsolutePath() + File.separator + eventFormatterName + ".xml";
            EventFormatterConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, eventFormatterName, pathInFileSystem, axisConfiguration);

        }

    }

    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventFormatterConfigurationException {
        RegistryService registryService = EventFormatterServiceValueHolder.getRegistryService();

        String registryData = "";
        Resource registryResource = null;
        try {
            String pathPrefix = resourcePath.substring(0, resourcePath.indexOf(':') + 2);
            if (pathPrefix.equalsIgnoreCase(EventFormatterConstants.REGISTRY_CONF_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getConfigSystemRegistry().get(resourcePath);
            } else if (pathPrefix.equalsIgnoreCase(EventFormatterConstants.REGISTRY_GOVERNANCE_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getGovernanceSystemRegistry().get(resourcePath);
            }

            if (registryResource != null) {
                registryData = (RegistryUtils.decodeBytes((byte[]) registryResource.getContent())).toString();
            } else {
                throw new EventFormatterConfigurationException("Resource couldn't found from registry at " + resourcePath);
            }

        } catch (RegistryException e) {
            throw new EventFormatterConfigurationException("Error while retrieving the resource from registry at " + resourcePath, e);
        }
        return registryData;
    }

    @Override
    public EventFormatterConfiguration getEventFormatterConfiguration(String eventFormatterName,
                                                                      int tenantId)
            throws EventFormatterConfigurationException {

        EventFormatterConfiguration eventFormatterConfiguration = null;

        Map<String, EventFormatter> tenantSpecificEventFormatterMap = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
        if (tenantSpecificEventFormatterMap != null && tenantSpecificEventFormatterMap.size() > 0) {
            eventFormatterConfiguration = tenantSpecificEventFormatterMap.get(eventFormatterName).getEventFormatterConfiguration();
        }
        return eventFormatterConfiguration;
    }

    @Override
    public void disableStatistics(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableStatistics(false);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    @Override
    public void enableStatistics(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableStatistics(true);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    @Override
    public void disableTracing(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableTracing(false);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    @Override
    public void enableTracing(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableTracing(true);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    private void editTracingStatistics(
            EventFormatterConfiguration eventFormatterConfiguration,
            String eventFormatterName, int tenantId, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        String pathInFileSystem = getFilePath(tenantId, eventFormatterName);
        removeEventFormatterConfiguration(eventFormatterName, axisConfiguration);
        OMElement omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
        EventFormatterConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, eventFormatterName, pathInFileSystem, axisConfiguration);
    }


    private void saveEditedEvenTFormatterConfiguration(int tenantId, String eventFormatterName,
                                                       AxisConfiguration axisConfiguration,
                                                       OMElement omElement)
            throws EventFormatterConfigurationException {

        String pathInFileSystem = getFilePath(tenantId, eventFormatterName);
        removeEventFormatterConfigurationFile(pathInFileSystem, axisConfiguration);
        EventFormatterConfigurationFilesystemInvoker.saveConfigurationToFileSystem(omElement, eventFormatterName, pathInFileSystem, axisConfiguration);

    }


    private String getFilePath(int tenantId, String eventFormatterName) {

        if (eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
            if (eventFormatterConfigurationFileList != null) {
                Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFileList.iterator();
                while (eventFormatterConfigurationFileIterator.hasNext()) {
                    EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName))) {
                        return eventFormatterConfigurationFile.getFilePath();
                    }
                }
            }
        }
        return null;
    }


    public boolean checkEventFormatterValidity(int tenantId, String eventFormatterName) {

        if (eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
            if (eventFormatterConfigurationFileList != null) {
                Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFileList.iterator();
                while (eventFormatterConfigurationFileIterator.hasNext()) {
                    EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) && (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
                        log.error("Event formatter " + eventFormatterName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private EventSource getEventSource(String streamNameWithVersion, int tenantId) {
        List<EventSource> eventSourceList = EventFormatterServiceValueHolder.getEventSourceList();

        EventSource eventSource = null;
        Iterator<EventSource> eventSourceIterator = eventSourceList.iterator();
        for (; eventSourceIterator.hasNext(); ) {
            EventSource currentEventSource = eventSourceIterator.next();
            List<String> streamList = currentEventSource.getStreamNames(tenantId);
            if (streamList != null) {
                Iterator<String> stringIterator = streamList.iterator();
                for (; stringIterator.hasNext(); ) {
                    String stream = stringIterator.next();
                    if (stream.equals(streamNameWithVersion)) {
                        eventSource = currentEventSource;
                        break;
                    }
                }
            }
        }

        return eventSource;

    }

    public void addEventFormatterFileConfiguration(int tenantId, String eventFormatterName,
                                                   String filePath,
                                                   EventFormatterConfigurationFile.Status status,
                                                   AxisConfiguration axisConfiguration,
                                                   String deploymentStatusMessage,
                                                   String dependency) {

        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);

        if (eventFormatterConfigurationFileList == null) {
            eventFormatterConfigurationFileList = new ArrayList<EventFormatterConfigurationFile>();
        }
        EventFormatterConfigurationFile eventFormatterConfigurationFile = new EventFormatterConfigurationFile();
        eventFormatterConfigurationFile.setFilePath(filePath);
        eventFormatterConfigurationFile.setEventFormatterName(eventFormatterName);
        eventFormatterConfigurationFile.setStatus(status);
        eventFormatterConfigurationFile.setDependency(dependency);
        eventFormatterConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventFormatterConfigurationFile.setAxisConfiguration(axisConfiguration);
        eventFormatterConfigurationFileList.add(eventFormatterConfigurationFile);
        eventFormatterConfigurationFileMap.put(tenantId, eventFormatterConfigurationFileList);

    }

    public void addEventFormatterConfigurationForTenant(
            int tenantId, EventFormatterConfiguration eventFormatterConfiguration)
            throws EventFormatterConfigurationException {

        Map<String, EventFormatter> eventFormatterConfigurationMap
                = tenantSpecificEventFormatterConfigurationMap.get(tenantId);

        if (eventFormatterConfigurationMap == null) {
            eventFormatterConfigurationMap = new ConcurrentHashMap<String, EventFormatter>();
        }

        EventFormatter eventFormatter = null;
        eventFormatter = eventFormatterConfiguration.getEventFormatterFactory().constructEventFormatter(eventFormatterConfiguration, tenantId);

        eventFormatterConfigurationMap.put(eventFormatterConfiguration.getEventFormatterName(), eventFormatter);

        tenantSpecificEventFormatterConfigurationMap.put(tenantId, eventFormatterConfigurationMap);
    }

    public void removeEventFormatterConfigurationFromMap(String filePath, int tenantId) {
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
        if (eventFormatterConfigurationFileList != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                if ((eventFormatterConfigurationFile.getFilePath().equals(filePath))) {
                    if (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) {
                        String eventFormatterName = eventFormatterConfigurationFile.getEventFormatterName();
                        if (tenantSpecificEventFormatterConfigurationMap.get(tenantId) != null) {
                            tenantSpecificEventFormatterConfigurationMap.get(tenantId).remove(eventFormatterName);
                        }
                    }
                    eventFormatterConfigurationFileList.remove(eventFormatterConfigurationFile);
                    return;
                }
            }
        }
    }


//    public void deployEventFormatterConfigurationFiles(AxisConfiguration axisConfiguration)
//            throws EventFormatterConfigurationException {
//
//        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();
//        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
//
//        if (eventFormatterConfigurationFileMap != null && eventFormatterConfigurationFileMap.size() > 0) {
//            List<EventFormatterConfigurationFile> eventFormatterConfigurationFiles = eventFormatterConfigurationFileMap.get(tenantId);
//
//            if (eventFormatterConfigurationFiles != null) {
//                Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFiles.iterator();
//                while (eventFormatterConfigurationFileIterator.hasNext()) {
//                    EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
//                    if ((!eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY))) {
//                        fileList.add(eventFormatterConfigurationFile);
//                    }
//                }
//                eventFormatterConfigurationFiles.removeAll(fileList);
//                if (eventFormatterConfigurationFiles.size() == 0) {
//                    eventFormatterConfigurationFileMap.remove(tenantId);
//                }
//            }
//        }
//
//        if (fileList.size() > 0) {
//            for (int i = 0; i < fileList.size(); i++) {
//                EventFormatterConfigurationFile eventFormatterConfigurationFile = fileList.remove(0);
//                executeDeploy(eventFormatterConfigurationFile.getFilePath(), axisConfiguration);
//            }
//        }
//    }

    public void reDeployEventFormatterConfigurationFiles(int tenantId)
            throws EventFormatterConfigurationException {

        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();

        if (eventFormatterConfigurationFileMap != null && eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFiles = eventFormatterConfigurationFileMap.get(tenantId);

            if (eventFormatterConfigurationFiles != null) {
                Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFiles.iterator();
                while (eventFormatterConfigurationFileIterator.hasNext()) {
                    EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
                    if ((!eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY))) {
                        fileList.add(eventFormatterConfigurationFile);
                    }
                }
                eventFormatterConfigurationFiles.removeAll(fileList);
                if (eventFormatterConfigurationFiles.size() == 0) {
                    eventFormatterConfigurationFileMap.remove(tenantId);
                }
            }
        }

        if (fileList.size() > 0) {
            for (int i = 0; i < fileList.size(); i++) {
                EventFormatterConfigurationFile eventFormatterConfigurationFile = fileList.get(i);
                EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFilePath(), eventFormatterConfigurationFile.getAxisConfiguration());
            }
        }

    }

    public void reDeployEventFormatterConfigurationFile(int tenantId, String dependency)
            throws EventFormatterConfigurationException {

        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();

        if (eventFormatterConfigurationFileMap != null && eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);

            if (eventFormatterConfigurationFileList != null) {
                Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFileList.iterator();
                while (eventFormatterConfigurationFileIterator.hasNext()) {
                    EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
                    if ((eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && eventFormatterConfigurationFile.getDependency().equalsIgnoreCase(dependency)) {
                        fileList.add(eventFormatterConfigurationFile);
                        eventFormatterConfigurationFileIterator.remove();
                    }
                }
            }
        }
        for (int i = 0; i < fileList.size(); i++) {
            EventFormatterConfigurationFile eventFormatterConfigurationFile = fileList.get(i);
            try {
                EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFilePath(), eventFormatterConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event formatter configuration file : " + new File(eventFormatterConfigurationFile.getFilePath()).getName());
            }
        }

    }

    public void unDeployEventFormatterConfigurationFile(int tenantId, String dependency)
            throws EventFormatterConfigurationException {

        List<String> fileList = new ArrayList<String>();
        if (tenantSpecificEventFormatterConfigurationMap != null && tenantSpecificEventFormatterConfigurationMap.size() > 0) {
            Map<String, EventFormatter> eventFormatterMap = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
            if (eventFormatterMap != null) {
                Iterator<EventFormatter> eventFormatterIterator = eventFormatterMap.values().iterator();
                while (eventFormatterIterator.hasNext()) {
                    EventFormatter eventFormatter = eventFormatterIterator.next();
                    String streamNameWithVersion = eventFormatter.getEventFormatterConfiguration().getFromStreamName() + ":" + eventFormatter.getEventFormatterConfiguration().getFromStreamVersion();
                    String transportAdaptorName = eventFormatter.getEventFormatterConfiguration().getToPropertyConfiguration().getTransportAdaptorName();
                    if (streamNameWithVersion.equals(dependency) || transportAdaptorName.equals(dependency)) {
                        fileList.add(eventFormatter.getEventFormatterConfiguration().getEventFormatterName());
                        eventFormatterIterator.remove();
                    }
                }
            }
        }

        for (int i = 0; i < fileList.size(); i++) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFiles = eventFormatterConfigurationFileMap.get(tenantId);
            if (eventFormatterConfigurationFiles != null) {
                Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFiles.iterator();
                while (eventFormatterConfigurationFileIterator.hasNext()) {
                    EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
                    if ((eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) && eventFormatterConfigurationFile.getEventFormatterName().equalsIgnoreCase(fileList.get(i))) {
                        eventFormatterConfigurationFile.setStatus(EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY);
                        eventFormatterConfigurationFile.setDependency(dependency);
                        eventFormatterConfigurationFile.setDeploymentStatusMessage("Dependency not loaded");
                        log.info("EventFormatter " + eventFormatterConfigurationFile.getEventFormatterName() + " is undeployed because dependency " + dependency + " couldn't found");
                    }

                }
            }
        }
    }

}