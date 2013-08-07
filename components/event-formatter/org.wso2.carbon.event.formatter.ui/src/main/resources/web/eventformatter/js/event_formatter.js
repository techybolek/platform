//Method that used in jsp files

function addOutputWSO2EventProperty(dataType) {
    var propName = document.getElementById("output" + dataType + "DataPropName");
    var propValueOf = document.getElementById("output" + dataType + "DataPropValueOf");
    var propType = document.getElementById("output" + dataType + "DataPropType");
    var propertyTable = document.getElementById("output" + dataType + "DataTable");
    var noPropertyDiv = document.getElementById("noOutput" + dataType + "Data");

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


    var newTableRow = propertyTable.insertRow(propertyTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propName.value;

    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propValueOf.value;

    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propType.value;

    YAHOO.util.Dom.addClass(newCell2, "property-names");

    var newCel3 = newTableRow.insertCell(3);
    newCel3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeOutputProperty(this,\'' + dataType + '\')">Delete</a>';

    YAHOO.util.Dom.addClass(newCel3, "property-names");

    propName.value = "";
    propValueOf.value = "";
    noPropertyDiv.style.display = "none";

}

function getWSO2EventDataValues(dataTable) {

    var wso2EventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].textContent;
        var column1 = row.cells[1].textContent;
        var column2 = row.cells[2].textContent;

        wso2EventData = wso2EventData + column0 + "^" + column1 + "^" + column2 + "$";
    }
    return wso2EventData;
}

function getMapDataValues(dataTable) {

    var mapEventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].textContent;
        var column1 = row.cells[1].textContent;

        mapEventData = mapEventData + column0 + "^" + column1 + "^" + "$";
    }
    return mapEventData;
}

function addEventFormatter(form) {

    var isFieldEmpty = false;
    var payloadString = "";
    var correlationString = "";
    var metaString = "";
    var inline = "inline";
    var registry = "registry";
    var dataFrom = "";

    var eventFormatterName = document.getElementById("eventFormatterId").value.trim();
    var streamNameWithVersion = document.getElementById("streamNameFilter")[document.getElementById("streamNameFilter").selectedIndex].text;
    var transportAdaptorName = document.getElementById("transportAdaptorNameFilter")[document.getElementById("transportAdaptorNameFilter").selectedIndex].text;


    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventFormatterName)) {
        CARBON.showErrorDialog("White spaces are not allowed in event formatter name.");
        return;
    }
    if (isFieldEmpty || (eventFormatterName == "")) {
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
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'wso2event') {

        var parameters = "?eventFormatter=" + eventFormatterName + "&streamNameWithVersion=" + streamNameWithVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=wso2event" + "&outputParameters=" + outputPropertyParameterString;

        var metaData = "";
        var correlationData = "";
        var payloadData = "";

        var metaDataTable = document.getElementById("outputMetaDataTable");
        if (metaDataTable.rows.length > 1) {
            metaData = getWSO2EventDataValues(metaDataTable);
            parameters = parameters + "&metaData=" + metaData;
        }
        var correlationDataTable = document.getElementById("outputCorrelationDataTable");
        if (correlationDataTable.rows.length > 1) {
            correlationData = getWSO2EventDataValues(correlationDataTable);
            parameters = parameters + "&correlationData=" + correlationData;
        }
        var payloadDataTable = document.getElementById("outputPayloadDataTable");
        if (payloadDataTable.rows.length > 1) {
            payloadData = getWSO2EventDataValues(payloadDataTable);
            parameters = parameters + "&payloadData=" + payloadData;
        }

        if (metaData == "" && correlationData == "" && payloadData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'text') {

        var parameters = "?eventFormatter=" + eventFormatterName + "&streamNameWithVersion=" + streamNameWithVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=text" + "&outputParameters=" + outputPropertyParameterString;

        if ((document.getElementById("inline_text")).checked) {
            var textData = document.getElementById("textSourceText").value;
            parameters = parameters + "&textData=" + textData;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_text")).checked) {
            var textData = document.getElementById("textSourceRegistry").value;
            parameters = parameters + "&textData=" + textData;
            dataFrom = registry;
        }

        parameters = parameters + "&dataFrom=" + dataFrom;

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }

    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'xml') {

        var parameters = "?eventFormatter=" + eventFormatterName + "&streamNameWithVersion=" + streamNameWithVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=xml" + "&outputParameters=" + outputPropertyParameterString;

        if ((document.getElementById("inline_xml")).checked) {
            var textData = document.getElementById("xmlSourceText").value;
            parameters = parameters + "&textData=" + textData;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_xml")).checked) {
            var textData = document.getElementById("xmlSourceRegistry").value;
            parameters = parameters + "&textData=" + textData;
            dataFrom = registry;
        }
        parameters = parameters + "&dataFrom=" + dataFrom;

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }

    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'map') {

        var parameters = "?eventFormatter=" + eventFormatterName + "&streamNameWithVersion=" + streamNameWithVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=map" + "&outputParameters=" + outputPropertyParameterString;

        var mapData = "";

        var mapDataTable = document.getElementById("outputMapPropertiesTable");
        if (mapDataTable.rows.length > 1) {
            mapData = getMapDataValues(mapDataTable);
            parameters = parameters + "&mapData=" + mapData;
        }

        else {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'json') {

        var parameters = "?eventFormatter=" + eventFormatterName + "&streamNameWithVersion=" + streamNameWithVersion + "&transportAdaptorName=" + transportAdaptorName + "&mappingType=json" + "&outputParameters=" + outputPropertyParameterString;

        if ((document.getElementById("inline_json")).checked) {
            var jsonData = document.getElementById("jsonSourceText").value;
            parameters = parameters + "&jsonData=" + jsonData;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_json")).checked) {
            var jsonData = document.getElementById("jsonSourceRegistry").value;
            parameters = parameters + "&jsonData=" + jsonData;
            dataFrom = registry;
        }
        parameters = parameters + "&dataFrom=" + dataFrom;

        if (jsonData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }

    }


    jQuery.ajax({
                    type:"POST",
                    url:"../eventformatter/add_event_formatter_ajaxprocessor.jsp" + parameters,
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


function removeOutputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToERemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Output Property removed successfully!!");
    return;
}


function addOutputMapProperty() {
    var propName = document.getElementById("outputMapPropName");
    var propValueOf = document.getElementById("outputMapPropValueOf");
    var propertyTable = document.getElementById("outputMapPropertiesTable");
    var noPropertyDiv = document.getElementById("noOutputMapProperties");

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

    var newTableRow = propertyTable.insertRow(propertyTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propValueOf.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCel2 = newTableRow.insertCell(2);
    newCel2.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeOutputProperty(this,\'' + 'map' + '\')">Delete</a>';
    YAHOO.util.Dom.addClass(newCel2, "property-names");

    propName.value = "";
    propValueOf.value = "";
    noPropertyDiv.style.display = "none";

}


