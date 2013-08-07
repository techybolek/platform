<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.InputTransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.stub.types.InputTransportAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.input.transport.adaptor.manager.ui.InputTransportAdaptorUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.input.transport.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="transportmanager.details"
            resourceBundle="org.wso2.carbon.input.transport.adaptor.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <div id="middle">
        <h2>Transport Adaptor Details</h2>

        <div id="workArea">
            <table id="transportInputTable" class="styledLeft"
                   style="width:100%">
                <tbody>
                <%
                    String transportName = request.getParameter("transportName");
                    String transportType = request.getParameter("transportType");
                    if (transportName != null) {
                        InputTransportAdaptorManagerAdminServiceStub stub = InputTransportAdaptorUIUtils.getInputTransportManagerAdminService(config, session, request);


                        InputTransportAdaptorPropertiesDto transportAdaptorPropertiesDto = stub.getInputTransportAdaptorConfigurationDetails(transportName);
                        InputTransportAdaptorPropertyDto[] inputTransportProperties = transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos();


                %>
                <tr>
                    <td class="leftCol-small">Transport Adaptor Name</td>
                    <td><input type="text" name="transportName" id="transportNameId"
                               value=" <%=transportName%>"
                               disabled="true"
                               style="width:50%"/></td>

                    </td>
                </tr>
                <tr>
                    <td>Transport Adaptor Type</td>
                    <td><select name="transportTypeFilter"
                                disabled="true">
                        <option><%=transportType%>
                        </option>
                    </select>
                    </td>
                </tr>
                <%

                    if (inputTransportProperties != null) {
                        for (InputTransportAdaptorPropertyDto transportAdaptorPropertyDto : inputTransportProperties) {

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