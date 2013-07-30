/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.tenant.roles.S2Integration;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.adc.mgt.dto.SubscriptionInfo;
import org.wso2.carbon.adc.mgt.exception.ADCException;
import org.wso2.carbon.adc.mgt.exception.AlreadySubscribedException;
import org.wso2.carbon.adc.mgt.exception.DuplicateCartridgeAliasException;
import org.wso2.carbon.adc.mgt.exception.InvalidCartridgeAliasException;
import org.wso2.carbon.adc.mgt.exception.InvalidRepositoryException;
import org.wso2.carbon.adc.mgt.exception.PolicyException;
import org.wso2.carbon.adc.mgt.exception.RepositoryCredentialsRequiredException;
import org.wso2.carbon.adc.mgt.exception.RepositoryRequiredException;
import org.wso2.carbon.adc.mgt.exception.RepositoryTransportException;
import org.wso2.carbon.adc.mgt.exception.UnregisteredCartridgeException;
import org.wso2.carbon.adc.mgt.utils.ApplicationManagementUtil;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.wso2.carbon.appfactory.tenant.roles.util.CommonUtil.*;

public class SubscribeExecutor implements Runnable {
    private static final Log log = LogFactory.getLog(SubscribeExecutor.class);

    private String applicationId;
    private DeployerInfo deployerInfo;
    private String stage;
    private int tenantId;


    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setDeployerInfo(DeployerInfo deployerInfo) {
        this.deployerInfo = deployerInfo;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Thread started for application id : " + applicationId + " for cartridge type : "
                    + deployerInfo.getCartridgeType());
        }
        String repoUrl = null;
//        This is where we create a git repo
        try {
            RepositoryProvider repoProvider = (RepositoryProvider) deployerInfo.getRepoProvider().newInstance();
            repoProvider.setBaseUrl(deployerInfo.getBaseURL());
            repoProvider.setAdminUsername(deployerInfo.getAdminUserName());
            repoProvider.setAdminPassword(deployerInfo.getAdminPassword());
            repoProvider.setRepoName(generateRepoUrlFromTemplate(deployerInfo.getRepoPattern(), applicationId, stage));

            repoUrl = repoProvider.createRepository();
        } catch (InstantiationException e) {
            String msg = "Unable to create repository";
            log.error(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Unable to create repository";
            log.error(msg, e);
        } catch (AppFactoryException e) {
            String msg = "Unable to create repository";
            log.error(msg, e);
        }


        /* Alias needs to be alpha-numeric - lower case */
        String alias =  new StringBuilder(deployerInfo.getAlias()).append(applicationId).toString().toLowerCase();
        try {

            if (deployerInfo.getEndpoint() != null) {

//                Subscribing via the Utility method.
//                Username is passed as null because it is only used when an internal git repo is used
                                
                ApplicationManagementUtil.doSubscribe(deployerInfo.getCartridgeType(),
                                                      alias,
                                                      /*This will the name of policy file*/
                                                      deployerInfo.getPolicyName() , repoUrl,
                                                      true,deployerInfo.getAdminUserName(), 
                                                      deployerInfo.getAdminPassword(), 
                                                      deployerInfo.getDataCartridgeType(),
                                                      deployerInfo.getDataCartridgeAlias(), null /*This argument needs to be removed*/,
                                                      tenantId,applicationId);
                
                
            }

        } catch (ADCException e){
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId;
            log.error(msg, e);
        }  catch (PolicyException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " policy name: " + deployerInfo.getPolicyName();
            log.error(msg, e);
            
        } catch (UnregisteredCartridgeException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " cartridge type: " + deployerInfo.getCartridgeType();
            log.error(msg, e);
        } catch (InvalidCartridgeAliasException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " cartridge alias used : " + alias;
            log.error(msg, e);
        } catch (DuplicateCartridgeAliasException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " cartridge alias used : " + alias;
            log.error(msg, e);
        } catch (RepositoryRequiredException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " repo Url : " + repoUrl;
            log.error(msg, e);
        } catch (AlreadySubscribedException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId;
            log.error(msg, e);

        } catch (RepositoryCredentialsRequiredException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " repo user name : " + deployerInfo.getAdminUserName();
            log.error(msg, e);
           
        } catch (InvalidRepositoryException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " repo Url : " + repoUrl;
            log.error(msg, e);
        } catch (RepositoryTransportException e) {
            String msg = "Unable to subscribe to the cartridge : " + deployerInfo.getCartridgeType() +
                    " for application : " + applicationId + " repo Url : " + repoUrl;
            log.error(msg, e);
        }

    }
    
    private String generateRepoUrlFromTemplate(String pattern, String applicationId, String stage) {
        return pattern.replace("{@application_key}", applicationId).replace("{@stage}", stage);

    }
}
