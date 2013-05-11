package org.wso2.carbon.event.builder.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.*;
import org.wso2.carbon.event.builder.core.EventListener;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventBuilderService implements EventBuilderService {

    private static final Log log = LogFactory.getLog(CarbonEventBuilderService.class);
    private Map<StreamDefinition, EventBuilder> eventBuilderMap = new HashMap<StreamDefinition, EventBuilder>();
    private Map<Integer, Map<String, EventBuilderConfiguration>> tenantSpecificEventBuilderConfigMap;
    private Map<Integer, List<EventBuilderConfigurationFile>> eventBuilderConfigFileMap;
    private Map<Integer, List<String>> cancelDeploymentMap;
    private Map<Integer, List<String>> cancelUnDeploymentMap;

    public CarbonEventBuilderService() {
        tenantSpecificEventBuilderConfigMap = new ConcurrentHashMap<Integer, Map<String, EventBuilderConfiguration>>();
        eventBuilderConfigFileMap = new ConcurrentHashMap<Integer, List<EventBuilderConfigurationFile>>();
        cancelDeploymentMap = new ConcurrentHashMap<Integer, List<String>>();
        cancelUnDeploymentMap = new ConcurrentHashMap<Integer, List<String>>();
    }

    @Override
    public void subscribe(StreamDefinition streamDefinition, EventListener eventListener, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        //TODO This is test code. Need to implement properly
        EventBuilder eventBuilder = eventBuilderMap.get(streamDefinition);
        if (eventBuilder == null) {
            throw new EventBuilderConfigurationException("No event builder exists for the stream definition " + streamDefinition + " provided for this tenant");
        }
        eventBuilder.subscribe(eventListener, axisConfiguration);
    }

    @Override
    public void unsubsribe(StreamDefinition streamDefinition, EventListener eventListener) {
        if (eventBuilderMap.get(streamDefinition) != null) {
            eventBuilderMap.get(streamDefinition).unsubscribe(eventListener);
        }
    }

    @Override
    public void addEventBuilder(EventBuilderConfiguration eventBuilderConfiguration, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<String, EventBuilderConfiguration> eventBuilderConfigurationMap
                = tenantSpecificEventBuilderConfigMap.get(tenantId);
        if (eventBuilderConfigurationMap == null) {
            eventBuilderConfigurationMap = new ConcurrentHashMap<String, EventBuilderConfiguration>();
            tenantSpecificEventBuilderConfigMap.put(tenantId, eventBuilderConfigurationMap);
        }
        eventBuilderConfigurationMap.put(eventBuilderConfiguration.getName(), eventBuilderConfiguration);
        testSubscription(eventBuilderConfiguration, axisConfiguration);
    }

    private void testSubscription(EventBuilderConfiguration eventBuilderConfiguration, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        InputTransportMessageConfiguration messageConfiguration = eventBuilderConfiguration.getInputTransportMessageConfiguration();
        String streamName = messageConfiguration.getInputMessageProperties().get("streamName");
        String version = messageConfiguration.getInputMessageProperties().get("version");

        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(streamName, version);
            eventBuilderMap.put(streamDefinition, new TupleInputEventBuilder(eventBuilderConfiguration));
            subscribe(streamDefinition, new BasicEventListener() {
                @Override
                public void onEvent(Object[] event) {
                    System.out.println("Yippee!!! " + event);
                }

                @Override
                public void onAddDefinition(Object definition) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void onRemoveDefinition(Object definition) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            }, axisConfiguration);

        } catch (MalformedStreamDefinitionException e) {
            log.error("Cannot define stream:" + e.getMessage());
            throw new EventBuilderConfigurationException(e);
        }
    }

    @Override
    public void removeEventBuilder(String name, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<EventBuilder> getAllEventBuilders(AxisConfiguration axisConfiguration) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EventBuilder getEventBuilder(String name, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getStreamDefinitionsAsString(AxisConfiguration axisConfiguration) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<StreamDefinition> getStreamDefinitions() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<EventBuilderConfigurationFile> getUnDeployedFiles(AxisConfiguration axisConfiguration) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private String getFilePath(int tenantId, String transportAdaptorName) {
        if (eventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderConfigFileMap.get(tenantId);
            Iterator<EventBuilderConfigurationFile> eventBuilderConfigurationFileIterator = eventBuilderConfigurationFileList.iterator();

            while (eventBuilderConfigurationFileIterator.hasNext()) {
                EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileIterator.next();
                if ((eventBuilderConfigurationFile.getEventBuilderName().equals(transportAdaptorName))) {
                    return eventBuilderConfigurationFile.getFilePath();
                }
            }
        }
        return null;
    }

    private void addToCancelDeployMap(int tenantId, String pathInFileSystem) {
        if (cancelDeploymentMap.size() > 0) {
            List<String> eventBuilderNameList = cancelDeploymentMap.get(tenantId);
            if (eventBuilderNameList == null) {
                eventBuilderNameList = new ArrayList<String>();
                eventBuilderNameList.add(pathInFileSystem);
                cancelDeploymentMap.put(tenantId, eventBuilderNameList);
            } else {
                eventBuilderNameList.add(pathInFileSystem);
            }
        }
    }

    private void addToCancelUnDeployMap(int tenantId, String eventBuilderName) {
        if (cancelUnDeploymentMap.size() > 0) {
            List<String> transportAdaptorList = cancelUnDeploymentMap.get(tenantId);
            if (transportAdaptorList == null) {
                transportAdaptorList = new ArrayList<String>();
                transportAdaptorList.add(eventBuilderName);
                cancelUnDeploymentMap.put(tenantId, transportAdaptorList);

            } else {
                transportAdaptorList.add(eventBuilderName);
            }

        }
    }

    public void addFileConfiguration(int tenantId, String eventBuilderName, String filePath, boolean flag) {
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderConfigFileMap.get(tenantId);

        if (eventBuilderConfigurationFileList == null) {
            eventBuilderConfigurationFileList = new ArrayList<EventBuilderConfigurationFile>();
            EventBuilderConfigurationFile eventBuilderConfigurationFile = new EventBuilderConfigurationFile();
            eventBuilderConfigurationFile.setFilePath(filePath);
            eventBuilderConfigurationFile.setEventBuilderName(eventBuilderName);
            eventBuilderConfigurationFile.setSuccess(flag);
            eventBuilderConfigurationFileList.add(eventBuilderConfigurationFile);
            eventBuilderConfigFileMap.put(tenantId, eventBuilderConfigurationFileList);
        } else {
            EventBuilderConfigurationFile eventBuilderConfigurationFile = new EventBuilderConfigurationFile();
            eventBuilderConfigurationFile.setFilePath(filePath);
            eventBuilderConfigurationFile.setEventBuilderName(eventBuilderName);
            eventBuilderConfigurationFile.setSuccess(flag);
            eventBuilderConfigurationFileList.add(eventBuilderConfigurationFile);
            eventBuilderConfigFileMap.put(tenantId, eventBuilderConfigurationFileList);

        }

    }

    public void removeEventBuilderConfiguration(String filePath, int tenantId) {
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderConfigFileMap.get(tenantId);

        Iterator<EventBuilderConfigurationFile> eventBuilderConfigurationFileIterator = eventBuilderConfigurationFileList.iterator();
        while (eventBuilderConfigurationFileIterator.hasNext()) {

            EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileIterator.next();
            if ((eventBuilderConfigurationFile.getFilePath().equals(filePath))) {
                if (eventBuilderConfigurationFile.isSuccess()) {
                    tenantSpecificEventBuilderConfigMap.get(tenantId).remove(eventBuilderConfigurationFile.getEventBuilderName());
                }
                eventBuilderConfigurationFileList.remove(eventBuilderConfigurationFile);
                return;
            }
        }
    }

    public boolean checkEventBuilderValidity(int tenantId, String eventBuilderName) {
        if (eventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderConfigFileMap.get(tenantId);
            Iterator<EventBuilderConfigurationFile> eventBuilderConfigurationFileIterator = eventBuilderConfigurationFileList.iterator();
            while (eventBuilderConfigurationFileIterator.hasNext()) {
                EventBuilderConfigurationFile eventBuilderConfigurationFile = eventBuilderConfigurationFileIterator.next();
                if ((eventBuilderConfigurationFile.getEventBuilderName().equals(eventBuilderName)) && (eventBuilderConfigurationFile.isSuccess())) {
                    log.error("Event builder " + eventBuilderName + " is already registered with this tenant");
                    return false;
                }
            }
        }
        return true;
    }

    public void removeFromCancelDeployMap(int tenantId, String eventBuilderName) {
        if (cancelDeploymentMap.size() != 0) {
            List<String> eventBuilderConfigurationList = cancelDeploymentMap.get(tenantId);
            Iterator<String> eventBuilderConfigIterator = eventBuilderConfigurationList.iterator();
            while (eventBuilderConfigIterator.hasNext()) {
                if (eventBuilderConfigIterator.next().equals(eventBuilderName)) {
                    eventBuilderConfigurationList.remove(eventBuilderName);
                    return;
                }
            }
        }
    }

    public void removeFromCancelUnDeployMap(int tenantId, String pathInFileSystem) {
        if (cancelUnDeploymentMap.size() != 0) {
            List<String> eventBuilderList = cancelUnDeploymentMap.get(tenantId);
            Iterator<String> eventBuilderNameList = eventBuilderList.iterator();
            while (eventBuilderNameList.hasNext()) {
                if (eventBuilderNameList.next().equals(pathInFileSystem)) {
                    eventBuilderList.remove(pathInFileSystem);
                    return;
                }
            }
        }
    }


}
