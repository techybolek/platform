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
package org.wso2.carbon.dataservices.queries.rdbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.dataservices.objectmodel.context.FieldContextCache;
import org.wso2.carbon.dataservices.objectmodel.context.FieldContextException;
import org.wso2.carbon.dataservices.objectmodel.context.FieldContextPath;
import org.wso2.carbon.dataservices.objectmodel.context.FieldContextUtils;
import org.wso2.carbon.dataservices.objectmodel.types.ContainerType;
import org.wso2.carbon.dataservices.objectmodel.types.DataFormat;
import org.wso2.carbon.dataservices.objectmodel.types.DataType;
import org.wso2.carbon.dataservices.query.rdbms.RDBMSResultSetContext;

/**
 * RDBMS field context tests.
 */
public class RDBMSFieldContextTest {

    @Test
    public void testRDBMSRAWRead() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:h2:mem:db1");
            conn.createStatement().execute("CREATE TABLE Entry (id INT, val VARCHAR(200))");
            int total = 1000000;
            for (int i = 0; i < total; i++) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Entry VALUES (?, ?)");
                stmt.setInt(1, i);
                stmt.setString(2, "OIOIJ@#$)(@JRFONWICNQCOI@WMCOFIJR)@#$IJFO@MWRCOWNDCOWJNIRFEILWRUGNWEILGUHW#$T@#PIQJNAIJNIGUGOIUHVWEI");
                stmt.execute();
            }
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Entry");
            int c = 0;
            long start = System.currentTimeMillis();
            while (rs.next()) {
                rs.getInt(1);
                rs.getString(2);
                c++;
            }
            long end = System.currentTimeMillis();
            System.out.println("testRDBMSRAWRead: Time: " + (end - start) + "ms, TPS: " + total / (double) (end - start) * 1000.0);
            Assert.assertEquals(c, total);
        } finally {
            conn.close();
        }
    }
    
    
    @Test
    public void testRDBMSRead() throws SQLException, FieldContextException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:h2:mem:db1");
            conn.createStatement().execute("CREATE TABLE Entry (id INT, val VARCHAR(200))");
            int total = 1000000;
            for (int i = 0; i < total; i++) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Entry VALUES (?, ?)");
                stmt.setInt(1, i);
                stmt.setString(2, "OIOIJ@#$)(@JRFONWICNQCOI@WMCOFIJR)@#$IJFO@MWRCOWNDCOWJNIRFEILWRUGNWEILGUHW#$T@#PIQJNAIJNIGUGOIUHVWEI");
                stmt.execute();
            }
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Entry");
            FieldContextCache cache = new FieldContextCache();
            RDBMSResultSetContext ctx = new RDBMSResultSetContext("rs1", cache, rs);
            cache.addToFieldCache(FieldContextUtils.parseFieldContextPath("rs1"), ctx);
            FieldContextPath idPath = FieldContextUtils.parseFieldContextPath("rs1.id");
            FieldContextPath valPath = FieldContextUtils.parseFieldContextPath("rs1.val");
            DataFormat idFormat = new DataFormat(DataType.INTEGER, ContainerType.SCALAR);
            DataFormat valFormat = new DataFormat(DataType.STRING, ContainerType.SCALAR);
            int c = 0;
            long start = System.currentTimeMillis();
            while (ctx.nextListState()) {
                ctx.getSubContext(idPath, idFormat).getCurrentValue();
                ctx.getSubContext(valPath, valFormat).getCurrentValue();
                c++;
            }
            long end = System.currentTimeMillis();
            System.out.println("testRDBMSRead: Time: " + (end - start) + "ms, TPS: " + total / (double) (end - start) * 1000.0);
            Assert.assertEquals(c, total);
        } finally {
            conn.close();
        }
    }
    
}
