<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.cep.process.flow.ui.i18n.Resources">

<link type="text/css" href="../eventformatter/css/eventFormatter.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventformatter/js/event_formatter.js"></script>
<script type="text/javascript"
        src="../eventformatter/js/create_eventFormatter_helper.js"></script>

<script type="text/javascript">
jQuery(document).ready(function () {
    showMappingContext();
});

function addCEPEventFormatter(form) {

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
                            CARBON.showConfirmationDialog(
                                    "Event Formatter " + eventFormatterName + " successfully added, Do you want to add another Event Formatter?", null, function () {

                                    });
                        } else {
                            CARBON.showErrorDialog("Failed to add event formatter, Exception: " + msg);
                        }
                    }
                });

}
</script>

<div id="middle">
    <div id="workArea">

        <h6><fmt:message key="title.event.formatter.create"/></h6>
        <br/>

        <form name="inputForm" method="get" id="addEventFormatter">
            <table style="width:100%" id="eventFormatterAdd" class="styledLeft">
                <% EventFormatterAdminServiceStub eventFormatterstub = CEPProcessFlowUIUtils.getEventFormatterAdminService(config, session, request);
                    String[] outputStreamNames = eventFormatterstub.getAllEventStreamNames();
                    String[] outputTransportAdaptorNames = eventFormatterstub.getOutputTransportAdaptorNames();
                    if (outputStreamNames != null && outputTransportAdaptorNames != null) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="title.event.formatter.details"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <%@include file="../eventformatter/inner_eventFormatter_ui.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" value="<fmt:message key="add.event.formatter"/>"
                               onclick="addCEPEventFormatter(document.getElementById('addEventFormatter'))"/>
                    </td>
                </tr>
                </tbody>
                <%
                } else { %>
                <thead>
                <tr>
                    <th><fmt:message key="event.formatter.nostreams.header"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <table id="noEventBuilderInputTable" class="normal-nopadding"
                               style="width:100%">
                            <tbody>

                            <tr>
                                <td width="80%">Event Streams or output
                                                Transport Adaptors are
                                                not available
                                </td>
                                </td>
                                <td width="20%"><a onclick="loadUIElements('outputAdaptor')"
                                                   style="background-image:url(images/add.gif);"
                                                   class="icon-link" href="#">
                                    Add New Output Transport Adaptor
                                </a>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                </tbody>


                <% }

                %>
            </table>
        </form>
    </div>
</div>

</fmt:bundle>
