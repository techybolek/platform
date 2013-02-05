package org.wso2.siddhi.core.query.processor.handler.chain;

import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.BundleEvent;


public class PassThroughHandlerChain implements HandlerChain {
    @Override
    public AtomicEvent process(AtomicEvent atomicEvent) {
        return atomicEvent;
    }


    @Override
    public BundleEvent process(BundleEvent bundleEvent) {
        return bundleEvent;
    }
}
