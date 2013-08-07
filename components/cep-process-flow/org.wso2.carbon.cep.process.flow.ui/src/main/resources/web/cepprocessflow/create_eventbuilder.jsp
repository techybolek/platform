<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  WSO2 Inc. licenses this file to you under the Apache License,
~  Version 2.0 (the "License"); you may not use this file except
~  in compliance with the License.
~  You may obtain a copy of the License at
~
~     http://www.apache.org/licenses/LICENSE-2.0
~
~  Unless required by applicable law or agreed to in writing,
~  software distributed under the License is distributed on an
~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~  KIND, either express or implied.  See the License for the
~  specific language governing permissions and limitations
~  under the License.
--%>

<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.cep.process.flow.ui.i18n.Resources">


<link type="text/css" href="../eventbuilder/css/cep.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<%--<script type="text/javascript" src="../eventbuilder/js/subscriptions.js"></script>--%>
<%--<script type="text/javascript" src="../eventbuilder/js/eventing_utils.js"></script>--%>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>
<script type="text/javascript" src="../eventbuilder/js/create_eventBuilder_helper.js"></script>

<script type="text/javascript">
    function addCEPEventBuilder(form) {

        var isFieldEmpty = false;

        var eventBuilderName = document.getElementById("eventBuilderNameId").value.trim();
        var toStreamName = document.getElementById("toStreamName").value.trim();
        var toStreamVersion = document.getElementById("toStreamVersion").value.trim();
        var transportAdaptorName = document.getElementById("transportAdaptorNameSelect")[document.getElementById("transportAdaptorNameSelect").selectedIndex].text;


        var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
        // Check for white space
        if (!reWhiteSpace.test(eventBuilderName)) {
            CARBON.showErrorDialog("White spaces are not allowed in event builder name.");
            return;
        }
        if (isFieldEmpty || (eventBuilderName == "")) {
            // empty fields are encountered.
            CARBON.showErrorDialog("Empty inputs fields are not allowed.");
            return;
        }

        var propertyCount = 0;
        var msgConfigPropertyString = "";

        // all properties, not required and required are checked
        while (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null ||
               document.getElementById("msgConfigProperty_" + propertyCount) != null) {
            // if required fields are empty
            if (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null) {
                if (document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim() == "") {
                    // values are empty in fields
                    isFieldEmpty = true;
                    msgConfigPropertyString = "";
                    break;
                }
                else {
                    // values are stored in parameter string to send to backend
                    var propertyValue = document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim();
                    var propertyName = document.getElementById("msgConfigProperty_Required_" + propertyCount).name;
                    msgConfigPropertyString = msgConfigPropertyString + propertyName + "$" + propertyValue + "|";

                }
            } else if (document.getElementById("msgConfigProperty_" + propertyCount) != null) {
                var notRequiredPropertyName = document.getElementById("msgConfigProperty_" + propertyCount).name;
                var notRequiredPropertyValue = document.getElementById("msgConfigProperty_" + propertyCount).value.trim();
                if (notRequiredPropertyValue == "") {
                    notRequiredPropertyValue = "  ";
                }
                msgConfigPropertyString = msgConfigPropertyString + notRequiredPropertyName + "$" + notRequiredPropertyValue + "|";
            }
            propertyCount++;
        }

        if (isFieldEmpty) {
            // empty fields are encountered.
            CARBON.showErrorDialog("Empty inputs fields are not allowed.");
            return;
        } else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'wso2event') {

            var parameters = "?eventBuilderName=" + eventBuilderName + "&toStreamName=" + toStreamName + "&toStreamVersion=" + toStreamVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=wso2event" + "&msgConfigPropertySet=" + msgConfigPropertyString;

            var metaData = "";
            var correlationData = "";
            var payloadData = "";

            var metaDataTable = document.getElementById("inputMetaDataTable");
            if (metaDataTable.rows.length > 1) {
                metaData = getWso2EventDataValues(metaDataTable);
                parameters = parameters + "&metaData=" + metaData;
            }
            var correlationDataTable = document.getElementById("inputCorrelationDataTable");
            if (correlationDataTable.rows.length > 1) {
                correlationData = getWso2EventDataValues(correlationDataTable);
                parameters = parameters + "&correlationData=" + correlationData;
            }
            var payloadDataTable = document.getElementById("inputPayloadDataTable");
            if (payloadDataTable.rows.length > 1) {
                payloadData = getWso2EventDataValues(payloadDataTable);
                parameters = parameters + "&payloadData=" + payloadData;
            }

            if (metaData == "" && correlationData == "" && payloadData == "") {
                CARBON.showErrorDialog("Mapping parameters cannot be empty.");
                return;
            }
        }

        else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'text') {

            var parameters = "?eventBuilderName=" + eventBuilderName + "&toStreamName=" + toStreamName
                                     + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=text" + "&msgConfigPropertySet=" + msgConfigPropertyString;

            if (textData == "") {
                CARBON.showErrorDialog("Mapping parameters cannot be empty.");
                return;
            }

            else {
                parameters = parameters + "&textData=" + textData;
            }
        }

        else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'xml') {
            var batchProcessingEnabled = document.getElementById("batchProcessingEnabled").checked;
            var parameters = "?eventBuilderName=" + eventBuilderName + "&toStreamName=" + toStreamName + "&toStreamVersion=" + toStreamVersion
                                     + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=xml" + "&msgConfigPropertySet=" + msgConfigPropertyString
                                     + "&batchProcessingEnabled=" + batchProcessingEnabled;

            var prefixData = "";
            var xpathData = "";

            var xpathPrefixTable = document.getElementById("inputXpathPrefixTable");
            if (xpathPrefixTable.rows.length > 1) {
                prefixData = getXpathPrefixValues(xpathPrefixTable);
                parameters = parameters + "&prefixData=" + prefixData;
            }

            var xpathExprTable = document.getElementById("inputXpathExprTable");
            if (xpathExprTable.rows.length > 1) {
                xpathData = getXpathDataValues(xpathExprTable);
                parameters = parameters + "&xpathData=" + xpathData;
            }

            if (prefixData == "" && xpathData == "") {
                CARBON.showErrorDialog("Mapping parameters cannot be empty.");
                return;
            }
        }
        else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'map') {

            var parameters = "?eventBuilderName=" + eventBuilderName + "&toStreamName=" + toStreamName + "&toStreamVersion=" + toStreamVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=map" + "&msgConfigPropertySet=" + msgConfigPropertyString;

            var mapData = "";

            var mapDataTable = document.getElementById("inputMapPropertiesTable");
            if (mapDataTable.rows.length > 1) {
                mapData = getMapDataValues(mapDataTable);
                parameters = parameters + "&mapData=" + mapData;
            }

            else {
                CARBON.showErrorDialog("Mapping parameters cannot be empty.");
                return;
            }
        }
        else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'json') {

            var parameters = "?eventBuilderName=" + eventBuilderName + "&toStreamName=" + toStreamName + "&toStreamVersion=" + toStreamVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=json" + "&msgConfigPropertySet=" + msgConfigPropertyString;

            var jsonData = document.getElementById("jsonSourceText").value;

            if (jsonData == "") {
                CARBON.showErrorDialog("Mapping parameters cannot be empty.");
                return;
            }

            else {
                parameters = parameters + "&jsonData=" + jsonData;
            }
        }


        jQuery.ajax({
                        type:"POST",
                        url:"../eventbuilder/add_eventbuilder_ajaxprocessor.jsp" + parameters,
                        contentType:"application/json; charset=utf-8",
                        dataType:"text",
                        data:{},
                        async:false,
                        success:function (msg) {
                            if (msg.trim() == "true") {
                                CARBON.showConfirmationDialog(
                                        "Event Builder " + eventBuilderName + " successfully added, Do you want to add another Event Builder?", function () {
                                            loadUIElements('builder')
                                        }, function () {
                                            loadUIElements('processor')
                                        });
                            } else {
                                CARBON.showErrorDialog("Failed to add event builder, Exception: " + msg);
                            }
                        }
                    });

    }
