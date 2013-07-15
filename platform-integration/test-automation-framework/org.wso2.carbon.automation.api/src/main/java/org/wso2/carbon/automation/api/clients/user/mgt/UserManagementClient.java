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
import org.wso2.carbon.user.mgt.stub.*;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class UserManagementClient {
    private static final int LIMIT = 100;
    private final Log log = LogFactory.getLog(UserManagementClient.class);

    private UserAdminStub userAdminStub;
    private final String serviceName = "UserAdmin";

    public UserManagementClient(String backendURL, String sessionCookie) throws AxisFault {

        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, userAdminStub);
    }

    public UserManagementClient(String backendURL, String userName, String password)
            throws AxisFault {

        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, userAdminStub);
    }

    public void addRole(String roleName, String[] userList, String[] permissions)
            throws Exception {

        userAdminStub.addRole(roleName, userList, permissions, false);
    }

    public void addRole(String roleName, String[] userList, String[] permissions,
                        boolean isSharedRole)
            throws RemoteException, UserAdminUserAdminException {

        try {
            userAdminStub.addRole(roleName, userList, permissions, isSharedRole);
        } catch (RemoteException e) {
            log.error("Fail to add Role - " + roleName, e);
            throw new RemoteException("Fail to add Role - " + roleName, e);
        } catch (UserAdminUserAdminException e) {
            log.error("Fail to add Role - " + roleName, e);
            throw new UserAdminUserAdminException("Fail to add Role - " + roleName, e);
        }
    }

    public void addUser(String userName, String password, String[] roles,
                        String profileName) throws Exception {

        userAdminStub.addUser(userName, password, roles, null, profileName);
    }

    public static ClaimValue[] toADBClaimValues(
            org.wso2.carbon.user.mgt.common.ClaimValue[] claimValues) {
        if (claimValues == null) {
            return new ClaimValue[0];
        }
        ClaimValue[] values = new ClaimValue[claimValues.length];
        for (org.wso2.carbon.user.mgt.common.ClaimValue cvalue : claimValues) {
            ClaimValue value = new ClaimValue();
            value.setClaimURI(cvalue.getClaimURI());
            value.setValue(cvalue.getValue());
        }
        return values;
    }

    public void deleteRole(String roleName) throws Exception {
        FlaggedName[] existingRoles;
        try {
            userAdminStub.deleteRole(roleName);
            existingRoles = userAdminStub.getAllRolesNames(roleName, LIMIT);

            for (FlaggedName existingRole : existingRoles) {
                if (roleName.equals(existingRole.getItemName())) {
                    assert false : "Deleted role still exists..";
                }
            }
        } catch (RemoteException e) {
            handleException("Failed to get all roles", e);
        }
    }

    public void deleteUser(String userName) throws Exception {

        String[] userList;
        try {
            userAdminStub.deleteUser(userName);
            userList = userAdminStub.listUsers(userName, LIMIT);
            assert (userList == null || userList.length == 0);
        } catch (RemoteException e) {
            handleException("Failed to list users", e);
        }
    }

    private void addRoleWithUser(String roleName, String userName, String[] permission)
            throws Exception {
        userAdminStub.addRole(roleName, new String[]{userName}, permission, false);
        FlaggedName[] roles = userAdminStub.getAllRolesNames(roleName, LIMIT);
        for (FlaggedName role : roles) {
            if (!role.getItemName().equals(roleName)) {
                continue;
            } else {
                assert (role.getItemName().equals(roleName));
            }
            assert false : "Role: " + roleName + " was not added properly.";
        }
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
        //call userAdminStub to update user list of role
        try {
            userAdminStub.updateUsersOfRole(roleName, updatedUserList.toArray(
                    new FlaggedName[updatedUserList.size()]));

            //if delete users in retrieved list, fail
            if (deletingUsers != null) {
                for (String deletedUser : deletingUsers) {
                    FlaggedName[] verifyingList;
                    verifyingList = userAdminStub.getUsersOfRole(roleName, deletedUser, LIMIT);

                    assert (!verifyingList[0].getSelected());
                }
            }

            if (addingUsers != null) {
                //if all added users are not in list fail
                for (String addingUser : addingUsers) {
                    FlaggedName[] verifyingList = userAdminStub.getUsersOfRole(roleName, addingUser, LIMIT);
                    assert (verifyingList[0].getSelected());
                }
            }
        } catch (RemoteException e1) {
            handleException("Failed to update role", e1);
        }
    }

    public boolean roleNameExists(String roleName)
            throws Exception {
        FlaggedName[] roles = new FlaggedName[0];
        try {
            roles = userAdminStub.getAllRolesNames(roleName, LIMIT);
        } catch (RemoteException e) {
            handleException("Unable to get role names list", e);
        } catch (UserAdminUserAdminException e) {
            handleException("Faile to get all roles", e);
        }

        for (FlaggedName role : roles) {
            if (role.getItemName().equals(roleName)) {
                log.info("Role name " + roleName + " already exists");
                return true;
            }
        }
        return false;
    }

    public boolean userNameExists(String roleName, String userName)
            throws Exception {
        FlaggedName[] users = new FlaggedName[0];
        try {
            users = userAdminStub.getUsersOfRole(roleName, "*", LIMIT);
        } catch (RemoteException e) {
            log.error("Unable to get user names list");
            throw new RemoteException("Unable to get user names list");
        } catch (UserAdminUserAdminException e) {
            handleException("Unable to get user name list", e);
        }

        for (FlaggedName user : users) {
            if (user.getItemName().equals(userName)) {
                log.info("User name " + userName + " already exists");
                return true;
            }
        }
        return false;
    }
}
