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

//Method that used in jsp files

function getWso2EventDataValues(dataTable) {

    var wso2EventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;

        wso2EventData = wso2EventData + column0 + "^" + column1 + "^" + column2 + "$";
    }
    return wso2EventData;
}

function getJsonDataValues(dataTable) {

    var jsonData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;
        var column3 = row.cells[3].innerHTML;

        // For JSON we use a different terminator (*) since $ is already used in JSONPath
        jsonData = jsonData + column0 + "^" + column1 + "^" + column2 + "^" + column3 + "*";
    }
    return jsonData;
}

function getXpathDataValues(dataTable) {

    var xpathData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;
        var column3 = row.cells[3].innerHTML;

        xpathData = xpathData + column0 + "^" + column1 + "^" + column2 + "^" + column3 + "$";
    }
    return xpathData;
}

function getXpathPrefixValues(dataTable) {
    var xpathPrefixes = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;

        xpathPrefixes = xpathPrefixes + column0 + "^" + column1 + "$";
    }

    return xpathPrefixes;
}

function getTextDataValues(dataTable) {

    var textData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;
        var column3 = row.cells[3].innerHTML;

        textData = textData + column0 + "^" + column1 + "^" + column2 + "^" + column3 + "$";
    }
    return textData;
}

function getMapDataValues(dataTable) {

    var mapEventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;

        mapEventData = mapEventData + column0 + "^" + column1 + "^" + column2 + "^" + "$";
    }
    return mapEventData;
}

