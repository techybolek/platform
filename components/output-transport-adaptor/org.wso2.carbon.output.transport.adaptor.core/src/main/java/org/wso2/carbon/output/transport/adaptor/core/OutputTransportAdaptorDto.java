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

package org.wso2.carbon.output.transport.adaptor.core;

import java.util.List;

/**
 * this class is used to transfer the transport proxy type details to the UI. UI renders the
 * properties according to the properties specified here.
 */
public class OutputTransportAdaptorDto {


    /**
     * logical name of this type
     */
    private String transportAdaptorTypeName;

    /**
     * Adaptor type that supports
     */

    public enum MessageType {
        XML, WSO2EVENT, MAP, TEXT, JSON
    }

    ;


    private List<MessageType> supportedMessageTypes;

    /**
     * Property Lists
     */
    private List<Property> adaptorPropertyList;

    public String getTransportAdaptorTypeName() {
        return transportAdaptorTypeName;
    }

    public void setTransportAdaptorTypeName(String transportAdaptorTypeName) {
        this.transportAdaptorTypeName = transportAdaptorTypeName;
    }

    public List<MessageType> getSupportedMessageTypes() {
        return supportedMessageTypes;
    }

    public void setSupportedMessageTypes(
            List<MessageType> supportedMessageTypes) {
        this.supportedMessageTypes = supportedMessageTypes;
    }

    public List<Property> getAdaptorPropertyList() {
        return adaptorPropertyList;
    }

    public void setAdaptorPropertyList(List<Property> adaptorPropertyList) {
        this.adaptorPropertyList = adaptorPropertyList;
    }
}
