<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.InputTransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorConfigurationInfoDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorFileDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.ui.InputTransportAdaptorUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.input.transport.adaptor.manager.ui.i18n.Resources">

<carbon:breadcrumb
        label="transportmanager.list"
        resourceBundle="org.wso2.carbon.input.transport.adaptor.manager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<script type="text/javascript">
    var ENABLE = "enable";
    var DISABLE = "disable";
    var STAT = "statistics";
    var TRACE = "Tracing";

    function doDelete(transportName) {
        var theform = document.getElementById('deleteForm');
        theform.transportname.value = transportName;
        theform.submit();
    }

    function disableStat(transportAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'transportAdaptorName=' + transportAdaptorName + '&action=disableStat',
                   success:function (msg) {
                       handleCallback(transportAdaptorName, DISABLE, STAT);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                              ' ' + transportAdaptorName);
                   }
               });
    }

    function enableStat(transportAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'transportAdaptorName=' + transportAdaptorName + '&action=enableStat',
                   success:function (msg) {
                       handleCallback(transportAdaptorName, ENABLE, STAT);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                              ' ' + transportAdaptorName);
                   }
               });
    }

    function handleCallback(transportAdaptor, action, type) {
        var element;
        if (action == "enable") {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + transportAdaptor);
                element.style.display = "";
                element = document.getElementById("enableStat" + transportAdaptor);
                element.style.display = "none";
            } else {
                element = document.getElementById("disableTracing" + transportAdaptor);
                element.style.display = "";
                element = document.getElementById("enableTracing" + transportAdaptor);
                element.style.display = "none";
            }
        } else {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + transportAdaptor);
                element.style.display = "none";
                element = document.getElementById("enableStat" + transportAdaptor);
                element.style.display = "";
            } else {
                element = document.getElementById("disableTracing" + transportAdaptor);
                element.style.display = "none";
                element = document.getElementById("enableTracing" + transportAdaptor);
                element.style.display = "";
            }
        }
    }

    function enableTracing(transportAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'transportAdaptorName=' + transportAdaptorName + '&action=enableTracing',
                   success:function (msg) {
                       handleCallback(transportAdaptorName, ENABLE, TRACE);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                              ' ' + transportAdaptorName);
                   }
               });
    }

    function disableTracing(transportAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'transportAdaptorName=' + transportAdaptorName + '&action=disableTracing',
                   success:function (msg) {
                       handleCallback(transportAdaptorName, DISABLE, TRACE);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                              ' ' + transportAdaptorName);
                   }
               });
    }

</script>
<%
    String transportName = request.getParameter("transportname");
    int totalTransportAdaptors = 0;
    int totalNotDeployedTransportAdaptors = 0;
    if (transportName != null) {
        InputTransportAdaptorManagerAdminServiceStub stub = InputTransportAdaptorUIUtils.getInputTransportManagerAdminService(config, session, request);
        stub.undeployActiveInputTransportAdaptorConfiguration(transportName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Transport adaptor successfully deleted.');</script>
<%
    }

    InputTransportAdaptorManagerAdminServiceStub stub = InputTransportAdaptorUIUtils.getInputTransportManagerAdminService(config, session, request);
    InputTransportAdaptorConfigurationInfoDto[] transportDetailsArray = stub.getAllActiveInputTransportAdaptorConfiguration();
    if (transportDetailsArray != null) {
        totalTransportAdaptors = transportDetailsArray.length;
    }

    InputTransportAdaptorFileDto[] notDeployedTransportAdaptorConfigurationFiles = stub.getAllInactiveInputTransportAdaptorConfigurationFile();
    if (notDeployedTransportAdaptorConfigurationFiles != null) {
        totalNotDeployedTransportAdaptors = notDeployedTransportAdaptorConfigurationFiles.length;
    }

%>

<div id="middle">
<h2><fmt:message key="available.input.transport.adaptors"/></h2>
<a href="create_transportAdaptor.jsp"
   style="background-image:url(images/add.gif);"
   class="icon-link">
    Add Input Transport Adaptor
</a>
<br/> <br/>
<div id="workArea">

    <%=totalTransportAdaptors%> <fmt:message
            key="active.transport.adaptors"/> <% if (totalNotDeployedTransportAdaptors > 0) { %><a
            href="transport_adaptor_files_details.jsp"><%=totalNotDeployedTransportAdaptors%>
        <fmt:message
                key="inactive.transport.adaptors"/></a><% } else {%><%=totalNotDeployedTransportAdaptors%>
        <fmt:message key="inactive.transport.adaptors"/> <% } %>
    <br/><br/>
    <table class="styledLeft">
        <%

            if (transportDetailsArray != null) {
        %>

        <thead>
        <tr>
            <th><fmt:message key="transport.adaptor.name"/></th>
            <th><fmt:message key="transport.adaptor.type"/></th>
            <th><fmt:message key="actions"/></th>
        </tr>
        </thead>
        <tbody>
                <%
                for (InputTransportAdaptorConfigurationInfoDto transportDetails : transportDetailsArray) {
            %>
        <tr>
            <td>
                <a href="transport_details.jsp?transportName=<%=transportDetails.getTransportAdaptorName()%>&transportType=<%=transportDetails.getTransportAdaptorType()%>"><%=transportDetails.getTransportAdaptorName()%>
                </a>

            </td>
            <td><%=transportDetails.getTransportAdaptorType().trim()%>
            </td>
            <td>
                <% if (transportDetails.getEnableStats()) {%>
                <div class="inlineDiv">
                    <div id="disableStat<%= transportDetails.getTransportAdaptorName()%>">
                        <a href="#"
                           onclick="disableStat('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                    <div id="enableStat<%= transportDetails.getTransportAdaptorName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableStat('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableStat<%= transportDetails.getTransportAdaptorName()%>">
                        <a href="#"
                           onclick="enableStat('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                    <div id="disableStat<%= transportDetails.getTransportAdaptorName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableStat('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                </div>
                <% }
                    if (transportDetails.getEnableTracing()) {%>
                <div class="inlineDiv">
                    <div id="disableTracing<%= transportDetails.getTransportAdaptorName()%>">
                        <a href="#"
                           onclick="disableTracing('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                    <div id="enableTracing<%= transportDetails.getTransportAdaptorName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableTracing('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableTracing<%= transportDetails.getTransportAdaptorName()%>">
                        <a href="#"
                           onclick="enableTracing('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                    <div id="disableTracing<%= transportDetails.getTransportAdaptorName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableTracing('<%= transportDetails.getTransportAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                </div>

                <% } %>
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="doDelete('<%=transportDetails.getTransportAdaptorName()%>')"><font
                        color="#4682b4">Delete</font></a>
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="edit_transport_details.jsp?transportName=<%=transportDetails.getTransportAdaptorName()%>"><font
                        color="#4682b4">Edit</font></a>

            </td>


        </tr>
                <%
                    }

                } else{  %>

        <tbody>
        <tr>
            <td class="formRaw">
                <table id="noInputTransportAdaptorInputTable" class="normal-nopadding"
                       style="width:100%">
                    <tbody>

                    <tr>
                        <td class="leftCol-med" colspan="2"><fmt:message
                                key="empty.transport.adaptor.msg"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>


        <% }
        %>
        </tbody>
    </table>

    <div>
        <br/>

        <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                         name="transportname"
                                                                         value=""/></form>
    </div>
</div>


<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
