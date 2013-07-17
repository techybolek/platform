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
package org.wso2.carbon.com.core.model;

import java.util.Collection;
import java.util.LinkedHashMap;

public class ComplexField implements Field {

	private String name;
	
	private LinkedHashMap<String, Field> childFields;
	
	public ComplexField(String name, LinkedHashMap<String, Field> childFields) {
		this.name = name;
		this.childFields = childFields;
	}
	
	public Collection<Field> getChildFields() {
		return this.childFields.values();
	}
	
	public Field getChildField(String name) {
		return this.childFields.get(name);
	}

	@Override
	public String getName() {
		return name;
	}
	
}
