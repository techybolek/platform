package org.wso2.carbon.identity.user.store.configuration.ui.utils;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.UserCoreConstants;
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
import java.util.HashMap;
import java.util.Map;


public class UserStoreUIUtils {


    private static final String realmName = "org.wso2.carbon.user.core.common.DefaultRealm";
    private Map<Integer, Map<String, String>> userStoreManagers = UserStoreMgtDataKeeper.getUserStoreManagers();
    private Map<String, String> userStoreProperties = new HashMap<String, String>();
    private Map<String, String> configProperties = UserStoreMgtDataKeeper.getConfigProperties();
    private Map<String, String> authzProperties = UserStoreMgtDataKeeper.getAuthzProperties();
    private final String CarbonHome = CarbonUtils.getCarbonHome();

    private String filePath = CarbonHome + "/repository/deployment/server/userstores";

    private String tenantFilePath = CarbonHome + "/repository/deployment/server/tenants";


    public void saveConfigurationToFile(String[] orders) throws TransformerException, ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root element
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("UserManager");
        doc.appendChild(rootElement);

        // realm element
        Element realmElement = doc.createElement("Realm");
        rootElement.appendChild(realmElement);

        Attr attrName = doc.createAttribute("name");
        attrName.setValue(realmName);
        realmElement.setAttributeNode(attrName);

        Element configElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_CONFIGURATION);
        realmElement.appendChild(configElement);
        addElement(configProperties, UserCoreConstants.RealmConfig.LOCAL_NAME_ADD_ADMIN, doc, configElement);
        addElement(configProperties, UserCoreConstants.RealmConfig.LOCAL_NAME_ADMIN_ROLE, doc, configElement);

        Element adminUserElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_ADMIN_USER);
        configElement.appendChild(adminUserElement);
        addElement(configProperties, UserCoreConstants.RealmConfig.LOCAL_NAME_USER_NAME, doc, adminUserElement);
        addElement(configProperties, UserCoreConstants.RealmConfig.LOCAL_NAME_PASSWORD, doc, adminUserElement);

        addElement(configProperties, UserCoreConstants.RealmConfig.LOCAL_NAME_EVERYONE_ROLE, doc, configElement);
        addProperties(configProperties, doc, configElement);

        //Add user store managers
        for (int i = 0; i < orders.length; i++) {
            if ((userStoreManagers.get(Integer.parseInt(orders[i])) != null)) {
                userStoreProperties = userStoreManagers.get(Integer.parseInt(orders[i]));
                //create UserStoreManager element
                Element userStoreElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER);
                realmElement.appendChild(userStoreElement);

                Attr attrClass = doc.createAttribute("class");
                attrClass.setValue(userStoreProperties.get("Class"));
                userStoreElement.setAttributeNode(attrClass);
                userStoreProperties.remove("Class");

                //create Description element
                Element descriptionElement = doc.createElement(UserCoreConstants.RealmConfig.CLASS_DESCRIPTION);
                userStoreElement.appendChild(descriptionElement);
                descriptionElement.setTextContent(userStoreProperties.get("Description"));
                userStoreProperties.remove("Description");
                addProperties(userStoreProperties, doc, userStoreElement);
            }

        }

        //Add authorization manager
        /**
         * <AuthorizationManager
         class="org.wso2.carbon.user.core.authorization.JDBCAuthorizationManager">
         <Property name="AdminRoleManagementPermissions">/permission</Property>
         <Property name="AuthorizationCacheEnabled">true</Property>
         </AuthorizationManager>
         */
        Element authzElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_ATHZ_MANAGER);
        realmElement.appendChild(authzElement);

        Attr attrClass = doc.createAttribute(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS);
        attrClass.setValue(authzProperties.get(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS));
        authzElement.setAttributeNode(attrClass);
        authzProperties.remove(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS);

        addProperties(authzProperties, doc, authzElement);

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);


        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        StreamResult result;

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            File userStore = new File(filePath);
            if (!userStore.exists()) {
                new File(filePath).mkdir();
            } else {

            }
            result = new StreamResult(new File(filePath + "/user-mgt.xml"));
        } else {
            tenantFilePath = tenantFilePath + File.separator + tenantId;
            File userStore = new File(tenantFilePath);
            if (!userStore.exists()) {
                new File(tenantFilePath).mkdir();
            } else {

            }
            result = new StreamResult(new File(tenantFilePath + "/user-mgt.xml"));
        }

        transformer.transform(source, result);

        UserStoreMgtDataKeeper.setEdited(false);
    }


    private void addProperties(Map<String, String> properties, Document doc, Element parent) {
        for (Map.Entry entry : properties.entrySet()) {
            if (entry.getValue() != null) {
                Element property = doc.createElement("Property");

                Attr attr = doc.createAttribute("name");
                attr.setValue(entry.getKey().toString());
                property.setAttributeNode(attr);

                property.setTextContent(entry.getValue().toString());
                parent.appendChild(property);
            }
        }
    }

    private void addElement(Map<String, String> properties, String name, Document doc, Element parent) {
        Element child = doc.createElement(name);
        parent.appendChild(child);
        child.setTextContent(properties.get(name));
        properties.remove(name);
    }


}
