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

public class PizzaDeliveryClient {
    public static void main(String[] args) {
        String host = args[0];
        String port = args[1];
        String service = args[2];
        String topic = args[3];
        ServiceClient serviceClient = null;
        try {
            serviceClient = new ServiceClient();
            Options options = new Options();
            options.setTo(new EndpointReference("http://" + host + ":" + port + "/services/" + service + "/" + topic));
            serviceClient.setOptions(options);

            if (serviceClient != null) {
                String[] xmlElements = new String[]{"<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0023</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Card</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>",
                                                    "<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0024</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Card</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>2CYL Morris Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>",
                                                    "<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0025</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Cash</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>22RE Robinwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>",
                                                    "<mypizza:PizzaDeliveryStream xmlns:mypizza=\"http://samples.wso2.org/\">\n"
                                                    + "        <mypizza:PizzaDelivery>\n"
                                                    + "              <mypizza:OrderNo>0026</mypizza:OrderNo>\n"
                                                    + "              <mypizza:PaymentType>Card</mypizza:PaymentType>\n"
                                                    + "              <mypizza:Address>29BX Finchwood Ave, Clovis, CA 93611</mypizza:Address>\n"
                                                    + "        </mypizza:PizzaDelivery>\n"
                                                    + "</mypizza:PizzaDeliveryStream>"
                };

                try {
                    for (int i = 0, xmlElementsLength = xmlElements.length; i < xmlElementsLength; i++) {
                        serviceClient.fireAndForget(AXIOMUtil.stringToOM(xmlElements[i]));

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
