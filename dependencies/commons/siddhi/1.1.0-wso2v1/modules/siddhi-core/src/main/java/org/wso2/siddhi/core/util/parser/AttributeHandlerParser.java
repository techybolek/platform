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

import org.wso2.siddhi.core.exception.CannotLoadClassException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.Aggregator;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.avg.AvgAggregatorDouble;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.avg.AvgAggregatorFloat;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.avg.AvgAggregatorInt;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.avg.AvgAggregatorLong;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.count.CountAggregator;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.max.MaxAggregatorDouble;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.max.MaxAggregatorFloat;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.max.MaxAggregatorInt;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.max.MaxAggregatorLong;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.min.MinAggregatorDouble;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.min.MinAggregatorFloat;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.min.MinAggregatorInt;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.min.MinAggregatorLong;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.sum.SumAggregatorDouble;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.sum.SumAggregatorFloat;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.sum.SumAggregatorInt;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.sum.SumAggregatorLong;
import org.wso2.siddhi.core.query.projector.attribute.factory.AttributeAggregatorFactory;
import org.wso2.siddhi.core.query.projector.attribute.factory.AttributeConverterFactory;
import org.wso2.siddhi.query.api.definition.Attribute;

public class AttributeHandlerParser {


    public static AttributeAggregatorFactory loadAggregatorClass(String attributeName) throws CannotLoadClassException {
        return (AttributeAggregatorFactory) org.wso2.siddhi.core.util.ClassLoader.loadClass(AttributeAggregatorFactory.class.getPackage().getName() + "." + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1) + "AttributeAggregatorFactory");
    }

    public static AttributeConverterFactory loadConverterClass(String attributeName) throws CannotLoadClassException {
        return (AttributeConverterFactory) org.wso2.siddhi.core.util.ClassLoader.loadClass(AttributeConverterFactory.class.getPackage().getName() + "." + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1) + "AttributeConverterFactory");
    }

    public static Aggregator createSumAggregator(Attribute.Type type) {
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Sum not supported for string");
            case INT:
                return new SumAggregatorInt();
            case LONG:
                return new SumAggregatorLong();
            case FLOAT:
                return new SumAggregatorFloat();
            case DOUBLE:
                return new SumAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Sum not supported for bool");
        }
        throw new OperationNotSupportedException("Sum not supported for " + type);
    }

    public static Aggregator createAvgAggregator(Attribute.Type type) {
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Avg not supported for string");
            case INT:
                return new AvgAggregatorInt();
            case LONG:
                return new AvgAggregatorLong();
            case FLOAT:
                return new AvgAggregatorFloat();
            case DOUBLE:
                return new AvgAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Avg not supported for bool");
        }
        throw new OperationNotSupportedException("Avg not supported for " + type);
    }

    public static Aggregator createMaxAggregator(Attribute.Type type) {
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Max not supported for string");
            case INT:
                return new MaxAggregatorInt();
            case LONG:
                return new MaxAggregatorLong();
            case FLOAT:
                return new MaxAggregatorFloat();
            case DOUBLE:
                return new MaxAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Max not supported for bool");
        }
        throw new OperationNotSupportedException("Max not supported for " + type);
    }


    public static Aggregator createMinAggregator(Attribute.Type type) {
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Min not supported for string");
            case INT:
                return new MinAggregatorInt();
            case LONG:
                return new MinAggregatorLong();
            case FLOAT:
                return new MinAggregatorFloat();
            case DOUBLE:
                return new MinAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Min not supported for bool");
        }
        throw new OperationNotSupportedException("Min not supported for " + type);
    }

    public static Aggregator createCountAggregator() {
        return new CountAggregator();
    }
}
