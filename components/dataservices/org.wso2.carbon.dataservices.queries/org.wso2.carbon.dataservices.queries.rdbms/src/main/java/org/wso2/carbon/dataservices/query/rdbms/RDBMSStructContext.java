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
package org.wso2.carbon.dataservices.query.rdbms;

import java.sql.SQLException;
import java.sql.Struct;

import org.wso2.carbon.com.core.model.DataFormat;
import org.wso2.carbon.com.core.fieldcontext.FieldContextException;
import org.wso2.carbon.com.core.fieldcontext.FieldContextPath.PathComponent;

public class RDBMSStructContext extends AbstractRDBMSObjectFieldContext {

	private Object[] attributes;
	
	public RDBMSStructContext(Struct struct) throws FieldContextException {
		if (struct == null) {
			throw new FieldContextException("The RDBMS Struct cannot be null");
		}
		try {
		    this.attributes = struct.getAttributes();
		} catch (SQLException e) {
			throw new FieldContextException("Error in getting attributes from RDBMS Struct: " + 
		            e.getMessage(), e);
		}
	}

	@Override
	protected Object getRDBMSResultValue(PathComponent comp, DataFormat format)
			throws SQLException {
		if (!comp.isIndex()) {
			throw new SQLException("An index should be provided to access " +
					"attributes on a RDBMS Struct: " + comp);
		}
		int index = comp.getIndexValue();
		if (index >= this.attributes.length) {
			throw new SQLException("The index: " + index + 
					" is larger than the attribute list size: " + this.attributes.length);
		}
		return this.attributes[index];
	}

	@Override
	protected boolean moveToNextListResultValue() throws FieldContextException {
		return false;
	}
	

}
