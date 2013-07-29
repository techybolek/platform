/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.remotetasks.core.admin;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.remotetasks.common.DeployedTaskInformation;
import org.wso2.carbon.remotetasks.common.RemoteTasksException;
import org.wso2.carbon.remotetasks.common.StaticTaskInformation;
import org.wso2.carbon.remotetasks.common.TriggerInformation;
import org.wso2.carbon.remotetasks.core.RemoteTaskManager;

/**
 * This class represents the admin service for remote tasks.
 */
public class RemoteTasksAdmin extends AbstractAdmin {

	public void addRemoteTask(StaticTaskInformation taskInfo) throws RemoteTasksException {
		RemoteTaskManager.getInstance().addTask(taskInfo);
	}
	
	public void rescheduleRemoteTask(String taskName, 
			TriggerInformation stTriggerInfo) throws RemoteTasksException {
		RemoteTaskManager.getInstance().rescheduleTask(taskName, stTriggerInfo);
	}
	
	public DeployedTaskInformation getRemoteTask(String name) throws RemoteTasksException {
		return RemoteTaskManager.getInstance().getTask(name);
	}
	
	public boolean deleteRemoteTask(String name) throws RemoteTasksException {
		return RemoteTaskManager.getInstance().deleteTask(name);
	}
	
	public void pauseRemoteTask(String name) throws RemoteTasksException {
		RemoteTaskManager.getInstance().pauseTask(name);
	}
	
	public void resumeRemoteTask(String name) throws RemoteTasksException {
		RemoteTaskManager.getInstance().resumeTask(name);
	}
	
	public String[] getAllRemoteTasks() throws RemoteTasksException {
		return RemoteTaskManager.getInstance().getAllTasks();
	}
	
	public void addRemoteSystemTask(StaticTaskInformation taskInfo,
			String targetTenantDomain) throws RemoteTasksException {
		RemoteTaskManager.getInstance().addSystemTask(targetTenantDomain, taskInfo);
	}
	
	public void rescheduleRemoteSystemTask(String taskName, TriggerInformation stTriggerInfo,
			String targetTenantDomain) throws RemoteTasksException {
		RemoteTaskManager.getInstance().rescheduleSystemTask(targetTenantDomain,
				taskName, stTriggerInfo);
	}
	
	public DeployedTaskInformation getRemoteSystemTask(String name,
			String targetTenantDomain) throws RemoteTasksException {
		try {
		    return RemoteTaskManager.getInstance().getSystemTask(targetTenantDomain, name);
		} catch (RemoteTasksException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public boolean deleteRemoteSystemTask(String name,
			String targetTenantDomain) throws RemoteTasksException {
		return RemoteTaskManager.getInstance().deleteSystemTask(targetTenantDomain, name);
	}
	
	public void pauseRemoteSystemTask(String name,
			String targetTenantDomain) throws RemoteTasksException {
		RemoteTaskManager.getInstance().pauseSystemTask(targetTenantDomain, name);
	}
	
	public void resumeRemoteSystemTask(String name,
			String targetTenantDomain) throws RemoteTasksException {
		RemoteTaskManager.getInstance().resumeSystemTask(targetTenantDomain, name);
	}
	
	public String[] getAllRemoteSystemTasks(String targetTenantDomain) throws RemoteTasksException {
		return RemoteTaskManager.getInstance().getAllSystemTasks(targetTenantDomain);
	}
	
}
