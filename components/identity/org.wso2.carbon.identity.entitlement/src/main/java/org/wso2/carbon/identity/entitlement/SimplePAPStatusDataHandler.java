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

import java.util.List;
import java.util.Properties;

/**
 *    TODO
 */
public class SimplePAPStatusDataHandler implements PAPStatusDataHandler{

    @Override
    public void init(Properties properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolder)
                                                                    throws EntitlementException {

        if(EntitlementConstants.Status.ABOUT_POLICY.equals(about)){
            PAPPolicyStoreManager manager = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager();
            PolicyDTO policyDTO = manager.getPolicy(key);
            policyDTO.addPolicyStatusHolder(statusHolder);
            manager.addOrUpdatePolicy(policyDTO);
        } else {
            PolicyPublisher publisher =  EntitlementAdminEngine.getInstance().getPolicyPublisher();
            PublisherDataHolder holder =  publisher.retrieveSubscriber(key);
            holder.addStatusHolders(statusHolder);
            publisher.persistSubscriber(holder, true);
        }
    }

    @Override
    public void handle(StatusHolder statusHolder) throws EntitlementException {

    }

    @Override
    public StatusHolder[] getStatusData(String about, String key, String type, String searchString)
                                                                    throws EntitlementException {
        
        if(EntitlementConstants.Status.ABOUT_POLICY.equals(about)){
            PAPPolicyStoreReader reader = new PAPPolicyStoreReader(new PAPPolicyStore());
            List<StatusHolder> holders = reader.readStatus(key);
            if(holders != null){
                return holders.toArray(new StatusHolder[holders.size()]);
            }
            return null;
        } else {
            PolicyPublisher publisher =  EntitlementAdminEngine.getInstance().getPolicyPublisher();
            PublisherDataHolder holder =  publisher.retrieveSubscriber(key);
            return holder.getStatusHolders();
        }
    }
}
