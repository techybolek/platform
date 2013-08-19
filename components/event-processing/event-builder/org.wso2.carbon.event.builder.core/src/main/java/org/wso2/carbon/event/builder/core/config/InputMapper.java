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

package org.wso2.carbon.event.builder.core.config;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

public interface InputMapper {
    /**
     * Converts the passed in object and returns an object array with attributes as array elements.
     *
     * @param obj the object to be converted
     */
    public void processInputEvent(Object obj);

    /**
     * Returns true if the stream definition passed in is compatible with the event builder configuration
     *
     * @param eventBuilderConfiguration the event builder configuration to be checked against
     * @param exportedStreamDefinition  the stream definition to be tested
     * @return {@value true} if it is compatible, {@value false} otherwise
     */
    public boolean isStreamDefinitionValidForConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            StreamDefinition exportedStreamDefinition);

    /**
     * Creates the exported stream definition based on the event builder configuration of this mapper
     *
     * @return the stream definition that will be exposed from this mapper
     */
    public StreamDefinition createExportedStreamDefinition();

    /**
     * Creates a mapping based on the input stream definition.
     *
     * @param eventDefinition
     * @throws EventBuilderConfigurationException
     *
     */
    public void createMapping(Object eventDefinition)
            throws EventBuilderConfigurationException;

    /**
     * Validates the incoming stream definition with the attributes expected by the event builder
     *
     * @param inputStreamDefinition
     * @throws EventBuilderConfigurationException
     *
     */
    public void validateInputStreamAttributes(StreamDefinition inputStreamDefinition)
            throws EventBuilderConfigurationException;

}

