/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.hive.annotation;

import org.wso2.carbon.analytics.hive.extension.AbstractHiveAnalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractHiveAnnotation extends AbstractHiveAnalyzer {


    private String annotationName = "";

    private String scope = "";

    private Set<String> paramValues = new HashSet<String>();



    private Map<String, String> parameters = new HashMap<String, String>();

    public abstract void init();

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public void setAnnotationName(String annotationName) {
        this.annotationName = annotationName;
    }

    public void addParameter(String key) {
        paramValues.add(key);
    }

    public boolean isParameter(String key) {

        return paramValues.contains(key);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public boolean validate(Set<String> params){

        if (paramValues != null && params != null && paramValues.size() == params.size()) {
            return paramValues.containsAll(params);
        } else {
            return false;
        }

    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }


}
