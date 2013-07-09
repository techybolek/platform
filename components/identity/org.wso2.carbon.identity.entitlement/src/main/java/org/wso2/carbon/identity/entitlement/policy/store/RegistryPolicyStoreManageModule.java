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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStore;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreReader;
import org.wso2.carbon.identity.entitlement.policy.finder.AbstractPolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.finder.registry.RegistryPolicyReader;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.*;

/**
 *
 */
public class RegistryPolicyStoreManageModule extends AbstractPolicyFinderModule
                                                                implements PolicyStoreManageModule {

    private String policyStorePath;

    private static final String MODULE_NAME = "Registry Policy Finder Module";

    private static final String PROPERTY_POLICY_STORE_PATH = "policyStorePath";

    private static final String PROPERTY_ATTRIBUTE_SEPARATOR = "attributeValueSeparator";

    private static final String DEFAULT_POLICY_STORE_PATH = "/repository/identity/entitlement" +
            "/policy/pdp";

    private static final String KEY_VALUE_POLICY_ODER = "policyOrder";

    private static final String KEY_VALUE_POLICY_META_DATA = "policyMetaData";

    private static final String POLICY_MEDIA_TYPE = "application/xacml-policy+xml";

	private static Log log = LogFactory.getLog(RegistryPolicyStoreManageModule.class);

    @Override
    public void init(Properties properties) {
        policyStorePath = properties.getProperty(PROPERTY_POLICY_STORE_PATH);
        if(policyStorePath == null){
            policyStorePath = DEFAULT_POLICY_STORE_PATH;
        }
    }

    @Override
    public void addPolicy(PolicyStoreDTO policy) throws IdentityException{

        Registry registry;
        String policyPath;
        Collection policyCollection;
        Resource resource;
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        if(policy == null || policy.getPolicy() == null || policy.getPolicy().trim().length() == 0
                 || policy.getPolicyId() == null || policy.getPolicyId().trim().length() == 0){
            throw new IdentityException("Policy can not be null");
        }

        try {
            registry = EntitlementServiceComponent.getRegistryService().
                                                            getGovernanceSystemRegistry(tenantId);

            if(registry.resourceExists(policyStorePath)){
                policyCollection = (Collection) registry.get(policyStorePath);
            } else {
                policyCollection = registry.newCollection();
            }

            registry.put(policyStorePath, policyCollection);

            policyPath = policyStorePath + policy.getPolicyId();

            if (registry.resourceExists(policyPath)) {
                resource = registry.get(policyPath);
            } else {
                resource = registry.newResource();
            }

            resource.setProperty(KEY_VALUE_POLICY_ODER, Integer.toString(policy.getPolicyOrder()));
            resource.setContent(policy.getPolicy());
            resource.setMediaType(POLICY_MEDIA_TYPE);
            AttributeDTO[] attributeDTOs = policy.getAttributeDTOs();
            if(attributeDTOs != null){
                setAttributesAsProperties(attributeDTOs, resource);
            }
            registry.put(policyPath, resource);
        } catch (RegistryException e) {
            log.error("Error while persisting policy",e);
            throw new IdentityException("Error while persisting policy" , e);
        }
    }

    @Override
    public void updatePolicy(PolicyStoreDTO policy) {

    }


    @Override
    public boolean deletePolicy(String policyIdentifier) {

        Registry registry;
        String policyPath;
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        if(policyIdentifier == null || policyIdentifier.trim().length() == 0){
            return false;
        }

        try {
            registry = EntitlementServiceComponent.getRegistryService().
                                                            getGovernanceSystemRegistry(tenantId);

            policyPath = policyStorePath + policyIdentifier;
            registry.delete(policyPath);
            return true;
        } catch (RegistryException e) {
            log.error(e);
            return false;
        }
    }


    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public String getPolicy(String policyId) {
        PolicyDTO dto;
        try {
            dto = getPolicyReader().readPolicy(policyId);
            return  dto.getPolicy();
        } catch (Exception e) {
            log.error("Policy with identifier " + policyId + " can not be retrieved " +
                    "from registry policy finder module" , e);
        }
        return null;
    }

    @Override
    public String[] getPolicies() {

        List<String> policies = new ArrayList<String>();

        try {
            PolicyDTO[] policyDTOs =  getPolicyReader().readAllPolicies();
            for(PolicyDTO dto : policyDTOs){
                if(dto.getPolicy() != null){
                    policies.add(dto.getPolicy());
                }
            }
        } catch (Exception e) {
            log.error("Policies can not be retrieved from registry policy finder module" , e);
        }
        return  policies.toArray(new String[policies.size()]);
    }

    @Override
    public String[] getPolicyIdentifiers() {
        String[] policyIds = null;
        try {
            policyIds =  getPolicyReader().getAllPolicyIds();
        } catch (Exception e) {
            log.error("Policy identifiers can not be retrieved from registry policy finder module" , e);
        }
        return  policyIds;
    }

    @Override
    public String getReferencedPolicy(String policyId) {

        // retrieve for policies that are not promoted policies but active

        PAPPolicyStoreReader papPolicyStoreReader = new PAPPolicyStoreReader(new PAPPolicyStore());

        try{
            PolicyDTO dto = papPolicyStoreReader.readPolicyDTO(policyId);
            if(dto != null && dto.getPolicy() != null && dto.isActive()){
                return dto.getPolicy();
            }
        } catch (IdentityException e) {
            log.error("Error while retrieving reference policy " + policyId);
            // ignore
        }

        return null;
    }

    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier, Set<AttributeDTO> givenAttribute) {

        PolicyDTO[] policyDTOs = null;
        Map<String, Set<AttributeDTO>> attributeMap = null;
        try {
            policyDTOs =  getPolicyReader().readAllPolicies();
        } catch (Exception e) {
            log.error("Policies can not be retrieved from registry policy finder module" , e);
        }

        if(policyDTOs != null){
            attributeMap = new  HashMap<String, Set<AttributeDTO>>();
            for (PolicyDTO policyDTO : policyDTOs) {
                Set<AttributeDTO> attributeDTOs =
                        new HashSet<AttributeDTO>(Arrays.asList(policyDTO.getAttributeDTOs()));
                String[] policyIdRef = policyDTO.getPolicyIdReferences();
                String[] policySetIdRef = policyDTO.getPolicySetIdReferences();

                if(policyIdRef != null && policyIdRef.length > 0 || policySetIdRef != null &&
                        policySetIdRef.length > 0){
                    for(PolicyDTO dto : policyDTOs){
                        if(policyIdRef != null){
                            for(String policyId : policyIdRef){
                                if(dto.getPolicyId().equals(policyId)){
                                    attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                                }
                            }
                        }
                        for(String policySetId : policySetIdRef){
                            if(dto.getPolicyId().equals(policySetId)){
                                attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                            }
                        }
                    }
                }
                attributeMap.put(policyDTO.getPolicyId(), attributeDTOs);
            }
        }

        return attributeMap;
    }


    @Override
    public int getSupportedSearchAttributesScheme() {
        return PolicyFinderModule.COMBINATIONS_BY_CATEGORY_AND_PARAMETER;
    }

    @Override
    public boolean isDefaultCategoriesSupported() {
        return true;
    }

    @Override
    public boolean isPolicyOrderingSupport() {
        return true;
    }

    /**
     *
     */
    public static void clearCache(){
        invalidateCache();
    }


    /**
     * creates policy reader instance
     * @return
     */
    private RegistryPolicyReader getPolicyReader() {

        Registry registry = null;
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
        } catch (RegistryException e) {
            log.error("Error while obtaining registry for tenant :  " + tenantId, e);
        }
        return new RegistryPolicyReader(registry, policyStorePath);
    }

    /**
     * This helper method creates properties object which contains the policy meta data.
     *
     * @param attributeDTOs List of AttributeDTO
     * @param resource registry resource
     */
    private void setAttributesAsProperties(AttributeDTO[] attributeDTOs, Resource resource) {
        
        int attributeElementNo = 0;
        if(attributeDTOs != null){
            for(AttributeDTO attributeDTO : attributeDTOs){
                resource.setProperty(KEY_VALUE_POLICY_META_DATA + attributeElementNo,
                       attributeDTO.getCategory() + "," +
                       attributeDTO.getAttributeValue() + "," +
                       attributeDTO.getAttributeId() + "," +
                       attributeDTO.getAttributeDataType());
                attributeElementNo ++;
            }
        }
    }

}
