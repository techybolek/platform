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
package org.wso2.event.adaptor.kafka;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.event.adaptor.kafka.internal.util.ConsumerKafkaConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import kafka.consumer.ConsumerConfig;
import org.wso2.carbon.event.input.adaptor.core.MessageType;

public final class ConsumerKafkaType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(ConsumerKafkaType.class);
    private ResourceBundle resourceBundle;
    private static ConsumerKafkaType consumerKafkaAdaptor = new ConsumerKafkaType();
    ConsumerKafkaRedBorder consumerRedBorder;

    private ConsumerKafkaType() {
    }

    public static ConsumerKafkaType getInstance() {

        return consumerKafkaAdaptor;
    }

    @Override
    protected String getName() {
        return ConsumerKafkaConstants.BROKER_TYPE_REDBORDER;
    }

    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.MAP);
        return supportInputMessageTypes;
    }

    @Override
    protected void init() {
        this.resourceBundle = ResourceBundle.getBundle("org.wso2.event.adaptor.kafka.i18n.Resources", Locale.getDefault());
    }

    @Override
    protected List<Property> getInputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        //set Zk Connect of broker
        Property webZKConnect = new Property(ConsumerKafkaConstants.BROKER_SUSCRIBER_ZOOKEEPER_CONNECT);
        webZKConnect.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.BROKER_SUSCRIBER_ZOOKEEPER_CONNECT));
        webZKConnect.setRequired(true);
        propertyList.add(webZKConnect);

        //set GroupID of broker
        Property webGroupID = new Property(ConsumerKafkaConstants.BROKER_SUSCRIBER_GROUP_ID);
        webGroupID.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.BROKER_SUSCRIBER_GROUP_ID));
        webGroupID.setRequired(true);
        propertyList.add(webGroupID);

        //set GroupID of broker
        Property webThreads = new Property(ConsumerKafkaConstants.BROKER_SUSCRIBER_THREADS);
        webThreads.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.BROKER_SUSCRIBER_THREADS));
        webThreads.setRequired(true);
        propertyList.add(webThreads);

        return propertyList;
    }

    @Override
    protected List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();
        //set Topic of broker
        Property webTopic = new Property(ConsumerKafkaConstants.BROKER_SUSCRIBER_TOPIC);
        webTopic.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.BROKER_SUSCRIBER_TOPIC));
        webTopic.setRequired(true);
        propertyList.add(webTopic);
        return propertyList;
    }

    @Override
    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {

        Map<String, String> brokerProperties = new HashMap<String, String>();
        brokerProperties.putAll(inputEventAdaptorConfiguration.getInputProperties());
        String zkConnect = brokerProperties.get(ConsumerKafkaConstants.BROKER_SUSCRIBER_ZOOKEEPER_CONNECT);
        String groupID = brokerProperties.get(ConsumerKafkaConstants.BROKER_SUSCRIBER_GROUP_ID);
        String threadsStr = brokerProperties.get(ConsumerKafkaConstants.BROKER_SUSCRIBER_THREADS);
        int threads = Integer.parseInt(threadsStr);

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(ConsumerKafkaConstants.BROKER_SUSCRIBER_TOPIC);

        consumerRedBorder = new ConsumerKafkaRedBorder(zkConnect, groupID, topic,
                ConsumerKafkaType.createConsumerConfig(zkConnect, groupID));
        consumerRedBorder.run(threads, inputEventAdaptorListener);

        return null;
    }

    @Override
    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String s) {
        consumerRedBorder.shutdown();
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("auto.commit.enable", "true");
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "60000");
        props.put("auto.offset.reset", "largest");
        return new ConsumerConfig(props);
    }
}
