<%@ page import="com.google.gson.Gson" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorPropertiesDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>

<%
    // get Transport Adaptor properties
    TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
    String transportType = request.getParameter("transportType");

%>

<%

    if (transportType != null) {

        TransportAdaptorPropertiesDto transportAdaptorPropertiesDto = stub.getAllTransportAdaptorPropertiesDto(transportType);
        String propertiesString = "";
        propertiesString = new Gson().toJson(transportAdaptorPropertiesDto);


%>


<%=propertiesString%>
<%
    }

%>
