<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.output.transport.adaptor.manager.stub.OutputTransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.stub.types.OutputTransportAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.ui.OutputTransportAdaptorUIUtils" %>

<%
    // get Transport Adaptor properties
    OutputTransportAdaptorManagerAdminServiceStub stub = OutputTransportAdaptorUIUtils.getOutputTransportManagerAdminService(config, session, request);
    String transportType = request.getParameter("transportType");

%>

<%

    if (transportType != null) {

        OutputTransportAdaptorPropertiesDto transportAdaptorPropertiesDto = stub.getAllTransportAdaptorPropertiesDto(transportType);
        String propertiesString = "";
        propertiesString = new Gson().toJson(transportAdaptorPropertiesDto);


%>


<%=propertiesString%>
<%
    }

%>
