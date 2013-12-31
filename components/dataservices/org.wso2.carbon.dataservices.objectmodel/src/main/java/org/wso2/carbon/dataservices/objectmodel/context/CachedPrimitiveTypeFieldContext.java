/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.objectmodel.context;

import org.wso2.carbon.dataservices.objectmodel.types.DataFormat;

/**
 * This class represents a cached primitive type field context.
 */
public class CachedPrimitiveTypeFieldContext extends CachedFieldContext {

    private Object object;

    public CachedPrimitiveTypeFieldContext(String path, FieldContextCache fieldContextCache, 
            Object object) {
        super(path, fieldContextCache);
        this.object = object;
    }

    @Override
    public Object getCurrentValue() {
        return object;
    }

    @Override
    public boolean nextListState() throws FieldContextException {
        return false;
    }

    @Override
    public CachedFieldContext getChildData(FieldContextPath childPath, DataFormat format)
            throws FieldContextException {
        throw new FieldContextException("No subcontext for this context at: " + this.getPath());
    }

}
