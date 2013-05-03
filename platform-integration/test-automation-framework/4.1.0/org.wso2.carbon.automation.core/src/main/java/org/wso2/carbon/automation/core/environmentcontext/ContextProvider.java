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

package org.wso2.carbon.automation.core.environmentcontext;

import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.core.environmentcontext.environmentenum.NodeType;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.Context;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.EnvironmentContext;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.GroupContext;
import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.FrameworkContext;
import org.wso2.carbon.automation.core.globalcontext.GlobalContextInitiator;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;

import java.rmi.RemoteException;

public class ContextProvider {
    private String managerSessionCookie;
    private String serviceUrl;
    private String secureServiceUrl;
    private EnvironmentContext context;
    private Context managerContext;
    private Context workerContext;
    private FrameworkContext frameworkContext;

    public void ContextProvider() {
        GlobalContextInitiator globalContextInitiator = new GlobalContextInitiator();
        frameworkContext = new FrameworkContext();
        frameworkContext = globalContextInitiator.getContext().getFrameworkContext();
    }

    public EnvironmentContext getNodeContext(String nodeId, int tenant)
            throws RemoteException, LoginAuthenticationExceptionException {
        ContextProvider();
        context = new EnvironmentContext();
        Context nodeInfoContext;
        new Context();
        NodeContextProvider nodeContextProvider = new NodeContextProvider();
        nodeInfoContext = nodeContextProvider.getContext(nodeId);
        nodeInfoContext.getInstanceProperties();
        nodeInfoContext.getInstanceVariables();
        NodeContextProvider contextProvider = new NodeContextProvider();
        nodeInfoContext = contextProvider.getContext(nodeId);
        if (frameworkContext.getEnvironmentSettings().isClusterEnable()) {
            if (nodeInfoContext.getInstanceProperties().getNodeType().equals(NodeType.node.name())) {
                managerContext = nodeContextProvider.getManager(nodeId);
                workerContext = nodeContextProvider.getWorker(nodeId);
            }
        } else {
            managerContext = nodeInfoContext;
            workerContext = nodeInfoContext;
        }
        UserInfo userInfo = UserListCsvReader.getUserInfo(tenant);
        AuthenticatorClient serviceAuthentication = new AuthenticatorClient(managerContext.getInstanceVariables().getBackendUrl());
        managerSessionCookie = serviceAuthentication.login(userInfo.getUserName(),
                userInfo.getPassword(), managerContext.getInstanceVariables().getHostName());
        return setContext(tenant, serviceAuthentication, frameworkContext,
                managerContext, workerContext);
    }


    public EnvironmentContext getNodeContextByGroup(String groupId, int tenant)
            throws RemoteException, LoginAuthenticationExceptionException {
        GroupContextProvider providerContext = new GroupContextProvider();
        GroupContext groupContext = providerContext.getGroupContext(groupId);
        context = new EnvironmentContext();
        Context nodeInfoContext;
        new Context();
        NodeContextProvider nodeContextProvider = new NodeContextProvider();
        nodeInfoContext = nodeContextProvider.getContext(groupContext.getNode().getNodeId());
        nodeInfoContext.getInstanceProperties();
        nodeInfoContext.getInstanceVariables();
        NodeContextProvider contextProvider = new NodeContextProvider();
        managerContext = nodeContextProvider.getManager(groupContext.getNode().getNodeId());
        workerContext = nodeContextProvider.getWorker(groupContext.getNode().getNodeId());
        nodeInfoContext = contextProvider.getContext(groupContext.getNode().getNodeId());
        if (frameworkContext.getEnvironmentSettings().isClusterEnable()) {
            if (nodeInfoContext.getInstanceProperties().getNodeType().equals(NodeType.node.name())) {
                managerContext = nodeContextProvider.getManager(groupContext.getNode().getNodeId());
                workerContext = nodeContextProvider.getWorker(groupContext.getNode().getNodeId());
            }
        } else {
            managerContext = nodeInfoContext;
            workerContext = nodeInfoContext;
        }
        UserInfo userInfo = UserListCsvReader.getUserInfo(tenant);
        AuthenticatorClient serviceAuthentication = new AuthenticatorClient(managerContext.getInstanceVariables().getBackendUrl());
        managerSessionCookie = serviceAuthentication.login(userInfo.getUserName(),
                userInfo.getPassword(), managerContext.getInstanceVariables().getHostName());
        return setContext(tenant, serviceAuthentication, frameworkContext,
                managerContext, workerContext);
    }

