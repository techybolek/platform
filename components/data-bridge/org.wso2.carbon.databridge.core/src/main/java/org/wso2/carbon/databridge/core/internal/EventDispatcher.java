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

package org.wso2.carbon.databridge.core.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.*;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.Utils.EventComposite;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.queue.EventQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispactches events  and their definitions subscribers
 */
public class EventDispatcher {

    private List<AgentCallback> subscribers = new ArrayList<AgentCallback>();
    private List<RawDataAgentCallback> rawDataSubscribers = new ArrayList<RawDataAgentCallback>();
    private AbstractStreamDefinitionStore streamDefinitionStore;
    private Map<String, StreamTypeHolder> domainNameStreamTypeHolderCache = new ConcurrentHashMap<String, StreamTypeHolder>();
    private EventQueue eventQueue;

    private static final Log log = LogFactory.getLog(EventDispatcher.class);


    public EventDispatcher(AbstractStreamDefinitionStore streamDefinitionStore,
                           DataBridgeConfiguration dataBridgeConfiguration) {
        this.eventQueue = new EventQueue(subscribers, rawDataSubscribers, dataBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
    }

    public void addCallback(AgentCallback agentCallback) {
        subscribers.add(agentCallback);
    }

    /**
     * Add thrift subscribers
     *
     * @param agentCallback
     */
    public void addCallback(RawDataAgentCallback agentCallback) {
        rawDataSubscribers.add(agentCallback);
    }

    public String defineStream(String streamDefinition, AgentSession agentSession)
            throws
            MalformedStreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionStoreException {
        synchronized (EventDispatcher.class) {
            StreamDefinition newStreamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinition);

            StreamDefinition existingStreamDefinition;
            existingStreamDefinition = streamDefinitionStore.getStreamDefinition(agentSession.getCredentials(), newStreamDefinition.getName(), newStreamDefinition.getVersion());
            if (null == existingStreamDefinition) {
                streamDefinitionStore.saveStreamDefinition(agentSession.getCredentials(), newStreamDefinition);
                updateDomainNameStreamTypeHolderCache(newStreamDefinition, agentSession.getCredentials());
            } else {
                if (!existingStreamDefinition.equals(newStreamDefinition)) {
                    throw new DifferentStreamDefinitionAlreadyDefinedException("Similar event stream for " + newStreamDefinition + " with the same name and version already exist: " + streamDefinitionStore.getStreamDefinition(agentSession.getCredentials(), newStreamDefinition.getName(), newStreamDefinition.getVersion()));
                }
                newStreamDefinition = existingStreamDefinition;
                //to load the stream definitions to cache
                getStreamDefinitionHolder(agentSession.getCredentials());
            }

            for (AgentCallback agentCallback : subscribers) {
                agentCallback.definedStream(newStreamDefinition, agentSession.getCredentials());
            }
            return newStreamDefinition.getStreamId();
        }
    }


    public void publish(Object eventBundle, AgentSession agentSession,
                        EventConverter eventConverter) {
        eventQueue.publish(new EventComposite(eventBundle, getStreamDefinitionHolder(agentSession.getCredentials()), agentSession, eventConverter));
    }

    private StreamTypeHolder getStreamDefinitionHolder(Credentials credentials) {
        // this will occur only outside of carbon (ex: Siddhi)

        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(credentials.getDomainName());

        if (streamTypeHolder != null) {
            if (log.isDebugEnabled()) {
                String logMsg = "Event stream holder for domain name : " + credentials.getDomainName() + " : \n ";
                logMsg += "Meta, Correlation & Payload Data Type Map : ";
                for (Map.Entry entry : streamTypeHolder.getAttributeCompositeMap().entrySet()) {
                    logMsg += "StreamID=" + entry.getKey() + " :  ";
                    logMsg += "Meta= " + Arrays.deepToString(((AttributeComposite) entry.getValue()).getAttributeTypes()[0]) + " :  ";
                    logMsg += "Correlation= " + Arrays.deepToString(((AttributeComposite) entry.getValue()).getAttributeTypes()[1]) + " :  ";
                    logMsg += "Payload= " + Arrays.deepToString(((AttributeComposite) entry.getValue()).getAttributeTypes()[2]) + "\n";
                }
                log.debug(logMsg);
            }
            return streamTypeHolder;
        } else {
            return initDomainNameStreamTypeHolderCache(credentials);
        }
    }

    private synchronized void updateDomainNameStreamTypeHolderCache(StreamDefinition streamDefinition, Credentials credentials) {
        StreamTypeHolder streamTypeHolder = getStreamDefinitionHolder(credentials);
        streamTypeHolder.putDataType(streamDefinition.getStreamId(),
                EventDefinitionConverterUtils.generateAttributeTypeArray(streamDefinition.getMetaData()),
                EventDefinitionConverterUtils.generateAttributeTypeArray(streamDefinition.getCorrelationData()),
                EventDefinitionConverterUtils.generateAttributeTypeArray(streamDefinition.getPayloadData()));
    }

    private synchronized StreamTypeHolder initDomainNameStreamTypeHolderCache(Credentials credentials) {
        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(credentials.getDomainName());
        if (null == streamTypeHolder) {
            streamTypeHolder = new StreamTypeHolder(credentials.getDomainName());
            Collection<StreamDefinition> allStreamDefinitions =
                    streamDefinitionStore.getAllStreamDefinitions(credentials);
            if (null != allStreamDefinitions) {
                for (StreamDefinition aStreamDefinition : allStreamDefinitions) {
                    streamTypeHolder.putDataType(aStreamDefinition.getStreamId(),
                            EventDefinitionConverterUtils.generateAttributeTypeArray(aStreamDefinition.getMetaData()),
                            EventDefinitionConverterUtils.generateAttributeTypeArray(aStreamDefinition.getCorrelationData()),
                            EventDefinitionConverterUtils.generateAttributeTypeArray(aStreamDefinition.getPayloadData()));
                }
            }
            domainNameStreamTypeHolderCache.put(credentials.getDomainName(), streamTypeHolder);
        }
        return streamTypeHolder;
    }


    public List<AgentCallback> getSubscribers() {
        return subscribers;
    }

    public List<RawDataAgentCallback> getRawDataSubscribers() {
        return rawDataSubscribers;
    }

    public String findStreamId(Credentials credentials, String streamName,
                               String streamVersion)
            throws StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamId(credentials, streamName, streamVersion);
    }

    public boolean deleteStream(Credentials credentials, String streamId) {
        removeStramDefinitionFromStreamTypeHolder(credentials, streamId);
        return streamDefinitionStore.deleteStreamDefinition(credentials, streamId);
    }

    public boolean deleteStream(Credentials credentials, String streamName, String streamVersion) {
        String streamId;
        try {
            streamId = streamDefinitionStore.getStreamId(credentials, streamName, streamVersion);
        } catch (Exception e) {
            log.warn("Error when deleting stream definition of " + streamName + " " + streamVersion, e);
            return false;
        }
        if (streamId == null) {
            return false;
        }
        removeStramDefinitionFromStreamTypeHolder(credentials, streamId);
        return streamDefinitionStore.deleteStreamDefinition(credentials, streamId);
    }

    private synchronized void removeStramDefinitionFromStreamTypeHolder(Credentials credentials, String streamId) {
        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(credentials.getDomainName());
        if (streamTypeHolder != null) {
            streamTypeHolder.getAttributeCompositeMap().remove(streamId);
        }
    }
}
