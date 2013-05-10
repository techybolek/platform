/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.expression;

import org.wso2.siddhi.query.api.extension.Extension;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpressionExtension extends Expression implements Extension {


    private String extensionName;
    private String functionName;
    private Expression[] parameters;

    public ExpressionExtension(String extensionName, String functionName, Expression... parameters) {
        this.extensionName = extensionName;
        this.functionName = functionName;
        this.parameters = parameters;
    }

    public String getNamespace() {
        return extensionName;
    }

    public String getFunction() {
        return functionName;
    }

    public Expression[] getParameters() {
        return parameters;
    }

    @Override
    protected void validate(List<QueryEventSource> queryEventSources, String streamReferenceId,
                            boolean processInStreamDefinition) {
        for (Expression expression : parameters) {
            expression.validate(queryEventSources, streamReferenceId, processInStreamDefinition);
        }
    }

    @Override
    public String toString() {
        return "ExpressionExtension{" +
               "extensionName='" + extensionName + '\'' +
               ", functionName='" + functionName + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExpressionExtension that = (ExpressionExtension) o;

        if (extensionName != null ? !extensionName.equals(that.extensionName) : that.extensionName != null) {
            return false;
        }
        if (functionName != null ? !functionName.equals(that.functionName) : that.functionName != null) {
            return false;
        }
        if (!Arrays.equals(parameters, that.parameters)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = extensionName != null ? extensionName.hashCode() : 0;
        result = 31 * result + (functionName != null ? functionName.hashCode() : 0);
        result = 31 * result + (parameters != null ? Arrays.hashCode(parameters) : 0);
        return result;
    }

    protected Set<String> getDependencySet() {
        Set<String> dependencySet = new HashSet<String>();
        for (Expression expression : parameters) {
            dependencySet.addAll(expression.getDependencySet());
        }
        return dependencySet;
    }
}