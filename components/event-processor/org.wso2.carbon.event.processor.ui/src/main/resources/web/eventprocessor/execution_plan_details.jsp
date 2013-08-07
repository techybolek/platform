<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.SiddhiConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIUtils" %>


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
<script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>

<link type="text/css" href="../eventprocessor/css/buckets.css" rel="stylesheet"/>
<link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>

<%--<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">--%>

<div id="middle">
<div id="workArea">
<table style="width:100%" id="eventProcessorDetails" class="styledLeft noBorders spacer-bot">
<thead>
<tr>
    <th>Event Processor Details</th>
</tr>
</thead>
<tbody>
<tr>
    <td>
        <%

            String execPlanName = request.getParameter("execPlan");
            EventProcessorAdminServiceStub stub = UIUtils.getEventProcessorAdminService(config, session, request);
            ExecutionPlanConfigurationDto configurationDto = stub.getExecutionPlan(execPlanName);
        %>
        <table class="normal-nopadding">

            <tr>
                <td class="leftCol-med">Execution Plan Name<span class="required">*</span></td>
                <td><input type="text" name="executionPlanName" id="executionPlanId"
                           class="initE"
                           style="width:100%"
                           value=<%= "\"" + execPlanName + "\"" %>
                                   readonly/>
                </td>
            </tr>


            <tr>
                <td class="leftCol-med">
                    Description
                </td>

                <td>
                    <input type="text" name="executionPlanDescription" id="executionPlanDescId"
                           class="initE"
                           style="width:100%"
                           value=<%= "\"" +((configurationDto.getDescription()!= null)? configurationDto.getDescription().trim():"")+ "\"" %>
                                   readonly/>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr>
    <td colspan="2">
        <table class="normal-nopadding">
            <tbody>
            <tr name="siddhiConfigsHeader">
                <td colspan="2">
                    <b>Siddhi Configurations</b>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med">
                    Snapshot time interval
                </td>

                <td>

                    <%
                        String snapshotTime = "0";
                        if (configurationDto.getSiddhiConfigurations() != null) {
                            for (SiddhiConfigurationDto siddhiConfigurationDto : configurationDto.getSiddhiConfigurations()) {
                                if (UIConstants.SIDDHI_SNAPSHOT_INTERVAL.equalsIgnoreCase(siddhiConfigurationDto.getKey())) {
                                    snapshotTime = siddhiConfigurationDto.getValue();
                                }
                            }
                        }
                    %>

                    <input type="text" name="siddhiSnapshotTime" id="siddhiSnapshotTime"
                           class="initE"
                           style="width:100%"
                           value=<%= "\"" +snapshotTime + "\"" %>
                                   readonly/>

                </td>
            </tr>

            <tr>
                <td class="leftCol-med">
                    Distributed processing
                </td>
                <td>
                    <%
                        String distributedProcessingStatus = "N/A";
                        if (configurationDto.getSiddhiConfigurations() != null) {
                            for (SiddhiConfigurationDto siddhiConfig : configurationDto.getSiddhiConfigurations()) {
                                if (UIConstants.SIDDHI_DISTRIBUTED_PROCESSING.equalsIgnoreCase(siddhiConfig.getKey())) {
                                    if ("true".equals(siddhiConfig.getValue())) {
                                        distributedProcessingStatus = "Enabled";
                                    } else {
                                        distributedProcessingStatus = "Disabled";
                                    }
                                }
                            }
                        }
                    %>
                    <input type="text" name="siddhiDistrProcessing" id="siddhiDistrProcessing"
                           class="initE"
                           style="width:100%"
                           value=<%= "\"" +distributedProcessingStatus + "\"" %>
                                   readonly/>

                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<!--imported stream mappings-->

<!--query expressions-->
<tr>
    <td>

        <table id="streamDefinitionsTable" class="styledLeft noBorders spacer-bot">
            <tbody>
            <tr name="streamDefinitions">
                <td colspan="2" class="middle-header">
                    <fmt:message key="wso2imported.stream.definitions"/>
                </td>
            </tr>

            <%
                if (configurationDto.getImportedStreams() != null) {
                    for (org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto dto : configurationDto.getImportedStreams()) {
                        String paramDefinitions = stub.getStreamDefinitionAsString(dto.getStreamId());
                        String query = "define stream " + dto.getSiddhiStreamName() + " " + paramDefinitions;

            %>
            <tr>
                <td>
                    <i>
                        <%=
                        "// " + dto.getStreamId()
                        %>
                    </i>
                    <br>
                    <%=
                    query
                    %>

                </td>
            </tr>

            <%
                    }
                }
            %>

            </tbody>
        </table>
    </td>
</tr>

    <%--query expressions--%>
<tr>

    <td>

        <table id="queryExpressionsTable" class="normal-nopadding">
            <tbody>
            <tr name="queryExpressions">
                <td colspan="2">
                    <b><fmt:message key="wso2query.expressions"/></b>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <textarea class="queryExpressionsTextArea" style="width:100%; height: 150px"
                              name="queryExpressions"
                              readonly><%= configurationDto.getQueryExpressions() %>
                    </textarea>
                </td>
            </tr>
            </tbody>
        </table>

    </td>
</tr>


<!--exported stream mappings-->
<tr>
    <td>

            <%--begin exported streams table          --%>
        <table class="normal-nopadding"
               style="width:100%">
            <tbody>
            <tr name="exportedStreamMapping">
                <td colspan="2">
                    <b><fmt:message key="wso2exported.stream.mapping"/></b>
                </td>
            </tr>
            <tr name="exportedStreamMapping">
                <td colspan="2">

                    <table class="styledLeft noBorders spacer-bot" id="exportedStreamsTable"
                           style="width:100%">
                        <thead>
                        <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                        <th class="leftCol-med"><fmt:message key="property.stream.id"/></th>
                            <%--<th><fmt:message key="actions"/></th>--%>
                        </thead>
                        <tbody>

                        <%
                            if (configurationDto.getExportedStreams() != null) {
                                for (org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto dto : configurationDto.getExportedStreams()) {

                        %>

                        <tr>
                            <td><%= dto.getSiddhiStreamName() %>
                            </td>
                            <td><%= dto.getStreamId() %>
                            </td>

                        </tr>
                        <%
                                }
                            }
                        %>

                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
            <%--end of exported streams table--%>

    </td>
</tr>
</tbody>
</table>

</div>
</div>

</fmt:bundle>