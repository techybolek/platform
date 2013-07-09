/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.pap.store;

import org.wso2.balana.finder.PolicyFinder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.identity.entitlement.policy.PolicyReader;
import org.wso2.balana.AbstractPolicy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.nio.charset.Charset;
import java.util.*;

public class PAPPolicyStoreReader {

    // the optional logger used for error reporting
    private static Log log = LogFactory.getLog(PAPPolicyStoreReader.class);

    private PAPPolicyStore store;

    /**
     * 
     * @param store
     */
    public PAPPolicyStoreReader(PAPPolicyStore store) {
        this.store = store;
    }


    /**
     *
     * @param policyId
     * @param finder
     * @return
     * @throws IdentityException
     */
    public synchronized AbstractPolicy readActivePolicy(String policyId, PolicyFinder finder)
                                                                        throws IdentityException {
        Resource resource = store.getPolicy(policyId, IdentityRegistryResources.ENTITLEMENT);
        if(resource != null){
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                try {
                    String policy = new String((byte[]) resource.getContent(),  Charset.forName("UTF-8"));
                    return PolicyReader.getInstance(null).getPolicy(policy);
                } catch (RegistryException e) {
                    log.error("Error while parsing entitlement policy", e);
                    throw new IdentityException("Error while loading entitlement policy");
                }
            }                       
        }
        return null;
    }

    /**
     * Reads All policies as Light Weight PolicyDTO
     * @return Array of PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO[] readAllLightPolicyDTOs() throws IdentityException {
        String[] resources = null;
        PolicyDTO[] policies = null;
        resources = store.getAllPolicyIds();

        if (resources == null) {
            return new PolicyDTO[0];
        }
        policies = new PolicyDTO[resources.length];

        List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();
        int[] policyOrder = new int[resources.length];

        for(int i = 0; i < resources.length; i++){
            PolicyDTO policyDTO = readLightPolicyDTO(resources[i]);
            policyDTOList.add(policyDTO);
            policyOrder[i] = policyDTO.getPolicyOrder();
        }

        // sorting array            TODO  : with Comparator class
        int[] tempArray = new int[policyOrder.length];
        Arrays.sort(policyOrder);
        for (int i = 0; i < tempArray.length; i++) {
            int j = (policyOrder.length-1)-i;
            tempArray[j] = policyOrder[i];
        }
        policyOrder = tempArray;

        for (int i = 0; i < policyOrder.length; i++) {
            for(PolicyDTO policyDTO : policyDTOList){
                if(policyOrder[i] == policyDTO.getPolicyOrder()){
                    policies[i] = policyDTO;
                }
            }
        }

        return policies;
    }

    /**
     * Reads PolicyDTO for given policy id
     * @param policyId  policy id
     * @return PolicyDTO 
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO readPolicyDTO(String policyId) throws IdentityException {
        Resource resource = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;        
        try {
            resource = store.getPolicy(policyId, IdentityRegistryResources.ENTITLEMENT);
            if (resource == null) {
                return null;
            }
            String userName = CarbonContext.getCurrentContext().getUsername();
            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            RealmService realmService = EntitlementServiceComponent.getRealmservice();
            if(realmService != null){
                policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(),"write" );
                policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(),"delete" );
            }
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicy(new String((byte[]) resource.getContent(), Charset.forName("UTF-8")));
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);            
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }

            if ("true".equals(resource.getProperty(EntitlementConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }      
            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }            
            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(EntitlementConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new IdentityException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new IdentityException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }


    /**
     * Reads Light Weight PolicyDTO for given policy id
     * @param policyId  policy id
     * @return PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO readLightPolicyDTO(String policyId) throws IdentityException {

        Resource resource = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;
        try {
            resource = store.getPolicy(policyId, IdentityRegistryResources.ENTITLEMENT);
            if (resource == null) {
                return null;
            }
            String userName = CarbonContext.getCurrentContext().getUsername();
            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            RealmService realmService = EntitlementServiceComponent.getRealmservice();
            if(realmService != null){
                policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(), "write");
                policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(), "delete");
            }
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            if ("true".equals(resource.getProperty(EntitlementConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }
            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }
            
            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));

            return dto;
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new IdentityException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }

    /**
     * Reads Light Weight PolicyDTO with Attribute meta data for given policy id
     * @param policyId  policy id
     * @return PolicyDTO but don not contains XACML policy
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO readMetaDataPolicyDTO(String policyId) throws IdentityException {
        Resource resource = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;
        try {
            resource = store.getPolicy(policyId, IdentityRegistryResources.ENTITLEMENT);
            if (resource == null) {
                return null;
            }
            String userName = CarbonContext.getCurrentContext().getUsername();
            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            RealmService realmService = EntitlementServiceComponent.getRealmservice();
            if(realmService != null){
                policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(), "write");
                policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(), "delete");
            }
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            if ("true".equals(resource.getProperty(EntitlementConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }

            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(EntitlementConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new IdentityException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }

    /**
     * Reads PolicyDTO for given registry resource
     * @param resource Registry resource
     * @return  PolicyDTO
     * @throws IdentityException throws, if fails
     */
    public PolicyDTO readPolicyDTO(Resource resource) throws IdentityException {
        String policy = null;
        String policyId = null;
        AbstractPolicy absPolicy = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;
        try {
            String userName = CarbonContext.getCurrentContext().getUsername();
            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            RealmService realmService = EntitlementServiceComponent.getRealmservice();
            if(realmService != null){
                policyEditable = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(),"write" );
                policyCanDelete = realmService.getTenantUserRealm(tenantId).getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(),"delete" );
            }
            policy = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));
            absPolicy = PolicyReader.getInstance(null).getPolicy(policy);
            policyId = absPolicy.getId().toASCIIString();
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);
            if ("true".equals(resource.getProperty(EntitlementConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            if ("true".equals(resource.getProperty(EntitlementConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(EntitlementConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);                
            }
            
            dto.setPolicyType(resource.getProperty(EntitlementConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(EntitlementConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(EntitlementConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(EntitlementConstants.ATTRIBUTE_SEPARATOR));
            }

            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(EntitlementConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(EntitlementConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(EntitlementConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new IdentityException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new IdentityException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }
}
