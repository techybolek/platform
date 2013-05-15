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

package org.wso2.carbon.transport.adaptor.core;

import java.util.List;

/**
 * this class is used to transfer the transport proxy type details to the UI. UI renders the
 * properties according to the properties specified here.
 */
public class TransportAdaptorDto {


    /**
     * logical name of this type
     */
    private String transportAdaptorTypeName;

    /**
     * Adaptor type that supports
     */

    public enum TransportAdaptorType {
        IN, OUT, INOUT
    }

    ;

    public enum MessageType {
        XML, TUPLE, MAP, TEXT, JSON
    }

    ;

    /**
     * Input Message types supported by the adaptor
     */
    private List<MessageType> supportedInputMessageTypes;

    /**
     * Output Message types supported by the adaptor
     */
    private List<MessageType> supportedOutputMessageTypes;

    /**
     * types that supported by the transport adaptor
     */
    private TransportAdaptorType supportedTransportAdaptorType;


    /**
     * Property Lists
     */
    private List<Property> adaptorOutPropertyList;
    private List<Property> adaptorInPropertyList;
    private List<Property> adaptorCommonPropertyList;


    public void addAdaptorInProperty(Property adaptorInProperty) {
        this.adaptorInPropertyList.add(adaptorInProperty);
    }

    public void addAdaptorOutProperty(Property adaptorOutProperty) {
        this.adaptorOutPropertyList.add(adaptorOutProperty);
    }

    public void addAdaptorCommonProperty(Property adaptorCommonProperty) {
        this.adaptorCommonPropertyList.add(adaptorCommonProperty);
    }

    public List<Property> getAdaptorOutPropertyList() {
        return adaptorOutPropertyList;
    }

    public void setAdaptorOutPropertyList(List<Property> adaptorOutPropertyList) {
        this.adaptorOutPropertyList = adaptorOutPropertyList;
    }

    public List<Property> getAdaptorInPropertyList() {
        return adaptorInPropertyList;
    }

    public void setAdaptorInPropertyList(List<Property> adaptorInPropertyList) {
        this.adaptorInPropertyList = adaptorInPropertyList;
    }

    public List<Property> getAdaptorCommonPropertyList() {
        return adaptorCommonPropertyList;
    }

    public void setAdaptorCommonPropertyList(List<Property> adaptorCommonPropertyList) {
        this.adaptorCommonPropertyList = adaptorCommonPropertyList;
    }

    public String getTransportAdaptorTypeName() {
        return transportAdaptorTypeName;
    }

    public void setTransportAdaptorTypeName(String transportAdaptorTypeName) {
        this.transportAdaptorTypeName = transportAdaptorTypeName;
    }

    public List<MessageType> getSupportedInputMessageType() {
        return supportedInputMessageTypes;
    }

    public void setSupportedInputMessageType(List<MessageType> supportInputMessageType) {
        this.supportedInputMessageTypes = supportInputMessageType;
    }

    public List<MessageType> getSupportedOutputMessageType() {
        return supportedOutputMessageTypes;
    }

    public void setSupportedOutputMessageType(List<MessageType> supportOutputMessageType) {
        this.supportedOutputMessageTypes = supportOutputMessageType;
    }


    public TransportAdaptorType getSupportedTransportAdaptorType() {
        return supportedTransportAdaptorType;
    }

    public void setSupportedTransportAdaptorType(TransportAdaptorType supportedTransportAdaptorType) {
        this.supportedTransportAdaptorType = supportedTransportAdaptorType;
    }

}
