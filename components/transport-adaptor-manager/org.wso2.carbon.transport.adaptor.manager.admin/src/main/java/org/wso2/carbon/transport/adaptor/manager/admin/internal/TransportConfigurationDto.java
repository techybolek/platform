package org.wso2.carbon.transport.adaptor.manager.admin.internal;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Transport Configuration Details are stored
 */
public class TransportConfigurationDto {
    private String transportName;
    private String transportType;

    // array of transport adaptor input properties
    private TransportPropertyDto[] inputTransportPropertyDtos;

    // array of transport adaptor output properties
    private TransportPropertyDto[] outputTransportPropertyDtos;

    // array of transport adaptor common properties
    private TransportPropertyDto[] commonTransportPropertyDtos;

    // to keep track of current index of input adaptor properties
    private int currentInputTransportPropertyIndex = 0;

    // to keep track of current index of input adaptor properties
    private int currentOutputTransportPropertyIndex = 0;

    // to keep track of current index of input adaptor properties
    private int currentCommonTransportPropertyIndex = 0;

    public TransportConfigurationDto(String transportName, String transportType, int numberOfInputTransportProperties, int numberOfOutputTransportProperties, int numberOfCommonTransportProperties) {
        this.transportName = transportName;
        this.transportType = transportType;
        inputTransportPropertyDtos = new TransportPropertyDto[numberOfInputTransportProperties];
        outputTransportPropertyDtos = new TransportPropertyDto[numberOfOutputTransportProperties];
        commonTransportPropertyDtos = new TransportPropertyDto[numberOfCommonTransportProperties];
    }

    public String getTransportName() {
        return transportName;
    }

    public String getTransportType() {
        return transportType;
    }

    public TransportPropertyDto[] getInputTransportPropertyDtos() {
        return inputTransportPropertyDtos;
    }

    public void addInputTransportProperty(String key, String value) {
        inputTransportPropertyDtos[currentInputTransportPropertyIndex] = new TransportPropertyDto(key, value);
        currentInputTransportPropertyIndex++;
    }

    public TransportPropertyDto[] getOutputTransportPropertyDtos() {
        return outputTransportPropertyDtos;
    }

    public void addOutputTransportProperty(String key, String value) {
        outputTransportPropertyDtos[currentOutputTransportPropertyIndex] = new TransportPropertyDto(key, value);
        currentOutputTransportPropertyIndex++;
    }

    public TransportPropertyDto[] getCommonTransportPropertyDtos() {
        return commonTransportPropertyDtos;
    }

    public void addCommonTransportProperty(String key, String value) {
        commonTransportPropertyDtos[currentCommonTransportPropertyIndex] = new TransportPropertyDto(key, value);
        currentCommonTransportPropertyIndex++;
    }


}
