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

<fmt:bundle basename="org.wso2.carbon.output.transport.adaptor.manager.ui.i18n.Resources">

    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
    <%@ page
            import="org.wso2.carbon.output.transport.adaptor.manager.stub.OutputTransportAdaptorManagerAdminServiceStub" %>
    <%@ page
            import="org.wso2.carbon.output.transport.adaptor.manager.stub.types.OutputTransportAdaptorPropertiesDto" %>
    <%@ page
            import="org.wso2.carbon.output.transport.adaptor.manager.stub.types.OutputTransportAdaptorPropertyDto" %>
    <%@ page
            import="org.wso2.carbon.output.transport.adaptor.manager.ui.OutputTransportAdaptorUIUtils" %>


    <script type="text/javascript"
            src="../outputtransportadaptormanager/js/create_transport_adaptor_helper.js"></script>

    <table id="transportInputTable" class="normal-nopadding"
           style="width:100%">
        <tbody>

        <tr>
            <td class="leftCol-med"><fmt:message
                    key="transport.adaptor.name"/><span
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
            <td><fmt:message key="transport.adaptor.type"/><span
                    class="required">*</span></td>
            <td><select name="transportTypeFilter"
                        onchange="showTransportProperties('<fmt:message key="output.transport.all.properties"/>')"
                        id="transportTypeFilter">
                <%
                    OutputTransportAdaptorManagerAdminServiceStub stub = OutputTransportAdaptorUIUtils.getOutputTransportManagerAdminService(config, session, request);
                    String[] transportNames = stub.getOutputTransportAdaptorNames();
                    OutputTransportAdaptorPropertiesDto transportAdaptorPropertiesDto = null;
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
            if ((transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos()) != null) {

        %>
        <tr>
            <td colspan="2"><b>
                <fmt:message key="output.transport.all.properties"/> </b>
            </td>
        </tr>

        <%
            }

            if (firstTransportName != null) {

        %>


        <%
            //input fields for output transport adaptor properties
            if ((transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos()) != null & firstTransportName != null) {

                OutputTransportAdaptorPropertyDto[] outputTransportProperties = transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos();

                if (outputTransportProperties != null) {
                    for (int index = 0; index < outputTransportProperties.length; index++) {
        %>
        <tr>

            <td class="leftCol-med"><%=outputTransportProperties[index].getDisplayName()%>
                <%
                    String propertyId = "outputProperty_";
                    if (outputTransportProperties[index].getRequired()) {
                        propertyId = "outputProperty_Required_";

                %>
                <span class="required">*</span>
                <%
                    }
                %>
            </td>
            <%
                String type = "text";
                if (outputTransportProperties[index].getSecured()) {
                    type = "password";
                }
            %>

            <td>
                <div class=outputFields>
                    <%

                        if (outputTransportProperties[index].getOptions()[0] != null) {

                    %>

                    <select name="<%=outputTransportProperties[index].getKey()%>"
                            id="<%=propertyId%><%=index%>">

                        <%
                            for (String property : outputTransportProperties[index].getOptions()) {
                                if (property.equals(outputTransportProperties[index].getDefaultValue())) {
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
                           name="<%=outputTransportProperties[index].getKey()%>"
                           id="<%=propertyId%><%=index%>" class="initE"
                           style="width:50%"
                           value="<%= (outputTransportProperties[index].getDefaultValue()) != null ? outputTransportProperties[index].getDefaultValue() : "" %>"/>

                    <% }

                        if (outputTransportProperties[index].getHint() != null) { %>
                    <div class="sectionHelp">
                        <%=outputTransportProperties[index].getHint()%>
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
