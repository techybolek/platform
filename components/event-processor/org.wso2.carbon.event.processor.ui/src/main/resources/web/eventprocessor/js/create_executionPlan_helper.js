/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

function addImportedStreamDefinition() {
    var propStreamId = document.getElementById("importedStreamId");
    var propAs = document.getElementById("importedStreamAs");

    var error = "";

    if (propStreamId.value == "") {
        error = "Name field is empty.\n";
    }

    if (propAs.value == "") {
        error = "As field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }

// checking for duplicate stream definitions.
    var streamDefinitionsTable = document.getElementById("streamDefinitionsTable");
    for (var i = 0, row; row = streamDefinitionsTable.rows[i]; i++) {
        if (row.cells.length > 1) { // leaving out headers.
            var existingStreamId = row.getAttribute("streamId");
            var existingAs = row.getAttribute("as");
            if ((propStreamId.value == existingStreamId) && (propAs.value == existingAs)) {
                CARBON.showErrorDialog("An identical imported stream already exists.");
                return;
            }
        }
    }


    jQuery.ajax({
                    type:"POST",
                    url:"../eventprocessor/get_stream_definition_ajaxprocessor.jsp?streamId=" + propStreamId.value + "&streamAs=" + propAs.value,
                    data:{},
                    contentType:"application/json; charset=utf-8",
                    dataType:"text",
                    async:false,
                    success:function (streamDefinitionString) {

                        var definitions = streamDefinitionString.split("|");
                        var streamId = definitions[0].trim();
                        var streamAs = definitions[1].trim();
                        var streamDefinitionString = "define stream " + streamAs + " " + definitions[2].trim();

                        var streamDefinitionsTable = document.getElementById("streamDefinitionsTable");
                        var newStreamDefTableRow = streamDefinitionsTable.insertRow(streamDefinitionsTable.rows.length);
                        newStreamDefTableRow.setAttribute("id", "def:imported:" + streamId);
                        newStreamDefTableRow.setAttribute("streamId", streamId);
                        newStreamDefTableRow.setAttribute("as", streamAs);

                        var newStreamDefCell0 = newStreamDefTableRow.insertCell(0);
                        newStreamDefCell0.innerHTML = "// <i>" + streamId + "</i><br>" + streamDefinitionString;
                        YAHOO.util.Dom.addClass(newStreamDefCell0, "property-names");

                        var newStreamDefCell1 = newStreamDefTableRow.insertCell(1);
                        newStreamDefCell1.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeImportedStreamDefinition(this)">Delete</a>';
                        YAHOO.util.Dom.addClass(newStreamDefCell1, "property-names");
                    }
                });
}


function addExportedStreamDefinition() {
    var propStreamId = document.getElementById("exportedStreamId");
    var propValueOf = document.getElementById("exportedStreamValueOf");
    var propertyTable = document.getElementById("exportedStreamsTable");

    var error = "";

    if (propStreamId.value == "") {
        error = "Name field is empty.\n";
    }

    if (propValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";


// checking for duplicate stream definitions.
    for (var i = 0, row; row = propertyTable.rows[i]; i++) {
        if (i > 0) {  // leaving out the headers.
            var existingValueOf = row.cells[0].innerHTML;
            var existingStreamId = row.cells[1].innerHTML;
            if ((propStreamId.value == existingStreamId) && (propValueOf.value == existingValueOf)) {
                CARBON.showErrorDialog("An identical exported stream already exists.");
                return;
            }
        }
    }

    var newTableRow = propertyTable.insertRow(propertyTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propValueOf.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propStreamId.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCel2 = newTableRow.insertCell(2);
    newCel2.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeExportedStream(this)">Delete</a>';

    propStreamId.value = "";
    propValueOf.value = "";
//    noPropertyDiv.style.display = "none";
//    propType.value = "";
//    showAddProperty();
}


function addExecutionPlan(form) {
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
                            form.submit();
                        } else {
                            CARBON.showErrorDialog("Failed to add event formatter, Exception: " + msg);
                        }
                    }
                });
}


function getExportedStreamDataValues(dataTable) {

    var streamData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;

        streamData = streamData + column0 + "^" + column1 + "^" + "$";
    }
    return streamData;
}

function getImportedStreamDataValues(dataTable) {

    var streamData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {
        var row = dataTable.rows[i];
        var column0 = row.getAttribute("streamId");
        var column1 = row.getAttribute("as");
        streamData = streamData + column0 + "^" + column1 + "^" + "$";
    }
    return streamData;
}

