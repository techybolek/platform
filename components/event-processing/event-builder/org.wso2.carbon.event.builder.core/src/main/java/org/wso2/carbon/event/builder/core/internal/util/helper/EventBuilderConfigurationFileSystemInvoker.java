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
import org.wso2.carbon.event.builder.core.internal.util.XmlFormatter;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;

public class EventBuilderConfigurationFileSystemInvoker {
    private static final Log log = LogFactory.getLog(EventBuilderConfigurationFileSystemInvoker.class);

    public static void saveConfigurationToFileSystem(
            EventBuilderConfiguration eventBuilderConfiguration,
            String filePath)
            throws EventBuilderConfigurationException {
        String inputMappingType = eventBuilderConfiguration.getInputMapping().getMappingType();
        OMElement ebConfigElement = EventBuilderConfigHelper.getEventBuilderConfigBuilder(inputMappingType).eventBuilderConfigurationToOM(eventBuilderConfiguration);
        save(ebConfigElement.toString(), filePath);
    }

    public static void save(String eventBuilderConfigXml, String ebConfigFilePath) {
        String filename = new File(ebConfigFilePath).getName();
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(ebConfigFilePath));
            out.write(XmlFormatter.format(eventBuilderConfigXml));
            out.close();
            log.info("Event builder configuration saved to the filesystem :" + filename);
        } catch (IOException e) {
            log.error("Error while saving event builder configuration: " + filename, e);
        }
    }

    public static void deleteConfigurationFromFileSystem(String filePath)
            throws EventBuilderConfigurationException {
        try {
            File file = new File(filePath);
            String filename = file.getName();
            if (file.exists()) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + filename);
                } else {
                    log.info("Event builder configuration deleted from the file system :" + filename);
                }
            }
        } catch (Exception e) {
            throw new EventBuilderConfigurationException("Error while deleting the event builder :" + e.getMessage(), e);
        }
    }


    public static String readEventBuilderConfigurationFile(String filePath)
            throws EventBuilderConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventBuilderConfigurationException("Event builder file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventBuilderConfigurationException("Cannot read the event builder file : " + e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file : " + e.getMessage(), e);
            }
        }
        return stringBuilder.toString().trim();
    }

}
