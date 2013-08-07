<%@ page import="org.wso2.carbon.output.transport.adaptor.manager.stub.OutputTransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.ui.OutputTransportAdaptorUIUtils" %>

<%
    // get required parameters to add a transport adaptor to back end.
    OutputTransportAdaptorManagerAdminServiceStub stub = OutputTransportAdaptorUIUtils.getOutputTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String transportPath = request.getParameter("transportPath");
    String transportAdaptorConfiguration = request.getParameter("transportConfiguration");
    String msg = null;
    if (transportName != null) {
        try {
            // add transport adaptor via admin service
            stub.editOutputTransportAdaptorConfigurationFile(transportAdaptorConfiguration, transportName);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    } else if (transportPath != null) {
        try {
            // add transport adaptor via admin service
            stub.editNotDeployedOutputTransportAdaptorConfigurationFile(transportAdaptorConfiguration, transportPath);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }

%>  <%=msg%>   <%

%>
