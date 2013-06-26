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
package org.wso2.carbon.identity.user.store.configuration.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceStub;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Property;
import org.wso2.carbon.identity.user.store.configuration.stub.config.UserStoreDTO;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserStoreConfigAdminServiceClient {
    private UserStoreConfigAdminServiceStub stub;

    /**
     * Constructor UserStoreConfigAdminServiceClient
     *
     * @param cookie
     * @param backendServerURL
     * @param configContext
     */
    public UserStoreConfigAdminServiceClient(String cookie, String backendServerURL,
                                             ConfigurationContext configContext) throws AxisFault {
        String serviceURL = backendServerURL + "UserStoreConfigAdminService";
        stub = new UserStoreConfigAdminServiceStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Get all the configured domains
     *
     * @return
     * @throws Exception
     */
    public UserStoreDTO[] getActiveDomains() throws RemoteException, UserStoreConfigAdminServiceIdentityUserStoreMgtException {
        return stub.getActiveDomains();
    }

    /**
     * Get the defined properties for a given active user store
     *
     * @param order
     * @return
     * @throws Exception
     */
    public Map<String, String> getActiveUserStoreProperties(int order) throws RemoteException, UserStoreConfigAdminServiceIdentityUserStoreMgtException {

        Map<String, String> propertyMap = new HashMap<String, String>();
        String[] properties;


        properties = stub.getActiveUserStoreProperties(order);
        for (int i = 0; i < properties.length; i++) {
            String[] property = properties[i].split("#");
            if (property.length > 1) {
                propertyMap.put(property[0], property[1]);
            }
        }

        return propertyMap;
    }

    /**
     * Get available user store implementations
     *
     * @return
     * @throws java.rmi.RemoteException
     */
    public String[] getAvailableUserStoreClasses() throws RemoteException {
        return stub.getAvailableUserStoreClasses();

    }

    /**
     * Get properties required for the given user store
     *
     * @param className
     * @return
     * @throws java.rmi.RemoteException
     */
    public ArrayList<Property> getUserStoreProperties(String className) throws RemoteException {
        Property[] properties = stub.getUserStoreManagerProperties(className);
        ArrayList<Property> propertyList=new ArrayList<Property>(Arrays.asList(properties));


        return propertyList;
    }



    /**
     * Get the configuration properties for user manager
     *
     * @return
     * @throws java.rmi.RemoteException
     */
    public Map<String, String> getConfigProperties() throws RemoteException {
        return convertArrayToMap(stub.getConfigProperties());

    }

    /**
     * Get the authorization manager properties for user manager
     *
     * @return
     * @throws java.rmi.RemoteException
     */
    public Map<String, String> getAuthzProperties() throws RemoteException {
        return convertArrayToMap(stub.getAuthzProperties());

    }

    /**
     * Convert a given String[] propertyName#propertyValue to a Map<String,String>
     *
     * @param properties
     * @return
     */
    private Map<String, String> convertArrayToMap(String[] properties) {
        Map<String, String> propertyMap = new HashMap<String, String>();
        for (int i = 0; i < properties.length; i++) {
            if (properties[i] != null) {
                String[] property = properties[i].split("#");
                propertyMap.put(property[0], property[1]);
            }
        }

        return propertyMap;

    }

}
