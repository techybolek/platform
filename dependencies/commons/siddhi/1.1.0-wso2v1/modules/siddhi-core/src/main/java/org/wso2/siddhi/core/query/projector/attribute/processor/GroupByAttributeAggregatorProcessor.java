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
package org.wso2.siddhi.core.query.projector.attribute.processor;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.query.projector.attribute.aggregator.Aggregator;
import org.wso2.siddhi.core.query.projector.attribute.factory.AttributeAggregatorFactory;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.QueryEventStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupByAttributeAggregatorProcessor extends AbstractAggregationAttributeProcessor implements GroupByAttributeProcessor{

    protected Map<String, Aggregator> aggregatorMap = new HashMap<String, Aggregator>();

    public GroupByAttributeAggregatorProcessor(Expression[] expressions, List<QueryEventStream> queryEventStreamList, AttributeAggregatorFactory attributeAggregatorFactory, String elementId, SiddhiContext siddhiContext) {
        super(expressions, queryEventStreamList, attributeAggregatorFactory, elementId, siddhiContext);
    }

    public synchronized Object process(AtomicEvent event, String key) {
        Aggregator currentAggregator = aggregatorMap.get(key);
        if (currentAggregator == null) {
            currentAggregator = sampleAggregator.createNewInstance();
            aggregatorMap.put(key, currentAggregator);
        }
        return process(event, currentAggregator);
    }

    public synchronized Object[] currentState() {
        return new Object[]{aggregatorMap};
    }

    public synchronized void restoreState(Object[] objects) {
        this.aggregatorMap = (Map<String, Aggregator>) objects[0];
    }

    @Override
    public void lock() {

    }

    @Override
    public void unlock() {

    }


}
