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
import org.wso2.carbon.identity.entitlement.pap.EntitlementAdminEngine;
import org.wso2.carbon.identity.entitlement.pap.EntitlementDataFinder;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.policy.*;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisher;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisherModule;
import org.wso2.carbon.identity.entitlement.policy.version.PolicyVersionManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Entitlement Admin Service Class which exposes the PAP
 */
public class EntitlementPolicyAdminService extends AbstractAdmin {

	private static Log log = LogFactory.getLog(EntitlementPolicyAdminService.class);


    /**
     * Add a new XACML policy in to the system.
     *
     * @param policyDTO policy object
     * @throws IdentityException throws
     */
    public void addPolicy(PolicyDTO policyDTO) throws IdentityException {

        addOrUpdatePolicy(policyDTO, true);

    }

    
	/**
	 * Adds XACML policies in bulk to the system.
	 * 
	 * @param policies Array of policies.
	 * @throws IdentityException throws
	 */
	public void addPolicies(PolicyDTO[] policies) throws IdentityException {

        if(policies != null){
            for(PolicyDTO policyDTO : policies){
                addOrUpdatePolicy(policyDTO, true);
            }
        } else {
            throw new IdentityException("No Entitlement policies are provided.");
        }
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
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String stringLine;
            StringBuilder buffer = new StringBuilder(policy);
            while ((stringLine = bufferedReader.readLine()) != null) {
                buffer.append(stringLine);
            }
            policy = buffer.toString();
            policyDTO.setPolicy(policy.replaceAll(">\\s+<", "><"));
            addOrUpdatePolicy(policyDTO, true);
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
	 * Updates given policy
	 *
	 * @param policyDTO policy object
	 * @throws IdentityException throws if invalid policy
	 */
	public void updatePolicy(PolicyDTO policyDTO) throws IdentityException {

        addOrUpdatePolicy(policyDTO, false);

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

		List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();
		PolicyDTO[] policyDTOs = EntitlementAdminEngine.getInstance().
                                                getPapPolicyStoreManager().getAllLightPolicyDTOs();

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
                PolicyDTO metaDataPolicyDTO = EntitlementAdminEngine.getInstance().
                        getPapPolicyStoreManager().getMetaDataPolicy(policyDTO.getPolicyId());
                AttributeDTO[] attributeDTOs = metaDataPolicyDTO.getAttributeDTOs();
                for (AttributeDTO attributeDTO : attributeDTOs) {
                    if (policySearchString.equals(attributeDTO.getAttributeValue())) {
                        useAttributeFiler = true;
                        break;
                    }
                }

                if (!useAttributeFiler) {
                    // Filter out policies based on policy Search String
                    if ( policySearchString.trim().length() > 0) {
                        if (!policyDTO.getPolicyId().toLowerCase().contains(policySearchString.toLowerCase())) {
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

        PolicyDTO policyDTO = null;

        try{
            policyDTO = EntitlementAdminEngine.getInstance().
                                getPapPolicyStoreManager().getPolicy(policyId);
        } catch (IdentityException e){
            handleNotifications("Get Policy", policyId, null, false, e.getMessage());
            throw e;
        }

        handleNotifications("Get Policy", policyId, policyDTO.getPolicy(), true, null);

        return policyDTO;
	}

    /**
     * Gets policy for given policy id and version
     *
     * @param policyId policy id
     * @param version version of policy
     * @return returns policy
     * @throws org.wso2.carbon.identity.base.IdentityException throws
     */
    public PolicyDTO getPolicyByVersion(String policyId, int version) throws IdentityException {

        PolicyDTO policyDTO = null;

        try{
            PolicyVersionManager versionManager = EntitlementAdminEngine.getInstance().getVersionManager();
            policyDTO = versionManager.getPolicy(policyId, version);
        } catch (IdentityException e){
            handleNotifications("Get Policy", policyId, null, false, e.getMessage());
            throw e;
        }

        handleNotifications("Get Policy", policyId, policyDTO.getPolicy(), true, null);

        return policyDTO;
    }

	/**
	 * Gets light weight policy DTO for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PolicyDTO getLightPolicy(String policyId) throws IdentityException {

        return EntitlementAdminEngine.getInstance().
                getPapPolicyStoreManager().getLightPolicy(policyId);

	}

	/**
	 * Gets light weight policy DTO with attribute Meta data for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws   // TODO
	 */
//	public PolicyDTO getMetaDataPolicy(String policyId) throws IdentityException {
//		PAPPolicyStoreReader policyReader = null;
//        EntitlementEngine.getInstance();
//		policyReader = new PAPPolicyStoreReader(new PAPPolicyStore());
//		return policyReader.readMetaDataPolicyDTO(policyId);
//	}

	/**
	 * Removes policy for given policy object
	 * 
	 * @param policyId policy id
	 * @throws IdentityException throws
	 */
	public void removePolicy(String policyId) throws IdentityException {

		PAPPolicyStoreManager policyAdmin = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager();
        PolicyDTO oldPolicy = null;

        try{
            try{
                oldPolicy = getPolicy(policyId);
            } catch (Exception e){
                // exception is ignore. as unwanted details are throws
            }
            if(oldPolicy == null){
                oldPolicy = new PolicyDTO();
                oldPolicy.setPolicyId(policyId);
            }
            policyAdmin.removePolicy(oldPolicy);
        } catch (IdentityException e){
            handleNotifications("Delete Policy", policyId, null, false, e.getMessage());
            throw e;
        }
        handleNotifications("Delete Policy", policyId, oldPolicy.getPolicy(), true, null);
	}

	/**
	 * This method returns the list of policy id available in PDP
	 * 
	 * @return list of ids
	 * @throws IdentityException throws
	 */
	public String[] getAllPolicyIds() throws IdentityException {

        return EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().getPolicyIds();
	}


    /**
     * policy re-ordering
     * 
     * @param policyDTOs
     * @throws  IdentityException     // TODO remove
     */
//    public void reOderPolicies(PolicyDTO[] policyDTOs) throws IdentityException {
//
//        boolean success = true;
//
//        for(PolicyDTO dto : policyDTOs){
//            try {
//                updatePolicy(dto);  // TODO as transactions
//            } catch (IdentityException e) {
//                // ignore this error as this is reOrdering process
//                success = false;
//            }
//        }
//
//        if(success){
//            PolicyStoreManager manager = EntitlementEngine.getInstance().getPolicyStoreManager();
//            for(PolicyDTO dto : policyDTOs){
//                manager.promotePolicy(dto.getPolicyId());
//                updatePolicy(dto);
//            }
//        }
//        EntitlementEngine.getInstance().getPapPolicyFinder().init();
//    }


    /**
     * Gets subscriber details
     *
     * @param subscribeId subscriber id
     * @return subscriber details as SubscriberDTO
     * @throws IdentityException throws, if any error
     */
    public PublisherDataHolder getSubscriber(String subscribeId) throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        return publisher.retrieveSubscriber(subscribeId);
    }

    /**
     * Gets all subscribers ids that is registered,
     *
     * @return subscriber's ids as String array
     * @throws IdentityException throws, if fails
     */
    public String[] getSubscriberIds() throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        String[] ids = publisher.retrieveSubscriberIds();
        if(ids != null){
            return ids;
        } else {
            return new String[0];
        }
    }

    /**
     * Add subscriber details in to registry
     *
     * @param holder subscriber data as PublisherDataHolder object
     * @throws IdentityException throws, if fails
     */
    public void addSubscriber(PublisherDataHolder holder) throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        publisher.persistSubscriber(holder,false);

    }

    /**
     * Update subscriber details in registry
     *
     * @param holder subscriber data as PublisherDataHolder object
     * @throws IdentityException throws, if fails
     */
    public void updateSubscriber(PublisherDataHolder holder) throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        publisher.persistSubscriber(holder, true);

    }

