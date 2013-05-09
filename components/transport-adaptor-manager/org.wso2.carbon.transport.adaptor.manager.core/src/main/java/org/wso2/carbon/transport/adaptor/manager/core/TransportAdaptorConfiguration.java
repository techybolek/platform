/*
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

package org.wso2.carbon.transport.adaptor.manager.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * represents details of a particular transport adaptor connection
 */
public class TransportAdaptorConfiguration {

    /**
     * logical name use to identify this configuration
     */
    private String name;

    /**
     * transport  type for this configuration
     */
    private String type;

    /**
     * Map contains the input adaptor property configuration details
     */
    private Map<String, String> inputAdaptorProperties;

    /**
     * Map contains the output adaptor property configuration details
     */
    private Map<String, String> outputAdaptorProperties;

    /**
     * Map contains the common adaptor property configuration details
     */
    private Map<String, String> commonAdaptorProperties;

    public TransportAdaptorConfiguration() {
        this.inputAdaptorProperties = new ConcurrentHashMap<String, String>();
        this.outputAdaptorProperties = new ConcurrentHashMap<String, String>();
        this.commonAdaptorProperties = new ConcurrentHashMap<String, String>();
    }

    public void addInputAdaptorProperty(String name, String value) {
        this.inputAdaptorProperties.put(name, value);
    }

    public void addOutputAdaptorProperty(String name, String value) {
        this.outputAdaptorProperties.put(name, value);
    }

    public void addCommonAdaptorProperty(String name, String value) {
        this.commonAdaptorProperties.put(name, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getInputAdaptorProperties() {
        return inputAdaptorProperties;
    }

    public void setInputAdaptorProperties(Map<String, String> inputAdaptorProperties) {
        this.inputAdaptorProperties = inputAdaptorProperties;
    }

    public Map<String, String> getOutputAdaptorProperties() {
        return outputAdaptorProperties;
    }

    public void setOutputAdaptorProperties(Map<String, String> outputAdaptorProperties) {
        this.outputAdaptorProperties = outputAdaptorProperties;
    }

    public Map<String, String> getCommonAdaptorProperties() {
        return commonAdaptorProperties;
    }

    public void setCommonAdaptorProperties(Map<String, String> commonAdaptorProperties) {
        this.commonAdaptorProperties = commonAdaptorProperties;
    }
}

