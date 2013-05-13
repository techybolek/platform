<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorConfigurationInfoDto" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>

<fmt:bundle basename="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="transportmanager.list"
            resourceBundle="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">
        function doDelete(transportName) {
            var theform = document.getElementById('deleteForm');
            theform.transportname.value = transportName;
            theform.submit();
        }
    </script>
    <%
        String transportName = request.getParameter("transportname");
        if (transportName != null) {
            TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
            stub.removeTransportAdaptorConfiguration(transportName);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Transport adaptor successfully deleted.');</script>
    <%
        }
    %>

    <div id="middle">
    <div id="workArea">
        <h3>Available Transport Adaptors</h3>
        <table class="styledLeft">
            <thead>
            <tr>
                <th>Transport Adaptor Name</th>
                <th>Transport Adaptor Type</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
                TransportAdaptorConfigurationInfoDto[] transportDetailsArray = stub.getAllTransportAdaptorConfigurationInfo();
                if (transportDetailsArray != null) {
                    for (TransportAdaptorConfigurationInfoDto transportDetails : transportDetailsArray) {

            %>
            <tr>
                <td>
                    <a href="transport_details.jsp?transportName=<%=transportDetails.getTransportAdaptorName()%>&transportType=<%=transportDetails.getTransportAdaptorType()%>"><%=transportDetails.getTransportAdaptorName()%>
                    </a>

                </td>
                <td><%=transportDetails.getTransportAdaptorType()%>
                </td>
                <td>
                    <a style="background-image: url(../admin/images/delete.gif);"
                       class="icon-link"
                       onclick="doDelete('<%=transportDetails.getTransportAdaptorName()%>')"><font
                            color="#4682b4">Delete</font></a>
                    <a style="background-image: url(../admin/images/edit.gif);"
                       class="icon-link"
                       href="edit_transport_details.jsp?transportName=<%=transportDetails.getTransportAdaptorName()%>&transportType=<%=transportDetails.getTransportAdaptorType()%>"><font
                            color="#4682b4">Edit</font></a>

                </td>


            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>

        <div>
            <br/>
            <a href="transport_adaptor_files_details.jsp">Not-Deployed Files</a>

            <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                             name="transportname"
                                                                             value=""/></form>
        </div>
    </div>


    <script type="text/javascript">
        alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
