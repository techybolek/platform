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
package org.wso2.carbon.identity.user.store.configuration.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserStoreConfigAdminService extends AbstractAdmin {
    public static final Log log = LogFactory.getLog(UserStoreConfigAdminService.class);
    private RealmConfiguration realmConfiguration;
    private Map<Integer, Map<String, String>> activeUserStores = new HashMap<Integer, Map<String, String>>();
    public static final String CLASS = "Class";
    public static final String DESCRIPTION = "Description";
    public static final String DISABLED = "Disabled";
    private UserStoreDTO[] userstoreDTOs;
    private String domainName;


    /**
     * Initialize all the details of available user stores, using current realm configuration
     *
     * @throws IdentityUserStoreMgtException
     */
    private void initialize() throws IdentityUserStoreMgtException, UserStoreException {

        String className;
        String description;
        String domainId;
        int order;
        ArrayList<UserStoreDTO> domains = new ArrayList<UserStoreDTO>();
        UserStoreDTO userstoreDTO = new UserStoreDTO();

        realmConfiguration = CarbonContext.getCurrentContext().getUserRealm().getRealmConfiguration();

        RealmConfiguration secondaryRealmConfig = realmConfiguration.getSecondaryRealmConfig();
        className = realmConfiguration.getUserStoreClass();
        description = realmConfiguration.getDescription();
        order = 1;            //Since Primary
        domainId = domainName;


        userstoreDTO.setClassName(className);
        userstoreDTO.setOrder(order);
        userstoreDTO.setDescription(description);
        userstoreDTO.setDomainId(domainId);

        domains.add(userstoreDTO);

        Map<String, String> userStoreProperties = realmConfiguration.getUserStoreProperties();
        if (userStoreProperties.get(DISABLED) != null) {
            userstoreDTO.setDisabled(Boolean.valueOf(userStoreProperties.get(DISABLED)));
        }
//        userStoreProperties.put("Class", className);
//        userStoreProperties.put("Description", description);

        activeUserStores.put(order, userStoreProperties);

        if (secondaryRealmConfig != null) {
            getSecondaryRealmConfigurations(secondaryRealmConfig, domains);

        } else {

        }

        userstoreDTOs = domains.toArray(new UserStoreDTO[domains.size()]);

    }


    /**
     * Initialize details of current secondary user store configurations
     *
     * @param subSecondaryRealmConfig
     * @throws org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException
     *
     */
    private void getSecondaryRealmConfigurations(RealmConfiguration subSecondaryRealmConfig, ArrayList domains) throws IdentityUserStoreMgtException {
        int i = 2;        //since Primary is set at this moment, start from first secondary org.wso2.carbon.identity.user.store.configuration
        String userStoreClass;
        String description;
        String domainId;
        int order;
        do {
            Map<String, String> userStoreProperties = subSecondaryRealmConfig.getUserStoreProperties();
            userStoreClass = subSecondaryRealmConfig.getUserStoreClass();
            description = subSecondaryRealmConfig.getDescription();
            order = i;
            domainId = subSecondaryRealmConfig.getUserStoreProperty("DomainName");
            UserStoreDTO userStoreDTO = new UserStoreDTO();


            userStoreDTO.setClassName(userStoreClass);
            userStoreDTO.setOrder(order);
            userStoreDTO.setDescription(description);
            userStoreDTO.setDomainId(domainId);
            if (userStoreProperties.get("Disabled") != null) {
                userStoreDTO.setDisabled(Boolean.valueOf(userStoreProperties.get("Disabled")));
            }
            domains.add(userStoreDTO);
//
//            userStoreProperties.put("Class", userStoreClass);
//            userStoreProperties.put("Description", description);

            activeUserStores.put(order, userStoreProperties);

            subSecondaryRealmConfig = subSecondaryRealmConfig.getSecondaryRealmConfig();
            i++;

        } while (subSecondaryRealmConfig != null);             //Add all the secondary realm-config details to database

    }

    /**
     * Get all the details of available user stores
     *
     * @return details - Class,Order,Properties
     * @throws org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException
     *
     */
    public UserStoreDTO[] getActiveDomains() throws IdentityUserStoreMgtException, UserStoreException {
        initialize();
        return userstoreDTOs.clone();

    }

    /**
     * Get user store properties of a given active user store manager
     *
     * @param order
     * @return
     */
    public String[] getActiveUserStoreProperties(int order) throws IdentityUserStoreMgtException, UserStoreException {
        initialize();
        Set<Map.Entry<String, String>> properties = activeUserStores.get(order).entrySet();
        ArrayList<String> propertiesList = new ArrayList<String>();
        String[] propertyArray;
        String key;
        String value;
//        int i = 0;
        for (Map.Entry entry : properties) {
            key = (String) entry.getKey();
            value = (String) entry.getValue();

            if (key.contains("password") || key.contains("Password")) {
                value = " ";
            }

            if (!key.contains("SQL")) {                                    //Not to add SQL statements
                propertiesList.add(key + "#" + value);                     //key#value []
            }

        }

        return propertiesList.toArray(new String[propertiesList.size()]);
    }

    /**
     * Get available user store manager implementations
     *
     * @return
     */
    public String[] getAvailableUserStoreClasses() {
        Set<String> classNames = UserStoreManagerRegistry.getUserStoreManagerClasses();
        return classNames.toArray(new String[classNames.size()]);
    }

    /**
     * Get User Store Manager default properties for a given implementation
     *
     * @param className
     * @return
     */
    public Property[] getUserStoreManagerProperties(String className) {
        return UserStoreManagerRegistry.getUserStoreProperties(className);
    }

    /**
     * Get the properties required for authorization manager
     *
     * @return
     */
    public synchronized String[] getAuthzProperties() throws UserStoreException, IdentityUserStoreMgtException {
        initialize();
        Map<String, String> properties = realmConfiguration.getAuthzProperties();
        properties.put(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS, realmConfiguration.getAuthorizationManagerClass());

        return convertMaptoArray(properties);

    }


    /**
     * Get the properties for configuration
     *
     * @return
     */
    public synchronized String[] getConfigProperties() throws UserStoreException, IdentityUserStoreMgtException {
        initialize();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(UserCoreConstants.RealmConfig.LOCAL_NAME_ADD_ADMIN, realmConfiguration.getAddAdmin());
        properties.put(UserCoreConstants.RealmConfig.LOCAL_NAME_USER_NAME, realmConfiguration.getAdminUserName());
        properties.put(UserCoreConstants.RealmConfig.LOCAL_NAME_PASSWORD, realmConfiguration.getAdminPassword());
        properties.put(UserCoreConstants.RealmConfig.LOCAL_NAME_ADMIN_ROLE, realmConfiguration.getAdminRoleName());
        properties.put(UserCoreConstants.RealmConfig.LOCAL_NAME_EVERYONE_ROLE, realmConfiguration.getEveryOneRoleName());
        properties.putAll(realmConfiguration.getRealmProperties());

        return convertMaptoArray(properties);
    }


    /**
     * Convert a given Map<String,String> to String[] with propertyName#propertyValue entries
     *
     * @param properties
     * @return
     */
    private String[] convertMaptoArray(Map<String, String> properties) {
        Set<Map.Entry<String, String>> propertyEntries = properties.entrySet();
        String[] propertyArray = new String[propertyEntries.size()];
        int i = 0;
        for (Map.Entry entry : propertyEntries) {
            if (entry.getValue() != null && entry.getValue().toString().trim().length() > 0) {
                propertyArray[i] = entry.getKey() + "#" + entry.getValue();                //key#value []
                i++;
            } else {

            }
        }

        return propertyArray;

    }
}
