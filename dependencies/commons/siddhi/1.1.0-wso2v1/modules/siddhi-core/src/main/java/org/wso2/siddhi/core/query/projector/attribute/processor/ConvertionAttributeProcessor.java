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
package org.wso2.siddhi.core.query.projector.attribute.processor;

import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.query.projector.attribute.convertor.Convertor;
import org.wso2.siddhi.core.query.projector.attribute.factory.AttributeConverterFactory;
import org.wso2.siddhi.core.util.parser.ExecutorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.QueryEventStream;

import java.util.List;

public class ConvertionAttributeProcessor implements NonGroupingAttributeProcessor {
    private ExpressionExecutor expressionExecutor;
    private AttributeConverterFactory attributeConverterFactory;
    private Convertor converter;

    public ConvertionAttributeProcessor(Expression[] expressions, List<QueryEventStream> queryEventStreamList, AttributeConverterFactory attributeConverterFactory) {
        this.expressionExecutor = ExecutorParser.parseExpression(expressions[0], queryEventStreamList, null);
        this.attributeConverterFactory = attributeConverterFactory;
        this.converter = attributeConverterFactory.createConvertor(expressionExecutor.getType());
    }

    @Override
    public Attribute.Type getOutputType() {
        return converter.getType();
    }

    public Object process(AtomicEvent event){
        return converter.convert(expressionExecutor.execute(event));
    }

    @Override
    public void lock() {

    }

    @Override
    public void unlock() {

    }
}
