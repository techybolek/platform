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
    public void deployEventFormatterConfiguration(
            EventFormatterConfiguration eventFormatterConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        String eventFormatterName = eventFormatterConfiguration.getEventFormatterName();

        OMElement omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
        EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
        if (EventFormatterConfigurationHelper.getOutputMappingType(omElement) != null) {

            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new EventFormatterConfigurationException("Cannot create directory to add tenant specific Event Formatter : " + eventFormatterName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + EventFormatterConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new EventFormatterConfigurationException("Cannot create directory " + EventFormatterConstants.TM_ELE_DIRECTORY + " to add tenant specific transport adaptor :" + eventFormatterName);
                }
            }
            String pathInFileSystem = directory.getAbsolutePath() + File.separator + eventFormatterName + ".xml";
            EventFormatterConfigurationFilesystemInvoker.save(omElement, pathInFileSystem, axisConfiguration);

        }

    }

    @Override
    public void undeployActiveEventFormatterConfiguration(String eventFormatterName,
                                                          AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        String filePath = getFilePath(tenantId, eventFormatterName);
        if (filePath != null) {
            EventFormatterConfigurationFilesystemInvoker.delete(filePath, axisConfiguration);
        } else {
            throw new EventFormatterConfigurationException("Couldn't undeploy the Event Formatter configuration : " + eventFormatterName);
        }

    }

    @Override
    public void undeployInactiveEventFormatterConfiguration(String filePath,
                                                            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        EventFormatterConfigurationFilesystemInvoker.delete(filePath, axisConfiguration);
    }

    @Override
    public void editInactiveEventFormatterConfiguration(
            String eventFormatterConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        editEvenTFormatterConfiguration(filePath, axisConfiguration, eventFormatterConfiguration, null);
    }

    @Override
    public void editActiveEventFormatterConfiguration(String eventFormatterConfiguration,
                                                      String eventFormatterName,
                                                      AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        String pathInFileSystem = getFilePath(tenantId, eventFormatterName);
        editEvenTFormatterConfiguration(pathInFileSystem, axisConfiguration, eventFormatterConfiguration, eventFormatterName);

    }

    @Override
    public EventFormatterConfiguration getActiveEventFormatterConfiguration(
            String eventFormatterName,
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
    public List<EventFormatterConfiguration> getAllActiveEventFormatterConfiguration(
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

    @Override
    public List<EventFormatterConfigurationFile> getAllInactiveEventFormatterConfiguration(
            AxisConfiguration axisConfiguration) {

        List<EventFormatterConfigurationFile> undeployedEventFormatterFileList = new ArrayList<EventFormatterConfigurationFile>();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (eventFormatterConfigurationFileMap.get(tenantId) != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileMap.get(tenantId)) {
                if (!eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) {
                    undeployedEventFormatterFileList.add(eventFormatterConfigurationFile);
                }
            }
        }
        return undeployedEventFormatterFileList;
    }


    @Override
    public String getInactiveEventFormatterConfigurationContent(String filePath)
            throws EventFormatterConfigurationException {
        return readEventFormatterConfigurationFile(filePath);
    }

    @Override
    public String getActiveEventFormatterConfigurationContent(String eventFormatterName,
                                                              AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {


        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        String filePath = getFilePath(tenantId, eventFormatterName);
        return readEventFormatterConfigurationFile(filePath);
    }


    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        List<String> streamList = new ArrayList<String>();

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<EventSource> eventSourceList = EventFormatterServiceValueHolder.getEventSourceList();
        if (eventSourceList != null) {
            for (EventSource eventSource : eventSourceList) {
                List<String> streamNameList = eventSource.getAllStreamId(tenantId);
                if (streamNameList != null) {
                    for (String streamName : streamNameList) {
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

        return eventSource.getStreamDefinition(streamDetail[0], streamDetail[1], tenantId);
    }


    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventFormatterConfigurationException {
        RegistryService registryService = EventFormatterServiceValueHolder.getRegistryService();

        String registryData;
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
                registryData = (RegistryUtils.decodeBytes((byte[]) registryResource.getContent()));
            } else {
                throw new EventFormatterConfigurationException("Resource couldn't found from registry at " + resourcePath);
            }

        } catch (RegistryException e) {
            throw new EventFormatterConfigurationException("Error while retrieving the resource from registry at " + resourcePath, e);
        }
        return registryData;
    }

    @Override
    public void setStatisticsEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
                                     boolean flag)
            throws EventFormatterConfigurationException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getActiveEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableStatistics(flag);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    @Override
    public void setTraceEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
                                boolean flag)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getActiveEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableTracing(flag);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    //Non-Interface public methods

    public boolean checkEventFormatterValidity(int tenantId, String eventFormatterName) {

        if (eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
            if (eventFormatterConfigurationFileList != null) {
                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) && (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
                        log.error("Event Formatter " + eventFormatterName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void addEventFormatterConfigurationFile(int tenantId,
                                                   EventFormatterConfigurationFile eventFormatterConfigurationFile) {

        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);

        if (eventFormatterConfigurationFileList == null) {
            eventFormatterConfigurationFileList = new ArrayList<EventFormatterConfigurationFile>();
        } else {
            for (EventFormatterConfigurationFile anEventFormatterConfigurationFileList : eventFormatterConfigurationFileList) {
                if (anEventFormatterConfigurationFileList.getFilePath().equals(eventFormatterConfigurationFile.getFilePath())) {
                    return;
                }
            }
        }
        eventFormatterConfigurationFileList.add(eventFormatterConfigurationFile);
        eventFormatterConfigurationFileMap.put(tenantId, eventFormatterConfigurationFileList);
    }

    public void addEventFormatterConfiguration(
            int tenantId, EventFormatterConfiguration eventFormatterConfiguration)
            throws EventFormatterConfigurationException {

        Map<String, EventFormatter> eventFormatterConfigurationMap
                = tenantSpecificEventFormatterConfigurationMap.get(tenantId);

        if (eventFormatterConfigurationMap == null) {
            eventFormatterConfigurationMap = new ConcurrentHashMap<String, EventFormatter>();
        }

        EventFormatter eventFormatter = new EventFormatter(eventFormatterConfiguration, tenantId);
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
                            EventFormatter eventFormatter = tenantSpecificEventFormatterConfigurationMap.get(tenantId).get(eventFormatterName);
                            List<EventSource> eventSourceList = eventFormatter.getEventSourceList();
                            for (EventSource eventSource : eventSourceList) {
                                eventSource.unsubscribe(tenantId, eventFormatter.getStreamId(), eventFormatter.getEventFormatterListener());
                            }

                            tenantSpecificEventFormatterConfigurationMap.get(tenantId).remove(eventFormatterName);
                        }
                    }
                    eventFormatterConfigurationFileList.remove(eventFormatterConfigurationFile);
                    return;
                }
            }
        }
    }

    public void activateInactiveEventFormatterConfiguration(int tenantId, String dependency)
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
                    }
                }
            }
        }
        for (EventFormatterConfigurationFile eventFormatterConfigurationFile : fileList) {
            try {
                EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFilePath(), eventFormatterConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event formatter configuration file : " + new File(eventFormatterConfigurationFile.getFilePath()).getName());
            }
        }

    }

    public void deactivateActiveEventFormatterConfiguration(int tenantId, String dependency)
            throws EventFormatterConfigurationException {

        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();
        if (tenantSpecificEventFormatterConfigurationMap != null && tenantSpecificEventFormatterConfigurationMap.size() > 0) {
            Map<String, EventFormatter> eventFormatterMap = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
            if (eventFormatterMap != null) {
                for (EventFormatter eventFormatter : eventFormatterMap.values()) {
                    String streamNameWithVersion = eventFormatter.getEventFormatterConfiguration().getFromStreamName() + ":" + eventFormatter.getEventFormatterConfiguration().getFromStreamVersion();
                    String transportAdaptorName = eventFormatter.getEventFormatterConfiguration().getToPropertyConfiguration().getTransportAdaptorName();
                    if (streamNameWithVersion.equals(dependency) || transportAdaptorName.equals(dependency)) {
                        EventFormatterConfigurationFile eventFormatterConfigurationFile = getEventFormatterConfigurationFile(eventFormatter.getEventFormatterConfiguration().getEventFormatterName(), tenantId);
                        if (eventFormatterConfigurationFile != null) {
                            fileList.add(eventFormatterConfigurationFile);
                        }
                    }
                }
            }
        }

        for (EventFormatterConfigurationFile eventFormatterConfigurationFile : fileList) {
            EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFilePath(), eventFormatterConfigurationFile.getAxisConfiguration());
            log.info("EventFormatter  is undeployed because dependency : " + eventFormatterConfigurationFile.getEventFormatterName() + " , dependency couldn't found : " + dependency);
        }
    }

    //Private Methods are below

    private void editTracingStatistics(
            EventFormatterConfiguration eventFormatterConfiguration,
            String eventFormatterName, int tenantId, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        String pathInFileSystem = getFilePath(tenantId, eventFormatterName);
        undeployActiveEventFormatterConfiguration(eventFormatterName, axisConfiguration);
        OMElement omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
        EventFormatterConfigurationFilesystemInvoker.save(omElement, pathInFileSystem, axisConfiguration);
    }

    private String getFilePath(int tenantId, String eventFormatterName) {

        if (eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
            if (eventFormatterConfigurationFileList != null) {
                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName))) {
                        return eventFormatterConfigurationFile.getFilePath();
                    }
                }
            }
        }
        return null;
    }

    private String readEventFormatterConfigurationFile(String filePath)
            throws EventFormatterConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventFormatterConfigurationException("Event formatter file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventFormatterConfigurationException("Cannot read the Event Formatter file : " + e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file : " + e.getMessage(), e);
            }
        }
        return stringBuilder.toString().trim();
    }

    private void editEvenTFormatterConfiguration(String pathInFileSystem,
                                                 AxisConfiguration axisConfiguration,
                                                 String eventFormatterConfiguration,
                                                 String eventFormatterName)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventFormatterConfiguration);
            omElement.toString();
            EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
            String mappingType = EventFormatterConfigurationHelper.getOutputMappingType(omElement);
            if (mappingType != null) {
                EventFormatterConfiguration eventFormatterConfigurationObject = FormatterConfigurationBuilder.getEventFormatterConfiguration(omElement, tenantId, mappingType);
                if (!(eventFormatterConfigurationObject.getEventFormatterName().equals(eventFormatterName))) {
                    if (checkEventFormatterValidity(tenantId, eventFormatterConfigurationObject.getEventFormatterName())) {
                        EventFormatterConfigurationFilesystemInvoker.delete(pathInFileSystem, axisConfiguration);
                        EventFormatterConfigurationFilesystemInvoker.save(omElement, pathInFileSystem, axisConfiguration);
                    } else {
                        throw new EventFormatterConfigurationException("There is a Event Formatter with the same name");
                    }
                } else {
                    EventFormatterConfigurationFilesystemInvoker.delete(pathInFileSystem, axisConfiguration);
                    EventFormatterConfigurationFilesystemInvoker.save(omElement, pathInFileSystem, axisConfiguration);
                }
            }

        } catch (XMLStreamException e) {
            throw new EventFormatterConfigurationException("Not a valid xml object : " + e.getMessage(), e);
        }

    }

    private EventSource getEventSource(String streamNameWithVersion, int tenantId) {
        List<EventSource> eventSourceList = EventFormatterServiceValueHolder.getEventSourceList();

        EventSource eventSource = null;
        for (EventSource currentEventSource : eventSourceList) {
            List<String> streamList = currentEventSource.getAllStreamId(tenantId);
            if (streamList != null) {
                for (String stream : streamList) {
                    if (stream.equals(streamNameWithVersion)) {
                        eventSource = currentEventSource;
                        break;
                    }
                }
            }
        }
        return eventSource;
    }

    private EventFormatterConfigurationFile getEventFormatterConfigurationFile(
            String eventFormatterName, int tenantId) {
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);

        if (eventFormatterConfigurationFileList != null) {
            Iterator<EventFormatterConfigurationFile> eventFormatterConfigurationFileIterator = eventFormatterConfigurationFileList.iterator();
            while (eventFormatterConfigurationFileIterator.hasNext()) {
                EventFormatterConfigurationFile eventFormatterConfigurationFile = eventFormatterConfigurationFileIterator.next();
                if (eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) {
                    return eventFormatterConfigurationFile;
                }
            }
        }
        return null;

    }


}