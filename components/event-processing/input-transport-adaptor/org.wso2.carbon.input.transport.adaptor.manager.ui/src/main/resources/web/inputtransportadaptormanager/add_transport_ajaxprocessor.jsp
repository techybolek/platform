<%@ page import="org.wso2.carbon.input.transport.adaptor.manager.ui.InputTransportAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.InputTransportAdaptorManagerAdminServiceStub" %>

<%
    // get required parameters to add a transport adaptor to back end.
    InputTransportAdaptorManagerAdminServiceStub stub = InputTransportAdaptorUIUtils.getInputTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String msg = null;

    String transportType = request.getParameter("transportType");

    String inputPropertySet = request.getParameter("inputPropertySet");
    InputTransportAdaptorPropertyDto[] inputTransportProperties = null;

    if (inputPropertySet != null) {
        String[] properties = inputPropertySet.split("\\|");
        if (properties != null) {
            // construct transport adaptor property array for each transport adaptor property
            inputTransportProperties = new InputTransportAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$");
                if (propertyNameAndValue != null) {
                    inputTransportProperties[index] = new InputTransportAdaptorPropertyDto();
                    inputTransportProperties[index].setKey(propertyNameAndValue[0].trim());
                    inputTransportProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }

    try {
        // add transport adaptor via admin service
        stub.deployInputTransportAdaptorConfiguration(transportName, transportType, inputTransportProperties);
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
