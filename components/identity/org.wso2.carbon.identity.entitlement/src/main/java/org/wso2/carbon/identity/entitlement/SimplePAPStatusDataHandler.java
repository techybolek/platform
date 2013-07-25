/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement;

import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.pap.EntitlementAdminEngine;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStore;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreReader;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *    TODO
 */
public class SimplePAPStatusDataHandler implements PAPStatusDataHandler{

    @Override
    public void init(Properties properties) {

    }

    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolder)
                                                                    throws EntitlementException {

        if(EntitlementConstants.Status.ABOUT_POLICY.equals(about)){
            PAPPolicyStore policyStore = new PAPPolicyStore();
            policyStore.persistStatus(key, statusHolder);
        } else {
            PolicyPublisher publisher =  EntitlementAdminEngine.getInstance().getPolicyPublisher();
            PublisherDataHolder holder =  publisher.retrieveSubscriber(key);
            holder.addStatusHolders(statusHolder);
            publisher.persistSubscriber(holder, true);
        }
    }


    @Override
    public void handle(String about, StatusHolder statusHolder) throws EntitlementException {
        List<StatusHolder> list = new ArrayList<StatusHolder>();
        list.add(statusHolder);
        handle(about, statusHolder.getKey(), list);
    }


    @Override
    public StatusHolder[] getStatusData(String about, String key, String type, String searchString)
                                                                    throws EntitlementException {
        
        if(EntitlementConstants.Status.ABOUT_POLICY.equals(about)){
            PAPPolicyStoreReader reader = new PAPPolicyStoreReader(new PAPPolicyStore());
            List<StatusHolder> holders = reader.readStatus(key);
            List<StatusHolder> filteredHolders = new ArrayList<StatusHolder>();
            if(holders != null){
                for(StatusHolder holder : holders){
                    if(type != null && type.equals(holder.getType())){
                        filteredHolders.add(holder);
                    } else if(type == null){
                        filteredHolders.add(holder);
                    }
                }
            }
            return filteredHolders.toArray(new StatusHolder[filteredHolders.size()]);
        } else {
            PolicyPublisher publisher =  EntitlementAdminEngine.getInstance().getPolicyPublisher();
            PublisherDataHolder holder =  publisher.retrieveSubscriber(key);
            return holder.getStatusHolders();
        }
    }
}
