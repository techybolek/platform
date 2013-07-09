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
package org.wso2.siddhi.test.management;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.treaser.EventTracer;
import org.wso2.siddhi.core.treaser.LogEventTracer;
import org.wso2.siddhi.core.util.EventPrinter;

public class TraceTestCase {

    static final Logger log = Logger.getLogger(PersistenceTestCase.class);

    private int count;
    private long lastValue;
    private long firstValue;
    private boolean eventArrived;

    @Before
    public void init() {
        lastValue = -1;
        firstValue = -1;
        count = 0;
        eventArrived = false;
    }


    @Test
    public void testFilterQuery1() throws InterruptedException {
        log.info("Filter test1");

        EventTracer eventTracer = new LogEventTracer();

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.setEventTracer(eventTracer);
        siddhiManager.enableStats(true);
        siddhiManager.enableTrace(true);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume != 100] " +
                                                       "select symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                //Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"FB", 57.6f, 10l});
        Thread.sleep(500);
        Assert.assertEquals(1, count);

        Assert.assertTrue(eventTracer.getStatStartTime() < eventTracer.getLastUpdateTime());
        Assert.assertEquals(2, eventTracer.getStreamStats().values().size()) ;
        Assert.assertEquals(2l, eventTracer.getStreamStats().get("cseEventStream").longValue()) ;
        Assert.assertEquals(1l, eventTracer.getStreamStats().get("OutputStream").longValue()) ;

        inputHandler.send(new Object[]{"GOOG", 55.6f, 99l});

        Assert.assertEquals(2, eventTracer.getStreamStats().values().size()) ;
        Assert.assertEquals(3l, eventTracer.getStreamStats().get("cseEventStream").longValue()) ;
        Assert.assertEquals(2l, eventTracer.getStreamStats().get("OutputStream").longValue()) ;

        eventTracer.resetStats();
        Assert.assertEquals(0, eventTracer.getStreamStats().values().size()) ;

        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"FB", 57.6f, 10l});
        Thread.sleep(500);
        Assert.assertEquals(3, count);

        Assert.assertTrue(eventTracer.getStatStartTime() < eventTracer.getLastUpdateTime());
        Assert.assertEquals(2, eventTracer.getStreamStats().values().size()) ;
        Assert.assertEquals(2l, eventTracer.getStreamStats().get("cseEventStream").longValue()) ;
        Assert.assertEquals(1l, eventTracer.getStreamStats().get("OutputStream").longValue()) ;

        siddhiManager.shutdown();


    }

}
