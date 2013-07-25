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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.wso2.carbon.com.core.model.DataFormat;
import org.wso2.carbon.com.core.fieldcontext.FieldContextException;
import org.wso2.carbon.com.core.fieldcontext.FieldContextPath.PathComponent;

/**
 * This class represents an RDBMS result set based field context implementation. 
 */
public class RDBMSResultSetContext extends AbstractRDBMSObjectFieldContext {
	
	private ResultSet resultSet;
		
	public RDBMSResultSetContext(ResultSet resultSet) throws FieldContextException {
		this.resultSet = resultSet;
	}
	
	@Override
	protected Object getRDBMSResultValue(PathComponent comp, DataFormat format) throws SQLException {
		return RDBMSResultHelper.getResultSetValue(this.resultSet, comp, 
				format.getDataType());
	}
	
	@Override
	protected boolean moveToNextListResultValue() throws FieldContextException {
		try {
		    return this.resultSet.next();
		} catch (SQLException e) {
			throw new FieldContextException("Error in getting next RDBMS list state: " +
		            e.getMessage(), e);
		}
	}
	
	public void close() throws FieldContextException {
		super.close();
		if (this.resultSet != null) {
			try {
			    this.resultSet.close();
			} catch (SQLException e) {
				throw new FieldContextException("Error in closing RDBMS result set: " + 
			            e.getMessage(), e);
			}
		}
	}

}
