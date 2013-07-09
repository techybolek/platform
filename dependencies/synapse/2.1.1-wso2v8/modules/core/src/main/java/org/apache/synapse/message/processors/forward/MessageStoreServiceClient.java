package org.apache.synapse.message.processors.forward;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MessageStoreServiceClient extends ServiceClient {
    private static final String EMPTY_MEDIA_TYPE = "";
    private static final String H_CONTENT_TYPE = "Content-Type";
    private static final String STR_JSON = "json";
    private static final String STR_JS = "javascript";
    private static final String SOAP_ACTION = "SOAPAction";
    private static final String MS_SC_LOG = "msmp.serviceclient";
    private static final Log log = LogFactory.getLog(MS_SC_LOG);

    public MessageStoreServiceClient(ConfigurationContext configContext, AxisService axisService)
            throws AxisFault {
        super(configContext, axisService);
    }

    /**
     * Directly invoke a named operation with a Robust In-Only MEP. This method just sends your
     * supplied XML and possibly receives a fault. For more control, you can instead create a client
     * for the operation and use that client to execute the send.
     *
     * @param payload         Payload to send
     * @param mc              message context
     * @param endpoint        Endpoint definition
     * @param synapseInMsgCtx Synapse Message context
     * @throws AxisFault
     */
    public void sendRobust(OMElement payload, MessageContext mc, EndpointDefinition endpoint,
                           org.apache.synapse.MessageContext synapseInMsgCtx) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Start:sendRobust");
            log.debug("Request:" + (payload == null ? "Null" : payload.toString()));
            log.debug("Endpoint-URL:" + endpoint.getAddress());
        }
        MessageContext messageContext = new MessageContext();
        setTransportHeaders(mc, messageContext);
        setProperties(mc, messageContext);
        fillSOAPEnvelope(messageContext, payload);
        setMessageType(mc, messageContext);
        if (mc.isDoingREST()) {
            messageContext.setDoingREST(true);
        }
        boolean isDoingREST = isDoingRest(endpoint);
        if (log.isDebugEnabled()) {
            log.debug("Is-REST:" + isDoingREST);
        }
        handleRESTfulInvocation(endpoint, synapseInMsgCtx, messageContext, getRESTPostfix(mc), isDoingREST);
        if (isDoingREST) {
            /* format=rest is kept only backward compatibility. We no longer needed that.*/
            /* Remove Message Type  for GET and DELETE Request */
            String method = getHTTPMethod(synapseInMsgCtx, mc);
            if (isGET(method) || isDELETE(method)) {
                messageContext.removeProperty(Constants.Configuration.MESSAGE_TYPE);
                messageContext.removeProperty(Constants.Configuration.CONTENT_TYPE);
            }
            messageContext.setProperty(Constants.Configuration.HTTP_METHOD, method);
            // TODO: this doesn't work. i.e. The outgoing message has the SOAPAction header.
            getOptions().setProperty(org.apache.axis2.Constants.Configuration.DISABLE_SOAP_ACTION, true);
            messageContext.setDoingREST(true);
        }
        OperationClient mepClient = createClient(ANON_ROBUST_OUT_ONLY_OP);
        mepClient.addMessageContext(messageContext);
        mepClient.execute(true);
        if (log.isDebugEnabled()) {
            log.debug("End:sendRobust");
        }
    }

    /**
     * Prepare a SOAP envelope with the stuff to be sent.
     *
     * @param messageContext the message context to be filled
     * @param payload        the payload content
     * @throws AxisFault if something goes wrong
     */
    private void fillSOAPEnvelope(MessageContext messageContext, OMElement payload)
            throws AxisFault {
        messageContext.setServiceContext(getServiceContext());
        SOAPFactory soapFactory = getSOAPFactory();
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        if (payload != null) {
            if (isDoingJSON(messageContext)) {
                Iterator children = payload.getChildElements();
                while (children.hasNext()) {
                    envelope.getBody().addChild((OMElement) children.next());
                }
            } else if (payload.getFirstElement() != null) {
                envelope.getBody().addChild(payload.getFirstElement());
            }
        }
        addHeadersToEnvelope(envelope);
        messageContext.setEnvelope(envelope);
    }

    /**
     * Return the SOAP factory to use depending on what options have been set. If the SOAP version
     * can not be seen in the options, version 1.1 is the default.
     *
     * @return the SOAP factory
     * @see Options#setSoapVersionURI(String)
     */
    private SOAPFactory getSOAPFactory() {
        String soapVersionURI = getOptions().getSoapVersionURI();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else {
            // make the SOAP 1.1 the default SOAP version
            return OMAbstractFactory.getSOAP11Factory();
        }
    }

    public MessageContext sendReceive(OMElement payload, MessageContext messageContext,
                                      EndpointDefinition endpoint,
                                      org.apache.synapse.MessageContext synapseInMsgCtx)
            throws AxisFault {
        return sendReceive(ANON_OUT_IN_OP, payload, messageContext, endpoint, synapseInMsgCtx);
    }

    /**
     * Directly invoke a named operationQName with an In-Out MEP. This method sends your supplied
     * XML and receives a response. For more control, you can instead create a client for the
     * operationQName and use that client to execute the exchange.
     * <p/>
     * Unless the <code>callTransportCleanup</code> property on the {@link Options} object has been
     * set to <code>true</code>, the caller must invoke {@link #cleanupTransport()} after
     * processing the response.
     *
     * @param operationQName name of operationQName to be invoked (non-<code>null</code>)
     * @param payload        the data to send (becomes the content of SOAP body)
     * @return response OMElement
     * @throws AxisFault in case of error
     * @see #cleanupTransport()
     */
    public MessageContext sendReceive(QName operationQName, OMElement payload, MessageContext mc,
                                      EndpointDefinition endpoint,
                                      org.apache.synapse.MessageContext synapseInMsgCtx)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Start:sendReceive");
            log.debug("Request:" + (payload == null ? "Null" : payload.toString()));
            log.debug("Endpoint-URL:" + endpoint.getAddress());
        }
        MessageContext messageContext = new MessageContext();
        setTransportHeaders(mc, messageContext);
        setProperties(mc, messageContext);
        fillSOAPEnvelope(messageContext, payload);
        setMessageType(mc, messageContext);
        if (mc.isDoingREST()) {
            messageContext.setDoingREST(true);
        }
        boolean isDoingREST = isDoingRest(endpoint);
        if (log.isDebugEnabled()) {
            log.debug("Is-REST:" + isDoingREST);
        }
        handleRESTfulInvocation(endpoint, synapseInMsgCtx, messageContext, getRESTPostfix(mc), isDoingREST);
        if (isDoingREST) {
            /* format=rest is kept only backward compatibility. We no longer needed that.*/
            /* Remove Message Type  for GET and DELETE Request */
            String method = getHTTPMethod(synapseInMsgCtx, mc);
            if (isGET(method) || isDELETE(method)) {
                messageContext.removeProperty(Constants.Configuration.MESSAGE_TYPE);
                messageContext.removeProperty(Constants.Configuration.CONTENT_TYPE);
            }
            messageContext.setProperty(Constants.Configuration.HTTP_METHOD, method);
            // TODO: this doesn't work. i.e. The outgoing message has the SOAPAction header.
            getOptions().setProperty(Constants.Configuration.DISABLE_SOAP_ACTION, true);
            messageContext.setDoingREST(true);
        }
        OperationClient operationClient = createClient(operationQName);
        operationClient.addMessageContext(messageContext);
        operationClient.execute(true);
        MessageContext response = operationClient
                .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        if (getOptions().isCallTransportCleanup()) {
            response.getEnvelope().buildWithAttachments();
            cleanupTransport();
        }
        if (log.isDebugEnabled()) {
            log.debug("Response:" + (response == null ? "Null" : response.getEnvelope().toString()));
            log.debug("End:sendReceive");
        }

        return response;
    }

    private String getRESTPostfix(MessageContext messageContext) {
        Object restPostfix = messageContext.getProperty(NhttpConstants.REST_URL_POSTFIX);
        if (restPostfix != null) {
            if (log.isDebugEnabled()) {
                log.debug("REST-URL-Postfix:" + restPostfix);
            }
            return (String) restPostfix;
        }
        return EMPTY_MEDIA_TYPE;
    }

    private boolean isDoingRest(EndpointDefinition endpoint) {
        return SynapseConstants.FORMAT_REST.equals(endpoint.getFormat());
    }

    private boolean isPOST(String method) {
        return Constants.Configuration.HTTP_METHOD_POST.equals(method);
    }

    private boolean isGET(String method) {
        return Constants.Configuration.HTTP_METHOD_GET.equals(method);
    }

    private boolean isDELETE(String method) {
        return Constants.Configuration.HTTP_METHOD_DELETE.equals(method);
    }

    private boolean isPUT(String method) {
        return Constants.Configuration.HTTP_METHOD_PUT.equals(method);
    }

    private String getHTTPMethod(org.apache.synapse.MessageContext synapsemc,
                                 MessageContext messageContext) {
        Object method = synapsemc.getProperty(RESTConstants.REST_METHOD);
        if (method instanceof String) {
            return (String) method;
        } else {
            method = messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
            if (method instanceof String) {
                if (log.isDebugEnabled()) {
                    log.debug("HTTP-Method:" + method);
                }
                return (String) method;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("HTTP-Method:" + Constants.Configuration.HTTP_METHOD_POST);
        }
        return Constants.Configuration.HTTP_METHOD_POST;
    }

    private void handleRESTfulInvocation(EndpointDefinition endpoint,
                                         org.apache.synapse.MessageContext syapseMessageIn,
                                         org.apache.axis2.context.MessageContext axis2Ctx,
                                         String restSuffix, boolean isDoingREST) {
        if (endpoint.getAddress() != null) {
            if (isDoingREST && restSuffix != null && !"".equals(restSuffix)) {
                String address = endpoint.getAddress(syapseMessageIn);
                String url;
                if (!address.endsWith("/") && !restSuffix.startsWith("/")
                    && !restSuffix.startsWith("?")) {
                    url = address + "/" + restSuffix;
                } else if (address.endsWith("/") && restSuffix.startsWith("/")) {
                    url = address + restSuffix.substring(1);
                } else if (address.endsWith("/") && restSuffix.startsWith("?")) {
                    url = address.substring(0, address.length() - 1)
                          + restSuffix;
                } else {
                    url = address + restSuffix;
                }
                axis2Ctx.setTo(new EndpointReference(url));
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint-URL:" + url);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint-URL:" + endpoint.getAddress(syapseMessageIn));
                }
                axis2Ctx.setTo(new EndpointReference(endpoint.getAddress(syapseMessageIn)));
            }
            axis2Ctx.setProperty(NhttpConstants.ENDPOINT_PREFIX, endpoint.getAddress(syapseMessageIn));
            if (log.isDebugEnabled()) {
                log.debug("Endpoint-Prefix:" + endpoint.getAddress(syapseMessageIn));
            }
        } else {
            // Supporting RESTful invocation
            if (isDoingREST && restSuffix != null && !"".equals(restSuffix)) {
                EndpointReference epr = axis2Ctx.getTo();
                if (epr != null) {
                    String address = epr.getAddress();
                    String url;
                    if (!address.endsWith("/") && !restSuffix.startsWith("/")
                        && !restSuffix.startsWith("?")) {
                        url = address + "/" + restSuffix;
                    } else {
                        url = address + restSuffix;
                    }
                    axis2Ctx.setTo(new EndpointReference(url));
                    if (log.isDebugEnabled()) {
                        log.debug("Endpoint-URL:" + url);
                    }
                }
            }
        }
    }

    public boolean isDoingJSON(MessageContext messageContext) {
        String ct = getContentType(messageContext);
        if (log.isDebugEnabled()) {
            log.debug("Is-Doing-JSON:" + (ct.contains(STR_JSON) || ct.contains(STR_JS)));
        }
        return ct.contains(STR_JSON) || ct.contains(STR_JS);
    }

    private String getContentType(MessageContext messageContext) {
        String contentType = EMPTY_MEDIA_TYPE;
        Object property = messageContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
        if (property == null) {
            property = messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        }
        if (property == null) {
            property = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (property instanceof Map) {
                Map<String, String> headers = (Map) property;
                if (headers.containsKey(H_CONTENT_TYPE)) {
                    contentType = headers.get(H_CONTENT_TYPE);
                } else if (headers.containsKey("Content-type")) {
                    contentType = headers.get("Content-type");
                } else if (headers.containsKey("content-type")) {
                    contentType = headers.get("content-type");
                } else if (headers.containsKey("content-Type")) {
                    contentType = headers.get("content-Type");
                }
            }
        }
        if (property instanceof String) {
            return (String) property;
        }
        if (log.isDebugEnabled()) {
            log.debug("Content-Type:" + contentType);
        }
        return contentType;
    }

    private String getTargetMessageType(MessageContext messageContext) {
        Object messageType = messageContext.getProperty(BlockingMessageSender.TARGET_MESSAGE_TYPE);
        if (messageType instanceof String) {
            if (log.isDebugEnabled()) {
                log.debug("Target-Message-Type:" + messageType);
            }
            return (String) messageType;
        }
        return EMPTY_MEDIA_TYPE;
    }

    private Map<String, String> setTransportHeaders(MessageContext source, MessageContext target) {
        Object headers = source.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (headers instanceof Map) {
            target.setProperty(MessageContext.TRANSPORT_HEADERS, headers);
            if (log.isDebugEnabled()) {
                log.debug("Transport-Headers:" + headers.toString());
            }
            return (Map) headers;
        }
        return Collections.emptyMap();
    }

    // TODO: What properties should we copy from source to target mc?
    private void setProperties(MessageContext source, MessageContext target) {
        Iterator<String> properties = source.getPropertyNames();
        while (properties.hasNext()) {
            String propertyName = properties.next();
            Object propertyValue = source.getProperty(propertyName);
            if (PropertyExceptions.isAllowed(propertyName)) {
                target.setProperty(propertyName, propertyValue);
                if (log.isDebugEnabled()) {
                    log.debug("Set:" + propertyName + "->" + propertyValue);
                }
            }
        }
    }

    private void setMessageType(MessageContext source, MessageContext target) {
        String messageType = !EMPTY_MEDIA_TYPE.equals(getTargetMessageType(source))
                             ? getTargetMessageType(source) : getContentType(source);
        target.setProperty(PropertyExceptions.MESSAGE_TYPE, messageType);
        target.setProperty(PropertyExceptions.CONTENT_TYPE, messageType);
        if (log.isDebugEnabled()) {
            log.debug("Set:messageType->" + messageType);
            log.debug("Set:ContentType->" + messageType);
        }
    }

    /**
     * These properties will be allowed to copy from incoming message context
     * to the target message context.
     */
    private static final class PropertyExceptions {
        public static final String JSON_STREAM = "JSON_STREAM";
        public static final String JSON_STRING = "JSON_STRING";
        public static final String JSON_OBJECT = "JSON_OBJECT";
        public static final String HTTP_METHOD = Constants.Configuration.HTTP_METHOD;
        public static final String MESSAGE_TYPE = Constants.Configuration.MESSAGE_TYPE;
        public static final String CONTENT_TYPE = Constants.Configuration.CONTENT_TYPE;
        public static final String TARGET_MESSAGE_TYPE = BlockingMessageSender.TARGET_MESSAGE_TYPE;
        private static final Set<String> properties = new HashSet<String>(14);

        static {
            // register properties that are allowed.
            // TODO: what other property names must be here? SOAPAction
            properties.add(MESSAGE_TYPE);
            properties.add(CONTENT_TYPE);
            properties.add(TARGET_MESSAGE_TYPE);
            properties.add(JSON_STREAM);
            properties.add(JSON_STRING);
            properties.add(JSON_OBJECT);
            properties.add(HTTP_METHOD);
        }

        /**
         * Returns if the given property is allowed or not to be copied from source message context
         * to the new message context that is used in sending message out from within the service
         * client.
         *
         * @param property value of the property
         * @return
         */
        public static boolean isAllowed(String property) {
            return properties.contains(property);
        }
    }
}
