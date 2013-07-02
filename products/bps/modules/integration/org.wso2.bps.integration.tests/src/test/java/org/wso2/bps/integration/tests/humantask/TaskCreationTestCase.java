/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bps.integration.tests.humantask;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.*;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.humantask.stub.mgt.HumanTaskPackageManagementStub;
import org.wso2.carbon.humantask.stub.mgt.types.HumanTaskPackageDownloadData;
import org.wso2.carbon.humantask.stub.ui.task.client.api.*;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TComment;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TPriority;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAbstract;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskEvent;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskEvents;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import javax.xml.stream.XMLStreamException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertTrue;

/**
 * This test case deals with task creation by calling the task service interface.
 */
public class TaskCreationTestCase {

    private static final Log log = LogFactory.getLog(TaskCreationTestCase.class);
    private static String SERVICE_URL_PREFIX;

    private UserAdminStub userAdminStub = null;
    private HumanTaskClientAPIAdminStub taskOperationsStub = null;
    private InstanceManagementServiceStub instanceManagementServiceStub = null;
    private HumanTaskPackageManagementStub packageManagementStub = null;

    private URI taskId = null;

     private Set<String> taskEvents = new HashSet<String>();

    @BeforeGroups(groups = {"wso2.bps.task.create"}, description = " Copying sample HumanTask packages")
    protected void init() throws Exception {

        log.info("Initializing HumanTask task creation Test...");

        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                FrameworkSettings.HTTPS_PORT + "/services/";

        userAdminStub = HumanTaskAdminServiceUtils.getUserAdminStub();
        addRoles();
        addUsers();
        taskOperationsStub = HumanTaskAdminServiceUtils.getTaskOperationServiceStub();
        instanceManagementServiceStub = HumanTaskAdminServiceUtils.getInstanceManagementServiceStub();
        packageManagementStub = HumanTaskAdminServiceUtils.getPackageManagementServiceStub();
    }

    private void addRoles() throws Exception {
        userAdminStub.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, null,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"});
        userAdminStub.addRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, null,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"});
    }


    private void addUsers()
            throws Exception {
        userAdminStub.addUser(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD,
                new String[]{HumanTaskTestConstants.REGIONAL_CLERKS_ROLE}, null, null);
        userAdminStub.addUser(HumanTaskTestConstants.CLERK2_USER, HumanTaskTestConstants.CLERK2_PASSWORD,
                new String[]{HumanTaskTestConstants.REGIONAL_CLERKS_ROLE}, null, null);

        userAdminStub.addUser(HumanTaskTestConstants.MANAGER_USER, HumanTaskTestConstants.MANAGER_PASSWORD,
                new String[]{HumanTaskTestConstants.REGIONAL_MANAGER_ROLE}, null, null);

        FlaggedName[] clerkUsers = userAdminStub.getUsersOfRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE,
                "clerk*");
        assertTrue(clerkUsers.length == 2, "There should be exactly 2 clerks users in the system!");

