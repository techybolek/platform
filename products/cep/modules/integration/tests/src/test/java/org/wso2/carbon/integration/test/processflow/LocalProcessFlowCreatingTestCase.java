package org.wso2.carbon.integration.test.processflow;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.cep.*;
import org.wso2.carbon.integration.test.CEPIntegrationTest;
import org.wso2.carbon.integration.test.util.ConfigurationUtil;

import java.rmi.RemoteException;

/**
 * Check whether CEPAdminService properly creates SiddhiBucket to be used with localBroker
 */
public class LocalProcessFlowCreatingTestCase extends CEPIntegrationTest {

    private static final Log log = LogFactory.getLog(LocalProcessFlowCreatingTestCase.class);
    private ConfigurationUtil configurationUtil;
    private EventFormatterAdminServiceClient eventFormatterAdminServiceClient;
    private EventBuilderAdminServiceClient eventBuilderAdminServiceClient;
    private EventProcessorAdminServiceClient eventProcessorAdminServiceClient;
    private InputEventAdaptorManagerAdminServiceClient inputEventAdaptorManagerAdminServiceClient;
    private OutputEventAdaptorManagerAdminServiceClient outputEventAdaptorManagerAdminServiceClient;

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
        configurationUtil.initEventStreamManager(userInfo,cepServer,loggedInSessionCookie);

        eventBuilderAdminServiceClient = configurationUtil.getEventBuilderAdminServiceClient();
        eventFormatterAdminServiceClient = configurationUtil.getEventFormatterAdminServiceClient();
        eventProcessorAdminServiceClient = configurationUtil.getEventProcessorAdminServiceClient();
        inputEventAdaptorManagerAdminServiceClient = configurationUtil.getInputEventAdaptorManagerAdminServiceClient();
        outputEventAdaptorManagerAdminServiceClient = configurationUtil.getOutputEventAdaptorManagerAdminServiceClient();

        try {
            configurationUtil.addInEventStream();
            configurationUtil.addOutEventStream();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    //Scenario 1 adding order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file deployment order ITA, EB, EP, OTA, EF")
    public void addInputEventAdapterTestScenario1()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount();
        configurationUtil.addInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdapterTestScenario1"})
    public void addEventBuilderTestScenario1() throws RemoteException, InterruptedException {

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getActiveEventBuilderCount();
        configurationUtil.addEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario1"})
    public void addEventProcessorTestScenario1() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount();
        configurationUtil.addEventProcessor();
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
        configurationUtil.addOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdapterTestScenario1"})
    public void addEventFormatterTestScenario1() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getActiveEventFormatterCount();
        Assert.assertEquals(startCount, 0);
        log.info("=======================Adding a event formatter ======================= ");
        configurationUtil.addEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1 + startCount);

    }

    //Scenario 1 removing order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file un-deployment order ITA, EB, EP, OTA, EF", dependsOnMethods = {"addEventFormatterTestScenario1"})
    public void removeInputEventAdapterTestScenario1()
            throws RemoteException, InterruptedException {

        log.info("=======================Removing a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeInputEventAdapterTestScenario1"})
    public void removeEventBuilderTestScenario1() throws RemoteException, InterruptedException {

        log.info("=======================Removing a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();
        configurationUtil.removeInActiveEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventBuilderTestScenario1"})
    public void removeEventProcessorTestScenario1() throws RemoteException, InterruptedException {
        log.info("=======================Removing a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.removeInActiveEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventProcessorTestScenario1"})
    public void removeOutputEventAdapterTestScenario1()
            throws RemoteException, InterruptedException {
        log.info("=======================Removing a Output event adaptor ======================= ");
        int startCount = outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount(), startCount - 1);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeOutputEventAdapterTestScenario1"})
    public void removeEventFormatterTestScenario1() throws RemoteException, InterruptedException {
        log.info("=======================Removing a event formatter ======================= ");
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        configurationUtil.removeInActiveEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), startCount - 1);
    }

    //Scenario 2 adding order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file deployment order  EB, EP, OTA, EF, ITA", dependsOnMethods = {"removeEventFormatterTestScenario1"})
    public void addEventBuilderTestScenario2() throws RemoteException, InterruptedException {

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();

        try {
            configurationUtil.addEventBuilder();

        } catch (AxisFault axisFault) {
            log.info("ERROR !!!  " + axisFault.getMessage());
        }
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario2"})
    public void addEventProcessorTestScenario2() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.addEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventProcessorTestScenario2"})
    public void addOutputEventAdapterTestScenario2()
            throws RemoteException, InterruptedException {
        int startCount = outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount();
        Assert.assertEquals(startCount, 1);

        log.info("=======================Adding a Output event adaptor ======================= ");
        configurationUtil.addOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdapterTestScenario2"})
    public void addEventFormatterTestScenario2() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 0);
        System.out.println("****Adding a Output transport adaptor **** ");

        log.info("=======================Adding a event formatter ======================= ");
        try {
            configurationUtil.addEventFormatter();
        } catch (Exception e) {

        }
        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), 1 + startCount);

