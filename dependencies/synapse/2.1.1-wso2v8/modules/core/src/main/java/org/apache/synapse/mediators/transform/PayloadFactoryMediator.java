/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.mediators.transform;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.AXIOMUtils;
import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayloadFactoryMediator extends AbstractMediator {
    private Value formatKey = null;
    private boolean isFormatDynamic = false;
    private String formatRaw;
    private String mediaType;
    private final static String JSON_CONTENT = "application/json";
    private final static String JSON_TYPE = "json";
    private final static String JSON_STREAM = "JSON_STREAM";

    private List<Argument> xPathArgumentList = new ArrayList<Argument>();
    private List<Argument> jsonPathArgumentList = new ArrayList<Argument>();
    private Pattern pattern = Pattern.compile("\\$(\\d)+");

    private static final Log log = LogFactory.getLog(PayloadFactoryMediator.class);


    public boolean mediate(MessageContext synCtx) {

        String contentType = "";
        String format = formatRaw;  // This is to preserve the original format,because the format will be altered when parsing to json.

        Axis2MessageContext axis2SynapseCtx = (Axis2MessageContext) synCtx;
        org.apache.axis2.context.MessageContext axis2MessageContext = axis2SynapseCtx.getAxis2MessageContext();

        if (axis2MessageContext.getProperty(Constants.Configuration.MESSAGE_TYPE) != null) {
            contentType = axis2MessageContext.getProperty(Constants.Configuration.MESSAGE_TYPE).toString();
        }

        String jsonStreamStr = null;
        boolean isStreamBuilder = false;
        if (contentType.equals(JSON_CONTENT) || (mediaType != null && mediaType.equals(JSON_TYPE))) {
            try {
                isStreamBuilder = isStreamBuilder(axis2MessageContext, contentType);
            }catch (SynapseException synapseExp) {
                handleException("JSON Stream not found", synCtx);
            }
        }


        if (contentType.contains(JSON_CONTENT) && isStreamBuilder) {
            InputStream jsonStream = (InputStream) axis2SynapseCtx.getAxis2MessageContext().getProperty(JSON_STREAM);
            StringBuilder jsonStringBuilder = new StringBuilder();
            byte[] buf = new byte[4096];
            int count;
            try {
                while ((count = jsonStream.read(buf)) > 0) {
                    jsonStringBuilder.append(new String(buf));
                }
            } catch (IOException e) {
                handleException("Cannot read JSON Stream", synCtx);
            }
            jsonStreamStr = jsonStringBuilder.toString();
        }

        //depending on the media-Type, the PF Mediator mediate differently.
        if (mediaType != null && mediaType.equals(JSON_TYPE)) {
            //mediate according to the media-type
            if (isStreamBuilder) {
                if (log.isDebugEnabled()) {
                    log.debug("Using Stream : Content-Type= " + contentType);
                }
                return mediateJsonStream(synCtx, contentType, jsonStreamStr, format, isStreamBuilder);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Using OM builder : Content-Type= " + contentType);
                }
                format = mediateJson(synCtx, format);
            }
        }
        //if no media-type is defined or unknown media type is defined,
        // use XML as default
        return mediateXml(synCtx, contentType, jsonStreamStr, format, isStreamBuilder);
    }

    private boolean isStreamBuilder(org.apache.axis2.context.MessageContext axis2MessageCtx, String contentType) {
        Builder messageBuilder = null;
        try {
            messageBuilder = MessageProcessorSelector.getMessageBuilder(JSON_CONTENT, axis2MessageCtx);
            axis2MessageCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
        } catch (AxisFault axisFault) {
            throw new SynapseException("Unable to get the message builder", axisFault);
        }
        if (messageBuilder instanceof org.apache.axis2.json.JSONStreamBuilder) {
            return true;
        }
        return false;
    }

    private boolean mediateXml(MessageContext synCtx, String contentType, String jsonStream, String format, Boolean isStreamBuilder) {
        SOAPBody soapBody = synCtx.getEnvelope().getBody();
        StringBuffer result = new StringBuffer();
        regexTransform(result, synCtx, contentType , jsonStream, format, isStreamBuilder);

        OMElement resultElement = null;
        try {
            resultElement = AXIOMUtil.stringToOM(result.toString());
        } catch (XMLStreamException e) {      /*Use the XMLStreamException and log the proper stack trace*/
            handleException("Unable to create a valid XML payload", synCtx);
        }

        for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }

        if (resultElement != null) {
            OMElement firstChild = resultElement.getFirstElement();
            if (firstChild != null) {
                QName resultQName = firstChild.getQName();
                if (resultQName.getLocalPart().equals("Envelope") && (
                        resultQName.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) ||
                                resultQName.getNamespaceURI().
                                        equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))) {
                    SOAPEnvelope soapEnvelope = AXIOMUtils.getSOAPEnvFromOM(resultElement.getFirstElement());
                    if (soapEnvelope != null) {
                        try {
                            synCtx.setEnvelope(soapEnvelope);
                        } catch (AxisFault axisFault) {
                            handleException("Unable to attach SOAPEnvelope", axisFault, synCtx);
                        }
                    }
                } else {
                    for (Iterator itr = resultElement.getChildElements(); itr.hasNext(); ) {
                        OMElement child = (OMElement) itr.next();
                        soapBody.addChild(child);
                    }
                }
            }
        }

        return true;
    }

    private boolean mediateJsonStream(MessageContext synCtx, String contentType, String jsonStream, String format, Boolean isStreamBuilder) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        //Axis2MessageContext axis2=(Axis2MessageContext)synCtx;

        StringBuffer stringBuffer = new StringBuffer();
        regexTransform(stringBuffer, synCtx, contentType, jsonStream, format, isStreamBuilder);

        InputStream formatedJsonStream = new ByteArrayInputStream(stringBuffer.toString().getBytes());
        axis2MessageContext.setProperty(JSON_STREAM, formatedJsonStream);

        return true;
    }

    private String mediateJson(MessageContext synCtx, String format) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ \"XMLPayload\" : ");
        stringBuilder.append(format);
        stringBuilder.append("}");

        try {
            /*ToDo : Use JSONBuilder utils*/
            OMElement omElement = parseJsonToXml(stringBuilder);
            return omElement.toString();
        } catch (Exception e) {
            handleException("Error occured while mediating json", e, synCtx);
        }
        return null;
    }

    private OMElement parseJsonToXml(StringBuilder sb) throws JSONException, XMLStreamException, IOException {
        StringWriter sw = new StringWriter(5120);
        JSONObject jsonObject = new JSONObject(sb.toString());
        AbstractXMLStreamReader reader = new MappedXMLStreamReader(jsonObject);

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(sw);

        xmlStreamWriter.writeStartDocument();
        while (reader.hasNext()) {
            int x = reader.next();
            switch (x) {
                case XMLStreamConstants.START_ELEMENT:
                    xmlStreamWriter.writeStartElement(reader.getPrefix(), reader.getLocalName(),
                            reader.getNamespaceURI());
                    int namespaceCount = reader.getNamespaceCount();
                    for (int i = namespaceCount - 1; i >= 0; i--) {
                        xmlStreamWriter.writeNamespace(reader.getNamespacePrefix(i),
                                reader.getNamespaceURI(i));
                    }
                    int attributeCount = reader.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        xmlStreamWriter.writeAttribute(reader.getAttributePrefix(i),
                                reader.getAttributeNamespace(i),
                                reader.getAttributeLocalName(i),
                                reader.getAttributeValue(i));
                    }
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.CHARACTERS:
                    xmlStreamWriter.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.CDATA:
                    xmlStreamWriter.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    xmlStreamWriter.writeEndElement();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    xmlStreamWriter.writeEndDocument();
                    break;
                case XMLStreamConstants.SPACE:
                    break;
                case XMLStreamConstants.COMMENT:
                    xmlStreamWriter.writeComment(reader.getText());
                    break;
                case XMLStreamConstants.DTD:
                    xmlStreamWriter.writeDTD(reader.getText());
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    xmlStreamWriter
                            .writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    xmlStreamWriter.writeEntityRef(reader.getLocalName());
                    break;
                default:
                    throw new RuntimeException("Error in converting JSON to XML");
            }
        }
        xmlStreamWriter.writeEndDocument();
        xmlStreamWriter.flush();
        xmlStreamWriter.close();

        OMElement element = AXIOMUtil.stringToOM(sw.toString());

        /*OMNamespace ns = OMAbstractFactory.getOMFactory().createOMNamespace(
                JsonRoot.getNamespaceURI(), JsonRoot.getPrefix());
        element.setNamespace(ns);  */
        return element;
    }


    /* ToDO : Return string buffer*/
    private void regexTransform(StringBuffer result, MessageContext synCtx, String contentType, String stream, String format, Boolean isStreamBuilder) {
        if (isFormatDynamic()) {
            String key = formatKey.evaluateValue(synCtx);
            OMElement element = (OMElement) synCtx.getEntry(key);
            removeIndentations(element);
            String format2 = element.toString();
            replace(format2, result, synCtx, contentType, stream, isStreamBuilder);
        } else {
            replace(format, result, synCtx, contentType, stream, isStreamBuilder);
        }
    }

    /**
     * repplace the message with format
     *
     * @param format
     * @param result
     * @param synCtx
     */
    private void replace(String format, StringBuffer result, MessageContext synCtx, String contentType, String stream, Boolean isStreamBuilder) {
        Object[] argValues;
        if (contentType != null && contentType.equals(JSON_CONTENT) && isStreamBuilder!= null && isStreamBuilder) {
            argValues = getJsonArgValues(synCtx, stream);
        } else {
            argValues = getArgValues(synCtx);

        }
        Matcher matcher;

        if (mediaType != null && mediaType.equals(JSON_TYPE)) {
            matcher = pattern.matcher(format);
        } else {
            matcher = pattern.matcher("<dummy>" + format + "</dummy>");
        }

        try {
            while (matcher.find()) {
                String matchSeq = matcher.group();
                int argIndex = Integer.parseInt(matchSeq.substring(1));
                matcher.appendReplacement(result, argValues[argIndex - 1].toString());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Mis-match detected between number of formatters and arguments", e);
        }
        matcher.appendTail(result);
    }

    private void removeIndentations(OMElement element) {
        List<OMText> removables = new ArrayList<OMText>();
        removeIndentations(element, removables);
        for (OMText node : removables) {
            node.detach();
        }
    }

    private void removeIndentations(OMElement element, List<OMText> removables) {
        Iterator children = element.getChildren();
        while (children.hasNext()) {
            Object next = children.next();
            if (next instanceof OMText) {
                OMText text = (OMText) next;
                if (text.getText().trim().equals("")) {
                    removables.add(text);
                }
            } else if (next instanceof OMElement) {
                removeIndentations((OMElement) next, removables);
            }
        }
    }

    private Object[] getArgValues(MessageContext synCtx) {

        Object[] argValues = new Object[xPathArgumentList.size()];
        for (int i = 0; i < xPathArgumentList.size(); ++i) {       /*ToDo use foreach*/
            Argument arg = xPathArgumentList.get(i);
            if (arg.getValue() != null) {
                String value = arg.getValue();
                if (!isXML(value)) {
                    value = StringEscapeUtils.escapeXml(value);
                }
                value = Matcher.quoteReplacement(value);
                argValues[i] = value;
            } else if (arg.getExpression() != null) {
                String value = arg.getExpression().stringValueOf(synCtx);   /*ToDo We can change this to string array*/
                if (value != null) {
                    //escaping string unless there might be exceptions when tries to insert values
                    // such as string with & (XML special char) and $ (regex special char)
                    if (!isXML(value)) {
                        value = StringEscapeUtils.escapeXml(value);
                    }
                    value = Matcher.quoteReplacement(value);
                    argValues[i] = value;
                } else {
                    argValues[i] = "";
                }
            } else {
                handleException("Unexpected arg type detected", synCtx);
            }
        }
        return argValues;
    }

    private Object[] getJsonArgValues(MessageContext synCtx, String stream) {


        Object[] argValues = new Object[jsonPathArgumentList.size()];
        for (int i = 0; i < jsonPathArgumentList.size(); ++i) {       /*ToDo use foreach*/
            Argument arg = jsonPathArgumentList.get(i);
            if (arg.getValue() != null) {
                String value = arg.getValue();
                if (!isXML(value)) {
                    value = StringEscapeUtils.escapeXml(value);
                }
                value = Matcher.quoteReplacement(value);
                argValues[i] = value;
            } else if (arg.getJsonPath() != null) {
                String value = arg.getJsonPath().stringValueOf(stream);
                if (value != null) {
                    //escaping string unless there might be exceptions when tries to insert values
                    // such as string with & (XML special char) and $ (regex special char)
                    /*if (!isXML(value)) {
                        value = StringEscapeUtils.escapeXml(value);
                    }   */
                    value = Matcher.quoteReplacement(value);
                    argValues[i] = value;
                } else {
                    argValues[i] = "";
                }
            } else {
                handleException("Unexpected arg type detected", synCtx);
            }
        }

        return argValues;
    }

    public String getFormat() {
        return formatRaw;
    }

    public void setFormat(String format) {
        this.formatRaw = format;
    }

    public void addXPathArgument(Argument arg) {
        xPathArgumentList.add(arg);
    }

    public List<Argument> getXPathArgumentList() {
        return xPathArgumentList;
    }

    public void addJsonPathArgument(Argument arg) {
        jsonPathArgumentList.add(arg);
    }

    public List<Argument> getJsonPathArgumentList() {
        return jsonPathArgumentList;
    }

    private boolean isXML(String value) {
        try {
            AXIOMUtil.stringToOM(value);
        } catch (XMLStreamException ignore) {
            // means not a xml
            return false;
        } catch (OMException ignore) {
            // means not a xml
            return false;
        }
        return true;
    }

    public String getType() {
        return mediaType;
    }

    public void setType(String type) {
        this.mediaType = type;
    }

    /**
     * To get the key which is used to pick the format definition from the local registry
     *
     * @return return the key which is used to pick the format definition from the local registry
     */
    public Value getFormatKey() {
        return formatKey;
    }

    /**
     * To set the local registry key in order to pick the format definition
     *
     * @param key the local registry key
     */
    public void setFormatKey(Value key) {
        this.formatKey = key;
    }

    public void setFormatDynamic(boolean formatDynamic) {
        this.isFormatDynamic = formatDynamic;
    }

    public boolean isFormatDynamic() {
        return isFormatDynamic;
    }


}
