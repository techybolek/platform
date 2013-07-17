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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;

import org.wso2.carbon.com.core.fieldcontext.AbstractObjectFieldContext;
import org.wso2.carbon.com.core.model.DataFormat;
import org.wso2.carbon.com.core.fieldcontext.FieldContext;
import org.wso2.carbon.com.core.fieldcontext.FieldContextException;
import org.wso2.carbon.com.core.fieldcontext.PrimitiveTypeFieldContext;
import org.wso2.carbon.com.core.fieldcontext.FieldContextPath.PathComponent;
import org.wso2.carbon.com.core.model.DataType;

public abstract class AbstractRDBMSObjectFieldContext extends AbstractObjectFieldContext {

	@Override
	protected FieldContext getResultValue(PathComponent comp, DataFormat format)
			throws FieldContextException {
		Object objResult;
		try {
			objResult = this.getRDBMSResultValue(comp, format);
		} catch (SQLException e) {
			throw new FieldContextException("Error in reading RDBMS result: " + e.getMessage(), e);
		}
		FieldContext fieldCtx;
		if (DataType.OBJECT.equals(format.getDataType())) {
			if (objResult instanceof Array) {
            	fieldCtx = new RDBMSArrayContext((Array) objResult);
			} else if (objResult instanceof ResultSet) {
				fieldCtx = new RDBMSResultSetContext((ResultSet) objResult);
			} else if (objResult instanceof Struct) {
				fieldCtx = new RDBMSStructContext((Struct) objResult);
			} else {
				throw new FieldContextException("Unrecognized object type: " + objResult.getClass());
			}
            return fieldCtx;
		} else {
			return new PrimitiveTypeFieldContext(objResult);
		}
	}
	
	protected abstract Object getRDBMSResultValue(PathComponent comp, DataFormat format) throws SQLException;

}
