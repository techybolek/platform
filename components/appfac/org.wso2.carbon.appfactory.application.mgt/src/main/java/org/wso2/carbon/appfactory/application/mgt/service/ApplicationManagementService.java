/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.application.mgt.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.application.mgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.appfactory.application.mgt.service.applicationqueue.ApplicationCreator;
import org.wso2.carbon.appfactory.application.mgt.util.UserApplicationCache;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.bam.integration.BamDataPublisher;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsListener;
import org.wso2.carbon.appfactory.core.cache.AppVersionCache;
import org.wso2.carbon.appfactory.core.deploy.Artifact;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.queue.AppFactoryQueueException;
import org.wso2.carbon.appfactory.jenkins.build.JenkinsCISystemDriver;
import org.wso2.carbon.appfactory.tenant.roles.RoleBean;
import org.wso2.carbon.appfactory.utilities.dataservice.DSApplicationListener;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.email.sender.api.EmailSender;
import org.wso2.carbon.email.sender.api.EmailSenderConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.*;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ApplicationManagementService extends AbstractAdmin {

    private static final String EXCEPTION = "EXCEPTION";

    private static Log log = LogFactory.getLog(ApplicationManagementService.class);

    public static String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";

    public static UserApplicationCache userApplicationCache =
            UserApplicationCache.getUserApplicationCache();

    // Holds the activities successfully completed systems ->
    // ApplicationId+SystemName
    private static List<String> systemStatus = new ArrayList<String>();

    /**
     * Check the system activities has completed or not
     *
     * @param applicationSystemId ApplicationId+SystemName
     * @return
     */
    public boolean checkSystemStatus(String applicationSystemId)
            throws ApplicationManagementException {
        ArrayList<String> systems = null;
        String id = applicationSystemId.toUpperCase();
        if (systemStatus.contains(id)) {
            systemStatus.remove(id);
            return true;
        } else {
            String exception = id + EXCEPTION;
            if (systemStatus.contains(exception)) {
                systemStatus.remove(exception);
                throw new ApplicationManagementException();
            }
        }
        return false;
    }

    /**
     * This createApplication method is used for the create an application. When
     * call this method, it put to the queue.
     *
     * @param applicationName        Application name.
     * @param applicationKey         Key for the Application. This should be unique.
     * @param applicationDescription Description of the application.
     * @param applicationType        Type of the application. ex: war, jaxrs, jaxws ...
     * @param repositoryType         Type of the repository that should use. ex: svn, git
     * @param userName               Logged-in user name.
     */
    public void createApplication(String applicationName, String applicationKey,
                                  String applicationDescription, String applicationType,
                                  String repositoryType, String userName)
            throws ApplicationManagementException {

        ApplicationInfoBean applicationInfoBean = new ApplicationInfoBean();
        applicationInfoBean.setName(applicationName);
        applicationInfoBean.setApplicationKey(applicationKey);
        applicationInfoBean.setDescription(applicationDescription);
        applicationInfoBean.setApplicationType(applicationType);
        applicationInfoBean.setRepositoryType(repositoryType);
        applicationInfoBean.setOwnerUserName(userName);


        try {
            ApplicationCreator applicationCreator = ApplicationCreator.getInstance();
            applicationCreator.getExecutionEngine().getSynchQueue().put(applicationInfoBean);

            BamDataPublisher publisher = new BamDataPublisher();
            String tenantId = "" + Util.getRealmService().getBootstrapRealmConfiguration().getTenantId();
            publisher.PublishAppCreationEvent(applicationName, applicationKey, applicationDescription, applicationType, repositoryType, System.currentTimeMillis(), tenantId, userName);

        } catch (AppFactoryQueueException e) {
            String errorMsg =
                    "Error occured when adding an application in to queue, " +
                            e.getMessage();
            log.error(errorMsg, e);
            throw new ApplicationManagementException(errorMsg, e);
        }

    }

    public boolean addUserToApplication(String domainName,String applicationId, String userName, String[] roles)
            throws ApplicationManagementException {
        int tenantId = 0;
        try {
            tenantId=Util.getRealmService().getTenantManager().getTenantId(domainName);
        } catch (UserStoreException e) {
            String msg = "Error while getting tenant id for "+domainName;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        try {
            UserRealm realm = Util.getRealmService().getTenantUserRealm(tenantId);
            String [] carbonRoles=getCarbonRoles(roles,applicationId);
            String[] newRolesForUser = removeRolesUserAlreadyIn(userName,carbonRoles, realm);
            realm.getUserStoreManager().updateRoleListOfUser(userName, null, newRolesForUser);

            userApplicationCache.clearFromCache(userName);
            //clearRealmCache(applicationId);

        } catch (UserStoreException e) {
            String msg = "Error while adding user " + userName + " to application " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

        // Notify the event listener about the user addition
        Iterator<ApplicationEventsListener> appEventListeners =
                Util.getApplicationEventsListeners()
                        .iterator();
        try {
            Application app = ProjectUtils.getApplicationInfo(applicationId,domainName);
            UserInfo user = new UserInfo(userName, roles);
            while (appEventListeners.hasNext()) {
                ApplicationEventsListener listener = appEventListeners.next();
                listener.onUserAddition(app, user);
            }

        } catch (Exception ex) {

            if (ex.getCause() != null && ex.getCause().getCause() != null &&
                    ex.getCause().getCause().getMessage().trim().equals("User has already been taken")) {
                log.warn(ex.getCause().getCause().getMessage());
            } else {
                String errorMsg = "Unable to publish user addition event due to " + ex.getMessage();
                log.error(errorMsg, ex);
                if (ex.getMessage().equals("0")) {
                    return false;
                } else {
                    throw new ApplicationManagementException(errorMsg, ex);
                }
            }
        }
        // sending the notification email to the user
        try {
            AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
            String emailSend = config.getFirstProperty("EmailSend");
            if (emailSend.equals("true") && !roles[0].equals(AppFactoryConstants.APP_OWNER_ROLE)) {
                sendMail(domainName,applicationId, userName, roles, "invite-user-email-config.xml");
            }
        } catch (AppFactoryException e) {
            // TODO Auto-generated catch block
            String errorMsg = "Unable to notify user " + e.getMessage();
            log.error(errorMsg, e);
            throw new ApplicationManagementException(errorMsg, e);
        }

        return true;
    }

    private String[] getCarbonRoles(String[] aFRoles,String applicationId) {
       List<String> carbonRoles=new ArrayList<String>();
        for(String role:aFRoles){
            carbonRoles.add(getCarbonRole(applicationId,role));
        }
        return carbonRoles.toArray(new String[carbonRoles.size()]);
    }

    private String getCarbonRole(String appId,String roleName) {
        return (appId+"_"+roleName);
    }


    /**
     * Update the user roles of a user for a given application
     *
     * @param applicationId application identifier
     * @param userName      user name
     * @param rolesToDelete roles to be removed from present roles of the user
     * @param rolesToAdd    roles to be added to present roles of the user
     * @throws AppFactoryException
     * @throws ApplicationManagementException
     */
    public boolean updateUserOfApplication(String domainName,String applicationId, String userName,
                                           String[] rolesToDelete, String[] rolesToAdd, String tenantDomain)
            throws ApplicationManagementException,
            UserStoreException {

        String[] userRoles = getRolesOfUserPerApplication(applicationId, userName);
        List<String> finalRoles =
                getResultedRoles(applicationId, userName, rolesToDelete,
                        rolesToAdd);
        if (isAppOwner(userRoles)) {
            if (!finalRoles.contains(AppFactoryConstants.APP_OWNER_ROLE)) {
                finalRoles.add(AppFactoryConstants.APP_OWNER_ROLE);
            }
        }
        String[] finalRolesArray = finalRoles.toArray(new String[finalRoles.size()]);
        updateRolesOfUserForApplication(applicationId, userName, userRoles, finalRolesArray);


        // Notify the event listener about the user addition
        Iterator<ApplicationEventsListener> appEventListeners =
                Util.getApplicationEventsListeners()
                        .iterator();
        try {
            Application app = ProjectUtils.getApplicationInfo(applicationId,domainName);
            UserInfo user = new UserInfo(userName, finalRolesArray);
            while (appEventListeners.hasNext()) {
                ApplicationEventsListener listener = appEventListeners.next();
                listener.onUserUpdate(app, user);

            }
            return true;

        } catch (Exception ex) {

            if (ex.getCause() != null && ex.getCause().getCause() != null &&
                    ex.getCause().getCause().getMessage().trim().equals("User has already been taken")) {
                log.warn(ex.getCause().getCause().getMessage());
            } else {
                String errorMsg = "Unable to publish user update event due to " + ex.getMessage();
                log.error(errorMsg, ex);
                if (ex.getMessage().equals("0")) {
                    return false;
                } else {
                    throw new ApplicationManagementException(errorMsg, ex);
                }
            }
            return false;
        }

    }

    /**
     * Get the final roles of the user after deleting and adding roles
     *
     * @param applicationId application identifier
     * @param userName      user name
     * @param rolesToDelete roles to be removed from present roles of the user
     * @param rolesToAdd    roles to be added to present roles of the user
     * @return resulted roles of String Array
     */
    private List<String> getResultedRoles(String applicationId, String userName,
                                          String[] rolesToDelete, String[] rolesToAdd) {
        List<String> presentRoles = new LinkedList<String>();
        try {

            presentRoles =
                    new ArrayList<String>(
                            Arrays.asList(getRolesOfUserPerApplication(applicationId,
                                    userName)));
            if (presentRoles != null) {
                // do the logic to differentiate between toDelete and to Add
                // roles
                for (String role : rolesToDelete) {// remove roles from
                    // presentRoles

                    if (presentRoles.contains(new String(role)) && (!role.equals(""))) {
                        presentRoles.remove(role);
                    }
                }
                for (String role : rolesToAdd) {// add roles to presentRoles

                    if (!presentRoles.contains(new String(role)) && (!role.equals(""))) {
                        presentRoles.add(role);
                    }
                }

            }
        } catch (ApplicationManagementException e) {
            String msg =
                    "Error while evaluating resulted roles roles for user: " + userName +
                            " of application " + applicationId;
            log.error(msg, e);
        }
        return presentRoles;

    }

    // If user is going to be added to a role that he is already having, remove
    // that role from 'newRoles'
    private String[] removeRolesUserAlreadyIn(String userName, String[] newRoles, UserRealm realm)
            throws UserStoreException {

        ArrayList<String> newRolesArray = new ArrayList<String>();
        for (String newRole : newRoles) {
            newRolesArray.add(newRole);
        }

        String[] existingRoles = realm.getUserStoreManager().getRoleListOfUser(userName);
        if (existingRoles != null) {
            for (String role : existingRoles) {
                if (newRolesArray.contains(role)) {
                    newRolesArray.remove(role);
                }
            }
        }

        return newRolesArray.toArray(new String[newRolesArray.size()]);
    }

    public boolean updateRolesOfUserForApplication(String applicationId, String userName,
                                                   String[] rolesToDelete, String[] newRoles)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            UserRealm realm =getUserRealm();
            realm.getUserStoreManager().updateRoleListOfUser(userName, rolesToDelete, newRoles);
            userApplicationCache.clearFromCache(userName);
            return true;
        } catch (UserStoreException e) {
            String msg =
                    "Error while updating roles for user: " + userName + " of application " +
                            applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    public String[] getUsersOfApplication(String applicationId)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        ArrayList<String> userList = new ArrayList<String>();
        try {
            UserRealm realm =getUserRealm();
            String[] roles = realm.getUserStoreManager().getRoleNames();
            if (roles.length > 0) {
                for (String roleName : roles) {
                    if (!Util.getRealmService().getBootstrapRealmConfiguration()
                            .getEveryOneRoleName().equals(roleName)) {
                        String[] usersOfRole =
                                realm.getUserStoreManager()
                                        .getUserListOfRole(roleName);
                        if (usersOfRole != null && usersOfRole.length > 0) {
                            for (String userName : usersOfRole) {
                                if (!userList.contains(userName) &&
                                        !Util.getRealmService().getBootstrapRealmConfiguration()
                                                .getAdminUserName().equals(userName) &&
                                        !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
                                    userList.add(userName);
                                }
                            }
                        }
                    }

                }
            }
            return userList.toArray(new String[userList.size()]);
        } catch (UserStoreException e) {
            String msg = "Error while getting users of application " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    // TODO this method should be removed after changing the FE callers
    public UserRoleCount[] getUserListOfRole(String roleName) throws ApplicationManagementException {
        Map<String, UserRoleCount> tempUserMap = new HashMap<String, UserRoleCount>();
        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();

            for (Tenant tenant : tenants) {
                UserRealm realm = Util.getRealmService().getTenantUserRealm(tenant.getId());
                String[] userList = realm.getUserStoreManager().getUserListOfRole(roleName);

                if (userList != null && userList.length > 0) {
                    for (String userIdentifier : userList) {

                        UserRoleCount userRoleObj = tempUserMap.get(userIdentifier);
                        if (userRoleObj == null) {
                            String firstName =
                                    realm.getUserStoreManager()
                                            .getUserClaimValue(userIdentifier,
                                                    FIRST_NAME_CLAIM_URI, null);
                            String lastName =
                                    realm.getUserStoreManager()
                                            .getUserClaimValue(userIdentifier,
                                                    LAST_NAME_CLAIM_URI, null);

                            String fullName = firstName.concat(" ").concat(lastName);

                            userRoleObj = new UserRoleCount();
                            userRoleObj.setRoleName(userIdentifier);
                            userRoleObj.setFullName(fullName);
                            userRoleObj.setUserCount(0);
                        }
                        userRoleObj.setUserCount(userRoleObj.getUserCount() + 1);
                        tempUserMap.put(userIdentifier, userRoleObj);
                    }
                }
            }

        } catch (UserStoreException e) {
            String msg = "Error while getting user list of role";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

        UserRoleCount userRoleCount[];
        if (!tempUserMap.isEmpty()) {
            userRoleCount = new UserRoleCount[tempUserMap.keySet().size()];

            int counter = 0;
            for (String mapKey : tempUserMap.keySet()) {
                userRoleCount[counter++] = tempUserMap.get(mapKey);
            }
        } else {
            userRoleCount = new UserRoleCount[0];
        }

        return userRoleCount;
    }

    public boolean removeUserFromApplication(String domainName,String applicationId, String userName)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        String[] rolesOfUser = getRolesOfUserPerApplication(applicationId, userName);
        if (!isAppOwner(rolesOfUser)) {
            // if the app owner is deleted then application becomes in
            // accessible,to avoid this user is deleted if user is not the
            // appowner
            try {
                UserRealm realm =
                        Util.getRealmService()
                                .getTenantUserRealm(tenantManager.getTenantId(applicationId));
                // remove from the LDAP group of the app and relevant user roles
                updateRolesOfUserForApplication(applicationId, userName, rolesOfUser,
                        new String[]{});
                userApplicationCache.clearFromCache(userName);

            } catch (UserStoreException e) {
                String msg =
                        "Error while removing user " + userName + " from application " +
                                applicationId;
                log.error(msg, e);
                throw new ApplicationManagementException(msg, e);
            }

            // Notify listeners about removing the user from application.
            Iterator<ApplicationEventsListener> appEventListeners =
                    Util.getApplicationEventsListeners()
                            .iterator();
            try {
                Application app = ProjectUtils.getApplicationInfo(applicationId,domainName);

                /** Update the roles in this dto */
                UserInfo user = new UserInfo(userName);
                while (appEventListeners.hasNext()) {
                    ApplicationEventsListener listener = appEventListeners.next();
                    listener.onUserDeletion(app, user);
                }

            } catch (Exception ex) {
                String errorMsg = "Unable to publish user deletion event due to " + ex.getMessage();
                log.error(errorMsg, ex);
                throw new ApplicationManagementException(errorMsg, ex);
            }

            return true;
        } else {
            return false;
        }

    }

    public boolean revokeApplication(String domainName,String applicationId) throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        try {
            tenantManager.deleteTenant(tenantManager.getTenantId(applicationId));

        } catch (UserStoreException e) {
            String msg = "Error while revoking application " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

        // Notify listeners about the revoke
        Iterator<ApplicationEventsListener> appEventListeners =
                Util.getApplicationEventsListeners()
                        .iterator();

        try {
            Application application = ProjectUtils.getApplicationInfo(applicationId,domainName);
            while (appEventListeners.hasNext()) {
                ApplicationEventsListener listener = appEventListeners.next();
                listener.onRevoke(application);
            }
        } catch (AppFactoryException ex) {
            String errorMsg = "Unable to notify revoke application event due to " + ex.getMessage();
            log.error(errorMsg, ex);
            throw new ApplicationManagementException(errorMsg, ex);
        }

        return true;
    }

    public boolean isApplicationIdAvailable(String applicationKey)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        int tenantID;
        try {
            tenantID = tenantManager.getTenantId(applicationKey);
        } catch (UserStoreException e) {
            String msg = "Error while getting applicationKey " + applicationKey;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return tenantID < 0;
    }

    public UserInfoBean[] getUserInfo(String applicationId) throws ApplicationManagementException {

        String[] users = getUsersOfApplication(applicationId);
        ArrayList<UserInfoBean> userInfoList = new ArrayList<UserInfoBean>();
        if (users != null && users.length > 0) {
            for (int i = 0; i < users.length; i++) {
                try {
                    userInfoList.add(getUserInfoBean(users[i]));
                } catch (ApplicationManagementException e) {
                    String msg =
                            "Error while getting info for user " + users[i] +
                                    "\n Continue getting other users information";
                    log.error(msg, e);
                }
            }
        }
        return userInfoList.toArray(new UserInfoBean[userInfoList.size()]);
    }

    public UserInfoBean getUserInfoBean(String userName) throws ApplicationManagementException {
        return Util.getUserInfoBean(userName);
    }

    public String[] getAllApplications(String domainName,String userName) throws ApplicationManagementException {

      String apps[] = new String[0]; /*= userApplicationCache.getValueFromCache(userName);*/
        /*if (apps != null) {
            return apps;
        } else {
            apps = new String[0];
        }*/
        List<String> list;
        int tenantId = 0;
        try {
            tenantId=Util.getRealmService().getTenantManager().getTenantId(domainName);
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        UserStoreManager userStoreManager = null;
        try {
            userStoreManager=Util.getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UserStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            String[] roles = userStoreManager.getRoleListOfUser(userName);
            roles=getUniqueApplicationList(roles);
            list = Arrays.asList(roles);
        } catch (UserStoreException e) {
            String msg = "Error while getting all applications";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        if (!list.isEmpty()) {
            apps = list.toArray(new String[list.size()]);
        }
       // userApplicationCache.addToCache(userName, apps);
        return apps;
    }
    public String[] getAllApplications(String userName) throws ApplicationManagementException{
        //TODO(ajanthan):fix it properly
        throw new UnsupportedOperationException("Not supported yet");
    }

    private String[] getUniqueApplicationList(String[] tenantDomainStrs) {
        Set<String> appSet=new HashSet<String>();
        for(String role:tenantDomainStrs){

            if(!(role.indexOf("_")<0)){
                appSet.add(role.substring(0, role.lastIndexOf("_")));
            }
        }
        return appSet.toArray(new String[appSet.size()]);
    }

    public String[] getAllCreatedApplications() throws ApplicationManagementException {
        String apps[] = new String[0];
        List<String> list = new ArrayList<String>();
        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();

            for (Tenant tenant : tenants) {
                list.add(tenant.getDomain());
            }

        } catch (UserStoreException e) {
            String msg = "Error while getting all applications";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        if (!list.isEmpty()) {
            apps = list.toArray(new String[list.size()]);
        }
        return apps;
    }

    /**
     * Service method to get basic application information.
     * <br/>
     * <p>
     * Note:- this method does not retrieve the version information if the
     * application.
     * </p>
     *
     * @param applicationKey
     * @return {@link ApplicationInfoBean} without versions.
     * @throws AppFactoryException
     */
    public ApplicationInfoBean getBasicApplicationInfo(String domainName,String applicationKey)
            throws AppFactoryException {
        Application application = ProjectUtils.getApplicationInfo(applicationKey,domainName);
        ApplicationInfoBean applicaitonBean = new ApplicationInfoBean(application);
        return applicaitonBean;
    }

   /* public ApplicationInfoBean[] getAllVersionsOfApplications()
            throws ApplicationManagementException,
            AppFactoryException {
        ApplicationInfoBean[] arrApplicationInfo;
        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();

            if (tenants == null || tenants.length == 0) {
                arrApplicationInfo = new ApplicationInfoBean[0];
            } else {
                arrApplicationInfo = new ApplicationInfoBean[tenants.length];
            }

            int index = 0;
            for (Tenant tenant : tenants) {
                Application application = ProjectUtils.getApplicationInfo(tenant.getDomain(), "");          // TODO: need to fix
                ApplicationInfoBean applicationInfo = new ApplicationInfoBean();
                applicationInfo.setApplicationKey(tenant.getDomain());
                applicationInfo.setName(application.getName());
                Version[] versions = ProjectUtils.getVersions(tenant.getDomain());
                if (versions == null || versions.length == 0) {
                    applicationInfo.setVersions(new String[0]);
                } else {
                    String strVersions[] = new String[versions.length];
                    for (int i = 0; i < versions.length; i++) {
                        strVersions[i] = versions[i].getId();
                    }
                    applicationInfo.setVersions(strVersions);
                }

                arrApplicationInfo[index++] = applicationInfo;
            }

        } catch (UserStoreException e) {
            String msg = "Error while getting all applications";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

        return arrApplicationInfo;
    }*/

    public UserApplications[] getApplicationsOfUser(String roleName)
            throws ApplicationManagementException {
        Map<String, ArrayList<String>> tempUserMap = new HashMap<String, ArrayList<String>>();
        TenantManager manager = Util.getRealmService().getTenantManager();
        try {
            Tenant[] tenants = manager.getAllTenants();

            for (Tenant tenant : tenants) {
                UserRealm realm = Util.getRealmService().getTenantUserRealm(tenant.getId());
                String[] userList = realm.getUserStoreManager().getUserListOfRole(roleName);

                if (userList != null && userList.length > 0) {
                    for (String userIdentifier : userList) {
                        ArrayList<String> elementList = tempUserMap.get(userIdentifier);
                        if (elementList == null) {
                            elementList = new ArrayList<String>();
                        }
                        elementList.add(tenant.getDomain());
                        tempUserMap.put(userIdentifier, elementList);
                    }
                }
            }

        } catch (UserStoreException e) {
            String msg = "Error while getting all users of applications";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

        UserApplications arrUserApplications[];
        if (!tempUserMap.isEmpty()) {
            arrUserApplications = new UserApplications[tempUserMap.keySet().size()];
            int index = 0;
            for (String mapKey : tempUserMap.keySet()) {
                UserApplications userApplication = new UserApplications();
                userApplication.setUserNam(mapKey);
                userApplication.setApplications(tempUserMap.get(mapKey)
                        .toArray(new String[tempUserMap.get(mapKey)
                                .size()]));
                arrUserApplications[index++] = userApplication;
            }
        } else {
            arrUserApplications = new UserApplications[0];
        }

        return arrUserApplications;
    }

    public String[] getRolesOfUserPerApplication(String appId, String userName)
            throws ApplicationManagementException {
        TenantManager tenantManager = Util.getRealmService().getTenantManager();
        org.wso2.carbon.user.api.UserStoreManager userStoreManager;
        ArrayList<String> roleList = new ArrayList<String>();
        String roles[];
        try {
            UserRealm realm =
                    Util.getRealmService()
                            .getTenantUserRealm(tenantManager.getTenantId(appId));
            userStoreManager = realm.getUserStoreManager();
            roles = userStoreManager.getRoleListOfUser(userName);
            for (String role : roles) {
                if (!Util.getRealmService().getBootstrapRealmConfiguration().getEveryOneRoleName()
                        .equals(role)) {
                    roleList.add(role);
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error while getting role of the user " + userName;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
        return roleList.toArray(new String[roleList.size()]);
    }

    public String getStage(String applicationId, String version)
            throws ApplicationManagementException {
        try {
            return new RxtManager().getStage(applicationId, version);
        } catch (AppFactoryException e) {
            String msg = "Unable to get stage for " + applicationId + "and version : " + version;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    public void publishApplicationCreation(String domainName,String applicationId)
            throws ApplicationManagementException {
        // New application is created successfully so now time to clear realm in
        // cache to reload
        // the new realm with updated permissions
        clearRealmCache(applicationId);

        Iterator<ApplicationEventsListener> appEventListeners =
                Util.getApplicationEventsListeners()
                        .iterator();
        ApplicationEventsListener listener = null;
        try {
            Application application = ProjectUtils.getApplicationInfo(applicationId,domainName);
            if (application == null) {
                String errorMsg =
                        String.format("Unable to load application information for id %s",
                                applicationId);
                throw new ApplicationManagementException(errorMsg);
            }

            while (appEventListeners.hasNext()) {
                listener = appEventListeners.next();

                if (listener instanceof DSApplicationListener) {
                    if ("dbs".equals(application.getType())) {
                        listener.onCreation(application, domainName);
                        systemStatus.add((applicationId + listener.getIdentifier()).toUpperCase());
                    }
                } else {
                    listener.onCreation(application, domainName);
                    systemStatus.add((applicationId + listener.getIdentifier()).toUpperCase());
                }
            }
        } catch (AppFactoryException ex) {
            systemStatus.add((applicationId + listener.getIdentifier()).toUpperCase() + EXCEPTION);
            String errorMsg = "Unable to publish application creation due to : " + ex.getMessage();
            log.error(errorMsg, ex);
            throw new ApplicationManagementException(errorMsg, ex);
        }
    }

    public void publishApplicationVersionCreation(String domainName,String applicationId, String sourceVersion,
                                                  String targetVersion)
            throws ApplicationManagementException {
        try {

            Iterator<ApplicationEventsListener> appEventListeners =
                    Util.getApplicationEventsListeners()
                            .iterator();

            Application application = ProjectUtils.getApplicationInfo(applicationId,domainName);

            Version[] versions = ProjectUtils.getVersions(applicationId);

            // find the versions.
            Version source = null;
            Version target = null;
            for (Version v : versions) {
                if (v.getId().equals(sourceVersion)) {
                    source = v;
                }

                if (v.getId().equals(targetVersion)) {
                    target = v;
                }

                if (source != null && target != null) {
                    // both version are found. no need to traverse more
                    break;
                }

            }

            while (appEventListeners.hasNext()) {
                ApplicationEventsListener listener = appEventListeners.next();
                listener.onVersionCreation(application, source, target);
            }

        } catch (AppFactoryException ex) {
            String errorMsg = "Unable to publish version creation due to " + ex.getMessage();
            log.error(errorMsg, ex);
            throw new ApplicationManagementException(errorMsg, ex);
        }
    }

    /**
     * Service method to make the application related to given
     * {@code applicationId} auto build.
     *
     * @param applicationId
     * @param stage
     * @param version
     * @param isAutoBuildable
     * @throws ApplicationManagementException
     */
    public void publishSetApplicationAutoBuild(String applicationId, String stage, String version,
                                               boolean isAutoBuildable, String tenantDomain)
            throws ApplicationManagementException {
        log.info("Auto build change event recieved for : " + applicationId + " " + " Version : " +
                version + " stage :" + stage + " isAutoBuildable :" + isAutoBuildable);
        updateRxtWithBuildStatus(applicationId, stage, version, isAutoBuildable);

        try {
            JenkinsCISystemDriver jenkinsCISystemDriver =
                    (JenkinsCISystemDriver) Util.getContinuousIntegrationSystemDriver();
            // TODO this from configuration
            int pollingPeriod = 6;

            jenkinsCISystemDriver.setJobAutoBuildable(applicationId, version, isAutoBuildable,
                    pollingPeriod, tenantDomain);

            // Clear the cache
            AppVersionCache.getAppVersionCache().clearCacheForAppId(applicationId);

            log.info("Application : " + applicationId +
                    " sccessfully configured for auto building " + isAutoBuildable);
        } catch (AppFactoryException e) {
            String msg = "Error occured while updating jenkins configuration";
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        }

    }

    /**
     * Service method to make the application related to given
     * {@code applicationId} auto deploy.
     *
     * @param applicationId
     * @param stage
     * @param version
     * @param isAutoDeployable
     * @throws ApplicationManagementException
     */
    public void publishSetApplicationAutoDeploy(String applicationId, String stage, String version,
                                                boolean isAutoDeployable)
            throws ApplicationManagementException {
        log.info("Auto deploy change event recieved for : " + applicationId + " " + " Version : " +
                version + " stage :" + stage + " isAutoBuildable :" + isAutoDeployable);
        updateRxtWithDeplymentStatus(applicationId, stage, version, isAutoDeployable);
        try {
            JenkinsCISystemDriver jenkinsCISystemDriver =
                    (JenkinsCISystemDriver) Util.getContinuousIntegrationSystemDriver();

            jenkinsCISystemDriver.setJobAutoDeployable(applicationId, version, isAutoDeployable);

            // Clear the cache
            AppVersionCache.getAppVersionCache().clearCacheForAppId(applicationId);

            log.info("Application : " + applicationId + " sccessfully configured for auto deploy " +
                    isAutoDeployable);
        } catch (AppFactoryException e) {
            String msg = "Error occured while updating jenkins configuration";
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        }
    }

    /**
     * Updates the rxt registry with given auto build information.
     *
     * @param applicationId
     * @param stage
     * @param version
     * @param isAutoBuildable
     * @throws ApplicationManagementException
     */
    private void updateRxtWithBuildStatus(String applicationId, String stage, String version,
                                          boolean isAutoBuildable)
            throws ApplicationManagementException {
        RxtManager rxtManager = new RxtManager();
        try {
            rxtManager.updateAppVersionRxt(applicationId, version, "appversion_isAutoBuild",
                    String.valueOf(isAutoBuildable));
            log.debug(" Rtx updated successfully for : " + applicationId + " " + " Version : " +
                    version + " stage :" + stage + " isAutoBuildable :" + isAutoBuildable);

        } catch (AppFactoryException e) {
            String msg = "Error occured while updating the rxt with auto-build status";
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        }
    }

    /**
     * Updates the rxt registry with given auto deploy information.
     *
     * @param applicationId
     * @param stage
     * @param version
     * @param isAutoDeployable
     * @throws ApplicationManagementException
     */
    private void updateRxtWithDeplymentStatus(String applicationId, String stage, String version,
                                              boolean isAutoDeployable)
            throws ApplicationManagementException {
        RxtManager rxtManager = new RxtManager();
        try {
            rxtManager.updateAppVersionRxt(applicationId, version,
                    "appversion_isAutoDeploy",
                    String.valueOf(isAutoDeployable));
            log.debug(" Rtx updated successfully for : " + applicationId + " " + " Version : " +
                    version + " stage :" + stage + " isAutoDeployable :" + isAutoDeployable);

        } catch (AppFactoryException e) {
            String msg = "Error occured while updating the rxt with auto-build status";
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        }
    }

    public void publishApplicationAutoDeploymentChange(String domainName,String applicationId,
                                                       String previousVersion, String nextVersion,
                                                       String versionStage)
            throws ApplicationManagementException {

        JenkinsCISystemDriver jenkinsCISystemDriver =
                (JenkinsCISystemDriver) Util.getContinuousIntegrationSystemDriver();

        int pollingPeriod = 0;

        try {

            Application application = ProjectUtils.getApplicationInfo(applicationId,domainName);

            Version[] versions = ProjectUtils.getVersions(applicationId);

            // find the versions.
            /*
             * Version previous = null;
             * Version next = null;
             * if (!previousVersion.trim().equals("") &&
             * !nextVersion.trim().equals("")) {
             * for (Version v : versions) {
             * if (v.getId().equals(previousVersion)) {
             * previous = v;
             * }
             * 
             * if (v.getId().equals(nextVersion)) {
             * next = v;
             * }
             * 
             * if (previous != null && next != null) {
             * // both version are found. no need to traverse more
             * break;
             * }
             * 
             * }
             * } else {
             * for (Version v : versions) {
             * if (v.getId().equals(previousVersion)) {
             * previous = v;
             * }
             * }
             * 
             * }
             */

            log.info("AutoDeployment Version Change event recieved for : " + application.getId() +
                    " " + application.getName() + " From Version : " + previousVersion +
                    " To Version : " + nextVersion);

            if ((!previousVersion.equals(null)) && (!previousVersion.equals(""))) {
                jenkinsCISystemDriver.editADJobConfiguration(applicationId, previousVersion,
                        "removeAD", pollingPeriod);

            }
            if ((!nextVersion.equals(null)) && (!nextVersion.equals(""))) {
                AppFactoryConfiguration configuration = Util.getConfiguration();
                pollingPeriod =
                        Integer.parseInt(configuration.getFirstProperty("ApplicationDeployment.DeploymentStage." +
                                versionStage +
                                ".AutomaticDeployment.PollingPeriod"));
                jenkinsCISystemDriver.editADJobConfiguration(applicationId, nextVersion, "addAD",
                        pollingPeriod);

            }

        } catch (AppFactoryException ex) {
            String errorMsg =
                    "Unable to publish Auto Deoployment State Change due to  " +
                            ex.getMessage();
            log.error(errorMsg, ex);
            throw new ApplicationManagementException(errorMsg, ex);
        }

    }

    private void clearRealmCache(String applicationKey) throws ApplicationManagementException {
        RealmService realmService = Util.getRealmService();
        int tenantID;
        try {
            tenantID = Util.getRealmService().getTenantManager().getTenantId(applicationKey);
            realmService.clearCachedUserRealm(tenantID);
        } catch (UserStoreException e) {
            String errorMsg =
                    "Unable to clear user realm cache for tenant id  " + applicationKey +
                            " due to : " + e.getMessage();
            log.error(errorMsg, e);
            throw new ApplicationManagementException(errorMsg, e);
        }
    }

    public boolean sendMail(String domainName,String applicationId, String userName, String roles[], String config)
            throws ApplicationManagementException {
        EmailSender sender = new EmailSender(loadEmailSenderConfiguration(config));
        try {
            Application currentApp = ProjectUtils.getApplicationInfo(applicationId,domainName);
            String applicationName = currentApp.getName();
            String applicationDescription = currentApp.getDescription();
            Map<String, String> userParams = new HashMap<String, String>();
            userParams.put("applicationId", applicationId);
            userParams.put("userName", userName);
            userParams.put("roles", roles[0]);
            userParams.put("roleTask", loadRoleTask(config, roles[0]));
            userParams.put("applicationName", applicationName);
            URLCodec codec = new URLCodec();
            userParams.put("applicationNameEncoded", codec.encode(applicationName));
            if (applicationDescription != null && !applicationDescription.equals("")) {
                userParams.put("applicationDescription", applicationDescription);
            } else {
                userParams.put("applicationDescription", "");
            }

            sender.sendEmail(userName, userParams);

        } catch (Exception e) {
            String msg = "Email sending is failed for  " + userName;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);

        }
        return true;

    }

    private String loadRoleTask(String configFile, String role) throws FileNotFoundException,
            XMLStreamException, JaxenException {
        String configFilePath =
                CarbonUtils.getCarbonConfigDirPath() + File.separator + "email" +
                        File.separator + configFile;
        OMElement element = new StAXOMBuilder(configFilePath).getDocumentElement();
        AXIOMXPath xpathExpression = new AXIOMXPath("/configuration/userParams/roleTask/" + role);
        OMElement roleNode = (OMElement) xpathExpression.selectSingleNode(element);
        return roleNode.getText();
    }

    private EmailSenderConfiguration loadEmailSenderConfiguration(String configFile) {
        String configFilePath =
                CarbonUtils.getCarbonConfigDirPath() + File.separator + "email" +
                        File.separator + configFile;
        return EmailSenderConfiguration.loadEmailSenderConfiguration(configFilePath);
    }

    /**
     * check whether the user is app owner
     *
     * @param roles - list of roles of the user
     * @return - true if user is app owner
     */
    private boolean isAppOwner(String[] roles) {
        boolean result = false;
        if (roles.length > 0) {
            for (String role : roles) {
                if (role.equals(AppFactoryConstants.APP_OWNER_ROLE)) {
                    result = true;
                    break;
                }
            }
        }

        return result;

    }

    public Artifact[] getAllVersionsOfApplication(String applicationId) throws AppFactoryException {
        AppVersionCache cache = AppVersionCache.getAppVersionCache();
        Artifact[] artifacts = cache.getAppVersions(applicationId);
        if (artifacts != null) {
            if (log.isDebugEnabled()) {
                log.debug("*** Retrieved all versions from cache " + applicationId);
            }
            return artifacts;
        }
        RxtManager rxtManager = new RxtManager();
        try {
            List<Artifact> artifactsList = rxtManager.getAppVersionRxtForApplication(applicationId);
            artifacts = artifactsList.toArray(new Artifact[artifactsList.size()]);
            cache.addToCache(applicationId, artifacts);
            if (log.isDebugEnabled()) {
                log.debug("*** Added all versions to cache " + applicationId);
            }
            return artifacts;
        } catch (AppFactoryException e) {
            log.error("Error while retrieving artifat information from rxt");
            throw new AppFactoryException(e.getMessage());
        } catch (RegistryException e) {
            log.error("Error while retrieving artifat information from rxt");
            throw new AppFactoryException(e.getMessage());
        }
    }

    public static String getFullyQualifiedDbUsername(String username, String applicationKey) {

        byte[] bytes = getByteArray(applicationKey.hashCode());
        return username + "_" + Base64.encode(bytes);
    }

    private static byte[] getByteArray(int value) {
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
  public boolean createDefaultRoles(String domainName,String applicationId,String appOwner) throws ApplicationManagementException{
      List<RoleBean> roleBeanList = initRoleBean();
      int tenantId = 0;
      try {
          tenantId=Util.getRealmService().getTenantManager().getTenantId(domainName);
      } catch (UserStoreException e) {
          e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
      try {
          String[] users={appOwner};
          UserStoreManager userStoreManager =
                  Util.getRealmService()
                          .getTenantUserRealm(tenantId)
                          .getUserStoreManager();
          for (RoleBean roleBean : roleBeanList) {
              if (!userStoreManager.isExistingRole(roleBean.getRoleName())) {
                  userStoreManager.addRole(getCarbonRole(applicationId, roleBean.getRoleName()),users
                          ,
                          roleBean.getPermissions().
                                  toArray(new Permission[roleBean.getPermissions().
                                          size()]));
              }
          }
      } catch (UserStoreException e) {
          String message =
                  "Failed to create default roles of tenant:" +
                          domainName;
          log.error(message,e);
          throw new ApplicationManagementException(message, e);
      }
      return true;
  }
    private List<RoleBean> initRoleBean() throws ApplicationManagementException {
         List<RoleBean> roleBeanList = null;
        roleBeanList = new ArrayList<RoleBean>();
        try {
            AppFactoryConfiguration configuration = Util.getConfiguration();
            String[] roles = configuration.getProperties("ApplicationRoles.Role");
            String adminUser =
                    Util.getRealmService().getBootstrapRealm().getRealmConfiguration()
                            .getAdminUserName();
            for (String role : roles) {
                String resourceIdString =
                        configuration.getFirstProperty("ApplicationRoles.Role." +
                                role + ".Permission");
                String[] resourceIds = resourceIdString.split(",");
                RoleBean roleBean = new RoleBean(role.trim());
                roleBean.addUser(adminUser);
                for (String resourceId : resourceIds) {
                    Permission permission =
                            new Permission(resourceId.trim(),
                                    CarbonConstants.UI_PERMISSION_ACTION);
                    roleBean.addPermission(permission);
                }
                roleBeanList.add(roleBean);
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            String message = "Failed to read default roles from appfactory configuration.";
            log.error(message);
            throw new ApplicationManagementException(message, e);
        }
        return roleBeanList;
    }
}