    /**
     * delete subscriber details from registry
     *
     * @param subscriberId subscriber id
     * @throws IdentityException throws, if fails
     */
    public void deleteSubscriber(String subscriberId) throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        publisher.deleteSubscriber(subscriberId);

    }

    /**
     * Publishes given set of policies to all subscribers
     *
     * @param policyIds policy ids to publish,  if null or empty, all policies are published
     * @param subscriberIds subscriber ids to publish,  if null or empty, all policies are published
     * @throws IdentityException throws, if fails
     */
    public void publishPolicies(String[] policyIds, int version,
                                String action, String[] subscriberIds) throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        if(policyIds == null || policyIds.length < 1){
            policyIds = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager().getPolicyIds();
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

        publisher.publishPolicy(policyIds, version, action, subscriberIds, null);
    }

    /**
     * Publishes given set of policies to all subscribers
     *
     * @param verificationCode verification code that is received by administrator to publish
     * @throws IdentityException throws, if fails
     */
    public void publish(String verificationCode)  throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        publisher.publishPolicy(null, 0, null, null, verificationCode);
        
    }

    /**
     *
     * @param policyIds
     * @throws IdentityException
     */
    public void publishToPDP(String[] policyIds, int version, String action) throws IdentityException {

        PolicyPublisher publisher = EntitlementAdminEngine.getInstance().getPolicyPublisher();
        publisher.publishPolicy(policyIds, version, action, null, null);
    }

    /**
     *
     * @param policyId
     * @param version
     */
    public void rollBackPolicy(String policyId, int version) throws IdentityException {

        PolicyVersionManager versionManager = EntitlementAdminEngine.getInstance().getVersionManager();
        PolicyDTO  policyDTO = versionManager.getPolicy(policyId, version);
        addOrUpdatePolicy(policyDTO, false);

    }

    /**
     *
     * @param policyId
     * @return
     */
    public StatusHolder[] getPolicyPolishData(String policyId){
        EntitlementAdminEngine.getInstance().getPolicyPublisher();
        return new StatusHolder[0];
    }


    /**
     * Gets policy publisher module data to populate in the UI
     *
     * @return
     */
    public PublisherDataHolder[]  getPublisherModuleData(){

        List<PublisherDataHolder> holders = EntitlementServiceComponent.
            getEntitlementConfig().getModulePropertyHolders(PolicyPublisherModule.class.getName());
        if(holders != null){
            return holders.toArray(new PublisherDataHolder[holders.size()]);
        }

        return null;
    }



    /**
     *
     * @param dataModule
     * @param category
     * @param regexp
     * @param dataLevel
     * @param limit
     * @return
     */
    public EntitlementTreeNodeDTO getEntitlementData(String dataModule, String category,
                                                        String regexp, int dataLevel, int limit){

        EntitlementDataFinder dataFinder = EntitlementAdminEngine.getInstance().getEntitlementDataFinder();
        return dataFinder.getEntitlementData(dataModule, category, regexp, dataLevel, limit);
    }

    /**
     *
     * @return
     */
    public EntitlementFinderDataHolder[] getEntitlementDataModules(){

        EntitlementDataFinder dataFinder = EntitlementAdminEngine.getInstance().getEntitlementDataFinder();
        return dataFinder.getEntitlementDataModules();
    }


    /**
     * This method persists a XACML policy
     *
     * @param policyDTO PolicyDTO object
     * @param isAdd  whether this is policy adding or updating
     * @throws IdentityException throws if invalid policy or if policy
     *             with same id is exist
     */
    private void addOrUpdatePolicy(PolicyDTO policyDTO, boolean isAdd)  throws IdentityException{

        PAPPolicyStoreManager policyAdmin = EntitlementAdminEngine.getInstance().getPapPolicyStoreManager();
        PolicyVersionManager versionManager = EntitlementAdminEngine.getInstance().getVersionManager();

        AbstractPolicy policyObj;
        String policyId = null;
        String policy = null;
        String operation = "Update Policy";
        if(isAdd){
            operation = "Add Policy";
        }

        try{
            if(policyDTO == null ){
                throw new IdentityException("Entitlement Policy can not be null.");
            }

            if(isAdd && policyDTO.getPolicy() == null){
                throw new IdentityException("Entitlement Policy can not be null.");
            }

            policy = policyDTO.getPolicy();
            if(policy != null){
                policyDTO.setPolicy(policy.replaceAll(">\\s+<", "><"));
                if(!EntitlementUtil.validatePolicy(policyDTO)){
                    throw new IdentityException("Invalid Entitlement Policy. " +
                            "Policy is not valid according to XACML schema");
                }
                policyObj = PolicyReader.getInstance(null).getPolicy(policy);
                if (policyObj != null) {
                    policyId = policyObj.getId().toASCIIString();
                    policyDTO.setPolicyId(policyId);
                    // All the policies wont be active at the time been added.
                    policyDTO.setActive(policyDTO.isActive());

                    if(isAdd){
                        try{
                            if (getPolicy(policyId) != null) {
                                throw new IdentityException(
                                        "An Entitlement Policy with the given Id already exists");
                            }
                        } catch (Exception e) {
                            // exception is ignore. as unwanted details are throws
                        }
                    }
                } else {
                    throw new IdentityException("Unsupported Entitlement Policy. Policy can not be parsed");
                }
                try{
                    int version = versionManager.createVersion(policyDTO);
                    policyDTO.setVersion(version);
                } catch (IdentityException e){
                    log.error("Policy versioning is not supported", e);
                }

            } else {
                policy = policyDTO.getPolicyId();
            }
            policyAdmin.addOrUpdatePolicy(policyDTO);
        } catch (IdentityException e){
            handleNotifications(operation, policyId, policy, false, e.getMessage());
            throw e;
        }

        handleNotifications(operation, policyId, policy, true, null);

        if(policyDTO.isPromote()){
            if(isAdd){
                publishToPDP(new String[] {policyDTO.getPolicyId()}, 0,
                        EntitlementConstants.PolicyPublish.ACTION_CREATE);
            } else {
                publishToPDP(new String[] {policyDTO.getPolicyId()}, 0,
                        EntitlementConstants.PolicyPublish.ACTION_UPDATE);
            }
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
    
    
    private void handleNotifications(String action, String id, String policy, 
                                                                boolean success, String message){

        Set<EntitlementNotificationHandler> handlers = EntitlementServiceComponent.
                                        getEntitlementConfig().getNotificationHandlers().keySet();
        
        Map<String, String> data = new HashMap<String, String>();
        if(id != null){
            data.put("policyId", id);
        }
        if(policy != null){
            data.put("policy", policy);
        }

        if(handlers != null){
           for(EntitlementNotificationHandler handler : handlers){
               handler.handle(action, data, success, message);
           }
        }
    }
}