</script>

<div id="middle">
    <div id="workArea">
        <table width="100%" class="styledLeft noBorders spacer-bot">
            <tbody>
            <tr>
                <td><h4>Create a New Event Builder</h4></td>
                <td style="text-align:right;">
                    <input type="button" value="Create Input Transport Adaptor"
                           onclick="loadUIElements('inputAdaptor')"/>
                </td>
            </tr>
            </tbody>
        </table>
        <%
            EventBuilderAdminServiceStub eventBuilderStub = CEPProcessFlowUIUtils.getEventBuilderAdminService(config, session, request);
            String[] inputTransportNames = eventBuilderStub.getInputTransportNames();
            if (inputTransportNames != null) {
        %>
        <form name="inputForm" method="get" id="addEventBuilder">
            <table style="width:100%" id="ebAdd" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="event.builder.create.header"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <%@include file="../eventbuilder/inner_eventbuilder_ui.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="buttonRow">
                        <input type="button" value="Add Event Builder"
                               onclick="addCEPEventBuilder(document.getElementById('addEventBuilder'))"/>
                    </td>
                </tr>
                </tbody>

            </table>
        </form>
        <%
        } else {
        %>
        <table style="width:100%" id="ebNoAdd" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="event.builder.notransport.header"/></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="formRaw">
                    <table id="noEventBuilderInputTable" class="normal-nopadding"
                           style="width:100%">
                        <tbody>

                        <tr>
                            <td class="leftCol-med" colspan="2">No Input Transport Adaptors
                                                                available
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
        <%
            }
        %>

    </div>
</div>
</fmt:bundle>
