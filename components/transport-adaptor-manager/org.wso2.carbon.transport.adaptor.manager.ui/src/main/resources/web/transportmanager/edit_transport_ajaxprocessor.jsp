<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>
<%
    // get required parameters to add a transport adaptor to back end.
    TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String transportPath = request.getParameter("transportPath");
    String transportAdaptorConfiguration = request.getParameter("transportConfiguration");
    String msg = null;
    if (transportName != null) {
        try {
            // add transport adaptor via admin service
            stub.editTransportAdaptorConfigurationFile(transportAdaptorConfiguration, transportName);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    } else if (transportPath != null) {
        try {
            // add transport adaptor via admin service
            stub.editNotDeployedTransportAdaptorConfigurationFile(transportAdaptorConfiguration, transportPath);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }

%>  <%=msg%>   <%

%>
