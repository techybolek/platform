<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorPropertyDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>
<%
    // get required parameters to add a transport adaptor to back end.
    TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String msg = null;

    String transportType = request.getParameter("transportType");
    // property set contains a set of properties, eg; userName$myName|url$http://wso2.org|
    String commonPropertySet = request.getParameter("commonPropertySet");
    TransportAdaptorPropertyDto[] commonTransportProperties = null;

    if (commonPropertySet != null) {
        String[] properties = commonPropertySet.split("\\|");
        if (properties != null) {
            // construct transport adaptor property array for each transport adaptor property
            commonTransportProperties = new TransportAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$");
                if (propertyNameAndValue != null) {
                    commonTransportProperties[index] = new TransportAdaptorPropertyDto();
                    commonTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                    commonTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }

    String inputPropertySet = request.getParameter("inputPropertySet");
    TransportAdaptorPropertyDto[] inputTransportProperties = null;

    if (inputPropertySet != null) {
        String[] properties = inputPropertySet.split("\\|");
        if (properties != null) {
            // construct transport adaptor property array for each transport adaptor property
            inputTransportProperties = new TransportAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$");
                if (propertyNameAndValue != null) {
                    inputTransportProperties[index] = new TransportAdaptorPropertyDto();
                    inputTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                    inputTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }

    String outputPropertySet = request.getParameter("outputPropertySet");
    TransportAdaptorPropertyDto[] outputTransportProperties = null;

    if (outputPropertySet != null) {
        String[] properties = outputPropertySet.split("\\|");
        if (properties != null) {
            // construct transport adaptor property array for each transport adaptor property
            outputTransportProperties = new TransportAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$");
                if (propertyNameAndValue != null) {
                    outputTransportProperties[index] = new TransportAdaptorPropertyDto();
                    outputTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                    outputTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }

    try {
        // add transport adaptor via admin service
        stub.addTransportAdaptorConfiguration(transportName, transportType, inputTransportProperties, outputTransportProperties, commonTransportProperties);
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

    }

%>  <%=msg%>   <%

%>
