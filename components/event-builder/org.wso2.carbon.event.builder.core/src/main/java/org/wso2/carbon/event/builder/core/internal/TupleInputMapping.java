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

package org.wso2.carbon.event.builder.core.internal;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.InputMapping;

import java.util.List;

public class TupleInputMapping implements InputMapping {
    private String inputName;
    private InputDataType inputDataType;
    private Attribute attribute;
    private int inputStreamPosition;
    private int streamPosition;

    public TupleInputMapping(String inputName, InputDataType inputDataType, String attributeName, AttributeType attributeType, StreamDefinition fromStreamDefinition, int streamPosition) {
        this.attribute = new Attribute(attributeName, attributeType);
        this.inputName = inputName;
        this.inputDataType = inputDataType;
        this.streamPosition = streamPosition;
        this.inputStreamPosition = -1;
        List<Attribute> attributeList;
        switch (inputDataType) {
            case META_DATA:
                attributeList = fromStreamDefinition.getMetaData();
                break;
            case CORRELATION_DATA:
                attributeList = fromStreamDefinition.getCorrelationData();
                break;
            case PAYLOAD_DATA:
            default:
                attributeList = fromStreamDefinition.getPayloadData();
        }

        for (int i = 0; i < attributeList.size(); i++) {
            if (attributeList.get(i).getName().equals(inputName)) {
                this.inputStreamPosition = i;
                break;
            }
        }
    }

    public int getInputStreamPosition() {
        return inputStreamPosition;
    }

    public String getInputName() {
        return inputName;
    }

    public InputDataType getInputDataType() {
        return inputDataType;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public int getStreamPosition() {
        return streamPosition;
    }

    public enum InputDataType {
        PAYLOAD_DATA, META_DATA, CORRELATION_DATA
    }

}
