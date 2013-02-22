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
package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.entitlement.dto.*;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.PolicyEditorDataFinder;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStore;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreReader;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.policy.*;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisher;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisherModule;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entitlement Admin Service Class which exposes the PAP
 */
public class EntitlementPolicyAdminService extends AbstractAdmin {

	private static Log log = LogFactory.getLog(EntitlementPolicyAdminService.class);

	/**
	 * This method persists a new XACML policy
	 * 
	 * @param policy PolicyDTO object
	 * @throws IdentityException throws if invalid policy or if policy
	 *             with same id is exist
	 */
	public void addPolicy(PolicyDTO policy) throws IdentityException {

		PAPPolicyStoreManager policyAdmin;
		AbstractPolicy policyObj;

	    EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
        if(policy.getPolicy() != null){
            policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><"));
        }
        if(!EntitlementUtil.validatePolicy(policy)){
            throw new IdentityException("Invalid Entitlement Policy. " +
                    "Policy is not valid according to XACML schema");
        }
		policyObj = PolicyReader.getInstance(null).getPolicy(policy.getPolicy());
		if (policyObj != null) {
			policyAdmin = new PAPPolicyStoreManager(new PAPPolicyStore());
			policy.setPolicyId(policyObj.getId().toASCIIString());
			// All the policies wont be active at the time been added.
			policy.setActive(policy.isActive());
			if (getPolicy(policy.getPolicyId()) != null) {
				throw new IdentityException(
						"An Entitlement Policy with the given Id already exists");
			}

			policyAdmin.addOrUpdatePolicy(policy);
            if(policy.isPromote()){
                syncPDPPolicy(policy, policyAdmin);
            }
			entitlementEngine.getPapPolicyFinder().init();
		} else {
			throw new IdentityException("Unsupported Entitlement Policy. Policy can not be parsed");
		}
	}

	/**
	 * Adds XACML policies in bulk to the system.
	 * 
	 * @param policies Array of policies.
	 * @throws IdentityException throws
	 */
	public void addPolicies(PolicyDTO[] policies) throws IdentityException {

		if (policies != null && policies.length > 0) {
			EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
			PAPPolicyStoreManager policyAdmin;
			policyAdmin = new PAPPolicyStoreManager(new PAPPolicyStore());

			for (int i = 0; i < policies.length; i++) {
				AbstractPolicy policyObj;
				PolicyDTO policy = policies[i];
                if(!EntitlementUtil.validatePolicy(policy)){
                    throw new IdentityException("Invalid Entitlement Policy. " +
                            "Policy is not valid according to XACML schema");
                }
				policyObj = PolicyReader.getInstance(null).getPolicy(policy.getPolicy());
				if (policyObj != null) {
					policy.setPolicyId(policyObj.getId().toASCIIString());
					policy.setActive(policy.isActive());
					if (getPolicy(policy.getPolicyId()) != null) {
						throw new IdentityException(
								"An Entitlement Policy with the given ID already exists");
					}
					policyAdmin.addOrUpdatePolicy(policy);
					entitlementEngine.getPapPolicyFinder().init();
				} else {
                    throw new IdentityException("Unsupported Entitlement Policy. Policy can not be parsed");
				}
			}
		} else {
			throw new IdentityException("No Entitlement policies are provided.");
		}
	}

