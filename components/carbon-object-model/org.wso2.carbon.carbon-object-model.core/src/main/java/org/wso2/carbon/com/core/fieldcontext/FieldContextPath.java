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

public class FieldContextPath {
	
	private PathComponent[] components;
	
	private int offset;
	
	public FieldContextPath(PathComponent[] components, int offset) throws FieldContextException {
		if (components == null) {
			throw new FieldContextException("The components should be not null");
		}
		if (offset >= components.length || offset < 0) {
			throw new FieldContextException("Invalid field context path offset: Length - " + 
		            components.length + " Offset - " + offset);
		}
		this.components = components;
		this.offset = offset;
	}
	
	public FieldContextPath(PathComponent[] components) throws FieldContextException {
		this (components, 0);
	}
	
	public FieldContextPath getFieldContextSubPath(int offset) throws FieldContextException {
		return new FieldContextPath(this.components, this.offset + offset);
	}
	
	public int getLength() {
		return this.components.length - this.offset;
	}
	
	public PathComponent getComponentAt(int index) throws FieldContextException {
		if (index + this.offset >= this.components.length) {
			throw new FieldContextException("Invalid index for field context path: " +
					"Length - " + this.getLength() + " Index - " + 
					index + " Offset - " + this.offset);
		}
		return this.components[index + this.offset];
	}
	
	public static class PathComponent {
		
		private Object value;
		
		private boolean index;
		
		public PathComponent(boolean index, Object value) {
			this.value = value;
			this.index = index;
		}
		
		public boolean isIndex() {
			return index;
		}
		
		public String getStringValue() {
			return (String) value;
		}
		
		public int getIndexValue() {
			return (Integer) value;
		}
		
		@Override
		public String toString() {
			if (this.isIndex()) {
				return "[" + this.getIndexValue() + "]";
			} else {
				return this.getStringValue();
			}
		}
		
	}
	
}