    /**
     * The module for high level node implementation of the selection nodes as per the scenario.
     */
    public void getGroupContext(String groupId, int nodeIndex) {

    }

    private EnvironmentContext setContext(int userID,
                                          AuthenticatorClient managerServiceAuthentication,
                                          FrameworkContext frameworkcontext,
                                          Context managerContext, Context workerContext)
            throws LoginAuthenticationExceptionException, RemoteException {
        UserInfo userInfo = UserListCsvReader.getUserInfo(userID);
        managerSessionCookie = managerServiceAuthentication.login(userInfo.getUserName(),
                userInfo.getPassword(), managerContext.getInstanceVariables().getHostName());
        serviceUrl = getServiceURL(frameworkcontext, managerContext, workerContext, userInfo);
        secureServiceUrl = getSecureServiceURL(frameworkcontext, managerContext, managerContext, userInfo);
        context.setAuthenticatorClient(managerServiceAuthentication);
        context.setBackEndUrl(managerContext.getInstanceVariables().getBackendUrl());
        context.setSecureServiceUrl(secureServiceUrl);
        context.setManagerVariables(managerContext.getInstanceVariables());
        context.setWorkerVariables(workerContext.getInstanceVariables());
        context.setSessionCookie(managerSessionCookie);
        context.setServiceUrl(serviceUrl);
        context.setFrameworkContext(frameworkcontext);
        return context;
    }

    private String getServiceURL(FrameworkContext frameworkProperties,
                                 Context managerContext, Context workerContext, UserInfo userInfo) {
        String generatedServiceURL;
        if (frameworkProperties.getEnvironmentSettings().isClusterEnable()) {
            if (workerContext.getInstanceVariables().getNhttpPort() != null) { //if port is nhttp port
                generatedServiceURL = new UrlGenerator().
                        getHttpServiceURL(workerContext, frameworkProperties, userInfo);
            } else {
                generatedServiceURL = new UrlGenerator().
                        getHttpServiceURL(workerContext, frameworkProperties, userInfo);
            }
        } else {
            if (managerContext.getInstanceVariables().getNhttpPort() != null) { //if port is nhttp port
                generatedServiceURL = new UrlGenerator().
                        getHttpServiceURL(managerContext, frameworkProperties, userInfo);
            } else {
                generatedServiceURL = new UrlGenerator().
                        getHttpServiceURL(managerContext, frameworkProperties, userInfo);
            }
        }
        return generatedServiceURL;
    }


    private String getSecureServiceURL(FrameworkContext frameworkProperties,
                                       Context managerContext, Context workerContext, UserInfo userInfo) {
        String generatedSecureServiceURL;
        if (frameworkContext.getEnvironmentSettings().isClusterEnable()) {
            if (workerContext.getInstanceVariables().getNhttpsPort() != null) { //if port is nhttp port
                generatedSecureServiceURL = new UrlGenerator().
                        getHttpsServiceURL(workerContext, frameworkProperties, userInfo);
            } else {
                generatedSecureServiceURL = new UrlGenerator().
                        getHttpsServiceURL(workerContext, frameworkProperties, userInfo);
            }
        } else {
            if (managerContext.getInstanceVariables().getNhttpPort() != null) { //if port is nhttp port
                generatedSecureServiceURL = new UrlGenerator().
                        getHttpsServiceURL(managerContext, frameworkProperties, userInfo);
            } else {
                generatedSecureServiceURL = new UrlGenerator().
                        getHttpsServiceURL(managerContext, frameworkProperties, userInfo);
            }
        }
        return generatedSecureServiceURL;
    }


}