	/**
	 * Updates given policy
	 *
	 * @param policy policy object
	 * @throws org.wso2.carbon.identity.base.IdentityException throws if invalid policy
	 */
	public void updatePolicy(PolicyDTO policy) throws IdentityException {

        PAPPolicyStoreManager policyAdmin;
		AbstractPolicy policyObj;
        String policyId;
        boolean changeActive = false;
        boolean changePolicyStatus = false;

		EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
        if(policy.getPolicy() != null){
            policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><"));
        }
        if(!EntitlementUtil.validatePolicy(policy)){
            throw new IdentityException("Invalid Entitlement Policy. " +
                    "Policy is not valid according to XACML schema");
        }

        policyId = policy.getPolicyId();

        if(policyId == null && policy.getPolicy() != null){
            policyObj = PolicyReader.getInstance(null).getPolicy(policy.getPolicy());
            if (policyObj != null) {
                policy.setPolicyId(policyObj.getId().toASCIIString());
            } else {
                throw new IdentityException("Unsupported Entitlement Policy. Policy can not be parsed");
            }
        }

        PolicyDTO oldPolicy = getLightPolicy(policyId);
        // treat this as a new policy
        if(oldPolicy == null){
            addPolicy(policy);
            return;
        }

        if(oldPolicy.isActive() != policy.isActive()){
            changeActive = true;
        }

        if(oldPolicy.isPromote() != policy.isPromote() ||
            (PolicyDTO.PAP_UN_SYNC_POLICY == policy.getPolicyLifeCycle() && policy.isPromote())){
            changePolicyStatus = true;
        }

        policyAdmin = new PAPPolicyStoreManager(new PAPPolicyStore());
        policyAdmin.addOrUpdatePolicy(policy);


        if(!changeActive && changePolicyStatus){
            syncPDPPolicy(policy, policyAdmin);
        } else if(!(changeActive && policy.getPolicy() == null)){
            if(policy.isPromote()){
                policy.setPolicyLifeCycle(PolicyDTO.PAP_UN_SYNC_POLICY);
            }
            policy.setActive(false);
            policyAdmin.addOrUpdatePolicy(policy);
        }

        // Reload the policies to the memory.
        entitlementEngine.getPapPolicyFinder().init();
	}
    
