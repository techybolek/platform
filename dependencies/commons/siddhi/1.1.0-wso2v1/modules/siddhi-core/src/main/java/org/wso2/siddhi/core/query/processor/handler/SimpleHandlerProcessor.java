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
package org.wso2.siddhi.core.query.processor.handler;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.*;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.persistence.ThreadBarrier;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.query.SchedulerElement;
import org.wso2.siddhi.core.query.processor.handler.chain.HandlerChain;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerQueue;
import org.wso2.siddhi.query.api.query.input.BasicStream;

import java.util.concurrent.ThreadPoolExecutor;

public class SimpleHandlerProcessor implements HandlerProcessor, Runnable, SchedulerElement {

    static final Logger log = Logger.getLogger(SimpleHandlerProcessor.class);

    private BasicStream inputStream;
    private ThreadPoolExecutor threadPoolExecutor;
    private SchedulerQueue<StreamEvent> inputQueue;
    private HandlerChain handlerChain;
    private SiddhiContext context;
    private final ThreadBarrier threadBarrier;


    protected QueryPostProcessingElement next;

    public SimpleHandlerProcessor(BasicStream inputStream,
                                  HandlerChain handlerChain,
                                  SiddhiContext siddhiContext) {
        this.inputStream = inputStream;
        this.handlerChain = handlerChain;
        this.threadPoolExecutor = siddhiContext.getThreadPoolExecutor();
        this.context = siddhiContext;
        this.threadBarrier = siddhiContext.getThreadBarrier();
        this.inputQueue = new SchedulerQueue<StreamEvent>(this);

    }

    @Override
    public void receive(StreamEvent streamEvent) {
//        System.out.println(event);
        if (context.isAsyncProcessing() || context.isDistributedProcessing()) {
            inputQueue.put(streamEvent);
        } else {
            if (streamEvent instanceof  AtomicEvent) {
                processHandler((AtomicEvent) streamEvent);
            } else {
                processHandler((BundleEvent) streamEvent);
            }
        }
    }


    @Override
    public void run() {
        InListEvent listEvent = new InListEvent();
        while (true) {
            threadBarrier.pass();
            StreamEvent streamEvent = inputQueue.poll();
            if (streamEvent == null) {
                if (listEvent.getActiveEvents() == 1) {
                    processHandler(listEvent.getEvent0());
                } else if (listEvent.getActiveEvents() > 1) {
                    processHandler(listEvent);
                }
                break;
            } else if (context.getEventBatchSize() > 0 && listEvent.getActiveEvents() > context.getEventBatchSize()) {
                if (listEvent.getActiveEvents() == 1) {
                    processHandler(listEvent.getEvent0());
                } else if (listEvent.getActiveEvents() > 1) {
                    processHandler(listEvent);
                }
                threadPoolExecutor.execute(this);
                break;
            }
            if (streamEvent instanceof ListEvent) {
                for (Event event : ((ListEvent) streamEvent).getEvents()) {
                    listEvent.addEvent(event);
                }
            } else {
                listEvent.addEvent((Event) streamEvent);
            }
        }
    }

    private void processHandler(BundleEvent bundleEvent) {
        bundleEvent = handlerChain.process(bundleEvent);
        if (bundleEvent != null) {
            next.process(bundleEvent);
        }
    }

    private void processHandler(AtomicEvent atomicEvent) {
        atomicEvent = handlerChain.process(atomicEvent);
        if (atomicEvent != null) {
            next.process(atomicEvent);
        }
    }

    public String getStreamId() {
        return inputStream.getStreamId();
    }

    @Override
    public void schedule() {
        threadPoolExecutor.execute(this);
    }

    @Override
    public void scheduleNow() {
        threadPoolExecutor.execute(this);
    }

    public void setNext(QueryPostProcessingElement next) {
        this.next = next;
    }
}
