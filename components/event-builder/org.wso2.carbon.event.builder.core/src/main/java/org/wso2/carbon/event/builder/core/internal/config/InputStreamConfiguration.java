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

package org.wso2.carbon.event.builder.core.internal.config;

import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;

public class InputStreamConfiguration {

    private String transportAdaptorName;

    private String transportAdaptorType;

    private InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration;

    public InputTransportAdaptorMessageConfiguration getInputTransportAdaptorMessageConfiguration() {
        return inputTransportAdaptorMessageConfiguration;
    }

    public void setInputTransportAdaptorMessageConfiguration(
            InputTransportAdaptorMessageConfiguration inputTransportAdaptorMessageConfiguration) {
        this.inputTransportAdaptorMessageConfiguration = inputTransportAdaptorMessageConfiguration;
    }

    public String getTransportAdaptorType() {
        return transportAdaptorType;
    }

    public void setTransportAdaptorType(String transportAdaptorType) {
        this.transportAdaptorType = transportAdaptorType;
    }

    public String getTransportAdaptorName() {
        return transportAdaptorName;
    }

    public void setTransportAdaptorName(String transportAdaptorName) {
        this.transportAdaptorName = transportAdaptorName;
    }


}
