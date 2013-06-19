/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.lifecycle.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Use LCs with different transitionPermission roles defined for
 * "Promote/Demote" options and make sure that users under only those roles are
 * allowed to do the transition. Login from other roles and verify whether they
 * are not allowed to perform LC transition
 */
public class LCPromoteDemotePermissionTestCase {
    private int userId = 2;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private int user2Id = 3;
    private UserInfo user2Info = UserListCsvReader.getUserInfo(user2Id);
    private String serviceString;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private UserManagementClient userManagementClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private static final String SERVICE_NAME = "IntergalacticService";
    private static final String LC_NAME = "CheckListPermissionLC";

    private static final String ACTION_PROMOTE = "Promote";
    private static final String ACTION_DEMOTE = "Demote";
    private static final String LC_STATE0 = "Commencement";
    private static final String LC_STATE1 = "Creation";
    
    private static final String ACTION_ITEM_CLICK = "itemClick";

    private LifecycleBean lifeCycle;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private ServiceBean service;

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, LoginAuthenticationExceptionException,
                              RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            environment.getGreg().getSessionCookie());
        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);


    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
                                           AddServicesServiceRegistryExceptionException,
                                           ListMetadataServiceRegistryExceptionException,
                                           ResourceAdminServiceExceptionException {

        String servicePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource("/_system/governance/trunk/services/com/abb/IntergalacticService");
        
        assertNotNull(data, "Service not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle() throws LifeCycleManagementServiceExceptionException,
                                                IOException, InterruptedException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "CheckListPermissionLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException {

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Promote from Commencement to Creation",
          dependsOnMethods = "testAddLcToService")
    public void testPromoteToCreation() throws Exception {
        addRole("managerrole", userInfo.getUserNameWithoutDomain());
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_PROMOTE)) {
                isPermitted = true;
            }
        }
        assertTrue(isPermitted, "Not allowed to promote with permission");

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME,
                                                 ACTION_PROMOTE, null);

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1),
                           "Not promoted to Creation with permission");
            }
        }

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Demote from Commencement to Creation",
          dependsOnMethods = "testPromoteToCreation" , expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testDemoteFromCreation() throws Exception {
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_DEMOTE)) {
                isPermitted = true;
            }
        }
        assertFalse(isPermitted, "Allowed to demote without permission");

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME,
                                                 ACTION_DEMOTE, null);

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1),
                           "Demoted without permission");
            }
        }
    }

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @Test(groups = "wso2.greg", description = "Log in as a different user without devrole",
          dependsOnMethods = "testDemoteFromCreation")
    public void testSetNewUser() throws RemoteException, LoginAuthenticationExceptionException,
                                        RegistryException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(user2Id);
        ManageEnvironment environment = builder.build();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(
                        environment.getGreg().getProductVariables().getBackendUrl(),
                        environment.getGreg().getSessionCookie());

        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            environment.getGreg().getSessionCookie());
        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(user2Id,
                                                   ProductConstant.GREG_SERVER_NAME);
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Demote  from Creation with permission",
          dependsOnMethods = "testSetNewUser")
    public void testDemoteFromCreationAgain() throws Exception {
        addRole("archrole", user2Info.getUserNameWithoutDomain());
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_DEMOTE)) {
                isPermitted = true;
            }
        }
        assertTrue(isPermitted, "Not allowed to demote with permission");

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME,
                                                 ACTION_DEMOTE, null);

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE0),
                           "Not promoted to Creation with permission");
            }
        }

    }

    /**
     * @throws Exception 
     * Expected exception because user is not allowed to promote LC without permission(ex: using WS)
     */
    @Test(groups = "wso2.greg", description = "Promote from Commencement to Creation without permission",
          dependsOnMethods = "testDemoteFromCreationAgain", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testPromoteToCreationAgain() throws Exception {
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_PROMOTE)) {
                isPermitted = true;
            }
        }
        assertFalse(isPermitted, "Allowed to promote without permission");
        
        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME,
                								ACTION_ITEM_CLICK, new String[]{"false", "false", "false",
                             									"false", "false", "false"});

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME,
                                                 ACTION_PROMOTE, null);

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE0),
                           "Promoted without permission");
            }
        }

    }

    /**
     * @param roleName name of the role needs to be added to the current user
     * @throws Exception
     */
    private void addRole(String roleName, String userName) throws Exception {
        String[] permissions = {"/permission/admin/manage/"};
        if (!userManagementClient.roleNameExists(roleName)) {
            userManagementClient.addRole(roleName, new String[]{userName}, permissions);
        }
    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void clear() throws Exception {
        String servicePathToDelete = "/_system/governance/" + serviceString;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
        String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        userManagementClient.deleteRole("archrole");
        userManagementClient.deleteRole("managerrole");

        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        governanceServiceClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
        resourceAdminServiceClient = null;
        userManagementClient = null;

    }

}
