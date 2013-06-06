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

package org.wso2.carbon.identity.entitlement.policy.finder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.*;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.*;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.cache.EntitlementPolicyClearingCache;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.policy.collection.SimplePolicyCollection;

import java.net.URI;
import java.util.*;

/**
 * Policy finder of the WSO2 entitlement engine.  This an implementation of <code>PolicyFinderModule</code>
 * of Balana engine. Extensions can be plugged with this. 
 */
public class CarbonPolicyFinder extends PolicyFinderModule {

    private List<CarbonPolicyFinderModule> finderModules = null;

    private PolicyCollection policyCollection;

    private PolicyFinder finder;

    /**
     * this is a flag to keep whether init it has finished or not.
     */
    private static volatile boolean initFinish;

	private EntitlementPolicyClearingCache policyClearingCache = EntitlementPolicyClearingCache.getInstance();

    private LinkedHashMap<URI, AbstractPolicy> policyReferenceCache = null;

    private int maxReferenceCacheEntries = EntitlementConstants.MAX_NO_OF_IN_MEMORY_POLICIES;    

    public PolicyReader policyReader;

    private static Log log = LogFactory.getLog(CarbonPolicyFinder.class);

    @Override
    public void init(PolicyFinder finder) {
        initFinish = false;
        this.finder = finder;
        init();
    }

    private synchronized void init(){

        if(initFinish){
            return;    
        }
        
        log.info("Initializing of policy store is started at :  " + new Date());

        String maxEntries = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                getProperty(EntitlementConstants.MAX_POLICY_REFERENCE_ENTRIES);

        if(maxEntries != null){
            try{
                maxReferenceCacheEntries = Integer.parseInt(maxEntries.trim());
            } catch (Exception e){
                //ignore
            }
        }

        policyReferenceCache = new LinkedHashMap<URI, AbstractPolicy>(){

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                // oldest entry of the cache would be removed when max cache size become, i.e 50
                return size() > maxReferenceCacheEntries;
            }

        };

        PolicyCombiningAlgorithm policyCombiningAlgorithm = null;        
        // get registered finder modules
		Map<CarbonPolicyFinderModule, Properties> finderModules = EntitlementServiceComponent.
                                                getEntitlementConfig().getPolicyFinderModules();

        if(finderModules != null){
            this.finderModules = new ArrayList<CarbonPolicyFinderModule>(finderModules.keySet());
        }

        PolicyCollection tempPolicyCollection = null;
        
        // get policy collection
        Map<PolicyCollection, Properties> policyCollections = EntitlementServiceComponent.
                                                getEntitlementConfig().getPolicyCollections();
        if(policyCollections != null && policyCollections.size() > 0){
            tempPolicyCollection =  policyCollections.entrySet().iterator().next().getKey();
        } else {
            tempPolicyCollection = new SimplePolicyCollection();
        }

        // get policy reader
        policyReader = PolicyReader.getInstance(finder);

        // get order of the policy finder modules
        int[] moduleOrders = getPolicyModuleOrder();

        if(this.finderModules != null && this.finderModules.size() > 0){
            // find policy combining algorithm.
            // only check in highest order module and it would be the
            // PDP policy combining algorithm
            for(CarbonPolicyFinderModule finderModule : this.finderModules){
                if(finderModule.getModulePriority() == moduleOrders[0]){
                    String algorithm = finderModule.getPolicyCombiningAlgorithm();
                    try {
                        policyCombiningAlgorithm = EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);
                    } catch (IdentityException e) {
                        // ignore
                    }
                    break;
                }
            }

            // if policy combining algorithm is null, set default one
            if(policyCombiningAlgorithm == null){
                policyCombiningAlgorithm  = new DenyOverridesPolicyAlg();
            }

            tempPolicyCollection.setPolicyCombiningAlgorithm(policyCombiningAlgorithm);

