<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.TransportManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportConfigurationDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportPropertyDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>
<%
    // get required parameters to add a transport adaptor to back end.
    TransportManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
    String transportName = request.getParameter("transportName");
    String transportAdaptorConfiguration = request.getParameter("transportConfiguration");
    String msg = null;

    try {
            // add transport adaptor via admin service
            stub.editTransportAdaptorConfigurationFile(transportAdaptorConfiguration,transportName);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }

%>  <%=msg%>   <%

%>
