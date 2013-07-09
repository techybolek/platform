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
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
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
    Set<PostPublisherModule> postPublisherModules = new HashSet<PostPublisherModule>();

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
            this.publisherModules = publisherModules.keySet();
        }

        Map<PostPublisherModule, Properties> postPublisherModules = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyPostPublisherModules();
        if(postPublisherModules != null && !postPublisherModules.isEmpty()){
            this.postPublisherModules = postPublisherModules.keySet();
        }

        Map<PublisherVerificationModule, Properties> prePublisherModules = EntitlementServiceComponent.
                getEntitlementConfig().getPublisherVerificationModule();
        if(prePublisherModules != null && !prePublisherModules.isEmpty()){
            this.verificationModule = prePublisherModules.keySet().iterator().next();
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
     * @throws IdentityException throws if can not be created PolicyPublishExecutor instant
     */
    public void publishPolicy(String[] policyIds, int version, String action, String[] subscriberIds,
                                            String verificationCode) throws IdentityException {

        boolean toPDP = false;
        
        if(subscriberIds == null){
            toPDP = true;
        }
        
        PolicyPublishExecutor executor = new PolicyPublishExecutor(policyIds, version, action,
                        subscriberIds, this, toPDP, verificationCode);
        threadPool.execute(executor);
    }


    public void persistSubscriber(PublisherDataHolder holder, boolean update) throws IdentityException {

        Collection policyCollection;
        String subscriberPath;
        String subscriberId = null;

        if(holder == null || holder.getPropertyDTOs() == null){
            return;
        }

        for(PublisherPropertyDTO dto  : holder.getPropertyDTOs()){
            if(SUBSCRIBER_ID.equals(dto.getId())){
                subscriberId = dto.getValue();
            }
        }

        if(subscriberId == null){
            return;
        }

        try{
            if(registry.resourceExists(EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER)){
                policyCollection = registry.newCollection();
                registry.put(EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER, policyCollection);
            }

            subscriberPath =  EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER +
                        RegistryConstants.PATH_SEPARATOR + subscriberId;

            Resource resource;

            if(registry.resourceExists(subscriberPath)){
                if(update){
                    resource = registry.get(subscriberPath);
                } else {
                    throw new IdentityException("Subscriber ID already exists");
                }
            } else {
                resource = registry.newResource();
            }

            populateProperties(holder, resource);
            registry.put(subscriberPath, resource);
            
        } catch (RegistryException e) {
            log.error("Error while persisting subscriber details", e);
            throw new IdentityException("Error while persisting subscriber details", e);
        }
    }


    public void deleteSubscriber(String subscriberId) throws IdentityException {

        String subscriberPath;
        
        if(subscriberId == null){
            return;
        }

        try{
            subscriberPath =  EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER +
                        RegistryConstants.PATH_SEPARATOR + subscriberId;

            if(registry.resourceExists(subscriberPath)){
                registry.delete(subscriberPath);
            }
        } catch (RegistryException e) {
            log.error("Error while deleting subscriber details", e);
            throw new IdentityException("Error while deleting subscriber details", e);
        }
    }

    public PublisherDataHolder retrieveSubscriber(String id) throws IdentityException {

        try{
            if(registry.resourceExists(EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                        RegistryConstants.PATH_SEPARATOR + id)){
                Resource resource = registry.get(EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                        RegistryConstants.PATH_SEPARATOR + id);

                return new PublisherDataHolder(resource);
            }
        } catch (RegistryException e) {
            log.error("Error while retrieving subscriber detail of id : " + id, e);
            throw new IdentityException("Error while retrieving subscriber detail of id : " + id, e);
        }

        return null;
    }

    public String[] retrieveSubscriberIds() throws IdentityException {

        try{
            if(registry.resourceExists(EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER +
                                                        RegistryConstants.PATH_SEPARATOR)){
                Resource resource = registry.get(EntitlementConstants.ENTITLEMENT_POLICY_PUBLISHER +
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
            throw new IdentityException("Error while retrieving subscriber ids", e);

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
        int num = 0;
        if(statusHolders != null){
            for(StatusHolder statusHolder : statusHolders){
                List<String>  list = new ArrayList<String>();
                list.add(statusHolder.getTimeInstance());
                list.add(statusHolder.getUser());
                list.add(statusHolder.getKey());
                if(statusHolder.getMessage() != null){
                    list.add(statusHolder.getMessage());
                }
                resource.setProperty(StatusHolder.STATUS_HOLDER_NAME + num, list);
                num ++;
            }
        }

        resource.setProperty(PublisherDataHolder.MODULE_NAME, holder.getModuleName());
    }

    public Set<PolicyPublisherModule> getPublisherModules() {
        return publisherModules;
    }

    public Set<PostPublisherModule> getPostPublisherModules() {
        return postPublisherModules;
    }

    public PublisherVerificationModule getVerificationModule() {
        return verificationModule;
    }
}
