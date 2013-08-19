<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.stub.OutputTransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.stub.types.OutputTransportAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.ui.OutputTransportAdaptorUIUtils" %>

<%
    // get required parameters to add a transport adaptor to back end.
    OutputTransportAdaptorManagerAdminServiceStub stub = OutputTransportAdaptorUIUtils.getOutputTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String testConnection = request.getParameter("testConnection");
    String msg = null;

    String transportType = request.getParameter("transportType");

    String inputPropertySet = request.getParameter("outputPropertySet");
    OutputTransportAdaptorPropertyDto[] inputTransportProperties = null;

    if (inputPropertySet != null) {
        String[] properties = inputPropertySet.split("\\|");
        if (properties != null) {
            // construct transport adaptor property array for each transport adaptor property
            inputTransportProperties = new OutputTransportAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$");
                if (propertyNameAndValue != null) {
                    inputTransportProperties[index] = new OutputTransportAdaptorPropertyDto();
                    inputTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                    inputTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }


    try {
        if (testConnection != null) {
            stub.testConnection(transportName, transportType, inputTransportProperties);
        } else {
            // add transport adaptor via admin service
            stub.deployOutputTransportAdaptorConfiguration(transportName, transportType, inputTransportProperties);
        }
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

%>

<script type="text/javascript">
    alert(msg);
</script>
<%

    }

%>  <%=msg%>   <%

%>
