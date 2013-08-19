<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.cep.process.flow.ui.CEPProcessFlowUIUtils" %>
<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>


<fmt:bundle basename="org.wso2.carbon.cep.process.flow.ui.i18n.Resources">

    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>
    <script type="text/javascript"
            src="../eventprocessor/js/create_executionPlan_helper.js"></script>

    <link type="text/css" href="../eventprocessor/css/buckets.css" rel="stylesheet"/>
    <link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>

    <script type="text/javascript">
        function addCEPExecutionPlan(form) {
            var isFieldEmpty = false;
            var execPlanName = document.getElementById("executionPlanId").value.trim();
            var description = document.getElementById("executionPlanDescId").value.trim();
            var snapshotInterval = document.getElementById("siddhiSnapshotTime").value.trim();
            var distributedProcessing = document.getElementById("distributedProcessing").value.trim();

            // query expressions can be empty for pass thru buckets...
            var queryExpressions = document.getElementById("queryExpressions").value.trim();
            var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");

            if (!reWhiteSpace.test(execPlanName)) {
                CARBON.showErrorDialog("White spaces are not allowed in execution plan name.");
                return;
            }
            if ((execPlanName == "")) {
                // empty fields are encountered.
                CARBON.showErrorDialog("Empty inputs fields are not allowed.");
                return;
            }

            var propertyCount = 0;
            var outputPropertyParameterString = "";

            // all properties, not required and required are checked
            while (document.getElementById("property_Required_" + propertyCount) != null ||
                   document.getElementById("property_" + propertyCount) != null) {
                // if required fields are empty
                if (document.getElementById("property_Required_" + propertyCount) != null) {
                    if (document.getElementById("property_Required_" + propertyCount).value.trim() == "") {
                        // values are empty in fields
                        isFieldEmpty = true;
                        outputPropertyParameterString = "";
                        break;
                    }
                    else {
                        // values are stored in parameter string to send to backend
                        var propertyValue = document.getElementById("property_Required_" + propertyCount).value.trim();
                        var propertyName = document.getElementById("property_Required_" + propertyCount).name;
                        outputPropertyParameterString = outputPropertyParameterString + propertyName + "$" + propertyValue + "|";

                    }
                } else if (document.getElementById("property_" + propertyCount) != null) {
                    var notRequriedPropertyValue = document.getElementById("property_" + propertyCount).value.trim();
                    var notRequiredPropertyName = document.getElementById("property_" + propertyCount).name;
                    if (notRequriedPropertyValue == "") {
                        notRequriedPropertyValue = "  ";
                    }
                    outputPropertyParameterString = outputPropertyParameterString + notRequiredPropertyName + "$" + notRequriedPropertyValue + "|";

                }
                propertyCount++;
            }

            if (isFieldEmpty) {
                // empty fields are encountered.
                CARBON.showErrorDialog("Empty inputs fields are not allowed.");
                return;
            } else {

                var mapData = "";
                var importedStreams = "";

                var importedStreamTable = document.getElementById("streamDefinitionsTable");
                if (importedStreamTable.rows.length > 1) {
                    importedStreams = getImportedStreamDataValues(importedStreamTable);
                }
                else {
                    CARBON.showErrorDialog("Imported streams cannot be empty.");
                    return;
                }

                var exportedStreams = "";
                var exportedStreamsTable = document.getElementById("exportedStreamsTable");
                if (exportedStreamsTable.rows.length > 1) {
                    exportedStreams = getExportedStreamDataValues(exportedStreamsTable);
                }
                else {
                    CARBON.showErrorDialog("Exported streams cannot be empty.");
                    return;
                }
            }

            var paramString = "?execPlanName=" + execPlanName + "&description=" + description +
                              "&snapshotInterval=" + snapshotInterval + "&distributedProcessing=" + distributedProcessing +
                              "&queryExpressions=" + queryExpressions + "&importedStreams=" + importedStreams +
                              "&exportedStreams=" + exportedStreams;

            jQuery.ajax({
                            type:"POST",
                            url:"../eventprocessor/add_execution_plan_ajaxprocessor.jsp" + paramString,
                            contentType:"application/json; charset=utf-8",
                            dataType:"text",
                            data:{},
                            async:false,
                            success:function (msg) {
                                if (msg.trim() == "true") {
                                    CARBON.showInfoDialog(
                                            "Execution Plan " + execPlanName + " successfully added, Proceed to create Event Formatter", function () {
                                                loadUIElements('formatter');
                                            }, null);
                                } else {
                                    CARBON.showErrorDialog("Failed to add event formatter, Exception: " + msg);
                                }
                            }
                        });
        }
    </script>

    <div id="middle">
        <div id="workArea">

            <form name="inputForm" method="get" id="addExecutionPlanForm">
                <h6><fmt:message key="title.execution.plan.create"/></h6>
                <br/>
                <table style="width:100%" id="eventProcessorAdd"
                       class="styledLeft noBorders spacer-bot">
                    <%
                        EventProcessorAdminServiceStub eventProcessorStub = CEPProcessFlowUIUtils.getEventProcessorAdminService(config, session, request);
                        String[] streamNames = eventProcessorStub.getStreamNames();
                        if (streamNames == null || streamNames.length == 0) {
                    %>
                        <%--error page--%>

                    <thead>
                    <tr>
                        <th><fmt:message key="event.processor.nostreams.header"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table id="noExecutionPlanInputTable" class="normal-nopadding"
                                   style="width:100%">
                                <tbody>

                                <tr>
                                    <td width="80%">Event Streams are not
                                                    available to import. Please
                                                    Create Event Builders to
                                                    proceed further
                                    </td>
                                    <td width="20%"><a onclick="loadUIElements('builder')"
                                                       style="background-image:url(images/add.gif);"
                                                       class="icon-link" href="#">
                                        Add New Event Builder
                                    </a>
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
                    <%@include file="../eventprocessor/inner_executionPlan_ui.jsp" %>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="add.execution.plan"/>"
                                   onclick="addCEPExecutionPlan(document.getElementById('addExecutionPlanForm'))"/>
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