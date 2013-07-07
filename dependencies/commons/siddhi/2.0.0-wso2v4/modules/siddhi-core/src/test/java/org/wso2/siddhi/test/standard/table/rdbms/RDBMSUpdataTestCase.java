/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.test.standard.table.rdbms;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.table.SiddhiDataSource;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class RDBMSUpdataTestCase {


    static final Logger log = Logger.getLogger(RDBMSDeleteTestCase.class);
    SiddhiDataSource dataSource = new SiddhiDataSource() {
        @Override
        public Connection getConnection(String database) throws ClassNotFoundException, SQLException {
            Class.forName("com.mysql.jdbc.Driver");
            try {
                return DriverManager.getConnection(RDBMSTestConstants.CONNECTION_URL + "/" + database, RDBMSTestConstants.USERNAME, RDBMSTestConstants.PASSWORD);
            } catch (Exception ex) {
                Connection connection = DriverManager.getConnection(RDBMSTestConstants.CONNECTION_URL, RDBMSTestConstants.USERNAME, RDBMSTestConstants.PASSWORD);
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE DATABASE " + database);
                statement.close();
                return DriverManager.getConnection(RDBMSTestConstants.CONNECTION_URL + "/" + database, RDBMSTestConstants.USERNAME, RDBMSTestConstants.PASSWORD);
            }
        }

        @Override
        public String getType() {
            return "MYSQL";
        }

        @Override
        public String getName() {
            return "cepDataSource";
        }
    };

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("UpdateFromTableTestCase test1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseUpdateEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from MYSQL.cepDataSource:cepdb.cepEventTable");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "insert into cseEventTable;");

        siddhiManager.addQuery("from cseUpdateEventStream " +
                               "update cseEventTable;");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        InputHandler cseUpdateEventStream = siddhiManager.getInputHandler("cseUpdateEventStream");
        cseUpdateEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("UpdateFromTableTestCase test2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseUpdateEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from MYSQL.cepDataSource:cepdb.cepEventTable");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "insert into cseEventTable;");

        siddhiManager.addQuery("from cseUpdateEventStream " +
                               "update cseEventTable" +
                               "    on cseEventTable.symbol=='IBM';");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        InputHandler cseUpdateEventStream = siddhiManager.getInputHandler("cseUpdateEventStream");
        cseUpdateEventStream.send(new Object[]{"GOOG", 10f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("UpdateFromTableTestCase test1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseUpdateEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from MYSQL.cepDataSource:cepdb.cepEventTable");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "insert into cseEventTable;");

        siddhiManager.addQuery("from cseUpdateEventStream " +
                               "update cseEventTable" +
                               "    on cseEventTable.symbol==symbol;");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        InputHandler cseUpdateEventStream = siddhiManager.getInputHandler("cseUpdateEventStream");
        cseUpdateEventStream.send(new Object[]{"WSO2", 10f, 200l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }
}
