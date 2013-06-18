/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.transport.adaptor.wsevent.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionMessageReceiver extends AbstractInMessageReceiver {

    private static final Log log = LogFactory.getLog(SubscriptionMessageReceiver.class);

    private List<TransportAdaptorListener> transportAdaptorListeners;
    private ConcurrentHashMap<String, TransportAdaptorListener> transportAdaptorListenerMap;

    public SubscriptionMessageReceiver() {
        this.transportAdaptorListenerMap = new ConcurrentHashMap<String, TransportAdaptorListener>();
        this.transportAdaptorListeners = new ArrayList<TransportAdaptorListener>();
    }

    public void addTransportAdaptorListener(String subscriptionId,
                                            TransportAdaptorListener transportAdaptorListener) {
        if (null == transportAdaptorListenerMap.putIfAbsent(subscriptionId, transportAdaptorListener)) {
            this.transportAdaptorListeners = new ArrayList<TransportAdaptorListener>(transportAdaptorListenerMap.values());
        }
    }

    public boolean removeTransportAdaptorListener(String subscriptionId) {
        if (null != transportAdaptorListenerMap.remove(subscriptionId)) {
            this.transportAdaptorListeners = new ArrayList<TransportAdaptorListener>(transportAdaptorListenerMap.values());
        }
        if (transportAdaptorListeners.size() == 0) {
            return true;
        }
        return false;
    }

    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        OMElement bodyElement = soapEnvelope.getBody().getFirstElement();

        // notify the TransportAdaptorProxies
        try {
            for (TransportAdaptorListener transportAdaptorListener : this.transportAdaptorListeners) {
                transportAdaptorListener.onEvent(bodyElement);
            }
        } catch (TransportAdaptorEventProcessingException e) {
            log.error("Can not process the received event ", e);
        }
    }
}