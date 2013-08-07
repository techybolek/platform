/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.util;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.EventBuilderDeployer;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

import java.io.File;

public class EventBuilderUtil {

    public static void executeDeployment(String eventBuilderPath,
                                         AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfiguration.getConfigurator();
        EventBuilderDeployer deployer = (EventBuilderDeployer) deploymentEngine.getDeployer(EventBuilderConstants.EB_CONFIG_DIRECTORY, "xml");
        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(eventBuilderPath));
        deployer.deployConfigFile(deploymentFileData);
    }

    public static String deriveEventBuilderNameFrom(String filename) {
        int beginIndex = 0;
        int endIndex = filename.lastIndexOf(".xml");
        if (filename.contains("/")) {
            beginIndex = filename.lastIndexOf(File.separator) + 1;
        }
        return filename.substring(beginIndex, endIndex);
    }

    public static Object getConvertedAttributeObject(String value, AttributeType type) {
        switch (type) {
            case INT:
                return Integer.valueOf(value);
            case LONG:
                return Long.valueOf(value);
            case DOUBLE:
                return Double.valueOf(value);
            case FLOAT:
                return Float.valueOf(value);
            case BOOL:
                return Boolean.valueOf(value);
            case STRING:
            default:
                return value;
        }
    }
}
