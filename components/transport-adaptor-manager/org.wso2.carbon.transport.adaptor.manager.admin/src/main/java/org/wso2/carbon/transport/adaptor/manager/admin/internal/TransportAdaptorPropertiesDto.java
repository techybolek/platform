package org.wso2.carbon.transport.adaptor.manager.admin.internal;/*
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


public class TransportAdaptorPropertiesDto {

    private TransportAdaptorPropertyDto[] inputTransportAdaptorPropertyDtos;
    private TransportAdaptorPropertyDto[] outputTransportAdaptorPropertyDtos;
    private TransportAdaptorPropertyDto[] commonTransportAdaptorPropertyDtos;

    public TransportAdaptorPropertyDto[] getInputTransportAdaptorPropertyDtos() {
        return inputTransportAdaptorPropertyDtos;
    }

    public void setInputTransportAdaptorPropertyDtos(
            TransportAdaptorPropertyDto[] inputTransportAdaptorPropertyDtos) {
        this.inputTransportAdaptorPropertyDtos = inputTransportAdaptorPropertyDtos;
    }

    public TransportAdaptorPropertyDto[] getOutputTransportAdaptorPropertyDtos() {
        return outputTransportAdaptorPropertyDtos;
    }

    public void setOutputTransportAdaptorPropertyDtos(
            TransportAdaptorPropertyDto[] outputTransportAdaptorPropertyDtos) {
        this.outputTransportAdaptorPropertyDtos = outputTransportAdaptorPropertyDtos;
    }

    public TransportAdaptorPropertyDto[] getCommonTransportAdaptorPropertyDtos() {
        return commonTransportAdaptorPropertyDtos;
    }

    public void setCommonTransportAdaptorPropertyDtos(
            TransportAdaptorPropertyDto[] commonTransportAdaptorPropertyDtos) {
        this.commonTransportAdaptorPropertyDtos = commonTransportAdaptorPropertyDtos;
    }
}
