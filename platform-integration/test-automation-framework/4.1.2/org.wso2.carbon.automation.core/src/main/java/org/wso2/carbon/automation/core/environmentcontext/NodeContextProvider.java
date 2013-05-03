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
import org.wso2.carbon.automation.core.globalcontext.GlobalContextInitiator;
import org.wso2.carbon.automation.core.globalcontext.GlobalContextProvider;

import java.util.HashMap;
import java.util.Map;
/**
 * Reads the node values form the input XML and set the Node object
 * **/
public class NodeContextProvider extends GlobalContextProvider {
    private static Map<String, Context> nodeMap = new HashMap();

    public NodeContextProvider() {
        GlobalContextInitiator nodeContextInitiator =new GlobalContextInitiator();
        nodeMap = nodeContextInitiator.getNodeContext().getInstanceNodeMap();
    }

    protected Context getContext(String nodeId) {
        return nodeMap.get(nodeId);
    }

    protected Context getManager(String nodeId) {
        String managerId = null;
        Context node = nodeMap.get(nodeId);
        String groupId = node.getInstanceProperties().getGroupId();
        for (Context groupNode : nodeMap.values()) {        //Worker manager seperated environment
            if (groupNode.getInstanceProperties().getGroupId().equals(groupId)
                    && groupNode.getInstanceProperties().getNodeType().equals(NodeType.lb.name())) {
                if (groupNode.getInstanceProperties().getNodeContent().equals(NodeType.manager.name())) {
                    managerId = groupNode.getNodeId();
                    break;
                }
            }
        }
        return nodeMap.get(managerId);
    }

    protected Context getWorker(String nodeId) {
        String workerId = null;
        Context node = nodeMap.get(nodeId);
        String groupId = node.getInstanceProperties().getGroupId();
        for (Context groupNode : nodeMap.values()) {        //Worker manager seperated environment
            if (groupNode.getInstanceProperties().getGroupId().equals(groupId)
                    && groupNode.getInstanceProperties().getNodeType().equals(NodeType.lb.name())) {
                if (groupNode.getInstanceProperties().getNodeContent().equals(NodeType.worker.name())) {
                    workerId = groupNode.getNodeId();
                    break;
                }
            }
        }
        return nodeMap.get(workerId);
    }
}
