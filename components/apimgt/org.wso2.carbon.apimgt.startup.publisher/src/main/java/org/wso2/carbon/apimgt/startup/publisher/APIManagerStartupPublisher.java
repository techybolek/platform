/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.startup.publisher;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.cache.Cache;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.core.APIManagerConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.startup.publisher.internal.DataHolder;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;

public class APIManagerStartupPublisher implements ServerStartupHandler{
	private static final Log log = LogFactory.getLog(APIManagerStartupPublisher.class);
	Cache contextCache = APIUtil.getAPIContextCache();
	APIProvider provider;
	protected Registry registry;
	
	@Override
	public void invoke() {
		if (log.isDebugEnabled()) {
			log.info("Startup Publisher Invoked");
		}
		
		String apiManagementEnabled = CarbonUtils.getServerConfiguration().getFirstProperty(APIManagerConstants.API_MANGEMENT_ENABLED);
        String externalAPIManagerGatewayURL = CarbonUtils.getServerConfiguration().getFirstProperty(APIManagerConstants.EXTERNAL_API_GATEWAY);
		
        if (apiManagementEnabled.equalsIgnoreCase("true")) {
	        APIManagerConfiguration configuration =
		                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
	        	
	        	List<String> apiContexts = configuration.getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_CONTEXT);
	        	List<String> apiProviders = configuration.getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_PROVIDER);
	        	List<String> apiVersions = configuration.getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_VERSION);
	        	List<String> apiEndpoints = configuration.getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_ENDPOINT);
				
	        	if (apiProviders == null || apiVersions == null || apiEndpoints == null || apiContexts == null) {
					if (log.isDebugEnabled()) {
						log.info("StartupAPIPublisher is not configured or invalid StartupAPIPublisher configuration");
					}
					return;
				}
	        	
				
				
				for (int i = 0; i <apiContexts.size(); i ++) {
					try {
						String apiContext = apiContexts.get(i);
						String apiProvider = apiProviders.get(i);
						String apiVersion = apiVersions.get(i);
						String apiEndpoint = apiEndpoints.get(i);
					
					
						String apiName = null;
						
						if (apiProvider == null || apiVersion == null || apiEndpoint == null || apiContext == null) {
							if (log.isDebugEnabled()) {
								log.info("StartupAPIPublisher is not configured or invalid StartupAPIPublisher configuration");
							}
							return;
						}
						
						/* API Context validations and initialize apiName to context without slash*/
						if (!apiContext.startsWith("/")) {
							apiName = apiContext;
							apiContext = "/" + apiContext;
						} else {
							apiName = apiContext.substring(1);
						}
						
						/* API Management Embedded mode */
						if (apiManagementEnabled.equalsIgnoreCase("true") && externalAPIManagerGatewayURL == null) {
							createAPIInEmbeddedMode(apiName, apiProvider, apiVersion, apiEndpoint, apiContext);
						} else {
							//publish api to external gateway
						}
					} catch (IndexOutOfBoundsException e) {
						log.error("Invalid StartupAPIPublisher configuration");
					}
				}
        }
			
	}
	
	private void createAPIInEmbeddedMode(String apiName, String apiProvider, String apiVersion, 
			String apiEndpoint, String apiContext) {
		/* Check whether API already published */
		if (contextCache.get(apiContext) != null || ApiMgtDAO.isContextExist(apiContext)) {
			if (log.isDebugEnabled()) {
				log.info("API Context " + apiContext+ " already exists");
			}
			log.info("API Context" + apiContext+ "already exists");
			return;
		} 
		
		try {
			API api = createAPIModel(apiName, apiProvider, apiVersion, apiEndpoint, apiContext);
			if (api != null) {
				addAPI(api);
			}
		} catch (APIManagementException e) {
			log.error(e);
		} catch (RegistryException e) {
			log.error(e);
		}
	}
	
	private API createAPIModel(String apiName, String apiProvider, String apiVersion, 
			String apiEndpoint, String apiContext) throws APIManagementException {
		API api = null;
		try {
			provider = APIManagerFactory.getInstance().getAPIProvider(apiProvider);
			APIIdentifier identifier = new APIIdentifier(apiProvider , apiName , apiVersion);
			api = new API(identifier);
			api.setContext(apiContext);
			api.setUrl(apiEndpoint);
			api.setUriTemplates(getURITemplates(apiEndpoint));
			api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
			api.addAvailableTiers(provider.getTiers());
			api.setEndpointSecured(false);
			api.setStatus(APIStatus.CREATED);
		} catch (APIManagementException e) {
			handleException("Error while initializing API Provider", e);
		}
			return api;
	}
	
	private void addAPI(API api) throws RegistryException, APIManagementException{
		ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
		try {  
			this.registry = DataHolder.getRegistryService().getGovernanceSystemRegistry();
			createAPIArtifact(api);
            apiMgtDAO.addAPI(api);
            if (APIUtil.isAPIManagementEnabled()) {
            	Boolean apiContext = null;
            	if (contextCache.get(api.getContext()) != null) {
            		apiContext = Boolean.parseBoolean(contextCache.get(api.getContext()).toString());
            	} 
            	if (apiContext == null) {
                    contextCache.put(api.getContext(), true);
                }
            }
        } catch (APIManagementException e) {          
          throw new APIManagementException("Error in adding API :"+api.getId().getApiName(),e);
        } catch (RegistryException e) {
        	throw e;
        }
	}
	
	/**
     * Create an Api
     *
     * @param api API
     * @throws APIManagementException if failed to create API
     */
    private void createAPIArtifact(API api) throws APIManagementException {
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                            APIConstants.API_KEY);
        try {
        	registry.beginTransaction();
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            GenericArtifact artifact = APIUtil.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = APIUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null && tagSet.size() > 0) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
           
            if (api.getUrl() != null && !"".equals(api.getUrl())){
                String path = APIUtil.createEndpoint(api.getUrl(), registry);
                if (path != null) {
                    registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                }
            }
            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().getStatus();
            saveAPIStatus(artifactPath, apiStatus);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, artifactPath);
            registry.commitTransaction();

             //Generate API Definition for Swagger 
            createUpdateAPIDefinition(api);

        } catch (RegistryException e) {
        	 try {
                 registry.rollbackTransaction();
             } catch (RegistryException re) {
                 handleException("Error while rolling back the transaction for API: " +
                                 api.getId().getApiName(), re);
             }
             handleException("Error while performing registry transaction operation", e);
        }
        
    }
    
    /**
     * Create API Definition in JSON and save in the registry
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to generate the content and save
     */
    private void createUpdateAPIDefinition(API api) throws APIManagementException {
    	APIIdentifier identifier = api.getId(); 
    	
    	try{
    		String jsonText = APIUtil.createSwaggerJSONContent(api);
    		
    		String resourcePath = APIUtil.getAPIDefinitionFilePath(identifier.getApiName(), identifier.getVersion()); 
    		
    		Resource resource = registry.newResource();
    		    		
    		resource.setContent(jsonText);
    		resource.setMediaType("application/json");
    		registry.put(resourcePath, resource);
    		
    		/*Set permissions to anonymous role */
    		APIUtil.setResourcePermissions(api.getId().getProviderName(), null, null, resourcePath);
    			    
    	} catch (RegistryException e) {
    		handleException("Error while adding API Definition for " + identifier.getApiName() + "-" + identifier.getVersion(), e);
		} catch (APIManagementException e) {
			handleException("Error while adding API Definition for " + identifier.getApiName() + "-" + identifier.getVersion(), e);
		}
    }
    
        
    /**
     * Persist API Status into a property of API Registry resource
     *
     * @param artifactId API artifact ID
     * @param apiStatus Current status of the API
     * @throws APIManagementException on error
     */
    private void saveAPIStatus(String artifactId, String apiStatus) throws APIManagementException{
        try{
            Resource resource = registry.get(artifactId);
            if (resource != null) {
                String propValue = resource.getProperty(APIConstants.API_STATUS);
                if (propValue == null) {
                    resource.addProperty(APIConstants.API_STATUS, apiStatus);
                } else {
                    resource.setProperty(APIConstants.API_STATUS, apiStatus);
                }
                registry.put(artifactId,resource);
            }
        }catch (RegistryException e) {
            handleException("Error while adding API", e);
        }
    }
		
	private Set<URITemplate> getURITemplates(String endpoint) {
		Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
		String[] httpVerbs = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
		for(int i=0 ; i < 5; i ++) {
			URITemplate template = new URITemplate();
			if (i != 4) {
				template.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
			} else {
				template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
			}
			template.setHTTPVerb(httpVerbs[i]);
			template.setResourceURI(endpoint);
			template.setUriTemplate("/*");
			uriTemplates.add(template);
		}
		return uriTemplates;
	}
	
	private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }

}
