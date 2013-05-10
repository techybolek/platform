package org.wso2.carbon.event.builder.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderServiceValueHolder;
import org.wso2.carbon.transport.adaptor.core.TransportListener;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;

import java.util.ArrayList;
import java.util.List;

public class XmlInputEventBuilder implements EventBuilder {
    private static final Log log = LogFactory.getLog(XmlInputEventBuilder.class);

    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration(null);

    @Override
    public void subscribe(EventListener eventListener) {
        if(eventListener instanceof BasicEventListener) {
            basicEventListeners.add((BasicEventListener)eventListener);
        } else if(eventListener instanceof Wso2EventListener) {
            wso2EventListeners.add((Wso2EventListener)eventListener);
        }
        try {
            EventBuilderServiceValueHolder.getTransportAdaptorService().subscribe(null, eventBuilderConfiguration.getInputTransportMessageConfiguration(), new XMLInputTransportListener(), null);
        } catch (TransportEventProcessingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void unsubscribe(EventListener eventListener) {
        if(eventListener instanceof BasicEventListener) {
            basicEventListeners.remove(eventListener);
        } else if(eventListener instanceof Wso2EventListener) {
            wso2EventListeners.remove(eventListener);
        }
    }

    @Override
    public EventBuilderConfiguration getEventBuilderConfiguration() {
        return eventBuilderConfiguration;
    }

    @Override
    public void configureEventBuilder(EventBuilderConfiguration builderConfiguration) {
        this.eventBuilderConfiguration = builderConfiguration;
    }

    public void sendEvent(Object obj) {
        for(BasicEventListener basicEventListener : basicEventListeners) {
            sendEvent(basicEventListener, obj);
        }
        for(Wso2EventListener wso2EventListener : wso2EventListeners) {
            sendEvent(wso2EventListener, obj);
        }
    }

    public void sendEvent(Wso2EventListener eventListener, Object obj) {
        log.debug(obj.toString());
    }

    public void sendEvent(BasicEventListener basicEventListener, Object obj) {
        log.debug(obj.toString());
    }

    private class XMLInputTransportListener implements TransportListener {

        @Override
        public void addEventDefinition(Object o) throws TransportEventProcessingException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void removeEventDefinition(Object o) throws TransportEventProcessingException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onEvent(Object o) throws TransportEventProcessingException {
            sendEvent(o);
        }
    }
}
