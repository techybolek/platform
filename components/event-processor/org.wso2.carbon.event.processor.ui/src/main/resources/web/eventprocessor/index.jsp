<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

<carbon:breadcrumb
        label="eventprocessor.list"
        resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%--<script type="text/javascript">--%>
    <%--function doDelete(executionPlanName) {--%>
        <%--var theform = document.getElementById('deleteForm');--%>
        <%--theform.executionPlan.value = executionPlanName;--%>
        <%--theform.submit();--%>
    <%--}--%>


    <%--function setStatisticsTracingEnabled(elementPrefix, executionPlanName) {--%>
        <%--var statsElement = document.getElementById(elementPrefix + executionPlanName);--%>
        <%--var currentAction = statsElement.getAttribute('operationTodo');--%>
        <%--var nextAction;--%>
        <%--if (currentAction == 'enableStat') {--%>
            <%--nextAction = 'disableStat';--%>
        <%--} else if (currentAction == 'disableStat') {--%>
            <%--nextAction = 'enableStat';--%>
        <%--} else if (currentAction == 'enableTracing') {--%>
            <%--nextAction = 'disableTracing';--%>
        <%--} else if (currentAction == 'disableTracing') {--%>
            <%--nextAction = 'enableTracing';--%>
        <%--}--%>
        <%--jQuery.ajax({--%>
                        <%--type:'POST',--%>
                        <%--url:'stats_tracing_ajaxprocessor.jsp',--%>
                        <%--data:'execPlanName=' + executionPlanName + '&action=' + currentAction,--%>
                        <%--success:function (msg) {--%>
                            <%--statsElement.setAttribute('operationTodo', nextAction);--%>
                            <%--var anchorElement = document.getElementById('anchor' + executionPlanName);--%>
                            <%--if (nextAction == 'enableStat') {--%>
                                <%--anchorElement.textContent = '<fmt:message key="stat.enable.link"/>';--%>
                            <%--} else if (nextAction == 'disableStat') {--%>
                                <%--anchorElement.textContent = '<fmt:message key="stat.disable.link"/>';--%>
                            <%--} else if (nextAction == 'enableTracing') {--%>
                                <%--anchorElement.textContent = '<fmt:message key="tracing.enable.link"/>';--%>
                            <%--} else if (nextAction == 'disableTracing') {--%>
                                <%--anchorElement.textContent = '<fmt:message key="tracing.disable.link"/>';--%>

                            <%--}--%>
                        <%--},--%>
                        <%--error:function (msg) {--%>
                            <%--CARBON.showErrorDialog('<fmt:message key="stat.change.error"/>' +--%>
                                                   <%--' ' + executionPlanName);--%>
                        <%--}--%>
                    <%--});--%>
    <%--}--%>

<%--</script>--%>

<script type="text/javascript">

    var ENABLE = "enable";
    var DISABLE = "disable";
    var STAT = "statistics";
    var TRACE = "Tracing";

    function doDelete(executionPlanName) {
        var theform = document.getElementById('deleteForm');
        theform.executionPlan.value = executionPlanName;
        theform.submit();
    }

    function disableStat(execPlanName) {
        jQuery.ajax({
                        type:'POST',
                        url:'stats_tracing_ajaxprocessor.jsp',
                        data:'execPlanName=' + execPlanName + '&action=disableStat',
                        success:function (msg) {
                            handleCallback(execPlanName, DISABLE, STAT);
                        },
                        error:function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                                   ' ' + execPlanName);
                        }
                    });
    }

    function enableStat(execPlanName) {
        jQuery.ajax({
                        type:'POST',
                        url:'stats_tracing_ajaxprocessor.jsp',
                        data:'execPlanName=' + execPlanName + '&action=enableStat',
                        success:function (msg) {
                            handleCallback(execPlanName, ENABLE, STAT);
                        },
                        error:function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                                   ' ' + execPlanName);
                        }
                    });
    }

    function handleCallback(execPlanName, action, type) {
        var element;
        if (action == "enable") {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + execPlanName);
                element.style.display = "";
                element = document.getElementById("enableStat" + execPlanName);
                element.style.display = "none";
            } else {
                element = document.getElementById("disableTracing" + execPlanName);
                element.style.display = "";
                element = document.getElementById("enableTracing" + execPlanName);
                element.style.display = "none";
            }
        } else {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + execPlanName);
                element.style.display = "none";
                element = document.getElementById("enableStat" + execPlanName);
                element.style.display = "";
            } else {
                element = document.getElementById("disableTracing" + execPlanName);
                element.style.display = "none";
                element = document.getElementById("enableTracing" + execPlanName);
                element.style.display = "";
            }
        }
    }

    function enableTracing(execPlanName) {
        jQuery.ajax({
                        type:'POST',
                        url:'stats_tracing_ajaxprocessor.jsp',
                        data:'execPlanName=' + execPlanName + '&action=enableTracing',
                        success:function (msg) {
                            handleCallback(execPlanName, ENABLE, TRACE);
                        },
                        error:function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                                   ' ' + execPlanName);
                        }
                    });
    }

    function disableTracing(execPlanName) {
        jQuery.ajax({
                        type:'POST',
                        url:'stats_tracing_ajaxprocessor.jsp',
                        data:'execPlanName=' + execPlanName + '&action=disableTracing',
                        success:function (msg) {
                            handleCallback(execPlanName, DISABLE, TRACE);
                        },
                        error:function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                                   ' ' + execPlanName);
                        }
                    });
    }


