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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class PlatformContextFactory {
    private PlatformContext platformContext;

    public PlatformContextFactory() {
        platformContext = new PlatformContext();
    }


    /*
     this method creates the platform object
      */
    public void createPlatformContext(OMElement nodeElement) {

        Iterator instanceGroupIterator = nodeElement.getChildElements();
        OMNode instanceGroupNode;

        // Walk through the instance group list
        while (instanceGroupIterator.hasNext()) {
            InstanceGroup instanceGroup = new InstanceGroup();
            instanceGroupNode = (OMNode) instanceGroupIterator.next();

            //set the attributes of the current instance group
            String groupName = ((OMElementImpl) instanceGroupNode).getAttributeValue
                    (QName.valueOf(ContextConstants.PLATFORM_CONTEXT_INSTANCE_GROUP_NAME));
            Boolean clusteringEnabled = Boolean.parseBoolean(((OMElementImpl) instanceGroupNode).getAttributeValue
                    (QName.valueOf(ContextConstants.PLATFORM_CONTEXT_INSTANCE_GROUP_CLUSTERING_ENABLED)));
            instanceGroup.setGroupName(groupName);
            instanceGroup.setClusteringEnabled(clusteringEnabled);

            // walk through the instances in the instance group
            Iterator instances = ((OMElementImpl) instanceGroupNode).getChildElements();
            OMNode instanceNode;
            while (instances.hasNext()) {

                //adding the instance to the instance group
                instanceNode = (OMNode) instances.next();
                Instance currentInstance = createInstance(instanceNode);
                addInstanceToTypeList(currentInstance, instanceGroup);


            }

            platformContext.addInstanceGroup(instanceGroup);


        }
    }

    /*
    this method adds the given instance in to the given instance group's matching hashMap considering
    the type of the instance
     */
    protected void addInstanceToTypeList(Instance instance, InstanceGroup instanceGroup) {

        //add the instance to the complete list
        instanceGroup.instanceList.put(instance.getName(), instance);

        //add the instance to the separate lists
        if (instance.getType().equals(InstanceTypeEnum.instance)) {
            instanceGroup.addInstance(instance.getName(), instance);
        } else if (instance.getType().equals(InstanceTypeEnum.worker)) {
            instanceGroup.addWorkerInstance(instance.getName(), instance);
        } else if (instance.getType().equals(InstanceTypeEnum.manager)) {
            instanceGroup.addManagerInstance(instance.getName(), instance);
        } else if (instance.getType().equals(InstanceTypeEnum.lb_worker.worker)) {
            instanceGroup.addLoadBalanceWorkerInstance(instance.getName(), instance);
        } else if (instance.getType().equals(InstanceTypeEnum.lb_manager)) {
            instanceGroup.addLoadBalanceManagerInstance(instance.getName(), instance);
        } else if (instance.getType().equals(InstanceTypeEnum.lb)) {
            instanceGroup.addLoadBalancerInstance(instance.getName(), instance);
        }

    }

    /*
     this method create and returns the instance object
      */
    protected Instance createInstance(OMNode instanceNode) {

        Instance instance = new Instance();


        String instanceName = ((OMElementImpl) instanceNode).getAttributeValue
                (QName.valueOf(ContextConstants.PLATFORM_CONTEXT_INSTANCE_NAME));
        String instanceType = ((OMElementImpl) instanceNode).getAttributeValue
                (QName.valueOf(ContextConstants.PLATFORM_CONTEXT_INSTANCE_TYPE));


        instance.setName(instanceName);
        instance.setType(instanceType);

        Iterator instancePropertiesIterator = ((OMElementImpl) instanceNode).getChildElements();
        OMNode instancePropertyNode;
        while (instancePropertiesIterator.hasNext()) {
            instancePropertyNode = (OMNode) instancePropertiesIterator.next();

            //set the attribute values of the current instance
            String attribute = ((OMElementImpl) instancePropertyNode).getLocalName();
            String attributeValue = ((OMElementImpl) instancePropertyNode).getText();


            //set the property values of the current instance
            if (attribute.equals(ContextConstants.PLATFORM_CONTEXT_INSTANCE_HOST)) {

                instance.setHost(attributeValue);
            } else if (attribute.equals(ContextConstants.PLATFORM_CONTEXT_INSTANCE_HTTP_PORT)) {

                instance.setHttpPort(attributeValue);
            } else if (attribute.equals(ContextConstants.PLATFORM_CONTEXT_INSTANCE_HTTPS_PORT)) {

                instance.setHttpsPort(attributeValue);
            } else if (attribute.equals(ContextConstants.PLATFORM_CONTEXT_INSTANCE_WEB_CONTEXT)) {

                instance.setWebContext(attributeValue);
            }

        }
        return instance;
    }


    /*
    this returns the platform context
     */
    public PlatformContext getPlatformContext() {
        return platformContext;
    }
}
