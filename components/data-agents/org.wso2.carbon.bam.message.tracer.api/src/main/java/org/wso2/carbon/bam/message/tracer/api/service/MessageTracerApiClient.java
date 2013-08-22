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
package org.wso2.carbon.bam.message.tracer.api.service;


import org.wso2.carbon.bam.message.tracer.api.data.Message;
import org.wso2.carbon.bam.message.tracer.api.internal.conf.ServerConfig;
import org.wso2.carbon.bam.message.tracer.api.internal.publisher.EventPublisher;

public class MessageTracerApiClient {

    private ServerConfig serverConfig;

    public MessageTracerApiClient(String url, String username, String password) {
        serverConfig = new ServerConfig(url, username, password);
    }

    void publishMessage(Message message) {
        EventPublisher eventPublisher = new EventPublisher();
        eventPublisher.publish(message, serverConfig);
    }
}
