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

package org.wso2.carbon.transport.adaptor.email;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.transport.adaptor.core.AbstractTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.OutputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.InputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;
import org.wso2.carbon.transport.adaptor.email.internal.ds.EmailTransportAdaptorServiceValueHolder;
import org.wso2.carbon.transport.adaptor.email.internal.Axis2Util;
import org.wso2.carbon.transport.adaptor.email.internal.util.EmailTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class EmailTransportAdaptorType extends AbstractTransportAdaptor
        implements OutputTransportAdaptor, InputTransportAdaptor {

    private static final Log log = LogFactory.getLog(EmailTransportAdaptorType.class);

    private ConcurrentHashMap<OutputTransportMessageConfiguration, EmailSenderConfiguration> emailSenderConfigurationMap = new ConcurrentHashMap<OutputTransportMessageConfiguration, EmailSenderConfiguration>();
    private static EmailTransportAdaptorType emailTransportAdaptor = new EmailTransportAdaptorType();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, Integer.MAX_VALUE, AgentConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    private ResourceBundle resourceBundle;
    private Map<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>> inputTransportListenerMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, Map<String, TransportAdaptorListener>>();
    private Map<InputTransportMessageConfiguration, StreamDefinition> inputStreamDefinitionMap =
            new ConcurrentHashMap<InputTransportMessageConfiguration, StreamDefinition>();
    private Map<String, Map<String, TransportAdaptorListener>> streamIdTransportListenerMap =
            new ConcurrentHashMap<String, Map<String, TransportAdaptorListener>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>> dataPublisherMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputTransportAdaptorConfiguration, AsyncDataPublisher>>();


    private EmailTransportAdaptorType() {

    }

    @Override
    protected TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType() {
        return TransportAdaptorDto.TransportAdaptorType.INOUT;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportInputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.XML);
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.JSON);
        supportInputMessageTypes.add(TransportAdaptorDto.MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.XML);
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.JSON);
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.TEXT);
        return supportOutputMessageTypes;
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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.email.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {
        return null;
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set default outgoing mail subject
        Property subjectProperty = new Property(EmailTransportAdaptorConstants.TRANSPORT_CONF_EMAIL_DEFAULT_SUBJECT);
        subjectProperty.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_EMAIL_DEFAULT_SUBJECT));
        subjectProperty.setRequired(true);
        subjectProperty.setHint(resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_CONF_EMAIL_HINT_DEFAULT_SUBJECT));

        propertyList.add(subjectProperty);

        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set email address
        Property emailAddress = new Property(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_EMAIL_ADDRESS);
        emailAddress.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_EMAIL_ADDRESS));
        emailAddress.setRequired(true);


        // set email subject
        Property subject = new Property(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_EMAIL_SUBJECT);
        subject.setDisplayName(
                resourceBundle.getString(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_EMAIL_SUBJECT));
        subject.setRequired(true);

        propertyList.add(emailAddress);
        propertyList.add(subject);

        return propertyList;
    }

    /**
     * @param outputTransportMessageConfiguration
     *                - outputTransportMessageConfiguration to publish messages
     * @param message
     * @param outputTransportAdaptorConfiguration
     *                - transport configuration to be used
     */
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {

        EmailSenderConfiguration emailSenderConfiguration = emailSenderConfigurationMap.get(outputTransportMessageConfiguration);
        if (emailSenderConfiguration == null) {
            emailSenderConfiguration = new EmailSenderConfiguration(outputTransportMessageConfiguration, outputTransportAdaptorConfiguration.getOutputProperties().get(EmailTransportAdaptorConstants.TRANSPORT_CONF_EMAIL_DEFAULT_SUBJECT));
            emailSenderConfigurationMap.putIfAbsent(outputTransportMessageConfiguration, emailSenderConfiguration);
        }

        String[] emailIds = emailSenderConfiguration.getEmailIds();
        if (emailIds != null) {
            for (String email : emailIds) {
                threadPoolExecutor.submit(new EmailSender(email, emailSenderConfiguration.getSubject(), (String) message));
            }
        }
    }


    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        // no test
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
    public String subscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration, TransportAdaptorListener transportAdaptorListener, InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {
        // When publishing we only need to register the axis2 service
        String subscriptionId = UUID.randomUUID().toString();
        try {
            Axis2Util.registerAxis2EmailService(inputTransportMessageConfiguration, transportAdaptorListener,
                                                inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new TransportAdaptorEventProcessingException("Can not create the axis2 service to receive email events", axisFault);
        }
        return subscriptionId;    }

    @Override
    public void unsubscribe(InputTransportMessageConfiguration inputTransportMessageConfiguration, InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration, AxisConfiguration axisConfiguration, String subscriptionId) {
        try {
            Axis2Util.removeEmailServiceOperation(inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new TransportAdaptorEventProcessingException("Can not remove operation ", axisFault);
        }
    }


    class EmailSender implements Runnable {
        String to;
        String subject;
        String body;

        EmailSender(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        @Override
        public void run() {
            Map<String, String> headerMap = new HashMap<String, String>();
            headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, subject);
            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            payload.setText(body);

            try {
                ServiceClient serviceClient;
                ConfigurationContext configContext = EmailTransportAdaptorServiceValueHolder.getConfigurationContextService().getClientConfigContext();
                if (configContext != null) {
                    serviceClient = new ServiceClient(configContext, null);
                } else {
                    serviceClient = new ServiceClient();
                }
                Options options = new Options();
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
                options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT,
                                    MailConstants.TRANSPORT_FORMAT_TEXT);
                options.setTo(new EndpointReference("mailto:" + to));
                serviceClient.setOptions(options);
                serviceClient.fireAndForget(payload);
                log.debug("Sending confirmation mail to " + to);
            } catch (AxisFault e) {
                String msg = "Error in delivering the message, " +
                             "subject: " + subject + ", to: " + to + ".";
                log.error(msg);
            }
        }
    }

    private final class EmailSenderConfiguration {


        private String subject;
        private String[] emailIds;

        private EmailSenderConfiguration(
                OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                String defaultSubject) {
            if (defaultSubject != null) {
                subject = defaultSubject;
            } else {
                subject = "";
            }
            String emailIdString = null;
            if (outputTransportMessageConfiguration.getOutputMessageProperties().size() == 2) {
                subject = outputTransportMessageConfiguration.getOutputMessageProperties().get(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_EMAIL_SUBJECT);
                emailIdString = outputTransportMessageConfiguration.getOutputMessageProperties().get(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_EMAIL_ADDRESS);
            } else if (outputTransportMessageConfiguration.getOutputMessageProperties().size() == 1) {
                emailIdString = outputTransportMessageConfiguration.getOutputMessageProperties().get(EmailTransportAdaptorConstants.TRANSPORT_MESSAGE_EMAIL_ADDRESS);
                log.info("Subject is empty");
            } else {
                log.error("Doesn't contains E-mail ids hence no message will be sent");
            }
            emailIds = null;
            if (emailIdString != null) {
                emailIds = emailIdString.replaceAll(" ", "").split(",");
            }
        }

        public String getSubject() {
            return subject;
        }

        public String[] getEmailIds() {
            return emailIds;
        }
    }

}
