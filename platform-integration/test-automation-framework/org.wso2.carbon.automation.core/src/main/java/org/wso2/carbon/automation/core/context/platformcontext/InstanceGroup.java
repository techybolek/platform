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
    private HashMap<String, Instance> instances = new HashMap<String, Instance>();
    private HashMap<String, Instance> loadBalancerInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> managerInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> workerInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> loadBalanceWorkerInstances = new HashMap<String, Instance>();
    private HashMap<String, Instance> loadBalanceManagerInstances = new HashMap<String, Instance>();

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


    public void addInstance(String name, Instance instance) {
        instances.put(name, instance);
    }

    public HashMap<String, Instance> getInstances() {
        return instances;
    }

    public void setInstances(HashMap<String, Instance> instances) {
        this.instances = instances;
    }

    public HashMap<String, Instance> getLoadBalancerInstances() {
        return loadBalancerInstances;
    }

    public void setLoadBalancerInstances(HashMap<String, Instance> loadBalancerInstances) {
        this.loadBalancerInstances = loadBalancerInstances;
    }

    public void addLoadBalancerInstance(String name, Instance instance) {
        this.loadBalancerInstances.put(name, instance);
    }

    public HashMap<String, Instance> getManagerInstances() {
        return managerInstances;
    }

    public void setManagerInstances(HashMap<String, Instance> managerInstances) {
        this.managerInstances = managerInstances;
    }

    public void addManagerInstance(String name, Instance instance) {
        this.managerInstances.put(name, instance);
    }

    public HashMap<String, Instance> getWorkerInstances() {
        return workerInstances;
    }

    public void setWorkerInstances(HashMap<String, Instance> workerInstances) {
        this.workerInstances = workerInstances;
    }

    public void addWorkerInstance(String name, Instance instance) {
        this.workerInstances.put(name, instance);
    }

    public HashMap<String, Instance> getLoadBalanceWorkerInstances() {
        return loadBalanceWorkerInstances;
    }

    public void setLoadBalanceWorkerInstances(HashMap<String, Instance> loadBalanceWorkerInstances) {
        this.loadBalanceWorkerInstances = loadBalanceWorkerInstances;
    }

    public void addLoadBalanceWorkerInstance(String name, Instance instance) {
        this.loadBalanceWorkerInstances.put(name, instance);
    }

    public HashMap<String, Instance> getLoadBalanceManagerInstances() {
        return loadBalanceManagerInstances;
    }

    public void setLoadBalanceManagerInstances(HashMap<String, Instance> loadBalanceManagerInstances) {
        this.loadBalanceManagerInstances = loadBalanceManagerInstances;
    }

    public void addLoadBalanceManagerInstance(String name, Instance instance) {
        this.loadBalanceManagerInstances.put(name, instance);
    }

    public Instance getInstance(String instanceId) {

        return instanceList.get(instanceId);

    }
}
