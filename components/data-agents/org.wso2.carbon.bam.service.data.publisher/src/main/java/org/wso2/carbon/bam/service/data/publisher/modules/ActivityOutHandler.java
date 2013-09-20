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
package org.wso2.carbon.bam.service.data.publisher.modules;


import org.apache.axiom.om.*;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherUtil;
import org.wso2.carbon.bam.service.data.publisher.conf.EventConfigNStreamDef;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.publish.EventPublisher;
import org.wso2.carbon.bam.service.data.publisher.publish.ServiceAgentUtil;
import org.wso2.carbon.bam.service.data.publisher.util.ActivityPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.TenantEventConfigData;
import org.wso2.carbon.core.util.SystemFilter;

import java.sql.Timestamp;
import java.util.*;

public class ActivityOutHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(ActivityOutHandler.class);

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        int tenantID = PublisherUtil.getTenantId(messageContext);
        Map<Integer, EventConfigNStreamDef> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
        EventConfigNStreamDef eventingConfigData = tenantSpecificEventConfig.get(tenantID);

        if (eventingConfigData != null && eventingConfigData.isMsgDumpingEnable()) {

            AxisService service = messageContext.getAxisService();

            if (service == null || SystemFilter.isFilteredOutService(service.getAxisServiceGroup()) || service.isClientSide()) {
                return InvocationResponse.CONTINUE;
            } else {

                if (messageContext.getMessageID() == null) {
                    messageContext.setMessageID(getUniqueId());
                }
                //get IN Message Context from OutMessageContext to track request and response
                MessageContext inMessageContext = messageContext.getOperationContext().getMessageContext(
                        WSDL2Constants.MESSAGE_LABEL_IN);


                String activityID = getUniqueId();
                EventData eventData = new EventData();

                PublishData publishData = new PublishData();

                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());

                //engage transport headers
                Object transportHeaders = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

                Object inTransportHeaders = inMessageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

                if(transportHeaders != null) {
                    String aid = (String) ((Map) transportHeaders).get(ActivityPublisherConstants.ACTIVITY_ID);

                    if (aid ==  null || aid.equals("")) {
                        if(inTransportHeaders != null) {
                            String inID = (String) ((Map) inTransportHeaders).get(ActivityPublisherConstants.ACTIVITY_ID);
                            if (! ((inID == null) || (inID.equals("")))){
                                activityID = inID;
                                log.info("OUT using IN's AID, transport header present");
                            }
                        }
                        ((Map)messageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).
                                put(ActivityPublisherConstants.ACTIVITY_ID, activityID);
                    } else {
                        activityID = aid;
                        log.info("OUT using "+aid);
                    }

                } else {
                    if(inTransportHeaders != null) {
                        String inID = (String) ((Map) inTransportHeaders).get(ActivityPublisherConstants.ACTIVITY_ID);
                        if (! ((inID == null) || (inID.equals("")))){
                            activityID = inID;
                            log.info("OUT using IN's AID, transport header absent");
                        }
                    }
                    Map<String, String> headers = new TreeMap<String, String>();
                    headers.put(ActivityPublisherConstants.ACTIVITY_ID, activityID);
                    messageContext.setProperty(MessageContext.TRANSPORT_HEADERS, headers);
                }


                addDetailsOfTheMessage(eventData, timestamp, activityID, messageContext);

                publishData.setEventData(eventData);


                BAMServerInfo bamServerInfo = ServiceAgentUtil.addBAMServerInfo(eventingConfigData);
                publishData.setBamServerInfo(bamServerInfo);

                Event event = ServiceAgentUtil.makeEventList(publishData, eventingConfigData);
                EventPublisher publisher = new EventPublisher();
                publisher.publish(event, eventingConfigData);

            }
        }

        return InvocationResponse.CONTINUE;
    }


    private EventData addDetailsOfTheMessage(EventData eventData, Timestamp timestamp,
                                             String activityID,
                                             MessageContext outMessageContext) throws AxisFault {

        // INFLOW is no longer being checked

        eventData.setTimestamp(timestamp);
        eventData.setActivityId(activityID);
        eventData.setOperationName(outMessageContext.getAxisService().getName());
        eventData.setServiceName(outMessageContext.getAxisOperation().getName().getLocalPart());

        eventData.setMessageDirection(BAMDataPublisherConstants.OUT_DIRECTION);
        eventData.setMessageId(outMessageContext.getMessageID());

        SOAPEnvelope envelope = outMessageContext.getEnvelope();

        String soapBody = null;
        String soapHeader = null;
        try {
            SOAPHeader header = envelope.getHeader();
            SOAPBody body = envelope.getBody();

            soapHeader = (header == null) ? "" : header.toString();
            soapBody = (body == null) ? "" : body.toString();

        } catch (OMException e) {
            log.warn("Exception occurred while getting SOAP envelope", e);
        }

        eventData.setSOAPHeader(soapHeader);
        eventData.setSOAPBody(soapBody);

        return eventData;
    }

    public String getUniqueId() {
        return System.nanoTime() + "_" + Thread.currentThread().getId();
    }

}