/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sample.pizzadelivery.client;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import javax.xml.stream.XMLStreamException;

public class PizzaOrderClient {
    public static void main(String[] args) {
        String host = args[0];
        String port = args[1];
        String service = args[2];
        String topic = args[3];
        boolean batchedElements = Boolean.valueOf(args[4]);
        ServiceClient serviceClient = null;
        try {
            serviceClient = new ServiceClient();
            Options options = new Options();
            options.setTo(new EndpointReference("http://" + host + ":" + port + "/services/" + service + "/" + topic));
            serviceClient.setOptions(options);

            if (serviceClient != null) {
                String[] xmlElements = new String[]{
                        "<mypizza:PizzaOrderStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0023</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>PEPPERONI</mypizza:Type>\n"
                                + "              <mypizza:Size>L</mypizza:Size>\n"
                                + "              <mypizza:Quantity>2</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>James Mark</mypizza:Contact>\n"
                                + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "</mypizza:PizzaOrderStream>",
                        "<mypizza:PizzaOrderStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0024</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>CHEESE</mypizza:Type>\n"
                                + "              <mypizza:Size>M</mypizza:Size>\n"
                                + "              <mypizza:Quantity>1</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>Henry Clock</mypizza:Contact>\n"
                                + "              <mypizza:Address>2CYL Morris Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "</mypizza:PizzaOrderStream>",
                        "<mypizza:PizzaOrderStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0025</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>SEAFOOD</mypizza:Type>\n"
                                + "              <mypizza:Size>S</mypizza:Size>\n"
                                + "              <mypizza:Quantity>4</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>James Mark</mypizza:Contact>\n"
                                + "              <mypizza:Address>22RE Robinwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "</mypizza:PizzaOrderStream>",
                        "<mypizza:PizzaOrderStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0026</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>CHICKEN</mypizza:Type>\n"
                                + "              <mypizza:Size>L</mypizza:Size>\n"
                                + "              <mypizza:Contact>Alis Miranda</mypizza:Contact>\n"
                                + "              <mypizza:Address>779 Burl Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "</mypizza:PizzaOrderStream>",
                        "<mypizza:PizzaOrderStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0026</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>VEGGIE</mypizza:Type>\n"
                                + "              <mypizza:Size>L</mypizza:Size>\n"
                                + "              <mypizza:Quantity>1</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>James Mark</mypizza:Contact>\n"
                                + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "</mypizza:PizzaOrderStream>"
                };

                String[] batchedXmlElements = new String[]{
                        "<mypizza:PizzaOrderStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0023</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>PEPPERONI</mypizza:Type>\n"
                                + "              <mypizza:Size>L</mypizza:Size>\n"
                                + "              <mypizza:Quantity>2</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>James Mark</mypizza:Contact>\n"
                                + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0024</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>CHEESE</mypizza:Type>\n"
                                + "              <mypizza:Size>M</mypizza:Size>\n"
                                + "              <mypizza:Quantity>1</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>Henry Clock</mypizza:Contact>\n"
                                + "              <mypizza:Address>2CYL Morris Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0025</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>SEAFOOD</mypizza:Type>\n"
                                + "              <mypizza:Size>S</mypizza:Size>\n"
                                + "              <mypizza:Quantity>4</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>James Mark</mypizza:Contact>\n"
                                + "              <mypizza:Address>22RE Robinwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "</mypizza:PizzaOrderStream>",
                        "<mypizza:PizzaOrderStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0026</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>CHICKEN</mypizza:Type>\n"
                                + "              <mypizza:Size>L</mypizza:Size>\n"
                                + "              <mypizza:Quantity>1</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>Alis Miranda</mypizza:Contact>\n"
                                + "              <mypizza:Address>779 Burl Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "        <mypizza:PizzaOrder>\n"
                                + "              <mypizza:OrderNo>0026</mypizza:OrderNo>\n"
                                + "              <mypizza:Type>VEGGIE</mypizza:Type>\n"
                                + "              <mypizza:Size>L</mypizza:Size>\n"
                                + "              <mypizza:Quantity>1</mypizza:Quantity>\n"
                                + "              <mypizza:Contact>James Mark</mypizza:Contact>\n"
                                + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                + "        </mypizza:PizzaOrder>\n"
                                + "</mypizza:PizzaOrderStream>"
                };

                try {
                    if (batchedElements) {
                        for (String xmlElement : batchedXmlElements) {
                            serviceClient.fireAndForget(AXIOMUtil.stringToOM(xmlElement));
                        }
                    } else {
                        for (String xmlElement : xmlElements) {
                            serviceClient.fireAndForget(AXIOMUtil.stringToOM(xmlElement));
                        }
                    }
                    Thread.sleep(500); // We need to wait some time for the message to be sent
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (AxisFault axisFault) {
                    axisFault.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
