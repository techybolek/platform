package org.wso2.carbon.event.builder.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.*;
import org.wso2.carbon.event.builder.core.EventListener;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventBuilderService implements EventBuilderService {

    private static final Log log = LogFactory.getLog(CarbonEventBuilderService.class);

    private Map<Integer, Map<StreamDefinition, EventBuilder>> tenantSpecificEventBuilderMap;
    private Map<Integer, List<EventBuilderConfigurationFile>> eventBuilderConfigFileMap;
    private Map<Integer, List<String>> cancelDeploymentMap;
    private Map<Integer, List<String>> cancelUnDeploymentMap;

    public CarbonEventBuilderService() {
        tenantSpecificEventBuilderMap = new ConcurrentHashMap<Integer, Map<StreamDefinition, EventBuilder>>();
        eventBuilderConfigFileMap = new ConcurrentHashMap<Integer, List<EventBuilderConfigurationFile>>();
        cancelDeploymentMap = new ConcurrentHashMap<Integer, List<String>>();
        cancelUnDeploymentMap = new ConcurrentHashMap<Integer, List<String>>();
    }

    @Override
    public void subscribe(StreamDefinition streamDefinition, EventListener eventListener, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventBuilder eventBuilder = tenantSpecificEventBuilderMap.get(tenantId).get(streamDefinition);
        if (eventBuilder == null) {
            throw new EventBuilderConfigurationException("No event builder exists for the stream definition '" + streamDefinition.getStreamId() + "' provided for this tenant");
        }
        eventBuilder.subscribe(eventListener, axisConfiguration);
    }

    @Override
    public void unsubsribe(StreamDefinition streamDefinition, EventListener eventListener, AxisConfiguration axisConfiguration) {
        if (tenantSpecificEventBuilderMap.get(streamDefinition) != null) {
            int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
            tenantSpecificEventBuilderMap.get(tenantId).get(streamDefinition).unsubscribe(eventListener);
        }
    }

    @Override
    public void addEventBuilder(EventBuilder eventBuilder, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<StreamDefinition, EventBuilder> eventBuilderMap
                = tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderMap == null) {
            eventBuilderMap = new ConcurrentHashMap<StreamDefinition, EventBuilder>();
            tenantSpecificEventBuilderMap.put(tenantId, eventBuilderMap);
        }
        eventBuilderMap.put(eventBuilder.getEventBuilderConfiguration().getStreamDefinition(), eventBuilder);

        //TODO Invokes test code. Need to remove
        testSubscription(eventBuilder, axisConfiguration);
    }

    private void testSubscription(EventBuilder eventBuilder, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        StreamDefinition streamDefinition = eventBuilder.getEventBuilderConfiguration().getStreamDefinition();
/*
        tenantSpecificEventBuilderMap.put(streamDefinition, new TupleInputEventBuilder(eventBuilder));
        subscribe(streamDefinition, new Wso2EventListener() {
            @Override
            public void onEvent(Event event) {
                log.debug(event);

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
*/

        Map<StreamDefinition, EventBuilder> eventBuilderMap = tenantSpecificEventBuilderMap.get(tenantId);
        eventBuilderMap.put(streamDefinition, new TupleInputEventBuilder(eventBuilder.getEventBuilderConfiguration()));
        subscribe(streamDefinition, new BasicEventListener() {
            @Override
            public void onEvent(Object[] event) {
                log.debug(event);
            }

            @Override
            public void onAddDefinition(Object definition) {
                log.debug(definition);
            }

            @Override
            public void onRemoveDefinition(Object definition) {
                log.debug(definition);
            }
        }, axisConfiguration);

    }

    @Override
    public void removeEventBuilder(EventBuilder eventBuilder, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventBuilder removedEventBuilder = null;
        Map<StreamDefinition, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if(eventBuilderMap != null) {
            removedEventBuilder = eventBuilderMap.remove(eventBuilder.getEventBuilderConfiguration().getStreamDefinition());
        }

        if(removedEventBuilder == null) {
            throw new EventBuilderConfigurationException("Could not find the specified event builder for removal for the given axis configuration");
        }
    }

    @Override
    public List<EventBuilder> getAllEventBuilders(AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        List<EventBuilder> eventBuilders = null;
        Map<StreamDefinition, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if(eventBuilderMap != null) {
            eventBuilders = new ArrayList<EventBuilder>(eventBuilderMap.values());
        }

        return eventBuilders;
    }

    @Override
    public EventBuilder getEventBuilder(String streamId, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        EventBuilder eventBuilder = null;
        Map<StreamDefinition, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if(eventBuilderMap != null) {
            for(StreamDefinition streamDefinition: eventBuilderMap.keySet()) {
                if(streamDefinition.getStreamId().equals(streamId)) {
                    eventBuilder = eventBuilderMap.get(streamDefinition);
                }
            }
        }

        return eventBuilder;
    }

    @Override
    public List<String> getStreamDefinitionsAsString(AxisConfiguration axisConfiguration) {
        List<StreamDefinition> streamDefinitions = getStreamDefinitions(axisConfiguration);
        List<String> streamDefinitionsAsString = null;
        if(streamDefinitions != null && !streamDefinitions.isEmpty()) {
            streamDefinitionsAsString = new ArrayList<String>();
            for(StreamDefinition streamDefinition: streamDefinitions) {
                streamDefinitionsAsString.add(streamDefinition.getStreamId());
            }
        }

        return streamDefinitionsAsString;
    }

    @Override
    public List<StreamDefinition> getStreamDefinitions(AxisConfiguration axisConfiguration) {
        List<StreamDefinition> streamDefinitions = null;
        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<StreamDefinition, EventBuilder> eventBuilderMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if(eventBuilderMap != null && !eventBuilderMap.isEmpty()) {
            streamDefinitions = new ArrayList<StreamDefinition>(eventBuilderMap.keySet());
        }

        return streamDefinitions;
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
                    tenantSpecificEventBuilderMap.get(tenantId).remove(eventBuilderConfigurationFile.getEventBuilderName());
                }
                eventBuilderConfigurationFileList.remove(eventBuilderConfigurationFile);
                break;
            }
        }
    }

    public boolean checkEventBuilderValidity(int tenantId, String eventBuilderName) {
        if (eventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderConfigFileMap.get(tenantId);
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileList) {
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
            for (String anEventBuilderConfigurationList : eventBuilderConfigurationList) {
                if (anEventBuilderConfigurationList.equals(eventBuilderName)) {
                    eventBuilderConfigurationList.remove(eventBuilderName);
                    break;
                }
            }
        }
    }

    public void removeFromCancelUnDeployMap(int tenantId, String pathInFileSystem) {
        if (cancelUnDeploymentMap.size() != 0) {
            List<String> eventBuilderList = cancelUnDeploymentMap.get(tenantId);
            for (String anEventBuilderList : eventBuilderList) {
                if (anEventBuilderList.equals(pathInFileSystem)) {
                    eventBuilderList.remove(pathInFileSystem);
                    break;
                }
            }
        }
    }
}