</script>

<%--
<script language="javascript">

</script>
--%>


<%
    EventProcessorAdminServiceStub stub = UIUtils.getEventProcessorAdminService(config, session, request);
    String executionPlanName = request.getParameter("executionPlan");
    int totalExecutionPlans = 0;

    String[] failedExecPlanNames = stub.getFailedExecutionPlanFileNames();
    int totalNotDeployedExecutionPlans = 0;
    if (failedExecPlanNames != null) {
        totalNotDeployedExecutionPlans = failedExecPlanNames.length;
    }

    if (executionPlanName != null) {
        stub.removeExecutionPlanConfiguration(executionPlanName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Execution Plan successfully deleted.');</script>
<%
    }

    ExecutionPlanConfigurationDto[] executionPlanConfigurationDtos = stub.getAllExecutionPlans();
    if (executionPlanConfigurationDtos != null) {
        totalExecutionPlans = executionPlanConfigurationDtos.length;
    }

//        ExecutionPlanConfigurationFileDto[] notDeployedEventFormatterConfigurationFiles = stub.get();
//        if (notDeployedEventFormatterConfigurationFiles != null) {
//            totalNotDeployedEventFormatters = notDeployedEventFormatterConfigurationFiles.length;
//        }

%>

<div id="middle">
    <div id="workArea">
        <h3>Available Execution Plans</h3><br/>
        <h5 class="activeExecutionPlans"><%=totalExecutionPlans%> Active Execution
                                                                  Plans.

            <% if (totalNotDeployedExecutionPlans > 0) { %><a
                    href="notdeployed_execution_plan_files_details.jsp"><%=totalNotDeployedExecutionPlans%>
                <fmt:message
                        key="inactive.execution.plans"/></a><% } else {%><%="0"%>
            <fmt:message key="inactive.execution.plans"/> <% } %>


                <%--<% if (totalNotDeployedExecutionPlans > 0) { %>
                <a
                        href="notdeployed_execution_plan_files_details.jsp"><%=totalNotDeployedExecutionPlans%>
                    Inactive Execution Plans</a><% } else {%><%=totalNotDeployedExecutionPlans%>
                Inactive Execution
                Plans
                --%>
                <%--<% } %>--%>
        </h5>

        <table class="styledLeft">
            <%

                if (executionPlanConfigurationDtos != null) {
            %>
            <thead>
            <tr>
                <th>Execution Plan Name</th>
                <th>Description</th>
                <th style="width:30%">Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (ExecutionPlanConfigurationDto executionPlanConfigurationDto : executionPlanConfigurationDtos) {
            %>
            <tr>
                <td>
                    <a href="execution_plan_details.jsp?execPlan=<%=executionPlanConfigurationDto.getName()%>">
                        <%=executionPlanConfigurationDto.getName()%>
                    </a>

                        <%--<%=executionPlanConfigurationDto.getName()%>--%>
                </td>
                <td><%=executionPlanConfigurationDto.getDescription()%>
                </td>
                <td>

                    <%--<div class="inlineDiv">--%>
                        <%--<div id="changeStats<%= executionPlanConfigurationDto.getName()%>"--%>
                             <%--operationTodo="<%=(executionPlanConfigurationDto.getStatisticsEnabled()? "disableStat": "enableStat")%>">--%>
                            <%--<a href="#" id="anchorchangeStats"--%>
                               <%--onclick="setStatisticsTracingEnabled('changeStats',  '<%= executionPlanConfigurationDto.getName() %>')"--%>
                               <%--class="icon-link"--%>
                               <%--style="background-image:url(../admin/images/static-icon.gif);">--%>
                                <%--<%--%>
                                    <%--if (executionPlanConfigurationDto.getStatisticsEnabled()) {--%>
                                <%--%>--%>
                                <%--<fmt:message key="stat.disable.link"/>--%>
                                <%--<%--%>
                                <%--} else {--%>
                                <%--%>--%>
                                <%--<fmt:message key="stat.enable.link"/>--%>
                                <%--<%--%>
                                    <%--}--%>
                                <%--%>--%>
                            <%--</a>--%>
                        <%--</div>--%>


                        <%--<div id="changeTracing<%= executionPlanConfigurationDto.getName()%>"--%>
                             <%--operationTodo="<%=(executionPlanConfigurationDto.getTracingEnabled()? "disableTracing": "enableTracing")%>">--%>
                            <%--<a href="#" id="anchor<%= executionPlanConfigurationDto.getName() %>"--%>
                               <%--onclick="setStatisticsTracingEnabled('changeTracing',  '<%= executionPlanConfigurationDto.getName() %>')"--%>
                               <%--class="icon-link"--%>
                               <%--style="background-image:url(../admin/images/static-icon.gif);">--%>
                                <%--<%--%>
                                    <%--if (executionPlanConfigurationDto.getTracingEnabled()) {--%>
                                <%--%>--%>
                                <%--<fmt:message key="tracing.disable.link"/>--%>
                                <%--<%--%>
                                <%--} else {--%>
                                <%--%>--%>
                                <%--<fmt:message key="tracing.enable.link"/>--%>
                                <%--<%--%>
                                    <%--}--%>
                                <%--%>--%>
                            <%--</a>--%>

                        <%--</div>--%>
                    <%--</div>--%>




                    <% if (executionPlanConfigurationDto.getStatisticsEnabled()) {%>
                    <div class="inlineDiv">
                        <div id="disableStat<%= executionPlanConfigurationDto.getName()%>">
                            <a href="#"
                               onclick="disableStat('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                        <div id="enableStat<%= executionPlanConfigurationDto.getName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enableStat('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableStat<%= executionPlanConfigurationDto.getName()%>">
                            <a href="#"
                               onclick="enableStat('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                        <div id="disableStat<%= executionPlanConfigurationDto.getName()%>"
                             style="display:none">
                            <a href="#"
                               onclick="disableStat('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                    </div>
                    <% }
                        if (executionPlanConfigurationDto.getTracingEnabled()) {%>
                    <div class="inlineDiv">
                        <div id="disableTracing<%= executionPlanConfigurationDto.getName()%>">
                            <a href="#"
                               onclick="disableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                        <div id="enableTracing<%= executionPlanConfigurationDto.getName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableTracing<%= executionPlanConfigurationDto.getName() %>">
                            <a href="#"
                               onclick="enableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                        <div id="disableTracing<%= executionPlanConfigurationDto.getName() %>"
                             style="display:none">
                            <a href="#"
                               onclick="disableTracing('<%= executionPlanConfigurationDto.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                    </div>

                    <% } %>


                    <a style="background-image: url(../admin/images/delete.gif);"
                       class="icon-link"
                       onclick="doDelete('<%=executionPlanConfigurationDto.getName()%>')"><font
                            color="#4682b4">Delete</font></a>
                    <a style="background-image: url(../admin/images/edit.gif);"
                       class="icon-link"
                       href="edit_execution_plan.jsp?execPlanName=<%=executionPlanConfigurationDto.getName()%>"><font
                            color="#4682b4">Edit</font></a>

                </td>
            </tr>
            </tbody>
            <%
                    }

                }
            %>

        </table>

        <div>
            <br/>

            <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                             name="executionPlan"
                                                                             value=""/></form>
        </div>
    </div>
</div>

<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
