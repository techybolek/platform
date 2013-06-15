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
package org.wso2.carbon.message.store.persistence.jms.util;

public class JMSConstants {

    /**
     * Value indicating a JMS 1.1 Generic Destination used by {@link DEST_PARAM_TYPE}, {@link REPLY_PARAM_TYPE}
     */
    public static final String DESTINATION_TYPE_GENERIC = "generic";
    /**
     * Value indicating a Queue used for {@link DEST_PARAM_TYPE}, {@link REPLY_PARAM_TYPE}
     */
    public static final String DESTINATION_TYPE_QUEUE = "queue";
    /**
     * Value indicating a Topic used for {@link DEST_PARAM_TYPE}, {@link REPLY_PARAM_TYPE}
     */
    public static final String DESTINATION_TYPE_TOPIC = "topic";

    /**
     * Naming factory initial
     */
    public static final String NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    /**
     * Default Connection Factory
     */
    public static final String CONNECTION_STRING = "connectionfactory.QueueConnectionFactory";
    /**
     * JNDI Topic prefix
     */
    public static final String TOPIC_PREFIX = "topic.";
    /**
     * JNDI Queue prefix
     */
    public static final String QUEUE_PREFIX = "queue.";

    /**
     * Andes Naming Factory
     */
    public static final String ANDES_NAMING_FACTORY = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";


    /**
     * Constant that holds the name of the environment property
     * for specifying configuration information for the service provider
     * to use. The value of the property should contain a URL string
     * (e.g. "ldap://somehost:389").
     * This property may be specified in the environment,
     * an applet parameter, a system property, or a resource file.
     * If it is not specified in any of these sources,
     * the default configuration is determined by the service provider.
     * <p/>
     * <p> The value of this constant is "java.naming.provider.url".
     */
    public static final String PROVIDER_URL = "java.naming.provider.url";

}
