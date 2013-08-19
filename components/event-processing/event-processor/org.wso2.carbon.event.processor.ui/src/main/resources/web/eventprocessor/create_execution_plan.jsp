<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

    <carbon:breadcrumb
            label="transportmanager.details"
            resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="js/execution_plans.js"></script>
    <script type="text/javascript"
            src="js/create_executionPlan_helper.js"></script>

    <link type="text/css" href="css/buckets.css" rel="stylesheet"/>
    <link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>


    <div id="middle">
        <h2><fmt:message key="title.execution.plan.create"/></h2>
        <div id="workArea">

            <form name="inputForm" action="index.jsp" method="get" id="addExecutionPlanForm">

                <table style="width:100%" id="eventProcessorAdd"
                       class="styledLeft noBorders spacer-bot">

                    <%
                        EventProcessorAdminServiceStub eventProcessorStub = UIUtils.getEventProcessorAdminService(config, session, request);
                        String[] streamNames = eventProcessorStub.getStreamNames();
                        if (streamNames == null || streamNames.length == 0) {
                    %>
                        <%--error page--%>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table id="noExecutionPlanInputTable" class="normal-nopadding"
                                   style="width:100%">
                                <tbody>

                                <tr>
                                    <td class="leftCol-med" colspan="2">Event Streams are not
                                                                        available to import.
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                    <%
                    } else {
                    %>

                    <thead>
                    <tr>
                        <th>Enter Event Processor Details</th>
                    </tr>
                    </thead>
                    <tbody>
                    <%@include file="inner_executionPlan_ui.jsp" %>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="add.execution.plan"/>"
                                   onclick="addExecutionPlan(document.getElementById('addExecutionPlanForm'))"/>
                        </td>
                    </tr>
                    </tbody>
                    <%
                        }
                    %>

                </table>

            </form>
        </div>
    </div>

</fmt:bundle>