        FlaggedName[] managerUsers = userAdminStub.getUsersOfRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE,
                "manager*");
        assertTrue(managerUsers.length == 1,
                "The manager was not added to the regional manager's role properly");
    }

    @Test(groups = {"wso2.bps.task.create"}, description = "Claims approval test case",priority = 1, singleThreaded = true)
    public void createFirstTask() throws XMLStreamException, AxisFault, InterruptedException {
        String soapBody =
                "<p:ClaimApprovalData xmlns:p=\"http://www.example.com/claims/schema\">\n" +
                        "      <p:cust>\n" +
                        "         <p:id>235235</p:id>\n" +
                        "         <p:firstname>sanjaya</p:firstname>\n" +
                        "         <p:lastname>vithanagama</p:lastname>\n" +
                        "      </p:cust>\n" +
                        "      <p:amount>2500</p:amount>\n" +
                        "      <p:region>LK</p:region>\n" +
                        "      <p:prio>7</p:prio>\n" +
                        "      <p:activateAt>2012-12-09T01:01:01</p:activateAt>\n" +
                        "</p:ClaimApprovalData>";

        String operation = "approve";
        String serviceName = "ClaimService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("taskid>");
        log.info("Calling Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, soapBody, 1,
                expectedOutput, BPSTestUtils.TWO_WAY);

    }

    @Test(groups = {"wso2.bps.task.create"}, description = "Claims approval test case", priority = 2, singleThreaded = true)
    public void createSecondTask() throws XMLStreamException, AxisFault, InterruptedException {
        String soapBody =
                "<p:ClaimApprovalData xmlns:p=\"http://www.example.com/claims/schema\">\n" +
                        "      <p:cust>\n" +
                        "         <p:id>452422</p:id>\n" +
                        "         <p:firstname>John</p:firstname>\n" +
                        "         <p:lastname>Doe</p:lastname>\n" +
                        "      </p:cust>\n" +
                        "      <p:amount>50000</p:amount>\n" +
                        "      <p:region>US</p:region>\n" +
                        "      <p:prio>1</p:prio>\n" +
                        "      <p:activateAt>2012-12-09T01:01:01</p:activateAt>\n" +
                        "</p:ClaimApprovalData>";

        String operation = "approve";
        String serviceName = "ClaimService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("taskid>");
        log.info("Calling Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, soapBody, 1,
                expectedOutput, BPSTestUtils.TWO_WAY);

    }

    @Test(groups = {"wso2.bps.task.create"}, description = "Claims approval B4P test case", priority = 3, singleThreaded = true)
    public void createTaskB4P() throws XMLStreamException, RemoteException, InterruptedException,
            IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
            InstanceManagementException {
        String soapBody =
                "<cla:ClaimApprovalProcessInput xmlns:cla=\"http://www.wso2.org/humantask/claimsapprovalprocessservice.wsdl\">\n" +
                        "         <cla:custID>C002</cla:custID>\n" +
                        "         <cla:custFName>Waruna</cla:custFName>\n" +
                        "         <cla:custLName>Ranasinghe</cla:custLName>\n" +
                        "         <cla:amount>10000</cla:amount>\n" +
                        "         <cla:region>Gampaha</cla:region>\n" +
                        "         <cla:priority>2</cla:priority>\n" +
                        "      </cla:ClaimApprovalProcessInput>";

        String operation = "claimsApprovalProcessOperation";
        String serviceName = "ClaimsApprovalProcessService";
        List<String> expectedOutput = Collections.emptyList();
        log.info("Calling Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, soapBody, 1,
                expectedOutput, BPSTestUtils.ONE_WAY);
        Thread.sleep(5000);
        BPSMgtUtils.listInstances(instanceManagementServiceStub, 1,
                "{http://www.wso2.org/humantask/claimsapprovalprocess.bpel}ClaimsApprovalProcess");

        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ALL_TASKS);

        TTaskSimpleQueryResultSet taskResults = taskOperationsStub.simpleQuery(queryInput);

        TTaskSimpleQueryResultRow[] rows = taskResults.getRow();
        TTaskSimpleQueryResultRow b4pTask = null;

        Assert.assertNotNull(rows, "No tasks found. Task creation has failed. ");

        // looking for the latest task
        for (TTaskSimpleQueryResultRow row : rows) {
            if (b4pTask == null) {
                b4pTask = row;
            } else {
                if (Long.parseLong(b4pTask.getId().toString()) < Long.parseLong(row.getId().toString())) {
                    b4pTask = row;
                }
            }
        }

        Assert.assertNotNull(b4pTask, "Task creation has failed");

        String claimApprovalRequest = (String) taskOperationsStub.getInput(b4pTask.getId(), null);

        Assert.assertNotNull(claimApprovalRequest, "The input of the Task:" +
                b4pTask.getId() + " is null.");

        Assert.assertFalse(!claimApprovalRequest.contains("C002"),
                "Unexpected input found for the Task");

        //claim the task before starting.
        taskOperationsStub.claim(b4pTask.getId());

        //start the task before completing.
        taskOperationsStub.start(b4pTask.getId());

        taskOperationsStub.complete(b4pTask.getId(), "<sch:ClaimApprovalResponse xmlns:sch=\"http://www.example.com/claims/schema\">\n" +
                "         <sch:approved>true</sch:approved>\n" +
                "      </sch:ClaimApprovalResponse>");

        Thread.sleep(5000);
        List<String> instances = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1,
                "{http://www.wso2.org/humantask/claimsapprovalprocess.bpel}ClaimsApprovalProcess");

        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "COMPLETED", "b4pOutput",
                ">true<", instances);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, instances.size());
    }

    @Test(groups = {"wso2.bps.task.operate.list"}, description = "package download test case",priority = 4, singleThreaded = true)
    public void testSimpleTaskQuery() throws Exception {
        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ALL_TASKS);

        TTaskSimpleQueryResultSet allTasksList = taskOperationsStub.simpleQuery(queryInput);

        TTaskSimpleQueryResultRow[] rows = allTasksList.getRow();


        int taskIdInt = Integer.MAX_VALUE;

        for (TTaskSimpleQueryResultRow row : rows) {
            int rowTaskId = Integer.parseInt(row.getId().toString());
            if (rowTaskId < taskIdInt) {
                taskIdInt = rowTaskId;
                taskId = row.getId();
            }
        }
        Assert.assertNotNull(rows, "The task results cannot be null");
        Assert.assertEquals(rows.length, 3, "There should be 3 tasks from the query");
    }


    @Test( groups = {"wso2.bps.task.operate"}, description = "Claims approval test case", priority = 5, singleThreaded = true)
    public void testLoadTask()
            throws Exception {

        Assert.assertNotNull(taskId, "The task ID has to be set by now!");

        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertNotNull(loadedTask, "The task is not created successfully");
        Assert.assertEquals(loadedTask.getId().toString(), taskId.toString(), "The task id is wrong");

    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",priority = 6, singleThreaded = true)
    public void taskClaimTask()
            throws Exception {

        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        taskOperationsStub.claim(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getActualOwner().getTUser(), HumanTaskTestConstants.CLERK1_USER,
                            "The assignee should be clerk1 !");
        Assert.assertEquals(loadedTask.getStatus().toString(), "RESERVED",
                            "The task status should be RESERVED!");

        taskEvents.add("claim");

    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case", priority = 7,singleThreaded = true)
    public void testTaskStartWithoutClaim() throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        taskOperationsStub.release(taskId);

        // Now start the task without claiming it explicitly.
        taskOperationsStub.start(taskId);

        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);

        //2. The task status should go back to READY
        Assert.assertEquals(loadedTask.getStatus().toString(), "IN_PROGRESS",
                            "The task status should be IN_PROGRESS!");

        taskOperationsStub.stop(taskId);

        taskOperationsStub.release(taskId);

        taskOperationsStub.claim(taskId);

        loadedTask = taskOperationsStub.loadTask(taskId);

        Assert.assertNotNull(loadedTask.getActualOwner(),
                             "After releasing the task the actual owner should be null");

        Assert.assertEquals("clerk1", loadedTask.getActualOwner().getTUser(), "Actual owner should be clerk1");
    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case release and reclaim task",priority = 8,singleThreaded = true)
    public void taskReleaseAndReClaimTask()
            throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        taskOperationsStub.release(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);

        //Now as the task have been release
        //1. The actual user value should be empty.
        Assert.assertNull(loadedTask.getActualOwner(),
                          "After releasing the task the actual owner should be null");
        //2. The task status should go back to READY
        Assert.assertEquals(loadedTask.getStatus().toString(), "READY",
                            "The task status should be READY!");

        taskEvents.add("release");

        // Now reclaim the task to continue with other operations.
        taskOperationsStub.claim(taskId);
        TTaskAbstract loadedTaskAferReClaim = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTaskAferReClaim.getActualOwner().getTUser(), HumanTaskTestConstants.CLERK1_USER,
                            "The assignee should be clerk1 !");
        Assert.assertEquals(loadedTaskAferReClaim.getStatus().toString(), "RESERVED",
                            "The task status should be RESERVED!");
    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case release and reclaim task",priority = 9,singleThreaded = true)
    public void testTaskGetInput() throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        String input = (String) taskOperationsStub.getInput(taskId, null);

        Assert.assertNotNull(input, "The input message cannot be null");
        Assert.assertTrue(input.contains("<ClaimApprovalData xmlns=\"http://www.example.com/claims/schema\" " +
                                         "xmlns:p=\"http://www.example.com/claims/schema\" " +
                                         "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"),
                          "The retrieved input message should contain message data");

        NCName ncName = new NCName();
        ncName.setValue("ClaimApprovalRequest");

        String inputMessageWithPartName = (String) taskOperationsStub.getInput(taskId, ncName);
        Assert.assertNotNull(input, "The input message cannot be null");
        Assert.assertTrue(input.contains("<ClaimApprovalData xmlns=\"http://www.example.com/claims/schema\" " +
                                         "xmlns:p=\"http://www.example.com/claims/schema\" " +
                                         "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"),
                          "The retrieved input message should contain message data");


        Assert.assertEquals(input, inputMessageWithPartName, "2 returned values are different");
    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",priority = 10,singleThreaded = true)
    public void testStartTask()
            throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        taskOperationsStub.start(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getStatus().toString(), "IN_PROGRESS",
                            "The task status should be IN_PROGRESS after starting the task!");
        taskEvents.add("start");
    }


    @Test(groups = {"wso2.bps.task.operate"}, description = "Task priority change test case",priority = 11,singleThreaded = true)
    public void testChangeTaskPriority() throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        TPriority newPriority1 = new TPriority();
        newPriority1.setTPriority(BigInteger.valueOf(1));

        HumanTaskClientAPIAdminStub clientAPIAdminStubForManager =
                HumanTaskAdminServiceUtils.getTaskOperationServiceStub(HumanTaskTestConstants.MANAGER_USER,
                                                                       HumanTaskTestConstants.MANAGER_PASSWORD);

        clientAPIAdminStubForManager.setPriority(taskId, newPriority1);

        TTaskAbstract taskAfterPriorityChange1 = taskOperationsStub.loadTask(taskId);
        TPriority prio1 = taskAfterPriorityChange1.getPriority();
        int newPriority1Int = prio1.getTPriority().intValue();
        Assert.assertEquals(newPriority1Int, 1, "The new priority should be 1 after the set priority " +
                                                "operation");

        TPriority newPriority2 = new TPriority();
        newPriority2.setTPriority(BigInteger.valueOf(10));

        clientAPIAdminStubForManager.setPriority(taskId, newPriority2);

        TTaskAbstract taskAfterPriorityChange2 = clientAPIAdminStubForManager.loadTask(taskId);
        TPriority prio2 = taskAfterPriorityChange2.getPriority();
        int newPriority2Int = prio2.getTPriority().intValue();
        Assert.assertEquals(newPriority2Int, 10, "The new priority should be 10 after the set priority " +
                                                 "operation");

    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",priority = 12,singleThreaded = true)
    public void testStopTask()
            throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        taskOperationsStub.stop(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getStatus().toString(), "RESERVED",
                            "The task status should be RESERVED after stopping the task!");
        taskEvents.add("stop");

        // Now start the task again
        taskOperationsStub.start(taskId);
        TTaskAbstract loadedTask2 = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask2.getStatus().toString(), "IN_PROGRESS",
                            "The task status should be IN_PROGRESS after re-starting the task!");
    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",priority = 13,singleThreaded = true)
    public void testSuspendAndResume()
            throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        taskOperationsStub.suspend(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getStatus().toString(), "SUSPENDED",
                            "The task status should be SUSPENDED after suspending the task!");
        Assert.assertEquals(loadedTask.getPreviousStatus().toString(), "IN_PROGRESS",
                            "The task previous status should be IN_PROGRESS");
        taskEvents.add("suspend");

        taskOperationsStub.resume(taskId);
        TTaskAbstract loadedTaskAfterResume = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTaskAfterResume.getStatus().toString(), "IN_PROGRESS",
                            "The task status should be IN_PROGRESS after resuming the suspended task!");
        taskEvents.add("resume");
    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",priority = 14,singleThreaded = true)
    public void testTaskCommentOperations() throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        String commentText1 = "This is a test comment";
        URI taskCommentId = taskOperationsStub.addComment(taskId, commentText1);

        taskEvents.add("addcomment");

        Assert.assertNotNull(taskCommentId, "The comment id cannot be null");

        TComment[] taskComments = taskOperationsStub.getComments(taskId);
        Assert.assertEquals(taskComments.length, 1, "The task comments size should be 1 after adding only 1 comment");
        Assert.assertEquals(taskComments[0].getId(), taskCommentId, "The task comment id returned should be equal");

        String commentText2 = "This is a test comment 2";
        URI taskCommentId2 = taskOperationsStub.addComment(taskId, commentText2);
        Assert.assertNotNull(taskCommentId2, "The comment id cannot be null");

        TComment[] taskComments2 = taskOperationsStub.getComments(taskId);
        Assert.assertEquals(taskComments2.length, 2, "The task comments size should be 2 after adding 2 comments");
        Assert.assertEquals(taskComments2[1].getId(), taskCommentId2, "The task comment id returned should be equal");

        //delete the comments
        taskOperationsStub.deleteComment(taskId, taskCommentId);
        TComment[] commentsAfterDeletion = taskOperationsStub.getComments(taskId);
        Assert.assertEquals(commentsAfterDeletion.length, 1, "Only 1 comment should be left");
        Assert.assertEquals(commentsAfterDeletion[0].getId(), taskCommentId2, "Only comment 2 should be left after deleting comment 1");

        //delete the left over comment as well
        taskOperationsStub.deleteComment(taskId, taskCommentId2);
        TComment[] commentsAfterAllDeletions = taskOperationsStub.getComments(taskId);
        Assert.assertNull(commentsAfterAllDeletions, "There should not be any comments left!");

        taskEvents.add("deletecomment");

    }

    // check the task events are persisted properly
    @Test(groups = {"wso2.bps.task.operate"}, description = "Task event persistence",priority = 15,singleThreaded = true)
    public void testTaskEventHistory() throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        TTaskEvents tTaskEvents = taskOperationsStub.loadTaskEvents(taskId);
        TTaskEvent[] events = tTaskEvents.getEvent();

        Assert.assertNotNull(events, "The task event history cannot be empty after performing task operations");
        Assert.assertNotEquals(events.length, 0, "The task event history objects should be a positive number");

        Set<String> persistedTaskEvents = new HashSet<String>();
        for (TTaskEvent event : events) {
            persistedTaskEvents.add(event.getEventType());
        }

        for (String occurredTaskEvent : this.taskEvents) {
            Assert.assertTrue(persistedTaskEvents.contains(occurredTaskEvent),
                              "The occurred task event [" + occurredTaskEvent +
                              "] is not in the persisted task event list :[" +
                              StringUtils.join(persistedTaskEvents.toArray(), ",") + "]");
        }

    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "package download test case",priority = 16, singleThreaded = true)
    public void testDownloadPackage() throws Exception {
        HumanTaskPackageDownloadData downloadData =
                packageManagementStub.downloadHumanTaskPackage(
                        HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_NAME);

        Assert.assertNotNull(downloadData.getPackageFileData(), "The downloaded package data cannot be null");
        Assert.assertEquals(downloadData.getPackageName(), HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_NAME + ".zip");
    }
}
