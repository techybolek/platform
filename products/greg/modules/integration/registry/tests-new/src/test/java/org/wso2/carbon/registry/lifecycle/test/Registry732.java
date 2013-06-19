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
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
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
import org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class Registry732 {

    private UserInfo userInfo;
    private String serviceString;

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RelationAdminServiceClient relationServiceClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;

    private static final String SERVICE_NAME = "IntergalacticService";
    private static final String ROOT = "/";
    private static final String DEPENDENCY_RES_NAME = "AResource";
    private static final String ASSOCIATION_COLL = "/_system";
    private static final String SERVICE_LOCATION = "/_system/governance";
    private static final String LC_NAME = "CARepService";
    private static final String RES_DESC = "Description of the resource";
    private static final String LC_STATE1 = "Planned";
    private static final String ACTION_PROMOTE = "Promote";
    
    private static final String ACTION_ITEM_CLICK = "itemClick";

    private LifecycleBean lifeCycle;
    private String[] dependencyList;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        int userId = 2;
        userInfo = UserListCsvReader.getUserInfo(userId);

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();


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

        relationServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        searchAdminServiceClient =
                new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        wsRegistryServiceClient =
                new RegistryProviderUtil().getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
                                           AddServicesServiceRegistryExceptionException,
                                           ListMetadataServiceRegistryExceptionException,
                                           ResourceAdminServiceExceptionException,
                                           CustomLifecyclesChecklistAdminServiceExceptionException {

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

        ServiceBean service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
            }
        }

        //set the dependencies
        dependencyList = lifeCycleAdminServiceClient.getAllDependencies("/_system/governance" +
                                                                        serviceString);


    }

    @Test(dependsOnMethods = "testCreateService")
    public void testAddResource()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {

        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        String fileType = "plain/text";
        resourceAdminServiceClient.addResource(ROOT + DEPENDENCY_RES_NAME, fileType, RES_DESC, dataHandler);

        String authorUserName =
                resourceAdminServiceClient.getResource(ROOT + DEPENDENCY_RES_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Dependency resource creation failure");

    }

    @Test(description = "Add dependency", dependsOnMethods = "testAddResource")
    public void testAddDependencyToService()
            throws AddAssociationRegistryExceptionException, RemoteException {
        String dependencyType = "depends";
        String todo = "add";

        relationServiceClient.addAssociation(SERVICE_LOCATION + serviceString,
                                             dependencyType, ROOT + DEPENDENCY_RES_NAME, todo);

        DependenciesBean bean = relationServiceClient.getDependencies(SERVICE_LOCATION + serviceString);

        boolean dependencyTypeMatches = false;
        boolean targetDependencyMatches = false;
        boolean sourceDependencyMatches = false;

        for (AssociationBean dBean : bean.getAssociationBeans()) {
            if (dBean.getDestinationPath().equalsIgnoreCase(ROOT + DEPENDENCY_RES_NAME)) {
                targetDependencyMatches = true;

                if (dBean.getAssociationType().equalsIgnoreCase(dependencyType)) {
                    dependencyTypeMatches = true;
                }

                if (dBean.getSourcePath().equalsIgnoreCase(SERVICE_LOCATION + serviceString)) {
                    sourceDependencyMatches = true;
                }
            }

        }
        assertTrue(targetDependencyMatches,
                   "Target dependency is not correct");

        assertTrue(dependencyTypeMatches,
                   "Dependency type is not correct");


        assertTrue(sourceDependencyMatches,
                   "Source dependency is not correct");
    }

    @Test(description = "Add dependency", dependsOnMethods = "testAddDependencyToService")
    public void testAddAssociationToService()
            throws AddAssociationRegistryExceptionException, RemoteException {
        String dependencyType = "association";
        String todo = "add";

        relationServiceClient.addAssociation(SERVICE_LOCATION + serviceString, dependencyType, ASSOCIATION_COLL, todo);

        DependenciesBean bean = relationServiceClient.getDependencies(SERVICE_LOCATION + serviceString);

        AssociationBean[] aBeans = bean.getAssociationBeans();
        boolean found = false;

        for (AssociationBean aBean : aBeans) {
            if (ASSOCIATION_COLL.equalsIgnoreCase(aBean.getDestinationPath())) {
                found = true;
            }
        }

        assertTrue(found, "Association cannot be found");

    }


    @Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testAddDependencyToService")
    public void testCreateNewLifeCycle() throws LifeCycleManagementServiceExceptionException,
                                                IOException, InterruptedException,
                                                SearchAdminServiceRegistryExceptionException,
                                                RegistryException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "CARepService.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        LifeCycleUtils.deleteLcUsageResources(searchAdminServiceClient, wsRegistryServiceClient, LC_NAME);
        LifeCycleUtils.deleteLifeCycleIfExist(LC_NAME, lifeCycleManagementClient);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeClycles) {

            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a service",
          dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException {


        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    @Test(groups = "wso2.greg", description = "test the promoting", dependsOnMethods = "testAddLcToService")
    public void testPromoting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {

    	lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString, LC_NAME,
												ACTION_ITEM_CLICK, new String[]{"true"});
        
    	lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + serviceString,
                                                           LC_NAME, ACTION_PROMOTE, null);
        serviceString = "/_system/governance/branches/testing/services/com/abb/IntergalacticService/1.0.0-SNAPSHOT/service";

        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();
        if (properties != null) {
            for (Property prop : properties) {
                if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                    assertNotNull(prop.getValues(), "State Value Not Found");
                    assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1),
                               "Not promoted to Testing");
                }
            }
        }

    }

    @Test(groups = "wso2.greg", description = "test The dependencies after promoting",
          dependsOnMethods = "testPromoting")
    public void testDependencies()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, AddAssociationRegistryExceptionException {

        DependenciesBean bean = relationServiceClient.getDependencies(serviceString);

        //search for the association
        boolean associationFound = false;
        boolean dependencyFound = false;

        AssociationBean[] aBeans = bean.getAssociationBeans();

        if (aBeans != null) {
            associationFound = true;
            dependencyFound = true;
        }
        assertFalse(associationFound, "Association exists even after promoting the service");
        assertFalse(dependencyFound, "Dependency exists even after promoting the service");

    }

    @AfterClass
    public void cleanup()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   LifeCycleManagementServiceExceptionException {
        resourceAdminServiceClient.deleteResource(ROOT + DEPENDENCY_RES_NAME);


        resourceAdminServiceClient.deleteResource(serviceString);
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services/com/abb/IntergalacticService");

        SchemaBean schema = listMetadataServiceClient.listSchemas();

        if (schema != null) {
            String schemaPathToDelete = "/_system/governance/" + schema.getPath()[0];
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }

        WSDLBean wsdl = listMetadataServiceClient.listWSDLs();

        if (wsdl != null) {
            for (String path : wsdl.getPath()) {
                String wsdlPathToDelete = "/_system/governance/" + path;
                resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
            }

        }

        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
    }
}