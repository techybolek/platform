/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.policy.version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStore;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreReader;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Resource;

import java.util.Properties;

/**
 *
 */
public class DefaultPolicyVersionManager implements PolicyVersionManager {


    private static Log log = LogFactory.getLog(DefaultPolicyVersionManager.class);

    @Override
    public void init(Properties properties) {

    }

    @Override
    public PolicyDTO getPolicy(String policyId, int version) throws IdentityException {

        // Zero means current version
        if(version == 0){
            Registry registry = EntitlementServiceComponent.
                    getGovernanceRegistry(CarbonContext.getCurrentContext().getTenantId());
            try{
                Collection collection = (Collection)registry.
                        get(EntitlementConstants.ENTITLEMENT_POLICY_VERSION + policyId);
                if(collection != null){
                    version = Integer.parseInt(collection.getProperty("version"));
                }
            } catch (RegistryException e) {
                log.error(e);
                throw  new IdentityException("Invalid policy version");
            }
        }

        PAPPolicyStore policyStore = new PAPPolicyStore();
        PAPPolicyStoreReader reader = new PAPPolicyStoreReader(policyStore);

        Resource resource = policyStore.getPolicy(Integer.toString(version),
                                    EntitlementConstants.ENTITLEMENT_POLICY_VERSION + policyId);
        if(resource == null){
            throw  new IdentityException("Invalid policy version");
        }

        return reader.readPolicyDTO(resource);
    }

    @Override
    public int createVersion(PolicyDTO policyDTO) throws IdentityException {

        PAPPolicyStore policyStore = new PAPPolicyStore();
        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getCurrentContext().getTenantId());

        int version = 0;

        try{
            Collection collection = (Collection)registry.
                    get(EntitlementConstants.ENTITLEMENT_POLICY_VERSION + policyDTO.getPolicyId());
            if(collection != null){
                version = Integer.parseInt(collection.getProperty("version"));
            } else {
                collection = registry.newCollection();
                collection.setProperty("version", Integer.toString(version + 1));
                registry.put(EntitlementConstants.ENTITLEMENT_POLICY_VERSION +
                                                            policyDTO.getPolicyId(), collection);
            }

            String policyPath = EntitlementConstants.ENTITLEMENT_POLICY_VERSION +
                                                        policyDTO.getPolicyId() + (version + 1);
            policyStore.addOrUpdatePolicy(policyDTO, policyPath);

        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Error while creating new version of policy", e);
        }
        return version;
    }

    @Override
    public void deletePolicy(String policyId) throws IdentityException {

    }
}
