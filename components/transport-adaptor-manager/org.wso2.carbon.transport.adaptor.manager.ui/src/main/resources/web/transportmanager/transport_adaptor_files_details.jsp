<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorFileDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="transportmanager.details"
            resourceBundle="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">
        function doDelete(filePath) {
            var theform = document.getElementById('deleteForm');
            theform.filePath.value = filePath;
            theform.submit();
        }
    </script>
    <%
        String filePath = request.getParameter("filePath");
        if (filePath != null) {
            TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
            stub.removeTransportAdaptorConfigurationFile(filePath);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Transport File successfully deleted.');</script>
    <%
        }
    %>


    <div id="middle">
        <div id="workArea">
            <h3>Not-Deployed Transport Adaptors</h3>
            <table class="styledLeft">

                <%
                    TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
                    TransportAdaptorFileDto[] transportDetailsArray = stub.getNotDeployedTransportAdaptorConfigurationFiles();
                    if (transportDetailsArray != null) {
                %>
                <thead>
                <tr>
                    <th>File Name</th>
                    <th>Transport Adaptor Name</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <%
                    for (TransportAdaptorFileDto transportAdaptorFile : transportDetailsArray) {

                %>

                <tbody>
                <tr>
                    <td>
                        <%=transportAdaptorFile.getFilePath().substring(transportAdaptorFile.getFilePath().lastIndexOf('/') + 1, transportAdaptorFile.getFilePath().length())%>
                    </td>
                    <td><%=transportAdaptorFile.getTransportAdaptorName()%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=transportAdaptorFile.getFilePath()%>')"><font
                                color="#4682b4">Delete</font></a>
                        <a style="background-image: url(../admin/images/edit.gif);"
                           class="icon-link"
                           href="edit_transport_details.jsp?transportPath=<%=transportAdaptorFile.getFilePath()%>"><font
                                color="#4682b4">Source View</font></a>
                    </td>

                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>

            <div>
                <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                                 name="filePath"
                                                                                 value=""/></form>
            </div>
        </div>


        <script type="text/javascript">
            alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
        </script>
</fmt:bundle>