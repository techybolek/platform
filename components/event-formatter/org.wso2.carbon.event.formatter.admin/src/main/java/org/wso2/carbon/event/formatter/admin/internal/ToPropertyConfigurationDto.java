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

package org.wso2.carbon.event.formatter.admin.internal;


import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;

public class ToPropertyConfigurationDto {

    private String transportAdaptorName;

    private String transportAdaptorType;

    private EventFormatterPropertyDto[] outputTransportAdaptorMessageConfiguration;

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

    public EventFormatterPropertyDto[] getOutputTransportAdaptorMessageConfiguration() {
        return outputTransportAdaptorMessageConfiguration;
    }

    public void setOutputTransportAdaptorMessageConfiguration(
            EventFormatterPropertyDto[] outputTransportAdaptorMessageConfiguration) {
        this.outputTransportAdaptorMessageConfiguration = outputTransportAdaptorMessageConfiguration;
    }
}
