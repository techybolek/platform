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
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.expression.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsMatchExecutorFunction extends ExecutorFunction {

    private Pattern pattern;
    private ExpressionExecutor expressionExecutor;

    @Override
    public Attribute.Type getType() {
        return Attribute.Type.BOOL;
    }

    @Override
    public void init() {
        if (attributeSize != 2) {
            throw new QueryCreationException("IsMatch has to have 2 expressions regex and the attribute, currently " + attributeSize + " expressions provided");
        }
        ExpressionExecutor regexExecutor = attributeExpressionExecutors.get(0);
        if (regexExecutor.getType() != Attribute.Type.STRING && regexExecutor instanceof ConstantExpressionExecutor) {
            throw new QueryCreationException("IsMatch expects regex string input expression but found " + regexExecutor.getType());
        }
        expressionExecutor = attributeExpressionExecutors.get(1);

//        patternString = (String) expressionExecutor.execute(null);
        pattern = Pattern.compile((String) regexExecutor.execute(null));

    }

    @Override
    public Object execute(AtomicEvent event) {
        return process(expressionExecutor.execute(event));
    }

    protected Object process(Object obj) {
        Matcher matcher = pattern.matcher(obj.toString());
        return matcher.matches();
    }

}
