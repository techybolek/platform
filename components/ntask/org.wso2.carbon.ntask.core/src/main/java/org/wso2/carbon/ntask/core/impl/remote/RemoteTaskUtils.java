/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ntask.core.impl.remote;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;
import org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.remotetasks.stub.admin.common.xsd.StaticTaskInformation;
import org.wso2.carbon.remotetasks.stub.admin.common.xsd.TriggerInformation;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * This class represents remote task related utility methods.
 */
public class RemoteTaskUtils {
	
	private static final String REMOTE_TASKS_CALLBACK_SERVLET_CONTEXT = "remote_tasks_callback";

	public static final String REG_REMOTE_TASK_PROPS_BASE_PATH = RegistryBasedTaskRepository.REG_TASK_BASE_PATH + "/" + "remote_task_props";
	
	public static final String REMOTE_TASK_TENANT_DOMAIN = "REMOTE_TASK_TENANT_DOMAIN";
	
	public static final String REMOTE_TASK_TASK_TYPE = "REMOTE_TASK_TASK_TYPE";
	
	public static final String REMOTE_TASK_TASK_NAME = "REMOTE_TASK_TASK_NAME";

	public static String generateRemoteTaskID() {
		return UUID.randomUUID().toString() + UUID.randomUUID().toString();
	}
	
	private static String resourcePathFromRemoteTaskId(String remoteTaskId) {
		return REG_REMOTE_TASK_PROPS_BASE_PATH + "/" + remoteTaskId;
	}
	
	public static void removeRemoteTaskMapping(String remoteTaskId) throws TaskException {
		try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getCurrentContext().
                            setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
			Registry registry = RegistryBasedTaskRepository.getRegistry();
			registry.delete(resourcePathFromRemoteTaskId(remoteTaskId));
		} catch (Exception e) {
			throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
		} finally {
                        PrivilegedCarbonContext.endTenantFlow();
                }
	}
	
	public static String createRemoteTaskMapping(String tenantDomain,
			String taskType, String taskName) throws TaskException {
		try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getCurrentContext().
                            setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        Registry registry = RegistryBasedTaskRepository.getRegistry();
                        Resource res = registry.newResource();
                        res.setProperty(REMOTE_TASK_TENANT_DOMAIN, tenantDomain);
                        res.setProperty(REMOTE_TASK_TASK_TYPE, taskType);
                        res.setProperty(REMOTE_TASK_TASK_NAME, taskName);
                        String remoteTaskId = generateRemoteTaskID();
                        registry.put(resourcePathFromRemoteTaskId(remoteTaskId), res);
		        return remoteTaskId;
		} catch (Exception e) {
			throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
		} finally {
                        PrivilegedCarbonContext.endTenantFlow();
                }
	}
	
	public static Object[] lookupRemoteTask(String remoteTaskId) throws TaskException {
		try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getCurrentContext().
                        setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        Registry registry = RegistryBasedTaskRepository.getRegistry();
                        Resource res = registry.get(resourcePathFromRemoteTaskId(remoteTaskId));
                        Object[] result = new Object[3];
                        result[0] = res.getProperty(REMOTE_TASK_TENANT_DOMAIN);
                        result[1] = res.getProperty(REMOTE_TASK_TASK_TYPE);
                        result[2] = res.getProperty(REMOTE_TASK_TASK_NAME);
                        return result;
		} catch (Exception e) {
			throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
		} finally {
                        PrivilegedCarbonContext.endTenantFlow();
                }
	}

	private static String getTenantSectionInURL(String tenantDomain) {
		if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
			return "";
		} else {
			return "/t/" + tenantDomain;
		}
	}
	
	private static String getTaskNodeBaseURL(String tenantDomain) {
		return TasksDSComponent.getTaskService().getServerConfiguration().getTaskClientDispatchAddress() +
				getTenantSectionInURL(tenantDomain) + "/" + REMOTE_TASKS_CALLBACK_SERVLET_CONTEXT;
	}
	
	public static String remoteTaskNameFromTaskInfo(String taskType, String taskName) {
		return taskType + "_" + taskName;
	}
	
	public static StaticTaskInformation convert(TaskInfo taskInfo, 
			String taskType, String remoteTaskId, String tenantDomain) throws TaskException {
		StaticTaskInformation stTaskInfo = new StaticTaskInformation();
		stTaskInfo.setName(remoteTaskNameFromTaskInfo(taskType, taskInfo.getName()));
		stTaskInfo.setTargetURI(getTaskNodeBaseURL(tenantDomain) + "/" + remoteTaskId);
		TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
		TriggerInformation stTriggerInfo = new TriggerInformation();
		stTriggerInfo.setCronExpression(triggerInfo.getCronExpression());
		stTriggerInfo.setStartTime(dateToCal(triggerInfo.getStartTime()));
		stTriggerInfo.setEndTime(dateToCal(triggerInfo.getEndTime()));
		stTriggerInfo.setTaskCount(triggerInfo.getRepeatCount());
		stTriggerInfo.setTaskInterval(triggerInfo.getIntervalMillis());
		stTaskInfo.setTriggerInformation(stTriggerInfo);
		stTaskInfo.setAllowConcurrentExecutions(!triggerInfo.isDisallowConcurrentExecution());
		return stTaskInfo;
	}
	
	private static Calendar dateToCal(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
	
}
