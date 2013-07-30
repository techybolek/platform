/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.tests.scim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.ClientHandler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.identity.scim.SCIMConfigAdminClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.environmentcontext.ContextProvider;
import org.wso2.carbon.automation.core.environmentcontext.GroupContextProvider;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.EnvironmentContext;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.GroupContext;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.identity.tests.utils.BasicAuthInfo;
import org.wso2.carbon.identity.tests.utils.SCIM.SCIMResponseHandler;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.schema.SCIMConstants;

public class SCIMServiceProviderGroupTestCase extends MasterSCIMInitiator {
    private static final Log log = LogFactory.getLog(SCIMServiceProviderGroupTestCase.class);
    public static final String DISPLAY_NAME = "eng";
    public static final String EXTERNAL_ID = "eng";
    String scimUserId = null;
    String scimGroupId = null;
    String skim_url;
    public static final int providerUserId = 0;
    public static final int consumerUserId = 0;

    private UserInfo userInfo;
    String serviceEndPoint = null;
    String backendUrl = null;
    String sessionCookie = null;
    UserManagementClient userMgtClient;
    SCIMConfigAdminClient scimConfigAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws Exception {
        userInfo = UserListCsvReader.getUserInfo(providerUserId);
        GroupContextProvider consumerGroupContext = new GroupContextProvider();
        GroupContext consumerGroup = consumerGroupContext.getGroupContext("node1");
        ContextProvider consumer = new ContextProvider();
        EnvironmentContext consumerNodeContext = consumer.getNodeContext(consumerGroup.getNode().getNodeId(), consumerUserId);
        backendUrl = consumerNodeContext.getBackEndUrl();
        skim_url = "https://" + consumerNodeContext.getWorkerVariables().getHostName() + ":" + consumerNodeContext.getWorkerVariables().getHttpsPort() + "/wso2/scim/";
        serviceEndPoint = consumerNodeContext.getBackEndUrl();
        sessionCookie = consumerNodeContext.getSessionCookie();
        userMgtClient = new UserManagementClient(backendUrl, sessionCookie);
        scimConfigAdminClient = new SCIMConfigAdminClient(backendUrl, sessionCookie);
        scimClient = new SCIMClient();
        //creating users for the test
        createUser();
    }

    @Test(alwaysRun = true, description = "Add SCIM user", priority = 2)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void createGroupTest() throws Exception {
        //create a group according to SCIM Group Schema
        Group scimGroup = scimClient.createGroup();
        scimGroup.setExternalId(EXTERNAL_ID);
        scimGroup.setDisplayName(DISPLAY_NAME);
        //set group members
        scimGroup.setMember(scimUserId, userName);

        String encodedGroup = scimClient.encodeSCIMObject(scimGroup, SCIMConstants.JSON);
        Resource groupResource = getGroupResource(scimClient, skim_url);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(userInfo);

        //send previously registered SCIM consumer credentials in http headers.
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedGroup);
        //decode the response
        System.out.println(response);
        scimGroupId = response.split(",")[0].split(":")[1].replace('"', ' ').trim();
        Assert.assertTrue(userMgtClient.roleNameExists(DISPLAY_NAME));
    }

    @Test(alwaysRun = true, description = "Add SCIM user", priority = 3)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void getGroup() throws Exception {

        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(skim_url + "Groups/" + scimGroupId);
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);
        //  Assert.assertTrue(response.split(",")[0].split(":")[1].replace('"', ' ').trim().equals(scimGroupId));
        log.info(response.toString());
    }


    @Test(alwaysRun = true, description = "Add SCIM user", priority = 6)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void deleteGroup() throws Exception {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(skim_url + "Groups/" + scimGroupId);
        //had to set content type for the delete request as well, coz wink client sets */* by default.
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                accept(SCIMConstants.APPLICATION_JSON).
                delete(String.class);

        //decode the response
        log.info(response.toString());
        Assert.assertFalse(userMgtClient.roleNameExists(DISPLAY_NAME));
    }

    @Test(alwaysRun = true, description = "Add SCIM user", priority = 5)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void listGroup() throws Exception {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);

        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(skim_url + "Groups/" + scimGroupId);
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);

        FlaggedName[] roleNames = userMgtClient.listRoles("", 100);
        for (FlaggedName role : roleNames) {
            role.getDomainName();
        }
        log.info(response.toString());
    }


    @Test(alwaysRun = true, description = "Add SCIM user", priority = 4)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void updateGroup() throws Exception {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(userInfo);
        //create resource endpoint to access a known user resource.
        Resource groupResource = restClient.resource(skim_url + "Groups/" + scimGroupId);
        String response = groupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .get(String.class);

        log.info("Retrieved group: " + response);
        //decode retrieved group
        Group decodedGroup = (Group) scimClient.decodeSCIMResponse(response, SCIMConstants.JSON, 2);

        decodedGroup.setDisplayName("eng2");
        String updatedGroupString = scimClient.encodeSCIMObject(decodedGroup, SCIMConstants.JSON);

        Resource updateGroupResource = restClient.resource(skim_url + "Groups/" + scimGroupId);
        String responseUpdated = updateGroupResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON)
                .put(String.class, updatedGroupString);
       log.info("Updated group: " + responseUpdated);
    }

    @AfterClass(alwaysRun = true)
    public void cleanOut() throws Exception {
        deleteUser();
    }

    public void createUser() throws Exception {
        //create SCIM client
        String encodedUser = getScimUser();
        //create a apache wink ClientHandler to intercept and identify response messages
        Resource userResource = getResource(scimClient, skim_url);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(userInfo);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
        scimUserId = response.split(",")[0].split(":")[1].replace('"', ' ').trim();
    }


    public void deleteUser() throws Exception {
        //create SCIM client
        String encodedUser = getScimUser();
        //create a apache wink ClientHandler to intercept and identify response messages

        Resource userResource = getResource(scimClient, skim_url, scimUserId);
        BasicAuthInfo encodedBasicAuthInfo = getBasicAuthInfo(userInfo);
        String response = userResource.
                header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                post(String.class, encodedUser);
        scimUserId = response.split(",")[0].split(":")[1].replace('"', ' ').trim();
    }
}
