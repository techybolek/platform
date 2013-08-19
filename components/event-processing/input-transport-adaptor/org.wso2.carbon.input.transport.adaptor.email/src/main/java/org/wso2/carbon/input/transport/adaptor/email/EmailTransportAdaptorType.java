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

package org.wso2.carbon.input.transport.adaptor.email;


import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.input.transport.adaptor.core.AbstractInputTransportAdaptor;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.MessageType;
import org.wso2.carbon.input.transport.adaptor.core.Property;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.input.transport.adaptor.email.internal.Axis2Util;
import org.wso2.carbon.input.transport.adaptor.email.internal.util.EmailTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public final class EmailTransportAdaptorType extends AbstractInputTransportAdaptor {

    private static final Log log = LogFactory.getLog(EmailTransportAdaptorType.class);

    private static EmailTransportAdaptorType emailTransportAdaptor = new EmailTransportAdaptorType();
    private ResourceBundle resourceBundle;

    private EmailTransportAdaptorType() {

    }

    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }
    /**
     * @return Email transport adaptor instance
     */
    public static EmailTransportAdaptorType getInstance() {
        return emailTransportAdaptor;
    }

    /**
     * @return name of the Email transport adaptor
     */
    @Override
    protected String getName() {
        return EmailTransportAdaptorConstants.TRANSPORT_TYPE_EMAIL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.input.transport.adaptor.email.i18n.Resources", Locale.getDefault());
    }


    @Override
    public List<Property> getInputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set receiving mail address
        Property emailAddress = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_ADDRESS);
        emailAddress.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_ADDRESS));
        emailAddress.setRequired(true);
        emailAddress.setHint(resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_ADDRESS_HINT));
        propertyList.add(emailAddress);

        // set receiving mail protocol
        Property protocol = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PROTOCOL);
        protocol.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PROTOCOL));

        protocol.setOptions(new String[]{"pop3", "imap"});
        protocol.setDefaultValue("imap");
        protocol.setHint(resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PROTOCOL_HINT));
        propertyList.add(protocol);

        // set receiving mail poll interval
        Property pollInterval = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_POLL_INTERVAL);
        pollInterval.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_POLL_INTERVAL));
        pollInterval.setRequired(true);
        pollInterval.setHint(resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_POLL_INTERVAL_HINT));
        propertyList.add(pollInterval);

        // set receiving mail host
        Property host = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PROTOCOL_HOST);
        host.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PROTOCOL_HOST));
        host.setRequired(true);
        propertyList.add(host);

        // set receiving mail host
        Property port = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PROTOCOL_PORT);
        port.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PROTOCOL_PORT));
        port.setRequired(true);
        propertyList.add(port);

        // set receiving mail username
        Property userName = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_USERNAME);
        userName.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_USERNAME));
        userName.setRequired(true);
        propertyList.add(userName);

        // set receiving mail password
        Property password = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PASSWORD);
        password.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_PASSWORD));
        password.setRequired(true);
        password.setSecured(true);
        propertyList.add(password);

        // set receiving mail socket factory class
        Property socketFactoryClass = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_CLASS);
        socketFactoryClass.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_CLASS));
        socketFactoryClass.setRequired(true);
        propertyList.add(socketFactoryClass);

        // set receiving mail socket factory fallback
        Property socketFactoryFallback = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_FALLBACK);
        socketFactoryFallback.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_FALLBACK));
        socketFactoryFallback.setRequired(true);
        socketFactoryFallback.setOptions(new String[]{"true", "false"});
        socketFactoryFallback.setDefaultValue("false");
        propertyList.add(socketFactoryFallback);

        return propertyList;

    }

    @Override
    public List<Property> getInputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set incoming email subject
        Property subject = new Property(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_RECEIVING_EMAIL_SUBJECT);
        subject.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_RECEIVING_EMAIL_SUBJECT));
        subject.setRequired(true);
        subject.setHint(resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_RECEIVING_EMAIL_SUBJECT_HINT));

        propertyList.add(subject);
        return propertyList;
    }

    @Override
    public String subscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration, InputTransportAdaptorListener inputTransportAdaptorListener, InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {

        // When publishing we only need to register the axis2 service
        String subscriptionId = UUID.randomUUID().toString();
        try {
            Axis2Util.registerAxis2EmailService(inputTransportMessageConfiguration, inputTransportAdaptorListener,
                                                inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputTransportAdaptorEventProcessingException("Can not create the axis2 service to receive email events", axisFault);
        }
        return subscriptionId;    }

    @Override
    public void unsubscribe(InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration, InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration, AxisConfiguration axisConfiguration, String subscriptionId) {
        try {
            Axis2Util.removeEmailServiceOperation(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputTransportAdaptorEventProcessingException("Can not remove operation ", axisFault);
        }
    }



}
