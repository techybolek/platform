/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLEventBuilderFactory;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLInputEventBuilder;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLInputMapping;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathData;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import java.util.ArrayList;
import java.util.List;

public class XMLMappingTestCase {
    private static final String XPATH_NS = "http://ws.cdyne.com/";
    private static final String XPATH_PREFIX = "quotedata";
    private static final String XPATH_FOR_SYMBOL = "//quotedata:StockQuoteEvent/quotedata:StockSymbol";
    private static final String XPATH_FOR_PRICE = "//quotedata:StockQuoteEvent/quotedata:LastTradeAmount";
    private XMLInputEventBuilder xmlInputEventBuilder;
    private EventBuilderConfiguration xmlEventBuilderConfig;

    @Before
    public void init() {
        xmlEventBuilderConfig = new EventBuilderConfiguration(new XMLEventBuilderFactory());
        xmlEventBuilderConfig.setToStreamName("stockQuotes");
        xmlEventBuilderConfig.setToStreamVersion("1.0.0");
        XMLInputMapping xmlInputMapping = new XMLInputMapping();
        xmlInputMapping.setXpathDefinition(new XPathDefinition(XPATH_PREFIX, XPATH_NS));
        xmlInputMapping.addInputMappingAttribute(new InputMappingAttribute(XPATH_FOR_SYMBOL, "symbol", AttributeType.STRING));
        xmlInputMapping.addInputMappingAttribute(new InputMappingAttribute(XPATH_FOR_PRICE, "price", AttributeType.DOUBLE));
        xmlEventBuilderConfig.setInputMapping(xmlInputMapping);
        xmlInputEventBuilder = new XMLInputEventBuilder(xmlEventBuilderConfig, null, false);
    }

    @Test
    public void testCreateMapping() throws MalformedStreamDefinitionException, JaxenException {
        StreamDefinition exportedStreamDefinition = new StreamDefinition("stockQuotes", "1.0.0");
        List<XPathData> attributeXpathList = new ArrayList<XPathData>();
        xmlInputEventBuilder.createMapping(xmlEventBuilderConfig, exportedStreamDefinition, attributeXpathList);
        List<XPathData> expectedAttributeList = new ArrayList<XPathData>();
        // Set expected xpath expressions
        AXIOMXPath xpathForSymbol = new AXIOMXPath(XPATH_FOR_SYMBOL);
        AXIOMXPath xpathForPrice = new AXIOMXPath(XPATH_FOR_PRICE);
        XPathDefinition xPathDefinition = ((XMLInputMapping) xmlEventBuilderConfig.getInputMapping()).getXpathDefinition();
        xpathForSymbol.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
        xpathForPrice.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
        expectedAttributeList.add(new XPathData(xpathForSymbol, EventBuilderConstants.CLASS_FOR_STRING));
        expectedAttributeList.add(new XPathData(xpathForPrice, EventBuilderConstants.CLASS_FOR_DOUBLE));

        int exprCount = 0;
        for (XPathData expectedXpathData : expectedAttributeList) {
            // Double iteration since AXIOMXPath equals() and hashCode() does not seem to be overloaded properly.
            // i.e. For same namespace and xpath expr, equals does not return equals true.
            // So individual attributes need to be checked manually.
            for (XPathData xPathData : attributeXpathList) {
                if (expectedXpathData.getXpath().toString().equals(xPathData.getXpath().toString())) {
                    Assert.assertEquals(expectedXpathData.getType(), xPathData.getType());
                    Assert.assertEquals(expectedXpathData.getXpath().getNamespaces(), xPathData.getXpath().getNamespaces());
                    exprCount++;
                    break;
                }
            }
        }
        Assert.assertEquals(attributeXpathList.size(), exprCount);

        StreamDefinition expectedStreamDefinition = new StreamDefinition("stockQuotes", "1.0.0");
        List<Attribute> expectedPayloadAttributes = new ArrayList<Attribute>();
        expectedPayloadAttributes.add(new Attribute("symbol", AttributeType.STRING));
        expectedPayloadAttributes.add(new Attribute("price", AttributeType.DOUBLE));
        expectedStreamDefinition.setPayloadData(expectedPayloadAttributes);
        for (Attribute attribute : exportedStreamDefinition.getPayloadData()) {
            Assert.assertTrue(expectedPayloadAttributes.contains(attribute));
        }
        Assert.assertEquals(expectedStreamDefinition, exportedStreamDefinition);
    }

}
