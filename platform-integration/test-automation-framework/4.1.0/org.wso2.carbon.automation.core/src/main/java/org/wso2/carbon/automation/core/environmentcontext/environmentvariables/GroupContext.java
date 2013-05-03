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

package org.wso2.carbon.automation.core.environmentcontext.environmentvariables;

import java.util.ArrayList;

public class GroupContext {
    private String groupName;
    private Context node;
    private ArrayList<Context> workerList;
    private ArrayList<Context> managerList;
    private Context lbWorker;
    private Context lbManager;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setNode(Context node) {
        this.node = node;
    }

    public Context getNode() {
        return this.node;
    }


    public void setWorkerList(ArrayList<Context> workerList) {
        this.workerList = workerList;
    }

    public ArrayList<Context> getWorkerList() {
        return this.workerList;
    }

    public void setManagerList(ArrayList<Context> managerList) {
        this.managerList = managerList;
    }

    public ArrayList<Context> getManagerList() {
        return this.managerList;
    }

    public void setLbWorker(Context node) {
        this.lbWorker = node;
    }

    public Context getLbWorker() {
        return this.lbWorker;
    }

    public void setLbManager(Context node) {
        this.lbManager = node;
    }

    public Context getLbManager() {
        return this.lbManager;
    }
}
