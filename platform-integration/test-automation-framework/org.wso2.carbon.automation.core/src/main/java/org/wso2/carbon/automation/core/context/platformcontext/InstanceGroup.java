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

package org.wso2.carbon.automation.core.context.platformcontext;


import java.util.HashMap;

public class InstanceGroup {

    private String groupName;
    private Boolean clusteringEnabled;

    //separate instance lists  based on the type of the instances
    private HashMap<String, Instance> instanceTypeInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> loadBalanceTypeInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> managerTypeInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> workerTypeInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> loadBalanceWorkerTypeInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> loadBalanceManagerTypeInstances = new HashMap<String, Instance>();

    //this list holds the complete list of instances

    protected HashMap<String, Instance> instanceList = new HashMap<String, Instance>();


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getClusteringEnabled() {
        return clusteringEnabled;
    }

    public void setClusteringEnabled(Boolean clusteringEnabled) {
        this.clusteringEnabled = clusteringEnabled;
    }


    public void addInstance(Instance instance, HashMap specificMap) {

        specificMap.put(instance.getName(), instance);
    }

    public HashMap<String, Instance> getInstanceTypeInstances() {
        return instanceTypeInstances;
    }

    public void setInstanceTypeInstances(HashMap<String, Instance> instanceTypeInstances) {
        this.instanceTypeInstances = instanceTypeInstances;
    }

    public HashMap<String, Instance> getLoadBalanceTypeInstances() {
        return loadBalanceTypeInstances;
    }

    public void setLoadBalanceTypeInstances(HashMap<String, Instance> loadBalanceTypeInstances) {
        this.loadBalanceTypeInstances = loadBalanceTypeInstances;
    }

    public HashMap<String, Instance> getManagerTypeInstances() {
        return managerTypeInstances;
    }

    public void setManagerTypeInstances(HashMap<String, Instance> managerTypeInstances) {
        this.managerTypeInstances = managerTypeInstances;
    }

    public HashMap<String, Instance> getWorkerTypeInstances() {
        return workerTypeInstances;
    }

    public void setWorkerTypeInstances(HashMap<String, Instance> workerTypeInstances) {
        this.workerTypeInstances = workerTypeInstances;
    }

    public HashMap<String, Instance> getLoadBalanceWorkerTypeInstances() {
        return loadBalanceWorkerTypeInstances;
    }

    public void setLoadBalanceWorkerTypeInstances(HashMap<String, Instance> loadBalanceWorkerTypeInstances) {
        this.loadBalanceWorkerTypeInstances = loadBalanceWorkerTypeInstances;
    }

    public HashMap<String, Instance> getLoadBalanceManagerTypeInstances() {
        return loadBalanceManagerTypeInstances;
    }

    public void setLoadBalanceManagerTypeInstances(HashMap<String, Instance> loadBalanceManagerTypeInstances) {
        this.loadBalanceManagerTypeInstances = loadBalanceManagerTypeInstances;
    }

    public Instance getInstance(String instanceId) {

        return instanceList.get(instanceId);

    }
}
