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

import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.wso2.carbon.com.core.fieldcontext.FieldContextPath.PathComponent;
import org.wso2.carbon.com.core.model.DataType;

public class RDBMSResultHelper {

	public static Object getResultSetValue(ResultSet rs, PathComponent comp, 
			DataType dataType) throws SQLException {
		switch (dataType) {
		case STRING:
			return readString(rs, comp);
		case BLOB:
			return readBinaryStream(rs, comp);
		case CLOB:
			return readCharacterStream(rs, comp);
		case DOUBLE:
			return readDouble(rs, comp);
		case FLOAT:
			return readFloat(rs, comp);
		case INTEGER:
			return readInteger(rs, comp);
		case OBJECT:
			return readObject(rs, comp);
		default:
			throw new SQLException("Unrecognized data type: " + dataType);
		}
	}
	
	private static String readString(ResultSet rs, PathComponent comp) throws SQLException {
		if (comp.isIndex()) {
			return rs.getString(comp.getIndexValue());
		} else {
			return rs.getString(comp.getStringValue());
		}
	}
	
	private static float readFloat(ResultSet rs, PathComponent comp) throws SQLException {
		if (comp.isIndex()) {
			return rs.getFloat(comp.getIndexValue());
		} else {
			return rs.getFloat(comp.getStringValue());
		}
	}
	
	private static double readDouble(ResultSet rs, PathComponent comp) throws SQLException {
		if (comp.isIndex()) {
			return rs.getDouble(comp.getIndexValue());
		} else {
			return rs.getDouble(comp.getStringValue());
		}
	}
	
	private static int readInteger(ResultSet rs, PathComponent comp) throws SQLException {
		if (comp.isIndex()) {
			return rs.getInt(comp.getIndexValue());
		} else {
			return rs.getInt(comp.getStringValue());
		}
	}
	
	private static Object readObject(ResultSet rs, PathComponent comp) throws SQLException {
		if (comp.isIndex()) {
			return rs.getObject(comp.getIndexValue());
		} else {
			return rs.getObject(comp.getStringValue());
		}
	}
	
	private static InputStream readBinaryStream(ResultSet rs, PathComponent comp) throws SQLException {
		if (comp.isIndex()) {
			return rs.getBinaryStream(comp.getIndexValue());
		} else {
			return rs.getBinaryStream(comp.getStringValue());
		}
	}
	
	private static Reader readCharacterStream(ResultSet rs, PathComponent comp) throws SQLException {
		if (comp.isIndex()) {
			return rs.getCharacterStream(comp.getIndexValue());
		} else {
			return rs.getCharacterStream(comp.getStringValue());
		}
	}
	
}
