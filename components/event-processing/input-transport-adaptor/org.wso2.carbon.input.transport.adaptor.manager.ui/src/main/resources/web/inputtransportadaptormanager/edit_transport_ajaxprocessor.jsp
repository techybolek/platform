
<%@ page import="org.wso2.carbon.input.transport.adaptor.manager.ui.InputTransportAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.InputTransportAdaptorManagerAdminServiceStub" %>

<%
    // get required parameters to add a transport adaptor to back end.
    InputTransportAdaptorManagerAdminServiceStub stub = InputTransportAdaptorUIUtils.getInputTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String transportPath = request.getParameter("transportPath");
    String transportAdaptorConfiguration = request.getParameter("transportConfiguration");
    String msg = null;
    if (transportName != null) {
        try {
            // add transport adaptor via admin service
            stub.editActiveInputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportName);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    } else if (transportPath != null) {
        try {
            // add transport adaptor via admin service
            stub.editInactiveInputTransportAdaptorConfiguration(transportAdaptorConfiguration, transportPath);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }

%>  <%=msg%>   <%

%>
