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
package org.wso2.siddhi.core.query.processor.handler.chain.filter;

import org.wso2.siddhi.core.event.*;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;

public class DefaultFilterHandler implements FilterHandler {
    private ConditionExecutor conditionExecutor;

    public DefaultFilterHandler(ConditionExecutor conditionExecutor) {
        this.conditionExecutor = conditionExecutor;
    }

    @Override
    public AtomicEvent process(AtomicEvent atomicEvent) {
        if (conditionExecutor.execute(atomicEvent)) {
            return atomicEvent;
        }
        return null;
    }



    @Override
    public BundleEvent process(BundleEvent bundleEvent) {
        BundleEvent resultEvent=bundleEvent.getNewInstance();
        for (AtomicEvent event : bundleEvent.getEvents()) {
            if (conditionExecutor.execute(event)) {
                if(bundleEvent instanceof ListEvent){
                    ((ListEvent)resultEvent).addEvent((Event)event);
                }else {
                    ((ListAtomicEvent)resultEvent).addEvent(event);

                }
            }
        }
        if (resultEvent.getActiveEvents() > 0) {
            return resultEvent;
        }
        return null;
    }
}
