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

import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.DifferentDefinitionAlreadyExistException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.table.SiddhiDataSource;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DefineRDBMSTableTestCase {

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
    public void testSingleDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);
        TableDefinition tableDefinition = QueryFactory.createTableDefinition();
        tableDefinition.name("cseEventTable").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);
        tableDefinition.from("MYSQL", "cepDataSource", "cepdb", "testTable");

        siddhiManager.defineTable(tableDefinition);
    }

    @Test
    public void testSingleDefinitionQuery() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);
        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) from MYSQL.cepDataSource:cepdb.cepSingleDefinitionTable");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition1() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) from MYSQL.cepDataSource:cepdb.cepEventTable0");
        siddhiManager.defineTable("define table cseEventTable(symbols string, price int, volume float) from MYSQL.cepDataSource:cepdb.cepEventTable0");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition2() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, volume float) from MYSQL.cepDataSource:cepdb.cepEventTableDiff");
        siddhiManager.defineTable("define table cseEventTable(symbols string, price int, volume float) from MYSQL.cepDataSource:cepdb.cepEventTableDiff");
    }

    @Test
    public void testAddingTwoSameDefinition3() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) ");
        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) ");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition4() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventTable(symbol string, price int, volume float) ");
        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) from MYSQL.cepDataSource:cepdb.cepEventTableTwoDiff ");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition5() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float)  from MYSQL.cepDataSource:cepdb.cepEventTableTwoDiff");
        siddhiManager.defineStream("define stream cseEventTable(symbol string, price int, volume float)");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testOutputDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select * " +
                                                       "insert into OutputStream ;");


        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from MYSQL.cepDataSource:cepdb.cepEventTable1");
    }

    @Test
    public void testOutputDefinition2() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from MYSQL.cepDataSource:cepdb.cepEventTable2");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select * " +
                                                       "insert into OutputStream ;");

    }

    @Test(expected = QueryCreationException.class)
    public void testOutputDefinition3() {
        String ts = Float.toString(Float.MIN_VALUE);
        System.out.println(ts.length());
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from MYSQL.cepDataSource:cepdb.cepEventTable ");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long, time long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select * " +
                                                       "insert into OutputStream ;");
    }

    @Test(expected = QueryCreationException.class)
    public void testOutputDefinition4() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from MYSQL.cepDataSource:cepdb.cepEventTable4 ");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select * " +
                                                       "insert into OutputStream ;");

    }

    @Test
    public void testOutputDefinition5() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from MYSQL.cepDataSource:cepdb.cepEventTable5 ");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, test long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select * " +
                                                       "insert into OutputStream ;");

    }

    @Test
    public void testSingleerDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        TableDefinition tableDefinition = QueryFactory.createTableDefinition();
        tableDefinition.name("cseEventTable").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT).from("MYSQL", "cepDataSource", "", "");
        tableDefinition.from("MYSQL", "cepDataSource", "cepdb", "testEventTable");

        siddhiManager.defineTable(tableDefinition);
    }


}
