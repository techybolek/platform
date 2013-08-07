<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.input.transport.adaptor.manager.ui.InputTransportAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.InputTransportAdaptorManagerAdminServiceStub" %>

<%
    // get Transport Adaptor properties
    InputTransportAdaptorManagerAdminServiceStub stub = InputTransportAdaptorUIUtils.getInputTransportManagerAdminService(config, session, request);
    String transportType = request.getParameter("transportType");

%>

<%

    if (transportType != null) {

        InputTransportAdaptorPropertiesDto transportAdaptorPropertiesDto = stub.getAllTransportAdaptorPropertiesDto(transportType);
        String propertiesString = "";
        propertiesString = new Gson().toJson(transportAdaptorPropertiesDto);


%>


<%=propertiesString%>
<%
    }

%>
