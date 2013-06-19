package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceStub;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.user.mgt.common.ClaimValue;
import org.wso2.carbon.user.mgt.stub.*;
import org.wso2.carbon.user.mgt.ui.Util;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.Calendar;

public class UserManagementClient {

    private UserProfileMgtServiceStub profileMgtServiceStub;
    private UserAdminStub userAdminStub;
    private ResourceAdminServiceStub resourceAdminStub;
    private TenantMgtAdminServiceStub tenantAdminStub;

    public UserManagementClient(String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {
        try {
            String epr = backendServerURL + "UserProfileMgtService";
            profileMgtServiceStub = new UserProfileMgtServiceStub(configContext, epr);

            ServiceClient client = profileMgtServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate user profile management service client. " + e.getMessage();
            throw new RegistryException(msg, e);
        }

        try {
            String epr = backendServerURL + "UserAdmin";
            userAdminStub = new UserAdminStub(configContext, epr);
            ServiceClient client = userAdminStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate user admin service client. " + e.getMessage();
            throw new RegistryException(msg, e);
        }

        try {
            String epr = backendServerURL + "ResourceAdminService";
            resourceAdminStub = new ResourceAdminServiceStub(configContext, epr);
            ServiceClient client = resourceAdminStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate resource admin service client. " + e.getMessage();
            throw new RegistryException(msg, e);
        }

        try {
            String epr = backendServerURL + "TenantMgtAdminService";
            tenantAdminStub = new TenantMgtAdminServiceStub(configContext, epr);
            ServiceClient client = tenantAdminStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate tenant management admin service client. " + e.getMessage();
            throw new RegistryException(msg, e);
        }
    }

    public UserProfileDTO getUserProfile(String username, String profile) throws Exception {
        return profileMgtServiceStub.getUserProfile(username, profile);
    }

    public void setUserProfile(String username, UserProfileDTO profile) throws Exception {
        profileMgtServiceStub.setUserProfile(username, profile);
    }

    public void addRole(String roleName, String[] userList, String[] permissions) throws Exception {
        userAdminStub.addRole(roleName, userList, permissions);
    }

    public void addUser(String userName, String password, String[] roles, ClaimValue[] claims,
                        String profileName) throws Exception {
        userAdminStub.addUser(userName, password, roles, Util.toADBClaimValues(claims), profileName);
    }

    public void setRoleUIPermission(String roleName, String[] permissions) throws Exception {
        userAdminStub.setRoleUIPermission(roleName, permissions);
    }

    public void setRoleResourcePermission(String path, String roleName, String[] permissions) throws Exception {
        String permissionString = "ra^false:rd^false:wa^false:wd^false:da^false:dd^false:aa^false:ad^false";
        for (String permission : permissions) {
            permissionString = permissionString.replace(permission + "^false", permission + "^true");
        }
        resourceAdminStub.addRolePermission(path, roleName, "2", "1");
        resourceAdminStub.changeRolePermissions(path, roleName + ":" + permissionString);
    }

    public void addTenant(String adminUsername, String adminPassword, String adminEmail, String firstName,
                          String lastName, String tenantDomain) throws Exception {
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setAdmin(adminUsername);
        tenantInfoBean.setAdminPassword(adminPassword);
        tenantInfoBean.setEmail(adminEmail);
        tenantInfoBean.setFirstname(firstName);
        tenantInfoBean.setLastname(lastName);
        tenantInfoBean.setTenantDomain(tenantDomain);
        tenantInfoBean.setUsagePlan("Demo");
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantAdminStub.addTenant(tenantInfoBean);
    }
}

