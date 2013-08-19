<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIUtils" %>

<%
    EventProcessorAdminServiceStub stub = UIUtils.getEventProcessorAdminService(config, session, request);
    String strStreamId = request.getParameter("streamId");
    String strStreamAs = request.getParameter("streamAs");

    if (strStreamId != null) {
        String definition = stub.getStreamDefinitionAsString(strStreamId);
        String streamDefinitionString = strStreamId + " | " + strStreamAs + " | " + definition;

%>

<%=streamDefinitionString%>
<%
    }

%>
