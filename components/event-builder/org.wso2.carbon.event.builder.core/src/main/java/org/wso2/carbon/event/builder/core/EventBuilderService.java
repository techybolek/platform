package org.wso2.carbon.event.builder.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

import java.util.List;

public interface EventBuilderService {

    /**
     * Subscribes to a particular event builder identified by the stream definition
     * @param streamDefinition
     * @param eventListener
     */
    public void subscribe(StreamDefinition streamDefinition, EventListener eventListener, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;

    /**
     * Unsubscribes from a particular event builder for the given stream definition and event listener
     * @param streamDefinition
     * @param eventListener
     */
    public void unsubsribe(StreamDefinition streamDefinition, EventListener eventListener);

    /**
     * Used to add a new Event Builder Configuration instance to the system. An event builder configuration instance represents the
     * details of a particular event builder.
     *
     * @param eventBuilderConfiguration - event builder configuration to be added
     * @param axisConfiguration
     */
    public void addEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;

    /**
     * Removes the event builder configuration instance from the system.
     *
     * @param name              - name of the event builder configuration to be removed
     * @param axisConfiguration
     */
    public void removeEventBuilder(String name,
                                   AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;

    /**
     * Getting all the event builder instance details. this is used to display all the
     * transport configuration instances.
     *
     * @param axisConfiguration
     * @return - list of available transport configuration
     */
    public List<EventBuilder> getAllEventBuilders(AxisConfiguration axisConfiguration);

    /**
     * Returns the transport configuration for the given name
     *
     * @param name              - transport configuration name
     * @param axisConfiguration
     * @return - transport configuration
     */
    public EventBuilder getEventBuilder(String name, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;

    public List<String> getStreamDefinitionsAsString(AxisConfiguration axisConfiguration);

    public List<StreamDefinition> getStreamDefinitions();

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of EventBuilderConfigurationFile
     */
    public List<EventBuilderConfigurationFile> getUnDeployedFiles(AxisConfiguration axisConfiguration);

}
