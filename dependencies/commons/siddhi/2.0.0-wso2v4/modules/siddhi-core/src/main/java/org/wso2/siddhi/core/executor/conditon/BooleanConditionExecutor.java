/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.executor.conditon;

import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.expression.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.VariableExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.Set;

public class BooleanConditionExecutor implements ConditionExecutor {

    public ExpressionExecutor expressionExecutor;

    public BooleanConditionExecutor(ExpressionExecutor expressionExecutor) {
        if (expressionExecutor.getType() != Attribute.Type.BOOL) {
            throw new OperationNotSupportedException("Boolean condition cannot handle non boolean values");
        }
        this.expressionExecutor = expressionExecutor;
    }

    public boolean execute(AtomicEvent event) {
        Object value = expressionExecutor.execute(event);
        return !(value == null) && (Boolean) value;
    }

    @Override
    public String constructFilterQuery(AtomicEvent newEvent, int level) {
        return constructQuery(newEvent, level, null);
    }

    @Override
    public String constructSQLPredicate(AtomicEvent newEvent, TableDefinition tableDefinition) {
        return constructQuery(newEvent, SQL_LEVEL, tableDefinition);
    }

    public String constructQuery(AtomicEvent newEvent, int level, TableDefinition tableDefinition) {
        if (expressionExecutor instanceof ConstantExpressionExecutor) {
            Object obj = expressionExecutor.execute(newEvent);
            if (obj instanceof Boolean) {
                return obj.toString();
            } else {
                return "*";
            }
        } else if (expressionExecutor instanceof VariableExpressionExecutor) {
            if (level == SQL_LEVEL) {
                return ((VariableExpressionExecutor) expressionExecutor).constructSQLPredicate(newEvent, tableDefinition);
            } else {
                return ((VariableExpressionExecutor) expressionExecutor).constructFilterQuery(newEvent, level);
            }
        } else {
            return "*";
        }
    }

}
