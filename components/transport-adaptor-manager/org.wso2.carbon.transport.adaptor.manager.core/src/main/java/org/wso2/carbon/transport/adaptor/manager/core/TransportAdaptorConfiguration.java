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

/**
 * represents details of a particular transport adaptor connection
 */
public class TransportAdaptorConfiguration {


    private String name;

    private String type;

    private TransportAdaptorPropertyConfiguration inputAdaptorPropertyConfiguration = null;

    private TransportAdaptorPropertyConfiguration outputAdaptorPropertyConfiguration = null;

    private TransportAdaptorPropertyConfiguration commonAdaptorPropertyConfiguration = null;

    public TransportAdaptorPropertyConfiguration getInputAdaptorPropertyConfiguration() {
        return inputAdaptorPropertyConfiguration;
    }

    public void setInputAdaptorPropertyConfiguration(
            TransportAdaptorPropertyConfiguration inputAdaptorPropertyConfiguration) {
        this.inputAdaptorPropertyConfiguration = inputAdaptorPropertyConfiguration;
    }

    public TransportAdaptorPropertyConfiguration getOutputAdaptorPropertyConfiguration() {
        return outputAdaptorPropertyConfiguration;
    }

    public void setOutputAdaptorPropertyConfiguration(
            TransportAdaptorPropertyConfiguration outputAdaptorPropertyConfiguration) {
        this.outputAdaptorPropertyConfiguration = outputAdaptorPropertyConfiguration;
    }

    public TransportAdaptorPropertyConfiguration getCommonAdaptorPropertyConfiguration() {
        return commonAdaptorPropertyConfiguration;
    }

    public void setCommonAdaptorPropertyConfiguration(
            TransportAdaptorPropertyConfiguration commonAdaptorPropertyConfiguration) {
        this.commonAdaptorPropertyConfiguration = commonAdaptorPropertyConfiguration;
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

}

