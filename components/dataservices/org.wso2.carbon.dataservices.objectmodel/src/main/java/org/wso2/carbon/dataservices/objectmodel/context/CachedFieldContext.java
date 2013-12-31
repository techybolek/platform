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
 * This represents a cached field context.
 */
public abstract class CachedFieldContext implements FieldContext {
    
    private String path;
    
    private FieldContextCache fieldContextCache;
        
    public CachedFieldContext(String path, FieldContextCache fieldContextCache) {
        this.path = path;
        this.fieldContextCache = fieldContextCache;
    }
    
    /**
     * Returns the child data of the current context.
     * @param comp The path of the sub context it should return
     * @param format The data format
     * @return The sub-field-context 
     * @throws FieldContextException
     */
    protected abstract CachedFieldContext getChildData(FieldContextPath childPath, DataFormat format) 
            throws FieldContextException;

    /**
     * Returns the current context path.
     * @return The context path
     */
    protected String getPath() {
        return path;
    }

    /**
     * Returns the field context cache.
     * @return The field context cache
     */
    protected FieldContextCache getFieldContextCache() {
        return fieldContextCache;
    }

    private CachedFieldContext recursiveCacheLookup(FieldContextPath path, 
            DataFormat format) throws FieldContextException {
        FieldContextCache cache = this.getFieldContextCache();
        CachedFieldContext ctx = cache.getCachedField(path.getAbsolutePath());
        if (ctx != null) {
            /* cache hit */
            return ctx;
        }
        FieldContextPath headPath = path.getHeadPath();
        if (headPath == null) {
            throw new FieldContextException("Requested context field is not in cache: " + 
                    path.getAbsolutePath());
        }
        /* recursively look up the item */
        CachedFieldContext result = this.recursiveCacheLookup(headPath, 
                format).getChildData(path, format);
        /* add to the cache */
        cache.addToFieldCache(path, result);
        return result;
    }
    
    public FieldContext getSubContext(FieldContextPath path, DataFormat format)
            throws FieldContextException {
        return this.recursiveCacheLookup(path, format);
    }
    
    public void clearFieldCache() throws FieldContextException {
        this.getFieldContextCache().clearCacheForHead(this.getPath());
    }
    
    @Override
    public void close() throws FieldContextException {
        this.clearFieldCache();
    }
    
}
