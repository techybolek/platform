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

import org.wso2.carbon.automation.core.context.AutomationContext;

import java.util.HashMap;

public class PlatformContext extends AutomationContext {

    private HashMap<String, InstanceGroup> instanceGroupMap = new HashMap<String, InstanceGroup>();

    public HashMap<String, InstanceGroup> getInstanceGroupMap() {
        return instanceGroupMap;
    }

    public InstanceGroup getInstanceGroup(String instanceGroupName) {
        return instanceGroupMap.get(instanceGroupName);
    }

    public void setInstanceGroupMap(HashMap<String, InstanceGroup> instanceGroupMap) {
        this.instanceGroupMap = instanceGroupMap;
    }

    // this method to add new instance group to the instance group list
    public void addInstanceGroup(InstanceGroup instanceGroup) {

        instanceGroupMap.put(instanceGroup.getGroupName(), instanceGroup);
    }
}
