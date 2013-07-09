/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.message.store;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.SynapseEnvironment;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractMessageStore implements MessageStore {
    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    protected static final OMNamespace nullNS = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    /**
     * message store name
     */
    protected String name;

    /**
     * name of the sequence to be executed before storing the message
     */
    protected String sequence;

    /**
     * Message store JMX view
     */
    protected MessageStoreView messageStoreMBean;

    /**
     * synapse configuration reference
     */
    protected SynapseConfiguration synapseConfiguration;

    /**
     * synapse environment reference
     */
    protected SynapseEnvironment synapseEnvironment;

    /**
     * Message store parameters
     */
    protected Map<String,Object> parameters;

    /**
     * Message Store description
     */
    protected String description;

    /**
     * Name of the file where this message store is defined
     */
    protected String fileName;

    /**
     * List that holds the MessageStore observers registered with the Message Store
     */
    protected List<MessageStoreObserver> messageStoreObservers =
            new ArrayList<MessageStoreObserver>();

    protected Lock lock = new ReentrantLock();


    public void init(SynapseEnvironment se) {
        this.synapseEnvironment = se;
        this.synapseConfiguration = synapseEnvironment.getSynapseConfiguration();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        messageStoreMBean = new MessageStoreView(name, this);
        MBeanRegistrar.getInstance().registerMBean(messageStoreMBean,
                "MessageStore", this.name);
    }

    public void registerObserver(MessageStoreObserver observer) {
        if(observer != null && !messageStoreObservers.contains(observer)) {
            messageStoreObservers.add(observer);
        }
    }

    public void unregisterObserver(MessageStoreObserver observer) {
        if(observer != null && messageStoreObservers.contains(observer)) {
            messageStoreObservers.remove(observer);
        }
    }

    /**
     * Notify Message Addition to the observers
     * @param messageId of the Message added.
     */
    protected void notifyMessageAddition(String messageId) {
        for(MessageStoreObserver o : messageStoreObservers) {
            o.messageAdded(messageId);
        }
    }

    /**
     * Notify Message removal to the observers
     * @param messageId of the Message added
     */
    protected void notifyMessageRemoval(String messageId) {
        for(MessageStoreObserver o : messageStoreObservers) {
            o.messageRemoved(messageId);
        }
    }

    public int size() {
        return -1;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void destroy() {
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setFileName(String filename) {
        this.fileName = filename;
    }

    public String getFileName() {
        return this.fileName;
    }

    public Lock getLock() {
        return lock;
    }

    public OMElement serialize() {
        OMElement store = fac.createOMElement("messageStore", synNS);

        if (!getClass().getName().equals(InMemoryMessageStore.class.getName())) {
            store.addAttribute(fac.createOMAttribute("class", nullNS,
                                                     getClass().getName()));
        }
        if (getName() != null) {
            store.addAttribute(fac.createOMAttribute("name", nullNS, getName()));
        }
        if (getParameters() != null) {
            Iterator iter = getParameters().keySet().iterator();
            while (iter.hasNext()) {
                String name = (String) iter.next();
                String value = (String) getParameters().get(name);
                OMElement property = fac.createOMElement("parameter", synNS);
                property.addAttribute(fac.createOMAttribute(
                        "name", nullNS, name));
                property.setText(value.trim());
                store.addChild(property);
            }
        }
        if (getDescription() != null) {
            OMElement descriptionElem = fac.createOMElement(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "description"));
            descriptionElem.setText(getDescription());
            return descriptionElem;
        }
        return store;
    }
}
