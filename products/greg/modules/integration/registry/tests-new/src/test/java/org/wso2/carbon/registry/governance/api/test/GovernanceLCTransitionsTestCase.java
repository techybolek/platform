package org.wso2.carbon.registry.governance.api.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GovernanceLCTransitionsTestCase  {
    int userId = 2;
    private String LIFE_CYCLE_NAME = "StoreServiceLifeCycle";
    private Registry governance;
    private ServiceManager serviceManager;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws IOException, LoginAuthenticationExceptionException,
            RegistryException, LifeCycleManagementServiceExceptionException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
                ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("https://www.wso2.com/greg/store", "StoreService")) ;
        service.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(service);

        LifeCycleManagementClient lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());

        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "StoreServiceLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition")
    public void testAttachLifecycle() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        Service service = services[0];
        service.attachLifecycle(LIFE_CYCLE_NAME);
        String lifecycleName = service.getLifecycleName();
        String lifecycleState = service.getLifecycleState();

        Assert.assertEquals(lifecycleName, LIFE_CYCLE_NAME, "Different lifecycle found");
        Assert.assertEquals(lifecycleState, "Development", "Different lifecycle state found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testAttachLifecycle")
    public void testGetCheckListItems() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        Service service = services[0];
        String[] checklistItems = service.getAllCheckListItemNames();

        Assert.assertEquals(checklistItems[0], "Code Completed", "Expected checklist item not found");
        Assert.assertEquals(checklistItems[1], "WSDL, Schema Created", "Expected checklist item not found");
        Assert.assertEquals(checklistItems[2], "QoS Created", "Expected checklist item not found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testGetCheckListItems")
    public void testCheckLCItem() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        Service service = services[0];

        service.checkLCItem(0);
        service.checkLCItem(1);
        service.checkLCItem(2);

        Assert.assertTrue(service.isLCItemChecked(0), "Lifecycle item not checked");
        Assert.assertTrue(service.isLCItemChecked(1), "Lifecycle item not checked");
        Assert.assertTrue(service.isLCItemChecked(2), "Lifecycle item not checked");

        service.uncheckLCItem(0);

        Assert.assertFalse(service.isLCItemChecked(0), "Lifecycle item unchecked");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testCheckLCItem")
    public void testGetVotingEvents() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        Service service = services[0];

        String[] votingItems = service.getAllVotingItems();
        Assert.assertEquals(votingItems[0], "Promote", "Unexpected voting event found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testGetVotingEvents")
    public void testVoting() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        Service service = services[0];

        service.vote(0);
        Assert.assertTrue(service.isVoted(0), "Not voted");

        service.unvote(0);
        Assert.assertFalse(service.isVoted(0), "Vote not reverted");

        service.vote(0);
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testVoting")
    public void testGetAllActions() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        Service service = services[0];

        String[] actions = service.getAllLifecycleActions();
        Assert.assertEquals(actions[0], "Promote", "Unexpected action found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testGetAllActions")
    public void testPromote() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        Service service = services[0];

        Map<String, String> map = new HashMap<String, String>();
        map.put("/_system/governance/trunk/services/com/wso2/www/greg/store/StoreService", "2.3.5");
        service.invokeAction("Promote", map);


        services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String version = service.getAttribute("overview_version");
                if(version.equals("2.3.5")){
                    return true;
                }
                return false;
            }
        });

        Assert.assertEquals(services[0].getQName().getLocalPart(), "StoreService", "New storage service version not created");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws GovernanceException {
        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            serviceManager.removeService(service.getId());
        }
    }

}
