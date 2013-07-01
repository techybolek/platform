/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.user.core.system;

import org.apache.axis2.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SystemUserManager {

    private static Log log = LogFactory.getLog(SystemUserManager.class);

    private DataSource dataSource = null;
    private int tenantId = -1;
    private RealmConfiguration realmConfig = null;
    private UserRealm userRealm = null;
    private Random random = new Random();
    private SystemRoleManager systemRoleManager = null;
    private ClaimManager claimManager = null;

    public SystemUserManager(DataSource dataSource, int tenantId, RealmConfiguration realmConfig,
                             UserRealm realm, SystemRoleManager systemRoleManager, ClaimManager claimManager) {
        super();
        this.dataSource = dataSource;
        this.tenantId = tenantId;
        this.realmConfig = realmConfig;
        this.userRealm = realm;
        this.systemRoleManager = systemRoleManager;
        this.claimManager = claimManager;
    }

    public boolean isExistingSystemUser(String userName) throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isExisting = false;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SystemJDBCConstants.GET_USER_ID_SQL);
            prepStmt.setString(1, userName);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                int value = rs.getInt(1);
                if (value > -1) {
                    isExisting = true;
                }
            }
            return isExisting;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    public void addSystemUser(String userName, Object credential, String[] roleList,
                              Map<String, String> claims, String profileName,
                              boolean requirePasswordChange) throws UserStoreException{

        Connection dbConnection = null;
        String password = (String) credential;
        try {
            dbConnection = getDBConnection();
            String sqlStmt1 = SystemJDBCConstants.ADD_USER_SQL;

            String saltValue = null;
            byte[] bytes = new byte[16];
            random.nextBytes(bytes);
            saltValue = Base64.encode(bytes);

            password = this.preparePassword(password, saltValue);

            this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password,
                    saltValue, requirePasswordChange, new Date(), tenantId);

            // add user to role.
            systemRoleManager.updateSystemRoleListOfUser(userName, null, roleList);

            dbConnection.commit();
        } catch (Throwable e) {
            try {
                dbConnection.rollback();
            } catch (SQLException e1) {
                log.error(e.getMessage(), e1);
                throw new UserStoreException(e.getMessage(), e1);
            }
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    private Connection getDBConnection() throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        return dbConnection;
    }

    protected String preparePassword(String password, String saltValue) throws UserStoreException {
        try {
            String digestInput = password;
            if (saltValue != null) {
                digestInput = password + saltValue;
            }
            MessageDigest dgst = MessageDigest.getInstance("SHA-256");
            byte[] byteValue = dgst.digest(digestInput.getBytes());
            password = Base64.encode(byteValue);
            return password;
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    private void updateStringValuesToDatabase(Connection dbConnection, String sqlStmt,
                                              Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = getDBConnection();
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Invalid data provided");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Date) {
                        //Timestamp timestamp = new Timestamp(((Date) param).getTime());
                        //prepStmt.setTimestamp(i + 1, timestamp);
                        prepStmt.setTimestamp(i + 1,new Timestamp(System.currentTimeMillis()));
                    } else if (param instanceof Boolean) {
                        prepStmt.setBoolean(i + 1, (Boolean) param);
                    }
                }
            }
            int count = prepStmt.executeUpdate();
            if(count == 0) {
                log.info("No rows were updated");
            }
            if (log.isDebugEnabled()) {
                log.debug("Executed querry is " + sqlStmt + " and number of updated rows :: "
                        + count);
            }

            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }
    public String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {

        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        if (maxItemLimit == 0) {
            return new String[0];
        }

        if(maxItemLimit < 0){
            log.error("Invalid arguments for listUsers");
            throw new UserStoreException("Error while listing users");
        }
        String[] systemsUsers = new String[0];
        try {

            if (filter != null && filter.trim().length() != 0) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                filter = "%";
            }

            List<String> lst = new LinkedList<String>();

            dbConnection = getDBConnection();

            if (dbConnection == null) {
                throw new UserStoreException("null connection");
            }
            dbConnection.setAutoCommit(false);
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            sqlStmt = SystemJDBCConstants.GET_SYSTEM_USER_FILTER_SQL;

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }

            rs = prepStmt.executeQuery();

            int i = 0;
            while (rs.next()) {
                if (i < maxItemLimit) {
                String name = rs.getString(1);
                lst.add(name);
                }else{
                    break;
                }
                i++;
            }
            rs.close();

            if (lst.size() > 0) {
                systemsUsers = lst.toArray(new String[lst.size()]);
            }
            Arrays.sort(systemsUsers);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return systemsUsers;

    }

    public int getUserId(String username) throws UserStoreException {
        String sqlStmt = SystemJDBCConstants.GET_USERID_FROM_USERNAME_SQL;
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for retrieving ID is null");
        }
        int id = -1;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, username, tenantId);
            } else {
                id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, username);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        return id;
    }

    public String[] getUserNames(int tenantId) throws UserStoreException {
        String sqlStmt = SystemJDBCConstants.GET_USERNAME_FROM_TENANT_ID_SQL;
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for retrieving user names is null");
        }
        String[] userNames;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            userNames = DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt, tenantId);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        return userNames;
    }

    public int getTenantId() throws UserStoreException {
        return this.tenantId;
    }

    public int getTenantId(String username) throws UserStoreException {
        if (this.tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new UserStoreException("Not allowed to perform this operation");
        }
        String sqlStmt = SystemJDBCConstants.GET_TENANT_ID_FROM_USERNAME_SQL;
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for retrieving ID is null");
        }
        int id = -1;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, username);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        return id;
    }

}