	/**
	 * This method finds the policy file from given registry path and adds the policy
	 * 
	 * @param policyRegistryPath given registry path
	 * @throws org.wso2.carbon.identity.base.IdentityException throws when fails or registry error
	 *             occurs
	 */
	public void importPolicyFromRegistry(String policyRegistryPath) throws IdentityException {

		Registry registry;
		PolicyDTO policyDTO = new PolicyDTO();
		String policy = "";
		BufferedReader bufferedReader = null;
		InputStream inputStream = null;

		// Finding from which registry by comparing prefix of resource path
		String resourceUri = policyRegistryPath.substring(policyRegistryPath.lastIndexOf(':') + 1);
		String registryIdentifier = policyRegistryPath.substring(0,
				policyRegistryPath.lastIndexOf(':'));
		if (IdentityRegistryResources.CONFIG_REGISTRY_IDENTIFIER.equals(registryIdentifier)) {
			registry = getConfigSystemRegistry();
		} else {
			registry = getGovernanceUserRegistry();
		}

		try {
			Resource resource = registry.get(resourceUri);
			inputStream = resource.getContentStream();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String stringLine;
			StringBuffer buffer = new StringBuffer(policy);
			while ((stringLine = bufferedReader.readLine()) != null) {
				buffer.append(stringLine);
			}
			policy = buffer.toString();
			policyDTO.setPolicy(policy.replaceAll(">\\s+<", "><"));
			addPolicy(policyDTO);
		} catch (RegistryException e) {
            log.error("Registry Error occurs while reading policy from registry", e);
			throw new IdentityException("Error loading policy from carbon registry");
		} catch (IOException e) {
            log.error("I/O Error occurs while reading policy from registry", e);
			throw new IdentityException("Error loading policy from carbon registry");
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.error("Error occurs while closing inputStream", e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Error occurs while closing inputStream", e);
				}
			}
		}
	}

	/**
	 * This method paginates policies
	 * 
	 * @param policyTypeFilter policy type to filter
	 * @param policySearchString policy search String
	 * @param pageNumber page number
	 * @return paginated and filtered policy set
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PaginatedPolicySetDTO getAllPolicies(String policyTypeFilter, String policySearchString,
			int pageNumber) throws IdentityException {
        
		PAPPolicyStoreReader policyReader;
		List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();
		policyReader = new PAPPolicyStoreReader(new PAPPolicyStore());
		PolicyDTO[] policyDTOs = policyReader.readAllLightPolicyDTOs();

		for (PolicyDTO policyDTO : policyDTOs) {
			boolean useAttributeFiler = false;
			// Filter out policies based on policy type
			if (!policyTypeFilter.equals("ALL")
					&& (!policyTypeFilter.equals(policyDTO.getPolicyType()) &&
                    !("Active".equals(policyTypeFilter) && policyDTO.isActive()) &&
                    !("Promoted".equals(policyTypeFilter) && 
                            policyDTO.isPromote()))) {
				continue;
			}

            if(policySearchString != null && policySearchString.trim().length() > 0){
                // Filter out policies based on attribute value
                PolicyDTO metaDataPolicyDTO = policyReader.readMetaDataPolicyDTO(policyDTO.getPolicyId());
                AttributeDTO[] attributeDTOs = metaDataPolicyDTO.getPolicyMetaData();
                for (AttributeDTO attributeDTO : attributeDTOs) {
                    if (policySearchString.equals(attributeDTO.getAttributeValue())) {
                        useAttributeFiler = true;
                        break;
                    }
                }

                if (!useAttributeFiler) {
                    // Filter out policies based on policy Search String
                    if (policySearchString != null && policySearchString.trim().length() > 0) {
                        if (policyDTO.getPolicyId().toLowerCase()
                                .indexOf(policySearchString.toLowerCase()) == -1) {
                            continue;
                        }
                    }
                }
            }

			policyDTOList.add(policyDTO);
		}

		// Do the pagination and return the set of policies.
		return doPaging(pageNumber, policyDTOList.toArray(new PolicyDTO[policyDTOList.size()]));
	}

	/**
	 * Gets policy for given policy id
	 * 
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PolicyDTO getPolicy(String policyId) throws IdentityException {
		PAPPolicyStoreReader policyReader = null;
        EntitlementEngine.getInstance();
		policyReader = new PAPPolicyStoreReader(new PAPPolicyStore());
		return policyReader.readPolicyDTO(policyId);
	}


	/**
	 * Gets light weight policy DTO for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PolicyDTO getLightPolicy(String policyId) throws IdentityException {
		PAPPolicyStoreReader policyReader = null;
        EntitlementEngine.getInstance();
		policyReader = new PAPPolicyStoreReader(new PAPPolicyStore());
		return policyReader.readLightPolicyDTO(policyId);
	}

	/**
	 * Gets light weight policy DTO with attribute Meta data for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PolicyDTO getMetaDataPolicy(String policyId) throws IdentityException {
		PAPPolicyStoreReader policyReader = null;
        EntitlementEngine.getInstance();
		policyReader = new PAPPolicyStoreReader(new PAPPolicyStore());
		return policyReader.readMetaDataPolicyDTO(policyId);
	}

	/**
	 * Removes policy for given policy object
	 * 
	 * @param policy policy object
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public void removePolicy(PolicyDTO policy) throws IdentityException {
		PAPPolicyStoreManager policyAdmin;
		EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
		policyAdmin = new PAPPolicyStoreManager(new PAPPolicyStore());

        PolicyDTO oldPolicy = getLightPolicy(policy.getPolicyId());
        if(oldPolicy != null && oldPolicy.isPromote()){
            policy.setPromote(false);
            syncPDPPolicy(policy, null);
        }

        policyAdmin.removePolicy(policy);
		// Reload the policies to the memory.
		entitlementEngine.getPapPolicyFinder().init();
	}

    /**
     * get attribute values to XACML editor UI from meta data finder modules
     * @return Array of attribute value trees as PolicyEditorAttributeDTO object
     * @throws IdentityException throws, if fails
     */
    public PolicyEditorAttributeDTO[] getPolicyAttributeValues()
            throws IdentityException {
        Set<PolicyEditorAttributeDTO> policyAttributeDTOSet = null;
		PolicyEditorDataFinder metaDataFinder = EntitlementEngine.getInstance().getMetaDataFinder();
        policyAttributeDTOSet = metaDataFinder.getPolicyAttributeValues();
        if(policyAttributeDTOSet != null){
            return policyAttributeDTOSet.toArray(new PolicyEditorAttributeDTO[policyAttributeDTOSet.size()]);
        }

        return null;
    }

	/**
	 * This method returns the list of policy id available in PDP
	 * 
	 * @return list of ids
	 * @throws IdentityException throws
	 */
	public String[] getAllPolicyIds() throws IdentityException {
		List<String> policyIds = new ArrayList<String>();
		PAPPolicyStoreReader policyReader;
        EntitlementEngine.getInstance();
		policyReader = new PAPPolicyStoreReader(new PAPPolicyStore());

		for (PolicyDTO policyDTO : policyReader.readAllLightPolicyDTOs()) {
			policyIds.add(policyDTO.getPolicyId());
		}
		return policyIds.toArray(new String[policyIds.size()]);
	}

	/**
	 * Sets policy combining algorithm globally
	 * 
	 * @param policyCombiningAlgorithm policy combining algorithm as a String
	 * @throws IdentityException throws
	 */
	public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws IdentityException {
		EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
        PAPPolicyStore policyStore = new PAPPolicyStore();
		policyStore.addPolicyCombiningAlgorithm(policyCombiningAlgorithm);
        // set policy combining algorithm in policy store also
        entitlementEngine.getPolicyStoreManager().setPolicyCombiningAlgorithm();
		entitlementEngine.getPapPolicyFinder().init();
	}

	/**
	 * Gets globally defined policy combining algorithm
	 * 
	 * @return policy combining algorithm as a String
	 * @throws IdentityException throws
	 */
	public String getGlobalPolicyAlgorithm() throws IdentityException {
		EntitlementEngine.getInstance();
        PAPPolicyStoreReader reader = new PAPPolicyStoreReader(new PAPPolicyStore());
        return reader.readPolicyCombiningAlgorithm();
	}

    /**
     * policy re-ordering
     * 
     * @param policyDTOs
     * @throws  IdentityException
     */
    public void reOderPolicies(PolicyDTO[] policyDTOs) throws IdentityException {

        boolean success = true;

        for(PolicyDTO dto : policyDTOs){
            try {
                updatePolicy(dto);  // TODO as transactions
            } catch (IdentityException e) {
                // ignore this error as this is reOrdering process
                success = false;
            }
        }
        
        if(success){
            PolicyStoreManager manager = EntitlementEngine.getInstance().getPolicyStoreManager();
            for(PolicyDTO dto : policyDTOs){
                manager.promotePolicy(dto.getPolicyId());
                updatePolicy(dto);
            }
        }
        EntitlementEngine.getInstance().getPapPolicyFinder().init();
    }


    /**
     * Gets subscriber details
     *
     * @param id  subscriber id
     * @return subscriber details as SubscriberDTO
     * @throws IdentityException throws, if any error
     */
    public ModuleDataHolder getSubscriber(String id) throws IdentityException {

        PolicyPublisher publisher = EntitlementEngine.getInstance().getPolicyPublisher();
        return publisher.retrieveSubscriber(id);
    }

    /**
     * Gets all subscribers ids that is registered,
     *
     * @return subscriber's ids as String array
     * @throws IdentityException throws, if fails
     */
    public String[] getSubscriberIds() throws IdentityException {

        PolicyPublisher publisher = EntitlementEngine.getInstance().getPolicyPublisher();
        String[] ids = publisher.retrieveSubscriberIds();
        if(ids != null){
            return ids;
        } else {
            return new String[0];
        }
    }


    /**
     * Set or update subscriber details in to registry
     *
     * @param holder subscriber data as ModuleDataHolder object
     * @param update boolean indicating if this is an update or add
     * @throws IdentityException throws, if fails
     */
    public void updateSubscriber(ModuleDataHolder holder,boolean update) throws IdentityException {

        PolicyPublisher publisher = EntitlementEngine.getInstance().getPolicyPublisher();
        publisher.persistSubscriber(holder,update);

    }

    /**
     * delete subscriber details from registry
     *
     * @param subscriberId subscriber id
     * @throws IdentityException throws, if fails
     */
    public void deleteSubscriber(String subscriberId) throws IdentityException {

        PolicyPublisher publisher = EntitlementEngine.getInstance().getPolicyPublisher();
        publisher.deleteSubscriber(subscriberId);

    }

    /**
     * Publishes given set of policies to all subscribers
     *
     * @param policyIds policy ids to publish,  if null or empty, all policies are published
     * @param subscriberIds subscriber ids to publish,  if null or empty, all policies are published
     * @throws IdentityException throws, if fails
     */
    public void publishPolicies(String[] policyIds, String[] subscriberIds)  throws IdentityException {

        EntitlementEngine engine = EntitlementEngine.getInstance();
        PolicyPublisher publisher = engine.getPolicyPublisher();
        PAPPolicyStore policyStore = new PAPPolicyStore();
        if(policyIds == null || policyIds.length < 1){
            policyIds = policyStore.getAllPolicyIds();
        }
        if(subscriberIds == null || subscriberIds.length < 1){
            subscriberIds = publisher.retrieveSubscriberIds();
        }

        if(policyIds == null || policyIds.length  < 1){
            throw new IdentityException("There are no policies to publish");
        }

        if(subscriberIds == null || subscriberIds.length < 1){
            throw new IdentityException("There are no subscribers to publish");
        }

        publisher.publishPolicy(policyIds, subscriberIds);
    }


    /**
     * Gets policy publisher module data to populate in the UI
     *
     * @return
     */
    public ModuleDataHolder[]  getPublisherModuleProperties(){

        List<ModuleDataHolder> holders = EntitlementServiceComponent.
            getEntitlementConfig().getModulePropertyHolders(PolicyPublisherModule.class.getName());
        if(holders != null){
            return holders.toArray(new ModuleDataHolder[holders.size()]);
        }

        return null;
    }

    /**
     * Helper method
     *
     * @param policyDTO
     * @param papManager
     * @throws IdentityException
     */
    private void syncPDPPolicy(PolicyDTO policyDTO, PAPPolicyStoreManager papManager)
                                                                        throws IdentityException {

        PolicyStoreManager manager = EntitlementEngine.getInstance().getPolicyStoreManager();

        if(policyDTO.isPromote()){
            if(manager.promotePolicy(policyDTO.getPolicyId())){
                policyDTO.setPromote(true);
                policyDTO.setPolicyLifeCycle(PolicyDTO.PDP_PROMOTED_POLICY);
            } else {
                throw new IdentityException("Policy can not be promoted to PDP policy store");
            }
        } else {
            if(manager.removePolicy(policyDTO.getPolicyId())){
                policyDTO.setPolicyLifeCycle(PolicyDTO.PAP_POLICY);
            } else {
                throw new IdentityException("Policy can not be removed from PDP policy store");
            }
        }

        if(papManager != null){
            papManager.addOrUpdatePolicy(policyDTO);
        }
    }

	/**
	 * This method is used internally to do the pagination purposes.
	 * 
	 * @param pageNumber page Number
	 * @param policySet set of policies
	 * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies
	 *         that reside in the given page.
	 */
	private PaginatedPolicySetDTO doPaging(int pageNumber, PolicyDTO[] policySet) {

		PaginatedPolicySetDTO paginatedPolicySet = new PaginatedPolicySetDTO();
		if (policySet.length == 0) {
			paginatedPolicySet.setPolicySet(new PolicyDTO[0]);
			return paginatedPolicySet;
		}
		String itemsPerPage = ServerConfiguration.getInstance().getFirstProperty("ItemsPerPage");
		int itemsPerPageInt = EntitlementConstants.DEFAULT_ITEMS_PER_PAGE;
		if (itemsPerPage != null) {
			itemsPerPageInt = Integer.parseInt(itemsPerPage);
		}
		int numberOfPages = (int) Math.ceil((double) policySet.length / itemsPerPageInt);
		if (pageNumber > numberOfPages - 1) {
			pageNumber = numberOfPages - 1;
		}
		int startIndex = pageNumber * itemsPerPageInt;
		int endIndex = (pageNumber + 1) * itemsPerPageInt;
		PolicyDTO[] returnedPolicySet = new PolicyDTO[itemsPerPageInt];

		for (int i = startIndex, j = 0; i < endIndex && i < policySet.length; i++, j++) {
			returnedPolicySet[j] = policySet[i];
		}

		paginatedPolicySet.setPolicySet(returnedPolicySet);
		paginatedPolicySet.setNumberOfPages(numberOfPages);

		return paginatedPolicySet;
	}
}
