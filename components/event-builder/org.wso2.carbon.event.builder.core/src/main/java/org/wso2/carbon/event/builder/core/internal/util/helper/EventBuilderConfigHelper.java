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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.type.json.JsonBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.map.MapBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.text.TextBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigurationFile;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.XmlFormatter;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.message.MessageDto;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;

import javax.xml.namespace.QName;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EventBuilderConfigHelper {

    private static final Log log = LogFactory.getLog(EventBuilderConfigHelper.class);

    public static void saveConfigurationToFileSystem(
            EventBuilderConfiguration eventBuilderConfiguration,
            String filePath)
            throws EventBuilderConfigurationException {
        String inputMappingType = eventBuilderConfiguration.getInputMapping().getMappingType();
        OMElement ebConfigElement = getEventBuilderConfigBuilder(inputMappingType).eventBuilderConfigurationToOM(eventBuilderConfiguration);
        save(ebConfigElement.toString(), eventBuilderConfiguration.getEventBuilderName(), filePath);
    }

    public static void save(String eventBuilderConfigXml, String eventBuilderName,
                            String ebConfigFilePath) {
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(ebConfigFilePath));
            out.write(XmlFormatter.format(eventBuilderConfigXml));
            out.close();
            log.info("Event builder configuration for " + eventBuilderName + " saved in the filesystem");
        } catch (IOException e) {
            log.error("Error while saving " + eventBuilderName, e);
        }
    }

    public static void deleteEventBuilderConfigurationFile(
            EventBuilderConfigurationFile eventBuilderConfigurationFile)
            throws EventBuilderConfigurationException {
        String filePath = eventBuilderConfigurationFile.getFilePath();
        try {
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length());
            File file = new File(filePath);
            if (file.exists()) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + fileName);
                } else {
                    log.info(fileName + " is deleted from the file system");
                }
            }
        } catch (Exception e) {
            throw new EventBuilderConfigurationException("Error while deleting the event builder ", e);
        }
    }

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
}
