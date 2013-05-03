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

package org.wso2.carbon.automation.core.environmentcontext;

import org.wso2.carbon.automation.core.environmentcontext.environmentenum.NodeType;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.Context;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.GroupContext;
import org.wso2.carbon.automation.core.globalcontext.GlobalContextInitiator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupContextProvider {
    private static Map<String, Context> nodeMap = new HashMap();

    public GroupContextProvider() {
        GlobalContextInitiator nodeContextInitiator =new GlobalContextInitiator();
        nodeMap = nodeContextInitiator.getNodeContext().getInstanceNodeMap();
    }

    public GroupContext getGroupContext(String groupId) {
        ArrayList<Context> workerList = new ArrayList<Context>();
        ArrayList<Context> managerList = new ArrayList<Context>();
        GroupContext groupContext = new GroupContext();
        for (Context instance : nodeMap.values()) {
            if (instance.getInstanceProperties().getGroupId().equals(groupId)) {
                groupContext.setGroupName(groupId);
                if (instance.getInstanceProperties().getNodeType().equals(NodeType.node.name())) {
                    groupContext.setNode(instance);
                } else if (instance.getInstanceProperties().getNodeType().equals(NodeType.worker.name())) {
                    workerList.add(instance);
                } else if (instance.getInstanceProperties().getNodeType().equals(NodeType.manager.name())) {
                    managerList.add(instance);
                } else if (instance.getInstanceProperties().getNodeType().equals(NodeType.lb.name())) {
                    if (instance.getInstanceProperties().getNodeContent().equals(NodeType.worker.name())) {
                        groupContext.setLbWorker(instance);
                    } else if (instance.getInstanceProperties().getNodeContent().equals(NodeType.manager.name())) {
                        groupContext.setLbManager(instance);
                    }
                }
            }
        }
        groupContext.setWorkerList(workerList);
        groupContext.setManagerList(managerList);
        return groupContext;
    }

}
