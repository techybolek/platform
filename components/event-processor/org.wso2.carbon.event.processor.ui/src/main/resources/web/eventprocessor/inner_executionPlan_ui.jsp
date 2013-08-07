<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIUtils" %>


<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">


<script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>
<script type="text/javascript"
        src="../eventprocessor/js/create_executionPlan_helper.js"></script>

<%--code mirror code--%>

<link rel="stylesheet" href="../eventprocessor/css/codemirror.css"/>
<script src="../eventprocessor/js/codemirror.js"></script>
<script src="../eventprocessor/js/sql.js"></script>

<style>
    .CodeMirror {
        border-top: 1px solid black;
        border-bottom: 1px solid black;
    }
</style>


<script>
    var init = function () {
        var mime = 'text/siddhi-sql-db';

        // get mime type
        if (window.location.href.indexOf('mime=') > -1) {
            mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
        }

        window.editor = CodeMirror.fromTextArea(document.getElementById('queryExpressions'), {
            mode:mime,
            indentWithTabs:true,
            smartIndent:true,
            lineNumbers:true,
            matchBrackets:true,
            autofocus:true
        });
    };
</script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        init();
    });
</script>

<%--Code mirror code end--%>

<%
    EventProcessorAdminServiceStub stub = UIUtils.getEventProcessorAdminService(config, session, request);
    String[] names = stub.getStreamNames();
%>
<tr>
    <td>
        <table class="normal-nopadding">
            <tbody>
            <tr>

                <td class="leftCol-med">Execution Plan Name<span class="required">*</span></td>
                <td><input type="text" name="executionPlanName" id="executionPlanId"
                           class="initE"
                           value=""
                           style="width:100%"/>

                    <div class="sectionHelp">
                        Please Enter the Execution Plan Name.
                    </div>
                </td>
            </tr>


                <%--Name: <input type="text" name="executionPlanName"><br>--%>
            <tr>
                <td class="leftCol-med">
                    Description
                </td>

                <td>
                    <input type="text" name="executionPlanDescription" id="executionPlanDescId"
                           class="initE"
                           value=""
                           style="width:100%"/>

                    <div class="sectionHelp">
                        Please Enter the Execution Plan Description (optional).
                    </div>
                </td>
            </tr>


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
                    <input type="text" name="siddhiSnapshotTime" id="siddhiSnapshotTime"
                           class="initE"
                           value="0"
                           style="width:50%"/>

                    <div class="sectionHelp">
                        Enter the snapshot time in minutes. Entering zero disables snapshots.
                    </div>
                </td>
            </tr>

            <tr>
                <td class="leftCol-med">
                    Distributed processing
                </td>
                <td>
                    <select name="distributedProcessing" id="distributedProcessing">
                        <option value="enabled">Enabled</option>
                        <option value="disabled">Disabled</option>
                    </select>
                </td>
            </tr>


            <!-- imported stream definitions-->
            <tr>
                <td colspan="2">
                    <table id="streamDefinitionsTable" class="styledLeft noBorders spacer-bot">
                        <tbody>
                        <tr name="streamDefinitions">
                            <td colspan="2" class="middle-header">
                                <fmt:message key="wso2imported.stream.mapping"/>
                            </td>
                        </tr>

                        </tbody>
                    </table>
                </td>
            </tr>

            <tr>
                <td colspan="2">
                    <table id="addImportedStreams" class="normal">
                        <tbody>
                        <tr>
                            <td class="col-small"><fmt:message key="property.stream.id"/> :
                            </td>
                            <td>

                                <select id="importedStreamId">
                                    <%
                                        if (names != null) {
                                            for (String streamName : names) {

                                    %>
                                    <option value= <%= "\"" + streamName + "\""%>><%= streamName %>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>

                                </select>
                            </td>

                            <td class="col-small"><fmt:message key="property.as"/> :
                            </td>
                            <td>
                                <input type="text" id="importedStreamAs"/>
                            </td>

                            <td><input type="button" class="button"
                                       value="<fmt:message key="add"/>"
                                       onclick="addImportedStreamDefinition()"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>


                <%--query expressions--%>

            <tr name="queryExpressions">
                <td colspan="2">
                    <b><fmt:message key="wso2query.expressions"/></b>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <textarea class="queryExpressionsTextArea" style="width:100%; height: 150px"
                              id="queryExpressions"
                              name="queryExpressions"></textarea>
                </td>
            </tr>


            <!--exported stream mappings-->

            <tr name="exportedStreamMapping">
                <td colspan="2">
                    <b><fmt:message key="wso2exported.stream.mapping"/></b>
                </td>
            </tr>
            <tr name="exportedStreamMapping">
                <td colspan="2">

                        <%--<h4><fmt:message key="property.data.type.meta"/></h4>--%>
                    <table class="styledLeft noBorders spacer-bot" id="exportedStreamsTable"
                           style="display:none">
                        <thead>
                        <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                        <th class="leftCol-med"><fmt:message key="property.stream.id"/></th>
                        <th><fmt:message key="actions"/></th>
                        </thead>
                    </table>
                    <div class="noDataDiv-plain" id="noExportedStreamData">
                        No Exported Streams Defined
                    </div>
                    <table id="addExportedStreams" class="normal">
                        <tbody>
                        <tr>
                            <td class="col-small"><fmt:message key="property.value.of"/> :
                            </td>
                            <td>
                                <input type="text" id="exportedStreamValueOf"/>
                            </td>
                            <td class="col-small"><fmt:message key="property.stream.id"/> :
                            </td>
                            <td>
                                <input type="text" id="exportedStreamId"/>
                            </td>
                            <td><input type="button" class="button"
                                       value="<fmt:message key="add"/>"
                                       onclick="addExportedStreamDefinition()"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</fmt:bundle>