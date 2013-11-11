package org.wso2.carbon.integration.test.util;

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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.automation.api.clients.cep.*;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.stub.types.EventBuilderPropertyDto;
import org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.SiddhiConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto;
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto;

import java.rmi.RemoteException;

public class ConfigurationUtil {

    private static ConfigurationUtil configurationUtil;
    private EventFormatterAdminServiceClient eventFormatterAdminServiceClient;
    private EventBuilderAdminServiceClient eventBuilderAdminServiceClient;
    private EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    private InputEventAdaptorManagerAdminServiceClient inputEventAdaptorManagerAdminServiceClient;
    private OutputEventAdaptorManagerAdminServiceClient outputEventAdaptorManagerAdminServiceClient;
    private EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;

    private ConfigurationUtil() {
    }

    public static ConfigurationUtil getConfigurationUtil() {
        if (configurationUtil == null) {
            configurationUtil = new ConfigurationUtil();
        }

        return configurationUtil;
    }

    public EventStreamManagerAdminServiceClient getEventStreamManagerAdminServiceClient() {
        return eventStreamManagerAdminServiceClient;
    }

    public EventFormatterAdminServiceClient getEventFormatterAdminServiceClient() {
        return eventFormatterAdminServiceClient;
    }

    public EventBuilderAdminServiceClient getEventBuilderAdminServiceClient() {
        return eventBuilderAdminServiceClient;
    }

    public EventProcessorAdminServiceClient getEventProcessorAdminServiceClient() {
        return eventProcessorAdminServiceClient;
    }

    public InputEventAdaptorManagerAdminServiceClient getInputEventAdaptorManagerAdminServiceClient() {
        return inputEventAdaptorManagerAdminServiceClient;
    }

    public OutputEventAdaptorManagerAdminServiceClient getOutputEventAdaptorManagerAdminServiceClient() {
        return outputEventAdaptorManagerAdminServiceClient;
    }