            for (int moduleOrder : moduleOrders) {
                for(CarbonPolicyFinderModule finderModule : this.finderModules){
                    // policy collection is created by using all policies in
                    // the modules.  highest ordered module's policies would
                    // be picked 1st
                    if(finderModule.getModulePriority() == moduleOrder){
                        String[] policies = finderModule.getPolicies();
                        // check whether module itself support for policy ordering
                        // if not, sort them according to natural order
                        if(!finderModule.isPolicyOrderingSupport()){
                            Arrays.sort(policies);
                        }
                        for(String policy : policies){
                            AbstractPolicy abstractPolicy = policyReader.getPolicy(policy);
                            if(abstractPolicy != null){
                                tempPolicyCollection.addPolicy(abstractPolicy);
                            }
                        }
                    }
                }
            }
        } else {
            log.warn("No Carbon policy finder modules are registered");

        }
        
        policyCollection = tempPolicyCollection;
        initFinish = true;
        log.info("Initializing of policy store is finished at :  " + new Date());
    }

    @Override
    public String getIdentifier() {
        return super.getIdentifier();
    }
    
    @Override
    public boolean isRequestSupported() {
        return true;
    }

    @Override
    public boolean isIdReferenceSupported() {
        return true;
    }

    @Override
    public PolicyFinderResult findPolicy(EvaluationCtx context) {

        if(policyClearingCache.getFromCache() == 0){
            init(this.finder);
            policyClearingCache.addToCache(1);
            policyReferenceCache.clear();
            EntitlementEngine.getInstance().clearDecisionCache(false);
            if(log.isDebugEnabled()){
                int tenantId = CarbonContext.getCurrentContext().getTenantId();
                log.debug("Invalidation cache message is received. " +
                "Re-initialized policy finder module of current node and invalidate decision " +
                        "caching for tenantId : " + tenantId);
            }
        }

        try{
            AbstractPolicy policy = policyCollection.getEffectivePolicy(context);
            if (policy == null) {
                return new PolicyFinderResult();
            } else {
                return new PolicyFinderResult(policy);
            }
        } catch (EntitlementException e) {
            return new PolicyFinderResult(e.getStatus());
        }
    }

    @Override
    public PolicyFinderResult findPolicy(URI idReference, int type, VersionConstraints constraints,
                                                            PolicyMetaData parentMetaData) {

        AbstractPolicy policy = policyReferenceCache.get(idReference);
        
        if(policy == null){
            if(this.finderModules != null){
                for(CarbonPolicyFinderModule finderModule : this.finderModules){
                    String policyString = finderModule.getReferencedPolicy(idReference.toString());
                    if(policyString != null){
                        policy = policyReader.getPolicy(policyString);
                        if(policy != null){
                            policyReferenceCache.put(idReference, policy);
                            break;
                        }
                    }
                }
            }
        }

        if(policy != null){
            // we found a valid version, so see if it's the right kind,
            // and if it is then we return it
            if (type == PolicyReference.POLICY_REFERENCE) {
                if (policy instanceof Policy)
                    return new PolicyFinderResult(policy);
            } else {
                if (policy instanceof PolicySet)
                   return new PolicyFinderResult(policy);
            }
        }

        return new PolicyFinderResult();
    }


    /**
     * Helper method to order the module according to module number  //TODO : with Comparator class
     * Highest module number would be the 1st ordered value
     * 
     * @return int array with ordered values
     */
    private int[] getPolicyModuleOrder(){

        int[] moduleOrder = new int[finderModules.size()];

        for(int i = 0; i < finderModules.size(); i++){
            CarbonPolicyFinderModule module = finderModules.get(i);
            moduleOrder[i] = module.getModulePriority();
        }

        int[] tempArray = new int[moduleOrder.length];
        Arrays.sort(moduleOrder);
        for (int i = 0; i < tempArray.length; i++) {
            int j = (moduleOrder.length-1)-i;
            tempArray[j] = moduleOrder[i];
        }        
        moduleOrder = tempArray;

        return moduleOrder;
    }
}
