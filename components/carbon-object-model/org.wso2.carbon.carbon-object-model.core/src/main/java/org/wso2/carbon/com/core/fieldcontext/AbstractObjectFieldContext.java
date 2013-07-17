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
package org.wso2.carbon.com.core.fieldcontext;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.com.core.fieldcontext.FieldContextPath.PathComponent;
import org.wso2.carbon.com.core.model.ContainerType;
import org.wso2.carbon.com.core.model.DataFormat;
import org.wso2.carbon.com.core.model.DataType;

public abstract class AbstractObjectFieldContext implements FieldContext {

	private Map<String, FieldContext> fieldContextCache = new HashMap<String, FieldContext>();
	
	private boolean listStarted;
	
	protected void addToFieldCache(String name, FieldContext fieldCtx) {
		this.fieldContextCache.put(name, fieldCtx);
	}
	
	protected FieldContext getCachedField(String name) {
		return this.fieldContextCache.get(name);
	}
	
	protected Map<String, FieldContext> getFieldContextCache() {
		return fieldContextCache;
	}
	
	protected void clearFieldCache() {
		this.fieldContextCache.clear();
	}

	@Override
	public boolean nextListState() throws FieldContextException {
		boolean result = this.moveToNextListResultValue();
		this.listStarted = true;
		this.clearFieldCache();
		return result;
	}

	protected abstract boolean moveToNextListResultValue() throws FieldContextException;
	
	protected abstract FieldContext getResultValue(PathComponent comp, 
			DataFormat format) throws FieldContextException; 

	@Override
	public FieldContext getSubContext(FieldContextPath path, DataFormat format) throws FieldContextException {
		PathComponent firstComp = path.getComponentAt(0);
		FieldContext result = this.getCachedField(firstComp.toString());
		if (result != null) {
			return result;
		}
		if (!this.listStarted) {
			this.nextListState();
			this.listStarted = true;
		}
		if (path.getLength() == 1) {
			result = this.getResultValue(firstComp, format);
		} else {
			result = this.getResultValue(firstComp, new DataFormat(DataType.OBJECT, 
					ContainerType.SCALAR)).getSubContext(path.getFieldContextSubPath(1), format);
		}
        this.addToFieldCache(firstComp.toString(), result);
		return result;
	}
	
	@Override
	public Object getCurrentValue() {
		return this.getClass().getName();
	}
	
	@Override
	public void close() throws FieldContextException {
		for (Object obj : this.getFieldContextCache().values()) {
			if (obj instanceof FieldContext) {
			    ((FieldContext) obj).close();
			}
		}
	}

}
