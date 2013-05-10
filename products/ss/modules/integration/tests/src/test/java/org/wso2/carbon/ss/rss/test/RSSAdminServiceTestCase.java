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
package org.wso2.carbon.ss.rss.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminDataSourceException;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminStub;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceInfo;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSManagerExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminStub;
import org.wso2.carbon.rssmanager.ui.stub.types.*;
import org.wso2.carbon.ss.RSSTestConstants;
import org.wso2.carbon.ss.RSSTestUtil;
import org.wso2.carbon.ss.util.RSSTestHelper;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class RSSAdminServiceTestCase extends RSSBaseTestCase {

    private RSSAdminStub rssAdminStub;
    private NDataSourceAdminStub nDataSourceAdminStub;

    @BeforeClass(alwaysRun = true)
    public void initSetup() throws Exception {
        super.initEnvironment();

        String rssManagerAdminServiceEpr = this.getEnvironment().getBackEndUrl() +
                RSSTestConstants.EnvironmentConfigurations.RSS_ADMIN_SERVICE;
        this.rssAdminStub = new RSSAdminStub(rssManagerAdminServiceEpr);
        AuthenticateStub.authenticateStub(this.getEnvironment().getSessionCookie(), rssAdminStub);

        String ndataSourceAdminServiceEpr = this.getEnvironment().getBackEndUrl() +
                RSSTestConstants.EnvironmentConfigurations.NDATASOURCE_ADMIN_SERVICE;
        this.nDataSourceAdminStub = new NDataSourceAdminStub(ndataSourceAdminServiceEpr);
        AuthenticateStub.authenticateStub(this.getEnvironment().getSessionCookie(),
                nDataSourceAdminStub);
    }

    @Test(description = "Create database")
    public void createDatabase() throws Exception {
        try {
            boolean status = this.getRSSAdminStub().isDatabaseExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
            assertFalse(status, "Database with the name of sample database already exists");

            Database database = new Database();
            database.setName(RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
            database.setRssInstanceName(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM);
            rssAdminStub.createDatabase(database);

            status = this.getRSSAdminStub().isDatabaseExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
            assertFalse(status, "Sample database hasn't been created properly");
            assertTrue(true);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while creating sample database", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while creating sample database", e);
        }
    }

    @Test(dependsOnMethods = {"createDatabase", "createDatabaseUser"}, description = "Drop database")
    public void dropDatabase() throws Exception {
        try {
            boolean status = this.getRSSAdminStub().isDatabaseExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
            assertTrue(status, "Database already exists");

            this.getRSSAdminStub().dropDatabase(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);

            status = this.getRSSAdminStub().isDatabaseExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
            assertFalse(status, "Database hasn't been created properly");
            assertTrue(true);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while dropping sample database", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while dropping sample database", e);
        }
    }

    @Test
    public void getRSSInstanceList() throws Exception {
        try {
            this.getRSSAdminStub().getRSSInstances();
            assertTrue(true);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while retrieving the list of RSS instances", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while retrieving the list of RSS instances", e);
        }
    }

    @Test
    public void getDatabasesList() throws Exception {
        try {
            this.getRSSAdminStub().getDatabases();
            assertTrue(true);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while retrieving the list of databases", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while retrieving the list of databases", e);
        }
    }

    @Test(dependsOnMethods = "createDatabase", description = "Create database user")
    public void createDatabaseUser() throws Exception {
        try {
            boolean status = this.getRSSAdminStub().isDatabaseUserExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
            assertFalse(status, "Sample database user already exists");

            DatabaseUser databaseUser = new DatabaseUser();
            databaseUser.setUsername(
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
            databaseUser.setPassword(
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USER_PASSWORD);
            databaseUser.setRssInstanceName(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM);
            this.getRSSAdminStub().createDatabaseUser(databaseUser);

            status = this.getRSSAdminStub().isDatabaseUserExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
            assertFalse(status, "Sample database user hasn't been created properly");
            assertTrue(true);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while creating sample database user", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while creating sample database user", e);
        }
    }

    @Test(dependsOnMethods = {"attachDatabaseUser", "createDatabaseUser"}, description = "Edit database user")
    public void editDatabaseUser() throws RSSAdminRSSManagerExceptionException, RemoteException {
        DatabaseUser user = new DatabaseUser();
        user.setUsername(databaseUserName);
        user.setPassword(databaseUserPassword);
        user.setRssInstanceName(rssInstanceName);


        rssAdminStub.editDatabaseUserPrivileges(privileges, user, databaseName);
        privileges = rssAdminStub.getUserDatabasePermissions(rssInstanceName, databaseName, databaseUserName);
        DatabaseUserMetaData databaseUserMetaData = rssAdminStub.getDatabaseUser(rssInstanceName, databaseUserName);
        assertEquals(privileges.getSelectPriv(), "Y");
        assertEquals(privileges.getInsertPriv(), "Y");
        assertEquals(privileges.getIndexPriv(), "N");
        assertEquals(privileges.getAlterPriv(), "N");

    }

    @Test(dependsOnMethods = {"createDatabase", "createDatabaseUser"},
            description = "Drop database user")
    public void dropDatabaseUser() throws Exception {
        try {
            boolean status = this.getRSSAdminStub().isDatabaseUserExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
            assertTrue(status, "Database user already exists");

            this.getRSSAdminStub().dropDatabaseUser(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);

            status = this.getRSSAdminStub().isDatabaseUserExist(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
            assertFalse(status, "Database hasn't been dropped properly");
            assertTrue(true);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while dropping sample database user", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while dropping sample database user", e);
        }
    }

    @Test(description = "Create privilege template")
    public void createPrivilegeTemplate() throws Exception {
        try {
            boolean status =
                    this.getRSSAdminStub().isDatabasePrivilegesTemplateExist(
                            RSSTestConstants.SampleConfigurations.SAMPLE_PRIVILEGE_TEMPLATE_NAME);
            assertFalse(status, "Sample database privilege template already exists");

            DatabasePrivilegeTemplate template = RSSTestUtil.createSamplePrivilegeTemplate();
            this.getRSSAdminStub().createDatabasePrivilegesTemplate(template);

            status =
                    this.getRSSAdminStub().isDatabasePrivilegesTemplateExist(
                            RSSTestConstants.SampleConfigurations.SAMPLE_PRIVILEGE_TEMPLATE_NAME);
            assertTrue(status, "Sample database privilege template hasn't been created properly");

            DatabasePrivilegeTemplate createdTemplate =
                    this.getRSSAdminStub().getDatabasePrivilegesTemplate(
                            RSSTestConstants.SampleConfigurations.SAMPLE_PRIVILEGE_TEMPLATE_NAME);

            this.checkPrivileges(template, createdTemplate);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while creating sample database privilege " +
                    "template", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while creating sample database privilege " +
                    "template", e);
        }
    }

    @Test(dependsOnMethods = {"attachDatabaseUser"}, description = "Edit privilege template")
    public void editPrivilegeTemplate()
            throws RSSAdminRSSManagerExceptionException, RemoteException {
        assertTrue(isDatabasePrivilegeTemplateExists(privilegeTemplateName), "Database privilege template is not exists");
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
        privileges.setSelectPriv("Y");
        privileges.setInsertPriv("Y");
        privileges.setUpdatePriv("N");
        privileges.setDeletePriv("Y");
        privileges.setCreatePriv("Y");
        privileges.setDropPriv("Y");
        privileges.setGrantPriv("Y");
        privileges.setReferencesPriv("Y");
        privileges.setIndexPriv("Y");
        privileges.setAlterPriv("Y");
        privileges.setCreateTmpTablePriv("N");
        privileges.setLockTablesPriv("N");
        privileges.setCreateViewPriv("N");
        privileges.setShowViewPriv("N");
        privileges.setCreateRoutinePriv("N");
        privileges.setAlterRoutinePriv("N");
        privileges.setExecutePriv("N");
        privileges.setEventPriv("N");
        privileges.setTriggerPriv("N");
        DatabasePrivilegeTemplate template = new DatabasePrivilegeTemplate();
        template.setName(privilegeTemplateName);
        template.setPrivileges(privileges);
        rssAdminStub.editDatabasePrivilegesTemplate(template);
        assertTrue(isDatabasePrivilegeTemplateExists(privilegeTemplateName), "Edited database privilege template is not in the list");
        DatabasePrivilegeTemplate databasePrivilegeTemplate = rssAdminStub.getDatabasePrivilegesTemplate(privilegeTemplateName);
        privileges = databasePrivilegeTemplate.getPrivileges();
        assertEquals(privileges.getSelectPriv(), "Y");
        assertEquals(privileges.getInsertPriv(), "Y");
        assertEquals(privileges.getUpdatePriv(), "N");
        assertEquals(privileges.getDeletePriv(), "Y");
        assertEquals(privileges.getCreatePriv(), "Y");
        assertEquals(privileges.getDropPriv(), "Y");
        assertEquals(privileges.getGrantPriv(), "Y");
        assertEquals(privileges.getReferencesPriv(), "Y");
        assertEquals(privileges.getIndexPriv(), "Y");
        assertEquals(privileges.getAlterPriv(), "Y");
        assertEquals(privileges.getCreateTmpTablePriv(), "N");
        assertEquals(privileges.getLockTablesPriv(), "N");
        assertEquals(privileges.getCreateViewPriv(), "N");
        assertEquals(privileges.getShowViewPriv(), "N");
        assertEquals(privileges.getCreateRoutinePriv(), "N");
        assertEquals(privileges.getExecutePriv(), "N");
        assertEquals(privileges.getEventPriv(), "N");
        assertEquals(privileges.getTriggerPriv(), "N");
    }

    @Test(dependsOnMethods = {"createDatabase", "createDatabaseUser"}, description = "Drop privilege template")
    public void dropDatabasePrivilegeTemplate() throws Exception {
        try {
            this.getRSSAdminStub().dropDatabasePrivilegesTemplate(
                    RSSTestConstants.SampleConfigurations.SAMPLE_PRIVILEGE_TEMPLATE_NAME);
            assertTrue(true);
        } catch (RemoteException e) {
            throw new Exception("Error occurred while dropping sample database privilege " +
                    "template", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while dropping sample database privilege " +
                    "template", e);
        }
    }

    @Test(dependsOnMethods = {"createDatabase", "createDatabaseUser", "createPrivilegeTemplate"},
            description = "Attach database user")
    public void attachDatabaseUser() throws Exception {
        String[] before =
                this.getRSSAdminStub().getAvailableUsersToAttachToDatabase(
                        RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);

        boolean status =
                this.isDatabaseUserAlreadyAttached(
                        RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
        assertTrue(status, "Database user not available to attach to database");

        this.getRSSAdminStub().attachUserToDatabase(
                RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME,
                RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME,
                RSSTestConstants.SampleConfigurations.SAMPLE_PRIVILEGE_TEMPLATE_NAME);

        String[] after =
                this.getRSSAdminStub().getAvailableUsersToAttachToDatabase(
                        RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
        if (after == null) {
            after = new String[0];
        }
        assertTrue((before.length - 1) == after.length);

        status =
                this.isDatabaseUserAlreadyAttached(
                        RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
        assertFalse(status, "Database user still available for attach after attached to " +
                "the database");
    }

    @Test(dependsOnMethods = {"createDatabase", "createDatabaseUser", "createDataSource"}, description = "Drop attach database user")
    public void dropAttachedDatabaseUser()
            throws RSSAdminRSSManagerExceptionException, RemoteException {
        String[] attachedDatabaseUsers =
                rssAdminStub.getUsersAttachedToDatabase(rssInstanceName, databaseName);
        int availableUserCount =
                rssAdminStub.getAvailableUsersToAttachToDatabase(rssInstanceName, databaseName).length;
        boolean contains = false;
        for (String temp : attachedDatabaseUsers) {
            if (databaseUserName.equals(temp)) {
                contains = true;
            }
        }
        assertTrue(contains, "Database user not attached to the database");
        rssAdminStub.detachUserFromDatabase(rssInstanceName, databaseName, databaseUserName);
        assertTrue((availableUserCount + 1) ==
                rssAdminStub.getAvailableUsersToAttachToDatabase(rssInstanceName, databaseName).length);
        contains = false;
        for (String temp : rssAdminStub.getUsersAttachedToDatabase(rssInstanceName, databaseName)) {
            if (databaseUserName.equals(temp)) {
                contains = true;
            }
        }
        assertFalse(contains, "Database user still attached to the database");
        contains = false;
        for (String temp :
                rssAdminStub.getAvailableUsersToAttachToDatabase(rssInstanceName, databaseName)) {
            if (databaseUserName.equals(temp)) {
                contains = true;
            }
        }
        assertTrue(contains, "Database user not available to attach after de-attached");
    }

    @Test(dependsOnMethods = {"createDatabase", "createDatabaseUser", "attachDatabaseUser"},
            description = "Create data source")
    public void createDataSource() throws Exception {
        try {
            UserDatabaseEntry entry = new UserDatabaseEntry();
            entry.setRssInstanceName(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM);
            entry.setDatabaseName(RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
            entry.setUsername(RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
            this.getRSSAdminStub().createCarbonDataSource(entry);

            WSDataSourceInfo[] allDataSources = this.getNDataSourceAdminStub().getAllDataSources();
            boolean isCreated = false;
            for (WSDataSourceInfo wsDataSourceInfo : allDataSources) {
                if (entry.getDatabaseName().equals(wsDataSourceInfo.getDsMetaInfo().getName())) {
                    isCreated = true;
                }
            }
            assertTrue(isCreated, "Data source has not been created");
        } catch (RemoteException e) {
            throw new Exception("Error occurred while creating a sample datasource", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while creating a sample datasource", e);
        } catch (NDataSourceAdminDataSourceException e) {
            throw new Exception("Error occurred while creating a sample datasource", e);
        }
    }

    @Test(dependsOnMethods = {"createDatabase", "createDatabaseUser"}, description = "Create rss instance")
    public void createRSSInstance() throws RSSAdminRSSManagerExceptionException, RemoteException {
        this.getRSSAdminStub().createRSSInstance(RSSTestUtil.createRSSInstance());

        RSSInstanceMetaData retrievedRSSInstance = this.getRSSAdminStub().getRSSInstance(
                RSSTestConstants.SampleConfigurations.SAMPLE_RSS_INSTANCE_NAME);
        assertTrue((retrievedRSSInstance != null &&
                RSSTestConstants.SampleConfigurations.SAMPLE_RSS_INSTANCE_NAME.equals(
                        retrievedRSSInstance.getName())),
                "Sample RSS instance hasn't been created");
    }

    @Test(dependsOnMethods = "createRSSInstance", description = "Edit rss instance")
    public void editRSSInstance() throws RSSAdminRSSManagerExceptionException, RemoteException {
        RSSInstance before = RSSTestUtil.createRSSInstance();
        this.getRSSAdminStub().editRSSInstance(RSSTestUtil.modifyRSSInstanceConfiguration(before));

        RSSInstanceMetaData after = this.getRSSAdminStub().getRSSInstance(before.getName());

        assertFalse(before.getServerURL().equals(after.getServerUrl()),
                "Sample RSS instance URL hasn't been edited");
        assertFalse(before.getInstanceType().equals(after.getInstanceType()),
                "Sample RSS instance type hasn't been edited");
        assertFalse(before.getServerCategory().equals(after.getServerCategory()),
                "Sample RSS instance server category hasn't been edited");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        boolean status =
                this.getRSSAdminStub().isDatabaseUserExist(
                        RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
        if (status) {
            this.getRSSAdminStub().dropDatabaseUser(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_USERNAME);
        }

        status =
                this.getRSSAdminStub().isDatabaseExist(
                        RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                        RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
        if (status) {
            this.getRSSAdminStub().dropDatabase(
                    RSSTestConstants.EnvironmentConfigurations.RSS_MANAGEMENT_MODE_SYSTEM,
                    RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
        }

        status =
                this.getRSSAdminStub().isDatabasePrivilegesTemplateExist(
                        RSSTestConstants.SampleConfigurations.SAMPLE_PRIVILEGE_TEMPLATE_NAME);
        if (status) {
            this.getRSSAdminStub().dropDatabasePrivilegesTemplate(
                    RSSTestConstants.SampleConfigurations.SAMPLE_PRIVILEGE_TEMPLATE_NAME);
        }

        RSSInstanceMetaData rssInstance =
                this.getRSSAdminStub().getRSSInstance(
                        RSSTestConstants.SampleConfigurations.SAMPLE_RSS_INSTANCE_NAME);
        if (rssInstance != null) {
            this.getRSSAdminStub().dropRSSInstance(
                    RSSTestConstants.SampleConfigurations.SAMPLE_RSS_INSTANCE_NAME);
        }

        WSDataSourceInfo[] wsDataSourceInfos = nDataSourceAdminStub.getAllDataSources();
        if (wsDataSourceInfos != null) {
            for (WSDataSourceInfo wsDataSourceInfo : wsDataSourceInfos) {
                if (RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME.equals(
                        wsDataSourceInfo.getDsMetaInfo().getName())) {
                    nDataSourceAdminStub.deleteDataSource(
                            RSSTestConstants.SampleConfigurations.SAMPLE_DATABASE_NAME);
                }
            }
        }

    }

    private void checkPrivileges(DatabasePrivilegeTemplate src, DatabasePrivilegeTemplate target) {
        DatabasePrivilegeSet srcPrivileges = src.getPrivileges();
        DatabasePrivilegeSet targetPrivileges = target.getPrivileges();
        assertEquals(targetPrivileges.getSelectPriv(), srcPrivileges.getSelectPriv());
        assertEquals(targetPrivileges.getInsertPriv(), srcPrivileges.getInsertPriv());
        assertEquals(targetPrivileges.getUpdatePriv(), srcPrivileges.getUpdatePriv());
        assertEquals(targetPrivileges.getDeletePriv(), srcPrivileges.getDeletePriv());
        assertEquals(targetPrivileges.getCreatePriv(), srcPrivileges.getCreatePriv());
        assertEquals(targetPrivileges.getDropPriv(), srcPrivileges.getDropPriv());
        assertEquals(targetPrivileges.getGrantPriv(), srcPrivileges.getGrantPriv());
        assertEquals(targetPrivileges.getReferencesPriv(), srcPrivileges.getReferencesPriv());
        assertEquals(targetPrivileges.getIndexPriv(), srcPrivileges.getIndexPriv());
        assertEquals(targetPrivileges.getAlterPriv(), srcPrivileges.getAlterPriv());
        assertEquals(targetPrivileges.getCreateTmpTablePriv(), srcPrivileges.getCreateTmpTablePriv());
        assertEquals(targetPrivileges.getLockTablesPriv(), srcPrivileges.getLockTablesPriv());
        assertEquals(targetPrivileges.getCreateViewPriv(), srcPrivileges.getCreateViewPriv());
        assertEquals(targetPrivileges.getShowViewPriv(), srcPrivileges.getShowViewPriv());
        assertEquals(targetPrivileges.getCreateRoutinePriv(), srcPrivileges.getCreateRoutinePriv());
        assertEquals(targetPrivileges.getExecutePriv(), srcPrivileges.getExecutePriv());
        assertEquals(targetPrivileges.getEventPriv(), srcPrivileges.getEventPriv());
        assertEquals(targetPrivileges.getTriggerPriv(), srcPrivileges.getTriggerPriv());
    }

    private boolean isDatabaseUserAlreadyAttached(String rssInstanceName, String databaseName,
                                                  String username) throws Exception {
        try {
            String[] availableUsers =
                    this.getRSSAdminStub().getAvailableUsersToAttachToDatabase(rssInstanceName,
                            databaseName);

            boolean contains = false;
            for (String user : availableUsers) {
                if (username.equals(user)) {
                    contains = true;
                }
            }
            return contains;
        } catch (RemoteException e) {
            throw new Exception("Error occurred while checking whether the sample user is " +
                    "already attached", e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new Exception("Error occurred while checking whether the sample user is " +
                    "already attached", e);
        }
    }

    private RSSAdminStub getRSSAdminStub() {
        return rssAdminStub;
    }

    private NDataSourceAdminStub getNDataSourceAdminStub() {
        return nDataSourceAdminStub;
    }

}
