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

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;

import java.io.File;
import java.util.List;

public class EventBuilderUtil {

    public static String deriveEventBuilderNameFrom(String filename) {
        int beginIndex = 0;
        int endIndex = filename.lastIndexOf(EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT);
        if (filename.contains(File.separator)) {
            beginIndex = filename.lastIndexOf(File.separator) + 1;
        }
        return filename.substring(beginIndex, endIndex);
    }

    public static String deriveConfigurationFilenameFrom(String filePath) {
        int beginIndex = 0;
        int endIndex = filePath.length();
        if (filePath.contains(File.separator)) {
            beginIndex = filePath.lastIndexOf(File.separator) + 1;
        }
        return filePath.substring(beginIndex, endIndex);
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

    public static String getStreamIdFrom(EventBuilderConfiguration eventBuilderConfiguration) {
        String streamId = null;
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getToStreamName() != null && !eventBuilderConfiguration.getToStreamName().isEmpty()) {
            streamId = eventBuilderConfiguration.getToStreamName() + EventBuilderConstants.STREAM_NAME_VER_DELIMITER +
                       ((eventBuilderConfiguration.getToStreamVersion() != null && !eventBuilderConfiguration.getToStreamVersion().isEmpty()) ?
                        eventBuilderConfiguration.getToStreamVersion() : EventBuilderConstants.DEFAULT_STREAM_VERSION);
        }

        return streamId;
    }

    public static void addAttributesToStreamDefinition(StreamDefinition streamDefinition,
                                                       List<InputMappingAttribute> inputMappingAttributeList,
                                                       String propertyType) {
        if (propertyType.equals("meta")) {
            for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
                streamDefinition.addMetaData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            }
        } else if (propertyType.equals("correlation")) {
            for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
                streamDefinition.addCorrelationData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            }
        } else if (propertyType.equals("payload")) {
            for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
                streamDefinition.addPayloadData(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType());
            }
        }
    }

    public static String generateFilePath(String filename,
                                          AxisConfiguration axisConfiguration) {
        File repoDir = new File(axisConfiguration.getRepository().getPath());
        if (!repoDir.exists()) {
            if (repoDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory to add tenant specific event builder configuration file :" + filename);
            }
        }
        File subDir = new File(repoDir.getAbsolutePath() + File.separator + EventBuilderConstants.EB_CONFIG_DIRECTORY);
        if (!subDir.exists()) {
            if (!subDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory " + EventBuilderConstants.EB_CONFIG_DIRECTORY
                                                             + " to add tenant specific event builder configuration file:" + filename);
            }
        }
        return subDir.getAbsolutePath() + File.separator + filename;
    }

    public static String generateFilePath(EventBuilderConfiguration eventBuilderConfiguration,
                                          AxisConfiguration axisConfiguration) {
        String eventBuilderName = eventBuilderConfiguration.getEventBuilderName();
        File repoDir = new File(axisConfiguration.getRepository().getPath());
        if (!repoDir.exists()) {
            if (repoDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory to add tenant specific event builder :" + eventBuilderName);
            }
        }
        File subDir = new File(repoDir.getAbsolutePath() + File.separator + EventBuilderConstants.EB_CONFIG_DIRECTORY);
        if (!subDir.exists()) {
            if (!subDir.mkdir()) {
                throw new EventBuilderConfigurationException("Cannot create directory " + EventBuilderConstants.EB_CONFIG_DIRECTORY + " to add tenant specific event builder :" + eventBuilderName);
            }
        }
        return subDir.getAbsolutePath() + File.separator + eventBuilderName + EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
    }

}
