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
 * A default Abstract implementation of FieldContext with data item caching.
 */
public abstract class CachedObjectFieldContext extends CachedFieldContext {
    
    private boolean listStarted;
    
    public CachedObjectFieldContext(String path, FieldContextCache fieldContextCache) {
        super(path, fieldContextCache);
    }
    
    @Override
    public boolean nextListState() throws FieldContextException {
        boolean result = this.moveToNextListResultValue();
        this.listStarted = true;
        this.clearFieldCache();
        return result;
    }

    protected abstract boolean moveToNextListResultValue() throws FieldContextException;

    protected abstract CachedFieldContext getResultValue(FieldContextPath childPath, DataFormat format)
            throws FieldContextException;

    @Override
    protected CachedFieldContext getChildData(FieldContextPath childPath, DataFormat format)
            throws FieldContextException {
        if (!this.listStarted) {
            this.nextListState();
            this.listStarted = true;
        }
        return this.getResultValue(childPath, format);
    }

    @Override
    public Object getCurrentValue() throws FieldContextException {
        return this.getClass().getName();
    }

}
