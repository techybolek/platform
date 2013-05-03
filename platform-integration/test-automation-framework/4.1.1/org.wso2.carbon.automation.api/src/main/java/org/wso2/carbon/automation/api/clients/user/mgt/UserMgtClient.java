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
package org.wso2.carbon.automation.api.clients.user.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.user.mgt.stub.DeleteUserUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.GetAllRolesNamesUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.GetUsersOfRoleUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.ListUsersUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.UpdateUsersOfRoleUserAdminExceptionException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class UserMgtClient {
    private final Log log = LogFactory.getLog(UserManagementClient.class);

    private UserAdminStub userAdminStub;
    private final String serviceName = "UserAdmin";

    public UserMgtClient(String backendURL, String sessionCookie) throws AxisFault {

        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, userAdminStub);
    }

    public UserMgtClient(String backendURL, String userName, String password)
            throws AxisFault {

        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, userAdminStub);
    }

    public void addRole(String roleName, String[] userList, String[] permissions)
            throws Exception {
        userAdminStub.addRole(roleName, userList, permissions);
    }

    public FlaggedName[] listRoles()
            throws Exception {
        return userAdminStub.getAllRolesNames();
    }

    public void addUser(String userName, String password, String[] roles,
                        String profileName) throws Exception {
        userAdminStub.addUser(userName, password, roles, null, profileName);
    }

    public void deleteRole(String roleName) throws Exception {
        FlaggedName[] existingRoles;
        userAdminStub.deleteRole(roleName);
    }

    public void deleteUser(String userName) throws Exception {

        String[] userList;
        userAdminStub.deleteUser(userName);
    }

    public String[] listUser(String filter) throws Exception {

        String[] userList;
        String[] users = userAdminStub.listUsers(filter);
        return users;
    }


    private void addRoleWithUser(String roleName, String userName)
            throws Exception {
        userAdminStub.addRole(roleName, new String[]{userName}, null);
    }


    protected void handleException(String msg, Exception e) throws Exception {
        log.error(msg, e);
        throw new Exception(msg + ": " + e);
    }

    public void updateUserListOfRole(String roleName, String[] addingUsers,
                                     String[] deletingUsers) throws Exception {
        List<FlaggedName> updatedUserList = new ArrayList<FlaggedName>();
        if (addingUsers != null) {
            for (String addUser : addingUsers) {
                FlaggedName fName = new FlaggedName();
                fName.setItemName(addUser);
                fName.setSelected(true);
                updatedUserList.add(fName);
            }
        }
        //add deleted users to the list
        if (deletingUsers != null) {
            for (String deletedUser : deletingUsers) {
                FlaggedName fName = new FlaggedName();
                fName.setItemName(deletedUser);
                fName.setSelected(false);
                updatedUserList.add(fName);
            }
        }
    }

    public boolean roleNameExists(String roleName)
            throws RemoteException, GetAllRolesNamesUserAdminExceptionException {
        FlaggedName[] roles = new FlaggedName[0];

        roles = userAdminStub.getAllRolesNames();

        for (FlaggedName role : roles) {
            if (role.getItemName().equals(roleName)) {
                log.info("Role name " + roleName + " already exists");
                return true;
            }
        }
        return false;
    }

    public boolean userNameExists(String roleName, String userName)
            throws RemoteException, GetAllRolesNamesUserAdminExceptionException, GetUsersOfRoleUserAdminExceptionException {
        FlaggedName[] users = new FlaggedName[0];

        users = userAdminStub.getUsersOfRole(roleName, "*");

        for (FlaggedName user : users) {
            if (user.getItemName().equals(userName)) {
                log.info("User name " + userName + " already exists");
                return true;
            }
        }
        return false;
    }
}
