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
package org.wso2.event.adaptor.kafka.internal.util;

public final class ConsumerKafkaConstants {

    private ConsumerKafkaConstants() {
    }

    public static String BROKER_TYPE_REDBORDER = "kafkaConsumerBroker";
    public static String BROKER_SUSCRIBER_TOPIC = "topic";
    public static String BROKER_SUSCRIBER_GROUP_ID = "group.id";
    public static String BROKER_SUSCRIBER_ZOOKEEPER_CONNECT = "zookeeper.id";
    public static String BROKER_SUSCRIBER_THREADS = "threads";

}
