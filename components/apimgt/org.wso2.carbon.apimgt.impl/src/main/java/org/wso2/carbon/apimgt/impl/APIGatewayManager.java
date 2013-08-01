/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.utils.RESTAPIAdminClient;

import java.util.List;

public class APIGatewayManager {

    private static final Log log = LogFactory.getLog(APIGatewayManager.class);

    private static APIGatewayManager instance;
    
    private List<Environment> environments;

    private boolean debugEnabled = log.isDebugEnabled();

    private APIGatewayManager(){
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        environments = config.getApiGatewayEnvironments();
    }

    public synchronized static APIGatewayManager getInstance(){
        if(instance == null){
            instance = new APIGatewayManager();
        }
        return instance;
    }
    
    public void publishToGateway(API api, APITemplateBuilder builder, String tenantDomain) throws Exception{
        for(Environment environment : environments){
            RESTAPIAdminClient client = new RESTAPIAdminClient(api.getId(), environment);
            //If the API exists in the Gateway
            if (client.getApi(tenantDomain) != null) {

                //If the Gateway type is 'production' and the production url has been removed
                //Or if the Gateway type is 'sandbox' and the sandbox url has been removed.
                if((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType()) && api.getUrl() == null) ||
                   (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType()) && api.getSandboxUrl() == null)){
                    if(debugEnabled){
                        log.debug("Removing API " + api.getId().getApiName() + " from Environment " +
                                   environment.getName() + " since its relevant URL has been removed." );
                    }
                    //We need to remove the api from the environment since its relevant url has been removed.
                    client.deleteApi(tenantDomain);
                }
                else{
                    if(debugEnabled){
                        log.debug("API exists, updating existing API " + api.getId().getApiName() + " in environment " +
                                environment.getName());
                    }
                    client.updateApi(builder,tenantDomain);
                }
            }
            else {
                //If the Gateway type is 'production' and a production url has not been specified
                //Or if the Gateway type is 'sandbox' and a sandbox url has not been specified
                if((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType()) && api.getUrl() == null) ||
                   (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType()) && api.getSandboxUrl() == null)){

                    if(debugEnabled){
                        log.debug("Not adding API to environment " + environment.getName() + " since its endpoint URL " +
                                "cannot be found");
                    }
                    //Do not add the API, continue loop.
                    continue;
                }
                else{
                    if(debugEnabled){
                        log.debug("API does not exist, adding new API " + api.getId().getApiName() + " in environment " +
                                environment.getName());
                    }
                    client.addApi(builder,tenantDomain);
                }
            }
        }
    }
    
    public void removeFromGateway(API api, String tenantDomain) throws Exception{
        for(Environment environment : environments){
            RESTAPIAdminClient client = new RESTAPIAdminClient(api.getId(), environment);
            if (client.getApi(tenantDomain) != null) {
                if(debugEnabled){
                    log.debug("Removing API " + api.getId().getApiName() + " From environment " + environment.getName());
                }
                client.deleteApi(tenantDomain);
            }
        }
    }
    
    public boolean isAPIPublished(API api, String tenantDomain) throws Exception{
        for(Environment environment : environments){
            RESTAPIAdminClient client = new RESTAPIAdminClient(api.getId(), environment);
            //If the API exists in at least one environment, consider as published and return true.
            if(client.getApi(tenantDomain) != null){
                return true;
            }
        }
        return false;
    }
}
