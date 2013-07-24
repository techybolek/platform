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

package org.wso2.carbon.identity.entitlement.policy.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is policy publisher. There can be different modules that have been plugged with this.
 * This module currently is bound with the WSO2 registry, as some meta data is store there,
 */
public class PolicyPublisher{

    private Registry registry;

    public static final String SUBSCRIBER_ID = "subscriberId";

    public static final String SUBSCRIBER_DISPLAY_NAME = "Subscriber Id";

    private static Log log = LogFactory.getLog(PolicyPublisher.class);

    /**
     * set of publisher modules
     */
    Set<PolicyPublisherModule> publisherModules = new HashSet<PolicyPublisherModule>();

    /**
     * set of post publisher modules
     */
    Set<PAPStatusDataHandler>  papStatusDataHandlers = new HashSet<PAPStatusDataHandler>();

    /**
     * Verification publisher modules
     */
    PublisherVerificationModule verificationModule = null;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(2);

    /**
     * Creates PolicyPublisher instance
     */
    public PolicyPublisher() {
        
        this.registry = EntitlementServiceComponent.
                            getGovernanceRegistry(CarbonContext.getCurrentContext().getTenantId());
        Map<PolicyPublisherModule, Properties> publisherModules = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyPublisherModules();
        if(publisherModules != null && !publisherModules.isEmpty()){
            this.publisherModules.addAll(publisherModules.keySet());
        }

        Map<PublisherVerificationModule, Properties> prePublisherModules = EntitlementServiceComponent.
                getEntitlementConfig().getPublisherVerificationModule();
        if(prePublisherModules != null && !prePublisherModules.isEmpty()){
            this.verificationModule = prePublisherModules.keySet().iterator().next();
        }

        //  creating default subscriber to publish policies to PDP
        CarbonPDPPublisher publisher = new CarbonPDPPublisher();
        this.publisherModules.add(publisher);

        PublisherDataHolder holder = new PublisherDataHolder(publisher.getModuleName());
        PublisherPropertyDTO dto = new PublisherPropertyDTO();
        dto.setId(SUBSCRIBER_ID);
        dto.setDisplayName(SUBSCRIBER_DISPLAY_NAME);
        dto.setValue(EntitlementConstants.PDP_SUBSCRIBER_ID);
        holder.setPropertyDTOs(new PublisherPropertyDTO[]{dto});
        try {
            PublisherDataHolder pdpDataHolder = null;
            try{
                pdpDataHolder = retrieveSubscriber(EntitlementConstants.PDP_SUBSCRIBER_ID);
            }catch (Exception e){
                // ignore
            }
            if(pdpDataHolder == null){
                persistSubscriber(holder, false);
            }
        } catch (EntitlementException e) {
            // ignore
        }
    }

    /**
     * publish policy
     *
     *
     * @param policyIds policy ids to publish,
     * @param version
     * @param action
     * @param subscriberIds subscriber ids to publish,
     * @param verificationCode verificationCode as String
     * @throws EntitlementException throws if can not be created PolicyPublishExecutor instant
     */
    public void publishPolicy(String[] policyIds, String version, String action, String[] subscriberIds,
                                            String verificationCode) throws EntitlementException {

        boolean toPDP = false;
        
        if(subscriberIds == null){
            toPDP = true;
        }
        
        PolicyPublishExecutor executor = new PolicyPublishExecutor(policyIds, version, action,
                        subscriberIds, this, toPDP, verificationCode);
        executor.setTenantDomain(CarbonContext.getCurrentContext().getTenantDomain());
        executor.setTenantId(CarbonContext.getCurrentContext().getTenantId());
        executor.setUserName(CarbonContext.getCurrentContext().getUsername());

        threadPool.execute(executor);
    }


