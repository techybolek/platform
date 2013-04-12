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

/**
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.ClientHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.api.clients.identity.*;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.identity.tests.utils.BasicAuthHandler;
import org.wso2.carbon.identity.tests.utils.BasicAuthInfo;
import org.wso2.carbon.identity.tests.utils.SCIM.SCIMResponseHandler;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;
*/

import java.rmi.RemoteException;

public class SCIMServiceProviderUserTestCase {
/**
    public static final String CRED_USER_NAME = "admin";
    public static final String CRED_PASSWORD = "admin";
    public static final int mainUserId = 2;
    public static final String nodeId = "is001";
    private static String userName = "HasiniG";
    private static String externalID = "test";
    private static String[] emails = {"hasini@gmail.com", "hasinig@yahoo.com"};
    private static String displayName = "Hasini";
    private static String password = "dummyPW1";
    private static String language = "Sinhala";
    private static String phone_number = "0772508354";
    private UserInfo userInfo;
    String serviceEndPoint = null;
    String sessionCookie=null;

    @BeforeClass(alwaysRun = true)
    public void initiate() throws RemoteException, LoginAuthenticationExceptionException {
        userInfo = UserListCsvReader.getUserInfo(mainUserId);
        EnvironmentBuilder builder = new EnvironmentBuilder().clusterNode(nodeId,mainUserId);

        ManageEnvironment environment = builder.build();
        serviceEndPoint= environment.getClusterNode(nodeId).getBackEndUrl();
        sessionCookie= environment.getClusterNode(nodeId).getSessionCookie();
        System.setProperty("javax.net.ssl.trustStore",builder.getFrameworkSettings().getEnvironmentVariables().getKeystorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", builder.getFrameworkSettings().getEnvironmentVariables().getKeyStrorePassword());
    }
    @Test */
/**
    public void createUser() throws Exception {

        SCIMAdminClient scimAdminClient = new SCIMAdminClient(serviceEndPoint,sessionCookie);

            //create SCIM client
            SCIMClient scimClient = new SCIMClient();
        scimAdminClient.addUserProvider(userInfo.getUserName(),"trestProvider",userName,password,"https://localhost:9445/wso2/scim/Users","https://localhost:9445/wso2/scim/Groups");
            //create a user according to SCIM User Schema
            User scimUser = scimClient.createUser();
            scimUser.setUserName(userInfo.getUserName());
            scimUser.setExternalId(externalID);
            scimUser.setEmails(emails);
            scimUser.setDisplayName(displayName);
            scimUser.setPassword(password);
            scimUser.setPreferredLanguage(language);
            scimUser.setPhoneNumber(phone_number, null, false);
            //encode the user in JSON format
            String encodedUser = scimClient.encodeSCIMObject(scimUser, SCIMConstants.JSON);
            //create a apache wink ClientHandler to intercept and identify response messages
            SCIMResponseHandler responseHandler = new SCIMResponseHandler();
            responseHandler.setSCIMClient(scimClient);
            //set the handler in wink client config
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.handlers(new ClientHandler[]{responseHandler});
            //create a wink rest client with the above config
            RestClient restClient = new RestClient(clientConfig);
            //create resource endpoint to access User resource
            Resource userResource = restClient.resource("https://localhost:9445/wso2/scim/Users");

            BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
            basicAuthInfo.setUserName(CRED_USER_NAME);
            basicAuthInfo.setPassword(CRED_PASSWORD);

            BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
            BasicAuthInfo encodedBasicAuthInfo = (BasicAuthInfo) basicAuthHandler.getAuthenticationToken(basicAuthInfo);


            //TODO:enable, disable SSL. For the demo purpose, we make the calls over http
            //send previously registered SCIM consumer credentials in http headers.
            String response = userResource.
                    header(SCIMConstants.AUTHORIZATION_HEADER, encodedBasicAuthInfo.getAuthorizationHeader()).
                    contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                    post(String.class, encodedUser);

            //decode the response
            System.out.println(response);

    } */
}
