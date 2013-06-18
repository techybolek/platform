<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorConfigurationInfoDto" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorFileDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>

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
        int totalTransportAdaptors = 0;
        int totalNotDeployedTransportAdaptors = 0;
        if (transportName != null) {
            TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
            stub.removeTransportAdaptorConfiguration(transportName);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Transport adaptor successfully deleted.');</script>
    <%
        }

        TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
        TransportAdaptorConfigurationInfoDto[] transportDetailsArray = stub.getAllTransportAdaptorConfigurationInfo();
        if (transportDetailsArray != null) {
            totalTransportAdaptors = transportDetailsArray.length;
        }

        TransportAdaptorFileDto[] notDeployedTransportAdaptorConfigurationFiles = stub.getNotDeployedTransportAdaptorConfigurationFiles();
        if (notDeployedTransportAdaptorConfigurationFiles != null) {
            totalNotDeployedTransportAdaptors = notDeployedTransportAdaptorConfigurationFiles.length;
        }

    %>

    <div id="middle">
    <div id="workArea">
        <h3>Available Transport Adaptors</h3><br/>
        <h5 class="activeAdaptors"><%=totalTransportAdaptors%> Active Transport
                                                               Adaptors. <% if (totalNotDeployedTransportAdaptors > 0) { %><a
                    href="transport_adaptor_files_details.jsp"><%=totalNotDeployedTransportAdaptors%>
                Inactive Transport Adaptors</a><% } else {%><%=totalNotDeployedTransportAdaptors%>
                                                               Inactive Transport Adaptors <% } %>
        </h5>

        <table class="styledLeft">
        <%

            if (transportDetailsArray != null) {
        %>

            <thead>
            <tr>
                <th>Transport Adaptor Name</th>
                <th>Supported Transport Type</th>
                <th>Transport Adaptor Type</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (TransportAdaptorConfigurationInfoDto transportDetails : transportDetailsArray) {
            %>
            <tr>
                <td>
                    <a href="transport_details.jsp?transportName=<%=transportDetails.getTransportAdaptorName()%>&transportType=<%=transportDetails.getTransportAdaptorType()%>"><%=transportDetails.getTransportAdaptorName()%>
                    </a>

                </td>
                <td><%=transportDetails.getSupportedTransportType()%>
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
                       href="edit_transport_details.jsp?transportName=<%=transportDetails.getTransportAdaptorName()%>"><font
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
