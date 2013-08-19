<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.cep.process.flow.ui.i18n.Resources">

    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript">
        function addInputTransport(form) {

            var isFieldEmpty = false;
            var inputParameterString = "";
            var inputPropertyCount = 0;

            // all input properties, not required and required are checked
            while (document.getElementById("inputProperty_Required_" + inputPropertyCount) != null ||
                   document.getElementById("inputProperty_" + inputPropertyCount) != null) {
                // if required fields are empty
                if ((document.getElementById("inputProperty_Required_" + inputPropertyCount) != null)) {
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
                var transportAdaptorName = (document.getElementById("transportNameId").value.trim());
                var parameters = "?transportName=" + transportAdaptorName
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
                                        CARBON.showConfirmationDialog(
                                                "Input Transport Adaptor " + transportAdaptorName + " successfully added, Do you want to add another input Transport Adaptor?", function () {
                                                    loadUIElements('inputAdaptor')
                                                }, function () {
                                                    loadUIElements('builder')
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
    <h6><fmt:message key="input.transport.adaptor.create"/></h6>
    <br/>

    <table style="width:100%" id="transportAdd" class="styledLeft">
        <form name="inputForm" method="get" id="addTransport">
            <thead>
            <tr>
                <th><fmt:message key="input.transport.adaptor.details"/></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="formRaw">
                    <%@include
                            file="../inputtransportadaptormanager/inner_transport_adaptor_ui.jsp" %>
                </td>
            </tr>
            <tr>
                <td class="buttonRow">
                    <input type="button" value="<fmt:message key="add.transport.adaptor"/>"
                           onclick="addInputTransport(document.getElementById('addTransport'))"/>
                    <input type="button" value="<fmt:message key="skip"/>"
                           onclick="loadUIElements('builder')"/>
                </td>
            </tr>
            </tbody>
        </form>
    </table>

</fmt:bundle>
