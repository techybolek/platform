package org.wso2.siddhi.test.standard.table.rdbms;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.table.PooledDataSource;
import org.wso2.siddhi.core.table.SiddhiDataSource;

public class H2DatabaseTestCase {
    static final Logger log = Logger.getLogger(H2DatabaseTestCase.class);

    private int count;
    private boolean eventArrived;
    private SiddhiDataSource dataSource;

    @Before
    public void init() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        count = 0;
        eventArrived = false;
        dataSource = new PooledDataSource(RDBMSTestConstants.H2_DRIVER_CLASS, RDBMSTestConstants.H2_CONNECTION_URL, RDBMSTestConstants.USERNAME, RDBMSTestConstants.PASSWORD,
                "H2", "cepDataSource");
    }

    @Test
    public void testQuerySingleConnection() throws InterruptedException {
        log.info("InsertIntoTableTestCase test1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from H2.cepDataSource:cepdb.cepEventTable");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("InsertIntoTableTestCase test2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from H2.cepDataSource:cepdb.cepEventTable");
        siddhiManager.defineTable("define table cseEventTable2 (symbol string, price float, volume long)  from H2.cepDataSource:cepdb.cepEventTable2");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");
        siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable2;");

        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("InsertIntoTableTestCase test3");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseEventStream2 (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  from H2.cepDataSource:cepdb.cepEventTable");
        siddhiManager.defineTable("define table cseEventTable2 (symbol string, price float, volume long) from H2.cepDataSource:cepdb.cepEventTable2");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");
        siddhiManager.addQuery("from cseEventStream2 " +
                "insert into cseEventTable2;");

        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }
}