    public void initEventFormatter(UserInfo userInfo, EnvironmentVariables cepServer,
                                   String loggedInSessionCookie) throws AxisFault {
        eventFormatterAdminServiceClient = new EventFormatterAdminServiceClient(cepServer.getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = eventFormatterAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    public void initEventBuilder(UserInfo userInfo, EnvironmentVariables cepServer,
                                 String loggedInSessionCookie) throws AxisFault {
        eventBuilderAdminServiceClient = new EventBuilderAdminServiceClient(cepServer.getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = eventBuilderAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    public void initEventProcessor(UserInfo userInfo, EnvironmentVariables cepServer,
                                   String loggedInSessionCookie) throws AxisFault {
        eventProcessorAdminServiceClient = new EventProcessorAdminServiceClient(cepServer.getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = eventProcessorAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    public void initOutputEventAdapter(UserInfo userInfo, EnvironmentVariables cepServer,
                                       String loggedInSessionCookie) throws AxisFault {
        outputEventAdaptorManagerAdminServiceClient = new OutputEventAdaptorManagerAdminServiceClient(cepServer.getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = outputEventAdaptorManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    public void initInputEventAdapter(UserInfo userInfo, EnvironmentVariables cepServer,
                                      String loggedInSessionCookie) throws AxisFault {
        inputEventAdaptorManagerAdminServiceClient = new InputEventAdaptorManagerAdminServiceClient(cepServer.getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = inputEventAdaptorManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    public void initEventStreamManager(UserInfo userInfo, EnvironmentVariables cepServer,
                                       String loggedInSessionCookie) throws AxisFault {
        eventStreamManagerAdminServiceClient = new EventStreamManagerAdminServiceClient(cepServer.getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        ServiceClient client = eventStreamManagerAdminServiceClient._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);
    }

    public void addInEventStream() throws RemoteException, InterruptedException {
        EventStreamAttributeDto payloadDto = new EventStreamAttributeDto();
        payloadDto.setAttributeName("testProperty");
        payloadDto.setAttributeType("string");

        EventStreamAttributeDto[] payloadData = new EventStreamAttributeDto[] {payloadDto};
        eventStreamManagerAdminServiceClient.addEventStream("InStream","1.0.0", null, null, payloadData, "This is a input test stream", "test");
    }

    public void addOutEventStream() throws RemoteException, InterruptedException {
        EventStreamAttributeDto payloadDto = new EventStreamAttributeDto();
        payloadDto.setAttributeName("testProperty");
        payloadDto.setAttributeType("string");

        EventStreamAttributeDto[] payloadData = new EventStreamAttributeDto[] {payloadDto};
        eventStreamManagerAdminServiceClient.addEventStream("OutStream","1.0.0", null, null, payloadData, "This is a output test stream", "test");
    }

    public void addAnalyticsStream() throws RemoteException, InterruptedException {
        EventStreamAttributeDto payloadDto = new EventStreamAttributeDto();
        payloadDto.setAttributeName("testProperty");
        payloadDto.setAttributeType("string");

        EventStreamAttributeDto[] payloadData = new EventStreamAttributeDto[] {payloadDto};
        eventStreamManagerAdminServiceClient.addEventStream("OutStream","1.0.0", null, null, payloadData, "This is a output test stream", "test");
    }

    public void addInputEventAdaptor() throws RemoteException, InterruptedException {
        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration("wso2EventReceiver", "wso2event", new InputEventAdaptorPropertyDto[0]);
    }

    public void addEventBuilder() throws RemoteException, InterruptedException {
        EventBuilderConfigurationDto eventBuilderConfigurationDto = new EventBuilderConfigurationDto();
        eventBuilderConfigurationDto.setEventBuilderConfigName("testEventBuilder");
        eventBuilderConfigurationDto.setInputMappingType("wso2event");
        eventBuilderConfigurationDto.setToStreamName("InStream");
        eventBuilderConfigurationDto.setToStreamVersion("1.0.0");
        eventBuilderConfigurationDto.setInputEventAdaptorName("wso2EventReceiver");
        eventBuilderConfigurationDto.setInputEventAdaptorType("wso2event");
        EventBuilderPropertyDto streamName = new EventBuilderPropertyDto();
        streamName.setKey("stream_from");
        streamName.setValue("InStream");
        EventBuilderPropertyDto version = new EventBuilderPropertyDto();
        version.setKey("version_from");
        version.setValue("1.0.0");
        EventBuilderPropertyDto testMapping = new EventBuilderPropertyDto();
        testMapping.setKey("testProperty_mapping");
        testMapping.setValue("testProperty");
        testMapping.setPropertyType("string");
        EventBuilderPropertyDto customMappingEnabled = new EventBuilderPropertyDto();
        customMappingEnabled.setKey("specific_customMappingValue_mapping");
        customMappingEnabled.setValue("disable");
        EventBuilderPropertyDto[] eventBuilderPropertyDtos = new EventBuilderPropertyDto[]{streamName, version, testMapping, customMappingEnabled};

        eventBuilderConfigurationDto.setEventBuilderProperties(eventBuilderPropertyDtos);

        eventBuilderAdminServiceClient.addEventBuilderConfiguration(eventBuilderConfigurationDto);
    }

    public void addEventProcessor() throws RemoteException, InterruptedException {
        ExecutionPlanConfigurationDto executionPlanConfigurationDto = new ExecutionPlanConfigurationDto();
        executionPlanConfigurationDto.setName("TestExecutionPlan1");
        StreamConfigurationDto inStream = new StreamConfigurationDto();
        inStream.setSiddhiStreamName("InStream");
        inStream.setStreamId("InStream:1.0.0");
        executionPlanConfigurationDto.setImportedStreams(new StreamConfigurationDto[]{inStream});

        SiddhiConfigurationDto siddhiPersistenceConfigDto = new SiddhiConfigurationDto();
        siddhiPersistenceConfigDto.setKey("siddhi.persistence.snapshot.time.interval.minutes");
        siddhiPersistenceConfigDto.setValue("0");
        executionPlanConfigurationDto.addSiddhiConfigurations(siddhiPersistenceConfigDto);

        executionPlanConfigurationDto.setQueryExpressions("from InStream select * insert into OutStream; ");

        StreamConfigurationDto outStream = new StreamConfigurationDto();
        outStream.setSiddhiStreamName("OutStream");
        outStream.setStreamId("OutStream:1.0.0");
        executionPlanConfigurationDto.setExportedStreams(new StreamConfigurationDto[]{outStream});

        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanConfigurationDto);
    }

    public void addEventFormatter() throws RemoteException, InterruptedException {
        EventFormatterPropertyDto eventFormatterPropertyDto = new EventFormatterPropertyDto();
        eventFormatterPropertyDto.setKey("topic");
        eventFormatterPropertyDto.setValue("TargetTopic");
        EventFormatterPropertyDto eventFormatterPropertyDtos[] = new EventFormatterPropertyDto[]{eventFormatterPropertyDto};

        String xmlMapping = "<testData>{{testProperty}}</testData>";
        eventFormatterAdminServiceClient.addXMLEventFormatterConfiguration("xmlEventFormatter", "OutStream:1.0.0", "ws-local-sender", "ws-event-local", xmlMapping, eventFormatterPropertyDtos, "inline");
    }

    public void addOutputEventAdaptor() throws RemoteException, InterruptedException {
        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration("ws-local-sender", "ws-event-local", new OutputEventAdaptorPropertyDto[0]);
    }

    public void removeActiveInputEventAdaptor() throws RemoteException, InterruptedException {
        inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("wso2EventReceiver");
    }

    public void removeActiveEventBuilder() throws RemoteException, InterruptedException {
        eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("testEventBuilder");
    }

    public void removeActiveEventProcessor() throws RemoteException, InterruptedException {
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("TestExecutionPlan1");
    }

    public void removeActiveEventFormatter() throws RemoteException, InterruptedException {
        eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("xmlEventFormatter");
    }

    public void removeActiveOutputEventAdaptor() throws RemoteException, InterruptedException {
        outputEventAdaptorManagerAdminServiceClient.removeActiveOutputEventAdaptorConfiguration("ws-local-sender");
    }

    public void removeInActiveInputEventAdaptor() throws RemoteException, InterruptedException {
        inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("wso2EventReceiver");
    }

    public void removeInActiveEventBuilder() throws RemoteException, InterruptedException {
        eventBuilderAdminServiceClient.removeInactiveEventBuilderConfiguration("testEventBuilder.xml");
    }

    public void removeInActiveEventProcessor() throws RemoteException, InterruptedException {
        eventProcessorAdminServiceClient.removeInactiveExecutionPlan("TestExecutionPlan1.xml");
    }

    public void removeInActiveEventFormatter() throws RemoteException, InterruptedException {
        eventFormatterAdminServiceClient.removeInactiveEventFormatterConfiguration("xmlEventFormatter.xml");
    }

    public void removeInActiveOutputEventAdaptor()
            throws RemoteException, InterruptedException {
        outputEventAdaptorManagerAdminServiceClient.removeInactiveOutputEventAdaptorConfiguration("ws-local-sender.xml");
    }


}
