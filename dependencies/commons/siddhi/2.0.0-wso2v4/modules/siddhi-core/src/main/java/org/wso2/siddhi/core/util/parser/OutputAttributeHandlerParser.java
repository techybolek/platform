/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeProcessorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeProcessorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeProcessorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeProcessorLong;
import org.wso2.siddhi.core.query.selector.attribute.handler.coalesce.CoalesceOutputAttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.handler.count.CountOutputAttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeProcessorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeProcessorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeProcessorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeProcessorLong;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeProcessorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeProcessorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeProcessorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeProcessorLong;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeProcessorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeProcessorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeProcessorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeProcessorLong;
import org.wso2.siddhi.query.api.definition.Attribute;

public class OutputAttributeHandlerParser {


    public static OutputAttributeProcessor createSumAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Sum can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Sum not supported for string");
            case INT:
                return new SumOutputAttributeProcessorInt();
            case LONG:
                return new SumOutputAttributeProcessorLong();
            case FLOAT:
                return new SumOutputAttributeProcessorFloat();
            case DOUBLE:
                return new SumOutputAttributeProcessorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Sum not supported for bool");
        }
        throw new OperationNotSupportedException("Sum not supported for " + type);
    }

    public static OutputAttributeProcessor createAvgAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Avg can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Avg not supported for string");
            case INT:
                return new AvgOutputAttributeProcessorInt();
            case LONG:
                return new AvgOutputAttributeProcessorLong();
            case FLOAT:
                return new AvgOutputAttributeProcessorFloat();
            case DOUBLE:
                return new AvgOutputAttributeProcessorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Avg not supported for bool");
        }
        throw new OperationNotSupportedException("Avg not supported for " + type);
    }

    public static OutputAttributeProcessor createMaxAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Max can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Max not supported for string");
            case INT:
                return new MaxOutputAttributeProcessorInt();
            case LONG:
                return new MaxOutputAttributeProcessorLong();
            case FLOAT:
                return new MaxOutputAttributeProcessorFloat();
            case DOUBLE:
                return new MaxOutputAttributeProcessorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Max not supported for bool");
        }
        throw new OperationNotSupportedException("Max not supported for " + type);
    }


    public static OutputAttributeProcessor createMinAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Min can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Min not supported for string");
            case INT:
                return new MinOutputAttributeProcessorInt();
            case LONG:
                return new MinOutputAttributeProcessorLong();
            case FLOAT:
                return new MinOutputAttributeProcessorFloat();
            case DOUBLE:
                return new MinOutputAttributeProcessorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Min not supported for bool");
        }
        throw new OperationNotSupportedException("Min not supported for " + type);
    }

    public static OutputAttributeProcessor createCountAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Avg can only have one parameter");
        }
        return new CountOutputAttributeProcessor();
    }

    public static OutputAttributeProcessor createCoalesceConverter(Attribute.Type[] types) {
        Attribute.Type type = types[0];
        for (Attribute.Type aType : types) {
            if (type != aType) {
                throw new QueryCreationException("Coalesce cannot have parameters with different type");
            }
        }
        return new CoalesceOutputAttributeProcessor(type);
    }
}
