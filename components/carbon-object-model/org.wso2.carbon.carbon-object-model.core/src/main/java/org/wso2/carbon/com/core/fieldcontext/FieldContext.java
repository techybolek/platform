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

import org.wso2.carbon.com.core.model.DataFormat;

/**
 * This interface represents the concept of the Field Context. Which is basically a data related context which is used
 * to represent a specific item. For example, a result set from a SQL statement can be represented in a field context.
 * Such as "result" in some configuration represent the result set's field context. So using "result", we can traverse
 * its records and get the values. In getting sub-context's of the current context, such as "result.obj1", we can use
 * the methods to get sub contexts, which will itself return a sub-context of type FieldContex. 
 */
public interface FieldContext {
	
	/**
	 * Returns the given sub-context of the current field context.
	 * @param path The path of the sub-context it should return. Example field context path would be "obj1", which
	 * would return the "obj1" sub-context of the current field context. If the path is "obj1.item1", it directly
	 * returns the "obj1"'s sub-context's "item1" sub-context.
	 * @param format 
	 * @return
	 * @throws FieldContextException
	 */
	public FieldContext getSubContext(FieldContextPath path, DataFormat format) throws FieldContextException;
	
	/**
	 * Returns the current value of the field context.
	 * @return The current value
	 * @throws FieldContextException
	 */
	public Object getCurrentValue() throws FieldContextException;
	
	/**
	 * Moves the current list field context to the next state, this is only useful if the 
	 * underlying field context is actually a list based field context. This method is put
	 * to this interface and not in a sub-interface, to make the overhead low of users of 
	 * this to check the type and cast it to the proper type.
	 * @return If there is any more list states available
	 * @throws FieldContextException
	 */
	public boolean nextListState() throws FieldContextException;
	
	/**
	 * Closes and cleanups the current field context.
	 * @throws FieldContextException
	 */
	public void close() throws FieldContextException;
	
}
