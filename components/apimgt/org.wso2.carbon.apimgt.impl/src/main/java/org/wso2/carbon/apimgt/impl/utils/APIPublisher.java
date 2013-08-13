/*
*  Copyright WSO2 Inc.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/


package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is to support create/update/publish APIs to external APIStores
 */
public class APIPublisher {
    private Log log = LogFactory.getLog(getClass());

    /**
     * The method to publish API to external WSO2 Store
     * @param api      API
     * @param store    Store
     * @return   published/not
     */

    public boolean publishToWSO2Store(API api,APIStore store) {
        boolean published = false;

        if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APIStore endpoint URL or credentials are not defined.Cannot proceed with publishing API to the APIStore - "+store.getDisplayName();
            log.error(msg);
        }

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        if(authenticateAPIM(store,httpContext)){  //First try to login to store
        boolean added = addAPIToStore(api,store.getEndpoint(),httpContext);
        if (added) {   //If API creation success,then try publishing the API
            published = publishAPIToStore(api.getId(),store.getEndpoint(),httpContext);
        }
        }
        return published;
    }

    /**
     * Authenticate to external APIStore
     *
     * @param httpContext  HTTPContext
     */
    private boolean authenticateAPIM(APIStore store,HttpContext httpContext) {
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            String storeEndpoint=store.getEndpoint();
            if(!generateEndpoint(store.getEndpoint())){
                storeEndpoint=storeEndpoint+"/site/blocks/user/login/ajax/login.jag";
            }
            HttpPost httppost = new HttpPost(storeEndpoint);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair(APIConstants.API_ACTION, APIConstants.API_LOGIN_ACTION));
            params.add(new BasicNameValuePair(APIConstants.APISTORE_LOGIN_USERNAME, store.getUsername()));
            params.add(new BasicNameValuePair(APIConstants.APISTORE_LOGIN_PASSWORD, store.getPassword()));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            if (response.getStatusLine().getStatusCode() != 200) {
                log.error(" Authentication with external APIStore failed: HTTP error code : " +
                                           response.getStatusLine().getStatusCode());
                return false;
            } else{
                return true;
            }

        } catch (Exception e) {
            log.error("Authentication with external APIStore fails", e);
            return false;
        }
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }

    private boolean addAPIToStore(API api,String storeEndpoint,HttpContext httpContext) {
        boolean added=false;
        HttpClient httpclient = new DefaultHttpClient();
        if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+"/site/blocks/item-add/ajax/add.jag";
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "addAPI"));
        params.add(new BasicNameValuePair("name", api.getId().getApiName()));
        params.add(new BasicNameValuePair("version", api.getId().getVersion()));
        params.add(new BasicNameValuePair("provider", api.getId().getProviderName()));
        params.add(new BasicNameValuePair("description", api.getDescription()));
        params.add(new BasicNameValuePair("endpoint", api.getUrl()));
        params.add(new BasicNameValuePair("sandbox", api.getSandboxUrl()));
        params.add(new BasicNameValuePair("wsdl", api.getWadlUrl()));
        params.add(new BasicNameValuePair("wadl", api.getWsdlUrl()));

        StringBuilder tagsSet = new StringBuilder("");

        Iterator it = api.getTags().iterator();
        int j = 0;
        while (it.hasNext()) {
            Object tagObject = it.next();
            tagsSet.append((String) tagObject);
            if (j != api.getTags().size() - 1) {
                tagsSet.append(",");
            }
            j++;
        }
        params.add(new BasicNameValuePair("tags", checkValue(tagsSet.toString())));

        StringBuilder tiersSet = new StringBuilder("");
        Iterator tier = api.getTags().iterator();
        int k = 0;
        while (tier.hasNext()) {
            Object tierObject = tier.next();
            tiersSet.append((String) tierObject);
            if (k != api.getTags().size() - 1) {
                tiersSet.append(",");
            }
            k++;
        }
        params.add(new BasicNameValuePair("tiersCollection", checkValue(tiersSet.toString())));
        params.add(new BasicNameValuePair("context", api.getContext()));
        params.add(new BasicNameValuePair("bizOwner", api.getBusinessOwner()));
        params.add(new BasicNameValuePair("bizOwnerMail", api.getBusinessOwnerEmail()));
        params.add(new BasicNameValuePair("techOwner", api.getTechnicalOwner()));
        params.add(new BasicNameValuePair("techOwnerMail", api.getTechnicalOwnerEmail()));
        params.add(new BasicNameValuePair("visibility", api.getVisibility()));
        params.add(new BasicNameValuePair("roles", api.getVisibleRoles()));
        params.add(new BasicNameValuePair("endpointType", String.valueOf(api.isEndpointSecured())));
        params.add(new BasicNameValuePair("epUsername", api.getEndpointUTUsername()));
        params.add(new BasicNameValuePair("epPassword", api.getEndpointUTPassword()));
        params.add(new BasicNameValuePair("http_checked", api.getTransports()));
        params.add(new BasicNameValuePair("resourceCount", "Hello!"));
        Iterator urlTemplate = api.getUriTemplates().iterator();
        int i=0;
        while (urlTemplate.hasNext()) {
            Object templateObject = urlTemplate.next();
            URITemplate template=(URITemplate)templateObject;
            params.add(new BasicNameValuePair("uriTemplate-" + i, template.getUriTemplate()));
            params.add(new BasicNameValuePair("resourceMethod-" + i, template.getHTTPVerb()));
            params.add(new BasicNameValuePair("resourceMethodAuthType-" + i, template.getAuthType()));
            params.add(new BasicNameValuePair("resourceMethodThrottlingTier-" + i, template.getThrottlingTier()));
        }

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode== 200 ) {   //If API creation success,then try publishing the API
               added=true;

            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error while adding the API to the external WSO2 APIStore" + e);
            return false;
        } catch (ClientProtocolException e) {
            log.error("Error while adding the API to the external WSO2 APIStore" + e);
            return false;
        } catch (IOException e) {
            log.error("Error while adding the API to the external WSO2 APIStore" + e);
            return false;
        }
        return added;
    }

    public void updateWSO2Store(API api, APIStore store) {

        if (store.getEndpoint() == null || store.getUsername() == null || store.getPassword() == null) {
            String msg = "External APIStore endpoint URL or credentials are not defined.Cannot proceed with publishing API to the APIStore - " + store.getDisplayName();
            log.error(msg);
        }

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        if(authenticateAPIM(store, httpContext)){
        updateAPIStore(api, store.getEndpoint(), httpContext);
        }

    }
    public boolean updateAPIStore(API api,String storeEndpoint,HttpContext httpContext) {
        boolean updated=false;
        HttpClient httpclient = new DefaultHttpClient();
        if(!generateEndpoint(storeEndpoint)){
            storeEndpoint=storeEndpoint+"/site/blocks/item-add/ajax/add.jag";
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "updateAPI"));
        params.add(new BasicNameValuePair("name", api.getId().getApiName()));
        params.add(new BasicNameValuePair("version", api.getId().getVersion()));
        params.add(new BasicNameValuePair("provider", api.getId().getProviderName()));
        params.add(new BasicNameValuePair("description", api.getDescription()));
        params.add(new BasicNameValuePair("endpoint", api.getUrl()));
        params.add(new BasicNameValuePair("sandbox", api.getSandboxUrl()));
        params.add(new BasicNameValuePair("wsdl", api.getWadlUrl()));
        params.add(new BasicNameValuePair("wadl", api.getWsdlUrl()));

        StringBuilder tagsSet = new StringBuilder("");

        Iterator it = api.getTags().iterator();
        int j = 0;
        while (it.hasNext()) {
            Object tagObject = it.next();
            tagsSet.append((String) tagObject);
            if (j != api.getTags().size() - 1) {
                tagsSet.append(",");
            }
            j++;
        }
        params.add(new BasicNameValuePair("tags", checkValue(tagsSet.toString())));

        StringBuilder tiersSet = new StringBuilder("");
        Iterator tier = api.getTags().iterator();
        int k = 0;
        while (tier.hasNext()) {
            Object tierObject = tier.next();
            tiersSet.append((String) tierObject);
            if (k != api.getTags().size() - 1) {
                tiersSet.append(",");
            }
            k++;
        }
        params.add(new BasicNameValuePair("tiersCollection", checkValue(tiersSet.toString())));
        params.add(new BasicNameValuePair("context", api.getContext()));
        params.add(new BasicNameValuePair("bizOwner", api.getBusinessOwner()));
        params.add(new BasicNameValuePair("bizOwnerMail", api.getBusinessOwnerEmail()));
        params.add(new BasicNameValuePair("techOwner", api.getTechnicalOwner()));
        params.add(new BasicNameValuePair("techOwnerMail", api.getTechnicalOwnerEmail()));
        params.add(new BasicNameValuePair("visibility", api.getVisibility()));
        params.add(new BasicNameValuePair("roles", api.getVisibleRoles()));
        params.add(new BasicNameValuePair("endpointType", String.valueOf(api.isEndpointSecured())));
        params.add(new BasicNameValuePair("epUsername", api.getEndpointUTUsername()));
        params.add(new BasicNameValuePair("epPassword", api.getEndpointUTPassword()));
        params.add(new BasicNameValuePair("http_checked", api.getTransports()));
        params.add(new BasicNameValuePair("resourceCount", "Hello!"));
        Iterator urlTemplate = api.getUriTemplates().iterator();
        int i=0;
        while (urlTemplate.hasNext()) {
            Object templateObject = urlTemplate.next();
            URITemplate template=(URITemplate)templateObject;
            params.add(new BasicNameValuePair("uriTemplate-" + i, template.getUriTemplate()));
            params.add(new BasicNameValuePair("resourceMethod-" + i, template.getHTTPVerb()));
            params.add(new BasicNameValuePair("resourceMethodAuthType-" + i, template.getAuthType()));
            params.add(new BasicNameValuePair("resourceMethodThrottlingTier-" + i, template.getThrottlingTier()));
        }

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode== 200 ) {   //If API creation success,then try publishing the API
               updated=true;

            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error while updating the API in the external WSO2 APIStore" + e);
            return false;
        } catch (ClientProtocolException e) {
            log.error("Error while updating the API in the external WSO2 APIStore" + e);
            return false;
        } catch (IOException e) {
            log.error("Error while updating the API in the external WSO2 APIStore" + e);
            return false;
        }
        return updated;
    }

    private boolean publishAPIToStore(APIIdentifier apiId,String storeEndpoint,HttpContext httpContext) {
        boolean published=false;
        HttpClient httpclient = new DefaultHttpClient();
        if(!generateEndpoint(storeEndpoint)){
        storeEndpoint=storeEndpoint+"/site/blocks/life-cycles/ajax/life-cycles.jag";
        }
        HttpPost httppost = new HttpPost(storeEndpoint);

        List<NameValuePair> paramVals = new ArrayList<NameValuePair>();
        paramVals.add(new BasicNameValuePair("action", "updateStatus"));
        paramVals.add(new BasicNameValuePair("name", apiId.getApiName()));
        paramVals.add(new BasicNameValuePair("provider", apiId.getProviderName()));
        paramVals.add(new BasicNameValuePair("version", apiId.getVersion()));
        paramVals.add(new BasicNameValuePair("status", APIConstants.PUBLISHED));
        paramVals.add(new BasicNameValuePair("publishToGateway", "true"));
        paramVals.add(new BasicNameValuePair("deprecateOldVersions", "false"));
        paramVals.add(new BasicNameValuePair("requireResubscription", "false"));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(paramVals, "UTF-8"));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost,httpContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode== 200 ) {   //If API creation success,then try publishing the API
               published=true;

            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error while publishing the API to the external WSO2 APIStore" + e);
            return false;
        } catch (ClientProtocolException e) {
            log.error("Error while publishing the API to the external WSO2 APIStore" + e);
            return false;
        } catch (IOException e) {
            log.error("Error while publishing the API to the external WSO2 APIStore" + e);
            return false;
        }
        return published;
    }

    private boolean generateEndpoint(String inputEndpoint) {
        boolean isAbsoluteEndpoint=false;
        if(inputEndpoint.contains("/site/block/")) {
        isAbsoluteEndpoint=true;
        }
        return isAbsoluteEndpoint;
    }


}
