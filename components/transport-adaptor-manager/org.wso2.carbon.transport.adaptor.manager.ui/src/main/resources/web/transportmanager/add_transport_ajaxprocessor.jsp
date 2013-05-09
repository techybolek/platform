<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.TransportManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportConfigurationDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportPropertyDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>
<%
    // get required parameters to add a transport adaptor to back end.
    TransportManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String msg = null;

    TransportConfigurationDto[] transportConfigurationDtoArray = null;
    if (stub != null) {
        try {
            transportConfigurationDtoArray = stub.getAllTransportConfigurationNamesAndTypes();
        } catch (Exception e) {
%>
<script type="text/javascript">
    location.href = 'index.jsp';</script>
<%
            return;
        }
    }

    if (transportConfigurationDtoArray != null) {
        for (TransportConfigurationDto transportConfigurationDetails : transportConfigurationDtoArray) {
            if (transportConfigurationDetails.getTransportName().equals(transportName)) {
                msg = transportName + " already exists.";
                break;
            }
        }
    }
    if (msg == null) {
        String transportType = request.getParameter("transportType");
        // property set contains a set of properties, eg; userName$myName|url$http://wso2.org|
        String commonPropertySet = request.getParameter("commonPropertySet");
        TransportPropertyDto[] commonTransportProperties = null;

        if (commonPropertySet != null) {
            String[] properties = commonPropertySet.split("\\|");
            if (properties != null) {
                // construct transport adaptor property array for each transport adaptor property
                commonTransportProperties = new TransportPropertyDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyNameAndValue = property.split("\\$");
                    if (propertyNameAndValue != null) {
                        commonTransportProperties[index] = new TransportPropertyDto();
                        commonTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                        commonTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                        index++;
                    }
                }

            }
        }

        String inputPropertySet = request.getParameter("inputPropertySet");
        TransportPropertyDto[] inputTransportProperties = null;

        if (inputPropertySet != null) {
            String[] properties = inputPropertySet.split("\\|");
            if (properties != null) {
                // construct transport adaptor property array for each transport adaptor property
                inputTransportProperties = new TransportPropertyDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyNameAndValue = property.split("\\$");
                    if (propertyNameAndValue != null) {
                        inputTransportProperties[index] = new TransportPropertyDto();
                        inputTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                        inputTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                        index++;
                    }
                }

            }
        }

        String outputPropertySet = request.getParameter("outputPropertySet");
        TransportPropertyDto[] outputTransportProperties = null;

        if (outputPropertySet != null) {
            String[] properties = outputPropertySet.split("\\|");
            if (properties != null) {
                // construct transport adaptor property array for each transport adaptor property
                outputTransportProperties = new TransportPropertyDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyNameAndValue = property.split("\\$");
                    if (propertyNameAndValue != null) {
                        outputTransportProperties[index] = new TransportPropertyDto();
                        outputTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                        outputTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                        index++;
                    }
                }

            }
        }

        try {
            // add transport adaptor via admin service
            stub.addTransportConfiguration(transportName, transportType, inputTransportProperties, outputTransportProperties, commonTransportProperties);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }
%>  <%=msg%>   <%

%>
