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
package org.wso2.siddhi.core.query.selector.attribute.handler.coalesce;

import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeProcessor;
import org.wso2.siddhi.query.api.definition.Attribute;

public class CoalesceOutputAttributeProcessor implements OutputAttributeProcessor {

    private Attribute.Type type;

    public CoalesceOutputAttributeProcessor(Attribute.Type type) {
        this.type = type;
    }

    public Attribute.Type getType() {
        return type;
    }

    @Override
    public Object processInEventAttribute(Object obj) {
        return process(obj);
    }

    @Override
    public Object processRemoveEventAttribute(Object obj) {
        return process(obj);
    }

    private Object process(Object obj) {
        if (obj instanceof Object[]) {
            for (Object aObj : (Object[]) obj) {
                if (aObj != null) {
                    return aObj;
                }
            }
            return null;
        } else {
            return obj;
        }
    }

    @Override
    public OutputAttributeProcessor createNewInstance() {
        return new CoalesceOutputAttributeProcessor(type);
    }
}
