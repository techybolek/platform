<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationFileDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

    <carbon:breadcrumb
            label="executionplan.details"
            resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
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
            EventProcessorAdminServiceStub stub = UIUtils.getEventProcessorAdminService(config, session, request);
            stub.removeExecutionPlanConfigurationFile(filePath);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Execution plan file successfully deleted.');</script>
    <%
        }
    %>


    <div id="middle">
        <div id="workArea">
            <h3><fmt:message key="not.deployed.execution.plans"/></h3>
            <table class="styledLeft">

                <%
                    EventProcessorAdminServiceStub stub = UIUtils.getEventProcessorAdminService(config, session, request);
                    ExecutionPlanConfigurationFileDto[] execPlanDetailsArray = stub.getFailedExecutionPlanFiles();
                    if (execPlanDetailsArray != null) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="execution.plan.file.name"/></th>
                    <th><fmt:message key="execution.plan.name"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <%
                    for (ExecutionPlanConfigurationFileDto executionPlanConfigurationFileDto : execPlanDetailsArray) {

                %>

                <tbody>
                <tr>
                    <td>
                        <%=executionPlanConfigurationFileDto.getPath().substring(executionPlanConfigurationFileDto.getPath().lastIndexOf('/') + 1, executionPlanConfigurationFileDto.getPath().length())%>
                    </td>
                    <td><%=executionPlanConfigurationFileDto.getName()%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=executionPlanConfigurationFileDto.getPath()%>')"><font
                                color="#4682b4">Delete</font></a>
                        <a style="background-image: url(../admin/images/edit.gif);"
                           class="icon-link"
                           href="edit_execution_plan.jsp?execPlanPath=<%=executionPlanConfigurationFileDto.getPath()%>"><font
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