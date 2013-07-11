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
import org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceStub;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Properties;

import java.rmi.RemoteException;

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
     * @return: active domains
     * @throws Exception
     */
    public UserStoreDTO[] getActiveDomains() throws Exception {

        return stub.getSecondaryRealmConfigurations();
    }

    /**
     * Get available user store implementations
     *
     * @return : available user store managers
     * @throws java.rmi.RemoteException
     */
    public String[] getAvailableUserStoreClasses() throws RemoteException {
        return stub.getAvailableUserStoreClasses();

    }

    /**
     * Get properties required for the given user store
     *
     *
     * @param className : list of properties required by each user store manager
     * @return : list of properties(mandatory+optional)
     * @throws java.rmi.RemoteException
     */
    public Properties getUserStoreProperties(String className) throws RemoteException {
        return stub.getUserStoreManagerProperties(className);

    }


    /**
     * Save configuration to file system
     *
     * @param userStoreDTO : representation of new user store to be persisted
     * @throws Exception
     */
    public void saveConfigurationToFile(UserStoreDTO userStoreDTO) throws Exception {
         stub.saveConfigurationToFile(userStoreDTO);
    }

    /**
     * Deletes a given list of user stores
     *
     * @param userStores : domain names of user stores to deleted
     * @throws RemoteException
     */
    public void deleteUserStores(String[] userStores) throws RemoteException {
         stub.deleteUserStores(userStores);
    }

    public void updateUserStore(String domain,String disabled) throws Exception {
         stub.updateDomain(domain,disabled);
    }
}
