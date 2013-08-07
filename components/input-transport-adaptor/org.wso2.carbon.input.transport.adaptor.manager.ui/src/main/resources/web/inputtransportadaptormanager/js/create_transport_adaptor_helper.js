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


// this function validate required fields if they are required fields,
// other fields are ignored.In addition to that, the values from each
// field is taken and appended to a string.
// string = propertyName + $ + propertyValue + | +propertyName...

function addTransport(form) {

        var isFieldEmpty = false;
        var inputParameterString = "";
        var inputPropertyCount = 0;

    // all input properties, not required and required are checked
        while (document.getElementById("inputProperty_Required_" + inputPropertyCount) != null ||
        document.getElementById("inputProperty_" + inputPropertyCount) != null) {
    // if required fields are empty
        if ((document.getElementById("inputProperty_Required_" + inputPropertyCount) != null) ) {
        if (document.getElementById("inputProperty_Required_" + inputPropertyCount).value.trim() == "") {
    // values are empty in fields
        isFieldEmpty = true;
        inputParameterString = "";
        break;
        }
else {
    // values are stored in parameter string to send to backend
        var propertyValue = document.getElementById("inputProperty_Required_" + inputPropertyCount).value.trim();
        var propertyName = document.getElementById("inputProperty_Required_" + inputPropertyCount).name;
        inputParameterString = inputParameterString + propertyName + "$" + propertyValue + "|";

        }
} else if (document.getElementById("inputProperty_" + inputPropertyCount) != null) {
        var notRequriedPropertyValue = document.getElementById("inputProperty_" + inputPropertyCount).value.trim();
        var notRequiredPropertyName = document.getElementById("inputProperty_" + inputPropertyCount).name;
        if (notRequriedPropertyValue == "") {
        notRequriedPropertyValue = "  ";
        }
inputParameterString = inputParameterString + notRequiredPropertyName + "$" + notRequriedPropertyValue + "|";


}
inputPropertyCount++;
}

var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
// Check for white space
if (!reWhiteSpace.test(document.getElementById("transportNameId").value)) {
        CARBON.showErrorDialog("White spaces are not allowed in transport adaptor name.");
        return;
        }
if (isFieldEmpty || (document.getElementById("transportNameId").value.trim() == "")) {
    // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
        }
else {
    // create parameter string
        var selectedIndex = document.getElementById("transportTypeFilter").selectedIndex;
        var selected_text = document.getElementById("transportTypeFilter").options[selectedIndex].text;

        var parameters = "?transportName=" + (document.getElementById("transportNameId").value.trim())
        + "&transportType=" + selected_text;

        if (inputParameterString != "") {
        parameters = parameters + "&inputPropertySet=" + inputParameterString;
        }

// ajax call for creating a transport adaptor at backend, needed parameters are appended.
    jQuery.ajax({
        type:"POST",
        url:"../inputtransportadaptormanager/add_transport_ajaxprocessor.jsp" + parameters,
        contentType:"application/json; charset=utf-8",
        dataType:"text",
        data:{},
async:false,
success:function (msg) {
        if (msg.trim() == "true") {
        form.submit();
        } else {
        CARBON.showErrorDialog("Failed to add transport adaptor, Exception: " + msg);
        }
}
});

}

}

function clearTextIn(obj) {
        if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
        YAHOO.util.Dom.removeClass(obj, 'initE');
        YAHOO.util.Dom.addClass(obj, 'normalE');
        textValue = obj.value;
        obj.value = "";
        }
}
function fillTextIn(obj) {
        if (obj.value == "") {
        obj.value = textValue;
        if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
        YAHOO.util.Dom.removeClass(obj, 'normalE');
        YAHOO.util.Dom.addClass(obj, 'initE');
        }
}
}

// transport adaptor properties are taken from back-end and render according to fields
function showTransportProperties(propertiesHeader) {

        var transportInputTable = document.getElementById("transportInputTable");
//    jQuery('input[name=transportTypeFilter]').val()
        var selectedIndex = document.getElementById("transportTypeFilter").selectedIndex;
        var selected_text = document.getElementById("transportTypeFilter").options[selectedIndex].text;

    // delete all rows except first two; transport name, transport type
        for (i = transportInputTable.rows.length - 1; i > 1; i--) {
        transportInputTable.deleteRow(i);
        }

    jQuery.ajax({
        type:"POST",
        url:"../inputtransportadaptormanager/get_properties_ajaxprocessor.jsp?transportType=" + selected_text + "",
        data:{},
contentType:"application/json; charset=utf-8",
dataType:"text",
async:false,
success:function (msg) {
        if (msg != null) {

        var jsonObject = JSON.parse(msg);
        var inputTransportProperties = jsonObject.localInputTransportAdaptorPropertyDtos;

        var tableRow = transportInputTable.insertRow(transportInputTable.rows.length);
        if (inputTransportProperties != undefined) {
        var transportInputPropertyLoop = 0;
        var inputProperty = "inputProperty_";
        var inputRequiredProperty = "inputProperty_Required_";
        var inputOptionProperty = "inputOptionProperty";

        tableRow.innerHTML = '<td colspan="2" ><b>'+propertiesHeader+'</b></td> ';

            jQuery.each(inputTransportProperties, function (index,
        inputTransportProperty) {

        loadTransportProperties('input', inputTransportProperty, transportInputTable, transportInputPropertyLoop, inputProperty, inputRequiredProperty, 'inputFields')
        transportInputPropertyLoop = transportInputPropertyLoop + 1;

        });
}
}
}
});
}

function loadTransportProperties(propertyType, transportProperty, transportInputTable,
transportPropertyLoop, propertyValue, requiredValue, classType
) {

        var property = transportProperty.localDisplayName.trim();
        var tableRow = transportInputTable.insertRow(transportInputTable.rows.length);
        var textLabel = tableRow.insertCell(0);
        var displayName = transportProperty.localDisplayName.trim();
        textLabel.innerHTML = displayName;
        var requiredElementId = propertyValue;
        var textPasswordType = "text";
        var hint = ""
        var defaultValue = "";

        if (transportProperty.localRequired) {
        textLabel.innerHTML = displayName + '<span class="required">*</span>';
        requiredElementId = requiredValue;
        }

if (transportProperty.localSecured) {
        textPasswordType = "password";
        }

if (transportProperty.localHint != "") {
        hint = transportProperty.localHint;
        }

if (transportProperty.localDefaultValue != undefined && transportProperty.localDefaultValue != "") {
        defaultValue = transportProperty.localDefaultValue;
        }


var inputField = tableRow.insertCell(1);

if (transportProperty.localOptions == '') {

        if (hint != undefined) {
        inputField.innerHTML = '<div class="' + classType + '"> <input style="width:50%" type="' + textPasswordType + '" id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
else {
        inputField.innerHTML = '<div class="' + classType + '"> <input style="width:50%" type="' + textPasswordType + '" id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> </div>';
        }
}

else {

        var option = '';
    jQuery.each(transportProperty.localOptions, function (index, localOption) {
        if (localOption == transportProperty.localDefaultValue) {
        option = option + '<option selected=selected>' + localOption + '</option>';
        }
else {
        option = option + '<option>' + localOption + '</option>';
        }

});


if (hint != undefined) {
        inputField.innerHTML = '<div class="' + classType + '"> <select  id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" />' + option + ' <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
else {
        inputField.innerHTML = '<div class="' + classType + '"> <select  id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" />' + option + ' </div>';
        }
}
}
