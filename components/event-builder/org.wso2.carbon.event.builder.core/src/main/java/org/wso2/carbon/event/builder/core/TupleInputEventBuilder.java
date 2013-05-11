/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderServiceValueHolder;
import org.wso2.carbon.transport.adaptor.core.TransportListener;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.TransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportEventProcessingException;

import java.util.ArrayList;
import java.util.List;

public class TupleInputEventBuilder implements EventBuilder {
    private static final Log log = LogFactory.getLog(TupleInputEventBuilder.class);

    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private EventBuilderConfiguration eventBuilderConfiguration = null;

    public TupleInputEventBuilder(EventBuilderConfiguration eventBuilderConfiguration) {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
    }

    @Override
    public void subscribe(EventListener eventListener, AxisConfiguration axisConfiguration) {
        if(eventListener instanceof BasicEventListener) {
            basicEventListeners.add((BasicEventListener)eventListener);
        } else if(eventListener instanceof Wso2EventListener) {
            wso2EventListeners.add((Wso2EventListener)eventListener);
        }
        try {
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new TransportAdaptorConfiguration();
            inputTransportAdaptorConfiguration.setType(eventBuilderConfiguration.getInputTransportMessageConfiguration().getTransportName());

            //TODO This is to circumvent what seems to be a bug on the TA core. Need to remove after fix
            eventBuilderConfiguration.getInputTransportMessageConfiguration().setTransportName(null);

            EventBuilderServiceValueHolder.getTransportAdaptorService().subscribe(inputTransportAdaptorConfiguration, eventBuilderConfiguration.getInputTransportMessageConfiguration(), new TupleInputTransportListener(), axisConfiguration);
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

    private class TupleInputTransportListener implements TransportListener {

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
