<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>

<%--
  ~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  --%>

<fmt:bundle basename="org.wso2.carbon.event.builder.ui.i18n.Resources">

<carbon:breadcrumb
        label="event.builder.list.breadcrumb"
        resourceBundle="org.wso2.carbon.event.builder.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="js/event_builders.js"></script>
<script type="text/javascript">
    var ENABLE = "enable";
    var DISABLE = "disable";
    var STAT = "statistics";
    var TRACE = "Tracing";

    function deleteEventBuilder(eventBuilderName) {
        var theform = document.getElementById('deleteForm');
        theform.eventBuilder.value = eventBuilderName;
        theform.submit();
    }

    function disableStat(eventBuilderName) {
        jQuery.ajax({
                        type: 'POST',
                        url: 'update_property_ajaxprocessor.jsp',
                        data: 'eventBuilderName=' + eventBuilderName + '&attribute=stat' + '&value=false',
                        success: function (msg) {
                            handleCallback(eventBuilderName, DISABLE, STAT);
                        },
                        error: function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                                   ' ' + eventBuilderName);
                        }
                    });
    }

    function enableStat(eventBuilderName) {
        jQuery.ajax({
                        type: 'POST',
                        url: 'update_property_ajaxprocessor.jsp',
                        data: 'eventBuilderName=' + eventBuilderName + '&attribute=stat' + '&value=true',
                        success: function (msg) {
                            handleCallback(eventBuilderName, ENABLE, STAT);
                        },
                        error: function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                                   ' ' + eventBuilderName);
                        }
                    });
    }

    function handleCallback(eventBuilderName, action, type) {
        var element;
        if (action == "enable") {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + eventBuilderName);
                element.style.display = "";
                element = document.getElementById("enableStat" + eventBuilderName);
                element.style.display = "none";
            } else {
                element = document.getElementById("disableTracing" + eventBuilderName);
                element.style.display = "";
                element = document.getElementById("enableTracing" + eventBuilderName);
                element.style.display = "none";
            }
        } else {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + eventBuilderName);
                element.style.display = "none";
                element = document.getElementById("enableStat" + eventBuilderName);
                element.style.display = "";
            } else {
                element = document.getElementById("disableTracing" + eventBuilderName);
                element.style.display = "none";
                element = document.getElementById("enableTracing" + eventBuilderName);
                element.style.display = "";
            }
        }
    }

    function enableTracing(eventBuilderName) {
        jQuery.ajax({
                        type: 'POST',
                        url: 'update_property_ajaxprocessor.jsp',
                        data: 'eventBuilderName=' + eventBuilderName + '&attribute=trace' + '&value=true',
                        success: function (msg) {
                            handleCallback(eventBuilderName, ENABLE, TRACE);
                        },
                        error: function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                                   ' ' + eventBuilderName);
                        }
                    });
    }

    function disableTracing(eventBuilderName) {
        jQuery.ajax({
                        type: 'POST',
                        url: 'update_property_ajaxprocessor.jsp',
                        data: 'eventBuilderName=' + eventBuilderName + '&attribute=trace' + '&value=false',
                        success: function (msg) {
                            handleCallback(eventBuilderName, DISABLE, TRACE);
                        },
                        error: function (msg) {
                            CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                                   ' ' + eventBuilderName);
                        }
                    });
    }

