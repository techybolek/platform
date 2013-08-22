/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.message.tracer.api.internal.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.message.tracer.api.data.Message;
import org.wso2.carbon.bam.message.tracer.api.internal.conf.EventPublisherConfig;
import org.wso2.carbon.bam.message.tracer.api.internal.conf.ServerConfig;
import org.wso2.carbon.bam.message.tracer.api.internal.utils.EventConfigUtil;
import org.wso2.carbon.bam.message.tracer.api.internal.utils.EventPublishConfigHolder;
import org.wso2.carbon.bam.message.tracer.api.internal.utils.StreamDefUtil;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

public class EventPublisher {

    public static final String UNDERSCORE = "_";

    private static Log log = LogFactory.getLog(EventPublisher.class);

    public void publish(Message message, ServerConfig serverConfig) {

        List<Object> correlationData = EventConfigUtil.getCorrelationData(message);
        List<Object> metaData = EventConfigUtil.getMetaData(message);
        List<Object> payLoadData = EventConfigUtil.getEventData(message);

        StreamDefinition streamDef = StreamDefUtil.getStreamDefinition();

        if (streamDef != null) {

            String key = serverConfig.getUrl() + UNDERSCORE + serverConfig.getUsername() + UNDERSCORE + serverConfig.getPassword();
            EventPublisherConfig eventPublisherConfig = EventPublishConfigHolder.getEventPublisherConfig(key);
            if (!serverConfig.isLoadBalancingConfig()) {
                try {
                    if (eventPublisherConfig == null) {
                        synchronized (EventPublisher.class) {
                            eventPublisherConfig = EventPublishConfigHolder.getEventPublisherConfig(key);
                            if (null == eventPublisherConfig) {
                                eventPublisherConfig = new EventPublisherConfig();
                                AsyncDataPublisher asyncDataPublisher = new AsyncDataPublisher(serverConfig.getUrl(),
                                                                                               serverConfig.getUsername(),
                                                                                               serverConfig.getPassword());
                                asyncDataPublisher.addStreamDefinition(streamDef);
                                eventPublisherConfig.setAsyncDataPublisher(asyncDataPublisher);
                                EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                            }
                        }
                    }

                    AsyncDataPublisher asyncDataPublisher = eventPublisherConfig.getAsyncDataPublisher();

                    asyncDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData),
                                               getObjectArray(correlationData),
                                               getObjectArray(payLoadData));

                } catch (AgentException e) {
                    log.error("Error occurred while sending the event", e);
                }
            } else {
                try {
                    if (eventPublisherConfig == null) {
                        synchronized (EventPublisher.class) {

                            eventPublisherConfig = EventPublishConfigHolder.getEventPublisherConfig(key);
                            if (null == eventPublisherConfig) {
                                eventPublisherConfig = new EventPublisherConfig();
                                ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
                                ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(serverConfig.getUrl());

                                for (String aReceiverGroupURL : receiverGroupUrls) {
                                    ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                                    String[] urls = aReceiverGroupURL.split(ServerConfig.URL_SEPARATOR);
                                    for (String aUrl : urls) {
                                        DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), serverConfig.getUsername(),
                                                                                            serverConfig.getPassword());
                                        dataPublisherHolders.add(aNode);
                                    }
                                    ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                                    allReceiverGroups.add(group);
                                }

                                LoadBalancingDataPublisher loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);

                                loadBalancingDataPublisher.addStreamDefinition(streamDef);
                                eventPublisherConfig.setLoadBalancingDataPublisher(loadBalancingDataPublisher);
                                EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                            }
                        }
                    }

                    LoadBalancingDataPublisher loadBalancingDataPublisher = eventPublisherConfig.getLoadBalancingDataPublisher();

                    loadBalancingDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData), getObjectArray(correlationData),
                                                       getObjectArray(payLoadData));

                } catch (AgentException e) {
                    log.error("Error occurred while sending the event", e);
                }
            }
        }
    }

    private Object[] getObjectArray(List<Object> list) {
        if (list.size() > 0) {
            return list.toArray();
        }
        return null;
    }
}
