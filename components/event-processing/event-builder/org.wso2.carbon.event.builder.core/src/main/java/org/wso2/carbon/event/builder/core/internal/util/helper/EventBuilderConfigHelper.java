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

package org.wso2.carbon.event.builder.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.event.builder.core.EventBuilderDeployer;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.type.json.JsonBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.map.MapBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.text.TextBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;

import javax.xml.namespace.QName;
import java.io.File;

public class EventBuilderConfigHelper {

    public static InputTransportAdaptorMessageConfiguration getInputTransportMessageConfiguration(
            String transportAdaptorTypeName) {
        MessageDto messageDto = EventBuilderServiceValueHolder.getInputTransportAdaptorService().getTransportMessageDto(transportAdaptorTypeName);
        InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration = null;
        if (messageDto != null && messageDto.getMessageInPropertyList() != null) {
            inputTransportMessageConfiguration = new InputTransportAdaptorMessageConfiguration();
            for (Property property : messageDto.getMessageInPropertyList()) {
                inputTransportMessageConfiguration.addInputMessageProperty(property.getPropertyName(), property.getDefaultValue());
            }
        }

        return inputTransportMessageConfiguration;
    }

    public static String getInputMappingType(OMElement eventBuilderOMElement) {
        OMElement mappingElement = eventBuilderOMElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_MAPPING));
        return mappingElement.getAttributeValue(new QName(EventBuilderConstants.EB_ATTR_TYPE));
    }

    public static EventBuilderConfigBuilder getEventBuilderConfigBuilder(String inputMappingType) {
        if (EventBuilderConstants.EB_WSO2EVENT_MAPPING_TYPE.equals(inputMappingType)) {
            return Wso2EventBuilderConfigBuilder.getInstance();
        } else if (EventBuilderConstants.EB_XML_MAPPING_TYPE.equals(inputMappingType)) {
            return XMLBuilderConfigBuilder.getInstance();
        } else if (EventBuilderConstants.EB_TEXT_MAPPING_TYPE.equals(inputMappingType)) {
            return TextBuilderConfigBuilder.getInstance();
        } else if (EventBuilderConstants.EB_MAP_MAPPING_TYPE.equals(inputMappingType)) {
            return MapBuilderConfigBuilder.getInstance();
        } else if (EventBuilderConstants.EB_JSON_MAPPING_TYPE.equals(inputMappingType)) {
            return JsonBuilderConfigBuilder.getInstance();
        } else {
            throw new UnsupportedOperationException(inputMappingType + " input mapping not yet supported.");
        }
    }

    public static void executeDeployment(String eventBuilderPath,
                                         AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        EventBuilderDeployer deployer = EventBuilderConfigHelper.getEventBuilderDeployer(axisConfiguration);
        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(eventBuilderPath));
        deployer.executeManualDeployment(deploymentFileData);
    }

    public static void executeUndeployment(String filePath, AxisConfiguration axisConfiguration) {
        EventBuilderDeployer deployer = EventBuilderConfigHelper.getEventBuilderDeployer(axisConfiguration);
        deployer.executeManualUndeployment(filePath);
    }

    public static void reload(String filePath, AxisConfiguration axisConfiguration) {
        EventBuilderDeployer deployer = EventBuilderConfigHelper.getEventBuilderDeployer(axisConfiguration);
        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(filePath));
        try {
            deployer.processUndeployment(filePath);
            deployer.processDeployment(deploymentFileData);
        } catch (DeploymentException e) {
            throw new EventBuilderConfigurationException("Deployment exception when trying to reload configuration file:" + e.getMessage(), e);
        }
    }

    private static EventBuilderDeployer getEventBuilderDeployer(
            AxisConfiguration axisConfiguration) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfiguration.getConfigurator();
        return (EventBuilderDeployer) deploymentEngine.getDeployer(EventBuilderConstants.EB_CONFIG_DIRECTORY, EventBuilderConstants.EB_CONFIG_FILE_EXTENSION);
    }
}
