/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.apiManager.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.appfactory.apiManager.integration.dao.ApiMgtDAO;
import org.wso2.carbon.appfactory.apiManager.integration.internal.ServiceHolder;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.APIIntegration;
import org.wso2.carbon.appfactory.core.dto.API;
import org.wso2.carbon.appfactory.core.dto.APIMetadata;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.appfactory.apiManager.integration.utils.Constants.*;
import static org.wso2.carbon.appfactory.apiManager.integration.utils.Utils.*;

public class APIManagerIntegrationService extends AbstractAdmin implements APIIntegration {
		
    private static final Log log = LogFactory.getLog(APIManagerIntegrationService.class);

    public void loginToStore(HttpClient httpClient) throws AppFactoryException {
		login(STORE_LOGIN_ENDPOINT, httpClient);
	}

	public void loginToPublisher(HttpClient httpClient) throws AppFactoryException {
		login(PUBLISHER_LOGIN_ENDPOINT, httpClient);
	}

	/**
	 * Method to login API Manager. Login is done using given {@code httpClient} so it gets authenticated.
	 * Use the same client for further processing.
	 * Otherwise Authentication failure will occur. 
	 * @param endpoint
	 * @param httpClient
	 * @throws AppFactoryException
	 */
	private void login(String endpoint, HttpClient httpClient) throws AppFactoryException {
		HttpServletRequest request =
		                             (HttpServletRequest) MessageContext.getCurrentMessageContext()
		                                                                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String samlToken = request.getHeader(SAML_TOKEN);

		// We expect an encoded saml token.
		if (samlToken == null || samlToken.equals("")) {
			String msg = "Unable to get the SAML token";
			log.error(msg);
			throw new AppFactoryException(msg);
		}

		URL apiManagerUrl = getApiManagerURL();

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();

		parameters.add(new BasicNameValuePair(ACTION, "loginWithSAMLToken"));
		parameters.add(new BasicNameValuePair("samlToken", samlToken));

		HttpPost postMethod = createHttpPostRequest(apiManagerUrl, parameters, endpoint);
		HttpResponse response = executeHttpMethod(httpClient, postMethod);

		try {
			EntityUtils.consume(response.getEntity());
		} catch (IOException e) {
			String msg = "Failed to consume http response";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
	}


	public boolean createApplication(String applicationId) throws AppFactoryException {

		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient);

			if (!isApplicationNameInUse(applicationId)) {
				URL apiManagerUrl = getApiManagerURL();

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();

				parameters.add(new BasicNameValuePair(ACTION, "addApplication"));
				parameters.add(new BasicNameValuePair("application", applicationId));
				parameters.add(new BasicNameValuePair("tier", getDefaultTier()));
				parameters.add(new BasicNameValuePair("callbackUrl", getDefaultCallbackURL()));

				HttpPost postMethod =
				                      createHttpPostRequest(apiManagerUrl, parameters,
				                                            CREATE_APPLICATION_ENDPOINT);
				HttpResponse response = executeHttpMethod(httpClient, postMethod);

				if (response != null) {
					try {
						EntityUtils.consume(response.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
			}
			return true;
		} finally {
			httpClient.getConnectionManager().shutdown();
		   
			try {
	   			//Remove DefaultApplication from API Manager
	   			new APIManagerIntegrationService().removeApplication("DefaultApplication");
	   		} catch (AppFactoryException e) {
	   			log.error("Error while deleteing 'DefaultApplication' from API Manager");
	   		}
		}
	}


	public boolean isApplicationNameInUse(String applicationId) throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient);

			URL apiManagerUrl = getApiManagerURL();

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "getApplications"));
			parameters.add(new BasicNameValuePair(USERNAME, CarbonContext.getCurrentContext()
			                                                             .getUsername()));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            LIST_APPLICATION_ENDPOINT);

			HttpResponse httpResponse = executeHttpMethod(httpClient, postMethod);
			if (httpResponse != null) {
				try {

					HttpEntity responseEntity = httpResponse.getEntity();
					String responseBody = EntityUtils.toString(responseEntity);

					JsonObject response = getJsonObject(responseBody);
					JsonArray applications = response.getAsJsonArray("applications");

					if (applications != null) {
						for (JsonElement application : applications) {
							String applicationName =
							                         ((JsonObject) application).get(NAME)
							                                                   .getAsString();
							if (applicationName.equals(applicationId)) {
								return true;
							}
						}
					}
				} catch (IOException e) {
					String msg = "Error reading the json response";
					log.error(msg, e);
					throw new AppFactoryException(msg, e);
				} finally {
					try {
						EntityUtils.consume(httpResponse.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
			}
			return false;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public boolean removeApplication(String applicationId) throws AppFactoryException {

		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient);
			URL apiManagerUrl = getApiManagerURL();
		
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "removeApplication"));
			parameters
					.add(new BasicNameValuePair("application", applicationId));
			parameters.add(new BasicNameValuePair("tier", getDefaultTier()));
		
			HttpPost postMethod = createHttpPostRequest(apiManagerUrl,
					parameters, DELETE_APPLICATION_ENDPOINT);
			HttpResponse response = executeHttpMethod(httpClient, postMethod);
		
			if (response != null) {
		
			}
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return true;
	}


    public boolean addAPIsToApplication(String s, String s1, String s2, String s3) throws AppFactoryException {
//        returning false since we do not support this for the moment.
        return false;
    }

   /**
    * Get the basic information of APIs belong to an AppOwner for a given appkey
    * @param userMailId appOwner email address
    * @param appKey application key
    * @return an Array or API objects
    * @throws AppFactoryException
    */
    public API[] getAPIsOfUserApp(String userMailId, String appKey) throws AppFactoryException{
    	ApiMgtDAO dao = ApiMgtDAO.getInstance();
    	List<API> apis = dao.getBasicAPIInfo(userMailId, appKey);
    	 return apis.toArray(new API[apis.size()]);
    }
    
	public API[] getAPIsOfApplication(String applicationId) throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient);

			URL apiManagerUrl = getApiManagerURL();

			HttpServletRequest request =
			                             (HttpServletRequest) MessageContext.getCurrentMessageContext()
			                                                                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
			String username = request.getHeader(USERNAME);

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "getAllSubscriptions"));
			parameters.add(new BasicNameValuePair(USERNAME, username));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            LIST_SUBSCRIPTIONS_ENDPOINT);
			HttpResponse httpResponse = executeHttpMethod(httpClient, postMethod);
			if (httpResponse != null) {
				// Reading the response json
				List<API> apiNames = new ArrayList<API>();
				try {
					HttpEntity responseEntity = httpResponse.getEntity();
					String responseBody = EntityUtils.toString(responseEntity);

					JsonObject response = getJsonObject(responseBody);
					JsonArray subscriptions = response.getAsJsonArray(SUBSCRIPTIONS);

					if (subscriptions != null) {
						for (JsonElement subscription : subscriptions) {
							String applicationName =
							                         ((JsonObject) subscription).get(NAME)
							                                                    .getAsString();
							if (applicationName.equals(applicationId)) {
								JsonArray applicationSubscriptions =
								                                     ((JsonObject) subscription).getAsJsonArray(SUBSCRIPTIONS);
								for (JsonElement applicationSubscription : applicationSubscriptions) {
									API apiInfo =
									              populateAPIInfo((JsonObject) applicationSubscription);

									apiNames.add(apiInfo);
								}
								break;
							}
						}
					}

				} catch (IOException e) {
					String msg = "Error reading the json response";
					log.error(msg, e);
					throw new AppFactoryException(msg, e);
				} finally {
					try {
						EntityUtils.consume(httpResponse.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
					}
				}
				return apiNames.toArray(new API[apiNames.size()]);
			}
			return new API[0];
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}


	public API getAPIInformation(String apiName, String apiVersion, String apiProvider)
	                                                                                   throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToPublisher(httpClient);

			URL apiManagerUrl = getApiManagerURL();

			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(ACTION, "getAPI"));
			parameters.add(new BasicNameValuePair(NAME, apiName));
			parameters.add(new BasicNameValuePair(VERSION, apiVersion));
			parameters.add(new BasicNameValuePair(PROVIDER, apiProvider));

			HttpPost postMethod =
			                      createHttpPostRequest(apiManagerUrl, parameters,
			                                            PUBLISHER_API_INFO_ENDPOINT);

			HttpResponse httpResponse = executeHttpMethod(httpClient, postMethod);
			if (httpResponse != null) {
				try {

					HttpEntity responseEntity = httpResponse.getEntity();
					String responseBody = EntityUtils.toString(responseEntity);

					JsonObject response = getJsonObject(responseBody);
					JsonObject apiElement = response.getAsJsonObject("api");

					return populateAPIInfo(apiElement);
				} catch (IOException e) {
					String msg = "Error reading the json response";
					log.error(msg, e);
					throw new AppFactoryException(msg, e);
				} finally {
					try {
						EntityUtils.consume(httpResponse.getEntity());
					} catch (IOException e) {
						String msg = "Failed to consume http response";
						log.error(msg, e);
						throw new AppFactoryException(msg, e);
					}
				}
			}
			return new API();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public boolean generateKeys(String appId, String apiName, String apiVersion, String apiProvider)
	                                                                                                throws AppFactoryException {
		HttpClient httpClient = new DefaultHttpClient();
		try {
			loginToStore(httpClient);

			URL apiManagerUrl = getApiManagerURL();

			generateKey(appId, apiManagerUrl, "SANDBOX", httpClient);
			generateKey(appId, apiManagerUrl, "PRODUCTION", httpClient);

			return true;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}


    public boolean removeAPIFromApplication(String s, String s1, String s2, String s3) throws AppFactoryException {
//        returning false since we do not support this for the moment.
        return false;
    }
    
    /**
     * Retrieves the saved keys in registry
     * @param applicationId appKey
     * @return array of APIMetadata objects
     * @throws AppFactoryException
     */
	public APIMetadata[] getKeysFromRegistry(String applicationId) throws AppFactoryException{
		
		Map<String,List<String[]>> mapping = new HashMap<String, List<String[]>>();
		List<String[]> sandbox = new ArrayList<String[]>(3);
		String[] s_key = {"key","sandboxKey"};
		sandbox.add(s_key);
		String[] s_consumerKey = {"consumerkey","sandboxConsumerKey"};
		sandbox.add(s_consumerKey);
		String[] s_consumerSecret = {"consumersecret","sandboxConsumerSecret"};
		sandbox.add(s_consumerSecret);
		mapping.put("Development", sandbox);
		
		List<String[]> prod = new ArrayList<String[]>(3);
		String[] p_key = {"key","prodKey"};
		prod.add(p_key);
		String[] p_consumerKey = {"consumerkey","prodConsumerKey"};
		prod.add(p_consumerKey);
		String[] p_consumerSecret = {"consumersecret","prodConsumerSecret"};
		prod.add(p_consumerSecret);
		mapping.put("Production", prod);
	        
	        Registry tenantRegistry = getTenantRegistryObj(applicationId);
	        List<APIMetadata> keyList = new ArrayList<APIMetadata>();
	
	        for (Map.Entry<String,List<String[]>> keyMapping : mapping.entrySet()) {
	        	String state = keyMapping.getKey();
	        	List<String[]> pairs = keyMapping.getValue();
	        	for (Iterator<String[]> iterator = pairs.iterator(); iterator.hasNext();) {
					String[] vals = (String[]) iterator.next();
					String value = null;
					try {
						value = readFromRegistry(tenantRegistry,state, vals[0]);
					} catch (Exception e) {
						log.error("Error while reading from the Tenant Registry");
					}
					keyList.add(new APIMetadata(vals[1], value, vals[0], state, false));
				}
	        }
	        return keyList.toArray(new APIMetadata[keyList.size()]);
	}
    
    public APIMetadata[] getSavedKeys(String applicationId, String isSyncStr) throws AppFactoryException{
        
        Boolean isSyncNeeded = Boolean.valueOf(isSyncStr);
        Registry tenantRegistry = getTenantRegistryObj(applicationId);

        API[] api = getAPIsOfApplication(applicationId);
        List<APIMetadata> keyList = new ArrayList<APIMetadata>();

        if(api != null && api.length > 0){
//            Because API Manager has keys per application, not for api
            API singleApi = api[0];
            APIMetadata[] keys = singleApi.getKeys();

            if(keys != null){
            	Map<String,String[]> keyMap = getKeyEnvironmentMapping();
            	for (Map.Entry<String, String[]> keyMapping : keyMap.entrySet()) {
            		for (APIMetadata key : keys) {
            			if(key.getType().toLowerCase().startsWith(keyMapping.getKey().toLowerCase())){  
            				String name = "";
            				name = key.getType().toLowerCase().replace(keyMapping.getKey().toLowerCase(), "");
            				String[] states = keyMapping.getValue();
            				for (String state : states) {
            					if(isSyncNeeded){
            						try {
            							writeToRegistry(tenantRegistry,state, name, key.getValue());
            						} catch (Exception e) {
            							log.error("Error while writing to the Tenant Registry");
            						}
            					} 
            					String value = "";
            					try {
            						value = readFromRegistry(tenantRegistry,state, name);
            					} catch (Exception e) {
            						log.error("Error while reading from the Tenant Registry");
            					}
            					Boolean isSynced = key.getValue().equals(value);
            					keyList.add(new APIMetadata(key.getType(),value,name, state, isSynced));
            				}
            			}
            		}

                }

            }
        }
        return keyList.toArray(new APIMetadata[keyList.size()]);
    }

    /**
     * Retrieves the registry object
     * @param applicationId appKey
     * @return Registry object
     * @throws AppFactoryException
     */
	private Registry getTenantRegistryObj(String applicationId)
			throws AppFactoryException {
		Registry tenantRegistry;
        RegistryService registryService = ServiceHolder.getInstance().getRegistryService();
        RealmService realmService = ServiceHolder.getInstance().getRealmService();
        TenantRegistryLoader tenantRegistryLoader = ServiceHolder.getInstance().getTenantRegistryLoader();

        if(registryService == null){
            String msg = "Unable to find the registry service";
            log.error(msg);
            throw new AppFactoryException(msg);
        }
        if(realmService == null){
            String msg = "Unable to find the realm service";
            log.error(msg);
            throw new AppFactoryException(msg);
        }
        if(tenantRegistryLoader == null){
            String msg = "Unable to find the tenant registry loader service";
            log.error(msg);
            throw new AppFactoryException(msg);
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(applicationId);
            if(tenantId == MultitenantConstants.INVALID_TENANT_ID){
                String msg = "Invalid tenant Id returned";
                log.error(msg);
                throw new AppFactoryException(msg);
            }

            tenantRegistryLoader.loadTenantRegistry(tenantId);
            tenantRegistry = registryService.getGovernanceSystemRegistry(tenantId);
        } catch (UserStoreException e) {
            String msg = "Unable to get the tenant id ";
            log.error(msg,e);
            throw new AppFactoryException(msg,e);
        } catch (RegistryException e) {
            String msg = "Unable to get the registry";
            log.error(msg,e);
            throw new AppFactoryException(msg,e);
        }
		return tenantRegistry;
	}
}
