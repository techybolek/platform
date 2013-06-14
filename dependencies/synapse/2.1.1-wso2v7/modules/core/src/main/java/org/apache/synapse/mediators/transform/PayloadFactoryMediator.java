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
import org.apache.synapse.MessageContext;
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

    private String format;
    private Value formatKey = null;
    private boolean isFormatDynamic = false;
    private String format_raw;

    private String json_stream;

    private String type;

    private final String JSON_CONTENT="application/json";
    private final String JSON_TYPE="json";
    private final String JSON_STREAM="JSON_STREAM";

    private boolean isStream=false;

   // private List<Argument> argumentList = new ArrayList<Argument>();

    private List<Argument> xPathArgumentList = new ArrayList<Argument>();

    private List<Argument> jsonPathArgumentList = new ArrayList<Argument>();

    private Pattern pattern = Pattern.compile("\\$(\\d)+");

    private String contentType=null;

    private org.apache.axis2.context.MessageContext axis2MessageContext=null;

    public boolean mediate(MessageContext synCtx){

        format_raw=format;  //because the format will be altered when parsing to json.
                            // This is to preserve the original format.
        axis2MessageContext = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        contentType = axis2MessageContext.getProperty(Constants.Configuration.CONTENT_TYPE).toString();
        Builder messageBuilder=null;
        try {
            messageBuilder= MessageProcessorSelector.getMessageBuilder(JSON_CONTENT, axis2MessageContext);
            axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,contentType);

        } catch (AxisFault axisFault) {
            handleException("Unable to get the message builder", synCtx);
            //To change body of catch statement use File | Settings | File Templates.
        }

        if(messageBuilder instanceof org.apache.axis2.json.JSONStreamBuilder){
            isStream=true;

            Axis2MessageContext axis2=(Axis2MessageContext)synCtx;

            if(contentType.contains(JSON_TYPE)){
                InputStream jsonStream= (InputStream)axis2.getAxis2MessageContext().getProperty(JSON_STREAM);
                // InputStreamReader jsonStreamReader=new InputStreamReader(jsonStream);

                StringBuilder jsonStringBuilder=new StringBuilder();

                byte[] buf=new byte[4096];
                int count;

                try {
                    while ((count=jsonStream.read(buf))>0){
                        jsonStringBuilder.append(new String(buf));
                    }
                } catch (IOException e) {
                    //No json stream
                    return false;  //To change body of catch statement use File | Settings | File Templates.
                }

                json_stream=jsonStringBuilder.toString();
            }
        }

        //depending on the media-Type, the PF Mediator mediate differently.
        if(type!=null){
            //mediate according to the media-type
            if(type.contains(JSON_TYPE)){
                if(isStream){
                    return mediateJsonStream(synCtx);
                } else{
                    mediateJson();
                }
                //return mediateXml(synCtx);
            }
        }
        //if no media-type is defined or unknown media type is defined,
        // use XML as default
        return mediateXml(synCtx);
    }

    private boolean mediateXml(MessageContext synCtx){

        SOAPBody soapBody = synCtx.getEnvelope().getBody();

        StringBuffer result = new StringBuffer();


        regexTransform(result, synCtx);


        OMElement resultElement;
        try {
            resultElement = AXIOMUtil.stringToOM(result.toString());
        } catch (XMLStreamException e) {      /*Use the XMLStreamException and log the proper stack trace*/
            handleException("Unable to create a valid XML payload", synCtx);
            return false;
        }

        for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }

        QName resultQName = resultElement.getFirstElement().getQName();


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
        format=format_raw;
        return true;
    }

    private boolean mediateJsonStream(MessageContext synCtx){

        //Axis2MessageContext axis2=(Axis2MessageContext)synCtx;

        StringBuffer stringBuffer=new StringBuffer();
        regexTransform(stringBuffer,synCtx);

        InputStream formatedJsonStream=new ByteArrayInputStream(stringBuffer.toString().getBytes());

        //axis2.getAxis2MessageContext().setProperty("JSON_STREAM", formatedJsonStream);
        axis2MessageContext.setProperty(JSON_STREAM, formatedJsonStream);

        return true;
    }

    private void mediateJson(){

        /*OMElement jasonOmElement=null;
        try {
            jasonOmElement=AXIOMUtil.stringToOM(format);
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        StringBuilder stringBuilder=new StringBuilder(jasonOmElement.getText());*/

        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("{ \"XMLPayload\" : ");
        stringBuilder.append(format);
        stringBuilder.append("}");


        try {
            OMElement omElement = parseJsonToXml(stringBuilder);
            format=omElement.toString();
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (NullPointerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

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
    private void regexTransform(StringBuffer result, MessageContext synCtx) {
        if (isFormatDynamic()) {
            String key = formatKey.evaluateValue(synCtx);

            OMElement element = (OMElement) synCtx.getEntry(key);
            removeIndentations(element);
            String format2 = element.toString();
            replace(format2, result, synCtx);
        } else {
            replace(format, result, synCtx);

        }
    }

    /**
     * repplace the message with format
     *
     * @param format
     * @param result
     * @param synCtx
     */
    private void replace(String format, StringBuffer result, MessageContext synCtx) {
        Object[] argValues;
        if(contentType.contains(JSON_CONTENT) && isStream){
            argValues = getJsonArgValues(synCtx);
        } else {
            argValues = getArgValues(synCtx);

        }

        Matcher matcher;

        if(type!=null && type.contains(JSON_TYPE)){
            matcher = pattern.matcher(format);

        }    else{
            matcher = pattern.matcher("<dummy>" + format + "</dummy>");
        }

        try {
            while (matcher.find()) {
                String matchSeq = matcher.group();
                int argIndex = Integer.parseInt(matchSeq.substring(1));
                matcher.appendReplacement(result, argValues[argIndex - 1].toString());
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
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

    private Object[] getJsonArgValues(MessageContext synCtx) {



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
                //String value = arg.getExpression().toString();   /*ToDo We can change this to string array*/

                /*StringTokenizer tokenizer=new StringTokenizer(value,"/");
                int tokens=tokenizer.countTokens();
                JSONObject jsonObject=null;
                JSONObject info=null;

                try {
                    jsonObject=new JSONObject(json_stream);
                    for (int j=1;j<tokens;j++){
                        if(info==null){  //can be done with j==1?
                            info = jsonObject.getJSONObject(tokenizer.nextToken());
                        }else{
                            info=info.getJSONObject(tokenizer.nextToken());
                        }
                    }
                    if(info==null){
                        value=jsonObject.get(tokenizer.nextToken()).toString();
                    }else{
                        value=info.get(tokenizer.nextToken()).toString();
                    }
                } catch (JSONException e) {
                    //Not a Json.
                    System.out.println("?????????????????? NOT A JSON ???????????????");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }*/

                String value=arg.getJsonPath().stringValueOf(json_stream);





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
        if(format_raw!=null){
            return format_raw;
        }else{
            return format;
        }
    }

    public void setFormat(String format) {
        this.format = format;
    }

  /*  public void addArgument(Argument arg) {
        argumentList.add(arg);
    }

    public List<Argument> getArgumentList() {
        return argumentList;
    }*/

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
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
