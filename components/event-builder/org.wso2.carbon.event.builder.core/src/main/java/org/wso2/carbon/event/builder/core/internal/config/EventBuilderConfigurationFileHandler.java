package org.wso2.carbon.event.builder.core.internal.config;/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class EventBuilderConfigurationFileHandler {

    private static final Log log = LogFactory.getLog(EventBuilderConfigurationFileHandler.class);

    public static void saveConfigurationToFileSystem(OMElement ebConfigElement, String eventBuilderName, String pathInFileSystem) throws EventBuilderConfigurationException {

        EventBuilderConfigurationFileHandler.save(ebConfigElement.toString(), eventBuilderName, pathInFileSystem);

    }

    public static void save(String event, String eventBuilderName,
                            String ebConfigFilePath) {
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(ebConfigFilePath));
            out.write(new XmlFormatter().format(event));
            out.close();
            log.info("Event builder configuration for " + eventBuilderName + " saved in the filesystem");
        } catch (IOException e) {
            log.error("Error while saving " + eventBuilderName, e);
        }

    }


}
