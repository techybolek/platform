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

// transport adaptor msg config properties are taken from back-end and rendered according to fields
function showMessageConfigProperties() {
    var eventBuilderInputTable = document.getElementById("eventBuilderInputTable");
    var taSelect = document.getElementById("transportAdaptorNameSelect");
    var selectedIndex = taSelect.selectedIndex;
    var selected_text = taSelect.options[selectedIndex].text;
    var insertIndex = YAHOO.util.Dom.getAncestorByTagName(taSelect, 'tr').rowIndex + 1;

    var inputPropertyIdPrefix = "msgConfigProperty_";
    var requiredInputPropertyIdPrefix = "msgConfigProperty_Required_";

    jQuery.ajax({
                    type: "POST",
                    url: "../eventbuilder/get_mappings_ajaxprocessor.jsp?transportAdaptorName=" + selected_text + "",
                    data: {},
                    contentType: "application/json; charset=utf-8",
                    dataType: "text",
                    async: false,
                    success: function (mappingTypes) {

                        if (mappingTypes != null) {
                            mappingTypes = mappingTypes.trim();
                            var mappings = JSON.parse(mappingTypes);
                            var inputMappingSelect = document.getElementById('inputMappingTypeSelect');
                            inputMappingSelect.length = 0;
                            // for each property, add a text and input field in a row
                            for (i = 0; i < mappings.length; i++) {
                                var mappingName = mappings[i];
                                if (mappingName != undefined && mappingName != "") {
                                    mappingName = mappingName.trim();
                                    inputMappingSelect.add(new Option(mappingName, mappingName), null);
                                }
                            }

                        }
                    }
                });


// delete all msg config property rows
    for (i = eventBuilderInputTable.rows.length - 1; i > 1; i--) {
        var tableRow = eventBuilderInputTable.rows[i];
        var inputCell = tableRow.cells[1];
        if (inputCell != undefined) {
            var cellElements = inputCell.getElementsByTagName('input');
            for (var j = 0; j < cellElements.length; j++) {
                if (cellElements[j].id.substring(0, inputPropertyIdPrefix.length) == inputPropertyIdPrefix) {
                    eventBuilderInputTable.deleteRow(i);
                    break;
                }
            }
        }
    }


    jQuery.ajax({
                    type: "POST",
                    url: "../eventbuilder/get_properties_ajaxprocessor.jsp?transportName=" + selected_text + "",
                    data: {},
                    contentType: "application/json; charset=utf-8",
                    dataType: "text",
                    async: false,
                    success: function (msg) {
                        if (msg != null) {
                            var inputTransportProperties = JSON.parse(msg);
                            if (inputTransportProperties != '') {
                                var propertyIndex = 0;

                                jQuery.each(inputTransportProperties, function (index,
                                                                                messageProperty) {
                                    loadTransportMessageProperty(messageProperty, eventBuilderInputTable, propertyIndex, insertIndex, inputPropertyIdPrefix, requiredInputPropertyIdPrefix);
                                    propertyIndex = propertyIndex + 1;
                                    insertIndex = insertIndex + 1;
                                });
                            }
                            loadMappingUiElements();
                        }
                    }
                });
}

function clearInputPropertyTable() {
    var inputPropertyTable = document.getElementById("wso2EventMappingPropertyTable");
    var noPropDiv = document.getElementById("noInputProperty");
    clearDataInTable(inputPropertyTable.getName());
    noPropDiv.style.display = "block";

}

function loadMappingUiElements() {
    var taSelect = document.getElementById("inputMappingTypeSelect");
    var inputMappingType = taSelect.options[taSelect.selectedIndex].text;
    var mappingUiTd = document.getElementById("mappingUiTd");
    mappingUiTd.innerHTML = "";

    jQuery.ajax({
                    type: "POST",
                    url: "../eventbuilder/get_mapping_ui_ajaxprocessor.jsp?mappingType=" + inputMappingType + "",
                    data: {},
                    contentType: "text/html; charset=utf-8",
                    dataType: "text",
                    success: function (ui_content) {
                        if (ui_content != null) {
                            mappingUiTd.innerHTML = ui_content;
                        }
                    }
                });

}

function loadTransportMessageProperty(messageProperty, eventBuilderInputTable, propertyIndex,
                                      insertIndex, optionalPropertyIdPrefix,
                                      requiredPropertyIdPrefix) {
    var tableRow = eventBuilderInputTable.insertRow(insertIndex);
    var textLabel = tableRow.insertCell(0);
    var displayName = messageProperty.localDisplayName.trim();
    textLabel.innerHTML = displayName;
    var elementIdPrefix = optionalPropertyIdPrefix;
    var inputElementType = "text";
    var hint = "";
    var defaultValue = "";
    var htmlForHint = "";

    if (messageProperty.localRequired) {
        textLabel.innerHTML = displayName + '<span class="required">*</span>';
        elementIdPrefix = requiredPropertyIdPrefix;
    }

    if (messageProperty.localSecured) {
        inputElementType = "password";
    }

    if (messageProperty.localHint != undefined && messageProperty.localHint != "") {
        hint = messageProperty.localHint;
        htmlForHint = '<div class="sectionHelp">' + hint + '</div>'
    }

    if (messageProperty.localDefaultValue != undefined && messageProperty.localDefaultValue != "") {
        defaultValue = messageProperty.localDefaultValue;
    }

    var inputField = tableRow.insertCell(1);
    inputField.innerHTML = '<input style="width:75%" type="' + inputElementType + '" id="' + elementIdPrefix + propertyIndex + '" name="' + messageProperty.localKey + '" value="' + defaultValue + '" class="initE" /> <br/> ' + htmlForHint;
}



