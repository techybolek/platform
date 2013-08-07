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

package org.wso2.carbon.event.builder.core.internal.type.xml;

import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import java.util.ArrayList;
import java.util.List;

public class XMLInputMapping implements InputMapping {

    private XPathDefinition xpathDefinition;
    private List<InputMappingAttribute> inputMappingAttributes;
    private boolean batchProcessingEnabled = false;

    public XMLInputMapping() {
        this.inputMappingAttributes = new ArrayList<InputMappingAttribute>();
    }

    public boolean isBatchProcessingEnabled() {
        return batchProcessingEnabled;
    }

    public void setBatchProcessingEnabled(boolean batchProcessingEnabled) {
        this.batchProcessingEnabled = batchProcessingEnabled;
    }

    public XPathDefinition getXpathDefinition() {
        return xpathDefinition;
    }

    public void setXpathDefinition(XPathDefinition xpathDefinition) {
        this.xpathDefinition = xpathDefinition;
    }

    public List<InputMappingAttribute> getInputMappingAttributes() {
        return inputMappingAttributes;
    }

    public void addInputMappingAttribute(InputMappingAttribute inputMappingAttribute) {
        inputMappingAttributes.add(inputMappingAttribute);
    }

    @Override
    public String getMappingType() {
        return EventBuilderConstants.EB_XML_MAPPING_TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XMLInputMapping that = (XMLInputMapping) o;

        if (batchProcessingEnabled != that.batchProcessingEnabled) {
            return false;
        }
        if (!inputMappingAttributes.equals(that.inputMappingAttributes)) {
            return false;
        }
        if (xpathDefinition != null ? !xpathDefinition.equals(that.xpathDefinition) : that.xpathDefinition != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = xpathDefinition != null ? xpathDefinition.hashCode() : 0;
        result = 31 * result + inputMappingAttributes.hashCode();
        result = 31 * result + (batchProcessingEnabled ? 1 : 0);
        return result;
    }
}
