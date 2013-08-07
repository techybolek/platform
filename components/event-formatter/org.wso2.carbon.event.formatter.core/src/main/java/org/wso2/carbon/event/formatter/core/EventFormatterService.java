package org.wso2.carbon.event.formatter.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;

import java.util.List;

public interface EventFormatterService {

    public List<EventFormatterConfiguration> getAllEventFormatterConfiguration(
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public void removeEventFormatterConfiguration(String eventFormatterName,
                                                  AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public List<EventFormatterConfigurationFile> getNotDeployedEventFormatterConfigurationFiles(
            AxisConfiguration axisConfiguration);

    public void removeEventFormatterConfigurationFile(String filePath,
                                                      AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public String getNotDeployedEventFormatterConfigurationFile(String filePath)
            throws EventFormatterConfigurationException;

    public void editNotDeployedEventFormatterConfigurationFile(
            String eventFormatterConfiguration,
            String filePath,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public String getEventFormatterConfigurationFile(String eventFormatterName,
                                                     AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public void editEventFormatterConfigurationFile(String eventFormatterConfiguration,
                                                    String eventFormatterName,
                                                    AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;


    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public void saveEventFormatterConfiguration(
            EventFormatterConfiguration eventFormatterConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public String getRegistryResourceContent(String resourcePath, int tenantId) throws EventFormatterConfigurationException;

    public EventFormatterConfiguration getEventFormatterConfiguration(String eventFormatterName, int tenantId) throws EventFormatterConfigurationException;

    public void disableStatistics(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public void enableStatistics(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public void disableTracing(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public void enableTracing(String eventFormatterName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

}
