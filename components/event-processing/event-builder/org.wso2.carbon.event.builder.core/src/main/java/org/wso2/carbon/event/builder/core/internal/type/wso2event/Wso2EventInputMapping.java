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

package org.wso2.carbon.event.builder.core.internal.type.wso2event;

import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import java.util.ArrayList;
import java.util.List;

public class Wso2EventInputMapping implements InputMapping {

    List<InputMappingAttribute> metaInputMappingAttributes;
    List<InputMappingAttribute> correlationInputMappingAttributes;
    List<InputMappingAttribute> payloadInputMappingAttributes;


    public Wso2EventInputMapping() {
        this.metaInputMappingAttributes = new ArrayList<InputMappingAttribute>();
        this.correlationInputMappingAttributes = new ArrayList<InputMappingAttribute>();
        this.payloadInputMappingAttributes = new ArrayList<InputMappingAttribute>();
    }

    public void addMetaInputEventAttribute(
            InputMappingAttribute inputMappingAttribute) {
        this.metaInputMappingAttributes.add(inputMappingAttribute);
    }

    public void addCorrelationInputEventAttribute(
            InputMappingAttribute inputMappingAttribute) {
        this.correlationInputMappingAttributes.add(inputMappingAttribute);
    }

    public void addPayloadInputEventAttribute(
            InputMappingAttribute inputMappingAttribute) {
        this.payloadInputMappingAttributes.add(inputMappingAttribute);
    }

    public List<InputMappingAttribute> getMetaInputMappingAttributes() {
        return metaInputMappingAttributes;
    }

    public List<InputMappingAttribute> getCorrelationInputMappingAttributes() {
        return correlationInputMappingAttributes;
    }

    public List<InputMappingAttribute> getPayloadInputMappingAttributes() {
        return payloadInputMappingAttributes;
    }

    @Override
    public String getMappingType() {
        return EventBuilderConstants.EB_WSO2EVENT_MAPPING_TYPE;
    }

}
