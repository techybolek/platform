/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;

import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Registry;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Specify incorrect paths for
 * -currentEnvironment
 * -targetEnvironment
 * and verify error handling when LC transition happens
 * <p/>
 * Specify invalid/non-existing paths as currentEnvironment path and
 * targetEnvironment paths
 */
public class LCTransitionExecutionTestCase {
    private int userId = 2;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private String serviceString;
    private WSRegistryServiceClient wsRegistryServiceClient;

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ServiceManager serviceManager;

    private static final String SERVICE_NAME = "IntergalacticService";
    private static final String LC_NAME1 = "TransitionExecutionLC";
    private static final String LC_NAME2 = "EnvironmentPathLC";
    private static final String LC_NAME3 = "EnvironmentPathLC2";
    private static final String LC_NAME4 = "InvalidCurrentEnvironmentPathLC";
    private static final String LC_NAME5 = "InvalidTargetEnvironmentPathLC";
    private static final String LC_STATE0 = "Testing";
    private static final String ACTION_PROMOTE = "Promote";

    private LifecycleBean lifeCycle;
    private ServiceBean service;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

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

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

	Registry reg = registryProviderUtil.getGovernanceRegistry(new RegistryProviderUtil().getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME), userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)reg);
        serviceManager = new ServiceManager(reg);
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
    @Test(groups = "wso2.greg", description = "Create services")
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
                "/_system/governance/service1", mediaType, description, dataHandler);
        
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
                File.separator + "TransitionExecutionLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LC_NAME1)) {
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
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service with incorrect path",
          dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
					    GovernanceException {

//      service = listMetadataServiceClient.listServices(null);

	Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
	    String path = service.getPath();
            if (path.contains("IntergalacticService")) {
                serviceString = path;
            }
        }
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME1);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME1)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Promote from Development",
          dependsOnMethods = "testAddLcToService", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testPromoteLC() throws RemoteException,
                                       CustomLifecyclesChecklistAdminServiceExceptionException,
                                       LifeCycleManagementServiceExceptionException,
                                       RegistryExceptionException {

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME1,
                                                 ACTION_PROMOTE, null);

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Clear the used resources", dependsOnMethods = "testPromoteLC")
    public void clearPrevious() throws Exception {
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
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME1);

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
    @Test(groups = "wso2.greg", description = "Create service again", dependsOnMethods = "clearPrevious")
    public void testCreateService2() throws XMLStreamException, IOException,
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
    @Test(groups = "wso2.greg", description = "Create new life cycle with non-existing path in " +
                                              "currentEnvironment", dependsOnMethods = "testCreateService2")
    public void testCreateNewLifeCycle2() throws LifeCycleManagementServiceExceptionException,
                                                 IOException, InterruptedException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "EnvironmentPathLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LC_NAME2)) {
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
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service with non-existing " +
                                              "path in currentEnvironment",
          dependsOnMethods = "testCreateNewLifeCycle2")
    public void testAddLcToService2() throws RegistryException, RemoteException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException,
                                             ListMetadataServiceRegistryExceptionException,
                                             ResourceAdminServiceExceptionException {

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME2);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME2)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Promote from Development",
          dependsOnMethods = "testAddLcToService2", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testPromoteLC2() throws RemoteException,
                                        CustomLifecyclesChecklistAdminServiceExceptionException,
                                        LifeCycleManagementServiceExceptionException,
                                        RegistryExceptionException {

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME2,
                                                 ACTION_PROMOTE, null);

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Clear the used resources", dependsOnMethods = "testPromoteLC2")
    public void clearPrevious2() throws Exception {
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
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME2);
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
    @Test(groups = "wso2.greg", description = "Create service again", dependsOnMethods = "clearPrevious2")
    public void testCreateService3() throws XMLStreamException, IOException,
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
                "/_system/governance/service3", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource("/_system/governance/trunk/services/com/abb/IntergalacticService");
        
        assertNotNull(data, "Service not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create new life cycle with non-existing path in " +
                                              "targetEnvironment", dependsOnMethods = "testCreateService3")
    public void testCreateNewLifeCycle3() throws LifeCycleManagementServiceExceptionException,
                                                 IOException, InterruptedException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "EnvironmentPathLC2.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LC_NAME3)) {
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
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service with non-existing path " +
                                              "in targetEnvironment", dependsOnMethods = "testCreateNewLifeCycle3")
    public void testAddLcToService3() throws RegistryException, RemoteException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException,
                                             ListMetadataServiceRegistryExceptionException,
                                             ResourceAdminServiceExceptionException {

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME3);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME3)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote from Development",
          dependsOnMethods = "testAddLcToService3")
    public void testPromoteLC3() throws RemoteException,
                                        CustomLifecyclesChecklistAdminServiceExceptionException,
                                        LifeCycleManagementServiceExceptionException,
                                        RegistryExceptionException,
                                        ResourceAdminServiceExceptionException {
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{"preserveOriginal", "false"});
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance" + serviceString,
                                                           LC_NAME3, ACTION_PROMOTE, null,
                                                           parameters);
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
            if (("registry.lifecycle." + LC_NAME3 + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE0),
                           "Not promoted to Testing");
            }
        }
    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Clear the used resources", dependsOnMethods = "testPromoteLC3")
    public void clearPrevious3() throws Exception {
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
        wsdlPathToDelete = "/_system/governance/wso2/branches/testing/wsdls/com/foo/1.0.0-SNAPSHOT/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME3);
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
    @Test(groups = "wso2.greg", description = "Create service again", dependsOnMethods = "clearPrevious3")
    public void testCreateService4() throws XMLStreamException, IOException,
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
                "/_system/governance/service4", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource("/_system/governance/trunk/services/com/abb/IntergalacticService");
        
        assertNotNull(data, "Service not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create new life cycle with invalid path in " +
                                              "currentEnvironment", dependsOnMethods = "testCreateService4")
    public void testCreateNewLifeCycle4() throws LifeCycleManagementServiceExceptionException,
                                                 IOException, InterruptedException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "InvalidEnvironmentPathLC1.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LC_NAME4)) {
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
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service with non-existing path in " +
                                              "currentEnvironment", dependsOnMethods = "testCreateNewLifeCycle4")
    public void testAddLcToService4() throws RegistryException, RemoteException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException,
                                             ListMetadataServiceRegistryExceptionException,
                                             ResourceAdminServiceExceptionException {

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME4);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME4)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Promote from Development",
          dependsOnMethods = "testAddLcToService4", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testPromoteLC4() throws RemoteException,
                                        CustomLifecyclesChecklistAdminServiceExceptionException,
                                        LifeCycleManagementServiceExceptionException,
                                        RegistryExceptionException {

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME4,
                                                 ACTION_PROMOTE, null);

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Clear the used resources", dependsOnMethods = "testPromoteLC4")
    public void clearPrevious4() throws Exception {
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
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME4);
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
    @Test(groups = "wso2.greg", description = "Create service again", dependsOnMethods = "clearPrevious4")
    public void testCreateService5() throws XMLStreamException, IOException,
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
                "/_system/governance/service5", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource("/_system/governance/trunk/services/com/abb/IntergalacticService");
        
        assertNotNull(data, "Service not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create new life cycle with invalid path in targetEnvironment",
          dependsOnMethods = "testCreateService5")
    public void testCreateNewLifeCycle5() throws LifeCycleManagementServiceExceptionException,
                                                 IOException, InterruptedException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "InvalidEnvironmentPathLC2.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LC_NAME5)) {
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
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service with invalid path in targetEnvironment",
          dependsOnMethods = "testCreateNewLifeCycle5")
    public void testAddLcToService5() throws RegistryException, RemoteException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException,
                                             ListMetadataServiceRegistryExceptionException,
                                             ResourceAdminServiceExceptionException {

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME5);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME5)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote from Development",
          dependsOnMethods = "testAddLcToService5", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testPromoteLC5() throws RemoteException,
                                        CustomLifecyclesChecklistAdminServiceExceptionException,
                                        LifeCycleManagementServiceExceptionException,
                                        RegistryExceptionException,
                                        ResourceAdminServiceExceptionException {
        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME5,
                                                 ACTION_PROMOTE, null);
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
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME5);

        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
    }

}
