/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core.context.usermanagementcontext;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMElement;

import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;


import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UserManagerContextFactory {

    UserManagerContext userManagerContext;

    public UserManagerContextFactory() {


        userManagerContext = new UserManagerContext();
    }

    /*
      this method is get the list of the tenant level objects from the configuration
     */

    protected List<OMElement> listTenants(OMElement endPointElem) {

        List<OMElement> tenantList = new ArrayList<OMElement>();

        OMElement node;
        Iterator children = endPointElem.getChildElements();
        while (children.hasNext()) {
            node = (OMElement) children.next();
            tenantList.add(node);
        }
        return tenantList;
    }


    /*
      List all the Tenant users (Tenant_admin and Tenant_users
     */
    protected List<OMElement> listTenantUsers(OMElement node) {
        List<OMElement> tenantUserList = new ArrayList<OMElement>();
        Iterator environmentNodeItr = node.getChildElements();
        while (environmentNodeItr.hasNext()) {
            tenantUserList.add((OMElement) environmentNodeItr.next());
        }
        return tenantUserList;
    }

    /*
      List all the users in tenant users
     */
    protected List<OMElement> listUsers(OMElement node) {
        List<OMElement> userList = new ArrayList<OMElement>();
        Iterator userIterator = node.getChildElements();
        while (userIterator.hasNext()) {
            userList.add((OMElement) userIterator.next());
        }
        return userList;
    }

    public UserManagerContext getUserManagementContext() {

        return userManagerContext;

    }


    protected User createUser(OMElement userNode) {

        User user = new User();
        String userType = userNode.getLocalName();
        String userKey = null;

        //tenant admin is also a special case of user but there is no key with it, so we give a default key for if the incoming node is a tenant admin

        if (userType.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_USER_TYPE_TENANT_ADMIN)) {

            userKey = ContextConstants.USER_MANAGEMENT_TENANT_ADMIN_KEY;


        }

        //this is the user case: there is a key with it
        else {

            userKey = userNode.getAttributeValue(QName.valueOf(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_USERS_USER_KEY));

        }
        user.setKey(userKey);
        OMElement usrNode;
        Iterator userProperties = userNode.getChildElements();
        while (userProperties.hasNext()) {
            usrNode = (OMElement) userProperties.next();
            String attributeName = usrNode.getLocalName();
            String attributeValue = usrNode.getText();
            if (attributeName.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_USERS_USER_USERNAME)) {
                user.setUserName(attributeValue);

            } else if (attributeName.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_USERS_USER_PASSWORD)) {
                user.setPassword(attributeValue);

            }


        }
        return user;
    }

    /**
     * @param nodeElement OMElement input from the xml reader
     */
    public void createUserManagementContext(OMElement nodeElement) {


        HashMap<String, Tenant> tenantMap = new HashMap<String, Tenant>();

        for (OMElement tenantNode : listTenants(nodeElement)) {

            Tenant tenant = new Tenant();
            String tenantDomain = tenantNode.getAttributeValue(QName.valueOf(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_DOMAIN));
            tenant.setDomain(tenantDomain);

            for (OMElement tenantUser : listTenantUsers(tenantNode)) {

                //  the tenant user can have two types
                //1. Tenant user type that has collection of users
                //2. Tenant admin

                //check for the type of the user

                String userType = tenantUser.getLocalName();

                //this is the case for the tenant admin
                if (userType.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_USER_TYPE_TENANT_ADMIN)) {
                    User tenantUsr = createUser(tenantUser);
                    tenant.setTenantAdmin(tenantUsr);
                    continue;

                }

                //this is the case of the tenant user that has collection of users

                else if (userType.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_USER_TYPE_TENANT_USERS)) {


                    HashMap<String, User> userList = new HashMap<String, User>();

                    for (OMElement usersNode : listUsers(tenantUser)) {

                        User user = createUser(usersNode);
                        userList.put(user.getKey(), user);


                    }

                    // set the users list of the tenant user category to the current tenants
                    tenant.setTenantUsers(userList);


                }


            }

            //add the current tenant to the tenant list
            tenantMap.put(tenant.getDomain(), tenant);
        }

        userManagerContext.setTenant(tenantMap);

        //return  userManagerContext;

    }
}
