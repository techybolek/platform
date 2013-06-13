package org.wso2.carbon.integration.test.sampleservices.mexservice;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.dataretrieval.client.MexClient;
import org.apache.axis2.mex.MexConstants;
import org.apache.axis2.mex.om.Metadata;
import org.apache.axis2.mex.om.MetadataSection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.aar.services.AARServiceUploaderClient;
import org.wso2.carbon.automation.api.clients.module.mgt.ModuleAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.integration.test.ASIntegrationTest;
import org.wso2.carbon.module.mgt.stub.types.ModuleMetaData;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MEXTestCase extends ASIntegrationTest {

    private static final Log log = LogFactory.getLog(MEXTestCase.class);

    @DataProvider
    public Object[][] serviceNameDataProvider() {    // service names
        return new Object[][]{
                {"HelloWorldService1"},
                {"HelloWorldService2"},
                {"HelloWorldService3"},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @AfterClass(alwaysRun = true)
    public void servicesDelete() throws Exception {
        deleteService("HelloWorldService1");      // removing uploaded HelloWorldService1.aar
        log.info("HelloWorldService1 deleted");

        deleteService("HelloWorldService2");      // removing uploaded HelloWorldService2.aar
        log.info("HelloWorldService2 deleted");

        deleteService("HelloWorldService3");      // removing uploaded HelloWorldService3.aar
        log.info("HelloWorldService3 deleted");
    }

    @Test(groups = "wso2.as", description = "Upload HelloWorldServices and verify deployment",
            dataProvider = "serviceNameDataProvider")
    public void servicesUpload(String serviceName) throws Exception {

        AARServiceUploaderClient aarServiceUploaderClient
                = new AARServiceUploaderClient(asServer.getBackEndUrl(),
                asServer.getSessionCookie());

        // uploading HelloWorldServices
        aarServiceUploaderClient.uploadAARFile(serviceName + ".aar",
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                        File.separator + "AS" + File.separator + "aar" + File.separator +
                        serviceName + ".aar", "");

        isServiceDeployed(serviceName);  // verifying the deployment
        log.info(serviceName + ".aar service uploaded and deployed successfully");
    }

    @Test(groups = "wso2.as", description = "invoke MEX services",
            dependsOnMethods = "servicesUpload", dataProvider = "serviceNameDataProvider")
    public void invokeServices(String serviceName) throws Exception {

        boolean moduleExists = false;  // checking the availability of wso2mex-4.0 module for the service

        ModuleAdminServiceClient moduleAdminServiceClient =
                new ModuleAdminServiceClient(asServer.getBackEndUrl(), asServer.getSessionCookie());

        ModuleMetaData[] moduleMetaData = moduleAdminServiceClient.listModulesForService(serviceName);
        for (int x = 0; x <= moduleMetaData.length; x++) {
            if (moduleMetaData[x].getModulename().equals("wso2mex")) {
                moduleExists = true;
                //engaging the module to the service
                moduleAdminServiceClient.engageModule("wso2mex-4.0", serviceName);
                break;
            }
        }

        assertTrue(moduleExists, "module engagement failure due to the unavailability of wso2mex module " +
                "at service level context");

        // for each service URL types : XML Schema , WSDL , WS-Policy
        for (int x = 1; x <= 3; x++) {
            mexClient(x, serviceName);
        }
    }

    private void mexClient(int type, String serviceName) throws Exception {
        String targetEPR = "http://localhost:9763/services/" + serviceName;
        MexClient serviceClient = getServiceClient(targetEPR);

        OMElement request;
        OMElement response = null;
        String dialect = null;
        String identifier = "";   // as this is optional
        identifier = (identifier.length() == 0) ? null : identifier;

        try {
            switch (type) {

                case 1:
                    dialect = MexConstants.SPEC.DIALECT_TYPE_SCHEMA;  // dialect type
                    request = serviceClient.setupGetMetadataRequest(dialect,
                            identifier);
                    response = serviceClient.sendReceive(request);  // sending the request
                    log.info(response);

                    if (serviceName.equals("HelloWorldService1")) {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:mex=\"http://" +
                                "schemas.xmlsoap.org/ws/2004/09/mex\" xmlns:tns=\"http://" +
                                "example1.service.mex.sample.appserver.wso2.org\">" +
                                "<mex:MetadataSection Dialect=\"http://www.w3.org/2001/XMLSchema\">" +
                                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
                                "xmlns:ns=\"http://example1.service.mex.sample.appserver.wso2.org\" " +
                                "attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" " +
                                "targetNamespace=\"http://example1.service.mex.sample.appserver.wso2.org\">\n" +
                                "<xs:element name=\"greetings\">\n" +
                                "<xs:complexType>\n" +
                                "<xs:sequence />\n" +
                                "</xs:complexType>\n" +
                                "</xs:element>\n" +
                                "<xs:element name=\"greetingsResponse\">\n" +
                                "<xs:complexType>\n" +
                                "<xs:sequence>\n" +
                                "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"" +
                                "xs:string\" />\n" +
                                "</xs:sequence>\n" +
                                "</xs:complexType>\n" +
                                "</xs:element>\n" +
                                "</xs:schema></mex:MetadataSection><mex:MetadataSection " +
                                "Dialect=\"http://www.w3.org/2001/XMLSchema\">" +
                                "<mex:Location>http://localhost:9763/services/HelloWorldService1?xsd" +
                                "</mex:Location></mex:MetadataSection></mex:Metadata>");

                    } else if (serviceName.equals("HelloWorldService2")) {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:" +
                                "mex=\"http://schemas.xmlsoap.org/ws/2004/09/mex\" xmlns:" +
                                "tns=\"http://example1.service.mex.appserver.wso2.org\">" +
                                "<mex:MetadataSection Dialect=\"http://www.w3.org/2001/XMLSchema\">" +
                                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
                                "xmlns:ns1=\"http://org.apache.axis2/xsd\" xmlns:" +
                                "wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:" +
                                "ns=\"http://example1.service.mex.appserver.wso2.org\" xmlns:" +
                                "wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:" +
                                "http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:" +
                                "ns0=\"http://example1.service.mex.appserver.wso2.org\"" +
                                " xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" " +
                                "xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" " +
                                "xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" " +
                                "attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" " +
                                "targetNamespace=\"http://example1.service.mex.appserver.wso2.org\">\n" +
                                "<xs:element name=\"greetingsResponse\">\n" +
                                "<xs:complexType>\n" +
                                "<xs:sequence>\n" +
                                "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" " +
                                "type=\"xs:string\" />\n" +
                                "</xs:sequence>\n" +
                                "</xs:complexType>\n" +
                                "</xs:element>\n" +
                                "</xs:schema></mex:MetadataSection>" +
                                "<mex:MetadataSection Dialect=\"http://www.w3.org/2001/XMLSchema\">" +
                                "<mex:Location>http://localhost:9763/services/HelloWorldService2?xsd" +
                                "</mex:Location></mex:MetadataSection></mex:Metadata>");

                    } else {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:mex=\"" +
                                "http://schemas.xmlsoap.org/ws/2004/09/mex\" " +
                                "xmlns:tns=\"http://example3.service.mex.sample.appserver.wso2.org\">" +
                                "<mex:MetadataSection Dialect=\"http://www.w3.org/2001/XMLSchema\">" +
                                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
                                "xmlns:ns=\"http://example3.service.mex.sample.appserver.wso2.org\" " +
                                "attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" " +
                                "targetNamespace=\"http://example3.service.mex.sample.appserver.wso2.org\">\n" +
                                "<xs:element name=\"greetings\">\n" +
                                "<xs:complexType>\n" +
                                "<xs:sequence />\n" +
                                "</xs:complexType>\n" +
                                "</xs:element>\n" +
                                "<xs:element name=\"greetingsResponse\">\n" +
                                "<xs:complexType>\n" +
                                "<xs:sequence>\n" +
                                "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" " +
                                "type=\"xs:string\" />\n" +
                                "</xs:sequence>\n" +
                                "</xs:complexType>\n" +
                                "</xs:element>\n" +
                                "</xs:schema></mex:MetadataSection><mex:MetadataSection " +
                                "Dialect=\"http://www.w3.org/2001/XMLSchema\"><mex:Location>" +
                                "http://localhost:9763/services/HelloWorldService3?xsd</mex:Location>" +
                                "</mex:MetadataSection></mex:Metadata>");
                    }

                    break;

                case 2:
                    dialect = MexConstants.SPEC.DIALECT_TYPE_WSDL;  // dialect type
                    request = serviceClient.setupGetMetadataRequest(dialect,
                            identifier);
                    response = serviceClient.sendReceive(request);    // sending the request
                    log.info(response);

                    if (serviceName.equals("HelloWorldService1")) {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:" +
                                "mex=\"http://schemas.xmlsoap.org/ws/2004/09/mex\" xmlns:" +
                                "tns=\"http://example1.service.mex.sample.appserver.wso2.org\">" +
                                "<mex:MetadataSection Dialect=\"http://schemas.xmlsoap.org/wsdl/\">" +
                                "<mex:Location>http://localhost:9763/services/HelloWorldService1?wsdl" +
                                "</mex:Location></mex:MetadataSection></mex:Metadata>");

                    } else if (serviceName.equals("HelloWorldService2")) {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:mex=\"http://" +
                                "schemas.xmlsoap.org/ws/2004/09/mex\" xmlns:tns=\"http://" +
                                "example1.service.mex.appserver.wso2.org\"><mex:MetadataSection " +
                                "Dialect=\"http://schemas.xmlsoap.org/wsdl/\"><wsdl:definitions " +
                                "xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:" +
                                "ns1=\"http://org.apache.axis2/xsd\" xmlns:wsp=\"http://" +
                                "schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:http=\"" +
                                "http://schemas.xmlsoap.org/wsdl/http/\" xmlns:ns0=\"" +
                                "http://example1.service.mex.appserver.wso2.org\" xmlns:" +
                                "xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:mime=" +
                                "\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:soap=\"" +
                                "http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:soap12=\"" +
                                "http://schemas.xmlsoap.org/wsdl/soap12/\" targetNamespace=" +
                                "\"http://example1.service.mex.appserver.wso2.org\">\n" +
                                "    <wsdl:documentation>HelloWorldService2</wsdl:documentation>\n" +
                                "    <wsdl:types>\n" +
                                "        <xs:schema xmlns:ns=\"http://example1.service.mex.appserver" +
                                ".wso2.org\" attributeFormDefault=\"qualified\" " +
                                "elementFormDefault=\"qualified\" targetNamespace=\"" +
                                "http://example1.service.mex.appserver.wso2.org\">\n" +
                                "            <xs:element name=\"greetingsResponse\">\n" +
                                "                <xs:complexType>\n" +
                                "                    <xs:sequence>\n" +
                                "                        <xs:element minOccurs=\"0\"" +
                                " name=\"return\" nillable=\"true\" type=\"xs:string\" />\n" +
                                "                    </xs:sequence>\n" +
                                "                </xs:complexType>\n" +
                                "            </xs:element>\n" +
                                "        </xs:schema>\n" +
                                "    </wsdl:types>\n" +
                                "    <wsdl:message name=\"greetingsRequest\" />\n" +
                                "    <wsdl:message name=\"greetingsResponse\">\n" +
                                "        <wsdl:part name=\"parameters\" element=\"ns0:" +
                                "greetingsResponse\" />\n" +
                                "    </wsdl:message>\n" +
                                "    <wsdl:portType name=\"HelloWorldService2PortType\">\n" +
                                "        <wsdl:operation name=\"greetings\">\n" +
                                "            <wsdl:input xmlns:wsaw=\"http://www.w3.org/2006/05/" +
                                "addressing/wsdl\" message=\"ns0:greetingsRequest\" wsaw:" +
                                "Action=\"urn:greetings\" />\n" +
                                "            <wsdl:output xmlns:wsaw=\"http://www.w3.org/2006/05/" +
                                "addressing/wsdl\" message=\"ns0:greetingsResponse\" wsaw:" +
                                "Action=\"urn:greetingsResponse\" />\n" +
                                "        </wsdl:operation>\n" +
                                "    </wsdl:portType>\n" +
                                "    <wsdl:binding name=\"HelloWorldService2SOAP11Binding\" " +
                                "type=\"ns0:HelloWorldService2PortType\">\n" +
                                "        <soap:binding transport=\"http://schemas.xmlsoap.org/soap/" +
                                "http\" style=\"document\" />\n" +
                                "        <wsdl:operation name=\"greetings\">\n" +
                                "            <soap:operation soapAction=\"urn:greetings\" " +
                                "style=\"document\" />\n" +
                                "            <wsdl:input>\n" +
                                "                <soap:body use=\"literal\" />\n" +
                                "            </wsdl:input>\n" +
                                "            <wsdl:output>\n" +
                                "                <soap:body use=\"literal\" />\n" +
                                "            </wsdl:output>\n" +
                                "        </wsdl:operation>\n" +
                                "    </wsdl:binding>\n" +
                                "    <wsdl:binding name=\"HelloWorldService2SOAP12Binding\" " +
                                "type=\"ns0:HelloWorldService2PortType\">\n" +
                                "        <soap12:binding transport=\"http://schemas.xmlsoap.org/" +
                                "soap/http\" style=\"document\" />\n" +
                                "        <wsdl:operation name=\"greetings\">\n" +
                                "            <soap12:operation soapAction=\"urn:greetings\" " +
                                "style=\"document\" />\n" +
                                "            <wsdl:input>\n" +
                                "                <soap12:body use=\"literal\" />\n" +
                                "            </wsdl:input>\n" +
                                "            <wsdl:output>\n" +
                                "                <soap12:body use=\"literal\" />\n" +
                                "\n" +
                                "            </wsdl:output>\n" +
                                "        </wsdl:operation>\n" +
                                "    </wsdl:binding>\n" +
                                "    <wsdl:binding name=\"HelloWorldService2HttpBinding\" " +
                                "type=\"ns0:HelloWorldService2PortType\">\n" +
                                "        <http:binding verb=\"POST\" />\n" +
                                "        <wsdl:operation name=\"greetings\">\n" +
                                "            <http:operation location=\"HelloWorldService2/" +
                                "greetings\" />\n" +
                                "            <wsdl:input>\n" +
                                "                <mime:content type=\"text/xml\" part=\"greetings\" />\n" +
                                "            </wsdl:input>\n" +
                                "            <wsdl:output>\n" +
                                "                <mime:content type=\"text/xml\" part=\"greetings\" />\n" +
                                "            </wsdl:output>\n" +
                                "        </wsdl:operation>\n" +
                                "    </wsdl:binding>\n" +
                                "    <wsdl:service name=\"HelloWorldService2\">\n" +
                                "    \t<wsp:Policy Id=\"HelloWorldService2_Policy\">\n" +
                                "    \t\t<wsp:ExactlyOne>\n" +
                                "\t\t\t<wsp:All>\n" +
                                "\t\t\t\t<intf:Assertion1 xmlns:intf=\"http://test.policy.org/" +
                                "appserver\" marker=\"1\" />\n" +
                                "\t\t\t</wsp:All>\n" +
                                "\t\t</wsp:ExactlyOne>\n" +
                                "\t</wsp:Policy>\n" +
                                "        <wsdl:port name=\"HelloWorldService2SOAP11port_http\" " +
                                "binding=\"ns0:HelloWorldService2SOAP11Binding\">\n" +
                                "            <soap:address location=\"http://127.0.0.1:9763/axis2/" +
                                "services/HelloWorldService2\" />\n" +
                                "        </wsdl:port>\n" +
                                "        <wsdl:port name=\"HelloWorldService2SOAP12port_http\" " +
                                "binding=\"ns0:HelloWorldService2SOAP12Binding\">\n" +
                                "            <soap12:address location=\"http://127.0.0.1:9763/axis2/" +
                                "services/HelloWorldService2\" />\n" +
                                "        </wsdl:port>\n" +
                                "        <wsdl:port name=\"HelloWorldService2Httpport\" " +
                                "binding=\"ns0:HelloWorldService2HttpBinding\">\n" +
                                "            <http:address location=\"http://127.0.0.1:9763/axis2/" +
                                "services/HelloWorldService2\" />\n" +
                                "        </wsdl:port>\n" +
                                "    </wsdl:service>\n" +
                                "</wsdl:definitions></mex:MetadataSection><mex:MetadataSection " +
                                "Dialect=\"http://schemas.xmlsoap.org/wsdl/\"><mex:Location>\n" +
                                "\t\t\thttp://10.100.1.204:9763/services/HelloWorldService2?wsdl\n" +
                                "\t\t</mex:Location></mex:MetadataSection></mex:Metadata>");

                    } else {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:mex=" +
                                "\"http://schemas.xmlsoap.org/ws/2004/09/mex\" " +
                                "xmlns:tns=\"http://example3.service.mex.sample.appserver.wso2.org\">" +
                                "<mex:MetadataSection Dialect=\"http://schemas.xmlsoap.org/wsdl/\">" +
                                "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">" +
                                "<wsdl:documentation>This is a dummy WSDL generated by the " +
                                "CustomWSDLLocator in Example 3.</wsdl:documentation> " +
                                "</wsdl:definitions></mex:MetadataSection><mex:MetadataSection " +
                                "Dialect=\"http://schemas.xmlsoap.org/wsdl/\">" +
                                "<mex:Location>http://localhost:9763/services/HelloWorldService3?wsdl" +
                                "</mex:Location></mex:MetadataSection></mex:Metadata>");
                    }

                    break;

                case 3:
                    dialect = MexConstants.SPEC.DIALECT_TYPE_POLICY;    // dialect type
                    request = serviceClient.setupGetMetadataRequest(dialect,
                            identifier);
                    response = serviceClient.sendReceive(request);    // sending the request

                    log.info(response);

                    if (serviceName.equals("HelloWorldService1")) {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:mex=\"" +
                                "http://schemas.xmlsoap.org/ws/2004/09/mex\" xmlns:" +
                                "tns=\"http://example1.service.mex.sample.appserver.wso2.org\" />");

                    } else if (serviceName.equals("HelloWorldService2")) {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:mex=\"" +
                                "http://schemas.xmlsoap.org/ws/2004/09/mex\" xmlns:" +
                                "tns=\"http://example1.service.mex.appserver.wso2.org\">" +
                                "<mex:MetadataSection Dialect=\"" +
                                "http://schemas.xmlsoap.org/ws/2004/09/policy\">" +
                                "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"" +
                                " Id=\"HelloWorldService2_Policy\">\n" +
                                "    \t<wsp:ExactlyOne>\n" +
                                "\t\t<wsp:All>\n" +
                                "\t\t\t<intf:Assertion1 xmlns:intf=\"http://test.policy.org/appserver\" " +
                                "marker=\"1\" />\n" +
                                "\t\t</wsp:All>\n" +
                                "\t</wsp:ExactlyOne>\n" +
                                "    </wsp:Policy></mex:MetadataSection><mex:MetadataSection " +
                                "Dialect=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"><" +
                                "mex:Location>\n" +
                                "\t\t\thttp://10.100.1.204:9763/services/HelloWorldService2?policy\n" +
                                "\t\t</mex:Location></mex:MetadataSection></mex:Metadata>");

                    } else {
                        assertEquals(response.toString(), "<mex:Metadata xmlns:mex=\"" +
                                "http://schemas.xmlsoap.org/ws/2004/09/mex\" xmlns:tns=\"" +
                                "http://example3.service.mex.sample.appserver.wso2.org\" />");
                    }

                    break;

                default:
                    break;
            }
        } catch (NumberFormatException ex) {
            log.info(ex);
        }

        Metadata metadata = new Metadata();
        metadata.fromOM(response);

        MetadataSection[] metaDatSections = metadata.getMetadatSections();
        // checking the metadata availability
        if (metaDatSections == null || metaDatSections.length == 0) {
            log.info("No MetadataSection is available for service " + serviceName + " ,  Dialect "
                    + dialect);
        }
    }

    private MexClient getServiceClient(String targetEPR) throws AxisFault {
        MexClient serviceClient = new MexClient();

        Options options = serviceClient.getOptions();
        options.setTo(new EndpointReference(targetEPR));
        options.setAction(DRConstants.SPEC.Actions.GET_METADATA_REQUEST);

        options.setExceptionToBeThrownOnSOAPFault(true);

        return serviceClient;
    }
}
