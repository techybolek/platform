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
package org.wso2.carbon.identity.user.store.configuration.ui.utils;

import org.wso2.carbon.identity.user.store.configuration.stub.config.UserStoreDTO;

import java.util.*;

public final class UserStoreMgtDataKeeper {
    private UserStoreMgtDataKeeper() {

    }

    private static Map<Integer, Map<String, String>> userStoreManagers = new HashMap<Integer, Map<String, String>>();
    private static Map<String, String> configProperties = new HashMap<String, String>();
    private static Map<String, String> authzProperties = new HashMap<String, String>();
    private static Boolean isEdited = false;                   //if edited load details from memory
    private static List<String> changedUserStores = new ArrayList<String>();


    /**
     * Get all the details of defined user stores
     *
     * @return Hashmap having the order as key and all the other properties of user store manager
     *         as Map stored to value(including class name)
     */
    public static Map<Integer, Map<String, String>> getUserStoreManagers() {
        return userStoreManagers;
    }

    /**
     * Add a new user store manager to in-memory map, at the given order
     *
     * @param userStoreManager
     */
    public static void addUserStoreManagers(Map<String, String> userStoreManager, int order) {
        UserStoreMgtDataKeeper.userStoreManagers.put(order, userStoreManager);
    }

    /**
     * Remove user store from in-memory map
     *
     * @param order
     */
    public static void removeUserStoreManagers(int order) {
        userStoreManagers.remove(order);
    }

    /**
     * Get the user store manager by order
     *
     * @param order
     * @return
     */
    public static Map<String, String> getUserStoreManager(int order) {
        return userStoreManagers.get(order);
    }


    /**
     * Get basic details of the active user store managers
     *
     * @return
     */
    public static UserStoreDTO[] getUserStoreManagersBasics() {
        ArrayList<UserStoreDTO> userStoreDTOList = new ArrayList<UserStoreDTO>();
        Set<Integer> keys = userStoreManagers.keySet();
        Map<String, String> properties;
        for (Integer key : keys) {
            properties = getUserStoreManager(key);
            UserStoreDTO userStoreDTO = new UserStoreDTO();
            userStoreDTO.setOrder(key);
            userStoreDTO.setClassName(properties.get("Class"));
            userStoreDTO.setDomainId(properties.get("DomainName"));
            userStoreDTO.setDescription(properties.get("Description"));
            userStoreDTO.setDisabled(Boolean.parseBoolean(properties.get("Disabled")));

            userStoreDTOList.add(userStoreDTO);
        }
        return userStoreDTOList.toArray(new UserStoreDTO[userStoreDTOList.size()]);
    }

    /**
     * The order to be given to newly adding user store
     *
     * @return
     */
    public static int getNextRank() {
        return userStoreManagers.size() + 1;
    }

    public static Map<String, String> getConfigProperties() {
        return configProperties;
    }

    public static void setConfigProperties(Map<String, String> configProperties) {
        UserStoreMgtDataKeeper.configProperties = configProperties;
    }

    public static Map<String, String> getAuthzProperties() {
        return authzProperties;
    }

    public static void setAuthzProperties(Map<String, String> authzProperties) {
        UserStoreMgtDataKeeper.authzProperties = authzProperties;
    }

    public static Boolean getEdited() {
        return isEdited;
    }

    public static void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public static void emptyUserStoreManagers() {
        userStoreManagers.clear();
    }

    public static void addChangedUserStore(String domainId) {
        changedUserStores.add(domainId);
    }

    public static void emptyChangedUserStores(){
        changedUserStores.clear();
    }

    public static List<String> getChangedUserStores(){
        return changedUserStores;
    }
}
