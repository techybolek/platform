/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.identity.provider.mgt.util;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.provider.mgt.IdentityProviderMgtException;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class IdentityProviderMgtUtil {

    private static final Log log = LogFactory.getLog(IdentityProviderMgtUtil.class);

    public static boolean validateURI(String uriString) throws IdentityProviderMgtException {
        try {
            new URL(uriString);
        }  catch (MalformedURLException e) {
            throw new IdentityProviderMgtException(e);
        }
        return true;
    }

    public static Connection getDBConnection() throws IdentityProviderMgtException{
        try {
            Connection conn = IdentityDatabaseUtil.getDBConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return conn;
        } catch (IdentityException e) {
            log.error("Error getting DB connection", e);
            throw new IdentityProviderMgtException("Error getting DB connection");
        } catch (SQLException e) {
            log.error("Error getting DB connection", e);
            throw new IdentityProviderMgtException("Error getting DB connection");
        }
    }

    /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes
     * @return  hexadecimal representation
     */
    public static String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

    public static String generatedThumbPrint(String publicCert) throws IdentityProviderMgtException{
        MessageDigest digestValue = null;
        try {
            digestValue = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new IdentityProviderMgtException(e);
        }
        byte[] der = Base64.decode(publicCert);
        digestValue.update(der);
        byte[] digestInBytes = digestValue.digest();
        String publicCertThumbprint = IdentityProviderMgtUtil.hexify(digestInBytes);
        return publicCertThumbprint;
    }

    /**
     * Get the tenant id of the given tenant domain.
     *
     * @param tenantDomain Tenant Domain
     * @return Tenant Id of domain user belongs to.
     * @throws IdentityException Error when getting an instance of Tenant Manger
     */
    public static int getTenantIdOfDomain(String tenantDomain) throws IdentityProviderMgtException {
        int tenantId = -1234;;
        if (tenantDomain != null) {
            try {
                TenantManager tenantManager = IdentityTenantUtil.getRealmService().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " + tenantDomain;
                log.error(errorMsg, e);
                throw new IdentityProviderMgtException(errorMsg);
            }
        }
        return tenantId;
    }

    public static String getKeyStoreFileName(String filePath) {
        String name = null;
        int index = filePath.lastIndexOf('/');
        if (index != -1) {
            name = filePath.substring(index + 1);
        } else {
            index = filePath.lastIndexOf(File.separatorChar);
            if (index != -1) {
                name = filePath.substring(filePath.lastIndexOf(File.separatorChar));
            } else {
                name = filePath;
            }
        }
        return name;
    }

    public static boolean containsEntry(Map<String,String> map, Map.Entry entry){
        Object key = entry.getKey();
        Object value = entry.getValue();
        if(map.containsKey(key) && map.containsValue(value)){
            return true;
        }
        return false;
    }
}
