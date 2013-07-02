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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.HumanTaskAdminServiceUtils;
import org.wso2.bps.integration.tests.util.HumanTaskTestConstants;
import org.wso2.carbon.humantask.stub.mgt.HumanTaskPackageManagementStub;
import org.wso2.carbon.humantask.stub.mgt.types.HumanTaskPackageDownloadData;
import org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TComment;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TPriority;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAbstract;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskEvent;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskEvents;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Test class to check the task operations functionality.
 */
public class TaskOperationsTestCase {

    private HumanTaskClientAPIAdminStub taskOperationsStub = null;

    private HumanTaskPackageManagementStub packageManagementStub = null;

    private URI taskId = null;

    private Set<String> taskEvents = new HashSet<String>();


    @BeforeGroups(groups = {"wso2.bps.task.operate", "wso2.bps.task.operate.list"}, description = " Copying sample HumanTask packages")
    public void init() throws Exception {
        taskOperationsStub = HumanTaskAdminServiceUtils.getTaskOperationServiceStub();
        packageManagementStub = HumanTaskAdminServiceUtils.getPackageManagementServiceStub();
    }

    @BeforeGroups(groups = {"wso2.bps.task.operate"}, description = " Copying sample HumanTask packages")
    @Test(groups = {"wso2.bps.task.operate.list"}, description = "package download test case", singleThreaded = true)
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


    @Test( groups = {"wso2.bps.task.operate"}, description = "Claims approval test case", singleThreaded = true, dependsOnMethods = {"testSimpleTaskQuery"})
    public void testLoadTask()
            throws Exception {

        Assert.assertNotNull(taskId, "The task ID has to be set by now!");

        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertNotNull(loadedTask, "The task is not created successfully");
        Assert.assertEquals(loadedTask.getId().toString(), "1", "The task id is wrong");

    }

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case", singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case release and reclaim task",singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case release and reclaim task",singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",singleThreaded = true)
    public void testStartTask()
            throws Exception {
        Assert.assertNotNull(taskId, "The task ID has to be set by now!");
        taskOperationsStub.start(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getStatus().toString(), "IN_PROGRESS",
                            "The task status should be IN_PROGRESS after starting the task!");
        taskEvents.add("start");
    }


    @Test(groups = {"wso2.bps.task.operate"}, description = "Task priority change test case",singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "Claims approval test case",singleThreaded = true)
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
    @Test(groups = {"wso2.bps.task.operate"}, description = "Task event persistence",singleThreaded = true)
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

    @Test(groups = {"wso2.bps.task.operate"}, description = "package download test case", singleThreaded = true)
    public void testDownloadPackage() throws Exception {
        HumanTaskPackageDownloadData downloadData =
                packageManagementStub.downloadHumanTaskPackage(
                        HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_NAME);

        Assert.assertNotNull(downloadData.getPackageFileData(), "The downloaded package data cannot be null");
        Assert.assertEquals(downloadData.getPackageName(), HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_NAME + ".zip");
    }

}
