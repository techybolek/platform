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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.databasecontext.Database;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UserManagerContextFactory {

    /**
     * this method is get the list of the tenant level objects from the configuration
     */

    public List<OMNode> listTenants(OMElement endPointElem) {

        List<OMNode> tenantList = new ArrayList<OMNode>();

        OMNode node;
        Iterator children = endPointElem.getChildElements();
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

/*    public User getUserContext(OMElement element) {


    }*/

/*    public HashMap<String, Tenant> getTenantList() {

        HashMap<String, Tenant> tenantList = new HashMap<String, Tenant>();

        for (OMNode tenant : listTenants()) {
            for (OMNode tenantUser : listTenantUsers(tenant)) {
                for (OMNode user : listUsers(tenantUser)) {
                    for (User user1 : getUserContext(user1).values()) {
                        // tenantList;
                    }
                }
            }
        }


    }*/
}
