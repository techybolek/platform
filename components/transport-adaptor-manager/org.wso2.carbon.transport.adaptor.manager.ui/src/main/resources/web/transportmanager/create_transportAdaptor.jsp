<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources">

<carbon:breadcrumb
        label="transportmanager.add"
        resourceBundle="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="eventing.js"></script>
<script type="text/javascript" src="js/subscriptions.js"></script>
<script type="text/javascript" src="js/eventing_utils.js"></script>

<script language="javascript">
// this function validate required fields if they are required fields,
// other fields are ignored.In addition to that, the values from each
// field is taken and appended to a string.
// string = propertyName + $ + propertyValue + | +propertyName...

function addTransport(form) {

    var commonPropertyCount = 0;
    var isFieldEmpty = false;
    var commonParameterString = "";
    var inputParameterString = "";
    var inputPropertyCount = 0;
    var outputParameterString = "";
    var outputPropertyCount = 0;


    // all common properties, not required and required are checked
    while (document.getElementById("commonProperty_Required_" + commonPropertyCount) != null ||
           document.getElementById("commonProperty_" + commonPropertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("commonProperty_Required_" + commonPropertyCount) != null) {
            if (document.getElementById("commonProperty_Required_" + commonPropertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                commonParameterString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("commonProperty_Required_" + commonPropertyCount).value.trim();
                var propertyName = document.getElementById("commonProperty_Required_" + commonPropertyCount).name;
                commonParameterString = commonParameterString + propertyName + "$" + propertyValue + "|";

            }
        } else if (document.getElementById("commonProperty_" + commonPropertyCount) != null) {
            var notRequriedPropertyValue = document.getElementById("commonProperty_" + commonPropertyCount).value.trim();
            var notRequiredPropertyName = document.getElementById("commonProperty_" + commonPropertyCount).name;
            if (notRequriedPropertyValue == "") {
                notRequriedPropertyValue = "  ";
            }
            commonParameterString = commonParameterString + notRequiredPropertyName + "$" + notRequriedPropertyValue + "|";


        }
        commonPropertyCount++;
    }


    // all input properties, not required and required are checked
    while (document.getElementById("inputProperty_Required_" + inputPropertyCount) != null ||
           document.getElementById("inputProperty_" + inputPropertyCount) != null) {
        // if required fields are empty
        if ((document.getElementById("inputProperty_Required_" + inputPropertyCount) != null) && ((document.getElementById("inputCheckbox")).checked)) {
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


    // all output properties, not required and required are checked
    while (document.getElementById("outputProperty_Required_" + outputPropertyCount) != null ||
           document.getElementById("outputProperty_" + outputPropertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("outputProperty_Required_" + outputPropertyCount) != null) {
            if ((document.getElementById("outputProperty_Required_" + outputPropertyCount).value.trim() == "") && ((document.getElementById("outputCheckbox")).checked)) {
                // values are empty in fields
                isFieldEmpty = true;
                outputParameterString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("outputProperty_Required_" + outputPropertyCount).value.trim();
                var propertyName = document.getElementById("outputProperty_Required_" + outputPropertyCount).name;
                outputParameterString = outputParameterString + propertyName + "$" + propertyValue + "|";

            }
        } else if (document.getElementById("outputProperty_" + outputPropertyCount) != null) {
            var notRequriedPropertyValue = document.getElementById("outputProperty_" + outputPropertyCount).value.trim();
            var notRequiredPropertyName = document.getElementById("outputProperty_" + outputPropertyCount).name;
            if (notRequriedPropertyValue == "") {
                notRequriedPropertyValue = "  ";
            }
            outputParameterString = outputParameterString + notRequiredPropertyName + "$" + notRequriedPropertyValue + "|";


        }
        outputPropertyCount++;
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
    } else {
        // create parameter string
        var selectedIndex = document.inputForm.transportTypeFilter.selectedIndex;
        var selected_text = document.inputForm.transportTypeFilter.options[selectedIndex].text;
        var parameters = "?transportName=" + (document.getElementById("transportNameId").value.trim())
                                 + "&transportType=" + selected_text;
        if (commonParameterString != "") {
            parameters = parameters + "&commonPropertySet=" + commonParameterString;
        }

        if (inputParameterString != "") {
            parameters = parameters + "&inputPropertySet=" + inputParameterString;
        }

        if (outputParameterString != "") {
            parameters = parameters + "&outputPropertySet=" + outputParameterString;
        }

        // ajax call for creating a transport adaptor at backend, needed parameters are appended.
        $.ajax({
                   type:"POST",
                   url:"add_transport_ajaxprocessor.jsp" + parameters,
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
function showTransportProperties() {

    var transportInputTable = document.getElementById("transportInputTable");
    var selectedIndex = document.inputForm.transportTypeFilter.selectedIndex;
    var selected_text = document.inputForm.transportTypeFilter.options[selectedIndex].text;

    // delete all rows except first two; transport name, transport type
    for (i = transportInputTable.rows.length - 1; i > 1; i--) {
        transportInputTable.deleteRow(i);
    }

    $.ajax({
               type:"POST",
               url:"get_properties_ajaxprocessor.jsp?transportType=" + selected_text + "",
               data:{},
               contentType:"application/json; charset=utf-8",
               dataType:"text",
               async:false,
               success:function (msg) {
                   if (msg != null) {

                       var jsonObject = JSON.parse(msg);

                       var commonTransportProperties = jsonObject.localCommonTransportAdaptorPropertyDtos;
                       var inputTransportProperties = jsonObject.localInputTransportAdaptorPropertyDtos;
                       var outputTransportProperties = jsonObject.localOutputTransportAdaptorPropertyDtos;
                       var supportedTransportAdaptorType = jsonObject.localSupportedTransportAdaptorType;


                       var tableRow = transportInputTable.insertRow(transportInputTable.rows.length);
                       if (supportedTransportAdaptorType == 'inout' && commonTransportProperties != undefined) {
                           tableRow.innerHTML = '<td colspan="2" ><b><fmt:message key='transport.common.tooltip'/></b></td> ';
                       }
                       else if ((inputTransportProperties != undefined) || (outputTransportProperties != undefined || (commonTransportProperties != undefined))) {
                           tableRow.innerHTML = '<td colspan="2" ><b><fmt:message key='transport.all.properties.tooltip'/></b></td> ';
                       }

                       if (commonTransportProperties != undefined) {
                           var transportCommonPropertyLoop = 0;
                           var commonProperty = "commonProperty_";
                           var commonRequiredProperty = "commonProperty_Required_";


                           $.each(commonTransportProperties, function (index,
                                                                       commonTransportProperty) {
                               loadTransportProperties('common', commonTransportProperty, transportInputTable, transportCommonPropertyLoop, commonProperty, commonRequiredProperty, 'commonFields')
                               transportCommonPropertyLoop = transportCommonPropertyLoop + 1;

                           });
                       }

                       if (supportedTransportAdaptorType == 'inout') {
                           var tableRow = transportInputTable.insertRow(transportInputTable.rows.length);
                           tableRow.innerHTML = '<td colspan="2" class="middle-header"><fmt:message key='transport.input.tooltip'/> <input type="checkbox" id="inputCheckbox" onclick="enableMyInput(this)" align="bottom"/></td> ';

                       }

                       if (inputTransportProperties != undefined) {
                           var transportInputPropertyLoop = 0;
                           var inputProperty = "inputProperty_";
                           var inputRequiredProperty = "inputProperty_Required_";

                           if (supportedTransportAdaptorType == 'inout') {
                               var tableRowHeading = transportInputTable.insertRow(transportInputTable.rows.length);
                               tableRowHeading.innerHTML = '<td colspan="2"><b><fmt:message key='transport.input.properties.tooltip'/></b></td> ';
                           }

                           $.each(inputTransportProperties, function (index,
                                                                      inputTransportProperty) {
                               loadTransportProperties('input', inputTransportProperty, transportInputTable, transportInputPropertyLoop, inputProperty, inputRequiredProperty, 'inputFields')
                               transportInputPropertyLoop = transportInputPropertyLoop + 1;


                           });
                       }

                       if (supportedTransportAdaptorType == 'inout') {
                           var tableRow = transportInputTable.insertRow(transportInputTable.rows.length);
                           tableRow.innerHTML = '<td colspan="2" class="middle-header"><fmt:message key='transport.output.tooltip'/>  <input type="checkbox" id="outputCheckbox" onclick="enableMyInput(this)" align="bottom"/> </td>';

                       }

                       if (outputTransportProperties != undefined) {
                           var transportOutputPropertyLoop = 0;
                           var outputProperty = "outputProperty_";
                           var outputRequiredProperty = "outputProperty_Required_";

                           if (supportedTransportAdaptorType == 'inout') {
                               var tableRowHeading = transportInputTable.insertRow(transportInputTable.rows.length);
                               tableRowHeading.innerHTML = '<td colspan="2"><b><fmt:message key='transport.output.properties.tooltip'/></b></td> ';
                           }
                           $.each(outputTransportProperties, function (index,
                                                                       outputTransportProperty) {
                               loadTransportProperties('output', outputTransportProperty, transportInputTable, transportOutputPropertyLoop, outputProperty, outputRequiredProperty, 'outputFields')
                               transportOutputPropertyLoop = transportOutputPropertyLoop + 1;

                           });
                       }
                   }
               }
           });
}

function loadTransportProperties(propertyType, transportProperty, transportInputTable,
                                 transportPropertyLoop, propertyValue, requiredValue, classType) {

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

    if (propertyType != 'common') {

        if (hint != undefined) {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:50%" type="' + textPasswordType + '"disabled=disabled"' + '" id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
        else {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:50%" type="' + textPasswordType + '"disabled=disabled"' + '" id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> </div>';
        }
    }
    else {
        if (hint != undefined) {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:50%" type="' + textPasswordType + '" id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
        else {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:50%" type="' + textPasswordType + '" id="' + requiredElementId + transportPropertyLoop + '" name="' + transportProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> </div>';
        }

    }

}


function enableMyInput(obj) {

    if (jQuery(obj).attr('id') == "outputCheckbox") {
        if ((jQuery(obj).is(':checked'))) {
            jQuery('.outputFields').find('input').attr('disabled', false)
        }
        else {
            jQuery('.outputFields').find('input', 'text').val("")
            jQuery('.outputFields').find('input').attr('disabled', 'disabled')
        }
    }

    else if (jQuery(obj).attr('id') == "inputCheckbox") {
        if ((jQuery(obj).is(':checked'))) {
            jQuery('.inputFields').find('input').attr('disabled', false)
        }
        else {
            jQuery('.inputFields').find('input', 'text').val("")
            jQuery('.inputFields').find('input').attr('disabled', 'disabled')
        }
    }

}


</script>

<div id="middle">
<div id="workArea">
<h3>Create a New Transport Adaptor</h3>

<form name="inputForm" action="index.jsp" method="get" id="addTransport">
<table style="width:100%" id="transportAdd" class="styledLeft">
<thead>
<tr>
    <th>Enter Transport Adaptor Details</th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRaw">
<table id="transportInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med">Transport Name<span
            class="required">*</span>
    </td>
    <td><input type="text" name="transportName" id="transportNameId"
               class="initE"
               onclick="clearTextIn(this)" onblur="fillTextIn(this)"
               value=""
               style="width:50%"/>

        <div class="sectionHelp">
            Please Enter the Transport Adaptor Name.
        </div>

    </td>
</tr>
<tr>
    <td>Transport Adaptor Type<span class="required">*</span></td>
    <td><select name="transportTypeFilter"
                onchange="showTransportProperties()">
        <%
            TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);
            String[] transportNames = stub.getTransportAdaptorNames();
            TransportAdaptorPropertiesDto transportAdaptorPropertiesDto = null;
            String firstTransportName = null;
            String supportedTransportAdaptorType = null;
            if (transportNames != null) {
                firstTransportName = transportNames[0];
                transportAdaptorPropertiesDto = stub.getAllTransportAdaptorPropertiesDto(firstTransportName);
                supportedTransportAdaptorType = transportAdaptorPropertiesDto.getSupportedTransportAdaptorType();
                for (String type : transportNames) {
        %>
        <option><%=type%>
        </option>
        <%
                }
            }
        %>
    </select>

        <div class="sectionHelp">
            Please Select the Transport Adaptor Type.
        </div>
    </td>

</tr>

<%
    if (supportedTransportAdaptorType.equals("inout") && (transportAdaptorPropertiesDto.getCommonTransportAdaptorPropertyDtos()) != null) {

%>
<tr>
    <td colspan="2"><b>
        <fmt:message key="transport.common.tooltip"/> </b>
    </td>
</tr>

<%

} else if ((transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos()) != null || (transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos()) != null || (transportAdaptorPropertiesDto.getCommonTransportAdaptorPropertyDtos()) != null) {

%>
<tr>
    <td colspan="2"><b>
        <fmt:message key="transport.all.properties.tooltip"/> </b>
    </td>
</tr>

<%
    }

    if ((transportAdaptorPropertiesDto.getCommonTransportAdaptorPropertyDtos()) != null & firstTransportName != null) {
        //Input fields for common transport adaptor properties

        TransportAdaptorPropertyDto[] commonTransportProperties = transportAdaptorPropertiesDto.getCommonTransportAdaptorPropertyDtos();

        //Need to add other types of properties also here

        if (commonTransportProperties != null) {
            for (int index = 0; index < commonTransportProperties.length; index++) {
%>
<tr>
    <td class="leftCol-med">
        <%=commonTransportProperties[index].getDisplayName()%>
        <%
            String propertyId = "commonProperty_";
            if (commonTransportProperties[index].getRequired()) {
                propertyId = "commonProperty_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>

    </td>
    <%
        String type = "text";
        if (commonTransportProperties[index].getSecured()) {
            type = "password";
        }
    %>
    <td>
        <div class=commonFields><input type="<%=type%>"
                                       name="<%=commonTransportProperties[index].getKey()%>"
                                       id="<%=propertyId%><%=index%>" class="initE"
                                       style="width:50%"
                                       value="<%= (commonTransportProperties[index].getDefaultValue()) != null ? commonTransportProperties[index].getDefaultValue() : "" %>"
                />
            <%
                if (commonTransportProperties[index].getHint() != null) { %>
            <div class="sectionHelp">
                <%=commonTransportProperties[index].getHint()%>
            </div>
            <% } %>
        </div>
    </td>

</tr>
<%
            }
        }
    }

    if (supportedTransportAdaptorType.equals("inout")) {
%>

<tr>
    <td colspan="2" class="middle-header">
        <fmt:message key="transport.input.tooltip"/>
        <input type="checkbox" id="inputCheckbox" onclick="enableMyInput(this)" align="bottom"/>
    </td>
</tr>


<%

    }
    //Input fields for input transport adaptor properties
    if ((transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos()) != null & firstTransportName != null) {
        if (supportedTransportAdaptorType.equals("inout")) {
%>


<tr>
    <td colspan="2"><b>
        <fmt:message key="transport.input.properties.tooltip"/></b>
    </td>
</tr>

<% }
    TransportAdaptorPropertyDto[] inputTransportProperties = transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos();

    if (inputTransportProperties != null) {
        for (int index = 0; index < inputTransportProperties.length; index++) {
%>
<tr>

    <td class="leftCol-med"><%=inputTransportProperties[index].getDisplayName()%>
        <%
            String propertyId = "inputProperty_";
            if (inputTransportProperties[index].getRequired()) {
                propertyId = "inputProperty_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (inputTransportProperties[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=inputFields><input type="<%=type%>"
                                      name="<%=inputTransportProperties[index].getKey()%>"
                                      id="<%=propertyId%><%=index%>" class="initE"
                                      style="width:50%"
                                      value="<%= (inputTransportProperties[index].getDefaultValue()) != null ? inputTransportProperties[index].getDefaultValue() : "" %>"
                                      disabled="disabled"/>
            <%
                if (inputTransportProperties[index].getHint() != null) { %>
            <div class="sectionHelp">
                <%=inputTransportProperties[index].getHint()%>
            </div>
            <% } %>
        </div>
    </td>

</tr>
<%
            }
        }
    }
    if (supportedTransportAdaptorType.equals("inout")) {

%>
<tr>
    <td colspan="2" class="middle-header">
        <fmt:message key="transport.output.tooltip"/>
        <input type="checkbox" id="outputCheckbox" onclick="enableMyInput(this)" align="bottom"/>
    </td>
</tr>


<% }
    if ((transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos()) != null & firstTransportName != null) {
        if (supportedTransportAdaptorType.equals("inout")) {
%>

<tr>
    <td colspan="2"><b>
        <fmt:message key="transport.output.properties.tooltip"/> </b>
    </td>
</tr>
<%
    }
    //Input fields for output transport adaptor properties

    TransportAdaptorPropertyDto[] outTransportProperties = transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos();

    //Need to add other types of properties also here

    if (outTransportProperties != null) {
        for (int index = 0; index < outTransportProperties.length; index++) {
%>
<tr>


    <td class="leftCol-med"><%=outTransportProperties[index].getDisplayName()%>
        <%
            String propertyId = "outputProperty_";
            if (outTransportProperties[index].getRequired()) {
                propertyId = "outputProperty_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (outTransportProperties[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=outputFields><input type="<%=type%>"
                                       name="<%=outTransportProperties[index].getKey()%>"
                                       id="<%=propertyId%><%=index%>" class="initE"
                                       style="width:50%"
                                       value="<%= (outTransportProperties[index].getDefaultValue()) != null ? outTransportProperties[index].getDefaultValue() : "" %>"
                                       disabled="disabled"/>
            <%
                if (outTransportProperties[index].getHint() != null) { %>
            <div class="sectionHelp">
                <%=outTransportProperties[index].getHint()%>
            </div>
            <% } %>
        </div>
    </td>

</tr>
<%
            }
        }
    }

%>

</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="Add Transport Adaptor"
               onclick="addTransport(document.getElementById('addTransport'))"/>
    </td>
</tr>
</tbody>
</table>


</form>
</div>
</div>
</fmt:bundle>
