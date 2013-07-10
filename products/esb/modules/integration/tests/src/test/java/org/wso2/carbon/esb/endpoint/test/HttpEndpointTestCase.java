/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.esb.endpoint.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.ESBTestConstant;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class HttpEndpointTestCase extends ESBIntegrationTest {

    private final String ENDPOINT_NAME = "HTTPEndpointTest";
    private EndPointAdminClient endPointAdminClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB" + File.separator + "endpoint" + File.separator + "httpEndpointConfig" + File.separator + "synapse.xml");

        endPointAdminClient = new EndPointAdminClient(esbServer.getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
    }

    @AfterClass(groups = "wso2.esb")
    public void close() throws Exception {
        //resourceAdminServiceClient.deleteResource("/_system/config/test_ep_config");
        endPointAdminClient = null;
        super.cleanup();
    }

    @Test(groups = {"wso2.esb"}, description = "Test http endpoint addition, deletion & stats")
    public void testHttpEndpoint() throws Exception {
        cleanupEndpoints();
        endpointAdditionScenario();
        endpointStatisticsScenario();
        endpointDeletionScenario();
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message to HTTP endpoint")
    public void testSendingToHttpEndpoint()
            throws IOException, EndpointAdminEndpointAdminException,
            LoginAuthenticationExceptionException,
            XMLStreamException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("httpEndPoint")
                , getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message to a Invalid Http endpoint")
    public void testSendingToInvalidHttpEndpoint()
            throws IOException, EndpointAdminEndpointAdminException,
            LoginAuthenticationExceptionException,
            XMLStreamException {
        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("invalidHttpEndPoint"),
                    getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof org.apache.axis2.AxisFault);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message to a HTTP endpoint with missing uri.var variable")
    public void testSendingToNoVarHttpEndpoint()
            throws XMLStreamException, FileNotFoundException, AxisFault {
        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("missingVariableEndPoint"),
                    getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof org.apache.axis2.AxisFault);
        }
    }

    private void cleanupEndpoints()
            throws RemoteException, EndpointAdminEndpointAdminException {
        String[] endpointNames = endPointAdminClient.getEndpointNames();
        List endpointList;
        if (endpointNames != null && endpointNames.length > 0 && endpointNames[0] != null) {
            endpointList = Arrays.asList(endpointNames);
            if (endpointList.contains(ENDPOINT_NAME)) {
                endPointAdminClient.deleteEndpoint(ENDPOINT_NAME);
            }
        }
    }

    private void endpointAdditionScenario()
            throws Exception {
        int beforeCount = endPointAdminClient.getEndpointCount();

        addEndpoint(AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" + ENDPOINT_NAME + "\">\n" +
                "<http method=\"POST\"\n" +
                "              uri-template=\"http://localhost:9000/services/SimpleStockQuoteService\"/>" +
                "</endpoint>"));
        int afterCount = endPointAdminClient.getEndpointCount();
        assertEquals(1, afterCount - beforeCount);

        String[] endpoints = endPointAdminClient.getEndpointNames();
        if (endpoints != null && endpoints.length > 0 && endpoints[0] != null) {
            List endpointList = Arrays.asList(endpoints);
            assertTrue(endpointList.contains(ENDPOINT_NAME));
        } else {
            fail("Endpoint has not been added to the system properly");
        }
    }

    private void endpointStatisticsScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        endPointAdminClient.enableEndpointStatistics(ENDPOINT_NAME);
        String endpoint = endPointAdminClient.getEndpointConfiguration(ENDPOINT_NAME);
        assertTrue(endpoint.contains("statistics=\"enable"));
    }

    private void endpointDeletionScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endPointAdminClient.getEndpointCount();
        endPointAdminClient.deleteEndpoint(ENDPOINT_NAME);
        int afterCount = endPointAdminClient.getEndpointCount();
        assertEquals(1, beforeCount - afterCount);
    }

}

