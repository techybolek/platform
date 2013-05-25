/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimsDO;
import org.wso2.carbon.user.api.UserStoreManager;

public class JDBCIdentityDataStore extends InMemoryIdentityDataStore {

	private static Log log = LogFactory.getLog(JDBCIdentityDataStore.class);

	@Override
	public void store(UserIdentityClaimsDO userIdentityDTO, UserStoreManager userStoreManager)
	                                                                                     throws IdentityException {
		if(userIdentityDTO == null || userIdentityDTO.getUserDataMap().size() < 1) {
			return;
		}
		super.store(userIdentityDTO, userStoreManager);
		
		String userName = userIdentityDTO.getUserName();
		int tenantId = IdentityUtil.getTenantIdOFUser(userName);
		Map<String, String> data = userIdentityDTO.getUserDataMap();
		if (log.isDebugEnabled()) {
			log.debug("Storing identity data for:" + tenantId + ":" + userName);
			for (Map.Entry<String, String> dataEntry : data.entrySet()) {
				log.debug(dataEntry.getKey() + " : " + dataEntry.getValue());
			}
		}

		Connection connection = JDBCPersistenceManager.getInstance().getDBConnection();
		PreparedStatement prepStmt = null;
		try {
			prepStmt = connection.prepareStatement(SQLQuery.STORE_USER_DATA);
			for (Map.Entry<String, String> dataEntry : data.entrySet()) {
				prepStmt.setInt(1, tenantId);
				prepStmt.setString(2, userName);
				prepStmt.setString(3, dataEntry.getKey());
				prepStmt.setString(4, dataEntry.getValue());
				prepStmt.addBatch();
			}
			prepStmt.executeBatch();
			connection.setAutoCommit(false);
			connection.commit();
		} catch (SQLException e) {
			throw new IdentityException("Error while storing user identity data", e);
		} finally {
			IdentityDatabaseUtil.closeStatement(prepStmt);
			IdentityDatabaseUtil.closeConnection(connection);
		}
	}

	@Override
	public UserIdentityClaimsDO load(String userName, UserStoreManager userStoreManager)
	                                                                               throws IdentityException {
		super.load(userName, userStoreManager);
		int tenantId = IdentityUtil.getTenantIdOFUser(userName);
		Connection connection = JDBCPersistenceManager.getInstance().getDBConnection();
		PreparedStatement prepStmt = null;
		ResultSet results = null;
		try {
			prepStmt = connection.prepareStatement(SQLQuery.LOAD_USER_DATA);
			prepStmt.setInt(1, IdentityUtil.getTenantIdOFUser(userName));
			prepStmt.setString(2, userName);
			results = prepStmt.executeQuery();
			Map<String, String> data = new HashedMap();
			while (results.next()) {
				data.put(results.getString(1), results.getString(2));
			}
			if (log.isDebugEnabled()) {
				log.debug("Storing identity data for:" + tenantId + ":" + userName);
				for (Map.Entry<String, String> dataEntry : data.entrySet()) {
					log.debug(dataEntry.getKey() + " : " + dataEntry.getValue());
				}
			}
			UserIdentityClaimsDO dto = new UserIdentityClaimsDO(userName, data);
			dto.setTenantId(tenantId);
			return dto;
		} catch (SQLException e) {
			throw new IdentityException("Error while reading user identity data", e);
		} finally {
			IdentityDatabaseUtil.closeResultSet(results);
			IdentityDatabaseUtil.closeStatement(prepStmt);
			IdentityDatabaseUtil.closeConnection(connection);
		}
	}

	/**
	 * This class contains the SQL queries.
	 * Schem:
	 * ||TENANT_ID || USERR_NAME || DATA_KEY || DATA_VALUE ||
	 * The primary key is tenantId, userName, DatKey combination
	 * 
	 * @author sga
	 * 
	 */
	private static class SQLQuery {
		public static final String LOAD_USER_DATA = "SELECT " + "DATA_KEY, DATA_VALUE "
		                                            + "FROM IDN_IDENTITY_USER_DATA "
		                                            + "WHERE TENANT_ID = ? AND USER_NAME = ?";
		public static final String STORE_USER_DATA =
		                                             "INSERT "
		                                                     + "INTO IDN_IDENTITY_USER_DATA "
		                                                     + "(TENANT_ID, USER_NAME, DATA_KEY, DATA_VALUE) "
		                                                     + "VALUES (?,?,?,?)";
	}

}
