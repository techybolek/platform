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

package org.wso2.carbon.transport.adaptor.jms.util;


public interface JMSTransportAdaptorConstants {

    String TRANSPORT_TYPE_JMS = "jms";

    String JNDI_INITIAL_CONTEXT_FACTORY_CLASS = "java.naming.factory.initial";
    String JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT = "java.naming.factory.initial.hint";
    String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";
    String JAVA_NAMING_PROVIDER_URL_HINT = "java.naming.provider.url.hint";
    String JAVA_NAMING_SECURITY_PRINCIPAL = "java.naming.security.principal";
    String JAVA_NAMING_SECURITY_CREDENTIALS = "java.naming.security.credentials";
    String TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME = "transport.jms.ConnectionFactoryJNDIName";
    String TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME_HINT = "transport.jms.ConnectionFactoryJNDIName.hint";
    String TRANSPORT_JMS_DURABLE_SUBSCRIBER_NAME = "transport.jms.DurableSubscriberName";
    String TRANSPORT_JMS_DURABLE_SUBSCRIBER_NAME_HINT = "transport.jms.DurableSubscriberName.hint";
    String TRANSPORT_JMS_SUBSCRIPTION_DURABLE = "transport.jms.SubscriptionDurable";
    String TRANSPORT_JMS_SUBSCRIPTION_DURABLE_HINT = "transport.jms.SubscriptionDurable.hint";
    String TRANSPORT_JMS_DESTINATION_TYPE = "transport.jms.DestinationType";
    String TRANSPORT_JMS_DESTINATION_TYPE_HINT = "transport.jms.DestinationType.hint";

    String TRANSPORT_JMS_DESTINATION = "transport.jms.Destination";

}