function addEventBuilder(form) {

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

        var parameters = "?eventBuilderName=" + eventBuilderName + "&toStreamName=" + toStreamName + "&toStreamVersion=" + toStreamVersion
                                 + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=text" + "&msgConfigPropertySet=" + msgConfigPropertyString;

        var textData = "";

        var textDataTable = document.getElementById("inputTextMappingTable");
        if (textDataTable.rows.length > 1) {
            textData = getTextDataValues(textDataTable);
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
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

        var jsonData = "";

        var jsonDataTable = document.getElementById("inputJsonpathExprTable");
        if (jsonDataTable.rows.length > 1) {
            jsonData = getJsonDataValues(jsonDataTable);
            parameters = parameters + "&jsonData=" + jsonData;
        }

        if (jsonData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }


    jQuery.ajax({
                    type: "POST",
                    url: "../eventbuilder/add_eventbuilder_ajaxprocessor.jsp" + parameters,
                    contentType: "application/json; charset=utf-8",
                    dataType: "text",
                    data: {},
                    async: false,
                    success: function (msg) {
                        if (msg.trim() == "true") {
                            form.submit();
                        } else {
                            CARBON.showErrorDialog("Failed to add event builder, Exception: " + msg);
                        }
                    }
                });

}

function addInputMapProperty() {
    var propName = document.getElementById("inputMapPropName");
    var propValueOf = document.getElementById("inputMapPropValueOf");
    var propertyTable = document.getElementById("inputMapPropertiesTable");
    var propertyTableTBody = document.getElementById("inputMapPropertiesTBody");
    var propType = document.getElementById("inputMapPropType");
    var noPropertyDiv = document.getElementById("noInputMapProperties");

    var error = "";

    if (propName.value == "") {
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

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = propertyTableTBody.insertRow(propertyTableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propValueOf.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    propValueOf.value = "";
    noPropertyDiv.style.display = "none";
    // propType.value = "";
    // showAddProperty();
}

function addInputXpathDef() {
    var prefixName = document.getElementById("inputPrefixName");
    var xpathNs = document.getElementById("inputXpathNs");
    var propertyTable = document.getElementById("inputXpathPrefixTable");
    var tableTBody = document.getElementById("inputXpathPrefixTBody");
    var noPropertyDiv = document.getElementById("noInputPrefixes");

    var error = "";

    if (prefixName.value == "") {
        error = "Prefix field is empty.\n";
    }

    if (xpathNs.value == "") {
        error = "Namespace field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = prefixName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = xpathNs.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'xml' + '\')">Delete</a>';

    prefixName.value = "";
    xpathNs.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputXmlProperty() {
    var propName = document.getElementById("inputPropertyName");
    var xpathExpr = document.getElementById("inputPropertyValue");
    var propDefault = document.getElementById("inputPropertyDefault");
    var propertyTable = document.getElementById("inputXpathExprTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputXpathExprTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }

    if (xpathExpr.value == "") {
        error = "XPath field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = xpathExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    xpathExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputRegexDef() {
    var regex = document.getElementById("inputRegexDef");
    var propertyTable = document.getElementById("inputRegexDefTable");
    var regexSelect = document.getElementById("inputPropertyValue");
    var tableTBody = document.getElementById("inputRegexDefTBody");
    var noPropertyDiv = document.getElementById("noInputRegex");

    var error = "";


    if (regex.value == "") {
        error = "Regular expression field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = regex.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'xml' + '\')">Delete</a>';


    if (regexSelect.value == "") {
        regexSelect.remove(regexSelect.selectedIndex);
    }
    var newRegexOption = document.createElement("option");
    newRegexOption.value = regex.value;
    newRegexOption.text = regex.value;
    regexSelect.add(newRegexOption, null);

    regex.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputTextProperty() {
    var propName = document.getElementById("inputPropertyName");
    var regexExpr = document.getElementById("inputPropertyValue");
    var propDefault = document.getElementById("inputPropertyDefault");
    var propertyTable = document.getElementById("inputTextMappingTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputTextMappingTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }

    if (regexExpr.value == "") {
        error = "Regular expression field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = regexExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    regexExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputJsonProperty() {
    var propName = document.getElementById("inputPropertyName");
    var propDefault = document.getElementById("inputPropertyDefault");
    var jsonpathExpr = document.getElementById("inputPropertyValue");
    var propertyTable = document.getElementById("inputJsonpathExprTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputJsonpathExprTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }

    if (jsonpathExpr.value == "") {
        error = "JSONPath field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = jsonpathExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    jsonpathExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputWso2EventProperty(dataType) {
    var propertyName = document.getElementById("input" + dataType + "DataPropName");
    var propertyValueOf = document.getElementById("input" + dataType + "DataPropValueOf");
    var propertyType = document.getElementById("input" + dataType + "DataPropType");
    var propertyTable = document.getElementById("input" + dataType + "DataTable");
    var noPropertyDiv = document.getElementById("noInput" + dataType + "Data");

    var error = "";

    if (propertyName.value == "") {
        error = "Name field is empty.\n";
    }

    if (propertyValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = propertyTable.insertRow(propertyTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propertyName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propertyValueOf.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propertyName.value = "";
    propertyValueOf.value = "";
    noPropertyDiv.style.display = "none";

}

function clearInputFields() {
    document.getElementById("queryName").value = "";
    document.getElementById("newTopic").value = "";
    document.getElementById("xmlSourceText").value = "";
    document.getElementById("textSourceText").value = "";
    document.getElementById("querySource").value = "";

    clearDataInTable("inputMetaDataTable");
    clearDataInTable("inputCorrelationDataTable");
    clearDataInTable("inputPayloadDataTable");

    document.getElementById("noInputMetaData").style.display = "";
    document.getElementById("noInputCorrelationData").style.display = "";
    document.getElementById("noInputPayloadData").style.display = "";
}

function removeInputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToRemove = rowToRemove.cells[0].innerHTML.trim();
    removePropertyFromSession(propertyToRemove, format, 'input');
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Input Property removed successfully!!");
}

function removePropertyFromSession(property, format, type) {
    var callback =
    {
        success: function (o) {
            if (o.responseText !== undefined) {
            }
        },
        failure: function (o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "cep_delete_property.jsp", callback, "property=" + property + "&type=" + type + "&format=" + format);

}

function goBack() {
    var callback =
    {
        success: function (o) {
            location.href = 'cep_buckets.jsp';
            if (o.responseText !== undefined) {
            }
        },
        failure: function (o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "cep_clear_property_sessions_ajaxprocessor.jsp", callback, "");

}

/**
 * Utils
 */
function clearDataInTable(tableName) {
    deleteTableRows(tableName, true);
}

function deleteTableRows(tl, keepHeader) {
    if (typeof(tl) != "object") {
        tl = document.getElementById(tl);

    }
    //debugger;
    for (var i = tl.rows.length; tl.rows.length > 0; i--) {
        if (tl.rows.length > 1) {
            tl.deleteRow(tl.rows.length - 1);
        }
        if (tl.rows.length == 1) {
            if (!keepHeader) {
                tl.deleteRow(0);
            }
            return;
        }
    }

}

function setEventClass() {
    var inputEventClass = document.getElementById("eventClassName");
    var selectedType = inputEventClass[inputEventClass.selectedIndex].value;
    populateElementDisplay(document.getElementsByName("inputEventClass"), "none");
    if (selectedType == "Class") {
        populateElementDisplay(document.getElementsByName("inputEventClass"), "");
    }
}

function setInputMapping() {
    var inputMappingElement = document.getElementById("inputMapping");
    var selectedType = inputMappingElement[inputMappingElement.selectedIndex].value;
    populateElementDisplay(document.getElementsByName("inputXMLMapping"), "none");
    populateElementDisplay(document.getElementsByName("inputTextMapping"), "none");
    populateElementDisplay(document.getElementsByName("inputElementMapping"), "none");
    populateElementDisplay(document.getElementsByName("inputTupleMapping"), "none");
    populateElementDisplay(document.getElementsByName("inputMapMapping"), "none");
    if (selectedType == "xml") {
        populateElementDisplay(document.getElementsByName("inputXMLMapping"), "");
    } else if (selectedType == "text") {
        populateElementDisplay(document.getElementsByName("inputTextMapping"), "");
    } else if (selectedType == "element") {
        populateElementDisplay(document.getElementsByName("inputElementMapping"), "");
    } else if (selectedType == "tuple") {
        populateElementDisplay(document.getElementsByName("inputTupleMapping"), "");
    } else if (selectedType == "map") {
        populateElementDisplay(document.getElementsByName("inputMapMapping"), "");
    }
}

function populateElementDisplay(elements, display) {
    for (var i = 0; i < elements.length; i++) {
        elements[i].style.display = display;
    }
}