</script>
<%
    String eventBuilderName = request.getParameter("eventBuilder");
    int activeEventBuilders = 0;
    int totalNotDeployedEventBuilders = 0;
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    if (stub != null) {
        if (eventBuilderName != null) {
            stub.undeployActiveConfiguration(eventBuilderName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Event builder successfully deleted.');</script>
<%
        }

        EventBuilderConfigurationDto[] deployedEventBuilderConfigFiles = stub.getAllActiveEventBuilderConfigurations();
        if (deployedEventBuilderConfigFiles != null) {
            activeEventBuilders = deployedEventBuilderConfigFiles.length;
        }

        String[] inactiveEventBuilderConfigurations = stub.getAllInactiveEventBuilderConfigurations();
        if (inactiveEventBuilderConfigurations != null) {
            totalNotDeployedEventBuilders = inactiveEventBuilderConfigurations.length;
        }
    }

%>

<div id="middle">
<h2><fmt:message key="available.event.builders.header"/></h2>
<a href="create_eventbuilder.jsp"
   style="background-image:url(images/add.gif);"
   class="icon-link">
    Add Event Builder
</a>
<br/> <br/>

<div id="workArea">
    <%=activeEventBuilders%> <fmt:message
        key="active.event.builders.header"/> , <% if (totalNotDeployedEventBuilders > 0) { %><a
        href="inactive_event_builder_files_details.jsp"><%=totalNotDeployedEventBuilders%>
    <fmt:message
            key="inactive.event.builders.header"/></a><% } else {%><%=totalNotDeployedEventBuilders%>
    <fmt:message key="inactive.event.builders.header"/> <% } %>

    <br/> <br/>
    <table class="styledLeft">
        <%
            if (stub != null) {
                EventBuilderConfigurationDto[] eventBuilderConfigurationDtos = stub.getAllActiveEventBuilderConfigurations();
                if (eventBuilderConfigurationDtos != null && eventBuilderConfigurationDtos.length > 0) {

        %>

        <thead>
        <tr>
            <th><fmt:message key="event.builder.name.header"/></th>
            <th><fmt:message key="event.builder.type.header"/></th>
            <th><fmt:message key="transport.adaptor.name.header"/></th>
            <th><fmt:message key="event.builder.stream.header"/></th>
            <th width="30%"><fmt:message key="actions.header"/></th>
        </tr>
        </thead>
        <tbody>
        <%
            for (EventBuilderConfigurationDto eventBuilderConfigurationDto : eventBuilderConfigurationDtos) {

        %>
        <tr>
            <td>
                <a href="eventbuilder_details.jsp?eventBuilderName=<%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                    <%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>
                </a>
            </td>
            <td><%=eventBuilderConfigurationDto.getInputMappingType()%>
            </td>
            <td><%=eventBuilderConfigurationDto.getInputTransportAdaptorName()%>
            </td>
            <td><%=eventBuilderConfigurationDto.getToStreamName() + ":" + eventBuilderConfigurationDto.getToStreamVersion()%>
            </td>
            <td>
                <% if (eventBuilderConfigurationDto.getStatisticsEnabled()) {%>
                <div class="inlineDiv">
                    <div id="disableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="disableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                    <div id="enableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="enableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                    <div id="disableStat<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableStat('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                </div>
                <% }
                    if (eventBuilderConfigurationDto.getTraceEnabled()) {%>
                <div class="inlineDiv">
                    <div id="disableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="disableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                    <div id="enableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>">
                        <a href="#"
                           onclick="enableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                    <div id="disableTracing<%= eventBuilderConfigurationDto.getEventBuilderConfigName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableTracing('<%= eventBuilderConfigurationDto.getEventBuilderConfigName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                </div>

                <% } %>
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="deleteEventBuilder('<%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>')"><font
                        color="#4682b4">Delete</font></a>
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="edit_event_builder_details.jsp?eventBuilderName=<%=eventBuilderConfigurationDto.getEventBuilderConfigName()%>"><font
                        color="#4682b4">Edit</font></a>
            </td>

        </tr>
        </tbody>

        <%
            }
        } else {
        %>

        <tbody>
        <tr>
            <td class="formRaw">
                <table id="noEventBuilderInputTable" class="normal-nopadding"
                       style="width:100%">
                    <tbody>

                    <tr>
                        <td class="leftCol-med" colspan="2"><fmt:message
                                key="event.builder.noeb.msg"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>

        <%
                }
            }
        %>
    </table>

</div>
<div>
    <br/>

    <form id="deleteForm" name="input" action="" method="get"><input type="hidden"
                                                                     name="eventBuilder"
                                                                     value=""/></form>
</div>


<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
