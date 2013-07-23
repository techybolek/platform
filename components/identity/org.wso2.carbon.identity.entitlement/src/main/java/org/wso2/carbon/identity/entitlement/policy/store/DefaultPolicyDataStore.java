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
package org.wso2.carbon.identity.entitlement.policy.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Properties;

/**
 * This is default implementation, where data are stored in carbon registry
 */
public class DefaultPolicyDataStore implements PolicyDataStore{
    
    private String policyDataCollection = PDPConstants.ENTITLEMENT_POLICY_DATA;

    public static final String POLICY_COMBINING_PREFIX_1 =
                            "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:";

    public static final String POLICY_COMBINING_PREFIX_3 =
                            "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:";

    private static Log log = LogFactory.getLog(DefaultPolicyDataStore.class);

    @Override
    public void init(Properties properties) throws EntitlementException {
        
    }

    @Override
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException{

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getCurrentContext().getTenantId());
        try {
            Collection policyCollection;
            if(registry.resourceExists(policyDataCollection)){
                policyCollection = (Collection) registry.get(policyDataCollection);
            } else {
                policyCollection = registry.newCollection();
            }
            policyCollection.setProperty("globalPolicyCombiningAlgorithm", policyCombiningAlgorithm);
            registry.put(policyDataCollection, policyCollection);
        } catch (RegistryException e) {
            log.error("Error while updating Global combing algorithm in policy store ", e);
            throw new EntitlementException("Error while updating combing algorithm in policy store");
        }
    }

    @Override
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {

        Registry registry = EntitlementServiceComponent.
                            getGovernanceRegistry(CarbonContext.getCurrentContext().getTenantId());
        String algorithm = null;
        try{
            
            if(registry.resourceExists(policyDataCollection)){
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty("globalPolicyCombiningAlgorithm");
            }

            if(algorithm != null && algorithm.trim().length() > 0){
                if("first-applicable".equals(algorithm) || "only-one-applicable".equals(algorithm)){
                    algorithm =  POLICY_COMBINING_PREFIX_1 + algorithm;
                } else {
                    algorithm =  POLICY_COMBINING_PREFIX_3 + algorithm;
                }
                return EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);
            }

        } catch (RegistryException e){
            if(log.isDebugEnabled()){
                log.debug(e);
            }
        } catch (EntitlementException e) {
            if(log.isDebugEnabled()){
                log.debug(e);
            }
        }

        // default algorithm
        log.warn("Default Global Policy combining algorithm is used");
        return new DenyOverridesPolicyAlg();
    }

    @Override
    public String getGlobalPolicyAlgorithmName() {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getCurrentContext().getTenantId());
        String algorithm = null;
        try{

            if(registry.resourceExists(policyDataCollection)){
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty("globalPolicyCombiningAlgorithm");
            }
        } catch (RegistryException e){
            if(log.isDebugEnabled()){
                log.debug(e);
            }
        }

        // set default
        if(algorithm == null){
            algorithm = "deny-overrides";
        }

        return algorithm;
    }

    @Override
    public String[] getAllGlobalPolicyAlgorithmNames() {

        return new String[] {"deny-overrides", "permit-overrides", "first-applicable",
                "ordered-deny-overrides" ,"ordered-permit-overrides", "only-one-applicable" };
    }
}
