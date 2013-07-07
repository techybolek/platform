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

import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.query.api.definition.Attribute;

public class CoalesceExecutorFunction extends ExecutorFunction {

    Attribute.Type returnType;

    @Override
    public Attribute.Type getType() {
        return returnType;
    }

    @Override
    public void init() {
        Attribute.Type type = attributeTypes[0];
        for (Attribute.Type aType : attributeTypes) {
            if (type != aType) {
                throw new QueryCreationException("Coalesce cannot have parameters with different type");
            }
        }
        returnType = type;
    }


    protected Object process(Object obj) {
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

}
