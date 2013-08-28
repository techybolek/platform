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

/**
 * This class represents a field context path.
 */
public class FieldContextPath {
	
	private PathComponent[] components;
	
	private int offset;
	
	private String absolutePath;
	
	private FieldContextPath tailPath;
		
	public FieldContextPath(PathComponent[] components) throws FieldContextException {
		this (components, 0);
	}
	
	private FieldContextPath(PathComponent[] components, int offset) throws FieldContextException {
		this.components = components;
		this.offset = offset;
		if (this.offset + 1 < this.components.length) {
			/* generate the tail path */
			this.tailPath = new FieldContextPath(this.components, this.offset + 1);
		}
		this.populateAbsolutePath();
	}
	
	private void populateAbsolutePath() {
		StringBuilder builder = new StringBuilder(this.getHead().toString());
		if (this.tailPath != null) {
			String tail = this.tailPath.getAbsolutePath();
			if (!tail.startsWith("[")) {
				builder.append('.');
			}
			builder.append(tail);
		}
		this.absolutePath = builder.toString();
	}
	
	public PathComponent getHead() {
		return this.components[this.offset];
	}
	
	public FieldContextPath getTailPath() {
		return tailPath;
	}
	
	public int getLength() {
		return this.components.length - this.offset;
	}
	
	protected PathComponent[] getComponents() {
		return components;
	}
	
	public PathComponent getComponentAt(int index) throws FieldContextException {
		if (index + this.offset >= this.components.length) {
			throw new FieldContextException("Invalid index for field context path: " +
					"Length - " + this.getLength() + " Index - " + 
					index + " Offset - " + this.offset);
		}
		return this.components[index + this.offset];
	}
	
	public String getAbsolutePath() {
		return absolutePath;
	}
	
	@Override
	public int hashCode() {
		return this.getAbsolutePath().hashCode();
	}
	
	@Override
	public boolean equals(Object rhs) {
		return this.getAbsolutePath().equals(rhs);
	}
	
	@Override
	public String toString() {
		return this.getAbsolutePath();
	}
	
	public static class PathComponent {
		
		private Object value;
		
		private boolean index;
		
		private String stringValue;
		
		public PathComponent(String name) {
			this.value = name;
			this.index = false;
		}
		
		public PathComponent(int index) {
			this.value = index;
			this.index = true;
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
			if (this.stringValue != null) {
				return this.stringValue;
			}
			if (this.isIndex()) {
				this.stringValue = "[" + this.getIndexValue() + "]";
			} else {
				this.stringValue = this.getStringValue();
			}
			return this.stringValue;
		}
		
	}
	
}