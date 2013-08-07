<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>

<%
    // get Event Stream Definition
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    String transportAdaptorName = request.getParameter("transportAdaptorName");

%>

<%

    if (transportAdaptorName != null) {
        String supportedMappings = "";
        String[] mappingTypes = stub.getSupportedMappingTypes(transportAdaptorName);
        for (String mappingType : mappingTypes) {
            supportedMappings = supportedMappings + "|" + mappingType;
        }

%>


<%=supportedMappings%>
<%
    }

%>
