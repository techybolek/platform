<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.TransportAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorPropertiesDto" %>
<%@ page import="org.wso2.carbon.transport.adaptor.manager.stub.types.TransportAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.transport.adaptor.manager.ui.UIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="transportmanager.details"
            resourceBundle="org.wso2.carbon.transport.adaptor.manager.ui.i18n.Resources"
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
                        TransportAdaptorManagerAdminServiceStub stub = UIUtils.getTransportManagerAdminService(config, session, request);


                        TransportAdaptorPropertiesDto transportAdaptorPropertiesDto = stub.getTransportAdaptorConfigurationDetails(transportName);
                        TransportAdaptorPropertyDto[] inputTransportProperties = transportAdaptorPropertiesDto.getInputTransportAdaptorPropertyDtos();
                        TransportAdaptorPropertyDto[] outputTransportProperties = transportAdaptorPropertiesDto.getOutputTransportAdaptorPropertyDtos();
                        TransportAdaptorPropertyDto[] commonTransportProperties = transportAdaptorPropertiesDto.getCommonTransportAdaptorPropertyDtos();


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
                    if (commonTransportProperties != null) {
                        for (TransportAdaptorPropertyDto transportAdaptorPropertyDto : commonTransportProperties) {

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

                    if (inputTransportProperties != null) {
                        for (TransportAdaptorPropertyDto transportAdaptorPropertyDto : inputTransportProperties) {

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

                    if (outputTransportProperties != null) {
                        for (TransportAdaptorPropertyDto transportAdaptorPropertyDto : outputTransportProperties) {

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