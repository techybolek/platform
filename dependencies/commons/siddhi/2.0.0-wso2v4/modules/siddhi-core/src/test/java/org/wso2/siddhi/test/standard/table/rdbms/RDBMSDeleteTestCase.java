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
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.table.SiddhiDataSource;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RDBMSDeleteTestCase {
    static final Logger log = Logger.getLogger(RDBMSDeleteTestCase.class);
    SiddhiDataSource dataSource = new SiddhiDataSource() {
        @Override
        public Connection getConnection() throws ClassNotFoundException, SQLException {
            Class.forName(RDBMSTestConstants.MYSQL_DRIVER_CLASS);
            try {
                return DriverManager.getConnection(RDBMSTestConstants.CONNECTION_URL, RDBMSTestConstants.USERNAME, RDBMSTestConstants.PASSWORD);    // todo get the db correctly.
            } catch (Exception ex) {
                Connection connection = DriverManager.getConnection(RDBMSTestConstants.CONNECTION_URL, RDBMSTestConstants.USERNAME, RDBMSTestConstants.PASSWORD);    // todo get the db correctly.
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE DATABASE cepdb");
                statement.close();
                return DriverManager.getConnection(RDBMSTestConstants.CONNECTION_URL + "/cepdb", RDBMSTestConstants.USERNAME, RDBMSTestConstants.PASSWORD);    // todo get the db correctly.

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
    /*
     delete with AND condition  [symbol, price]
     */
    public void testQuery1() throws InterruptedException {
        log.info("DeleteFromTableTestCase test5 OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");

        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from MYSQL.cepDataSource:cepdb.cepEventTable");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol==cseDeleteEventStream.symbol and cseEventTable.price != 75.6;");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    /*
     delete with OR condition  [symbol, price]
     */
    public void testQuery2() throws InterruptedException {
        log.info("DeleteFromTableTestCase test5 OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from MYSQL.cepDataSource:cepdb.cepEventTable");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol!='IBM';");
//                "    on cseEventTable.symbol!='IBM' or cseEventTable.price==10.0;");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    /*
     delete with AND condition. condition uses same parameter twice.
     */
    public void testQuery3() throws InterruptedException {
        log.info("DeleteFromTableTestCase test5 OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from MYSQL.cepDataSource:cepdb.cepEventTable ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol==cseDeleteEventStream.symbol and cseEventTable.symbol != 'WSO2';");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"IBM", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }


    @Test
    public void testSingleerDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        TableDefinition tableDefinition = QueryFactory.createTableDefinition();
        tableDefinition.name("cseEventTable").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT).from("MYSQL", "cepDataSource", "cepdb", "cepEventTable");
        siddhiManager.defineTable(tableDefinition);
    }

}
