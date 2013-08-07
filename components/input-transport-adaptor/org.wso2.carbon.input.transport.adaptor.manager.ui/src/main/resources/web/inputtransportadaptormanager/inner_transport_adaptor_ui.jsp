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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.InputTransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.ui.InputTransportAdaptorUIUtils" %>
<fmt:bundle basename="org.wso2.carbon.input.transport.adaptor.manager.ui.i18n.Resources">


    <script type="text/javascript"
            src="../inputtransportadaptormanager/js/create_transport_adaptor_helper.js"></script>

    <table id="transportInputTable" class="normal-nopadding"
           style="width:100%">
        <tbody>

        <tr>
            <td class="leftCol-med"><fmt:message key="transport.adaptor.name"/><span
                    class="required">*</span>
            </td>
            <td><input type="text" name="transportName" id="transportNameId"
                       class="initE"
                       onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                       value=""
                       style="width:50%"/>

                <div class="sectionHelp">
                    <fmt:message key="transport.adaptor.name.help"/>
                </div>

            </td>
        </tr>
        <tr>
            <td><fmt:message key="transport.adaptor.type"/><span class="required">*</span></td>
            <td><select name="transportTypeFilter"
                        onchange="showTransportProperties('<fmt:message key="input.transport.all.properties"/>')"
                        id="transportTypeFilter">
                <%
                    InputTransportAdaptorManagerAdminServiceStub stub = InputTransportAdaptorUIUtils.getInputTransportManagerAdminService(config, session, request);
                    String[] transportNames = stub.getInputTransportAdaptorNames();
                    InputTransportAdaptorPropertiesDto transportAdaptorPropertiesDto = null;
                    String firstTransportName = null;
                    String supportedTransportAdaptorType = null;
                    if (transportNames != null) {
                        firstTransportName = transportNames[0];
                        transportAdaptorPropertiesDto = stub.getAllTransportAdaptorPropertiesDto(firstTransportName);
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
                    <fmt:message key="transport.adaptor.type.help"/>
                </div>
            </td>

        </tr>

        <%
            if ((transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos()) != null) {

        %>
        <tr>
            <td colspan="2"><b>
                <fmt:message key="input.transport.all.properties"/> </b>
            </td>
        </tr>

        <%
            }

            if (firstTransportName != null) {

        %>


        <%
            //Input fields for input transport adaptor properties
            if ((transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos()) != null & firstTransportName != null) {

                InputTransportAdaptorPropertyDto[] inputTransportProperties = transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos();

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
                <div class=inputFields>
                    <%

                        if (inputTransportProperties[index].getOptions()[0] != null) {

                    %>

                    <select name="<%=inputTransportProperties[index].getKey()%>"
                            id="<%=propertyId%><%=index%>">

                        <%
                            for (String property : inputTransportProperties[index].getOptions()) {
                                if (property.equals(inputTransportProperties[index].getDefaultValue())) {
                        %>
                        <option selected="selected"><%=property%>
                        </option>
                        <% } else { %>
                        <option><%=property%>
                        </option>
                        <% }
                        }%>
                    </select>

                    <% } else { %>
                    <input type="<%=type%>"
                           name="<%=inputTransportProperties[index].getKey()%>"
                           id="<%=propertyId%><%=index%>" class="initE"
                           style="width:50%"
                           value="<%= (inputTransportProperties[index].getDefaultValue()) != null ? inputTransportProperties[index].getDefaultValue() : "" %>"/>

                    <% }

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
            }
        %>

        </tbody>
    </table>
</fmt:bundle>