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

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the field context environment, where this would be used
 * to query the field contexts' values in a specific instance.
 */
public class FieldContextEnv {

	private Map<String, Object> contexts = new HashMap<String, Object>();
	
	public void addRootFieldContext(String name, FieldContext fieldContext) {
		this.contexts.put(name, fieldContext);
	}
	
	public FieldContext getRootFieldContext() {
		return null;
	}
	
	/**
	 * Returns the field context value in this environment. 
	 * @param path The path to the field context in this environment
	 * @return The FieldContext object value
	 */
	public FieldContext getValue(FieldContextPath path) {
		return null;
	}
	
}
