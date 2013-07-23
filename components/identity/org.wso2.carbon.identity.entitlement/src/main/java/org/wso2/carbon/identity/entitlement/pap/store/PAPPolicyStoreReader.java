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
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.identity.entitlement.policy.PolicyReader;
import org.wso2.balana.AbstractPolicy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
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
     * @throws EntitlementException
     */
    public synchronized AbstractPolicy readActivePolicy(String policyId, PolicyFinder finder)
                                                                        throws EntitlementException {
        Resource resource = store.getPolicy(policyId, IdentityRegistryResources.ENTITLEMENT);
        if(resource != null){
            if ("true".equals(resource.getProperty(PDPConstants.ACTIVE_POLICY))) {
                try {
                    String policy = new String((byte[]) resource.getContent(),  Charset.forName("UTF-8"));
                    return PolicyReader.getInstance(null).getPolicy(policy);
                } catch (RegistryException e) {
                    log.error("Error while parsing entitlement policy", e);
                    throw new EntitlementException("Error while loading entitlement policy");
                }
            }                       
        }
        return null;
    }

    /**
     * Reads All policies as Light Weight PolicyDTO
     * @return Array of PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO[] readAllLightPolicyDTOs() throws EntitlementException {

        String[] resources = null;
        resources = store.getAllPolicyIds();

        if (resources == null) {
            return new PolicyDTO[0];
        }

        List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();

        for (String resource : resources) {
            PolicyDTO policyDTO = readLightPolicyDTO(resource);
            policyDTOList.add(policyDTO);
        }

        return policyDTOList.toArray(new PolicyDTO[policyDTOList.size()]);
    }

    /**
     * Reads PolicyDTO for given policy id
     * @param policyId  policy id
     * @return PolicyDTO 
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readPolicyDTO(String policyId) throws EntitlementException {
        Resource resource = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;        
        try {
            resource = store.getPolicy(policyId, IdentityRegistryResources.ENTITLEMENT);
            if (resource == null) {
                log.error("Policy does not exist in the system with id " + policyId);
                throw new EntitlementException("Policy does not exist in the system with id " + policyId);
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
            if ("true".equals(resource.getProperty(PDPConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }

            if ("true".equals(resource.getProperty(PDPConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }      
            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }            
            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(PDPConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }


    /**
     * Reads Light Weight PolicyDTO for given policy id
     * @param policyId  policy id
     * @return PolicyDTO but don not contains XACML policy and attribute meta data
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readLightPolicyDTO(String policyId) throws EntitlementException {

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
            if ("true".equals(resource.getProperty(PDPConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            if ("true".equals(resource.getProperty(PDPConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }
            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }
            
            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));

            return dto;
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }


    public List<StatusHolder> readStatus(String policyId) throws EntitlementException {

        Resource resource = store.getPolicy(policyId, IdentityRegistryResources.ENTITLEMENT);
        List<StatusHolder> statusHolders = new ArrayList<StatusHolder>();
        if(resource != null && resource.getProperties() != null){
            Properties properties = resource.getProperties();
            for(Map.Entry<Object, Object> entry : properties.entrySet()){
                PublisherPropertyDTO dto = new PublisherPropertyDTO();
                dto.setId((String)entry.getKey());
                Object value = entry.getValue();
                if(value instanceof ArrayList){
                    List list = (ArrayList) entry.getValue();
                    if(list != null && list.size() > 0 && list.get(0) != null){
                        if(((String)entry.getKey()).startsWith(StatusHolder.STATUS_HOLDER_NAME)){
                            StatusHolder statusHolder = new StatusHolder(EntitlementConstants.Status.ABOUT_POLICY);
                            if(list.size() > 0  && list.get(0) != null){
                                statusHolder.setType((String)list.get(0));
                            }
                            if(list.size() > 1  && list.get(1) != null){
                                statusHolder.setTimeInstance((String)list.get(1));
                            }
                            if(list.size() > 2  && list.get(2) != null){
                                statusHolder.setUser((String)list.get(2));
                            }
                            if(list.size() > 3  && list.get(3) != null){
                                statusHolder.setKey((String)list.get(3));
                            }
                            if(list.size() > 4  && list.get(4) != null){
                                statusHolder.setSuccess(Boolean.parseBoolean((String)list.get(4)));
                            }
                            if(list.size() > 5  && list.get(5) != null){
                                statusHolder.setMessage((String)list.get(5));
                            }
                            if(list.size() > 6  && list.get(6) != null){
                                statusHolder.setTarget((String) list.get(6));
                            }
                            if(list.size() > 7  && list.get(7) != null){
                                statusHolder.setTargetAction((String) list.get(7));
                            }
                            if(list.size() > 8  && list.get(8) != null){
                                statusHolder.setVersion((String) list.get(8));
                            }
                            statusHolders.add(statusHolder);
                        }
                    }
                }
            }
        }

        return statusHolders;
    }


    /**
     * Reads Light Weight PolicyDTO with Attribute meta data for given policy id
     * @param policyId  policy id
     * @return PolicyDTO but don not contains XACML policy
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readMetaDataPolicyDTO(String policyId) throws EntitlementException {
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
            if ("true".equals(resource.getProperty(PDPConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            if ("true".equals(resource.getProperty(PDPConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }

            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(PDPConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }

    /**
     * Reads PolicyDTO for given registry resource
     * @param resource Registry resource
     * @return  PolicyDTO
     * @throws EntitlementException throws, if fails
     */
    public PolicyDTO readPolicyDTO(Resource resource) throws EntitlementException {
        String policy = null;
        String policyId = null;
        AbstractPolicy absPolicy = null;
        PolicyDTO dto = null;
        boolean policyEditable = false;
        boolean policyCanDelete = false;
        try {
            String userName = CarbonContext.getCurrentContext().getUsername();
            RealmService realmService = EntitlementServiceComponent.getRealmservice();
            if(realmService != null){
                policyEditable = CarbonContext.getCurrentContext().getUserRealm().getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(),"write" );
                policyCanDelete = CarbonContext.getCurrentContext().getUserRealm().getAuthorizationManager().
                        isUserAuthorized(userName, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                resource.getPath(),"delete" );
            }
            policy = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));
            absPolicy = PolicyReader.getInstance(null).getPolicy(policy);
            policyId = absPolicy.getId().toASCIIString();
            dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicy(policy);
            dto.setPolicyEditable(policyEditable);
            dto.setPolicyCanDelete(policyCanDelete);
            if ("true".equals(resource.getProperty(PDPConstants.ACTIVE_POLICY))) {
                dto.setActive(true);
            }
            if ("true".equals(resource.getProperty(PDPConstants.PROMOTED_POLICY))) {
                dto.setPromote(true);
            }

            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if(policyOrder != null){
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);                
            }
            
            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if(policyReferences != null && policyReferences.trim().length() > 0){
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if(policySetReferences != null && policySetReferences.trim().length() > 0){
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            //read policy meta data that is used for basic policy editor
            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount = resource.getProperty(PDPConstants.
                    BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if(basicPolicyEditorMetaDataAmount != null){
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for(int i = 0; i < amount; i++){
                    basicPolicyEditorMetaData[i] = resource.
                            getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        } catch (UserStoreException e) {
            log.error("Error while loading entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while loading entitlement policy " + policyId +
                    " from PAP policy store");
        }
    }
}
