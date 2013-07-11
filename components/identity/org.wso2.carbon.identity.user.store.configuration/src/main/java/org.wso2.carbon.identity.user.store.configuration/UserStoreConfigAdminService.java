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
package org.wso2.carbon.identity.user.store.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class UserStoreConfigAdminService extends AbstractAdmin {
    public static final Log log = LogFactory.getLog(UserStoreConfigAdminService.class);
    public static final String DISABLED = "Disabled";
    public static final String DOMAIN_NAME = "DomainName";
    public static final String DESCRIPTION = "Description";
    public static final String USERSTORES = "userstores";
    private static final String deploymentDirectory = CarbonUtils.getCarbonRepository() + USERSTORES;

    /**
     * Get details of current secondary user store configurations
     *
     * @return : Details of all the configured secondary user stores
     * @throws IdentityUserStoreMgtException
     * @throws UserStoreException
     */
    public UserStoreDTO[] getSecondaryRealmConfigurations() throws IdentityUserStoreMgtException, UserStoreException {
        ArrayList<UserStoreDTO> domains = new ArrayList<UserStoreDTO>();

        RealmConfiguration secondaryRealmConfiguration = CarbonContext.getCurrentContext().getUserRealm().
                getRealmConfiguration().getSecondaryRealmConfig();

        //not editing primary store
        if (secondaryRealmConfiguration == null) {
            return null;
        } else {

            do {
                Map<String, String> userStoreProperties = secondaryRealmConfiguration.getUserStoreProperties();
                UserStoreDTO userStoreDTO = new UserStoreDTO();
                String className = secondaryRealmConfiguration.getUserStoreClass();
                userStoreDTO.setClassName(secondaryRealmConfiguration.getUserStoreClass());
                userStoreDTO.setDescription(secondaryRealmConfiguration.getUserStoreProperty(DESCRIPTION));
                userStoreDTO.setDomainId(secondaryRealmConfiguration.getUserStoreProperty(DOMAIN_NAME));
                if (userStoreProperties.get(DISABLED) != null) {
                    userStoreDTO.setDisabled(Boolean.valueOf(userStoreProperties.get(DISABLED)));
                }
                userStoreProperties.put("Class", className);
                userStoreDTO.setProperties(convertMapToArray(userStoreProperties));

                domains.add(userStoreDTO);
                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();

            } while (secondaryRealmConfiguration != null);
        }
        return domains.toArray(new UserStoreDTO[domains.size()]);
    }

    /**
     * Get user store properties of a given active user store manager as an array
     *
     * @param properties: properties of the user store
     * @return key#value
     */
    private PropertyDTO[] convertMapToArray(Map<String, String> properties) throws IdentityUserStoreMgtException, UserStoreException {
        Set<Map.Entry<String, String>> propertyEntries = properties.entrySet();
        ArrayList<PropertyDTO> propertiesList = new ArrayList<PropertyDTO>();
        String key;
        String value;
//        int i = 0;
        for (Map.Entry entry : propertyEntries) {
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            PropertyDTO propertyDTO = new PropertyDTO(key, value);

            if (!key.contains("SQL")) {
                propertiesList.add(propertyDTO);
            }

        }
        return propertiesList.toArray(new PropertyDTO[propertiesList.size()]);
    }

    /**
     * Get available user store manager implementations
     *
     * @return: Available implementations for user store managers
     */
    public String[] getAvailableUserStoreClasses() {
        Set<String> classNames = UserStoreManagerRegistry.getUserStoreManagerClasses();
        return classNames.toArray(new String[classNames.size()]);
    }

    /**
     * Get User Store Manager default properties for a given implementation
     *
     * @param className:Implementation class name for the user store
     * @return : list of default properties(mandatory+optional)
     */
    public Properties getUserStoreManagerProperties(String className) {
        return UserStoreManagerRegistry.getUserStoreProperties(className);
    }

    /**
     * Save the sent configuration to xml file
     *
     * @param userStoreDTO: Represent the configuration of user store
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public void saveConfigurationToFile(UserStoreDTO userStoreDTO) throws ParserConfigurationException, TransformerException {

        String tenantFilePath = CarbonUtils.getCarbonTenantsDirPath();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        String domainName = userStoreDTO.getDomainId();
        String fileName = domainName.replace(".", "_");

        //create UserStoreManager element
        Element userStoreElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER);
        doc.appendChild(userStoreElement);

        Attr attrClass = doc.createAttribute("class");
        attrClass.setValue(userStoreDTO.getClassName());
        userStoreElement.setAttributeNode(attrClass);

        addProperties(userStoreDTO.getProperties(), doc, userStoreElement);
        addProperty(DOMAIN_NAME, userStoreDTO.getDomainId(), doc, userStoreElement);
        addProperty(DESCRIPTION, userStoreDTO.getDescription(), doc, userStoreElement);

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);


        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        StreamResult result;

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            File userStore = new File(deploymentDirectory);
            if (!userStore.exists()) {
                if (new File(deploymentDirectory).mkdir()) {
                    //folder 'userstores' created
                } else {
                    log.error("Error at creating 'userstores' directory to store configurations for super tenant");
                }
            } else {

            }
            result = new StreamResult(new File(deploymentDirectory + File.separator + fileName + ".xml"));
        } else {
            tenantFilePath = tenantFilePath + File.separator + tenantId + File.separator + USERSTORES;
            File userStore = new File(tenantFilePath);
            if (!userStore.exists()) {
                if(new File(tenantFilePath).mkdir()){
                    //folder 'userstores' created
                } else{
                    log.error("Error at creating 'userstores' directory to store configurations for tenant:"+tenantId);
                }
            } else {

            }
            result = new StreamResult(new File(tenantFilePath + File.separator + fileName + ".xml"));
        }

        transformer.transform(source, result);


    }

    /**
     * Adds an array of properties
     *
     * @param propertyDTOs : List of user store properties
     * @param doc:         Document
     * @param parent       : Parent element of the properties to be added
     */
    private void addProperties(PropertyDTO[] propertyDTOs, Document doc, Element parent) {
        for (PropertyDTO propertyDTO : propertyDTOs) {
            if (propertyDTO.getValue() != null) {
                addProperty(propertyDTO.getName(), propertyDTO.getValue(), doc, parent);
            }
        }
    }

    /**
     * Adds a property
     *
     * @param name:   Name of property
     * @param value:  Value
     * @param doc:    Document
     * @param parent: Parent element of the property to be added as a child
     */
    private void addProperty(String name, String value, Document doc, Element parent) {
        Element property = doc.createElement("Property");

        Attr attr = doc.createAttribute("name");
        attr.setValue(name);
        property.setAttributeNode(attr);

        property.setTextContent(value);
        parent.appendChild(property);
    }

    /**
     * Delete the given list of user stores
     *
     * @param userStores: domain names of user stores to be deleted
     */
    public void deleteUserStores(String[] userStores) {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        String path;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            path = deploymentDirectory;
        } else {
            path = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator + USERSTORES;

        }
        File file = new File(path);
        for (String userStore : userStores) {
            deleteFile(file, userStore.replace(".", "_").concat(".xml"));
        }

    }

    private void deleteFile(File file, final String userStoreName) {
        File[] deleteCandidates = file.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(userStoreName);
            }
        });
        for (File file1 : deleteCandidates) {
            if(file1.delete()){
                //file deleted successfully
            }else{
                log.error("error at deleting file:"+file.getName());
            }
        }
    }

    /**
     * Update a domain to be disabled/enabled
     *
     * @param domain: Name of the domain to be updated
     * @param disable : Whether to disable/enable domain(true/false)
     */
    public void updateDomain(String domain, String disable) throws UserStoreException, IdentityUserStoreMgtException, TransformerException, ParserConfigurationException {
        RealmConfiguration secondaryRealmConfiguration = CarbonContext.getCurrentContext().getUserRealm().
                getRealmConfiguration().getSecondaryRealmConfig();

        //not editing primary store
        if (secondaryRealmConfiguration == null) {
        } else {

            do {

                if (secondaryRealmConfiguration.getUserStoreProperty(DOMAIN_NAME).equalsIgnoreCase(domain)) {
                    UserStoreDTO userStoreDTO = new UserStoreDTO();
                    String className = secondaryRealmConfiguration.getUserStoreClass();
                    userStoreDTO.setClassName(secondaryRealmConfiguration.getUserStoreClass());
                    userStoreDTO.setDescription(secondaryRealmConfiguration.getUserStoreProperty(DESCRIPTION));
                    userStoreDTO.setDomainId(secondaryRealmConfiguration.getUserStoreProperty(DOMAIN_NAME));
                    userStoreDTO.setDisabled(Boolean.parseBoolean(disable));

                    Map<String, String> userStoreProperties = secondaryRealmConfiguration.getUserStoreProperties();
                    userStoreProperties.put("Class", className);
                    userStoreDTO.setProperties(convertMapToArray(userStoreProperties));
                    saveConfigurationToFile(userStoreDTO);
                }

                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();

            } while (secondaryRealmConfiguration != null);
        }
    }

}
