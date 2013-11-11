package org.wso2.carbon.integration.test.processflow;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.cep.EventBuilderAdminServiceClient;
import org.wso2.carbon.automation.api.clients.cep.EventFormatterAdminServiceClient;
import org.wso2.carbon.automation.api.clients.cep.EventProcessorAdminServiceClient;
import org.wso2.carbon.automation.api.clients.cep.EventStreamManagerAdminServiceClient;
import org.wso2.carbon.automation.api.clients.cep.InputEventAdaptorManagerAdminServiceClient;
import org.wso2.carbon.automation.api.clients.cep.OutputEventAdaptorManagerAdminServiceClient;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.stub.types.EventBuilderPropertyDto;
import org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto;
import org.wso2.carbon.event.formatter.stub.types.EventOutputPropertyConfigurationDto;
import org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto;
import org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto;
import org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.SiddhiConfigurationDto;
import org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto;
import org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.integration.test.CEPIntegrationTest;
import org.wso2.carbon.integration.test.client.PhoneRetailAgent;
import org.wso2.carbon.integration.test.client.TestAgentServer;
import org.wso2.carbon.integration.test.util.ConfigurationUtil;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.RemoteException;

public class EventFlowTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(EventFlowTestCase.class);
    private ConfigurationUtil configurationUtil;
    private EventFormatterAdminServiceClient eventFormatterAdminServiceClient;
    private EventBuilderAdminServiceClient eventBuilderAdminServiceClient;
    private EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    private InputEventAdaptorManagerAdminServiceClient inputEventAdaptorManagerAdminServiceClient;
    private OutputEventAdaptorManagerAdminServiceClient outputEventAdaptorManagerAdminServiceClient;
    private EventStreamManagerAdminServiceClient eventStreamManagerAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws LoginAuthenticationExceptionException, RemoteException {
        super.init();
        String loggedInSessionCookie = cepServer.getSessionCookie();
        configurationUtil = ConfigurationUtil.getConfigurationUtil();
        configurationUtil.initInputEventAdapter(userInfo, cepServer, loggedInSessionCookie);
        configurationUtil.initOutputEventAdapter(userInfo, cepServer, loggedInSessionCookie);
        configurationUtil.initEventBuilder(userInfo, cepServer, loggedInSessionCookie);
        configurationUtil.initEventFormatter(userInfo, cepServer, loggedInSessionCookie);
        configurationUtil.initEventProcessor(userInfo, cepServer, loggedInSessionCookie);
        configurationUtil.initEventStreamManager(userInfo,cepServer, loggedInSessionCookie);

        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient();
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient();
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient();
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient();
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient();
        eventStreamManagerAdminServiceClient = configurationUtil.getEventStreamManagerAdminServiceClient();
    }

    //Scenario 1 adding order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file deployment order ITA, EB, EP, OTA, EF")
    public void addInputEventAdapterTestScenario1()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount();
        //configurationUtil.addInputEventAdaptor();

        inputEventAdaptorManagerAdminServiceClient.addInputEventAdaptorConfiguration("localEventReceiver", "wso2event", new InputEventAdaptorPropertyDto[0]);

        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdapterTestScenario1"})
    public void addEventBuilderTestScenario1() throws RemoteException, InterruptedException {

        log.info("=======================Adding a stream definition====================");

        EventStreamAttributeDto metaEventStreamAttributeDto1 = new EventStreamAttributeDto();
        metaEventStreamAttributeDto1.setAttributeName("ipAddress");
        metaEventStreamAttributeDto1.setAttributeType("string");

        EventStreamAttributeDto[] metaEventStreamAttributeDtos = new EventStreamAttributeDto[]{metaEventStreamAttributeDto1};

        EventStreamAttributeDto payloadEventStreamAttributeDto1 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto1.setAttributeName("userID");
        payloadEventStreamAttributeDto1.setAttributeType("string");

        EventStreamAttributeDto payloadEventStreamAttributeDto2= new EventStreamAttributeDto();
        payloadEventStreamAttributeDto2.setAttributeName("searchTerms");
        payloadEventStreamAttributeDto2.setAttributeType("string");

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos = new EventStreamAttributeDto[]{payloadEventStreamAttributeDto1,payloadEventStreamAttributeDto2};

        eventStreamManagerAdminServiceClient.addEventStream("analytics_Statistics","1.3.0",metaEventStreamAttributeDtos,null,payloadEventStreamAttributeDtos,"","");

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), 3);

        log.info("=======================Adding a stream definition====================");

        EventStreamAttributeDto metaEventStreamAttributeDto21 = new EventStreamAttributeDto();
        metaEventStreamAttributeDto21.setAttributeName("ipAddress");
        metaEventStreamAttributeDto21.setAttributeType("string");

        EventStreamAttributeDto[] metaEventStreamAttributeDtos2 = new EventStreamAttributeDto[]{metaEventStreamAttributeDto21};

        EventStreamAttributeDto payloadEventStreamAttributeDto21 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto21.setAttributeName("user");
        payloadEventStreamAttributeDto21.setAttributeType("string");

        EventStreamAttributeDto payloadEventStreamAttributeDto22= new EventStreamAttributeDto();
        payloadEventStreamAttributeDto22.setAttributeName("keywords");
        payloadEventStreamAttributeDto22.setAttributeType("string");

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos2 = new EventStreamAttributeDto[]{payloadEventStreamAttributeDto21,payloadEventStreamAttributeDto22};

        eventStreamManagerAdminServiceClient.addEventStream("summarizedStatistics","1.0.0",metaEventStreamAttributeDtos2,null,payloadEventStreamAttributeDtos2,"","");

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), 4);

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        //configurationUtil.addEventBuilder();

        EventBuilderConfigurationDto eventBuilderConfigurationDto = new EventBuilderConfigurationDto();
        eventBuilderConfigurationDto.setEventBuilderConfigName("wso2eventbuilder");
        eventBuilderConfigurationDto.setInputMappingType("wso2event");
        eventBuilderConfigurationDto.setToStreamName("summarizedStatistics");
        eventBuilderConfigurationDto.setToStreamVersion("1.0.0");
        eventBuilderConfigurationDto.setInputEventAdaptorName("localEventReceiver");
        eventBuilderConfigurationDto.setInputEventAdaptorType("wso2event");
        EventBuilderPropertyDto streamName = new EventBuilderPropertyDto();
        streamName.setKey("stream_from");
        streamName.setValue("analytics_Statistics");
        EventBuilderPropertyDto version = new EventBuilderPropertyDto();
        version.setKey("version_from");
        version.setValue("1.3.0");

        EventBuilderPropertyDto propertyMapping1 = new EventBuilderPropertyDto();
        propertyMapping1.setKey("meta_ipAddress_mapping");
        propertyMapping1.setValue("meta_ipAddress");
        propertyMapping1.setPropertyType("string");

        EventBuilderPropertyDto propertyMapping2 = new EventBuilderPropertyDto();
        propertyMapping2.setKey("userID_mapping");
        propertyMapping2.setValue("user");
        propertyMapping2.setPropertyType("string");

        EventBuilderPropertyDto propertyMapping3 = new EventBuilderPropertyDto();
        propertyMapping3.setKey("searchTerms_mapping");
        propertyMapping3.setValue("keywords");
        propertyMapping3.setPropertyType("string");

        EventBuilderPropertyDto customMappingEnabled = new EventBuilderPropertyDto();
        customMappingEnabled.setKey("specific_customMappingValue_mapping");
        customMappingEnabled.setValue("enable");

        EventBuilderPropertyDto[] eventBuilderPropertyDtos = new EventBuilderPropertyDto[]{streamName, version,
                propertyMapping1, propertyMapping2, propertyMapping3, customMappingEnabled};

        eventBuilderConfigurationDto.setEventBuilderProperties(eventBuilderPropertyDtos);

        eventBuilderAdminServiceClient.addEventBuilderConfiguration(eventBuilderConfigurationDto);


        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario1"})
    public void addEventProcessorTestScenario1() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount();
        //configurationUtil.addEventProcessor();

        ExecutionPlanConfigurationDto executionPlanConfigurationDto = new ExecutionPlanConfigurationDto();
        executionPlanConfigurationDto.setName("KPIAnalyzer");
        StreamConfigurationDto inStream = new StreamConfigurationDto();
        inStream.setSiddhiStreamName("summarizedStatistics");
        inStream.setStreamId("summarizedStatistics:1.0.0");

        executionPlanConfigurationDto.setImportedStreams(new StreamConfigurationDto[]{inStream});

        SiddhiConfigurationDto siddhiPersistenceConfigDto = new SiddhiConfigurationDto();
        siddhiPersistenceConfigDto.setKey("siddhi.persistence.snapshot.time.interval.minutes");
        siddhiPersistenceConfigDto.setValue("0");
        executionPlanConfigurationDto.addSiddhiConfigurations(siddhiPersistenceConfigDto);

        executionPlanConfigurationDto.setQueryExpressions("from summarizedStatistics insert into statisticsStream; ");

        StreamConfigurationDto outStream = new StreamConfigurationDto();
        outStream.setSiddhiStreamName("statisticsStream");
        outStream.setStreamId("statisticsStream:1.0.0");
        executionPlanConfigurationDto.setExportedStreams(new StreamConfigurationDto[]{outStream});

        eventProcessorAdminServiceClient.addExecutionPlan(executionPlanConfigurationDto);

        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventProcessorTestScenario1"})
    public void addOutputEventAdapterTestScenario1()
            throws RemoteException, InterruptedException {
        int startCount = outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount();
        Assert.assertEquals(startCount, 1);

        log.info("=======================Adding a Output event adaptor ======================= ");
        //configurationUtil.addOutputEventAdaptor();

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty1 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty1.setKey("username");
        inputEventAdaptorProperty1.setValue("admin");

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty2 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty2.setKey("receiverURL");
        inputEventAdaptorProperty2.setValue("tcp://localhost:7661");

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty3 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty3.setKey("password");
        inputEventAdaptorProperty3.setValue("admin");

        OutputEventAdaptorPropertyDto inputEventAdaptorProperty4 = new OutputEventAdaptorPropertyDto();
        inputEventAdaptorProperty4.setKey("authenticatorURL");
        inputEventAdaptorProperty4.setValue("ssl://localhost:7761");

        OutputEventAdaptorPropertyDto[] outputEventAdaptorPropertyDtos = new OutputEventAdaptorPropertyDto[]{inputEventAdaptorProperty1, inputEventAdaptorProperty2, inputEventAdaptorProperty3, inputEventAdaptorProperty4};

        outputEventAdaptorManagerAdminServiceClient.addOutputEventAdaptorConfiguration("localEventSender", "wso2event", outputEventAdaptorPropertyDtos);

        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdapterTestScenario1"})
    public void addEventFormatterTestScenario1() throws RemoteException, InterruptedException {
        log.info("=======================Adding a stream definition====================");

        EventStreamAttributeDto metaEventStreamAttributeDto21 = new EventStreamAttributeDto();
        metaEventStreamAttributeDto21.setAttributeName("ipAddress");
        metaEventStreamAttributeDto21.setAttributeType("string");

        EventStreamAttributeDto[] metaEventStreamAttributeDtos2 = new EventStreamAttributeDto[]{metaEventStreamAttributeDto21};

        EventStreamAttributeDto payloadEventStreamAttributeDto21 = new EventStreamAttributeDto();
        payloadEventStreamAttributeDto21.setAttributeName("user");
        payloadEventStreamAttributeDto21.setAttributeType("string");

        EventStreamAttributeDto payloadEventStreamAttributeDto22= new EventStreamAttributeDto();
        payloadEventStreamAttributeDto22.setAttributeName("keywords");
        payloadEventStreamAttributeDto22.setAttributeType("string");

        EventStreamAttributeDto[] payloadEventStreamAttributeDtos2 = new EventStreamAttributeDto[]{payloadEventStreamAttributeDto21,payloadEventStreamAttributeDto22};

        eventStreamManagerAdminServiceClient.addEventStream("statisticsStream","1.0.0",metaEventStreamAttributeDtos2,null,payloadEventStreamAttributeDtos2,"","");

        Assert.assertEquals(eventStreamManagerAdminServiceClient.getEventStreamCount(), 5);

        int startCount = eventFormatterAdminServiceClient.getActiveEventFormatterCount();
        Assert.assertEquals(startCount, 0);
        log.info("=======================Adding a event formatter ======================= ");
        //configurationUtil.addEventFormatter();

        EventOutputPropertyConfigurationDto eventOutputProperty1 = new EventOutputPropertyConfigurationDto();
        eventOutputProperty1.setName("ipAddress");
        eventOutputProperty1.setValueOf("meta_ipAddress");
        eventOutputProperty1.setType("string");
        EventOutputPropertyConfigurationDto[] metaEventOutputPropertyConfigurationDtos = new EventOutputPropertyConfigurationDto[]{eventOutputProperty1};

        EventOutputPropertyConfigurationDto eventOutputProperty2 = new EventOutputPropertyConfigurationDto();
        eventOutputProperty2.setName("user");
        eventOutputProperty2.setValueOf("user");
        eventOutputProperty2.setType("string");
        EventOutputPropertyConfigurationDto eventOutputProperty3 = new EventOutputPropertyConfigurationDto();
        eventOutputProperty3.setName("keywords");
        eventOutputProperty3.setValueOf("keywords");
        eventOutputProperty3.setType("string");
        EventOutputPropertyConfigurationDto[] payloadEventOutputPropertyConfigurationDtos = new EventOutputPropertyConfigurationDto[]{eventOutputProperty2, eventOutputProperty3};


        EventFormatterPropertyDto eventFormatterProperty1 = new EventFormatterPropertyDto();
        eventFormatterProperty1.setKey("stream");
        eventFormatterProperty1.setValue("analytics_outStream");

        EventFormatterPropertyDto eventFormatterProperty2 = new EventFormatterPropertyDto();
        eventFormatterProperty2.setKey("version");
        eventFormatterProperty2.setValue("1.3.0");

        EventFormatterPropertyDto eventFormatterPropertyDtos[] = new EventFormatterPropertyDto[]{eventFormatterProperty1, eventFormatterProperty2};

        eventFormatterAdminServiceClient.addWso2EventFormatterConfiguration("wso2eventformatter", "statisticsStream:1.0.0", "localEventSender", "wso2event", metaEventOutputPropertyConfigurationDtos, null, payloadEventOutputPropertyConfigurationDtos, eventFormatterPropertyDtos,true);
        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventFormatterTestScenario1"})
    public void kpiAnalyzerTest() throws AgentException, MalformedURLException,
                                         AuthenticationException,
                                         javax.security.sasl.AuthenticationException,
                                         MalformedStreamDefinitionException, SocketException,
                                         StreamDefinitionException,
                                         NoStreamDefinitionExistException,
                                         DifferentStreamDefinitionAlreadyDefinedException,
                                         InterruptedException, DataBridgeException, TransportException {
        TestAgentServer testAgentServer = new TestAgentServer();
        Thread thread = new Thread(testAgentServer);
        thread.start();

        Thread.sleep(5000);

        PhoneRetailAgent.publish();

        Thread.sleep(5000);

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        eventFormatterAdminServiceClient.removeActiveEventFormatterConfiguration("wso2eventformatter");
        outputEventAdaptorManagerAdminServiceClient.removeActiveOutputEventAdaptorConfiguration("localEventSender");
        eventProcessorAdminServiceClient.removeActiveExecutionPlan("KPIAnalyzer");
        eventBuilderAdminServiceClient.removeActiveEventBuilderConfiguration("wso2eventbuilder");
        inputEventAdaptorManagerAdminServiceClient.removeActiveInputEventAdaptorConfiguration("localEventReceiver");
    }
}
