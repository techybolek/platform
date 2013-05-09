<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.TransportManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorProperties" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>

<%
    // get Transport Adaptor properties
    TransportManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
    String transportType = request.getParameter("transportType");
    if (transportType != null) {

        TransportAdaptorProperties transportAdaptorProperties = stub.getAllTransportAdaptorProperties(transportType);
        String propertiesString = "";
        propertiesString = new Gson().toJson(transportAdaptorProperties);


%>


<%=propertiesString%>
<%
    }

%>
