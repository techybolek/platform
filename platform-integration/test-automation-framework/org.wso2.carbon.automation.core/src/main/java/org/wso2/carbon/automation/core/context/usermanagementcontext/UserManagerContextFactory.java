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
import org.apache.axiom.om.OMNode;

import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;


import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UserManagerContextFactory {

    /**
     * this method is get the list of the tenant level objects from the configuration
     */

    public List<OMNode> listTenants(OMNode endPointElem) {

        List<OMNode> tenantList = new ArrayList<OMNode>();

        OMNode node;
        Iterator children = ((OMElementImpl) endPointElem).getChildElements();
        while (children.hasNext()) {
            node = (OMNode) children.next();
            tenantList.add(node);
        }
        return tenantList;
    }


    /**
     * List all the Tenant users (Tenant_admin and Tenant_users
     * <p/>
     * *
     */
    public List<OMNode> listTenantUsers(OMNode node) {
        List<OMNode> tenantUserList = new ArrayList<OMNode>();
        Iterator environmentNodeItr = ((OMElementImpl) node).getChildElements();
        while (environmentNodeItr.hasNext()) {
            tenantUserList.add((OMNode) environmentNodeItr.next());
        }
        return tenantUserList;
    }

    /**
     * List all the users in tenant users
     * <p/>
     * *
     */
    public List<OMNode> listUsers(OMNode node) {
        List<OMNode> userList = new ArrayList<OMNode>();
        Iterator userIterator = ((OMElementImpl) node).getChildElements();
        while (userIterator.hasNext()) {
            userList.add((OMNode) userIterator.next());
        }
        return userList;
    }

    public UserManagerContext getUserManagementContext(OMNode omNode) {

        UserManagerContext userManagerContext = new UserManagerContext();
        HashMap<String, Tenant> tenantMap = new HashMap<String, Tenant>();

        for (OMNode tenantNode : listTenants(omNode)) {

            Tenant tenant = new Tenant();


            for (OMNode tenantUser : listTenantUsers(tenantNode)) {


                for (OMNode userNode : listUsers(tenantUser)) {
                    HashMap<String, User> userList = new HashMap<String, User>();


                    //  the tenant user can have two types
                    //1. Tenant user type that has collection of users
                    //2. Tenant admin

                    //check for the type of the user

                    String userType = ((OMElementImpl) userNode).getLocalName();

                    //this is the case for the tenant admin
                    if (userType.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_USER_TYPE_TENANT_ADMIN)) {
                        User tenantUsr = createUser(userNode);
                        tenant.setTenantAdmin(tenantUsr);
                        continue;

                    }

                    //this is the case of the tenant user that has collection of users

                    else if (userType.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_USER_TYPE_TENANT_USERS)) {


                        for (OMNode usersNode : listUsers(userNode)) {

                            User user = createUser(usersNode);
                            userList.put(user.getKey(), user);


                        }
                    }
                    // set the users list of the tenant user category to the current tenants
                    tenant.setTenantUsers(userList);

                }

                //add the current tenant to the tenat list
                tenantMap.put(tenant.getKey(), tenant);
            }
        }

        userManagerContext.setTenant(tenantMap);
        return userManagerContext;

    }


    public User createUser(OMNode userNode) {
        User user = new User();

        String userKey = ((OMElementImpl) userNode).getAttributeValue(QName.valueOf(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_USERS_USER_KEY));
        user.setKey(userKey);
        OMNode usrNode;
        Iterator userProperties = ((OMElementImpl) userNode).getChildElements();
        while (userProperties.hasNext()) {
            usrNode = (OMNode) userProperties.next();
            String attributeName = ((OMElementImpl) usrNode).getLocalName();
            String attributeValue = ((OMElementImpl) usrNode).getText();
            if (attributeName.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_USERS_USER_USERNAME)) {
                user.setUserName(attributeValue);

            } else if (attributeName.equals(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_USERS_USER_PASSWORD)) {
                user.setPassword(attributeValue);

            }


        }
        return user;
    }

    //creates the tenants object
    public Tenant createTenant(OMNode omNode) {
        Tenant tenant = new Tenant();
        String tenantKey = ((OMElementImpl) omNode).getAttributeValue(QName.valueOf(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_KEY));
        String tenantDomain = ((OMElementImpl) omNode).getAttributeValue(QName.valueOf(ContextConstants.USER_MANAGEMENT_CONTEXT_TENANT_DOMAIN));
        tenant.setKey(tenantKey);
        tenant.setDomain(tenantDomain);
        return tenant;

    }

    public void createConfiguration(OMElement nodeElement) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
