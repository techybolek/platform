<%@ page import="com.google.gson.Gson" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>

<%
    // get Transport Adaptor properties
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    String transportAdaptorName = request.getParameter("transportAdaptorName");

    if (transportAdaptorName != null) {
        EventFormatterPropertyDto[] eventFormatterPropertiesDto = stub.getEventFormatterProperties(transportAdaptorName);
        String propertiesString = "";
        propertiesString = new Gson().toJson(eventFormatterPropertiesDto);


%>


<%=propertiesString%>
<%
    }

%>
