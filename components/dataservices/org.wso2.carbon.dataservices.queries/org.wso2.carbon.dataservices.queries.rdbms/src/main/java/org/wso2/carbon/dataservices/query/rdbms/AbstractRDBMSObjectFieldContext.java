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

import org.wso2.carbon.dataservices.objectmodel.context.CachedFieldContext;
import org.wso2.carbon.dataservices.objectmodel.context.CachedObjectFieldContext;
import org.wso2.carbon.dataservices.objectmodel.context.CachedPrimitiveTypeFieldContext;
import org.wso2.carbon.dataservices.objectmodel.context.FieldContextCache;
import org.wso2.carbon.dataservices.objectmodel.context.FieldContextException;
import org.wso2.carbon.dataservices.objectmodel.context.FieldContextPath;
import org.wso2.carbon.dataservices.objectmodel.types.ContainerType;
import org.wso2.carbon.dataservices.objectmodel.types.DataFormat;
import org.wso2.carbon.dataservices.objectmodel.types.DataType;

/**
 * This class represents an abstract RDBMS field context implementation.
 */
public abstract class AbstractRDBMSObjectFieldContext extends CachedObjectFieldContext {

	public AbstractRDBMSObjectFieldContext(String path, FieldContextCache fieldContextCache) {
        super(path, fieldContextCache);
    }

    @Override
	protected CachedFieldContext getResultValue(FieldContextPath childPath, DataFormat format)
			throws FieldContextException {
		Object objResult;
		try {
			objResult = this.getRDBMSResultValue(childPath, format);
		} catch (SQLException e) {
			throw new FieldContextException("Error in reading RDBMS result: " + e.getMessage(), e);
		}
		CachedFieldContext fieldCtx = null;
		if (DataType.OBJECT.equals(format.getDataType())) {
			if (objResult instanceof Array) {
            	//fieldCtx = new RDBMSArrayContext((Array) objResult);
			} else if (objResult instanceof ResultSet) {
				//fieldCtx = new RDBMSResultSetContext((ResultSet) objResult);
			} else if (objResult instanceof Struct) {
				//fieldCtx = new RDBMSStructContext((Struct) objResult);
			} else {
				throw new FieldContextException("Unrecognized object type: " + objResult.getClass());
			}
            return fieldCtx;
		} else if (ContainerType.LIST.equals(format.getContainerType())) {
			if (objResult instanceof Array) {
            	//return new RDBMSArrayContext((Array) objResult);
			} else {
				throw new FieldContextException("Unsupported type to be used as a primitive " +
						"type list: " + objResult.getClass());
			}
		} else {
			return new CachedPrimitiveTypeFieldContext(childPath.getAbsolutePath(), this.getFieldContextCache(), objResult);
		}
		return null;
	}
	
	protected abstract Object getRDBMSResultValue(FieldContextPath childPath, DataFormat format) throws SQLException;

}
