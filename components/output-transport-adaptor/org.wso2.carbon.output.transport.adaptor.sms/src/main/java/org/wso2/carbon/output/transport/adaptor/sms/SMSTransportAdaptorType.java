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

package org.wso2.carbon.output.transport.adaptor.sms;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;
import org.wso2.carbon.output.transport.adaptor.core.AbstractOutputTransportAdaptor;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorDto;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.output.transport.adaptor.sms.internal.ds.SMSTransportAdaptorServiceValueHolder;
import org.wso2.carbon.output.transport.adaptor.sms.internal.util.SMSTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class SMSTransportAdaptorType extends AbstractOutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(SMSTransportAdaptorType.class);

    private static SMSTransportAdaptorType SMSTransportAdaptor = new SMSTransportAdaptorType();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, Integer.MAX_VALUE, AgentConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
    private ConcurrentHashMap<OutputTransportAdaptorMessageConfiguration, List<String>> smsSenderConfigurationMap = new ConcurrentHashMap<OutputTransportAdaptorMessageConfiguration, List<String>>();
    private ResourceBundle resourceBundle;
    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);

    private SMSTransportAdaptorType() {

    }

    @Override
    protected List<OutputTransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<OutputTransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<OutputTransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(OutputTransportAdaptorDto.MessageType.XML);
        supportOutputMessageTypes.add(OutputTransportAdaptorDto.MessageType.JSON);
        supportOutputMessageTypes.add(OutputTransportAdaptorDto.MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return Email transport adaptor instance
     */
    public static SMSTransportAdaptorType getInstance() {
        return SMSTransportAdaptor;
    }

    /**
     * @return name of the Email transport adaptor
     */
    @Override
    protected String getName() {
        return SMSTransportAdaptorConstants.TRANSPORT_TYPE_SMS;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.output.transport.adaptor.sms.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {
        return null;
    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set sms address
        Property phoneNo = new Property(SMSTransportAdaptorConstants.TRANSPORT_MESSAGE_SMS_NO);
        phoneNo.setDisplayName(
                resourceBundle.getString(SMSTransportAdaptorConstants.TRANSPORT_MESSAGE_SMS_NO));
        phoneNo.setHint(resourceBundle.getString(SMSTransportAdaptorConstants.TRANSPORT_CONF_SMS_HINT_NO));
        phoneNo.setRequired(true);

        propertyList.add(phoneNo);
        return propertyList;
    }

    /**
     * @param outputTransportMessageConfiguration
     *                - outputTransportMessageConfiguration to publish messages
     * @param message
     * @param outputTransportAdaptorConfiguration
     *                - transport configuration to be used
     */
    public void publish(
            OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration,
            Object message,
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {

        List<String> smsList = smsSenderConfigurationMap.get(outputTransportMessageConfiguration.getOutputMessageProperties().get(SMSTransportAdaptorConstants.TRANSPORT_MESSAGE_SMS_NO));
        if (smsList == null) {
            smsList = new ArrayList<String>();
            smsList.add(outputTransportMessageConfiguration.getOutputMessageProperties().get(SMSTransportAdaptorConstants.TRANSPORT_MESSAGE_SMS_NO));
            smsSenderConfigurationMap.putIfAbsent(outputTransportMessageConfiguration, smsList);
        }

        String[] smsNOs = smsList.toArray(new String[0]);
        if (smsNOs != null) {
            for (String smsNo : smsNOs) {
                OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                        BaseConstants.DEFAULT_TEXT_WRAPPER, null);
                payload.setText((String) message);

                try {
                    ServiceClient serviceClient;
                    ConfigurationContext configContext = SMSTransportAdaptorServiceValueHolder.getConfigurationContextService().getClientConfigContext();
                    if (configContext != null) {
                        serviceClient = new ServiceClient(configContext, null);
                    } else {
                        serviceClient = new ServiceClient();
                    }
                    Options options = new Options();
                    options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                    options.setTo(new EndpointReference("sms://" + smsNo));
                    serviceClient.setOptions(options);
                    serviceClient.fireAndForget(payload);

                } catch (AxisFault axisFault) {
                    String msg = "Error in delivering the message, " +
                                 "message: " + message + ", to: " + smsNo + ".";
                    log.error(msg, axisFault);
                }
            }
        }
    }


    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        // no test
    }

}
