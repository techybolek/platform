package org.wso2.carbon.event.builder.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
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
     * @param axisConfiguration
     */
    public void unsubsribe(StreamDefinition streamDefinition, EventListener eventListener, AxisConfiguration axisConfiguration);

    /**
     * Used to add a new Event Builder Configuration instance to the system. An event builder configuration instance represents the
     * details of a particular event builder.
     *
     * @param eventBuilderConfiguration - event builder configuration to be added
     * @param axisConfiguration
     */
    public void addEventBuilder(EventBuilder eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;

    /**
     * Removes the event builder configuration instance from the system.
     *
     * @param eventBuilder              - eventBuilder of the event builder configuration to be removed
     * @param axisConfiguration
     */
    public void removeEventBuilder(EventBuilder eventBuilder,
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
     * Returns the transport configuration for the given streamId
     *
     * @param streamId              - transport configuration streamId
     * @param axisConfiguration
     * @return - transport configuration
     */
    public EventBuilder getEventBuilder(String streamId, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;

    /**
     * Returns a {@link List} of stream definition ids.
     * @param axisConfiguration
     * @return
     */
    public List<String> getStreamDefinitionsAsString(AxisConfiguration axisConfiguration);

    /**
     * Returns a {@link List} of {@link StreamDefinition} objects that are currently accessible for the specified AxisConfiguration
     * @return
     */
    public List<StreamDefinition> getStreamDefinitions(AxisConfiguration axisConfiguration);

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of EventBuilderConfigurationFile
     */
    public List<EventBuilderConfigurationFile> getUnDeployedFiles(AxisConfiguration axisConfiguration);

}