//        System.out.println("****Adding a Output transport adaptor **** ");
//        outputTransportAdaptorManagerAdminServiceClient.addOutputTransportAdaptorConfiguration("ws-local-sender", "ws-event-local", new OutputTransportAdaptorPropertyDto[0]);
//        Thread.sleep(10000);
//        System.out.println("****Check the active output transport adaptors**** ");
//        Assert.assertEquals(outputTransportAdaptorManagerAdminServiceClient.getActiveOutputTransportAdaptorConfigurationCount(),1+startCount);


    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventFormatterTestScenario2"})
    public void addInputEventAdapterTestScenario2()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.addInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdapterTestScenario2"})
    public void addEventFormatterTestScenario2End() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 1);
    }

    @Test(groups = {"wso2.cep"}, description = "Test the active configuration files when deploying in the order ITA, EB, EP, OTA, EF", dependsOnMethods = {"addEventFormatterTestScenario2End"})
    public void checkActiveConfigurationsTestScenario2()
            throws RemoteException, InterruptedException {

        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 2);
        log.info("=======================Check the active event builders======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 3);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1);
        log.info("=======================Check the active execution plans======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), 1);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 2);

    }

    //Scenario 2 removing order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file un-deployment order EB, EP, OTA, EF, ITA", dependsOnMethods = {"checkActiveConfigurationsTestScenario2"})
    public void removeEventBuilderTestScenario2() throws RemoteException, InterruptedException {

        log.info("=======================Removing a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();
        configurationUtil.removeInActiveEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventBuilderTestScenario2"})
    public void removeEventProcessorTestScenario2() throws RemoteException, InterruptedException {
        log.info("=======================Removing a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.removeInActiveEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventProcessorTestScenario2"})
    public void removeOutputEventAdapterTestScenario2()
            throws RemoteException, InterruptedException {
        log.info("=======================Removing a Output event adaptor ======================= ");
        int startCount = outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount(), startCount - 1);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeOutputEventAdapterTestScenario2"})
    public void removeEventFormatterTestScenario2() throws RemoteException, InterruptedException {
        log.info("=======================Removing a event formatter ======================= ");
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        configurationUtil.removeInActiveEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventFormatterTestScenario2"})
    public void removeInputEventAdapterTestScenario2()
            throws RemoteException, InterruptedException {

        log.info("=======================Removing a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), startCount - 1);
    }

    //Scenario 3 adding order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file deployment order EP, OTA, EF, ITA, EB", dependsOnMethods = {"removeEventFormatterTestScenario2"})
    public void addEventProcessorTestScenario3() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.addEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventProcessorTestScenario3"})
    public void addOutputEventAdapterTestScenario3()
            throws RemoteException, InterruptedException {
        int startCount = outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount();
        Assert.assertEquals(startCount, 1);

        log.info("=======================Adding a Output event adaptor ======================= ");
        configurationUtil.addOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdapterTestScenario3"})
    public void addEventFormatterTestScenario3() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 0);

        log.info("=======================Adding a event formatter ======================= ");
        try {
            configurationUtil.addEventFormatter();
        } catch (Exception e) {

        }
        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventFormatterTestScenario3"})
    public void addInputEventAdapterTestScenario3()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.addInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdapterTestScenario3"})
    public void addEventBuilderTestScenario3() throws RemoteException, InterruptedException {

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();

        try {
            configurationUtil.addEventBuilder();

        } catch (AxisFault axisFault) {
            log.info("ERROR !!!  " + axisFault.getMessage());
        }
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario3"})
    public void addEventFormatterTestScenario3End() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 1);
    }

    @Test(groups = {"wso2.cep"}, description = "Test the active configuration files when deploying in the order EP, OTA, EF, EB, ITA", dependsOnMethods = {"addEventFormatterTestScenario3End"})
    public void checkActiveConfigurationsTestScenario3()
            throws RemoteException, InterruptedException {

        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 2);
        log.info("=======================Check the active event builders======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 3);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1);
        log.info("=======================Check the active execution plans======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), 1);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 2);

    }

    //Scenario 3 removing order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file un-deployment order EP, OTA, EF, ITA, EB", dependsOnMethods = {"addEventFormatterTestScenario3End"})
    public void removeEventProcessorTestScenario3() throws RemoteException, InterruptedException {
        log.info("=======================Removing a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.removeActiveEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventProcessorTestScenario3"})
    public void removeOutputEventAdapterTestScenario3()
            throws RemoteException, InterruptedException {
        log.info("=======================Removing a Output event adaptor ======================= ");
        int startCount = outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount(), startCount - 1);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeOutputEventAdapterTestScenario3"})
    public void removeEventFormatterTestScenario3() throws RemoteException, InterruptedException {
        log.info("=======================Removing a event formatter ======================= ");
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        configurationUtil.removeInActiveEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventFormatterTestScenario3"})
    public void removeInputEventAdapterTestScenario3()
            throws RemoteException, InterruptedException {

        log.info("=======================Removing a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeInputEventAdapterTestScenario3"})
    public void removeEventBuilderTestScenario3() throws RemoteException, InterruptedException {

        log.info("=======================Removing a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();
        configurationUtil.removeInActiveEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), startCount - 1);
    }


    //Scenario 4 adding order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file deployment order OTA, EF, ITA, EB, EP", dependsOnMethods = {"removeEventBuilderTestScenario3"})
    public void addOutputEventAdapterTestScenario4()
            throws RemoteException, InterruptedException {
        int startCount = outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount();
        Assert.assertEquals(startCount, 1);

        log.info("=======================Adding a Output event adaptor ======================= ");
        configurationUtil.addOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdapterTestScenario4"})
    public void addEventFormatterTestScenario4() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 0);

        log.info("=======================Adding a event formatter ======================= ");
        try {
            configurationUtil.addEventFormatter();
        } catch (Exception e) {

        }
        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventFormatterTestScenario4"})
    public void addInputEventAdapterTestScenario4()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.addInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdapterTestScenario4"})
    public void addEventBuilderTestScenario4() throws RemoteException, InterruptedException {

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();

        try {
            configurationUtil.addEventBuilder();

        } catch (AxisFault axisFault) {
            log.info("ERROR !!!  " + axisFault.getMessage());
        }
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario4"})
    public void addEventProcessorTestScenario4() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.addEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventProcessorTestScenario4"})
    public void addEventFormatterTestScenario4End() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 1);
    }

    @Test(groups = {"wso2.cep"}, description = "Test the active configuration files when deploying in the order OTA, EF, EB, ITA, EP", dependsOnMethods = {"addEventFormatterTestScenario4End"})
    public void checkActiveConfigurationsTestScenario4()
            throws RemoteException, InterruptedException {

        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 2);
        log.info("=======================Check the active event builders======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 3);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1);
        log.info("=======================Check the active execution plans======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), 1);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 2);

    }

    //Scenario 4 removing order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file un-deployment order OTA, EF, ITA, EB, EP", dependsOnMethods = {"checkActiveConfigurationsTestScenario4"})
    public void removeOutputEventAdapterTestScenario4()
            throws RemoteException, InterruptedException {
        log.info("=======================Removing a Output event adaptor ======================= ");
        int startCount = outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount(), startCount - 1);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeOutputEventAdapterTestScenario4"})
    public void removeEventFormatterTestScenario4() throws RemoteException, InterruptedException {
        log.info("=======================Removing a event formatter ======================= ");
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        configurationUtil.removeInActiveEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventFormatterTestScenario4"})
    public void removeInputEventAdapterTestScenario4()
            throws RemoteException, InterruptedException {

        log.info("=======================Removing a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeInputEventAdapterTestScenario4"})
    public void removeEventBuilderTestScenario4() throws RemoteException, InterruptedException {

        log.info("=======================Removing a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();
        configurationUtil.removeInActiveEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventBuilderTestScenario4"})
    public void removeEventProcessorTestScenario4() throws RemoteException, InterruptedException {
        log.info("=======================Removing a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.removeInActiveEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), startCount - 1);
    }

    //Scenario 5 adding order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file deployment order  EF, ITA, EB, EP, OTA", dependsOnMethods = {"removeEventProcessorTestScenario4"})
    public void addEventFormatterTestScenario5() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 0);

        log.info("=======================Adding a event formatter ======================= ");
        try {
            configurationUtil.addEventFormatter();
        } catch (Exception e) {

        }
        Thread.sleep(1000);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventFormatterTestScenario5"})
    public void addInputEventAdapterTestScenario5()
            throws RemoteException, InterruptedException {

        log.info("=======================Adding a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.addInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addInputEventAdapterTestScenario5"})
    public void addEventBuilderTestScenario5() throws RemoteException, InterruptedException {

        log.info("=======================Adding a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();

        try {
            configurationUtil.addEventBuilder();

        } catch (AxisFault axisFault) {
            log.info("ERROR !!!  " + axisFault.getMessage());
        }
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventBuilderTestScenario5"})
    public void addEventProcessorTestScenario5() throws RemoteException, InterruptedException {
        log.info("=======================Adding a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.addEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addEventProcessorTestScenario5"})
    public void addOutputEventAdapterTestScenario5()
            throws RemoteException, InterruptedException {
        int startCount = outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount();
        Assert.assertEquals(startCount, 1);

        log.info("=======================Adding a Output event adaptor ======================= ");
        configurationUtil.addOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 1 + startCount);

    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"addOutputEventAdapterTestScenario5"})
    public void addEventFormatterTestScenario5End() throws RemoteException, InterruptedException {
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        Assert.assertEquals(startCount, 1);
    }

    @Test(groups = {"wso2.cep"}, description = "Test the active configuration files when deploying in the order EF, EB, ITA, EP, OTA", dependsOnMethods = {"addEventFormatterTestScenario5End"})
    public void checkActiveConfigurationsTestScenario5()
            throws RemoteException, InterruptedException {

        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getActiveInputEventAdaptorConfigurationCount(), 2);
        log.info("=======================Check the active event builders======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getActiveEventBuilderCount(), 3);
        log.info("=======================Check the active event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getActiveEventFormatterCount(), 1);
        log.info("=======================Check the active execution plans======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getAllActiveExecutionPlanConfigurationCount(), 1);
        log.info("=======================Check the active output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getActiveOutputEventAdaptorConfigurationCount(), 2);

    }

    //Scenario 5 removing order

    @Test(groups = {"wso2.cep"}, description = "Test the configuration file un-deployment order EF, ITA, EB, EP, OTA", dependsOnMethods = {"checkActiveConfigurationsTestScenario5"})
    public void removeEventFormatterTestScenario5() throws RemoteException, InterruptedException {
        log.info("=======================Removing a event formatter ======================= ");
        int startCount = eventFormatterAdminServiceClient.getEventFormatterCount();
        configurationUtil.removeActiveEventFormatter();
        Thread.sleep(1000);
        log.info("=======================Check the event formatters======================= ");
        Assert.assertEquals(eventFormatterAdminServiceClient.getEventFormatterCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventFormatterTestScenario5"})
    public void removeInputEventAdapterTestScenario5()
            throws RemoteException, InterruptedException {

        log.info("=======================Removing a input event adaptor======================= ");
        int startCount = inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveInputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the active input event adaptors======================= ");
        Assert.assertEquals(inputEventAdaptorManagerAdminServiceClient.getInputEventAdaptorConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeInputEventAdapterTestScenario5"})
    public void removeEventBuilderTestScenario5() throws RemoteException, InterruptedException {

        log.info("=======================Removing a event builder ======================= ");
        int startCount = eventBuilderAdminServiceClient.getEventBuilderCount();
        configurationUtil.removeInActiveEventBuilder();
        Thread.sleep(1000);
        log.info("=======================Check the event builder ======================= ");
        Assert.assertEquals(eventBuilderAdminServiceClient.getEventBuilderCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventBuilderTestScenario5"})
    public void removeEventProcessorTestScenario5() throws RemoteException, InterruptedException {
        log.info("=======================Removing a execution plan ======================= ");
        int startCount = eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount();
        configurationUtil.removeInActiveEventProcessor();
        Thread.sleep(1000);
        log.info("=======================Check the execution plan ======================= ");
        Assert.assertEquals(eventProcessorAdminServiceClient.getExecutionPlanConfigurationCount(), startCount - 1);
    }

    @Test(groups = {"wso2.cep"}, dependsOnMethods = {"removeEventProcessorTestScenario5"})
    public void removeOutputEventAdapterTestScenario5()
            throws RemoteException, InterruptedException {
        log.info("=======================Removing a Output event adaptor ======================= ");
        int startCount = outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount();
        configurationUtil.removeActiveOutputEventAdaptor();
        Thread.sleep(1000);
        log.info("=======================Check the output event adaptors======================= ");
        Assert.assertEquals(outputEventAdaptorManagerAdminServiceClient.getOutputEventAdaptorConfigurationCount(), startCount - 1);

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
//        inputEventAdaptorManagerAdminServiceClient.removeExecutionPlan(EXECUTION_PLAN_NAME);
        inputEventAdaptorManagerAdminServiceClient = null;
        outputEventAdaptorManagerAdminServiceClient = null;
        eventBuilderAdminServiceClient = null;
        eventProcessorAdminServiceClient = null;
        eventFormatterAdminServiceClient = null;
    }

}
