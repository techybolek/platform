<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderPropertyDto" %>
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

    <script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>
    <script type="text/javascript" src="../eventbuilder/js/create_eventBuilder_helper.js"></script>

    <%
        EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
        String[] transportNames = stub.getInputTransportNames();
    %>

    <table id="eventBuilderInputTable" class="normal-nopadding"
           style="width:100%">
        <tbody>

        <tr>
            <td class="leftCol-med">Event Builder Name<span
                    class="required">*</span>
            </td>
            <td><input type="text" name="configName" id="eventBuilderNameId"
                       class="initE"
                       onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                       value=""
                       style="width:50%"/>

                <div class="sectionHelp">
                    <fmt:message key="event.builder.name.tooltip"/>
                </div>

            </td>
        </tr>
        <tr>
            <td colspan="2"><b><fmt:message key="event.builder.from.tooltip"/></b></td>
        </tr>
        <tr>
            <td>Input Transport Adaptor<span class="required">*</span></td>
            <td><select name="transportAdaptorNameSelect"
                        id="transportAdaptorNameSelect"
                        onchange="showMessageConfigProperties()">
                <%
                    String firstTransportName = transportNames[0];
                    for (String transportAdaptorName : transportNames) {
                %>
                <option><%=transportAdaptorName%>
                </option>
                <%
                    }
                %>
            </select>

                <div class="sectionHelp">
                    <fmt:message key="input.adaptor.select.tooltip"/>
                </div>
            </td>

        </tr>
        <tr>

            <% //Input fields for message configuration properties
                if (firstTransportName != null && !firstTransportName.isEmpty()) {
                    EventBuilderPropertyDto[] messageConfigurationProperties = stub.getMessageConfigurationProperties(firstTransportName);

                    //Need to add other types of properties also here
                    if (messageConfigurationProperties != null) {
                        for (int index = 0; index < messageConfigurationProperties.length; index++) {
            %>

            <td class="leftCol-med">
                <%=messageConfigurationProperties[index].getDisplayName()%>
                <%
                    String propertyId = "msgConfigProperty_";
                    if (messageConfigurationProperties[index].getRequired()) {
                        propertyId = "msgConfigProperty_Required_";

                %>
                <span class="required">*</span>
                <%
                    }
                %>

            </td>
            <%
                String type = "text";
                if (messageConfigurationProperties[index].getSecured()) {
                    type = "password";
                }
            %>
            <td><input type="<%=type%>"
                       name="<%=messageConfigurationProperties[index].getKey()%>"
                       id="<%=propertyId%><%=index%>" class="initE"
                       style="width:50%"
                       value="<%= (messageConfigurationProperties[index].getDefaultValue()) != null ? messageConfigurationProperties[index].getDefaultValue() : "" %>"/>
                <%
                    if (messageConfigurationProperties[index].getHint() != null) {
                %>
                <div class="sectionHelp">
                    <%=messageConfigurationProperties[index].getHint()%>
                </div>
                <%
                    }
                %>
            </td>

        </tr>
        <%
                    }
                }
            }
        %>
        <tr>
            <td colspan="2"><b><fmt:message key="event.builder.mapping.tooltip"/></b>
            </td>
        </tr>
        <tr>
            <td>Input Mapping Type<span class="required">*</span></td>
            <td><select name="inputMappingTypeSelect" id="inputMappingTypeSelect"
                        onchange="loadMappingUiElements()">
                <%
                    String[] mappingTypeNames = stub.getSupportedInputMappingTypes(firstTransportName);
                    String firstMappingTypeName = null;
                    if (mappingTypeNames != null) {
                        firstMappingTypeName = mappingTypeNames[0];
                        for (String mappingTypeName : mappingTypeNames) {
                %>
                <option><%=mappingTypeName%>
                </option>
                <%
                        }
                    }
                %>
            </select>

                <div class="sectionHelp">
                    <fmt:message key="input.mapping.type.tooltip"/>
                </div>
            </td>
        </tr>
        <tr>
            <td id="mappingUiTd" colspan="2">
                <%
                    if (firstMappingTypeName != null) {
                        if (firstMappingTypeName.equals("wso2event")) {
                %>
                <%@include file="../eventbuilder/wso2event_mapping_ui.jsp" %>
                <%
                } else if (firstMappingTypeName.equals("xml")) {
                %>
                <%@include file="../eventbuilder/xml_mapping_ui.jsp" %>
                <%
                } else if (firstMappingTypeName.equals("map")) {
                %>
                <%@include file="../eventbuilder/map_mapping_ui.jsp" %>
                <%
                } else if (firstMappingTypeName.equals("text")) {
                %>
                <%@include file="../eventbuilder/text_mapping_ui.jsp" %>
                <%
                } else if (firstMappingTypeName.equals("json")) {
                %>
                <%@include file="../eventbuilder/json_mapping_ui.jsp" %>
                <%
                        }
                    }
                %>
            </td>
        </tr>
        <tr>
            <td colspan="2"><b><fmt:message key="event.builder.to.tooltip"/></b></td>
        </tr>
        <tr>
            <td>To Stream Name<span class="required">*</span></td>
            <td><input type="text" name="toStreamName" id="toStreamName"
                       class="initE"
                       onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                       value=""
                       style="width:50%"/>

                <div class="sectionHelp">
                    <fmt:message key="to.stream.name.tooltip"/>
                </div>
            </td>

        </tr>
        <tr>
            <td>To Stream Version</td>
            <td><input type="text" name="toStreamVersion" id="toStreamVersion"
                       class="initE"
                       onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                       value=""
                       style="width:50%"/>

                <div class="sectionHelp">
                    <fmt:message key="to.stream.version.tooltip"/>
                </div>
            </td>

        </tr>
        </tbody>
    </table>
</fmt:bundle>