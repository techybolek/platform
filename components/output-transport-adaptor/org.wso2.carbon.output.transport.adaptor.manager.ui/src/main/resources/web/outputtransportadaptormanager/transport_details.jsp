<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.stub.OutputTransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.stub.types.OutputTransportAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.stub.types.OutputTransportAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.output.transport.adaptor.manager.ui.OutputTransportAdaptorUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.output.transport.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="transportmanager.details"
            resourceBundle="org.wso2.carbon.output.transport.adaptor.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <div id="middle">
        <h2><fmt:message key="output.transport.adaptor.details"/></h2>

        <div id="workArea">
            <table id="transportInputTable" class="styledLeft"
                   style="width:100%">
                <tbody>
                <%
                    String transportName = request.getParameter("transportName");
                    String transportType = request.getParameter("transportType");
                    if (transportName != null) {
                        OutputTransportAdaptorManagerAdminServiceStub stub = OutputTransportAdaptorUIUtils.getOutputTransportManagerAdminService(config, session, request);


                        OutputTransportAdaptorPropertiesDto transportAdaptorPropertiesDto = stub.getOutputTransportAdaptorConfigurationDetails(transportName);
                        OutputTransportAdaptorPropertyDto[] inputTransportProperties = transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos();


                %>
                <tr>
                    <td class="leftCol-small"><fmt:message key="transport.adaptor.name"/></td>
                    <td><input type="text" name="transportName" id="transportNameId"
                               value=" <%=transportName%>"
                               disabled="true"
                               style="width:50%"/></td>

                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.adaptor.type"/></td>
                    <td><select name="transportTypeFilter"
                                disabled="true">
                        <option><%=transportType%>
                        </option>
                    </select>
                    </td>
                </tr>
                <%

                    if (inputTransportProperties != null) {
                        for (OutputTransportAdaptorPropertyDto transportAdaptorPropertyDto : inputTransportProperties) {

                %>

                <tr>
                    <td><%=transportAdaptorPropertyDto.getDisplayName()%>
                    </td>
                    <%
                        if (!transportAdaptorPropertyDto.getSecured()) {
                    %>
                    <td><input type="input" value="<%=transportAdaptorPropertyDto.getValue()%>"
                               disabled="true"
                               style="width:50%"/>
                    </td>
                    <%
                    } else { %>
                    <td><input type="password" value="<%=transportAdaptorPropertyDto.getValue()%>"
                               disabled="true"
                               style="width:50%"/>
                    </td>
                    <%
                        }
                    %>
                </tr>
                <%

                            }
                        }
                    }

                %>

                </tbody>
            </table>


        </div>
    </div>
</fmt:bundle>