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

import java.sql.Array;
import java.sql.SQLException;

import org.wso2.carbon.com.core.model.DataFormat;
import org.wso2.carbon.com.core.fieldcontext.FieldContextException;
import org.wso2.carbon.com.core.fieldcontext.FieldContextPath.PathComponent;

public class RDBMSArrayContext extends AbstractRDBMSObjectFieldContext {

	private Object[] dataArray;
	
	private int currentIndex;
	
	public RDBMSArrayContext(Array sqlArray) throws FieldContextException {
		if (sqlArray == null) {
			throw new FieldContextException("The RDBMS SQL Array cannot be null");
		}
		try {
			this.dataArray = (Object[]) sqlArray.getArray();
			this.currentIndex = -1;
		} catch (SQLException e) {
			throw new FieldContextException("Error in creating RDBMS Array Context: " + 
		            e.getMessage(), e);
		}
	}

	@Override
	protected Object getRDBMSResultValue(PathComponent comp, DataFormat format)
			throws SQLException {
		if (this.currentIndex >= this.dataArray.length) {
			throw new SQLException("The current SQL Array size: " + this.dataArray.length + 
					" is smaller than the current index: " + this.currentIndex);
		}
		return this.dataArray[this.currentIndex];
	}

	@Override
	protected boolean moveToNextListResultValue() throws FieldContextException {
		if (this.currentIndex < this.dataArray.length - 1) {
			this.currentIndex++;
			return true;
		} else {
			return false;
		}
	}

}
