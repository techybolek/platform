<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.cep.process.flow.ui.i18n.Resources">

    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript">

        function getConfigurationProperties(form) {

            var isFieldEmpty = false;
            var outputParameterString = "";
            var outputPropertyCount = 0;

            // all output properties, not required and required are checked
            while (document.getElementById("outputProperty_Required_" + outputPropertyCount) != null ||
                   document.getElementById("outputProperty_" + outputPropertyCount) != null) {
                // if required fields are empty
                if ((document.getElementById("outputProperty_Required_" + outputPropertyCount) != null)) {
                    if (document.getElementById("outputProperty_Required_" + outputPropertyCount).value.trim() == "") {
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
            }
            else {
                // create parameter string
                var selectedIndex = document.getElementById("transportTypeFilter").selectedIndex;
                var selected_text = document.getElementById("transportTypeFilter").options[selectedIndex].text;
                var transportAdaptorName = (document.getElementById("transportNameId").value.trim());
                var parameters = "?transportName=" + transportAdaptorName
                                         + "&transportType=" + selected_text;

                if (outputParameterString != "") {
                    parameters = parameters + "&outputPropertySet=" + outputParameterString;
                }

                return parameters;
            }

        }

        function addOutputTransport(form) {

            var parameters = getConfigurationProperties(form);

            if (parameters != "") {
                // ajax call for creating a transport adaptor at backend, needed parameters are appended.
                jQuery.ajax({
                                type:"POST",
                                url:"../outputtransportadaptormanager/addtest_transport_ajaxprocessor.jsp" + parameters,
                                contentType:"application/json; charset=utf-8",
                                dataType:"text",
                                data:{},
                                async:false,
                                success:function (msg) {
                                    if (msg.trim() == "true") {
                                        CARBON.showConfirmationDialog(
                                                "Output Transport Adaptor successfully added, Do you want to add another Output Transport Adaptor?", function () {
                                                    loadUIElements('outputAdaptor')
                                                }, function () {
                                                    loadUIElements('formatter')
                                                });
                                    } else {
                                        CARBON.showErrorDialog("Failed to add transport adaptor, Exception: " + msg);
                                    }
                                }
                            });
            }

        }

    </script>

    <br/>
    <h6><fmt:message key="output.transport.adaptor.create"/></h6>
    <br/>
    <table style="width:100%" id="transportAdd" class="styledLeft">
        <form name="inputForm" method="get" id="addTransport">
            <thead>
            <tr>
                <th><fmt:message key="output.transport.adaptor.details"/></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="formRaw">
                    <%@include
                            file="../outputtransportadaptormanager/inner_transport_adaptor_ui.jsp" %>
                </td>
            </tr>
            <tr>
                <td class="buttonRow">
                    <input type="button" value="<fmt:message key="add.transport.adaptor"/>"
                           onclick="addOutputTransport(document.getElementById('addTransport'))"/>
                    <input type="button" value="<fmt:message key="test.connection"/>"
                           onclick="testConnection(document.getElementById('addTransport'))"/>
                </td>
            </tr>
            </tbody>
        </form>
    </table>

</fmt:bundle>
