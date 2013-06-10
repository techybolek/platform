/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.common.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class PolicyEditorDataHolder {
    
    private Map<String, String> categoryMap = new HashMap<String, String>();

    private Map<String, String> attributeIdMap = new HashMap<String, String>();

    private Map<String, String> dataTypeMap = new HashMap<String, String>();
    
    private Map<String, String> functionMap = new HashMap<String, String>();
    
    private Map<String, Set<String>> categoryAttributeIdMap =  new HashMap<String, Set<String>>();

    private Map<String, Set<String>> categoryDataTypeMap=  new HashMap<String, Set<String>>();

    private Map<String, Set<String>> attributeIdDataTypeMap =  new HashMap<String, Set<String>>();

    private Set<String> ruleFunctions;

    private Set<String> targetFunctions;

    private String defaultDataType;

    public String getCategoryUri(String categoryName){
        return categoryMap.get(categoryName);
    }

    public String getAttributeIdUri(String attributeId){
        return attributeIdMap.get(attributeId);
    }

    public String getDataTypeUri(String dataType){
        return dataTypeMap.get(dataType);
    }

    public Map<String, String> getCategoryMap() {
        return categoryMap;
    }

    public void setCategoryMap(Map<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }

    public Map<String, String> getAttributeIdMap() {
        return attributeIdMap;
    }

    public void setAttributeIdMap(Map<String, String> attributeIdMap) {
        this.attributeIdMap = attributeIdMap;
    }

    public Map<String, String> getDataTypeMap() {
        return dataTypeMap;
    }

    public void setDataTypeMap(Map<String, String> dataTypeMap) {
        this.dataTypeMap = dataTypeMap;
    }

    public Map<String, String> getFunctionMap() {
        return functionMap;
    }

    public void setFunctionMap(Map<String, String> functionMap) {
        this.functionMap = functionMap;
    }

    public Map<String, Set<String>> getCategoryAttributeIdMap() {
        return categoryAttributeIdMap;
    }

    public void setCategoryAttributeIdMap(Map<String, Set<String>> categoryAttributeIdMap) {
        this.categoryAttributeIdMap = categoryAttributeIdMap;
    }

    public Map<String, Set<String>> getCategoryDataTypeMap() {
        return categoryDataTypeMap;
    }

    public void setCategoryDataTypeMap(Map<String, Set<String>> categoryDataTypeMap) {
        this.categoryDataTypeMap = categoryDataTypeMap;
    }

    public Map<String, Set<String>> getAttributeIdDataTypeMap() {
        return attributeIdDataTypeMap;
    }

    public void setAttributeIdDataTypeMap(Map<String, Set<String>> attributeIdDataTypeMap) {
        this.attributeIdDataTypeMap = attributeIdDataTypeMap;
    }

    public Set<String> getRuleFunctions() {
        return ruleFunctions;
    }

    public void setRuleFunctions(Set<String> ruleFunctions) {
        this.ruleFunctions = ruleFunctions;
    }

    public Set<String> getTargetFunctions() {
        return targetFunctions;
    }

    public void setTargetFunctions(Set<String> targetFunctions) {
        this.targetFunctions = targetFunctions;
    }

    public String getDefaultDataType() {
        return defaultDataType;
    }

    public void setDefaultDataType(String defaultDataType) {
        this.defaultDataType = defaultDataType;
    }
}