    public void persistSubscriber(PublisherDataHolder holder, boolean update) throws EntitlementException {

        Collection policyCollection;
        String subscriberPath;
        String subscriberId = null;

        if(holder == null || holder.getPropertyDTOs() == null){
            log.error("Publisher data can not be null");
            throw new EntitlementException("Publisher data can not be null");
        }

        for(PublisherPropertyDTO dto  : holder.getPropertyDTOs()){
            if(SUBSCRIBER_ID.equals(dto.getId())){
                subscriberId = dto.getValue();
            }
        }

        if(subscriberId == null){
            log.error("Subscriber Id can not be null");
            throw new EntitlementException("Subscriber Id can not be null");
        }

        try{
            if(registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER)){
                policyCollection = registry.newCollection();
                registry.put(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER, policyCollection);
            }

            subscriberPath =  PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                        RegistryConstants.PATH_SEPARATOR + subscriberId;

            Resource resource;

            if(registry.resourceExists(subscriberPath)){
                if(update){
                    resource = registry.get(subscriberPath);
                } else {
                    throw new EntitlementException("Subscriber ID already exists");
                }
            } else {
                resource = registry.newResource();
            }

            populateProperties(holder, resource);
            registry.put(subscriberPath, resource);
            
        } catch (RegistryException e) {
            log.error("Error while persisting subscriber details", e);
            throw new EntitlementException("Error while persisting subscriber details", e);
        }
    }


    public void deleteSubscriber(String subscriberId) throws EntitlementException {

        String subscriberPath;
        
        if(subscriberId == null){
            log.error("Subscriber Id can not be null");
            throw new EntitlementException("Subscriber Id can not be null");
        }

        if(EntitlementConstants.PDP_SUBSCRIBER_ID.equals(subscriberId.trim())){
            log.error("Can not delete PDP publisher");
            throw new EntitlementException("Can not delete PDP publisher");
        }

        try{
            subscriberPath =  PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                    RegistryConstants.PATH_SEPARATOR + subscriberId;

            if(registry.resourceExists(subscriberPath)){
                registry.delete(subscriberPath);
            }
        } catch (RegistryException e) {
            log.error("Error while deleting subscriber details", e);
            throw new EntitlementException("Error while deleting subscriber details", e);
        }
    }

    public PublisherDataHolder retrieveSubscriber(String id) throws EntitlementException {

        try{
            if(registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                        RegistryConstants.PATH_SEPARATOR + id)){
                Resource resource = registry.get(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                        RegistryConstants.PATH_SEPARATOR + id);

                return new PublisherDataHolder(resource);
            }
        } catch (RegistryException e) {
            log.error("Error while retrieving subscriber detail of id : " + id, e);
            throw new EntitlementException("Error while retrieving subscriber detail of id : " + id, e);
        }

        throw new EntitlementException("No Subscriber is defined for given Id");
    }

    public String[] retrieveSubscriberIds() throws EntitlementException {

        try{
            if(registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                        RegistryConstants.PATH_SEPARATOR)){
                Resource resource = registry.get(PDPConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                        RegistryConstants.PATH_SEPARATOR);
                Collection collection = (Collection) resource;
                List<String> list = new ArrayList<String>();
                if(collection.getChildCount() > 0){
                    for(String path : collection.getChildren()){
                        String id = registry.get(path).getProperty(SUBSCRIBER_ID);
                        if(id != null){
                            list.add(id);
                        }
                    }
                }
                return list.toArray(new String[list.size()]);
            }
        } catch (RegistryException e) {
            log.error("Error while retrieving subscriber of ids" , e);
            throw new EntitlementException("Error while retrieving subscriber ids", e);

        }

        return null;
    }

    private void populateProperties(PublisherDataHolder holder, Resource resource){

        PublisherPropertyDTO[] propertyDTOs = holder.getPropertyDTOs();
        for(PublisherPropertyDTO dto : propertyDTOs){
            if(dto.getId() != null && dto.getValue() != null) {
                ArrayList<String> list = new ArrayList<String>();
                //password must be encrypted TODO
                if(dto.isSecret()){

                }
                list.add(dto.getValue());
                list.add(dto.getDisplayName());
                list.add(Integer.toString(dto.getDisplayOrder()));
                list.add(Boolean.toString(dto.isRequired()));
                list.add(Boolean.toString(dto.isSecret()));
                resource.setProperty(dto.getId(), list);
            }
        }

        StatusHolder[] statusHolders = holder.getStatusHolders();
        if(statusHolders != null){
            for(StatusHolder statusHolder : statusHolders){
                if(statusHolder != null){
                    List<String>  list = new ArrayList<String>();
                    list.add(statusHolder.getType());
                    list.add(statusHolder.getTimeInstance());
                    list.add(statusHolder.getUser());
                    list.add(statusHolder.getKey());
                    list.add(Boolean.toString(statusHolder.isSuccess()));
                    if(statusHolder.getMessage() != null){
                        list.add(statusHolder.getMessage());
                    } else {
                        list.add("");
                    }
                    if(statusHolder.getTarget() != null){
                        list.add(statusHolder.getTarget());
                    } else {
                        list.add("");
                    }
                    if(statusHolder.getTargetAction() != null){
                        list.add(statusHolder.getTargetAction());
                    } else {
                        list.add("");
                    }
                    if(statusHolder.getVersion() != null){
                        list.add(statusHolder.getVersion());
                    } else {
                        list.add("");
                    }
                    resource.setProperty(StatusHolder.STATUS_HOLDER_NAME +  UUID.randomUUID(), list);
                }
            }
        }

        resource.setProperty(PublisherDataHolder.MODULE_NAME, holder.getModuleName());
    }

    public Set<PolicyPublisherModule> getPublisherModules() {
        return publisherModules;
    }

    public Set<PAPStatusDataHandler> getPapStatusDataHandlers() {
        return papStatusDataHandlers;
    }

    public void setPapStatusDataHandlers(Set<PAPStatusDataHandler> papStatusDataHandlers) {
        this.papStatusDataHandlers = papStatusDataHandlers;
    }

    public PublisherVerificationModule getVerificationModule() {
        return verificationModule;
    }
}
