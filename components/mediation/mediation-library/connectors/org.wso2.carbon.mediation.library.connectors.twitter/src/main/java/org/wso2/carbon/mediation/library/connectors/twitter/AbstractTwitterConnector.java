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

package org.wso2.carbon.mediation.library.connectors.twitter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;

public abstract class AbstractTwitterConnector extends AbstractConnector  {

	 protected Log log = LogFactory.getLog(this.getClass());
		
	/**
	 * Util method which converts the the json to xml string format
	 * 
	 * @param sb
	 * @return
	 * @throws JSONException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public OMElement parseJsonToXml(String sb) throws JSONException, XMLStreamException, IOException {
		StringWriter sw = new StringWriter(5120);
		org.codehaus.jettison.json.JSONObject jsonObject = new org.codehaus.jettison.json.JSONObject(sb);
		AbstractXMLStreamReader reader = new MappedXMLStreamReader(jsonObject);

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(sw);

		xmlStreamWriter.writeStartDocument();
		while (reader.hasNext()) {
			int x = reader.next();
			switch (x) {
				case XMLStreamConstants.START_ELEMENT:
					xmlStreamWriter.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
					int namespaceCount = reader.getNamespaceCount();
					for (int i = namespaceCount - 1; i >= 0; i--) {
						xmlStreamWriter.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
					}
					int attributeCount = reader.getAttributeCount();
					for (int i = 0; i < attributeCount; i++) {
						xmlStreamWriter.writeAttribute(reader.getAttributePrefix(i), reader.getAttributeNamespace(i),
						                               reader.getAttributeLocalName(i), reader.getAttributeValue(i));
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
					xmlStreamWriter.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
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

		/*
		 * OMNamespace ns = OMAbstractFactory.getOMFactory().createOMNamespace(
		 * JsonRoot.getNamespaceURI(), JsonRoot.getPrefix());
		 * element.setNamespace(ns);
		 */
		return element;
	}
	
	protected void preparePayload(MessageContext messageContext, OMElement element) {
	    SOAPBody soapBody = messageContext.getEnvelope().getBody();
	    for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
	    	OMElement child = (OMElement) itr.next();
	    	child.detach();
	    }
	    for (Iterator itr = element.getChildElements(); itr.hasNext();) {
	    	OMElement child = (OMElement) itr.next();
	    	soapBody.addChild(child);
	    }
    }
	

}
